package com.pulse.domain

enum class WorkspaceRole { OWNER, ADMIN, MANAGER, MEMBER, GUEST }

enum class PresenceStatus { ONLINE, AWAY, BUSY, DO_NOT_DISTURB, OFFLINE }

enum class ChannelType { PUBLIC, PRIVATE, DIRECT, GROUP }

enum class MessageContentType { MARKDOWN, PLAIN, SYSTEM }

enum class TaskStatus { BACKLOG, TODO, IN_PROGRESS, REVIEW, TESTING, DONE }

enum class TaskPriority { LOW, MEDIUM, HIGH, URGENT }

enum class ProjectStatus { ACTIVE, ON_HOLD, COMPLETED, ARCHIVED }

enum class CalendarEventType { EVENT, MEETING, DEADLINE, REMINDER }

enum class RsvpStatus { PENDING, ACCEPTED, DECLINED }

enum class NotificationType {
    MENTION, MESSAGE, ASSIGNMENT, COMMENT, MEETING,
    DOCUMENT_CHANGE, FILE_SHARE, TASK_DEADLINE, CALL, SYSTEM
}

enum class IntegrationProvider { GITHUB, GITLAB, GOOGLE_CALENDAR, GOOGLE_DRIVE, DROPBOX, JIRA }

enum class FileContextType { CHANNEL, CHAT, TASK, DOCUMENT }
