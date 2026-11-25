# Keep androidx.activity.enableEdgeToEdge() despite Play Console deprecated-API warnings

Flux calls `androidx.activity.enableEdgeToEdge()` on its Android activities (`MainActivity`, `CrashDialogActivity`) to provide transparent edge-to-edge system bars and display cutout handling down to `minSdk = 29`, even though Google Play Console's static bytecode check flags `Window.setStatusBarColor`, `Window.setNavigationBarColor`, and `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` inside `androidx.activity`'s internal compatibility classes (`EdgeToEdgeApi28`, `EdgeToEdgeApi29`, `EdgeToEdgeApi35`).

## Considered Options

- **Remove `enableEdgeToEdge()` and call API 30+/35+ window APIs manually**: clears the informational Play Console warning, but leaves opaque or colored system bars on Android 10–14 (API 29–34) and drops display cutout support on API 29.

## Consequences

- Flux's own Kotlin code and XML themes must never call `setStatusBarColor`, `setNavigationBarColor`, or `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` directly; `enableEdgeToEdge()` manages them.
- System bar icon contrast and `window.isNavigationBarContrastEnforced` are controlled centrally through `SystemBarsAppearanceState` and `enableEdgeToEdge(statusBarStyle, navigationBarStyle)` rather than manual window color setters.
- Google Play Console's informational static warning pointing to `androidx.activity`'s obfuscated `EdgeToEdgeApi*` classes is an expected false positive and does not block release.
