<template>
  <div class="wrap">
    <section class="hero">
      <div>
        <div class="h1">异常 / 风险记录</div>
        <div class="sub">这里展示监管端标记为风险的批次。</div>
      </div>
      <div class="actions">
        <button class="btn" @click="load" :disabled="loading">{{ loading ? '刷新中…' : '刷新' }}</button>
      </div>
    </section>

    <section class="card">
      <div class="card-title">风险批次列表</div>

      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="rows.length === 0" class="empty">暂无风险批次</div>

      <div v-else class="table">
        <div class="row head">
          <div>#</div>
          <div>batchCode</div>
          <div>productId</div>
          <div>备注</div>
        </div>

        <div class="row" v-for="b in rows" :key="b.id">
          <div>{{ b.id }}</div>
          <div class="mono">{{ b.batchCode || b.batch_code }}</div>
          <div>{{ b.productId || b.product_id || '-' }}</div>
          <div class="note">{{ b.riskNote || b.risk_note || '-' }}</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const rows = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/batch/all')
    const all = Array.isArray(res.data) ? res.data : []
    rows.value = all.filter(b => (b.riskFlag ?? b.risk_flag) === 1)
  } catch (e) {
    rows.value = []
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
.actions{ display:flex; gap:10px; }

.btn{
  padding: 10px 12px; border-radius: 12px;
  border: 1px solid rgba(226,232,240,0.9);
  background: #fff; cursor:pointer;
}
.btn:disabled{ opacity:.6; cursor:not-allowed; }

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
  grid-template-columns: 70px 1.6fr 0.8fr 2.6fr;
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
.note{ color:#0f172a; }
.empty{ margin-top: 10px; color:#64748b; font-size: 13px; }
</style>
