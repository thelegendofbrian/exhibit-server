package minepop.exhibit.schedule

import java.sql.Date

open class Schedule(groupMemberId: Long, startDate: Date) {
    var groupMemberId: Long = groupMemberId
    var startDate: Date = startDate
}

class WeeklySchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: List<Int> = mutableListOf()
}

class IntervalSchedule(groupMemberId: Long, startDate: Date) : Schedule(groupMemberId, startDate) {
    var days: Int? = null
}