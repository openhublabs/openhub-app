import hashlib
import re
from datetime import datetime

import requests
from firecrawl import Firecrawl

from config import (
    EVENTBRITE_URLS,
    EVENT_DETAIL_SCHEMA,
    EVENT_JSON_SCHEMA,
    FIRECRAWL_API_KEY,
    MEETUP_URLS,
    PERUANOS_API_URL,
    TAG_TO_CATEGORY,
)


def log(msg: str):
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {msg}")


def generate_event_id(title: str, date: str) -> str:
    raw = f"{title}|{date}".lower().strip()
    return hashlib.md5(raw.encode()).hexdigest()[:16]


def infer_category(tags: list[str]) -> str:
    for tag in tags:
        key = tag.lower().strip()
        if key in TAG_TO_CATEGORY:
            return TAG_TO_CATEGORY[key]
    return ""


TITLE_CATEGORY_KEYWORDS: dict[str, str] = {
    "hackathon": "hackathon",
    "hackaton": "hackathon",
    "conference": "conferencia",
    "conferencia": "conferencia",
    "summit": "conferencia",
    "symposium": "conferencia",
    "workshop": "taller",
    "taller": "taller",
    "hands-on": "taller",
    "tutorial": "taller",
    "meetup": "meetup",
    "webinar": "webinar",
    "networking": "networking",
    "artificial intelligence": "inteligencia artificial",
    "machine learning": "inteligencia artificial",
    "deep learning": "inteligencia artificial",
    "llm": "inteligencia artificial",
    "generative ai": "inteligencia artificial",
    "genai": "inteligencia artificial",
    "gpt": "inteligencia artificial",
    "kubernetes": "devops",
    "k8s": "devops",
    "devops": "devops",
    "docker": "devops",
    "terraform": "devops",
    "cloud": "devops",
    "ci/cd": "devops",
    "ciberseguridad": "seguridad",
    "cybersecurity": "seguridad",
    "security": "seguridad",
    "hacking": "seguridad",
    "ctf": "seguridad",
    "pentest": "seguridad",
    "bug bounty": "seguridad",
    "python": "backend",
    "node.js": "backend",
    "nodejs": "backend",
    "rust": "backend",
    "golang": "backend",
    "java": "backend",
    "backend": "backend",
    "graphql": "backend",
    "javascript": "frontend",
    "typescript": "frontend",
    "react": "frontend",
    "angular": "frontend",
    "vue": "frontend",
    "frontend": "frontend",
    "android": "mobile",
    "ios": "mobile",
    "flutter": "mobile",
    "react native": "mobile",
    "kotlin": "mobile",
    "swift": "mobile",
    "startup": "startup",
    "emprendimiento": "startup",
    "entrepreneur": "startup",
    "pitch": "startup",
    "founders": "startup",
    "venture capital": "inversion",
    "investors": "inversion",
    "funding": "inversion",
    "design": "design",
    "ux": "design",
    "ui": "design",
    "figma": "design",
    "marketing": "marketing",
    "growth": "marketing",
    "seo": "marketing",
    "technology": "conferencia",
    "tech": "conferencia",
    "innovation": "conferencia",
    "data": "conferencia",
    "blockchain": "conferencia",
    "web3": "conferencia",
    "software": "conferencia",
    "developer": "conferencia",
    "coding": "conferencia",
    "programming": "conferencia",
}


def infer_category_from_title(title: str) -> str:
    title_lower = title.lower()
    words = set(title_lower.split())
    for keyword, category in TITLE_CATEGORY_KEYWORDS.items():
        if " " in keyword:
            if keyword in title_lower:
                return category
        else:
            if keyword in words:
                return category
    if re.search(r'\bai\b', title_lower):
        return "inteligencia artificial"
    return ""


def infer_tags_from_url(url: str) -> list[str]:
    url_lower = url.lower()
    if "eventbrite.com" in url_lower:
        match = re.search(r'/d/[^/]+/([^/?]+)', url_lower)
        if match:
            return [match.group(1)]
    if "meetup.com" in url_lower:
        match = re.search(r'keywords=([^&]+)', url_lower)
        if match:
            return [match.group(1)]
    return []


def parse_date(date_str: str) -> str:
    if not date_str:
        return ""
    try:
        dt = datetime.strptime(date_str, "%Y-%m-%d")
    except (ValueError, AttributeError):
        try:
            dt = datetime.fromisoformat(date_str.replace("Z", "+00:00"))
        except (ValueError, AttributeError):
            return date_str
    meses = [
        "ene", "feb", "mar", "abr", "may", "jun",
        "jul", "ago", "sep", "oct", "nov", "dic",
    ]
    return f"{dt.day} {meses[dt.month - 1]} {dt.year}"


def passes_quality_filter(merged: dict, url_tags: list[str]) -> tuple[bool, list[str]]:
    """
    Step 4 — Quality gate applied AFTER detail enrichment (merged listing + detail fields).

    Returns (passes: bool, missing_fields: list[str]).

    Required fields:
      - title       : non-empty string
      - url         : non-empty string
      - date        : non-empty (raw date string from scrape)
      - location    : non-empty (venue or "Online")
      - image_url   : non-empty URL
      - description : non-empty text
      - category    : must be inferrable from tags or title (no blank categoria)
    """
    missing = []

    if not (merged.get("title") or "").strip():
        missing.append("title")
    if not (merged.get("url") or "").strip():
        missing.append("url")
    if not (merged.get("date") or "").strip():
        missing.append("date")
    if not (merged.get("location") or "").strip():
        missing.append("location")
    if not (merged.get("image_url") or "").strip():
        missing.append("image_url")
    if not (merged.get("description") or "").strip():
        missing.append("description")

    # Category check — must be inferable (hard reject if not a recognizable tech event)
    tags = merged.get("tags", [])
    title = merged.get("title", "")
    category = (
        infer_category(tags)
        or infer_category_from_title(title)
        or infer_category(url_tags)
    )
    if not category:
        missing.append("categoria")

    return (len(missing) == 0, missing)


def normalize_event(merged: dict, source: str, url_tags: list[str] | None = None, ciudad: str = "") -> dict:
    """
    Convert a merged (listing + detail) raw dict into the final Firestore document shape.
    Assumes passes_quality_filter() has already been called and passed.
    """
    url_tags = url_tags or []
    tags = merged.get("tags", [])

    is_online = merged.get("is_online", False)
    event_type = (merged.get("type") or "").lower()
    if event_type in ("online", "virtual"):
        is_online = True

    category = (
        infer_category(tags)
        or infer_category_from_title(merged.get("title", ""))
        or infer_category(url_tags)
    )

    date_str = merged.get("date", "")
    title = (merged.get("title") or "").strip()
    event_id = generate_event_id(title, date_str)
    final_tags = tags if tags else url_tags

    return {
        "id": event_id,
        "source": source,
        "titulo": title,
        "descripcion": merged.get("description", ""),
        "categoria": category,
        "ciudad": ciudad,
        "ubicacion": merged.get("location", ""),
        "fecha": parse_date(date_str),
        "horaInicio": merged.get("time", ""),
        "horaFin": "",
        "organizador": merged.get("organizer", ""),
        "imagenUrl": merged.get("image_url", ""),
        "url": merged.get("url", ""),
        "isOnline": is_online,
        "clips": 0,
        "tiempoTexto": "",
        "tags": final_tags,
    }


# ── peruanos.dev ─────────────────────────────────────────────────────────────

def scrape_peruanos() -> list[dict]:
    log("[peruanos.dev] Fetching from API...")
    try:
        response = requests.get(PERUANOS_API_URL, timeout=30)
        response.raise_for_status()
        events = response.json()
    except Exception as e:
        log(f"[peruanos.dev] Error: {e}")
        return []

    normalized = []
    today = datetime.now().date()
    for event in events:
        date_str = event.get("date", "")
        try:
            event_date = datetime.strptime(date_str, "%Y-%m-%d").date()
            if event_date < today:
                continue
        except ValueError:
            pass
        raw = {
            "title": event.get("title", ""),
            "description": event.get("description", ""),
            "date": date_str,
            "time": event.get("time", ""),
            "location": event.get("location", ""),
            "organizer": event.get("organizer", ""),
            "image_url": event.get("image_url", ""),
            "url": event.get("registration_url", ""),
            "type": event.get("type", ""),
            "tags": event.get("tags", []),
        }
        passes, missing = passes_quality_filter(raw, [])
        if passes:
            normalized.append(normalize_event(raw, "peruanos.dev", ciudad="Lima"))
        else:
            log(f"  [peruanos.dev] SKIP '{raw['title'][:50]}' — missing: {missing}")

    log(f"[peruanos.dev] Got {len(normalized)} events")
    return normalized


# ── Firecrawl helpers ─────────────────────────────────────────────────────────

def _scrape_listing(firecrawl: Firecrawl, url: str) -> list[dict]:
    """Step 1 — Scrape the listing page, return list of raw event dicts."""
    try:
        result = firecrawl.scrape(
            url,
            formats=[{"type": "json", "schema": EVENT_JSON_SCHEMA}],
        )
        json_data = result.json if result and hasattr(result, "json") else None
        if json_data and "events" in json_data:
            return json_data["events"]
    except Exception as e:
        log(f"  [listing] Error scraping {url[:60]}: {e}")
    return []


def _scrape_event_detail(firecrawl: Firecrawl, event_url: str) -> dict:
    """Step 2 — Scrape the individual event page, return enriched fields (or empty dict)."""
    try:
        result = firecrawl.scrape(
            event_url,
            formats=[{"type": "json", "schema": EVENT_DETAIL_SCHEMA}],
        )
        detail = result.json if result and hasattr(result, "json") else None
        return detail if isinstance(detail, dict) else {}
    except Exception as e:
        log(f"    [detail] Error scraping {event_url[:60]}: {e}")
        return {}


def _merge(listing_event: dict, detail: dict) -> dict:
    """
    Step 3 — Merge listing + detail fields.
    Detail wins for any field it provides (it has more context from the full event page).
    """
    merged = {**listing_event}
    for key in ["title", "description", "date", "time", "location", "organizer", "image_url", "is_online", "tags"]:
        if detail.get(key):
            merged[key] = detail[key]
    return merged


# ── Main Firecrawl scrape loop ────────────────────────────────────────────────

def _firecrawl_scrape_urls(urls: list[tuple[str, str]], source: str) -> list[dict]:
    if not FIRECRAWL_API_KEY:
        log(f"[{source}] No FIRECRAWL_API_KEY set, skipping")
        return []

    if not urls:
        log(f"[{source}] No URLs configured, skipping")
        return []

    firecrawl = Firecrawl(api_key=FIRECRAWL_API_KEY)
    all_events = []

    for listing_url, ciudad in urls:
        log(f"  [{source}] Scraping listing: {listing_url}")

        # ── Step 1: Listing scrape ───────────────────────────────────────────
        raw_events = _scrape_listing(firecrawl, listing_url)
        if not raw_events:
            log(f"  [{source}] No events found on listing page")
            continue

        log(f"  [{source}] Found {len(raw_events)} raw events on listing")

        url_tags = infer_tags_from_url(listing_url)
        accepted = 0
        skipped = 0

        for raw in raw_events:
            event_url = (raw.get("url") or "").strip()
            if not event_url:
                skipped += 1
                continue

            # ── Step 2: Detail scrape ────────────────────────────────────────
            log(f"    [{source}] Detail: {event_url[:70]}")
            detail = _scrape_event_detail(firecrawl, event_url)

            # ── Step 3: Merge ────────────────────────────────────────────────
            merged = _merge(raw, detail)

            # ── Step 4: Quality filter ───────────────────────────────────────
            passes, missing = passes_quality_filter(merged, url_tags)
            if not passes:
                skipped += 1
                log(f"    [{source}] SKIP '{(merged.get('title') or '')[:45]}' — missing: {missing}")
                continue

            # ── Step 5: Normalize → add to results ──────────────────────────
            event = normalize_event(merged, source, url_tags=url_tags, ciudad=ciudad)
            all_events.append(event)
            accepted += 1

        log(f"  [{source}] {listing_url} → accepted {accepted}, skipped {skipped}")

    log(f"[{source}] Done — {len(all_events)} quality events from {len(urls)} URLs")
    return all_events


def scrape_eventbrite() -> list[dict]:
    return _firecrawl_scrape_urls(EVENTBRITE_URLS, "eventbrite")


def scrape_meetup() -> list[dict]:
    return _firecrawl_scrape_urls(MEETUP_URLS, "meetup")


SCRAPERS = {
    "peruanos.dev": scrape_peruanos,
    "eventbrite": scrape_eventbrite,
    "meetup": scrape_meetup,
}
