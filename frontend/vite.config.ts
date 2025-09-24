import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: '0.0.0.0', // 允许外部访问
    port: 5173,
    open: true, // 自动打开浏览器
    cors: true, // 启用CORS
    proxy: {
      '/api': {
        target: 'http://localhost:16999',
        changeOrigin: true,
        secure: false,
        ws: true, // 支持WebSocket和Server-Sent Events
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('代理错误:', err)
          })
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('代理请求:', req.method, req.url)
          })
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('代理响应:', proxyRes.statusCode, req.url)
          })
        },
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
      }
    }
  },
  define: {
    __VUE_OPTIONS_API__: true,
    __VUE_PROD_DEVTOOLS__: false,
  }
})