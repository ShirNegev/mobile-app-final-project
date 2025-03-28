package com.example.where_am_i_app.utils

import android.content.Context
import android.location.Geocoder
import ch.hsr.geohash.GeoHash
import java.util.Locale

fun getLocationFromGeoHash(geoHash: String?, context: Context): String {
    if (geoHash.isNullOrEmpty()) {
        return "No Location Detected."
    } else {
        return try {
            val geoHashObject = GeoHash.fromGeohashString(geoHash)
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(geoHashObject.originatingPoint.latitude, geoHashObject.originatingPoint.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val streetNumber = address.thoroughfare ?: "Unknown Street"
                val city = address.locality ?: "Unknown City"
                val country = address.countryName ?: "Unknown Country"
                return "$streetNumber, $city, $country"
            } else {
                return "Unknown Location"
            }
        } catch (e: Exception) {
            "Invalid GeoHash Location"
        }
    }
}