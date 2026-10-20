#!/bin/sh
# Runs inside the backup container. One file per backup, database and uploaded files together:
#   $GG_BACKUP_DIR/<kind>/gg-pocket-<date>.tar = db.dump (pg_dump custom format) + media.tar.gz
# Retention (spec §16): 14 daily, 8 weekly (Sunday's daily), 10 pre-deploy, 5 pre-restore;
# manual ones stay. A backup never overwrites another one, even within the same second.
#
# Usage: backup.sh [daily|pre-deploy|pre-restore|manual]
set -eu

kind=${1:-manual}
case $kind in
    daily) keep=14 ;;
    pre-deploy) keep=10 ;;
    pre-restore) keep=5 ;;
    manual) keep=0 ;;
    *) echo "Unknown backup kind: $kind" >&2; exit 2 ;;
esac

root=${GG_BACKUP_DIR:?}
umask 077
stamp=gg-pocket-$(date +%Y-%m-%d_%H%M%S)
name=$stamp.tar
suffix=1
while [ -e "$root/$kind/$name" ]; do
    suffix=$((suffix + 1))
    name=$stamp-$suffix.tar
done
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

pg_dump --format=custom --no-owner --file="$work/db.dump"
tar -C "${GG_MEDIA_DIR:?}" -czf "$work/media.tar.gz" .
mkdir -p "$root/$kind"
tar -C "$work" -cf "$root/$kind/.$name.part" db.dump media.tar.gz
mv "$root/$kind/.$name.part" "$root/$kind/$name"
echo "Backup written: $kind/$name ($(du -h "$root/$kind/$name" | cut -f1))"

# Keeps the newest $2 backups of directory $1 (names sort by date).
prune() {
    ls -1 "$1"/gg-pocket-*.tar 2>/dev/null | sort -r | tail -n +"$(($2 + 1))" | while read -r old; do
        rm -f -- "$old"
    done
}

if [ "$kind" = daily ] && [ "$(date +%u)" = 7 ]; then
    mkdir -p "$root/weekly"
    cp "$root/daily/$name" "$root/weekly/$name"
    prune "$root/weekly" 8
fi
[ "$keep" -eq 0 ] || prune "$root/$kind" "$keep"
