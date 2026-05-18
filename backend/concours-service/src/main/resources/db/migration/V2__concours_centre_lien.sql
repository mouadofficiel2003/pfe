-- Lien explicite concours ↔ centre (planification / répartition par ville du candidat).

CREATE INDEX IF NOT EXISTS idx_concours_affectation_centre_id
    ON concours_affectation_centre (centre_id)
    WHERE centre_id IS NOT NULL;

COMMENT ON TABLE concours_affectation_centre IS
    'Centres où un concours se déroule (vue concours → centre). centre_id référence lieux.centre ; nom_centre sert à la répartition si centre_id est absent.';
COMMENT ON COLUMN concours_affectation_centre.centre_id IS
    'Identifiant du centre dans lieux-service ; permet la navigation centre → concours.';
