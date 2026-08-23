package com.pulse.repository

import com.pulse.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun findAllByUserIdAndRevokedFalse(userId: UUID): List<RefreshToken>
}

interface OrganizationRepository : JpaRepository<Organization, UUID> {
    fun findBySlug(slug: String): Organization?
}

interface WorkspaceRepository : JpaRepository<Workspace, UUID> {
    fun findAllByOrganizationId(organizationId: UUID): List<Workspace>
}

interface WorkspaceMemberRepository : JpaRepository<WorkspaceMember, UUID> {
    fun findAllByUserId(userId: UUID): List<WorkspaceMember>
    fun findAllByWorkspaceId(workspaceId: UUID): List<WorkspaceMember>
    fun findByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): WorkspaceMember?
}

interface TeamRepository : JpaRepository<Team, UUID> {
    fun findAllByWorkspaceId(workspaceId: UUID): List<Team>
}

interface TeamMemberRepository : JpaRepository<TeamMember, UUID> {
    fun findAllByTeamId(teamId: UUID): List<TeamMember>
    fun findAllByUserId(userId: UUID): List<TeamMember>
}

interface BusinessHoursRepository : JpaRepository<BusinessHours, UUID> {
    fun findAllByWorkspaceIdOrderByDayOfWeekAsc(workspaceId: UUID): List<BusinessHours>
    fun findByWorkspaceIdAndDayOfWeek(workspaceId: UUID, dayOfWeek: Int): BusinessHours?
}

interface ChannelRepository : JpaRepository<Channel, UUID> {
    fun findAllByWorkspaceIdAndIsArchivedFalse(workspaceId: UUID): List<Channel>
}

interface ChannelMemberRepository : JpaRepository<ChannelMember, UUID> {
    fun findAllByChannelId(channelId: UUID): List<ChannelMember>
    fun findAllByUserId(userId: UUID): List<ChannelMember>
    fun findByChannelIdAndUserId(channelId: UUID, userId: UUID): ChannelMember?
}

interface MessageRepository : JpaRepository<Message, UUID> {
    fun findAllByChannelIdAndIsDeletedFalseOrderByCreatedAtDesc(channelId: UUID): List<Message>
    fun findAllByParentMessageIdOrderByCreatedAtAsc(parentMessageId: UUID): List<Message>

    @Query(
        "select m from Message m where m.channelId = :channelId and m.isDeleted = false " +
            "and lower(m.body) like lower(concat('%', :query, '%')) order by m.createdAt desc"
    )
    fun searchInChannel(@Param("channelId") channelId: UUID, @Param("query") query: String): List<Message>
}

interface ReactionRepository : JpaRepository<Reaction, UUID> {
    fun findAllByMessageId(messageId: UUID): List<Reaction>
    fun deleteByMessageIdAndUserIdAndEmoji(messageId: UUID, userId: UUID, emoji: String)
}

interface MessageAttachmentRepository : JpaRepository<MessageAttachment, UUID> {
    fun findAllByMessageId(messageId: UUID): List<MessageAttachment>
}

interface ProjectRepository : JpaRepository<Project, UUID> {
    fun findAllByWorkspaceId(workspaceId: UUID): List<Project>
}

interface TaskItemRepository : JpaRepository<TaskItem, UUID> {
    fun findAllByProjectIdOrderByPositionAsc(projectId: UUID): List<TaskItem>
    fun findAllByAssigneeId(assigneeId: UUID): List<TaskItem>

    @Query("select t from TaskItem t where t.dueDate between :from and :to and t.status <> 'DONE'")
    fun findUpcomingDeadlines(@Param("from") from: Instant, @Param("to") to: Instant): List<TaskItem>
}

interface TaskCommentRepository : JpaRepository<TaskComment, UUID> {
    fun findAllByTaskIdOrderByCreatedAtAsc(taskId: UUID): List<TaskComment>
}

interface TaskDependencyRepository : JpaRepository<TaskDependency, UUID> {
    fun findAllByTaskId(taskId: UUID): List<TaskDependency>
}

interface CalendarEventRepository : JpaRepository<CalendarEvent, UUID> {
    fun findAllByWorkspaceIdAndStartsAtBetween(workspaceId: UUID, from: Instant, to: Instant): List<CalendarEvent>
}

interface EventAttendeeRepository : JpaRepository<EventAttendee, UUID> {
    fun findAllByEventId(eventId: UUID): List<EventAttendee>
    fun findAllByUserId(userId: UUID): List<EventAttendee>
}

interface MeetingNoteRepository : JpaRepository<MeetingNote, UUID> {
    fun findAllByEventId(eventId: UUID): List<MeetingNote>
}

interface DocumentRepository : JpaRepository<DocumentEntity, UUID> {
    fun findAllByWorkspaceIdAndParentIdIsNull(workspaceId: UUID): List<DocumentEntity>
    fun findAllByParentId(parentId: UUID): List<DocumentEntity>
    fun findAllByWorkspaceIdAndIsFavoriteTrue(workspaceId: UUID): List<DocumentEntity>

    @Query(
        "select d from DocumentEntity d where d.workspaceId = :workspaceId " +
            "and lower(d.title) like lower(concat('%', :query, '%'))"
    )
    fun searchByTitle(@Param("workspaceId") workspaceId: UUID, @Param("query") query: String): List<DocumentEntity>
}

interface DocumentVersionRepository : JpaRepository<DocumentVersion, UUID> {
    fun findAllByDocumentIdOrderByCreatedAtDesc(documentId: UUID): List<DocumentVersion>
}

interface DocumentCommentRepository : JpaRepository<DocumentComment, UUID> {
    fun findAllByDocumentId(documentId: UUID): List<DocumentComment>
}

interface CollaborationSessionRepository : JpaRepository<CollaborationSession, UUID> {
    fun findAllByDocumentId(documentId: UUID): List<CollaborationSession>
    fun findByDocumentIdAndUserId(documentId: UUID, userId: UUID): CollaborationSession?
}

interface FileAssetRepository : JpaRepository<FileAsset, UUID> {
    fun findAllByContextTypeAndContextId(contextType: FileContextType, contextId: UUID): List<FileAsset>
    fun findAllByWorkspaceId(workspaceId: UUID): List<FileAsset>
}

interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<NotificationEntity>
    fun findAllByUserIdAndIsReadFalse(userId: UUID): List<NotificationEntity>
    fun countByUserIdAndIsReadFalse(userId: UUID): Long
}

interface IntegrationRepository : JpaRepository<Integration, UUID> {
    fun findAllByWorkspaceId(workspaceId: UUID): List<Integration>
}

interface AiSessionRepository : JpaRepository<AiSession, UUID> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<AiSession>
}

interface AuditLogRepository : JpaRepository<AuditLog, UUID> {
    fun findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId: UUID): List<AuditLog>
}
