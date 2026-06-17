// File: NetworkUtils.kt
// Purpose: Detects if the device is offline, on LAN with the local server, or on the Internet, providing both one-time checks and real-time flows.
// Layer: Layer 1 — Android App (Utils)
// Depends on: Android ConnectivityManager, java.net.Socket
// Created: 2026-06-17 | Modified: 2026-06-17
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.utils

import android.content.Context
// ^ Provides access to application-specific resources and classes, like the ConnectivityManager
import android.net.ConnectivityManager
// ^ System service that answers queries about the state of network connectivity
import android.net.Network
// ^ Represents a network link
import android.net.NetworkCapabilities
// ^ Represents the capabilities of a network, such as whether it has internet access
import kotlinx.coroutines.Dispatchers
// ^ Provides coroutine dispatchers (like IO) for threading
import kotlinx.coroutines.channels.awaitClose
// ^ Suspends a callbackFlow until it gets closed
import kotlinx.coroutines.flow.Flow
// ^ A cold asynchronous data stream that sequentially emits values
import kotlinx.coroutines.flow.callbackFlow
// ^ Builder function to convert callback-based APIs into Kotlin Flows
import kotlinx.coroutines.flow.distinctUntilChanged
// ^ Flow operator that filters out consecutive identical values
import kotlinx.coroutines.launch
// ^ Launches a new coroutine without blocking the current thread
import java.net.InetSocketAddress
// ^ Represents an IP Socket Address (IP address + port number)
import java.net.Socket
// ^ Implements client sockets for network communication

// 🏛️ CONCEPT: NetworkTier is an enum that defines the three possible connection states of our app.
//    Tier 1 is INTERNET (use cloud APIs), Tier 2 is LAN (use local Ubuntu server), Tier 3 is OFFLINE (use on-device models).
// 🏛️ ANALOGY: This is like choosing how to travel. LAN is a local road (fast, free, local server), INTERNET is a highway (cloud server), and OFFLINE is walking (on-device, no roads needed).
enum class NetworkTier {
// ^ Enum class restricting values to these three exact options
    INTERNET, 
    // ^ State representing access to the global internet
    LAN, 
    // ^ State representing local network access to the self-hosted Ubuntu server
    OFFLINE 
    // ^ State representing no network connectivity
}
// ^ Ends NetworkTier enum class definition

// 🏛️ CONCEPT: pingServer tries to open a quick connection to a specific IP and port to see if the server is alive.
//    It returns true if successful within the timeout, false otherwise.
// 🏛️ ANALOGY: Pinging a server is like knocking on a neighbor's door. If they answer quickly, they are home (true). If no one answers within a few seconds, they are away (false).
fun pingServer(host: String, port: Int, timeoutMs: Int): Boolean {
// ^ Function checking if a specific server is reachable on a port
    return try {
    // ^ Starts a try-catch block to handle connection errors safely
        val socket = Socket()
        // ^ Creates a new network socket
        socket.connect(InetSocketAddress(host, port), timeoutMs)
        // ^ Attempts to connect to the specified host and port, waiting up to timeoutMs
        socket.close()
        // ^ Closes the socket immediately since we only wanted to check if connection works
        true
        // ^ Returns true indicating the server is reachable
    } catch (e: Exception) {
    // ^ Catches any network errors (like timeout or connection refused)
        false
        // ^ Returns false indicating the server is unreachable
    }
    // ^ Ends try-catch block
}
// ^ Ends pingServer function

// 🏛️ CONCEPT: detectNetworkTier uses the Android system to check for active networks, and then pings our specific local server to determine the tier.
//    Since pinging involves a network call, this function should generally be run on a background thread (like Dispatchers.IO) to avoid freezing the UI.
// 🏛️ ANALOGY: This is like checking your phone's Wi-Fi. First, you check if Wi-Fi is on at all. Then you try to load your router's setup page. If that works, you're on LAN. If you can load Google, you're on the Internet.
fun detectNetworkTier(context: Context): NetworkTier {
// ^ Function to determine the current network tier (Internet, LAN, or Offline)
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    // ^ Retrieves the ConnectivityManager system service
    
    val network = cm.activeNetwork ?: return NetworkTier.OFFLINE
    // ^ Checks if ANY network is available at all; if not, immediately returns OFFLINE
    
    val caps = cm.getNetworkCapabilities(network) ?: return NetworkTier.OFFLINE
    // ^ Retrieves the capabilities of the active network; if none, returns OFFLINE
    
    // Try to ping the local Ubuntu server (LAN check — 200ms timeout)
    return if (pingServer("192.168.0.102", 8000, 200)) {
    // ^ Attempts to ping the local server IP at port 8000 with a 200 millisecond timeout
        NetworkTier.LAN  
        // ^ Local server is reachable, return LAN tier
    } else if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
    // ^ If LAN failed, checks if the network has global internet capability
        NetworkTier.INTERNET  
        // ^ Internet is available, use cloud/VPS, return INTERNET tier
    } else {
    // ^ If neither LAN nor INTERNET are available
        NetworkTier.OFFLINE  
        // ^ No usable network at all — use on-device models, return OFFLINE tier
    }
    // ^ Ends if-else chain
}
// ^ Ends detectNetworkTier function

// 🏛️ CONCEPT: observeNetworkTier creates a continuous stream (Flow) of network status updates.
//    It listens to Android's ConnectivityManager for network changes and re-evaluates the tier automatically in real-time.
// 🏛️ ANALOGY: This is like having a security camera watching the road. Instead of checking the road just once before you leave the house, the camera continuously alerts you the moment the road becomes blocked or clears up.
fun observeNetworkTier(context: Context): Flow<NetworkTier> = callbackFlow {
// ^ Creates a cold Flow that wraps Android's callback-based network API into a reactive stream
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    // ^ Retrieves the ConnectivityManager system service to listen for network changes
    
    val callback = object : ConnectivityManager.NetworkCallback() {
    // ^ Creates an anonymous object implementing NetworkCallback to handle network events
        override fun onAvailable(network: Network) {
        // ^ Triggered by the system when a new network connection becomes available
            launch(Dispatchers.IO) { send(detectNetworkTier(context)) }
            // ^ Launches a background coroutine to detect the new tier and send it to the flow
        }
        // ^ Ends onAvailable callback
        
        override fun onLost(network: Network) {
        // ^ Triggered by the system when the current network connection is completely lost
            launch(Dispatchers.IO) { send(detectNetworkTier(context)) }
            // ^ Launches a background coroutine to re-evaluate the tier (usually OFFLINE) and send it
        }
        // ^ Ends onLost callback
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        // ^ Triggered when a network's abilities change (e.g., it gains or loses internet access)
            launch(Dispatchers.IO) { send(detectNetworkTier(context)) }
            // ^ Launches a background coroutine to detect the tier and send it to the flow
        }
        // ^ Ends onCapabilitiesChanged callback
    }
    // ^ Ends anonymous callback definition
    
    cm.registerDefaultNetworkCallback(callback)
    // ^ Registers our callback with Android to start receiving real-time network updates for the default network
    
    launch(Dispatchers.IO) { send(detectNetworkTier(context)) }
    // ^ Instantly checks and sends the current network state to the flow the moment someone subscribes to it
    
    awaitClose {
    // ^ Suspends this flow block until the flow is closed or cancelled (e.g. ViewModel is destroyed)
        cm.unregisterNetworkCallback(callback)
        // ^ Unregisters the callback from Android to free up memory and prevent battery drain
    }
    // ^ Ends awaitClose block
}.distinctUntilChanged()
// ^ Ensures the flow only alerts its subscribers when the network tier actually changes to a different state
