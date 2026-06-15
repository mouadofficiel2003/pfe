-- Message libre attaché à un run : sert surtout à tracer la cause d'un échec (statut ECHEC),
-- ex. service aval indisponible. NULL pour les runs terminés normalement.

ALTER TABLE repartition_run ADD COLUMN message VARCHAR(500) NULL;

COMMENT ON COLUMN repartition_run.message IS 'Cause d''un échec (statut ECHEC) ou note libre ; NULL si run terminé normalement.';
