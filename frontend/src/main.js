import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import OutfitLab from './components/OutfitLab.vue'
import Wardrobe from './components/Wardrobe.vue'

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',        component: OutfitLab, name: 'lab'      },
    { path: '/wardrobe', component: Wardrobe, name: 'wardrobe' }
  ]
})

createApp(App).use(router).mount('#app')
