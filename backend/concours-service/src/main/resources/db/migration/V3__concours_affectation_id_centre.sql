-- id_centre = identifiant partagé avec lieux-service ; nom_centre = libellé affiché (dénormalisé).

DROP INDEX IF EXISTS idx_concours_affectation_centre_id;

ALTER TABLE concours_affectation_centre RENAME COLUMN centre_id TO id_centre;

ALTER TABLE concours_affectation_centre DROP CONSTRAINT uq_concours_affectation_nom;

DELETE FROM concours_affectation_centre WHERE id_centre IS NULL;

ALTER TABLE concours_affectation_centre ALTER COLUMN id_centre SET NOT NULL;

ALTER TABLE concours_affectation_centre
    ADD CONSTRAINT uq_concours_affectation_centre UNIQUE (numero_concours, id_centre);

CREATE INDEX idx_concours_affectation_id_centre ON concours_affectation_centre (id_centre);

COMMENT ON TABLE concours_affectation_centre IS
    'Centres où un concours se déroule. id_centre référence lieux.centre ; nom_centre sert à l''affichage.';
COMMENT ON COLUMN concours_affectation_centre.id_centre IS
    'Référence logique vers lieux.centre.id_centre (identifiant partagé entre microservices).';
COMMENT ON COLUMN concours_affectation_centre.nom_centre IS
    'Libellé du centre (copie dénormalisée, alignée avec lieux.centre.nom_centre).';
