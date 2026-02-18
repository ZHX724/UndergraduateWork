<template>
  <div class="records">
    <div class="head">
      <div class="left">
        <div class="logo">BC</div>
        <div class="txt">
          <div class="title">溯源上链（区块链）</div>
          <div class="sub">
            追加式写入：每次提交都会生成 data_hash / block_hash / nonce / height，并可一键校验链完整性。
          </div>
        </div>
      </div>

      <div class="right">
        <button class="btn ghost" type="button" :disabled="loadingChain" @click="loadChain">
          {{ loadingChain ? '刷新中…' : '刷新时间线' }}
        </button>
        <button class="btn ghost" type="button" :disabled="verifying" @click="verifyChain">
          {{ verifying ? '校验中…' : '校验链' }}
        </button>
      </div>
    </div>

    <div v-if="msg" class="toast" :class="msgType">{{ msg }}</div>

    <div class="body">
      <!-- 左：上链表单 -->
      <section class="card">
        <div class="card-title">新增链上记录</div>

        <div class="form">
          <div class="field">
            <div class="label">批次 ID（batchId）</div>
            <input class="input" v-model="form.batchId" placeholder="例如：1（建议后续由批次管理页自动带入）" />
            <div class="hint">
              说明：链按 batchId 分组。现在先手动填，后续做“批次管理”后可改成下拉选择。
            </div>
          </div>

          <div class="field">
            <div class="label">阶段类型（type）</div>
            <select class="input" v-model="form.type">
              <option value="PRODUCTION">PRODUCTION（生产）</option>
              <option value="PROCESSING">PROCESSING（加工）</option>
              <option value="TRANSPORT">TRANSPORT（运输）</option>
              <option value="DISTRIBUTION">DISTRIBUTION（流通）</option>
              <option value="CORRECTION">CORRECTION（补充/更正）</option>
              <option value="RISK">RISK（异常/风险）</option>
            </select>
          </div>

          <div class="field">
            <div class="label">记录内容（content）</div>
            <textarea class="textarea" v-model="form.content" placeholder="例如：2026-02-18 播种；基地A；负责人张三；温湿度xx…"></textarea>
          </div>

          <div class="actions">
            <button class="btn primary" type="button" :disabled="adding" @click="addRecord">
              {{ adding ? '上链中…' : '上链（追加写入）' }}
            </button>
            <button class="btn ghost" type="button" :disabled="adding" @click="resetForm">
              清空
            </button>
          </div>

          <div class="verifyBox" v-if="verifyResult">
            <div class="verifyTitle">校验结果</div>
            <div class="verifyText" :class="verifyResult.ok ? 'ok' : 'err'">
              {{ verifyResult.message }}
            </div>
          </div>
        </div>
      </section>

      <!-- 右：链时间线 -->
      <section class="card">
        <div class="card-title">链上时间线（按 height 升序）</div>

        <div v-if="loadingChain" class="empty">加载中…</div>
        <div v-else-if="chain.length === 0" class="empty">
          暂无记录。请先填写 batchId 并上链一条记录。
        </div>

        <div v-else class="timeline">
          <div v-for="(r, idx) in chain" :key="r.id || idx" class="block">
            <div class="blockTop">
              <div class="h">#{{ r.height ?? '-' }}</div>
              <div class="tag">{{ r.type }}</div>
              <div class="time">{{ formatTime(r.createdAt || r.created_at) }}</div>
            </div>

            <div class="content">{{ r.content }}</div>

            <div class="hashes">
              <div class="row">
                <span class="k">prev_hash</span>
                <span class="v mono">{{ shortHash(r.prevHash || r.prev_hash) }}</span>
              </div>
              <div class="row">
                <span class="k">data_hash</span>
                <span class="v mono">{{ shortHash(r.dataHash || r.data_hash) }}</span>
              </div>
              <div class="row">
                <span class="k">block_hash</span>
                <span class="v mono">{{ shortHash(r.blockHash || r.block_hash) }}</span>
              </div>
              <div class="row">
                <span class="k">nonce</span>
                <span class="v mono">{{ r.nonce ?? '-' }}</span>
              </div>
            </div>

            <div class="sep" v-if="idx !== chain.length - 1"></div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'

const form = reactive({
  batchId: '',
  type: 'PRODUCTION',
  content: ''
})

const msg = ref('')
const msgType = ref('') // ok/err
const adding = ref(false)
const loadingChain = ref(false)
const verifying = ref(false)
const chain = ref([])
const verifyResult = ref(null)

function toast(text, type = 'ok') {
  msg.value = text
  msgType.value = type
  window.clearTimeout(toast._t)
  toast._t = window.setTimeout(() => {
    msg.value = ''
    msgType.value = ''
  }, 3000)
}

const resetForm = () => {
  form.type = 'PRODUCTION'
  form.content = ''
}

const formatTime = (t) => {
  if (!t) return '-'
  return String(t).replace('T', ' ')
}

const shortHash = (h) => {
  if (!h) return '-'
  const s = String(h)
  if (s.length <= 18) return s
  return s.slice(0, 10) + '…' + s.slice(-8)
}

const loadChain = async () => {
  verifyResult.value = null
  const batchId = String(form.batchId || '').trim()
  if (!batchId) {
    toast('请先填写 batchId', 'err')
    return
  }

  loadingChain.value = true
  try {
    const res = await request.get('/trace/chain', { params: { batchId } })
    chain.value = res.data || []
  } catch (e) {
    toast(String(e?.response?.data || '获取链失败'), 'err')
  } finally {
    loadingChain.value = false
  }
}

const addRecord = async () => {
  verifyResult.value = null
  const batchId = String(form.batchId || '').trim()
  const type = String(form.type || '').trim().toUpperCase()
  const content = String(form.content || '').trim()

  if (!batchId) return toast('batchId 不能为空', 'err')
  if (!type) return toast('type 不能为空', 'err')
  if (!content) return toast('content 不能为空', 'err')

  adding.value = true
  try {
    const res = await request.post(
        '/trace/add',
        new URLSearchParams({ batchId, type, content }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )
    toast(String(res.data || '上链成功'), 'ok')
    form.content = ''
    await loadChain()
  } catch (e) {
    toast(String(e?.response?.data || '上链失败'), 'err')
  } finally {
    adding.value = false
  }
}

const verifyChain = async () => {
  verifyResult.value = null
  const batchId = String(form.batchId || '').trim()
  if (!batchId) {
    toast('请先填写 batchId', 'err')
    return
  }

  verifying.value = true
  try {
    const res = await request.get('/trace/verify', { params: { batchId } })
    // 兼容：后端可能返回 String / Map / Boolean
    const data = res.data
    if (typeof data === 'string') {
      verifyResult.value = { ok: !data.includes('篡改') && !data.includes('失败'), message: data }
    } else if (typeof data === 'boolean') {
      verifyResult.value = { ok: data, message: data ? '链完整（verify=true）' : '链疑似被篡改（verify=false）' }
    } else if (data && typeof data === 'object') {
      // 如果你后端后面改成 {ok,message,badHeight} 也能直接吃
      const ok = data.ok ?? data.valid ?? true
      verifyResult.value = { ok, message: data.message || JSON.stringify(data) }
    } else {
      verifyResult.value = { ok: true, message: String(data) }
    }
  } catch (e) {
    verifyResult.value = { ok: false, message: String(e?.response?.data || '校验失败') }
  } finally {
    verifying.value = false
  }
}
</script>

<style scoped>
.records { display:flex; flex-direction:column; gap:12px; }

.head{
  display:flex; align-items:center; justify-content:space-between; gap:12px;
  background: rgba(255,255,255,0.82);
  border: 1px solid rgba(226,232,240,0.9);
  border-radius: 16px;
  padding: 14px 16px;
}
.left{ display:flex; align-items:center; gap:12px; }
.logo{
  width:44px; height:44px; border-radius:14px; display:grid; place-items:center;
  color:#fff; font-weight:900;
  background: linear-gradient(135deg,#0ea5e9,#22c55e);
  box-shadow: 0 10px 22px rgba(2,6,23,0.15);
}
.txt .title{ font-weight:900; color:#0f172a; font-size:16px; }
.txt .sub{ font-size:12px; color:#64748b; margin-top:2px; max-width: 560px; }

.right{ display:flex; gap:10px; }

.toast{
  padding:10px 12px; border-radius:14px;
  border:1px solid rgba(226,232,240,0.9);
  background: rgba(255,255,255,0.86);
  font-size:13px;
}
.toast.ok{ color:#16a34a; border-color: rgba(22,163,74,0.25); }
.toast.err{ color:#ef4444; border-color: rgba(239,68,68,0.25); }

.body{
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap:12px;
}

.card{
  background: rgba(255,255,255,0.86);
  border: 1px solid rgba(226,232,240,0.9);
  border-radius:16px;
  padding:14px;
  box-shadow: 0 18px 40px rgba(2,6,23,0.08);
  min-height: 260px;
}
.card-title{ font-weight:900; color:#0f172a; margin-bottom:10px; }

.form{ display:flex; flex-direction:column; gap:10px; }
.field .label{ font-size:12px; color:#475569; margin-bottom:6px; }
.input, .textarea{
  width:100%;
  border:1px solid rgba(15,23,42,0.12);
  border-radius:12px;
  padding:10px 12px;
  outline:none;
  font-size:14px;
  background:#fff;
}
.textarea{ min-height:120px; resize:vertical; }

.actions{ display:flex; gap:10px; margin-top:4px; }

.btn{
  padding:10px 14px;
  border-radius:12px;
  cursor:pointer;
  border:1px solid rgba(226,232,240,0.9);
  background:#fff;
  color:#0f172a;
  font-weight:800;
}
.btn.primary{
  border:none;
  background: linear-gradient(135deg,#2563eb,#1d4ed8);
  color:#fff;
  box-shadow: 0 10px 18px rgba(37,99,235,0.18);
}
.btn.ghost{ background: rgba(255,255,255,0.7); }
.btn:disabled{ opacity:0.6; cursor:not-allowed; }

.hint{ font-size:12px; color:#64748b; line-height:1.6; margin-top:6px; }

.verifyBox{
  margin-top: 6px;
  border: 1px dashed rgba(15,23,42,0.18);
  border-radius: 14px;
  padding: 10px 12px;
  background: rgba(255,255,255,0.7);
}
.verifyTitle{ font-weight:900; color:#0f172a; font-size:13px; }
.verifyText{ margin-top:6px; font-size:13px; }
.verifyText.ok{ color:#16a34a; }
.verifyText.err{ color:#ef4444; }

.empty{ color:#64748b; font-size:13px; padding:10px 2px; }

.timeline{ display:flex; flex-direction:column; gap: 10px; }
.block{
  border:1px solid rgba(15,23,42,0.08);
  border-radius:14px;
  padding:12px;
  background: rgba(255,255,255,0.9);
}
.blockTop{
  display:flex; align-items:center; gap:8px;
}
.h{
  font-weight: 900; color:#0f172a;
  background: rgba(2,132,199,0.08);
  border: 1px solid rgba(2,132,199,0.18);
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
}
.tag{
  font-size: 12px;
  color:#0f172a;
  background: rgba(34,197,94,0.10);
  border: 1px solid rgba(34,197,94,0.22);
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 800;
}
.time{ margin-left:auto; font-size:12px; color:#94a3b8; }

.content{
  margin-top: 8px;
  font-size: 13px;
  color:#334155;
  line-height: 1.6;
  white-space: pre-wrap;
}

.hashes{
  margin-top: 10px;
  display:flex;
  flex-direction:column;
  gap: 6px;
}
.row{ display:flex; gap: 10px; align-items: baseline; }
.k{ width: 84px; font-size:12px; color:#64748b; }
.v{ flex: 1; font-size: 12px; color:#0f172a; }
.mono{ font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace; }

.sep{
  margin-top: 10px;
  border-top: 1px dashed rgba(15,23,42,0.10);
}

@media (max-width: 980px){
  .body{ grid-template-columns: 1fr; }
}
</style>
