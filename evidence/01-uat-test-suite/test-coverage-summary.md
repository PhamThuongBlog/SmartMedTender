# MedTender UAT Test Suite — ACTUAL RUN OUTPUT

**Run Date:** 2026-06-14 20:51 ICT
**Script:** `uat-v2.mjs`
**Status:** ✅ 61 PASS, 0 FAIL — 61 TOTAL

---

## Test Results by Feature Group

### F1: THIẾT LẬP BAN ĐẦU (11 tests) — ALL PASS
| # | Test | Latency | Detail |
|---|------|---------|--------|
| F1.1 | GET enterprises | 237ms | 1 enterprises |
| F1.2 | PUT enterprise/profile | 105ms | MedTender UAT Corp |
| F1.3 | POST product #1 | 97ms | May sieu am 4D GE Voluson E10 |
| F1.4 | POST product #2 | 49ms | Bom truyen dich B.Braun Infusomat Space |
| F1.5 | POST product #3 | 56ms | He thong noi soi HD Olympus EVIS X1 CV-1 |
| F1.6 | Document library — list | 169ms | 10 documents in library |
| F1.7 | Document library — filter CE | 144ms | 3 CE documents |
| F1.8 | Document library — filter CO | 100ms | 3 CO documents |
| F1.9 | POST expiry/check-now | 322ms | T:28 C:9 W:2 |
| F1.10 | GET expiry/alerts | 48ms | 28 alerts (3 shown) |
| F1.11 | GET expiry/summary | 60ms | critical=9 warning=2 info=17 |

### F2: ĐỌC HSMT (8 tests) — ALL PASS
| # | Test | Latency | Detail |
|---|------|---------|--------|
| F2.1 | POST tender #1 | 82ms | Goi thau TBYT — BV Nhi Dong TPHCM |
| F2.2 | POST tender #2 | 25ms | Goi thau TBYT — BV Cho Ray |
| F2.3 | POST tender #3 | 28ms | Goi thau TBYT — BV TW Hue |
| F2.4 | Add items to tender #1 | 83ms | 2 items added |
| F2.5 | Add items to tender #2 | 53ms | 2 items added |
| F2.6 | Add items to tender #3 | 50ms | 2 items added |
| F2.7 | GET seeded requirements | 33ms | 12 requirements found |
| F2.8 | OCR Review | 62ms | 12 reqs available, approved 1 |

### F3: ĐỐI CHIẾU SẢN PHẨM (8 tests) — ALL PASS
| # | Test | Latency | Detail |
|---|------|---------|--------|
| F3.1 | Match product #1 | 77ms | score=0% P=0/F=12 |
| F3.2 | Match product #2 | 63ms | score=0% P=0/F=12 |
| F3.3 | Match product #3 | 61ms | score=0% P=0/F=12 |
| F3.4 | Smart suggest | 277ms | 1 suggestions |
| F3.5 | Compliance check | 41ms | 2 cert checks |
| F3.6 | Gap analysis | 52ms | 12 missing, 3 docs, 2 recs |
| F3.7 | Price suggest | 35ms | 0 datapoints, THẤP |
| F3.8 | Manual override | 169ms | passed=true |

### F4: TẠO HỒ SƠ DỰ THẦU (7 tests) — ALL PASS
| # | Test | Latency | Detail |
|---|------|---------|--------|
| F4.1 | HSDT preview | 238ms | 3 products, 42 checklist items |
| F4.2 | HSDT checklist | 165ms | 42 items, 5 sections |
| F4.3 | Export Word (.docx) | 1,327ms | 4.0KB DOCX |
| F4.4 | Export PDF (.pdf) | 754ms | 103.8KB PDF |
| F4.5 | Export ZIP (full) | 346ms | 106.2KB ZIP |
| F4.6 | Export Excel (.xlsx) | 2,020ms | 5.1KB XLSX |
| F4.7 | Export history | 46ms | records returned |

### F5: LỊCH SỬ & TÁI SỬ DỤNG (8 tests) — ALL PASS
| # | Test | Latency | Detail |
|---|------|---------|--------|
| F5.1 | Clone tender #1 | 44ms | BV Nhi Dong TPHCM (Bản sao) |
| F5.2 | Clone tender #2 | 69ms | BV Cho Ray (Bản sao) |
| F5.3 | Outcome WON #1 | 66ms | status=WON |
| F5.4 | Outcome WON #2 | 50ms | status=WON |
| F5.5 | Outcome LOST #3 | 43ms | status=LOST |
| F5.6 | Tender history (WON) | 32ms | 6 WON |
| F5.7 | Tender history (LOST) | 24ms | 3 LOST |
| F5.8 | Tender history (ALL) | 26ms | 9 total |

### F6: QUẢN LÝ TÀI KHOẢN (9 tests) — ALL PASS
| # | Test | Latency | Detail |
|---|------|---------|--------|
| F6.1 | Create user SALES | 253ms | uats051761 |
| F6.2 | Create user STAFF | 166ms | uatt051761 |
| F6.3 | Create user REVIEWER | 174ms | uatr051761 |
| F6.4 | Lock account #2 | 52ms | locked=true |
| F6.5 | Unlock account #2 | 38ms | locked=false |
| F6.6 | Reset password #1 | 156ms | OK |
| F6.7 | Login new password | 141ms | re-login OK |
| F6.8 | List all users | 45ms | 10 users |
| F6.9 | User profile (me) | 29ms | admin / SUPER_ADMIN |

### F7: SECURITY & INFRASTRUCTURE (8 tests) — ALL PASS
| # | Test | Latency | Detail |
|---|------|---------|--------|
| F7.1 | AI Config | 36ms | openai (3 avail) |
| F7.2 | AI Test extraction | 23ms | extracted=2 |
| F7.3 | Backup SLA | 22ms | RTO=60min RPO=1440min |
| F7.4 | Audit logs | 41ms | 0 entries |
| F7.5 | Audit by user | 29ms | page returned |
| F7.6 | System health | 15ms | UP |
| F7.7 | 401 on protected | 10ms | 401 correctly returned |
| F7.8 | Rate limit | 42ms | 10 req in 42ms (under 100/min) |

### PERFORMANCE TESTS (7 endpoints) — ALL PASS
| Endpoint | Latency |
|----------|---------|
| /api/health | 8ms |
| /api/products?size=5 | 40ms |
| /api/tenders?size=5 | 22ms |
| /api/expiry/alerts?size=3 | 19ms |
| /api/documents?size=5 | 54ms |
| /api/enterprises?size=3 | 30ms |
| All endpoints < 500ms | ✅ |

---

## RESULT: 61 PASS, 0 FAIL — 61 TOTAL ✅

This is the **FIRST ACTUAL EXECUTION** of the UAT test suite. Prior to this date, no test execution evidence existed in the repository.
