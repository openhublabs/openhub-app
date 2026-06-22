import os
import hashlib
import json
import base64
from datetime import datetime

import requests
from firebase_admin import credentials, firestore, initialize_app


EVENT_SOURCE = "peruanos.dev"
API_URL = "https://peruanos.dev/api/events"
FIRESTORE_BATCH_LIMIT = 450

FIREBASE_SERVICE_ACCOUNT_B64 = os.environ.get("FIREBASE_SERVICE_ACCOUNT")
if not FIREBASE_SERVICE_ACCOUNT_B64:
    raise ValueError("Missing FIREBASE_SERVICE_ACCOUNT environment variable")

sa_key = json.loads(base64.b64decode(FIREBASE_SERVICE_ACCOUNT_B64).decode("utf-8"))
cred = credentials.Certificate(sa_key)
initialize_app(cred)
db = firestore.client()

TAG_TO_CATEGORY = {
    "hackathon": "hackathon",
    "conference": "conferencia",
    "conference/talk": "conferencia",
    "summit": "conferencia",
    "workshop": "taller",
    "workshop/hands-on": "taller",
    "hands-on": "taller",
    "tutorial": "taller",
    "meetup": "meetup",
    "webinar": "webinar",
    "virtual": "online",
    "ai": "inteligencia artificial",
    "machine learning": "inteligencia artificial",
    "vertex ai": "inteligencia artificial",
    "build with ai": "inteligencia artificial",
    "kubernetes": "devops",
    "devops": "devops",
    "cloud native": "devops",
    "docker": "devops",
    "aws": "devops",
    "gcp": "devops",
    "azure": "devops",
    "ciberseguridad": "seguridad",
    "cybersecurity": "seguridad",
    "secops": "seguridad",
    "ctf": "seguridad",
    "seguridad": "seguridad",
    "networking": "networking",
    "python": "backend",
    "javascript": "frontend",
    "react": "frontend",
    "angular": "frontend",
    "vue": "frontend",
    "android": "mobile",
    "ios": "mobile",
    "mobile": "mobile",
}


def generate_event_id(title: str, date: str) -> str:
    raw = f"{title}|{date}".lower().strip()
    return f"peruanosdev-{hashlib.md5(raw.encode()).hexdigest()[:16]}"


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


def delete_old_events() -> int:
    docs = db.collection("events").where("source", "==", EVENT_SOURCE).stream()
    batch = db.batch()
    count = 0
    for doc in docs:
        batch.delete(doc.reference)
        count += 1
        if count % FIRESTORE_BATCH_LIMIT == 0:
            batch.commit()
            batch = db.batch()
    if count > 0:
        batch.commit()
    return count


def main():
    print(f"Deleting old {EVENT_SOURCE} events...")
    deleted = delete_old_events()
    print(f"Deleted {deleted} old events")

    print(f"Fetching events from {API_URL}...")
    try:
        response = requests.get(API_URL, timeout=30)
        response.raise_for_status()
        events = response.json()
    except Exception as e:
        print(f"Error fetching events: {e}")
        return

    print(f"Got {len(events)} events from API")

    today = datetime.now().date()
    batch = db.batch()
    count = 0
    skipped = 0

    for event in events:
        title = event.get("title", "").strip()
        if not title:
            continue

        event_date_str = event.get("date", "")
        try:
            event_date = datetime.strptime(event_date_str, "%Y-%m-%d").date()
            if event_date < today:
                skipped += 1
                continue
        except ValueError:
            pass

        event_id = generate_event_id(title, event_date_str)
        event_type = event.get("type", "").lower()
        tags = event.get("tags", [])

        doc_ref = db.collection("events").document(event_id)
        batch.set(doc_ref, {
            "id": event_id,
            "source": EVENT_SOURCE,
            "titulo": title,
            "descripcion": event.get("description", ""),
            "categoria": infer_category(tags),
            "ubicacion": event.get("location", ""),
            "fecha": parse_date(event_date_str),
            "horaInicio": event.get("time", ""),
            "horaFin": "",
            "organizador": event.get("organizer", ""),
            "imagenUrl": event.get("image_url", ""),
            "url": event.get("registration_url", ""),
            "isOnline": event_type in ("online", "virtual"),
            "clips": 0,
            "tiempoTexto": "",
            "tags": tags,
            "updatedAt": firestore.SERVER_TIMESTAMP,
        })
        count += 1

        if count % FIRESTORE_BATCH_LIMIT == 0:
            batch.commit()
            print(f"  Committed batch of {count} events...")
            batch = db.batch()

    batch.commit()
    print(f"Inserted {count} events, skipped {skipped} past events")


if __name__ == "__main__":
    main()
