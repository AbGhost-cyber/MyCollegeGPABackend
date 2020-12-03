package com.crushtech.cgpa.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserPdfDownloads(
    val noOfPdfDownloads: Int = 0,
    @BsonId
    val id: String = ObjectId().toString()
)