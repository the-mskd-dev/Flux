---
name: update-libraries
description: Update dependency versions in libs.versions.toml for the Android/KMP project, verify the build, and open a pull request. Use when the user asks to update dependencies, libraries or versions.
---

# Instructions

## 1. Find new versions

If your environment supports subagents, delegate this research. Launch several in parallel, one per family of libraries (AndroidX, Compose, Kotlin tooling, Ktor, etc.). Use the fastest and cheapest model available for them, and give each one the instructions from `references/researcher.md` as its prompt.

If subagents are not available, follow `references/researcher.md` yourself, library by library.

Verify any major upgrade yourself before applying it.

## 2. Version policy

- Use stable versions only.
- Use a release candidate (RC) only if the library has no stable release newer than the current version.
- Never use alpha or beta versions. If the current version is already an alpha or beta, update it only to a newer stable or RC, otherwise leave it.
- Do not apply major version upgrades silently. Apply them last, and flag them clearly in the report.

## 3. Update `libs.versions.toml`

Update in groups, so that a failure can be traced to a specific group. Verify (step 4) after each group.

1. Tightly coupled tools first: Kotlin, KSP, Compose compiler, Android Gradle Plugin, Gradle wrapper.
2. Then the remaining libraries, grouped by family (AndroidX, Compose, Ktor, etc.).

## 3b. Scan for impacted code

For each updated library with breaking or deprecated APIs listed in the research results, search the codebase for usages of those APIs. If subagents are available, delegate this to a cheap, read-only one using `references/usage-scanner.md` as its prompt. Use the results to know which files to fix in step 4.

## 4. Verify

After each group, run:

- `./gradlew :app:buildFossDebugUnitTest`
- `./gradlew :app:testFossDebugUnitTest`
- The compile tasks of every KMP target used by the project (Android, iOS, desktop, etc.).

You may delegate running these tasks to a cheap subagent, but it must return success or failure and the relevant error excerpts verbatim, with file and line. Do not accept paraphrased errors. Do not run builds in parallel on the same checkout.

If something fails:

- Fix it if the change is small and clearly related to the new version.
- Otherwise revert that library to its previous version and list it under "Not updated" in the report.

## 5. Report

Use these sections for each updated library:

- `library: old → new`
- `notable changes`
- `impact`
- `code changes required`

Then add:

- **Major upgrades**: libraries that changed major version.
- **Not updated**: libraries skipped or reverted, and why.

Keep it concise: this report becomes the pull request description.

## 6. Commit, push and pull request

Once the report is final, you may delegate the commands of this step to a cheap subagent, giving it the report and these instructions.

1. Never push to `main`. If you are on `main`, create a new branch first.
2. Commit with the message `[LIB] Update libraries`, then push.
3. Find the parent branch (the branch the current branch was created from) with `git reflog` or `git merge-base`. If it is ambiguous, ask the user before continuing.
4. Create the pull request with `gh pr create --base <parent-branch>`:
   - title: `[LIB] Update libraries`
   - description: the report from step 5