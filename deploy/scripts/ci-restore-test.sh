#!/usr/bin/env bash
# CI only (spec §16, "restore.sh testé"): on the running CI stack, writes a probe row and a probe
# file, backs up, deletes both, restores with restore.sh, and checks that both came back.
# shellcheck source=lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

export GG_ENV=ci
export GG_IMAGE_TAG=ci

sql() {
    compose exec -T db psql -U "$(env_value GG_DB_USER)" -d ggpocket -v ON_ERROR_STOP=1 -tA -c "$1"
}

log "Probes"
sql "CREATE TABLE restore_probe (note text); INSERT INTO restore_probe VALUES ('avant la sauvegarde');"
compose exec -T api bash -c 'echo "prise de test" > /data/media/restore-probe.txt'
members=$(sql "SELECT count(*) FROM member;")

log "Backup"
output=$(compose run --rm --no-deps --entrypoint /scripts/backup.sh backup manual)
echo "$output"
backup=$(sed -n 's/^Backup written: \([^ ]*\) .*/\1/p' <<<"$output")
[[ -n $backup ]] || die "no backup name in the output"

log "Damage"
sql "DROP TABLE restore_probe; DELETE FROM audit_entry;"
compose exec -T api rm /data/media/restore-probe.txt

deploy_dir=$(dirname "${BASH_SOURCE[0]}")
"$deploy_dir/restore.sh" ci "$backup" --yes

log "Checks"
[[ $(sql "SELECT note FROM restore_probe;") == "avant la sauvegarde" ]] || die "probe row not restored"
[[ $(compose exec -T api cat /data/media/restore-probe.txt) == "prise de test" ]] || die "probe file not restored"
[[ $(sql "SELECT count(*) FROM member;") == "$members" ]] || die "members not restored"
log "Backup restored: database and uploaded files are back"
