Migrations base de données (Flyway)
====================================

Les scripts SQL ne sont plus appliqués à la main depuis ce dossier. Chaque microservice
applique ses migrations au démarrage (table flyway_schema_history dans la base ciblée).

  • Auth (base ex. PFE_Data)
      backend/auth-service/src/main/resources/db/migration/
      Fichier initial : V1__init_auth_schema.sql

  • Candidats (base ex. data_candidats)
      backend/candidat-service/src/main/resources/db/migration/
      V1__init_candidat.sql — table candidat
      V2__candidat_concours_id_canonical.sql — unicité (numero_inscription, concours_id)

  • Concours (base ex. data_concours)
      backend/concours-service/src/main/resources/db/migration/
      V1__init_concours.sql
      V2__concours_centre_lien.sql — index centre_id, commentaires lien centre ↔ concours

  • Lieux — centres, établissements, salles (base ex. data_lieux)
      backend/lieux-service/src/main/resources/db/migration/
      V1__init_lieux.sql
      V2__lieux_concours_derives.sql — commentaires dérivation concoursIds (API)

Ordre d’exploitation
--------------------
  1. Créer les bases PostgreSQL vides (CREATE DATABASE …).
  2. Démarrer auth-service (Flyway crée utilisateur + comptes par défaut sur PFE_Data).
  3. Démarrer candidat-service (Flyway crée la table candidat sur data_candidats).
  4. Démarrer concours-service (Flyway crée concours + concours_affectation_centre sur data_concours).
  5. Démarrer lieux-service (Flyway crée centre, etablissement, salle sur data_lieux).

Les versions suivantes : ajouter V2__…sql, V3__…sql, etc. Flyway n’exécute chaque
fichier qu’une seule fois par base ; pas besoin de DROP pour rejouer l’historique.

Si vous aviez déjà créé les tables à la main (sans Flyway) avant cette bascule,
supprimez ces tables (ou recréez la base vide) puis redémarrez le service pour que
V1 s’applique une première fois via Flyway.

Base déjà initialisée sans Flyway
---------------------------------
Si les tables existent déjà mais sans table flyway_schema_history, soit recréer la
base en développement, soit utiliser une baseline Flyway (voir documentation Flyway
« baseline ») pour marquer V1 comme déjà appliquée sans réexécuter le script.
