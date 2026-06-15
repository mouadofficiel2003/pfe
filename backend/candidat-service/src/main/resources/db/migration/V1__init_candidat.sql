CREATE TABLE candidat (
    numero_inscription   VARCHAR(80)   PRIMARY KEY,
    nom                  VARCHAR(120)  NOT NULL,
    prenom               VARCHAR(120)  NOT NULL,
    cin                  VARCHAR(32)   NOT NULL,
    numero_telephone     VARCHAR(32)   NOT NULL,
    ville                VARCHAR(120)  NOT NULL,
    age                  SMALLINT      NOT NULL,
    email                VARCHAR(255)  NOT NULL,
    specialite           VARCHAR(200)  NOT NULL,
    nom_concours         VARCHAR(200)  NOT NULL,
    numero_concours      VARCHAR(80)   NULL,
    id_centre            BIGINT        NULL,
    id_etablissement     BIGINT        NULL,
    id_salle             BIGINT        NULL,
    numero_place         INTEGER       NULL,
    cree_le              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    modifie_le           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT candidat_age_raisonnable CHECK (age >= 10 AND age <= 120),
    CONSTRAINT candidat_numero_place_positif CHECK (numero_place IS NULL OR numero_place > 0),
    CONSTRAINT candidat_cin_unique UNIQUE (cin)
);

COMMENT ON TABLE candidat IS 'Candidats admis (import Excel + CRUD). PK = numero_inscription.';
COMMENT ON COLUMN candidat.nom_concours IS 'Libellé dénormalisé (copie concours-service), maintenu à chaque écriture.';
COMMENT ON COLUMN candidat.numero_concours IS 'Référence logique vers concours.numero_concours (service concours).';
COMMENT ON COLUMN candidat.id_centre IS 'Affectation répartition ; FK vers table centre quand le service lieux sera branché.';
COMMENT ON COLUMN candidat.id_etablissement IS 'Affectation répartition ; FK établissement à brancher plus tard.';
COMMENT ON COLUMN candidat.id_salle IS 'Affectation répartition ; FK salle à brancher plus tard.';
COMMENT ON COLUMN candidat.numero_place IS 'Place dans la salle après répartition manuelle ou automatique.';

CREATE INDEX idx_candidat_nom_concours ON candidat (nom_concours);
CREATE INDEX idx_candidat_ville_concours ON candidat (ville, nom_concours);
CREATE INDEX idx_candidat_numero_concours ON candidat (numero_concours) WHERE numero_concours IS NOT NULL;

CREATE OR REPLACE FUNCTION set_candidat_modifie_le()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modifie_le = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_candidat_modifie_le
    BEFORE UPDATE ON candidat
    FOR EACH ROW
    EXECUTE FUNCTION set_candidat_modifie_le();
