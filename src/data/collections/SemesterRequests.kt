package data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class SemesterRequests(
    val owner: String,
    val semesterId: String,
    var state: STATE = STATE.PENDING,
    @BsonId
    val id: String = ObjectId().toString()
)

enum class STATE {
    ACCEPTED, PENDING, REJECTED
}

