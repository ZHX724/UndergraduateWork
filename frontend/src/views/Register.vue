<template>
  <div class="page">
    <div class="card">
      <div class="side">
        <div class="brand">
          <div class="logo">FT</div>
          <div>
            <h1>农事溯源系统</h1>
            <p>Blockchain-style Traceability</p>
          </div>
        </div>
        <div class="desc">
          创建账号后会与溯源身份绑定，后续每条记录都能追责到主体。
        </div>
      </div>

      <div class="form">
        <h2 class="title">注册</h2>
        <p class="sub">几秒钟创建账号，马上开始记录。</p>

        <div class="field">
          <div class="label">用户名</div>
          <input class="input" v-model="username" placeholder="请输入用户名" />
        </div>

        <div class="field">
          <div class="label">密码</div>
          <input class="input" v-model="password" type="password" placeholder="请输入密码" />
        </div>

        <button class="btn" @click="register">注册</button>

        <div class="msg" :class="{ err: msgType==='err' }">{{ msg }}</div>

        <div class="msg">
          已有账号？ <a href="#" @click.prevent="router.push('/')">去登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')
const msg = ref('')
const msgType = ref('')

const register = async () => {
  msg.value = ''
  msgType.value = ''
  try {
    const res = await request.post(
        '/auth/register',
        new URLSearchParams({ username: username.value, password: password.value }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )
    msg.value = res.data
    msgType.value = res.data === '注册成功' ? 'ok' : 'err'
    if (res.data === '注册成功') router.push('/')
  } catch (e) {
    msg.value = '请求失败（检查后端是否启动/代理是否生效）'
    msgType.value = 'err'
  }
}
</script>

<style scoped>

.page{
  min-height: 100vh;
  display:flex;
  align-items:center;
  justify-content:center;
  padding: 24px;
  background:
      linear-gradient(rgba(15, 23, 42, 0.35), rgba(15, 23, 42, 0.35)),
      url('http://localhost:8080/home.png') center center / cover no-repeat;
}
.public-card{
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  cursor: pointer;
  user-select: none;

  background: rgba(255,255,255,0.82);
  border: 1px solid rgba(226,232,240,0.9);
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.10);
  backdrop-filter: blur(10px);

  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:12px;

  transition: transform .06s ease, box-shadow .15s ease, border-color .15s ease;
}

.public-card:hover{
  transform: translateY(-1px);
  border-color: rgba(37, 99, 235, 0.55);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12), 0 18px 40px rgba(2, 6, 23, 0.12);
}

.pc-title{
  font-weight: 900;
  color:#0f172a;
}

.pc-sub{
  margin-top: 6px;
  font-size: 12px;
  color:#64748b;
  line-height: 1.5;
}

.pc-right{
  font-weight: 900;
  color:#1d4ed8;
  white-space: nowrap;
}
</style>
