package com.example.letitgoat.db_models

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

//THIS IS A DB MODEL CLASS - Try and only use it for DB reading and writing

@IgnoreExtraProperties
data class User(
    val email: String,
    val name: String,
    //String of bitmap for the picture
    val profilePicture: String
) : Serializable