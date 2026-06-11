// File: NetworkModule.kt
// Purpose: Provides Retrofit and OkHttp instances using Hilt Dependency Injection.
// Layer: Layer 1 — Android App (DI Modules)
// Depends on: ApiService.kt
// Created: 2026-06-08 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.di

import com.rushdululilm.app.data.remote.ApiService
// ^ Imports our interface detailing backend API endpoints
import dagger.Module
// ^ Dagger annotation marking this object as a supplier of dependency injection blueprints
import dagger.Provides
// ^ Dagger annotation marking a method as an object builder (factory method) for Hilt
import dagger.hilt.InstallIn
// ^ Dagger Hilt annotation determining which level scope container this module's objects live in
import dagger.hilt.components.SingletonComponent
// ^ Tells Hilt that the provided objects should live in the Singleton container (app scope - alive as long as app runs)
import okhttp3.OkHttpClient
// ^ Square library class that handles low-level socket connections, connection pooling, and HTTP protocol handshakes
import retrofit2.Retrofit
// ^ Square library class that turns Java/Kotlin interfaces into HTTP clients using OkHttp
import retrofit2.converter.gson.GsonConverterFactory
// ^ Retrofit plugin to automatically convert JSON text responses into Kotlin data classes using Gson
import java.util.concurrent.TimeUnit
// ^ Java utility enum representing standard time units (e.g. SECONDS, MILLISECONDS)
import javax.inject.Singleton
// ^ Standard Java injection annotation instructing Hilt to instantiate this object exactly once (Singleton pattern)

// 🏛️ CONCEPT: Hilt Modules are configuration classes that define how to build external library classes (like Retrofit).
//    Methods annotated with @Provides and @Singleton build and share single instances of these classes throughout the app.
// 🏛️ ANALOGY: NetworkModule is like a tool rental catalog in a workshop. 
//    Instead of every worker (ViewModel) buying their own specialized power drill (Retrofit client), the catalog lists single shared tools everyone can borrow.
@Module
// ^ Marks this object class as a provider of Hilt dependencies
@InstallIn(SingletonComponent::class)
// ^ Configures the lifetime scope of all instances created in this module to the full lifespan of the Application
object NetworkModule {
// ^ object creates a Kotlin Singleton class definition
    
    private const val BASE_URL = "http://192.168.0.102:8000/"
    // ^ Defines a private constant string containing the base URL of our FastAPI backend server.
    // ^ Note: Using 192.168.x.x points to your Ubuntu host machine's Wi-Fi IP address.

    @Provides
    // ^ Tells Hilt that this method is the instructions for creating an OkHttpClient object
    @Singleton
    // ^ Tells Hilt to cache the built OkHttpClient and reuse this same instance everywhere
    fun provideOkHttpClient(): OkHttpClient {
    // ^ Builder function to construct and customize OkHttpClient
        return OkHttpClient.Builder()
        // ^ Instantiates the OkHttp builder pattern class
            .connectTimeout(30, TimeUnit.SECONDS)
            // ^ Sets connection timeout to 30 seconds (time allowed to establish socket link to the server)
            .readTimeout(60, TimeUnit.SECONDS)
            // ^ Sets read timeout to 60 seconds (time allowed to wait for the server to process and return response data)
            .writeTimeout(30, TimeUnit.SECONDS)
            // ^ Sets write timeout to 30 seconds (time allowed to send bytes to the server)
            .build()
            // ^ finalizes builder configuration and outputs the configured OkHttpClient instance
    }
    // ^ Ends provideOkHttpClient function

    @Provides
    // ^ Tells Hilt this function contains instructions for creating a Retrofit instance
    @Singleton
    // ^ Restricts Hilt to sharing a single Retrofit client instance across the app
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    // ^ Builder function to construct Retrofit. Hilt automatically supplies the okHttpClient parameter.
        return Retrofit.Builder()
        // ^ Instantiates the Retrofit builder pattern class
            .baseUrl(BASE_URL)
            // ^ Configures the target web API host server address
            .client(okHttpClient)
            // ^ Plugs in OkHttpClient as the underlying engine to perform the actual HTTP socket requests
            .addConverterFactory(GsonConverterFactory.create())
            // ^ Registers Gson as the parser to serialize data requests and deserialize JSON results automatically
            .build()
            // ^ Finalizes configuration and outputs the configured Retrofit instance
    }
    // ^ Ends provideRetrofit function

    @Provides
    // ^ Tells Hilt how to instantiate ApiService
    @Singleton
    // ^ Restricts Hilt to sharing a single ApiService instance across the app
    fun provideApiService(retrofit: Retrofit): ApiService {
    // ^ Factory method that creates our API endpoints mapping. Hilt automatically supplies the retrofit parameter.
        return retrofit.create(ApiService::class.java)
        // ^ Asks Retrofit to compile our ApiService interface functions into an executable network request client
    }
    // ^ Ends provideApiService function
}
// ^ Ends NetworkModule object class
