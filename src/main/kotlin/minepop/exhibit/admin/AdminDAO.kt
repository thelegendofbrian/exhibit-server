package minepop.exhibit.admin

import minepop.exhibit.dao.DAO
import java.sql.SQLException

class AdminDAO: DAO() {

    @Throws(SQLException::class)
	fun createUser(userName: String, salt: ByteArray, saltedHash: ByteArray) {
        connect().use { c ->
            c.prepareStatement("insert into user(user_name, salt, salted_hash) values (?, ?, ?)").use {
                it.setString(1, userName)
                it.setBytes(2, salt)
                it.setBytes(3, saltedHash)
                it.executeUpdate()
            }
        }
	}

    @Throws(SQLException::class)
	fun updateUserCredentials(userName: String, salt: ByteArray, saltedHash: ByteArray) {
        connect().use { c ->
            c.prepareStatement("update user set salt = ?, salted_hash = ? where user_name = ?").use {
                it.setBytes(1, salt)
                it.setBytes(2, saltedHash)
                it.setString(3, userName)
                it.executeUpdate()
            }
        }
	}

    @Throws(SQLException::class)
	fun updateUserFailedLogins(userName: String, failedLogins: Int) {
        connect().use { c ->
            c.prepareStatement("update user set failed_logins = ? where user_name = ?").use {
                it.setInt(1, failedLogins)
                it.setString(2, userName)
                it.executeUpdate()
            }
        }
	}

    @Throws(SQLException::class)
	fun deleteUser(userName: String) {
        connect().use { c ->
            c.prepareStatement("delete from user where user_name = ?").use {
                it.setString(1, userName)
                it.executeUpdate()
            }
        }
	}
}
