#!/usr/bin/env bash
# One-time GitHub setup of the deployments (DEPLOYMENT.md, step 3), run by a repository admin on
# their own machine with the GitHub CLI logged in (gh auth login). Idempotent.
#
#   .github/scripts/setup-github.sh --host 51.38.12.34 [--prod-url https://...] [--dev-url https://...]
#
# - environments "dev" and "production" (production: approval by the admin running this, from
#   main and v* tags only);
# - one SSH key pair per environment: private key into the environment secret DEPLOY_SSH_KEY,
#   public key printed for bootstrap.sh, nothing left on disk;
# - variables DEPLOY_HOST, DEPLOY_KNOWN_HOSTS (after you check the server fingerprint), GG_URL;
# - rulesets: main requires the new CI jobs too; prod/* branches and v* tags only by the release workflow;
# - label deploy:dev.
set -euo pipefail

repo=${GG_REPO:-Whiplashhhh/GG_Pocket}
host='' prod_url='' dev_url=''
while [[ $# -gt 0 ]]; do
    case $1 in
        --host) host=$2; shift 2 ;;
        --prod-url) prod_url=$2; shift 2 ;;
        --dev-url) dev_url=$2; shift 2 ;;
        *) echo "unknown option $1" >&2; exit 1 ;;
    esac
done
[[ $host =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "--host <IPv4 of the server> is required" >&2; exit 1; }
prod_url=${prod_url:-https://pocket.${host//./-}.sslip.io}
dev_url=${dev_url:-https://dev.pocket.${host//./-}.sslip.io}
say() { printf '\n==> %s\n' "$*"; }

say "Server fingerprint"
known_hosts=$(ssh-keyscan -t ed25519 "$host" 2>/dev/null)
[[ -n $known_hosts ]] || { echo "no answer from $host on port 22" >&2; exit 1; }
ssh-keygen -lf <(echo "$known_hosts")
echo "Compare with, on the server: ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub"
read -r -p "Same fingerprint? [y/N] " same
[[ $same == [yYoO]* ]] || { echo "stopped: check the server address" >&2; exit 1; }

say "Environments"
admin_id=$(gh api user --jq .id)
gh api -X PUT "repos/$repo/environments/dev" --silent
gh api -X PUT "repos/$repo/environments/production" --input - --silent <<JSON
{"wait_timer": 0, "prevent_self_review": false, "reviewers": [{"type": "User", "id": $admin_id}],
 "deployment_branch_policy": {"protected_branches": false, "custom_branch_policies": true}}
JSON
existing=$(gh api "repos/$repo/environments/production/deployment-branch-policies" --jq '.branch_policies[] | "\(.type):\(.name)"')
grep -qx 'branch:main' <<<"$existing" ||
    gh api -X POST "repos/$repo/environments/production/deployment-branch-policies" -f name=main -f type=branch --silent
grep -qx 'tag:v\*' <<<"$existing" ||
    gh api -X POST "repos/$repo/environments/production/deployment-branch-policies" -f 'name=v*' -f type=tag --silent

say "Deployment keys (private keys go straight to GitHub, never stay on disk)"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
declare -A public_keys
for env in dev production; do
    short=${env/production/prod}
    ssh-keygen -q -t ed25519 -N '' -C "gg-pocket-$short" -f "$work/$short"
    gh secret set DEPLOY_SSH_KEY --repo "$repo" --env "$env" <"$work/$short"
    public_keys[$short]=$(cat "$work/$short.pub")
    url=$dev_url
    [[ $env == dev ]] || url=$prod_url
    gh variable set DEPLOY_HOST --repo "$repo" --env "$env" --body "$host"
    gh variable set DEPLOY_KNOWN_HOSTS --repo "$repo" --env "$env" --body "$known_hosts"
    gh variable set GG_URL --repo "$repo" --env "$env" --body "$url"
done

say "Rulesets"
main_id=$(gh api "repos/$repo/rulesets" --jq '.[] | select(.name == "main") | .id')
if [[ -n $main_id ]]; then
    gh api "repos/$repo/rulesets/$main_id" | jq '
      {name, target, enforcement, conditions, bypass_actors,
       rules: [.rules[] | if .type == "required_status_checks" then
         .parameters.required_status_checks |= (. + [
           {context: "Deploy config (shellcheck, compose, Caddy)", integration_id: 15368},
           {context: "Docker stack (images, Trivy, e2e, backup and restore)", integration_id: 15368}
         ] | unique_by(.context)) else . end]}' |
        gh api -X PUT "repos/$repo/rulesets/$main_id" --input - --silent
    echo "main: CI jobs Deploy config and Docker stack now required"
fi
for target in branch tag; do
    if [[ $target == branch ]]; then name="prod branches (release workflow only)" pattern="refs/heads/prod/**"
    else name="release tags (release workflow only)" pattern="refs/tags/v*"; fi
    gh api "repos/$repo/rulesets" --jq '.[].name' | grep -qxF "$name" && continue
    ruleset() {
        cat <<JSON
{"name": "$name", "target": "$target", "enforcement": "active",
 "conditions": {"ref_name": {"include": ["$pattern"], "exclude": []}},
 "rules": [$1{"type": "update"}, {"type": "deletion"}, {"type": "non_fast_forward"}],
 "bypass_actors": [$2]}
JSON
    }
    actions='{"actor_id": 15368, "actor_type": "Integration", "bypass_mode": "always"}'
    if ! ruleset '{"type": "creation"}, ' "$actions" | gh api -X POST "repos/$repo/rulesets" --input - --silent; then
        # Some accounts cannot name GitHub Actions as a bypass: creation stays open, the rest is locked.
        echo "GitHub Actions cannot bypass rulesets here: $name created without the creation rule"
        ruleset '' '' | gh api -X POST "repos/$repo/rulesets" --input - --silent
    fi
    echo "$name: created"
done

gh label create deploy:dev --repo "$repo" --color 0E8A16 --description "Déploie la PR sur l'environnement dev" --force >/dev/null

say "Done. On the server, as root:"
cat <<NEXT
git clone https://github.com/$repo.git /tmp/gg-pocket
sudo /tmp/gg-pocket/deploy/scripts/server/bootstrap.sh --firewall \\
  --dev-key '${public_keys[dev]}' \\
  --prod-key '${public_keys[prod]}'
NEXT
