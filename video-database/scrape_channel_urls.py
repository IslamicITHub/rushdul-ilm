# 📁 File: video-database/scrape_channel_urls.py
# Why here: This folder contains all the scripts used to download, index, and transcribe Islamic video lectures. 
#           This script is the first step in the pipeline: getting the URLs before processing them.
# Relates to: index_videos.py (which might use these URLs to fetch metadata) and transcribe_videos.py (which will use these to generate text).
#
# 🎓 WHAT IS THIS SCRIPT?
# This script uses 'yt-dlp' to scrape all the video links from a YouTube channel.
# Because YouTube loads its videos dynamically (the webpage changes as you scroll), we can't just download the HTML. 
# yt-dlp bypasses the website layout and talks directly to YouTube's hidden data streams to get the list of videos very quickly without downloading the video files themselves.
#
# 💡 REAL-LIFE ANALOGY:
# Imagine trying to write down every book title in a massive library. 
# Trying to scrape the HTML is like walking down every aisle and reading every single book cover (slow and exhausting). 
# Using yt-dlp is like asking the librarian for the master index catalog on a computer — you get the whole list instantly without having to look at the actual books.
#
# Layer: 08_VIDEO_DATABASE
# Created: 2026-06-17
# Developer: Shaik Hidayatullah

import yt_dlp
# ^ yt_dlp is the main library we use to talk to YouTube and extract video information.
import json
# ^ json allows us to save the extracted data in a structured format so other scripts can read it easily.
import argparse
# ^ argparse lets us run this script from the command line and pass the channel URL as an argument.

def scrape_channel(channel_url, output_file="channel_videos.json"):
    # ^ This function takes a YouTube channel URL and an optional output file name to save the results.
    
    print(f"Starting to scrape: {channel_url}")
    # ^ Print a message so the user knows the script has started.
    
    # Configure how yt-dlp should behave
    ydl_opts = {
        # ^ This dictionary holds the settings we pass to yt-dlp. Think of it like a remote control's buttons.
        'extract_flat': 'in_playlist',
        # ^ extract_flat tells yt-dlp to only get the video details (like title and URL) efficiently, and NOT download the actual video files.
        'quiet': False,
        # ^ quiet=False means yt-dlp will show its progress in the terminal.
        'playlistend': None
        # ^ playlistend=None tells yt-dlp to keep going until it finds every single video in the channel.
    }
    
    video_data_list = []
    # ^ We create an empty list. We will store each video's details in this list as we find them.

    # Start the YouTube downloader with our settings
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        # ^ We use a 'with' statement. This sets up the yt-dlp tool, runs it, and then cleanly shuts it down when done.
        
        try:
            # ^ We use a try block to catch any errors that might happen if the URL is bad or YouTube blocks us.
            info = ydl.extract_info(channel_url, download=False)
            # ^ extract_info fetches the channel data. download=False double-checks that we don't download the video files.
            
            if 'entries' in info:
                # ^ Channels are treated like playlists in yt-dlp. 'entries' contains the list of videos.
                for video in info['entries']:
                    # ^ We loop through each video found in the channel.
                    
                    if video:
                        # ^ Sometimes a video might be private or deleted (None), so we check if it exists first.
                        video_url = video.get('url')
                        # ^ We extract the direct URL of the video.
                        title = video.get('title')
                        # ^ We extract the title of the video.
                        video_id = video.get('id')
                        # ^ We extract the unique ID of the video (like 'dQw4w9WgXcQ').
                        
                        video_info = {
                            "id": video_id,
                            "title": title,
                            "url": video_url
                        }
                        # ^ We create a small dictionary containing just the info we care about.
                        
                        video_data_list.append(video_info)
                        # ^ We add this dictionary to our big list of all videos.
            
            print(f"\nSuccessfully found {len(video_data_list)} videos!")
            # ^ Once the loop finishes, we print how many videos were found in total.
            
            with open(output_file, 'w', encoding='utf-8') as f:
                # ^ We open the output file in write mode ('w') and ensure it uses utf-8 encoding (to support Arabic/Urdu text).
                json.dump(video_data_list, f, indent=4, ensure_ascii=False)
                # ^ json.dump writes our list into the file. indent=4 makes the file easy for humans to read. ensure_ascii=False keeps special characters intact.
                
            print(f"Saved all video data to: {output_file}")
            # ^ We print a final success message.
            
        except Exception as e:
            # ^ If anything goes wrong, this block catches the error so the script doesn't crash silently.
            print(f"An error occurred: {e}")
            # ^ We print the error message so the user can debug what went wrong.

if __name__ == "__main__":
    # ^ This block checks if the script is being run directly from the terminal (not imported by another script).
    
    parser = argparse.ArgumentParser(description="Scrape YouTube channel video URLs.")
    # ^ We set up an argument parser to read command-line inputs.
    
    parser.add_argument("channel_url", help="The URL of the YouTube channel (e.g., https://www.youtube.com/@muftitariqmasood)")
    # ^ We tell the parser to expect the channel URL as a mandatory argument.
    
    parser.add_argument("--output", default="channel_videos.json", help="Output JSON file name")
    # ^ We add an optional argument for the output file, defaulting to 'channel_videos.json'.
    
    args = parser.parse_args()
    # ^ This reads the arguments the user typed in the terminal.
    
    scrape_channel(args.channel_url, args.output)
    # ^ Finally, we call our main function using the arguments provided.
