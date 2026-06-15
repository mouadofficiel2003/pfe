-- Microservice répartition : historique des exécutions + résultats d'affectation + alertes.

-- Base dédiée data_repartition (database-per-service). Les références (numero_inscription,

-- numero_concours, centre_id, etablissement_id, salle_id) sont logiques (autres services).



CREATE TABLE repartition_run (

    id               BIGSERIAL PRIMARY KEY,

    declenche_par    VARCHAR(120)  NULL,

    statut           VARCHAR(40)   NOT NULL,

    total_candidats  INTEGER       NOT NULL DEFAULT 0,

    total_affectes   INTEGER       NOT NULL DEFAULT 0,

    total_alertes    INTEGER       NOT NULL DEFAULT 0,

    demarre_le       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    termine_le       TIMESTAMPTZ   NULL,

    CONSTRAINT repartition_run_statut_connu CHECK (statut IN ('TERMINEE', 'TERMINEE_AVEC_ALERTES', 'ECHEC'))

);



COMMENT ON TABLE repartition_run IS 'Une exécution de la répartition automatique (déclenchement gestionnaire).';



CREATE TABLE repartition_affectation (

    id                 BIGSERIAL PRIMARY KEY,

    run_id             BIGINT        NOT NULL REFERENCES repartition_run (id) ON DELETE CASCADE,

    numero_inscription VARCHAR(80)   NOT NULL,

    candidat_nom       VARCHAR(250)  NULL,

    ville              VARCHAR(120)  NULL,

    numero_concours    VARCHAR(80)   NULL,

    nom_concours       VARCHAR(200)  NULL,

    centre_id          BIGINT        NULL,

    nom_centre         VARCHAR(200)  NULL,

    etablissement_id   BIGINT        NULL,

    nom_etablissement  VARCHAR(200)  NULL,

    salle_id           BIGINT        NULL,

    nom_salle          VARCHAR(200)  NULL,

    numero_place       INTEGER       NULL,

    CONSTRAINT repartition_affectation_place_positif CHECK (numero_place IS NULL OR numero_place > 0)

);



CREATE INDEX idx_repartition_affectation_run ON repartition_affectation (run_id);

CREATE INDEX idx_repartition_affectation_inscription ON repartition_affectation (numero_inscription);



CREATE TABLE repartition_alerte (

    id                 BIGSERIAL PRIMARY KEY,

    run_id             BIGINT        NOT NULL REFERENCES repartition_run (id) ON DELETE CASCADE,

    type               VARCHAR(40)   NOT NULL,

    numero_inscription VARCHAR(80)   NULL,

    candidat_nom       VARCHAR(250)  NULL,

    ville              VARCHAR(120)  NULL,

    numero_concours    VARCHAR(80)   NULL,

    nom_concours       VARCHAR(200)  NULL,

    centre_id          BIGINT        NULL,

    nom_centre         VARCHAR(200)  NULL,

    message            VARCHAR(500)  NOT NULL,

    CONSTRAINT repartition_alerte_type_connu

        CHECK (type IN ('CAPACITE_DEPASSEE', 'AUCUN_CENTRE_DISPONIBLE', 'CONCOURS_INCONNU', 'VILLE_NON_GEOLOCALISEE'))

);



CREATE INDEX idx_repartition_alerte_run ON repartition_alerte (run_id);


