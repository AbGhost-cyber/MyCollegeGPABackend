package com.crushtech.cgpa.data.collections

import data.collections.GradeClass
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val email: String,
    var password: String,
    var username: String,
    val numbersOfPdfDownloads: Int,
    val gradePoints: GradeClass = GradeClass(),
    @BsonId
    val id: String = ObjectId().toString()
)

data class ThirdPartyLoginUser(
    val email: String,
    val username: String,
    val numbersOfPdfDownloads: Int,
    val gradePoints: GradeClass = GradeClass(),
    @BsonId
    val id: String = ObjectId().toString()
)