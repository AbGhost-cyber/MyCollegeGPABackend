package com.crushtech.cgpa.data.request

import com.crushtech.cgpa.data.collections.Courses

data class updateCourseRequest(
    val semesterId: String,
    val course: Courses,
    val coursePosition: Int
)