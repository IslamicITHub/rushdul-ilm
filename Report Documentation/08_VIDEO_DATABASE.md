# 08_VIDEO_DATABASE.md
# Rushd-ul-Ilm (رشد العلم) — Video Database Documentation
# Last Updated: 2026-06-17 | Updated by: Antigravity
# Status: IN PROGRESS — Video database ingestion scripts being built

---

## Purpose Of This File

This report explains the Video Database layer of Rushd-ul-Ilm in beginner-friendly language.
This layer is responsible for downloading, indexing, and transcribing Islamic video lectures so they can be searched offline and related to text fatwas.

---

## Script: `scrape_channel_urls.py`

### What Was Created

A new Python script `scrape_channel_urls.py` was created in the `video-database/` folder.
This script extracts all the video URLs from a target YouTube channel and saves them into a structured JSON file.

- **Library Used:** `yt-dlp` — The industry standard tool for interacting with YouTube data.
- **Functionality:** It uses the `extract_flat` option to fetch the video metadata (like title and URL) without downloading the actual MP4 video files. This makes the script incredibly fast.
- **Output:** It saves the extracted data as a JSON file (by default `channel_videos.json`) ensuring that special characters (like Arabic or Urdu text in titles) are preserved perfectly using UTF-8 encoding.
- **Command Line:** The script accepts arguments, so the user can easily run it from the terminal and pass any channel URL they want.

### Beginner Explanation

Imagine you want to know all the titles of the books in a huge library. Instead of walking down every single aisle and reading every cover (which takes forever), you ask the librarian to just print out the master catalog list. 

`yt-dlp` is like that librarian. YouTube hides its video links dynamically, meaning they only load as you scroll down the page. Our script talks directly to YouTube's hidden "catalog" system and asks for the whole list at once, saving all the links into a single file so our other scripts can process them later.

### Verification

The script includes full line-by-line comments (`# ^`) as required by the project's beginner teaching rules.

---
