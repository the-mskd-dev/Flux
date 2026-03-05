# Flux
## _Local shows/movies/animes library and player_

DISCLAIMER: This app is mainly used for my own personal use, and as a sandbox for Android Development.

## Features

- Show your local files in a library
- Play your files as a media player and save your progress
- Get information thanks to TMDB
- Totally free and ad-free

## Tech

- Android : A full native Android app
- Koltin : Native Android language
- Jetpack Compose : To build the UI
- Material 3 Expressive : To get a fresh UI (I did my best)
- Jetpack Navigation 3 : New Navigation framework from Jetpack library
- Media3 : Media player
- TMDB API : To get medias information
- HILT : Dependencies injection
- Retrofit : HTTP requests
- Coil : Image managment
- Room : Database

## How to use

This app use your video files (>5min) and then use TMBD to get information.

To use TMDB, files need to have a specific formatted name

### Movies

Give your movies a clear name, followed by the year if you want. For example:

- Spider-man (2002).mkv
- Your name.avi
- Spider-man-no-way-home-(2021).mp4

### Shows

For TV show episodes, use a format that includes the season and episode number. For example:

- show name_s01e02.mkv
- show name_s01.e02.mkv
- show_name_1x02.mkv
- show_name_se1.ep2.mkv
- show_name-season1.episode2.mkv

## Development

If you want to create a fork of this app and use your own API Key, you have just to create a file local.properties and create a variable called tmdb_token with your API key

```
tmdb_token=<you_api_key>
```

And that all! The rest is up to you.

## License

Copyright (C) 2025 adyBct

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

See the full license in the [LICENSE](LICENSE) file.