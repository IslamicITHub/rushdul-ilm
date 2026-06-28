// File: AudioRecorderHelper.kt
// Purpose: Captures raw audio from the microphone at 16kHz mono for whisper.cpp processing
// Layer: Layer 3 — Offline STT Pipeline
// Created: 2026-06-26 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 🏛️ CONCEPT: AudioRecord is Android's lowest-level audio capture API. 
//    Unlike MediaRecorder which saves a compressed file (like MP3), AudioRecord gives us the raw audio bytes (PCM) 
//    exactly as the microphone hears them. Whisper AI requires this raw format at 16,000 Hz.
// 🏛️ ANALOGY: MediaRecorder is like a baker giving you a baked cake. AudioRecord is like the farmer 
//    giving you raw flour and eggs. Whisper needs the raw ingredients to work.

class AudioRecorderHelper {
// ^ Helper class that manages the microphone capture

    private var audioRecord: AudioRecord? = null
    // ^ Nullable variable holding the active recording session
    private var isRecording = false
    // ^ Boolean flag tracking if we are currently recording
    private val audioBuffer = mutableListOf<Float>()
    // ^ A dynamic list storing the accumulated audio floats

    private val sampleRate = 16000
    // ^ Whisper model strictly requires 16kHz sample rate
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    // ^ Single channel audio (mono)
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    // ^ 16-bit depth is the standard raw audio format

    @SuppressLint("MissingPermission")
    // ^ We assume the app has already requested the RECORD_AUDIO permission from the user
    fun startRecording() {
    // ^ Starts capturing audio from the microphone
        if (isRecording) return
        // ^ Prevent starting multiple recordings at once

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        // ^ Asks Android for the minimum memory chunk needed to store incoming audio safely
        
        audioRecord = AudioRecord(
        // ^ Initializes the audio capture session
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
        // ^ Checks if the microphone failed to open (e.g., used by another app, unsupported hardware, or permissions revoked late)
            println("❌ AudioRecord failed to initialize.")
            audioRecord?.release()
            audioRecord = null
            isRecording = false
            return
            // ^ Safely aborts the recording attempt without crashing
        }

        audioBuffer.clear()
        // ^ Empties the previous recording data

        audioRecord?.startRecording()
        // ^ Instructs the hardware microphone to begin capturing
        isRecording = true
        // ^ Updates our internal tracking flag

        Thread {
        // ^ Starts a dedicated background thread to continuously read the microphone data
            val shortBuffer = ShortArray(bufferSize)
            // ^ Creates a temporary array to hold the 16-bit PCM chunks (Shorts)
            
            while (isRecording) {
            // ^ Loops continuously as long as the user is holding the mic
                val readResult = audioRecord?.read(shortBuffer, 0, bufferSize) ?: 0
                // ^ Reads a chunk of audio from the microphone into our shortBuffer
                
                if (readResult > 0) {
                // ^ If we successfully read some audio data
                    for (i in 0 until readResult) {
                    // ^ Loop through every audio sample in the chunk
                        // Whisper requires float values between -1.0 and 1.0. 
                        // A 16-bit PCM value ranges from -32768 to 32767, so we divide to normalize.
                        audioBuffer.add(shortBuffer[i] / 32768.0f)
                        // ^ Normalizes the 16-bit integer to a Float and appends it to our master list
                    }
                    // ^ Ends float conversion loop
                }
                // ^ Ends valid read check
            }
            // ^ Ends continuous recording loop
        }.start()
        // ^ Starts the background thread immediately
    }
    // ^ Ends startRecording function

    suspend fun stopRecording(): FloatArray = withContext(Dispatchers.IO) {
    // ^ Stops recording and returns the final normalized float array, running safely on the IO thread
        isRecording = false
        // ^ Flips the flag, which stops the continuous reading thread
        
        audioRecord?.stop()
        // ^ Instructs the hardware to stop capturing
        audioRecord?.release()
        // ^ Frees the microphone so other apps can use it
        audioRecord = null
        // ^ Clears our reference

        return@withContext audioBuffer.toFloatArray()
        // ^ Converts our dynamic list into a fixed FloatArray (which whisper-jni needs) and returns it
    }
    // ^ Ends stopRecording function

    suspend fun saveAsWav(file: java.io.File): java.io.File = withContext(Dispatchers.IO) {
    // ^ Saves the current audio buffer into a standard WAV file for HTTP upload
        val floatArray = audioBuffer.toFloatArray()
        val numSamples = floatArray.size
        val numChannels = 1
        val sampleRate = 16000
        val byteRate = sampleRate * numChannels * 2
        val dataSize = numSamples * 2
        val headerSize = 44
        val totalDataLen = dataSize + 36

        java.io.FileOutputStream(file).use { out ->
            val header = java.nio.ByteBuffer.allocate(headerSize)
            header.order(java.nio.ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray(Charsets.US_ASCII))
            header.putInt(totalDataLen)
            header.put("WAVE".toByteArray(Charsets.US_ASCII))
            header.put("fmt ".toByteArray(Charsets.US_ASCII))
            header.putInt(16) // Subchunk1Size
            header.putShort(1.toShort()) // AudioFormat (PCM)
            header.putShort(numChannels.toShort()) // NumChannels
            header.putInt(sampleRate) // SampleRate
            header.putInt(byteRate) // ByteRate
            header.putShort((numChannels * 2).toShort()) // BlockAlign
            header.putShort(16.toShort()) // BitsPerSample
            header.put("data".toByteArray(Charsets.US_ASCII))
            header.putInt(dataSize) // Subchunk2Size

            out.write(header.array())

            val dataBuffer = java.nio.ByteBuffer.allocate(dataSize)
            dataBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
            for (f in floatArray) {
                // Convert float back to short
                var s = (f * 32768.0f).toInt()
                if (s > 32767) s = 32767
                if (s < -32768) s = -32768
                dataBuffer.putShort(s.toShort())
            }
            out.write(dataBuffer.array())
        }
        return@withContext file
    }
    // ^ Ends saveAsWav function
}
// ^ Ends AudioRecorderHelper class
