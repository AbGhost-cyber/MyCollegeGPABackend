package com.crushtech.cgpa.routes

import com.crushtech.cgpa.data.checkPasswordForEmail
import com.crushtech.cgpa.data.collections.response.SimpleResponse
import com.crushtech.cgpa.data.findUsernameWithEmail
import com.crushtech.cgpa.data.request.LogInRequest
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.loginRoute() {
    route("/login") {
        post {
            val request = try {
                call.receive<LogInRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val isPasswordCorrect = checkPasswordForEmail(
                request.email, request.password
            )
            if (isPasswordCorrect) {
                val username = findUsernameWithEmail(request.email)
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, username)
                )
            } else {
                call.respond(
                    HttpStatusCode.OK, SimpleResponse(false,
                    "the email or password is incorrect"))
            }
        }
    }
}