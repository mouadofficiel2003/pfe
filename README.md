# PFE — Plateforme de gestion de concours

Application full-stack pour la gestion de concours, candidats, lieux (centres, établissements, salles) et authentification.

## Structure du projet

| Dossier | Description |
|---------|-------------|
| `backend/` | Microservices Spring Boot (auth, concours, candidat, lieux) |
| `frontend/` | Interface React + TypeScript (Vite) |
| `database/` | Scripts et documentation des migrations |
| `scripts/` | Scripts utilitaires |
| `*.puml` / `*.png` | Diagrammes UML (cas d'utilisation, classes, séquences) |

## Prérequis

- Java 17+
- Maven (ou `mvnw` à la racine)
- Node.js 18+ et npm
- PostgreSQL

## Démarrage rapide

### Backend

```powershell
cd backend
.\run-backend.ps1
```

Configurer chaque service via `application-local.properties` (copier depuis `application-local.properties.example` dans chaque module).

### Frontend

```powershell
cd frontend
cp .env.example .env
npm install
npm run dev
```

## Services backend

| Service | Port par défaut |
|---------|-----------------|
| auth-service | 8081 |
| concours-service | 8082 |
| candidat-service | 8083 |
| lieux-service | 8084 |

## Licence

Projet de fin d'études (PFE).
