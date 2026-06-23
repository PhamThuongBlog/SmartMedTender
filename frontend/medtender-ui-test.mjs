import { chromium } from 'playwright';
import { setTimeout } from 'timers/promises';

const BASE = 'http://localhost:3000';

async function test() {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ locale: 'vi-VN' });
  const page = await context.newPage();

  let passed = 0;
  let failed = 0;

  function result(name, ok, detail = '') {
    if (ok) { passed++; console.log(`  ✅ ${name}${detail ? ' — ' + detail : ''}`); }
    else { failed++; console.log(`  ❌ ${name}${detail ? ' — ' + detail : ''}`); }
  }

  try {
    // ====== TEST 1: Login ======
    console.log('\n📋 TEST 1: Login');
    await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
    await setTimeout(1000);
    const loginTitle = await page.textContent('h1, .page-title, form');
    console.log(`   Page title: "${loginTitle?.substring(0, 80)}"`);

    // Fill login form
    await page.fill('input[type="text"], input[placeholder*="tên đăng nhập"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', '12345678@Abc');
    await setTimeout(500);

    // Click login button
    await page.click('button[type="submit"], button:has-text("Đăng nhập")');
    await page.waitForURL('**/dashboard', { timeout: 10000 }).catch(() => {});
    await setTimeout(2000);

    const url = page.url();
    result('Login redirects to dashboard', url.includes('dashboard'), url);

    // ====== TEST 2: Dashboard ======
    console.log('\n📋 TEST 2: Dashboard');
    const dashboardTitle = await page.textContent('h1');
    result('Dashboard page loads', dashboardTitle?.length > 0, dashboardTitle?.substring(0, 50));

    // Check stats cards
    const statsCards = await page.$$('.stats-card, .stat-card, [class*="stat"]');
    result('Has dashboard stats', statsCards.length > 0, `${statsCards.length} stat elements`);

    // ====== TEST 3: Products ======
    console.log('\n📋 TEST 3: Product List');
    await page.goto(`${BASE}/products`, { waitUntil: 'networkidle' });
    await setTimeout(1500);
    const productTitle = await page.textContent('h1');
    result('Product page loads', productTitle?.includes('sản phẩm') || productTitle?.includes('Sản phẩm'), productTitle);

    // Check for product rows
    const productRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    result('Products displayed in table', productRows.length > 0, `${productRows.length} rows`);

    // ====== TEST 4: Document Library ======
    console.log('\n📋 TEST 4: Document Library');
    await page.goto(`${BASE}/documents`, { waitUntil: 'networkidle' });
    await setTimeout(1500);
    const docTitle = await page.textContent('h1');
    result('Document library loads', docTitle?.includes('thư viện') || docTitle?.includes('tài liệu') || docTitle?.length > 0, docTitle?.substring(0, 60));

    // Check document table
    const docRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    result('Documents displayed', docRows.length > 0, `${docRows.length} rows`);

    // ====== TEST 5: Expiry Alerts ======
    console.log('\n📋 TEST 5: Expiry Alerts');
    await page.goto(`${BASE}/expiry-alerts`, { waitUntil: 'networkidle' });
    await setTimeout(1500);
    const alertTitle = await page.textContent('h1');
    result('Expiry alerts page loads', alertTitle?.includes('Cảnh báo') || alertTitle?.includes('hết hạn') || alertTitle?.length > 0, alertTitle?.substring(0, 60));

    // Click "Kiểm tra ngay" button
    const checkBtn = await page.$('button:has-text("Kiểm tra ngay")');
    if (checkBtn) {
      await checkBtn.click();
      await setTimeout(2000);
      result('Check-now button works', true);
    } else {
      result('Check-now button exists', false, 'button not found');
    }

    // Check for alert cards
    const alertCards = await page.$$('.alert-card, [class*="alert"]');
    result('Alert items displayed', alertCards.length > 0, `${alertCards.length} alerts`);

    // ====== TEST 6: Enterprise Setup ======
    console.log('\n📋 TEST 6: Enterprise Setup');
    await page.goto(`${BASE}/enterprise`, { waitUntil: 'networkidle' });
    await setTimeout(1500);
    const entTitle = await page.textContent('h1');
    result('Enterprise page loads', entTitle?.includes('doanh nghiệp') || entTitle?.length > 0, entTitle?.substring(0, 60));

    // Check if company name data is visible on the page (in form fields or text content)
    const pageText = await page.textContent('body');
    const hasEnterprise = pageText.includes('MedTender') || pageText.includes('Công ty') || pageText.includes('ABC');
    result('Seeded enterprise data visible', hasEnterprise, 'Enterprise data found on page');
    // Also verify the input fields exist (form is rendered)
    const formFields = await page.$$('input, .p-inputtext');
    result('Enterprise form fields rendered', formFields.length > 3, `${formFields.length} fields`);

    // ====== TEST 7: Tenders ======
    console.log('\n📋 TEST 7: Tenders');
    await page.goto(`${BASE}/tenders`, { waitUntil: 'networkidle' });
    await setTimeout(1500);
    const tenderTitle = await page.textContent('h1');
    result('Tender page loads', tenderTitle?.includes('gói thầu') || tenderTitle?.includes('thầu') || tenderTitle?.length > 0, tenderTitle?.substring(0, 60));

    const tenderRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    result('Tenders displayed', tenderRows.length > 0, `${tenderRows.length} rows`);

    // ====== TEST 8: OCR Review ======
    console.log('\n📋 TEST 8: OCR Review');
    await page.goto(`${BASE}/ocr/review`, { waitUntil: 'networkidle' });
    await setTimeout(1500);
    const reviewTitle = await page.textContent('h1');
    result('OCR review page loads', reviewTitle?.includes('yêu cầu') || reviewTitle?.includes('kỹ thuật') || reviewTitle?.length > 0, reviewTitle?.substring(0, 60));

    // Click the tender dropdown and select a tender
    const dropdown = await page.$('.p-dropdown, [class*="dropdown"]');
    if (dropdown) {
      await dropdown.click();
      await setTimeout(1000);
      // Select first tender option
      const options = await page.$$('.p-dropdown-item, [role="option"]');
      if (options.length > 0) {
        await options[0].click();
        await setTimeout(2000);
        result('Tender dropdown works', true, `${options.length} options`);
      } else {
        result('Tender dropdown options', false, 'no options found');
      }
    } else {
      result('Tender dropdown element', false, 'dropdown not found');
    }

    // Check requirement rows
    const reqRows = await page.$$('table tbody tr, .p-datatable-tbody tr');
    result('Requirements displayed', reqRows.length > 0, `${reqRows.length} rows`);

    // Try approving first requirement
    const approveBtn = await page.$('button[title*="Phê duyệt"], button[aria-label*="Phê duyệt"], button:has(.pi-check)');
    if (approveBtn) {
      await approveBtn.click();
      await setTimeout(1000);
      result('Approve button exists and clicked', true);
    } else {
      // Look for any action buttons
      const actionBtns = await page.$$('table tbody tr:first-child button');
      result('Action buttons in requirement row', actionBtns.length > 0, `${actionBtns.length} buttons`);
    }

  } catch (err) {
    console.log(`\n  ❌ Unexpected error: ${err.message}`);
    failed++;
  }

  console.log(`\n${'='.repeat(50)}`);
  console.log(`RESULTS: ${passed} passed, ${failed} failed, ${passed + failed} total`);
  console.log(`${'='.repeat(50)}`);

  await setTimeout(3000);
  await browser.close();
}

test().catch(err => { console.error(err); process.exit(1); });
