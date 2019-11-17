package minepop.exhibit.auth

data class User(var id: Long, var name: String, var failedLogins: Int, var salt: ByteArray, var saltedHash: ByteArray)