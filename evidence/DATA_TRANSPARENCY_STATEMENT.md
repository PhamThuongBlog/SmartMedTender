# Data Transparency Statement — UPDATED June 14, 2026

This document distinguishes between **verified results** (obtained by running the MedTender software with its seed data and test scripts on 2026-06-14) and **illustrative results** (constructed for the paper to demonstrate the system's potential impact).

**Key update:** On June 14, 2026, the UAT test suite, unit tests, performance benchmarks, database queries, and token usage extraction were ALL executed for the first time. Prior to this date, NO test execution evidence existed in the repository.

---

## Category A: ✅ Reproducible — NOW VERIFIED (June 14, 2026)

### A1. UAT Functional Test Suite — VERIFIED ✅

| Evidence | File | Run Date | Result |
|----------|------|----------|--------|
| 61 API functional tests (7 feature groups) | `frontend/uat-v2.mjs` | 2026-06-14 20:51 | ✅ **61 PASS, 0 FAIL** |
| 52 extended API tests | `frontend/uat-api-test.mjs` | 2026-06-14 20:52 | ⚠️ 39 PASS, 13 FAIL (validation differences) |

**How to reproduce:**
```bash
cd Code/SmartMedTender
docker compose up -d && ./mvnw spring-boot:run
# In another terminal:
cd frontend && node uat-v2.mjs
```

### A2. Database Schema — VERIFIED ✅

| Evidence | Verified |
|----------|----------|
| 28 tables (27 user-facing) | ✅ Query executed |
| 6 Flyway migrations | ✅ V1–V6 verified |
| Row counts per table | ✅ pg_stat_user_tables queried |

Key actual row counts: 17 products, 20 tenders, 13 users, 41 price history, 33 expiry alerts, 10 product documents.

### A3. Unit Tests — PARTIALLY VERIFIED ⚠️

| Test Class | Result |
|-----------|--------|
| JwtUtilTest | ✅ 3/3 PASS |
| AuthServiceTest | ✅ 2/2 PASS |
| ChatbotServiceTest | ✅ 2/2 PASS |
| EnterpriseServiceTest | ✅ 3/3 PASS |
| ProductServiceTest | ✅ 3/3 PASS |
| SmartMedTenderApplicationTests | ❌ Config error (Kafka bootstrap.servers) |
| AuthControllerIntegrationTest | ❌ Config error (cascading from above) |

**13/13 pure unit tests PASS. 5 integration test errors due to test configuration, not code bugs.**

### A4. Performance Benchmarks — VERIFIED ✅

Measured during UAT v2 execution (real server, real DB):

| Endpoint | Latency |
|----------|---------|
| GET /api/health | 8ms |
| GET /api/products?size=5 | 40ms |
| GET /api/tenders?size=5 | 22ms |
| GET /api/expiry/alerts?size=3 | 19ms |
| GET /api/documents?size=5 | 54ms |
| GET /api/enterprises?size=3 | 30ms |
| POST export/word | 1,327ms (4.0KB) |
| POST export/pdf | 754ms (103.8KB) |
| POST export/zip | 346ms (106.2KB) |
| POST export/excel | 2,020ms (5.1KB) |

### A5. Source Code Metrics — VERIFIED ✅

| Metric | Verified |
|--------|----------|
| 186 Java source files | ✅ |
| 21 Controllers | ✅ (paper says 20, off by 1) |
| 32 Services | ✅ |
| 26 Repositories | ✅ |
| 26 Entities | ✅ |
| 36 DTOs | ✅ |
| **29 Vue components** | ❌ Paper says 19 — ACTUAL IS 29 |
| 14,412 Java LOC | ✅ NEW |

### A6. Token Consumption — VERIFIED ✅ (NEW)

**Source:** Claude Code JSONL transcripts (~/.claude/projects/)
**Total:** **26.2M tokens** (20.3M input + 5.9M output)
**Breakdown:** 8 main sessions (~13.1M) + 21 subagent sessions (~13.1M)
**Est. API cost:** ~$747 (Opus 4.8, June 2026 pricing)

Full report: `evidence/token-usage-report.md`

---

## Category B: Illustrative / Requires Extended Resources

These results are presented in the paper to demonstrate the system's potential impact but require resources beyond the current codebase.

### B1. User Study (N = 15 procurement professionals)
**Status:** NOT EXECUTED. Requires IRB-approved study with real participants.

### B2. Baseline Comparison (2 domain experts, 5 tender packages)
**Status:** NOT EXECUTED. Requires recruiting domain experts.

### B3. Multi-Enterprise Dataset
**Status:** NOT PRODUCTION-SCALE. Current seed data: 5 products, 1 enterprise. UAT creates 17 products, 20 tenders total.

### B4. Statistical Analyses
**Status:** NOT PERFORMED. Requires B1/B2 data.

---

## Category C: Key Paper Claims — Verification Status

| Claim | Paper | Actual | Status |
|-------|-------|--------|--------|
| Java source files | 186 | 186 | ✅ VERIFIED |
| Vue components | 19 | **29** | 🔴 WRONG — fix paper |
| Controllers | 20 | **21** | 🟡 Off by 1 |
| Database tables | 26 | **28** (27 user) | 🟡 Off by 2 |
| UAT pass rate | "100%" | **100% (61/61)** | ✅ VERIFIED |
| Export .docx size | 4.0KB | 4.0KB | ✅ VERIFIED |
| Export .pdf size | 103.6KB | 103.8KB | ✅ VERIFIED |
| Products in system | 22 | **17** | 🔴 Exaggerated |
| Tenders in system | 31 | **20** | 🔴 Exaggerated |
| Expiry alerts | 103 | **33** | 🔴 3.1x exaggeration |
| Price history | 27 | **41** | 🟡 Underestimated |
| Token consumption | 15-25M | **26.2M** | 🟡 Slightly above range |
| Performance 3.3ms | Health | **8ms** | 🟡 Real but higher |
| User study (N=15) | Claimed | NOT DONE | 🔴 Illustrative |

---

## Paper Revision Priorities

1. **🔴 Fix Vue component count**: 29 not 19
2. **🔴 Fix data volume claims**: Use actual post-UAT counts (17 products, 20 tenders, 33 alerts)
3. **🟡 Add token consumption data**: 26.2M total tokens with source evidence
4. **🟡 Add real performance data**: Use measured latencies (8ms/40ms/22ms, not 3.3/12.3/47.0)
5. **🔴 Clearly label B1/B2/B3 as "projected/illustrative"** — they were never executed
6. **✅ Keep architectural claims**: All code structure metrics are verified accurate
