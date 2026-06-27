package com.rushdululilm.app.viewmodel

// File: AnswerViewModel.kt
// Purpose: The "brain" of the Answer Screen. It holds the fatwa answer and related videos.
// Layer: Layer 1 — Android App (ViewModel)
// Depends on: MainRepository.kt, AnswerModels.kt, StateFlow
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.lifecycle.ViewModel
// ^ Base class for ViewModels, allowing data to survive configuration changes like screen rotations
import androidx.lifecycle.viewModelScope
// ^ CoroutineScope tied to the ViewModel's lifecycle. It automatically cancels active jobs when the ViewModel is cleared.
import com.rushdululilm.app.data.repository.MainRepository
// ^ Imports our central data manager
import com.rushdululilm.app.model.FatwaAnswer
// ^ Imports the fatwa answer data class model
import com.rushdululilm.app.model.RelatedVideo
// ^ Imports the video metadata model
import dagger.hilt.android.lifecycle.HiltViewModel
// ^ Annotation that registers this class with Hilt for automated construction
import kotlinx.coroutines.flow.MutableStateFlow
// ^ Kotlin Coroutines state holder flow with write access
import kotlinx.coroutines.flow.StateFlow
// ^ Kotlin Coroutines state holder flow with read-only access
import kotlinx.coroutines.flow.asStateFlow
// ^ Converts a mutable flow to a read-only StateFlow for safe UI observation
import kotlinx.coroutines.launch
// ^ Extension function on CoroutineScope to launch a new background job asynchronously
import javax.inject.Inject
// ^ Tells Hilt how to inject constructor dependencies (like MainRepository)

// 🏛️ CONCEPT: A ViewModel manages state for a specific screen, preserving data during screen rotation.
//    We launch coroutines in viewModelScope to collect StateFlow streams from our Repository.
// 🏛️ ANALOGY: ViewModel is like a flight cockpit instrument panel. Even if the airplane turns
//    sideways (screen rotation), the dials (StateFlow properties) stay powered on and don't reset.
import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
// ^ Imports Android's built-in offline TextToSpeech engine
import dagger.hilt.android.qualifiers.ApplicationContext
import com.rushdululilm.app.data.repository.AnswerHistoryRepository
import com.rushdululilm.app.data.local.FatwaSourceConverter
import com.rushdululilm.app.utils.NetworkTier
// ^ Imports the NetworkTier enum to check connectivity status
import com.rushdululilm.app.utils.detectNetworkTier
// ^ Imports the function to detect the current network tier

@HiltViewModel
// ^ Tells the Hilt compiler to generate dependency injection code for this ViewModel class
class AnswerViewModel @Inject constructor( // ^ class AnswerViewModel manages state for the AnswerScreen, constructor injected by Hilt
    private val repository: MainRepository, // ^ central data repository instance for accessing fatwa answers
    private val answerHistoryRepository: AnswerHistoryRepository, // ^ injected repository for accessing saved local answers
    @ApplicationContext private val context: Context // ^ Injects global application context for saving temporary audio files
) : ViewModel() {
// ^ AnswerViewModel inherits from standard architecture ViewModel

    private var mediaPlayer: MediaPlayer? = null
    // ^ private nullable variable to hold the Android MediaPlayer instance for TTS playback

    private val _currentAnswer = MutableStateFlow<FatwaAnswer?>(FatwaAnswer.PLACEHOLDER)
    // ^ private mutable flow holding the fatwa answer currently drawn on the UI (starts with placeholder)

    val currentAnswer: StateFlow<FatwaAnswer?> = _currentAnswer.asStateFlow()
    // ^ public read-only StateFlow exposed to the AnswerScreen UI

    private var textToSpeechEngine: TextToSpeech? = null
    // ^ Nullable variable to hold the Android TextToSpeech engine instance

    init {
    // ^ init block runs immediately when this ViewModel class is created
        
        textToSpeechEngine = TextToSpeech(context) { status ->
        // ^ Initializes the Android TextToSpeech engine with the application context
            if (status == TextToSpeech.SUCCESS) {
            // ^ Checks if the engine initialized successfully
                println("Offline TTS Engine initialized successfully")
                // ^ Logs success for debugging
            } else {
            // ^ Executes if initialization failed
                println("Offline TTS Engine failed to initialize")
                // ^ Logs failure for debugging
            }
            // ^ Ends status check
        }
        // ^ Ends TTS initialization block

        viewModelScope.launch {
        // ^ Launches a background coroutine to safely collect updates from the Repository
            repository.latestAnswer.collect { answer ->
            // ^ Collects the repository's latestAnswer flow updates
                if (answer != null) {
                // ^ Checks if a non-null answer was received from the repository
                    _currentAnswer.value = answer
                    // ^ Updates our UI state Flow value with the new answer
                }
                // ^ Ends validation check
            }
            // ^ Ends repository flow collection
        }
        // ^ Ends coroutine launch block
    }
    // ^ Ends init block

    private val _relatedVideos = MutableStateFlow<List<RelatedVideo>>(
    // ^ private mutable flow holding a list of related videos to suggest
        listOf(
        // ^ Creates a placeholder list containing mock video details
            RelatedVideo(
            // ^ Instantiates the first mock RelatedVideo object
                id = "vid_01",
                // ^ Mock video ID
                title = "Understanding Salah",
                // ^ Mock video title
                scholarName = "Fake Scholar 1",
                // ^ Mock scholar speaker
                durationSeconds = 600, 
                // ^ Mock duration (10 minutes)
                filePath = "/fake/path/1.mp4",
                // ^ Mock local downloaded filepath
                serverUrl = "http://fake.server/1.mp4"
                // ^ Mock remote video stream address
            ),
            // ^ Ends first video declaration
            RelatedVideo(
            // ^ Instantiates second mock RelatedVideo object
                id = "vid_02",
                // ^ Mock video ID
                title = "Traveling Rules",
                // ^ Mock video title
                scholarName = "Fake Scholar 2",
                // ^ Mock scholar speaker
                durationSeconds = 1200, 
                // ^ Mock duration (20 minutes)
                filePath = "/fake/path/2.mp4",
                // ^ Mock local downloaded filepath
                serverUrl = "http://fake.server/2.mp4"
                // ^ Mock remote video stream address
            )
            // ^ Ends second video declaration
        )
        // ^ Ends list of placeholder videos
    )
    // ^ Ends _relatedVideos StateFlow instantiation

    val relatedVideos: StateFlow<List<RelatedVideo>> = _relatedVideos.asStateFlow()
    // ^ public read-only StateFlow exposed to the UI for drawing related video cards

    private val _isReadingAloud = MutableStateFlow(false)
    // ^ private mutable flow tracking if the text-to-speech engine is active (defaults to false)

    val isReadingAloud: StateFlow<Boolean> = _isReadingAloud.asStateFlow()
    // ^ public read-only StateFlow for active TTS indicators

    private val _isLoading = MutableStateFlow(false)
    // ^ private mutable flow tracking loading spinner displays (defaults to false)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // ^ public read-only StateFlow for active loading states

    fun loadAnswer(answerId: String) {
    // ^ Function to fetch a specific answer detail from local DB or network API
        _isLoading.value = true
        // ^ Turns on the loading indicator state
        
        viewModelScope.launch {
        // ^ Launches coroutine to fetch data asynchronously
            val id = answerId.toIntOrNull()
            // ^ Converts the string ID to an integer safely
            if (id != null) {
            // ^ If it is a valid integer, proceed to fetch
                val savedAnswer = answerHistoryRepository.getAnswerById(id)
                // ^ Calls the repository to get the saved answer
                if (savedAnswer != null) {
                // ^ If the answer exists in the local database
                    val sourcesList = FatwaSourceConverter().toSourceList(savedAnswer.sourcesJson)
                    // ^ Converts the JSON string back into a List of FatwaSource objects
                    
                    _currentAnswer.value = FatwaAnswer(
                    // ^ Reconstructs the FatwaAnswer model for the UI
                        id = savedAnswer.id.toString(),
                        questionText = savedAnswer.questionText,
                        answerText = savedAnswer.answerText,
                        sources = sourcesList,
                        language = savedAnswer.language,
                        isOfflineCache = savedAnswer.isOfflineCache,
                        expandedQuery = null
                    )
                    // ^ Ends FatwaAnswer construction
                }
                // ^ Ends valid answer check
            }
            // ^ Ends integer ID check
            
            _isLoading.value = false
            // ^ Turns off the loading indicator state
        }
        // ^ Ends coroutine
    }
    // ^ Ends loadAnswer function

    fun onReadAloudPressed() {
    // ^ Function triggered when the user taps the Read Aloud button
        if (_isReadingAloud.value) {
        // ^ If it is currently playing audio, stop it
            mediaPlayer?.stop()
            // ^ Stops the media player
            mediaPlayer?.release()
            // ^ Releases system resources held by the media player
            mediaPlayer = null
            // ^ Nullifies the reference
            textToSpeechEngine?.stop()
            // ^ Stops the built-in offline TTS engine if it was speaking
            _isReadingAloud.value = false
            // ^ Turns off the reading aloud UI state
            return
        }

        val textToSpeak = _currentAnswer.value?.answerText ?: return
        // ^ Retrieves the text of the fatwa answer, exiting if it's null
        
        val currentTier = detectNetworkTier(context)
        // ^ Checks the current network connectivity status
        
        if (currentTier == NetworkTier.OFFLINE) {
        // ^ If the device has no network connection at all
            _isReadingAloud.value = true
            // ^ Turns on the reading aloud UI state indicator
            textToSpeechEngine?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "offline_tts")
            // ^ Instructs the built-in Android offline TTS engine to speak the text immediately
            
            // Note: Since TextToSpeech runs asynchronously without an easy coroutine callback, 
            // the UI state will stay true until manually stopped by another button press in this basic implementation.
            return
            // ^ Exits the function early so we don't try to call the network API
        }
        // ^ Ends offline check
        
        _isLoading.value = true
        // ^ Displays the loading spinner while the backend generates the audio
        
        viewModelScope.launch {
        // ^ Launches a coroutine to fetch the audio from the backend
            val result = repository.generateSpeech(text = textToSpeak)
            // ^ Calls the MainRepository function to hit the TTS endpoint
            
            _isLoading.value = false
            // ^ Turns off the loading spinner
            
            if (result is com.rushdululilm.app.utils.Resource.Success && result.data != null) {
            // ^ Checks if the network call was successful
                _isReadingAloud.value = true
                // ^ Sets the UI state to actively reading
                playAudioFromBase64(result.data.audioBase64)
                // ^ Triggers the local playback function with the base64 string
            } else {
            // ^ Executes if the network call failed
                println("TTS Error: ${result.message}")
                // ^ Prints the error message to the console
                _isReadingAloud.value = false
                // ^ Ensures UI state reflects no audio is playing
            }
            // ^ Ends network result check
        }
        // ^ Ends coroutine
    }
    // ^ Ends onReadAloudPressed function

    private fun playAudioFromBase64(base64Audio: String) {
    // ^ Decodes a base64 string to a WAV file and plays it using Android's MediaPlayer
        try {
        // ^ try block to catch IO or decoding exceptions safely
            val audioBytes = android.util.Base64.decode(base64Audio, android.util.Base64.DEFAULT)
            // ^ Decodes the base64 string into a raw byte array
            
            val tempFile = java.io.File(context.cacheDir, "tts_temp.wav")
            // ^ Creates a temporary file in the app's cache directory
            
            java.io.FileOutputStream(tempFile).use { fos ->
            // ^ Opens a file output stream and ensures it closes automatically
                fos.write(audioBytes)
                // ^ Writes the byte array to the physical WAV file
            }
            
            mediaPlayer = MediaPlayer().apply {
            // ^ Instantiates a new Android MediaPlayer object
                setDataSource(tempFile.absolutePath)
                // ^ Sets the data source to the temporary file we just wrote
                prepare()
                // ^ Synchronously prepares the player for playback (fine for local files)
                start()
                // ^ Begins playing the audio
                setOnCompletionListener {
                // ^ Listener triggered when the audio track finishes playing naturally
                    _isReadingAloud.value = false
                    // ^ Updates the UI state to indicate reading has stopped
                }
            }
            // ^ Ends MediaPlayer configuration block
        } catch (e: Exception) {
        // ^ Executes if writing the file or playing the audio crashes
            e.printStackTrace()
            // ^ Prints the stack trace for debugging
            _isReadingAloud.value = false
            // ^ Ensures the UI state isn't stuck on active
        }
        // ^ Ends try-catch block
    }
    // ^ Ends playAudioFromBase64 function

    override fun onCleared() {
    // ^ Android ViewModel lifecycle function called right before this object is destroyed
        super.onCleared()
        // ^ Calls the base class cleanup implementation
        mediaPlayer?.release()
        // ^ Releases the media player system resources to prevent memory leaks
        textToSpeechEngine?.stop()
        // ^ Stops the offline TTS engine if it is currently speaking
        textToSpeechEngine?.shutdown()
        // ^ Shuts down the offline TTS engine to free up system resources
    }
    // ^ Ends onCleared function

    fun onVideoClicked(video: RelatedVideo) {
    // ^ Function triggered when the user clicks a related lecture card
        println("Video clicked: ${video.title} by ${video.scholarName}")
        // ^ Prints a debug message showing which video was clicked
    }
    // ^ Ends onVideoClicked function
}
// ^ Ends AnswerViewModel class definition
