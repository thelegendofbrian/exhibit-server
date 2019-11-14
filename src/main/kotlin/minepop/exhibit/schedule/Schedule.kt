package minepop.exhibit.schedule

open class Schedule(userName: String, groupName: String) {
    var userName: String = userName
    var groupName: String = groupName
}

class WeeklySchedule(userName: String, groupName: String) : Schedule(userName, groupName) {
    var days: List<String> = mutableListOf()
}

class IntervalSchedule(userName: String, groupName: String) : Schedule(userName, groupName) {
    var days: Int? = null
}