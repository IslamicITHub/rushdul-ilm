# Report Documentation — Offline Strategy
# Rushd-ul-Ilm (رشد العلم)
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: PARTIAL FALLBACKS ONLY; OFFLINE Q&A IS NOT IMPLEMENTED

## What works offline in the inspected code

- **Speech input:** `HomeViewModel` calls whisper.cpp JNI when its network-tier check returns `OFFLINE`. The model is included in app assets. Native code currently forces English (`wparams.language = "en"`), so this path is not a verified Telugu/Urdu fallback.
- **Read aloud:** `AnswerViewModel` uses Android `TextToSpeech` when the tier check returns `OFFLINE`; available languages depend on voice data installed on the device.
- **Answer history:** Room stores previously received answers. This lets users revisit history, but does not find new answers from a local fatwa collection.

## Not implemented as offline fallbacks

- **Translation:** Opus-MT ONNX inference is not implemented in app code. ONNX Runtime is only declared as a dependency.
- **Q&A:** The Room database contains `saved_answers`; there is no Room FTS5 fatwa table or offline RAG answer path.
- **Video:** No local video catalog, download workflow, or playback implementation is wired.
- **LAN-to-Internet routing:** `NetworkUtils` detects a LAN server by pinging a fixed private address and otherwise reports Internet based on network capabilities. Retrofit and the STT/TTS calls still use hard-coded LAN addresses; the Internet tier does not select a configured VPS endpoint.
- **Failure fallback:** When the device is considered online but an online TTS or translation request fails, the app/backend does not consistently retry the on-device path.

## Current tier summary

| Capability | Backend reachable | Network reported offline |
|---|---|---|
| Speech input | faster-whisper endpoint | whisper.cpp JNI; language forced to English |
| Translation | IndicTrans2 service (runtime placement needs verification) | Not implemented |
| Answer search/generation | FastAPI + Qdrant + selected LLM | Not implemented |
| Read aloud | Parler TTS endpoint | Android TextToSpeech |
| Video | Sample cards only | Not implemented |
| Saved answers | Room database | Room history remains accessible |

This table describes code paths, not a completed or tested three-tier system. Internet, LAN, and offline behavior should be validated separately before making a full offline-readiness claim.

## See Also

- [03_STT_PIPELINE.md](./03_STT_PIPELINE.md)
- [04_TRANSLATION_PIPELINE.md](./04_TRANSLATION_PIPELINE.md)
- [05_TTS_PIPELINE.md](./05_TTS_PIPELINE.md)
- [06_RAG_PIPELINE.md](./06_RAG_PIPELINE.md)
- [08_VIDEO_DATABASE.md](./08_VIDEO_DATABASE.md)
