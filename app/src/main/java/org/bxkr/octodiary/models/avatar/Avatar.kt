package org.bxkr.octodiary.models.avatar


import com.google.gson.annotations.SerializedName

data class Avatar(
    @SerializedName("default")
    val default: Boolean,
    @SerializedName("id")
    val id: Long,
    @SerializedName("url")
    val url: String,
)