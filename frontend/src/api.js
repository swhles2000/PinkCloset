/**
 * api.js — Axios 请求封装
 * 统一处理请求/响应，封装衣物与搭配方案相关接口
 */
import axios from 'axios'

// 创建 axios 实例
const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// ─── 响应拦截器：统一提取 data 字段 ───────────────────────
http.interceptors.response.use(
  res => res.data,   // 直接返回 Result 对象 { code, message, data }
  err => {
    console.error('请求出错：', err)
    // 网络错误（服务未启动等），触发全局服务宕机提示
    if (!err.response && (err.code === 'ERR_NETWORK' || err.code === 'ECONNABORTED' || err.code === 'ERR_CONNECTION_REFUSED')) {
      window.dispatchEvent(new CustomEvent('server-down', { detail: err.message }))
    }
    return Promise.reject(err)
  }
)

// ─── 获取当前登录用户 ID ───────────────────────────────────
export function getUserId() {
  const raw = localStorage.getItem('pc_user')
  if (raw) {
    try { return JSON.parse(raw).id } catch { return 0 }
  }
  return 0
}

// ══════════════════════════════════════════════════
//  衣物相关接口
// ══════════════════════════════════════════════════

/**
 * 获取衣物列表（按用户隔离）
 * @param {string} [category] - 可选，TOP / BOTTOM / SHOES / ACCESSORY
 * @param {number} [userId]   - 可选，默认使用当前登录用户 ID
 */
export function getClothes(category = '', userId) {
  const uid = userId || getUserId()
  return http.get('/clothes', {
    params: { userId: uid, ...(category ? { category } : {}) }
  })
}

/**
 * 上传衣物（含图片，按用户隔离）
 * @param {FormData} formData - 包含 name, category, color, style, image
 * @param {number} [userId]   - 可选，默认使用当前登录用户 ID
 */
export function uploadClothes(formData, userId) {
  const uid = userId || getUserId()
  formData.append('userId', uid)
  return http.post('/clothes', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 删除衣物
 * @param {number|string} id
 */
export function deleteClothes(id) {
  return http.delete(`/clothes/${id}`)
}

/**
 * 修改衣物信息（名称、分类、颜色、风格、图片）
 * @param {number|string} id
 * @param {FormData} formData - 包含 name, category, color(可选), style(可选), image(可选)
 */
export function updateClothing(id, formData) {
  return http.put(`/clothes/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ══════════════════════════════════════════════════
//  搭配方案接口
// ══════════════════════════════════════════════════

/** 获取当前用户保存的搭配方案 */
export function getOutfits(userId) {
  const uid = userId || getUserId()
  return http.get('/outfits', { params: { userId: uid } })
}

/**
 * 保存搭配方案（按用户隔离）
 * @param {{ name, topId, bottomId, shoesId, accessoryId, note }} plan
 * @param {number} [userId]
 */
export function saveOutfit(plan, userId) {
  const uid = userId || getUserId()
  return http.post('/outfits', { ...plan, userId: uid })
}

/**
 * 删除搭配方案
 * @param {number|string} id
 */
export function deleteOutfit(id) {
  return http.delete(`/outfits/${id}`)
}

// ══════════════════════════════════════════════════
//  普通用户相关接口
// ══════════════════════════════════════════════════

/**
 * 登录
 * @param {{ account: string, password: string }} data
 */
export function login(data) {
  return http.post('/user/login', data)
}

/**
 * 注册
 * @param {{ name, gender, age, phone, email, password, confirmPassword }} data
 */
export function register(data) {
  return http.post('/user/register', data)
}

/**
 * 注销账号（软删除）
 * @param {number|string} id - 用户 ID
 */
export function deleteAccount(id) {
  return http.delete(`/user/${id}`)
}

/**
 * 上传/更新用户头像
 * @param {File} file - 图片文件
 * @param {number} [userId] - 可选，默认使用当前登录用户 ID
 */
export function uploadAvatar(file, userId) {
  const uid = userId || getUserId()
  const formData = new FormData()
  formData.append('userId', uid)
  formData.append('avatar', file)
  return http.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ══════════════════════════════════════════════════
//  管理员接口
// ══════════════════════════════════════════════════

/**
 * 管理员登录
 * @param {{ username: string, password: string }} data
 */
export function adminLogin(data) {
  return http.post('/admin/login', data)
}

/** 获取所有用户（含已注销） */
export function adminGetUsers() {
  return http.get('/admin/users')
}

/** 获取某用户的所有衣物（含已删除） */
export function adminGetUserClothes(userId) {
  return http.get(`/admin/users/${userId}/clothes`)
}

/** 获取某用户的所有搭配方案（含已删除） */
export function adminGetUserOutfits(userId) {
  return http.get(`/admin/users/${userId}/outfits`)
}

/** 管理员软删除衣物（deleted=1） */
export function adminSoftDeleteClothing(id) {
  return http.put(`/admin/clothes/${id}/soft`)
}

/** 恢复已软删除的衣物 */
export function adminRestoreClothing(id) {
  return http.put(`/admin/clothes/${id}/restore`)
}

/** 彻底删除衣物 */
export function adminHardDeleteClothing(id) {
  return http.delete(`/admin/clothes/${id}/hard`)
}

/** 管理员软删除搭配方案（deleted=1） */
export function adminSoftDeleteOutfit(id) {
  return http.put(`/admin/outfits/${id}/soft`)
}

/** 恢复已软删除的搭配方案 */
export function adminRestoreOutfit(id) {
  return http.put(`/admin/outfits/${id}/restore`)
}

/** 彻底删除搭配方案 */
export function adminHardDeleteOutfit(id) {
  return http.delete(`/admin/outfits/${id}/hard`)
}

/** 冻结用户（软删除） */
export function adminFreezeUser(id) {
  return http.put(`/admin/users/${id}/freeze`)
}

/** 恢复已注销用户 */
export function adminRestoreUser(id) {
  return http.put(`/admin/users/${id}/restore`)
}

/** 彻底删除用户及关联数据 */
export function adminHardDeleteUser(id) {
  return http.delete(`/admin/users/${id}`)
}
