<template>
  <div class="product">
    <!-- 顶部小标题条 -->
    <div class="head">
      <div class="left">
        <div class="txt">
          <div class="h1">农产品管理</div>
          <div class="sub">新增 / 查看 / 删除你的农产品。</div>
        </div>
      </div>

      <button class="btn ghost" type="button" :disabled="loadingList" @click="loadList">
        {{ loadingList ? '刷新中…' : '刷新列表' }}
      </button>
    </div>

    <!-- 提示条 -->
    <div v-if="msg" class="toast" :class="msgType">
      {{ msg }}
    </div>

    <div class="body">
      <!-- 新增 -->
      <section class="card">
        <div class="card-title">新增农产品</div>

        <div class="form">
          <div class="field">
            <div class="label">名称</div>
            <input class="input" v-model="form.name" placeholder="例如：大连红富士苹果" />
          </div>

          <div class="field">
            <div class="label">描述（可选）</div>
            <textarea class="textarea" v-model="form.description" placeholder="例如：产地、品种、种植方式等"></textarea>
          </div>

          <div class="actions">
            <button class="btn primary" type="button" :disabled="loading" @click="createProduct">
              {{ loading ? '提交中…' : '创建' }}
            </button>
            <button class="btn ghost" type="button" :disabled="loading" @click="resetForm">
              清空
            </button>
          </div>
        </div>
      </section>

      <!-- 列表 -->
      <section class="card">
        <div class="card-title">我的农产品</div>

        <div v-if="loadingList" class="empty">加载中…</div>
        <div v-else-if="list.length === 0" class="empty">暂无农产品，先在左侧创建一个吧。</div>

        <div v-else class="grid">
          <div v-for="p in list" :key="p.id" class="item">
            <div class="item-top">
              <div class="name">{{ p.name }}</div>
              <div class="id">#{{ p.id }}</div>
            </div>

            <div class="desc">{{ p.description || '（无描述）' }}</div>

            <div class="meta">
              <span>创建时间：{{ formatTime(p.createdAt || p.created_at) }}</span>
            </div>

            <div class="item-actions">
              <button class="btn danger" type="button" :disabled="deletingId === p.id" @click="del(p.id)">
                {{ deletingId === p.id ? '删除中…' : '删除' }}
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'

const form = reactive({
  name: '',
  description: ''
})

const msg = ref('')
const msgType = ref('') // ok / err
const loading = ref(false)

const list = ref([])
const loadingList = ref(false)
const deletingId = ref(null)

function toast(text, type = 'ok') {
  msg.value = text
  msgType.value = type
  // 3秒后自动清
  window.clearTimeout(toast._t)
  toast._t = window.setTimeout(() => {
    msg.value = ''
    msgType.value = ''
  }, 3000)
}

const formatTime = (t) => {
  if (!t) return '-'
  return String(t).replace('T', ' ')
}

const resetForm = () => {
  form.name = ''
  form.description = ''
}

const loadList = async () => {
  loadingList.value = true
  try {
    const res = await request.get('/product/my')
    list.value = res.data || []
  } catch (e) {
    const text = e?.response?.data || '获取列表失败（检查后端 / Session）'
    toast(String(text), 'err')
  } finally {
    loadingList.value = false
  }
}

const createProduct = async () => {
  if (!form.name.trim()) {
    toast('名称不能为空', 'err')
    return
  }

  loading.value = true
  try {
    const res = await request.post(
        '/product/create',
        new URLSearchParams({
          name: form.name.trim(),
          description: form.description || ''
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )

    toast(String(res.data || '创建成功'), 'ok')
    resetForm()
    await loadList()
  } catch (e) {

    const text = e?.response?.data || e?.message || '创建失败'
    toast(String(text), 'err')
  } finally {
    loading.value = false
  }
}

const del = async (productId) => {
  if (!confirm('确定删除该农产品？')) return

  deletingId.value = productId
  try {
    const res = await request.post(
        '/product/delete',
        new URLSearchParams({ productId }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )
    toast(String(res.data || '删除成功'), 'ok')
    await loadList()
  } catch (e) {
    const text = e?.response?.data || '删除失败'
    toast(String(text), 'err')
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.product {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.h1{ font-size: 22px; font-weight: 900; color:#0f172a; }

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px 16px;
}

.left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 900;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.15);
}

.txt .title {
  font-weight: 900;
  color: #0f172a;
  font-size: 16px;
}
.txt .sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

/* 提示条 */
.toast {
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.86);
  font-size: 13px;
}
.toast.ok {
  color: #16a34a;
  border-color: rgba(22, 163, 74, 0.25);
}
.toast.err {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.25);
}

.body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.card {
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.08);
  min-height: 260px;
}

.card-title {
  font-weight: 900;
  color: #0f172a;
  margin-bottom: 10px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field .label {
  font-size: 12px;
  color: #475569;
  margin-bottom: 6px;
}

.input,
.textarea {
  width: 100%;
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 12px;
  padding: 10px 12px;
  outline: none;
  font-size: 14px;
  background: #fff;
}
.textarea {
  min-height: 90px;
  resize: vertical;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

.btn {
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: #fff;
  color: #0f172a;
  font-weight: 800;
}

.btn.primary {
  border: none;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.18);
}

.btn.ghost {
  background: rgba(255, 255, 255, 0.7);
}

.btn.danger {
  border: none;
  background: #ef4444;
  color: #fff;
  box-shadow: 0 10px 18px rgba(239, 68, 68, 0.18);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint {
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
  margin-top: 6px;
}

.empty {
  color: #64748b;
  font-size: 13px;
  padding: 10px 2px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.item {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 14px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.9);
}

.item-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.name {
  font-weight: 900;
  color: #0f172a;
  font-size: 14px;
}

.id {
  font-size: 12px;
  color: #94a3b8;
}

.desc {
  margin-top: 8px;
  font-size: 13px;
  color: #334155;
  line-height: 1.5;
  min-height: 36px;
}

.meta {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
}

.item-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 980px) {
  .body {
    grid-template-columns: 1fr;
  }
}
</style>

