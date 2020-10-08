package com.crushtech.cgpa.data.request

data class SignInRequest(
        val email:String,
        var password:String,
        var username:String
)