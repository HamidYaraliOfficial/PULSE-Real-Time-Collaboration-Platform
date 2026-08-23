package com.pulse.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "projects")
class Project(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Column(name = "team_id")
    var teamId: UUID? = null,

    var name: String,
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    var status: ProjectStatus = ProjectStatus.ACTIVE,

    @Column(name = "created_by")
    var createdBy: UUID,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "tasks")
class TaskItem(
    @Column(name = "project_id")
    var projectId: UUID,

    var title: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    var status: TaskStatus = TaskStatus.BACKLOG,

    @Enumerated(EnumType.STRING)
    var priority: TaskPriority = TaskPriority.MEDIUM,

    @Column(name = "assignee_id")
    var assigneeId: UUID? = null,

    @Column(name = "reporter_id")
    var reporterId: UUID,

    @Column(name = "parent_task_id")
    var parentTaskId: UUID? = null,

    @Column(name = "due_date")
    var dueDate: Instant? = null,

    var position: Int = 0,

    @Column(columnDefinition = "text[]")
    var labels: Array<String> = arrayOf(),

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "task_comments")
class TaskComment(
    @Column(name = "task_id")
    var taskId: UUID,

    @Column(name = "author_id")
    var authorId: UUID,

    @Column(columnDefinition = "text")
    var body: String,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "task_dependencies")
class TaskDependency(
    @Column(name = "task_id")
    var taskId: UUID,

    @Column(name = "depends_on_id")
    var dependsOnId: UUID
) : BaseEntity()
