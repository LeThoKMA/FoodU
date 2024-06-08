package com.example.footu.Response

import android.os.Parcelable
import com.example.footu.model.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageResponse(
    val messageId: Long?,
    val hintId: Long,
    val fromUser: User,
    val toUser: User,
    val content: String? = "",
    val iv: String,
    val time: String,
    val type: Int = 0,
    @Transient
    var byteArray: ByteArray? = byteArrayOf(),
    @Transient
    var isSendByUser: Boolean = false,

) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageResponse

        if (messageId != other.messageId) return false
        if (hintId != other.hintId) return false
        if (fromUser != other.fromUser) return false
        if (toUser != other.toUser) return false
        if (content != other.content) return false
        if (iv != other.iv) return false
        if (time != other.time) return false
        if (type != other.type) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messageId?.hashCode() ?: 0
        result = 31 * result + hintId.hashCode()
        result = 31 * result + fromUser.hashCode()
        result = 31 * result + toUser.hashCode()
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + iv.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + type
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}
