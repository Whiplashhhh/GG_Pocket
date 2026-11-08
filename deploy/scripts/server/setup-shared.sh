#!/usr/bin/env bash
# Prepares GG Pocket's prod on a server whose reverse proxy is shared with other apps and where we
# are not root (the club's server, DECISIONS D-126). Run it on the server as the user who will run
# the app (member of the docker group), from a clone of the repository:
#
#   git clone https://github.com/Whiplashhhh/GG_Pocket.git /tmp/gg-pocket
#   /tmp/gg-pocket/deploy/scripts/server/setup-shared.sh --key 'ssh-ed25519 AAAA... gg-pocket-prod'
#
# The key is the public half of the deployment key, printed by .github/scripts/setup-prod-shared.sh.
# Rerunnable: the .env and its secrets are kept, the key line is replaced. It writes:
#   <home>/.env              secrets and settings (mode 600)
#   <home>/releases/         one directory per deployed version, filled by each deployment
#   <home>/data/backups/     nightly dumps (database and photos), for the server's own backup
#   <home>/bin/gg-deploy     SSH gate of the deployment key
#   ~/.ssh/authorized_keys   the deployment key, pinned to gg-deploy (no shell, no forwarding)
#
# Options: --home /srv/ggpocket  --network web (the proxy's Docker network)  --alias ggpocket
set -euo pipefail

repo=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)
home=/srv/ggpocket network=web alias=ggpocket key=''
while [[ $# -gt 0 ]]; do
    case $1 in
        --key) key=$2; shift 2 ;;
        --home) home=$2; shift 2 ;;
        --network) network=$2; shift 2 ;;
        --alias) alias=$2; shift 2 ;;
        *) echo "unknown option $1" >&2; exit 1 ;;
    esac
done

say() { printf '\n==> %s\n' "$*"; }
die() { echo "ERROR: $*" >&2; exit 1; }

[[ $EUID -ne 0 ]] || die "run it as the user who will run the app, not as root"
[[ $key =~ ^ssh-ed25519\ [A-Za-z0-9+/=]+(\ .*)?$ ]] || die "--key 'ssh-ed25519 AAAA...' is required"
[[ $home == /* && $home != *..* ]] || die "invalid --home '$home'"
[[ $alias =~ ^[a-z0-9][a-z0-9-]*$ ]] || die "invalid --alias '$alias'"
docker info >/dev/null 2>&1 || die "this user cannot use Docker (not in the docker group?)"
docker compose version >/dev/null || die "the Docker Compose plugin is missing"
docker network inspect "$network" >/dev/null 2>&1 || die "the proxy network '$network' does not exist"
[[ -w $home || (! -e $home && -w $(dirname "$home")) ]] || die "$home is not writable by $(id -un)"

say "Directories in $home"
install -d -m 750 "$home" "$home/releases" "$home/bin"
install -d -m 755 "$home/data"
install -m 755 "$repo/deploy/scripts/server/gg-deploy" "$home/bin/gg-deploy"
# The backup container writes as postgres (uid 70); only root may hand it the directory: Docker's.
docker run --rm --network none --user 0 -v "$home/data:/data" postgres:18-alpine \
    sh -c 'mkdir -p /data/backups && chown 70:70 /data/backups && chmod 700 /data/backups'

say "Settings and secrets ($home/.env)"
secret() { openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c "$1"; }
if [[ -f $home/.env ]]; then
    echo "$home/.env exists: kept"
else
    (
        umask 077
        sed -e "s|^GG_DB_PASSWORD=.*|GG_DB_PASSWORD=$(secret 40)|" \
            -e "s|^GG_DB_ADMIN_PASSWORD=.*|GG_DB_ADMIN_PASSWORD=$(secret 40)|" \
            -e "s|^GG_PROXY_NETWORK=.*|GG_PROXY_NETWORK=$network|" \
            -e "s|^GG_PROXY_ALIAS=.*|GG_PROXY_ALIAS=$alias|" \
            -e "s|^GG_BACKUP_HOST_DIR=.*|GG_BACKUP_HOST_DIR=$home/data/backups|" \
            "$repo/deploy/env/prod-shared.env.example" >"$home/.env"
    )
    echo "$home/.env written with fresh secrets"
fi

say "Deployment key (~/.ssh/authorized_keys)"
install -d -m 700 ~/.ssh
keys=~/.ssh/authorized_keys
touch "$keys"
chmod 600 "$keys"
grep -v 'gg-deploy prod ' "$keys" >"$keys.new" || true
echo "restrict,command=\"$home/bin/gg-deploy prod $home\" $key" >>"$keys.new"
mv "$keys.new" "$keys"
# sshd ignores a key file others may write to.
chmod 600 "$keys"
echo "key pinned to $home/bin/gg-deploy (deploy, rollback, status, login only)"

say "Done. Next: GitHub Actions → « Prepare release » (DEPLOYMENT.md, section 10)"
