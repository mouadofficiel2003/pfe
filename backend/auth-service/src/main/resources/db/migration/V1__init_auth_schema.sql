-- Comptes applicatifs (admin / gestionnaire). Mots de passe bcrypt (jamais en clair).
-- Comptes par défaut (à changer en production) : admin / Admin123! ; gestionnaire / Gest123!

CREATE TYPE role_utilisateur AS ENUM ('ADMINISTRATEUR', 'GESTIONNAIRE');

CREATE TABLE utilisateur (
    id              BIGSERIAL PRIMARY KEY,
    nom_utilisateur VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            role_utilisateur NOT NULL,
    actif           BOOLEAN NOT NULL DEFAULT TRUE,
    cree_le         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE utilisateur IS 'Comptes applicatifs (admin / gestionnaire).';

INSERT INTO utilisateur (nom_utilisateur, password_hash, role)
VALUES
    (
        'admin',
        '$2b$10$9.kSa72nCyxkq6rCYbXBT.wDfwOFd7e4adDdtliTOPlIMbehyHtd6',
        'ADMINISTRATEUR'
    ),
    (
        'gestionnaire',
        '$2b$10$IV9RJWWRWGUVk0LaGH9n6ez9lB1mIfQwZ1tH.X5gA1581cn2pyf1y',
        'GESTIONNAIRE'
    );
