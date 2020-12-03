package com.crushtech.cgpa.data.collections


import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import kotlin.math.roundToInt


data class Semester(
    val courses:List<Courses>,
    val owners: List<String>,
    val semesterName:String,
    @BsonId
val id:String = ObjectId().toString()
) {
    fun getGPA(): Double {
        var totalHours = 0F
        var totalPoints = 0F

        courses.forEach {
            totalHours += it.creditHours
            totalPoints += it.getQualityPoints()
        }
        val GPA = totalPoints / totalHours
        if(GPA.isNaN()){
            return 0.00
        }
        return (GPA * 1000.0).roundToInt() / 1000.0
    }

    fun getThreeCoursesName(): String {
        val threeCourses = ArrayList<String>()
        courses.forEach {
            if (threeCourses.size < 3) {
                threeCourses.add(it.courseName)
            }
        }
        return threeCourses.joinToString(", ")
    }
}

fun getCGPA(semester:Triple<Semester,Semester,Semester>):Double{
    val firstSemesterGPA = semester.first.getGPA()
    val secondSemesterGPA = semester.second.getGPA()
    val thirdSemesterGPA = semester.third.getGPA()
    val totalGPA = firstSemesterGPA + secondSemesterGPA + thirdSemesterGPA
    return totalGPA/3
}

