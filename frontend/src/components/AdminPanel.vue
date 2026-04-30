<template>
  <div class="admin-wrap fade-in-up">

    <!-- 顶部信息栏 -->
    <div class="admin-header">
      <div>
        <h1 class="admin-title">🛡️ 管理员控制台</h1>
        <p class="admin-sub">PinkCloset 用户管理面板</p>
      </div>
      <div class="admin-actions">
        <span class="admin-badge">{{ adminUser.username }}</span>
        <button class="btn-admin-logout" @click="handleLogout">退出管理</button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-row">
      <div class="stat-box">
        <div class="stat-num">{{ users.length }}</div>
        <div class="stat-label">总用户数</div>
      </div>
      <div class="stat-box stat-active">
        <div class="stat-num">{{ activeCount }}</div>
        <div class="stat-label">正常用户</div>
      </div>
      <div class="stat-box stat-deleted">
        <div class="stat-num">{{ frozenCount }}</div>
        <div class="stat-label">已冻结</div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-table-wrap">
      <table class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>性别</th>
            <th>年龄</th>
            <th>手机号</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>衣物数</th>
            <th>搭配数</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id" :class="{ 'row-deleted': u.deleted === 1 }">
            <td>{{ u.id }}</td>
            <td><strong>{{ u.name }}</strong></td>
            <td>{{ u.gender || '—' }}</td>
            <td>{{ u.age || '—' }}</td>
            <td>{{ u.phone }}</td>
            <td>{{ u.email || '—' }}</td>
            <td>
              <span :class="['status-badge', u.deleted ? 'status-off' : 'status-on']">
                {{ u.deleted ? '已冻结' : '正常' }}
              </span>
            </td>
            <td class="time-cell">{{ formatTime(u.createTime) }}</td>
            <td>
              <button class="btn-view" @click="viewClothes(u)">
                📦 {{ clothesCountMap[u.id] ?? '...' }}
              </button>
            </td>
            <td>
              <button class="btn-view" @click="viewOutfits(u)" style="background:#fce7f3;color:#db2777">
                💖 {{ outfitsCountMap[u.id] ?? '...' }}
              </button>
            </td>
            <td class="action-cell">
              <button
                v-if="u.deleted !== 1"
                class="btn-freeze"
                @click="confirmFreeze(u)"
              >冻结</button>
              <button
                v-if="u.deleted === 1"
                class="btn-restore"
                @click="restoreUser(u.id)"
              >恢复</button>
              <button
                v-if="u.deleted === 1"
                class="btn-hard-delete"
                @click="confirmHardDelete(u)"
              >彻底删除</button>
            </td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="11" class="empty-row">暂无用户数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 衣物查看弹窗 -->
    <Teleport to="body">
      <div v-if="showClothesModal" class="modal-mask" @click.self="showClothesModal = false">
        <div class="modal-box fade-in-up">
          <div class="modal-head">
            <h3>📦 {{ viewingUser?.name }} 的衣物（{{ viewingClothes.length }} 件 / 已删除 {{ deletedClothesCount }} 件）</h3>
            <button class="modal-close" @click="showClothesModal = false">✕</button>
          </div>
          <div class="clothes-grid">
            <div v-for="item in viewingClothes" :key="item.id" class="clothes-card" :class="{ 'clothes-deleted': item.deleted === 1 }">
              <div class="clothes-img-wrap">
                <img
                  v-if="item.imageUrl"
                  :src="item.imageUrl"
                  :alt="item.name"
                  class="clothes-img"
                  :class="{ 'img-grey': item.deleted === 1 }"
                />
                <div v-else class="clothes-img clothes-img-placeholder">👗</div>
                <!-- 已删除标签 -->
                <span v-if="item.deleted === 1" class="deleted-badge">已删除</span>
              </div>
              <div class="clothes-info">
                <div class="clothes-name">{{ item.name }}</div>
                <div class="clothes-meta">
                  <span :class="['cat-tag', 'cat-' + item.category?.toLowerCase()]">{{ item.category }}</span>
                  <span v-if="item.color" class="color-tag">{{ item.color }}</span>
                </div>
                <!-- 操作按钮：始终显示 -->
                <div class="clothes-actions">
                  <button v-if="item.deleted !== 1" class="btn-soft-delete" @click="softDeleteClothing(item)">删除</button>
                  <template v-else>
                    <button class="btn-restore" @click="restoreClothing(item.id)">恢复</button>
                    <button class="btn-hard-delete" @click="confirmHardDeleteClothing(item)">彻底删除</button>
                  </template>
                </div>
              </div>
            </div>
          </div>
          <p v-if="viewingClothes.length === 0" class="empty-clothes">该用户暂无衣物数据</p>
        </div>
      </div>
    </Teleport>

    <!-- 搭配方案查看弹窗 -->
    <Teleport to="body">
      <div v-if="showOutfitsModal" class="modal-mask" @click.self="showOutfitsModal = false">
        <div class="modal-box fade-in-up">
          <div class="modal-head">
            <h3>💖 {{ viewingUser?.name }} 的搭配方案（{{ viewingOutfits.length }} 套 / 已删除 {{ deletedOutfitsCount }} 套）</h3>
            <button class="modal-close" @click="showOutfitsModal = false">✕</button>
          </div>
          <div class="outfits-list">
            <div v-for="outfit in viewingOutfits" :key="outfit.id" class="outfit-card" :class="{ 'outfit-deleted': outfit.deleted === 1 }">
              <div class="outfit-header">
                <span class="outfit-name">{{ outfit.name || '我的搭配' }}</span>
                <span v-if="outfit.deleted === 1" class="deleted-badge">已删除</span>
              </div>
              <div class="outfit-meta">
                <span v-if="outfit.note" class="outfit-note">{{ outfit.note }}</span>
                <span class="outfit-date">{{ formatTime(outfit.createTime) }}</span>
              </div>
              <!-- 操作按钮：始终显示 -->
              <div class="clothes-actions" style="margin-top:8px">
                <button v-if="outfit.deleted !== 1" class="btn-soft-delete" @click="softDeleteOutfit(outfit)">删除</button>
                <template v-else>
                  <button class="btn-restore" @click="restoreOutfit(outfit.id)">恢复</button>
                  <button class="btn-hard-delete" @click="confirmHardDeleteOutfit(outfit)">彻底删除</button>
                </template>
              </div>
            </div>
          </div>
          <p v-if="viewingOutfits.length === 0" class="empty-clothes">该用户暂无搭配方案</p>
        </div>
      </div>
    </Teleport>

    <!-- 冻结确认弹窗 -->
    <Teleport to="body">
      <div v-if="showFreezeConfirm" class="modal-mask" @click.self="showFreezeConfirm = false">
        <div class="modal-box modal-small fade-in-up">
          <h3 class="modal-warn-title">🧊 确认冻结用户？</h3>
          <p class="modal-desc">
            即将冻结用户 <strong>{{ freezingUser?.name }}</strong>（ID: {{ freezingUser?.id }}）。
          </p>
          <p class="modal-desc">
            冻结后该用户将 <strong>无法登录</strong> 系统使用。
          </p>
          <p class="modal-hint">冻结可通过「恢复」操作撤销。</p>
          <div class="modal-btns">
            <button class="btn-cancel" @click="showFreezeConfirm = false">取消</button>
            <button class="btn-confirm-freeze" @click="freezeUser" :disabled="freezeLoading">
              {{ freezeLoading ? '冻结中...' : '确认冻结' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 彻底删除确认弹窗 -->
    <Teleport to="body">
      <div v-if="showDeleteConfirm" class="modal-mask" @click.self="showDeleteConfirm = false">
        <div class="modal-box modal-small fade-in-up">
          <h3 class="modal-danger-title">⚠️ 确认彻底删除？</h3>
          <p class="modal-desc">
            {{ deleteConfirmMessage }}
          </p>
          <p class="modal-warn">此操作 <strong>不可恢复</strong>！</p>
          <div class="modal-btns">
            <button class="btn-cancel" @click="showDeleteConfirm = false">取消</button>
            <button class="btn-confirm-delete" @click="executeHardDelete" :disabled="deleteLoading">
              {{ deleteLoading ? '删除中...' : '确认彻底删除' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminGetUsers, adminGetUserClothes, adminGetUserOutfits, adminFreezeUser, adminRestoreUser, adminHardDeleteUser, adminSoftDeleteClothing, adminRestoreClothing, adminHardDeleteClothing, adminSoftDeleteOutfit, adminRestoreOutfit, adminHardDeleteOutfit } from '../api.js'

const router = useRouter()

const adminUser = ref({ username: '' })
const users = ref([])
const clothesCountMap = ref({})
const outfitsCountMap = ref({})
const showClothesModal = ref(false)
const viewingUser = ref(null)
const viewingClothes = ref([])
const showOutfitsModal = ref(false)
const viewingOutfits = ref([])
const showDeleteConfirm = ref(false)
const deleteConfirmMessage = ref('')
const deleteConfirmType = ref('') // 'user' | 'clothing' | 'outfit'
const deletingUser = ref(null)
const deletingClothing = ref(null)
const deletingOutfit = ref(null)
const deleteLoading = ref(false)
const showFreezeConfirm = ref(false)
const freezingUser = ref(null)
const freezeLoading = ref(false)

const activeCount = computed(() => users.value.filter(u => u.deleted !== 1).length)
const frozenCount = computed(() => users.value.filter(u => u.deleted === 1).length)
const deletedClothesCount = computed(() => viewingClothes.value.filter(i => i.deleted === 1).length)
const deletedOutfitsCount = computed(() => viewingOutfits.value.filter(o => o.deleted === 1).length)

onMounted(async () => {
  const raw = localStorage.getItem('pc_admin')
  if (!raw) {
    router.replace('/admin/login')
    return
  }
  adminUser.value = JSON.parse(raw)
  await loadUsers()
})

async function loadUsers() {
  try {
    const res = await adminGetUsers()
    if (res.code === 200) {
      users.value = res.data || []
      // 加载每个用户的衣物数量和搭配方案数量
      for (const u of users.value) {
        try {
          const cRes = await adminGetUserClothes(u.id)
          clothesCountMap.value[u.id] = (cRes.data || []).length
        } catch {
          clothesCountMap.value[u.id] = 0
        }
        try {
          const oRes = await adminGetUserOutfits(u.id)
          outfitsCountMap.value[u.id] = (oRes.data || []).length
        } catch {
          outfitsCountMap.value[u.id] = 0
        }
      }
    }
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
}

async function viewClothes(user) {
  viewingUser.value = user
  try {
    const res = await adminGetUserClothes(user.id)
    viewingClothes.value = (res.code === 200) ? (res.data || []) : []
  } catch {
    viewingClothes.value = []
  }
  showClothesModal.value = true
}

async function viewOutfits(user) {
  viewingUser.value = user
  try {
    const res = await adminGetUserOutfits(user.id)
    viewingOutfits.value = (res.code === 200) ? (res.data || []) : []
  } catch {
    viewingOutfits.value = []
  }
  showOutfitsModal.value = true
}

async function restoreClothing(id) {
  try {
    const res = await adminRestoreClothing(id)
    if (res.code === 200) {
      // 刷新当前弹窗的衣物列表
      if (viewingUser.value) {
        const cRes = await adminGetUserClothes(viewingUser.value.id)
        viewingClothes.value = (cRes.code === 200) ? (cRes.data || []) : []
        clothesCountMap.value[viewingUser.value.id] = viewingClothes.value.length
      }
    } else {
      alert(res.message || '恢复失败')
    }
  } catch {
    alert('操作失败')
  }
}

async function softDeleteClothing(item) {
  try {
    const res = await adminSoftDeleteClothing(item.id)
    if (res.code === 200) {
      // 刷新当前弹窗的衣物列表
      if (viewingUser.value) {
        const cRes = await adminGetUserClothes(viewingUser.value.id)
        viewingClothes.value = (cRes.code === 200) ? (cRes.data || []) : []
        clothesCountMap.value[viewingUser.value.id] = viewingClothes.value.length
      }
    } else {
      alert(res.message || '删除失败')
    }
  } catch {
    alert('操作失败')
  }
}

async function restoreOutfit(id) {
  try {
    const res = await adminRestoreOutfit(id)
    if (res.code === 200) {
      // 刷新当前弹窗的搭配方案列表
      if (viewingUser.value) {
        const oRes = await adminGetUserOutfits(viewingUser.value.id)
        viewingOutfits.value = (oRes.code === 200) ? (oRes.data || []) : []
        outfitsCountMap.value[viewingUser.value.id] = viewingOutfits.value.length
      }
    } else {
      alert(res.message || '恢复失败')
    }
  } catch {
    alert('操作失败')
  }
}

async function softDeleteOutfit(outfit) {
  try {
    const res = await adminSoftDeleteOutfit(outfit.id)
    if (res.code === 200) {
      // 刷新当前弹窗的搭配方案列表
      if (viewingUser.value) {
        const oRes = await adminGetUserOutfits(viewingUser.value.id)
        viewingOutfits.value = (oRes.code === 200) ? (oRes.data || []) : []
        outfitsCountMap.value[viewingUser.value.id] = viewingOutfits.value.length
      }
    } else {
      alert(res.message || '删除失败')
    }
  } catch {
    alert('操作失败')
  }
}

function confirmFreeze(user) {
  freezingUser.value = user
  showFreezeConfirm.value = true
}

async function freezeUser() {
  if (!freezingUser.value) return
  freezeLoading.value = true
  try {
    const res = await adminFreezeUser(freezingUser.value.id)
    if (res.code === 200) {
      showFreezeConfirm.value = false
      freezingUser.value = null
      await loadUsers()
    } else {
      alert(res.message || '冻结失败')
    }
  } catch {
    alert('操作失败')
  } finally {
    freezeLoading.value = false
  }
}

async function restoreUser(id) {
  try {
    const res = await adminRestoreUser(id)
    if (res.code === 200) {
      await loadUsers()
    } else {
      alert(res.message || '恢复失败')
    }
  } catch (e) {
    alert('操作失败')
  }
}

function confirmHardDelete(user) {
  deletingUser.value = user
  deleteConfirmType.value = 'user'
  deleteConfirmMessage.value = `即将彻底删除用户 ${user.name}（ID: ${user.id}）的所有数据，包括衣物记录、搭配方案和账号信息。`
  showDeleteConfirm.value = true
}

function confirmHardDeleteClothing(item) {
  deletingClothing.value = item
  deleteConfirmType.value = 'clothing'
  deleteConfirmMessage.value = `即将彻底删除衣物「${item.name}」（ID: ${item.id}），此操作不可恢复。`
  showDeleteConfirm.value = true
}

function confirmHardDeleteOutfit(outfit) {
  deletingOutfit.value = outfit
  deleteConfirmType.value = 'outfit'
  deleteConfirmMessage.value = `即将彻底删除搭配方案「${outfit.name || '我的搭配'}」（ID: ${outfit.id}），此操作不可恢复。`
  showDeleteConfirm.value = true
}

async function executeHardDelete() {
  deleteLoading.value = true
  try {
    if (deleteConfirmType.value === 'user') {
      const res = await adminHardDeleteUser(deletingUser.value.id)
      if (res.code === 200) {
        showDeleteConfirm.value = false
        deletingUser.value = null
        await loadUsers()
      } else {
        alert(res.message || '删除失败')
      }
    } else if (deleteConfirmType.value === 'clothing') {
      const res = await adminHardDeleteClothing(deletingClothing.value.id)
      if (res.code === 200) {
        // 刷新衣物列表
        if (viewingUser.value) {
          const cRes = await adminGetUserClothes(viewingUser.value.id)
          viewingClothes.value = (cRes.code === 200) ? (cRes.data || []) : []
          clothesCountMap.value[viewingUser.value.id] = viewingClothes.value.length
        }
        showDeleteConfirm.value = false
        deletingClothing.value = null
      } else {
        alert(res.message || '删除失败')
      }
    } else if (deleteConfirmType.value === 'outfit') {
      const res = await adminHardDeleteOutfit(deletingOutfit.value.id)
      if (res.code === 200) {
        // 刷新搭配方案列表
        if (viewingUser.value) {
          const oRes = await adminGetUserOutfits(viewingUser.value.id)
          viewingOutfits.value = (oRes.code === 200) ? (oRes.data || []) : []
          outfitsCountMap.value[viewingUser.value.id] = viewingOutfits.value.length
        }
        showDeleteConfirm.value = false
        deletingOutfit.value = null
      } else {
        alert(res.message || '删除失败')
      }
    }
  } catch {
    alert('操作失败')
  } finally {
    deleteLoading.value = false
  }
}

function handleLogout() {
  localStorage.removeItem('pc_admin')
  router.replace('/admin/login')
}

function formatTime(t) {
  if (!t) return '—'
  const d = new Date(t)
  return d.getFullYear() + '-' +
    String(d.getMonth() + 1).padStart(2, '0') + '-' +
    String(d.getDate()).padStart(2, '0') + ' ' +
    String(d.getHours()).padStart(2, '0') + ':' +
    String(d.getMinutes()).padStart(2, '0')
}
</script>

<style scoped>
.admin-wrap {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.admin-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}
.admin-sub {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 4px;
}
.admin-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-badge {
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  color: white;
  padding: 4px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}
.btn-admin-logout {
  padding: 6px 16px;
  border-radius: 8px;
  border: 1.5px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-admin-logout:hover {
  border-color: #f87171;
  color: #ef4444;
}

/* 统计 */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}
.stat-box {
  flex: 1;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}
.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
}
.stat-label {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 4px;
}
.stat-active .stat-num { color: #16a34a; }
.stat-deleted .stat-num { color: #dc2626; }

/* 表格 */
.user-table-wrap {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow-x: auto;
}
.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.user-table th {
  background: #f8fafc;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #64748b;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
  border-bottom: 1px solid #e2e8f0;
}
.user-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
  white-space: nowrap;
}
.row-deleted {
  background: #fef2f2 !important;
  opacity: 0.7;
}
.time-cell { font-size: 12px; color: #94a3b8; }
.action-cell { white-space: nowrap; }

/* 状态标签 */
.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.status-on {
  background: #dcfce7;
  color: #16a34a;
}
.status-off {
  background: #fee2e2;
  color: #dc2626;
}

/* 操作按钮 */
.btn-view {
  background: #eff6ff;
  color: #2563eb;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.btn-view:hover { background: #dbeafe; }
.btn-restore {
  background: #ecfdf5;
  color: #059669;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  margin-right: 6px;
}
.btn-restore:hover { background: #d1fae5; }
.btn-freeze {
  background: #fff7ed;
  color: #ea580c;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  margin-right: 6px;
}
.btn-freeze:hover { background: #ffedd5; }
.btn-hard-delete {
  background: #fef2f2;
  color: #dc2626;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.btn-hard-delete:hover { background: #fee2e2; }
.btn-soft-delete {
  background: #fef2f2;
  color: #dc2626;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  margin-right: 6px;
}
.btn-soft-delete:hover { background: #fee2e2; }
.empty-row {
  text-align: center;
  color: #94a3b8;
  padding: 40px !important;
}

/* 弹窗通用 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}
.modal-box {
  background: white;
  border-radius: 16px;
  padding: 24px;
  max-width: 800px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}
.modal-small { max-width: 460px; }
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.modal-head h3 { font-size: 18px; color: #1e293b; }
.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #94a3b8;
  padding: 4px;
}

/* 衣物弹窗 */
.clothes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}
.clothes-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.15s;
}
.clothes-card:hover { transform: translateY(-2px); }
.clothes-img {
  width: 100%;
  height: 140px;
  object-fit: cover;
  background: #f8fafc;
}
.clothes-img-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: linear-gradient(135deg, #fce7f3, #f0f9ff);
}
.clothes-info { padding: 10px 12px; }
.clothes-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.clothes-meta { display: flex; gap: 6px; }
.cat-tag {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 600;
  text-transform: uppercase;
}
.cat-top { background: #dbeafe; color: #2563eb; }
.cat-bottom { background: #dcfce7; color: #16a34a; }
.cat-shoes { background: #fef3c7; color: #d97706; }
.cat-accessory { background: #fce7f3; color: #db2777; }
.color-tag { font-size: 11px; color: #94a3b8; }
.empty-clothes {
  text-align: center;
  color: #94a3b8;
  padding: 30px;
  font-size: 14px;
}

/* 已删除衣物样式 */
.clothes-deleted {
  border: 1px solid #fecaca !important;
  background: #fef2f2 !important;
}
.clothes-deleted:hover {
  transform: translateY(-2px);
}
.clothes-img-wrap {
  position: relative;
}
.img-grey {
  filter: grayscale(100%) opacity(0.5);
}
.deleted-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  background: rgba(220, 38, 38, 0.85);
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
  z-index: 1;
}
.clothes-actions {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

/* 搭配方案弹窗 */
.outfits-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.outfit-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px 16px;
  transition: background 0.15s;
}
.outfit-card:hover { background: #f8fafc; }
.outfit-deleted {
  border-color: #fecaca !important;
  background: #fef2f2 !important;
}
.outfit-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.outfit-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}
.outfit-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}
.outfit-note {
  font-size: 13px;
  color: #64748b;
}
.outfit-date {
  font-size: 12px;
  color: #94a3b8;
}

/* 删除确认弹窗 */
.modal-danger-title {
  font-size: 18px;
  font-weight: 700;
  color: #dc2626;
  margin-bottom: 12px;
}

/* 冻结确认弹窗 */
.modal-warn-title {
  font-size: 18px;
  font-weight: 700;
  color: #ea580c;
  margin-bottom: 12px;
}
.modal-hint {
  font-size: 13px;
  color: #16a34a;
  margin-top: 8px;
}
.modal-desc {
  font-size: 14px;
  color: #475569;
  line-height: 1.7;
}
.delete-list {
  margin: 12px 0;
  padding-left: 20px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}
.modal-warn {
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
  margin-top: 8px;
}
.modal-btns {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}
.btn-cancel {
  padding: 8px 20px;
  border-radius: 8px;
  border: 1.5px solid #e2e8f0;
  background: white;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
}
.btn-confirm-delete {
  padding: 8px 20px;
  border-radius: 8px;
  border: none;
  background: #dc2626;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.btn-confirm-delete:hover { background: #b91c1c; }
.btn-confirm-delete:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-confirm-freeze {
  padding: 8px 20px;
  border-radius: 8px;
  border: none;
  background: #ea580c;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.btn-confirm-freeze:hover { background: #c2410c; }
.btn-confirm-freeze:disabled { opacity: 0.5; cursor: not-allowed; }

.fade-in-up {
  animation: fadeInUp 0.4s ease both;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
