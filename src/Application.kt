package com.crushtech.cgpa

import com.crushtech.cgpa.data.checkPasswordForEmail
import com.crushtech.cgpa.routes.loginRoute
import com.crushtech.cgpa.routes.registerRoute
import com.crushtech.cgpa.routes.semesterRoute
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

//@KtorExperimentalAPI
//fun main(args: Array<String>) {
//    val server = embeddedServer(
//        Netty,
//        port = 8080,
//        module = Application::module
//    ).apply {
//        start(wait = false)
//    }

//    runBlocking {
//        val client = HttpClient(CIO) {
//            install(JsonFeature) {
//                serializer = GsonSerializer {
//                    // .GsonBuilder
//                    serializeNulls()
//                    disableHtmlEscaping()
//                }
//            }
//        }
//
//        val message = client.post<OneSignalConfig> {
//            url("https://onesignal.com/api/v1/notifications/")
//            contentType(ContentType.Application.Json)
//            header("Authorization", "Basic NTY3Y2YwMWEtNTZmNi00Y2VjLThmYWUtODg5M2Q5ZDZkMDBh")
//            body = OneSignalConfig(
//                "ab830271-41ae-4e33-a673-23414a8c9ba2",
//                contents = OneSignalConfig.Contents(en = "English Message"),
//                headings = OneSignalConfig.Headings(en = "English Title"),
//                channel_for_external_user_ids = "push",
//                include_external_user_ids = listOf("xplendo@gmail.com")
//            )
//        }
//
//
//        client.close()
//       server.stop(300L, 300L, TimeUnit.SECONDS)
//    }
//}

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    install(Authentication) {
        configureAuth()
    }
    install(Routing) {
        registerRoute()
        loginRoute()
        semesterRoute()
    }


}

private fun Authentication.Configuration.configureAuth() {

    basic {
        realm = "Semester Server"
        validate { credentials ->
            val email = credentials.name
            val password = credentials.password
            if (password.isNotEmpty()) {
                checkPasswordForEmail(email, password)
                UserIdPrincipal(email)
            } else {
                UserIdPrincipal(email)
            }
        }
    }
}


//data class Filters(
//    val field: String,
//    val key: String,
//    val relation: String,
//    val value: Int
//)


