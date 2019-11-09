package minepop.exhibit.auth

import minepop.exhibit.dao.DAO
import java.sql.SQLException

class SessionAuthDAO : DAO() {

    @Throws(SQLException::class)
    fun retrieveUser(userName: String): User? {
        connect().use {
            c ->
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
        connect().use {
            c ->
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
        connect().use {
            c ->
            c.prepareStatement("insert into quick_auth(user_name, auth_key) values (?, ?)").use {
                it.setString(1, user.userName)
                it.setString(2, authKey)
                it.executeUpdate()
            }
            // delete all but 3 latest records
            c.prepareStatement("delete from quick_auth where auth_key not in (" +
                    "select auth_key from quick_auth where user_name = ? order by date_created desc fetch first 3 rows only)").use {
                it.setString(1, user.userName)
                it.executeUpdate()
            }
        }
    }

    @Throws(SQLException::class)
    fun retrieveQuickAuthKeys(user: User): List<String> {
        val keys = mutableListOf<String>()
        connect().use {
            c ->
            c.prepareStatement("select auth_key from quick_auth where user_name = ?").use {
                it.setString(1, user.userName)
                val rs = it.executeQuery()
                while (rs.next()) {
                    keys.add(rs.getString(1))
                }
            }
        }
        return keys
    }
}
