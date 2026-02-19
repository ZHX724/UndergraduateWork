<template>
  <div class="home">
    <!-- Topbar -->
    <header class="topbar">
      <div class="brand" @click="goHome">
        <div class="logo">FT</div>
        <div class="txt">
          <div class="title">农事溯源系统|Traceability System</div>
        </div>
      </div>

      <div class="actions">
        <span class="pill">
          {{ user }}
          <span class="roleTag" :class="{ admin: isAdmin }">
            {{ isAdmin ? '监管端' : '用户端' }}
          </span>
        </span>
        <button class="btn ghost" type="button" @click="logout">退出</button>
      </div>
    </header>

    <!-- Body -->
    <div class="layout">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="menu-head">
          <div class="menu-title">功能</div>
          <div class="menu-sub">按角色开放：用户端/监管端互斥</div>
        </div>

        <nav class="menu">
          <button
              class="item"
              :class="{ active: active === 'dashboard', disabled: isAdmin }"
              type="button"
              :disabled="isAdmin"
              @click="setActive('dashboard')"
          >
            仪表盘
          </button>

          <button
              class="item"
              :class="{ active: active === 'products', disabled: isAdmin }"
              type="button"
              :disabled="isAdmin"
              @click="setActive('products')"
          >
            农产品管理
          </button>

          <button
              class="item"
              :class="{ active: active === 'batches', disabled: isAdmin }"
              type="button"
              :disabled="isAdmin"
              @click="setActive('batches')"
          >
            批次管理
          </button>

          <button
              class="item"
              :class="{ active: active === 'records', disabled: isAdmin }"
              type="button"
              :disabled="isAdmin"
              @click="setActive('records')"
          >
            溯源记录（上链）
          </button>

          <button
              class="item"
              :class="{ active: active === 'query', disabled: isAdmin }"
              type="button"
              :disabled="isAdmin"
              @click="setActive('query')"
          >
            溯源查询（管理端）
          </button>

          <button
              class="item"
              :class="{ active: active === 'risk', disabled: isAdmin }"
              type="button"
              :disabled="isAdmin"
              @click="setActive('risk')"
          >
            异常/风险
          </button>

          <!-- 审核：普通用户灰不可点；监管端可点且默认选中 -->
          <button
              class="item"
              :class="{ active: active === 'audit', disabled: !isAdminUserCanClickAudit }"
              type="button"
              :disabled="!isAdminUserCanClickAudit"
              @click="setActive('audit')"
              title="仅监管端可用"
          >
            审核（监管端）
          </button>

          <button
              class="item"
              :class="{ active: active === 'stats', disabled: isAdmin }"
              type="button"
              :disabled="isAdmin"
              @click="setActive('stats')"
          >
            统计分析
          </button>
        </nav>

        <div class="sidebar-foot">
          <div class="tip">
            <template v-if="isAdmin">
              ✅ 当前为监管端：仅开放“审核（监管端）”，其它功能已锁定。
            </template>
            <template v-else>
              ✅ 当前为用户端：已锁定“审核（监管端）”，其它功能正常使用。
            </template>
          </div>
        </div>
      </aside>

      <!-- Content -->
      <main class="content">
        <!-- 用户端模块 -->
        <template v-if="!isAdmin">
          <Dashboard v-if="active === 'dashboard'" />
          <Product v-else-if="active === 'products'" />
          <Batch v-else-if="active === 'batches'" />
          <TraceRecords v-else-if="active === 'records'" />
          <TraceQueryAdmin v-else-if="active === 'query'" />
          <Risk v-else-if="active === 'risk'" />
          <Stats v-else-if="active === 'stats'" />

          <!-- 其它占位 -->
          <template v-else>
            <section class="hero">
              <div class="hero-left">
                <h2 class="h2">{{ titleMap[active] }}</h2>
                <p class="p">这里是占位区域，后续把模块逐个替换为真实页面。</p>
              </div>

              <div class="hero-right">
                <div class="kpi">
                  <div class="kpi-num">—</div>
                  <div class="kpi-label">农产品</div>
                </div>
                <div class="kpi">
                  <div class="kpi-num">—</div>
                  <div class="kpi-label">批次</div>
                </div>
                <div class="kpi">
                  <div class="kpi-num">—</div>
                  <div class="kpi-label">异常</div>
                </div>
              </div>
            </section>

            <section class="grid">
              <div class="card" v-for="n in 6" :key="n">
                <div class="card-title">模块卡片 {{ n }}</div>
                <div class="card-sub">占位：后续放表格/表单/时间线等。</div>
                <div class="card-actions">
                  <button class="btn mini" type="button">查看</button>
                  <button class="btn mini ghost" type="button">配置</button>
                </div>
              </div>
            </section>
          </template>
        </template>

        <!-- 监管端模块：只允许 audit -->
        <template v-else>
          <Audit />
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import Product from './Product.vue'
import Batch from './Batch.vue'
import TraceRecords from './TraceRecords.vue'
import TraceQueryAdmin from './TraceQueryAdmin.vue'
import Audit from './Audit.vue'

// ✅ 新增：三个页面组件（同目录）
import Dashboard from './Dashboard.vue'
import Risk from './Risk.vue'
import Stats from './Stats.vue'

import request from '@/utils/request'

const router = useRouter()

const user = ref(localStorage.getItem('user') || '未登录')
const role = ref(0) // 0=用户端 1=监管端
const active = ref('dashboard')

const isAdmin = computed(() => Number(role.value) === 1)

// 普通用户：audit 不可点；监管端：audit 可点
const isAdminUserCanClickAudit = computed(() => isAdmin.value)

const titleMap = {
  dashboard: '仪表盘',
  products: '农产品管理',
  batches: '批次管理',
  records: '溯源记录（上链）',
  query: '溯源查询（管理端）',
  risk: '异常 / 风险记录',
  audit: '溯源信息审核（监管端）',
  stats: '数据统计与分析',
}

const setActive = (key) => {
  // 监管端：只能待在 audit
  if (isAdmin.value) {
    active.value = 'audit'
    return
  }
  // 用户端：点到 audit 直接阻止
  if (key === 'audit') return
  active.value = key
}

const logout = async () => {
  try {
    await request.post('/auth/logout')
  } catch (e) {}
  localStorage.removeItem('user')
  router.push('/login')
}

const goHome = () => {
  // 监管端点击 logo 也回 audit
  active.value = isAdmin.value ? 'audit' : 'dashboard'
}

async function loadMe() {
  // 复用你后端 /auth/me：建议返回 {login:true, username, role}
  const res = await request.get('/auth/me')
  const me = res.data || {}
  if (me?.username) user.value = me.username
  role.value = Number(me?.role ?? 0)

  // 监管端登录后直接跳 audit
  active.value = isAdmin.value ? 'audit' : 'dashboard'
}

onMounted(() => {
  loadMe().catch(() => {
    // 如果 /auth/me 失败，交给路由守卫处理，这里不强行跳转
  })
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: radial-gradient(1200px 600px at 10% 10%, rgba(255, 255, 255, 0.35), transparent 60%),
  radial-gradient(900px 500px at 90% 30%, rgba(255, 255, 255, 0.25), transparent 55%),
  linear-gradient(135deg, #0ea5e9, #6366f1);
}

/* Topbar */
.topbar {
  flex: 0 0 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}
.title {
  margin-top: 10px;
  font-size: 22px;
  letter-spacing: 0.2px;
}
.brand {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  user-select: none;
}
.logo {
  margin-top: 10px;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 800;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.15);
}
.txt .title {
  font-weight: 900;
  color: #0f172a;
  letter-spacing: 0.2px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pill {
  padding: 6px 10px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: #334155;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.roleTag {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 1);
  color: #475569;
}
.roleTag.admin {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.35);
  color: #166534;
}

/* Layout */
.layout {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 260px 1fr;
}

/* Sidebar */
.sidebar {
  padding: 16px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(226, 232, 240, 0.8);
  overflow: auto;
}
.menu-head {
  padding: 4px 2px 12px;
  border-bottom: 1px dashed rgba(148, 163, 184, 0.45);
  margin-bottom: 12px;
}
.menu-title {
  font-weight: 900;
  color: #0f172a;
}
.menu-sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}
.menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item {
  text-align: left;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: #fff;
  cursor: pointer;
  color: #0f172a;
  transition: transform 0.05s ease, box-shadow 0.15s ease, border-color 0.15s ease, opacity 0.15s ease;
}
.item:active {
  transform: translateY(1px);
}
.item.active {
  border-color: rgba(37, 99, 235, 0.55);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
}
.item.disabled,
.item:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.sidebar-foot {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed rgba(148, 163, 184, 0.45);
}
.tip {
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

/* Content */
.content {
  padding: 18px;
  overflow: auto;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 14px;
}
.h2 {
  margin: 0 0 6px;
  font-weight: 900;
  color: #0f172a;
}
.p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.hero-right {
  display: flex;
  gap: 10px;
}
.kpi {
  width: 88px;
  padding: 10px 10px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.9);
  text-align: center;
}
.kpi-num {
  font-weight: 900;
  font-size: 18px;
  color: #0f1721;
}
.kpi-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.card {
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  padding: 14px;
  min-height: 120px;
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.08);
}
.card-title {
  font-weight: 900;
  color: #0f172a;
}
.card-sub {
  color: #64748b;
  font-size: 13px;
  margin-top: 6px;
}
.card-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

/* Buttons */
.btn {
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: #fff;
  cursor: pointer;
  color: #0f172a;
}
.btn.ghost {
  background: transparent;
}
.btn.mini {
  padding: 6px 10px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  border: none;
}
.btn.mini.ghost {
  background: transparent;
  border: 1px solid rgba(226, 232, 240, 0.9);
  color: #0f172a;
}

@media (max-width: 760px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    display: none;
  }
}
</style>

