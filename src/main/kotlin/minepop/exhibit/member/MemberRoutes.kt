package minepop.exhibit.member

import io.ktor.routing.*

fun Route.memberRoutes() {
    route("member") {

        memberGroupRoutes()

        route("{groupId}") {
            route("statistics") {
                memberStatisticsRoutes()
            }

            route("checkin") {
                checkinRoutes()
            }
            route("schedule") {
                scheduleRoutes()
            }
            route("activity") {
                activityRoutes()
            }
            route("settings") {
                settingsRoutes()
            }
            route("text") {
                textRoutes()
            }
        }
    }
}