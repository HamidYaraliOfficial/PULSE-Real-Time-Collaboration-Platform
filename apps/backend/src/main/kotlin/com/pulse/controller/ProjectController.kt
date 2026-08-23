package com.pulse.controller

import com.pulse.dto.CreateProjectRequest
import com.pulse.dto.ProjectResponse
import com.pulse.service.ProjectService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/projects")
class ProjectController(private val projectService: ProjectService) {

    @GetMapping
    fun list(@PathVariable workspaceId: UUID): List<ProjectResponse> = projectService.list(workspaceId)

    @PostMapping
    fun create(@PathVariable workspaceId: UUID, @Valid @RequestBody request: CreateProjectRequest): ProjectResponse =
        projectService.create(workspaceId, currentUserId(), request)

    @PatchMapping("/{projectId}/status")
    fun updateStatus(@PathVariable workspaceId: UUID, @PathVariable projectId: UUID, @RequestBody body: Map<String, String>): ProjectResponse =
        projectService.updateStatus(projectId, currentUserId(), com.pulse.domain.ProjectStatus.valueOf(body["status"]!!))
}
