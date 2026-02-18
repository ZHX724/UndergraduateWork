<template>
  <div class="wrap">
    <section class="hero">
      <div class="hero-left">
        <h2 class="h2">溯源查询（管理端）</h2>
        <p class="p">选择农产品与批次，查询该批次上链记录，并可校验链完整性。</p>
      </div>
      <div class="hero-right">
        <button class="btn ghost" type="button" @click="refresh">刷新</button>
      </div>
    </section>

    <section class="grid2">
      <!-- 左：条件 -->
      <div class="card">
        <div class="card-title">查询条件</div>

        <div class="field">
          <div class="label">选择农产品</div>
          <select class="input" v-model="productId" @change="onProductChange">
            <option value="">请选择…</option>
            <option v-for="p in products" :key="p.id" :value="String(p.id)">
              #{{ p.id }} · {{ p.name }}
            </option>
          </select>
        </div>

        <div class="field">
          <div class="label">选择批次</div>
          <select class="input" v-model="batchId">
            <option value="">请选择…</option>
            <option v-for="b in batches" :key="b.id" :value="String(b.id)">
              #{{ b.id }} · {{ b.batchCode }}
            </option>
          </select>
          <div class="hint">提示：也可以先去“批次管理”创建批次。</div>
        </div>

        <div class="actions">
          <button class="btn primary" :disabled="loading || !batchId" @click="queryByBatchId">
            {{ loading ? '查询中…' : '查询' }}
          </button>
          <button class="btn ghost" @click="clear">清空</button>
        </div>

        <div v-if="verifyMsg" class="verify" :class="verifyOk ? 'ok' : 'err'">
          {{ verifyMsg }}
        </div>

        <button class="btn ghost w100" :disabled="loading || !batchId" @click="verifyChain">
          校验链
        </button>
      </div>

      <!-- 右：时间线 -->
      <div class="card">
        <div class="card-title">溯源时间线（按 height 升序）</div>

        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="chain.length===0" class="empty">暂无数据。</div>

        <div v-else class="timeline">
          <div v-for="r in chain" :key="r.id" class="node">
            <div class="dot"></div>
            <div class="box">
              <div class="top">
                <div class="tag">{{ r.type }}</div>
                <div class="h">Height #{{ r.height }}</div>
              </div>
              <div class="content">{{ r.content }}</div>
              <div class="meta">
                <span>时间：{{ fmt(r.createdAt || r.created_at) }}</span>
                <span>状态：{{ r.status }}</span>
              </div>

              <details class="detail">
                <summary>区块详情（hash/nonce）</summary>
                <div class="kv"><b>prevHash</b>：{{ r.prevHash }}</div>
                <div class="kv"><b>dataHash</b>：{{ r.dataHash }}</div>
                <div class="kv"><b>blockHash</b>：{{ r.blockHash }}</div>
                <div class="kv"><b>nonce</b>：{{ r.nonce }}</div>
              </details>
            </div>
          </div>
        </div>

      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'

const products = ref([])
const batches = ref([])
const productId = ref('')
const batchId = ref('')

const chain = ref([])
const loading = ref(false)

const verifyMsg = ref('')
const verifyOk = ref(true)

const fmt = (t) => (t ? String(t).replace('T',' ') : '-')

const loadProducts = async () => {
  try {
    const res = await request.get('/product/my')
    products.value = res.data || []
  } catch (e) {
    products.value = []
  }
}

const onProductChange = async () => {
  batchId.value = ''
  chain.value = []
  verifyMsg.value = ''
  if (!productId.value) {
    batches.value = []
    return
  }
  try {
    const res = await request.get('/batch/list', { params: { productId: productId.value } })
    batches.value = res.data || []
  } catch (e) {
    batches.value = []
  }
}

const queryByBatchId = async () => {
  if (!batchId.value) return
  loading.value = true
  verifyMsg.value = ''
  try {
    const res = await request.get('/trace/chain', { params: { batchId: batchId.value } })
    chain.value = res.data || []
  } finally {
    loading.value = false
  }
}

const verifyChain = async () => {
  if (!batchId.value) return
  try {
    const res = await request.get('/trace/verify', { params: { batchId: batchId.value } })
    const data = res.data || {}
    verifyOk.value = !!data.ok
    verifyMsg.value = data.ok ? (data.msg || '链校验通过') : (data.reason || '链校验失败')
  } catch (e) {
    verifyOk.value = false
    verifyMsg.value = '校验失败（接口异常）'
  }
}

const clear = () => {
  productId.value = ''
  batchId.value = ''
  batches.value = []
  chain.value = []
  verifyMsg.value = ''
}

const refresh = async () => {
  await loadProducts()
  verifyMsg.value = ''
}

refresh()
</script>

<style scoped>
.wrap{ display:flex; flex-direction:column; gap:12px; }
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

.grid2{ display:grid; grid-template-columns: 0.9fr 1.1fr; gap:12px; align-items: stretch; }
.card{
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.08);
  min-height: 360px;
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
.hint{ margin-top:6px; font-size:12px; color:#94a3b8; }

.actions{ display:flex; gap:10px; margin-top: 12px; }

.btn{
  padding:8px 12px;
  border-radius:12px;
  border:1px solid rgba(226,232,240,0.9);
  background:#fff;
  cursor:pointer;
  color:#0f172a;
}
.btn.ghost{ background: transparent; }
.btn.primary{
  background: linear-gradient(135deg,#2563eb,#1d4ed8);
  border:none;
  color:#fff;
  font-weight:900;
}
.btn:disabled{ opacity:0.6; cursor:not-allowed; }
.w100{ width:100%; margin-top: 10px; }

.verify{
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  border: 1px solid rgba(226,232,240,0.9);
  background: rgba(255,255,255,0.92);
}
.verify.ok{ color:#16a34a; border-color: rgba(22,163,74,0.25); }
.verify.err{ color:#ef4444; border-color: rgba(239,68,68,0.25); }

.empty{ color:#64748b; font-size:13px; padding:10px 2px; }

.timeline{ display:flex; flex-direction:column; gap:12px; margin-top: 6px; }
.node{ display:flex; gap:12px; align-items:flex-start; }
.dot{
  width: 12px; height: 12px; border-radius: 999px;
  background: #2563eb;
  margin-top: 14px;
  box-shadow: 0 10px 18px rgba(37,99,235,0.25);
}
.box{
  flex:1;
  border:1px solid rgba(15,23,42,0.08);
  border-radius:14px;
  padding:12px;
  background: rgba(255,255,255,0.92);
}
.top{ display:flex; align-items:center; justify-content:space-between; gap:10px; }
.tag{
  font-size:12px;
  padding:6px 10px;
  border-radius:999px;
  font-weight:900;
  color:#1d4ed8;
  background: rgba(37,99,235,0.10);
  border: 1px solid rgba(37,99,235,0.15);
}
.h{ font-weight:900; color:#0f172a; }
.content{ margin-top: 8px; color:#0f172a; line-height:1.6; }
.meta{ margin-top: 8px; color:#64748b; font-size:12px; display:flex; gap:12px; flex-wrap:wrap; }
.detail{ margin-top: 10px; font-size: 12px; color:#334155; }
.kv{ margin-top: 6px; word-break: break-all; }

@media (max-width: 980px){
  .grid2{ grid-template-columns: 1fr; }
}
</style>
