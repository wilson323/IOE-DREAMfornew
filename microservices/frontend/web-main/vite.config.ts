import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'

/**
 * 微服务代理配置
 * 支持通过环境变量 VITE_API_GATEWAY 统一使用网关，或直接访问各微服务
 */
const createProxyConfig = (env: Record<string, string>) => {
  const useGateway = env.VITE_API_GATEWAY === 'true'
  const gatewayUrl = env.VITE_GATEWAY_URL || 'http://localhost:8080'
  const baseUrl = env.VITE_API_BASE_URL || 'http://localhost'

  /**
   * 创建代理配置项
   * @param servicePath API路径前缀
   * @param port 微服务端口
   * @param useGateway 是否通过网关访问
   */
  const createProxy = (servicePath: string, port: number, useGateway: boolean = false) => {
    if (useGateway) {
      // 通过网关访问，路径保持不变
      return {
        target: gatewayUrl,
        changeOrigin: true,
        secure: false,
        ws: true,
        configure: (proxy: any) => {
          proxy.on('error', (err: Error) => {
            console.error(`[Proxy Error] ${servicePath}:`, err.message)
          })
        }
      }
    } else {
      // 直接访问微服务，移除路径前缀
      return {
        target: `${baseUrl}:${port}`,
        changeOrigin: true,
        secure: false,
        ws: true,
        rewrite: (path: string) => path.replace(new RegExp(`^${servicePath}`), ''),
        configure: (proxy: any) => {
          proxy.on('error', (err: Error) => {
            console.error(`[Proxy Error] ${servicePath} -> ${port}:`, err.message)
          })
        }
      }
    }
  }

  return {
    // 统一网关入口（如果使用网关模式）
    ...(useGateway && {
      '/api': {
        target: gatewayUrl,
        changeOrigin: true,
        secure: false,
        ws: true
      }
    }),

    // 基础设施服务
    '/api/auth': createProxy('/api/auth', 8081, useGateway),
    '/api/identity': createProxy('/api/identity', 8082, useGateway),
    '/api/system': createProxy('/api/system', 8103, useGateway), // 系统管理服务

    // 核心业务服务
    '/api/device': createProxy('/api/device', 8083, useGateway),
    '/api/area': createProxy('/api/area', 8084, useGateway),
    '/api/access': createProxy('/api/access', 8085, useGateway),
    '/api/consume': createProxy('/api/consume', 8086, useGateway),
    '/api/attendance': createProxy('/api/attendance', 8087, useGateway),
    '/api/video': createProxy('/api/video', 8088, useGateway),
    '/api/visitor': createProxy('/api/visitor', 8089, useGateway),
    '/api/notification': createProxy('/api/notification', 8090, useGateway),
    '/api/file': createProxy('/api/file', 8091, useGateway),
    '/api/report': createProxy('/api/report', 8092, useGateway),

    // 扩展业务服务
    '/api/hr': createProxy('/api/hr', 8093, useGateway),
    '/api/erp': createProxy('/api/erp', 8095, useGateway),
    '/api/monitor': createProxy('/api/monitor', 8097, useGateway),
    '/api/audit': createProxy('/api/audit', 8096, useGateway), // 审计服务
    '/api/scheduler': createProxy('/api/scheduler', 8087, useGateway), // 调度服务
    '/api/integration': createProxy('/api/integration', 8088, useGateway), // 集成服务
    '/api/infrastructure': createProxy('/api/infrastructure', 8089, useGateway) // 基础设施服务
  }
}

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, (process as any).cwd() || '.', '')

  return {
    plugins: [
      vue(),
      AutoImport({
        imports: [
          'vue',
          'vue-router',
          'pinia',
          '@vueuse/core'
        ],
        dts: true,
        eslintrc: {
          enabled: true,
          filepath: './.eslintrc-auto-import.json'
        }
      }),
      Components({
        resolvers: [
          AntDesignVueResolver({
            importStyle: false
          })
        ],
        dts: true,
        directoryAsNamespace: false
      })
    ],

    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        '/@': fileURLToPath(new URL('./src', import.meta.url)),
        '@components': fileURLToPath(new URL('./src/components', import.meta.url)),
        '@utils': fileURLToPath(new URL('./src/utils', import.meta.url)),
        '@api': fileURLToPath(new URL('./src/api', import.meta.url)),
        '@store': fileURLToPath(new URL('./src/store', import.meta.url)),
        '@router': fileURLToPath(new URL('./src/router', import.meta.url)),
        '@views': fileURLToPath(new URL('./src/views', import.meta.url)),
        '@assets': fileURLToPath(new URL('./src/assets', import.meta.url)),
        '@styles': fileURLToPath(new URL('./src/styles', import.meta.url))
      }
    },

    css: {
      preprocessorOptions: {
        scss: {
          additionalData: '@import "@/styles/variables.scss";',
          charset: false
        }
      },
      devSourcemap: true
    },

    server: {
      port: Number(env.VITE_PORT) || 3000,
      host: env.VITE_HOST || '0.0.0.0',
      open: env.VITE_OPEN === 'true',
      cors: true,
      strictPort: false,
      proxy: createProxyConfig(env)
    },

    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: mode === 'development',
      minify: mode === 'production' ? 'terser' : false,
      terserOptions: {
        compress: {
          drop_console: mode === 'production',
          drop_debugger: true
        }
      },
      rollupOptions: {
        output: {
          chunkFileNames: 'js/[name]-[hash].js',
          entryFileNames: 'js/[name]-[hash].js',
          assetFileNames: (assetInfo) => {
            const info = assetInfo.name?.split('.') || []
            const ext = info[info.length - 1]
            if (/\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/i.test(assetInfo.name || '')) {
              return `media/[name]-[hash].[ext]`
            }
            if (/\.(png|jpe?g|gif|svg|webp|avif)(\?.*)?$/i.test(assetInfo.name || '')) {
              return `img/[name]-[hash].[ext]`
            }
            if (/\.(woff2?|eot|ttf|otf)(\?.*)?$/i.test(assetInfo.name || '')) {
              return `fonts/[name]-[hash].[ext]`
            }
            return `${ext}/[name]-[hash].[ext]`
          },
          manualChunks: (id) => {
            // 将 node_modules 中的包分离
            if (id.includes('node_modules')) {
              if (id.includes('vue') || id.includes('vue-router') || id.includes('pinia')) {
                return 'vendor-vue'
              }
              if (id.includes('ant-design-vue') || id.includes('@ant-design')) {
                return 'vendor-antd'
              }
              if (id.includes('axios') || id.includes('dayjs') || id.includes('lodash')) {
                return 'vendor-utils'
              }
              if (id.includes('qiankun')) {
                return 'vendor-qiankun'
              }
              return 'vendor-others'
            }
          }
        }
      },
      chunkSizeWarningLimit: 1500,
      reportCompressedSize: false,
      target: 'es2015'
    },

    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'ant-design-vue',
        '@ant-design/icons-vue',
        'axios',
        'dayjs',
        'lodash-es',
        'qiankun',
        '@vueuse/core'
      ],
      exclude: []
    },

    define: {
      __VUE_OPTIONS_API__: true,
      __VUE_PROD_DEVTOOLS__: false,
      __APP_VERSION__: JSON.stringify((process as any).env?.npm_package_version || '1.0.0')
    },

    // 预览配置
    preview: {
      port: Number(env.VITE_PREVIEW_PORT) || 3000,
      host: env.VITE_HOST || '0.0.0.0',
      cors: true,
      proxy: createProxyConfig(env)
    }
  }
})
