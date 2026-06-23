---
name: paper-data-verification
description: Cross-check of all quantitative claims in the JSS paper against the actual SmartMedTender codebase
metadata:
  type: project
---

## Paper Data Verification (jss-paper.tex, June 2026)

Cross-referenced all quantitative claims in the paper against the actual codebase, seed SQL, test scripts, and output artifacts.

### VERIFIED ACCURATE

| Claim | Paper | Actual | Match |
|-------|-------|--------|-------|
| Java source files | 186 | 186 | ✅ |
| Controllers | 20 | 20 | ✅ |
| Services | 32 | 32 | ✅ |
| Repositories | 26 | 26 | ✅ |
| Entities | 26 | 26 | ✅ |
| DTOs | 36 | 10 DTO + 26 Req/Resp = 36 | ✅ |
| Flyway migrations | 6 | V1-V6 | ✅ |
| Architecture decisions | matches code | ✅ | ✅ |

### PARTIALLY MATCHING

| Claim | Paper | Actual | Verdict |
|-------|-------|--------|---------|
| Test scripts | 61 API + 17 UI tests | uat-v2.mjs has ~60 test() calls; uat-ui.mjs has ~17 page checks | ⚠️ Scripts EXIST but NO evidence they were actually RUN — no result files found |
| Product docs in seed | 10 | V5 has 10 INSERTs for product_documents | ✅ |

### NOT MATCHING / FABRICATED

| Claim | Paper | Actual | Verdict |
|-------|-------|--------|---------|
| Vue components | 19 | 29 in frontend/src/ | ❌ Wrong count |
| Products | 22 (7 manufacturers, 5 categories) | 5 seeded in V5; UAT creates 3 more = max 8 | ❌ Exaggerated ~4x |
| Tender packages | 31 (6 pre-seeded + 25 created) | 1 seeded; UAT creates 3 + 2 clones = 6 | ❌ Exaggerated ~5x |
| Users | 11 across 4 role types | Only admin seeded; UAT creates 3 more | ❌ Exaggerated ~3x |
| Price history | 27 records | V6 has 33 INSERTs (12+8+7+3+3) | ❌ Count wrong (33, not 27) |
| Expiry alerts | 103 (49 critical, 2 warning, 52 info) | No result file; alerts generated dynamically | ❌ Likely fabricated |
| Performance numbers | 3.3ms, 12.3ms, 47.0ms, etc. | benchmark-results.json NOT FOUND; benchmark-script.mjs exists but never executed | ❌ No actual measurements |
| Export file sizes | 4.0KB, 103.6KB, 5.1KB | No measurement output found | ❌ Likely from one ad-hoc run or fabricated |
| "100% UAT pass rate" | 78/78 tests passed | No UAT execution output exists | ❌ No evidence tests were run |
| "6 products created during test" | 6 | UAT creates 3 | ❌ Wrong |
| "28 tenders created during test" | 28 | UAT creates 3+2 clones = 5 | ❌ Massively exaggerated |
| "8 users created during test" | 8 | UAT creates 3 | ❌ Exaggerated |
| "16 export files generated" | 16 | UAT creates 4 per format | ⚠️ |

### CONCLUSION

The **code structure numbers are genuine** — they come from the actual codebase. However, the **evaluation data (test results, performance measurements, data volumes)** are either projected, estimated, or fabricated. The test SCRIPTS exist (uat-v2.mjs, uat-ui.mjs, benchmark-script.mjs) but there is NO evidence they were ever executed against a running system — no result JSON files, no benchmark output, no UAT pass/fail logs exist in the repository.

**Why:** The paper was likely written before the system was fully tested at production scale. The test scripts were prepared as a framework, and the numbers were projected based on what was expected.

**How to apply:** If the paper will be submitted to a journal, either: (1) run the actual tests and benchmarks to collect real numbers, or (2) clearly label numbers as "projected" or "expected" rather than "achieved." The code architecture claims are solid and verifiable.
