/**
 * Jest配置文件
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

module.exports = {
  // 测试环境
  testEnvironment: 'jsdom',
  
  // 模块路径映射
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    '\\.(jpg|jpeg|png|gif|svg)$': '<rootDir>/__mocks__/fileMock.js'
  },
  
  // 转换配置
  transform: {
    '^.+\\.vue$': '@vue/vue3-jest',
    '^.+\\.js$': 'babel-jest'
  },
  
  // 测试文件匹配模式
  testMatch: [
    '**/__tests__/**/*.test.js',
    '**/?(*.)+(spec|test).js'
  ],
  
  // 覆盖率收集
  collectCoverageFrom: [
    'src/**/*.{js,vue}',
    '!src/main.js',
    '!src/**/*.spec.js',
    '!src/**/*.test.js'
  ],
  
  // 覆盖率阈值
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80
    }
  },
  
  // 忽略的文件
  testPathIgnorePatterns: [
    '/node_modules/',
    '/dist/',
    '/unpackage/'
  ],
  
  // 设置超时时间
  testTimeout: 10000,
  
  // 模拟uni-app全局对象
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js']
}

