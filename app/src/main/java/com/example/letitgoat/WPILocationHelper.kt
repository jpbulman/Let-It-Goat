package com.example.letitgoat

import android.location.Location

class WPILocationHelper {

    val listOfLocationAsStrings = listOf(
        "Gordon Library",
        "Alden Hall",
        "Fuller Labs"
    )

    val locationNameToLocationObjectMap = mapOf(
        "Gordon Library" to getLocationOfGordonLibrary(),
        "Alden Hall" to getLocationOfAldenHall(),
        "Fuller Labs" to getLoctaionOfFullerLabs()
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

    fun getLoctaionOfFullerLabs() : Location{
        val fullerLabs = Location("Alden Hall")
        fullerLabs.latitude = 42.2751
        fullerLabs.longitude = -71.8065

        return fullerLabs
    }
}