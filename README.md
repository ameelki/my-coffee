# Coffee Events

Application complète pour un café avec salle des fêtes : commande à table ou à emporter, écran cuisine, réservation de la salle, et un espace admin où tout se configure.

**Stack** : Spring Boot 3.3 (Java 21) · PostgreSQL · Angular 18 · JWT. Même structure que l'application Syndic (`backend/`, `frontend/`, Docker, Render).

## Les trois espaces

| Espace | Accès | Ce qu'on y fait |
|---|---|---|
| **Client** | `/` (sans compte) ou compte client | Parcourir la carte, composer un panier (options : taille, lait, cuisson…), commander sur place (numéro de table / QR code) ou à emporter, suivre la commande en direct, réserver la salle (calendrier, formule, options, prix en direct), retrouver son historique |
| **Cuisine** | `/cuisine` (rôle Cuisine ou Admin) | Tableau en 3 colonnes (nouvelles, en préparation, prêtes), alerte sonore, minuteur, marquer un produit épuisé |
| **Admin** | `/admin` (rôle Admin) | Tableau de bord, commandes, réservations (confirmer / refuser avec message), carte et produits avec options, formules et options de la salle, dates fermées, tables et QR codes à imprimer, comptes, réglages (identité, couleurs, ouverture des commandes, règles de la salle) |

Tout ce qui est « règle » se modifie dans l'admin, sans toucher au code : tarif horaire, acompte, capacité, horaires, délai de réservation, jours de fermeture, types d'événements, couleurs, textes, activation de chaque mode de commande.

## Démarrage rapide

```bash
docker compose up -d                 # PostgreSQL local
cd backend && ./mvnw spring-boot:run # ou : mvn spring-boot:run    (http://localhost:8080)
cd frontend && npm install && npm start   # http://localhost:4200
```

Au premier démarrage, le backend crée :
- un compte **admin** (`admin@coffee.lu` / `changeme123`) et un compte **cuisine** (`cuisine@coffee.lu` / `changeme123`) ;
- une carte d'exemple, 9 tables, 3 formules et 4 options pour la salle (désactivable avec `SEED_DEMO=false`).

**Changez ces mots de passe** (variables `ADMIN_PASSWORD`, `KITCHEN_PASSWORD`, ou depuis l'admin, page Équipe).

## Déploiement

Serveur unique (Docker) :

```bash
cp .env.example .env     # puis modifier les valeurs
docker compose -f docker-compose.prod.yml up -d --build
```

Sinon : back-end sur Render (`render.yaml`) avec une base Neon / Supabase, front sur Vercel / Netlify (le fichier `frontend/src/_redirects` gère les routes Angular ; pensez à mettre l'URL de l'API dans `frontend/src/environments/environment.prod.ts` si le front n'est pas servi avec le proxy nginx).

### Variables du back-end

`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (32 caractères minimum), `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `KITCHEN_EMAIL`, `KITCHEN_PASSWORD`, `CORS_ORIGINS`, `SEED_DEMO`, et facultatif pour les emails : `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `MAIL_FROM`, `FRONT_URL`. Sans SMTP, les emails (confirmation de commande, commande prête, réservation confirmée ou refusée) sont écrits dans les logs.

## Fonctionnement

- **QR code à table** : l'admin imprime un QR code par table (`/admin/tables`). Il ouvre `/carte?table=T4` ; la table est retenue pour la commande. Les images de QR codes sont générées par le service `api.qrserver.com`.
- **Commande** : numéro de suivi `C-XXXXXX`. Statuts : reçue, en préparation, prête, servie, annulée. La page de suivi se met à jour toutes les 10 s, l'écran cuisine toutes les 8 s.
- **Salle** : numéro `R-XXXXXX`. Les demandes sont « en attente » jusqu'à confirmation (ou confirmées d'office si l'option est activée). Le serveur vérifie les chevauchements (avec un temps de battement réglable), les fermetures, le délai minimum, la capacité et la durée, puis calcule le prix : `tarif horaire × durée + formule × invités + options`.
- **Paiement** : sur place uniquement (pas de paiement en ligne).
- **Prix et options** : le prix d'une ligne est toujours recalculé côté serveur à partir de la carte.

## Structure

```
backend/   Spring Boot : entity/, repository/, service/, controller/, config/, security/
frontend/  Angular 18 (composants autonomes) : core/, pages/, admin/, shared/
```

Le fichier `index.html` à la racine est l'ancienne page vitrine statique.
