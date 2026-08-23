package com.pulse.service

import com.pulse.domain.NotificationType
import com.pulse.domain.TaskItem
import com.pulse.domain.TaskPriority
import com.pulse.domain.TaskStatus
import com.pulse.dto.*
import com.pulse.exception.ApiException
import com.pulse.repository.TaskCommentRepository
import com.pulse.repository.TaskItemRepository
import com.pulse.repository.UserRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TaskService(
    private val taskItemRepository: TaskItemRepository,
    private val taskCommentRepository: TaskCommentRepository,
    private val userRepository: UserRepository,
    private val projectService: ProjectService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val notificationService: NotificationService,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun create(projectId: UUID, reporterId: UUID, request: CreateTaskRequest): TaskResponse {
        val project = projectService.get(projectId)
        val maxPosition = taskItemRepository.findAllByProjectIdOrderByPositionAsc(projectId)
            .filter { it.status == TaskStatus.valueOf(request.status) }
            .maxOfOrNull { it.position } ?: -1

        val task = TaskItem(
            projectId = projectId, title = request.title, description = request.description,
            status = TaskStatus.valueOf(request.status), priority = TaskPriority.valueOf(request.priority),
            assigneeId = request.assigneeId, reporterId = reporterId, dueDate = request.dueDate,
            position = maxPosition + 1, labels = request.labels.toTypedArray(), parentTaskId = request.parentTaskId
        )
        taskItemRepository.save(task)

        if (request.assigneeId != null && request.assigneeId != reporterId) {
            notificationService.create(request.assigneeId, NotificationType.ASSIGNMENT, "New task assigned: ${task.title}", task.description, "/kanban/$projectId")
        }
        auditLogService.log(project.workspaceId, reporterId, "TASK_CREATED", "TASK", task.id)
        broadcast(project.workspaceId, task)
        return toResponse(task)
    }

    fun listByProject(projectId: UUID): List<TaskResponse> =
        taskItemRepository.findAllByProjectIdOrderByPositionAsc(projectId).map { toResponse(it) }

    fun listMine(userId: UUID): List<TaskResponse> =
        taskItemRepository.findAllByAssigneeId(userId)
            .filter { it.status != TaskStatus.DONE }
            .map { toResponse(it) }

    @Transactional
    fun update(taskId: UUID, actorId: UUID, request: UpdateTaskRequest): TaskResponse {
        val task = get(taskId)
        request.title?.let { task.title = it }
        request.description?.let { task.description = it }
        request.status?.let { task.status = TaskStatus.valueOf(it) }
        request.priority?.let { task.priority = TaskPriority.valueOf(it) }
        request.dueDate?.let { task.dueDate = it }
        request.labels?.let { task.labels = it.toTypedArray() }
        if (request.assigneeId != null && request.assigneeId != task.assigneeId) {
            task.assigneeId = request.assigneeId
            notificationService.create(request.assigneeId, NotificationType.ASSIGNMENT, "Task assigned: ${task.title}", task.description, null)
        }
        task.updatedAt = Instant.now()
        taskItemRepository.save(task)
        val project = projectService.get(task.projectId)
        broadcast(project.workspaceId, task)
        return toResponse(task)
    }

    /** Drag & drop between Kanban columns: updates status + position, then live-broadcasts to the board. */
    @Transactional
    fun move(taskId: UUID, actorId: UUID, request: MoveTaskRequest): TaskResponse {
        val task = get(taskId)
        val newStatus = TaskStatus.valueOf(request.status)
        val siblings = taskItemRepository.findAllByProjectIdOrderByPositionAsc(task.projectId)
            .filter { it.status == newStatus && it.id != taskId }
            .sortedBy { it.position }
            .toMutableList()

        val insertAt = request.position.coerceIn(0, siblings.size)
        siblings.add(insertAt, task)
        siblings.forEachIndexed { index, t ->
            t.status = newStatus
            t.position = index
            taskItemRepository.save(t)
        }
        val project = projectService.get(task.projectId)
        broadcast(project.workspaceId, task)
        return toResponse(task)
    }

    @Transactional
    fun addComment(taskId: UUID, authorId: UUID, request: AddTaskCommentRequest) {
        val task = get(taskId)
        taskCommentRepository.save(com.pulse.domain.TaskComment(taskId = taskId, authorId = authorId, body = request.body))
        if (task.assigneeId != null && task.assigneeId != authorId) {
            notificationService.create(task.assigneeId!!, NotificationType.COMMENT, "New comment on ${task.title}", request.body.take(140), null)
        }
    }

    fun get(taskId: UUID): TaskItem = taskItemRepository.findById(taskId).orElseThrow { ApiException(404, "Task not found") }

    private fun broadcast(workspaceId: UUID, task: TaskItem) {
        messagingTemplate.convertAndSend("/topic/kanban.${task.projectId}", toResponse(task))
    }

    private fun toResponse(task: TaskItem): TaskResponse {
        val assignee = task.assigneeId?.let { userRepository.findById(it).orElse(null) }
        return TaskResponse(
            id = task.id!!, projectId = task.projectId, title = task.title, description = task.description,
            status = task.status.name, priority = task.priority.name, assigneeId = task.assigneeId,
            assigneeName = assignee?.displayName, reporterId = task.reporterId, dueDate = task.dueDate,
            position = task.position, labels = task.labels.toList(),
            commentCount = taskCommentRepository.findAllByTaskIdOrderByCreatedAtAsc(task.id!!).size,
            createdAt = task.createdAt
        )
    }
}
