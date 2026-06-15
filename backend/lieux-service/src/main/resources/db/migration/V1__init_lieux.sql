-- Centres (ville / pôle), établissements, salles — base dédiée lieux-service.

CREATE TABLE centre (
    id_centre       BIGSERIAL PRIMARY KEY,
    nom_centre      VARCHAR(200) NOT NULL,
    cree_le         TIMESTAMPTZ NOT NULL,
    modifie_le      TIMESTAMPTZ NOT NULL
);

CREATE TABLE etablissement (
    id_etablissement     BIGSERIAL PRIMARY KEY,
    centre_id            BIGINT NOT NULL REFERENCES centre (id_centre) ON DELETE CASCADE,
    nom_etablissement    VARCHAR(200) NOT NULL,
    cree_le              TIMESTAMPTZ NOT NULL,
    modifie_le           TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_etablissement_centre ON etablissement (centre_id);

CREATE TABLE salle (
    id_salle           BIGSERIAL PRIMARY KEY,
    etablissement_id   BIGINT NOT NULL REFERENCES etablissement (id_etablissement) ON DELETE CASCADE,
    nom_salle          VARCHAR(200) NOT NULL,
    nombre_places      INT NOT NULL,
    numero_concours    VARCHAR(80),
    cree_le            TIMESTAMPTZ NOT NULL,
    modifie_le         TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_salle_nombre_places CHECK (nombre_places > 0)
);

CREATE INDEX idx_salle_etablissement ON salle (etablissement_id);
CREATE INDEX idx_salle_numero_concours ON salle (numero_concours);

COMMENT ON TABLE centre IS
    'Pôle géographique (ville). concoursNumeros en lecture = union des concours affectés (concours-service, centre_id) et des numero_concours des salles du centre.';
COMMENT ON TABLE etablissement IS
    'Établissement d''un centre. concoursNumeros en lecture = concours distincts des salles de l''établissement.';
COMMENT ON COLUMN salle.numero_concours IS
    'Concours accueilli par la salle (un seul). Référence logique vers concours.numero_concours ; source opérationnelle du lien établissement ↔ concours.';

CREATE UNIQUE INDEX IF NOT EXISTS uq_centre_nom_lower
    ON centre (LOWER(nom_centre));

CREATE UNIQUE INDEX IF NOT EXISTS uq_etablissement_nom_par_centre_lower
    ON etablissement (centre_id, LOWER(nom_etablissement));

CREATE UNIQUE INDEX IF NOT EXISTS uq_salle_nom_par_etablissement_lower
    ON salle (etablissement_id, LOWER(nom_salle));
