# 01: Ne plus proposer de passer les scènes post-générique ni les marqueurs de fin

Spec : `../spec.md`

**What to build:** Pendant la lecture, le Chapter button ne propose plus de passer un Chapter qui est une scène post-générique (« Post-Credits », « Post Credit Scene », « Credits Scene ») ou un simple marqueur de fin (« OP End », « Opening End », « Ending: End ») : ces Chapters deviennent des Chapters ordinaires. Dans un film avec plusieurs génériques entrecoupés de scènes post-générique, chaque générique reste un Ending que l'on peut passer, et chaque scène post-générique est lue. Le comportement actuel pour tous les autres titres est figé par des tests de non-régression, qui servent de filet pour le ticket suivant.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] `VideoChapterType.fromString` est couvert par des tests pilotés par les données (Kotest `FunSpec` + `withData`, comme les tests existants des use cases), titre → type attendu
- [ ] Non-régression : « OP », « OP1 », « Opening », « NCOP », « Intro », « Introduction » → OPENING ; « ED », « ED2 », « Ending », « NCED », « Outro », « Credits », « Credit », « End Credits », « End » → ENDING ; la casse est ignorée
- [ ] Restent CHAPTER : `null`, titre vide, « Chapter 01 », « 01 », « Opera », « Edward », « Prologue », « Avant », « Part A », « Preview », « PV », « Recap »
- [ ] Deviennent CHAPTER : « OP End », « Opening End », « Ending: End », « ED End », « Post-Credits », « Post Credits », « Post Credit Scene », « Credits Scene »
- [ ] Les exclusions sont évaluées avant la reconnaissance Opening / Ending
- [ ] Aucun changement de `VideoChapter`, de la lecture des chapitres ni du Chapter button
- [ ] `./gradlew :app:testFossDebugUnitTest` passe
