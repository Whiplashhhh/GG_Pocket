#!/usr/bin/env bash
# Shared by the host-side scripts (deploy.sh, rollback.sh, restore.sh). Not meant to run alone.
#
# On the server, one directory per environment (DEPLOYMENT.md):
#   /opt/gg-pocket/<env>/.env               secrets and settings (mode 600)
#   /opt/gg-pocket/<env>/releases/<tag>/    compose files and scripts of each deployed version
#   /opt/gg-pocket/<env>/current-tag        deployed version, previous-tag the one before
# GG_HOME and GG_ENV_FILE can point elsewhere (the CI does).

set -euo pipefail

GG_STACK_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
GG_HOME=${GG_HOME:-$(cd "$GG_STACK_DIR/../.." && pwd)}
GG_ENV_FILE=${GG_ENV_FILE:-$GG_HOME/.env}

log() { printf '\n==> %s\n' "$*"; }
die() {
    printf 'ERROR: %s\n' "$*" >&2
    [[ -z ${GITHUB_ACTIONS:-} ]] || echo "::error title=$(basename "$0")::$*"
    exit 1
}

# In GitHub Actions, a failing command is also reported as an annotation (raw logs are not
# always at hand, see CLAUDE.md).
if [[ -n ${GITHUB_ACTIONS:-} ]]; then
    set -E
    trap 'echo "::error title=$(basename "$0") line $LINENO::$BASH_COMMAND"' ERR
fi

check_env() {
    case ${1:-} in
        dev | prod | ci) ;;
        *) die "unknown environment '${1:-}' (dev, prod or ci)" ;;
    esac
}

check_tag() {
    [[ ${1:-} =~ ^([0-9]+\.[0-9]+\.[0-9]+|dev-[0-9a-f]{7,40}|ci)$ ]] || die "invalid image tag '${1:-}'"
}

# Reads one value of the env file without executing it.
env_value() {
    sed -n "s/^$1=//p" "$GG_ENV_FILE" | tail -n 1
}

compose() {
    local profiles=()
    [[ $GG_ENV == dev ]] || profiles=(--profile backup)
    docker compose \
        -f "$GG_STACK_DIR/compose.yml" -f "$GG_STACK_DIR/compose.$GG_ENV.yml" \
        --env-file "$GG_ENV_FILE" "${profiles[@]}" "$@"
}

# Prints /api/v1/meta as served through the web container, like a phone would get it.
meta() {
    curl -fsS --max-time 10 "http://127.0.0.1:$(env_value GG_WEB_PORT)/api/v1/meta"
}

json_field() {
    sed -n "s/.*\"$1\":\"\([^\"]*\)\".*/\1/p"
}
