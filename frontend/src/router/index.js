import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Product from '../views/Product.vue'

import request from '@/utils/request'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', redirect: '/login' },
        { path: '/login', component: Login },
        { path: '/register', component: Register },
        { path: '/home', component: Home },
        { path: '/:pathMatch(.*)*', redirect: '/login' },
    ],
})

// 调用后端 Session 校验
async function isLogin() {
    const res = await request.get('/auth/me')
    return res.data?.login === true
}

router.beforeEach(async (to, from, next) => {
    // 登录 / 注册页
    if (to.path === '/login' || to.path === '/register') {
        try {
            if (await isLogin()) return next('/home')
        } catch (e) {}
        return next()
    }

    // 其它页面都需要登录
    try {
        if (await isLogin()) return next()
        return next('/login')
    } catch (e) {
        return next('/login')
    }
})

export default router
