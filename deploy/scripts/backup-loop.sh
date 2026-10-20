#!/bin/sh
# Entry point of the backup service: one daily backup at GG_BACKUP_AT (default 03:30, Paris time).
set -eu

at=${GG_BACKUP_AT:-03:30}
echo "Daily backups at $at ($(date +%Z))"
while true; do
    now=$(date +%s)
    next=$(date -d "$(date +%Y-%m-%d) $at" +%s)
    [ "$next" -gt "$now" ] || next=$((next + 86400))
    sleep $((next - now))
    /scripts/backup.sh daily || echo "Daily backup FAILED" >&2
done
