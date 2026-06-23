#!/bin/bash
set -e

BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/backup_$TIMESTAMP.sql"

echo "=== MedTender Database Backup ==="
echo "Started at: $(date)"

mkdir -p "$BACKUP_DIR"

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-medbid_db}"
DB_USER="${DB_USER:-postgres}"
DB_PASS="${DB_PASS:-123456}"

echo "Backing up $DB_NAME@$DB_HOST:$DB_PORT to $BACKUP_FILE..."

PGPASSWORD="$DB_PASS" pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -F c \
    -f "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "Backup successful: $BACKUP_FILE ($SIZE)"
else
    echo "Backup failed!"
    exit 1
fi

# Cleanup old backups (keep last 30 days)
find "$BACKUP_DIR" -name "backup_*.sql" -mtime +30 -delete 2>/dev/null || true

echo "Backup completed at: $(date)"
