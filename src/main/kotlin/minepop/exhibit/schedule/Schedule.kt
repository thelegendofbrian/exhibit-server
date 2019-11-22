package minepop.exhibit.schedule

import java.sql.Date
import java.time.LocalDate

/**
 * A schedule represents a plan by the group member for when and how often they will check in.
 * For example, a group member may plan to check in every Tuesday, or every other day.
 */
abstract class Schedule(var groupMemberId: Long, var startDate: Date) {

    /**
     * This method determines the changes that need to be made to the group member's based on this check in.
     *
     * @param stats An object encapsulating the changes to be made.
     * @param now The date that the group member is checking in.
     * @param lastCheckin The date of the last scheduled check in prior to today.
     */
    abstract fun calculateStats(stats: ScheduleStats, now: LocalDate, lastCheckin: LocalDate)
}

class WeeklySchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: List<Int> = mutableListOf()

    override fun calculateStats(stats: ScheduleStats, now: LocalDate, lastCheckin: LocalDate) {
        /*
            Iterate backwards from today until a scheduled weekday is found.
            If the last check in was not on that day, then the streak has been broken.
         */
        for (i in 1..7) {
            val weekDay = now.dayOfWeek.value - i
            if (days.contains(weekDay)) {
                stats.isStreakBroken = now.minusDays(i.toLong()) != lastCheckin
                break
            }
        }
        /*
            If the schedule does not contain the weekday of check in, then this is a bonus check in.
         */
        stats.isBonusCheckin = days.contains(now.dayOfWeek.value)

        /*
            Step forward from the last check in one day at a time until the date of check in is reached.
            If the day is a scheduled check in, then it was a missed check in.
         */
        var weekDay = lastCheckin
        while (weekDay < now) {
            weekDay = weekDay.plusDays(1)
            if (days.contains(weekDay.dayOfWeek.value)) {
                stats.missedCheckins++
            }
        }
    }
}

class IntervalSchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: Int? = null

    override fun calculateStats(stats: ScheduleStats, now: LocalDate, lastCheckin: LocalDate) {
        /*
            This could perform poorly if the start date was a long time ago.
         */
        var intervalDay = startDate.toLocalDate()
        val interval = days!!.toLong()
        /*
            Iterate the schedule forward through its scheduled days until the last scheduled check in is reached.
         */
        while (intervalDay <= lastCheckin) {
            intervalDay = intervalDay.plusDays(interval)
        }
        /*
            If the last scheduled check in does not match the resulting value, then the streak has been broken.
         */
        stats.isStreakBroken = lastCheckin != intervalDay
        /*
            Iterate forward until the date of this check in.
            For each iteration, there is expected to be 1 check in.
            So for each iteration past the first, this is a missed check in.
         */
        while (intervalDay <= now) {
            intervalDay = intervalDay.plusDays(interval)
            stats.missedCheckins++
        }
        /*
            If the current check in does not much the expected next check in, then this is a bonus check in.
         */
        stats.isBonusCheckin = now != intervalDay
        /*
            The first iteration needs to be reversed, since it was not a missed check in.
         */
        stats.missedCheckins--
    }
}

data class ScheduleStats(var groupMemberId: Long, var isStreakBroken: Boolean, var isBonusCheckin: Boolean, var missedCheckins: Int)

fun Schedule?.newStats(): ScheduleStats {
    return ScheduleStats(this?.groupMemberId ?: -1,this == null, this == null, 0)
}