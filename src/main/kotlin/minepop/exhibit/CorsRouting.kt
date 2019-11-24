package minepop.exhibit

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.response.cacheControl
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.options

fun Route.corsRouting() {
    if (!prod) {
        options("/{url...}") {
            call.request.headers["Access-Control-Request-Method"]?.let {
                call.response.header("Access-Control-Allow-Methods", it)
            }
            call.respond(HttpStatusCode.OK)
        }
    }

    intercept(ApplicationCallPipeline.Features) {
        call.response.headers.append("Access-Control-Allow-Origin", if (prod) "https://${conf.getHost()}" else "http://localhost:${conf.getOriginPort()}")
        call.response.cacheControl(CacheControl.NoStore(CacheControl.Visibility.Private))
        if (!prod) {
            call.response.headers.append("Access-Control-Allow-Credentials", "true")
            call.response.headers.append("Access-Control-Allow-Headers", "timezone, content-type")
        }
    }
}