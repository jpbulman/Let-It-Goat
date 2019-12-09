package com.example.letitgoat

import android.location.Location

class WPILocationHelper {

    val listOfLocationAsStrings = listOf<String>(
        "Gordon Library",
        "Alden Hall"
    )

    val locationNameToLocationObjectMap = mapOf<String, Location>(
        "Gordon Library" to getLocationOfGordonLibrary(),
        "Alden Hall" to getLocationOfAldenHall()
    )

    fun getLocationOfGordonLibrary() : Location{
        val library = Location("Gordon Library")
        library.latitude = 42.2742
        library.longitude = -71.8065

        return library
    }

    fun getLocationOfAldenHall() : Location{
        val alden = Location("Alden Hall")
        alden.latitude = 42.2731
        alden.longitude = -71.8083

        return alden
    }
}