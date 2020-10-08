package com.crushtech.cgpa.data.request

import com.crushtech.cgpa.data.collections.Courses

data class AddCourseRequest(
        val semesterId:String,
        val course:Courses
)