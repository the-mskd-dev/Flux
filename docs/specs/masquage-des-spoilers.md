# Spec : Masquage des spoilers

## Problem Statement

Quand je parcours la page d'un Show que je n'ai pas fini, je vois en clair la description et l'image des Episode que je n'ai pas encore regardés, ainsi que le résumé des Season que je n'ai pas commencées. Ces textes et ces images (issus de TMDB) révèlent souvent des éléments d'intrigue : morts de personnages, retournements, révélations de fin de saison. Je ne peux pas consulter la liste des Episode pour savoir où j'en suis ou lancer le suivant sans risquer de me faire spoiler.

Par ailleurs, le bloc de description en haut de la page d'un Show alterne aujourd'hui entre deux pages (Episode en cours / Season) au tap, ce qui rend son contenu peu prévisible.

## Solution

Une option facultative **Masquage des spoilers**, désactivée par défaut et activable dans les Settings. Lorsqu'elle est active, la page d'un Artwork rend illisibles (floutés) les **Spoiler** :

- la description et l'image (vignette) d'un Episode à voir ;
- la description d'une Season dont tous les Episode sont à voir.

Tout le reste reste lisible : titres, numéros de Season et d'Episode, genres, Status, durée, progression, etc. Les Movie, la description du Show lui-même et l'Unknown Artwork n'ont jamais de Spoiler.

L'utilisateur peut à tout moment révéler un Spoiler en touchant le texte flouté (et le masquer à nouveau en le touchant une seconde fois), ou en dépliant la description (« Lire la suite »). Dès qu'un Media est commencé ou vu, son contenu n'est plus un Spoiler et devient lisible automatiquement.

Indépendamment de l'option, le bloc de description en haut de la page d'un Artwork n'a plus qu'une seule page :

- pour un Movie : la description du Movie ;
- pour un Show : la description de l'Episode en cours s'il y en a un, sinon la description de la Season.

## User Stories

1. As a viewer, I want an option in the Settings to enable the Masquage des spoilers, so that I can browse my Shows without reading what happens in upcoming Episode.
2. As a viewer, I want the Masquage des spoilers to be disabled by default, so that the app behaves as before unless I explicitly opt in.
3. As a viewer, I want my Masquage des spoilers choice to be remembered across app restarts, so that I don't have to enable it every time.
4. As a viewer, I want the Artwork page to react immediately when I toggle the option, so that I don't need to leave and reopen the page.
5. As a viewer with the option enabled, I want the description of every Episode I haven't started to be blurred, so that I am not spoiled by its synopsis.
6. As a viewer with the option enabled, I want the image of every Episode I haven't started to be blurred, so that a still frame doesn't reveal a plot point.
7. As a viewer with the option enabled, I want the Episode title, number, duration and Status to remain readable, so that I can still navigate and pick the Episode to watch.
8. As a viewer with the option enabled, I want the description of an Episode I am currently watching to be readable, so that I can remember what it is about.
9. As a viewer with the option enabled, I want the description and image of an Episode I have already watched to be readable, so that I can look back at past Episode.
10. As a viewer with the option enabled, I want the description of a Season I haven't started at all to be blurred, so that the Season summary doesn't spoil its story arc.
11. As a viewer with the option enabled, I want the description of a Season in which at least one Episode is started or watched to be readable, so that I'm not blocked once I've begun it.
12. As a viewer, I want the description of the Show itself to never be blurred, so that I can always know what the Show is about.
13. As a viewer, I want the description of a Movie to never be blurred, so that I can decide whether I want to watch it.
14. As a viewer, I want Episode belonging to the Unknown Artwork to never be blurred, so that unmatched files are displayed normally.
15. As a viewer, I want to tap a blurred description to reveal it, so that I can read it when I choose to.
16. As a viewer, I want to tap a revealed Spoiler description to blur it again, so that I can hide it after an accidental reveal.
17. As a viewer, I want tapping a blurred Episode description to reveal it without starting playback, so that revealing never launches a video by surprise.
18. As a viewer, I want revealing an Episode description to also reveal its image, so that one gesture reveals the whole Episode.
19. As a viewer, I want tapping an Episode image to still start playback, even if it is blurred, so that the main action of the list keeps working.
20. As a viewer, I want tapping the description of a non-Spoiler Episode to keep its current behaviour, so that nothing changes when the option is irrelevant.
21. As a viewer, I want expanding an Episode description with "Read more" to reveal it (description and image), so that reading the full text is never blocked by the blur.
22. As a viewer, I want collapsing an expanded Episode description to blur it again unless I had revealed it by tapping, so that the reveal only lasts as long as I asked for it.
23. As a viewer, I want the revealed state of Spoiler to survive scrolling the Episode list, so that a revealed Episode doesn't blur again when it leaves and comes back on screen.
24. As a viewer, I want all Spoiler to be blurred again when I come back to an Artwork page later, so that a past reveal doesn't permanently expose content.
25. As a viewer, I want the blur to fade in and out smoothly when revealing or hiding, so that the transition feels polished.
26. As a viewer, I want a Spoiler to become readable automatically (with the same smooth transition) when its Episode becomes started or watched — after playback or after "Mark as watched" — so that I don't have to reveal it manually.
27. As a viewer, I want a Spoiler to become blurred again when I reset its Progress back to "to watch", so that the rule stays consistent with the Status.
28. As a viewer using TalkBack, I want a blurred description to be announced as a hidden spoiler rather than read aloud, so that the screen reader doesn't spoil me.
29. As a viewer using TalkBack, I want an accessibility action to reveal a hidden spoiler, so that I can read it when I choose to.
30. As a viewer on Android 10 or 11, I want Spoiler to be effectively hidden, so that the option works on every supported device.
31. As a viewer, I want the description block at the top of a Movie page to show the Movie description only, so that the content is predictable.
32. As a viewer, I want the description block at the top of a Show page to show the description of the Episode I'm currently watching, so that I can quickly remember where I left off.
33. As a viewer, I want the description block at the top of a Show page to show the Season description when no Episode of the Season is in progress, so that I get an overview of the Season.
34. As a viewer, I want the description block to fall back to the Show description when the Season has no description, so that the block is never empty — and never blurred in that case.
35. As a viewer, I want the description block not to switch pages when tapped anymore, so that I never land on unexpected content.
36. As a viewer with the option enabled, I want the description block of a fully unwatched Season to be blurred and revealable by tapping its text, so that it behaves like the Episode list.
37. As a viewer, I want unavailable Episode to keep their current greyed-out look in addition to the blur, so that both pieces of information remain visible.

## Implementation Decisions

**Vocabulaire** — `GLOSSARY.md` définit désormais **Spoiler** et **Masquage des spoilers** ; le code et l'UI utilisent ces termes (éviter « spoil », « anti-spoiler » ; « blur » n'est employé que pour la technique de rendu).

**Règle « est-ce un Spoiler ? »** (uniquement lorsque l'option est active) :
- Un Episode est un Spoiler si son Status est `TO_WATCH` et qu'il n'appartient pas à l'Unknown Artwork. Sa description et son image sont alors masquées.
- La description d'une Season est un Spoiler si tous les Episode de cette Season ont le Status `TO_WATCH`. Si au moins un Episode est en cours ou vu, elle est lisible.
- Les Movie, la description du Show (y compris lorsqu'elle sert de repli à une Season sans description) et l'Unknown Artwork ne sont jamais des Spoiler.
- Cette règle est une fonction pure du domaine (Status + appartenance), sans dépendance à Compose.

**Bloc de description de la page Artwork** — le composant de description paginée est remplacé par un composant à page unique, renommé `ArtworkOverview` :
- Movie : description du Movie (avec genres et détails comme aujourd'hui).
- Show : si le Media sélectionné de la Season est `IS_WATCHING`, la description de cet Episode (avec ses détails Season/Episode) ; sinon la description de la Season, ou à défaut celle du Show.
- Conséquence : dans ce bloc, seule la description d'une Season entièrement à voir peut être un Spoiler (l'Episode affiché est toujours en cours).
- La Card n'est plus cliquable pour changer de page. Ce changement s'applique que l'option soit active ou non.
- Le cas « Season partiellement vue sans aucun Episode `IS_WATCHING` » affiche la description de la Season (non masquée), contrairement au comportement actuel qui affichait le prochain Episode à voir.

**État « révélé »** :
- Un seul état révélé par élément (bloc de description ou Episode), couvrant à la fois la description et l'image.
- Un élément est lisible si : ce n'est pas un Spoiler, OU il a été révélé au tap, OU il est déplié (expanded).
- Replier un élément le masque à nouveau sauf s'il a été révélé au tap.
- L'état révélé est éphémère : détenu par l'`ArtworkViewModel` (au même titre que l'Episode déplié) pour survivre au recyclage de la liste, jamais persisté, réinitialisé en quittant la page.
- Nouveaux intents sur le contrat Artwork pour basculer révélé/masqué d'un Episode et du bloc de description.
- L'`ArtworkContent` exposé à l'UI porte, déjà résolus, le contenu du bloc de description et pour chaque élément s'il est actuellement masqué ; l'UI ne recalcule pas la règle.

**Interactions** :
- Tap sur le texte d'une description masquée → révèle ; tap sur le texte d'un Spoiler révélé → masque. Ce tap est consommé (pas de lecture, pas d'autre action).
- Tap sur le texte d'une description qui n'est pas un Spoiler → comportement actuel inchangé.
- Tap sur l'image d'un Episode (masquée ou non) → lecture, comme aujourd'hui.
- « Lire la suite » reste disponible sur un texte masqué qui déborde ; déplier révèle.

**Rendu** :
- Nouveau modifier de flou de contenu, à côté des extensions de flou existantes basées sur Haze, utilisant l'entrée « contenu » de Haze (le composable floute son propre contenu, sans paire source/effet) et forçant l'activation du flou pour bénéficier du délégué RenderScript de Haze sur API 29-30.
- Le même modifier s'applique au texte de description et à l'image d'un Episode, dans les variantes Large et Small de l'item Episode, et au bloc de description.
- Le rayon de flou est animé (~300 ms) entre masqué et lisible, que le changement vienne d'un tap, d'un dépli ou d'un changement de Status.
- Si le délégué RenderScript ne fonctionne pas avec l'entrée « contenu » sur API 29-30, le voile de repli de Haze est accepté.
- Le grisage des Episode Unavailable est conservé et se cumule avec le flou.

**Accessibilité** :
- Tant qu'une description est masquée, sa sémantique est remplacée par un libellé « Spoiler masqué » (localisé) avec une action d'accessibilité « Révéler ».
- La `contentDescription` de la vignette est conservée (elle ne contient que le titre, qui n'est pas un Spoiler).

**Settings** :
- Nouvelle préférence booléenne dans le DataStore des Settings (clé `blur_spoilers`, défaut `false`), exposée dans l'état des Settings et dotée d'un setter.
- Un switch dans la section Personnalisation de l'écran Settings, avec titre et sous-titre explicatif localisés.
- L'`ArtworkViewModel`, qui observe déjà le DataStore des Settings, combine cette préférence dans le calcul de son état.

## Testing Decisions

- Un bon test ne vérifie que le comportement observable : on pilote les entrées publiques (préférence, Status des Media, intents) et on vérifie l'état exposé. Aucun test sur une fonction interne, une classe privée ou la structure des composables.
- **Seam principal : l'état exposé par l'`ArtworkViewModel`.** La règle Spoiler est testée à travers lui, sans test unitaire séparé. Cas à couvrir :
  - option désactivée → rien n'est masqué, quels que soient les Status ;
  - Episode `TO_WATCH` masqué, `IS_WATCHING` et `WATCHED` lisibles ;
  - Season entièrement `TO_WATCH` → bloc de description masqué ; Season avec au moins un Episode non `TO_WATCH` → lisible ;
  - Movie `TO_WATCH` → jamais masqué ;
  - Unknown Artwork → jamais masqué ;
  - repli sur la description du Show → jamais masqué ;
  - contenu du bloc de description : Movie → Movie ; Show avec Media sélectionné `IS_WATCHING` → Episode ; Show sans Episode en cours (entièrement à voir, partiellement vu, entièrement vu) → Season ;
  - intent révéler puis masquer sur un Episode et sur le bloc de description ;
  - déplier un Episode masqué → lisible ; replier → de nouveau masqué ; révélé au tap puis déplié puis replié → reste lisible ;
  - changement de Status (via le flux observé) d'un Episode masqué vers `IS_WATCHING` → lisible ;
  - basculer la préférence pendant que la page est ouverte → l'état se met à jour.
- Prior art : le test existant de l'`ArtworkViewModel` (Kotest FunSpec, Turbine, faux use case d'observation de l'Artwork, DataStore des Settings mocké exposant un flux d'état, mockups de Media).
- **Seams Settings existants** pour la nouvelle préférence uniquement : le test du DataStore des Settings (persistance, valeur par défaut `false`) et le test du ViewModel des Settings (l'intent du switch met à jour la préférence).
- **Pas de test automatisé du rendu** (flou, animation, sémantique TalkBack) : vérification manuelle sur un émulateur API 29 (repli RenderScript / voile) et un émulateur API 31+, ainsi qu'avec TalkBack activé.
- Commande : `./gradlew :app:testFossDebugUnitTest`.

## Out of Scope

- La page d'un Show hors page Artwork (liste des Season, aperçu d'une Season à l'appui long).
- L'Artwork image en haut de la page, les Poster, Backdrop et la Gallery.
- Les autres écrans affichant des Media : accueil / « continuer à regarder », recherche, page d'une Person, lecteur (y compris le Chapter button « épisode suivant »).
- Les titres d'Episode, même s'ils peuvent être révélateurs.
- La persistance de l'état révélé entre deux visites de la page.
- Un réglage du niveau de flou ou un choix de ce qui est masqué (description seule, image seule).

## Further Notes

- `GLOSSARY.md` a été mis à jour pendant la conception (Spoiler, Masquage des spoilers).
- Le glossaire contient par ailleurs des doublons contradictoires sans lien avec cette fonctionnalité (Sync / Synchro, Catalog / Catalogue, Media / Média) qui mériteraient un nettoyage séparé.
- Haze 2.0.1 désactive le flou par défaut sous API 31 ; forcer son activation passe par un délégué RenderScript (API dépréciée mais fonctionnelle sur API 29-30). À valider sur appareil avant de conclure.
