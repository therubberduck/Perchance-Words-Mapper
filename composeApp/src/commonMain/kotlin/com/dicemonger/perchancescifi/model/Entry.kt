package com.dicemonger.perchancescifi.model

data class Entry(
    val id: Long,
    val title: String,
    val desc: String,
    val tags: List<Tag>
)