package com.pulse.controller

import com.pulse.dto.*
import com.pulse.service.TaskService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class TaskController(private val taskService: TaskService) {

    @GetMapping("/api/v1/projects/{projectId}/tasks")
    fun list(@PathVariable projectId: UUID): List<TaskResponse> = taskService.listByProject(projectId)

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    fun create(@PathVariable projectId: UUID, @Valid @RequestBody request: CreateTaskRequest): TaskResponse =
        taskService.create(projectId, currentUserId(), request)

    @GetMapping("/api/v1/tasks/mine")
    fun mine(): List<TaskResponse> = taskService.listMine(currentUserId())

    @PatchMapping("/api/v1/tasks/{taskId}")
    fun update(@PathVariable taskId: UUID, @RequestBody request: UpdateTaskRequest): TaskResponse =
        taskService.update(taskId, currentUserId(), request)

    @PostMapping("/api/v1/tasks/{taskId}/move")
    fun move(@PathVariable taskId: UUID, @Valid @RequestBody request: MoveTaskRequest): TaskResponse =
        taskService.move(taskId, currentUserId(), request)

    @PostMapping("/api/v1/tasks/{taskId}/comments")
    fun addComment(@PathVariable taskId: UUID, @Valid @RequestBody request: AddTaskCommentRequest) {
        taskService.addComment(taskId, currentUserId(), request)
    }
}
