# 08_VIDEO_DATABASE.md
# Rushd-ul-Ilm (رشد العلم) — Video Library
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: PROTOTYPE — METADATA SCRAPER EXISTS; APP PLAYBACK/INDEXING NOT IMPLEMENTED

## Current implementation

The Android Video Library screen currently filters five hard-coded sample records. Selecting a card only prints a message; no ExoPlayer playback is wired. The app's Media3 dependencies are present, but no player usage was found in first-party app code.

The only first-party pipeline script currently present is `video-database/scrape_channel_urls.py`. It uses yt-dlp with `download=False` and writes URL/title/ID metadata to JSON. It does not download videos, transcribe them, index transcripts in Qdrant, or make an offline video database. `video-database/video_urls.txt` is a separate text file, not an integrated catalog.

## Files

- `video-database/scrape_channel_urls.py` — prototype metadata collection from a supplied channel URL.
- `video-database/video_urls.txt` — manually stored URL list.
- `android-app/app/src/main/java/com/rushdululilm/app/viewmodel/VideoLibraryViewModel.kt` — five mock videos and in-memory title/scholar filtering.
- `android-app/app/src/main/java/com/rushdululilm/app/ui/screens/VideoLibraryScreen.kt` — list UI; click action is a placeholder.
- `android-app/app/src/main/java/com/rushdululilm/app/ui/components/VideoCard.kt` — reusable sample card.

## Remaining work

Video metadata persistence, transcript generation, transcript search, lawful source/usage review, local download management, and ExoPlayer integration are not present in the inspected implementation. Treat these as planned, not complete.

## See Also

- [02_ANDROID_APP_LAYER.md](./02_ANDROID_APP_LAYER.md) — Android screens
- [09_BACKEND_DOCKER.md](./09_BACKEND_DOCKER.md) — current backend
- [10_OFFLINE_STRATEGY.md](./10_OFFLINE_STRATEGY.md) — offline video status

---
