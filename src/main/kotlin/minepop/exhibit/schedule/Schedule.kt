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
    abstract fun calculateStats(stats: ScheduleStats, start: LocalDate, end: LocalDate)
}

class WeeklySchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: List<Int> = mutableListOf()

    override fun calculateStats(stats: ScheduleStats, start: LocalDate, end: LocalDate) {
        /*
            If the schedule does not contain the day of week of today, then this is a bonus check in.
         */
        stats.isBonusCheckin = !days.contains(end.dayOfWeek.value)

        /*
            Step forward from the last scheduled check in one day at a time until today.
            If the day is a scheduled check in, then it was a missed check in.
         */
        val scheduleStart = startDate.toLocalDate().minusDays(1)
        var weeklyDay = if (start > scheduleStart) start else scheduleStart
        weeklyDay = weeklyDay.plusDays(1)
        while (weeklyDay < end) {
            if (days.contains(weeklyDay.dayOfWeek.value)) {
                stats.missedCheckins++
            }
            weeklyDay = weeklyDay.plusDays(1)
        }
    }
}

class IntervalSchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: Int? = null

    override fun calculateStats(stats: ScheduleStats, start: LocalDate, end: LocalDate) {
        /*
            This could perform poorly if the start date was a long time ago.
         */
        val scheduleStart = startDate.toLocalDate()
        var intervalDay = if (start > scheduleStart) start else scheduleStart
        val interval = days!!.toLong()
        /*
            Iterate the schedule forward through its scheduled days until today.
            For each iteration, there is expected to be 1 check in.
            So for each iteration past the first, there is a missed check in.
         */
        while (intervalDay < end) {
            intervalDay = intervalDay.plusDays(interval)
            stats.missedCheckins++
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

data class ScheduleStats(var groupMemberId: Long, var isBonusCheckin: Boolean, var missedCheckins: Int)

fun Schedule?.newStats(): ScheduleStats {
    return ScheduleStats(this?.groupMemberId ?: -1,this == null, 0)
}

/*
    The time between the last scheduled check in and today may span across multiple schedules.
    We need to add the missed days for each schedule.
 */
fun List<Schedule>.calculateStats(groupMemberId: Long, lastScheduledCheckin: LocalDate?, dateNow: LocalDate): ScheduleStats {
    if (isEmpty()) {
       return ScheduleStats(groupMemberId, true, 0)
    } else {

        val schedule = this[size - 1]
        val totalStats = schedule.newStats()
        val scheduleStart = schedule.startDate.toLocalDate()
        val start = if (lastScheduledCheckin == null) scheduleStart.minusDays(1)
            else if (scheduleStart > lastScheduledCheckin) scheduleStart.minusDays(1)
            else lastScheduledCheckin
        schedule.calculateStats(totalStats, start, dateNow)

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
            pastSchedule.calculateStats(stats, start, nextScheduleStartDate)

            totalStats.missedCheckins += stats.missedCheckins
        }

        return totalStats
    }
}