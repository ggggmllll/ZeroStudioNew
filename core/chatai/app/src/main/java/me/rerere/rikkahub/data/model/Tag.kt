package me.rerere.rikkahub.data.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Uuid,
    val name: String,
)
