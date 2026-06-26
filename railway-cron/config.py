import os
import warnings

warnings.filterwarnings("ignore", message="Detected filter using positional arguments")

FIRESTORE_BATCH_LIMIT = 450

# Firebase — validated at startup in main.py (not here, so test_scrape.py can import freely)
FIREBASE_SERVICE_ACCOUNT_B64 = os.environ.get("FIREBASE_SERVICE_ACCOUNT")

FIRECRAWL_API_KEY = os.environ.get("FIRECRAWL_API_KEY", "")

PERUANOS_API_URL = "https://peruanos.dev/api/events"

EVENTBRITE_URLS = [
    ("https://www.eventbrite.com/d/peru/technology/", "Lima"),
    ("https://www.eventbrite.com/d/argentina--buenos-aires/technology/", "Buenos Aires"),
    ("https://www.eventbrite.com/d/mexico--ciudad-de-mexico/technology/", "Ciudad de México"),
]

MEETUP_URLS = [
    ("https://www.meetup.com/find/?keywords=technology&location=pe--Lima", "Lima"),
    ("https://www.meetup.com/find/?keywords=technology&location=ar--Buenos+Aires", "Buenos Aires"),
    ("https://www.meetup.com/find/?keywords=technology&location=mx--Mexico+City", "Ciudad de México"),
]

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
    "online": "online",
    "ai": "inteligencia artificial",
    "artificial intelligence": "inteligencia artificial",
    "machine learning": "inteligencia artificial",
    "deep learning": "inteligencia artificial",
    "llm": "inteligencia artificial",
    "vertex ai": "inteligencia artificial",
    "build with ai": "inteligencia artificial",
    "generative ai": "inteligencia artificial",
    "kubernetes": "devops",
    "devops": "devops",
    "cloud native": "devops",
    "docker": "devops",
    "containers": "devops",
    "aws": "devops",
    "gcp": "devops",
    "azure": "devops",
    "cloud": "devops",
    "ci/cd": "devops",
    "ciberseguridad": "seguridad",
    "cybersecurity": "seguridad",
    "secops": "seguridad",
    "ctf": "seguridad",
    "seguridad": "seguridad",
    "security": "seguridad",
    "networking": "networking",
    "python": "backend",
    "javascript": "frontend",
    "typescript": "frontend",
    "react": "frontend",
    "angular": "frontend",
    "vue": "frontend",
    "node": "backend",
    "nodejs": "backend",
    "java": "backend",
    "go": "backend",
    "golang": "backend",
    "rust": "backend",
    "android": "mobile",
    "ios": "mobile",
    "mobile": "mobile",
    "flutter": "mobile",
    "startup": "startup",
    "emprendimiento": "startup",
    "entrepreneurship": "startup",
    "entrepreneur": "startup",
    "pitch": "startup",
    "pitch competition": "startup",
    "founders": "startup",
    "business": "negocios",
    "negocios": "negocios",
    "marketing": "marketing",
    "growth": "marketing",
    "product": "producto",
    "producto": "producto",
    "design": "design",
    "ux": "design",
    "ui": "design",
    "investors": "inversion",
    "venture capital": "inversion",
    "vc": "inversion",
    "funding": "inversion",
    "technology": "conferencia",
}

# Schema for listing pages — extracts all event cards visible on the page
EVENT_JSON_SCHEMA = {
    "type": "object",
    "properties": {
        "events": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "title": {"type": "string"},
                    "description": {"type": "string"},
                    "date": {"type": "string"},
                    "time": {"type": "string"},
                    "location": {"type": "string"},
                    "organizer": {"type": "string"},
                    "image_url": {"type": "string"},
                    "url": {"type": "string"},
                    "is_online": {"type": "boolean"},
                    "tags": {"type": "array", "items": {"type": "string"}},
                },
                "required": ["title", "url"],
            },
        }
    },
    "required": ["events"],
}

# Schema for individual event detail pages — richer fields from the event page itself
EVENT_DETAIL_SCHEMA = {
    "type": "object",
    "properties": {
        "title": {"type": "string"},
        "description": {"type": "string"},
        "date": {"type": "string"},
        "time": {"type": "string"},
        "location": {"type": "string"},
        "organizer": {"type": "string"},
        "image_url": {"type": "string"},
        "url": {"type": "string"},
        "is_online": {"type": "boolean"},
        "tags": {"type": "array", "items": {"type": "string"}},
    },
    "required": ["title"],
}
