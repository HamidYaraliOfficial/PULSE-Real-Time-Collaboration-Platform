package com.pulse.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateProjectRequest(
    @field:NotBlank val name: String,
    val description: String? = null,
    val teamId: UUID? = null
)

data class ProjectResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val status: String,
    val taskCount: Int,
    val completedCount: Int
)

data class CreateTaskRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    val status: String = "BACKLOG",
    val priority: String = "MEDIUM",
    val assigneeId: UUID? = null,
    val dueDate: Instant? = null,
    val labels: List<String> = emptyList(),
    val parentTaskId: UUID? = null
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val assigneeId: UUID? = null,
    val dueDate: Instant? = null,
    val labels: List<String>? = null
)

data class MoveTaskRequest(
    @field:NotBlank val status: String,
    val position: Int
)

data class TaskResponse(
    val id: UUID,
    val projectId: UUID,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val assigneeId: UUID?,
    val assigneeName: String?,
    val reporterId: UUID,
    val dueDate: Instant?,
    val position: Int,
    val labels: List<String>,
    val commentCount: Int,
    val createdAt: Instant
)

data class AddTaskCommentRequest(@field:NotBlank val body: String)
