import os
import gzip
import json
import sqlite3
from datetime import datetime

# --- CONFIGURATION ---
DUMP_FILE = "data.ndjson.gz"
OUTPUT_JSON = "islamqa_data_organized.json"
OUTPUT_DB = "islamqa_database.sqlite"

def setup_database():
    """Initializes the SQLite database with optimized tables."""
    print(f"[*] Initializing SQLite database: {OUTPUT_DB}")
    conn = sqlite3.connect(OUTPUT_DB)
    cursor = conn.cursor()

    # Create Category Table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS categories (
            id INTEGER PRIMARY KEY,
            title TEXT NOT NULL,
            parent_id INTEGER,
            FOREIGN KEY (parent_id) REFERENCES categories (id)
        )
    ''')

    # Create Fatwa Table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS fatwas (
            id INTEGER PRIMARY KEY,
            reference_id INTEGER,
            title TEXT NOT NULL,
            question TEXT,
            answer TEXT,
            category_id INTEGER,
            source TEXT,
            date TEXT,
            views INTEGER,
            url TEXT,
            FOREIGN KEY (category_id) REFERENCES categories (id)
        )
    ''')

    # Indexing for fast search
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_cat_id ON fatwas (category_id)')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_fatwa_title ON fatwas (title)')
    
    conn.commit()
    return conn

def process_dump(conn):
    """Processes the compressed NDJSON dump into JSON and SQLite."""
    cursor = conn.cursor()
    
    all_data = [] # For the JSON file
    categories_map = {} # To avoid duplicate categories
    
    print("[*] Reading data.ndjson.gz...")
    
    count = 0
    with gzip.open(DUMP_FILE, 'rt', encoding='utf-8') as f:
        for line in f:
            try:
                item = json.loads(line)
                if item.get('type') != 'answer':
                    continue
                
                data = item.get('data', {})
                f_id = data.get('id')
                f_title = data.get('title')
                f_question = data.get('question')
                f_answer = data.get('body')
                f_source = data.get('source', {}).get('title', 'IslamQA')
                f_views = data.get('views', 0)
                f_date = data.get('showDate', '')
                f_url = f"https://islamqa.info/en/answers/{f_id}"
                
                # --- ORGANIZE CATEGORIES ---
                topics = data.get('topics', [])
                main_cat_id = None
                
                if topics:
                    # We take the deepest category as the "main" one
                    main_topic = topics[0]
                    t_id = main_topic['reference']
                    t_title = main_topic['title']
                    main_cat_id = t_id
                    
                    # Store hierarchy in DB
                    ancestors = main_topic.get('ancestors', [])
                    prev_id = None
                    for anc in ancestors:
                        a_id = anc['reference']
                        a_title = anc['title']
                        if a_id not in categories_map:
                            cursor.execute('INSERT OR IGNORE INTO categories (id, title, parent_id) VALUES (?, ?, ?)', (a_id, a_title, prev_id))
                            categories_map[a_id] = a_title
                        prev_id = a_id
                    
                    if t_id not in categories_map:
                        cursor.execute('INSERT OR IGNORE INTO categories (id, title, parent_id) VALUES (?, ?, ?)', (t_id, t_title, prev_id))
                        categories_map[t_id] = t_title

                # --- INSERT FATWA ---
                cursor.execute('''
                    INSERT OR IGNORE INTO fatwas (id, reference_id, title, question, answer, category_id, source, date, views, url)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ''', (f_id, f_id, f_title, f_question, f_answer, main_cat_id, f_source, f_date, f_views, f_url))

                # --- ADD TO JSON LIST ---
                all_data.append({
                    "id": f_id,
                    "title": f_title,
                    "category": categories_map.get(main_cat_id, "General"),
                    "question": f_question,
                    "answer": f_answer,
                    "metadata": {
                        "date": f_date,
                        "source": f_source,
                        "views": f_views,
                        "url": f_url
                    }
                })

                count += 1
                if count % 5000 == 0:
                    print(f"    Processed {count} fatwas...")

            except Exception as e:
                continue

    conn.commit()
    print(f"[+] SQLite Database Ready: {count} fatwas saved.")

    # Save JSON
    print(f"[*] Saving organized JSON: {OUTPUT_JSON}")
    with open(OUTPUT_JSON, "w", encoding="utf-8") as jf:
        json.dump(all_data, jf, indent=4, ensure_ascii=False)
    print("[+] JSON File Saved.")

if __name__ == "__main__":
    if not os.path.exists(DUMP_FILE):
        print(f"[!] Error: {DUMP_FILE} not found. Please download it first.")
    else:
        conn = setup_database()
        process_dump(conn)
        conn.close()
        print("\n[SUCCESS] Data transformation complete!")
        print(f"1. SQLite: {OUTPUT_DB}")
        print(f"2. JSON: {OUTPUT_JSON}")
