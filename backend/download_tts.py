# 📁 File: backend/download_tts.py
#    Why here: This is a standalone utility script placed in the backend folder where other server scripts live.
#    Relates to: tts_service.py (which uses the model downloaded by this script)

# -------------------------------------------------------------------------------------------------
# 📝 WHAT THIS SCRIPT IS:
# This script securely downloads the 'indic-parler-tts' artificial intelligence voice model 
# from the internet (Hugging Face) and saves it directly to our local hard drive.
#
# 💡 REAL-LIFE ANALOGY:
# Imagine Hugging Face is a massive online library of AI brains. The "indic-parler-tts" model is a 
# specialized brain that knows how to speak Indian languages. Because this brain is locked in a 
# restricted section of the library, we need to show our special library card (HF_TOKEN) to check it 
# out. This script drives to the library, shows the card, and brings the brain back to our house 
# (local_models folder) so we can use it offline forever.
# -------------------------------------------------------------------------------------------------

import os
# ^ The 'os' module lets Python talk to the operating system (like checking files or folder paths)
from huggingface_hub import snapshot_download
# ^ 'snapshot_download' is a tool from the Hugging Face library that copies an entire folder of files 
# ^ from their internet server down to our local computer

# Parse .env manually
# ^ The .env file is a hidden text file that stores secret passwords, like our Hugging Face token.
env_vars = {}
# ^ Create an empty dictionary (like a blank phonebook) to store the secret keys and values

with open(".env") as f:
# ^ Open the hidden ".env" file in read mode and refer to it as 'f'
    for line in f:
    # ^ Loop through every single line of text inside the .env file one by one
        line = line.strip()
        # ^ Remove any accidental spaces or hidden enter-key characters at the start/end of the line
        if line and not line.startswith("#"):
        # ^ If the line isn't totally blank AND doesn't start with a '#' (which means it's just a comment)
            key, val = line.split("=", 1)
            # ^ Split the line exactly at the first equals sign '='. 
            # ^ Example: "HF_TOKEN=123" becomes key="HF_TOKEN" and val="123"
            env_vars[key] = val
            # ^ Save the split key and value into our dictionary phonebook

token = env_vars.get("HF_TOKEN")
# ^ Search the dictionary for "HF_TOKEN". If found, store the secret password in the 'token' variable.

print("Downloading ai4bharat/indic-parler-tts...")
# ^ Print a message to the computer screen so the user knows the download is starting

snapshot_download(
# ^ Trigger the Hugging Face tool to start downloading the files
    repo_id='ai4bharat/indic-parler-tts',
    # ^ 'repo_id' is the exact name of the AI model on the Hugging Face website
    local_dir='/home/hidayat/Documents/Islamic-Knowledge-QA-App/backend/local_models/indic-parler-tts',
    # ^ 'local_dir' tells the tool exactly which folder on our hard drive to save the downloaded files into
    token=token
    # ^ 'token' provides our secret password to prove we are allowed to download this gated model
)

print("Done!")
# ^ Print a success message to the screen when the massive download finally finishes
