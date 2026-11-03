---
name: update-changelog
description: Update the CHANGELOG.md and changelog files for Fastlane
argument-hint: [version name]
arguments: version
---

# Instructions

## build.gradle.tks

1. Increase by 1 the `versionCode` in `build.gradle.kts (:app)`
2. Set the `$version` as `versionName` in `build.gradle.kts (:app)`

## CHANGELOG.md

1. Replace the `versionName` from the `build.gradle.kts (:app)` at the top of `CHANGELOG.md`
2. For every new things since the last update, add a one-line description in the file. For each line, you must start by a keyword to categorize the change. For exemple:
    - feature: for a new feature or feature improvement
    - fix: for a bug fix
    - core: for a refactoring, internal change, or any other change in the core like dependencies update

## Fastlane changelog

1. Create a new file `<versionCode>.txt` in `fastlane/medatada` for `android` and `playstore`, inside their `en-US` folders
2. Add the content of `CHANGELOG.md` inside, except the first line with the `versionName`