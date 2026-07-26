# HomeArcade TV

**Android TV client for HomeArcade** — the premium retro gaming frontend for Home Assistant.

Play your ROM library on the big screen. Point the app at your HomeArcade server, grab a controller, and launch straight into EmulatorJS-powered games — no Home Assistant dashboard required.

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="120" alt="HomeArcade TV icon" />
</p>

## Features

- **Direct connection** — enter your HomeArcade server IP + port, play without the Home Assistant UI
- **Fullscreen emulator** — WebView-powered EmulatorJS with video fullscreen support
- **TV-optimized** — D-pad navigation, focus indicators, auto-hide UI elements
- **Game controller support** — Bluetooth/wired gamepads work natively via Gamepad API passthrough
- **Auto-reconnect** — monitors server health every 15s, shows error overlay with auto-retry
- **Persistent config** — server address saved to DataStore, skips setup on subsequent launches
- **Splash screen** — branded startup while configuration loads
- **Material Design 3** — dark theme with TV Material components from `androidx.tv:tv-material:1.1.0`

## Architecture

```
Native Compose UI (Setup/Config)  ──→  Fullscreen WebView (Gameplay)
       │                                      │
       │  SetupViewModel                      │  TVKeyBridge (D-pad → JS)
       │  DataStore                           │  ConnectionMonitor (/api/health)
       │  TV Material 3                       │  GamepadPoller (InputManager)
       └──────────────────────────────────────┘
                JS Bridge (Toast, Exit)
```

### Tech stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose for TV (`androidx.tv:tv-material:1.1.0`) |
| Navigation | `navigation-compose` (3 screens: Splash → Setup → Home) |
| State | `ViewModel` + `StateFlow` |
| Persistence | DataStore Preferences |
| Web | WebView with JavaScript bridge |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

## Getting started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest)
- Android TV device or emulator (API 24+)
- A running [HomeArcade-HA](https://github.com/JMaroz/HomeArcade-HA) instance with:
  - Port exposed on the host network (`5000/tcp: 9876` in `config.yaml`)

### Build & run

```bash
# Clone
git clone https://github.com/JMaroz/HomeArcade-TV.git
cd HomeArcade-TV

# Open in Android Studio — File → Open → select folder
# Let Gradle sync, then Run on your TV/emulator
```

### First-time setup

1. Launch the app → **Splash Screen** (1.5s)
2. Enter your **HomeArcade server IP** and **port** (e.g. `192.168.1.50` : `9876`)
3. Tap **Test Connection** → confirms server is reachable
4. Tap **Launch** → fullscreen WebView loads HomeArcade

## Project structure

```
HomeArcade-TV/
├── app/
│   ├── src/main/
│   │   ├── java/com/homearcade/tv/
│   │   │   ├── MainActivity.kt          # Entry point + system UI
│   │   │   ├── data/
│   │   │   │   ├── ServerConfig.kt       # Host + port model
│   │   │   │   └── ServerRepository.kt   # DataStore persistence
│   │   │   ├── navigation/
│   │   │   │   └── NavGraph.kt           # 4 routes with nav args
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── SplashScreen.kt   # Branded loading
│   │   │   │   │   ├── SetupScreen.kt    # Server config input
│   │   │   │   │   ├── HomeScreen.kt     # WebView + overlay + monitor
│   │   │   │   │   ├── SettingsScreen.kt # Edit server address
│   │   │   │   │   ├── ErrorOverlay.kt   # Connection error + retry
│   │   │   │   │   └── IpInputField.kt   # TV-friendly text input
│   │   │   │   └── theme/
│   │   │   │       ├── Color.kt          # Pink/dark palette
│   │   │   │       ├── Type.kt           # Monospace typography
│   │   │   │       └── Theme.kt          # TV Material 3 theme
│   │   │   ├── viewmodel/
│   │   │   │   └── SetupViewModel.kt     # Connection + state mgmt
│   │   │   └── webview/
│   │   │       ├── HomeArcadeWebViewClient.kt  # Page lifecycle + JS injection
│   │   │       ├── HomeArcadeChromeClient.kt   # Fullscreen video
│   │   │       ├── TVKeyBridge.kt              # D-pad → evaluateJavascript
│   │   │       ├── JSBridge.kt                 # @JavascriptInterface
│   │   │       ├── GamepadPoller.kt            # InputManager → window.__tvGamepads
│   │   │       └── ConnectionMonitor.kt        # Health polling + callbacks
│   │   └── res/
│   │       ├── xml/network_security_config.xml  # HTTP for local IPs
│   │       ├── drawable/ic_launcher_foreground.xml
│   │       └── values/{strings,themes,colors}.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
└── .gitignore
```

## D-pad navigation

The app forwards D-pad key events from the TV remote into the WebView via `TVKeyBridge`:

1. `dispatchKeyEvent` / `setOnKeyListener` intercepts `DPAD_UP/DOWN/LEFT/RIGHT/CENTER`, `ENTER`, `BACK`, `MENU`
2. Injects `KeyboardEvent` via `evaluateJavascript()` → triggers EmulatorJS / HomeArcade menu handlers
3. HomeArcade's `cabinetSetupMenu()` (in `player.ts`) already handles ArrowUp/Down/Left/Right + Enter

## Game controllers

Physical gamepads are handled two ways:

- **Direct passthrough** — Android WebView may forward `navigator.getGamepads()` to EmulatorJS natively
- **Fallback poller** — `GamepadPoller` reads connected devices via `InputManager` and patches `window.__tvGamepads` so EmulatorJS detects them

## Related

- [HomeArcade-HA](https://github.com/JMaroz/HomeArcade-HA) — server-side app running on Home Assistant

## License

MIT
