import base64
import json
import time
import warnings
from datetime import datetime

warnings.filterwarnings("ignore", message='Field name "json"')

from firebase_admin import credentials, firestore, initialize_app

from config import FIREBASE_SERVICE_ACCOUNT_B64, FIRESTORE_BATCH_LIMIT
from scrapers import SCRAPERS

sa_key = json.loads(base64.b64decode(FIREBASE_SERVICE_ACCOUNT_B64).decode("utf-8"))
cred = credentials.Certificate(sa_key)
initialize_app(cred)
db = firestore.client()


def log(msg: str):
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {msg}")


def delete_old_events(source: str) -> int:
    docs = db.collection("events").where("source", "==", source).stream()
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


def insert_events(events: list[dict], source: str) -> int:
    batch = db.batch()
    count = 0
    for event in events:
        doc_ref = db.collection("events").document(event["id"])
        batch.set(doc_ref, {
            **event,
            "sources": firestore.ArrayUnion([source]),
            "updatedAt": firestore.SERVER_TIMESTAMP,
        }, merge=True)
        count += 1
        if count % FIRESTORE_BATCH_LIMIT == 0:
            batch.commit()
            batch = db.batch()
    if count > 0:
        batch.commit()
    return count


def main():
    start_time = time.time()
    log("Starting event scraper...")

    totals = {}

    for source, scraper_fn in SCRAPERS.items():
        scraper_start = time.time()
        log(f"=== {source} ===")
        try:
            events = scraper_fn()
        except Exception as e:
            log(f"[{source}] Scraper failed: {e}")
            totals[source] = {"events": 0, "deleted": 0, "inserted": 0}
            continue

        scraper_duration = time.time() - scraper_start

        if not events:
            log(f"[{source}] No events found, skipping Firestore update ({scraper_duration:.1f}s)")
            totals[source] = {"events": 0, "deleted": 0, "inserted": 0}
            continue

        deleted = delete_old_events(source)
        inserted = insert_events(events, source)

        log(f"[{source}] Deleted {deleted} old, inserted {inserted} new ({scraper_duration:.1f}s)")
        totals[source] = {"events": len(events), "deleted": deleted, "inserted": inserted}

    total_duration = time.time() - start_time
    total_events = sum(t["events"] for t in totals.values())
    total_deleted = sum(t["deleted"] for t in totals.values())
    total_inserted = sum(t["inserted"] for t in totals.values())

    log("=== Summary ===")
    for source, t in totals.items():
        log(f"  {source}: {t['events']} events, {t['deleted']} deleted, {t['inserted']} inserted")
    log(f"  Total: {total_events} events, {total_deleted} deleted, {total_inserted} inserted")
    log(f"  Duration: {total_duration:.1f}s")


if __name__ == "__main__":
    main()
