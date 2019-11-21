package com.example.letitgoat.ui.db_models

import com.google.firebase.database.IgnoreExtraProperties

//THIS IS A DB MODEL CLASS - Try and only use it for DB reading and writing

@IgnoreExtraProperties
data class User(
    val email: String?,
    val name: String?
)