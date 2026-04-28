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
    return Promise.reject(err)
  }
)

// ══════════════════════════════════════════════════
//  衣物相关接口
// ══════════════════════════════════════════════════

/**
 * 获取衣物列表
 * @param {string} [category] - 可选，TOP / BOTTOM / SHOES / ACCESSORY
 */
export function getClothes(category = '') {
  return http.get('/clothes', {
    params: category ? { category } : {}
  })
}

/**
 * 上传衣物（含图片）
 * @param {FormData} formData - 包含 name, category, color, style, image
 */
export function uploadClothes(formData) {
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

// ══════════════════════════════════════════════════
//  搭配方案接口
// ══════════════════════════════════════════════════

/** 获取所有保存的搭配方案 */
export function getOutfits() {
  return http.get('/outfits')
}

/**
 * 保存搭配方案
 * @param {{ name, topId, bottomId, shoesId, accessoryId, note }} plan
 */
export function saveOutfit(plan) {
  return http.post('/outfits', plan)
}

/**
 * 删除搭配方案
 * @param {number|string} id
 */
export function deleteOutfit(id) {
  return http.delete(`/outfits/${id}`)
}
