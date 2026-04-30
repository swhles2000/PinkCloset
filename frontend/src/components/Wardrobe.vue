<template>
  <!--
    Wardrobe.vue — 我的衣橱 🗂
    功能：
      1. 展示所有已上传衣物（按分类筛选）
      2. 上传新衣物（支持图片 + 信息填写）
      3. 删除衣物
  -->
  <div class="wardrobe-container fade-in-up">

    <!-- ══════ 页面标题 ══════ -->
    <div class="page-header">
      <h1 class="page-title">🗂 我的衣橱</h1>
      <p class="page-subtitle">管理你的衣物，让每一件都被看见 🌸</p>
    </div>

    <!-- ══════ 统计卡片 ══════ -->
    <div class="stat-row">
      <div
        v-for="cat in statCategories"
        :key="cat.value"
        class="stat-card card"
      >
        <div class="stat-icon">{{ cat.icon }}</div>
        <div class="stat-num">{{ countByCategory(cat.value) }}</div>
        <div class="stat-label">{{ cat.label }}</div>
      </div>
    </div>

    <!-- ══════ 工具栏：分类筛选 + 上传按钮 ══════ -->
    <div class="toolbar">
      <div class="category-tabs">
        <button
          v-for="cat in allCategories"
          :key="cat.value"
          class="cat-tab"
          :class="{ 'cat-tab-active': activeCategory === cat.value }"
          @click="activeCategory = cat.value"
        >
          {{ cat.icon }} {{ cat.label }}
        </button>
      </div>
      <button class="btn btn-primary" @click="showUploadModal = true">
        + 添加衣物
      </button>
    </div>

    <!-- ══════ 衣物网格 ══════ -->
    <div v-if="filteredClothes.length > 0" class="clothes-grid">
      <div
        v-for="(item, index) in filteredClothes"
        :key="item.id"
        class="clothes-card card"
      >
        <!-- 序号角标 -->
        <div class="item-index">{{ index + 1 }}</div>

        <!-- 图片区 -->
        <div class="item-img-wrap">
          <img :src="getImgSrc(item)" :alt="item.name" class="item-img" />
          <!-- 悬停操作层 -->
          <div class="item-overlay">
            <button class="overlay-btn btn-edit" @click="openEditModal(item)">
              ✏️ 编辑
            </button>
            <button class="overlay-btn btn-danger" @click="confirmDelete(item)">
              🗑 删除
            </button>
          </div>
        </div>

        <!-- 信息区 -->
        <div class="item-info">
          <div class="item-name">{{ item.name }}</div>
          <div class="item-meta">
            <span class="tag tag-pink">{{ getCatLabel(item.category) }}</span>
            <span v-if="item.color" class="color-dot-wrap">
              <span class="color-label">{{ item.color }}</span>
            </span>
          </div>
          <div v-if="item.style" class="item-style">{{ item.style }}</div>
          <div class="item-date">{{ formatDate(item.createTime) }}</div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">🌸</div>
      <p>还没有衣物，快来添加第一件吧~</p>
      <button class="btn btn-primary" style="margin-top:16px" @click="showUploadModal = true">
        + 添加衣物
      </button>
    </div>

    <!-- ══════ 上传弹窗 ══════ -->
    <div v-if="showUploadModal" class="modal-overlay" @click.self="closeUploadModal">
      <div class="modal-box card fade-in-up">

        <div class="modal-header">
          <h3>✨ 添加新衣物</h3>
          <button class="modal-close" @click="closeUploadModal">✕</button>
        </div>

        <div class="modal-body">

          <!-- 图片上传区 -->
          <div
            class="upload-zone"
            :class="{ 'upload-zone-active': dragOver }"
            @click="triggerFileInput"
            @dragover.prevent="dragOver = true"
            @dragleave="dragOver = false"
            @drop.prevent="handleDrop"
          >
            <template v-if="previewUrl">
              <img :src="previewUrl" class="upload-preview" alt="预览" />
              <button class="remove-preview" @click.stop="clearPreview">✕</button>
            </template>
            <template v-else>
              <div class="upload-icon">📷</div>
              <p class="upload-hint">点击或拖拽图片到此处</p>
              <p class="upload-hint-sub">支持 JPG / PNG / WebP，最大 10MB</p>
            </template>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display:none"
            @change="handleFileChange"
          />

          <!-- 表单字段 -->
          <div class="form-grid">
            <div class="form-item">
              <label class="form-label">衣物名称 <span class="required">*</span></label>
              <input
                v-model="form.name"
                class="form-input"
                placeholder="如：白色棉质T恤"
                maxlength="50"
              />
            </div>

            <div class="form-item">
              <label class="form-label">分类 <span class="required">*</span></label>
              <select v-model="form.category" class="form-input form-select">
                <option value="">请选择分类</option>
                <option value="TOP">👕 上衣</option>
                <option value="BOTTOM">👖 下装</option>
                <option value="SHOES">👟 鞋子</option>
                <option value="ACCESSORY">💍 配饰</option>
              </select>
            </div>

            <div class="form-item">
              <label class="form-label">主色调</label>
              <input
                v-model="form.color"
                class="form-input"
                placeholder="如：粉色、白色"
                maxlength="20"
              />
            </div>

            <div class="form-item">
              <label class="form-label">风格标签</label>
              <input
                v-model="form.style"
                class="form-input"
                placeholder="如：甜美, 休闲（逗号分隔）"
                maxlength="100"
              />
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="uploadError" class="error-tip">⚠️ {{ uploadError }}</div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeUploadModal">取消</button>
          <button
            class="btn btn-primary"
            :disabled="uploading"
            @click="submitUpload"
          >
            <span v-if="uploading">上传中…</span>
            <span v-else>上传衣物 🌸</span>
          </button>
        </div>

      </div>
    </div>

    <!-- ══════ 编辑弹窗 ══════ -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-box card fade-in-up">

        <div class="modal-header">
          <h3>✏️ 修改衣物信息</h3>
          <button class="modal-close" @click="closeEditModal">✕</button>
        </div>

        <div class="modal-body">

          <!-- 当前图片预览 + 更换 -->
          <div
            class="upload-zone"
            :class="{ 'upload-zone-active': editDragOver }"
            @click="editFileRef?.click()"
            @dragover.prevent="editDragOver = true"
            @dragleave="editDragOver = false"
            @drop.prevent="handleEditDrop"
          >
            <template v-if="editPreviewUrl">
              <img :src="editPreviewUrl" class="upload-preview" alt="预览" />
              <button class="remove-preview" @click.stop="clearEditPreview">✕</button>
            </template>
            <template v-else>
              <div class="upload-icon">📷</div>
              <p class="upload-hint">点击或拖拽新图片更换</p>
              <p class="upload-hint-sub">不选择则保留原图</p>
            </template>
          </div>
          <input
            ref="editFileRef"
            type="file"
            accept="image/*"
            style="display:none"
            @change="handleEditFileChange"
          />

          <!-- 表单字段 -->
          <div class="form-grid">
            <div class="form-item">
              <label class="form-label">衣物名称 <span class="required">*</span></label>
              <input
                v-model="editForm.name"
                class="form-input"
                placeholder="如：白色棉质T恤"
                maxlength="50"
              />
            </div>

            <div class="form-item">
              <label class="form-label">分类 <span class="required">*</span></label>
              <select v-model="editForm.category" class="form-input form-select">
                <option value="">请选择分类</option>
                <option value="TOP">👕 上衣</option>
                <option value="BOTTOM">👖 下装</option>
                <option value="SHOES">👟 鞋子</option>
                <option value="ACCESSORY">💍 配饰</option>
              </select>
            </div>

            <div class="form-item">
              <label class="form-label">主色调</label>
              <input
                v-model="editForm.color"
                class="form-input"
                placeholder="如：粉色、白色"
                maxlength="20"
              />
            </div>

            <div class="form-item">
              <label class="form-label">风格标签</label>
              <input
                v-model="editForm.style"
                class="form-input"
                placeholder="如：甜美, 休闲（逗号分隔）"
                maxlength="100"
              />
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="editError" class="error-tip">⚠️ {{ editError }}</div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeEditModal">取消</button>
          <button
            class="btn btn-primary"
            :disabled="editSaving"
            @click="submitEdit"
          >
            <span v-if="editSaving">保存中…</span>
            <span v-else>保存修改 ✨</span>
          </button>
        </div>

      </div>
    </div>

    <!-- ══════ 删除确认弹窗 ══════ -->
    <div v-if="deleteTarget" class="modal-overlay" @click.self="deleteTarget = null">
      <div class="modal-box card fade-in-up" style="max-width:380px; text-align:center">
        <div class="modal-body" style="padding:32px 24px">
          <div style="font-size:48px;margin-bottom:12px">🗑</div>
          <h3 style="font-size:17px;margin-bottom:8px">确认删除？</h3>
          <p style="color:var(--text-muted);font-size:14px">
            将删除「{{ deleteTarget.name }}」，你可以联系管理员恢复哦~
          </p>
        </div>
        <div class="modal-footer" style="justify-content:center;gap:16px">
          <button class="btn btn-ghost" @click="deleteTarget = null">取消</button>
          <button class="btn btn-danger" @click="doDelete">确认删除</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getClothes, uploadClothes, deleteClothes, updateClothing } from '../api.js'

// ─────────────────────────────────────────────────
//  分类配置
// ─────────────────────────────────────────────────
const allCategories = [
  { value: 'ALL',       label: '全部',  icon: '🌸' },
  { value: 'TOP',       label: '上衣',  icon: '👕' },
  { value: 'BOTTOM',    label: '下装',  icon: '👖' },
  { value: 'SHOES',     label: '鞋子',  icon: '👟' },
  { value: 'ACCESSORY', label: '配饰',  icon: '💍' }
]

const statCategories = [
  { value: 'TOP',       label: '上衣',  icon: '👕' },
  { value: 'BOTTOM',    label: '下装',  icon: '👖' },
  { value: 'SHOES',     label: '鞋子',  icon: '👟' },
  { value: 'ACCESSORY', label: '配饰',  icon: '💍' }
]

const catMap = { TOP: '上衣', BOTTOM: '下装', SHOES: '鞋子', ACCESSORY: '配饰' }
const getCatLabel = (cat) => catMap[cat] || cat

// ─────────────────────────────────────────────────
//  状态
// ─────────────────────────────────────────────────
const allClothes     = ref([])
const activeCategory = ref('ALL')
const showUploadModal = ref(false)
const showEditModal  = ref(false)
const deleteTarget   = ref(null)
const editTarget     = ref(null)  // 当前正在编辑的衣物对象

// 上传表单
const form = ref({ name: '', category: '', color: '', style: '' })
const fileInputRef = ref(null)
const selectedFile = ref(null)
const previewUrl   = ref('')
const dragOver     = ref(false)
const uploading    = ref(false)
const uploadError  = ref('')

// 编辑表单
const editForm       = ref({ name: '', category: '', color: '', style: '' })
const editFileRef    = ref(null)
const editFile       = ref(null)
const editPreviewUrl = ref('')
const editDragOver   = ref(false)
const editSaving     = ref(false)
const editError      = ref('')

// ─────────────────────────────────────────────────
//  计算属性
// ─────────────────────────────────────────────────
const filteredClothes = computed(() =>
  activeCategory.value === 'ALL'
    ? allClothes.value
    : allClothes.value.filter(c => c.category === activeCategory.value)
)

const countByCategory = (cat) =>
  allClothes.value.filter(c => c.category === cat).length

// ─────────────────────────────────────────────────
//  生命周期
// ─────────────────────────────────────────────────
onMounted(loadClothes)

// ─────────────────────────────────────────────────
//  数据加载
// ─────────────────────────────────────────────────
async function loadClothes() {
  try {
    const res = await getClothes()
    if (res.code === 200) allClothes.value = res.data
  } catch (e) {
    console.error('加载失败', e)
  }
}

// ─────────────────────────────────────────────────
//  图片处理
// ─────────────────────────────────────────────────
function triggerFileInput() {
  fileInputRef.value?.click()
}

function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (file) setPreview(file)
}

function handleDrop(e) {
  dragOver.value = false
  const file = e.dataTransfer.files?.[0]
  if (file && file.type.startsWith('image/')) setPreview(file)
}

function setPreview(file) {
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

function clearPreview() {
  selectedFile.value = null
  previewUrl.value = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
}

// ─────────────────────────────────────────────────
//  上传衣物
// ─────────────────────────────────────────────────
async function submitUpload() {
  uploadError.value = ''

  // 基础校验
  if (!form.value.name.trim()) {
    uploadError.value = '请输入衣物名称'
    return
  }
  if (!form.value.category) {
    uploadError.value = '请选择衣物分类'
    return
  }

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('name',     form.value.name.trim())
    fd.append('category', form.value.category)
    if (form.value.color)  fd.append('color', form.value.color.trim())
    if (form.value.style)  fd.append('style', form.value.style.trim())
    if (selectedFile.value) fd.append('image', selectedFile.value)

    const res = await uploadClothes(fd)
    if (res.code === 200) {
      allClothes.value.unshift(res.data)  // 插入列表头部
      window.dispatchEvent(new CustomEvent('clothes-updated'))
      closeUploadModal()
    } else {
      uploadError.value = res.message || '上传失败'
    }
  } catch (e) {
    uploadError.value = '网络错误，请重试'
    console.error(e)
  } finally {
    uploading.value = false
  }
}

function closeUploadModal() {
  showUploadModal.value = false
  form.value = { name: '', category: '', color: '', style: '' }
  clearPreview()
  uploadError.value = ''
}

// ─────────────────────────────────────────────────
//  删除衣物
// ─────────────────────────────────────────────────
function confirmDelete(item) {
  deleteTarget.value = item
}

async function doDelete() {
  if (!deleteTarget.value) return
  const id = deleteTarget.value.id
  try {
    const res = await deleteClothes(id)
    if (res.code === 200) {
      // 从本地列表中移除
      allClothes.value = allClothes.value.filter(c => c.id !== id)
      // 同时通知 OutfitLab 刷新（通过事件总线 / 全局状态）
      window.dispatchEvent(new CustomEvent('clothes-updated'))
    } else {
      alert('删除失败：' + (res.message || '未知错误'))
    }
  } catch (e) {
    alert('网络错误，删除失败：' + (e.message || '请检查后端是否在运行'))
    console.error('删除失败', e)
  } finally {
    deleteTarget.value = null
  }
}

// ─────────────────────────────────────────────────
//  编辑衣物
// ─────────────────────────────────────────────────
function openEditModal(item) {
  editTarget.value = item
  editForm.value = {
    name: item.name || '',
    category: item.category || '',
    color: item.color || '',
    style: item.style || ''
  }
  // 显示当前图片
  editPreviewUrl.value = item.imageUrl || ''
  editFile.value = null
  editError.value = ''
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
  editTarget.value = null
  editFile.value = null
  editPreviewUrl.value = ''
  editError.value = ''
  if (editFileRef.value) editFileRef.value.value = ''
}

function handleEditFileChange(e) {
  const file = e.target.files?.[0]
  if (file) {
    editFile.value = file
    editPreviewUrl.value = URL.createObjectURL(file)
  }
}

function handleEditDrop(e) {
  editDragOver.value = false
  const file = e.dataTransfer.files?.[0]
  if (file && file.type.startsWith('image/')) {
    editFile.value = file
    editPreviewUrl.value = URL.createObjectURL(file)
  }
}

function clearEditPreview() {
  editFile.value = null
  // 恢复显示原图
  if (editTarget.value?.imageUrl) {
    editPreviewUrl.value = editTarget.value.imageUrl
  } else {
    editPreviewUrl.value = ''
  }
  if (editFileRef.value) editFileRef.value.value = ''
}

async function submitEdit() {
  editError.value = ''

  if (!editForm.value.name.trim()) {
    editError.value = '请输入衣物名称'
    return
  }
  if (!editForm.value.category) {
    editError.value = '请选择衣物分类'
    return
  }

  editSaving.value = true
  try {
    const fd = new FormData()
    fd.append('name', editForm.value.name.trim())
    fd.append('category', editForm.value.category)
    if (editForm.value.color)  fd.append('color', editForm.value.color.trim())
    if (editForm.value.style)  fd.append('style', editForm.value.style.trim())
    // 只在用户选择了新图片时才传 image
    if (editFile.value) fd.append('image', editFile.value)

    const res = await updateClothing(editTarget.value.id, fd)
    if (res.code === 200) {
      // 用返回的最新数据更新本地列表
      const idx = allClothes.value.findIndex(c => c.id === editTarget.value.id)
      if (idx !== -1) {
        allClothes.value[idx] = { ...allClothes.value[idx], ...res.data }
      }
      // 通知其他页面刷新
      window.dispatchEvent(new CustomEvent('clothes-updated'))
      closeEditModal()
    } else {
      editError.value = res.message || '修改失败'
    }
  } catch (e) {
    editError.value = '网络错误，请重试'
    console.error(e)
  } finally {
    editSaving.value = false
  }
}

// ─────────────────────────────────────────────────
//  工具方法
// ─────────────────────────────────────────────────
function getImgSrc(item) {
  if (item.imageUrl) return item.imageUrl
  const icons = { TOP: '👕', BOTTOM: '👖', SHOES: '👟', ACCESSORY: '💍' }
  const emoji = icons[item.category] || '👗'
  return `data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect width='100' height='100' fill='%23fff0f5' rx='12'/><text y='62' x='50' font-size='52' text-anchor='middle'>${emoji}</text></svg>`
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}
</script>

<style scoped>
/* ================================================================
   我的衣橱 — 专属样式
   ================================================================ */

.wardrobe-container {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* ─── 页面标题 ─────────────────── */
.page-header { text-align: center; }

.page-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
}

.page-subtitle {
  color: var(--text-muted);
  font-size: 15px;
}

/* ─── 统计卡片 ─────────────────── */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.stat-card {
  padding: 18px 12px;
  text-align: center;
  border-radius: var(--radius-md);
  transition: var(--transition);
}

.stat-card:hover {
  transform: translateY(-3px);
}

.stat-icon  { font-size: 28px; margin-bottom: 6px; }
.stat-num   { font-size: 24px; font-weight: 700; color: var(--pink-dark); }
.stat-label { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

/* ─── 工具栏 ─────────────────── */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.category-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cat-tab {
  padding: 7px 16px;
  border-radius: 999px;
  border: 1.5px solid var(--border);
  background: var(--white);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition-fast);
}

.cat-tab:hover {
  border-color: var(--pink-300);
  color: var(--pink-400);
}

.cat-tab-active {
  background: linear-gradient(135deg, var(--pink-200), var(--pink-100));
  border-color: var(--pink-300);
  color: var(--pink-dark);
  font-weight: 600;
}

/* ─── 衣物网格 ─────────────────── */
.clothes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 18px;
}

.clothes-card {
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: var(--transition);
  position: relative;
}

/* 序号角标 */
.item-index {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-300));
  color: white;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  box-shadow: 0 2px 6px rgba(255,105,135,0.35);
}

.clothes-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

/* 图片区 */
.item-img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 3/4;
  overflow: hidden;
  background: var(--pink-50);
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.clothes-card:hover .item-img {
  transform: scale(1.07);
}

/* 悬停操作层 */
.item-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 230, 240, 0.72);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.22s ease;
}

.clothes-card:hover .item-overlay {
  opacity: 1;
}

.overlay-btn {
  padding: 9px 18px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
  transition: var(--transition-fast);
}

.overlay-btn.btn-edit {
  color: var(--pink-dark);
}

.overlay-btn.btn-edit:hover {
  background: var(--pink-100);
  transform: scale(1.05);
}

.overlay-btn.btn-danger {
  color: #e53e3e;
}

.overlay-btn.btn-danger:hover {
  background: #fee2e2;
  transform: scale(1.05);
}

/* 信息区 */
.item-info {
  padding: 10px 12px 12px;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.color-label {
  font-size: 11px;
  color: var(--text-muted);
}

.item-style {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-date {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 5px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 14px;
}

/* ─── 弹窗 ─────────────────── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(60, 20, 40, 0.35);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-box {
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border);
}

.modal-header h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
}

.modal-close {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: var(--pink-100);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-fast);
}

.modal-close:hover { background: var(--pink-200); }

.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border);
}

/* ─── 图片上传区 ─────────────────── */
.upload-zone {
  position: relative;
  width: 100%;
  height: 200px;
  border: 2px dashed var(--pink-300);
  border-radius: var(--radius-md);
  background: var(--pink-50);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: var(--transition-fast);
  overflow: hidden;
}

.upload-zone:hover,
.upload-zone-active {
  border-color: var(--pink-400);
  background: var(--pink-100);
}

.upload-icon { font-size: 36px; }

.upload-hint {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.upload-hint-sub {
  font-size: 12px;
  color: var(--text-muted);
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-preview {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  border: 1px solid var(--border);
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

/* ─── 表单 ─────────────────── */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.required { color: var(--pink-400); }

.form-input {
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--border);
  font-size: 14px;
  color: var(--text-primary);
  background: var(--pink-50);
  outline: none;
  transition: var(--transition-fast);
}

.form-input:focus {
  border-color: var(--pink-400);
  background: white;
  box-shadow: 0 0 0 3px rgba(255,105,135,0.12);
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23b08b99' stroke-width='1.5' fill='none' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
  cursor: pointer;
}

.error-tip {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #e53e3e;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

/* ─── 响应式 ─────────────────── */
@media (max-width: 600px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .clothes-grid {
    grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  }
}
</style>
