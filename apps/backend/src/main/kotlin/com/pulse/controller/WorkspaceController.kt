package com.pulse.controller

import com.pulse.domain.Team
import com.pulse.dto.*
import com.pulse.service.WorkspaceService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces")
class WorkspaceController(private val workspaceService: WorkspaceService) {

    @GetMapping
    fun list(): List<WorkspaceResponse> = workspaceService.listForUser(currentUserId())

    @PostMapping
    fun create(@RequestParam organizationId: UUID, @Valid @RequestBody request: CreateWorkspaceRequest): WorkspaceResponse =
        workspaceService.create(currentUserId(), organizationId, request)

    @GetMapping("/{workspaceId}/members")
    fun members(@PathVariable workspaceId: UUID): List<WorkspaceMemberResponse> = workspaceService.listMembers(workspaceId)

    @PostMapping("/{workspaceId}/members/invite")
    fun invite(@PathVariable workspaceId: UUID, @Valid @RequestBody request: InviteMemberRequest): WorkspaceMemberResponse =
        workspaceService.invite(workspaceId, currentUserId(), request)

    @PostMapping("/{workspaceId}/teams")
    fun createTeam(@PathVariable workspaceId: UUID, @Valid @RequestBody request: CreateTeamRequest): Team =
        workspaceService.createTeam(workspaceId, currentUserId(), request)

    @GetMapping("/{workspaceId}/teams")
    fun listTeams(@PathVariable workspaceId: UUID): List<Team> = workspaceService.listTeams(workspaceId)
}
