package com.example.letitgoat.db_models

import android.graphics.Bitmap
import com.google.firebase.database.IgnoreExtraProperties

//THIS IS A DB MODEL CLASS - Try and only use it for DB reading and writing

@IgnoreExtraProperties
data class User(
    val email: String,
    val name: String,
    val profilePicture: String
)