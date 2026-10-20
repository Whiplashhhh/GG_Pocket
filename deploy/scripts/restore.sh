#!/usr/bin/env bash
# Restores a backup (database and uploaded files) into an environment, replacing everything.
# The API refuses to start on a backup of another environment (database marker, D-034): restore
# a prod backup into prod only.
#
# Usage: restore.sh <prod|ci> <backup> [--yes]
#   <backup>: path inside the backups directory, for instance daily/gg-pocket-2026-10-20_033000.tar
# shellcheck source=lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

GG_ENV=${1:-}
backup=${2:-}
check_env "$GG_ENV"
[[ $GG_ENV != dev ]] || die "dev has no backups: redeploy it with --reset instead"
[[ $backup =~ ^(daily|weekly|pre-deploy|manual)/gg-pocket-[0-9_-]+\.tar$ ]] || die "invalid backup name '$backup'"
export GG_IMAGE_TAG=${GG_IMAGE_TAG:-$(cat "$GG_HOME/current-tag" 2>/dev/null || true)}
[[ -n $GG_IMAGE_TAG ]] || die "no deployed version recorded in $GG_HOME"

if [[ ${3:-} != --yes ]]; then
    read -r -p "Replace ALL the $GG_ENV data with $backup? Type the environment name to confirm: " answer
    [[ $answer == "$GG_ENV" ]] || die "cancelled"
fi

log "Safety backup of the current data"
compose run --rm --no-deps --entrypoint /scripts/backup.sh backup manual

log "Stopping the app"
compose stop web api

log "Restoring $backup"
compose run --rm --no-deps --user 0 --entrypoint /scripts/restore-data.sh backup "$backup"

log "Starting the app"
compose up -d --wait --wait-timeout 300
meta
log "Restored $backup into $GG_ENV"
