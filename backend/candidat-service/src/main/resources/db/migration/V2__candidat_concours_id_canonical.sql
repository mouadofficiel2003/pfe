-- concours_id = référence canonique ; nom_concours = libellé dénormalisé (maintenu par candidat-service).

COMMENT ON COLUMN candidat.concours_id IS 'Référence canonique vers concours.id (service concours).';
COMMENT ON COLUMN candidat.nom_concours IS 'Libellé dénormalisé copié depuis concours-service à chaque écriture.';

ALTER TABLE candidat DROP CONSTRAINT IF EXISTS candidat_inscription_concours_unique;

ALTER TABLE candidat ADD CONSTRAINT candidat_inscription_concours_unique
    UNIQUE (numero_inscription, concours_id);
