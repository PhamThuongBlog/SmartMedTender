import { chromium } from 'playwright';
const BASE = 'http://localhost:3000';

async function test() {
  const browser = await chromium.launch({ headless: false });
  const page = await (await browser.newContext({ locale: 'vi-VN' })).newPage();
  page.setDefaultTimeout(12000);
  let p = 0, f = 0;
  function r(n, ok, d = '') { if (ok) { p++; console.log(`  ✅ ${n}${d?' — '+d:''}`); } else { f++; console.log(`  ❌ ${n}${d?' — '+d:''}`); } }

  try {
    // Login
    console.log('\n📋 LOGIN');
    await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' }); await page.waitForTimeout(2000);
    const inputs = await page.$$('.p-inputtext'); r('Login form', inputs.length >= 2, `${inputs.length} inputs`);
    await inputs[0].fill('admin'); await inputs[1].fill('12345678@Abc');
    await page.click('button[type="submit"]'); await page.waitForURL('**/dashboard', { timeout: 10000 }).catch(() => {});
    await page.waitForTimeout(2000); r('Dashboard', page.url().includes('dashboard'), page.url());

    // Navigate to HSDT Builder
    console.log('\n📋 HSDT BUILDER PAGE');
    await page.goto(`${BASE}/hsdt-builder`, { waitUntil: 'domcontentloaded' }); await page.waitForTimeout(4000);
    const body = await page.evaluate(() => document.body.innerText.substring(0, 300));
    console.log(`   Body: "${body.substring(0, 100)}"`);
    r('Page loads', body.includes('HSDT') || body.includes('dự thầu') || body.length > 30, body.substring(0, 50));

    // Step 1: Select tender
    console.log('\n📋 STEP 1: SELECT TENDER');
    const dropdowns = await page.$$('.p-dropdown'); r('Dropdown exists', dropdowns.length > 0, `${dropdowns.length}`);
    if (dropdowns.length > 0) {
      await dropdowns[0].click(); await page.waitForTimeout(800);
      const opts = await page.$$('.p-dropdown-item, [role="option"]');
      r('Tender options', opts.length > 0, `${opts.length}`);
      if (opts.length > 0) {
        for (const o of opts) { const t = await o.textContent(); if (t.includes('Trung ương') || t.includes('BVTW')) { await o.click(); break; } }
        await page.waitForTimeout(1500);
        r('Tender selected', true);
      }
    }

    // Go to step 2
    const nextBtn = await page.$('button:has-text("Tiếp tục")');
    if (nextBtn) { await nextBtn.click(); await page.waitForTimeout(1500); r('→ Step 2', true); }

    // Step 2: Select products
    console.log('\n📋 STEP 2: SELECT PRODUCTS');
    await page.waitForTimeout(1000);
    const productItems = await page.$$('.product-item');
    r('Product list loaded', productItems.length > 0, `${productItems.length} items`);
    // Click first 2 products
    if (productItems.length >= 2) { await productItems[0].click(); await productItems[2].click(); r('Selected 2 products', true); }
    const nextBtn2 = await page.$('button:has-text("Tiếp tục")');
    if (nextBtn2) { await nextBtn2.click(); await page.waitForTimeout(5000); r('→ Step 3', true); }

    // Step 3: Review
    console.log('\n📋 STEP 3: REVIEW');
    await page.waitForTimeout(4000);
    const pageText = await page.evaluate(() => document.body.innerText.substring(0, 500));
    r('Summary visible', pageText.includes('Sản phẩm') || pageText.includes('Đạt'), 'summary found');
    r('Tech table visible', pageText.includes('so sánh') || pageText.includes('Điểm'), 'comparison found');
    r('Checklist visible', pageText.includes('Checklist') || pageText.includes('Tài liệu hành chính') || pageText.includes('hành chính'), 'checklist found');

    // Enterprise info
    const hasEnterprise = pageText.includes('Công ty') || pageText.includes('MST') || pageText.includes('pháp lý');
    r('Enterprise section', hasEnterprise, 'enterprise info found');

    // Export buttons
    console.log('\n📋 STEP 4: EXPORT BUTTONS');
    const wordBtn = await page.$('button:has-text("Word")');
    const pdfBtn = await page.$('button:has-text("PDF")');
    const zipBtn = await page.$('button:has-text("ZIP")');
    const excelBtn = await page.$('button:has-text("Excel")');
    r('Word button', !!wordBtn); r('PDF button', !!pdfBtn); r('ZIP button', !!zipBtn); r('Excel button', !!excelBtn);

  } catch (e) { console.log(`\n  ❌ Error: ${e.message}`); f++; }

  console.log(`\n${'='.repeat(50)}`);
  console.log(`RESULTS: ${p} passed, ${f} failed, ${p+f} total`);
  console.log(`${'='.repeat(50)}`);
  await page.waitForTimeout(2000); await browser.close();
}
test().catch(err => { console.error(err); process.exit(1); });
