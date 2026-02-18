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
