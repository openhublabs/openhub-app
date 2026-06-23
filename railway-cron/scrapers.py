import hashlib
from datetime import datetime

import requests
from firecrawl import Firecrawl

from config import (
    EVENTBRITE_URLS,
    EVENT_JSON_SCHEMA,
    FIRECRAWL_API_KEY,
    LUMA_URLS,
    MEETUP_URLS,
    PERUANOS_API_URL,
    TAG_TO_CATEGORY,
)


def generate_event_id(title: str, date: str, source: str) -> str:
    raw = f"{title}|{date}".lower().strip()
    return f"{source}-{hashlib.md5(raw.encode()).hexdigest()[:16]}"


def infer_category(tags: list[str]) -> str:
    for tag in tags:
        key = tag.lower().strip()
        if key in TAG_TO_CATEGORY:
            return TAG_TO_CATEGORY[key]
    return ""


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
    event_id = generate_event_id(title, date_str, source)
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
    print("[peruanos.dev] Fetching from API...")
    try:
        response = requests.get(PERUANOS_API_URL, timeout=30)
        response.raise_for_status()
        events = response.json()
    except Exception as e:
        print(f"[peruanos.dev] Error: {e}")
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

    print(f"[peruanos.dev] Got {len(normalized)} events")
    return normalized


def _firecrawl_scrape_urls(urls: list[str], source: str) -> list[dict]:
    if not FIRECRAWL_API_KEY:
        print(f"[{source}] No FIRECRAWL_API_KEY set, skipping")
        return []

    firecrawl = Firecrawl(api_key=FIRECRAWL_API_KEY)
    all_events = []

    for url in urls:
        print(f"[{source}] Scraping {url}")
        try:
            result = firecrawl.scrape(
                url,
                formats=[{
                    "type": "json",
                    "schema": EVENT_JSON_SCHEMA,
                }],
            )
            json_data = result.json if result else None
            if json_data and "events" in json_data:
                for event in json_data["events"]:
                    normalized = normalize_event(event, source)
                    if normalized:
                        all_events.append(normalized)
        except Exception as e:
            print(f"[{source}] Error scraping {url}: {e}")

    print(f"[{source}] Got {len(all_events)} events")
    return all_events


def scrape_eventbrite() -> list[dict]:
    return _firecrawl_scrape_urls(EVENTBRITE_URLS, "eventbrite")


def scrape_luma() -> list[dict]:
    return _firecrawl_scrape_urls(LUMA_URLS, "luma")


def scrape_meetup() -> list[dict]:
    return _firecrawl_scrape_urls(MEETUP_URLS, "meetup")


SCRAPERS = {
    "peruanos.dev": scrape_peruanos,
    "eventbrite": scrape_eventbrite,
    "luma": scrape_luma,
    "meetup": scrape_meetup,
}
