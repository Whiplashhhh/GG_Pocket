#!/usr/bin/env bash
# Deploys one version of one environment (spec §15.2, step 8): backup first in prod, pull, start,
# wait for the healthchecks, smoke test /api/v1/meta, and go back to the previous version if any
# of it fails.
#
# Usage: deploy.sh <dev|prod|ci> <image-tag> [--reset]
#   --reset (dev and ci only): deletes the database and the uploaded files first; the demo seed
#   fills them again on start.
# shellcheck source=lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

GG_ENV=${1:-}
tag=${2:-}
reset=${3:-}
check_env "$GG_ENV"
check_tag "$tag"
[[ -z $reset || $reset == --reset ]] || die "unknown option '$reset'"
[[ -f $GG_ENV_FILE ]] || die "missing $GG_ENV_FILE (see DEPLOYMENT.md)"
export GG_IMAGE_TAG=$tag

previous=$(cat "$GG_HOME/current-tag" 2>/dev/null || true)
# Redeploying the version in place: the way back stays the one before it.
[[ $previous != "$tag" ]] || previous=$(cat "$GG_HOME/previous-tag" 2>/dev/null || true)

roll_back() {
    if [[ $GG_ENV == prod ]]; then
        # Never in the job's public log: members' data, or the first start admin code.
        log "Deployment of $tag failed: logs of the API on the server (sudo docker logs gg-pocket-prod-api-1)"
    else
        log "Deployment of $tag failed: logs of the API"
        compose logs --no-color --tail 120 api | grep -iv 'registration code' || true
    fi
    if [[ -n ${GG_ROLLING_BACK:-} || -z $previous || $previous == "$tag" ]]; then
        die "no previous version to go back to"
    fi
    local release="$GG_HOME/releases/$previous"
    [[ -x $release/scripts/deploy.sh ]] || die "previous release $previous not found in $release"
    log "Going back to $previous"
    GG_ROLLING_BACK=1 "$release/scripts/deploy.sh" "$GG_ENV" "$previous" || die "rollback to $previous failed too"
    die "$tag was not deployed; $previous is running again"
}

if [[ $reset == --reset ]]; then
    [[ $GG_ENV != prod ]] || die "the production database is never reset"
    log "Reset: removing the $GG_ENV database and uploaded files"
    compose down --volumes --remove-orphans
elif [[ $GG_ENV == prod && -n $(compose ps --status running --quiet db 2>/dev/null) ]]; then
    log "Backup before deploying"
    compose run --rm --no-deps --entrypoint /scripts/backup.sh backup pre-deploy
fi

# Tags are immutable (a version, or dev-<commit>): an image already here is never pulled again,
# so a rollback from the server works after the job's registry token has expired.
missing=0
for image in $(compose config --images | grep -E '/gg-pocket-(api|web):'); do
    docker image inspect "$image" >/dev/null 2>&1 || missing=1
done
if [[ -z ${GG_SKIP_PULL:-} && $missing == 1 ]]; then
    log "Pulling images $tag"
    compose pull --quiet web api || roll_back
fi

log "Starting $tag"
compose up -d --remove-orphans --wait --wait-timeout 300 || roll_back

log "Smoke test"
answer=$(meta) || roll_back
echo "$answer"
case $tag in
    dev-*) [[ $(json_field commit <<<"$answer") == "${tag#dev-}"* ]] || roll_back ;;
    ci) [[ $(json_field environment <<<"$answer") == dev ]] || roll_back ;;
    *) [[ $(json_field version <<<"$answer") == "$tag" ]] || roll_back ;;
esac

if [[ $(cat "$GG_HOME/current-tag" 2>/dev/null || true) != "$tag" ]]; then
    [[ -z $previous ]] || echo "$previous" >"$GG_HOME/previous-tag"
    echo "$tag" >"$GG_HOME/current-tag"
fi

if [[ -d $GG_HOME/releases ]]; then
    # Keep the files and images of this version and the previous one (rollback without pulling:
    # the registry token of the deployment job has expired by then). Older ones of this
    # environment go; the other environment's images are left alone.
    ls -1t "$GG_HOME/releases" | tail -n +3 | while read -r old; do
        [[ $old == "$tag" || $old == "$previous" ]] || rm -rf -- "${GG_HOME:?}/releases/$old"
    done
    pattern='^[0-9]+\.[0-9]+\.[0-9]+$'
    [[ $GG_ENV != dev ]] || pattern='^dev-'
    docker image ls --format '{{.Repository}}:{{.Tag}}' | grep -E '/gg-pocket-(api|web):' |
        while read -r image; do
            image_tag=${image##*:}
            if [[ $image_tag =~ $pattern && $image_tag != "$tag" && $image_tag != "$previous" ]]; then
                docker image rm "$image" >/dev/null || true
            fi
        done
fi

log "$GG_ENV is running $tag"
