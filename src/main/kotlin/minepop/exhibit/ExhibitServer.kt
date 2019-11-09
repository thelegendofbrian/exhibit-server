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
import io.ktor.sessions.*
import minepop.exhibit.auth.installExhibitAuth
import minepop.exhibit.checkin.checkinRoutes

val prod = Files.exists(Paths.get("letsencrypt.jks"))

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

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
            default("index.html")
        }

        authenticate("Form") {
            post("login") {
                val userName = call.sessions.get<ExhibitSession>()?.username
                if (userName != null) {
                    val body = JsonObject()
                    body.addProperty("username", userName)
                    call.respond(body)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            checkinRoutes()
        }
    }
}