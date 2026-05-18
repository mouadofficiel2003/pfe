-- Concours + centres où il se déroule (noms alignés avec le futur service lieux ; centre_id optionnel).

CREATE TABLE concours (
    id                  BIGSERIAL PRIMARY KEY,
    nom_concours        VARCHAR(200) NOT NULL,
    numero_concours     VARCHAR(80) NULL,
    date_heure_examen   TIMESTAMPTZ NOT NULL,
    cree_le             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modifie_le          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_concours_numero_concours UNIQUE (numero_concours)
);

COMMENT ON TABLE concours IS 'Concours : nom, date/heure d''examen ; centres dans concours_affectation_centre.';

CREATE TABLE concours_affectation_centre (
    id           BIGSERIAL PRIMARY KEY,
    concours_id  BIGINT NOT NULL REFERENCES concours (id) ON DELETE CASCADE,
    nom_centre   VARCHAR(200) NOT NULL,
    centre_id    BIGINT NULL,
    CONSTRAINT uq_concours_affectation_nom UNIQUE (concours_id, nom_centre)
);

CREATE INDEX idx_concours_affectation_concours ON concours_affectation_centre (concours_id);

COMMENT ON COLUMN concours_affectation_centre.nom_centre IS 'Ex. Centre Rabat — utilisé pour la répartition (ville du candidat).';
COMMENT ON COLUMN concours_affectation_centre.centre_id IS 'Référence future vers le service lieux (nullable).';
