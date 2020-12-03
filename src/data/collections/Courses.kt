package com.crushtech.cgpa.data.collections

import data.collections.GradeClass
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Courses(
    val courseName: String,
    val creditHours: Float,
    val grade: String,
    var color: String,
    val semesterId: String,
    var gradesPoints: List<GradeClass> = listOf(),
    @BsonId
    val id: String = ObjectId().toString()
) {
    fun getQualityPoints(): Float {
        return creditHours * getGradePoints()
    }

    private fun getGradePoints(): Float {
        gradesPoints.forEach { gradesPoints ->
            return when (grade) {
                "A+" -> gradesPoints.APlusGrade
                "A-" -> gradesPoints.AMinusGrade
                "B+" -> gradesPoints.BPlusGrade
                "B" -> gradesPoints.BGrade
                "B-" -> gradesPoints.BMinusGrade
                "C+" -> gradesPoints.CPlusGrade
                "C" -> gradesPoints.CGrade
                "C-" -> gradesPoints.CMinusGrade
                "D+" -> gradesPoints.DPlusGrade
                "D" -> gradesPoints.DGrade
                "E/F" -> gradesPoints.FOrEGrade
                else -> gradesPoints.FOrEGrade
            }
        }
        return 0.0F
    }
}


