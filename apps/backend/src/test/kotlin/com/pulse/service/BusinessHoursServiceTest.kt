package com.pulse.service

import com.pulse.domain.BusinessHours
import com.pulse.repository.BusinessHoursRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.util.UUID

class BusinessHoursServiceTest {

    private val repository = mockk<BusinessHoursRepository>()
    private val service = BusinessHoursService(repository)
    private val workspaceId = UUID.randomUUID()

    @Test
    fun `returns closed status when no hours are configured`() {
        every { repository.findAllByWorkspaceIdOrderByDayOfWeekAsc(workspaceId) } returns emptyList()

        val status = service.getStatus(workspaceId)

        assertEquals(false, status.isOpenNow)
        assertEquals("UTC", status.timezone)
    }

    @Test
    fun `is open when current UTC time falls inside todays configured window`() {
        val today = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")).dayOfWeek.value % 7
        val hours = BusinessHours(
            workspaceId = workspaceId,
            dayOfWeek = today,
            isClosed = false,
            openTime = LocalTime.of(0, 0),
            closeTime = LocalTime.of(23, 59),
            timezone = "UTC"
        )
        every { repository.findAllByWorkspaceIdOrderByDayOfWeekAsc(workspaceId) } returns listOf(hours)

        val status = service.getStatus(workspaceId)

        assertTrue(status.isOpenNow)
        assertEquals("CLOSES", status.nextChangeType)
    }

    @Test
    fun `computed schedule always has all seven days even if only some are configured`() {
        every { repository.findAllByWorkspaceIdOrderByDayOfWeekAsc(workspaceId) } returns listOf(
            BusinessHours(workspaceId = workspaceId, dayOfWeek = 1, isClosed = false, openTime = LocalTime.of(9, 0), closeTime = LocalTime.of(17, 0))
        )

        val schedule = service.getSchedule(workspaceId)

        assertEquals(7, schedule.size)
        assertTrue(schedule.first { it.dayOfWeek == 2 }.isClosed)
    }
}
