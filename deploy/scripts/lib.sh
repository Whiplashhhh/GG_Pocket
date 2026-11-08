#!/usr/bin/env bash
# Shared by the host-side scripts (deploy.sh, rollback.sh, restore.sh). Not meant to run alone.
#
# On the server, one directory per environment (DEPLOYMENT.md), /opt/gg-pocket/<env> or, on the
# club's server, /srv/ggpocket (D-126):
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
    gg_stderr=$(mktemp)
    exec 2> >(tee -a "$gg_stderr" >&2)
    report_failure() {
        local where output
        where="$(basename "$0") lines ${BASH_LINENO[*]}"
        output=$(tail -n 15 "$gg_stderr" | cut -c1-300)
        output=${output//'%'/'%25'}
        echo "::error title=$where::$1%0A${output//$'\n'/'%0A'}"
    }
    set -E
    trap 'report_failure "$BASH_COMMAND"' ERR
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

# Behind a reverse proxy shared with other apps (the club's server, D-126): its Docker network.
proxy_network() {
    env_value GG_PROXY_NETWORK
}

compose() {
    local profiles=() files=(-f "$GG_STACK_DIR/compose.yml" -f "$GG_STACK_DIR/compose.$GG_ENV.yml")
    [[ $GG_ENV == dev ]] || profiles=(--profile backup)
    [[ -z $(proxy_network) ]] || files+=(-f "$GG_STACK_DIR/compose.proxy.yml")
    docker compose "${files[@]}" --env-file "$GG_ENV_FILE" "${profiles[@]}" "$@"
}

# Prints /api/v1/meta as served through the web container, like a phone would get it: on the
# loopback port, or from inside the web container when no port is published (shared proxy).
meta() {
    if [[ -n $(proxy_network) ]]; then
        compose exec -T web wget -q -O - -T 10 http://127.0.0.1:8080/api/v1/meta
    else
        curl -fsS --max-time 10 "http://127.0.0.1:$(env_value GG_WEB_PORT)/api/v1/meta"
    fi
}

json_field() {
    sed -n "s/.*\"$1\":\"\([^\"]*\)\".*/\1/p"
}
