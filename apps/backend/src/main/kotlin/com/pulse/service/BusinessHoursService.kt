package com.pulse.service

import com.pulse.domain.BusinessHours
import com.pulse.dto.BusinessHourEntry
import com.pulse.dto.BusinessHoursStatusResponse
import com.pulse.dto.BusinessHoursUpdateRequest
import com.pulse.repository.BusinessHoursRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Lets a workspace enter its own opening hours per day of the week (fully
 * user-editable, no hardcoded schedule) and, from that, computes in real
 * time: whether the workspace is open right now, when the next status
 * change happens (open -> closed or closed -> open), and how many seconds
 * remain until that transition. The frontend polls/renders this to show a
 * live "Open now / closes in 2h 14m" or "Closed - opens in 6h 40m" badge.
 */
@Service
class BusinessHoursService(private val repository: BusinessHoursRepository) {

    @Transactional
    fun update(workspaceId: UUID, request: BusinessHoursUpdateRequest): List<BusinessHourEntry> {
        val existing = repository.findAllByWorkspaceIdOrderByDayOfWeekAsc(workspaceId).associateBy { it.dayOfWeek }
        request.days.forEach { entry ->
            val row = existing[entry.dayOfWeek] ?: BusinessHours(workspaceId = workspaceId, dayOfWeek = entry.dayOfWeek)
            row.isClosed = entry.isClosed
            row.openTime = entry.openTime?.let { LocalTime.parse(it) }
            row.closeTime = entry.closeTime?.let { LocalTime.parse(it) }
            row.timezone = entry.timezone
            repository.save(row)
        }
        return getSchedule(workspaceId)
    }

    fun getSchedule(workspaceId: UUID): List<BusinessHourEntry> {
        val rows = repository.findAllByWorkspaceIdOrderByDayOfWeekAsc(workspaceId).associateBy { it.dayOfWeek }
        return (0..6).map { day ->
            val row = rows[day]
            BusinessHourEntry(
                dayOfWeek = day,
                isClosed = row?.isClosed ?: true,
                openTime = row?.openTime?.format(DateTimeFormatter.ofPattern("HH:mm")),
                closeTime = row?.closeTime?.format(DateTimeFormatter.ofPattern("HH:mm")),
                timezone = row?.timezone ?: "UTC"
            )
        }
    }

    fun getStatus(workspaceId: UUID): BusinessHoursStatusResponse {
        val schedule = getSchedule(workspaceId)
        val timezone = schedule.firstOrNull { !it.isClosed }?.timezone ?: schedule.firstOrNull()?.timezone ?: "UTC"
        val zoneId = runCatching { ZoneId.of(timezone) }.getOrDefault(ZoneId.of("UTC"))
        val now = ZonedDateTime.now(zoneId)

        val byDay = schedule.associateBy { it.dayOfWeek }

        // Is it open right now?
        val todayEntry = byDay[isoDayToStored(now.dayOfWeek)]
        val isOpenNow = todayEntry != null &&
            !todayEntry.isClosed &&
            todayEntry.openTime != null &&
            todayEntry.closeTime != null &&
            isWithin(now.toLocalTime(), LocalTime.parse(todayEntry.openTime), LocalTime.parse(todayEntry.closeTime))

        val (nextChangeAt, nextChangeType) = findNextTransition(now, byDay, isOpenNow)
        val seconds = nextChangeAt?.let { java.time.Duration.between(Instant.now(), it).seconds.coerceAtLeast(0) }

        return BusinessHoursStatusResponse(
            isOpenNow = isOpenNow,
            timezone = zoneId.id,
            currentLocalTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            nextChangeAt = nextChangeAt,
            nextChangeType = nextChangeType,
            secondsUntilNextChange = seconds,
            schedule = schedule
        )
    }

    private fun isWithin(time: LocalTime, open: LocalTime, close: LocalTime): Boolean =
        if (close.isAfter(open)) !time.isBefore(open) && time.isBefore(close)
        else !time.isBefore(open) || time.isBefore(close) // overnight window, e.g. 22:00-02:00

    /** Walk forward up to 8 days (today + a full week) to find the next open or close boundary. */
    private fun findNextTransition(
        now: ZonedDateTime,
        byDay: Map<Int, BusinessHourEntry>,
        isOpenNow: Boolean
    ): Pair<Instant?, String> {
        if (isOpenNow) {
            val today = byDay[isoDayToStored(now.dayOfWeek)]
            val closeTime = today?.closeTime?.let { LocalTime.parse(it) }
            if (closeTime != null) {
                var closeAt = now.toLocalDate().atTime(closeTime).atZone(now.zone)
                if (closeAt.isBefore(now)) closeAt = closeAt.plusDays(1)
                return closeAt.toInstant() to "CLOSES"
            }
        }
        for (offset in 0..7) {
            val candidateDate = now.toLocalDate().plusDays(offset.toLong())
            val storedDay = isoDayToStored(candidateDate.dayOfWeek)
            val entry = byDay[storedDay] ?: continue
            if (entry.isClosed || entry.openTime == null) continue
            val openAt = candidateDate.atTime(LocalTime.parse(entry.openTime)).atZone(now.zone)
            if (openAt.isAfter(now)) {
                return openAt.toInstant() to "OPENS"
            }
        }
        return null to "OPENS"
    }

    /** java.time.DayOfWeek is MONDAY=1..SUNDAY=7; our schema stores SUNDAY=0..SATURDAY=6. */
    private fun isoDayToStored(day: DayOfWeek): Int = day.value % 7
}
