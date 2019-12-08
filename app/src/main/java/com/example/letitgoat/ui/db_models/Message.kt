package com.example.letitgoat.ui.db_models

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

//THIS IS A DB MODEL CLASS - Try and only use it for DB reading and writing

@IgnoreExtraProperties
data class Message (
    val sender: String = "",
    val recipient: String = "",
    val contents: String = "",
    val timeSent: Date = Date()
)