package com.crushtech.cgpa

import com.crushtech.cgpa.data.checkPasswordForEmail
import com.crushtech.cgpa.routes.loginRoute
import com.crushtech.cgpa.routes.registerRoute
import com.crushtech.cgpa.routes.semesterRoute
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.Routing

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Authentication){
        configureAuth()
    }
    install(Routing){
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
            if (checkPasswordForEmail(email, password)) {
                UserIdPrincipal(email)
            } else null

        }
    }

}

