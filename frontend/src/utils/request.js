// src/utils/request.js
import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    withCredentials: true, // ✅ Session 必备（尤其你用 8080 直连时）
})

export default request
