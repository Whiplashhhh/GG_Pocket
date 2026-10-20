#!/bin/sh
# Healthcheck of the backup service: unhealthy when the last daily backup is more than 26 hours
# old (disk full, database unreachable…), so it shows in "docker ps" and "gg-deploy status".
# Healthy during the first 26 hours after the service starts, before its first night.
set -eu
[ -n "$(find /tmp/backup-loop-started -mmin -1560 2>/dev/null)" ] && exit 0
[ -n "$(find "${GG_BACKUP_DIR:?}/daily" -name 'gg-pocket-*.tar' -mmin -1560 2>/dev/null)" ]
