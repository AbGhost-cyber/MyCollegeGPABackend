package com.crushtech.cgpa.routes


import com.crushtech.cgpa.data.*
import com.crushtech.cgpa.data.collections.Semester
import com.crushtech.cgpa.data.collections.UserPdfDownloads
import com.crushtech.cgpa.data.collections.response.SimpleResponse
import com.crushtech.cgpa.data.request.*
import data.collections.GradeClass
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

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
                    call.receive<updateCourseRequest>()
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

    route("/addOwnerToSemester") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddOwnerRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (!checkIfUserExistsInAllUsersCollections(request.owner)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "No user with this E-mail exists"
                        )
                    )
                    return@post
                }
                if (isOwnerOfSemester(
                        request.semesterId,
                        request.owner
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
                if (addOwnerToSemester(request.semesterId, request.owner)) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "${request.owner} can now see this semester"
                        )
                    )
                } else {
                    call.respond(Conflict)
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
}

