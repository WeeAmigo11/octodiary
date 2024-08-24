package org.bxkr.octodiary.models.classmembers


import com.google.gson.annotations.SerializedName

data class ClassMember(
    @SerializedName("person_id")
    val personId: String?,
    @SerializedName("user")
    val user: User,
    @SerializedName("is_custom")
    val isCustom: Boolean = false,
)