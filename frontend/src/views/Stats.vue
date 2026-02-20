<template>
  <div class="wrap">
    <section class="hero">
      <div>
        <div class="h1">数据统计</div>
        <div class="sub">基于批次数据做快速统计。</div>
      </div>
      <button class="btn" @click="load" :disabled="loading">{{ loading ? '刷新中…' : '刷新' }}</button>
    </section>

    <section class="grid">
      <div class="card">
        <div class="card-title">风险占比</div>
        <div class="card-sub">全部批次</div>

        <div class="ring">
          <div class="ring-bar">
            <div class="fill" :style="{ width: pct + '%' }"></div>
          </div>
          <div class="ring-text">
            <div class="big">{{ pct }}%</div>
            <div class="muted">{{ riskCount }} / {{ total }}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-title">近 7 天新增批次</div>

        <div v-if="loading" class="empty">加载中…</div>
        <div v-else class="bars">
          <div class="bar" v-for="d in days" :key="d.day">
            <div class="col">
              <div class="h" :style="{ height: d.h + 'px' }"></div>
            </div>
            <div class="lab">{{ d.day }}</div>
            <div class="num">{{ d.c }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const all = ref([])

const total = computed(() => all.value.length)
const riskCount = computed(() => all.value.filter(b => (b.riskFlag ?? b.risk_flag) === 1).length)
const pct = computed(() => {
  if (!total.value) return 0
  return Math.round((riskCount.value / total.value) * 100)
})

const toDayStr = (dt) => {
  if (!dt) return null
  const s = String(dt)

  return s.slice(0, 10)
}

const days = computed(() => {
  const now = new Date()
  const map = new Map()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now.getTime() - i * 86400000)
    const key = d.toISOString().slice(0, 10)
    map.set(key, 0)
  }
  for (const b of all.value) {
    const day = toDayStr(b.createdAt || b.created_at)
    if (day && map.has(day)) map.set(day, map.get(day) + 1)
  }
  const arr = Array.from(map.entries()).map(([day, c]) => ({
    day: day.slice(5),
    c,
  }))
  const max = Math.max(1, ...arr.map(x => x.c))
  return arr.map(x => ({ ...x, h: Math.round((x.c / max) * 110) }))
})

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/batch/all')
    all.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    all.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.wrap { padding: 4px; }
.hero{
  display:flex; align-items:center; justify-content:space-between; gap:12px;
  padding: 14px 16px;
  background: rgba(255,255,255,0.82);
  border: 1px solid rgba(226,232,240,0.9);
  border-radius: 16px;
  margin-bottom: 12px;
}
.h1{ font-size: 22px; font-weight: 900; color:#0f172a; }
.sub{ margin-top: 4px; font-size: 12px; color:#64748b; }

.btn{
  padding: 10px 12px; border-radius: 12px;
  border: 1px solid rgba(226,232,240,0.9);
  background: #fff; cursor:pointer;
}
.btn:disabled{ opacity:.6; cursor:not-allowed; }

.grid{
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.card{
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(226,232,240,0.9);
  background: rgba(255,255,255,0.86);
}
.card-title{ font-weight: 900; color:#0f172a; }
.card-sub{ margin-top: 4px; font-size: 12px; color:#64748b; }

.ring{ margin-top: 12px; display:flex; gap: 12px; align-items:center; }
.ring-bar{
  flex: 1;
  height: 12px;
  border-radius: 999px;
  background: rgba(15,23,42,0.08);
  overflow:hidden;
}
.fill{
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}
.ring-text .big{ font-size: 22px; font-weight: 900; color:#0f172a; }
.muted{ color:#64748b; font-size: 12px; }

.bars{
  margin-top: 12px;
  display:flex;
  gap: 10px;
  align-items:flex-end;
}
.bar{ width: 42px; text-align:center; }
.col{
  height: 120px;
  display:flex;
  align-items:flex-end;
  justify-content:center;
  border-radius: 12px;
  background: rgba(15,23,42,0.06);
  padding: 6px;
}
.h{
  width: 100%;
  border-radius: 10px;
  background: linear-gradient(135deg, #22c55e, #2563eb);
}
.lab{ margin-top: 6px; font-size: 12px; color:#64748b; }
.num{ margin-top: 2px; font-size: 12px; color:#0f172a; font-weight: 900; }

.tip{ margin-top: 10px; font-size: 12px; color:#94a3b8; }
.empty{ margin-top: 10px; color:#64748b; font-size: 13px; }

@media (max-width: 980px){
  .grid{ grid-template-columns: 1fr; }
}
</style>
