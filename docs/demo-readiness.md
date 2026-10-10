# GitHub demo readiness

## Included

- [x] Android debug APK build and install instructions.
- [x] Local card collection, field journal, share action, and practice battles with selectable cards.
- [x] GPS trail distance tracking with permission prompts.
- [x] Live OpenStreetMap map with user-selected start/finish points and Google Maps walking directions.
- [x] GPS traces, distances, and completed walks saved locally and viewable from trail history.
- [x] Several offline haiku verses when Ollama is missing or unreachable.
- [x] First-run 3-step guide, decluttered Trail page, optional collapsed sample deck, and functional Naturedex/card navigation.
- [x] Practice battles with varied rivals, counter-attacks, restart, animated turns, and outcome overlay.
- [x] Ollama endpoint auto-check on Cards entry and retry on network return after initial configuration.
- [x] Additional field badges and card-count shortcut from Scout Log.
- [x] MIT license and privacy/network behavior documented.
- [x] Multiplayer UI, client, server module, and deployment compose files removed.

## Before asking others to try it

- [ ] Install the latest updated debug APK on a clean emulator and one physical phone.
- [ ] Walk outdoors and confirm permission prompts, map tiles, GPS accuracy, route line, distance, and finish/save behavior.
- [ ] Pick route start/finish on the map and verify the walking directions open the right endpoints in Google Maps.
- [ ] Finish a walk, tap it in Saved Walks, and confirm the stored GPS trace zooms into view and persists after restart.
- [ ] Save several observations with Ollama stopped; confirm offline haiku changes and cards persist after restart.
- [ ] Connect to Ollama once, then test generation and automatic reconnect after Wi-Fi is toggled; verify behavior when the Ollama host itself is stopped.
- [ ] Test photo selection/camera with Ollama available, and manually verify uncertain model suggestions.
- [ ] Exercise card sharing, Naturedex shortcut, Scout card navigation, battle card selection/swap, varied rival wins/losses, restart, and empty-deck guidance.
- [ ] Add screenshots and a short outdoor demo clip to the GitHub project page.
- [x] Set the GitHub repository description and topics from [the prepared About copy](GITHUB_ABOUT.md).
- [x] Provide a direct APK download link in the README for phone-only testers; replace the binary after each tested update.

The app store signing, permanent domain, server operations, account system, and public-service scaling checklist is intentionally out of scope for a GitHub demo.
