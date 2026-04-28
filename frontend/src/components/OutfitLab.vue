<template>
  <!--
    OutfitLab.vue — 搭配实验室 🌸
    核心功能：
      1. 顶部三大槽位（上衣 / 裤子 / 鞋子）平铺展示
      2. 下方衣物选择区（按分类网格展示）
      3. 点击衣物 → 填入对应槽位
      4. 槽位右上角 X 清空 / 刷新图标快速切换
      5. 保存搭配并支持对比历史搭配
  -->
  <div class="lab-container fade-in-up">

    <!-- ══════════════════════════════════════════
         页面标题
    ══════════════════════════════════════════ -->
    <div class="lab-header">
      <h1 class="lab-title">✨ 燕燕公主的搭配实验室</h1>
      <p class="lab-subtitle">点击下方衣物，快速拼出你的今日穿搭 🎀</p>
    </div>

    <!-- ══════════════════════════════════════════
         槽位区（上衣 / 裤子 / 鞋子 + 配饰）
    ══════════════════════════════════════════ -->
    <section class="slots-section card">
      <div class="slots-row">

        <!-- 上衣槽 -->
        <div class="slot" :class="{ 'slot-filled': slots.TOP }">
          <span class="slot-label">上衣</span>
          <div class="slot-content" @click="openPicker('TOP')">
            <template v-if="slots.TOP">
              <img :src="getImgSrc(slots.TOP)" :alt="slots.TOP.name" class="slot-img" />
              <!-- 清空按钮 -->
              <button class="slot-clear" @click.stop="clearSlot('TOP')" title="移除">✕</button>
              <div class="slot-name">{{ slots.TOP.name }}</div>
            </template>
            <template v-else>
              <div class="slot-empty">
                <span class="slot-icon">👕</span>
                <span class="slot-hint">点击选择上衣</span>
              </div>
            </template>
          </div>
        </div>

        <!-- 加号分隔 -->
        <div class="slot-plus">+</div>

        <!-- 裤子槽 -->
        <div class="slot" :class="{ 'slot-filled': slots.BOTTOM }">
          <span class="slot-label">下装</span>
          <div class="slot-content" @click="openPicker('BOTTOM')">
            <template v-if="slots.BOTTOM">
              <img :src="getImgSrc(slots.BOTTOM)" :alt="slots.BOTTOM.name" class="slot-img" />
              <button class="slot-clear" @click.stop="clearSlot('BOTTOM')" title="移除">✕</button>
              <div class="slot-name">{{ slots.BOTTOM.name }}</div>
            </template>
            <template v-else>
              <div class="slot-empty">
                <span class="slot-icon">👖</span>
                <span class="slot-hint">点击选择下装</span>
              </div>
            </template>
          </div>
        </div>

        <div class="slot-plus">+</div>

        <!-- 鞋子槽 -->
        <div class="slot" :class="{ 'slot-filled': slots.SHOES }">
          <span class="slot-label">鞋子</span>
          <div class="slot-content" @click="openPicker('SHOES')">
            <template v-if="slots.SHOES">
              <img :src="getImgSrc(slots.SHOES)" :alt="slots.SHOES.name" class="slot-img" />
              <button class="slot-clear" @click.stop="clearSlot('SHOES')" title="移除">✕</button>
              <div class="slot-name">{{ slots.SHOES.name }}</div>
            </template>
            <template v-else>
              <div class="slot-empty">
                <span class="slot-icon">👟</span>
                <span class="slot-hint">点击选择鞋子</span>
              </div>
            </template>
          </div>
        </div>

        <div class="slot-plus">+</div>

        <!-- 配饰槽 -->
        <div class="slot" :class="{ 'slot-filled': slots.ACCESSORY }">
          <span class="slot-label">配饰</span>
          <div class="slot-content" @click="openPicker('ACCESSORY')">
            <template v-if="slots.ACCESSORY">
              <img :src="getImgSrc(slots.ACCESSORY)" :alt="slots.ACCESSORY.name" class="slot-img" />
              <button class="slot-clear" @click.stop="clearSlot('ACCESSORY')" title="移除">✕</button>
              <div class="slot-name">{{ slots.ACCESSORY.name }}</div>
            </template>
            <template v-else>
              <div class="slot-empty">
                <span class="slot-icon">💍</span>
                <span class="slot-hint">点击选择配饰</span>
              </div>
            </template>
          </div>
        </div>

      </div>

      <!-- 操作按钮行 -->
      <div class="slots-actions">
        <button class="btn btn-ghost" @click="clearAll">🗑 清空全部</button>
        <button class="btn btn-outline" @click="randomize">🎲 随机搭配</button>
        <button class="btn btn-primary" @click="openSaveModal" :disabled="!hasAnySlot">
          💾 保存此搭配
        </button>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         衣物选择区 — 分类 Tab + 网格列表
    ══════════════════════════════════════════ -->
    <section class="wardrobe-section">
      <div class="section-title">
        <h2>🗂 我的衣橱</h2>
        <span class="item-count">共 {{ filteredClothes.length }} 件</span>
      </div>

      <!-- 分类 Tab -->
      <div class="category-tabs">
        <button
          v-for="cat in categories"
          :key="cat.value"
          class="cat-tab"
          :class="{ 'cat-tab-active': activeCategory === cat.value }"
          @click="activeCategory = cat.value"
        >
          {{ cat.icon }} {{ cat.label }}
        </button>
      </div>

      <!-- 衣物网格 -->
      <div v-if="filteredClothes.length > 0" class="clothes-grid">
        <div
          v-for="item in filteredClothes"
          :key="item.id"
          class="clothes-card card"
          :class="{ 'card-selected': isSelected(item) }"
          @click="selectItem(item)"
        >
          <!-- 已选中打勾标记 -->
          <div v-if="isSelected(item)" class="selected-badge">✓</div>

          <!-- 衣物图片 -->
          <div class="item-img-wrap">
            <img :src="getImgSrc(item)" :alt="item.name" class="item-img" />
          </div>

          <!-- 衣物信息 -->
          <div class="item-info">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-meta">
              <span class="tag tag-pink">{{ getCatLabel(item.category) }}</span>
              <span v-if="item.color" class="item-color">{{ item.color }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">🌸</div>
        <p>这里还没有衣物哦，快去"我的衣橱"添加吧~</p>
        <router-link to="/wardrobe" class="btn btn-primary" style="margin-top:16px">
          去添加衣物 →
        </router-link>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         已保存的搭配方案
    ══════════════════════════════════════════ -->
    <section v-if="savedOutfits.length > 0" class="saved-section">
      <div class="section-title">
        <h2>💖 已保存搭配</h2>
        <span class="item-count">{{ savedOutfits.length }} 套</span>
      </div>

      <!-- 横向滑动对比 -->
      <div class="saved-scroll">
        <div
          v-for="outfit in savedOutfits"
          :key="outfit.id"
          class="saved-card card"
        >
          <!-- 搭配名称 -->
          <div class="saved-name">{{ outfit.name || '我的搭配' }}</div>

          <!-- 四件衣物缩略图 -->
          <div class="saved-items">
            <div
              v-for="slot in ['top','bottom','shoes','accessory']"
              :key="slot"
              class="saved-thumb"
            >
              <img
                v-if="outfit[slot]"
                :src="getImgSrc(outfit[slot])"
                :alt="outfit[slot]?.name"
              />
              <span v-else class="saved-empty-thumb">-</span>
            </div>
          </div>

          <!-- 操作 -->
          <div class="saved-actions">
            <button class="btn btn-ghost" style="font-size:12px;padding:6px 12px"
              @click="loadOutfit(outfit)">
              🔄 应用
            </button>
            <button class="btn btn-danger" style="font-size:12px;padding:6px 12px"
              @click="removeSavedOutfit(outfit.id)">
              🗑
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         弹窗 — 快速选择某分类衣物
    ══════════════════════════════════════════ -->
    <div v-if="pickerVisible" class="modal-overlay" @click.self="pickerVisible = false">
      <div class="modal-box card fade-in-up">
        <div class="modal-header">
          <h3>选择{{ getCatLabel(pickerCategory) }}</h3>
          <button class="modal-close" @click="pickerVisible = false">✕</button>
        </div>
        <div class="picker-grid">
          <div
            v-for="item in pickerItems"
            :key="item.id"
            class="picker-item"
            @click="confirmPick(item)"
          >
            <img :src="getImgSrc(item)" :alt="item.name" />
            <span>{{ item.name }}</span>
          </div>
          <div v-if="pickerItems.length === 0" class="picker-empty">
            暂无该分类衣物 🌸
          </div>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         弹窗 — 保存搭配
    ══════════════════════════════════════════ -->
    <div v-if="saveModalVisible" class="modal-overlay" @click.self="saveModalVisible = false">
      <div class="modal-box card fade-in-up" style="max-width:420px">
        <div class="modal-header">
          <h3>💾 保存这套搭配</h3>
          <button class="modal-close" @click="saveModalVisible = false">✕</button>
        </div>
        <div class="modal-body">
          <label class="form-label">搭配名称</label>
          <input
            v-model="newOutfitName"
            class="form-input"
            placeholder="给这套搭配起个名字吧~"
            maxlength="50"
          />
          <label class="form-label" style="margin-top:12px">备注（可选）</label>
          <input
            v-model="newOutfitNote"
            class="form-input"
            placeholder="今天穿去哪里？"
            maxlength="100"
          />
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="saveModalVisible = false">取消</button>
          <button class="btn btn-primary" @click="confirmSave">保存 🎀</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getClothes, getOutfits, saveOutfit, deleteOutfit } from '../api.js'

// ─────────────────────────────────────────────────────────────
//  分类配置
// ─────────────────────────────────────────────────────────────
const categories = [
  { value: 'ALL',       label: '全部',  icon: '🌸' },
  { value: 'TOP',       label: '上衣',  icon: '👕' },
  { value: 'BOTTOM',    label: '下装',  icon: '👖' },
  { value: 'SHOES',     label: '鞋子',  icon: '👟' },
  { value: 'ACCESSORY', label: '配饰',  icon: '💍' }
]

const catMap = { TOP: '上衣', BOTTOM: '下装', SHOES: '鞋子', ACCESSORY: '配饰' }
const getCatLabel = (cat) => catMap[cat] || cat

// ─────────────────────────────────────────────────────────────
//  状态
// ─────────────────────────────────────────────────────────────

/** 全部衣物数据 */
const allClothes = ref([])

/** 当前激活的分类 Tab */
const activeCategory = ref('ALL')

/** 槽位状态：每个分类对应一件衣物（或 null） */
const slots = ref({ TOP: null, BOTTOM: null, SHOES: null, ACCESSORY: null })

/** 已保存的搭配方案 */
const savedOutfits = ref([])

// 快速选择弹窗
const pickerVisible  = ref(false)
const pickerCategory = ref('')

// 保存搭配弹窗
const saveModalVisible = ref(false)
const newOutfitName    = ref('')
const newOutfitNote    = ref('')

// ─────────────────────────────────────────────────────────────
//  计算属性
// ─────────────────────────────────────────────────────────────

/** 按当前 Tab 过滤的衣物列表 */
const filteredClothes = computed(() =>
  activeCategory.value === 'ALL'
    ? allClothes.value
    : allClothes.value.filter(c => c.category === activeCategory.value)
)

/** 快速选择弹窗的候选衣物 */
const pickerItems = computed(() =>
  allClothes.value.filter(c => c.category === pickerCategory.value)
)

/** 是否有任意槽位已选 */
const hasAnySlot = computed(() =>
  Object.values(slots.value).some(v => v !== null)
)

// ─────────────────────────────────────────────────────────────
//  生命周期
// ─────────────────────────────────────────────────────────────
// 监听衣橱数据变更事件（新增/删除衣物时由 Wardrobe 触发）
function onClothesUpdated() {
  loadClothes()
}

onMounted(() => {
  loadClothes()
  loadSavedOutfits()
  window.addEventListener('clothes-updated', onClothesUpdated)
})

onUnmounted(() => {
  window.removeEventListener('clothes-updated', onClothesUpdated)
})

// ─────────────────────────────────────────────────────────────
//  数据加载
// ─────────────────────────────────────────────────────────────
async function loadClothes() {
  try {
    const res = await getClothes()
    if (res.code === 200) allClothes.value = res.data
  } catch (e) {
    console.error('加载衣物失败', e)
  }
}

async function loadSavedOutfits() {
  try {
    const res = await getOutfits()
    if (res.code === 200) savedOutfits.value = res.data
  } catch (e) {
    console.error('加载搭配失败', e)
  }
}

// ─────────────────────────────────────────────────────────────
//  槽位操作
// ─────────────────────────────────────────────────────────────

/** 点击衣物卡片 → 放入对应分类槽位 */
function selectItem(item) {
  slots.value[item.category] = item
}

/** 判断某件衣物是否已在槽位中 */
function isSelected(item) {
  return slots.value[item.category]?.id === item.id
}

/** 清空某个槽位 */
function clearSlot(cat) {
  slots.value[cat] = null
}

/** 清空所有槽位 */
function clearAll() {
  Object.keys(slots.value).forEach(k => (slots.value[k] = null))
}

/** 随机搭配：每个分类各随机选一件 */
function randomize() {
  const catKeys = ['TOP', 'BOTTOM', 'SHOES', 'ACCESSORY']
  catKeys.forEach(cat => {
    const items = allClothes.value.filter(c => c.category === cat)
    if (items.length > 0) {
      slots.value[cat] = items[Math.floor(Math.random() * items.length)]
    }
  })
}

// ─────────────────────────────────────────────────────────────
//  快速选择弹窗
// ─────────────────────────────────────────────────────────────
function openPicker(cat) {
  pickerCategory.value = cat
  pickerVisible.value = true
}

function confirmPick(item) {
  slots.value[item.category] = item
  pickerVisible.value = false
}

// ─────────────────────────────────────────────────────────────
//  保存搭配
// ─────────────────────────────────────────────────────────────
function openSaveModal() {
  newOutfitName.value = ''
  newOutfitNote.value = ''
  saveModalVisible.value = true
}

async function confirmSave() {
  const plan = {
    name:        newOutfitName.value || '我的搭配',
    topId:       slots.value.TOP?.id       || null,
    bottomId:    slots.value.BOTTOM?.id    || null,
    shoesId:     slots.value.SHOES?.id     || null,
    accessoryId: slots.value.ACCESSORY?.id || null,
    note:        newOutfitNote.value || null
  }
  try {
    const res = await saveOutfit(plan)
    if (res.code === 200) {
      saveModalVisible.value = false
      await loadSavedOutfits()
    }
  } catch (e) {
    console.error('保存搭配失败', e)
  }
}

/** 应用某套已保存的搭配到槽位 */
function loadOutfit(outfit) {
  slots.value.TOP       = outfit.top       || null
  slots.value.BOTTOM    = outfit.bottom    || null
  slots.value.SHOES     = outfit.shoes     || null
  slots.value.ACCESSORY = outfit.accessory || null
}

/** 删除已保存搭配 */
async function removeSavedOutfit(id) {
  try {
    await deleteOutfit(id)
    savedOutfits.value = savedOutfits.value.filter(o => o.id !== id)
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ─────────────────────────────────────────────────────────────
//  工具方法
// ─────────────────────────────────────────────────────────────

/**
 * 获取图片 src
 * - 有 imageUrl 则返回真实路径
 * - 否则返回 emoji 占位（用 SVG data-uri）
 */
function getImgSrc(item) {
  if (!item) return ''
  if (item.imageUrl) return item.imageUrl

  const icons = { TOP: '👕', BOTTOM: '👖', SHOES: '👟', ACCESSORY: '💍' }
  const emoji = icons[item.category] || '👗'
  // 返回 SVG 占位图
  return `data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect width='100' height='100' fill='%23fff0f5' rx='12'/><text y='62' x='50' font-size='52' text-anchor='middle'>${emoji}</text></svg>`
}
</script>

<style scoped>
/* ================================================================
   搭配实验室 — 页面专属样式
   ================================================================ */

.lab-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* ─── 页面标题 ─────────────────── */
.lab-header {
  text-align: center;
}

.lab-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
}

.lab-subtitle {
  color: var(--text-muted);
  font-size: 15px;
}

/* ─── 槽位区 ─────────────────────── */
.slots-section {
  padding: 28px 24px 20px;
}

.slots-row {
  display: flex;
  align-items: stretch;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.slot-plus {
  display: flex;
  align-items: center;
  font-size: 22px;
  color: var(--pink-200);
  font-weight: 300;
  padding-top: 20px;  /* 对齐内容区 */
}

.slot {
  flex: 1;
  min-width: 140px;
  max-width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.slot-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 0.5px;
}

.slot-content {
  position: relative;
  width: 100%;
  aspect-ratio: 3/4;
  border-radius: var(--radius-md);
  border: 2px dashed var(--pink-200);
  background: var(--pink-50);
  cursor: pointer;
  overflow: hidden;
  transition: var(--transition);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.slot-content:hover {
  border-color: var(--pink-400);
  background: var(--pink-100);
  transform: scale(1.02);
}

.slot-filled .slot-content {
  border-style: solid;
  border-color: var(--pink-300);
  background: white;
}

.slot-img {
  width: 100%;
  height: calc(100% - 28px);
  object-fit: cover;
}

.slot-name {
  width: 100%;
  padding: 4px 8px;
  font-size: 11px;
  color: var(--text-secondary);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: rgba(255,255,255,0.9);
}

.slot-clear {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  border: 1px solid var(--border);
  font-size: 11px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  z-index: 1;
  transition: var(--transition-fast);
}

.slot-clear:hover {
  background: #fee2e2;
  color: #e53e3e;
  border-color: #fecaca;
}

.slot-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  pointer-events: none;
}

.slot-icon  { font-size: 32px; }
.slot-hint  { font-size: 12px; color: var(--text-muted); }

/* 槽位操作按钮行 */
.slots-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
  flex-wrap: wrap;
}

/* ─── 衣物选择区 ─────────────────── */
.wardrobe-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.item-count {
  font-size: 13px;
  color: var(--text-muted);
  background: var(--pink-100);
  padding: 2px 10px;
  border-radius: 999px;
}

/* 分类 Tab */
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

/* 衣物网格 */
.clothes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.clothes-card {
  position: relative;
  cursor: pointer;
  overflow: hidden;
  border-radius: var(--radius-md);
  transition: var(--transition);
}

.clothes-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: var(--shadow-md);
}

.card-selected {
  border: 2.5px solid var(--pink-400) !important;
  box-shadow: 0 0 0 4px rgba(255, 105, 135, 0.15) !important;
}

.selected-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--pink-400);
  color: white;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  box-shadow: 0 2px 6px rgba(255, 77, 109, 0.4);
}

.item-img-wrap {
  width: 100%;
  aspect-ratio: 1/1;
  overflow: hidden;
  background: var(--pink-50);
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.clothes-card:hover .item-img {
  transform: scale(1.06);
}

.item-info {
  padding: 8px 10px 10px;
}

.item-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 5px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.item-color {
  font-size: 11px;
  color: var(--text-muted);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* ─── 已保存搭配 ─────────────────── */
.saved-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.saved-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 12px;
  scrollbar-width: thin;
  scrollbar-color: var(--pink-200) transparent;
}

.saved-scroll::-webkit-scrollbar { height: 6px; }
.saved-scroll::-webkit-scrollbar-track { background: transparent; }
.saved-scroll::-webkit-scrollbar-thumb { background: var(--pink-200); border-radius: 3px; }

.saved-card {
  flex-shrink: 0;
  width: 220px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.saved-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.saved-items {
  display: flex;
  gap: 6px;
}

.saved-thumb {
  flex: 1;
  aspect-ratio: 1/1;
  border-radius: 8px;
  overflow: hidden;
  background: var(--pink-50);
  display: flex;
  align-items: center;
  justify-content: center;
}

.saved-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.saved-empty-thumb {
  font-size: 18px;
  color: var(--text-muted);
}

.saved-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* ─── 弹窗通用 ─────────────────── */
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
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  padding: 0;
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

.modal-close:hover {
  background: var(--pink-200);
}

.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border);
}

/* 快速选择网格 */
.picker-grid {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.picker-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--border);
  background: var(--pink-50);
  transition: var(--transition-fast);
}

.picker-item:hover {
  border-color: var(--pink-400);
  background: var(--pink-100);
  transform: scale(1.04);
}

.picker-item img {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 8px;
}

.picker-item span {
  font-size: 11px;
  color: var(--text-secondary);
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 90px;
}

.picker-empty {
  grid-column: 1/-1;
  text-align: center;
  padding: 32px;
  color: var(--text-muted);
}

/* ─── 表单样式 ─────────────────── */
.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
  display: block;
}

.form-input {
  width: 100%;
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
  box-shadow: 0 0 0 3px rgba(255, 105, 135, 0.12);
}

/* ─── 响应式 ─────────────────── */
@media (max-width: 600px) {
  .slots-row {
    gap: 6px;
  }

  .slot {
    min-width: 80px;
  }

  .slot-plus {
    font-size: 16px;
    padding-top: 16px;
  }

  .clothes-grid {
    grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
    gap: 10px;
  }
}
</style>
