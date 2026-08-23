package com.pulse.controller

import com.pulse.dto.*
import com.pulse.service.CalendarService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/calendar")
class CalendarController(private val calendarService: CalendarService) {

    @GetMapping("/events")
    fun listInRange(
        @PathVariable workspaceId: UUID,
        @RequestParam from: Instant,
        @RequestParam to: Instant
    ): List<EventResponse> = calendarService.listInRange(workspaceId, from, to)

    @PostMapping("/events")
    fun create(@PathVariable workspaceId: UUID, @Valid @RequestBody request: CreateEventRequest): EventResponse =
        calendarService.create(workspaceId, currentUserId(), request)

    @GetMapping("/events/upcoming")
    fun upcoming(@PathVariable workspaceId: UUID): List<EventResponse> = calendarService.upcomingForUser(currentUserId())

    @PostMapping("/events/{eventId}/rsvp")
    fun rsvp(@PathVariable workspaceId: UUID, @PathVariable eventId: UUID, @Valid @RequestBody request: RsvpRequest) {
        calendarService.rsvp(eventId, currentUserId(), request)
    }

    @PostMapping("/events/{eventId}/notes")
    fun addNote(@PathVariable workspaceId: UUID, @PathVariable eventId: UUID, @Valid @RequestBody request: AddMeetingNoteRequest) {
        calendarService.addNote(eventId, currentUserId(), request)
    }
}
