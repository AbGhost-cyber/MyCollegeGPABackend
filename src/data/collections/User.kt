package com.crushtech.cgpa.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val email:String,
    var password:String,
    var username:String,
    @BsonId
    val id:String = ObjectId().toString()
) {
}