package minepop.exhibit.auth

import minepop.exhibit.dao.DAO
import minepop.exhibit.user.UserSettings
import java.sql.SQLException

class SessionAuthDAO : DAO() {

    fun retrieveUser(name: String? = null, id: Long? = null): AuthUser? {
        connect().use { c ->
            c.prepareStatement("select id, name, failed_logins, salt, salted_hash, timezone, default_group_id, display_name from user" +
                    " inner join user_settings on user.id = user_id" +
                    " where ${if (name == null) "id" else "name"} = ?").use {
                if (name == null) {
                    it.setLong(1, id!!)
                } else {
                    it.setString(1, name)
                }
                val rs = it.executeQuery()
                if (rs.next()) {
                    val userId = rs.getLong(1)
                    var defaultGroupId: Long? = rs.getLong(7)
                    if (rs.wasNull()) {
                        defaultGroupId = null
                    }
                    return AuthUser(userId, rs.getString(2),
                        rs.getInt(3), rs.getBytes(4), rs.getBytes(5),
                        UserSettings(userId, rs.getString(6), defaultGroupId, rs.getString(8))
                    )
                }
            }
        }
        return null
    }

    fun updateUser(user: AuthUser) {
        connect().use { c ->
            c.prepareStatement("update user set failed_logins = ?, salt = ?, salted_hash = ? where name = ?").use {
                it.setInt(1, user.failedLogins)
                it.setBytes(2, user.salt)
                it.setBytes(3, user.saltedHash)
                it.setString(4, user.name)
                it.executeUpdate()
            }
        }
    }

    fun createQuickAuth(user: AuthUser, authKey: String) {
        connect().use { c ->
            c.prepareStatement("insert into quick_auth(user_id, auth_key) values (?, ?)").use {
                it.setLong(1, user.id)
                it.setString(2, authKey)
                it.executeUpdate()
            }
            // delete all but 3 latest records
            var quickAuthKeys: Int = 0
            c.prepareStatement("select count(1) from quick_auth where user_id = ?").use {
                it.setLong(1, user.id)
                val rs = it.executeQuery()
                if (rs.next()) {
                    quickAuthKeys = rs.getInt(1)
                }
            }
            if (quickAuthKeys > 3) {
                c.prepareStatement("delete from exhibit.quick_auth where user_id = ? order by date_created asc limit ?").use {
                    it.setLong(1, user.id)
                    it.setInt(2, quickAuthKeys - 3)
                    it.executeUpdate()
                }
            }
        }
    }

    fun retrieveUserForQuickAuth(authKey: String): AuthUser? {
        var id: Long? = null
        connect().use { c ->
            c.prepareStatement("select user_id from quick_auth where auth_key = ?").use {
                it.setString(1, authKey)
                val rs = it.executeQuery()
                if (rs.next()) {
                    id = rs.getLong(1)
                }
            }
        }
        id?.let {
            return retrieveUser(id = id)
        }
        return null
    }
}
