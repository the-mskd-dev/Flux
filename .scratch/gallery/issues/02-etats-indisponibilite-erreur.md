# 02: États d'indisponibilité et d'erreur

**What to build:** la Gallery gère ses états non nominaux. Pendant le chargement, un indicateur s'affiche. Sans connexion, elle affiche « Indisponible, vérifiez votre connexion » avec un bouton « Réessayer ». Une erreur réseau donne un état d'erreur avec « Réessayer », sans fermer la page, et les images déjà affichées restent visibles. Une liste vide donne un état vide.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] Indicateur de chargement affiché pendant la requête
- [ ] Hors connexion : message « Indisponible, vérifiez votre connexion » et bouton « Réessayer »
- [ ] « Réessayer » recharge la liste, y compris après le retour de la connexion
- [ ] Une erreur réseau donne un état d'erreur avec « Réessayer », la page reste ouverte
- [ ] Une liste vide affiche un état vide
- [ ] Ces états sont couverts par les tests du `GalleryViewModel`
