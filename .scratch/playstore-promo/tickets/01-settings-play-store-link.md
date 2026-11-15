# 01: Play Store link in Settings (FOSS only)

**What to build:** On the FOSS flavor, the Settings page shows a "Play Store" item first in the app info section. It shows the colored Play Store logo on a white background (no tint), the title "Play Store" and the subtitle "Support the development", translated in the 7 locales (default English, fr, de, es, it, ja, ko). Tapping it opens the Play Store listing. The URL lives in a single constant in the contact group of the shared constants, built from the base application id plus the `.playstore` suffix, with a TODO to replace it with the real link. The Play Store flavor does not show the item.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] A constant holds the placeholder Play Store URL, with a TODO comment
- [ ] The item is first in the app info section on the FOSS flavor
- [ ] The item is hidden on the Play Store flavor
- [ ] The logo keeps its colors on a white background
- [ ] Title and subtitle are translated in all 7 locales
- [ ] Tapping the item opens the URL through the existing open-URL intent
- [ ] Checked manually on a FOSS build
