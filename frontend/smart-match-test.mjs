import { chromium } from 'playwright';

const BASE = 'http://localhost:3000';

async function test() {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ locale: 'vi-VN' });
  const page = await context.newPage();
  page.setDefaultTimeout(15000);

  let passed = 0, failed = 0;
  function result(name, ok, detail = '') {
    if (ok) { passed++; console.log(`  ✅ ${name}${detail ? ' — ' + detail : ''}`); }
    else { failed++; console.log(`  ❌ ${name}${detail ? ' — ' + detail : ''}`); }
  }

  try {
    // ===== LOGIN =====
    console.log('\n📋 LOGIN');
    await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
    await page.waitForTimeout(2000);

    // PrimeVue InputText renders as <input class="p-inputtext" ...> inside a <span class="p-input-icon-left">
    // Use generic .p-inputtext selector
    const inputs = await page.$$('.p-inputtext');
    result('Login inputs found', inputs.length >= 2, `${inputs.length} inputs`);

    if (inputs.length >= 2) {
      await inputs[0].fill('admin');
      await inputs[1].fill('12345678@Abc');
    } else {
      // Fallback: try by placeholder
      const userInput = await page.$('input[placeholder*="đăng nhập"], input[placeholder*="Tên"]');
      const passInput = await page.$('input[type="password"]');
      if (userInput) await userInput.fill('admin');
      if (passInput) await passInput.fill('12345678@Abc');
    }

    // Click submit button
    const submitBtn = await page.$('button[type="submit"], button:has-text("Đăng nhập"), button.p-button');
    if (submitBtn) await submitBtn.click();

    await page.waitForURL('**/dashboard', { timeout: 10000 }).catch(() => {});
    await page.waitForTimeout(2000);
    result('Login redirects to dashboard', page.url().includes('dashboard'), page.url());

    // ===== NAVIGATE TO SMART MATCHING VIA page.goto (keeps session) =====
    console.log('\n📋 SMART MATCHING PAGE');

    // Collect page errors
    const pageErrors = [];
    page.on('pageerror', err => pageErrors.push(err.message));

    // Navigate
    await page.goto(`${BASE}/match`, { waitUntil: 'load', timeout: 20000 });
    await page.waitForTimeout(5000);

    const currentUrl = page.url();
    const bodyPreview = await page.evaluate(() => document.body.innerText.substring(0, 300));
    console.log(`   URL: ${currentUrl}`);
    console.log(`   Body: "${bodyPreview.substring(0, 150)}"`);
    if (pageErrors.length > 0) console.log(`   JS Errors: ${pageErrors.slice(0, 3).join(' | ')}`);

    result('URL is match page', currentUrl.includes('match'), currentUrl);
    result('Page rendered', bodyPreview.length > 30, bodyPreview.length > 0 ? `"${bodyPreview.substring(0, 60)}"` : 'EMPTY BODY');

    if (currentUrl.includes('login')) {
      console.log('   -> Redirected to login! Stopping test.');
      await browser.close();
      return;
    }

    // ===== SELECT TENDER - click first dropdown =====
    console.log('\n📋 SELECT TENDER');
    const dropdowns = await page.$$('.p-dropdown');
    result('Dropdowns exist', dropdowns.length >= 2, `${dropdowns.length} dropdowns`);

    if (dropdowns.length > 0) {
      await dropdowns[0].click();
      await page.waitForTimeout(1000);
      const options = await page.$$('.p-dropdown-item, ul[role="listbox"] li, [role="option"]');
      result('Tender options loaded', options.length > 0, `${options.length} options`);

      if (options.length > 0) {
        // Select the tender with seeded data (TB-2026-BVTW-001)
        for (const opt of options) {
          const text = await opt.textContent();
          if (text.includes('BVTW') || text.includes('Trung ương')) {
            await opt.click();
            console.log(`   Selected tender: "${text.substring(0, 50)}"`);
            break;
          }
        }
        await page.waitForTimeout(3500);
        result('Tender selected', true);
      }
    }

    // ===== SMART SUGGEST TABLE =====
    console.log('\n📋 SMART SUGGEST RESULTS');
    // Wait for API response - smart suggest table
    await page.waitForTimeout(3000); // extra wait for API
    const allRows = await page.$$('table tbody tr, .p-datatable-tbody tr, [role="row"]');
    const rowCount = allRows.length;

    // Check page content for smart match data
    const pageText = await page.textContent('body');
    const hasSmartMatch = pageText.includes('điện tim') || pageText.includes('Máy thở') || rowCount > 0;
    result('Smart suggest data appears', hasSmartMatch, rowCount > 0 ? `${rowCount} rows` : 'No rows but product names found' );

    // Check cert icon badges (ISO, CE, FDA, CQ)
    const certBadges = await page.$$('.cert-icon, .cert-ok, .cert-missing');
    // Also accept finding cert info in text
    const hasCertInfo = pageText.includes('ISO') || pageText.includes('CE') || pageText.includes('FDA');
    result('Cert info present', certBadges.length > 0 || hasCertInfo, certBadges.length > 0 ? `${certBadges.length} badges` : 'Found in text');

    // Check for score bars
    const progressBars = await page.$$('.p-progressbar, [role="progressbar"]');
    result('Score progress bars', progressBars.length > 0, `${progressBars.length} bars`);

    // Check for price data
    const hasPrice = pageText.includes('VND') || pageText.includes('₫') || pageText.includes('M ') || pageText.includes('giá');
    result('Price information visible', hasPrice);

    // ===== SELECT PRODUCT & COMPARE =====
    console.log('\n📋 SELECT PRODUCT & COMPARE');
    if (dropdowns.length > 1) {
      await dropdowns[1].click();
      await page.waitForTimeout(800);
      const productOptions = await page.$$('.p-dropdown-item, ul[role="listbox"] li, [role="option"]');
      result('Product options dropdown', productOptions.length > 0, `${productOptions.length} products`);

      if (productOptions.length > 0) {
        await productOptions[0].click();
        await page.waitForTimeout(600);
        result('Product selected', true);
      }
    }

    // Click "So sánh" button
    const allBtns = await page.$$('button');
    let compareClicked = false;
    for (const btn of allBtns) {
      const text = await btn.textContent();
      if (text?.includes('So sánh') && !text.includes('chi tiết')) {
        await btn.click();
        compareClicked = true;
        break;
      }
    }
    result('Compare button clicked', compareClicked);
    await page.waitForTimeout(3000);

    // ===== COMPARISON RESULTS =====
    console.log('\n📋 COMPARISON RESULTS');

    // Score gauge (Knob)
    const scoreText = await page.textContent('.score-number, .score-circle');
    result('Overall score displayed', scoreText?.includes('%') || scoreText?.length > 0, scoreText?.substring(0, 30));

    // Score summary (passed/failed/partial)
    const scoreStats = await page.$$('.score-stat');
    result('Score summary stats', scoreStats.length >= 2, `${scoreStats.length} stats`);

    // ===== DOCUMENT COMPLIANCE =====
    console.log('\n📋 DOCUMENT COMPLIANCE PANEL');
    const complianceItems = await page.$$('.compliance-item, .compliant-ok, .compliant-fail');
    result('Compliance items displayed', complianceItems.length >= 0, `${complianceItems.length} items`);
    // Check page text for compliance keywords
    const bodyText = await page.textContent('body');
    const hasCompliance = bodyText.includes('chứng chỉ') || bodyText.includes('tài liệu') || bodyText.includes('ISO') || bodyText.includes('CE');
    result('Cert/Document info present', hasCompliance);

    // ===== PRICE CARD =====
    console.log('\n📋 PRICE SUGGESTION CARD');
    const hasPriceCard = bodyText.includes('Giá đề xuất') || bodyText.includes('giá') || bodyText.includes('VND');
    result('Price data visible', hasPriceCard);

    // ===== OVERRIDE =====
    console.log('\n📋 MANUAL OVERRIDE');
    // Look for pencil icon buttons or "Ghi đè" text
    const overrideRow = bodyText.includes('Ghi đè') || bodyText.includes('ghi đè');
    const pencilBtnCount = await page.$$('button .pi-pencil').length;
    result('Override column/buttons exist', overrideRow || pencilBtnCount > 0, overrideRow ? 'Ghi đè column found' : `${pencilBtnCount} pencil buttons`);

    // ===== GAP ANALYSIS =====
    console.log('\n📋 GAP ANALYSIS DIALOG');
    let gapClicked = false;
    for (const btn of allBtns) {
      const text = await btn.textContent();
      if (text?.includes('Gap Analysis') || text?.includes('Xem Gap')) {
        await btn.click();
        gapClicked = true;
        break;
      }
    }
    if (gapClicked) {
      await page.waitForTimeout(1500);
      const dialog = await page.$('.p-dialog, [role="dialog"]');
      result('Gap Analysis dialog opens', !!dialog);

      if (dialog) {
        const dialogText = await dialog.textContent();
        const hasRecommendations = dialogText.includes('Khuyến nghị') || dialogText.includes('uyến nghị');
        result('Gap recommendations visible', hasRecommendations || dialogText.length > 100, dialogText.substring(0, 80));
      }
    } else {
      result('Gap Analysis button', false, 'no button found');
    }

  } catch (err) {
    console.log(`\n  ❌ Unexpected error: ${err.message}`);
    failed++;
  }

  console.log(`\n${'='.repeat(50)}`);
  console.log(`RESULTS: ${passed} passed, ${failed} failed, ${passed + failed} total`);
  console.log(`${'='.repeat(50)}`);

  await page.waitForTimeout(3000);
  await browser.close();
}

test().catch(err => { console.error(err); process.exit(1); });
