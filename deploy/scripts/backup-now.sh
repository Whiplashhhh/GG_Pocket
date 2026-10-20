#!/usr/bin/env bash
# Takes a backup right now, next to the nightly ones (in manual/, kept until deleted by hand).
# Usage (on the server): sudo -u deploy /opt/gg-pocket/prod/releases/<version>/scripts/backup-now.sh prod
# shellcheck source=lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

GG_ENV=${1:-}
check_env "$GG_ENV"
[[ $GG_ENV != dev ]] || die "dev has no backups"
export GG_IMAGE_TAG=${GG_IMAGE_TAG:-$(cat "$GG_HOME/current-tag" 2>/dev/null || true)}
[[ -n $GG_IMAGE_TAG ]] || die "no deployed version recorded in $GG_HOME"
compose run --rm --no-deps --entrypoint /scripts/backup.sh backup manual
