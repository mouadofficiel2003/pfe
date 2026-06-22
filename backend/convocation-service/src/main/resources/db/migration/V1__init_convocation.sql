-- Microservice convocation : historique des envois de convocations par e-mail.
-- Base dédiée data_convocations (database-per-service). Les références
-- (numero_inscription, numero_concours) sont logiques (gérées par d'autres services).

CREATE TABLE convocation_envoi (
    id                 BIGSERIAL PRIMARY KEY,
    numero_inscription VARCHAR(80)   NOT NULL,
    candidat_nom       VARCHAR(250)  NULL,
    email              VARCHAR(255)  NULL,
    numero_concours    VARCHAR(80)   NULL,
    nom_concours       VARCHAR(200)  NULL,
    statut             VARCHAR(20)   NOT NULL,
    message            VARCHAR(500)  NULL,
    declenche_par      VARCHAR(120)  NULL,
    envoye_le          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT convocation_envoi_statut_connu CHECK (statut IN ('ENVOYE', 'ECHEC'))
);

COMMENT ON TABLE convocation_envoi IS 'Trace d''un envoi de convocation par e-mail (réussi ou en échec).';

CREATE INDEX idx_convocation_envoi_inscription ON convocation_envoi (numero_inscription);
CREATE INDEX idx_convocation_envoi_date ON convocation_envoi (envoye_le DESC);
