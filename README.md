[![GitHub Sponsors](https://img.shields.io/github/sponsors/the-mskd-dev?style=for-the-badge)](https://github.com/sponsors/the-mskd-dev/)
[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://www.buymeacoffee.com/the.masked.dev)
[![Downloads](https://img.shields.io/github/downloads/the-mskd-dev/Flux/total)](https://github.com/the-mskd-dev/Flux/releases)
[![F-Droid Version](https://img.shields.io/f-droid/v/com.mskd.flux)](https://f-droid.org/ko/packages/com.mskd.flux/)
[![X (formerly Twitter) Follow](https://img.shields.io/twitter/follow/themskddev?style=for-the-badge)](https://x.com/themskddev)

---

# Flux

**Flux** is a modern, open-source media library and video player for Android. It scans your local video files, fetches rich metadata (posters, descriptions, genres...) from [The Movie Database (TMDB)](https://www.themoviedb.org/), and organises everything into a clean, browsable library. **Here you can find the completely free, ad-free, and tracker-free version.**

> **Platform status:** Flux currently ships on **Android**. The shared module is structured for Kotlin Multiplatform, I will probably make a Desktop and iOS support in the future.

---

## Features

- 📂 **Local library** - Browse your movies, TV shows, and anime in one place
- 🎬 **TMDB metadata sync** - Automatic posters, descriptions, genres, and ratings
- ▶️ **Built-in player** - Smooth playback with persistent progress history
- ▶️ **External player support** - If you don't like my player, you can use another one
- 🆓 **Truly free** - No ads, no trackers, no account required
- 📦 **Available on F-Droid** - Fully FOSS build with no proprietary dependencies

---

## Tech Stack

| Category | Library / Tool |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose · Material 3 Expressive |
| Architecture | MVI + Clean Architecture + KMP |
| Networking | Ktor · kotlinx.serialization |
| Dependency Injection | Koin |
| Database | Room · DataStore |
| Video Player | Media3 / ExoPlayer |
| Image Loading | Coil |
| Crash Reporting | ACRA (FOSS) · Firebase Crashlytics (Play Store) |
| Metadata API | TMDB |
| Testing | Kotest · MockK · Turbine |

---

## Build Flavors

Flux uses a `distribution` flavor dimension to produce two distinct variants:

| Flavor | Distribution | Crash Reporting |
|---|---|---|
| `foss` *(default)* | F-Droid | ACRA (open-source) |
| `playstore` | Google Play | Firebase Crashlytics + Analytics |

The `foss` flavor automatically disables all Google Services and Firebase tasks at build time, making it fully reproducible and compatible with F-Droid's build server requirements.

---

## File Naming Conventions

Flux uses your file names to identify and match content against TMDB. Follow the conventions below for best results.

### Movies

Include the title and, optionally, the release year:

```
Spider-Man (2002).mkv
Your Name.avi
Spider-Man.No.Way.Home.(2021).mp4
```

### TV Shows

Include the season and episode numbers in any of the supported formats:

```
show_name_s01e02.mkv
show_name_s01.e02.mkv
show_name_1x02.mkv
show_name (1995)_s01e02.mkv

# Folder-based naming is also supported:
show name/s01e02.mkv
show name/Season 1/02.mkv
```



## License

[![GPL-3.0-or-later](https://img.shields.io/badge/License-GPL--3.0--or--later-blue.svg)](https://spdx.org/licenses/GPL-3.0-or-later.html)

Copyright (C) 2026 the-mskd-dev

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

See the full license in the [LICENSE](LICENSE) file.
