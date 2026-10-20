#!/usr/bin/env bash
# Puts the previous version of an environment back (DEPLOYMENT.md, "Revenir en arrière").
# The database stays as it is: migrations are expand/contract, so version N-1 runs on schema N
# (spec §15.4). To also go back in data, see restore.sh.
#
# Usage: rollback.sh <dev|prod>
# shellcheck source=lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

env=${1:-}
check_env "$env"
previous=$(cat "$GG_HOME/previous-tag" 2>/dev/null) || die "no previous version recorded in $GG_HOME"
release="$GG_HOME/releases/$previous"
[[ -x $release/scripts/deploy.sh ]] || die "release $previous not found in $release"

log "Rolling $env back to $previous"
GG_ROLLING_BACK=1 "$release/scripts/deploy.sh" "$env" "$previous"
