package com.pulse.controller

import com.pulse.dto.BusinessHourEntry
import com.pulse.dto.BusinessHoursStatusResponse
import com.pulse.dto.BusinessHoursUpdateRequest
import com.pulse.service.BusinessHoursService
import com.pulse.service.WorkspaceService
import com.pulse.util.currentUserId
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Lets each workspace configure its own opening hours (per day of week) and
 * exposes a live "is it open right now / when's the next change" status,
 * fully driven by whatever the user enters - nothing is hardcoded.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/business-hours")
class BusinessHoursController(
    private val businessHoursService: BusinessHoursService,
    private val workspaceService: WorkspaceService
) {

    @GetMapping
    fun getSchedule(@PathVariable workspaceId: UUID): List<BusinessHourEntry> =
        businessHoursService.getSchedule(workspaceId)

    @PutMapping
    fun updateSchedule(@PathVariable workspaceId: UUID, @RequestBody request: BusinessHoursUpdateRequest): List<BusinessHourEntry> {
        workspaceService.requireMembership(workspaceId, currentUserId())
        return businessHoursService.update(workspaceId, request)
    }

    @GetMapping("/status")
    fun getStatus(@PathVariable workspaceId: UUID): BusinessHoursStatusResponse =
        businessHoursService.getStatus(workspaceId)
}
