package com.pulse.controller

import com.pulse.dto.*
import com.pulse.service.DocumentService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/documents")
class DocumentController(private val documentService: DocumentService) {

    @GetMapping
    fun listTopLevel(@PathVariable workspaceId: UUID): List<DocumentResponse> = documentService.listTopLevel(workspaceId)

    @PostMapping
    fun create(@PathVariable workspaceId: UUID, @Valid @RequestBody request: CreateDocumentRequest): DocumentResponse =
        documentService.create(workspaceId, currentUserId(), request)

    @GetMapping("/search")
    fun search(@PathVariable workspaceId: UUID, @RequestParam q: String): List<DocumentResponse> =
        documentService.search(workspaceId, q)

    @GetMapping("/{documentId}")
    fun get(@PathVariable workspaceId: UUID, @PathVariable documentId: UUID): DocumentResponse = documentService.get(documentId)

    @GetMapping("/{documentId}/children")
    fun children(@PathVariable workspaceId: UUID, @PathVariable documentId: UUID): List<DocumentResponse> =
        documentService.listChildren(documentId)

    @PutMapping("/{documentId}/content")
    fun updateContent(
        @PathVariable workspaceId: UUID,
        @PathVariable documentId: UUID,
        @Valid @RequestBody request: UpdateDocumentContentRequest
    ): DocumentResponse = documentService.updateContent(documentId, currentUserId(), request)

    @GetMapping("/{documentId}/versions")
    fun versions(@PathVariable workspaceId: UUID, @PathVariable documentId: UUID) = documentService.versions(documentId)

    @PostMapping("/{documentId}/versions/{versionId}/restore")
    fun restore(@PathVariable workspaceId: UUID, @PathVariable documentId: UUID, @PathVariable versionId: UUID): DocumentResponse =
        documentService.restoreVersion(documentId, versionId, currentUserId())

    @PostMapping("/{documentId}/favorite")
    fun toggleFavorite(@PathVariable workspaceId: UUID, @PathVariable documentId: UUID): DocumentResponse =
        documentService.toggleFavorite(documentId)
}
