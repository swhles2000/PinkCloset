<template>
  <div class="auth-bg">
    <div class="auth-card fade-in-up">
      <!-- Logo -->
      <div class="auth-logo">
        <span class="logo-icon">🛡️</span>
        <h1 class="logo-title">超级管理员</h1>
        <p class="logo-sub">PinkCloset Admin Console</p>
      </div>

      <h2 class="auth-heading">管理员登录 🔐</h2>
      <p class="auth-desc">仅限授权管理员使用</p>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="field-group">
          <label class="field-label">管理员账号</label>
          <input v-model="form.username" class="field-input" type="text" placeholder="请输入管理员账号" autocomplete="username" />
        </div>

        <div class="field-group">
          <label class="field-label">密码</label>
          <div class="pwd-wrap">
            <input
              v-model="form.password"
              class="field-input"
              :type="showPwd ? 'text' : 'password'"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
            <span class="pwd-eye" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁' }}</span>
          </div>
        </div>

        <div v-if="errMsg" class="err-msg">⚠️ {{ errMsg }}</div>

        <button class="btn btn-primary auth-submit" type="submit" :disabled="loading">
          <span v-if="loading" class="spin">⏳</span>
          <span v-else>登 录</span>
        </button>
      </form>

      <div class="auth-footer">
        <router-link to="/login" class="link-pink">← 返回普通用户登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../api.js'

const router  = useRouter()
const loading = ref(false)
const showPwd = ref(false)
const errMsg  = ref('')

const form = ref({
  username: '',
  password: ''
})

async function handleLogin() {
  errMsg.value = ''
  if (!form.value.username.trim()) { errMsg.value = '请输入管理员账号'; return }
  if (!form.value.password) { errMsg.value = '请输入密码'; return }

  loading.value = true
  try {
    const res = await adminLogin({
      username: form.value.username.trim(),
      password: form.value.password
    })
    if (res.code === 200) {
      localStorage.setItem('pc_admin', JSON.stringify(res.data))
      router.push('/admin')
    } else {
      errMsg.value = res.message || '登录失败'
    }
  } catch (e) {
    errMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-bg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%) !important;
}
.auth-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.3);
  padding: 40px 40px 36px;
  width: 100%;
  max-width: 420px;
}
.auth-logo {
  text-align: center;
  margin-bottom: 24px;
}
.logo-icon { font-size: 48px; display: block; }
.logo-title {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #1a1a2e, #0f3460);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-top: 8px;
}
.logo-sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  letter-spacing: 1px;
}
.auth-heading {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
}
.auth-desc {
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
  margin-top: 4px;
  margin-bottom: 24px;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}
.field-input {
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #1e293b;
  background: #f8fafc;
  outline: none;
  transition: all 0.15s ease;
}
.field-input:focus {
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.12);
}
.pwd-wrap { position: relative; }
.pwd-wrap .field-input { padding-right: 44px; }
.pwd-eye {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 15px;
  user-select: none;
}
.err-msg {
  background: #fff0f0;
  color: #e53e3e;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
}
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.25s cubic-bezier(0.34,1.56,0.64,1);
  white-space: nowrap;
}
.btn-primary {
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  color: white;
  box-shadow: 0 4px 12px rgba(59,130,246,0.35);
}
.btn-primary:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 18px rgba(59,130,246,0.45);
}
.auth-submit {
  width: 100%;
  padding: 13px;
  font-size: 15px;
  margin-top: 4px;
}
.auth-submit:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none !important;
}
.spin {
  animation: spin 1s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }
.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #94a3b8;
}
.link-pink {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 600;
}
.link-pink:hover { text-decoration: underline; }
.fade-in-up {
  animation: fadeInUp 0.4s ease both;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
