#!/usr/bin/env python3
"""
darulifta_scrapy_downloader.py
==============================================================
New Darul Ifta Deoband offline downloader — powered by Scrapy's
parsel library for HTML parsing (replaces BeautifulSoup4).

KEY IMPROVEMENTS OVER THE OLD downloader2_old.py:
  1. Uses parsel (Scrapy's CSS/XPath selector library) instead of BeautifulSoup4.
     Parsel is faster, has cleaner syntax, and is the same engine Scrapy uses internally.
  2. Full pagination support — finds the LAST page of each category and iterates
     through every single page (old script only handled pages 1, 2, 3).
  3. Parallel downloading of fatwa detail pages using ThreadPoolExecutor.
  4. Fallback to sequential (single-worker) mode if parallel processing fails.
  5. Same SQLite database schema as the old script — 100% compatible, you can
     continue a download that the old script started.

HOW TO RUN:
  Step 1 — Open Terminal 1 and run this command to start a Chromium browser
            that this script can connect to (required to bypass Cloudflare):
    chromium --remote-debugging-port=9222 --user-data-dir=/tmp/darulifta-real-browser

  Step 2 — In the Chromium window, manually navigate to darulifta-deoband.com/en
            and complete any Cloudflare verification if it appears (click the checkbox).

  Step 3 — Open Terminal 2 and run this script:
    python darulifta_scrapy_downloader.py --cdp-url http://127.0.0.1:9222 --workers 3

COMMAND-LINE OPTIONS:
  --cdp-url       URL of the running Chromium browser (default: http://127.0.0.1:9222)
  --workers       How many parallel threads to use for downloading fatwa pages (default: 3)
  --output-dir    Where to save files (default: offline_darulifta2)
  --delay         Seconds to wait between page requests (default: 1.5)
  --full-refresh  Re-download fatwas that were already downloaded before
  --max-pages-per-category  Safety limit (e.g. 5 means only first 5 pages per category)
  --headless      Run without a visible browser window
  --no-site-pages Skip crawling non-category pages like About, Contact

OUTPUT FILES:
  offline_darulifta2/site/         Offline-browsable HTML pages
  offline_darulifta2/data/         JSON records (one per fatwa)
  offline_darulifta2/offline.sqlite  SQLite database with all Q&A content
  offline_darulifta2/site/index.html  Searchable index page for offline browsing
"""

# ---------------------------------------------------------------------------
# STANDARD LIBRARY IMPORTS
# ---------------------------------------------------------------------------

# __future__.annotations allows using modern type hint syntax (like str | None)
# even on older Python versions.
from __future__ import annotations

# argparse reads command-line arguments like --workers 3 and --cdp-url ...
import argparse

# hashlib creates SHA-256 fingerprints of content so we can detect changes.
import hashlib

# html provides html.escape() which converts characters like < > & into safe HTML entities.
import html

# json reads and writes .json files — used to save one JSON file per fatwa.
import json

# mimetypes guesses file extensions from content types like "image/png" → ".png"
import mimetypes

# os provides os.path.relpath() for creating relative file paths in HTML links.
import os

# re provides regular expressions — used to find page numbers in URLs, extract IDs, etc.
import re

# requests makes HTTP GET requests with cookies — used for fast parallel fatwa downloads.
import requests

# shutil.which() finds installed programs like chromium, google-chrome, etc.
import shutil

# sqlite3 is Python's built-in SQLite database module.
import sqlite3

# sys gives access to command-line arguments (sys.argv).
import sys

# threading provides RLock() — a lock that prevents two threads writing to the DB at once.
import threading

# time.sleep() pauses between requests so we don't hammer the server.
import time

# ThreadPoolExecutor runs multiple fatwa downloads at the same time.
# as_completed() yields futures as they finish (not in submission order).
from concurrent.futures import ThreadPoolExecutor, as_completed

# dataclass is a decorator that auto-generates __init__ and other boilerplate.
from dataclasses import dataclass

# datetime.now() with timezone=utc gives us UTC timestamps for the database.
from datetime import datetime, timezone

# Path is a modern, cleaner way to handle file and directory paths.
from pathlib import Path

# Iterable is a type hint — it means "any collection you can loop over".
from typing import Iterable

# URL utilities:
#   urldefrag  → removes the #fragment part from a URL
#   urljoin    → resolves a relative URL against a base URL
#   urlparse   → breaks a URL into scheme, netloc, path, query, fragment parts
#   parse_qs   → parses ?key=value query strings into dictionaries
#   urlunparse → reassembles URL parts back into a full URL string
#   urlencode  → converts a dict into a ?key=value query string
from urllib.parse import urldefrag, urljoin, urlparse, parse_qs, urlunparse, urlencode

# ---------------------------------------------------------------------------
# THIRD-PARTY LIBRARY IMPORTS
# ---------------------------------------------------------------------------

# parsel is Scrapy's HTML/CSS/XPath selector library.
# It replaces BeautifulSoup4 in this script.
#
# INSTALLATION (if you don't have it):
#   pip install parsel
#   OR if you install Scrapy: pip install scrapy  (parsel comes with it)
#
# KEY DIFFERENCE FROM BEAUTIFULSOUP4:
#   BeautifulSoup4: soup.find("ul", class_="pagination")
#   parsel:         selector.css("ul.pagination")
#
#   BeautifulSoup4: tag.get_text()
#   parsel:         selector.css("::text").getall()
#
# Parsel uses CSS selectors (like web browsers use) and XPath (a powerful
# query language for XML/HTML). CSS selectors are easier to read.
try:
    import parsel  # type: ignore[import]
except ImportError:
    # If parsel is not installed, show a helpful error message and exit.
    print("ERROR: 'parsel' library is not installed.")
    print("Install it with:  pip install parsel")
    print("Or install scrapy (which includes parsel):  pip install scrapy")
    sys.exit(1)

# playwright is used to control the Chromium browser via CDP (Chrome DevTools Protocol).
# We need a real browser to bypass Cloudflare's bot detection.
try:
    from playwright.sync_api import BrowserContext, Page, TimeoutError as PlaywrightTimeoutError, sync_playwright
except ImportError:
    print("ERROR: 'playwright' library is not installed.")
    print("Install it with:  pip install playwright && playwright install chromium")
    sys.exit(1)


# ---------------------------------------------------------------------------
# GLOBAL CONSTANTS
# ---------------------------------------------------------------------------

# The base URL for the English section of the website we are downloading.
DEFAULT_BASE_URL = "https://darulifta-deoband.com/en"

# The default folder where all downloaded files will be saved.
DEFAULT_OUTPUT_DIR = "offline_darulifta2"

# A realistic browser User-Agent string. Websites use this to identify
# what browser is making the request. We pretend to be a real Chrome browser.
USER_AGENT = (
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
)

# Content types that indicate the response is an HTML page (not an image/CSS/JS).
HTMLISH_TYPES = (
    "text/html",
    "application/xhtml+xml",
)

# HTML tag names and their URL-containing attributes.
# We scan these to find all assets (images, CSS, JS files) that a page references.
# This is used when saving offline HTML files with locally-downloaded assets.
ASSET_ATTRS = (
    ("img",    "src"),
    ("img",    "srcset"),
    ("source", "src"),
    ("source", "srcset"),
    ("script", "src"),
    ("link",   "href"),
    ("video",  "src"),
    ("audio",  "src"),
    ("iframe", "src"),
)

# External hosts whose scripts/images we skip — AddThis is a social sharing
# widget that adds tracking and is not useful for offline use.
BLOCKED_ASSET_HOSTS = {
    "s7.addthis.com",
    "addthis.com",
    "www.addthis.com",
}


# ---------------------------------------------------------------------------
# DATA CLASSES
# ---------------------------------------------------------------------------

@dataclass(frozen=True)
class CrawlPaths:
    """
    A frozen data class (cannot be modified after creation) that holds all
    the directory and file paths used by this script.

    frozen=True means Python will raise an error if you accidentally try to
    change any attribute after the object is created.
    """
    # The top-level output directory (e.g. offline_darulifta2/)
    output: Path

    # Where HTML pages are saved for offline browsing
    site: Path

    # Individual HTML page files go here
    pages: Path

    # Downloaded CSS/images/JS assets go here
    assets: Path

    # JSON records (one per fatwa) go here
    data: Path

    # Playwright browser profile folder (stores Cloudflare cookies)
    profile: Path

    # The SQLite database file path
    db: Path

    @classmethod
    def build(cls, output_dir: str) -> "CrawlPaths":
        """
        Create a CrawlPaths object from a directory name string.
        .resolve() converts it to an absolute path (e.g. /home/user/offline_darulifta2).
        """
        output = Path(output_dir).resolve()
        return cls(
            output=output,
            site=output / "site",
            pages=output / "site" / "pages",
            assets=output / "site" / "assets",
            data=output / "data",
            profile=output / "browser_profile",
            db=output / "offline.sqlite",
        )

    def ensure(self) -> None:
        """
        Create all required directories if they don't already exist.
        parents=True creates parent directories too (like mkdir -p in Linux).
        exist_ok=True prevents an error if the directory already exists.
        """
        for path in (self.site, self.pages, self.assets, self.data, self.profile):
            path.mkdir(parents=True, exist_ok=True)


# ---------------------------------------------------------------------------
# SQLITE DATABASE CLASS
# ---------------------------------------------------------------------------

class OfflineStore:
    """
    Manages all reading and writing to the SQLite database.

    This class is thread-safe — it uses a threading.RLock() (re-entrant lock)
    so that multiple parallel download threads don't corrupt the database by
    writing at the same time.

    The database schema is IDENTICAL to the old downloader2_old.py script,
    so you can continue a download that the old script started.
    """

    def __init__(self, db_path: Path) -> None:
        """
        Opens (or creates) the SQLite database and sets up the tables.
        check_same_thread=False is required because we use multiple threads.
        """
        # RLock = Re-entrant Lock. A thread that already holds this lock can
        # acquire it again without deadlocking (useful for nested calls).
        self.lock = threading.RLock()

        # sqlite3.connect() opens the .sqlite file. If it doesn't exist, it creates it.
        self.conn = sqlite3.connect(db_path, check_same_thread=False)

        # Row factory makes rows behave like dicts: row["column_name"] instead of row[0]
        self.conn.row_factory = sqlite3.Row

        # Run all the CREATE TABLE IF NOT EXISTS statements
        self.setup()

    def setup(self) -> None:
        """
        Create all database tables and full-text search indexes if they don't exist.
        This uses the exact same schema as downloader2_old.py.
        """
        with self.lock:
            # WAL = Write-Ahead Logging: makes SQLite faster and more crash-resistant
            self.conn.execute("PRAGMA journal_mode = WAL")
            # busy_timeout: if the DB is locked, wait up to 30 seconds before giving up
            self.conn.execute("PRAGMA busy_timeout = 30000")

            # executescript() runs multiple SQL statements at once.
            # CREATE TABLE IF NOT EXISTS = safe to run even if the table already exists.
            self.conn.executescript("""
                -- Categories table: stores each fatwa category (like "Zakat & Charity")
                CREATE TABLE IF NOT EXISTS categories (
                    url                     TEXT PRIMARY KEY,
                    name                    TEXT NOT NULL,
                    last_seen_at            TEXT,
                    last_crawled_at         TEXT,
                    newest_known_fatwa_url  TEXT
                );

                -- Fatwas table: one row per Q&A, stores the full question and answer text
                CREATE TABLE IF NOT EXISTS fatwas (
                    url                 TEXT PRIMARY KEY,
                    fatwa_id            TEXT,
                    category_url        TEXT,
                    category_name       TEXT,
                    title               TEXT,
                    question            TEXT,
                    answer              TEXT,
                    answer_id           TEXT,
                    html_path           TEXT,
                    json_path           TEXT,
                    content_hash        TEXT,
                    first_seen_at       TEXT,
                    last_seen_at        TEXT,
                    last_downloaded_at  TEXT,
                    FOREIGN KEY(category_url) REFERENCES categories(url)
                );

                -- Pages table: tracks all HTML pages downloaded (category listings, etc.)
                CREATE TABLE IF NOT EXISTS pages (
                    url           TEXT PRIMARY KEY,
                    kind          TEXT NOT NULL,
                    local_path    TEXT,
                    content_hash  TEXT,
                    http_status   INTEGER,
                    fetched_at    TEXT
                );

                -- Assets table: tracks downloaded CSS/image/JS files
                CREATE TABLE IF NOT EXISTS assets (
                    url           TEXT PRIMARY KEY,
                    local_path    TEXT NOT NULL,
                    content_type  TEXT,
                    content_hash  TEXT,
                    downloaded_at TEXT
                );

                -- Full-text search virtual table — allows fast keyword search
                -- across fatwa titles, questions, and answers.
                -- This is what the Islamic App will use to search fatwas.
                CREATE VIRTUAL TABLE IF NOT EXISTS fatwas_fts USING fts5(
                    fatwa_id,
                    title,
                    question,
                    answer,
                    category_name,
                    content='fatwas',
                    content_rowid='rowid'
                );

                -- Triggers: automatically update the full-text search index
                -- whenever a row is inserted, deleted, or updated in the fatwas table.
                CREATE TRIGGER IF NOT EXISTS fatwas_ai AFTER INSERT ON fatwas BEGIN
                    INSERT INTO fatwas_fts(rowid, fatwa_id, title, question, answer, category_name)
                    VALUES (new.rowid, new.fatwa_id, new.title, new.question, new.answer, new.category_name);
                END;

                CREATE TRIGGER IF NOT EXISTS fatwas_ad AFTER DELETE ON fatwas BEGIN
                    INSERT INTO fatwas_fts(fatwas_fts, rowid, fatwa_id, title, question, answer, category_name)
                    VALUES ('delete', old.rowid, old.fatwa_id, old.title, old.question, old.answer, old.category_name);
                END;

                CREATE TRIGGER IF NOT EXISTS fatwas_au AFTER UPDATE ON fatwas BEGIN
                    INSERT INTO fatwas_fts(fatwas_fts, rowid, fatwa_id, title, question, answer, category_name)
                    VALUES ('delete', old.rowid, old.fatwa_id, old.title, old.question, old.answer, old.category_name);
                    INSERT INTO fatwas_fts(rowid, fatwa_id, title, question, answer, category_name)
                    VALUES (new.rowid, new.fatwa_id, new.title, new.question, new.answer, new.category_name);
                END;
            """)
            self.conn.commit()

    def known_fatwa_count(self) -> int:
        """Returns the total number of fatwas in the database."""
        with self.lock:
            row = self.conn.execute("SELECT COUNT(*) FROM fatwas").fetchone()
            return row[0] if row else 0

    def has_fatwa(self, url: str) -> bool:
        """Returns True if this fatwa URL is already in the database."""
        with self.lock:
            row = self.conn.execute("SELECT 1 FROM fatwas WHERE url = ?", (url,)).fetchone()
            return row is not None

    def upsert_category(self, url: str, name: str) -> None:
        """
        Saves or updates a category.
        INSERT OR IGNORE = insert if new, skip if already exists.
        Then UPDATE the last_seen_at timestamp.
        """
        now = utc_now()
        with self.lock:
            self.conn.execute(
                "INSERT OR IGNORE INTO categories(url, name, last_seen_at) VALUES (?, ?, ?)",
                (url, name, now),
            )
            self.conn.execute(
                "UPDATE categories SET last_seen_at = ? WHERE url = ?",
                (now, url),
            )
            self.conn.commit()

    def categories(self) -> list:
        """Returns all known categories, sorted alphabetically by name."""
        with self.lock:
            return list(self.conn.execute(
                "SELECT url, name, newest_known_fatwa_url FROM categories ORDER BY name COLLATE NOCASE"
            ))

    def mark_category_crawled(self, category_url: str, newest_fatwa_url: str | None) -> None:
        """
        Records that we have finished crawling a category.
        COALESCE(?, newest_known_fatwa_url) means: use the new value if not None,
        otherwise keep the old value.
        """
        with self.lock:
            self.conn.execute(
                """
                UPDATE categories
                SET last_crawled_at = ?, newest_known_fatwa_url = COALESCE(?, newest_known_fatwa_url)
                WHERE url = ?
                """,
                (utc_now(), newest_fatwa_url, category_url),
            )
            self.conn.commit()

    def has_page(self, url: str) -> bool:
        """Returns True if this page URL has already been downloaded."""
        with self.lock:
            row = self.conn.execute("SELECT 1 FROM pages WHERE url = ?", (url,)).fetchone()
            return row is not None

    def upsert_page(
        self, url: str, kind: str, local_path: Path | None, content_hash: str | None, status: int | None
    ) -> None:
        """
        Saves or updates a page record.
        kind = "listing" (category page), "fatwa" (individual Q&A page), or "page" (general site page)
        """
        with self.lock:
            self.conn.execute(
                """
                INSERT INTO pages(url, kind, local_path, content_hash, http_status, fetched_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(url) DO UPDATE SET
                    kind         = excluded.kind,
                    local_path   = excluded.local_path,
                    content_hash = excluded.content_hash,
                    http_status  = excluded.http_status,
                    fetched_at   = excluded.fetched_at
                """,
                (url, kind, str(local_path) if local_path else None, content_hash, status, utc_now()),
            )
            self.conn.commit()

    def upsert_asset(self, url: str, local_path: Path, content_type: str | None, content_hash: str) -> None:
        """Saves or updates an asset record (images, CSS, JS files)."""
        with self.lock:
            self.conn.execute(
                """
                INSERT INTO assets(url, local_path, content_type, content_hash, downloaded_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(url) DO UPDATE SET
                    local_path   = excluded.local_path,
                    content_type = excluded.content_type,
                    content_hash = excluded.content_hash,
                    downloaded_at = excluded.downloaded_at
                """,
                (url, str(local_path), content_type, content_hash, utc_now()),
            )
            self.conn.commit()

    def asset_path(self, url: str) -> Path | None:
        """Returns the local file path for a previously-downloaded asset, or None."""
        with self.lock:
            row = self.conn.execute("SELECT local_path FROM assets WHERE url = ?", (url,)).fetchone()
            return Path(row["local_path"]) if row else None

    def upsert_fatwa(self, record: dict) -> None:
        """
        Saves or updates a fatwa record in the database.

        first_seen_at is preserved if the fatwa already exists — we never
        overwrite the date when we first downloaded it.

        ON CONFLICT(url) DO UPDATE = "upsert" — insert if new, update if exists.
        """
        now = utc_now()
        with self.lock:
            # Check if this fatwa already exists to preserve its first_seen_at date
            existing = self.conn.execute(
                "SELECT first_seen_at FROM fatwas WHERE url = ?", (record["url"],)
            ).fetchone()
            first_seen = existing["first_seen_at"] if existing else now

            self.conn.execute(
                """
                INSERT INTO fatwas(
                    url, fatwa_id, category_url, category_name, title, question, answer, answer_id,
                    html_path, json_path, content_hash, first_seen_at, last_seen_at, last_downloaded_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(url) DO UPDATE SET
                    fatwa_id           = excluded.fatwa_id,
                    category_url       = excluded.category_url,
                    category_name      = excluded.category_name,
                    title              = excluded.title,
                    question           = excluded.question,
                    answer             = excluded.answer,
                    answer_id          = excluded.answer_id,
                    html_path          = excluded.html_path,
                    json_path          = excluded.json_path,
                    content_hash       = excluded.content_hash,
                    last_seen_at       = excluded.last_seen_at,
                    last_downloaded_at = excluded.last_downloaded_at
                """,
                (
                    record["url"],
                    record.get("fatwa_id"),
                    record.get("category_url"),
                    record.get("category_name"),
                    record.get("title"),
                    record.get("question"),
                    record.get("answer"),
                    record.get("answer_id"),
                    record.get("html_path"),
                    record.get("json_path"),
                    record.get("content_hash"),
                    first_seen,
                    now,
                    now,
                ),
            )
            self.conn.commit()


# ---------------------------------------------------------------------------
# MAIN CRAWLER CLASS
# ---------------------------------------------------------------------------

class DarulIftaScrapyDownloader:
    """
    The main crawler class. This is the "brain" of the script.

    It uses:
    - Playwright to control a real Chromium browser (bypasses Cloudflare)
    - parsel.Selector to parse HTML (replaces BeautifulSoup4)
    - requests + browser cookies for fast parallel fatwa downloads
    - ThreadPoolExecutor for downloading multiple fatwas at the same time
    - OfflineStore (SQLite) to save all the Q&A content

    PAGINATION FIX:
    The old script only processed pages 1, 2, and 3 of each category.
    This new script properly finds the LAST page number from the pagination
    widget and processes ALL pages (e.g., Zakat & Charity has 25 pages!).
    """

    def __init__(
        self,
        base_url: str,
        paths: CrawlPaths,
        delay: float,
        max_pages_per_category: int | None,
        full_refresh: bool,
        headless: bool,
        executable_path: str | None,
        cdp_url: str | None,
        workers: int,
        crawl_site_pages: bool,
        max_site_pages: int | None,
    ) -> None:
        # normalize_url cleans up the URL (removes trailing slashes, etc.)
        self.base_url = normalize_url(base_url)
        # origin_of extracts just the "https://darulifta-deoband.com" part
        self.base_origin = origin_of(self.base_url)
        self.paths = paths
        # How many seconds to wait between page requests (be polite to the server)
        self.delay = delay
        # Safety limit: stop after N pages per category (None = no limit)
        self.max_pages_per_category = max_pages_per_category
        # If True, re-download even fatwas we already have
        self.full_refresh = full_refresh
        self.headless = headless
        self.executable_path = executable_path
        # CDP URL of the already-running Chromium browser
        self.cdp_url = cdp_url
        # How many parallel threads to use (minimum 1)
        self.workers = max(1, workers)
        self.crawl_site_pages_enabled = crawl_site_pages
        self.max_site_pages = max_site_pages

        # Create all output directories
        self.paths.ensure()

        # Open (or create) the SQLite database
        self.store = OfflineStore(paths.db)

        # Lock to prevent multiple threads writing to files at the same time
        self.io_lock = threading.RLock()

    # -----------------------------------------------------------------------
    # ENTRY POINT
    # -----------------------------------------------------------------------

    def run(self) -> None:
        """
        Main entry point. Connects to the browser, discovers categories,
        then crawls each category page by page.
        """
        # sync_playwright() is a context manager that starts the Playwright engine.
        # Inside this 'with' block, we can control the browser.
        with sync_playwright() as playwright:
            # Connect to the already-running Chromium browser
            context = self.launch_context(playwright)
            try:
                # Get the first open tab, or open a new one
                page = context.pages[0] if context.pages else context.new_page()

                # Make sure Cloudflare verification is already done
                self.ensure_verified(page)

                # Step 1: Find all category URLs (e.g., Zakat & Charity, Salah, etc.)
                self.discover_categories(page)
                categories = self.store.categories()

                if not categories:
                    raise RuntimeError(
                        "No categories were discovered. The website structure may have changed, "
                        "or Cloudflare verification may not be complete."
                    )

                print(f"\nFound {len(categories)} categories. "
                      f"Known fatwas in DB: {self.store.known_fatwa_count()}")

                # Step 2: Crawl each category, downloading ALL pages
                for idx, category in enumerate(categories, start=1):
                    print(f"\n[{idx}/{len(categories)}] Crawling category: {category['name']}")
                    self.crawl_category(page, category["url"], category["name"])

                # Step 3 (optional): Crawl general site pages (About, Contact, etc.)
                if self.crawl_site_pages_enabled:
                    print("\nCrawling general website pages (About, Contact, etc.)")
                    self.crawl_general_site_pages(page)

                # Step 4: Build the offline index HTML page and search_index.json
                self.write_offline_indexes()

            finally:
                # Always close the browser context when done (cleanup)
                context.close()

    # -----------------------------------------------------------------------
    # BROWSER SETUP
    # -----------------------------------------------------------------------

    def launch_context(self, playwright) -> BrowserContext:
        """
        Connects to the running Chromium browser via CDP.

        CDP = Chrome DevTools Protocol — a way for external programs to control
        a Chrome/Chromium browser that was started with --remote-debugging-port=9222.

        This is how we bypass Cloudflare: the USER has already passed the
        verification in that browser, so the cookies are already valid.
        """
        if self.cdp_url:
            print(f"Connecting to browser via CDP: {self.cdp_url}")
            # Connect to the existing browser (already has Cloudflare cookies)
            browser = playwright.chromium.connect_over_cdp(self.cdp_url)
            # Return the first context (browser profile) if one exists
            if browser.contexts:
                return browser.contexts[0]
            # Otherwise create a new context in that browser
            return browser.new_context(
                viewport={"width": 1365, "height": 900},
                user_agent=USER_AGENT,
                locale="en-US",
            )

        # If no CDP URL given, try to launch a new browser.
        # Find the Chromium/Chrome executable.
        launch_options: dict = {
            "user_data_dir": str(self.paths.profile),
            "headless": self.headless,
            "viewport": {"width": 1365, "height": 900},
            "user_agent": USER_AGENT,
            "locale": "en-US",
            "chromium_sandbox": False,
            "args": ["--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage"],
        }
        candidates: list[str | None] = []
        if self.executable_path:
            candidates.append(self.executable_path)
        candidates.append(None)  # None = use Playwright's bundled Chromium
        for binary in ("chromium", "chromium-browser", "google-chrome", "brave-browser"):
            path = shutil.which(binary)
            if path:
                candidates.append(path)

        last_error: Exception | None = None
        for binary_path in candidates:
            try:
                opts = dict(launch_options)
                if binary_path:
                    opts["executable_path"] = binary_path
                return playwright.chromium.launch_persistent_context(**opts)
            except Exception as exc:
                last_error = exc
        raise RuntimeError(f"Could not launch any Chromium browser. Last error: {last_error}")

    def ensure_verified(self, page: Page) -> None:
        """
        Navigates to the website and waits until the real content is visible
        (not a Cloudflare "Checking your browser..." page).

        We detect the real page by looking for the website's navigation menu.
        """
        print(f"Loading: {self.base_url}")
        try:
            page.goto(self.base_url, wait_until="domcontentloaded", timeout=120_000)
        except Exception as exc:
            print(f"Warning: page load raised {exc!r}")

        # Try for up to 30 seconds to see the real navigation (not Cloudflare)
        for attempt in range(30):
            content = page.content()
            # The real site has "darulifta-deoband.com" links and a questions list
            if ("darulifta-deoband.com/home/qa/" in content or
                    "questions_list" in content or
                    "Total Questions" in content):
                print("Real site content detected — Cloudflare verification is complete.")
                return
            if attempt == 0:
                print("Waiting for Cloudflare verification... (please complete it in the browser)")
            time.sleep(1)
        print("Warning: Could not confirm real site content. Proceeding anyway.")

    # -----------------------------------------------------------------------
    # CATEGORY DISCOVERY  (uses parsel instead of BeautifulSoup4)
    # -----------------------------------------------------------------------

    def discover_categories(self, page: Page) -> None:
        """
        Finds all Q&A category links on the homepage.

        The website has a "Categories" mega-menu in the navigation bar.
        We parse it with parsel CSS selectors to find all category URLs.

        PARSEL USAGE EXAMPLE:
          sel.css('a[href*="/home/qa/"]') finds ALL <a> tags whose href
          contains the text "/home/qa/"

          sel.css('a::attr(href)').getall() extracts the href attribute from
          all matching elements and returns them as a Python list.
        """
        print("Discovering categories...")

        # Get the full HTML of the current page as a string
        html_text = page.content()

        # Create a parsel Selector object from the HTML string.
        # This is equivalent to: soup = BeautifulSoup(html_text, "html.parser")
        sel = parsel.Selector(text=html_text)

        # Find all <a> tags whose href contains "/home/qa/"
        # CSS selector breakdown:
        #   a            = find <a> tags
        #   [href*="/home/qa/"]  = where the href attribute CONTAINS "/home/qa/"
        # This matches URLs like: https://darulifta-deoband.com/home/qa/zakat-charity/23
        category_links = sel.css('a[href*="/home/qa/"]')

        # Count how many categories we found before filtering
        found_count = 0

        for link in category_links:
            # Extract the href attribute value
            # ::attr(href) = get the href attribute
            # .get() = return the first match (or None if not found)
            href = link.css("::attr(href)").get("")

            # Extract the link text (what the user sees, e.g. "Zakat & Charity ()")
            # ::text = get text content of this element
            # .get() = return the first match
            text = link.css("::text").get("").strip()

            # Clean the name: remove the count in parentheses like "(495)"
            # e.g., "Zakat & Charity (495)" → "Zakat & Charity"
            name = re.sub(r"\s*\(\d*\)\s*$", "", text).strip()

            # Skip empty names or hrefs
            if not href or not name:
                continue

            # Normalize the URL (remove trailing slashes, fragments)
            full_url = normalize_url(urljoin(self.base_url, href))

            # Only keep URLs that look like category listing pages
            # (not individual fatwa pages)
            if not self.is_potential_listing_url(full_url):
                continue
            if self.looks_like_fatwa_url(full_url):
                continue

            # Save this category to the database
            self.store.upsert_category(full_url, name)
            found_count += 1

        print(f"Discovered {found_count} category links. "
              f"Total unique categories in DB: {len(self.store.categories())}")

    # -----------------------------------------------------------------------
    # CATEGORY CRAWLING  (FULL PAGINATION SUPPORT — the main fix)
    # -----------------------------------------------------------------------

    def crawl_category(self, page: Page, category_url: str, category_name: str) -> None:
        """
        Downloads all fatwa listing pages for ONE category, then downloads
        each individual fatwa.

        THE KEY FIX: Instead of the old approach that only processed pages
        1, 2, and 3, this method:
        1. Loads page 1 of the category
        2. Uses parsel to find the LAST page number from the pagination widget
        3. Builds a list of ALL page URLs (page=1 through page=N)
        4. Processes every single page

        Example: Zakat & Charity has 25 pages. The old script only got
        pages 1-3 (about 60 fatwas). This new script gets all 25 pages
        (about 495 fatwas — as shown by "Total Questions: 495").
        """
        # Track the URL of the most recently seen fatwa (for incremental updates)
        newest_seen: str | None = None

        # ---- STEP 1: Load page 1 to find the total number of pages ----
        print(f"  Loading page 1 to discover total pages: {category_url}")
        try:
            page.goto(category_url, wait_until="domcontentloaded", timeout=120_000)
            self.wait_for_real_page(page)
        except Exception as exc:
            print(f"  ERROR loading category page 1: {exc}")
            return

        # Get the HTML of page 1
        page1_html = page.content()

        # ---- STEP 2: Find the total number of pages ----
        # This uses parsel to look at the pagination widget
        total_pages = self.get_total_pages_parsel(page1_html, category_url)
        print(f"  Category '{category_name}' has {total_pages} page(s) of fatwas.")

        # Apply the optional safety limit (--max-pages-per-category)
        if self.max_pages_per_category:
            total_pages = min(total_pages, self.max_pages_per_category)
            print(f"  Limiting to {total_pages} page(s) due to --max-pages-per-category setting.")

        # ---- STEP 3: Process page 1 (we already loaded it) ----
        # Extract all fatwa links from page 1 using parsel
        fatwa_links_page1 = self.extract_fatwa_links_parsel(page1_html, category_url)
        # Debug :: check how many fatwa_links_page1 has
        ### print(f"[!] ### Debug ###\n Here are all the extract fatwa links for page 1 : \n{fatwa_links_page1}")
        print(f"  Page 1: found {len(fatwa_links_page1)} fatwa links.")

        if fatwa_links_page1:
            # Record the first (most recent) fatwa URL we saw on this category
            if newest_seen is None:
                newest_seen = fatwa_links_page1[0]

        # Filter: skip fatwas we already have (unless --full-refresh)
        to_download = [
            url for url in fatwa_links_page1
            if self.full_refresh or not self.store.has_fatwa(url)
        ]
        skipped = len(fatwa_links_page1) - len(to_download)
        if skipped > 0:
            print(f"  Page 1: skipping {skipped} already-downloaded fatwas.")

        # Download the fatwas from page 1 (parallel if workers > 1)
        if to_download:
            self.download_fatwas_parallel(page, to_download, category_url, category_name)

        # Save page 1 HTML to disk and record it in the database
        page1_hash = sha256_text(page1_html)
        local_page1 = self.save_html_page(category_url, page1_html, "listing")
        self.store.upsert_page(category_url, "listing", local_page1, page1_hash, None)

        # Pause politely before requesting the next page
        polite_pause(self.delay)

        # ---- STEP 4: Process pages 2 through N ----
        for page_num in range(2, total_pages + 1):
            # Build the URL for this page: e.g., ?page=2, ?page=3, etc.
            page_url = build_page_url(category_url, page_num)
            print(f"  Page {page_num}/{total_pages}: {page_url}")

            # Load the page in the real Chromium browser
            try:
                page.goto(page_url, wait_until="domcontentloaded", timeout=120_000)
                self.wait_for_real_page(page)
            except Exception as exc:
                print(f"  ERROR loading page {page_num}: {exc}")
                # Continue to the next page even if this one fails
                continue

            # Get the HTML for this page
            page_html = page.content()

            # Extract fatwa links using parsel
            fatwa_links = self.extract_fatwa_links_parsel(page_html, page_url)
            print(f"  Page {page_num}: found {len(fatwa_links)} fatwa links.")
            print(f"[!] ### Debug ###\n Here are all the extract fatwa links for page 1 : \n{fatwa_links}")
            if not fatwa_links:
                # No fatwas found on this page — might be past the last page
                print(f"  No fatwas on page {page_num}, stopping early.")
                break

            # Filter: skip already-downloaded fatwas
            to_download = [
                url for url in fatwa_links
                if self.full_refresh or not self.store.has_fatwa(url)
            ]
            skipped = len(fatwa_links) - len(to_download)
            if skipped > 0:
                print(f"  Page {page_num}: skipping {skipped} already-downloaded fatwas.")

            # Download fatwas from this page
            if to_download:
                self.download_fatwas_parallel(page, to_download, category_url, category_name)

            # Save this listing page HTML to disk
            page_hash = sha256_text(page_html)
            local_listing = self.save_html_page(page_url, page_html, "listing")
            self.store.upsert_page(page_url, "listing", local_listing, page_hash, None)

            # Be polite — wait before loading the next page
            polite_pause(self.delay)

        # Mark this category as fully crawled in the database
        self.store.mark_category_crawled(category_url, newest_seen)
        print(f"  Finished category: {category_name}")

    # -----------------------------------------------------------------------
    # PAGINATION DETECTION  (the core new function using parsel)
    # -----------------------------------------------------------------------

    def get_total_pages_parsel(self, html_text: str, category_url: str) -> int:
        """
        Finds the total number of pages for a category by parsing the
        pagination widget in the HTML using parsel CSS selectors.

        The pagination HTML on darulifta-deoband.com looks like this:
        ---------------------------------------------------------------
        <nav aria-label="Page navigation">
          <ul class="pagination">
            <li class="active">
              <a href="https://darulifta-deoband.com/home/qa/zakat-charity/23?page=1">1</a>
            </li>
            <li>
              <a href="...?page=2">2</a>
            </li>
            <li>
              <a href="...?page=3">3</a>
            </li>
            <li>
              <a href="...?page=4" aria-label="Next">
                <span aria-hidden="true">Next</span>
              </a>
            </li>
            <li>
              <a href="...?page=25" aria-label="Last">
                <span aria-hidden="true">Last</span>
              </a>
            </li>
          </ul>
        </nav>
        ---------------------------------------------------------------

        STRATEGY:
        1. First, look for the <a aria-label="Last"> link — its ?page=N value
           tells us directly how many pages there are.
        2. If that fails, find the highest ?page=N value among all pagination links.
        3. If no pagination found, assume 1 page.

        WHY THIS IS BETTER THAN THE OLD APPROACH:
        The old script tried to find a "Next" link by looking at link text,
        which was unreliable. It then hardcoded "try pages 2 and 3".
        This new approach extracts the actual last page number from the
        "Last" button, which the website shows explicitly.
        """
        # Create a parsel Selector from the HTML string
        sel = parsel.Selector(text=html_text)

        # ---- Strategy 1: Find the "Last" pagination button ----
        # CSS selector: ul.pagination a[aria-label="Last"]
        # Breakdown:
        #   ul.pagination  = <ul> element with class="pagination"
        #   a[aria-label="Last"]  = <a> element where aria-label attribute equals "Last"
        #   ::attr(href)   = extract the href attribute value
        # .get() returns the first match, or None if not found
        last_href = sel.css('ul.pagination a[aria-label="Last"]::attr(href)').get()

        if last_href:
            # Extract the page number from the URL using regex
            # Pattern: [?&]page=  matches either ?page= or &page=
            # (\d+) captures one or more digits as a group
            match = re.search(r'[?&]page=(\d+)', last_href)
            if match:
                total = int(match.group(1))  # group(1) is the first captured group
                print(f"  Found 'Last' pagination link → total pages: {total}")
                return total

        # ---- Strategy 2: Find highest page number in all pagination links ----
        # Get ALL href values from links inside ul.pagination
        # .getall() returns a Python list of all matches
        all_pagination_hrefs = sel.css('ul.pagination a::attr(href)').getall()

        page_numbers: list[int] = []
        for href in all_pagination_hrefs:
            match = re.search(r'[?&]page=(\d+)', href)
            if match:
                page_numbers.append(int(match.group(1)))

        if page_numbers:
            total = max(page_numbers)
            print(f"  Found {len(page_numbers)} pagination numbers, max = {total}")
            return total

        # ---- Strategy 3: Check if there's any pagination at all ----
        # If no pagination widget is found, there's only 1 page
        print("  No pagination found — category has only 1 page.")
        return 1

    # -----------------------------------------------------------------------
    # FATWA LINK EXTRACTION  (using parsel instead of BeautifulSoup4)
    # -----------------------------------------------------------------------

    def extract_fatwa_links_parsel(self, html_text: str, base_url: str) -> list[str]:
        """
        Finds all fatwa links on a category listing page using parsel.

        The fatwa listing HTML looks like this:
        -------------------------------------------------
        <ul class="questions_list">
          <li>
            <a href="https://darulifta-deoband.com/home/en/zakat-charity/183134">
              <span>Q.</span> Am I eligible to take zakat money?
            </a>
          </li>
          <li>
            <a href="https://darulifta-deoband.com/home/en/zakat-charity/171066">
              <span>Q.</span> Zakath on House
            </a>
          </li>
          ...
        </ul>
        -------------------------------------------------

        PARSEL CSS SELECTORS USED:
          ul.questions_list         = <ul> with class "questions_list"
          ul.questions_list li a    = <a> inside <li> inside that <ul>
          ::attr(href)              = get the href attribute
          .getall()                 = return ALL matches as a list
        """
        sel = parsel.Selector(text=html_text)

        # Extract all href values from <a> tags inside ul.questions_list
        raw_hrefs = sel.css('ul.questions_list li a::attr(href)').getall()
        print(f"[!] ### Debug ###\n Here are all the extracted RAW fatwa links for page : \n{raw_hrefs}")
        fatwa_urls: list[str] = []
        seen: set[str] = set()  # Track already-seen URLs to avoid duplicates

        for href in raw_hrefs:
            if not href:
                continue
            # Resolve relative URLs: urljoin("https://example.com/home", "/page") = "https://example.com/page"
            full_url = normalize_url(urljoin(base_url, href))
            # Only keep URLs that look like individual fatwa pages
            if self.looks_like_fatwa_url(full_url) and full_url not in seen:
                fatwa_urls.append(full_url)
                seen.add(full_url)
        #print(f"[!] ### Debug ###\n Here are all the extracted FATWA_URLs links for page : \n{fatwa_urls}")
        return raw_hrefs

    # -----------------------------------------------------------------------
    # PARALLEL FATWA DOWNLOADING
    # -----------------------------------------------------------------------

    def get_cookies(self, page: Page) -> list[dict]:
        """
        Extracts all cookies from the browser session.
        These cookies include the Cloudflare clearance token that allows
        us to make HTTP requests without being blocked.
        """
        return page.context.cookies()

    def download_fatwas_parallel(
        self,
        page: Page,
        fatwa_urls: list[str],
        category_url: str,
        category_name: str,
    ) -> None:
        """
        Downloads multiple fatwa pages at the same time using ThreadPoolExecutor.

        WHAT IS THREADING?
        Instead of downloading fatwas one by one (slow), we download several
        at the same time. ThreadPoolExecutor manages a pool of worker threads —
        each thread downloads one fatwa independently.

        FALLBACK:
        If parallel downloading fails (e.g., Cloudflare 403 Forbidden),
        we automatically fall back to using the real browser (sequentially).
        """
        if not fatwa_urls:
            return

        # Get browser cookies to pass to the requests-based parallel downloader
        cookies = self.get_cookies(page)
        session = self.make_requests_session(cookies)

        total = len(fatwa_urls)
        successful = 0
        failed_urls: list[str] = []

        if self.workers == 1:
            # Sequential mode: download one fatwa at a time
            print(f"  Downloading {total} fatwas sequentially...")
            for url in fatwa_urls:
                try:
                    self.download_single_fatwa(url, category_url, category_name, session)
                    successful += 1
                except Exception as exc:
                    print(f"    FAILED: {url} — {exc}")
                    failed_urls.append(url)
                polite_pause(self.delay * 0.3)  # Short pause between fatwas
        else:
            # Parallel mode: download multiple fatwas at the same time
            print(f"  Downloading {total} fatwas with {self.workers} parallel workers...")

            # ThreadPoolExecutor creates a pool of worker threads
            # max_workers=self.workers means N threads run at the same time
            try:
                with ThreadPoolExecutor(max_workers=self.workers) as executor:
                    # Submit each fatwa URL as a separate download task
                    future_to_url = {
                        executor.submit(
                            self.download_single_fatwa,
                            url, category_url, category_name, session
                        ): url
                        for url in fatwa_urls
                    }

                    # Process results as each download completes
                    for future in as_completed(future_to_url):
                        url = future_to_url[future]
                        try:
                            future.result()  # Raises exception if download failed
                            successful += 1
                        except Exception as exc:
                            print(f"    FAILED: {url} — {exc}")
                            failed_urls.append(url)

            except Exception as exc:
                # If parallel mode completely fails, fall back to sequential
                print(f"  Parallel mode failed ({exc}).")

        # ---- STEP 5: Browser Fallback for Failed Downloads ----
        if failed_urls:
            print(f"  Attempting browser fallback for {len(failed_urls)} failed download(s)...")
            for url in failed_urls:
                self.download_fatwa_browser(page, url, category_url, category_name)
                successful += 1
                polite_pause(self.delay)

        print(f"  Done: {successful} fatwas processed (out of {total}).")

    def download_fatwa_browser(
        self,
        page: Page,
        url: str,
        category_url: str,
        category_name: str
    ) -> None:
        """
        Downloads ONE fatwa page using the real browser (fallback).
        """
        print(f"    Retrying in browser: {url}")
        try:
            page.goto(url, wait_until="domcontentloaded", timeout=60_000)
            self.wait_for_real_page(page)
            html_text = page.content()
            self.process_fatwa_content(url, html_text, category_url, category_name, 200)
        except Exception as exc:
            print(f"      BROWSER FALLBACK FAILED: {url} — {exc}")

    def download_single_fatwa(
        self,
        url: str,
        category_url: str,
        category_name: str,
        session: requests.Session,
    ) -> None:
        """
        Downloads ONE fatwa page using requests (fast mode).
        """
        # HTTP GET request to download the fatwa page
        try:
            resp = session.get(url, timeout=45, allow_redirects=True)
            resp.raise_for_status()  # Raise exception for 4xx/5xx errors
            self.process_fatwa_content(url, resp.text, category_url, category_name, resp.status_code)
        except Exception as exc:
            raise RuntimeError(f"HTTP request failed: {exc}") from exc

    def process_fatwa_content(
        self,
        url: str,
        html_text: str,
        category_url: str,
        category_name: str,
        status_code: int | None = None
    ) -> None:
        """
        Shared logic to parse HTML content, save files, and update the database.
        """
        content_hash = sha256_text(html_text)

        # Parse the fatwa HTML using parsel (replaces BeautifulSoup4)
        extracted = extract_fatwa_content_parsel(html_text, url)

        # Save the raw HTML file to disk
        with self.io_lock:
            local_html = self.save_html_page(url, html_text, "fatwa")

        # Save a JSON record with all the extracted data
        with self.io_lock:
            local_json = self.save_json_record(
                url, category_url, category_name, extracted, content_hash
            )

        # Build the complete database record dictionary
        record: dict = {
            "url": url,
            "fatwa_id": extracted.get("fatwa_id"),
            "category_url": category_url,
            "category_name": category_name,
            "title": extracted.get("title"),
            "question": extracted.get("question"),
            "answer": extracted.get("answer"),
            "answer_id": extracted.get("answer_id"),
            "html_path": str(local_html),
            "json_path": str(local_json),
            "content_hash": content_hash,
        }

        # Save to SQLite database
        self.store.upsert_fatwa(record)
        # Track the page in the pages table too
        self.store.upsert_page(url, "fatwa", local_html, content_hash, status_code)


    # -----------------------------------------------------------------------
    # GENERAL SITE PAGE CRAWLING (About, Contact, etc.)
    # -----------------------------------------------------------------------

    def crawl_general_site_pages(self, page: Page) -> None:
        """
        Crawls non-category pages like About, Contact, Terms of Use.
        These are not fatwas but we save them for complete offline access.
        Uses parsel to extract links for breadth-first crawling.
        """
        queue: list[str] = [self.base_url]
        seen: set[str] = set()
        saved = 0

        while queue:
            if self.max_site_pages is not None and saved >= self.max_site_pages:
                print(f"  Reached --max-site-pages limit ({self.max_site_pages}).")
                break

            current_url = queue.pop(0)
            if current_url in seen:
                continue
            seen.add(current_url)

            if not self.is_general_page_url(current_url):
                continue
            if not self.full_refresh and self.store.has_page(current_url):
                continue

            print(f"  General page: {current_url}")
            try:
                page.goto(current_url, wait_until="domcontentloaded", timeout=60_000)
                self.wait_for_real_page(page)
            except Exception as exc:
                print(f"  Could not load page: {exc}")
                continue

            html_text = page.content()
            content_hash = sha256_text(html_text)
            local_path = self.save_html_page(current_url, html_text, "page")
            self.store.upsert_page(current_url, "page", local_path, content_hash, None)
            saved += 1

            # Use parsel to find more links on this page to crawl
            sel = parsel.Selector(text=html_text)
            for href in sel.css('a::attr(href)').getall():
                if not href:
                    continue
                full_url = normalize_url(urljoin(current_url, href))
                if (full_url not in seen
                        and self.is_allowed_url(full_url)
                        and not self.looks_like_fatwa_url(full_url)
                        and not self.is_potential_listing_url(full_url)):
                    queue.append(full_url)

            polite_pause(self.delay)

    # -----------------------------------------------------------------------
    # UTILITY METHODS
    # -----------------------------------------------------------------------

    def wait_for_real_page(self, page: Page) -> None:
        """
        After navigating to a URL, wait until the real site content is visible.
        Cloudflare may briefly show a "checking browser" page before the real
        content appears. This method polls until we see real content.
        """
        for _ in range(15):
            content = page.content()
            if ("darulifta-deoband.com/home/" in content or
                    "questions_list" in content or
                    "Total Questions" in content or
                    "fatwa_id" in content or
                    "Question" in content):
                return
            time.sleep(0.5)

    def make_requests_session(self, cookies: list[dict]) -> requests.Session:
        """
        Creates a requests.Session with:
        1. A realistic browser User-Agent header
        2. The browser's Cloudflare cookies

        The session is reused for all parallel downloads so TCP connections
        are reused (faster than creating a new connection for each fatwa).
        """
        session = requests.Session()
        session.headers.update({
            "User-Agent": USER_AGENT,
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.5",
        })
        # Copy each cookie from the browser into the requests session
        for cookie in cookies:
            domain = cookie.get("domain", "")
            name = cookie.get("name", "")
            value = cookie.get("value")
            if not domain or not name or value is None:
                continue
            # domain.lstrip(".") removes the leading dot (browsers use ".example.com"
            # to mean "all subdomains", but requests expects "example.com")
            session.cookies.set(
                name, value,
                domain=domain.lstrip("."),
                path=cookie.get("path") or "/",
            )
        return session

    def save_html_page(self, url: str, html_text: str, kind: str) -> Path:
        """
        Saves an HTML page to a local file.
        The filename is derived from the URL (with special characters replaced)
        plus a short hash to make it unique.
        """
        filename = f"{safe_id_for_url(url)}.{kind}.html"
        local_path = self.paths.pages / filename
        with self.io_lock:
            local_path.write_text(html_text, encoding="utf-8", errors="replace")
        return local_path

    def save_json_record(
        self, url: str, category_url: str, category_name: str,
        extracted: dict, content_hash: str
    ) -> Path:
        """
        Saves a fatwa's extracted data as a JSON file.
        Each fatwa gets its own .json file in the data/ directory.
        """
        local_path = self.paths.data / f"{safe_id_for_url(url)}.json"
        payload = {
            **extracted,
            "url": url,
            "category_url": category_url,
            "category_name": category_name,
            "content_hash": content_hash,
            "downloaded_at": utc_now(),
        }
        with self.io_lock:
            local_path.write_text(
                json.dumps(payload, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )
        return local_path

    def write_offline_indexes(self) -> None:
        """
        Creates two files for offline browsing:
        1. site/index.html — a searchable HTML page listing all fatwas
        2. site/search_index.json — a JSON file for programmatic search
           (used by the Islamic App's RAG pipeline)
        """
        print("\nBuilding offline index pages...")
        categories = self.store.conn.execute(
            "SELECT url, name FROM categories ORDER BY name COLLATE NOCASE"
        ).fetchall()
        fatwas = self.store.conn.execute(
            "SELECT url, fatwa_id, category_name, title, question, answer, html_path "
            "FROM fatwas ORDER BY CAST(fatwa_id AS INTEGER) DESC, fatwa_id DESC"
        ).fetchall()
        pages = self.store.conn.execute(
            "SELECT url, local_path FROM pages WHERE kind = 'page' ORDER BY url"
        ).fetchall()

        search_rows: list[dict] = []
        category_items: list[str] = []
        page_items: list[str] = []
        fatwa_items: list[str] = []

        for cat in categories:
            safe_href = f"pages/{safe_id_for_url(cat['url'])}.listing.html"
            category_items.append(
                f'<li><a href="{safe_href}">{html.escape(cat["name"])}</a></li>'
            )

        for page_row in pages:
            html_name = (Path(page_row["local_path"]).name
                         if page_row["local_path"] else safe_id_for_url(page_row["url"]))
            page_items.append(
                f'<li><a href="pages/{html.escape(html_name)}">'
                f'{html.escape(page_row["url"])}</a></li>'
            )

        for row in fatwas:
            html_name = (Path(row["html_path"]).name
                         if row["html_path"] else f"{safe_id_for_url(row['url'])}.fatwa.html")
            title = row["title"] or row["question"] or row["fatwa_id"] or row["url"]
            fatwa_items.append(
                f'<li>'
                f'<a href="pages/{html.escape(html_name)}">{html.escape(str(title))}</a>'
                f' <span>{html.escape(str(row["category_name"] or ""))}</span>'
                f'</li>'
            )
            search_rows.append({
                "url": row["url"],
                "fatwa_id": row["fatwa_id"],
                "category_name": row["category_name"],
                "title": row["title"],
                "question": row["question"],
                "answer": row["answer"],
                "html_path": f"pages/{html_name}",
            })

        # Write the offline index HTML with a JavaScript search filter
        index_html = f"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Darul Ifta Deoband Offline Archive — {len(fatwas)} Fatwas</title>
  <style>
    body {{ font-family: system-ui, sans-serif; margin: 2rem auto; max-width: 980px; line-height: 1.5; }}
    h1 {{ margin-bottom: .25rem; color: #1a5276; }}
    p.subtitle {{ color: #555; margin-top: 0; }}
    input {{ box-sizing: border-box; font: inherit; margin: 1rem 0; padding: .7rem;
             width: 100%; border: 1px solid #ccc; border-radius: 4px; }}
    li {{ margin: .45rem 0; }}
    span {{ color: #666; font-size: .9em; }}
    a {{ color: #1a5276; }}
  </style>
</head>
<body>
  <h1>Darul Ifta Deoband — Offline Archive</h1>
  <p class="subtitle">{len(fatwas)} fatwas downloaded. Built by Rushd-ul-Ilm Islamic App project.</p>
  <input id="q" type="search" placeholder="Type to filter fatwas by title or content...">
  <h2>Categories ({len(category_items)})</h2>
  <ul>{''.join(category_items)}</ul>
  <h2>Website Pages</h2>
  <ul>{''.join(page_items) or '<li>None</li>'}</ul>
  <h2>All Fatwas ({len(fatwas)})</h2>
  <ul id="fatwas">{''.join(fatwa_items)}</ul>
  <script>
    const q = document.getElementById('q');
    const items = [...document.querySelectorAll('#fatwas li')];
    q.addEventListener('input', () => {{
      const needle = q.value.toLowerCase();
      for (const item of items)
        item.hidden = needle && !item.textContent.toLowerCase().includes(needle);
    }});
  </script>
</body>
</html>"""
        (self.paths.site / "index.html").write_text(index_html, encoding="utf-8")
        (self.paths.site / "search_index.json").write_text(
            json.dumps(search_rows, ensure_ascii=False, indent=2), encoding="utf-8"
        )
        print(f"Offline index written: {self.paths.site / 'index.html'}")
        print(f"Search index written: {self.paths.site / 'search_index.json'}")

    # -----------------------------------------------------------------------
    # URL CLASSIFICATION HELPERS
    # -----------------------------------------------------------------------

    def is_allowed_url(self, url: str) -> bool:
        """Returns True if the URL is a valid https URL on the same website."""
        parsed = urlparse(url)
        if parsed.scheme not in {"http", "https"}:
            return False
        if origin_of(url) != self.base_origin:
            return False
        path = parsed.path.lower()
        blocked_prefixes = ("/cdn-cgi/", "/wp-login", "/login", "/admin")
        return not any(path.startswith(p) for p in blocked_prefixes)

    def looks_like_fatwa_url(self, url: str) -> bool:
        """
        Returns True if the URL is an individual fatwa page.
        Examples:
          https://darulifta-deoband.com/home/en/zakat-charity/183134  → True
          https://darulifta-deoband.com/home/qa/zakat-charity/23      → False (category)
          https://darulifta-deoband.com/home/qa/zakat-charity/23?page=2 → False (listing page)
        """
        if not self.is_allowed_url(url):
            return False
        parsed = urlparse(url)
        # URLs with ?page= are listing pages, not fatwa pages
        if query_page_number(parsed.query) is not None:
            return False
        path = parsed.path.rstrip("/").lower()
        if path.endswith("/en"):
            return False
        # Pattern: /home/en/{category-slug}/{numeric-id}
        if re.search(r"/home/en/[^/]*\d+/?$", path):
            return True
        if re.search(r"/en/[^/]*\d{3,}/?$", path):
            return True
        if re.search(r"/(?:fatwa|question|answer|qa)/[^/]*\d+/?$", path):
            return True
        return bool(re.search(r"/\d{4,}/?$", path))

    def is_potential_listing_url(self, url: str) -> bool:
        """Returns True if the URL looks like a category listing page."""
        if not self.is_allowed_url(url):
            return False
        path = urlparse(url).path.lower()
        return any(token in path for token in (
            "/home/qa/", "/qa/", "/category", "/categories", "/masail", "/questions",
        ))

    def is_general_page_url(self, url: str) -> bool:
        """Returns True if the URL is a general website page (not a fatwa or category)."""
        if not self.is_allowed_url(url):
            return False
        if self.looks_like_fatwa_url(url) or self.is_potential_listing_url(url):
            return normalize_url(url) == self.base_url
        path = urlparse(url).path.lower()
        if path.endswith((".jpg", ".jpeg", ".png", ".gif", ".webp",
                          ".svg", ".css", ".js", ".pdf", ".zip")):
            return False
        return True


# ---------------------------------------------------------------------------
# FATWA CONTENT EXTRACTION  (using parsel instead of BeautifulSoup4)
# ---------------------------------------------------------------------------

def extract_fatwa_content_parsel(html_text: str, url: str) -> dict:
    """
    Parses a fatwa detail page HTML and extracts:
    - fatwa_id: the numeric ID from the URL (e.g., "183134")
    - answer_id: the answer reference number shown on the page
    - title: the fatwa title (what the question is about)
    - question: the full question text
    - answer: the full answer text from the Darul Ifta scholars

    PARSEL REPLACEMENT FOR BEAUTIFULSOUP4:
    Old (BeautifulSoup4):
      soup = BeautifulSoup(html_text, "html.parser")
      soup.find("h1").get_text()

    New (parsel):
      sel = parsel.Selector(text=html_text)
      sel.css("h1::text").get()

    WHY PARSEL IS BETTER HERE:
    - CSS selectors are more concise than BeautifulSoup's find() calls
    - parsel handles malformed HTML gracefully (uses lxml internally)
    - Same engine used by Scrapy's production spiders
    - Supports XPath too (more powerful for complex extractions)
    """
    # Create a parsel Selector from the HTML string
    sel = parsel.Selector(text=html_text)

    # ---- Extract fatwa_id from the URL ----
    # e.g., from ".../home/en/zakat-charity/183134" → "183134"
    fatwa_id = extract_fatwa_id_from_url(url)

    # ---- Extract answer_id ----
    # Look for text like "Answer ID: 183134-67" on the page
    answer_id = extract_answer_id_parsel(sel)

    # ---- Extract title ----
    title = extract_title_parsel(sel)

    # ---- Extract question text ----
    question = extract_section_parsel(sel, "question")

    # ---- Extract answer text ----
    answer = extract_section_parsel(sel, "answer")

    # If structured extraction failed, fall back to full-page text extraction
    if not question or not answer:
        # Get ALL text from the page (joining all text nodes with newlines)
        # This extracts text from every element: headings, paragraphs, list items, etc.
        # ::text = text nodes directly inside any element
        # //* = any element in XPath
        all_text = "\n".join(sel.xpath("//text()").getall())
        all_text = clean_text(all_text)

        if not question:
            question = extract_section_by_text(all_text, "Question", ("Answer", "Answer ID"))
        if not answer:
            answer = extract_section_by_text(all_text, "Answer", ("Darul Ifta", "Answered by", "Checked by"))

    return {
        "fatwa_id": fatwa_id,
        "answer_id": answer_id,
        "title": title,
        "question": question,
        "answer": answer,
    }


def extract_title_parsel(sel: "parsel.Selector") -> str | None:
    """
    Extracts the fatwa title from the page.
    Tries multiple strategies in order of reliability.
    """
    # Strategy 1: Look for a visible "Title:" label on the page
    # XPath: find an element whose text contains "Title" (case-insensitive)
    # then get the text of the FOLLOWING sibling or parent element
    for label_text in ("Title", "Subject", "Topic"):
        # Find all elements that contain this label text
        elements = sel.xpath(
            f'//*[contains(translate(normalize-space(text()), "ABCDEFGHIJKLMNOPQRSTUVWXYZ", '
            f'"abcdefghijklmnopqrstuvwxyz"), "{label_text.lower()}")]'
            f'/following-sibling::*[1]//text()'
        ).getall()
        if elements:
            result = clean_text(" ".join(elements))
            if result and len(result) > 3:
                return result

    # Strategy 2: Look in h1, h2, h3 headings
    for heading_tag in ("h1", "h2", "h3"):
        text = sel.css(f"{heading_tag}::text").get()
        if text:
            result = clean_text(text)
            # Filter out generic headings like "Darul Ifta"
            if result and len(result) > 5 and "darul" not in result.lower():
                return result

    # Strategy 3: Use the page <title> tag
    title_text = sel.css("title::text").get()
    if title_text:
        result = clean_text(title_text)
        # Remove the site name suffix (e.g., " - Darul Ifta Deoband")
        result = re.sub(r"\s*[-|—]\s*Darul.*$", "", result, flags=re.IGNORECASE).strip()
        if result:
            return result

    return None


def extract_answer_id_parsel(sel: "parsel.Selector") -> str | None:
    """
    Extracts the Answer ID reference number from the page.
    Looks for patterns like "Answer ID: 183134-67" in the page text.
    """
    # Get all text from the page
    full_text = " ".join(sel.xpath("//text()").getall())

    # Look for "Answer ID" followed by a number pattern
    # \s* = optional whitespace
    # [:\-]? = optional colon or dash
    # ([\d\-]+) = captures digits and dashes (like "183134-67")
    match = re.search(r"Answer\s*ID\s*[:\-]?\s*([\d\-]+)", full_text, re.IGNORECASE)
    if match:
        return match.group(1).strip()

    return None


def extract_section_parsel(sel: "parsel.Selector", section_type: str) -> str | None:
    """
    Extracts either the 'question' section or the 'answer' section from a
    fatwa page using parsel CSS/XPath selectors.

    The website's fatwa detail pages have sections labeled:
    - "Question" containing the user's question
    - "Answer" containing the scholar's response

    This function tries multiple CSS selectors to find these sections.
    """
    # Common CSS class names used on the website for Q&A content
    if section_type == "question":
        selectors = [
            ".question",
            ".question-text",
            "#question",
            "[class*='question']",
        ]
    else:  # answer
        selectors = [
            ".answer",
            ".answer-text",
            "#answer",
            "[class*='answer']",
        ]

    # Try each CSS selector
    for css_sel in selectors:
        # ::text gets direct text nodes
        # We use .getall() to get ALL text nodes (not just the first)
        texts = sel.css(f"{css_sel} ::text").getall()
        if texts:
            result = clean_text(" ".join(texts))
            if result and len(result) > 20:  # Must have meaningful content
                return result

    # Fallback: look for elements containing the section label text
    # XPath breakdown:
    #   //*  = any element
    #   [contains(normalize-space(.), "Question")]  = whose text contains "Question"
    label = "Question" if section_type == "question" else "Answer"
    label_elements = sel.xpath(
        f'//*[normalize-space(.)="{label}" or normalize-space(.)="{label}:"]'
    )

    for label_el in label_elements:
        # Try to get the text from the NEXT sibling element after the label
        next_texts = label_el.xpath("following-sibling::*[1]//text()").getall()
        if next_texts:
            result = clean_text(" ".join(next_texts))
            if result and len(result) > 20:
                return result

        # Try to get the text from the PARENT element
        parent_texts = label_el.xpath("parent::*/text()").getall()
        if parent_texts:
            result = clean_text(" ".join(parent_texts))
            if result and len(result) > 20:
                return result

    return None


def extract_section_by_text(
    full_text: str, start_label: str, stop_labels: tuple[str, ...]
) -> str | None:
    """
    Extracts a section from plain text by finding its start label and
    stopping at the next section's label.

    For example, to get the "Question" section:
    - Start after the line containing "Question"
    - Stop when we see "Answer" or "Answer ID"

    This is the same approach as the old script's extract_section_by_text()
    but takes plain text (from parsel's text extraction) instead of a
    BeautifulSoup object.
    """
    # Split the text into lines for easier processing
    lines = full_text.splitlines()

    # Find the line index where the section starts
    start_idx: int | None = None
    for i, line in enumerate(lines):
        if start_label.lower() in line.lower().strip():
            start_idx = i + 1  # Start from the line AFTER the label
            break

    if start_idx is None:
        return None  # Label not found

    # Collect lines until we hit a stop label
    section_lines: list[str] = []
    for line in lines[start_idx:]:
        # Check if any stop label appears in this line
        if any(stop.lower() in line.lower() for stop in stop_labels):
            break
        section_lines.append(line)

    result = clean_text("\n".join(section_lines))
    return result if result and len(result) > 10 else None


# ---------------------------------------------------------------------------
# STANDALONE HELPER FUNCTIONS
# ---------------------------------------------------------------------------

def build_page_url(category_url: str, page_num: int) -> str:
    """
    Builds a paginated URL for a category page.

    For example:
      build_page_url("https://darulifta-deoband.com/home/qa/zakat-charity/23", 2)
      → "https://darulifta-deoband.com/home/qa/zakat-charity/23?page=2"

      build_page_url("https://darulifta-deoband.com/home/qa/zakat-charity/23?page=1", 3)
      → "https://darulifta-deoband.com/home/qa/zakat-charity/23?page=3"

    The function removes any existing ?page= parameter before adding the new one,
    so we don't get URLs like ?page=1&page=2.
    """
    # Parse the URL into its component parts
    parsed = urlparse(category_url)

    # Parse the existing query string into a dictionary
    # e.g., "page=1&lang=en" → {"page": ["1"], "lang": ["en"]}
    params = parse_qs(parsed.query)

    # Set the page number (overwrite any existing page param)
    # We set it as a list because parse_qs returns lists
    params["page"] = [str(page_num)]

    # Rebuild the query string from the dictionary
    # doseq=True handles list values correctly
    new_query = urlencode(params, doseq=True)

    # Reassemble the URL with the new query string
    new_parsed = parsed._replace(query=new_query)
    return urlunparse(new_parsed)


def query_page_number(query: str) -> int | None:
    """
    Extracts the 'page' parameter value from a URL query string.
    Returns None if there is no 'page' parameter.

    Example: query_page_number("page=3&lang=en") → 3
    """
    params = parse_qs(query)
    if "page" in params:
        try:
            return int(params["page"][0])
        except (ValueError, IndexError):
            return None
    return None


def extract_fatwa_id_from_url(url: str) -> str | None:
    """
    Extracts the numeric fatwa ID from a fatwa URL.

    Examples:
      .../home/en/zakat-charity/183134 → "183134"
      .../home/en/salah-prayer/196968  → "196968"
    """
    # Look for a number at the end of the URL path
    match = re.search(r"/(\d+)/?$", urlparse(url).path)
    if match:
        return match.group(1)
    return None


def normalize_url(url: str) -> str:
    """
    Cleans up a URL by:
    1. Removing the #fragment part (e.g., #section-1)
    2. Ensuring there's always a path (at least "/")
    3. Removing trailing slashes (except for root "/")
    """
    if not url:
        return ""
    # urldefrag splits a URL into (url_without_fragment, fragment)
    clean, _fragment = urldefrag(url.strip())
    parsed = urlparse(clean)
    if parsed.scheme in {"http", "https"}:
        path = parsed.path or "/"
        clean = parsed._replace(path=path).geturl()
    # Remove trailing slash unless it's the root path
    return clean.rstrip("/") if clean.endswith("/") and urlparse(clean).path != "/" else clean


def origin_of(url: str) -> str:
    """
    Extracts the origin (scheme + host) of a URL.
    Example: "https://darulifta-deoband.com/home/en/..." → "https://darulifta-deoband.com"
    """
    parsed = urlparse(url)
    return f"{parsed.scheme}://{parsed.netloc}".lower()


def sha256_text(value: str) -> str:
    """Creates a SHA-256 hash fingerprint of a text string."""
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def sha256_bytes(value: bytes) -> str:
    """Creates a SHA-256 hash fingerprint of bytes."""
    return hashlib.sha256(value).hexdigest()


def safe_id_for_url(url: str) -> str:
    """
    Converts a URL into a safe filename.
    Replaces special characters with underscores and appends a short hash.

    Example:
      "https://darulifta-deoband.com/home/en/zakat-charity/183134"
      → "home_en_zakat-charity_183134_a3f7b2c1d4.fatwa.html"
    """
    parsed = urlparse(url)
    path = parsed.path.strip("/").replace("/", "_")
    path = re.sub(r"[^A-Za-z0-9_.-]+", "_", path)
    if not path:
        path = "index"
    # Add a short hash to ensure uniqueness (handles URLs with same path but different query)
    short_hash = hashlib.sha256(url.encode("utf-8")).hexdigest()[:10]
    return f"{path}_{short_hash}"


def clean_text(text: str | None) -> str:
    """
    Cleans extracted text by:
    1. Stripping leading/trailing whitespace
    2. Collapsing multiple whitespace characters into a single space
    3. Removing non-breaking spaces (&nbsp; = \xa0)
    4. Removing zero-width characters
    """
    if not text:
        return ""
    # Replace various whitespace characters with a regular space
    text = re.sub(r"[\r\n\t\xa0\u200b\u200c\u200d\ufeff]+", " ", text)
    # Collapse multiple spaces into one
    text = re.sub(r" {2,}", " ", text)
    return text.strip()


def utc_now() -> str:
    """Returns the current UTC time as an ISO-8601 string."""
    return datetime.now(timezone.utc).isoformat()


def polite_pause(delay: float) -> None:
    """
    Pauses for a specified number of seconds between HTTP requests.
    This is important to avoid overwhelming the server with too many
    requests per second (which could get our IP blocked).
    """
    if delay > 0:
        time.sleep(delay)


# ---------------------------------------------------------------------------
# COMMAND-LINE ARGUMENT PARSING
# ---------------------------------------------------------------------------

def parse_args(argv: list[str]) -> argparse.Namespace:
    """
    Parses command-line arguments.
    This function reads the arguments you type after the script name
    when running it from the terminal.
    """
    parser = argparse.ArgumentParser(
        description=(
            "Darul Ifta Deoband offline downloader — parsel/Scrapy edition.\n"
            "Replaces BeautifulSoup4 with parsel and adds full pagination support.\n\n"
            "USAGE:\n"
            "  Terminal 1: chromium --remote-debugging-port=9222 --user-data-dir=/tmp/darulifta-real-browser\n"
            "  Terminal 2: python darulifta_scrapy_downloader.py --cdp-url http://127.0.0.1:9222"
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "--base-url",
        default=DEFAULT_BASE_URL,
        help=f"Base URL of the English section (default: {DEFAULT_BASE_URL})",
    )
    parser.add_argument(
        "--output-dir",
        default=DEFAULT_OUTPUT_DIR,
        help=f"Directory for all downloaded files (default: {DEFAULT_OUTPUT_DIR})",
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=1.5,
        help="Seconds to wait between page requests (default: 1.5). "
             "Lower = faster but risky. Raise if you get rate-limited.",
    )
    parser.add_argument(
        "--max-pages-per-category",
        type=int,
        default=None,
        help="Safety limit: only download the first N pages of each category. "
             "Useful for testing. Omit to download all pages.",
    )
    parser.add_argument(
        "--full-refresh",
        action="store_true",
        help="Re-download fatwas that are already in the database.",
    )
    parser.add_argument(
        "--headless",
        action="store_true",
        help="Launch Chromium without a visible window. "
             "Only use this AFTER Cloudflare has already verified the browser profile.",
    )
    parser.add_argument(
        "--executable-path",
        default=None,
        help="Optional path to a Chromium/Chrome/Brave executable. "
             "Only needed if the auto-detection fails.",
    )
    parser.add_argument(
        "--cdp-url",
        default=None,
        help="URL of an already-running Chrome/Chromium with CDP enabled. "
             "Example: http://127.0.0.1:9222 "
             "(Start browser with: chromium --remote-debugging-port=9222 "
             "--user-data-dir=/tmp/darulifta-real-browser)",
    )
    parser.add_argument(
        "--workers",
        type=int,
        default=3,
        help="Number of parallel download threads for fatwa detail pages. "
             "Use 1 to disable parallelism. Recommended: 2-4 (default: 3).",
    )
    parser.add_argument(
        "--no-site-pages",
        action="store_true",
        help="Skip downloading general pages like About, Contact, Terms of Use.",
    )
    parser.add_argument(
        "--max-site-pages",
        type=int,
        default=50,
        help="Maximum number of general site pages to download (default: 50).",
    )
    return parser.parse_args(argv)


# ---------------------------------------------------------------------------
# MAIN FUNCTION
# ---------------------------------------------------------------------------

def main(argv: list[str]) -> int:
    """
    The main entry point of the script.
    1. Parses command-line arguments
    2. Sets up output directories
    3. Creates the crawler
    4. Runs the crawler
    """
    args = parse_args(argv)

    print("=" * 60)
    print("Darul Ifta Deoband Downloader — parsel/Scrapy Edition")
    print("=" * 60)
    print(f"Output directory:  {args.output_dir}")
    print(f"CDP URL:           {args.cdp_url or '(none — will launch new browser)'}")
    print(f"Parallel workers:  {args.workers}")
    print(f"Delay per request: {args.delay}s")
    if args.max_pages_per_category:
        print(f"Max pages/category: {args.max_pages_per_category} (testing mode)")
    print("=" * 60)

    paths = CrawlPaths.build(args.output_dir)

    crawler = DarulIftaScrapyDownloader(
        base_url=args.base_url,
        paths=paths,
        delay=args.delay,
        max_pages_per_category=args.max_pages_per_category,
        full_refresh=args.full_refresh,
        headless=args.headless,
        executable_path=args.executable_path,
        cdp_url=args.cdp_url,
        workers=args.workers,
        crawl_site_pages=not args.no_site_pages and args.max_site_pages != 0,
        max_site_pages=args.max_site_pages,
    )

    crawler.run()

    print("\n" + "=" * 60)
    print(f"Download complete!")
    print(f"Offline files:  {paths.site}")
    print(f"JSON records:   {paths.data}")
    print(f"SQLite DB:      {paths.db}")
    print(f"Browse offline: open {paths.site / 'index.html'} in your browser")
    print("=" * 60)
    return 0


# ---------------------------------------------------------------------------
# SCRIPT ENTRY POINT
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    # sys.argv[1:] passes all command-line arguments (excluding the script name)
    # raise SystemExit causes the shell to see the script's exit code correctly
    raise SystemExit(main(sys.argv[1:]))
