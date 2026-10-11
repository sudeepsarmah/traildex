# TrailDex: first-run tester tutorial

This guide walks through the demo from installation to a first practice battle. TrailDex is an Android demo; no account or server is needed.

## 1. Install the demo on an Android phone

### Install the APK (no computer or Android Studio required)

1. On the phone, tap [Download TrailDex for Android](https://raw.githubusercontent.com/sudeepsarmah/traildex/main/downloads/traildex-debug.apk).
2. Wait for the approximately 12 MiB download to finish.
3. Open the downloaded file from the browser's download notification or from **Files → Downloads**.
4. If Android blocks the installation, open the offered settings and enable **Allow from this source** for the browser or Files app. Return to the installer and tap **Install**. You can disable this permission after installation.
5. Tap **Open**. On first use, grant location while using TrailDex when prompted.

This is a debug build distributed directly from GitHub, outside Google Play. Android may show a warning for apps from outside the Play Store. Install only if you trust this project. The APK is hosted directly in this repository. Updates do not install automatically; revisit the README and download the current APK again when the project is updated.

### Build from source

Install Android Studio, Android SDK 36, and JDK 17. Clone this repository and open the project folder in Android Studio:

```sh
git clone https://github.com/sudeepsarmah/traildex.git
cd traildex
```

Wait for Gradle sync, then run the `app` configuration on an emulator or connected Android phone.

Or, from PowerShell with an emulator or phone already connected:

```powershell
.\gradlew.bat installDebug
```

To build without installing, run `.\gradlew.bat :app:assembleDebug`. The APK appears at `app/build/outputs/apk/debug/app-debug.apk`.

## 2. Plan and record your own walk

1. Open **Trail**. Tap **Pick Start**, tap a location on the map, then repeat with **Pick Finish**.
2. Tap **Find Walking Route**. TrailDex requests a pedestrian route from the public FOSSGIS OSRM service and draws its geometry on the map. The request shares the selected endpoints with FOSSGIS and needs internet. Review the route before walking.
3. Tap **Start a Walk**, give it a name, and grant location permission while using the app. On Android 13+, allow notifications for the active tracking notification.
4. Go outdoors and follow the route in your map app. TrailDex records GPS points and distance while its foreground walk session is active.
5. Return to TrailDex and tap **Finish & Save Walk**. The GPS trace, distance, and end time are saved locally. Tap a saved walk to display its GPS path on the map.

The live OSM map tiles and walking route service need internet. GPS logging and saved route data stay on the phone; GPS traces still display over an empty tile background offline. OSM tiles and FOSSGIS routing are public community services, best-effort, rate limited, and not an offline route service. Avoid repeated route requests (the service policy allows at most one per second), bulk tile use, or relying on the route as safety-critical navigation. Attribution: Map data © OpenStreetMap contributors; routes by OSRM / FOSSGIS.

## 3. Create a field card without AI

1. Open **Cards** and enter an observation such as “crow”, “moss”, or “orange mushroom”.
2. Add a trail or habitat name.
3. Tap **Generate Field Haiku**. If Ollama is unavailable, TrailDex selects an offline verse from its built-in collection. The verse is a template, not model output.
4. Edit the observation or verse if you like, then tap **Save Field Card**.
5. Restart the app and confirm the card remains in your collection.

## 4. Optional Ollama photo and haiku generation

Ollama runs on a computer, not inside TrailDex or on the phone. The phone must be able to reach it over the local network. After a successful address check, TrailDex saves the endpoint, checks it when Cards opens, and retries when network connectivity returns. The host computer and model must still be running; internet access alone cannot deploy or start Ollama.

1. Install Ollama on a computer and run `ollama pull gemma3:4b`.
2. By default, Ollama listens only on `127.0.0.1`. For a physical phone, set the computer's user environment variable `OLLAMA_HOST` to `0.0.0.0:11434`, fully quit and restart Ollama, and allow inbound port 11434 only on a trusted private network. The official [Ollama network configuration guide](https://docs.ollama.com/faq#how-can-i-expose-ollama-on-my-network) has OS-specific steps.
3. Find the computer's private LAN IPv4 address (for example, `192.168.1.25`). In TrailDex **Cards**, enter `http://192.168.1.25:11434` in **Ollama address** and tap **Check Ollama Connection**. Replace the example IP with the computer's actual address. This address is remembered on this phone.
4. For an Android emulator on the same computer, use `http://10.0.2.2:11434` instead; this is the default in debug builds.
5. Describe an observation or choose/take a photo, then request generation. Read the model's suggestion carefully before saving; it can be incorrect or uncertain.

Keep this connection on a trusted local network. Do not expose Ollama to the public internet. If Ollama cannot be reached, card saving still works with an offline verse.

## 5. Battle with a collected card

1. Open **Battle**. Cards saved in Cards appear in your selectable deck.
2. Tap the card you want to use and try the available moves and field pouch. Each rival has different HP and attack strength, and retaliates after moves.
3. Watch the win/loss overlay, then use **Restart With a New Rival** for another match.

Battles are local practice only. Online matchmaking and accounts are not part of this demo.

## 6. Share a card and review your field journal

Use the card's share action to send its text to an Android app that accepts shared text. The Scout page shows your saved field journal and local progress. TrailDex does not upload cards or trail history to a TrailDex server.

## Troubleshooting

| Problem | Try this |
| --- | --- |
| No device appears for `installDebug` | Start the Android emulator first, or enable USB debugging and accept the phone's computer prompt. Check `adb devices`. |
| GPS is still waiting | Go outdoors, enable phone location, grant precise location if available, and wait briefly for a fix. |
| Map tiles are blank | Check internet; GPS tracking and route history still work offline. OSM's public tile service is best-effort. |
| No walking route appears | Check internet, select both map points again, and retry later. The public FOSSGIS route service may be busy or may have no walkable OSM path between the selected points. |
| Phone cannot reach Ollama | Confirm both devices are on the same Wi-Fi, `OLLAMA_HOST` is set, Ollama was restarted, the LAN IP is current, and the private-network firewall rule allows the connection. |
| Model missing | Run `ollama pull gemma3:4b` on the computer hosting Ollama. |
| Offline verse appears | This is the expected fallback when Ollama is unavailable; it is generated from the built-in verse collection and does not require internet. |

## Tester feedback

When reporting an issue, include your Android version, whether you used an emulator or phone, the screen and action, and the visible error text. Do not include private location coordinates in public issue reports.
