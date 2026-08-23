package com.pulse.service

import com.pulse.domain.Project
import com.pulse.domain.ProjectStatus
import com.pulse.domain.TaskStatus
import com.pulse.dto.CreateProjectRequest
import com.pulse.dto.ProjectResponse
import com.pulse.exception.ApiException
import com.pulse.repository.ProjectRepository
import com.pulse.repository.TaskItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val taskItemRepository: TaskItemRepository,
    private val workspaceService: WorkspaceService,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun create(workspaceId: UUID, actorId: UUID, request: CreateProjectRequest): ProjectResponse {
        workspaceService.requireMembership(workspaceId, actorId)
        val project = Project(
            workspaceId = workspaceId, teamId = request.teamId, name = request.name,
            description = request.description, createdBy = actorId
        )
        projectRepository.save(project)
        auditLogService.log(workspaceId, actorId, "PROJECT_CREATED", "PROJECT", project.id)
        return toResponse(project)
    }

    fun list(workspaceId: UUID): List<ProjectResponse> =
        projectRepository.findAllByWorkspaceId(workspaceId).map { toResponse(it) }

    fun get(projectId: UUID): Project =
        projectRepository.findById(projectId).orElseThrow { ApiException(404, "Project not found") }

    @Transactional
    fun updateStatus(projectId: UUID, actorId: UUID, status: ProjectStatus): ProjectResponse {
        val project = get(projectId)
        project.status = status
        projectRepository.save(project)
        auditLogService.log(project.workspaceId, actorId, "PROJECT_STATUS_CHANGED", "PROJECT", project.id)
        return toResponse(project)
    }

    private fun toResponse(project: Project): ProjectResponse {
        val tasks = taskItemRepository.findAllByProjectIdOrderByPositionAsc(project.id!!)
        return ProjectResponse(
            id = project.id!!, name = project.name, description = project.description,
            status = project.status.name, taskCount = tasks.size,
            completedCount = tasks.count { it.status == TaskStatus.DONE }
        )
    }
}
