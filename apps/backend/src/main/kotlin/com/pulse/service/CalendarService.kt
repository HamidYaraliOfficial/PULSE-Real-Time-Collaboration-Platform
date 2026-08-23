package com.pulse.service

import com.pulse.domain.CalendarEvent
import com.pulse.domain.CalendarEventType
import com.pulse.domain.EventAttendee
import com.pulse.domain.NotificationType
import com.pulse.domain.RsvpStatus
import com.pulse.dto.*
import com.pulse.exception.ApiException
import com.pulse.repository.CalendarEventRepository
import com.pulse.repository.EventAttendeeRepository
import com.pulse.repository.MeetingNoteRepository
import com.pulse.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class CalendarService(
    private val eventRepository: CalendarEventRepository,
    private val attendeeRepository: EventAttendeeRepository,
    private val meetingNoteRepository: MeetingNoteRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun create(workspaceId: UUID, actorId: UUID, request: CreateEventRequest): EventResponse {
        val event = CalendarEvent(
            workspaceId = workspaceId, title = request.title, description = request.description,
            location = request.location, eventType = CalendarEventType.valueOf(request.eventType),
            startsAt = request.startsAt, endsAt = request.endsAt, timezone = request.timezone,
            isRecurring = request.isRecurring, recurrenceRule = request.recurrenceRule,
            meetingUrl = if (request.eventType == "MEETING") "https://meet.pulse.app/${UUID.randomUUID()}" else null,
            createdBy = actorId
        )
        eventRepository.save(event)

        (request.attendeeIds + actorId).distinct().forEach { uid ->
            attendeeRepository.save(EventAttendee(eventId = event.id!!, userId = uid, rsvp = if (uid == actorId) RsvpStatus.ACCEPTED else RsvpStatus.PENDING))
            if (uid != actorId) {
                notificationService.create(uid, NotificationType.MEETING, "Invited: ${event.title}", event.description, "/calendar")
            }
        }
        auditLogService.log(workspaceId, actorId, "EVENT_CREATED", "CALENDAR_EVENT", event.id)
        return toResponse(event)
    }

    fun listInRange(workspaceId: UUID, from: Instant, to: Instant): List<EventResponse> =
        eventRepository.findAllByWorkspaceIdAndStartsAtBetween(workspaceId, from, to).map { toResponse(it) }

    fun upcomingForUser(userId: UUID, limit: Int = 5): List<EventResponse> {
        val eventIds = attendeeRepository.findAllByUserId(userId).map { it.eventId }
        return eventIds.mapNotNull { eventRepository.findById(it).orElse(null) }
            .filter { it.startsAt.isAfter(Instant.now()) }
            .sortedBy { it.startsAt }
            .take(limit)
            .map { toResponse(it) }
    }

    @Transactional
    fun rsvp(eventId: UUID, userId: UUID, request: RsvpRequest) {
        val attendee = attendeeRepository.findAllByEventId(eventId).firstOrNull { it.userId == userId }
            ?: attendeeRepository.save(EventAttendee(eventId = eventId, userId = userId))
        attendee.rsvp = RsvpStatus.valueOf(request.rsvp)
        attendeeRepository.save(attendee)
    }

    @Transactional
    fun addNote(eventId: UUID, authorId: UUID, request: AddMeetingNoteRequest) {
        eventRepository.findById(eventId).orElseThrow { ApiException(404, "Event not found") }
        val actionItemsJson = request.actionItems.joinToString(",", "[", "]") { "\"${it.replace("\"", "\\\"")}\"" }
        meetingNoteRepository.save(
            com.pulse.domain.MeetingNote(eventId = eventId, authorId = authorId, body = request.body, actionItems = actionItemsJson)
        )
    }

    private fun toResponse(event: CalendarEvent): EventResponse {
        val attendees = attendeeRepository.findAllByEventId(event.id!!).mapNotNull { a ->
            val user = userRepository.findById(a.userId).orElse(null) ?: return@mapNotNull null
            AttendeeResponse(userId = a.userId, displayName = user.displayName, rsvp = a.rsvp.name)
        }
        return EventResponse(
            id = event.id!!, title = event.title, description = event.description, location = event.location,
            eventType = event.eventType.name, startsAt = event.startsAt, endsAt = event.endsAt,
            meetingUrl = event.meetingUrl, attendees = attendees
        )
    }
}
