package com.pulse.controller

import com.pulse.dto.DashboardSummaryResponse
import com.pulse.service.DashboardService
import com.pulse.util.currentUserId
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/dashboard")
class DashboardController(private val dashboardService: DashboardService) {

    @GetMapping("/summary")
    fun summary(@PathVariable workspaceId: UUID): DashboardSummaryResponse =
        dashboardService.summarize(workspaceId, currentUserId())
}
