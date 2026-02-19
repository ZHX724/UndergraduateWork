<template>
  <div class="wrap">
    <section class="hero">
      <div class="hero-left">
        <div class="badge">监管端 · 审计</div>
        <h2 class="h2">批次抽查 / 链校验 / 风险标记</h2>
        <p class="p">
          监管端只做“查询、校验、标记”，不参与写链；链数据只增不删。
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
      <!-- 左：批次列表 + 搜索 -->
      <div class="card">
        <div class="card-title">批次列表</div>
        <div class="card-sub">可按批次码 / 产品ID 搜索，点选后右侧展示时间线。</div>

        <div class="field">
          <div class="label">搜索</div>
          <input class="input" v-model="keyword" placeholder="输入 batchCode 或 productId" />
        </div>

        <div class="list" v-if="filteredBatches.length">
          <button
              v-for="b in filteredBatches"
              :key="b.id"
              class="row"
              :class="{ active: Number(selectedBatchId) === Number(b.id) }"
              type="button"
              @click="selectBatch(b.id)"
          >
            <div class="row-main">
              <div class="row-top">
                <div class="id">#{{ b.id }}</div>
                <div class="code">{{ b.batchCode }}</div>
                <span v-if="Number(b.riskFlag ?? b.risk_flag) === 1" class="risk">风险</span>
              </div>
              <div class="row-sub">产品ID：{{ b.productId }}</div>
            </div>
          </button>
        </div>

        <div v-else class="empty">暂无批次数据</div>

        <div class="divider"></div>

        <!-- 风险标记 -->
        <div class="riskbox" v-if="curBatch">
          <div class="rb-title">风险标记</div>

          <div class="rb-row">
            <span class="k">当前状态：</span>
            <span class="v">
              <b v-if="isRisk">⚠ 风险</b>
              <b v-else>✅ 正常</b>
            </span>
          </div>

          <div class="field" style="margin-top:10px;">
            <div class="label">风险备注（可选）</div>
            <input class="input" v-model="riskNote" placeholder="例如：校验失败 / 信息不一致 / 投诉线索…" />
          </div>

          <div class="rb-actions">
            <button class="btn" type="button" @click="setRisk(1)" :disabled="loadingRisk || isRisk">
              标记风险
            </button>
            <button class="btn ghost" type="button" @click="setRisk(0)" :disabled="loadingRisk || !isRisk">
              取消风险
            </button>
          </div>

          <div class="rb-tip" v-if="verifyRes && verifyRes.ok === false">
            ❗当前批次链校验失败，建议标记风险并进一步核查。
          </div>
        </div>
      </div>

      <!-- 右：时间线 + 校验结果 -->
      <div class="card">
        <div class="card-title">链时间线 + 校验结果</div>
        <div class="card-sub">按 height 升序展示；校验失败会定位 badIndex 与原因。</div>

        <div v-if="!selectedBatchId" class="empty">请先在左侧选择一个批次。</div>
        <div v-else-if="loadingChain" class="empty">时间线加载中…</div>
        <div v-else-if="chain.length === 0" class="empty">该批次暂无上链记录。</div>

        <!-- 校验结果条 -->
        <div v-if="verifyRes" class="verify" :class="{ bad: verifyRes.ok === false }">
          <div class="v-title">
            <b>{{ verifyRes.ok ? '✅ 校验通过' : '❌ 校验失败' }}</b>
            <span class="v-sub">记录数：{{ verifyRes.size }}</span>
          </div>
          <div class="v-msg">{{ verifyRes.msg || verifyRes.reason }}</div>
          <div v-if="verifyRes.ok === false" class="v-msg">badIndex：{{ verifyRes.badIndex }}</div>
        </div>

        <!-- 时间线 -->
        <div v-if="chain.length" class="timeline">
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

const keyword = ref('')
const riskNote = ref('')

const loadingBatches = ref(false)
const loadingChain = ref(false)
const loadingVerify = ref(false)
const loadingRisk = ref(false)

const fmt = (t) => (t ? String(t).replace('T', ' ') : '-')

const refreshBatches = async () => {
  loadingBatches.value = true
  try {
    const res = await request.get('/batch/all')
    batches.value = res.data || []
  } finally {
    loadingBatches.value = false
  }
}

const filteredBatches = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return batches.value
  return batches.value.filter(b => {
    const code = String(b.batchCode ?? '').toLowerCase()
    const pid = String(b.productId ?? '')
    return code.includes(k) || pid.includes(k)
  })
})

const curBatch = computed(() => {
  const id = Number(selectedBatchId.value)
  return batches.value.find(b => Number(b.id) === id) || null
})

const isRisk = computed(() => {
  if (!curBatch.value) return false
  return Number(curBatch.value.riskFlag ?? curBatch.value.risk_flag ?? 0) === 1
})

const selectBatch = async (id) => {
  selectedBatchId.value = String(id)
  verifyRes.value = null
  riskNote.value = (curBatch.value?.riskNote ?? curBatch.value?.risk_note ?? '') || ''
  await loadChain()
}

const loadChain = async () => {
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

const setRisk = async (flag) => {
  if (!curBatch.value) return
  loadingRisk.value = true
  try {
    await request.post(
        '/batch/risk',
        new URLSearchParams({
          id: String(curBatch.value.id),
          riskFlag: String(flag),
          riskNote: riskNote.value || '',
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )
    await refreshBatches()
  } finally {
    loadingRisk.value = false
  }
}

onMounted(async () => {
  await refreshBatches()
})
</script>

<style scoped>
.wrap { display:flex; flex-direction:column; gap: 12px; }
.hero{
  display:flex; align-items:center; justify-content:space-between; gap: 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px 16px;
}
.badge{
  display:inline-block; font-size: 12px; font-weight: 900;
  color:#1d4ed8; background: rgba(37,99,235,0.10);
  border: 1px solid rgba(37,99,235,0.18);
  padding: 6px 10px; border-radius: 999px; margin-bottom: 8px;
}
.h2{ margin:0 0 6px; font-weight:900; color:#0f172a; }
.p{ margin:0; color:#64748b; font-size:13px; line-height:1.6; }
.hero-right{ display:flex; gap: 10px; flex-wrap:wrap; }

.grid{ display:grid; grid-template-columns: 360px 1fr; gap: 12px; }
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
.input{
  width:100%;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(15,23,42,0.12);
  background:#fff;
  outline:none;
}
.input:focus{
  border-color: rgba(37,99,235,0.55);
  box-shadow: 0 0 0 4px rgba(37,99,235,0.12);
}

.list{ margin-top: 12px; display:flex; flex-direction:column; gap: 8px; max-height: 360px; overflow:auto; }
.row{
  width:100%;
  text-align:left;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(226,232,240,0.9);
  background:#fff;
  cursor:pointer;
  transition: box-shadow .15s ease, border-color .15s ease;
}
.row.active{
  border-color: rgba(37,99,235,0.55);
  box-shadow: 0 0 0 4px rgba(37,99,235,0.12);
}
.row-top{ display:flex; align-items:center; gap: 8px; }
.id{ font-weight:900; color:#0f172a; }
.code{ color:#334155; font-weight:700; }
.risk{
  margin-left:auto;
  font-size:12px;
  font-weight:900;
  color:#b91c1c;
  background: rgba(185,28,28,0.10);
  border: 1px solid rgba(185,28,28,0.18);
  padding: 4px 8px;
  border-radius: 999px;
}
.row-sub{ margin-top: 6px; font-size:12px; color:#64748b; }

.divider{ margin: 12px 0; border-top: 1px dashed rgba(148, 163, 184, 0.45); }

.riskbox{
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(15,23,42,0.08);
  background: rgba(255,255,255,0.9);
}
.rb-title{ font-weight:900; color:#0f172a; margin-bottom: 8px; }
.rb-row{ display:flex; gap: 8px; font-size: 12px; color:#334155; }
.k{ width: 70px; color:#64748b; }
.v{ flex:1; }
.rb-actions{ margin-top: 10px; display:flex; gap: 8px; }
.rb-tip{ margin-top: 10px; font-size: 12px; color:#b91c1c; }

.empty{ margin-top: 14px; color:#64748b; font-size: 13px; padding: 10px 2px; }

/* 校验结果条 */
.verify{
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(34,197,94,0.25);
  background: rgba(34,197,94,0.08);
  color:#14532d;
}
.verify.bad{
  border-color: rgba(185,28,28,0.25);
  background: rgba(185,28,28,0.08);
  color:#7f1d1d;
}
.v-title{ display:flex; justify-content:space-between; gap:10px; align-items:center; }
.v-sub{ font-size:12px; opacity:.85; }
.v-msg{ margin-top: 6px; font-size:12px; line-height:1.6; }

/* 时间线 */
.timeline{ margin-top: 12px; display:flex; flex-direction:column; gap: 12px; }
.node{ display:flex; gap: 12px; align-items:flex-start; }
.dot{
  width: 12px; height: 12px; border-radius: 999px;
  background: #2563eb; margin-top: 14px;
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

/* 按钮 */
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
