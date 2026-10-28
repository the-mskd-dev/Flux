# 01: ArtworkOverview — bloc de description sur une seule page

**What to build:** En haut de la page d'un Artwork, le bloc de description paginé (qui alternait au tap entre l'Episode en cours et la Season) est remplacé par un bloc `ArtworkOverview` à page unique, non cliquable. Ce changement s'applique à tous les utilisateurs, indépendamment du Masquage des spoilers.

Contenu affiché :
- Movie : la description du Movie, avec ses genres et ses détails.
- Show : si le Media sélectionné de la Season a le Status en cours (`IS_WATCHING`), la description de cet Episode (avec ses détails Season / Episode) ; sinon la description de la Season, ou à défaut celle du Show (« pas de résumé » si aucune).

Le choix du contenu est résolu par l'`ArtworkViewModel` et exposé dans `ArtworkContent` ; l'UI se contente de l'afficher.

Voir la spec `docs/specs/masquage-des-spoilers.md` (Implementation Decisions → Bloc de description).

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Le composant paginé est renommé `ArtworkOverview` et n'affiche plus qu'une seule page ; la Card ne change plus de contenu au tap.
- [ ] `ArtworkContent` expose le contenu du bloc de description (Movie, Episode ou Season / repli Show), résolu par l'`ArtworkViewModel`.
- [ ] Movie → description du Movie.
- [ ] Show avec Media sélectionné `IS_WATCHING` → description de cet Episode.
- [ ] Show dont la Season est entièrement à voir, partiellement vue sans Episode en cours, ou entièrement vue → description de la Season.
- [ ] Season sans description → description du Show.
- [ ] Les variantes Regular et Large de la page Artwork utilisent `ArtworkOverview` ; les previews sont mises à jour.
- [ ] Tests de l'`ArtworkViewModel` couvrant chacun des cas ci-dessus (prior art : test existant de l'`ArtworkViewModel`, Kotest + Turbine + faux use case d'observation).
- [ ] `./gradlew :app:testFossDebugUnitTest` passe.
