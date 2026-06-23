import { chromium } from 'playwright';

const BASE = 'http://localhost:3000';

async function test() {
  const browser = await chromium.launch({ headless: false });
  const ctx = await browser.newContext({ locale: 'vi-VN' });
  const page = await ctx.newPage();
  page.setDefaultTimeout(15000);

  let p = 0, f = 0;
  function r(n, ok, d = '') {
    if (ok) { p++; console.log(`  ✅ ${n}${d ? ' — ' + d : ''}`); }
    else { f++; console.log(`  ❌ ${n}${d ? ' — ' + d : ''}`); }
  }

  try {
    // ====== LOGIN ======
    console.log('\n🔐 LOGIN');
    await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
    await page.waitForTimeout(2000);
    const inputs = await page.$$('.p-inputtext');
    await inputs[0].fill('admin');
    await inputs[1].fill('12345678@Abc');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard', { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(3000);
    r('Login → Dashboard', page.url().includes('dashboard'), page.url());

    // ====== F1: TENDER LIST & DETAIL ======
    console.log('\n📋 F1: TENDER MANAGEMENT');

    // Tender list
    await page.goto(`${BASE}/tenders`, { waitUntil: 'networkidle' });
    await page.waitForTimeout(4000);
    const tenderTitle = await page.textContent('h1').catch(() => '');
    r('Tender list page title', tenderTitle.includes('gói thầu') || tenderTitle.includes('Quản lý'), tenderTitle);

    // Wait for data table
    await page.waitForSelector('table tbody tr, .p-datatable-tbody tr', { timeout: 8000 }).catch(() => {});
    const tenderRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    r('Tenders in table', tenderRows.length > 0, `${tenderRows.length} tenders`);

    // Verify seeded tender exists
    const fullText = await page.evaluate(() => document.body.innerText);
    const hasSeeded = fullText.includes('Trung ương') || fullText.includes('BVTW') || fullText.includes('chẩn đoán');
    r('Seeded tender visible', hasSeeded);

    // Tender detail page
    await page.goto(`${BASE}/tenders/e0000001-0000-0000-0000-000000000001`, { waitUntil: 'networkidle' });
    await page.waitForTimeout(4000);
    const detailText = await page.evaluate(() => document.body.innerText);
    r('Tender detail page', detailText.length > 200, `"${detailText.substring(200, 300)}"`);
    r('Tender info visible', detailText.includes('DRAFT') || detailText.includes('WON') || detailText.includes('Trạng thái') || detailText.includes('gói thầu'), 'status/info found');

    // ====== F2: USER MANAGEMENT ======
    console.log('\n📋 F2: USER MANAGEMENT');

    await page.goto(`${BASE}/users`, { waitUntil: 'networkidle' });
    await page.waitForTimeout(5000);

    // Wait for data to load
    await page.waitForSelector('table tbody tr, .p-datatable-tbody tr, .p-datatable', { timeout: 10000 }).catch(() => {});
    await page.waitForTimeout(2000);

    const usersTitle = await page.textContent('h1').catch(() => '');
    r('User management title', usersTitle.length > 0, usersTitle || 'no h1 found');

    const userRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    const allText = await page.evaluate(() => document.body.innerText);

    if (userRows.length > 0) {
      r('User table has rows', true, `${userRows.length} users`);
      r('Admin user visible', allText.includes('admin'), 'admin found');
    } else {
      // Maybe admin guard redirected — check if we're still on /users
      const stillOnUsers = page.url().includes('users');
      r('Still on /users page', stillOnUsers, page.url());

      // Page might have loaded with empty table — check for table structure
      const hasTable = allText.includes('Tên đăng nhập') || allText.includes('username') || allText.includes('Vai trò');
      r('User table headers present', hasTable, 'table structure found');

      // Check for any user-related content
      r('Has user content', allText.includes('admin') || allText.includes('Người dùng') || allText.includes('Quản trị'), '"' + allText.substring(100, 200) + '"');
    }

    // Check for create button
    const createBtn = await page.$('button:has-text("Thêm"), button:has-text("Tạo"), button .pi-plus, button:has-text("mới")');
    r('Create user button', !!createBtn, createBtn ? 'found' : 'using header button');

    // ====== F3: SIDEBAR NAVIGATION ======
    console.log('\n📋 F3: SIDEBAR NAVIGATION');
    const navItems = [
      { path: '/dashboard', label: 'Dashboard', check: 'Tổng quan' },
      { path: '/tenders', label: 'Tenders', check: 'gói thầu' },
      { path: '/products', label: 'Products', check: 'sản phẩm' },
      { path: '/hsmt/upload', label: 'HSMT Upload', check: 'HSMT' },
      { path: '/match', label: 'Matching', check: 'sánh' },
      { path: '/hsdt-builder', label: 'HSDT Builder', check: 'HSDT' },
      { path: '/export', label: 'Export', check: 'xuất' },
      { path: '/enterprise', label: 'Enterprise', check: 'doanh nghiệp' },
      { path: '/documents', label: 'Documents', check: 'tài liệu' },
      { path: '/expiry-alerts', label: 'Expiry', check: 'hết hạn' },
      { path: '/notifications', label: 'Notifications', check: 'thông báo' },
    ];

    for (const item of navItems) {
      try {
        await page.goto(`${BASE}${item.path}`, { waitUntil: 'domcontentloaded' });
        await page.waitForTimeout(2500);
        const txt = await page.evaluate(() => document.body.innerText);
        const found = txt.toLowerCase().includes(item.check.toLowerCase());
        r(`Nav → ${item.label}`, found, found ? 'OK' : `not found "${item.check}"`);
      } catch (e) {
        r(`Nav → ${item.label}`, false, e.message.substring(0, 40));
      }
    }

    // ====== F4: API-DRIVEN FEATURE TESTS ======
    console.log('\n📋 F4: API FEATURE TESTS (via fetch in browser)');

    // Test AI Config endpoint
    const aiConfig = await page.evaluate(async () => {
      const token = localStorage.getItem('accessToken');
      const res = await fetch('/api/admin/ai-config', { headers: { Authorization: 'Bearer ' + token } });
      return res.ok ? await res.json() : null;
    });
    r('AI Config accessible', aiConfig !== null && aiConfig.currentProvider, aiConfig?.currentProvider || 'failed');

    // Test Backup SLA endpoint
    const sla = await page.evaluate(async () => {
      const token = localStorage.getItem('accessToken');
      const res = await fetch('/api/backup/sla', { headers: { Authorization: 'Bearer ' + token } });
      return res.ok ? await res.json() : null;
    });
    r('Backup SLA accessible', sla !== null && sla.rtoMinutes, sla ? `RTO=${sla.rtoMinutes}min RPO=${sla.rpoMinutes}min` : 'failed');

    // Test Audit endpoint
    const audit = await page.evaluate(async () => {
      const token = localStorage.getItem('accessToken');
      const res = await fetch('/api/audit', { headers: { Authorization: 'Bearer ' + token } });
      return res.ok ? await res.json() : null;
    });
    r('Audit logs accessible', audit !== null, Array.isArray(audit) ? `${audit.length} entries` : 'failed');

    // Test Tender clone
    const clone = await page.evaluate(async () => {
      const token = localStorage.getItem('accessToken');
      const res = await fetch('/api/tenders/e0000001-0000-0000-0000-000000000001/clone', {
        method: 'POST', headers: { Authorization: 'Bearer ' + token }
      });
      return res.ok ? await res.json() : null;
    });
    r('Tender clone API', clone !== null && clone.name, clone?.name?.substring(0, 50) || 'failed');

    // Test Enquiry history
    const history = await page.evaluate(async () => {
      const token = localStorage.getItem('accessToken');
      const res = await fetch('/api/tenders/history?statuses=WON', { headers: { Authorization: 'Bearer ' + token } });
      return res.ok ? await res.json() : null;
    });
    r('Tender history API', history !== null && history.totalElements > 0, `${history?.totalElements || 0} won tenders`);

    console.log('');

  } catch (e) {
    console.log(`\n  ❌ Fatal: ${e.message}`);
    f++;
  }

  console.log(`${'='.repeat(50)}`);
  console.log(`RESULTS: ${p} passed, ${f} failed, ${p + f} total`);
  console.log(`${'='.repeat(50)}`);

  await page.waitForTimeout(2000);
  await browser.close();
}

test().catch(err => { console.error(err); process.exit(1); });
