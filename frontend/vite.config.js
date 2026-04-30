import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: '0.0.0.0',   // 允许局域网设备访问
    // 开发环境代理：/api 请求转发到 Spring Boot
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
