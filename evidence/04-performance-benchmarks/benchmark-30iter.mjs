// MedTender Performance Benchmark — 30 iterations per endpoint
// Automatically paces requests to stay under 100 req/min rate limit
// Usage: node benchmark-30iter.mjs
// Output: benchmark-30iter.csv + benchmark-30iter.json

const BASE = 'http://localhost:8082';
const ITERATIONS = 30;
const WARMUP = 3;
const DELAY_MS = 650; // Stay under 100 req/min with margin

let globalToken = '';
let requestCount = 0;

// Helper: delay between requests to avoid rate limiting
function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

async function api(method, path, body = null) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (globalToken) opts.headers['Authorization'] = 'Bearer ' + globalToken;
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(`${BASE}${path}`, opts);
  if (!res.ok) throw new Error(`${res.status}`);
  requestCount++;
  return res;
}

function stats(arr) {
  const n = arr.length;
  const mean = arr.reduce((a, b) => a + b, 0) / n;
  const sorted = [...arr].sort((a, b) => a - b);
  const p95 = sorted[Math.floor(n * 0.95)];
  const max = sorted[n - 1];
  const min = sorted[0];
  const variance = arr.reduce((s, x) => s + (x - mean) ** 2, 0) / (n - 1);
  const sd = Math.sqrt(variance);
  const se = sd / Math.sqrt(n);
  const ci_low = mean - 1.96 * se;
  const ci_high = mean + 1.96 * se;
  const cv = (sd / mean) * 100;
  return { n, mean, sd, se, ci_low, ci_high, p95, max, min, cv, raw: arr };
}

const ENDPOINTS = [
  { name: 'GET /api/health', method: 'GET', path: '/api/health', body: null },
  { name: 'GET /api/products?size=5', method: 'GET', path: '/api/products?size=5', body: null },
  { name: 'GET /api/tenders?size=5', method: 'GET', path: '/api/tenders?size=5', body: null },
  { name: 'GET /api/documents?size=5', method: 'GET', path: '/api/documents?size=5', body: null },
  { name: 'GET /api/expiry/alerts?size=3', method: 'GET', path: '/api/expiry/alerts?size=3', body: null },
  { name: 'GET /api/enterprises?size=3', method: 'GET', path: '/api/enterprises?size=3', body: null },
  { name: 'POST /api/hsdt/export/word', method: 'POST', path: '/api/hsdt/export/word', body: { tenderId: 'e0000001-0000-0000-0000-000000000001', productIds: ['a0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004'] } },
  { name: 'POST /api/hsdt/export/pdf', method: 'POST', path: '/api/hsdt/export/pdf', body: { tenderId: 'e0000001-0000-0000-0000-000000000001', productIds: ['a0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004'] } },
  { name: 'POST /api/hsdt/export/zip', method: 'POST', path: '/api/hsdt/export/zip', body: { tenderId: 'e0000001-0000-0000-0000-000000000001', productIds: ['a0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004'] } },
];

async function main() {
  const startTime = new Date();

  // --- LOGIN ---
  process.stderr.write('Login... ');
  const lr = await (await fetch(`${BASE}/api/auth/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: '12345678@Abc' }),
  })).json();
  globalToken = lr.accessToken;
  process.stderr.write('OK\n\n');

  // --- WARMUP ---
  process.stderr.write(`Warmup (${WARMUP} calls/endpoint)...\n`);
  for (const ep of ENDPOINTS) {
    for (let i = 0; i < WARMUP; i++) {
      try { await api(ep.method, ep.path, ep.body); await sleep(DELAY_MS); } catch (e) { process.stderr.write(`  WARN warmup ${ep.name}: ${e.message}\n`); }
    }
    process.stderr.write(`  ✓ ${ep.name}\n`);
  }
  process.stderr.write(`Warmup done. Requests sent: ${requestCount}\n`);

  // --- BENCHMARK ---
  const totalIter = ENDPOINTS.length * ITERATIONS;
  const estMinutes = Math.round(totalIter * DELAY_MS / 60000);
  process.stderr.write(`\nBenchmarking ${ENDPOINTS.length} endpoints × ${ITERATIONS} iterations = ${totalIter} requests...\n`);
  process.stderr.write(`Estimated time: ~${estMinutes} min (${DELAY_MS}ms delay between requests)\n\n`);

  const results = {};
  for (const ep of ENDPOINTS) {
    const times = [];
    let fails = 0;
    for (let i = 0; i < ITERATIONS; i++) {
      await sleep(DELAY_MS);
      try {
        const t0 = performance.now();
        await api(ep.method, ep.path, ep.body);
        const ms = performance.now() - t0;
        times.push(ms);
      } catch (e) {
        fails++;
        process.stderr.write(`  ✗ ${ep.name} #${i + 1}: ${e.message}\n`);
        // If rate-limited, wait and retry once
        if (e.message === '429') {
          await sleep(3000);
          try {
            const t0 = performance.now();
            await api(ep.method, ep.path, ep.body);
            const ms = performance.now() - t0;
            times.push(ms);
            fails--;
          } catch (e2) {
            process.stderr.write(`    Retry also failed: ${e2.message}\n`);
          }
        }
      }
    }
    if (times.length > 0) {
      results[ep.name] = stats(times);
      results[ep.name].failures = fails;
      process.stderr.write(`  ✓ ${ep.name}: n=${times.length}, mean=${results[ep.name].mean.toFixed(1)}ms, p95=${results[ep.name].p95.toFixed(0)}ms, fails=${fails}\n`);
    } else {
      process.stderr.write(`  ✗ ${ep.name}: ALL ${ITERATIONS} FAILED\n`);
    }
  }

  const duration = Math.round((new Date() - startTime) / 1000);
  process.stderr.write(`\nBenchmark complete in ${duration}s. Total requests: ${requestCount}\n`);

  // --- CSV OUTPUT ---
  console.log('Endpoint,Method,Mean_ms,SD_ms,95CI_Low_ms,95CI_High_ms,Min_ms,P95_ms,Max_ms,CV_pct,N,Success,Failures,SLO_500ms');
  for (const [name, r] of Object.entries(results)) {
    const slo = r.max <= 500 ? 'PASS' : `${((r.raw.filter(t => t <= 500).length / r.raw.length) * 100).toFixed(0)}%`;
    const path = ENDPOINTS.find(e => e.name === name).path;
    console.log(`"${name}","${path}",${r.mean.toFixed(2)},${r.sd.toFixed(2)},${r.ci_low.toFixed(2)},${r.ci_high.toFixed(2)},${r.min.toFixed(0)},${r.p95.toFixed(0)},${r.max.toFixed(0)},${r.cv.toFixed(2)},${r.n},${r.n - (r.failures || 0)},${r.failures || 0},${slo}`);
  }

  // --- SUMMARY ---
  const allMeans = Object.values(results).filter(r => r.mean !== undefined);
  const crudMeans = allMeans.filter((_, i) => i < 6);
  const exportMeans = allMeans.filter((_, i) => i >= 6);
  const avgCrud = crudMeans.reduce((s, r) => s + r.mean, 0) / crudMeans.length;
  const avgExport = exportMeans.reduce((s, r) => s + r.mean, 0) / exportMeans.length;
  const allUnderSLO = allMeans.every(r => r.max <= 500);

  console.log('');
  console.log('# SUMMARY');
  console.log(`# Date: ${startTime.toISOString()}`);
  console.log(`# Environment: Windows 11, Intel Core i7, 16GB RAM, Java 17, Spring Boot 3.4.5`);
  console.log(`# CRUD endpoints avg: ${avgCrud.toFixed(2)}ms`);
  console.log(`# Export endpoints avg: ${avgExport.toFixed(2)}ms`);
  console.log(`# All endpoints under 500ms SLO: ${allUnderSLO ? 'YES ✅' : 'NO ❌'}`);
  console.log(`# Total requests: ${requestCount}`);
  console.log(`# Duration: ${duration}s`);
  console.log(`# Rate limiting: ${DELAY_MS}ms delay between requests`);

  // --- JSON OUTPUT ---
  const fs = await import('fs');
  const jsonOut = {
    metadata: {
      date: startTime.toISOString(),
      environment: 'Windows 11, Intel Core i7, 16GB RAM, Java 17, Spring Boot 3.4.5',
      infrastructure: 'PostgreSQL 16 (Docker), Kafka 3.x (Docker), Redis 7.x (Docker)',
      iterations_per_endpoint: ITERATIONS,
      warmup_iterations: WARMUP,
      inter_request_delay_ms: DELAY_MS,
      total_requests: requestCount,
      duration_seconds: duration,
      all_under_slo_500ms: allUnderSLO,
      crud_avg_ms: +avgCrud.toFixed(2),
      export_avg_ms: +avgExport.toFixed(2),
    },
    results: {},
  };
  for (const [name, r] of Object.entries(results)) {
    jsonOut.results[name] = {
      endpoint: ENDPOINTS.find(e => e.name === name).path,
      method: ENDPOINTS.find(e => e.name === name).method,
      n: r.n,
      mean_ms: +r.mean.toFixed(2),
      sd_ms: +r.sd.toFixed(2),
      ci_95: `[${r.ci_low.toFixed(2)}, ${r.ci_high.toFixed(2)}]`,
      min_ms: r.min,
      p95_ms: r.p95,
      max_ms: r.max,
      cv_pct: +r.cv.toFixed(2),
      successes: r.n - (r.failures || 0),
      failures: r.failures || 0,
      slo_500ms: r.max <= 500 ? 'PASS' : 'FAIL',
    };
  }
  fs.writeFileSync('D:/NCKH_2027/Tien_ChuHai/MedTenderSystem_VERSION2/sn-article-template/evidence/04-performance-benchmarks/benchmark-30iter.json', JSON.stringify(jsonOut, null, 2));
  process.stderr.write('JSON results saved to benchmark-30iter.json\n');
}

main().catch(err => {
  process.stderr.write(`FATAL: ${err.message}\n${err.stack}\n`);
  process.exit(1);
});
