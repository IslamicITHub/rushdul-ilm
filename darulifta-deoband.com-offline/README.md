# Darul Ifta Deoband Scrapy Downloader (`downloader_claude.py`)

This documentation provides a comprehensive technical reference and architectural explanation for the `downloader_claude.py` script. It is designed for developers maintaining the codebase and AI agents performing "vibe coding" or system integration.

## 1. Mission and Intent

The `downloader_claude.py` is the primary data ingestion engine for the **Rushd-ul-Ilm** Islamic Knowledge Q&A App. Its goal is to create a high-fidelity, searchable, and 100% offline-ready archive of the Darul Ifta Deoband English fatwa database.

### Core Objectives
*   **Data Sovereignty:** Extract structured Islamic knowledge (Fatwas) to be used in a Retrieval-Augmented Generation (RAG) pipeline.
*   **Offline Accessibility:** Generate a local cache of HTML and JSON data to support users with unreliable internet connectivity.
*   **System Alignment:** Function as the **Layer 3 (Islamic Knowledge Sources)** component defined in the *Islamic App System Design Architecture*.

---

## 2. Architecture & Tech Stack

The script is built on a modern, asynchronous-ready Python stack optimized for speed and reliability.

### The Stack
*   **Automation:** [Playwright](https://playwright.dev/) via Chrome DevTools Protocol (CDP) for Cloudflare bypass.
*   **Parsing:** [Parsel](https://parsel.readthedocs.io/) (the engine behind Scrapy) for high-speed CSS/XPath extraction.
*   **Concurrency:** `ThreadPoolExecutor` for parallelized fatwa downloads.
*   **Storage:** 
    *   **SQLite:** Metadata, crawl state, and Full-Text Search (FTS5).
    *   **JSON:** Structured data exchange for Vector DB ingestion.
    *   **HTML:** Rewritten, offline-navigable page cache.

---

## 3. Engineering Strategy & Logic

### Cloudflare Bypass (The CDP Tunnel)
To navigate around sophisticated bot detection:
1.  **Human Verification:** The user launches a real Chromium instance and completes the Cloudflare "Turnstile" manually.
2.  **CDP Connection:** The script connects to this existing browser via port `9222`.
3.  **Cookie Cloning:** It extracts the valid session cookies and injects them into a `requests.Session` object, allowing for high-speed parallel downloads without triggering bot alerts.

### Smart Pagination
Unlike previous iterations, this script dynamically discovers the total number of pages in a category:
*   It identifies the `aria-label="Last"` pagination link.
*   It extracts the maximum page number using regex.
*   It iterates through the entire range, ensuring 100% data coverage.

### Robust Content Extraction
Using `parsel`, the script employs a multi-tier extraction strategy:
*   **Tier 1:** Targeted CSS selectors for `Question` and `Answer` containers.
*   **Tier 2:** XPath label-based extraction (e.g., finding the sibling of an element containing the word "Question").
*   **Tier 3:** Plain-text heuristic parsing as a final fallback.

---

## 4. Database & Storage Specification

### Directory Structure
```text
offline_darulifta2/
├── offline.sqlite      # SQLite database with FTS5 search index
├── data/               # Structured JSON records (source for Vector DB)
├── site/               # The offline website root
│   ├── index.html      # Searchable archive entry point
│   ├── search_index.json # Metadata for app-level search
│   └── pages/          # Cached HTML files
└── browser_profile/    # Persistent Playwright session data
```

### SQLite Schema Highlights
The schema includes an **FTS5 Virtual Table** for near-instant searching:
*   **`fatwas` table:** Primary storage for structured Q&A.
*   **`fatwas_fts` table:** A full-text index on `title`, `question`, and `answer`.
*   **Automated Triggers:** SQL triggers ensure the search index is updated automatically during inserts/updates.

---

## 5. Usage Guide

### Prerequisites
1.  Install dependencies: `pip install playwright parsel requests`
2.  Install Playwright browser: `playwright install chromium`

### Running a Crawl
1.  **Start the Browser:**
    ```bash
    chromium --remote-debugging-port=9222 --user-data-dir=/tmp/darulifta-profile
    ```
2.  **Verify Manually:** Navigate to the site and solve the Cloudflare puzzle.
3.  **Run the Script:**
    ```bash
    python downloader_claude.py --workers 5 --delay 1.0
    ```

---

## 6. Developer & AI Agent Notes (Vibe Coding)

*   **Thread Safety:** Always use `self.lock` (SQLite) and `self.io_lock` (Filesystem) when modifying state in multi-threaded methods.
*   **Normalization:** All URLs must be passed through `normalize_url()` to prevent duplicate entries caused by trailing slashes or fragments.
*   **Politeness:** Never set `--delay` to `0` in production; the server employs rate-limiting that cookies alone cannot bypass.
*   **Compatibility:** The SQLite schema is designed to be 100% compatible with `downloader2.py`.

---

## 7. System Design Alignment

| Component | Alignment Detail |
| :--- | :--- |
| **Layer 3** | Implements the Scrapy-based ingestion pipeline. |
| **Layer 4** | Generates the JSON data required for the Qdrant Vector DB. |
| **Tier 1 Strategy** | Enables 100% offline browsing via the `site/` directory. |
| **Trust Model** | Preserves original Source URLs for religious accuracy as required by the System Design. |
