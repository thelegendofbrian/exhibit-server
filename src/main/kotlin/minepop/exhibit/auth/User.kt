package minepop.exhibit.auth

data class User(var userName: String, var failedLogins: Int, var salt: ByteArray, var saltedHash: ByteArray)