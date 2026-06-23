<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Đối chiếu sản phẩm thông minh</h1>
        <p class="page-subtitle">Gợi ý sản phẩm, so sánh đạt/không đạt, cảnh báo thiếu tiêu chí/tài liệu, gợi ý giá</p>
      </div>
    </div>

    <!-- Selection -->
    <div class="card-container mb-4">
      <div class="selection-row">
        <div class="selection-field">
          <label>Gói thầu</label>
          <Dropdown v-model="selectedTender" :options="tenders" optionLabel="name" optionValue="id"
            placeholder="Chọn gói thầu" :loading="loadingTenders" filter showClear fluid @change="onTenderChange" />
        </div>
        <div class="selection-field">
          <label>Sản phẩm</label>
          <Dropdown v-model="selectedProduct" :options="products" optionLabel="name" optionValue="id"
            placeholder="Chọn sản phẩm" :loading="loadingProducts" filter showClear fluid />
        </div>
        <Button label="So sánh" icon="pi pi-search" :disabled="!selectedTender || !selectedProduct"
          :loading="comparing" @click="runComparison" class="compare-btn" />
        <Button v-if="selectedTender" label="Gap Analysis" icon="pi pi-exclamation-triangle"
          severity="warning" outlined :disabled="!selectedProduct" @click="runGapAnalysis" class="compare-btn" />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="comparing" class="loading-container">
      <ProgressSpinner /><p style="margin-left: 1rem;">Đang phân tích và so sánh...</p>
    </div>

    <!-- Smart Match Suggestions -->
    <div v-if="smartMatches.length > 0 && !comparisonResult" class="card-container mb-4">
      <h3 class="section-title">
        <i class="pi pi-star" style="color: var(--warning-color); margin-right: 0.5rem;"></i>
        Gợi ý sản phẩm thông minh
      </h3>
      <DataTable :value="smartMatches">
        <Column field="productName" header="Sản phẩm">
          <template #body="{ data }">
            <div class="product-cell">
              <span class="product-link">{{ data.productName }}</span>
              <span class="product-meta">{{ data.productManufacturer || '-' }} · {{ data.productCategory || '-' }}</span>
            </div>
          </template>
        </Column>
        <Column header="Chứng chỉ" style="width: 160px;">
          <template #body="{ data }">
            <div class="cert-icons">
              <span :class="['cert-icon', data.hasIso ? 'cert-ok' : 'cert-missing']" v-tooltip.top="data.hasIso ? 'ISO ✓' : 'Thiếu ISO'">ISO</span>
              <span :class="['cert-icon', data.hasCe ? 'cert-ok' : 'cert-missing']" v-tooltip.top="data.hasCe ? 'CE ✓' : 'Thiếu CE'">CE</span>
              <span :class="['cert-icon', data.hasFda ? 'cert-ok' : 'cert-missing']" v-tooltip.top="data.hasFda ? 'FDA ✓' : 'Thiếu FDA'">FDA</span>
              <span :class="['cert-icon', data.hasCoCq ? 'cert-ok' : 'cert-missing']" v-tooltip.top="data.hasCoCq ? 'CO/CQ ✓' : 'Thiếu CO/CQ'">CQ</span>
            </div>
          </template>
        </Column>
        <Column field="overallScore" header="Điểm phù hợp" sortable style="width: 160px;">
          <template #body="{ data }">
            <div class="score-cell">
              <ProgressBar :value="data.overallScore" :style="{ width: '90px', height: '8px' }"
                :class="{ 'score-high': data.overallScore >= 80, 'score-mid': data.overallScore >= 50 && data.overallScore < 80, 'score-low': data.overallScore < 50 }" />
              <span :class="data.overallScore >= 80 ? 'text-success' : data.overallScore >= 50 ? 'text-warning' : 'text-danger'">
                {{ data.overallScore }}%
              </span>
            </div>
          </template>
        </Column>
        <Column header="Đạt/Tổng" style="width: 100px;">
          <template #body="{ data }">
            <Tag :value="`${data.passed}/${data.totalRequirements}`"
              :severity="data.passed === data.totalRequirements ? 'success' : data.passed >= data.totalRequirements/2 ? 'warning' : 'danger'" />
          </template>
        </Column>
        <Column header="Giá gợi ý" style="width: 160px;">
          <template #body="{ data }">
            <div v-if="data.suggestedPrice" class="price-cell">
              <span class="price-value">{{ formatPrice(data.suggestedPrice) }}</span>
              <Tag :value="data.priceConfidence || 'THAP'" :severity="data.priceConfidence === 'CAO' ? 'success' : data.priceConfidence === 'TRUNG BINH' ? 'info' : 'warning'" style="font-size: 0.65rem;" />
            </div>
            <span v-else class="text-muted">-</span>
          </template>
        </Column>
        <Column header="Cảnh báo" style="width: 40px;">
          <template #body="{ data }">
            <i v-if="data.gapWarnings?.length" class="pi pi-exclamation-triangle" style="color: var(--danger-color);"
              v-tooltip.top="data.gapWarnings.join('\n')" />
          </template>
        </Column>
        <Column header="Thao tác" style="width: 100px;">
          <template #body="{ data }">
            <Button icon="pi pi-play" severity="info" text rounded size="small"
              v-tooltip.top="'So sánh chi tiết'"
              @click="selectedProduct = data.productId; runComparison()" />
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- Comparison Results -->
    <div v-if="comparisonResult" class="comparison-results">
      <!-- Overall Score + Product Info -->
      <div class="card-container mb-4">
        <div class="overall-score">
          <div class="score-circle">
            <Knob :model-value="comparisonResult.overallScore" :max="100" :size="120" :stroke-width="10"
              :valueColor="scoreColor" readonly />
            <div class="score-text">
              <span class="score-number">{{ comparisonResult.overallScore }}%</span>
              <span class="score-label">Phù hợp</span>
            </div>
          </div>
          <div class="score-summary">
            <div class="score-stat"><i class="pi pi-check-circle" style="color: var(--success-color);" /><span>{{ passedCount }} yêu cầu đạt</span></div>
            <div class="score-stat"><i class="pi pi-times-circle" style="color: var(--danger-color);" /><span>{{ failedCount }} yêu cầu không đạt</span></div>
            <div class="score-stat"><i class="pi pi-question-circle" style="color: var(--warning-color);" /><span>{{ partialCount }} yêu cầu một phần</span></div>
          </div>
          <!-- Price Card -->
          <div v-if="priceInfo" class="price-card">
            <h4><i class="pi pi-dollar" /> Gợi ý giá</h4>
            <div class="price-detail">
              <div class="price-main">
                <span class="price-label">Giá đề xuất</span>
                <span class="price-big">{{ formatPrice(priceInfo.suggestedPrice) }}</span>
              </div>
              <div class="price-aux">
                <div><span>TB lịch sử</span><strong>{{ formatPrice(priceInfo.averagePrice) }}</strong></div>
                <div><span>Thấp nhất</span><strong>{{ formatPrice(priceInfo.minPrice) }}</strong></div>
                <div><span>Cao nhất</span><strong>{{ formatPrice(priceInfo.maxPrice) }}</strong></div>
              </div>
              <Tag :value="'Độ tin cậy: ' + (priceInfo.confidence || 'THAP')"
                :severity="priceInfo.confidence === 'CAO' ? 'success' : priceInfo.confidence === 'TRUNG BINH' ? 'info' : 'warning'" />
            </div>
          </div>
        </div>
      </div>

      <!-- Document Compliance -->
      <div v-if="complianceDetails.length > 0" class="card-container mb-4">
        <h3 class="section-title">
          <i class="pi pi-verified" style="color: var(--primary-color); margin-right: 0.5rem;"></i>
          Tình trạng chứng chỉ / tài liệu
        </h3>
        <div class="compliance-grid">
          <div v-for="c in complianceDetails" :key="c.requirementId" :class="['compliance-item', c.compliant ? 'compliant-ok' : 'compliant-fail']">
            <div class="compliance-header">
              <i :class="c.compliant ? 'pi pi-check-circle' : 'pi pi-times-circle'"
                :style="{ color: c.compliant ? 'var(--success-color)' : 'var(--danger-color)', fontSize: '1.2rem' }" />
              <span>{{ c.requirement }}</span>
            </div>
            <Tag v-if="c.compliant" value="ĐẠT" severity="success" />
            <Tag v-else-if="c.status === 'MISSING_DOC'" value="THIẾU" severity="danger" />
            <Tag v-else-if="c.status === 'EXPIRED_DOC'" value="HẾT HẠN" severity="warning" />
            <div v-if="c.missingDocuments?.length" class="compliance-detail">
              <small>Thiếu: {{ c.missingDocuments.join(', ') }}</small>
            </div>
          </div>
        </div>
      </div>

      <!-- Gap Warnings -->
      <div v-if="smartMatchData?.gapWarnings?.length" class="card-container mb-4" style="border-left: 4px solid var(--danger-color);">
        <h3 class="section-title" style="color: var(--danger-color);">
          <i class="pi pi-exclamation-triangle" style="margin-right: 0.5rem;" /> Cảnh báo
        </h3>
        <div v-for="w in smartMatchData.gapWarnings" :key="w" class="gap-warning-item">
          <i class="pi pi-times" style="color: var(--danger-color); font-size: 0.75rem;" />
          <span>{{ w }}</span>
        </div>
      </div>

      <Divider />

      <!-- Detail Table -->
      <div class="card-container">
        <h3 class="section-title">Chi tiết đối chiếu</h3>
        <DataTable :value="comparisonResult.details" editMode="row" @row-edit-save="onRowEditSave">
          <Column field="requirement" header="Yêu cầu kỹ thuật" />
          <Column field="type" header="Loại" style="width: 120px;">
            <template #body="{ data }"><Tag :value="data.type" /></template>
          </Column>
          <Column field="requiredValue" header="Yêu cầu" style="width: 150px;">
            <template #body="{ data }">{{ data.operator }} {{ data.requiredValue }} {{ data.unit || '' }}</template>
          </Column>
          <Column field="actualValue" header="Thực tế" style="width: 140px;">
            <template #body="{ data }">{{ data.actualValue || '-' }}</template>
          </Column>
          <Column field="status" header="Kết quả" style="width: 110px;">
            <template #body="{ data }">
              <Tag :value="statusLabel(data.status)" :severity="statusSeverity(data.status)" />
            </template>
          </Column>
          <Column field="score" header="Điểm" style="width: 110px;">
            <template #body="{ data }">
              <div class="score-cell">
                <ProgressBar :value="data.score" :style="{ width: '60px', height: '6px' }" />
                <small>{{ data.score }}%</small>
              </div>
            </template>
          </Column>
          <Column field="notes" header="Ghi chú" style="width: 200px;">
            <template #body="{ data }">{{ data.notes || '-' }}</template>
          </Column>
          <!-- Manual Override Column -->
          <Column header="Ghi đè" style="width: 130px;">
            <template #body="{ data, index }">
              <div class="override-cell">
                <Button v-if="!overrides[index]"
                  icon="pi pi-pencil" severity="secondary" text rounded size="small"
                  v-tooltip.top="'Ghi đè kết quả'" @click="startOverride(index)" />
                <template v-else>
                  <ToggleButton v-model="overrides[index].passed" onLabel="Đạt" offLabel="Ko"
                    onIcon="pi pi-check" offIcon="pi pi-times" class="override-toggle" @change="applyOverride(index)" />
                  <Button icon="pi pi-times" severity="danger" text rounded size="small" @click="cancelOverride(index)" />
                </template>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>

      <div class="comparison-actions mt-4">
        <Button label="Xem Gap Analysis" icon="pi pi-exclamation-triangle" severity="warning" outlined @click="runGapAnalysis" />
        <Button label="Tạo HSDT" icon="pi pi-file-edit" @click="router.push('/hsdt-builder')" />
      </div>
    </div>

    <!-- Gap Analysis Dialog -->
    <Dialog v-model:visible="showGapDialog" :header="'Phân tích khoảng trống: ' + (gapData?.productName || '')" :modal="true" :style="{ width: '800px' }" :maximizable="true">
      <div v-if="gapLoading" class="loading-container"><ProgressSpinner /></div>
      <template v-else-if="gapData">
        <!-- Summary -->
        <div class="gap-summary">
          <div class="gap-stat"><span class="gap-num text-success">{{ gapData.passedRequirements }}</span><span>Đạt</span></div>
          <div class="gap-stat"><span class="gap-num text-danger">{{ gapData.failedRequirements }}</span><span>Không đạt</span></div>
          <div class="gap-stat"><span class="gap-num text-primary">{{ gapData.overallScore }}%</span><span>Điểm</span></div>
        </div>

        <!-- Missing criteria -->
        <h4 v-if="gapData.missingCriteria?.length" style="margin-top: 1.5rem;">
          <i class="pi pi-times-circle" style="color: var(--danger-color);" /> Tiêu chí không đạt ({{ gapData.missingCriteria.length }})
        </h4>
        <div v-for="g in gapData.missingCriteria" :key="g.description" :class="['gap-item', 'gap-' + g.severity.toLowerCase()]">
          <Tag :value="g.category" severity="info" />
          <span>{{ g.description }}</span>
          <small v-if="g.recommendation">{{ g.recommendation }}</small>
        </div>

        <!-- Missing documents -->
        <h4 v-if="gapData.missingDocuments?.length" style="margin-top: 1.5rem;">
          <i class="pi pi-file-excel" style="color: var(--warning-color);" /> Tài liệu/chứng chỉ còn thiếu ({{ gapData.missingDocuments.length }})
        </h4>
        <div v-for="g in gapData.missingDocuments" :key="g.description" class="gap-item gap-warning">
          <Tag :value="g.currentStatus" severity="warning" />
          <span>{{ g.description }}</span>
          <small>{{ g.recommendation }}</small>
        </div>

        <!-- Expired certificates -->
        <h4 v-if="gapData.expiredCertificates?.length" style="margin-top: 1.5rem;">
          <i class="pi pi-clock" style="color: var(--danger-color);" /> Chứng chỉ hết hạn ({{ gapData.expiredCertificates.length }})
        </h4>
        <div v-for="g in gapData.expiredCertificates" :key="g.description" class="gap-item gap-critical">
          <Tag value="HẾT HẠN" severity="danger" />
          <span>{{ g.description }}</span>
          <small>{{ g.recommendation }}</small>
        </div>

        <!-- Recommendations -->
        <h4 style="margin-top: 1.5rem;">
          <i class="pi pi-lightbulb" style="color: var(--primary-color);" /> Khuyến nghị
        </h4>
        <ul class="recommendation-list">
          <li v-for="r in gapData.recommendedActions" :key="r">{{ r }}</li>
        </ul>

        <!-- Price in Gap -->
        <div v-if="gapData.priceSuggestion" class="gap-price-card mt-4">
          <h4><i class="pi pi-dollar" /> Gợi ý giá dự thầu</h4>
          <div class="price-row">
            <div><span>Giá đề xuất</span><strong>{{ formatPrice(gapData.priceSuggestion.suggestedPrice) }}</strong></div>
            <div><span>Trúng thầu gần nhất</span><strong>{{ formatPrice(gapData.priceSuggestion.lastWinningPrice) }}</strong></div>
            <div><span>Trung bình</span><strong>{{ formatPrice(gapData.priceSuggestion.averagePrice) }}</strong></div>
            <div><span>Độ tin cậy</span><Tag :value="gapData.priceSuggestion.confidence || 'THAP'" :severity="gapData.priceSuggestion.confidence === 'CAO' ? 'success' : 'warning'" /></div>
          </div>
        </div>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import apiClient from '@/api/client'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import ProgressBar from 'primevue/progressbar'
import ProgressSpinner from 'primevue/progressspinner'
import Divider from 'primevue/divider'
import Knob from 'primevue/knob'
import Dialog from 'primevue/dialog'
import ToggleButton from 'primevue/togglebutton'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const loadingTenders = ref(false)
const loadingProducts = ref(false)
const comparing = ref(false)
const tenders = ref([])
const products = ref([])
const selectedTender = ref(route.query.tenderId || null)
const selectedProduct = ref(null)
const comparisonResult = ref(null)
const smartMatches = ref([])
const smartMatchData = ref(null)
const complianceDetails = ref([])
const priceInfo = ref(null)
const overrides = ref({})

// Gap analysis
const showGapDialog = ref(false)
const gapLoading = ref(false)
const gapData = ref(null)

const passedCount = computed(() => comparisonResult.value?.details?.filter(r => r.status === 'PASS').length || 0)
const failedCount = computed(() => comparisonResult.value?.details?.filter(r => r.status === 'FAIL').length || 0)
const partialCount = computed(() => comparisonResult.value?.details?.filter(r => r.status === 'PARTIAL').length || 0)

const scoreColor = computed(() => {
  const s = comparisonResult.value?.overallScore || 0
  if (s >= 80) return 'var(--success-color)'
  if (s >= 50) return 'var(--warning-color)'
  return 'var(--danger-color)'
})

function statusLabel(s) { return ({ PASS: 'ĐẠT', FAIL: 'KHÔNG ĐẠT', PARTIAL: 'MỘT PHẦN' })[s] || s }
function statusSeverity(s) { return ({ PASS: 'success', FAIL: 'danger', PARTIAL: 'warning' })[s] || 'info' }

function formatPrice(price) {
  if (!price) return '-'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(price)
}

async function fetchTenders() {
  loadingTenders.value = true
  try {
    const r = await apiClient.get('/tenders', { params: { size: 100 } })
    tenders.value = r.data.content || r.data || []
  } catch { toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách gói thầu', life: 5000 }) }
  finally { loadingTenders.value = false }
}

async function fetchProducts() {
  loadingProducts.value = true
  try {
    const r = await apiClient.get('/products', { params: { size: 100 } })
    products.value = r.data.content || r.data || []
  } catch { toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách sản phẩm', life: 5000 }) }
  finally { loadingProducts.value = false }
}

async function onTenderChange() {
  smartMatches.value = []
  comparisonResult.value = null
  smartMatchData.value = null
  complianceDetails.value = []
  priceInfo.value = null
  if (selectedTender.value) {
    try {
      const r = await apiClient.get(`/match/${selectedTender.value}/smart-suggest`, { params: { limit: 5 } })
      smartMatches.value = r.data || []
    } catch { /* silent */ }
  }
}

async function runComparison() {
  if (!selectedTender.value || !selectedProduct.value) return
  comparing.value = true
  comparisonResult.value = null
  complianceDetails.value = []
  priceInfo.value = null
  smartMatchData.value = null
  overrides.value = {}

  try {
    // Run comparison
    const r = await apiClient.post('/match', { tenderId: selectedTender.value, productId: selectedProduct.value })
    comparisonResult.value = r.data

    // Fetch compliance + price in parallel
    const [compRes, priceRes] = await Promise.allSettled([
      apiClient.get(`/match/${selectedTender.value}/product/${selectedProduct.value}/compliance`),
      apiClient.get(`/api/quotations/suggest/${selectedProduct.value}/tender/${selectedTender.value}`)
    ])
    if (compRes.status === 'fulfilled') complianceDetails.value = compRes.value.data || []
    if (priceRes.status === 'fulfilled') priceInfo.value = priceRes.value.data

    // Smart match data for gap warnings
    try {
      const sm = await apiClient.get(`/match/${selectedTender.value}/smart-suggest`, { params: { limit: 10 } })
      const matches = sm.data || []
      smartMatchData.value = matches.find(m => m.productId === selectedProduct.value) || null
    } catch { /* ignore */ }
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể thực hiện so sánh', life: 5000 })
  } finally { comparing.value = false }
}

async function runGapAnalysis() {
  if (!selectedTender.value || !selectedProduct.value) return
  showGapDialog.value = true
  gapLoading.value = true
  gapData.value = null
  try {
    const r = await apiClient.get(`/match/${selectedTender.value}/gap-analysis`, { params: { productId: selectedProduct.value } })
    gapData.value = r.data
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể phân tích khoảng trống', life: 5000 })
  } finally { gapLoading.value = false }
}

// Manual Override
function startOverride(index) {
  const d = comparisonResult.value.details[index]
  overrides.value[index] = { matchResultId: d.matchResultId || d.requirementId, passed: d.status !== 'PASS', reason: '' }
}

function cancelOverride(index) {
  delete overrides.value[index]
  overrides.value = { ...overrides.value }
}

async function applyOverride(index) {
  const ov = overrides.value[index]
  if (!ov) return
  try {
    const detail = comparisonResult.value.details[index]
    // Use the matchResultId from the detail (first field)
    await apiClient.put('/match/results/override', {
      matchResultId: detail.matchResultId || detail.requirementId,
      passed: ov.passed,
      reason: ov.reason || (ov.passed ? 'Ghi đè: Đạt' : 'Ghi đè: Không đạt')
    })
    // Update local state
    detail.status = ov.passed ? 'PASS' : 'FAIL'
    detail.score = ov.passed ? 100 : 0
    detail.notes = '[ĐÃ GHI ĐÈ] ' + (ov.reason || '')
    toast.add({ severity: 'success', summary: 'Đã ghi đè', detail: 'Kết quả đã được cập nhật thủ công', life: 3000 })
    delete overrides.value[index]
    overrides.value = { ...overrides.value }
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể ghi đè kết quả', life: 5000 })
  }
}

function onRowEditSave() {
  toast.add({ severity: 'success', summary: 'Đã lưu', detail: 'Đã cập nhật kết quả', life: 2000 })
}

onMounted(() => {
  fetchTenders()
  fetchProducts()
  if (selectedTender.value) onTenderChange()
})
</script>

<style scoped>
.selection-row { display: flex; align-items: flex-end; gap: 1rem; flex-wrap: wrap; }
.selection-field { display: flex; flex-direction: column; gap: 0.375rem; min-width: 220px; flex: 1; }
.selection-field label { font-size: 0.875rem; font-weight: 600; }
.compare-btn { height: 2.5rem; align-self: flex-end; }
.section-title { font-size: 1.0625rem; font-weight: 600; margin-bottom: 1rem; }

/* Product cell */
.product-cell { display: flex; flex-direction: column; }
.product-link { color: var(--primary-color); font-weight: 500; cursor: pointer; }
.product-meta { font-size: 0.75rem; color: var(--text-secondary); }

/* Cert icons */
.cert-icons { display: flex; gap: 0.35rem; }
.cert-icon { padding: 0.15rem 0.4rem; border-radius: 4px; font-size: 0.7rem; font-weight: 700; }
.cert-ok { background: #d1fae5; color: #065f46; }
.cert-missing { background: #fee2e2; color: #991b1b; text-decoration: line-through; }

/* Score */
.score-cell { display: flex; align-items: center; gap: 0.5rem; }
.text-success { color: var(--success-color); font-weight: 600; }
.text-warning { color: var(--warning-color); font-weight: 600; }
.text-danger { color: var(--danger-color); font-weight: 600; }
.text-muted { color: var(--text-secondary); }

/* Price */
.price-cell { display: flex; flex-direction: column; gap: 0.2rem; }
.price-value { font-weight: 600; font-size: 0.85rem; }

/* Overall */
.overall-score { display: flex; align-items: center; gap: 2rem; padding: 1.5rem; background: var(--surface-ground); border-radius: 12px; flex-wrap: wrap; }
.score-circle { position: relative; display: flex; align-items: center; justify-content: center; }
.score-text { position: absolute; display: flex; flex-direction: column; align-items: center; }
.score-number { font-size: 1.5rem; font-weight: 700; }
.score-label { font-size: 0.75rem; color: var(--text-secondary); }
.score-summary { display: flex; flex-direction: column; gap: 0.75rem; }
.score-stat { display: flex; align-items: center; gap: 0.75rem; font-size: 0.9375rem; font-weight: 500; }

/* Price card in overview */
.price-card { margin-left: auto; padding: 1rem 1.5rem; background: #fff; border-radius: 12px; border: 1px solid var(--surface-border); min-width: 220px; }
.price-card h4 { margin: 0 0 0.75rem; font-size: 0.9rem; }
.price-detail { display: flex; flex-direction: column; gap: 0.5rem; }
.price-main { display: flex; flex-direction: column; }
.price-label { font-size: 0.75rem; color: var(--text-secondary); }
.price-big { font-size: 1.3rem; font-weight: 700; color: var(--success-color); }
.price-aux { display: flex; flex-direction: column; gap: 0.25rem; font-size: 0.8rem; }
.price-aux div { display: flex; justify-content: space-between; }
.price-aux span { color: var(--text-secondary); }

/* Compliance */
.compliance-grid { display: flex; flex-direction: column; gap: 0.5rem; }
.compliance-item { display: flex; align-items: center; gap: 1rem; padding: 0.75rem 1rem; border-radius: 8px; }
.compliant-ok { background: #f0fdf4; border: 1px solid #bbf7d0; }
.compliant-fail { background: #fef2f2; border: 1px solid #fecaca; }
.compliance-header { display: flex; align-items: center; gap: 0.5rem; flex: 1; }
.compliance-detail { margin-left: auto; }
.compliance-detail small { color: var(--danger-color); font-weight: 500; }

/* Gaps */
.gap-warning-item { display: flex; align-items: center; gap: 0.5rem; padding: 0.35rem 0; font-size: 0.85rem; }
.gap-summary { display: flex; gap: 2rem; }
.gap-stat { display: flex; flex-direction: column; align-items: center; }
.gap-num { font-size: 1.5rem; font-weight: 700; }
.gap-item { display: flex; flex-direction: column; gap: 0.25rem; padding: 0.75rem; margin: 0.5rem 0; border-radius: 8px; border-left: 4px solid; }
.gap-critical { background: #fef2f2; border-color: var(--danger-color); }
.gap-warning { background: #fffbeb; border-color: #f59e0b; }
.gap-info { background: #eff6ff; border-color: var(--info-color); }
.gap-item small { color: var(--text-secondary); font-size: 0.8rem; }
.recommendation-list { margin: 0.5rem 0; padding-left: 1.5rem; }
.recommendation-list li { margin: 0.35rem 0; font-size: 0.9rem; }
.gap-price-card { padding: 1rem; background: var(--surface-ground); border-radius: 12px; }
.gap-price-card h4 { margin: 0 0 0.75rem; }
.price-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; }
.price-row div { display: flex; justify-content: space-between; align-items: center; }
.price-row span { font-size: 0.8rem; color: var(--text-secondary); }

/* Override */
.override-cell { display: flex; align-items: center; gap: 0.25rem; }
.override-toggle { width: 70px; font-size: 0.7rem; }

/* Misc */
.comparison-actions { display: flex; justify-content: flex-end; gap: 0.75rem; }
.loading-container { display: flex; align-items: center; justify-content: center; padding: 2rem; }
.mb-4 { margin-bottom: 1.25rem; }
.mt-4 { margin-top: 1.25rem; }
</style>
