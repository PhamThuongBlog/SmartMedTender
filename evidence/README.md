# MedTender — Experimental Evidence & Replication Package

**Paper:** MedTender: An AI-Driven Platform for Automated Medical Tender Document Processing, Smart Product Matching, and Bid Proposal Generation in Vietnamese Healthcare Procurement

**Target Journal:** Software Quality Journal (Springer, ISSN: 0963-9314)

**Date:** June 2026

---

## Directory Structure

```
evidence/
├── README.md                              ← This file
├── DATA_TRANSPARENCY_STATEMENT.md         ← What's real vs. simulated
├── REPLICATION_GUIDE.md                   ← How to reproduce results
├── 01-uat-test-suite/                     ← UAT test scripts & sample output
│   ├── uat-v2.mjs                         (Node.js, 57 API tests)
│   ├── uat-api-test.mjs                   (Node.js, 46 API tests)
│   ├── uat-ui.mjs                         (Playwright, 17 UI tests)
│   └── sample-output.log                 (Expected console output)
├── 02-database-schema/                    ← Flyway migrations
│   ├── schema-summary.md                  (26 tables in 10 groups)
│   ├── V1__init_schema.sql               (Full schema)
│   ├── V2__add_audit_columns.sql
│   ├── V3__enhance_enterprise_profile.sql
│   ├── V4__expiry_alert_table.sql
│   ├── V5__seed_test_data.sql
│   └── V6__seed_price_history.sql
├── 03-seed-data/                          ← Test dataset details
│   ├── dataset-manifest.md               (5 products, 27 prices, 10 docs, 1 tender)
│   ├── products.json                     (Product catalog)
│   └── price-history.json                (Price history data)
├── 04-performance-benchmarks/             ← Performance metrics
│   ├── benchmark-script.mjs              (Automated benchmark runner)
│   └── performance-results.md            (Tabulated results)
├── 05-unit-tests/                         ← JUnit/Spring Boot tests
│   ├── test-classes.md                   (8 test classes)
│   └── sample-junit-output.txt
└── 06-source-code-metrics/                ← Codebase statistics
    └── code-metrics.md                   (186 Java files, 19 Vue components, etc.)
```

## Quick Start

To reproduce the UAT tests:
```bash
cd D:/NCKH_2027/Tien_ChuHai/MedTenderSystem_VERSION2/Code/SmartMedTender

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

## Important: Read DATA_TRANSPARENCY_STATEMENT.md

Certain results in the paper (user study N=15, baseline comparison with 2 experts, multi-enterprise dataset of 58 products/73 tenders) are **constructed/illustrative data** used to demonstrate the paper's contribution and cannot be reproduced by running the software alone. See `DATA_TRANSPARENCY_STATEMENT.md` for a complete breakdown.
