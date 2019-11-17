package minepop.exhibit

import java.nio.file.Paths
import java.nio.file.Files

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import minepop.exhibit.auth.ExhibitSession
import minepop.exhibit.auth.installExhibitAuth
import minepop.exhibit.auth.loginRoute
import minepop.exhibit.checkin.checkinRoutes
import minepop.exhibit.schedule.scheduleRoutes

val conf = AppConfig()
val prod = Files.exists(Paths.get(conf.getKeystorePath()))

fun main(args: Array<String>) {
    if (prod) {
        io.ktor.server.netty.EngineMain.main(args)
    } else {
        val env = applicationEngineEnvironment {
            module {
                module()
            }
            connector {
                host = "0.0.0.0"
                port = conf.getPort()
            }
        }
        embeddedServer(Netty, env).start(true)
    }
}

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

        static("/") {
            files("public")
            default("public/index.html")
        }

        corsRouting()

        authenticate("Form") {
            loginRoute()
            checkinRoutes()
            scheduleRoutes()
        }
    }
}