import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',  // 设置统一的请求前缀
  timeout: 10000    // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    // 如果token存在，则添加到请求头
    if (token) {
      config.headers['satoken'] = token
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 如果响应成功
    if (res.code === 1) {
      return res
    }
    // 如果token失效
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
      return Promise.reject(new Error('登录已过期'))
    }
    // 其他错误
    console.log('res.code:', res.code) // 在控制台打印 res.code
    const errorMsg = res.msg || '请求失败'
    ElMessage.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  },
  error => {
    console.error('响应错误：', error)
    if (error.response) {
      // 服务器返回错误
      const errorMsg = error.response.data?.msg || `请求失败 (${error.response.status})`
      ElMessage.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    } else if (error.request) {
      // 请求发送失败
      ElMessage.error('网络请求失败，请检查网络连接')
      return Promise.reject(new Error('网络请求失败'))
    } else {
      // 其他错误
      ElMessage.error(error.message || '请求失败1')
      return Promise.reject(error)
    }
  }
)

export default request 