#!/bin/sh
# Runs inside the backup container, as root, the app stopped (called by restore.sh).
# Usage: restore-data.sh <path inside the backups directory>
set -eu

archive="${GG_BACKUP_DIR:?}/$1"
[ -f "$archive" ] || { echo "No such backup: $1" >&2; exit 1; }
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
tar -C "$work" -xf "$archive"

echo "Database: recreating $PGDATABASE"
psql -v ON_ERROR_STOP=1 -d postgres \
    -c "DROP DATABASE IF EXISTS \"$PGDATABASE\" WITH (FORCE);" \
    -c "CREATE DATABASE \"$PGDATABASE\" OWNER \"${GG_APP_DB_USER:?}\";"
# Objects belong to the API's role again, as Flyway created them.
pg_restore --exit-on-error --no-owner --role="$GG_APP_DB_USER" --dbname="$PGDATABASE" "$work/db.dump"

echo "Uploaded files: replacing"
find "${GG_MEDIA_DIR:?}" -mindepth 1 -delete
tar -C "$GG_MEDIA_DIR" -xzf "$work/media.tar.gz"
chown -R 10001:10001 "$GG_MEDIA_DIR"
echo "Done"
