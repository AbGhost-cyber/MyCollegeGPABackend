package com.crushtech.cgpa.data.request

import data.collections.SemesterRequests

data class RejectSemesterRequest(
    val semesterRequests: SemesterRequests,
    val receiver: String
)