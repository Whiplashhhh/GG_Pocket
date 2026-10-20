#!/usr/bin/env bash
# Prepares a fresh server for GG Pocket, once, as root (DEPLOYMENT.md, step 2). Idempotent:
# running it again keeps the existing secrets and only refreshes the gate and the edge proxy.
#
#   git clone https://github.com/Whiplashhhh/GG_Pocket.git /tmp/gg-pocket
#   sudo /tmp/gg-pocket/deploy/scripts/server/bootstrap.sh \
#       --dev-key 'ssh-ed25519 AAAA... gg-pocket-dev' --prod-key 'ssh-ed25519 AAAA... gg-pocket-prod'
#
# Options:
#   --dev-key KEY, --prod-key KEY   public keys of the GitHub deployments (printed by Claude / gh)
#   --prod-domain NAME              default: pocket.<ipv4 with dashes>.sslip.io
#   --dev-domain NAME               default: dev.pocket.<ipv4 with dashes>.sslip.io
#   --firewall                      also enable ufw with only SSH, HTTP and HTTPS open
set -euo pipefail

repo=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)
dev_key='' prod_key='' prod_domain='' dev_domain='' firewall=''
while [[ $# -gt 0 ]]; do
    case $1 in
        --dev-key) dev_key=$2; shift 2 ;;
        --prod-key) prod_key=$2; shift 2 ;;
        --prod-domain) prod_domain=$2; shift 2 ;;
        --dev-domain) dev_domain=$2; shift 2 ;;
        --firewall) firewall=1; shift ;;
        *) echo "unknown option $1" >&2; exit 1 ;;
    esac
done

say() { printf '\n==> %s\n' "$*"; }
[[ $EUID -eq 0 ]] || { echo "run as root (sudo)" >&2; exit 1; }
command -v docker >/dev/null || { echo "Docker is not installed" >&2; exit 1; }
docker compose version >/dev/null || { echo "the Docker Compose plugin is missing" >&2; exit 1; }

ip=$(ip -4 route get 1.1.1.1 | sed -n 's/.* src \([0-9.]*\).*/\1/p')
prod_domain=${prod_domain:-pocket.${ip//./-}.sslip.io}
dev_domain=${dev_domain:-dev.pocket.${ip//./-}.sslip.io}

say "User deploy (Docker only, through the gg-deploy gate)"
id deploy >/dev/null 2>&1 || useradd --create-home --shell /bin/bash deploy
passwd -l deploy >/dev/null
usermod -aG docker deploy
install -m 755 -o root -g root "$repo/deploy/scripts/server/gg-deploy" /usr/local/bin/gg-deploy

# Root owns the key list: the deploy user cannot add a key without a gate.
install -d -m 755 -o root -g root /home/deploy/.ssh
keys=/home/deploy/.ssh/authorized_keys
touch "$keys"
cp "$keys" "$keys.new"
# Only the environments given a new key lose their old one.
[[ -z $dev_key ]] || sed -i '/gg-deploy dev"/d' "$keys.new"
[[ -z $prod_key ]] || sed -i '/gg-deploy prod"/d' "$keys.new"
[[ -z $dev_key ]] || echo "restrict,command=\"/usr/local/bin/gg-deploy dev\" $dev_key" >>"$keys.new"
[[ -z $prod_key ]] || echo "restrict,command=\"/usr/local/bin/gg-deploy prod\" $prod_key" >>"$keys.new"
install -m 644 -o root -g root "$keys.new" "$keys"
rm -f "$keys.new"

say "Directories in /opt/gg-pocket"
install -d -m 755 -o deploy -g deploy /opt/gg-pocket /opt/gg-pocket/edge /opt/gg-pocket/edge/dev.d
for env in dev prod; do
    install -d -m 750 -o deploy -g deploy "/opt/gg-pocket/$env" "/opt/gg-pocket/$env/releases"
done
# uid 70 = postgres in the backup container; nobody else reads the backups.
install -d -m 700 -o 70 -g 70 /opt/gg-pocket/prod/backups

secret() { openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c "$1"; }
write_env() {
    local env=$1 file=/opt/gg-pocket/$1/.env
    if [[ -f $file ]]; then
        echo "$file exists: kept"
        return
    fi
    sed -e "s|^GG_DB_PASSWORD=.*|GG_DB_PASSWORD=$(secret 40)|" \
        -e "s|^GG_DB_ADMIN_PASSWORD=.*|GG_DB_ADMIN_PASSWORD=$(secret 40)|" \
        -e "s|^GG_SEED_PASSWORD=.*|GG_SEED_PASSWORD=$(secret 20)|" \
        "$repo/deploy/env/$env.env.example" >"$file"
    chown deploy:deploy "$file"
    chmod 600 "$file"
    echo "$file written with fresh secrets"
}
write_env dev
write_env prod

say "Edge proxy ($prod_domain, $dev_domain)"
cp "$repo/deploy/edge/Caddyfile" "$repo/deploy/edge/compose.yml" /opt/gg-pocket/edge/
printf 'GG_PROD_DOMAIN=%s\nGG_DEV_DOMAIN=%s\n' "$prod_domain" "$dev_domain" >/opt/gg-pocket/edge/.env
[[ -f /opt/gg-pocket/edge/dev.d/README.caddy ]] ||
    echo '# Extra rules for the dev site (basic authentication, see DEPLOYMENT.md).' >/opt/gg-pocket/edge/dev.d/README.caddy
chown -R deploy:deploy /opt/gg-pocket/edge
(cd /opt/gg-pocket/edge && docker compose up -d --wait)

if [[ -n $firewall ]]; then
    say "Firewall: SSH, HTTP and HTTPS only"
    ufw allow OpenSSH
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw --force enable
fi

say "Ready"
cat <<SUMMARY
  Prod: https://$prod_domain    Dev: https://$dev_domain
  Demo accounts password (dev): sudo grep GG_SEED_PASSWORD /opt/gg-pocket/dev/.env
  Next: the first deployment comes from GitHub (DEPLOYMENT.md, step 4).
SUMMARY
