-- Concours sur centre / établissement : pas de colonne dédiée ; exposés par l''API (dérivation).

COMMENT ON TABLE centre IS
    'Pôle géographique (ville). concoursIds en lecture = union des concours affectés (concours-service, centre_id) et des concours_id des salles du centre.';
COMMENT ON TABLE etablissement IS
    'Établissement d''un centre. concoursIds en lecture = concours distincts des salles de l''établissement.';
COMMENT ON COLUMN salle.concours_id IS
    'Concours accueilli par la salle (un seul). Référence logique vers concours.id ; source opérationnelle du lien établissement ↔ concours.';
