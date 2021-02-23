package com.crushtech.cgpa.data.request

import data.collections.SemesterRequests

data class AcceptSemesterRequest(
    val semesterRequests: SemesterRequests,
    val receiver: String
)