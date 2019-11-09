package minepop.exhibit.auth

import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.form
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import minepop.exhibit.Crypto
import minepop.exhibit.ExhibitSession
import java.util.*

private val dao = SessionAuthDAO()

fun Authentication.Configuration.installExhibitAuth() {
    form(name = "Form") {
        userParamName = "username"
        passwordParamName = "password"
        skipWhen { it.sessions.get<ExhibitSession>() != null }
        validate { credentials ->
            val user = dao.retrieveUser(credentials.name)
            if (user == null || user.failedLogins >= 5) {
                return@validate null
            }
            val quickAuth = request.cookies["Quick-Auth"]
            if (quickAuth != null) {
                val keys = dao.retrieveQuickAuthKeys(user)
                if (keys.contains(quickAuth))
                    return@validate UserIdPrincipal(credentials.name)
            }
            val digest = Crypto.hash(credentials.password.toCharArray(), user.salt)
            if (digest.contentEquals(user.saltedHash)) {
                sessions.set(ExhibitSession(credentials.name))
                val quickAuthKey = UUID.randomUUID().toString()
                response.cookies.append("Quick-Auth", quickAuthKey, maxAge = 2000000000, secure = true, httpOnly = true)
                dao.createQuickAuth(user, quickAuthKey)
                return@validate UserIdPrincipal(credentials.name)
            }
            else {
                return@validate null
            }
        }
    }
}