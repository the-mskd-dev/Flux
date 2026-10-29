# 03: Révéler / masquer un Episode

**What to build:** Sur la page d'un Artwork, l'utilisateur peut révéler un Episode masqué en touchant le texte flouté de sa description, et le masquer à nouveau en le touchant une seconde fois. Ce tap ne lance pas la lecture. Révéler la description révèle aussi la vignette de l'Episode ; toucher la vignette lance toujours la lecture. Déplier la description (« Lire la suite ») la révèle ; la replier la masque à nouveau, sauf si l'Episode avait été révélé au tap. L'état révélé survit au scroll de la liste et est réinitialisé en quittant la page. Avec TalkBack, une description masquée est annoncée « Spoiler masqué » avec une action « Révéler ».

Voir la spec `docs/specs/masquage-des-spoilers.md` (État « révélé », Interactions, Accessibilité).

**Blocked by:** 02 — Masquage des spoilers des Episode (réglage + flou)

**Status:** ready-for-agent

- [ ] Nouvel intent sur le contrat Artwork pour basculer révélé / masqué d'un Episode ; l'état révélé est détenu par l'`ArtworkViewModel` (comme l'Episode déplié), jamais persisté.
- [ ] Un Episode est lisible s'il n'est pas un Spoiler, OU s'il a été révélé au tap, OU s'il est déplié.
- [ ] Tap sur le texte d'une description masquée → révèle (description + vignette), sans lecture ; tap sur le texte d'un Spoiler révélé → masque, sans lecture.
- [ ] Tap sur le texte d'une description qui n'est pas un Spoiler, ou tap sur la vignette → comportement actuel (lecture) inchangé.
- [ ] « Lire la suite » reste disponible sur un texte masqué qui déborde ; déplier révèle, replier masque à nouveau sauf révélation au tap.
- [ ] La révélation et le masquage utilisent la transition animée du ticket 02.
- [ ] Sémantique d'une description masquée : libellé « Spoiler masqué » (localisé) et action d'accessibilité « Révéler » ; la `contentDescription` de la vignette est conservée.
- [ ] S'applique à `EpisodeItemLarge` et `EpisodeItemSmall`.
- [ ] Tests de l'`ArtworkViewModel` : révéler puis masquer ; déplier un Episode masqué → lisible ; replier → masqué ; révélé au tap puis déplié puis replié → reste lisible ; un nouvel `ArtworkViewModel` repart sans Episode révélé.
- [ ] Vérification manuelle avec TalkBack.
- [ ] `./gradlew :app:testFossDebugUnitTest` passe.
