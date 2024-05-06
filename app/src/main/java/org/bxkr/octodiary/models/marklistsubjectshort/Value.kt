package org.bxkr.octodiary.models.marklistsubjectshort


import com.google.gson.annotations.SerializedName
import org.bxkr.octodiary.models.events.Grade

data class Value(
    @SerializedName("grade")
    val grade: Grade,
    @SerializedName("grade_system_id")
    val gradeSystemId: Any?,
    @SerializedName("grade_system_type")
    val gradeSystemType: String,
    @SerializedName("name")
    val name: Any?
)