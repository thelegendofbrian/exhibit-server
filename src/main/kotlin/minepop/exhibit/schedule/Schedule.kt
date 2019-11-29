package minepop.exhibit.schedule

import java.sql.Date
import java.time.LocalDate

/**
 * A schedule represents a plan by the group member for when and how often they will check in.
 * For example, a group member may plan to check in every Tuesday, or every other day.
 */
abstract class Schedule(var groupMemberId: Long, var startDate: Date) {

    /**
     * This method determines the changes that need to be made to the group members based on this check in.
     *
     * @param stats An object encapsulating the changes to be made.
     * @param start The start date for the period of stats. Usually the date of the last scheduled check in prior to today.
     * @param end The end date for the period of stats. Usually is the date that the group member is checking in.
     */
    abstract fun calculateStatsUpdate(stats: ScheduleStatsUpdate, start: LocalDate, end: LocalDate)

    fun iterate(scheduleStart: LocalDate? = null, iterateStart: LocalDate? = null, iterateEnd: LocalDate, forEach: (date: LocalDate) -> Unit) {
        val localStartDate = startDate.toLocalDate()
        val actualStart = if (scheduleStart == null || localStartDate > scheduleStart) localStartDate else scheduleStart
        val actualIterateStart = iterateStart ?: actualStart
        iterate0(actualStart, actualIterateStart, iterateEnd, forEach)
    }

    abstract fun iterate0(scheduleStart: LocalDate, start: LocalDate, end: LocalDate, forEach: (date: LocalDate) -> Unit)
}

class WeeklySchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: List<Int> = mutableListOf()

    override fun iterate0(
        scheduleStart: LocalDate,
        start: LocalDate,
        end: LocalDate,
        forEach: (date: LocalDate) -> Unit
    ) {
        var weeklyDay = scheduleStart
        while (weeklyDay < end) {
            if (days.contains(weeklyDay.dayOfWeek.value)) {
                forEach(weeklyDay)
            }
            weeklyDay = weeklyDay.plusDays(1)
        }
    }

    override fun calculateStatsUpdate(stats: ScheduleStatsUpdate, start: LocalDate, end: LocalDate) {
        /*
            If the schedule does not contain the day of week of today, then this is a bonus check in.
         */
        stats.isBonusCheckin = !days.contains(end.dayOfWeek.value)

        /*
            Step forward from the last scheduled check in one day at a time until today.
            If the day is a scheduled check in, then it was a missed check in.
         */
        iterate(scheduleStart = start.plusDays(1), iterateEnd = end) {
            stats.missedCheckins++
        }
    }
}

class IntervalSchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: Int? = null

    override fun iterate0(
        scheduleStart: LocalDate,
        start: LocalDate,
        end: LocalDate,
        forEach: (date: LocalDate) -> Unit
    ) {
        var intervalDay = scheduleStart
        val interval = days!!.toLong()
        while (intervalDay < end) {
            intervalDay = intervalDay.plusDays(interval)
            forEach(intervalDay)
        }
    }

    override fun calculateStatsUpdate(stats: ScheduleStatsUpdate, start: LocalDate, end: LocalDate) {
        /*
            This could perform poorly if the start date was a long time ago.
         */
        var intervalDay: LocalDate? = null
        /*
            Iterate the schedule forward through its scheduled days until today.
            For each iteration, there is expected to be 1 check in.
            So for each iteration past the first, there is a missed check in.
         */
        iterate(scheduleStart = start, iterateEnd = end) {
            stats.missedCheckins++
            intervalDay = it
        }

        /*
            If the current check in does not much the expected next check in, then this is a bonus check in.
         */
        stats.isBonusCheckin = end != intervalDay
        /*
            The first iteration needs to be reversed, since it was not a missed check in.
         */
        stats.missedCheckins--
    }
}

data class ScheduleStatsUpdate(var groupMemberId: Long, var isBonusCheckin: Boolean, var missedCheckins: Int)

fun Schedule?.newStats(): ScheduleStatsUpdate {
    return ScheduleStatsUpdate(this?.groupMemberId ?: -1,this == null, 0)
}

/*
    The time between the last scheduled check in and today may span across multiple schedules.
    We need to add the missed days for each schedule.
 */
fun List<Schedule>.calculateStatsUpdate(groupMemberId: Long, lastScheduledCheckin: LocalDate?, dateNow: LocalDate): ScheduleStatsUpdate {
    if (isEmpty()) {
       return ScheduleStatsUpdate(groupMemberId, true, 0)
    } else {

        val schedule = this[size - 1]
        val totalStats = schedule.newStats()
        val scheduleStart = schedule.startDate.toLocalDate()
        val start = if (lastScheduledCheckin == null) scheduleStart.minusDays(1)
            else if (scheduleStart > lastScheduledCheckin) scheduleStart.minusDays(1)
            else lastScheduledCheckin
        schedule.calculateStatsUpdate(totalStats, start, dateNow)

        /*
            Iterate through all past schedules.
         */
        for (i in 0..size - 2) {

            val pastSchedule = this[i]
            val nextScheduleStartDate = this[i + 1].startDate.toLocalDate()
            val scheduleStartDate = pastSchedule.startDate.toLocalDate()
            val stats = pastSchedule.newStats()

            var start: LocalDate
            start = if (lastScheduledCheckin == null || scheduleStartDate > lastScheduledCheckin || nextScheduleStartDate < lastScheduledCheckin) {
                scheduleStartDate.minusDays(1)
            } else {
                lastScheduledCheckin
            }
            pastSchedule.calculateStatsUpdate(stats, start, nextScheduleStartDate)

            totalStats.missedCheckins += stats.missedCheckins
        }

        return totalStats
    }
}