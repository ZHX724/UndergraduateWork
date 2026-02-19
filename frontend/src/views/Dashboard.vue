<template>
  <div class="wrap">
    <section class="hero">
      <div>
        <div class="h1">仪表盘</div>
        <div class="sub">快速查看批次规模、风险概况、最近新增。</div>
      </div>
      <button class="btn" @click="load" :disabled="loading">{{ loading ? '刷新中…' : '刷新' }}</button>
    </section>

    <section class="kpis">
      <div class="kpi">
        <div class="kpi-num">{{ total }}</div>
        <div class="kpi-lab">批次数</div>
      </div>
      <div class="kpi">
        <div class="kpi-num">{{ riskCount }}</div>
        <div class="kpi-lab">风险批次</div>
      </div>
      <div class="kpi">
        <div class="kpi-num">{{ safeCount }}</div>
        <div class="kpi-lab">正常批次</div>
      </div>
    </section>

    <section class="grid">
      <div class="card">
        <div class="card-title">最近批次</div>
        <div class="card-sub">按 id 倒序（近 10 条）</div>

        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="list.length === 0" class="empty">暂无数据</div>

        <div v-else class="table">
          <div class="row head">
            <div>#</div>
            <div>batchCode</div>
            <div>productId</div>
            <div>风险</div>
            <div>备注</div>
          </div>

          <div class="row" v-for="b in list" :key="b.id">
            <div>{{ b.id }}</div>
            <div class="mono">{{ b.batchCode || b.batch_code }}</div>
            <div>{{ b.productId || b.product_id || '-' }}</div>
            <div>
              <span class="badge" :class="(b.riskFlag ?? b.risk_flag) === 1 ? 'bad' : 'ok'">
                {{ (b.riskFlag ?? b.risk_flag) === 1 ? '风险' : '正常' }}
              </span>
            </div>
            <div class="muted">{{ b.riskNote || b.risk_note || '-' }}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-title">说明（答辩友好）</div>
        <div class="card-sub">把“监管提示”作为对外展示口径</div>
        <ul class="ul">
          <li>消费者页面：只展示监管结论/备注，不暴露链校验细节。</li>
          <li>管理端：可做链校验 + 风险标记，用于内部追溯。</li>
          <li>链数据不可删除：即使标记风险也只是“监管结论”，不破坏历史记录。</li>
        </ul>
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
const safeCount = computed(() => total.value - riskCount.value)

const list = computed(() => all.value.slice(0, 10))

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

.kpis{
  display:grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.kpi{
  padding: 12px;
  border-radius: 16px;
  border: 1px solid rgba(226,232,240,0.9);
  background: rgba(255,255,255,0.86);
}
.kpi-num{ font-size: 22px; font-weight: 900; color:#0f172a; }
.kpi-lab{ margin-top: 4px; font-size: 12px; color:#64748b; }

.grid{
  display:grid;
  grid-template-columns: 2fr 1fr;
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

.table{ margin-top: 10px; }
.row{
  display:grid;
  grid-template-columns: 70px 1.6fr 0.8fr 0.6fr 2fr;
  gap: 10px;
  padding: 10px 10px;
  border-top: 1px dashed rgba(148,163,184,0.35);
  align-items:center;
}
.row.head{
  border-top: none;
  padding-top: 6px;
  color:#64748b;
  font-size: 12px;
}
.mono{ font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace; }
.muted{ color:#64748b; font-size: 12px; }
.empty{ margin-top: 10px; color:#64748b; font-size: 13px; }

.badge{
  display:inline-flex; align-items:center; justify-content:center;
  padding: 6px 10px; border-radius: 999px;
  font-size: 12px; font-weight: 900;
  border: 1px solid rgba(15,23,42,0.08);
}
.badge.ok{ color:#0f766e; background: rgba(20,184,166,0.12); border-color: rgba(20,184,166,0.18); }
.badge.bad{ color:#b91c1c; background: rgba(239,68,68,0.12); border-color: rgba(239,68,68,0.18); }

.ul{ margin: 10px 0 0; padding-left: 18px; color:#334155; font-size: 13px; line-height: 1.8; }

@media (max-width: 980px){
  .grid{ grid-template-columns: 1fr; }
  .row{ grid-template-columns: 60px 1.6fr 0.8fr 0.6fr 1.6fr; }
}
</style>
