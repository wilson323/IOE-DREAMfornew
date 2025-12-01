import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// Mock Vue Router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    back: vi.fn(),
    replace: vi.fn()
  }),
  useRoute: () => ({
    params: {},
    query: {},
    path: '/test'
  })
}))

// Mock Ant Design Vue
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  },
  Modal: {
    confirm: vi.fn()
  }
}))

// Mock icons
vi.mock('@ant-design/icons-vue', () => ({
  SearchOutlined: { template: '<div></div>' },
  ReloadOutlined: { template: '<div></div>' },
  PlusOutlined: { template: '<div></div>' },
  DeleteOutlined: { template: '<div></div>' },
  EditOutlined: { template: '<div></div>' },
  ExportOutlined: { template: '<div></div>' },
  DownOutlined: { template: '<div></div>' },
  DesktopOutlined: { template: '<div></div>' },
  WifiOutlined: { template: '<div></div>' },
  DisconnectOutlined: { template: '<div></div>' },
  ExclamationCircleOutlined: { template: '<div></div>' },
  CheckCircleOutlined: { template: '<div></div>' },
  StopOutlined: { template: '<div></div>' }
}))

// Global test configuration
config.global.mocks = {
  $t: (key) => key,
  $message: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
global.localStorage = localStorageMock

// Mock window.location
Object.defineProperty(window, 'location', {
  value: {
    href: 'http://localhost:8081',
    origin: 'http://localhost:8081',
    pathname: '/test',
    search: '',
    hash: ''
  },
  writable: true
})