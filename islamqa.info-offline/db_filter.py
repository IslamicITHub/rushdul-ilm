# 📁 File: /home/hidayat/Documents/Islamic-Knowledge-QA-App/islamqa.info-offline/db_filter.py
# Why here: This script is a utility to clean up the existing SQLite database.
#           It lives in the project root alongside the database and other ingestion scripts.

import sqlite3  # Standard library to talk to SQLite databases
from bs4 import BeautifulSoup  # Library to parse and clean HTML content
import html  # Standard library to handle HTML entities like &nbsp;

# --- EXPLANATION BEFORE CODE ---
# We are creating a function called 'clean_html' that takes a string full of HTML tags
# (like <p>, <a>, etc.) and returns only the clean, readable text.
# 1. BeautifulSoup "parses" the HTML, meaning it understands the structure.
# 2. .get_text() extracts only the text parts, ignoring the tags.
# 3. We use a newline ('\n') as a separator so that paragraphs don't stick together.
# 4. html.unescape() fixes things like '&nbsp;' (which means a space) or '&quot;'.

def clean_html(raw_html):
    if not raw_html:  # If the content is empty (None or ""), just return an empty string
        return ""
    
    # Create a 'soup' object which is a parsed version of our HTML
    soup = BeautifulSoup(raw_html, "html.parser")
    
    # Extract the text. We use separator='\n' so that block elements (like <p>) 
    # keep a line break between them, making the text more readable for the AI.
    clean_text = soup.get_text(separator='\n', strip=True)
    
    # Final cleanup of HTML entities (e.g., converting '&amp;' back to '&')
    return html.unescape(clean_text)

def main():
    # Path to our database file
    db_path = "islamqa_database.sqlite"
    
    # 1. Connect to the database
    # 'conn' stands for connection. It's like opening a phone line to the DB.
    conn = sqlite3.connect(db_path)
    
    # 2. Create a 'cursor'
    # A cursor is like a pointer or a pen that we use to write/read commands.
    cursor = conn.cursor()
    
    print(f"[*] Connected to {db_path}. Starting cleanup...")

    # 3. Fetch all fatwas (questions and answers)
    # We need the 'id' so we know exactly which row to update later.
    cursor.execute("SELECT id, question, answer FROM fatwas")
    rows = cursor.fetchall()  # This gets all 15,000+ rows into memory
    
    total_rows = len(rows)
    print(f"[*] Found {total_rows} rows to process.")

    # 4. Loop through each row and clean the content
    updated_data = []  # We will store the clean data here for a batch update
    
    count = 0
    for row in rows:
        row_id, q_html, a_html = row
        
        # Clean the question and the answer using our function
        clean_q = clean_html(q_html)
        clean_a = clean_html(a_html)
        
        # Add the cleaned data to our list, along with the ID for the WHERE clause
        # The format is (new_question, new_answer, row_id)
        updated_data.append((clean_q, clean_a, row_id))
        
        count += 1
        # Show progress every 1000 rows
        if count % 1000 == 0:
            print(f"[*] Processed {count}/{total_rows} rows...")

    # 5. Perform a "Batch Update"
    # This is MUCH faster than updating one by one. 
    # It tells SQLite: "For every item in updated_data, run this UPDATE command."
    print("[*] Saving changes to database... (this might take a few seconds)")
    cursor.executemany(
        "UPDATE fatwas SET question = ?, answer = ? WHERE id = ?",
        updated_data
    )
    
    # 6. Commit and Close
    # 'commit' is like clicking 'Save' in a document. Without it, changes are lost.
    conn.commit()
    conn.close()
    
    print("[+] Database cleanup complete! All HTML tags removed.")

if __name__ == "__main__":
    # This line ensures the script only runs if we call 'python3 db_filter.py' directly.
    main()
