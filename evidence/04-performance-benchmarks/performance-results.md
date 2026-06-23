# Performance Benchmark Results — ACTUAL MEASURED DATA

**Date:** 2026-06-14 20:51 ICT
**Source:** Measured during uat-v2.mjs execution (61 API calls, real server)
**Server:** `./mvnw spring-boot:run` on localhost:8082
**Environment:** Windows 11, Java 17 (Temurin), PostgreSQL 16, Kafka 7.6.1, Redis 7

## System Under Test

- Spring Boot 3.4.5 with 186 Java source files
- PostgreSQL 16 (medbid_db, 28 tables)
- Kafka + Zookeeper (async pipeline)
- Redis (caching)
- JaCoCo instrumented

## REST API Response Times

Measured via `performance.now()` in Node.js fetch against running Spring Boot app on localhost.

### Query Endpoints

| Endpoint | Latency (ms) | Notes |
|----------|-------------|-------|
| `GET /api/health` | **8ms** | Health check endpoint |
| `GET /api/products?size=5` | **40ms** | Product list with pagination |
| `GET /api/tenders?size=5` | **22ms** | Tender list with pagination |
| `GET /api/documents?size=5` | **54ms** | Document library with pagination |
| `GET /api/expiry/alerts?size=3` | **19ms** | Expiry alerts list |
| `GET /api/enterprises?size=3` | **30ms** | Enterprise list |

### Export Endpoints

| Endpoint | Latency (ms) | Output Size | Notes |
|----------|-------------|-------------|-------|
| `POST /api/hsdt/export/word` | **1,327ms** | 4.0 KB | Word (.docx) generation via Apache POI |
| `POST /api/hsdt/export/pdf` | **754ms** | 103.8 KB | PDF generation via iText 7 |
| `POST /api/hsdt/export/zip` | **346ms** | 106.2 KB | ZIP package (Word + PDF + metadata) |
| `POST /api/hsdt/export/excel` | **2,020ms** | 5.1 KB | Excel (.xlsx) generation |

### Other Endpoints (sampled from UAT run)

| Endpoint | Latency (ms) |
|----------|-------------|
| POST /api/auth/login | ~140ms |
| GET /api/match/gap-analysis | ~52ms |
| POST /api/match/smart-suggest | ~277ms |
| POST /api/tenders | ~25-82ms |
| POST /api/products | ~49-97ms |
| PUT /api/enterprises/profile | ~105ms |

## Rate Limiting

- Bucket4j rate limiter: **100 req/min** configured
- Verified: 30-iteration benchmark without delay triggered HTTP 429 after ~10 requests
- Health endpoint bypasses rate limiting

## Reliability

- All 61 UAT test calls returned correct responses
- Server remained stable (no crashes, no memory leaks observed)
- Database connection pool: healthy
- Kafka consumers: 4 consumer groups operational

## Comparison with Paper Claims

| Metric | Paper Claim | Measured | Match |
|--------|------------|----------|-------|
| /api/health latency | "3.3ms" | 8ms | ⚠️ Similar order |
| Products list | "12.3ms" | 40ms | ⚠️ Higher (Windows vs Linux?) |
| Export Word | — | 1,327ms | NEW |
| Export PDF | — | 754ms | NEW |
| Export ZIP | — | 346ms | NEW |
| Export Excel | — | 2,020ms | NEW |

**Note:** Paper's 3.3ms/12.3ms/47.0ms latency claims were **projected estimates** not backed by actual measurements. These real measurements (8ms/40ms/22ms) should be used in the revised paper.
