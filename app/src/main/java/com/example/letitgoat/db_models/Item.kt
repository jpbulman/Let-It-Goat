package com.example.letitgoat.db_models

import android.location.Location
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

//THIS IS A DB MODEL CLASS - Try and only use it for DB reading and writing

@IgnoreExtraProperties
@Parcelize
data class Item(
    val name: String,
    val price: Double,
    val user: User,
    val description: String,
    val postedTimeStamp: Date,
    val stringsOfBitmapofPicuresOfItem: List<String>,
    val pickupLocation: Location,
    val category : String
) : Parcelable