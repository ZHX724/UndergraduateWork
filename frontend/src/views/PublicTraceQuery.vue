<template>
  <div class="page">
    <!-- ✅ 统一顶栏（替代你左边那块扁卡片） -->
    <header class="topbar">
      <div class="brand" @click="goHome" title="回到查询页顶部">
        <div class="logo">FT</div>
        <div class="txt">
          <div class="t1">农事溯源查询（消费者）</div>
          <div class="t2">无需登录 · 输入批次号（batchCode）查看溯源时间线</div>
        </div>
      </div>

      <div class="top-actions">
        <button class="btn ghost" type="button" @click="goLogin">管理端登录</button>
      </div>
    </header>

    <main class="main">
      <section class="panel">
        <div class="panel-head">
          <div>
            <div class="panel-title">消费者查询</div>
            <div class="panel-sub">建议用批次码，例如：20260218-A01</div>
          </div>

          <div class="panel-actions">
            <button class="btn ghost" type="button" @click="clear" :disabled="loading && !code">
              清空
            </button>
          </div>
        </div>

        <div class="form">
          <div class="field">
            <div class="label">批次号（batchCode）</div>
            <input
                class="input"
                v-model="code"
                placeholder="请输入批次号，例如：20260218-A01"
                autocomplete="off"
                @keyup.enter="query"
            />
          </div>

          <button class="btn primary" type="button" :disabled="loading || !code.trim()" @click="query">
            {{ loading ? '查询中…' : '查询' }}
          </button>
        </div>

        <div class="hint">
          说明：默认只展示业务内容；区块 hash 细节可展开查看（便于答辩解释“链”）。
        </div>

        <div class="result">
          <div v-if="loading" class="empty">加载中…</div>
          <div v-else-if="chain.length === 0" class="empty">
            暂无数据。请输入批次号并点击查询。
          </div>

          <div v-else class="timeline">
            <div v-for="r in chain" :key="r.id" class="node">
              <div class="dot"></div>
              <div class="box">
                <div class="box-top">
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
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const code = ref('')
const chain = ref([])
const loading = ref(false)

const fmt = (t) => (t ? String(t).replace('T', ' ') : '-')

const query = async () => {
  const c = code.value.trim()
  if (!c) return
  loading.value = true
  try {
    const res = await request.get('/trace/chainByCode', { params: { batchCode: c } })
    chain.value = res.data || []
  } catch (e) {
    chain.value = []
  } finally {
    loading.value = false
  }
}

const clear = () => {
  code.value = ''
  chain.value = []
}

const goLogin = () => router.push('/login')
const goHome = () => window.scrollTo({ top: 0, behavior: 'smooth' })
</script>

<style scoped>
.page{
  min-height: 100vh;
  background:
      radial-gradient(1200px 600px at 10% 10%, rgba(255, 255, 255, 0.35), transparent 60%),
      radial-gradient(900px 500px at 90% 30%, rgba(255, 255, 255, 0.25), transparent 55%),
      linear-gradient(135deg, #0ea5e9, #6366f1);
  padding: 16px;
}

/* 顶栏（统一风格） */
.topbar{
  max-width: 1100px;
  margin: 0 auto 14px;
  height: 64px;
  display:flex;
  align-items:center;
  justify-content:space-between;
  padding: 0 16px;
  background: rgba(255,255,255,0.78);
  border: 1px solid rgba(226,232,240,0.9);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.brand{
  display:flex;
  align-items:center;
  gap: 12px;
  user-select:none;
  cursor: pointer;
}
.logo{
  width: 40px; height: 40px;
  border-radius: 12px;
  display:grid;
  place-items:center;
  color:#fff;
  font-weight:900;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.15);
}
.t1{ font-weight: 900; color:#0f172a; }
.t2{ margin-top: 2px; font-size: 12px; color:#64748b; }
.top-actions{ display:flex; gap: 10px; }

/* 主体 */
.main{
  max-width: 1100px;
  margin: 0 auto;
  display:flex;
  justify-content:center;
}

.panel{
  width: 100%;
  max-width: 920px;
  background: rgba(255,255,255,0.86);
  border: 1px solid rgba(226,232,240,0.9);
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.10);
}

.panel-head{
  display:flex;
  align-items:flex-start;
  justify-content:space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(148,163,184,0.45);
  margin-bottom: 12px;
}
.panel-title{ font-weight: 900; color:#0f172a; font-size: 18px; }
.panel-sub{ margin-top: 4px; font-size: 12px; color:#64748b; }

.form{
  display:grid;
  grid-template-columns: 1fr 160px;
  gap: 12px;
  align-items:end;
}

.field .label{
  font-size: 12px;
  color:#475569;
  margin-bottom: 6px;
}
.input{
  width: 100%;
  border: 1px solid rgba(15,23,42,0.12);
  border-radius: 12px;
  padding: 12px 12px;
  outline: none;
  font-size: 14px;
  background: #fff;
}
.input:focus{
  border-color: rgba(37,99,235,0.55);
  box-shadow: 0 0 0 4px rgba(37,99,235,0.12);
}

.hint{
  margin-top: 10px;
  font-size: 12px;
  color:#94a3b8;
  line-height: 1.6;
}

/* 结果区 */
.result{
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed rgba(148,163,184,0.45);
}
.empty{
  color:#64748b;
  font-size: 13px;
  padding: 8px 2px;
}

.timeline{
  display:flex;
  flex-direction:column;
  gap: 12px;
}
.node{
  display:flex;
  gap: 12px;
  align-items:flex-start;
}
.dot{
  width: 12px; height: 12px;
  border-radius: 999px;
  background: #2563eb;
  margin-top: 14px;
  box-shadow: 0 10px 18px rgba(37,99,235,0.25);
}
.box{
  flex: 1;
  background: rgba(255,255,255,0.92);
  border: 1px solid rgba(15,23,42,0.08);
  border-radius: 14px;
  padding: 12px;
}
.box-top{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap: 10px;
}
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
.meta{
  margin-top: 8px;
  display:flex;
  gap: 12px;
  flex-wrap:wrap;
  font-size: 12px;
  color:#64748b;
}
.detail{
  margin-top: 10px;
  font-size: 12px;
  color:#334155;
}
.kv{ margin-top: 6px; word-break: break-all; }

/* Buttons */
.btn{
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(226,232,240,0.9);
  background: #fff;
  cursor: pointer;
  color: #0f172a;
}
.btn.ghost{ background: transparent; }
.btn.primary{
  border: none;
  color: #fff;
  font-weight: 900;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}
.btn:disabled{ opacity: 0.6; cursor: not-allowed; }

@media (max-width: 720px){
  .form{ grid-template-columns: 1fr; }
  .topbar{ height: auto; padding: 12px; }
  .t2{ display:none; }
}
</style>
