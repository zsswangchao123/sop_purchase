#!/usr/bin/env bash
set -euo pipefail

cd /opt/sop_purchase/current/deploy
umask 077

if [[ ! -f .env ]]; then
  db_password="$(openssl rand -hex 24)"
  mysql_root_password="$(openssl rand -hex 24)"
  printf 'DB_PASSWORD=%s\nMYSQL_ROOT_PASSWORD=%s\n' \
    "$db_password" "$mysql_root_password" > .env
fi

docker compose -p sop_purchase up -d
docker compose -p sop_purchase ps
