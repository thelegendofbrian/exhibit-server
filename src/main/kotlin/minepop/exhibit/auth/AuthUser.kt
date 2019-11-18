package minepop.exhibit.auth

data class AuthUser(var id: Long, var name: String, var failedLogins: Int, var salt: ByteArray, var saltedHash: ByteArray)