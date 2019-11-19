package minepop.exhibit.user

import minepop.exhibit.dao.DAO
import java.sql.Types

class UserSettingsDAO: DAO() {

    fun updateSettings(settings: UserSettings) {
        connect().use { c ->
            c.prepareStatement("update user_settings set timezone = ?, default_group_id = ?, display_name = ? where user_id = ?").use {
                it.setString(1, settings.timezone)
                if (settings.defaultGroupId == null) {
                    it.setNull(2, Types.BIGINT)
                } else {
                    it.setLong(2, settings.defaultGroupId!!)
                }
                it.setLong(3, settings.userId)
                if (settings.displayName == null) {
                    it.setNull(4, Types.VARCHAR)
                } else {
                    it.setString(4, settings.displayName!!)
                }
                it.executeUpdate()
            }
        }
    }
}