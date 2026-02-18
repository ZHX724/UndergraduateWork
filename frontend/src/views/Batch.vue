<template>
  <div class="batch">
    <section class="hero">
      <div class="hero-left">
        <h2 class="h2">批次管理</h2>
        <p class="p">先选择一个农产品，然后为它创建多个生产批次（batch_code）。</p>
      </div>

      <div class="hero-right">
        <button class="btn ghost" type="button" :disabled="loadingProducts" @click="loadProducts">
          {{ loadingProducts ? '刷新中…' : '刷新产品' }}
        </button>
        <button class="btn ghost" type="button" :disabled="!selectedProductId || loadingBatches" @click="loadBatches">
          {{ loadingBatches ? '刷新中…' : '刷新批次' }}
        </button>
      </div>
    </section>

    <div v-if="msg" class="toast" :class="msgType">{{ msg }}</div>

    <section class="grid2">
      <!-- 左：选择产品 + 新建批次 -->
      <div class="card">
        <div class="card-title">选择农产品</div>

        <div class="field">
          <div class="label">农产品</div>
          <select class="input" v-model="selectedProductId" @change="onProductChange">
            <option value="">请选择…</option>
            <option v-for="p in products" :key="p.id" :value="String(p.id)">
              #{{ p.id }} · {{ p.name }}
            </option>
          </select>
        </div>

        <div class="miniTip" v-if="selectedProduct">
          当前选择：<b>{{ selectedProduct.name }}</b>（#{{ selectedProduct.id }}）
        </div>

        <div class="divider"></div>

        <div class="card-title">新建批次</div>

        <div class="field">
          <div class="label">批次号 batchCode</div>
          <input class="input" v-model="form.batchCode" placeholder="例如：20260218-A01" />
        </div>

        <div class="field">
          <div class="label">生产日期 productionDate（可选）</div>
          <input class="input" v-model="form.productionDate" placeholder="yyyy-MM-dd" />
        </div>

        <div class="actions">
          <button class="btn mini" type="button" :disabled="creating" @click="createBatch">
            {{ creating ? '创建中…' : '创建批次' }}
          </button>
          <button class="btn ghost" type="button" @click="resetForm">清空</button>
        </div>
      </div>

      <!-- 右：批次列表 -->
      <div class="card">
        <div class="card-title">批次列表</div>

        <div v-if="!selectedProductId" class="empty">请先选择一个农产品。</div>
        <div v-else-if="loadingBatches" class="empty">加载中…</div>
        <div v-else-if="batches.length === 0" class="empty">暂无批次。</div>

        <div v-else class="list">
          <div v-for="b in batches" :key="b.id" class="item">
            <div class="item-top">
              <div class="name">{{ b.batchCode }}</div>
              <div class="id">#{{ b.id }}</div>
            </div>

            <div class="meta">
              <div>生产日期：{{ b.productionDate || '-' }}</div>
              <div>创建时间：{{ formatTime(b.createdAt || b.created_at) }}</div>
            </div>

            <div class="item-actions">
              <button class="btn mini ghost" type="button" @click="goChain(b.id)">去上链</button>
              <button class="btn danger" type="button" :disabled="deletingId===b.id" @click="delBatch(b.id)">
                {{ deletingId===b.id ? '删除中…' : '删除' }}
              </button>
            </div>
          </div>
        </div>

      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import request from '@/utils/request'

const products = ref([])
const loadingProducts = ref(false)

const selectedProductId = ref('')
const batches = ref([])
const loadingBatches = ref(false)

const form = reactive({
  batchCode: '',
  productionDate: ''
})
const creating = ref(false)
const deletingId = ref(null)

const msg = ref('')
const msgType = ref('ok')

function toast(text, type = 'ok') {
  msg.value = text
  msgType.value = type
  window.clearTimeout(toast._t)
  toast._t = window.setTimeout(() => {
    msg.value = ''
  }, 3000)
}

const selectedProduct = computed(() => {
  const id = Number(selectedProductId.value)
  return products.value.find(p => p.id === id) || null
})

const formatTime = (t) => {
  if (!t) return '-'
  return String(t).replace('T', ' ')
}

const resetForm = () => {
  form.batchCode = ''
  form.productionDate = ''
}

const loadProducts = async () => {
  loadingProducts.value = true
  try {
    const res = await request.get('/product/my')
    products.value = res.data || []
  } catch (e) {
    toast(String(e?.response?.data || '获取产品失败'), 'err')
  } finally {
    loadingProducts.value = false
  }
}

const loadBatches = async () => {
  if (!selectedProductId.value) return
  loadingBatches.value = true
  try {
    const res = await request.get('/batch/list', { params: { productId: selectedProductId.value } })
    batches.value = res.data || []
  } catch (e) {
    toast(String(e?.response?.data || '获取批次失败'), 'err')
  } finally {
    loadingBatches.value = false
  }
}

const onProductChange = async () => {
  batches.value = []
  await loadBatches()
}

const createBatch = async () => {
  if (!selectedProductId.value) return toast('请先选择农产品', 'err')
  if (!form.batchCode.trim()) return toast('batchCode 不能为空', 'err')

  creating.value = true
  try {
    const res = await request.post(
        '/batch/create',
        new URLSearchParams({
          productId: selectedProductId.value,
          batchCode: form.batchCode.trim(),
          productionDate: form.productionDate.trim()
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )
    toast(String(res.data || '创建成功'), 'ok')
    resetForm()
    await loadBatches()
  } catch (e) {
    toast(String(e?.response?.data || '创建失败'), 'err')
  } finally {
    creating.value = false
  }
}

const delBatch = async (batchId) => {
  if (!confirm('确定删除该批次？')) return
  deletingId.value = batchId
  try {
    const res = await request.post(
        '/batch/delete',
        new URLSearchParams({ batchId }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )
    toast(String(res.data || '删除成功'), 'ok')
    await loadBatches()
  } catch (e) {
    toast(String(e?.response?.data || '删除失败'), 'err')
  } finally {
    deletingId.value = null
  }
}

// 先用 localStorage 让 records 页自动带入 batchId（下一步再优化成事件/状态管理）
const goChain = (batchId) => {
  localStorage.setItem('selectedBatchId', String(batchId))
  toast('已选择批次，去“溯源记录”上链', 'ok')
}
loadProducts()
</script>

<style scoped>
.batch{ display:flex; flex-direction:column; gap:12px; }

.hero{
  display:flex; align-items:center; justify-content:space-between; gap:14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px 16px;
}
.h2{ margin:0 0 6px; font-weight:900; color:#0f172a; }
.p{ margin:0; color:#64748b; font-size:13px; }
.hero-right{ display:flex; gap:10px; }

.toast{
  padding:10px 12px; border-radius:14px;
  border:1px solid rgba(226,232,240,0.9);
  background: rgba(255,255,255,0.86);
  font-size:13px;
}
.toast.ok{ color:#16a34a; border-color: rgba(22,163,74,0.25); }
.toast.err{ color:#ef4444; border-color: rgba(239,68,68,0.25); }

.grid2{
  display:grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 12px;
}
.card{
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.08);
  min-height: 220px;
}
.card-title{ font-weight:900; color:#0f172a; margin-bottom:10px; }

.field .label{ font-size:12px; color:#475569; margin-bottom:6px; }
.input{
  width:100%;
  border:1px solid rgba(15,23,42,0.12);
  border-radius:12px;
  padding:10px 12px;
  outline:none;
  font-size:14px;
  background:#fff;
}

.actions{ display:flex; gap:10px; margin-top: 10px; }

.btn{
  padding:8px 12px;
  border-radius:12px;
  border:1px solid rgba(226,232,240,0.9);
  background:#fff;
  cursor:pointer;
  color:#0f172a;
}
.btn.ghost{ background: transparent; }
.btn.mini{
  padding:8px 12px;
  border-radius:12px;
  background: linear-gradient(135deg,#2563eb,#1d4ed8);
  color:#fff;
  border:none;
  font-weight:800;
}
.btn.mini.ghost{
  background: transparent;
  border:1px solid rgba(226,232,240,0.9);
  color:#0f172a;
}
.btn.danger{
  background:#ef4444;
  border:none;
  color:#fff;
  font-weight:800;
}
.btn:disabled{ opacity:0.6; cursor:not-allowed; }

.divider{
  height: 1px;
  background: rgba(148,163,184,0.25);
  margin: 12px 0;
}
.miniTip{ font-size:12px; color:#64748b; margin-top:8px; }

.empty{ color:#64748b; font-size:13px; padding:10px 2px; }

.list{ display:flex; flex-direction:column; gap: 10px; }
.item{
  border:1px solid rgba(15,23,42,0.08);
  border-radius:14px;
  padding:12px;
  background: rgba(255,255,255,0.9);
}
.item-top{ display:flex; align-items:center; justify-content:space-between; }
.name{ font-weight:900; color:#0f172a; }
.id{ font-size:12px; color:#94a3b8; }
.meta{ margin-top:8px; font-size:12px; color:#64748b; line-height:1.6; }
.item-actions{ margin-top:10px; display:flex; gap:10px; }

/* 让左右两张卡片更“长”，视觉更饱满 */
.grid2 {
  align-items: stretch; /* 让左右卡片同高（关键） */
}

.grid2 > .card {
  min-height: 420px;      /* 你可以调成 460/520 看效果 */
}

/* 左侧卡片：用 flex 让内容纵向分布，避免三段文字挤成一团 */
.grid2 > .card:first-child {
  display: flex;
  flex-direction: column;
}

/* 左侧卡片里的内容区（你原来的各块），让表单区自动撑开 */
.grid2 > .card:first-child .field:last-of-type {
  margin-bottom: auto;
}

/* 按钮区贴底，整体更稳 */
.grid2 > .card:first-child .actions {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px dashed rgba(148, 163, 184, 0.35);
}

@media (max-width: 980px){
  .grid2{ grid-template-columns: 1fr; }
}
</style>
