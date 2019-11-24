package minepop.exhibit.stats

data class GroupMemberStatistics(val groupMemberId: Long, val streak: Int, val regularCheckins: Int, val bonusCheckins: Int, val missedCheckins: Int, val points: Long)

fun GroupMemberStatistics.calculateStatistics(): GroupMemberCalculatedStatistics {
    val possibleCheckins = regularCheckins + missedCheckins
    val adherence = if (possibleCheckins == 0) null else regularCheckins.toDouble() / possibleCheckins

    return GroupMemberCalculatedStatistics(streak, adherence, points, bonusCheckins, regularCheckins + bonusCheckins)
}

data class GroupMemberCalculatedStatistics(val dayStreak: Int, val adherence: Double?, val points: Long, val bonusCheckins: Int, val totalCheckins: Int)