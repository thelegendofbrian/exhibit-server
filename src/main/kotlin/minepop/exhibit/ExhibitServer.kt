package minepop.exhibit

import com.google.gson.JsonObject
import java.nio.file.Paths
import java.nio.file.Files

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import minepop.exhibit.auth.installExhibitAuth
import minepop.exhibit.checkin.checkinRoutes
import java.io.File
import java.security.KeyStore

val conf = AppConfig()
val prod = Files.exists(Paths.get(conf.getKeystorePath()))

fun main() {
    val env = applicationEngineEnvironment {
        module {
            module()
        }
        if (prod) {
            val path = File(conf.getKeystorePath())
            val passwd = conf.getKeystorePasswd().toCharArray()
            sslConnector(keyStore = KeyStore.getInstance(path, passwd), keyAlias = conf.getKeystoreAlias(),
                keyStorePassword = { passwd },
                privateKeyPassword = { passwd }) {
                port = conf.getPort()
                keyStorePath = path
            }
        } else {
            connector {
                host = "0.0.0.0"
                port = conf.getPort()
            }
        }
    }
    embeddedServer(Netty, env).start(true)
}

data class ExhibitSession(val username: String)

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
            // Configure Gson here
        }
    }

    install(Sessions) {
        cookie<ExhibitSession>("SESSION", storage = SessionStorageMemory())
    }

    install(Authentication) {
        installExhibitAuth()
    }

    routing {
        if (!prod) {
            options("/*") {
                val accessControlRequestMethod = call.request.headers["Access-Control-Request-Method"]
                if (accessControlRequestMethod != null) {
                    call.response.header("Access-Control-Allow-Methods", accessControlRequestMethod)
                }
            }
        }

        static("/") {
            files("public")
            default("public/index.html")
        }

        intercept(ApplicationCallPipeline.Features) {
            call.response.headers.append("Access-Control-Allow-Origin", if (prod) "https://" + conf.getHost() else "http://localhost:3000")
            if (!prod) {
                call.response.headers.append("Access-Control-Allow-Credentials", "true")
            }
        }

        authenticate("Form") {
            post("login") {
                val userName = call.sessions.get<ExhibitSession>()?.username
                if (userName != null) {
                    val body = JsonObject()
                    val user = JsonObject()
                    body.add("user", user)
                    user.addProperty("name", userName)
                    call.respond(body)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            checkinRoutes()
        }
    }
}