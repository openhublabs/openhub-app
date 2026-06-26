"""
test_scrape.py — Full pipeline diagnostic for the scraping system.

Tests the complete 5-step flow on ONE URL (Eventbrite Lima) to keep costs low:
  Step 1 — Listing scrape  (Firecrawl)
  Step 2 — Detail scrape   (Firecrawl, per event)
  Step 3 — Merge           (detail wins over listing fields)
  Step 4 — Quality filter  (date + location + image + description + category required)
  Step 5 — Normalize       (final Firestore shape)

Usage:
  Option A — inline key:
    FIRECRAWL_API_KEY=fc-xxx python test_scrape.py

  Option B — .env file in this folder:
    echo "FIRECRAWL_API_KEY=fc-xxx" > .env
    python test_scrape.py
"""

import json
import os
import sys

# Auto-load .env if present
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

api_key = os.environ.get("FIRECRAWL_API_KEY")
if not api_key:
    print("❌  Set FIRECRAWL_API_KEY first  (or create a .env file)")
    sys.exit(1)

# Patch env so config.py doesn't need FIREBASE_SERVICE_ACCOUNT at import time
os.environ.setdefault("FIREBASE_SERVICE_ACCOUNT", "dummy")

from firecrawl import Firecrawl
from config import EVENT_JSON_SCHEMA, EVENT_DETAIL_SCHEMA
from scrapers import (
    passes_quality_filter,
    normalize_event,
    infer_tags_from_url,
    _scrape_listing,
    _scrape_event_detail,
    _merge,
)

# ── Config ────────────────────────────────────────────────────────────────────
# Test only ONE URL to keep Firecrawl API cost low
TEST_URL   = "https://www.eventbrite.com/d/peru/technology/"
TEST_CITY  = "Lima"
TEST_SOURCE = "eventbrite"
MAX_DETAIL_SCRAPES = 5   # cap detail scrapes so the test runs fast (~5 API calls max)

firecrawl = Firecrawl(api_key=api_key)

print(f"\n{'═'*68}")
print(f"  PIPELINE TEST")
print(f"  URL    : {TEST_URL}")
print(f"  Ciudad : {TEST_CITY}")
print(f"  Detail scrapes capped at: {MAX_DETAIL_SCRAPES}")
print(f"{'═'*68}")

url_tags = infer_tags_from_url(TEST_URL)

# ── Step 1: Listing ───────────────────────────────────────────────────────────
print(f"\n📋  STEP 1 — Listing scrape")
raw_events = _scrape_listing(firecrawl, TEST_URL)
print(f"   Raw events returned: {len(raw_events)}")

if not raw_events:
    print("   ❌ No events from listing. Check URL and API key.")
    sys.exit(1)

# Show what the listing gave us for the first 3 events
print(f"\n   First 3 raw events from listing:")
for i, ev in enumerate(raw_events[:3], 1):
    print(f"   [{i}] title='{(ev.get('title') or '')[:50]}'")
    print(f"       url='{(ev.get('url') or '')[:60]}'")
    print(f"       date='{ev.get('date','')}' | location='{(ev.get('location') or '')[:30]}'")
    print(f"       image='{(ev.get('image_url') or '')[:40]}' | tags={ev.get('tags',[])}")

# ── Steps 2-4-5: Detail + Filter + Normalize ─────────────────────────────────
print(f"\n🔍  STEPS 2-3-4-5 — Detail scrape → Merge → Filter → Normalize")
print(f"    (capped at first {MAX_DETAIL_SCRAPES} events with a URL)")
print()

results      = []   # normalized events that passed
filter_stats = {}   # field → count of times it caused a skip
skipped      = []   # (title, missing_fields)

candidates = [ev for ev in raw_events if (ev.get("url") or "").strip()][:MAX_DETAIL_SCRAPES]

for i, raw in enumerate(candidates, 1):
    event_url = raw["url"].strip()
    title_preview = (raw.get("title") or "")[:45]

    print(f"  [{i}/{len(candidates)}] {title_preview}")
    print(f"  {'─'*60}")

    # Step 2 — Detail scrape
    detail = _scrape_event_detail(firecrawl, event_url)
    print(f"    Detail fields returned : {[k for k,v in detail.items() if v]}")

    # Step 3 — Merge
    merged = _merge(raw, detail)

    # Step 4 — Quality filter
    passes, missing = passes_quality_filter(merged, url_tags)
    if missing:
        print(f"    ⏭️  FILTER: missing → {missing}")
        for f in missing:
            filter_stats[f] = filter_stats.get(f, 0) + 1
        skipped.append((merged.get("title", "N/A"), missing))
    else:
        # Step 5 — Normalize
        event = normalize_event(merged, TEST_SOURCE, url_tags=url_tags, ciudad=TEST_CITY)
        results.append(event)
        print(f"    ✅ PASS")
        print(f"       titulo    : {event['titulo'][:55]}")
        print(f"       categoria : {event['categoria']}")
        print(f"       ciudad    : {event['ciudad']}")
        print(f"       fecha     : {event['fecha']}")
        print(f"       ubicacion : {(event['ubicacion'] or '—')[:50]}")
        print(f"       imagenUrl : {'✓' if event['imagenUrl'] else '—'}")
        print(f"       descripcion: {'✓ (' + str(len(event['descripcion'])) + ' chars)' if event['descripcion'] else '—'}")
    print()

# ── Summary ───────────────────────────────────────────────────────────────────
print(f"{'═'*68}")
print("  SUMMARY")
print(f"{'═'*68}")
print(f"  Listing events found  : {len(raw_events)}")
print(f"  Events detail-scraped : {len(candidates)}")
print(f"  Passed quality filter : {len(results)}")
print(f"  Skipped               : {len(skipped)}")

if filter_stats:
    print(f"\n  Filter rejection reasons:")
    for field, count in sorted(filter_stats.items(), key=lambda x: -x[1]):
        print(f"    {field:15}: {count}x")

verdict = "✅  PASS" if results else "❌  FAIL — 0 events passed the quality filter"
print(f"\n  {verdict}")
print(f"{'═'*68}\n")
