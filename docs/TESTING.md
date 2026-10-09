# TrailDex: first-run tester tutorial

This guide walks through the demo from installation to a first practice battle. TrailDex is an Android demo; no account or server is needed.

## 1. Install the demo

### Build from source (recommended)

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

### Install a downloaded APK

When a GitHub Release is published, download its attached debug APK, open it on the Android phone, and allow installation from that source if Android asks. Debug APKs are for testing and are not signed as a Play Store release. Until then, build it from source using the steps above.

## 2. Take a walk and find a mapped route

1. Open **Trail** and tap **Start Trail**.
2. Grant location permission while using the app. On Android 13+, allow notifications so the active tracking notification can be shown.
3. Wait outdoors for a GPS fix; the screen reports coordinates and accuracy when available.
4. Tap **Find Trails Near Me**. This sends the current approximate GPS fix to the public OpenStreetMap Overpass API and looks for named walking/hiking route relations within 8 km.
5. Tap a result to open its OpenStreetMap relation page. Use your preferred map app for navigation; TrailDex does not give turn-by-turn directions.
6. Stop the trail when finished. Trail distance is stored on the device.

The route search requires internet. Results are cached by approximate area and can be shown offline after the first successful lookup. OSM coverage varies by location. Map data © OpenStreetMap contributors.

## 3. Create a field card without AI

1. Open **Cards** and enter an observation such as “crow”, “moss”, or “orange mushroom”.
2. Add a trail or habitat name.
3. Tap **Generate Field Haiku**. If Ollama is unavailable, TrailDex selects an offline verse from its built-in collection. The verse is a template, not model output.
4. Edit the observation or verse if you like, then tap **Save Field Card**.
5. Restart the app and confirm the card remains in your collection.

## 4. Optional Ollama photo and haiku generation

Ollama runs on a computer, not inside TrailDex or on the phone. The phone must be able to reach it over the local network.

1. Install Ollama on a computer and run `ollama pull gemma3:4b`.
2. By default, Ollama listens only on `127.0.0.1`. For a physical phone, set the computer's user environment variable `OLLAMA_HOST` to `0.0.0.0:11434`, fully quit and restart Ollama, and allow inbound port 11434 only on a trusted private network. The official [Ollama network configuration guide](https://docs.ollama.com/faq#how-can-i-expose-ollama-on-my-network) has OS-specific steps.
3. Find the computer's private LAN IPv4 address (for example, `192.168.1.25`). In TrailDex **Cards**, enter `http://192.168.1.25:11434` in **Ollama address** and tap **Check Ollama Connection**. Replace the example IP with the computer's actual address.
4. For an Android emulator on the same computer, use `http://10.0.2.2:11434` instead; this is the default in debug builds.
5. Describe an observation or choose/take a photo, then request generation. Read the model's suggestion carefully before saving; it can be incorrect or uncertain.

Keep this connection on a trusted local network. Do not expose Ollama to the public internet. If Ollama cannot be reached, card saving still works with an offline verse.

## 5. Battle with a collected card

1. Open **Battle**. Cards saved in the Cards page appear as your selectable deck.
2. Tap the card you want to use.
3. Use the available moves and the field pouch to battle the local practice bot.
4. Try a newly saved card to see its HP and power values affect practice combat.

Battles are local practice only. Online matchmaking and accounts are not part of this demo.

## 6. Share a card and review your field journal

Use the card's share action to send its text to an Android app that accepts shared text. The Scout page shows your saved field journal and local progress. TrailDex does not upload cards or trail history to a TrailDex server.

## Troubleshooting

| Problem | Try this |
| --- | --- |
| No device appears for `installDebug` | Start the Android emulator first, or enable USB debugging and accept the phone's computer prompt. Check `adb devices`. |
| GPS is still waiting | Go outdoors, enable phone location, grant precise location if available, and wait briefly for a fix. |
| No mapped routes found | Try a mapped area and check internet. Some areas have little OpenStreetMap route data. |
| Phone cannot reach Ollama | Confirm both devices are on the same Wi-Fi, `OLLAMA_HOST` is set, Ollama was restarted, the LAN IP is current, and the private-network firewall rule allows the connection. |
| Model missing | Run `ollama pull gemma3:4b` on the computer hosting Ollama. |
| Offline verse appears | This is the expected fallback when Ollama is unavailable; it is generated from the built-in verse collection and does not require internet. |

## Tester feedback

When reporting an issue, include your Android version, whether you used an emulator or phone, the screen and action, and the visible error text. Do not include private location coordinates in public issue reports.
