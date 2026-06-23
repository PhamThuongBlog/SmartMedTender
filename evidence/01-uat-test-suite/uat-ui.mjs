// UAT UI Test — Verify all pages render correctly
import { chromium } from 'playwright';

const BASE = 'http://localhost:3000';

async function run() {
  const browser = await chromium.launch({ headless: false });
  const page = await (await browser.newContext({ locale: 'vi-VN' })).newPage();
  page.setDefaultTimeout(12000);
  let p = 0, f = 0;
  function r(n, ok, d) { if (ok) { p++; console.log(`  ✅ ${n}${d?' — '+d:''}`); } else { f++; console.log(`  ❌ ${n}${d?' — '+d:''}`); } }

  try {
    // LOGIN
    console.log('\n🔐 LOGIN');
    await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' }); await page.waitForTimeout(2000);
    const fi = await page.$$('.p-inputtext');
    await fi[0].fill('admin'); await fi[1].fill('12345678@Abc');
    await page.click('button[type="submit"]'); await page.waitForTimeout(3000);
    r('Login → Dashboard', page.url().includes('dashboard'), page.url());

    // F1: ALL PAGES LOAD TEST
    const pages = [
      { path: '/products', name: 'F1.1 Products', check: 'sản phẩm' },
      { path: '/enterprise', name: 'F1.2 Enterprise', check: 'doanh nghiệp' },
      { path: '/documents', name: 'F1.3 Document Library', check: 'tài liệu' },
      { path: '/expiry-alerts', name: 'F1.4 Expiry Alerts', check: 'hết hạn' },
      { path: '/hsmt/upload', name: 'F2.1 HSMT Upload', check: 'HSMT' },
      { path: '/ocr/review', name: 'F2.2 OCR Review', check: 'yêu cầu' },
      { path: '/match', name: 'F3.1 Smart Matching', check: 'sánh' },
      { path: '/hsdt-builder', name: 'F4.1 HSDT Builder', check: 'HSDT' },
      { path: '/export', name: 'F4.2 Export Center', check: 'xuất' },
      { path: '/tenders', name: 'F5.1 Tender List', check: 'gói thầu' },
      { path: '/users', name: 'F6.1 User Management', check: 'Người dùng' },
      { path: '/settings', name: 'F7.1 Settings', check: 'đặt' },
      { path: '/dashboard', name: 'Dashboard', check: 'Tổng quan' },
      { path: '/notifications', name: 'Notifications', check: 'thông báo' },
    ];

    for (const pg of pages) {
      try {
        await page.goto(`${BASE}${pg.path}`, { waitUntil: 'domcontentloaded' });
        await page.waitForTimeout(3000);
        const txt = await page.evaluate(() => document.body.innerText.substring(0, 300));
        const ok = txt.toLowerCase().includes(pg.check.toLowerCase());
        r(pg.name, ok, ok ? 'OK' : txt.substring(0, 50));
      } catch (e) { r(pg.name, false, e.message.substring(0, 40)); }
    }

    // F1: Product detail test
    console.log('\n📋 PRODUCT & TENDER INTERACTION');
    await page.goto(`${BASE}/tenders`, { waitUntil: 'domcontentloaded' }); await page.waitForTimeout(3000);
    const treeRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    r('Tenders displayed', treeRows.length > 0, `${treeRows.length} rows`);

    await page.goto(`${BASE}/products`, { waitUntil: 'domcontentloaded' }); await page.waitForTimeout(3000);
    const prodRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    r('Products displayed', prodRows.length > 0, `${prodRows.length} rows`);

    // Logout test
    const logoutLinks = await page.$$('a[href*="logout"], button:has-text("Đăng xuất")');
    if (logoutLinks.length > 0) {
      r('Logout button exists', true);
    }

    console.log(`\n${'='.repeat(50)}`);
    console.log(`UI RESULTS: ${p} pass, ${f} fail — ${p+f} total`);
    console.log(`${'='.repeat(50)}`);

  } catch (e) { console.error(e); f++; }
  await page.waitForTimeout(2000); await browser.close();
}

run();
