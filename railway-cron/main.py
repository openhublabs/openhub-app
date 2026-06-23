import base64
import json

from firebase_admin import credentials, firestore, initialize_app

from config import FIREBASE_SERVICE_ACCOUNT_B64, FIRESTORE_BATCH_LIMIT
from scrapers import SCRAPERS

sa_key = json.loads(base64.b64decode(FIREBASE_SERVICE_ACCOUNT_B64).decode("utf-8"))
cred = credentials.Certificate(sa_key)
initialize_app(cred)
db = firestore.client()


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


def insert_events(events: list[dict]) -> int:
    batch = db.batch()
    count = 0
    for event in events:
        doc_ref = db.collection("events").document(event["id"])
        batch.set(doc_ref, {**event, "updatedAt": firestore.SERVER_TIMESTAMP})
        count += 1
        if count % FIRESTORE_BATCH_LIMIT == 0:
            batch.commit()
            batch = db.batch()
    batch.commit()
    return count


def main():
    total_inserted = 0
    total_deleted = 0

    for source, scraper_fn in SCRAPERS.items():
        print(f"\n=== {source} ===")
        try:
            events = scraper_fn()
        except Exception as e:
            print(f"[{source}] Scraper failed: {e}")
            continue

        if not events:
            print(f"[{source}] No events found, skipping Firestore update")
            continue

        deleted = delete_old_events(source)
        print(f"[{source}] Deleted {deleted} old events")

        inserted = insert_events(events)
        print(f"[{source}] Inserted {inserted} events")

        total_deleted += deleted
        total_inserted += inserted

    print(f"\n=== Summary ===")
    print(f"Total deleted: {total_deleted}")
    print(f"Total inserted: {total_inserted}")


if __name__ == "__main__":
    main()
