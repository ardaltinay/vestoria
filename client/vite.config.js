import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(() => {
  const apiTarget = process.env.VITE_API_BASE || 'http://localhost:8081'
  return {
    plugins: [vue()],
    define: {
      global: 'window',
    },
    server: {
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: false,
          rewrite: undefined
        },
      },
    }
  }
})
