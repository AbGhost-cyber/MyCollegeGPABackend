package com.crushtech.cgpa.data.request

data class ThirdPartyAuthRequest(
    val email: String,
    val username: String = "user"
)