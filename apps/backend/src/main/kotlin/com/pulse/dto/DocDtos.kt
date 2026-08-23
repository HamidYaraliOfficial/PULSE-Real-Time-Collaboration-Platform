package com.pulse.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateDocumentRequest(
    @field:NotBlank val title: String,
    val parentId: UUID? = null,
    val icon: String? = null
)

data class UpdateDocumentContentRequest(
    @field:NotBlank val content: String, // JSON string of block content
    val title: String? = null
)

data class DocumentResponse(
    val id: UUID,
    val title: String,
    val content: String,
    val icon: String?,
    val isFavorite: Boolean,
    val parentId: UUID?,
    val hasChildren: Boolean,
    val updatedAt: Instant,
    val createdBy: UUID
)

data class DocumentCollabEvent(
    val documentId: UUID,
    val userId: UUID,
    val displayName: String,
    val type: String, // "CURSOR" | "CONTENT" | "PRESENCE_JOIN" | "PRESENCE_LEAVE"
    val cursorPosition: String? = null,
    val content: String? = null
)

data class AddDocumentCommentRequest(
    @field:NotBlank val body: String,
    val blockId: String? = null
)
