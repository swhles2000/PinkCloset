import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import OutfitLab   from './components/OutfitLab.vue'
import Wardrobe    from './components/Wardrobe.vue'
import Login       from './components/Login.vue'
import Register    from './components/Register.vue'
import Profile     from './components/Profile.vue'
import AdminLogin  from './components/AdminLogin.vue'
import AdminPanel  from './components/AdminPanel.vue'

// ── 路由配置 ──────────────────────────────────────
const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 普通用户页面（需要登录）
    { path: '/',            component: OutfitLab,   name: 'lab',         meta: { requiresAuth: true } },
    { path: '/wardrobe',    component: Wardrobe,    name: 'wardrobe',    meta: { requiresAuth: true } },
    { path: '/profile',     component: Profile,     name: 'profile',     meta: { requiresAuth: true } },
    // 公开页面
    { path: '/login',       component: Login,       name: 'login' },
    { path: '/register',    component: Register,    name: 'register' },
    // 管理员页面（独立认证体系）
    { path: '/admin/login', component: AdminLogin,  name: 'adminLogin' },
    { path: '/admin',       component: AdminPanel,  name: 'adminPanel',  meta: { requiresAdmin: true } },
    // 其他路径重定向到首页
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

// ── 全局路由守卫 ──────────────────────────────────
router.beforeEach((to, _from, next) => {
  const isLoggedIn    = !!localStorage.getItem('pc_user')
  const isAdminLogged = !!localStorage.getItem('pc_admin')

  // 普通用户页面需要登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    return next({ name: 'login' })
  }
  // 管理员页面需要管理员登录
  if (to.meta.requiresAdmin && !isAdminLogged) {
    return next({ name: 'adminLogin' })
  }
  // 已登录普通用户访问登录/注册页，跳转首页
  if ((to.name === 'login' || to.name === 'register') && isLoggedIn) {
    return next({ name: 'lab' })
  }
  // 已登录管理员访问管理员登录页，跳转面板
  if (to.name === 'adminLogin' && isAdminLogged) {
    return next({ name: 'adminPanel' })
  }
  next()
})

createApp(App).use(router).mount('#app')
