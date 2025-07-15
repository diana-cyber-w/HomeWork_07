package otus.homework.customview.data

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)