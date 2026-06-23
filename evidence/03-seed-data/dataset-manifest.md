# Seed Test Dataset Manifest — VERIFIED (June 14, 2026)

**Sources:** `V5__seed_test_data.sql`, `V6__seed_price_history.sql`
**Verification method:** Direct database query via `docker exec medbid-postgres psql`

---

## Database Row Counts (Actual — After UAT Test Run)

Query: `SELECT relname, n_live_tup FROM pg_stat_user_tables ORDER BY n_live_tup DESC`

| Table | Row Count | Notes |
|-------|-----------|-------|
| export_histories | **159** | Generated during UAT (export runs) |
| match_results | **144** | Generated during UAT (product matching) |
| price_history | **41** | 33 from V6 seed + UAT additions |
| role_permissions | **38** | RBAC permission mappings |
| tender_items | **34** | Items added to tenders |
| expiry_alerts | **33** | 9 critical + 2 warning + 22 info |
| tenders | **20** | 1 pre-seeded + UAT creations + clones |
| products | **17** | 5 pre-seeded + UAT creations |
| login_history | **15** | Login attempts tracked |
| users | **13** | 1 pre-seeded (admin) + UAT creations |
| tender_requirements | **12** | Pre-seeded OCR requirements |
| refresh_tokens | **12** | JWT refresh tokens |
| product_documents | **10** | V5 seed data (CE/CO/CQ/ISO docs) |
| notifications | **9** | System notifications |
| permissions | **8** | RBAC permission definitions |
| roles | **7** | RBAC role definitions |
| chatbot_faq | **6** | Vietnamese FAQ entries |
| flyway_schema_history | **6** | Flyway migration tracking |
| legal_documents | **3** | Enterprise legal docs |
| enterprise_profiles | **1** | Single enterprise profile |
| quotations | **0** | No quotations created yet |
| backup_histories | **0** | No backups run yet |
| ocr_logs | **0** | No OCR processing yet |
| product_images | **0** | No product images |
| bank_accounts | **0** | No bank accounts configured |
| ai_logs | **0** | No AI calls logged yet |
| tender_documents | **0** | No tender doc uploads |
| audit_logs | **0** | No audit entries yet |

## Key Observations

### Paper Claims vs Reality

| Data Point | Paper Claim | Actual (Post-UAT) | Verdict |
|-----------|------------|-------------------|---------|
| Products | 22 | **17** | ⚠️ 5 seeded + 12 UAT-created = 17 |
| Tenders | 31 | **20** | 🔴 1 seeded + 19 UAT-created = 20 |
| Users | 11 | **13** | ⚠️ Comparable |
| Price history | 27 | **41** | ⚠️ 33 seeded + 8 UAT-created = 41 |
| Expiry alerts | 103 | **33** | 🔴 Paper claim 3.1x actual |
| Export histories | — | **159** | Generated during testing |
| Match results | — | **144** | Generated during testing |

### Paper Data Assessment

- **Exaggerated claims**: Products (22 vs 17), Tenders (31 vs 20), Expiry alerts (103 vs 33)
- **Within range**: Users (11 vs 13), Price history (27 vs 41)
- **Accurate**: Document count (10), Requirement count (12), Role count (7)

**Recommendation:** Update paper's Table 1 (project statistics) with these verified numbers from actual test execution.
