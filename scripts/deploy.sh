#!/bin/bash
set -e

echo "=== MedTender V2 Deployment Script ==="
echo "Started at: $(date)"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

if [ -f .env ]; then
    echo "Loading .env file..."
    export $(grep -v '^#' .env | xargs)
fi

echo ""
echo "[1/4] Building backend..."
./mvnw clean package -DskipTests -q
echo "Backend build complete."

echo ""
echo "[2/4] Building Docker image..."
docker build -t smartmedtender:latest .
docker tag smartmedtender:latest "smartmedtender:v$(date +%Y%m%d-%H%M%S)"
echo "Docker image built."

echo ""
echo "[3/4] Stopping existing containers..."
docker compose down 2>/dev/null || true

echo ""
echo "[4/4] Starting services..."
docker compose up -d

echo ""
echo "Waiting for services to start..."
sleep 10

echo ""
echo "Checking health..."
for i in {1..10}; do
    if curl -sf http://localhost:8082/actuator/health > /dev/null 2>&1; then
        echo "Backend is healthy!"
        break
    fi
    echo "Waiting... ($i/10)"
    sleep 5
done

echo ""
echo "=== Deployment complete at $(date) ==="
docker compose ps
