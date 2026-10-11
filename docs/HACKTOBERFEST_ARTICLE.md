---
title: "TrailDex: Turn a Walk into a Pocket Nature Deck"
published: false
tags: devchallenge, hf26challenge, android, ai
---

*This is a submission for the [Hacktoberfest Open-Source AI Challenge Week 1: Touch Grass](https://dev.to/challenges/hacktoberfest-week1-2026-10-05)*

## What I Built

TrailDex is an Android field guide game for people who want a small reason to notice more on a walk. Pick two points on the map to preview an open walking route, start a GPS walk, and collect a bird, plant, fungus, insect, or other observation as a retro pocket-monster card. Each card has simple battle stats and a short field haiku. You can save and share cards, keep a local field journal, and use the collection in an offline practice battle.

The intended rhythm is short: plan the walk, put the phone away, stop briefly when something catches your eye, then look through the cards and battle after the walk. TrailDex does not identify species in the background; photo suggestions happen only when the user asks the configured model.

### Screenshots

These screenshots are from the Android demo build. The map and route screen requires internet for map tiles and route planning; GPS logs and cards remain on the device.

![TrailDex nature card collection](https://raw.githubusercontent.com/sudeepsarmah/traildex/main/docs/screenshots/nature-cards.png)

![TrailDex local practice battle](https://raw.githubusercontent.com/sudeepsarmah/traildex/main/docs/screenshots/practice-battle.png)

![TrailDex scout log and badges](https://raw.githubusercontent.com/sudeepsarmah/traildex/main/docs/screenshots/scout-log.png)

## Demo

Download the [TrailDex Android APK](https://raw.githubusercontent.com/sudeepsarmah/traildex/main/downloads/traildex-debug.apk) and follow the [phone installation guide](https://github.com/sudeepsarmah/traildex#try-traildex-on-your-android-phone). The APK is a debug demo distributed directly from GitHub; it is not a Play Store release and does not update automatically.

## Code

The complete Android project, build steps, and tester guide are in the [TrailDex GitHub repository](https://github.com/sudeepsarmah/traildex).

## How I Built It

TrailDex is written in Kotlin with Jetpack Compose. The interactive map is rendered with the open-source [Leaflet](https://leafletjs.com/) library in an Android WebView, using [OpenStreetMap tiles](https://www.openstreetmap.org/copyright). Walking route geometry comes from the public [FOSSGIS Open Source Routing Machine foot profile](https://routing.openstreetmap.de/about.html) and is drawn in TrailDex; no Google Maps or Mapbox API key or billing account is involved. The public map and routing services are community resources with fair-use limits and no uptime guarantee.

The AI card flow uses Gemma 3 4B through Ollama. Ollama runs on a computer the user controls, and the phone connects to it when the host is reachable. With a photo, the model can suggest a visible organism and write a haiku; a text observation can be used to generate a haiku. If the model is unavailable, saving still works with varied bundled verse templates. Those fallback verses are not model output. TrailDex does not bundle the model or run Ollama on the phone.

Cards, field notes, and saved GPS tracks are stored locally. There is no account system or multiplayer service. Map tiles and route requests need internet; route requests send the selected endpoints to FOSSGIS. See the README for the [map service limits](https://github.com/sudeepsarmah/traildex#what-works-offline), [Ollama setup](https://github.com/sudeepsarmah/traildex#optional-open-weight-ai), and [privacy scope](https://github.com/sudeepsarmah/traildex#scope-and-privacy).

## Why Does Open Innovation Matter?

The model is replaceable, and the prompt and client code are visible. A user can run Gemma on a computer they control instead of sending an observation photo to a hosted model API. That gives the user more control over where their photo and field note go, and avoids a per-request hosted AI bill for this flow. It does require the user to install Ollama and download a model, so this is not a one-tap offline AI app.

Open map rendering and OpenStreetMap data also let TrailDex show a real map and pedestrian route without a proprietary map SDK or API key. Using a public tile or routing service is not unlimited free hosting: the community endpoints have usage policies, can be unavailable, and should be replaced by a suitable provider or self-hosted infrastructure if the demo grows.

## My Agent Session

No shareable agent-session link is attached yet. This section is optional for the challenge.

## Prize Categories

- **Best Use of Gemma** — TrailDex uses Gemma 3 4B through Ollama for opt-in photo suggestions and haiku generation.

---

Before publishing, add any teammates' DEV handles if applicable, verify that the screenshots are present, and test the APK on a phone. The Week 1 challenge page lists submissions due October 11, 2026; confirm the current submission window on the [challenge page](https://dev.to/challenges/hacktoberfest-week1-2026-10-05).
