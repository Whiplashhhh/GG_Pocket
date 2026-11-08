#!/usr/bin/env bash
# Points the GitHub "production" environment at a server whose reverse proxy is shared with other
# apps and where the app runs under a personal account (the club's server, DECISIONS D-126). Run it
# on your own machine, with the GitHub CLI logged in as an admin of the repository:
#
#   .github/scripts/setup-prod-shared.sh --host 51.222.157.180 --user willem \
#     --url https://ggpocket.gravelinesgrimp.org
#
# It checks the server's fingerprint, generates a new deployment key (the private half goes
# straight into the environment's DEPLOY_SSH_KEY secret and is never written elsewhere), sets
# DEPLOY_HOST, DEPLOY_KNOWN_HOSTS, DEPLOY_USER and GG_URL, and prints the command to run on the
# server (deploy/scripts/server/setup-shared.sh). The dev environment is left alone.
set -euo pipefail

repo=${GG_REPO:-Whiplashhhh/GG_Pocket}
host='' user='' url=''
while [[ $# -gt 0 ]]; do
    case $1 in
        --host) host=$2; shift 2 ;;
        --user) user=$2; shift 2 ;;
        --url) url=$2; shift 2 ;;
        *) echo "unknown option $1" >&2; exit 1 ;;
    esac
done
[[ $host =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "--host <IPv4 of the server> is required" >&2; exit 1; }
[[ $user =~ ^[a-z_][a-z0-9_-]*$ ]] || { echo "--user <account on the server> is required" >&2; exit 1; }
[[ $url =~ ^https://[a-z0-9.-]+$ ]] || { echo "--url https://<address of the app> is required" >&2; exit 1; }

say() { printf '\n==> %s\n' "$*"; }
for tool in gh ssh-keyscan ssh-keygen; do
    command -v "$tool" >/dev/null || { echo "missing tool: $tool (run this on your own machine)" >&2; exit 1; }
done
gh auth status >/dev/null 2>&1 || { echo "gh is not logged in: gh auth login" >&2; exit 1; }

say "Server fingerprint"
known_hosts=$(ssh-keyscan -T 10 -t ed25519 "$host" 2>/dev/null || true)
[[ -n $known_hosts ]] || { echo "no answer from $host on port 22" >&2; exit 1; }
ssh-keygen -lf <(echo "$known_hosts")
echo "Compare with, on the server: ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub"
read -r -p "Same fingerprint? [y/N] " same
[[ $same == [yYoO]* ]] || { echo "stopped: check the server address" >&2; exit 1; }

say "Deployment key of the production environment (replaces the previous one)"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
ssh-keygen -q -t ed25519 -N '' -C gg-pocket-prod -f "$work/prod"
gh secret set DEPLOY_SSH_KEY --repo "$repo" --env production <"$work/prod"
gh variable set DEPLOY_HOST --repo "$repo" --env production --body "$host"
gh variable set DEPLOY_KNOWN_HOSTS --repo "$repo" --env production --body "$known_hosts"
gh variable set DEPLOY_USER --repo "$repo" --env production --body "$user"
gh variable set GG_URL --repo "$repo" --env production --body "$url"

say "Done. On the server, as $user (no sudo):"
cat <<NEXT
git clone https://github.com/$repo.git /tmp/gg-pocket
/tmp/gg-pocket/deploy/scripts/server/setup-shared.sh --key '$(cat "$work/prod.pub")'
rm -rf /tmp/gg-pocket
NEXT
