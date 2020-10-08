package com.crushtech.cgpa.routes

import com.crushtech.cgpa.data.*
import com.crushtech.cgpa.data.collections.Courses
import com.crushtech.cgpa.data.collections.Semester
import com.crushtech.cgpa.data.collections.response.SimpleResponse
import com.crushtech.cgpa.data.request.AddCourseRequest
import com.crushtech.cgpa.data.request.AddOwnerRequest
import com.crushtech.cgpa.data.request.DeleteCourseRequest
import com.crushtech.cgpa.data.request.DeleteSemesterRequest
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

    route("/deleteSemester"){
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try{
                    call.receive<DeleteSemesterRequest>()

                    //throw an error when content cannot be transformed to the desired type.
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if(deleteSemesterForSpecificUser(email,request.id)){
                    call.respond(OK)
                }else{
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/deleteCourse"){
        authenticate {
            post {
               // val email = call.principal<UserIdPrincipal>()!!.name
                val request = try{
                    call.receive<DeleteCourseRequest>()

                    //throw an error when content cannot be transformed to the desired type.
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if(deleteCourse(request.id,request.semesterId)){
                    call.respond(OK)
                }else{
                    call.respond(Conflict)
                }
            }
        }
    }


    route("/addSemester"){
        authenticate {
            post {
                val semester = try{
                    call.receive<Semester>()
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if (saveSemester(semester)){
                    call.respond(OK)
                }else{
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/addCourseToSemester"){
        authenticate {
            post {
                val request = try {
                    call.receive<AddCourseRequest>()
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if(addCourseToSemester(request.semesterId,request.course)){
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "course added successfully"
                        )
                    )
                }else{
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/addOwnerToSemester"){
        authenticate {
            post {
                val request = try{
                    call.receive<AddOwnerRequest>()
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if(!checkIfUserExists(request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "No user with this E-mail exists"
                        )
                    )
                    return@post
                }
                if(isOwnerOfSemester(request.SemesterId,request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "This user is already an owner of this note"
                        )
                    )
                    return@post
                }
                if (addOwnerToSemester(request.SemesterId,request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "${request.owner} can now see this note"
                        )
                    )
                }else{
                    call.respond(Conflict)
                }
            }
        }
    }
}

