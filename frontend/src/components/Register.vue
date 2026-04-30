<template>
  <div class="auth-bg">
    <div class="auth-card fade-in-up">
      <!-- Logo -->
      <div class="auth-logo">
        <span class="logo-icon">👗</span>
        <h1 class="logo-title">粉色衣橱</h1>
        <p class="logo-sub">PinkCloset · 创建你的专属衣橱</p>
      </div>

      <h2 class="auth-heading">创建账号 ✨</h2>
      <p class="auth-desc">填写以下信息完成注册</p>

      <form class="auth-form" @submit.prevent="handleRegister">

        <!-- 姓名（必填） -->
        <div class="field-group">
          <label class="field-label">姓名 <span class="req">*</span></label>
          <input v-model="form.name" class="field-input" type="text" placeholder="请输入你的姓名" />
        </div>

        <!-- 性别 + 年龄（一行两列） -->
        <div class="field-row">
          <div class="field-group">
            <label class="field-label">性别</label>
            <select v-model="form.gender" class="field-input field-select">
              <option value="">不填</option>
              <option value="女">女</option>
              <option value="男">男</option>
              <option value="其他">其他</option>
            </select>
          </div>
          <div class="field-group">
            <label class="field-label">年龄</label>
            <input v-model.number="form.age" class="field-input" type="number" min="1" max="120" placeholder="可选" />
          </div>
        </div>

        <!-- 手机号（必填） -->
        <div class="field-group">
          <label class="field-label">手机号 <span class="req">*</span></label>
          <input v-model="form.phone" class="field-input" type="tel" placeholder="11 位手机号" />
        </div>

        <!-- 邮箱（可选） -->
        <div class="field-group">
          <label class="field-label">邮箱 <span class="opt">（可用于登录）</span></label>
          <input v-model="form.email" class="field-input" type="email" placeholder="可选" />
        </div>

        <!-- 新密码 -->
        <div class="field-group">
          <label class="field-label">密码 <span class="req">*</span></label>
          <div class="pwd-wrap">
            <input
              v-model="form.password"
              class="field-input"
              :type="showPwd ? 'text' : 'password'"
              placeholder="至少8位，含大小写字母、数字、特殊字符"
              autocomplete="new-password"
            />
            <span class="pwd-eye" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁' }}</span>
          </div>
          <!-- 密码强度指示条 -->
          <div v-if="form.password" class="pwd-strength">
            <div class="strength-bars">
              <span :class="['bar', pwdLevel >= 1 ? 'bar-fill-' + pwdColor : '']"></span>
              <span :class="['bar', pwdLevel >= 2 ? 'bar-fill-' + pwdColor : '']"></span>
              <span :class="['bar', pwdLevel >= 3 ? 'bar-fill-' + pwdColor : '']"></span>
              <span :class="['bar', pwdLevel >= 4 ? 'bar-fill-' + pwdColor : '']"></span>
            </div>
            <span :class="'strength-label strength-' + pwdColor">{{ pwdLevelText }}</span>
          </div>
          <p class="pwd-hint">需含：大写字母 · 小写字母 · 数字 · 特殊字符（如 !@#$%）</p>
        </div>

        <!-- 确认密码 -->
        <div class="field-group">
          <label class="field-label">确认密码 <span class="req">*</span></label>
          <div class="pwd-wrap">
            <input
              v-model="form.confirmPassword"
              class="field-input"
              :type="showPwd2 ? 'text' : 'password'"
              placeholder="再次输入密码"
              autocomplete="new-password"
            />
            <span class="pwd-eye" @click="showPwd2 = !showPwd2">{{ showPwd2 ? '🙈' : '👁' }}</span>
          </div>
          <!-- 实时提示两次密码是否一致 -->
          <span
            v-if="form.confirmPassword"
            :class="pwdMatch ? 'pwd-ok' : 'pwd-fail'"
          >
            {{ pwdMatch ? '✅ 密码一致' : '❌ 密码不一致' }}
          </span>
        </div>

        <!-- 错误 / 成功提示 -->
        <div v-if="errMsg"     class="err-msg">⚠️ {{ errMsg }}</div>
        <div v-if="successMsg" class="ok-msg">🎉 {{ successMsg }}</div>

        <button class="btn btn-primary auth-submit" type="submit" :disabled="loading">
          <span v-if="loading" class="spin">⏳</span>
          <span v-else>注 册</span>
        </button>
      </form>

      <div class="auth-footer">
        已有账号？
        <router-link to="/login" class="link-pink">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api.js'

const router     = useRouter()
const loading    = ref(false)
const showPwd    = ref(false)
const showPwd2   = ref(false)
const errMsg     = ref('')
const successMsg = ref('')

const form = ref({
  name:            '',
  gender:          '',
  age:             '',
  phone:           '',
  email:           '',
  password:        '',
  confirmPassword: ''
})

// 实时检测两次密码是否一致
const pwdMatch = computed(() =>
  form.value.password && form.value.confirmPassword &&
  form.value.password === form.value.confirmPassword
)

// 密码强度评估（4项：大写/小写/数字/特殊字符，每满足一项+1）
const pwdLevel = computed(() => {
  const p = form.value.password
  if (!p) return 0
  let score = 0
  if (/[A-Z]/.test(p)) score++
  if (/[a-z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[!@#$%^&*()_+\-=\[\]{}|;':",./<>?`~\\]/.test(p)) score++
  // 长度不足8位最多2分
  if (p.length < 8 && score > 2) score = 2
  return score
})
const pwdColor = computed(() => {
  const lvl = pwdLevel.value
  if (lvl <= 1) return 'weak'
  if (lvl === 2) return 'fair'
  if (lvl === 3) return 'good'
  return 'strong'
})
const pwdLevelText = computed(() => {
  const map = { weak: '弱', fair: '一般', good: '较强', strong: '强' }
  return map[pwdColor.value]
})
const isPwdStrong = computed(() => {
  const p = form.value.password
  return p && p.length >= 8 &&
    /[A-Z]/.test(p) && /[a-z]/.test(p) &&
    /[0-9]/.test(p) && /[!@#$%^&*()_+\-=\[\]{}|;':",./<>?`~\\]/.test(p)
})

async function handleRegister() {
  errMsg.value     = ''
  successMsg.value = ''

  // 前端基础校验
  if (!form.value.name.trim())    { errMsg.value = '姓名不能为空'; return }
  if (!form.value.phone.trim())   { errMsg.value = '手机号不能为空'; return }
  if (!form.value.password)       { errMsg.value = '请设置密码'; return }
  if (!isPwdStrong.value) {
    errMsg.value = '密码需包含大写字母、小写字母、数字及特殊字符，且至少 8 位'
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    errMsg.value = '两次密码输入不一致'
    return
  }

  loading.value = true
  try {
    const res = await register({
      name:            form.value.name.trim(),
      gender:          form.value.gender || null,
      age:             form.value.age    || null,
      phone:           form.value.phone.trim(),
      email:           form.value.email.trim() || null,
      password:        form.value.password,
      confirmPassword: form.value.confirmPassword
    })

    if (res.code === 200) {
      successMsg.value = '注册成功！2 秒后跳转登录...'
      setTimeout(() => router.push('/login'), 2000)
    } else {
      errMsg.value = res.message || '注册失败'
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
  padding: 40px 40px 36px;
  width: 100%;
  max-width: 460px;
}

.auth-logo {
  text-align: center;
  margin-bottom: 20px;
}
.logo-icon {
  font-size: 42px;
  display: block;
  filter: drop-shadow(0 4px 8px rgba(255,105,135,0.3));
}
.logo-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-top: 6px;
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
  margin-bottom: 24px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.req { color: var(--pink-400); margin-left: 2px; }
.opt { color: var(--text-muted); font-weight: 400; font-size: 11px; }

.field-input {
  width: 100%;
  padding: 11px 14px;
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
.field-select {
  appearance: none;
  cursor: pointer;
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

.pwd-ok   { font-size: 12px; color: #38a169; }
.pwd-fail { font-size: 12px; color: #e53e3e; }

/* 密码强度条 */
.pwd-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}
.strength-bars {
  display: flex;
  gap: 4px;
  flex: 1;
}
.bar {
  height: 4px;
  flex: 1;
  border-radius: 2px;
  background: var(--border);
  transition: background 0.2s;
}
.bar-fill-weak   { background: #e53e3e; }
.bar-fill-fair   { background: #ed8936; }
.bar-fill-good   { background: #38a169; }
.bar-fill-strong { background: #276749; }
.strength-label { font-size: 11px; font-weight: 600; white-space: nowrap; }
.strength-weak   { color: #e53e3e; }
.strength-fair   { color: #ed8936; }
.strength-good   { color: #38a169; }
.strength-strong { color: #276749; }
.pwd-hint {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
  line-height: 1.5;
}

.err-msg {
  background: #fff0f0;
  color: #e53e3e;
  border: 1px solid #fecaca;
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 13px;
}
.ok-msg {
  background: #f0fff4;
  color: #276749;
  border: 1px solid #9ae6b4;
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 13px;
}

.auth-submit {
  width: 100%;
  padding: 13px;
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
.link-pink:hover { text-decoration: underline; }

@media (max-width: 480px) {
  .auth-card { padding: 32px 20px 28px; }
  .field-row { grid-template-columns: 1fr; }
}
</style>
