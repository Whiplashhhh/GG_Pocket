# Contribuer à GG Pocket

## Le cycle d'une modification

1. Partir de `main` à jour et créer une branche :
   - fonctionnalité : `feature/<slug>-<version-prod>` (ex. `feature/ecran-chat-2.5.3` quand la prod est en 2.5.3) ;
   - correctif : `fix/<slug>-<version-prod>` ;
   - technique sans effet pour les membres : `chore/<slug>`.

   Le suffixe de version est informatif. Si `main` avance, on rebase et on garde le nom.
2. Commits en anglais au format [Conventional Commits](https://www.conventionalcommits.org/fr/) :
   `feat(topo): add route sheet`, `fix(auth): keep session after password change`, `docs: …`, `chore: …`.
   Petites unités cohérentes ; jamais de force-push sur une branche partagée.
3. Ouvrir une PR vers `main`, en français, avec le gabarit (quoi, pourquoi, captures clair/sombre, comment tester, entrée CHANGELOG).
4. La CI doit être verte. Pour tester sur l'environnement dev : label `deploy:dev` sur la PR (phase 2).
5. Fusion par squash. Les releases sont préparées par un workflow dédié (phase 2) ; `prod/X.Y.Z` et les tags `vX.Y.Z`
   ne sont jamais créés à la main.

`main` est protégée : pas de push direct, pas de force-push.

## Définition de « fini »

Résumé ; la liste complète est dans [`CLAUDE.md`](CLAUDE.md).

- Règles métier testées unitairement ; endpoints testés en intégration (Testcontainers) avec au moins un test
  d'accès autorisé **et** refusé par rôle.
- Lint, format, typecheck : zéro erreur, zéro avertissement.
- Vérifié visuellement en clair et en sombre, à 360 px, et à 1280 px pour les écrans de gestion (`pnpm screenshots`).
- Accessibilité : labels, focus visible, contraste AA, cibles ≥ 44 px, `prefers-reduced-motion`.
- Aucun texte d'interface en dur : tout est dans `frontend/src/i18n/fr.json` (une règle ESLint le vérifie).
- `CHANGELOG.md`, section « Non publié », mis à jour si les membres voient la différence.

## Conventions

- **Langues** : code, identifiants et commentaires en anglais ; interface, docs et CHANGELOG en français.
  Vocabulaire métier : voir le glossaire de la spec (§3).
- **Backend** : un package par domaine, couches `api` → `application` → `domain`, `infra` pour la persistance, `config`
  pour le câblage (vérifié par ArchUnit). DTO explicites, jamais d'entité JPA dans l'API. Erreurs en Problem Details
  avec un `code` stable (`ErrorCode`). Format : `./mvnw spotless:apply`.
- **Migrations Flyway** : jamais modifiées une fois fusionnées ; schéma compatible avec la version précédente de l'appli
  (« expand / contract »).
- **Frontend** : `<script setup lang="ts">`, logique dans des composables testables, état serveur dans TanStack Query,
  Pinia pour l'état d'interface uniquement. Couleurs, polices et espacements : uniquement les tokens de `docs/DESIGN.md`.
- **Contrat d'API** : après un changement d'API, `./mvnw verify -Dgg.openapi.write=true` puis `pnpm api:types`, et
  commiter les deux fichiers.
- **Sécurité** : toute autorisation est vérifiée côté serveur. Aucun secret dans le dépôt (`.env.example` documente les variables).
- **Pas de TODO dans le code** : une tâche restante devient une ligne dans `docs/ROADMAP.md`.
- **Décisions** : un choix structurant ou un écart à la spec s'écrit dans `docs/DECISIONS.md`.
