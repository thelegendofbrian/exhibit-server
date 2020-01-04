package minepop.exhibit.admin

import minepop.exhibit.dao.DAO
import java.sql.PreparedStatement
import java.sql.SQLException

class AdminDAO: DAO() {

	fun createUser(userName: String, salt: ByteArray, saltedHash: ByteArray) {
        connect().use { c ->
            var userId: Long? = null
            c.prepareStatement("insert into user(name, salt, salted_hash) values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS).use {
                it.setString(1, userName)
                it.setBytes(2, salt)
                it.setBytes(3, saltedHash)
                it.executeUpdate()
                var keys = it.generatedKeys
                if (keys.next()) {
                    userId = keys.getLong(1)
                }
            }
            c.prepareStatement("insert into user_settings(user_id) values(?)").use {
                it.setLong(1, userId!!)
                it.executeUpdate()
            }
        }
	}

	fun updateUserCredentials(userName: String, salt: ByteArray, saltedHash: ByteArray) {
        connect().use { c ->
            c.prepareStatement("update user set salt = ?, salted_hash = ? where name = ?").use {
                it.setBytes(1, salt)
                it.setBytes(2, saltedHash)
                it.setString(3, userName)
                it.executeUpdate()
            }
        }
	}

	fun updateUserFailedLogins(userName: String, failedLogins: Int) {
        connect().use { c ->
            c.prepareStatement("update user set failed_logins = ? where name = ?").use {
                it.setInt(1, failedLogins)
                it.setString(2, userName)
                it.executeUpdate()
            }
        }
	}

	fun deleteUser(userName: String) {
        connect().use { c ->
            c.prepareStatement("delete from user where name = ?").use {
                it.setString(1, userName)
                it.executeUpdate()
            }
        }
	}
}
