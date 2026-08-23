package com.pulse.service

import com.pulse.domain.Team
import com.pulse.domain.Workspace
import com.pulse.domain.WorkspaceMember
import com.pulse.domain.WorkspaceRole
import com.pulse.dto.*
import com.pulse.exception.ApiException
import com.pulse.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val auditLogService: AuditLogService
) {

    fun listForUser(userId: UUID): List<WorkspaceResponse> {
        val memberships = workspaceMemberRepository.findAllByUserId(userId)
        return memberships.mapNotNull { m ->
            val ws = workspaceRepository.findById(m.workspaceId).orElse(null) ?: return@mapNotNull null
            toResponse(ws, m.role)
        }
    }

    @Transactional
    fun create(userId: UUID, organizationId: UUID, request: CreateWorkspaceRequest): WorkspaceResponse {
        val org = organizationRepository.findById(organizationId).orElseThrow { ApiException(404, "Organization not found") }
        if (org.ownerId != userId) throw ApiException(403, "Only the organization owner can create workspaces")

        val workspace = Workspace(
            organizationId = org.id!!,
            name = request.name,
            slug = "ws-${UUID.randomUUID().toString().take(8)}",
            description = request.description,
            timezone = request.timezone
        )
        workspaceRepository.save(workspace)
        workspaceMemberRepository.save(WorkspaceMember(workspaceId = workspace.id!!, userId = userId, role = WorkspaceRole.OWNER))
        auditLogService.log(workspace.id, userId, "WORKSPACE_CREATED", "WORKSPACE", workspace.id)
        return toResponse(workspace, WorkspaceRole.OWNER)
    }

    fun requireMembership(workspaceId: UUID, userId: UUID): WorkspaceMember =
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
            ?: throw ApiException(403, "You are not a member of this workspace")

    fun requireRole(workspaceId: UUID, userId: UUID, allowed: Set<WorkspaceRole>): WorkspaceMember {
        val member = requireMembership(workspaceId, userId)
        if (member.role !in allowed) throw ApiException(403, "Insufficient permissions for this action")
        return member
    }

    @Transactional
    fun invite(workspaceId: UUID, actorId: UUID, request: InviteMemberRequest): WorkspaceMemberResponse {
        requireRole(workspaceId, actorId, setOf(WorkspaceRole.OWNER, WorkspaceRole.ADMIN))
        val user = userRepository.findByEmail(request.email)
            ?: throw ApiException(404, "No PULSE account exists yet for this email. Ask them to sign up first.")
        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.id!!) != null) {
            throw ApiException(409, "User is already a member of this workspace")
        }
        val member = workspaceMemberRepository.save(
            WorkspaceMember(workspaceId = workspaceId, userId = user.id!!, role = WorkspaceRole.valueOf(request.role))
        )
        auditLogService.log(workspaceId, actorId, "MEMBER_INVITED", "WORKSPACE_MEMBER", member.id)
        return WorkspaceMemberResponse(
            userId = user.id!!, displayName = user.displayName, avatarUrl = user.avatarUrl,
            role = member.role.name, presenceStatus = user.presenceStatus.name, title = user.title
        )
    }

    fun listMembers(workspaceId: UUID): List<WorkspaceMemberResponse> =
        workspaceMemberRepository.findAllByWorkspaceId(workspaceId).mapNotNull { m ->
            val user = userRepository.findById(m.userId).orElse(null) ?: return@mapNotNull null
            WorkspaceMemberResponse(
                userId = user.id!!, displayName = user.displayName, avatarUrl = user.avatarUrl,
                role = m.role.name, presenceStatus = user.presenceStatus.name, title = user.title
            )
        }

    @Transactional
    fun createTeam(workspaceId: UUID, actorId: UUID, request: CreateTeamRequest): Team {
        requireMembership(workspaceId, actorId)
        val team = Team(workspaceId = workspaceId, name = request.name, description = request.description)
        teamRepository.save(team)
        auditLogService.log(workspaceId, actorId, "TEAM_CREATED", "TEAM", team.id)
        return team
    }

    fun listTeams(workspaceId: UUID): List<Team> = teamRepository.findAllByWorkspaceId(workspaceId)

    private fun toResponse(ws: Workspace, role: WorkspaceRole): WorkspaceResponse = WorkspaceResponse(
        id = ws.id!!, name = ws.name, slug = ws.slug, description = ws.description,
        logoUrl = ws.logoUrl, timezone = ws.timezone, role = role.name,
        memberCount = workspaceMemberRepository.findAllByWorkspaceId(ws.id!!).size
    )
}
