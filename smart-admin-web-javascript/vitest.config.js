import { defineConfig } from 'vitest/config'
import { resolve } from 'path'

export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./tests/setup.js'],
    include: ['tests/**/*.test.js'],
    exclude: ['node_modules', 'dist']
  },
  resolve: {
    alias: {
      '/@/': resolve(__dirname, 'src/')
    }
  }
})