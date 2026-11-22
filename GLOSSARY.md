# Flux

Flux organizes the user's UserFile into a Catalog (enriched through TMDB) and plays them while remembering their Progress.

## Models

**Artwork**:
Movie or series as identified on TMDB, carrying the metadata, independently of the Media attached to it.
_Avoid_: Œuvre, Content, Title, Media, Affiche, poster

**ContentType**:
Nature of an Artwork: MOVIE (film) or SHOW (series).
_Avoid_: Media type, category

**Show**:
Artwork of type SHOW.
_Avoid_: Série, TV show

**Unknown Artwork**:
Special Show that gathers, as Episode, the Media whose UserFile could not be matched to any TMDB Artwork, films included.
_Avoid_: Others, Autres, Inconnu

**Media**:
Playable item attached to a UserFile: a Movie or an Episode.
_Avoid_: Vidéo, file, Média

**Movie**:
Media of an Artwork of type MOVIE: the playable item, not the work. A MOVIE Artwork has exactly one Movie.
_Avoid_: Film

**Episode**:
Media of a Show, identified by its season number and its episode number. A Show can have several Episode.

**Season**:
Grouping of the Episode of a Show by season number. An Episode can exist without a matching Season.

**Unavailable**:
Said of a Media whose UserFile is temporarily inaccessible (UserFolder disconnected, permission revoked): it stays in the Catalog but cannot be played.
_Avoid_: Deleted, missing, offline

**Status**:
Watching state of a Media: to watch, in progress or watched. The status of an Artwork is only the summary of that of its Media.
_Avoid_: State, watched/unwatched

**Progress**:
Playback position of a Media, updated during playback and remembered for resuming.
_Avoid_: History, currentTime, Position

**History**:
List of resumable Media shown on the home screen, ordered with the most recent first, with at most one entry per Artwork. Excludes Private Artwork, the Unknown Artwork, and Media from an Unavailable source.
_Avoid_: Resume list, Recently watched

**Genre**:
Genre of the Artwork.
_Avoid_: Type, category

**Backdrop**:
16:9 TMDB image of an Artwork, in landscape.
_Avoid_: Fanart, banner, bannière

**Poster**:
2:3 TMDB image of an Artwork, in portrait.
_Avoid_: Affiche, cover, jaquette

**Artwork page**:
Page of an Artwork, generic term for a Movie page, a Show page or a Season page.
_Avoid_: Media page, Detail page, Fiche

**Season page**:
Page of one Season of a Show, reached from the Show page, listing its Episode. It shows the Season summary, the Episode actions and their Spoiler masking, and always the Backdrop as Artwork image.
_Avoid_: Episodes page, Page saison

**Logo**:
TMDB image of an Artwork showing its stylised title, on a transparent background. It replaces the title text on the Artwork page when it is available. An Artwork has at most one shown Logo: the custom Logo if there is one, otherwise the one TMDB gives automatically.
_Avoid_: Title image, Wordmark, Titre graphique

**Artwork image**:
Image shown at the top of an Artwork page: its Backdrop, or its Poster when the blurred header is enabled. The blurred header is available for a Movie page and a Show page, not for a Season page, which always shows the Backdrop. It is the matching Custom image if there is one, otherwise the TMDB one. A tap opens it in the Gallery when a TMDB token is configured, except while it shows the selected Episode and that Episode is a Spoiler: the tap then reveals or hides the Episode.
_Avoid_: Header image, banner, bannière

**Custom image**:
Backdrop, Poster or Logo chosen by the user in the Gallery to replace the TMDB one. The custom Backdrop replaces the Backdrop of the Artwork image; the custom Poster replaces the Poster of the Artwork image (blurred header) and the Artwork thumbnail in the Catalog, the search and the Private Folder; the custom Logo replaces the Logo of the Artwork page. An Artwork, Movie or Show, has at most one custom Backdrop, one custom Poster and one custom Logo. It is kept by the Light sync and the Language change (while a custom Logo is set, Sync skips TMDB Logo lookups for that Artwork), and cleared by the Full sync and the Identification reset. A Manual identification does not carry over the one of the source Artwork; the target Artwork keeps its own if it was already in the Catalog.
_Avoid_: Image personnalisée, Affiche personnalisée, override

**Gallery**:
Page listing the TMDB images of an Artwork (Backdrop, Poster and Logo, in every Image language) in a single grid sorted by descending rating, with a zoomable full-screen viewer. The user can narrow the grid by image type and by Image language; the filter is not remembered. For a Show, only the images of the Show itself, not those of its Season or Episode. Nothing is stored locally.
_Avoid_: Galerie d'images, Images, Photos

**Image language**:
Language written on a TMDB image (a Poster or a Logo in Japanese, for instance), or Global when the image carries no text. It is independent of the data language chosen in the settings.
_Avoid_: Locale, Langue des données, No language, Sans langue

**Image Download**:
Copy, into the device's image storage, of a Gallery image in original resolution. It leaves Flux: the Catalog does not know about it.
_Avoid_: Download, Save, Export, Sauvegarde

**Spoiler**:
Description or image of an Episode to watch, or description of a Season whose Episode are all to watch. Movies, the Show itself and the Unknown Artwork have no Spoiler.
_Avoid_: Spoil

**Spoiler masking**:
Optional setting that makes the Spoiler unreadable on an Artwork page, until the user reveals them.
_Avoid_: Masquage des spoilers, Flou, blur, anti-spoiler

## People

**Person**:
Individual, identified by TMDB, who took part in one or more Artwork.
_Avoid_: People, Credit, intervenant

**Cast**:
All the Person who play a character in an Artwork.
_Avoid_: Actors, casting, distribution

**Crew**:
All the Person holding a technical position on an Artwork (directing, writing…).
_Avoid_: Team, staff

**Role**:
Character (for the Cast) or position (for the Crew) of a Person in an Artwork.
_Avoid_: Job, character, function

## Catalog

**Catalog**:
All the Artwork and Media known to Flux for the user.
_Avoid_: Catalogue, Bibliothèque, Library, Collection

**UserFile**:
Video file present on the user's device, at the origin of a Media.
_Avoid_: Local file, video

**UserFolder**:
Device folder that the user entrusted to Flux to look for UserFile in it.
_Avoid_: Source, directory

**Sync**:
Operation that aligns the Catalog with the UserFolder and TMDB data. There are three Sync types: Light sync, Full sync, and Language change.
_Avoid_: Synchro, Scan, refresh, import, matching

**Private Artwork**:
Artwork hidden from the Catalog, only reachable in the Private Folder; it stays private as long as it exists.
_Avoid_: Artwork privé, hidden Artwork

**Private Folder**:
PIN-protected space gathering the Private Artwork.
_Avoid_: Vault, hidden folder

## Player

**Internal player**:
Flux's own player, with Playback speed, Chapter buttons and Track selection.
_Avoid_: Built-in player

**External player**:
Video app chosen by the viewer in the settings that plays the Media in place of the Internal player, and reports the playback position back when closed.
_Avoid_: Third-party player

**Playback launch**:
Journey from "the viewer asked to play a Media" to "the Progress reported by an External player is remembered": choosing the player, falling back to the Internal player if the External player cannot start, and recording the reported Progress.

**Interface**:
Layer of player controls (top bar, playback buttons, progress bar) shown on tap and hidden automatically.
_Avoid_: Controls, UI, overlay

**Seek overlay**:
Short-lived indication of the jump performed (rewind or fast forward) and of its cumulated duration.
_Avoid_: Double-tap overlay

**Ambient overlay**:
Short-lived indication of the volume or brightness value during a vertical gesture.
_Avoid_: Volume overlay, brightness overlay

**Chapter**:
Named or numbered section of a Media, as described in its UserFile. A Chapter is either an Opening, an Ending, or an ordinary Chapter.
_Avoid_: Segment, marker, part

**Opening**:
Chapter matching the opening credits of a Media.
_Avoid_: Intro, OP, générique

**Ending**:
Chapter matching an end credits sequence of a Media. A Media can have several (credits, post-credits scene, second credits).
_Avoid_: Outro, ED, Credits, générique

**Chapter button**:
Contextual action offered during playback: skip an Opening or an Ending, or start the Next Episode.
_Avoid_: Skip button

**Next Episode**:
Episode that follows, in the same Show, the one being played (by season then number). A Movie, the last Episode of a Show and the Episode of the Unknown Artwork have none.
_Avoid_: Prochain épisode, Up next

**Settings sheet**:
Player settings sheet, either general or dedicated to choosing an audio track, a subtitle track, a Chapter or a Playback speed.
_Avoid_: Bottom sheet, menu

**Playback speed**:
Rate at which a Media plays, from 0.25x to 2x by steps of 0.25, chosen in the Settings sheet. Resets to 1x for each new Media.
_Avoid_: Rate, tempo

**Boost speed**:
Temporary 2x speed while the viewer presses and holds the video; a "2x" pill shows at the top, and the previous Playback speed is restored on release. Not available in Picture-in-Picture.
_Avoid_: Fast play, turbo

**Media info**:
Read-only technical description of a Media (container, video and audio codecs, resolution, frame rate, bitrate, channels, sampling rate), shown in the Settings sheet. A value that cannot be read shows as a placeholder.
_Avoid_: File info, Technical details, Codec info

**Track**:
Selectable audio or subtitle track of a Media.
_Avoid_: Stream, language

## Language

**Source**:
A location the user's files come from (device storage or custom folder).
_Avoid_: Provider, root folder

**Unavailable source**:
A custom Source that cannot be read for now; its data is kept.
_Avoid_: Source indisponible, deleted Source

**Missing file**:
A file known to the Catalog that no longer exists while its Source is available; its data is deleted.
_Avoid_: Fichier manquant, lost file, orphan file

**Light sync**:
A Sync that only processes the new files and removes the Missing files.
_Avoid_: Synchro légère, incremental sync, onlyNew

**Full sync**:
An explicitly requested Sync that fetches the TMDB data of every file again.
_Avoid_: Synchro complète, resync

**Language change**:
A Sync requested when the data language changes: it translates the Catalog, refreshes Genres, searches Logos again in the new language (skipping Artwork with a custom Logo), and runs the Image prefetch, without scanning the UserFolder, removing Missing files, or resetting Custom images or Manual identification.
_Avoid_: Language update, locale sync

**Sync progress**:
The completion percentage of a Sync, computed from the units of work declared by its Sync steps and Enrichments; it never goes backwards.
_Avoid_: Progression de synchro

**Sync step**:
A global processing of the Sync (scan, removal of Missing files, genres, catalog, translation, logos, finalization…) that declares its work, runs it and signals its progress.
_Avoid_: Étape de synchro, Fetcher, resolver

**Translation**:
A Sync step that runs during a Language change to update the title and description of every Movie, Show, Season and Episode in the Catalog (including Private Artwork and Unavailable Media, excluding the Unknown Artwork) into the selected data language, keeping previous values for missing or blank fields and recording no Failed file on lookup error.
_Avoid_: Language update, translation enrichment

**Image prefetch**:
Background caching of the Catalog's TMDB images (Poster, Backdrop, Custom image, Logo, Season and Episode images) triggered at the end of every Sync for offline display, without delaying the end of the Sync.
_Avoid_: Image cache, HD prefetch, Image Download

**Enrichment**:
A piece of data fetched for an Artwork during the catalog Sync step (identification, details, seasons, translations, credits, durations); adding a TMDB piece of data almost always means adding an Enrichment.
_Avoid_: Enrichissement, Resolver, metadata fetcher

**Failed file**:
A file whose TMDB fetch failed (network, API, missing token); nothing is saved and it is retried at the next Sync.
_Avoid_: Fichier en échec, unknown file, UNKNOWN

**Manual identification**:
Choice, by the user, of the TMDB Artwork that all the Media of an Artwork are attached to. It prevails over the TMDB ID in the name of a file (file name or folder path) and over the name search, including for the new files of a Light sync. It is cleared by an Identification reset or a Full sync, after which the TMDB ID in the name applies again. An Episode that TMDB does not list in a Season of the chosen Show stays in that Show, including the Unrecognized files of the same file-name title that the automatic identification left behind; the Episode of a Season the chosen Show does not have become Unrecognized files.
_Avoid_: Identification manuelle, Data selection, rematch, correction

**Identification reset**:
Clearing of the Manual identification of an Artwork: each of its files gets back the Artwork of the TMDB ID in its name, or else the one the name search gives it. The Status and the Progress of the Media are kept.
_Avoid_: Réinitialisation, Reset (alone, ambiguous with the progress reset), cancellation

**Unrecognized file**:
A file that TMDB cannot identify; it is saved as a Media with no known Artwork.
_Avoid_: Fichier non reconnu, Failed file
