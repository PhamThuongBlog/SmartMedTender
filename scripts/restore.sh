#!/bin/bash
set -e

BACKUP_DIR="${BACKUP_DIR:-./backups}"

echo "=== MedTender Database Restore ==="

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-medbid_db}"
DB_USER="${DB_USER:-postgres}"
DB_PASS="${DB_PASS:-123456}"

if [ -z "$1" ]; then
    echo "Available backups:"
    ls -1t "$BACKUP_DIR"/backup_*.sql 2>/dev/null || echo "No backups found in $BACKUP_DIR"
    echo ""
    echo "Usage: $0 <backup-file>"
    echo "Example: $0 $BACKUP_DIR/backup_20260518_020000.sql"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: Backup file not found: $BACKUP_FILE"
    exit 1
fi

echo "WARNING: This will overwrite the database '$DB_NAME' on $DB_HOST:$DB_PORT"
echo "Backup file: $BACKUP_FILE"
echo ""
read -p "Are you sure? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

echo "Restoring from $BACKUP_FILE..."

PGPASSWORD="$DB_PASS" pg_restore \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -c \
    "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "Restore successful!"
else
    echo "Restore failed!"
    exit 1
fi
