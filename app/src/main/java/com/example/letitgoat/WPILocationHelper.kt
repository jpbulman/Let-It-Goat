package com.example.letitgoat

import android.location.Location

class WPILocationHelper {

    val listOfLocationAsStrings = listOf<String>(
        "Gordon Library",
        "Alden Hall"
    )

    fun getLocationOfGordonLibrary() : Location{
        val library = Location("")
        library.latitude = 42.2742
        library.longitude = 71.8065

        return library
    }

    fun getLocationOfAldenHall() : Location{
        val alden = Location("")
        alden.latitude = 42.2731
        alden.longitude = 71.8083

        return alden
    }
}