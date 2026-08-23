package com.pulse.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateEventRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    val location: String? = null,
    val eventType: String = "EVENT",
    val startsAt: Instant,
    val endsAt: Instant,
    val timezone: String = "UTC",
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val attendeeIds: List<UUID> = emptyList()
)

data class EventResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val location: String?,
    val eventType: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val meetingUrl: String?,
    val attendees: List<AttendeeResponse>
)

data class AttendeeResponse(val userId: UUID, val displayName: String, val rsvp: String)

data class RsvpRequest(@field:NotBlank val rsvp: String)

data class AddMeetingNoteRequest(
    @field:NotBlank val body: String,
    val actionItems: List<String> = emptyList()
)
