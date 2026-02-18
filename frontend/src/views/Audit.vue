<template>
  <div class="wrap">
    <!-- 顶部说明条 -->
    <section class="hero">
      <div class="hero-left">
        <div class="badge">监管端</div>
        <h2 class="h2">溯源信息审核与链完整性校验</h2>
        <p class="p">
          选择批次后可查看链时间线，并一键校验区块链（prev_hash / data_hash / block_hash / PoW）。
        </p>
      </div>

      <div class="hero-right">
        <button class="btn ghost" type="button" @click="refreshBatches" :disabled="loadingBatches">
          {{ loadingBatches ? '刷新中…' : '刷新批次' }}
        </button>
        <button class="btn" type="button" @click="verifyNow" :disabled="!selectedBatchId || loadingVerify">
          {{ loadingVerify ? '校验中…' : '校验链' }}
        </button>
      </div>
    </section>

    <section class="grid">
      <!-- 左：批次选择 -->
      <div class="card">
        <div class="card-title">选择批次</div>
        <div class="card-sub">监管端可查看所有批次。选择后自动加载时间线。</div>

        <div class="field">
          <div class="label">批次（batch）</div>
          <select class="select" v-model="selectedBatchId" @change="loadChain">
            <option value="">请选择批次</option>
            <option v-for="b in batches" :key="b.id" :value="b.id">
              #{{ b.id }} · {{ b.batchCode }} · 产品ID: {{ b.productId }}
            </option>
          </select>
        </div>

        <div class="mini-info" v-if="curBatch">
          <div class="line"><b>批次ID：</b>{{ curBatch.id }}</div>
          <div class="line"><b>批次码：</b>{{ curBatch.batchCode }}</div>
          <div class="line"><b>产品ID：</b>{{ curBatch.productId }}</div>
          <div class="line"><b>生产日期：</b>{{ curBatch.productionDate || '-' }}</div>
        </div>

        <div class="divider"></div>

        <div class="verify-box">
          <div class="vb-title">校验结果</div>

          <div v-if="!verifyRes" class="vb-empty">尚未校验。点击右上角“校验链”。</div>

          <div v-else class="vb-content" :class="{ bad: verifyRes.ok === false }">
            <div class="vb-row">
              <span class="k">批次：</span>
              <span class="v">#{{ verifyRes.batchId }}（记录数 {{ verifyRes.size }}）</span>
            </div>

            <div class="vb-row">
              <span class="k">结论：</span>
              <span class="v">
                <b v-if="verifyRes.ok">✅ {{ verifyRes.msg }}</b>
                <b v-else>❌ 校验失败：{{ verifyRes.reason }}</b>
              </span>
            </div>

            <div class="vb-row" v-if="verifyRes.ok === false">
              <span class="k">异常位置：</span>
              <span class="v">badIndex = {{ verifyRes.badIndex }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右：链时间线 -->
      <div class="card">
        <div class="card-title">链时间线（按 height 升序）</div>
        <div class="card-sub">默认展示业务内容；hash 细节可展开，方便监管核验与答辩说明。</div>

        <div v-if="!selectedBatchId" class="empty">
          请先在左侧选择一个批次。
        </div>

        <div v-else-if="loadingChain" class="empty">
          时间线加载中…
        </div>

        <div v-else-if="chain.length === 0" class="empty">
          该批次暂无上链记录。
        </div>

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
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'

const batches = ref([])
const selectedBatchId = ref('')
const chain = ref([])
const verifyRes = ref(null)

const loadingBatches = ref(false)
const loadingChain = ref(false)
const loadingVerify = ref(false)

const fmt = (t) => (t ? String(t).replace('T', ' ') : '-')

const curBatch = computed(() => {
  const id = Number(selectedBatchId.value)
  return batches.value.find(b => Number(b.id) === id) || null
})

const refreshBatches = async () => {
  loadingBatches.value = true
  try {
    // 监管端：拉所有批次
    const res = await request.get('/batch/all')
    batches.value = res.data || []
  } finally {
    loadingBatches.value = false
  }
}

const loadChain = async () => {
  verifyRes.value = null
  chain.value = []
  const id = selectedBatchId.value
  if (!id) return

  loadingChain.value = true
  try {
    const res = await request.get('/trace/chain', { params: { batchId: id } })
    chain.value = res.data || []
  } finally {
    loadingChain.value = false
  }
}

const verifyNow = async () => {
  const id = selectedBatchId.value
  if (!id) return
  loadingVerify.value = true
  try {
    const res = await request.get('/trace/verify', { params: { batchId: id } })
    verifyRes.value = res.data
  } finally {
    loadingVerify.value = false
  }
}

onMounted(async () => {
  await refreshBatches()
})
</script>

<style scoped>
.wrap { display:flex; flex-direction:column; gap: 12px; }

/* 顶部条：沿用你 Home 的风格 */
.hero{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap: 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px 16px;
}
.badge{
  display:inline-block;
  font-size: 12px;
  font-weight: 900;
  color:#1d4ed8;
  background: rgba(37,99,235,0.10);
  border: 1px solid rgba(37,99,235,0.18);
  padding: 6px 10px;
  border-radius: 999px;
  margin-bottom: 8px;
}
.h2{ margin:0 0 6px; font-weight:900; color:#0f172a; }
.p{ margin:0; color:#64748b; font-size:13px; line-height:1.6; }

.hero-right{ display:flex; gap: 10px; flex-wrap:wrap; }

.grid{
  display:grid;
  grid-template-columns: 380px 1fr;
  gap: 12px;
}
.card{
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.08);
}
.card-title{ font-weight:900; color:#0f172a; }
.card-sub{ color:#64748b; font-size:13px; margin-top:6px; line-height:1.6; }

.field{ margin-top: 12px; }
.label{ font-size:12px; color:#475569; margin-bottom: 6px; }
.select{
  width:100%;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(15,23,42,0.12);
  background:#fff;
  outline:none;
}
.select:focus{
  border-color: rgba(37,99,235,0.55);
  box-shadow: 0 0 0 4px rgba(37,99,235,0.12);
}

.mini-info{
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255,255,255,0.9);
  border: 1px solid rgba(15,23,42,0.08);
  color:#334155;
  font-size: 12px;
  line-height:1.8;
}
.divider{
  margin: 12px 0;
  border-top: 1px dashed rgba(148, 163, 184, 0.45);
}

.verify-box{
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(15,23,42,0.08);
  background: rgba(255,255,255,0.9);
}
.vb-title{ font-weight:900; color:#0f172a; margin-bottom: 8px; }
.vb-empty{ color:#64748b; font-size: 12px; }
.vb-content{ font-size: 12px; color:#334155; }
.vb-content.bad{ color:#b91c1c; }
.vb-row{ display:flex; gap: 8px; margin-top: 6px; }
.k{ width: 70px; color:#64748b; }
.v{ flex:1; }

.empty{
  margin-top: 14px;
  color:#64748b;
  font-size: 13px;
  padding: 10px 2px;
}

/* 时间线 */
.timeline{ margin-top: 12px; display:flex; flex-direction:column; gap: 12px; }
.node{ display:flex; gap: 12px; align-items:flex-start; }
.dot{
  width: 12px; height: 12px;
  border-radius: 999px;
  background: #2563eb;
  margin-top: 14px;
  box-shadow: 0 10px 18px rgba(37,99,235,0.25);
}
.box{
  flex:1;
  background: rgba(255,255,255,0.92);
  border: 1px solid rgba(15,23,42,0.08);
  border-radius: 14px;
  padding: 12px;
}
.top{ display:flex; align-items:center; justify-content:space-between; gap: 10px; }
.tag{
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 900;
  color:#1d4ed8;
  background: rgba(37,99,235,0.10);
  border: 1px solid rgba(37,99,235,0.15);
}
.h{ font-weight: 900; color:#0f172a; }
.content{ margin-top: 8px; color:#0f172a; line-height: 1.6; }
.meta{ margin-top: 8px; display:flex; gap: 12px; flex-wrap:wrap; font-size: 12px; color:#64748b; }
.detail{ margin-top: 10px; font-size: 12px; color:#334155; }
.kv{ margin-top: 6px; word-break: break-all; }

/* 按钮：沿用 Home 的按钮风格 */
.btn{
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: #fff;
  cursor: pointer;
  color: #0f172a;
}
.btn.ghost{ background: transparent; }
.btn:disabled{ opacity: .6; cursor: not-allowed; }

@media (max-width: 980px){
  .grid{ grid-template-columns: 1fr; }
}
</style>
