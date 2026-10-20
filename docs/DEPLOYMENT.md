# Déployer et exploiter GG Pocket

Le pas à pas pour le serveur, les déploiements, les releases, les retours arrière et les sauvegardes.
Les choix derrière ce document : `DECISIONS.md` D-059 à D-069. Le plan technique : `docs/plans/phase-2.md`.

## 1. Vue d'ensemble

Un seul serveur (VPS OVH, Ubuntu, 8 Go) héberge deux environnements isolés et un proxy frontal :

```
Internet ──80/443──► frontal Caddy (TLS Let's Encrypt)    /opt/gg-pocket/edge
                        ├─ prod ──► 127.0.0.1:8180 ──► web ─► api ─► db (+ backup)    /opt/gg-pocket/prod
                        └─ dev  ──► 127.0.0.1:8181 ──► web ─► api ─► db               /opt/gg-pocket/dev
```

- Chaque environnement est un projet Docker Compose à part : réseaux, volumes, base et secrets distincts.
  Le dev ne voit ni la base ni les fichiers de la prod.
- Seul le frontal écoute sur Internet. L'API et la base ne sont jamais publiées ; `web` ne l'est que sur la boucle locale.
- Images : `ghcr.io/whiplashhhh/gg-pocket-api` et `gg-pocket-web`, tag `X.Y.Z` (prod) ou `dev-<commit>` (dev), jamais `latest`.
- Fichiers : `deploy/` dans le dépôt. Chaque déploiement en envoie une copie sur le serveur
  (`/opt/gg-pocket/<env>/releases/<version>/`), donc chaque version se déploie avec ses propres fichiers.

| | Dev | Prod |
|---|---|---|
| Adresse provisoire | `https://dev.pocket.<ip-avec-tirets>.sslip.io` | `https://pocket.<ip-avec-tirets>.sslip.io` |
| Déclenchement | `deploy-dev.yml` (bouton ou label `deploy:dev`) | `release.yml` après la PR de release, approbation de Willem |
| Données | comptes de démonstration, réinitialisables | réelles, sauvegardées chaque nuit |
| Inscription | fermée (comptes de test) | par code du club |

## 2. Préparer le serveur (une seule fois)

Prérequis : Ubuntu avec Docker et le plugin Compose (`docker compose version`), un compte avec `sudo`.

```bash
git clone https://github.com/Whiplashhhh/GG_Pocket.git /tmp/gg-pocket
sudo /tmp/gg-pocket/deploy/scripts/server/bootstrap.sh \
  --dev-key  'ssh-ed25519 AAAA… gg-pocket-dev' \
  --prod-key 'ssh-ed25519 AAAA… gg-pocket-prod' \
  --firewall
```

Les deux clés publiques sont affichées par `setup-github.sh` (section 3), à lancer avant. Le script, que l'on peut relancer sans risque :

- crée l'utilisateur `deploy` (sans mot de passe, membre du groupe `docker`) et installe la porte `/usr/local/bin/gg-deploy` ;
- inscrit les clés dans `/home/deploy/.ssh/authorized_keys` (fichier à root), chacune **épinglée à un environnement** :
  la clé dev ne peut rien faire sur la prod ;
- crée `/opt/gg-pocket/{edge,dev,prod}`, et `prod/backups` lisible par le seul conteneur de sauvegarde ;
- écrit `dev/.env` et `prod/.env` (droits 600) avec des mots de passe aléatoires, s'ils n'existent pas déjà ;
- démarre le frontal Caddy avec les adresses `sslip.io` tirées de l'IPv4 du serveur ;
- avec `--firewall` : `ufw` n'ouvre que SSH, HTTP et HTTPS.

À faire en plus, une fois : `sudo apt install unattended-upgrades` (mises à jour de sécurité automatiques).

**À savoir** : être dans le groupe `docker` revient à être root. La porte limite ce que la clé peut demander
(`deploy`, `rollback`, `status`, `login`, `edge`), mais un déploiement exécute les scripts de la version envoyée : la vraie
protection est que seules les GitHub Actions du dépôt ont les clés, et que la prod attend l'approbation de Willem.

## 3. GitHub (une seule fois, avant le serveur)

Depuis ta machine, avec la CLI GitHub connectée en admin du dépôt (`gh auth login`) :

```bash
.github/scripts/setup-github.sh --host <IPv4 du serveur>
```

Le script (relançable) :

- affiche l'empreinte SSH du serveur et demande de la comparer à celle lue sur le serveur
  (`ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub`) avant de lui faire confiance ;
- crée les environnements `dev` et `production` (ce dernier : approbation par toi, depuis `main` et les tags `v*` seulement) ;
- génère une paire de clés SSH par environnement : la clé privée part directement dans le secret `DEPLOY_SSH_KEY`
  de l'environnement et n'est jamais écrite ailleurs ; les clés publiques s'affichent, prêtes pour `bootstrap.sh` ;
- pose les variables `DEPLOY_HOST`, `DEPLOY_KNOWN_HOSTS`, `GG_URL` de chaque environnement ;
- ajoute les jobs « Deploy config » et « Docker stack » aux checks obligatoires de `main`, et réserve les branches
  `prod/*` et les tags `v*` au workflow de release ;
- crée le label `deploy:dev`.

Il se termine en affichant la commande `bootstrap.sh` à lancer sur le serveur (section 2), clés comprises.
Changer de clés : relancer le script, puis `bootstrap.sh` avec les nouvelles clés publiques.

Les images restent privées sur ghcr : à chaque déploiement, le serveur s'authentifie avec le jeton éphémère du job
(valable jusqu'à la fin du job). Les images de la version en place et de la précédente restent sur le serveur, donc un
retour arrière depuis le serveur n'a rien à télécharger.

## 4. Nom de domaine

Tant que le club n'a pas de domaine, les adresses `sslip.io` (un service DNS public qui renvoie l'IP écrite dans le nom)
donnent du HTTPS valide tout de suite, indispensable pour la PWA. Le jour où le domaine arrive :

1. DNS chez le registrar : deux enregistrements `A` (et `AAAA` si IPv6) vers le serveur, par exemple
   `pocket.exemple.fr` et `dev.pocket.exemple.fr`.
2. Sur le serveur : modifier `/opt/gg-pocket/edge/.env` (`GG_PROD_DOMAIN`, `GG_DEV_DOMAIN`), puis
   `cd /opt/gg-pocket/edge && sudo docker compose up -d --force-recreate`.
3. Dans GitHub : mettre à jour la variable `GG_URL` des deux environnements.
4. Prévenir les membres : une appli installée depuis l'ancienne adresse doit être réinstallée depuis la nouvelle.

## 5. Déployer le dev

- **À la main** : Actions → « Deploy dev » → *Run workflow*, choisir la branche ; « Réinitialiser la base et rejouer le seed »
  est coché par défaut (la base dev repart des comptes de démonstration).
- **Depuis une PR** : poser le label `deploy:dev`. Chaque nouveau push sur la PR redéploie (avec réinitialisation).

Le workflow construit les images `dev-<commit>`, envoie `deploy/` au serveur et lance `deploy.sh dev` : téléchargement,
démarrage, attente des healthchecks, test de fumée (`/api/v1/meta` doit annoncer le bon commit), retour automatique
à la version précédente sinon.

Comptes de test du dev : `admin`, `direction`, `ouvreur`, `grimpeur`, mot de passe commun lisible par Willem :
`sudo grep GG_SEED_PASSWORD /opt/gg-pocket/dev/.env`. Le bandeau « DEV » s'affiche en haut de l'appli.

### Fermer davantage le dev (optionnel)

Niveau 1, toujours actif : l'appli dev n'a pas d'inscription, seuls les comptes de test existent, la connexion est
limitée en débit, rien n'est indexé (`noindex`).

Niveau 2, désactivé par défaut : un mot de passe devant tout le site (authentification basique du frontal).
Il gêne la PWA installée sur iPhone (iOS redemande le mot de passe, parfois en boucle) : à réserver à une période sensible.

```bash
sudo docker run --rm caddy:2.11-alpine caddy hash-password --plaintext 'mot de passe des testeurs'
sudo tee /opt/gg-pocket/edge/dev.d/basic-auth.caddy <<'EOF'
basic_auth {
	testeurs <empreinte affichée par la commande précédente>
}
EOF
cd /opt/gg-pocket/edge && sudo docker compose restart
```

Pour l'enlever : supprimer `basic-auth.caddy` et relancer le frontal.

## 6. Publier une version (release)

1. Tout ce qui part est fusionné dans `main`, et la section « Non publié » du CHANGELOG est remplie.
2. Actions → « Prepare release » → *Run workflow* sur `main`, choisir `patch` (correctif), `minor` (nouveauté) ou
   `major` (rupture). Le workflow ouvre la PR « Release X.Y.Z » (`VERSION` + CHANGELOG) et y lance la CI.
3. Relire la PR (les notes de version sont dans sa description), la fusionner (squash).
4. « Release » se lance tout seul : tag `vX.Y.Z`, branche `prod/X.Y.Z`, GitHub Release, images `X.Y.Z`.
5. Le job « Deploy to prod » attend l'approbation de Willem (Actions → la release → *Review deployments*).
   Il sauvegarde la base, déploie, vérifie que `/api/v1/meta` annonce `X.Y.Z`, et remet la version précédente sinon.

La première mise en service du club sera `1.0.0` ; d'ici là, des `0.x.y`.

### Premier démarrage de la prod : le code admin

Base vide : l'API affiche **une fois** dans ses journaux un code d'inscription admin, valable 24 heures. Aucun mot de passe par défaut.

```bash
sudo docker logs gg-pocket-prod-api-1 2>&1 | grep -A1 "admin registration code"
```

Ouvrir l'appli, « J'ai un code du club », saisir ce code : ce compte est l'admin. Tant que personne ne s'est inscrit,
chaque redémarrage génère un nouveau code.

## 7. Revenir en arrière

Les migrations de base suivent la règle « expand / contract » : la version N−1 fonctionne sur le schéma N, donc
revenir en arrière ne touche pas aux données.

- **Automatique** : un déploiement qui ne passe pas les healthchecks ou le test de fumée remet la version d'avant.
- **Version précédente** : Actions → « Release » → *Run workflow* avec le numéro de la version voulue (par exemple `0.1.0`) ;
  la même approbation est demandée. C'est la voie normale, tracée dans GitHub.
- **Depuis le serveur, en urgence** :
  `sudo -u deploy /opt/gg-pocket/prod/releases/$(cat /opt/gg-pocket/prod/current-tag)/scripts/rollback.sh prod`.
- **État** : `cat /opt/gg-pocket/prod/current-tag /opt/gg-pocket/prod/previous-tag`, `sudo docker ps`.

## 8. Sauvegardes et restauration

- Le service `backup` de la prod fait chaque nuit à 3 h 30 un fichier `gg-pocket-<date>.tar` (base au format `pg_dump`
  + photos téléversées) dans `/opt/gg-pocket/prod/backups/daily/`. Rétention : 14 jours ; celle du dimanche est copiée
  dans `weekly/` et gardée 8 semaines. Chaque déploiement prod en fait une avant de commencer (`pre-deploy/`, 10 gardées).
- **Copie hors du serveur, recommandée** : si le VPS disparaît, ses sauvegardes aussi. Le plus simple : l'option
  « Backup Storage » d'OVH, ou un `rsync` quotidien vers une autre machine. Non automatisé pour l'instant.
- Sauvegarde à la demande (dans `manual/`, gardée jusqu'à suppression à la main) :
  `sudo -u deploy /opt/gg-pocket/prod/releases/$(cat /opt/gg-pocket/prod/current-tag)/scripts/backup-now.sh prod`.

### Restaurer

Tout est remplacé par le contenu de la sauvegarde (base **et** photos). Une sauvegarde de l'état actuel est faite juste
avant, dans `manual/`.

```bash
sudo ls /opt/gg-pocket/prod/backups/daily/
cd /opt/gg-pocket/prod/releases/$(cat /opt/gg-pocket/prod/current-tag)
sudo -u deploy scripts/restore.sh prod daily/gg-pocket-2026-10-20_033000.tar
```

Le script demande de taper `prod` pour confirmer, arrête l'appli, recrée la base, remet les photos, redémarre et vérifie.
L'API refuse de démarrer sur une sauvegarde d'un autre environnement (marqueur en base) : on ne restaure la prod qu'en prod.
Cette restauration est testée à chaque CI (job « Docker stack » : sonde en base et fichier, sauvegarde, dégâts, restauration, vérification).

## 9. Dépannage

| Symptôme | Où regarder |
|---|---|
| Le déploiement échoue | Journal du job GitHub (les 120 dernières lignes de l'API y sont), puis `sudo docker logs gg-pocket-<env>-api-1` |
| « Refusing to fall back on the local defaults » | `.env` de l'environnement incomplet (`GG_DB_*`) : relancer `bootstrap.sh` ne l'écrase pas, le corriger à la main |
| « belongs to the … environment » | La base n'est pas celle de cet environnement (mauvais `.env` ou sauvegarde d'un autre environnement) |
| Certificat absent | `sudo docker logs gg-pocket-edge-caddy-1` : ports 80/443 ouverts ? DNS (ou IP du nom sslip.io) correct ? |
| Plus de place disque | `sudo docker system df` ; chaque déploiement ne garde que les images de la version en place et de la précédente |
| Mémoire | `sudo docker stats --no-stream` (budget : prod ≈ 1,5 Go, dev ≈ 1,1 Go, frontal 128 Mo) |
