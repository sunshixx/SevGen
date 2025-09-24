import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000, // 增加超时时间到30秒
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加token到header
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 开发环境下打印请求信息
    if (import.meta.env.DEV) {
      console.log(`[Request] ${config.method?.toUpperCase()} ${config.url}`, config.data || config.params)
    }
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 开发环境下打印响应信息
    if (import.meta.env.DEV) {
      console.log(`[Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, data)
    }
    
    // 如果后端返回的不是标准格式，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 检查是否是标准的ApiResponse格式
    if (data && typeof data === 'object' && 'success' in data) {
      // 处理业务逻辑错误
      if (data.success === false) {
        const errorMessage = data.message || '请求失败'
        ElMessage.error(errorMessage)
        
        // 401错误跳转到登录页
        if (data.code === 401) {
          localStorage.removeItem('token')
          window.location.href = '/login'
        }
        
        return Promise.reject(new Error(errorMessage))
      }
      
      return data
    }
    
    // 非标准格式，直接返回data
    return data
  },
  error => {
    console.error('响应拦截器错误:', error)
    
    let message = '网络错误'
    if (error.response) {
      const status = error.response.status
      const responseData = error.response.data
      
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = responseData?.message || error.message || '请求失败'
      }
    } else if (error.request) {
      message = '网络连接失败'
    } else {
      message = error.message || '请求配置错误'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request