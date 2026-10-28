# 02: Reconnaître plus de titres d'Opening et d'Ending (anglais et japonais)

Spec : `../spec.md`

**What to build:** Le Chapter button propose « Passer l'opening » / « Passer l'ending » sur davantage de fichiers réels : les titres qui nomment la chanson avant OP/ED (« Song Name OP », « Song Name ED »), « Mixed OP », « Credits Start », ainsi que les titres japonais (オープニング, エンディング) et en pleine chasse (ＯＰ, ＥＤ). Chaque Chapter reste classé uniquement d'après son propre titre ; les exclusions du ticket 01 s'appliquent aussi aux nouveaux titres, et les noms génériques restent des Chapters ordinaires.

**Blocked by:** 01 (Ne plus proposer de passer les scènes post-générique ni les marqueurs de fin)

**Status:** ready-for-agent

- [ ] Deviennent OPENING : « Song Name OP », « Mixed OP », « オープニング », « ＯＰ »
- [ ] Deviennent ENDING : « Song Name ED », « Credits Start », « エンディング », « ＥＤ »
- [ ] Les termes japonais et pleine chasse sont reconnus littéralement, sans dépendre du `\W` ASCII de Java
- [ ] Restent CHAPTER : « Endgame », « Introspection », « Credit Union Heist », « Tired », « Shop », « Chapter 01 »
- [ ] Les exclusions restent prioritaires : « Song Name OP End » et « Credits Start End » → CHAPTER ; « Post-Credits » → CHAPTER
- [ ] Tous les tests du ticket 01 passent toujours
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
