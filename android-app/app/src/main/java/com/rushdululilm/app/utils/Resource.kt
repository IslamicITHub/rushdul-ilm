// File: Resource.kt
// Purpose: A generic wrapper class for handling UI states (Success, Error, Loading).
// Layer: Layer 1 — Android App (Utils)
// Depends on: None (Utility Class)
// Created: 2026-06-08 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.utils

// 🏛️ CONCEPT: A sealed class is a closed hierarchy class. It restricts subclasses to be defined only within
//    this file. It is perfect for representing state states that are mutually exclusive (e.g. Loading OR Success OR Error).
//    The <T> is a Generic Type parameter, meaning it can hold any type of data (e.g. FatwaAnswer, VideoList).
// 🏛️ ANALOGY: Resource is like a delivery package tracking status. 
//    The status is either: "In Transit" (Loading), "Delivered" (Success containing the product), or "Failed" (Error containing a notice).
sealed class Resource<T>(
// ^ sealed class restricts class inheritance. <T> represents a placeholder for any data class type
    val data: T? = null,
    // ^ val declares a nullable read-only data property of type T, defaulting to null
    val message: String? = null
    // ^ val declares a nullable read-only string property for holding error details, defaulting to null
) {
// ^ Starts Resource body
    class Success<T>(data: T) : Resource<T>(data)
    // ^ Subclass Success represents a completed task, passing the non-null data back to the base class constructor

    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    // ^ Subclass Error represents a failed task, passing the error description string and optional cached data back to the base class

    class Loading<T>(data: T? = null) : Resource<T>(data)
    // ^ Subclass Loading represents an active background task, optionally carrying old cached data back to the base class
}
// ^ Ends Resource class definition
