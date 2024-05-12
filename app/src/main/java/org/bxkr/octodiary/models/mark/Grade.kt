package org.bxkr.octodiary.models.mark


import com.google.gson.annotations.SerializedName

data class Grade(
    @SerializedName("five")
    val five: Int,
    @SerializedName("hundred")
    val hundred: Int,
    @SerializedName("origin")
    val origin: String,
)