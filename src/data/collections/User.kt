package com.crushtech.cgpa.data.collections

import data.collections.GradeClass
import data.collections.SemesterRequests
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val email: String,
    var password: String,
    var username: String,
    val numbersOfPdfDownloads: Int,
    val gradePoints: GradeClass = GradeClass(),
    var acceptedSemestersRequests: List<SemesterRequests> = listOf(),
    var pendingSemesterRequests: List<SemesterRequests> = listOf(),
    var rejectedSemesterRequests: List<SemesterRequests> = listOf(),
    @BsonId
    val id: String = ObjectId().toString()
) {
    var allList: List<SemesterRequests>
        get() = acceptedSemestersRequests + pendingSemesterRequests + rejectedSemesterRequests
        set(value) {}

//    fun getAllSemesterRequestList(): List<SemesterRequests> {
//        return acceptedSemestersRequests + pendingSemesterRequests + rejectedSemesterRequests
//    }

}

data class ThirdPartyLoginUser(
    val email: String,
    val username: String,
    val numbersOfPdfDownloads: Int,
    val gradePoints: GradeClass = GradeClass(),
    var acceptedSemestersRequests: List<SemesterRequests> = listOf(),
    var pendingSemesterRequests: List<SemesterRequests> = listOf(),
    var rejectedSemesterRequests: List<SemesterRequests> = listOf(),
    @BsonId
    val id: String = ObjectId().toString()
) {
    var allList: List<SemesterRequests>
        get() = acceptedSemestersRequests + pendingSemesterRequests + rejectedSemesterRequests
        set(value) {}
}