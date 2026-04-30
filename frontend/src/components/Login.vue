<template>
  <div class="auth-bg">
    <div class="auth-card fade-in-up">
      <!-- Logo -->
      <div class="auth-logo">
        <span class="logo-icon">👗</span>
        <h1 class="logo-title">粉色衣橱</h1>
        <p class="logo-sub">PinkCloset · 你的私人衣橱助手</p>
      </div>

      <!-- 标题 -->
      <h2 class="auth-heading">欢迎回来 🌸</h2>
      <p class="auth-desc">请登录你的账号</p>

      <!-- 表单 -->
      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="field-group">
          <label class="field-label">账号</label>
          <input
            v-model="form.account"
            class="field-input"
            type="text"
            placeholder="手机号 或 邮箱"
            autocomplete="username"
          />
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
            <span class="pwd-eye" @click="showPwd = !showPwd">
              {{ showPwd ? '🙈' : '👁' }}
            </span>
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="errMsg" class="err-msg">⚠️ {{ errMsg }}</div>

        <button class="btn btn-primary auth-submit" type="submit" :disabled="loading">
          <span v-if="loading" class="spin">⏳</span>
          <span v-else>登 录</span>
        </button>
      </form>

      <div class="auth-footer">
        还没有账号？
        <router-link to="/register" class="link-pink">立即注册</router-link>
      </div>
      <div class="admin-entry">
        <router-link to="/admin/login" class="link-admin">🛡️ 管理员登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api.js'

const router  = useRouter()
const loading = ref(false)
const showPwd = ref(false)
const errMsg  = ref('')

const form = ref({
  account:  '',
  password: ''
})

async function handleLogin() {
  errMsg.value = ''
  if (!form.value.account.trim()) { errMsg.value = '请输入账号'; return }
  if (!form.value.password)       { errMsg.value = '请输入密码'; return }

  loading.value = true
  try {
    const res = await login({
      account:  form.value.account.trim(),
      password: form.value.password
    })
    if (res.code === 200) {
      // 将用户信息存入 localStorage，作为登录态
      localStorage.setItem('pc_user', JSON.stringify(res.data))
      // 通知 App.vue 更新用户信息显示
      window.dispatchEvent(new CustomEvent('user-login'))
      router.push('/')
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
}

.auth-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: 48px 40px 40px;
  width: 100%;
  max-width: 420px;
}

/* Logo 区 */
.auth-logo {
  text-align: center;
  margin-bottom: 28px;
}
.logo-icon {
  font-size: 48px;
  display: block;
  filter: drop-shadow(0 4px 8px rgba(255,105,135,0.3));
}
.logo-title {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-top: 8px;
}
.logo-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.auth-heading {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
}
.auth-desc {
  font-size: 13px;
  color: var(--text-muted);
  text-align: center;
  margin-top: 4px;
  margin-bottom: 28px;
}

/* 表单 */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.field-input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-primary);
  background: var(--pink-50);
  outline: none;
  transition: var(--transition-fast);
}
.field-input:focus {
  border-color: var(--pink-300);
  background: white;
  box-shadow: 0 0 0 3px rgba(255,105,135,0.12);
}

.pwd-wrap {
  position: relative;
}
.pwd-wrap .field-input {
  padding-right: 44px;
}
.pwd-eye {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 16px;
  user-select: none;
}

.err-msg {
  background: #fff0f0;
  color: #e53e3e;
  border: 1px solid #fecaca;
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 13px;
}

.auth-submit {
  width: 100%;
  padding: 14px;
  font-size: 15px;
  margin-top: 4px;
  border-radius: var(--radius-sm);
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
  color: var(--text-muted);
}
.link-pink {
  color: var(--pink-400);
  text-decoration: none;
  font-weight: 600;
}
.admin-entry {
  text-align: center;
  margin-top: 12px;
}
.link-admin {
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: none;
}
.link-admin:hover {
  color: #64748b;
}
.link-pink:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .auth-card { padding: 36px 24px 32px; }
}
</style>
