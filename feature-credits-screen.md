Agis en tant qu'Architecte KMP. Lis mon AGENT.md. Je veux implémenter la fonctionnalité suivante : ajout d'un nouvel écran pour afficher la liste des oeuvres dans lesquelles une personne joue.

# Description de la feature

Grâce à feature/credits, nous pouvons obtenir la liste des membres du casting d'un film ou d'une série.

Je souhaiterais maintenant un nouvel écran qui affiche le détail d'un membre du casting (photo, noms, date de naissance...), ainsi que les oeuvres disponibles dans la bdd sur lesquelles apparaissent la personne.

# Informations techniques

## Appels API

Il faut ajouter un nouvel appel API pour récupérer les informations personnelles
- [https://api.themoviedb.org/3/movie/{artwork_id}/credits](https://api.themoviedb.org/3/person/{person_id})

Les specs pour les réponses API se trouvent dans ./credits_details.yml

## Structure de l'implémentation

- Les détails d'une personne doivent être implémentés dans un objet "PeopleDetails" dans le package features/credits.
- Il doit y avoir un nouveau use case nommé "GetPeopleDetailsUseCase", qui renvoie PeopleDetails à partir d'un People. Ce use case doit se trouver dans le package features/credits
- GetPeopleDetailsUseCase doit récupérer les données suivantes : 
  - Les détails de la personne grâce au nouvel appel API, grâce au People.id
  - Les les Artworks disponibles en BSS pour un selon les artworks id que l'on trouve pour l'id de la personne dans la table PeopleEntity
- Lorsque les informations depuis l'api ne sont pas disponibles (ex: pas de connection, erreur réseau, erreur API etc), on utilise seulement ce qui est disponible dans People, ainsi que les Artworks
- Il n'y a pas besoin de stocker PeopleDetails en BDD, un appel réseau pourra être fait à chaque fois que cela sera nécessaire.

## Structure de l'écran

- Le nouvel écran doit être dans le package features/credits/ui et être nommé PeopleScreen
- Il doit également y avoir les fichiers PeopleViewModel et PeopleContract, qui se chargeront de gérer les données et les actions de l'écran.
- Il faut créer une nouvelle Route.People pour la navigation
- Le nouvel écran doit avoir cette structure :
  - pour les écrans de smartphone : un colonne avec dans l'ordre la photo, les noms et la date de naissance, les différentes informations, la biographie et enfin les oeuvres disponibles dans une LazyRow
  - pour les écrans larges (ex: pliables ou tablette) : la photo à gauche, les noms date de naissance et différentes informations à droite, la biographie en dessous et enfin les oeuvres disponibles dans une LazyRow en dessous
- Lorsque les informations depuis l'api ne sont pas disponibles (ex: pas de connection, erreur réseau, erreur API etc), il ne faut d'on afficher que ce qui est disponible dans People, ainsi que les Artworks

# Informations supplémentaires

- N'hésite pas à faire des suggestions si tu vois des améliorations ou des oublis
