package minepop.exhibit.user

import minepop.exhibit.dao.DAO
import java.sql.Types

class UserSettingsDAO: DAO() {

    fun updateSettings(settings: UserSettings) {
        connect().use { c ->
            c.prepareStatement("update user_settings set timezone = ?, default_group_id = ?, display_name = ? where user_id = ?").use {
                it.setString(1, settings.timeZone)
                if (settings.defaultGroupId == null) {
                    it.setNull(2, Types.BIGINT)
                } else {
                    it.setLong(2, settings.defaultGroupId!!)
                }
                if (settings.displayName == null) {
                    it.setNull(3, Types.VARCHAR)
                } else {
                    it.setString(3, settings.displayName!!)
                }
                it.setLong(4, settings.userId)
                it.executeUpdate()
            }
        }
    }

    fun retrieveSettings(userId: Long): UserSettings {
        connect().use { c ->
            c.prepareStatement("select timezone, default_group_id, display_name from user_settings where user_id = ?").use {
                it.setLong(1, userId)
                val rs = it.executeQuery()
                rs.next()
                return UserSettings(userId, rs.getString(1), rs.getLong(2), rs.getString(3))
            }
        }
    }
}