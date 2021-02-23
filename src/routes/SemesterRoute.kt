package com.crushtech.cgpa.routes


import com.crushtech.cgpa.data.*
import com.crushtech.cgpa.data.collections.OneSignalConfig
import com.crushtech.cgpa.data.collections.Semester
import com.crushtech.cgpa.data.collections.UserPdfDownloads
import com.crushtech.cgpa.data.collections.response.SimpleResponse
import com.crushtech.cgpa.data.request.*
import data.collections.GradeClass
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.contentType
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun Route.semesterRoute() {
    route("/getSemester") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val semesters = getSemesterForUser(email)
                call.respond(OK, semesters)
            }
        }
    }
    route("/getUserGradePoints") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val gradePoints = getUserGradePointsPattern(email)
                call.respond(OK, gradePoints)
            }
        }
    }


    route("/editUserGradePoints") {
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<GradeClass>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (upsertUserGradePointsPattern(request, email)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }


    route("/resetUserGradePoints") {
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                try {
                    call.receive<GradeClass>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (resetUserGradePointsPattern(email)) {
                    val gradePoints = getUserGradePointsPattern(email)
                    call.respond(OK, gradePoints)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }


    route("/deleteSemester") {
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<DeleteSemesterRequest>()

                    //throw an error when content cannot be transformed to the desired type.
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (deleteSemesterForSpecificUser(email, request.id)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/deleteCourse") {
        authenticate {
            post {
                val request = try {
                    call.receive<DeleteCourseRequest>()

                    //throw an error when content cannot be transformed to the desired type.
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (deleteCourse(request.id, request.semesterId)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }


    route("/addSemester") {
        authenticate {
            post {
                val semester = try {
                    call.receive<Semester>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (saveSemester(semester)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }


    route("/addCourseToSemester") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddCourseRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (addCourseToSemester(request.semesterId, request.course)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "course added successfully"
                        )
                    )
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/updateAddedCourse") {
        authenticate {
            post {
                val request = try {
                    call.receive<UpdateCourseRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (editAddedCourse(request.semesterId, request.course, request.coursePosition)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "course updated"
                        )
                    )
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/getSemReqLists") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val allLists = getAllSemRequestList(email)
                call.respond(OK, allLists)
            }
        }
    }

    route("/acceptSharedSemester") {
        authenticate {
            post {
                val request = try {
                    call.receive<AcceptSemesterRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (isOwnerOfSemester(request.semesterRequests.semesterId, request.receiver)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "you already own this semester"
                        )
                    )
                    return@post
                }
                if (acceptSharedSemester(request.semesterRequests, request.receiver)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "Semester accepted, check the semester screen to view it"
                        )
                    )
                    val username = findUsernameWithEmail(request.semesterRequests.owner)
                    sendNotification(
                        request.semesterRequests.owner,
                        "${request.receiver} accepted your semester sharing 😉",
                        "Hey $username "
                    )
                    return@post
                }
            }
        }
    }

    route("/rejectSharedSemester") {
        authenticate {
            post {
                val request = try {
                    call.receive<RejectSemesterRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (isOwnerOfSemester(request.semesterRequests.semesterId, request.receiver)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "unable to reject, you already own this semester"
                        )
                    )
                    return@post
                }
                if (rejectSharedSemester(request.semesterRequests, request.receiver)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "semester rejected successfully"
                        )
                    )
                    val username = findUsernameWithEmail(request.semesterRequests.owner)
                    sendNotification(
                        request.semesterRequests.owner,
                        "${request.receiver} rejected your semester sharing 😰",
                        "Hey $username 😨"
                    )
                    return@post
                }
            }
        }
    }

    route("/addUserToSemester") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddUserToSemesterRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (isOwnerOfSemester(
                        request.semesterRequests.semesterId,
                        request.receiver
                    )
                ) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "This user is already an owner of this semester"
                        )
                    )
                    return@post
                }
                if (!checkIfUserExistsInAllUsersCollections(request.receiver)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "No user with this E-mail exists"
                        )
                    )
                    return@post
                }
                if (addUserToSemester(
                        request.semesterRequests,
                        request.receiver
                    )
                ) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "semester shared," +
                                    "please wait for the receiver to accept"
                        )
                    )
                    val username = findUsernameWithEmail(request.receiver)
                    sendNotification(
                        request.receiver,
                        content = "A semester was shared to you by " +
                                request.semesterRequests.owner,
                        heading = "Hey, $username"
                    )

                } else {
                    call.respond(
                        OK, SimpleResponse(
                            true,
                            "an unknown error occurred"
                        )
                    )
                }
            }
        }
    }

    route("/getPdfDownloads") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val userPdfDownloads = checkUserPdfDownloads(email)
                if (userPdfDownloads.noOfPdfDownloads <= 0) {
                    call.respond(
                        SimpleResponse(
                            false,
                            "you ran out of download coins,please purchase some"
                        )
                    )
                } else {
                    call.respond(OK, userPdfDownloads)
                }
            }
        }
    }

    route("/addUserPdfDownloadsCount") {
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<UserPdfDownloads>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }

                if (upsertUserPdfDownloads(request, email)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "successfully purchased"
                        )
                    )
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/deleteSemRequests") {
        authenticate {
            post {
                val request = try {
                    call.receive<DeleteSemRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (deleteSemReq(request.semReqId, request.email)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "semester request deleted"
                        )
                    )
                }
            }
        }
    }
}

@KtorExperimentalAPI
private fun sendNotification(
    receiver: String,
    content: String,
    heading: String
) {
    runBlocking {
        val client = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    serializeNulls()
                    disableHtmlEscaping()
                }
            }
        }
        client.post<OneSignalConfig> {
            url("https://onesignal.com/api/v1/notifications/")
            contentType(ContentType.Application.Json)
            header("Authorization", "Basic NTY3Y2YwMWEtNTZmNi00Y2VjLThmYWUtODg5M2Q5ZDZkMDBh")
            body = OneSignalConfig(
                "ab830271-41ae-4e33-a673-23414a8c9ba2",
                contents = OneSignalConfig.Contents(en = content),
                headings = OneSignalConfig.Headings(en = heading),
                channel_for_external_user_ids = "push",
                include_external_user_ids = listOf(receiver)
            )
        }
    }
}
