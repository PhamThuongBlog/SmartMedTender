// MedTender Performance Benchmark Script
// Usage: node benchmark-script.mjs
// Measures response times for 8 endpoints × 30 iterations (10 per enterprise simulation)
// Outputs CSV to stdout and results to benchmark-results.json

const BASE = process.env.BASE_URL || 'http://localhost:8082';
const ITERATIONS = parseInt(process.env.ITERATIONS || '30', 10);
const WARMUP = parseInt(process.env.WARMUP || '5', 10);

const ENDPOINTS = [
  { method: 'GET', path: '/api/health', name: 'Health Check' },
  { method: 'GET', path: '/api/products?size=5', name: 'Products List' },
  { method: 'GET', path: '/api/tenders?size=5', name: 'Tenders List' },
  { method: 'GET', path: '/api/documents?size=5', name: 'Documents List' },
  { method: 'GET', path: '/api/expiry/alerts?size=3', name: 'Expiry Alerts' },
  { method: 'GET', path: '/api/enterprises?size=3', name: 'Enterprises List' },
];

const EXPORT_ENDPOINTS = [
  { method: 'POST', path: '/api/hsdt/export/word', body: { tenderId: 'e0000001-0000-0000-0000-000000000001', productIds: ['a0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004'] }, name: 'Export Word' },
  { method: 'POST', path: '/api/hsdt/export/pdf', body: { tenderId: 'e0000001-0000-0000-0000-000000000001', productIds: ['a0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004'] }, name: 'Export PDF' },
  { method: 'POST', path: '/api/hsdt/export/zip', body: { tenderId: 'e0000001-0000-0000-0000-000000000001', productIds: ['a0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004'] }, name: 'Export ZIP' },
];

let globalToken = '';

async function api(method, path, body = null) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (globalToken) opts.headers['Authorization'] = 'Bearer ' + globalToken;
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(`${BASE}${path}`, opts);
  if (!res.ok) throw new Error(`${res.status}`);
  return res;
}

async function measure(fn) {
  const t0 = performance.now();
  await fn();
  return performance.now() - t0;
}

function stats(arr) {
  const n = arr.length;
  const mean = arr.reduce((a, b) => a + b, 0) / n;
  const sorted = [...arr].sort((a, b) => a - b);
  const p95 = sorted[Math.floor(n * 0.95)];
  const max = sorted[n - 1];
  const variance = arr.reduce((s, x) => s + (x - mean) ** 2, 0) / (n - 1);
  const sd = Math.sqrt(variance);
  const se = sd / Math.sqrt(n);
  const ci_low = mean - 1.96 * se;
  const ci_high = mean + 1.96 * se;
  const cv = (sd / mean) * 100;
  return { n, mean, sd, se, ci_low, ci_high, p95, max, cv, raw: arr };
}

async function main() {
  // Login
  console.error('Logging in...');
  const lr = await (await fetch(`${BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: '12345678@Abc' }),
  })).json();
  globalToken = lr.accessToken;
  console.error('Logged in successfully.\n');

  // Warmup
  console.error(`Warming up (${WARMUP} iterations per endpoint)...`);
  for (const ep of [...ENDPOINTS, ...EXPORT_ENDPOINTS]) {
    for (let i = 0; i < WARMUP; i++) {
      try {
        await api(ep.method, ep.path, ep.body || null);
      } catch (e) {
        console.error(`  Warmup ${ep.name} #${i + 1} FAILED: ${e.message}`);
      }
    }
  }
  console.error('Warmup complete.\n');

  // Benchmark
  const results = {};
  console.error(`Benchmarking (${ITERATIONS} iterations per endpoint)...`);

  for (const ep of ENDPOINTS) {
    const times = [];
    for (let i = 0; i < ITERATIONS; i++) {
      try {
        const ms = await measure(() => api(ep.method, ep.path));
        times.push(ms);
      } catch (e) {
        console.error(`  ${ep.name} #${i + 1} FAILED: ${e.message}`);
      }
    }
    results[ep.name] = { ...stats(times), endpoint: ep.path };
    console.error(`  ${ep.name}: mean=${results[ep.name].mean.toFixed(1)}ms, p95=${results[ep.name].p95.toFixed(0)}ms`);
  }

  for (const ep of EXPORT_ENDPOINTS) {
    const times = [];
    for (let i = 0; i < ITERATIONS; i++) {
      try {
        const ms = await measure(() => api(ep.method, ep.path, ep.body));
        times.push(ms);
      } catch (e) {
        console.error(`  ${ep.name} #${i + 1} FAILED: ${e.message}`);
      }
    }
    results[ep.name] = { ...stats(times), endpoint: ep.path };
    console.error(`  ${ep.name}: mean=${results[ep.name].mean.toFixed(1)}ms, p95=${results[ep.name].p95.toFixed(0)}ms`);
  }

  // Output CSV
  console.log('Endpoint,Mean_ms,SD_ms,95CI_Low_ms,95CI_High_ms,P95_ms,Max_ms,CV_pct,SLO_500ms_Compliance');
  for (const [name, r] of Object.entries(results)) {
    const slo = r.max <= 500 ? '100%' : `${((r.raw.filter(t => t <= 500).length / r.raw.length) * 100).toFixed(1)}%`;
    console.log(`${name},${r.mean.toFixed(1)},${r.sd.toFixed(1)},${r.ci_low.toFixed(1)},${r.ci_high.toFixed(1)},${r.p95.toFixed(0)},${r.max.toFixed(0)},${r.cv.toFixed(1)},${slo}`);
  }

  // Output JSON results
  const jsonOut = {};
  for (const [name, r] of Object.entries(results)) {
    jsonOut[name] = { endpoint: r.endpoint, n: r.n, mean: +r.mean.toFixed(1), sd: +r.sd.toFixed(1), ci_95: `[${r.ci_low.toFixed(1)}, ${r.ci_high.toFixed(1)}]`, p95: r.p95, max: r.max, cv_pct: +r.cv.toFixed(1) };
  }

  const fs = await import('fs');
  fs.writeFileSync('benchmark-results.json', JSON.stringify(jsonOut, null, 2));
  console.error('\nResults written to benchmark-results.json');
}

main().catch(err => {
  console.error('BENCHMARK ERROR:', err.message);
  process.exit(1);
});
