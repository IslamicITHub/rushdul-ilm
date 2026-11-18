# Report Documentation — Speech-to-Text
# Rushd-ul-Ilm (رشد العلم)
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: ONLINE AND DEVICE PATHS EXIST; LANGUAGE AND RUNTIME TESTING INCOMPLETE

## What this layer does

Speech-to-text (STT) turns a microphone recording into text for the Q&A request. The Android app captures 16 kHz mono audio; when it can reach the configured backend it uploads a WAV file, and when its network-tier check says offline it calls the bundled whisper.cpp JNI library.

## Current flow

```text
AudioRecord (Android)
  ├─ reachable backend → POST /transcribe → faster-whisper
  └─ offline tier       → whisper.cpp JNI on the phone
Transcription → FastAPI /query
```

The online and offline branches exist in code, but this does not establish that all supported spoken languages work in both branches.

## Files

- `android-app/app/src/main/java/com/rushdululilm/app/utils/AudioRecorderHelper.kt` — records and exports WAV audio.
- `android-app/app/src/main/java/com/rushdululilm/app/viewmodel/HomeViewModel.kt` — routes recordings to backend STT or JNI.
- `android-app/app/src/main/java/com/rushdululilm/app/utils/WhisperHelper.kt` — Kotlin declarations for the JNI bridge.
- `android-app/app/src/main/cpp/whisper-jni.cpp` — calls whisper.cpp; currently sets `wparams.language = "en"`.
- `android-app/app/src/main/cpp/CMakeLists.txt` — builds the native library.
- `android-app/app/src/main/assets/models/ggml-small-q5_1.bin` — model asset present and tracked in Git.
- `backend/stt_service.py` — exposes `/transcribe` and uses faster-whisper with CUDA / INT8.
- `backend/docker-compose.yml` — defines the STT service on port 8003.

## Important implementation limits

- The on-device C++ path forces English, so Telugu/Urdu offline recognition is not implemented by the current JNI configuration.
- The online endpoint uses `task="translate"` and has different handling for raw PCM and uploaded audio; its language behavior needs an end-to-end test before documenting multilingual accuracy.
- The application has a fixed backend address in `NetworkModule.kt`; network detection does not itself configure a public VPS endpoint.
- If online STT fails, the ViewModel attempts the offline JNI path. That fallback depends on successful model initialization and supported audio.

## How to verify

Build and run the Android app on a device with microphone permission, then test Telugu, Urdu, and English independently with the backend reachable and unreachable. Confirm the recognized text and inspect backend health at `/health` and transcription at `/transcribe`. These tests were not run as part of this documentation audit.

## See Also

- [02_ANDROID_APP_LAYER.md](./02_ANDROID_APP_LAYER.md) — app screen and ViewModel structure
- [04_TRANSLATION_PIPELINE.md](./04_TRANSLATION_PIPELINE.md) — language translation
- [10_OFFLINE_STRATEGY.md](./10_OFFLINE_STRATEGY.md) — verified offline boundaries
