package minepop.exhibit.member

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.settingsRoutes() {
    get("/") {
        val view = JsonObject()
        val stats = JsonArray()
        stats.add("dayStreak")
        stats.add("adherence")
        stats.add("bonusCheckins")
        stats.add("totalCheckins")
        view.add("stats", stats)
        call.respond(view)
    }
}