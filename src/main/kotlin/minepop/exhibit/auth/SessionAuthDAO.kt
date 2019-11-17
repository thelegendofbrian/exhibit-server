package minepop.exhibit.auth

import minepop.exhibit.dao.DAO
import java.sql.SQLException

class SessionAuthDAO : DAO() {

    @Throws(SQLException::class)
    fun retrieveUser(userName: String): User? {
        connect().use { c ->
            c.prepareStatement("select failed_logins, salt, salted_hash from user where user_name = ?").use {
                it.setString(1, userName)
                val rs = it.executeQuery()
                if (rs.next()) {
                    return User(userName, rs.getInt(1), rs.getBytes(2), rs.getBytes(3))
                }
            }
        }
        return null
    }

    @Throws(SQLException::class)
    fun updateUser(user: User) {
        connect().use { c ->
            c.prepareStatement("update user set failed_logins = ?, salt = ?, salted_hash = ? where user_name = ?").use {
                it.setInt(1, user.failedLogins)
                it.setBytes(2, user.salt)
                it.setBytes(3, user.saltedHash)
                it.setString(4, user.userName)
                it.executeUpdate()
            }
        }
    }

    @Throws(SQLException::class)
    fun createQuickAuth(user: User, authKey: String) {
        connect().use { c ->
            c.prepareStatement("insert into quick_auth(user_name, auth_key) values (?, ?)").use {
                it.setString(1, user.userName)
                it.setString(2, authKey)
                it.executeUpdate()
            }
            // delete all but 3 latest records
            var quickAuthKeys: Int = 0
            c.prepareStatement("select count(1) from quick_auth where user_name = ?").use {
                it.setString(1, user.userName)
                val rs = it.executeQuery()
                if (rs.next()) {
                    quickAuthKeys = rs.getInt(1)
                }
            }
            if (quickAuthKeys > 3) {
                c.prepareStatement("delete from exhibit.quick_auth where user_name = ? order by date_created asc limit ?").use {
                    it.setString(1, user.userName)
                    it.setInt(2, quickAuthKeys - 3)
                    it.executeUpdate()
                }
            }
        }
    }

    @Throws(SQLException::class)
    fun retrieveUserForQuickAuth(authKey: String): String? {
        var userName: String? = null
        connect().use { c ->
            c.prepareStatement("select user_name from quick_auth where auth_key = ?").use {
                it.setString(1, authKey)
                val rs = it.executeQuery()
                if (rs.next()) {
                    userName = rs.getString(1)
                }
            }
        }
        return userName
    }
}
