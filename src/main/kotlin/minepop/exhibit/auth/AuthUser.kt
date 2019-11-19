package minepop.exhibit.auth

import minepop.exhibit.user.UserSettings

data class AuthUser(var id: Long, var name: String, var failedLogins: Int, var salt: ByteArray, var saltedHash: ByteArray, var userSettings: UserSettings)

fun AuthUser.newSession(): ExhibitSession {
    return ExhibitSession(id, name, userSettings.timezone!!, userSettings.defaultGroupId)
}