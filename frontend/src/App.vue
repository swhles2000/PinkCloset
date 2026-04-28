<template>
  <!--
    App.vue — 应用根组件
    负责：顶部导航栏 + 路由出口
    淡粉色主题在此处的 <style> 里全局定义
  -->
  <div id="pink-app">
    <!-- ══════ 顶部导航栏 ══════ -->
    <header class="app-header">
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
      </div>
    </header>

    <!-- ══════ 页面内容区域 ══════ -->
    <main class="app-main">
      <router-view />
    </main>

    <!-- ══════ 页脚 ══════ -->
    <footer class="app-footer">
      <span>🌸 Made with love · PinkCloset</span>
    </footer>
  </div>
</template>

<script setup>
// App.vue 本身不需要业务逻辑，仅作布局容器
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
