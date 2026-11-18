# Report Documentation — Text-to-Speech
# Rushd-ul-Ilm (رشد العلم)
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: BACKEND AND ANDROID FALLBACK PATHS PRESENT; RUNTIME TESTING INCOMPLETE

## What this layer does

Text-to-speech (TTS) reads the answer aloud for people who prefer listening. The app uses the backend Parler TTS endpoint while the network-tier check is not offline, and Android's `TextToSpeech` engine when that check reports offline.

## Current flow

```text
Answer screen → AnswerViewModel
  ├─ non-offline tier → POST /tts → Parler TTS → base64 WAV → MediaPlayer
  └─ offline tier     → Android TextToSpeech
```

## Files

- `backend/tts_service.py` — Parler-based `POST /tts` endpoint on port 8002.
- `backend/docker-compose.yml` — defines the TTS service and its mounted model/virtual-environment paths.
- `android-app/app/src/main/java/com/rushdululilm/app/viewmodel/AnswerViewModel.kt` — selects remote or Android TTS and plays remote WAV output.
- `android-app/app/src/main/java/com/rushdululilm/app/data/remote/ApiService.kt` — Retrofit TTS request.
- `android-app/app/src/main/java/com/rushdululilm/app/data/repository/MainRepository.kt` — remote request, currently using a hard-coded LAN URL.

The online backend uses `ParlerTTSForConditionalGeneration` with the local `indic-parler-tts` model path. It is not Coqui XTTS-v2. No on-device TTS voice-pack installation or language selection behavior is configured in this code.

## Limitations

- If the app believes it is online but the remote TTS request fails, `AnswerViewModel` reports an error; it does not then switch to Android TTS.
- The remote service's model-loading exception is logged and leaves the model unset; readiness must be tested at runtime.
- The answer's language is not passed as an explicit TTS language setting in the Android fallback path.
- The hard-coded backend address prevents a general VPS/LAN configuration without changing code.

## How to verify

Test a cited answer with the server available, then with the device truly offline. Confirm that the online branch returns playable audio and the offline branch uses the installed Android TTS engine. This audit did not run those tests.

## See Also

- [02_ANDROID_APP_LAYER.md](./02_ANDROID_APP_LAYER.md) — answer screen
- [09_BACKEND_DOCKER.md](./09_BACKEND_DOCKER.md) — backend service configuration
- [10_OFFLINE_STRATEGY.md](./10_OFFLINE_STRATEGY.md) — actual offline coverage
