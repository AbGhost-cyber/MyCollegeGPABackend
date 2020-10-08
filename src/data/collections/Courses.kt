package com.crushtech.cgpa.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Courses(
    val courseName: String,
    val creditHours: Float,
    val grade: String,
    var color: String,
    val semesterId:String,
    @BsonId
    val id:String = ObjectId().toString()
){
    fun getQualityPoints():Float{
        return creditHours * getGradePoints()
    }

    private fun getGradePoints():Float{
        return when(grade){
            "A"->4F
            "B+"->3.5F
            "B-"->3.0F
            "C+"->2.5F
            "C-"->2.0F
            "D+"->1.5F
            "D-"->1.0F
            "F"->0.0F
            else->0.0F
        }
    }
}
