# Unit & Integration Test Suite — ACTUAL RESULTS

**Date:** 2026-06-09 18:54 ICT
**Command:** `./mvnw test -Dtest="AuthServiceTest,ProductServiceTest,EnterpriseServiceTest,ChatbotServiceTest,JwtUtilTest"`
**Framework:** JUnit 5 (Jupiter) + Mockito 5.x + Spring Boot Test 3.4.5
**Coverage:** JaCoCo 0.8.12 (156 classes analyzed, report in `target/site/jacoco/`)
**Location:** `src/test/java/com/medbid/`

---

## Unit Test Results (5 classes, 13 tests)

### ✅ ALL 13/13 PASSING

| # | Test Class | Type | Tests | Time | Status |
|---|-----------|------|-------|------|--------|
| 1 | `AuthServiceTest` | Unit (Mockito) | 2 | 5.5s | ✅ PASS |
| 2 | `JwtUtilTest` | Unit (ReflectionTestUtils) | 3 | 4.6s | ✅ PASS |
| 3 | `ChatbotServiceTest` | Unit (Mockito) | 2 | 0.3s | ✅ PASS |
| 4 | `EnterpriseServiceTest` | Unit (Mockito) | 3 | 0.5s | ✅ PASS |
| 5 | `ProductServiceTest` | Unit (Mockito) | 3 | 0.3s | ✅ PASS |
| | **TOTAL** | | **13** | **23.9s** | **✅ ALL PASS** |

### Test Details

| Test Method | Class | Description |
|-------------|-------|-------------|
| `shouldLoginSuccessfully` | AuthServiceTest | Valid credentials → JWT tokens returned |
| `shouldRegisterUser` | AuthServiceTest | New user registration with role assignment |
| `shouldGenerateAndValidateAccessToken` | JwtUtilTest | Token generation + HMAC-SHA384 signing + validation |
| `shouldGenerateAndValidateRefreshToken` | JwtUtilTest | Refresh token generation + validation |
| `shouldThrowExceptionForInvalidToken` | JwtUtilTest | Malformed token → exception thrown |
| `shouldReturnAnswerWhenQuestionMatches` | ChatbotServiceTest | FAQ keyword search → matching answer |
| `shouldReturnDefaultWhenNoMatch` | ChatbotServiceTest | No FAQ match → default fallback response |
| `shouldCreateEnterprise` | EnterpriseServiceTest | Create enterprise profile with all fields |
| `shouldGetAllEnterprises` | EnterpriseServiceTest | Paginated list (empty database) |
| `shouldGetEnterpriseById` | EnterpriseServiceTest | Single enterprise retrieval by UUID |
| `shouldCreateProduct` | ProductServiceTest | Create medical device product with specs |
| `shouldSearchProducts` | ProductServiceTest | Keyword search with like-pattern matching |
| `shouldGetProductById` | ProductServiceTest | Single product retrieval by UUID |

---

## Integration Tests (2 classes, 5 tests) — NOT RUN

These tests require full Docker infrastructure:

| # | Test Class | Tests | Requirement | Status |
|---|-----------|-------|-------------|--------|
| 6 | `SmartMedTenderApplicationTests` | 1 | Full Spring context + TestContainers PostgreSQL + Kafka + Redis | ⚠️ SKIPPED |
| 7 | `AuthControllerIntegrationTest` | 4 | TestContainers PostgreSQL + Kafka + Redis | ⚠️ SKIPPED |

**Reason:** TestContainers successfully starts a PostgreSQL container, but the Spring context fails to load because `KafkaTemplate<String, String>` bean is required by `AIService` — and the Kafka broker configuration in `application-test.yml` needs the Docker Kafka to be accessible with correct topic configuration.

**To run integration tests:**
```bash
# Ensure Docker infrastructure is running
docker compose up -d

# Fix application-test.yml Kafka config to match Docker setup
# Then run:
./mvnw test
```

---

## Test Fixes Applied

3 test files were corrected to pass (they had mock configuration errors):

| File | Fix |
|------|-----|
| `JwtUtilTest.java` | `ReflectionTestUtils.setField(jwtUtil, "secret", ...)` → `"jwtSecret"` (matching actual @Value field); added `jwtUtil.init()` call to re-derive signingKey after setting jwtSecret |
| `AuthServiceTest.java` | Added `when(userRepository.save(any())).thenAnswer(...)` — the `register()` method calls `userRepository.save()` and accesses the returned user's username, but the mock wasn't returning the saved entity |
| `ProductServiceTest.java` | Changed `productRepository.searchByNameOrBrand(eq("CT"), ...)` → `productRepository.searchProductsByKeyword(eq("%CT%"), isNull(), isNull(), ...)` — matching the actual method signature called by ProductService |

## Surefire Reports

Individual test reports are archived in this directory:
- `com.medbid.auth.AuthServiceTest.txt`
- `com.medbid.auth.JwtUtilTest.txt`
- `com.medbid.chatbot.ChatbotServiceTest.txt`
- `com.medbid.enterprise.EnterpriseServiceTest.txt`
- `com.medbid.product.ProductServiceTest.txt`
- `sample-junit-output.txt` (consolidated summary)
