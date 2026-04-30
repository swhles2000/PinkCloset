<template>
  <!--
    App.vue — 应用根组件
    负责：顶部导航栏 + 路由出口
    淡粉色主题在此处的 <style> 里全局定义
  -->
  <div id="pink-app">
    <!-- ══════ 顶部导航栏（登录/注册页不显示） ══════ -->
    <header v-if="showNav" class="app-header">
      <div class="header-inner">
        <!-- 品牌标志 -->
        <div class="brand">
          <span class="brand-icon">👗</span>
          <span class="brand-name">粉色衣橱</span>
          <span class="brand-sub">PinkCloset</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-links">
          <router-link to="/" class="nav-item" active-class="nav-active">
            ✨ 搭配实验室
          </router-link>
          <router-link to="/wardrobe" class="nav-item" active-class="nav-active">
            🗂 我的衣橱
          </router-link>
        </nav>

        <!-- 用户信息 + 个人中心 + 退出 -->
        <div v-if="currentUser" class="user-bar">
          <div class="nav-avatar" @click="$router.push('/profile')">
            <img v-if="currentUser.avatar" :src="currentUser.avatar" class="nav-avatar-img" alt="头像" />
            <span v-else class="nav-avatar-emoji">🌸</span>
          </div>
          <router-link to="/profile" class="user-name user-name-link">{{ currentUser.name }}</router-link>
          <button class="logout-btn" @click="logout" title="退出登录">退出</button>
        </div>
      </div>
    </header>

    <!-- ══════ 页面内容区域 ══════ -->
    <main :class="showNav ? 'app-main' : 'app-main-full'">
      <router-view />
    </main>

    <!-- ══════ 页脚（登录/注册页不显示） ══════ -->
    <footer v-if="showNav" class="app-footer">
      <span>🌸 燕燕公主私厨 · PinkCloset</span>
    </footer>

    <!-- ══════ 服务宕机可爱提示 ══════ -->
    <Teleport to="body">
      <Transition name="server-down-fade">
        <div v-if="serverDown" class="server-down-overlay" @click.self="serverDown = false">
          <div class="server-down-card">
            <div class="server-down-icon">🥺</div>
            <h2 class="server-down-title">哎呀，服务走丢了~</h2>
            <p class="server-down-desc">
              后端服务好像没有启动呢，请先启动服务再试试吧！<br>
              <span class="server-down-hint">小贴士：在终端运行 Spring Boot 应用即可启动哦~</span>
            </p>
            <button class="server-down-btn" @click="serverDown = false">
              我知道了 💕
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router      = useRouter()
const route       = useRoute()
const currentUser = ref(null)
const serverDown  = ref(false)

// 登录/注册/管理员页面不显示普通导航栏和页脚
const showNav = computed(() =>
  route.name !== 'login' &&
  route.name !== 'register' &&
  route.name !== 'adminLogin' &&
  route.name !== 'adminPanel'
)

onMounted(() => {
  const raw = localStorage.getItem('pc_user')
  if (raw) currentUser.value = JSON.parse(raw)
  // 监听登录状态变化（login 页面登录成功后触发）
  window.addEventListener('user-login', () => {
    const raw2 = localStorage.getItem('pc_user')
    if (raw2) currentUser.value = JSON.parse(raw2)
  })
  // 监听注销事件（profile 页注销账号后触发）
  window.addEventListener('user-logout', () => {
    currentUser.value = null
  })
  // 监听头像更新事件（profile 页上传头像后触发）
  window.addEventListener('user-avatar-updated', (e) => {
    if (currentUser.value) {
      currentUser.value.avatar = e.detail
    }
  })
  // 监听服务宕机事件（api.js 拦截器触发）
  window.addEventListener('server-down', () => {
    serverDown.value = true
  })
})

function logout() {
  localStorage.removeItem('pc_user')
  currentUser.value = null
  router.push('/login')
}
</script>

<style>
/* ================================================================
   全局 CSS 变量 — 粉色主题调色盘
   ================================================================ */
:root {
  --pink-50:  #fff0f5;
  --pink-100: #ffe4e1;
  --pink-200: #ffb6c1;
  --pink-300: #ff91a4;
  --pink-400: #ff6b8a;
  --pink-500: #ff4d6d;
  --pink-dark: #d63065;

  --text-primary:   #3d2c35;
  --text-secondary: #7a5c68;
  --text-muted:     #b08b99;

  --white:      #ffffff;
  --card-bg:    #ffffffee;
  --border:     #f5d0dc;
  --shadow-sm:  0 2px 12px rgba(255, 105, 135, 0.12);
  --shadow-md:  0 6px 24px rgba(255, 105, 135, 0.18);
  --shadow-lg:  0 12px 40px rgba(255, 105, 135, 0.22);

  --radius-sm:  8px;
  --radius-md:  16px;
  --radius-lg:  24px;
  --radius-xl:  32px;

  --transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  --transition-fast: all 0.15s ease;
}

/* ================================================================
   CSS Reset & 基础样式
   ================================================================ */
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  /* 淡粉色渐变背景 */
  background: linear-gradient(135deg, var(--pink-50) 0%, var(--pink-100) 50%, #fce4ec 100%);
  background-attachment: fixed;
  color: var(--text-primary);
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
}

/* ================================================================
   顶部导航栏
   ================================================================ */
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 品牌区 */
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: default;
  user-select: none;
}

.brand-icon {
  font-size: 28px;
  filter: drop-shadow(0 2px 4px rgba(255, 105, 135, 0.3));
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-sub {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 300;
  letter-spacing: 1px;
}

/* 导航链接 */
.nav-links {
  display: flex;
  gap: 8px;
}

/* 用户栏 */
.user-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}
.nav-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(255,105,135,0.15);
}
.nav-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.nav-avatar-emoji {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 16px;
  background: linear-gradient(135deg, var(--pink-200), var(--pink-100));
  border-radius: 50%;
}
.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--pink-dark);
}
.user-name-link {
  text-decoration: none;
  transition: var(--transition-fast);
}
.user-name-link:hover {
  opacity: 0.75;
  text-decoration: underline;
}
.logout-btn {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  border: 1.5px solid var(--pink-200);
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: var(--transition-fast);
}
.logout-btn:hover {
  background: var(--pink-50);
  color: var(--pink-400);
  border-color: var(--pink-300);
}

.nav-item {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  padding: 8px 18px;
  border-radius: 999px;
  transition: var(--transition-fast);
}

.nav-item:hover {
  background: var(--pink-50);
  color: var(--pink-400);
}

.nav-active {
  background: linear-gradient(135deg, var(--pink-200), var(--pink-100)) !important;
  color: var(--pink-dark) !important;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 105, 135, 0.2);
}

/* ================================================================
   主内容区域
   ================================================================ */
.app-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 64px;
  min-height: calc(100vh - 130px);
}

/* 登录/注册页面：全屏无边距 */
.app-main-full {
  min-height: 100vh;
  padding: 0;
}

/* ================================================================
   页脚
   ================================================================ */
.app-footer {
  text-align: center;
  padding: 20px;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px solid var(--border);
  background: rgba(255,255,255,0.4);
}

/* ================================================================
   通用卡片样式
   ================================================================ */
.card {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
}

.card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

/* ================================================================
   通用按钮样式
   ================================================================ */
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
  transition: var(--transition);
  white-space: nowrap;
}

.btn-primary {
  background: linear-gradient(135deg, var(--pink-300), var(--pink-400));
  color: white;
  box-shadow: 0 4px 12px rgba(255, 105, 135, 0.35);
}

.btn-primary:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 18px rgba(255, 105, 135, 0.45);
}

.btn-primary:active {
  transform: translateY(0) scale(0.98);
}

.btn-outline {
  background: transparent;
  color: var(--pink-400);
  border: 2px solid var(--pink-200);
}

.btn-outline:hover {
  background: var(--pink-50);
  border-color: var(--pink-300);
}

.btn-ghost {
  background: rgba(255,255,255,0.7);
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.btn-ghost:hover {
  background: white;
  color: var(--pink-400);
}

.btn-danger {
  background: #fff0f0;
  color: #e53e3e;
  border: 1px solid #fecaca;
}

.btn-danger:hover {
  background: #fee2e2;
}

/* ================================================================
   通用标签（tag）
   ================================================================ */
.tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.tag-pink {
  background: var(--pink-100);
  color: var(--pink-dark);
}

/* ================================================================
   淡入动画
   ================================================================ */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0);    }
}

.fade-in-up {
  animation: fadeInUp 0.4s ease both;
}

/* ================================================================
   服务宕机可爱提示
   ================================================================ */
.server-down-overlay {
  position: fixed;
  inset: 0;
  background: rgba(60, 20, 40, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  padding: 24px;
}

.server-down-card {
  background: white;
  border-radius: 24px;
  padding: 40px 36px 32px;
  text-align: center;
  max-width: 380px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(255, 105, 135, 0.2);
}

.server-down-icon {
  font-size: 72px;
  margin-bottom: 16px;
  animation: serverDownBounce 1.5s ease infinite;
}

@keyframes serverDownBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

.server-down-title {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.server-down-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 24px;
}

.server-down-hint {
  font-size: 12px;
  color: var(--text-muted);
  display: block;
  margin-top: 6px;
}

.server-down-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 12px 28px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, var(--pink-300), var(--pink-400));
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255, 105, 135, 0.35);
  transition: all 0.2s ease;
}

.server-down-btn:hover {
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 6px 20px rgba(255, 105, 135, 0.45);
}

.server-down-btn:active {
  transform: translateY(0) scale(0.98);
}

/* 过渡动画 */
.server-down-fade-enter-active {
  transition: all 0.35s ease;
}
.server-down-fade-leave-active {
  transition: all 0.25s ease;
}
.server-down-fade-enter-from,
.server-down-fade-leave-to {
  opacity: 0;
}
.server-down-fade-enter-from .server-down-card,
.server-down-fade-leave-to .server-down-card {
  transform: scale(0.9) translateY(20px);
}

/* ================================================================
   响应式适配
   ================================================================ */
@media (max-width: 768px) {
  .app-main {
    padding: 20px 16px 48px;
  }

  .header-inner {
    padding: 0 16px;
  }

  .brand-sub {
    display: none;
  }
}
</style>
