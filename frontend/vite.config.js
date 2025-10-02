import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import mediaFileMiddleware from './src/middleware/mediaFileMiddleware.js'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), mediaFileMiddleware()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        timeout: 60000
      }
    },
    fs: {
      allow: ['..']
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  assetsInclude: ['**/*.mp4', '**/*.jpg', '**/*.png', '**/*.gif'],
  optimizeDeps: {
    esbuildOptions: {
      // 配置esbuild选项
    }
  }
})