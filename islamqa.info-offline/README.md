# IslamQA Offline Mirror & Database System

This documentation provides a technical reference for the `fast_mirror.py` and `dump_to_db.py` scripts, which together form an automated pipeline for maintaining an offline, searchable archive of IslamQA.info fatwas.

---

## 1. Overview
The system is designed to synchronize local data with the official IslamQA data dumps, transform that data into a structured SQLite database, and generate a high-fidelity "PRO" static mirror for offline browsing.

### Core Components
- **`fast_mirror.py`**: The main orchestrator. Handles update discovery, asset downloading, and static site generation.
- **`dump_to_db.py`**: The data transformation engine. Parses raw NDJSON dumps into a relational SQLite database.

---

## 2. `dump_to_db.py` - Database Engine

This script is responsible for taking the compressed raw data and creating a structured relational database and an organized JSON file.

### Configuration
- `DUMP_FILE`: "data.ndjson.gz" (Input)
- `OUTPUT_JSON`: "islamqa_data_organized.json" (Output)
- `OUTPUT_DB`: "islamqa_database.sqlite" (Output)

### Database Schema
The script initializes a SQLite database with two main tables:
- **`categories`**: Stores the hierarchical topic tree.
    - `id`: Unique category ID.
    - `title`: Name of the category.
    - `parent_id`: Reference to the parent category (Foreign Key).
- **`fatwas`**: Stores the actual Q&A content.
    - `id`: Unique fatwa ID.
    - `title`, `question`, `answer`: Content fields.
    - `category_id`: Reference to the deepest category (Foreign Key).
    - `date`, `views`, `url`, `source`: Metadata.

### Key Functions
- **`setup_database()`**: Creates tables and indexes (`idx_cat_id`, `idx_fatwa_title`) to ensure fast searching.
- **`process_dump(conn)`**: 
    1. Reads `data.ndjson.gz` line-by-line using `gzip`.
    2. Filters for items of type `answer`.
    3. Reconstructs the category hierarchy from the `ancestors` list in the JSON.
    4. Inserts data into the `categories` and `fatwas` tables using `INSERT OR IGNORE`.
    5. Saves a flat JSON list of all fatwas for external use.

---

## 3. `fast_mirror.py` - Mirroring & Automation

This script automates the retrieval of new content and generates a static website that mimics the "PRO" look of the official site.

### Automation & Updates
The script implements "Offline Settings" logic to find the latest updates:
- **`fetch_latest_dump()`**: 
    - Fetches the `manifest.json` from the official ZadApps server.
    - Identifies the latest full English dump (`lang: "en"`).
    - Downloads the `.gz` file directly to the local directory, bypassing automatic decompression to maintain GZIP integrity.
- **`update_database()`**: Automatically imports and calls `dump_to_db` logic to synchronize the SQLite database with the newly downloaded dump.

### Mirror Generation
- **`download_pro_assets()`**: Downloads the official CSS files and SVG logo to ensure the offline site looks like the real one.
- **`generate_pro_html()`**: 
    - Uses a comprehensive HTML template with Tailwind-like classes (`tw-font-bold`, `tw-bg-paperQuestion`).
    - Organizes files into subfolders (e.g., `en/answers/12/12345.html`) to avoid directory performance issues with thousands of files.
    - Reconstructs breadcrumbs based on the topic ancestors.
- **`create_pro_index()`**: Generates a main `index.html` listing all fatwas sorted by ID (newest first).

---

## 4. Data Flow Architecture

1.  **Check Manifest**: `fast_mirror.py` checks `manifest.json` for new English dumps.
2.  **Download**: The latest `data.ndjson.gz` is downloaded.
3.  **Database Sync**: `dump_to_db.py` parses the dump, populates `islamqa_database.sqlite`.
4.  **Static Generation**: `fast_mirror.py` iterates through the dump to generate thousands of HTML files.
5.  **Asset Refresh**: Design assets (CSS/Logo) are updated to match the latest site version.

---

## 5. Usage

To run the full pipeline (update data + update DB + rebuild mirror):

```bash
python3 fast_mirror.py
```

### Requirements
- `requests`: For fetching assets and dumps.
- `sqlite3`: Standard library for database operations.
- `gzip`, `json`: Standard library for data parsing.

### Output Location
The generated mirror will be in the `islamqa_pro_mirror/` directory. Open `index.html` in any browser to begin browsing offline.
