import hashlib
import re
from datetime import datetime

import requests
from firecrawl import Firecrawl

from config import (
    EVENTBRITE_URLS,
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


def normalize_event(raw: dict, source: str) -> dict | None:
    title = (raw.get("title") or "").strip()
    if not title:
        return None

    date_str = raw.get("date", "")
    event_id = generate_event_id(title, date_str)
    tags = raw.get("tags", [])
    is_online = raw.get("is_online", False)
    event_type = (raw.get("type") or "").lower()
    if event_type in ("online", "virtual"):
        is_online = True

    return {
        "id": event_id,
        "source": source,
        "titulo": title,
        "descripcion": raw.get("description", ""),
        "categoria": infer_category(tags),
        "ubicacion": raw.get("location", ""),
        "fecha": parse_date(date_str),
        "horaInicio": raw.get("time", ""),
        "horaFin": "",
        "organizador": raw.get("organizer", ""),
        "imagenUrl": raw.get("image_url", ""),
        "url": raw.get("url", ""),
        "isOnline": is_online,
        "clips": 0,
        "tiempoTexto": "",
        "tags": tags,
    }


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
        normalized_event = normalize_event(raw, "peruanos.dev")
        if normalized_event:
            normalized.append(normalized_event)

    log(f"[peruanos.dev] Got {len(normalized)} events")
    return normalized


SCRAPE_PROMPT = (
    "Extract all visible event details including title, full description (if visible), "
    "date, time, location, organizer, image URL, event URL, whether the event is online, "
    "and any visible tags or categories on the page."
)


def _firecrawl_scrape_urls(urls: list[str], source: str) -> list[dict]:
    if not FIRECRAWL_API_KEY:
        log(f"[{source}] No FIRECRAWL_API_KEY set, skipping")
        return []

    if not urls:
        log(f"[{source}] No URLs configured, skipping")
        return []

    firecrawl = Firecrawl(api_key=FIRECRAWL_API_KEY)

    log(f"[{source}] Sending {len(urls)} URLs to Firecrawl (batch):")
    for url in urls:
        log(f"  - {url}")

    try:
        job = firecrawl.batch_scrape(
            urls,
            formats=[{
                "type": "json",
                "prompt": SCRAPE_PROMPT,
                "schema": EVENT_JSON_SCHEMA,
            }],
            poll_interval=2,
            wait_timeout=300,
        )
    except Exception as e:
        log(f"[{source}] Batch scrape failed: {e}")
        return []

    all_events = []
    if job and job.data:
        for doc in job.data:
            json_data = doc.json if doc and hasattr(doc, "json") else None
            if not json_data or "events" not in json_data:
                continue

            source_url = ""
            if doc and hasattr(doc, "metadata") and doc.metadata:
                source_url = doc.metadata.get("sourceURL", "") or ""

            url_tags = infer_tags_from_url(source_url) if source_url else []
            event_count = len(json_data["events"])

            for event in json_data["events"]:
                existing_tags = event.get("tags") or []
                if url_tags and not existing_tags:
                    event["tags"] = url_tags
                normalized = normalize_event(event, source)
                if normalized:
                    all_events.append(normalized)

            log(f"  [{source}] {source_url} -> {event_count} events")

    status = getattr(job, "status", "unknown")
    completed = getattr(job, "completed", 0)
    total = getattr(job, "total", len(urls))
    log(f"[{source}] Done ({status}) — {completed}/{total} URLs, {len(all_events)} events")
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
