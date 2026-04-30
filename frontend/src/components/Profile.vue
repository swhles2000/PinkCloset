<template>
  <div class="profile-wrap fade-in-up">
    <!-- 个人信息卡片 -->
    <div class="profile-card card">
      <div class="profile-header">
        <div class="avatar-wrapper" @click="triggerUpload">
          <img v-if="user.avatar" :src="user.avatar" class="avatar-img" alt="头像" />
          <div v-else class="avatar-circle">🌸</div>
          <div class="avatar-overlay">
            <span>📷</span>
          </div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            class="avatar-file-input"
            @change="handleAvatarChange"
          />
        </div>
        <div class="profile-info">
          <h2 class="profile-name">{{ user.name }}</h2>
          <p class="profile-sub">{{ user.phone || user.email }}</p>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">姓名</span>
          <span class="info-value">{{ user.name || '—' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">性别</span>
          <span class="info-value">{{ user.gender || '未填写' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">年龄</span>
          <span class="info-value">{{ user.age || '未填写' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">手机号</span>
          <span class="info-value">{{ user.phone || '未绑定' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">邮箱</span>
          <span class="info-value">{{ user.email || '未绑定' }}</span>
        </div>
      </div>
    </div>

    <!-- 危险操作区 -->
    <div class="danger-zone card">
      <h3 class="danger-title">⚠️ 危险操作</h3>
      <p class="danger-desc">
        注销账号后，你的所有个人信息将被<strong>永久删除</strong>，且无法恢复。请谨慎操作。
      </p>
      <button class="btn btn-deactivate" @click="showConfirm = true">
        🗑 注销我的账号
      </button>
    </div>

    <!-- 确认弹窗 -->
    <Teleport to="body">
      <div v-if="showConfirm" class="modal-mask" @click.self="showConfirm = false">
        <div class="modal-box fade-in-up">
          <div class="modal-icon">⚠️</div>
          <h3 class="modal-title">确认注销账号？</h3>
          <p class="modal-desc">
            此操作将<strong>永久删除</strong>你在粉色衣橱的所有数据，包括账号信息、衣物记录和搭配方案，且<strong>无法恢复</strong>。
          </p>
          <p class="modal-confirm-hint">请在下方输入 <code>注销账号</code> 来确认操作：</p>
          <input
            v-model="confirmInput"
            class="confirm-input"
            placeholder="请输入：注销账号"
            type="text"
          />
          <div v-if="errMsg" class="err-msg">⚠️ {{ errMsg }}</div>
          <div class="modal-actions">
            <button class="btn btn-outline" @click="showConfirm = false; confirmInput = ''; errMsg = ''">
              取消
            </button>
            <button
              class="btn btn-deactivate"
              :disabled="loading || confirmInput !== '注销账号'"
              @click="handleDeactivate"
            >
              <span v-if="loading">注销中...</span>
              <span v-else>确认注销</span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { deleteAccount, uploadAvatar } from '../api.js'

const router       = useRouter()
const showConfirm  = ref(false)
const confirmInput = ref('')
const loading      = ref(false)
const errMsg       = ref('')
const avatarInput  = ref(null)

// 从 localStorage 读取当前用户
const user = ref({ name: '', phone: '', email: '', gender: '', age: '', avatar: '' })
onMounted(() => {
  const raw = localStorage.getItem('pc_user')
  if (raw) {
    user.value = JSON.parse(raw)
  } else {
    router.replace('/login')
  }
})

// 头像上传
function triggerUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // 校验文件大小 5MB
  if (file.size > 5 * 1024 * 1024) {
    alert('头像图片不能超过 5MB')
    e.target.value = ''
    return
  }

  try {
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      // 更新本地用户数据
      user.value.avatar = res.data
      localStorage.setItem('pc_user', JSON.stringify(user.value))
      // 通知 App.vue 更新头像
      window.dispatchEvent(new CustomEvent('user-avatar-updated', { detail: res.data }))
    } else {
      alert(res.message || '头像上传失败')
    }
  } catch (err) {
    console.error('头像上传出错', err)
    alert('头像上传失败，请稍后重试')
  }
  // 清空 input 以允许重复选择同一文件
  e.target.value = ''
}

async function handleDeactivate() {
  if (confirmInput.value !== '注销账号') return
  loading.value = true
  errMsg.value  = ''
  try {
    const res = await deleteAccount(user.value.id)
    if (res.code === 200) {
      localStorage.removeItem('pc_user')
      // 通知 App.vue 更新用户状态
      window.dispatchEvent(new CustomEvent('user-logout'))
      router.replace('/login')
    } else {
      errMsg.value = res.message || '注销失败，请稍后重试'
    }
  } catch (e) {
    errMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-wrap {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 个人信息卡片 */
.profile-card {
  padding: 32px 28px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border);
}
.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pink-200), var(--pink-100));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  box-shadow: 0 4px 12px rgba(255,105,135,0.2);
  flex-shrink: 0;
}
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
}
.avatar-wrapper .avatar-circle,
.avatar-wrapper .avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(255,105,135,0.2);
  object-fit: cover;
  transition: transform 0.2s ease;
}
.avatar-wrapper:hover .avatar-circle,
.avatar-wrapper:hover .avatar-img {
  transform: scale(1.05);
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.avatar-overlay span {
  font-size: 24px;
  filter: brightness(2);
}
.avatar-file-input {
  display: none;
}
.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}
.profile-sub {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}
.info-value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 600;
}

/* 危险区 */
.danger-zone {
  padding: 24px 28px;
  border-color: #fecaca !important;
  background: #fff8f8;
}
.danger-title {
  font-size: 16px;
  font-weight: 700;
  color: #c53030;
  margin-bottom: 10px;
}
.danger-desc {
  font-size: 13px;
  color: #7a5c68;
  line-height: 1.7;
  margin-bottom: 16px;
}
.btn-deactivate {
  background: #fff0f0;
  color: #e53e3e;
  border: 1.5px solid #fecaca;
  border-radius: 999px;
  padding: 9px 22px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}
.btn-deactivate:hover:not(:disabled) {
  background: #fee2e2;
  border-color: #fc8181;
}
.btn-deactivate:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 确认弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(61, 44, 53, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}
.modal-box {
  background: white;
  border-radius: 20px;
  padding: 36px 32px 28px;
  max-width: 420px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  text-align: center;
}
.modal-icon {
  font-size: 44px;
  margin-bottom: 12px;
}
.modal-title {
  font-size: 20px;
  font-weight: 700;
  color: #c53030;
  margin-bottom: 12px;
}
.modal-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  text-align: left;
  margin-bottom: 16px;
}
.modal-confirm-hint {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 8px;
  text-align: left;
}
.modal-confirm-hint code {
  background: var(--pink-50);
  color: var(--pink-dark);
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 700;
}
.confirm-input {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid var(--border);
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.15s;
  text-align: center;
  letter-spacing: 2px;
}
.confirm-input:focus {
  border-color: #fc8181;
  box-shadow: 0 0 0 3px rgba(252,129,129,0.15);
}
.err-msg {
  background: #fff0f0;
  color: #e53e3e;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  margin-top: 8px;
  text-align: left;
}
.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 480px) {
  .info-grid { grid-template-columns: 1fr; }
  .modal-box { padding: 28px 20px 24px; }
}
</style>
