#!/bin/bash
set -euo pipefail

: "${DB_HOST:?DB_HOST not set}"
: "${DB_PORT:?DB_PORT not set}"
: "${DB_USERNAME:?DB_USERNAME not set}"
: "${DB_NAME:?DB_NAME not set}"
: "${DB_PASSWORD:?DB_PASSWORD not set}"
: "${REDIS_HOST:?REDIS_HOST not set}"
: "${REDIS_PORT:?REDIS_PORT not set}"
: "${S3_ACCESS_KEY:?S3_ACCESS_KEY not set}"
: "${S3_SECRET_KEY:?S3_SECRET_KEY not set}"
: "${MINIO_ENDPOINT:?MINIO_ENDPOINT not set}"

NAMESPACE="agentos"
BACKUP_BUCKET="agentos-backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/tmp/agentos-backup-${TIMESTAMP}"

echo "=== AgentOS Backup: ${TIMESTAMP} ==="

mkdir -p "$BACKUP_DIR"

echo "Backing up PostgreSQL..."
PGPASSWORD="${DB_PASSWORD}" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -F c -b -f "${BACKUP_DIR}/postgres_${TIMESTAMP}.dump"

echo "Backing up Redis..."
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" --rdb "${BACKUP_DIR}/redis_${TIMESTAMP}.rdb"

if command -v mc &>/dev/null; then
    echo "Uploading backups to MinIO..."
    mc alias set agentos "http://${MINIO_ENDPOINT}" "${S3_ACCESS_KEY}" "${S3_SECRET_KEY}" 2>/dev/null
    mc cp "${BACKUP_DIR}/postgres_${TIMESTAMP}.dump" "agentos/${BACKUP_BUCKET}/"
    mc cp "${BACKUP_DIR}/redis_${TIMESTAMP}.rdb" "agentos/${BACKUP_BUCKET}/"
    echo "Backups stored in MinIO bucket: ${BACKUP_BUCKET}"
else
    echo "WARNING: 'mc' command not found, backups saved to ${BACKUP_DIR}"
fi

echo "Backup completed: ${TIMESTAMP}"