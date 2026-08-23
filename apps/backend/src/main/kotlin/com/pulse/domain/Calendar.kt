package com.pulse.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "calendar_events")
class CalendarEvent(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    var title: String,
    var description: String? = null,
    var location: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    var eventType: CalendarEventType = CalendarEventType.EVENT,

    @Column(name = "starts_at")
    var startsAt: Instant,

    @Column(name = "ends_at")
    var endsAt: Instant,

    var timezone: String = "UTC",

    @Column(name = "is_recurring")
    var isRecurring: Boolean = false,

    @Column(name = "recurrence_rule")
    var recurrenceRule: String? = null,

    @Column(name = "meeting_url")
    var meetingUrl: String? = null,

    @Column(name = "created_by")
    var createdBy: UUID,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "event_attendees")
class EventAttendee(
    @Column(name = "event_id")
    var eventId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

    @Enumerated(EnumType.STRING)
    var rsvp: RsvpStatus = RsvpStatus.PENDING
) : BaseEntity()

@Entity
@Table(name = "meeting_notes")
class MeetingNote(
    @Column(name = "event_id")
    var eventId: UUID,

    @Column(name = "author_id")
    var authorId: UUID,

    @Column(columnDefinition = "text")
    var body: String,

    @Column(name = "action_items", columnDefinition = "jsonb")
    var actionItems: String = "[]",

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()
