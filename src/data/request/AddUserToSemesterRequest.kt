package com.crushtech.cgpa.data.request

import data.collections.SemesterRequests

data class AddUserToSemesterRequest(
    val semesterRequests: SemesterRequests,
    val receiver: String
)