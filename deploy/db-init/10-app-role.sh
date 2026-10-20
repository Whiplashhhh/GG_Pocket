#!/bin/sh
# Run once by the official PostgreSQL entrypoint, when the database volume is created: the API
# gets its own role, owner of its database but not superuser (COPY ... TO PROGRAM and the like
# stay out of reach of an SQL injection). The superuser "postgres" only serves backups/restores.
set -eu
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    --set app_user="$GG_APP_DB_USER" --set app_password="$GG_APP_DB_PASSWORD" --set db="$POSTGRES_DB" <<'SQL'
CREATE ROLE :"app_user" LOGIN PASSWORD :'app_password';
ALTER DATABASE :"db" OWNER TO :"app_user";
SQL
