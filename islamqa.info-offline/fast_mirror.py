import os
import gzip
import json
import requests
import dump_to_db
from datetime import datetime

# --- CONFIGURATION ---
MANIFEST_URL = "https://files.zadapps.info/m.islamqa.info/dumps/manifest.json"
BASE_URL = "https://files.zadapps.info/m.islamqa.info/"
DUMP_FILE = "data.ndjson.gz"
OUTPUT_DIR = "islamqa_pro_mirror"
ASSETS_DIR = os.path.join(OUTPUT_DIR, "assets")
ANSWERS_DIR = os.path.join(OUTPUT_DIR, "en/answers")

# Official CSS Links for the PRO look
CSS_FILES = [
    "https://files.zadapps.info/m.islamqa.info/next-builds/1.2.354/_next/static/css/d3c82a541b62415a.css",
    "https://islamqa.info/assets/fontsource/noto-sans/index.css"
]

# --- HTML TEMPLATE (PRO VERSION) ---
# ... (rest of template remains the same)
HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="en" dir="ltr" data-theme="green">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <title>{title} - Islam Question & Answer</title>
    <link rel="stylesheet" href="../../assets/site_style.css">
    <link rel="stylesheet" href="../../assets/noto_sans.css">
    <style>
        /* Custom overrides to make the offline experience smoother */
        body {{ background-color: #f3f4f6; }}
        .pro-container {{ max-width: 720px; margin: 0 auto; padding: 20px; }}
        header.pro-header {{ background: white; padding: 15px; border-bottom: 1px solid #e5e7eb; display: flex; align-items: center; justify-content: center; position: sticky; top: 0; z-index: 50; }}
        .breadcrumb-custom {{ margin-bottom: 20px; font-size: 0.875rem; color: #6b7280; }}
    </style>
</head>
<body>
    <header class="pro-header">
        <img src="../../assets/logo.svg" alt="IslamQA" style="height: 40px;">
    </header>

    <main class="pro-container">
        <!-- BREADCRUMBS -->
        <nav class="breadcrumb-custom">
            {category}
        </nav>

        <!-- ARTICLE CONTENT -->
        <article id="single-post-content" class="tw-flex tw-flex-col tw-gap-3 tw-mb-2">
            <header>
                <h1 data-sut="question-title" class="tw-font-bold tw-text-[1.43em] tw-m-0 tw-font-title">
                    {title}
                </h1>
                <div class="tw-flex tw-flex-wrap tw-gap-2 tw-mt-2">
                    <time class="tw-text-gray-500 tw-font-bold tw-text-[0.9em]">
                        {date}
                    </time>
                </div>
            </header>

            <!-- QUESTION SECTION -->
            <section class="tw-bg-paperQuestion tw-border tw-border-[#FCE96A] tw-rounded-lg tw-p-3 tw-mt-4">
                <h2 class="tw-font-title tw-text-[#633112] tw-font-bold tw-m-0 tw-mb-1 tw-text-[1.14em]">
                    Question {id}
                </h2>
                <div class="tw-text-gray-900 tw-text-[1.14em]">
                    {question}
                </div>
            </section>

            <!-- ANSWER SECTION -->
            <section class="tw-mt-6">
                <h2 class="tw-font-bold tw-text-[1.2em] tw-mb-4 tw-border-b-2 tw-border-primary tw-inline-block">
                    Answer
                </h2>
                <div class="tw-text-gray-900 tw-text-[1.14em] tw-break-words">
                    {answer}
                </div>
            </section>

            <!-- SOURCE INFO -->
            <footer class="tw-mt-10 tw-pt-4 tw-border-top tw-text-gray-600">
                <strong>Source:</strong> {source}
            </footer>
        </article>
    </main>

    <footer style="text-align: center; padding: 40px; color: #9ca3af; font-size: 0.75rem;">
        &copy; 1997-2025 Islam Question & Answer. All rights reserved.
    </footer>
</body>
</html>
"""

def setup_pro_folders():
    for folder in [OUTPUT_DIR, ASSETS_DIR, ANSWERS_DIR]:
        os.makedirs(folder, exist_ok=True)

def download_pro_assets():
    print("[*] Downloading official website design files...")
    # 1. CSS
    try:
        r1 = requests.get(CSS_FILES[0])
        with open(os.path.join(ASSETS_DIR, "site_style.css"), "w") as f:
            f.write(r1.text)
        
        r2 = requests.get(CSS_FILES[1])
        with open(os.path.join(ASSETS_DIR, "noto_sans.css"), "w") as f:
            f.write(r2.text)
    except:
        print("[!] Warning: Could not download official CSS. Using fallbacks.")

    # 2. Logo
    logo_url = "https://files.zadapps.info/m.islamqa.info/next-builds/1.2.354/_next/static/media/en-logo-g.7069c2d7.svg"
    try:
        r_logo = requests.get(logo_url)
        with open(os.path.join(ASSETS_DIR, "logo.svg"), "wb") as f:
            f.write(r_logo.content)
    except: pass

def fetch_latest_dump():
    """Scrapes manifest.json to find and download the latest English dump."""
    print(f"[*] Fetching manifest: {MANIFEST_URL}")
    try:
        r = requests.get(MANIFEST_URL)
        manifest = r.json()
        
        # Find the latest English full dump
        en_dump = next((d for d in manifest.get('dumps', []) if d.get('lang') == 'en'), None)
        if not en_dump:
            print("[!] Error: English dump not found in manifest.")
            return False
        
        folder = en_dump['folder']
        filename = en_dump['file']['name']
        dump_url = f"{BASE_URL}{folder}/{filename}"
        
        print(f"[*] Downloading latest English dump: {dump_url}")
        r_dump = requests.get(dump_url, stream=True)
        with open(DUMP_FILE, 'wb') as f:
            for chunk in r_dump.raw.stream(8192, decode_content=False):
                f.write(chunk)
        print(f"[+] Downloaded: {DUMP_FILE}")
        return True
    except Exception as e:
        print(f"[!] Error updating dump: {e}")
        return False

def update_database():
    """Uses dump_to_db script to update the SQLite database."""
    print("[*] Updating SQLite database using dump_to_db...")
    try:
        conn = dump_to_db.setup_database()
        dump_to_db.process_dump(conn)
        conn.close()
        print("[+] Database update successful.")
        return True
    except Exception as e:
        print(f"[!] Error updating database: {e}")
        return False

def get_pro_category(topics):
    if not topics: return "Home"
    path = []
    main_topic = topics[0]
    if 'ancestors' in main_topic and main_topic['ancestors']:
        for anc in main_topic['ancestors']:
            path.append(f"<span>{anc['title']}</span>")
    path.append(f"<span style='color: #006633; font-weight: bold;'>{main_topic['title']}</span>")
    return " &gt; ".join(path)

def generate_pro_html(json_line, index_list):
    try:
        item = json.loads(json_line)
        if item.get('type') != 'answer': return
        
        data = item.get('data', {})
        id = data.get('id')
        title = data.get('title', 'No Title')
        # Some dumps have HTML in question/answer, some don't. We handle both.
        question = data.get('question', 'No Question')
        answer = data.get('body', 'No Answer')
        source = data.get('source', {}).get('title', 'IslamQA')
        
        date_str = data.get('showDate', '')
        try:
            date = datetime.strptime(date_str.split('T')[0], '%Y-%m-%d').strftime('%d %B %Y')
        except: date = date_str

        category = get_pro_category(data.get('topics', []))

        subfolder = str(id)[:2] if len(str(id)) > 2 else "0"
        os.makedirs(os.path.join(ANSWERS_DIR, subfolder), exist_ok=True)
        
        file_path = os.path.join(ANSWERS_DIR, subfolder, f"{id}.html")
        
        html = HTML_TEMPLATE.format(
            title=title, category=category, id=id,
            question=question, answer=answer, source=source,
            date=date
        )
        
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(html)
        
        index_list.append({"id": id, "title": title, "path": f"en/answers/{subfolder}/{id}.html"})
    except: pass

def create_pro_index(index_list):
    print("[*] Generating professional index.html...")
    path = os.path.join(OUTPUT_DIR, "index.html")
    
    items_html = ""
    for item in sorted(index_list, key=lambda x: int(x['id']), reverse=True):
        items_html += f"""
        <a href="{item['path']}" class="tw-block tw-p-4 tw-bg-white tw-rounded-xl tw-border tw-border-gray-200 tw-mb-3 hover:tw-border-primary tw-transition-colors">
            <span class="tw-text-gray-400 tw-font-bold tw-mr-2">#{item['id']}</span>
            <span class="tw-text-gray-900 tw-font-medium">{item['title']}</span>
        </a>"""

    index_page = f"""<!DOCTYPE html>
<html lang="en" data-theme="green">
<head>
    <meta charset="UTF-8">
    <title>IslamQA Official Offline Archive</title>
    <link rel="stylesheet" href="assets/site_style.css">
    <style>
        body {{ background: #f9fafb; font-family: sans-serif; padding: 20px; }}
        .header {{ text-align: center; margin-bottom: 40px; }}
        .search-box {{ max-width: 600px; margin: 0 auto 30px; }}
        .list-container {{ max-width: 800px; margin: 0 auto; }}
        .tw-block {{ text-decoration: none; display: block; }}
    </style>
</head>
<body>
    <div class="header">
        <img src="assets/logo.svg" style="height: 60px;">
        <h1 class="tw-text-2xl tw-font-bold tw-mt-4">Official Offline Archive</h1>
        <p class="tw-text-gray-500">Total Fatwas: {len(index_list)}</p>
    </div>
    <div class="list-container">
        {items_html}
    </div>
</body>
</html>"""
    with open(path, "w", encoding="utf-8") as f:
        f.write(index_page)

def run_pro_mirror():
    print("--- IslamQA PRO Mirror Tool ---")
    setup_pro_folders()
    download_pro_assets()
    
    # NEW: Fetch latest updates
    if fetch_latest_dump():
        update_database()
    
    if not os.path.exists(DUMP_FILE):
        print(f"[!] Error: {DUMP_FILE} not found. Could not update.")
        return

    index_list = []
    print("[*] Building the PRO mirror... (Please wait)")
    answers_generated = 0
    with gzip.open(DUMP_FILE, 'rt', encoding='utf-8') as f:
        for i, line in enumerate(f):
            if answers_generated >= 50:
                print("    Reached 50 answers subset limit. Stopping.")
                break
            # generate_pro_html needs to return true if it was an answer
            try:
                item = json.loads(line)
                if item.get('type') == 'answer':
                    generate_pro_html(line, index_list)
                    answers_generated += 1
            except Exception:
                pass
            if i > 0 and i % 5000 == 0: print(f"    Building... {i} lines processed, {answers_generated} answers done.")
            
    create_pro_index(index_list)
    print(f"\n[SUCCESS] PRO Mirror complete! Open {os.path.abspath(os.path.join(OUTPUT_DIR, 'index.html'))}")

if __name__ == "__main__":
    run_pro_mirror()
