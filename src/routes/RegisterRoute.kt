package com.crushtech.cgpa.routes

import com.crushtech.cgpa.data.checkIfUserExists
import com.crushtech.cgpa.data.collections.User
import com.crushtech.cgpa.data.collections.response.SimpleResponse
import com.crushtech.cgpa.data.registerUser
import com.crushtech.cgpa.data.request.SignInRequest
import com.crushtech.cgpa.security.getHashWithSalt
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route


fun Route.registerRoute() {
    route("/register") {
        post {
            val request = try {
                call.receive<SignInRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(BadRequest)
                return@post
            }
            //check if account exists already
            val userExist = checkIfUserExists(request.email)
            if (!userExist) {
                if (registerUser(
                        User(
                            request.email, getHashWithSalt(request.password),
                            request.username, 0
                        )
                    )
                ) {
                    call.respond(
                        OK, SimpleResponse(
                            true,
                            "Account Created Successfully"
                        )
                    )
                } else {
                    call.respond(
                        OK, SimpleResponse(
                            false, "An unknown occurred"
                        )
                    )
                }
            } else {
                call.respond(OK, SimpleResponse(
                        false, "A user with that email already exists"))
            }
        }
    }
}

