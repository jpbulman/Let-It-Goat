package com.example.letitgoat.db_models

import android.location.Location
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

//THIS IS A DB MODEL CLASS - Try and only use it for DB reading and writing

@IgnoreExtraProperties
data class Item(
    val name: String,
    val price: Double,
    val user: User,
    val description: String,
    val postedTimeStamp: Date,
    val stringsOfBitmapofPicuresOfItem: List<String>,
    val pickupLocation: Location
)