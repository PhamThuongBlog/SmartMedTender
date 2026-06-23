# MedTender Replication Guide

## Prerequisites

- Java 17 (OpenJDK)
- Docker Desktop (for PostgreSQL, Kafka, Redis, Zookeeper)
- Node.js 18+ (for UAT test scripts)
- Maven Wrapper (included: `./mvnw`)
- Tesseract OCR 5.x (optional — for OCR processing tests)

## Step 1: Start Infrastructure

```bash
cd Code/SmartMedTender
docker compose up -d
```

Wait for all services to be healthy:
```bash
docker compose ps
# Expected: postgres (healthy), zookeeper (healthy), kafka (healthy), redis (healthy)
```

## Step 2: Build the Application

```bash
cd Code/SmartMedTender
./mvnw clean install -DskipTests
```

## Step 3: Run Database Migrations + Seed Data

Start the application (Flyway runs on startup):
```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8082`. Flyway automatically executes V1–V6 migrations on first run, creating 26 tables and seeding test data.

Verify:
```bash
curl http://localhost:8082/
# Expected: {"status":"UP","message":"MedTender System is running"}
```

## Step 4: Run UAT Test Suite

In a separate terminal:

```bash
cd Code/SmartMedTender/frontend
node uat-v2.mjs
```

Expected output:
```
🔐 LOGIN
  ✅ Login admin (XXms) — OK

📋 F1: THIẾT LẬP BAN ĐẦU
  ✅ F1.1 GET enterprises (XXms) — X enterprises
  ✅ F1.2 PUT enterprise/profile (XXms) — MedTender UAT Corp
  ✅ F1.3 POST product #1 (XXms) — May sieu am 4D GE Voluson E10
  ...

============================================================
UAT RESULTS: XX PASS, 0 FAIL — XX TOTAL
============================================================
```

## Step 5: Run Unit Tests

```bash
cd Code/SmartMedTender
./mvnw test
```

Expected: 8 test classes, all passing.

## Step 6: Run Performance Benchmark

```bash
cd Code/SmartMedTender/frontend
node ../sn-article-template/evidence/04-performance-benchmarks/benchmark-script.mjs
```

## Step 7: Collect All Evidence

After running all tests, copy outputs:
```bash
# Copy UAT output
node uat-v2.mjs > ../sn-article-template/evidence/01-uat-test-suite/sample-output.log 2>&1

# Copy JUnit results
cp target/surefire-reports/*.xml ../sn-article-template/evidence/05-unit-tests/

# Copy benchmark results
node benchmark-script.mjs > ../sn-article-template/evidence/04-performance-benchmarks/benchmark-results.csv
```
