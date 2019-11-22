package minepop.exhibit.checkin

import java.sql.Date

data class Checkin(var groupMemberId: Long, var date: Date, var isBonus: Boolean)