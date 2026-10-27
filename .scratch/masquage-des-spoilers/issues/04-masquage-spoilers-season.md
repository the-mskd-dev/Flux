# 04: Masquage des spoilers de la Season dans ArtworkOverview

**What to build:** Lorsque le Masquage des spoilers est actif, la description d'une Season dont tous les Episode sont à voir est floutée dans le bloc `ArtworkOverview` en haut de la page d'un Artwork. Dès qu'au moins un Episode de la Season est en cours ou vu, elle est lisible. Le repli sur la description du Show (Season sans description) et la description d'un Movie ne sont jamais floutés. L'utilisateur révèle / masque la description en touchant son texte, avec la même transition et la même sémantique TalkBack que pour les Episode.

Voir la spec `docs/specs/masquage-des-spoilers.md` (Règle « est-ce un Spoiler ? », Bloc de description).

**Blocked by:** 01 — ArtworkOverview : bloc de description sur une seule page ; 03 — Révéler / masquer un Episode

**Status:** ready-for-agent

- [ ] `ArtworkContent` expose si le bloc de description est masqué.
- [ ] Règle : masqué si l'option est active, que le bloc affiche la description d'une Season et que tous les Episode de cette Season sont `TO_WATCH`.
- [ ] Jamais masqué : Movie, repli sur la description du Show, Unknown Artwork, Season avec au moins un Episode non `TO_WATCH`.
- [ ] Nouvel intent pour basculer révélé / masqué du bloc de description, état détenu par l'`ArtworkViewModel`, réinitialisé en quittant la page.
- [ ] Tap sur le texte masqué → révèle ; tap sur le texte d'un Spoiler révélé → masque ; mêmes modifier de flou, transition animée et sémantique « Spoiler masqué » / « Révéler » que les Episode.
- [ ] Le dépli de la description (« Lire la suite ») révèle ; le repli masque à nouveau sauf révélation au tap.
- [ ] Tests de l'`ArtworkViewModel` : Season entièrement `TO_WATCH` → masqué ; Season avec un Episode `IS_WATCHING` ou `WATCHED` → lisible ; repli Show → lisible ; Movie `TO_WATCH` → lisible ; option désactivée → lisible ; révéler puis masquer.
- [ ] `./gradlew :app:testFossDebugUnitTest` passe.
