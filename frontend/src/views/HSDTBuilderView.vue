<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Tạo hồ sơ dự thầu (HSDT)</h1>
        <p class="page-subtitle">Bảng so sánh kỹ thuật, giá chào, gom tài liệu, checklist, xuất Word/PDF/ZIP</p>
      </div>
    </div>

    <!-- Steps -->
    <div class="steps-bar mb-4">
      <div v-for="(s, i) in steps" :key="i" :class="['step', { active: currentStep === i, done: currentStep > i }]" @click="currentStep < i ? null : goToStep(i)">
        <div class="step-circle">{{ currentStep > i ? '✓' : i + 1 }}</div>
        <span class="step-label">{{ s }}</span>
      </div>
    </div>

    <!-- STEP 1 -->
    <div v-if="currentStep === 0" class="card-container">
      <h3 class="section-title">Bước 1: Chọn gói thầu</h3>
      <Dropdown v-model="selectedTender" :options="tenders" optionLabel="name" optionValue="id"
        placeholder="Chọn gói thầu cần dự thầu" :loading="loadingTenders" filter showClear fluid @change="onTenderChange" class="w-full mb-4" />
      <div v-if="tenderPreview" class="tender-preview">
        <h4>{{ tenderPreview.name }}</h4>
        <div class="preview-grid">
          <div class="preview-item"><span>Mã gói</span><strong>{{ tenderPreview.bidPackageCode || '-' }}</strong></div>
          <div class="preview-item"><span>Bên mời thầu</span><strong>{{ tenderPreview.procuringEntity || '-' }}</strong></div>
          <div class="preview-item"><span>Hạn nộp</span><strong>{{ fmtDate(tenderPreview.submissionDeadline) }}</strong></div>
          <div class="preview-item"><span>Giá dự toán</span><strong>{{ fmtPrice(tenderPreview.estimatedValue) }}</strong></div>
        </div>
        <Button label="Tiếp tục → Chọn sản phẩm" icon="pi pi-arrow-right" @click="goToStep(1)" class="mt-3" />
      </div>
    </div>

    <!-- STEP 2 -->
    <div v-if="currentStep === 1" class="step2-container">
      <div class="card-container mb-4">
        <div class="flex-between mb-3"><h3 class="section-title" style="margin:0">Bước 2: Chọn sản phẩm dự thầu</h3>
          <Button label="Tiếp tục → Xem xét" icon="pi pi-arrow-right" :disabled="selectedProducts.length === 0" @click="goToStep(2)" /></div>
        <div class="dual-pane">
          <div class="pane"><div class="pane-header">Sản phẩm có sẵn</div>
            <div style="padding:0.5rem"><InputText v-model="productSearch" placeholder="Tìm sản phẩm..." class="w-full" /></div>
            <div class="product-list">
              <div v-for="p in filteredAvailable" :key="p.id" :class="['product-item', { sel: isSel(p.id) }]" @click="toggleProduct(p)">
                <div class="product-info"><span class="product-name">{{ p.name }}</span><span class="product-meta">{{ p.manufacturer || '-' }} · {{ p.category || '-' }}</span>
                  <div class="product-certs"><span v-if="p.hasIso" class="cd ok">ISO</span><span v-if="p.hasCe" class="cd ok">CE</span><span v-if="p.hasFda" class="cd ok">FDA</span><span :class="['cd', p.hasCoCq ? 'ok' : 'no']">CQ</span></div></div>
                <Button :icon="isSel(p.id) ? 'pi pi-check' : 'pi pi-plus'" :severity="isSel(p.id) ? 'success' : 'secondary'" text rounded size="small" /></div>
            </div></div>
          <div class="pane"><div class="pane-header">Đã chọn ({{ selectedProducts.length }})</div>
            <div class="product-list"><div v-if="selectedProducts.length===0" class="empty-hint">Chọn sản phẩm từ danh sách bên trái</div>
              <div v-for="p in selectedProducts" :key="p.id" class="selected-item"><div class="selected-info"><strong>{{ p.name }}</strong><small>{{ p.manufacturer || '-' }}</small></div><Button icon="pi pi-times" severity="danger" text rounded size="small" @click="removeProduct(p.id)" /></div>
            </div></div>
        </div>
      </div>
    </div>

    <!-- STEP 3: Review -->
    <div v-if="currentStep === 2">
      <div v-if="previewLoading" class="loading-container"><ProgressSpinner /><p style="margin-left:1rem">Đang phân tích hồ sơ dự thầu...</p></div>
      <template v-else-if="hsdtData">
        <!-- Summary -->
        <div class="card-container mb-4"><h3 class="section-title">Bước 3: Xem xét hồ sơ dự thầu</h3>
          <div class="summary-grid">
            <div class="summary-card"><span class="sn text-primary">{{ hsdtData.totalProducts }}</span><span class="sl">Sản phẩm</span></div>
            <div class="summary-card"><span class="sn text-success">{{ hsdtData.completeProducts }}</span><span class="sl">Đạt đầy đủ</span></div>
            <div class="summary-card"><span :class="['sn', hsdtData.incompleteProducts>0?'text-danger':'text-success']">{{ hsdtData.incompleteProducts }}</span><span class="sl">Còn thiếu</span></div>
            <div class="summary-card"><span class="sn text-primary">{{ fmtPrice(hsdtData.totalPrice) }}</span><span class="sl">Tổng giá dự kiến</span></div>
          </div>
        </div>

        <!-- Tech Comparison -->
        <div class="card-container mb-4"><h3 class="section-title"><i class="pi pi-chart-bar" /> Bảng so sánh kỹ thuật</h3>
          <DataTable :value="hsdtData.products">
            <Column field="itemNumber" header="#" style="width:50px" />
            <Column field="productName" header="Sản phẩm"><template #body="{data}"><div class="pc"><strong>{{ data.productName }}</strong><small>{{ data.manufacturer }} · {{ data.model }}</small></div></template></Column>
            <Column field="matchScore" header="Điểm" sortable style="width:140px"><template #body="{data}"><div class="sc"><ProgressBar :value="data.matchScore" :style="{width:'80px',height:'6px'}" /><small>{{ data.matchScore }}%</small></div></template></Column>
            <Column header="Đạt/TC" style="width:80px"><template #body="{data}"><Tag :value="`${data.passedRequirements}/${data.totalRequirements}`" :severity="data.allMandatoryPassed?'success':'danger'" /></template></Column>
            <Column header="Chứng chỉ" style="width:150px"><template #body="{data}"><div class="cis"><span v-for="d in data.documents.filter(x=>['ISO_13485','CE','FDA','CO','CQ'].includes(x.docType))" :key="d.docType" :class="['cb', d.available&&!d.expired?'ok':d.expired?'exp':'mis']" v-tooltip.top="d.docType+(d.expired?' (hết hạn)':d.available?'':'(thiếu)')">{{ d.docType.replace('ISO_','') }}</span></div></template></Column>
            <Column header="Giá gợi ý" style="width:150px"><template #body="{data}"><div class="prc">{{ fmtPrice(data.suggestedPrice) }}<Tag :value="data.priceConfidence||'THAP'" :severity="data.priceConfidence==='CAO'?'success':'warning'" style="font-size:0.6rem" /></div></template></Column>
          </DataTable>
        </div>

        <!-- Checklist -->
        <div class="card-container mb-4"><div class="flex-between mb-3"><h3 class="section-title" style="margin:0"><i class="pi pi-list" /> Checklist hồ sơ ({{ clStats.total }})</h3><div class="flex-center gap-2"><Tag :value="'✓ '+clStats.ok" severity="success" /><Tag :value="'⚠ '+clStats.warning" severity="warning" /><Tag :value="'✗ '+clStats.missing" severity="danger" /></div></div>
          <div v-for="sec in checklistSections" :key="sec" class="clsec"><h4 :class="['clst', 'clst-'+sec.toLowerCase()]">{{ fmtSec(sec) }}</h4>
            <div v-for="item in clItems(sec)" :key="item.item" :class="['cli', 'cli-'+item.status.toLowerCase()]">
              <i :class="clIcon(item.status)" :style="{color:clColor(item.status)}" /><span :class="{'fw':item.mandatory}">{{ item.item }}</span><small v-if="item.detail" class="cld">{{ item.detail }}</small></div>
          </div>
        </div>

        <!-- Enterprise -->
        <div v-if="hsdtData.companyName" class="card-container mb-4"><h3 class="section-title"><i class="pi pi-building" /> Hồ sơ pháp lý doanh nghiệp</h3><div class="ei"><div><span>Công ty</span><strong>{{ hsdtData.companyName }}</strong></div><div><span>MST</span><strong>{{ hsdtData.taxCode||'-' }}</strong></div><div><span>Địa chỉ</span><strong>{{ hsdtData.companyAddress||'-' }}</strong></div><div><span>Đại diện</span><strong>{{ hsdtData.legalRepresentative||'-' }}</strong></div></div></div>

        <!-- Export -->
        <div class="card-container"><h3 class="section-title">Bước 4: Xuất hồ sơ</h3><div class="export-actions">
          <Button label="Word (.docx)" icon="pi pi-file-word" severity="info" :loading="exporting==='word'" @click="doExport('word')" />
          <Button label="PDF (.pdf)" icon="pi pi-file-pdf" severity="danger" :loading="exporting==='pdf'" @click="doExport('pdf')" />
          <Button label="Excel (.xlsx)" icon="pi pi-file-excel" severity="success" :loading="exporting==='excel'" @click="doExport('excel')" />
          <Button label="ZIP (đầy đủ)" icon="pi pi-box" severity="warning" :loading="exporting==='zip'" @click="doExport('zip')" /></div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import apiClient from '@/api/client'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import ProgressBar from 'primevue/progressbar'
import ProgressSpinner from 'primevue/progressspinner'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

const toast = useToast()
const steps = ['Chọn gói thầu', 'Chọn sản phẩm', 'Xem xét & Xuất']
const currentStep = ref(0)

const loadingTenders = ref(false), tenders = ref([]), selectedTender = ref(null), tenderPreview = ref(null)
const products = ref([]), productSearch = ref(''), selectedProducts = ref([])
const previewLoading = ref(false), hsdtData = ref(null), exporting = ref(null)

const checklistSections = ['HANH_CHINH','KY_THUAT','CHUNG_CHI','TAI_CHINH','KHAC']

const filteredAvailable = computed(() => {
  if (!productSearch.value) return products.value.filter(p => !isSel(p.id))
  const q = productSearch.value.toLowerCase()
  return products.value.filter(p => !isSel(p.id) && (p.name?.toLowerCase().includes(q) || p.manufacturer?.toLowerCase().includes(q)))
})

const clStats = computed(() => {
  const items = hsdtData.value?.checklist || []
  return { total: items.length, ok: items.filter(i=>i.status==='OK').length, warning: items.filter(i=>i.status==='WARNING'||i.status==='EXPIRED').length, missing: items.filter(i=>i.status==='MISSING').length }
})

function isSel(id) { return selectedProducts.value.some(p => p.id === id) }
function toggleProduct(p) { isSel(p.id) ? removeProduct(p.id) : selectedProducts.value.push(p) }
function removeProduct(id) { selectedProducts.value = selectedProducts.value.filter(p => p.id !== id) }
function clItems(s) { return (hsdtData.value?.checklist||[]).filter(i=>i.section===s) }
function fmtSec(s) { return ({HANH_CHINH:'I. Tài liệu hành chính',KY_THUAT:'II. Tài liệu kỹ thuật',CHUNG_CHI:'III. Chứng chỉ',TAI_CHINH:'IV. Tài chính',KHAC:'V. Khác'})[s]||s }
function clIcon(s) { return ({OK:'pi pi-check-circle',WARNING:'pi pi-exclamation-circle',MISSING:'pi pi-times-circle',EXPIRED:'pi pi-clock'})[s]||'pi pi-circle' }
function clColor(s) { return ({OK:'var(--success-color)',WARNING:'#f59e0b',MISSING:'var(--danger-color)',EXPIRED:'#ef4444'})[s]||'var(--text-secondary)' }
function fmtDate(d) { return d ? new Date(d).toLocaleDateString('vi-VN') : '-' }
function fmtPrice(p) { return p ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND',maximumFractionDigits:0}).format(p) : '-' }

function goToStep(i) {
  currentStep.value = i
  if (i === 2) buildHSDT()
}

async function fetchTenders() {
  loadingTenders.value = true
  try { const r = await apiClient.get('/tenders',{params:{size:100}}); tenders.value = r.data.content||r.data||[] }
  catch { toast.add({severity:'error',summary:'Lỗi',detail:'Không thể tải gói thầu',life:5000}) }
  finally { loadingTenders.value = false }
}

async function fetchProducts() {
  try { const r = await apiClient.get('/products',{params:{size:100}}); products.value = r.data.content||r.data||[] } catch {}
}

function onTenderChange() {
  tenderPreview.value = selectedTender.value ? tenders.value.find(t=>t.id===selectedTender.value) : null
}

async function buildHSDT() {
  previewLoading.value = true; hsdtData.value = null
  try {
    const r = await apiClient.post('/hsdt/preview',{tenderId:selectedTender.value,productIds:selectedProducts.value.map(p=>p.id)})
    hsdtData.value = r.data
  } catch (e) {
    toast.add({severity:'error',summary:'Lỗi',detail:'Không thể tạo preview HSDT',life:5000})
    currentStep.value = 1
  } finally { previewLoading.value = false }
}

async function doExport(format) {
  exporting.value = format
  try {
    const r = await apiClient.post(`/hsdt/export/${format}`,{tenderId:selectedTender.value,productIds:selectedProducts.value.map(p=>p.id)},{responseType:'blob'})
    const ext = {word:'docx',pdf:'pdf',zip:'zip',excel:'xlsx'}[format]||format
    const url = window.URL.createObjectURL(new Blob([r.data]))
    const a = document.createElement('a'); a.href = url; a.download = `HSDT_${hsdtData.value?.tenderName||'export'}.${ext}`
    document.body.appendChild(a); a.click(); a.remove(); window.URL.revokeObjectURL(url)
    toast.add({severity:'success',summary:'Thành công',detail:`Đã xuất ${format.toUpperCase()}`,life:3000})
  } catch { toast.add({severity:'error',summary:'Lỗi',detail:`Không thể xuất ${format.toUpperCase()}`,life:5000}) }
  finally { exporting.value = null }
}

onMounted(() => { fetchTenders(); fetchProducts() })
</script>

<style scoped>
.steps-bar{display:flex;gap:0;background:var(--surface-card);border-radius:12px;overflow:hidden;border:1px solid var(--surface-border)}
.step{flex:1;display:flex;align-items:center;gap:.6rem;padding:.85rem 1.25rem;cursor:pointer;transition:all .2s}
.step.active{background:var(--primary-color);color:#fff}.step.active .step-circle{background:#fff;color:var(--primary-color)}
.step.done{background:#d1fae5}.step.done .step-circle{background:var(--success-color);color:#fff}
.step-circle{width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:.8rem;background:var(--surface-ground);flex-shrink:0}
.step-label{font-size:.8rem;font-weight:600;white-space:nowrap}
.section-title{font-size:1.05rem;font-weight:600;margin-bottom:1rem}.mb-3{margin-bottom:.75rem}.mb-4{margin-bottom:1.25rem}.mt-3{margin-top:.75rem}.w-full{width:100%}.gap-2{gap:.5rem}
.tender-preview{background:var(--surface-ground);padding:1.5rem;border-radius:12px}.tender-preview h4{margin:0 0 1rem;font-size:1.1rem}
.preview-grid{display:grid;grid-template-columns:1fr 1fr;gap:.75rem}.preview-item{display:flex;justify-content:space-between;font-size:.85rem}
.preview-item span{color:var(--text-secondary)}.preview-item strong{font-weight:600}
.dual-pane{display:grid;grid-template-columns:1fr 1fr;gap:1.5rem}
.pane{border:1px solid var(--surface-border);border-radius:10px;overflow:hidden}.pane-header{padding:.75rem 1rem;background:var(--surface-ground);font-weight:600;font-size:.85rem;border-bottom:1px solid var(--surface-border)}
.product-list{max-height:400px;overflow-y:auto;padding:.5rem}
.product-item{display:flex;align-items:center;justify-content:space-between;padding:.65rem .75rem;border-radius:8px;cursor:pointer;transition:all .15s;margin-bottom:.25rem}
.product-item:hover{background:var(--surface-ground)}.product-item.sel{background:#dbeafe;border:1px solid #93c5fd}
.product-info{display:flex;flex-direction:column;gap:.15rem}.product-name{font-weight:500;font-size:.85rem}.product-meta{font-size:.72rem;color:var(--text-secondary)}
.product-certs{display:flex;gap:.3rem;margin-top:.2rem}.cd{padding:.1rem .35rem;border-radius:3px;font-size:.6rem;font-weight:700}.cd.ok{background:#d1fae5;color:#065f46}.cd.no{background:#fee2e2;color:#991b1b}
.selected-item{display:flex;align-items:center;justify-content:space-between;padding:.65rem .75rem;border-radius:8px;background:#f0fdf4;border:1px solid #bbf7d0;margin-bottom:.25rem}
.selected-info{display:flex;flex-direction:column}.selected-info strong{font-size:.85rem}.selected-info small{font-size:.72rem;color:var(--text-secondary)}
.empty-hint{padding:2rem;text-align:center;color:var(--text-secondary);font-size:.85rem}
.summary-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem}.summary-card{display:flex;flex-direction:column;align-items:center;padding:1.25rem;background:var(--surface-ground);border-radius:10px}
.sn{font-size:1.75rem;font-weight:700}.sl{font-size:.75rem;color:var(--text-secondary);margin-top:.25rem}
.text-primary{color:var(--primary-color)}.text-success{color:var(--success-color)}.text-danger{color:var(--danger-color)}
.sc{display:flex;align-items:center;gap:.5rem}.pc{display:flex;flex-direction:column}.pc strong{font-size:.85rem}.pc small{font-size:.7rem;color:var(--text-secondary)}
.cis{display:flex;gap:.25rem;flex-wrap:wrap}.cb{padding:.15rem .4rem;border-radius:4px;font-size:.65rem;font-weight:700}.cb.ok{background:#d1fae5;color:#065f46}.cb.exp{background:#fef3c7;color:#92400e}.cb.mis{background:#fee2e2;color:#991b1b}
.prc{display:flex;flex-direction:column;gap:.2rem;align-items:flex-start;font-weight:600;font-size:.85rem}
.clsec{margin-bottom:1rem}.clst{font-size:.9rem;font-weight:700;margin:.5rem 0;padding:.4rem .75rem;border-radius:6px}
.clst-hanh_chinh{background:#dbeafe;color:#1e40af}.clst-ky_thuat{background:#d1fae5;color:#065f46}.clst-chung_chi{background:#fef3c7;color:#92400e}.clst-tai_chinh{background:#ede9fe;color:#5b21b6}.clst-khac{background:var(--surface-ground);color:var(--text-secondary)}
.cli{display:grid;grid-template-columns:24px 1fr;align-items:center;gap:.5rem;padding:.4rem .5rem;font-size:.85rem;border-radius:6px;margin-bottom:.2rem}
.cli-warning{background:#fffbeb}.cli-missing{background:#fef2f2}.cli-expired{background:#fef2f2}
.cld{grid-column:2;font-size:.75rem;color:var(--text-secondary);font-style:italic}.fw{font-weight:600}.cli i{font-size:1rem;text-align:center}
.ei{display:grid;grid-template-columns:1fr 1fr;gap:.75rem}.ei div{display:flex;flex-direction:column;gap:.2rem}.ei span{font-size:.75rem;color:var(--text-secondary)}.ei strong{font-size:.9rem}
.export-actions{display:flex;gap:1rem;flex-wrap:wrap}.loading-container{display:flex;align-items:center;justify-content:center;padding:3rem}
@media(max-width:768px){.dual-pane,.summary-grid,.preview-grid,.ei{grid-template-columns:1fr}.steps-bar{flex-direction:column}}
</style>
