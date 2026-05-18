-- Centres (ville / pôle), établissements, salles — base dédiée lieux-service.

CREATE TABLE centre (
    id              BIGSERIAL PRIMARY KEY,
    nom_centre      VARCHAR(200) NOT NULL,
    cree_le         TIMESTAMPTZ NOT NULL,
    modifie_le      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_centre_nom UNIQUE (nom_centre)
);

CREATE TABLE etablissement (
    id                   BIGSERIAL PRIMARY KEY,
    centre_id            BIGINT NOT NULL REFERENCES centre (id) ON DELETE CASCADE,
    nom_etablissement    VARCHAR(200) NOT NULL,
    cree_le              TIMESTAMPTZ NOT NULL,
    modifie_le           TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_etablissement_nom_par_centre UNIQUE (centre_id, nom_etablissement)
);

CREATE INDEX idx_etablissement_centre ON etablissement (centre_id);

CREATE TABLE salle (
    id                 BIGSERIAL PRIMARY KEY,
    etablissement_id   BIGINT NOT NULL REFERENCES etablissement (id) ON DELETE CASCADE,
    nom_salle          VARCHAR(200) NOT NULL,
    nombre_places      INT NOT NULL,
    concours_id        BIGINT,  -- réf. logique concours.id (service concours) ; validée à l'écriture via API
    cree_le            TIMESTAMPTZ NOT NULL,
    modifie_le         TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_salle_nom_par_etablissement UNIQUE (etablissement_id, nom_salle),
    CONSTRAINT ck_salle_nombre_places CHECK (nombre_places > 0)
);

CREATE INDEX idx_salle_etablissement ON salle (etablissement_id);
CREATE INDEX idx_salle_concours ON salle (concours_id);
