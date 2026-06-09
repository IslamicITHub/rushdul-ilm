// File: Resource.kt
// Purpose: A generic wrapper class for handling UI states (Success, Error, Loading).
// Layer: Utils
// Created: 2026-06-08 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.utils

// 📦 This class helps the UI know what's happening with a data request.
// It's like a package that can contain either the Data or an Error message.
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    // 🎉 Success: The data arrived safely!
    class Success<T>(data: T) : Resource<T>(data)

    // ❌ Error: Something went wrong (no internet, server down, etc.)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    // ⏳ Loading: We are still waiting for the data to arrive.
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
