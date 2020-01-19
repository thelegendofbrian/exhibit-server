package minepop.exhibit.stats

import minepop.exhibit.schedule.CheckinType
import minepop.exhibit.schedule.ScheduleStatsUpdate

data class GroupMemberStatistics(val groupMemberId: Long, val streak: Int, val regularCheckins: Int, val bonusCheckins: Int, val missedCheckins: Int, val points: Long)

fun GroupMemberStatistics.calculateStatistics(): GroupMemberCalculatedStatistics {

    val possibleCheckins = regularCheckins + missedCheckins
    val adherence = if (possibleCheckins == 0) null else regularCheckins.toDouble() / possibleCheckins

    return GroupMemberCalculatedStatistics(streak, adherence, points, bonusCheckins, regularCheckins + bonusCheckins)
}

fun GroupMemberStatistics.updateStatistics(stats: ScheduleStatsUpdate): GroupMemberStatistics {

    var newStreak = streak
    if (stats.missedCheckins == 0) {
        if (stats.checkinType == CheckinType.SCHEDULED)
            newStreak++
    } else {
        newStreak = if (stats.checkinType == CheckinType.SCHEDULED) 1 else 0
    }

    var newScheduled = regularCheckins
    if (stats.checkinType == CheckinType.SCHEDULED)
        newScheduled++

    var newBonus = bonusCheckins
    if (stats.checkinType == CheckinType.BONUS)
        newBonus++

    val newMissed = missedCheckins + stats.missedCheckins

    return GroupMemberStatistics(groupMemberId, newStreak, newScheduled, newBonus, newMissed, points)
}

data class GroupMemberCalculatedStatistics(val dayStreak: Int, var adherence: Double?, val points: Long, val bonusCheckins: Int, val totalCheckins: Int)