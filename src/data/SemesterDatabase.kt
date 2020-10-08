package com.crushtech.cgpa.data


import com.crushtech.cgpa.data.collections.Courses
import com.crushtech.cgpa.data.collections.Semester
import com.crushtech.cgpa.data.collections.User
import com.crushtech.cgpa.security.checkHashForPassword
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("SemesterDatabase")
private val users = database.getCollection<User>()
private val semesters = database.getCollection<Semester>()


suspend fun registerUser(user: User): Boolean {
    return users.insertOne(user).wasAcknowledged()
}

suspend fun checkIfUserExists(email: String): Boolean {
    return users.findOne(User::email eq email) != null
}
suspend fun checkPasswordForEmail(email: String, passwordToCheck: String): Boolean {
    val actualPassword = users.findOne(User::email eq email)?.password ?: return false
    return checkHashForPassword(passwordToCheck, actualPassword)
}
suspend fun findUsernameWithEmail(email:String):String{
    return users.findOne(User::email eq email)!!.username
}

suspend fun getSemesterForUser(email: String): List<Semester> {
    return semesters.find(Semester::owners contains email).toList()
}

suspend fun saveSemester(semester: Semester): Boolean {
    val semesterExists = semesters.findOneById(semester.id) != null
    return if (semesterExists) {
        semesters.updateOneById(
            semester.id, semester
        ).wasAcknowledged()
    } else {
        semesters.insertOne(semester).wasAcknowledged()
    }

}


suspend fun deleteSemesterForSpecificUser(email: String, semesterId: String): Boolean {
    val semester = semesters.findOne(Semester::id eq semesterId,
       Semester::owners contains email)
   semester?.let { mySemester ->
        if (mySemester.owners.size > 1) {
            //the semester has multiple owners, so we just delete the email
            // from the owners list
            val newOwners = mySemester.owners - email
            val updateResult = semesters.updateOne(mySemester::id eq mySemester.id,
                setValue(Semester::owners, newOwners)
            )
            return updateResult.wasAcknowledged()
        }
        //ELSE THE NUMBERS OF OWNERS IS 1, SO WE CAN DELETE THE ENTIRE COLLECTION
        return semesters.deleteOneById(semesterId).wasAcknowledged()
    } ?: return false
}

suspend fun deleteCourse(courseId: String,semesterId: String):Boolean{
   val semester = semesters.findOne(Semester::id eq semesterId)
    semester?.let {mySemester->
       val courses = mySemester.courses
        courses.forEach {
            if(it.id == courseId){
                val newCourseList = courses - it
                val updateResult = semesters.updateOne(mySemester::id eq mySemester.id,
                    setValue(Semester::courses, newCourseList)
                )
                return updateResult.wasAcknowledged()
            }
        }
    }
    return false
}
suspend fun addOwnerToSemester(semesterId: String, owner: String): Boolean {
    val owners = semesters.findOneById(semesterId)?.owners ?: return false
    return semesters.updateOneById(semesterId, setValue(Semester::owners,
        owners + owner)).wasAcknowledged()
}

suspend fun addCourseToSemester(semesterId: String,course: Courses): Boolean {
    val semester = semesters.findOne(Semester::id eq semesterId)
    semester?.let {mySemester->
        val courses = mySemester.courses

       val newCourseList = courses + course
        val updateResult = semesters.updateOne(mySemester::id eq mySemester.id,
            setValue(Semester::courses, newCourseList)
        )
        return updateResult.wasAcknowledged()
    }
    return false
}


suspend fun isOwnerOfSemester(semesterId: String, owner: String):Boolean{
    val semester = semesters.findOneById(semesterId) ?: return false
    return owner in semester.owners
}

