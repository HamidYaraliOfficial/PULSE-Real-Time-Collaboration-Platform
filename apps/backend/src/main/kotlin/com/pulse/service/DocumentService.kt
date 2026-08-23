package com.pulse.service

import com.pulse.domain.DocumentEntity
import com.pulse.domain.DocumentVersion
import com.pulse.dto.CreateDocumentRequest
import com.pulse.dto.DocumentCollabEvent
import com.pulse.dto.DocumentResponse
import com.pulse.dto.UpdateDocumentContentRequest
import com.pulse.exception.ApiException
import com.pulse.repository.DocumentRepository
import com.pulse.repository.DocumentVersionRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Real-time collaborative editing.
 *
 * Each edit is broadcast immediately to every connected client on
 * /topic/document.{id} so open tabs update live (this is the real-time
 * sync layer). To keep this scaffold's persistence model simple and
 * dependency-free, saved state currently uses last-write-wins with a
 * full version snapshot on every save (see DocumentVersion / "Version
 * History" + "Restore Version"). For true multi-user conflict-free
 * merging at scale, swap the content payload for CRDT updates (e.g.
 * Yjs / Automerge) - clients would exchange binary CRDT update frames
 * over this same WebSocket topic instead of whole-document JSON, and
 * this service would persist the CRDT doc state instead of raw JSON.
 * That swap does not require changing the REST/WS surface below.
 */
@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val documentVersionRepository: DocumentVersionRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun create(workspaceId: UUID, actorId: UUID, request: CreateDocumentRequest): DocumentResponse {
        val doc = DocumentEntity(
            workspaceId = workspaceId, parentId = request.parentId, title = request.title,
            icon = request.icon, createdBy = actorId
        )
        documentRepository.save(doc)
        auditLogService.log(workspaceId, actorId, "DOCUMENT_CREATED", "DOCUMENT", doc.id)
        return toResponse(doc)
    }

    fun listTopLevel(workspaceId: UUID): List<DocumentResponse> =
        documentRepository.findAllByWorkspaceIdAndParentIdIsNull(workspaceId).map { toResponse(it) }

    fun listChildren(parentId: UUID): List<DocumentResponse> =
        documentRepository.findAllByParentId(parentId).map { toResponse(it) }

    fun get(documentId: UUID): DocumentResponse = toResponse(find(documentId))

    fun search(workspaceId: UUID, query: String): List<DocumentResponse> =
        documentRepository.searchByTitle(workspaceId, query).map { toResponse(it) }

    @Transactional
    fun updateContent(documentId: UUID, actorId: UUID, request: UpdateDocumentContentRequest): DocumentResponse {
        val doc = find(documentId)
        request.title?.let { doc.title = it }
        doc.content = request.content
        doc.updatedAt = Instant.now()
        documentRepository.save(doc)

        // Auto-save creates a version snapshot so "Restore Version" always has history to roll back to.
        documentVersionRepository.save(DocumentVersion(documentId = documentId, content = request.content, authorId = actorId))

        messagingTemplate.convertAndSend(
            "/topic/document.$documentId",
            DocumentCollabEvent(documentId = documentId, userId = actorId, displayName = "", type = "CONTENT", content = request.content)
        )
        return toResponse(doc)
    }

    fun versions(documentId: UUID) = documentVersionRepository.findAllByDocumentIdOrderByCreatedAtDesc(documentId)

    @Transactional
    fun restoreVersion(documentId: UUID, versionId: UUID, actorId: UUID): DocumentResponse {
        val version = documentVersionRepository.findById(versionId).orElseThrow { ApiException(404, "Version not found") }
        return updateContent(documentId, actorId, UpdateDocumentContentRequest(content = version.content))
    }

    @Transactional
    fun toggleFavorite(documentId: UUID): DocumentResponse {
        val doc = find(documentId)
        doc.isFavorite = !doc.isFavorite
        documentRepository.save(doc)
        return toResponse(doc)
    }

    fun broadcastCursor(event: DocumentCollabEvent) {
        messagingTemplate.convertAndSend("/topic/document.${event.documentId}", event)
    }

    private fun find(documentId: UUID): DocumentEntity =
        documentRepository.findById(documentId).orElseThrow { ApiException(404, "Document not found") }

    private fun toResponse(doc: DocumentEntity): DocumentResponse = DocumentResponse(
        id = doc.id!!, title = doc.title, content = doc.content, icon = doc.icon, isFavorite = doc.isFavorite,
        parentId = doc.parentId, hasChildren = documentRepository.findAllByParentId(doc.id!!).isNotEmpty(),
        updatedAt = doc.updatedAt, createdBy = doc.createdBy
    )
}
