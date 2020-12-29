package com.crushtech.cgpa.routes

import com.crushtech.cgpa.data.checkIfUserExistsInAllUsersCollections
import com.crushtech.cgpa.data.checkPasswordForEmail
import com.crushtech.cgpa.data.collections.ThirdPartyLoginUser
import com.crushtech.cgpa.data.collections.response.SimpleResponse
import com.crushtech.cgpa.data.findUsernameWithEmail
import com.crushtech.cgpa.data.registerThirdPartyLoginUser
import com.crushtech.cgpa.data.request.LogInRequest
import com.crushtech.cgpa.data.request.ThirdPartyAuthRequest
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
                    HttpStatusCode.OK, SimpleResponse(
                        false,
                        "the email or password is incorrect"
                    )
                )
            }
        }
    }
    route("/third_party_login") {
        post {
            val request = try {
                call.receive<ThirdPartyAuthRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userExist = checkIfUserExistsInAllUsersCollections(request.email)
            if (!userExist) {
                //if the user tries to login via third party but his info doesn't exist
                //create it and respond with it's username
                if (registerThirdPartyLoginUser(
                        ThirdPartyLoginUser(
                            request.email,
                            request.username, 0
                        )
                    )
                ) {
                    val username = findUsernameWithEmail(request.username)
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(true, username)
                    )
                }
            } else {
                val username = findUsernameWithEmail(request.username)
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, username)
                )
            }
        }
    }
}