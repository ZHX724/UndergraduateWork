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
          登录后你可以创建农产品与批次、记录生产/加工/流通信息，并按时间线展示全过程。
        </div>
      </div>

      <div class="form">
        <h2 class="title">登录</h2>
        <p class="sub">欢迎回来，继续你的溯源记录。</p>

        <div class="field">
          <div class="label">用户名</div>
          <input class="input" v-model="username" placeholder="请输入用户名" />
        </div>

        <div class="field">
          <div class="label">密码</div>
          <input class="input" v-model="password" type="password" placeholder="请输入密码" />
        </div>

        <button class="btn" @click="login">登录</button>

        <div class="msg" :class="{ err: msgType==='err' }">{{ msg }}</div>

        <div class="msg">
          没有账号？ <a href="#" @click.prevent="router.push('/register')">去注册</a>
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

const login = async () => {
  msg.value = ''
  msgType.value = ''
  try {
    const res = await request.post(
        '/auth/login',
        new URLSearchParams({ username: username.value, password: password.value }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )

    msg.value = res.data
    msgType.value = res.data === '登录成功' ? 'ok' : 'err'

    if (res.data === '登录成功') {
      // 你既然想用 Session，这个 localStorage 可以留着当展示用，不作为鉴权依据
      localStorage.setItem('user', username.value)
      router.push('/home')
    }
  } catch (e) {
    msg.value = '请求失败（检查后端是否启动/代理是否生效）'
    msgType.value = 'err'
    console.error(e)
  }
}
</script>
