# MedTender — Experimental Evidence & Replication Package

## Quick Start

To reproduce the UAT tests:
```bash
cd <path of SmartMedTender>

# Start infrastructure
docker compose up -d

# Start application
./mvnw spring-boot:run

# Run UAT tests (in another terminal)
cd frontend
node uat-v2.mjs
```

## Key Results Summary

| Metric | Value | Evidence File |
|--------|-------|---------------|
| UAT test cases | 57 (uat-v2) + 46 (uat-api) + 17 (uat-ui) | `01-uat-test-suite/` |
| Database tables | 26 (verified in V1__init_schema.sql) | `02-database-schema/` |
| Seed products | 5 (V5__seed_test_data.sql) | `03-seed-data/` |
| Price history records | 27 (V6__seed_price_history.sql) | `03-seed-data/` |
| Flyway migrations | 6 (V1–V6) | `02-database-schema/` |
| Java source files | 186 (.java) | `06-source-code-metrics/` |
| Vue components | 19 (.vue) | `06-source-code-metrics/` |
| Unit test classes | 8 | `05-unit-tests/` |
