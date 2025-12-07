/**
 * 离线数据管理器单元测试
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 * 
 * 测试覆盖：
 * - 离线队列加载和保存
 * - 离线操作添加
 * - 数据同步
 * - 网络监听
 * - 队列状态管理
 */

import { describe, it, expect, beforeEach, afterEach, jest } from '@jest/globals'
import { OfflineDataManager } from '../offline-manager'

// Mock uni API
global.uni = {
  getStorageSync: jest.fn(),
  setStorageSync: jest.fn(),
  onNetworkStatusChange: jest.fn(),
  showToast: jest.fn(),
  showLoading: jest.fn(),
  hideLoading: jest.fn(),
  request: jest.fn()
}

describe('OfflineDataManager - 队列管理', () => {
  let manager
  
  beforeEach(() => {
    // 重置mock
    jest.clearAllMocks()
    
    // 模拟空队列
    global.uni.getStorageSync.mockReturnValue([])
    
    manager = new OfflineDataManager()
  })
  
  it('应该正确初始化空队列', () => {
    expect(manager.queue).toEqual([])
    expect(manager.isSyncing).toBe(false)
  })
  
  it('应该正确加载已存在的队列', () => {
    const existingQueue = [
      { id: 'test1', type: 'test', status: 'pending' }
    ]
    global.uni.getStorageSync.mockReturnValue(existingQueue)
    
    const newManager = new OfflineDataManager()
    expect(newManager.queue).toEqual(existingQueue)
  })
  
  it('应该处理加载队列时的错误', () => {
    global.uni.getStorageSync.mockImplementation(() => {
      throw new Error('Storage error')
    })
    
    const newManager = new OfflineDataManager()
    expect(newManager.queue).toEqual([])
  })
  
  it('应该正确保存队列', () => {
    manager.queue = [{ id: 'test', type: 'test' }]
    manager.saveQueue()
    
    expect(global.uni.setStorageSync).toHaveBeenCalledWith(
      'OFFLINE_DATA_QUEUE',
      manager.queue
    )
  })
  
  it('应该处理保存队列时的错误', () => {
    global.uni.setStorageSync.mockImplementation(() => {
      throw new Error('Storage error')
    })
    
    // 不应该抛出异常
    expect(() => manager.saveQueue()).not.toThrow()
  })
})

describe('OfflineDataManager - 添加操作', () => {
  let manager
  
  beforeEach(() => {
    jest.clearAllMocks()
    global.uni.getStorageSync.mockReturnValue([])
    manager = new OfflineDataManager()
  })
  
  it('应该正确添加考勤打卡记录', () => {
    const punchData = {
      userId: 1,
      deviceId: 100,
      punchTime: '2024-12-05 09:00:00'
    }
    
    const id = manager.addAttendancePunch(punchData)
    
    expect(id).toBeDefined()
    expect(id).toContain('offline_')
    expect(manager.queue.length).toBe(1)
    
    const item = manager.queue[0]
    expect(item.type).toBe('attendance_punch')
    expect(item.module).toBe('attendance')
    expect(item.data).toEqual(punchData)
    expect(item.status).toBe('pending')
    expect(item.retryCount).toBe(0)
  })
  
  it('应该正确添加消费交易记录', () => {
    const consumeData = {
      accountId: 1,
      amount: 15.5,
      deviceId: 200
    }
    
    const id = manager.addConsumeTransaction(consumeData)
    
    expect(manager.queue.length).toBe(1)
    const item = manager.queue[0]
    expect(item.type).toBe('consume_transaction')
    expect(item.module).toBe('consume')
    expect(item.api).toBe('/api/v1/consume/mobile/transaction/quick')
    expect(item.method).toBe('POST')
  })
  
  it('应该正确添加访客签到记录', () => {
    const checkInData = {
      visitorId: 1,
      qrCode: 'QR123456'
    }
    
    const id = manager.addVisitorCheckIn(checkInData)
    
    expect(manager.queue.length).toBe(1)
    const item = manager.queue[0]
    expect(item.type).toBe('visitor_checkin')
    expect(item.module).toBe('visitor')
  })
  
  it('应该正确添加门禁记录', () => {
    const recordData = {
      userId: 1,
      deviceId: 300,
      accessTime: '2024-12-05 08:00:00'
    }
    
    const id = manager.addAccessRecord(recordData)
    
    expect(manager.queue.length).toBe(1)
    const item = manager.queue[0]
    expect(item.type).toBe('access_record')
    expect(item.module).toBe('access')
  })
  
  it('应该为每个操作生成唯一ID', () => {
    const id1 = manager.addAttendancePunch({ userId: 1 })
    const id2 = manager.addAttendancePunch({ userId: 2 })
    const id3 = manager.addConsumeTransaction({ accountId: 1 })
    
    expect(id1).not.toBe(id2)
    expect(id2).not.toBe(id3)
    expect(manager.queue.length).toBe(3)
  })
  
  it('应该在添加操作后保存队列', () => {
    manager.addAttendancePunch({ userId: 1 })
    
    expect(global.uni.setStorageSync).toHaveBeenCalled()
  })
})

describe('OfflineDataManager - 数据同步', () => {
  let manager
  
  beforeEach(() => {
    jest.clearAllMocks()
    global.uni.getStorageSync.mockReturnValue([])
    manager = new OfflineDataManager()
    
    // Mock getUserToken
    manager.getUserToken = jest.fn().mockReturnValue('test-token')
  })
  
  it('应该在队列为空时跳过同步', async () => {
    const result = await manager.syncAll()
    
    expect(result).toBeUndefined()
    expect(global.uni.request).not.toHaveBeenCalled()
  })
  
  it('应该在同步进行中时跳过新的同步请求', async () => {
    manager.addAttendancePunch({ userId: 1 })
    manager.isSyncing = true
    
    const result = await manager.syncAll()
    
    expect(result).toBeUndefined()
    expect(global.uni.request).not.toHaveBeenCalled()
  })
  
  it('应该成功同步单个操作', async () => {
    manager.addAttendancePunch({ userId: 1 })
    
    global.uni.request.mockImplementation((options) => {
      options.success({ data: { code: 1, message: 'success' } })
    })
    
    const result = await manager.syncAll()
    
    expect(result.success).toBe(1)
    expect(result.failed).toBe(0)
    expect(manager.queue.length).toBe(0) // 成功的项已移除
  })
  
  it('应该处理同步失败的情况', async () => {
    manager.addAttendancePunch({ userId: 1 })
    
    global.uni.request.mockImplementation((options) => {
      options.success({ data: { code: 0, message: 'error' } })
    })
    
    const result = await manager.syncAll()
    
    expect(result.success).toBe(0)
    expect(result.failed).toBe(1)
    expect(manager.queue.length).toBe(1) // 失败的项仍在队列
    expect(manager.queue[0].status).toBe('failed')
    expect(manager.queue[0].retryCount).toBe(1)
  })
  
  it('应该处理网络错误', async () => {
    manager.addAttendancePunch({ userId: 1 })
    
    global.uni.request.mockImplementation((options) => {
      options.fail(new Error('Network error'))
    })
    
    const result = await manager.syncAll()
    
    expect(result.failed).toBe(1)
    expect(manager.queue[0].status).toBe('failed')
  })
  
  it('应该在超过最大重试次数后标记为error', async () => {
    const item = {
      id: 'test1',
      type: 'test',
      api: '/test',
      method: 'POST',
      data: {},
      status: 'failed',
      retryCount: 2,
      timestamp: Date.now()
    }
    manager.queue = [item]
    
    global.uni.request.mockImplementation((options) => {
      options.fail(new Error('Network error'))
    })
    
    await manager.syncAll()
    
    expect(manager.queue[0].status).toBe('error')
    expect(manager.queue[0].retryCount).toBe(3)
  })
  
  it('应该跳过已成功的项', async () => {
    const items = [
      {
        id: 'test1',
        type: 'test',
        status: 'success',
        api: '/test',
        method: 'POST',
        data: {},
        timestamp: Date.now()
      },
      {
        id: 'test2',
        type: 'test',
        status: 'pending',
        api: '/test',
        method: 'POST',
        data: {},
        timestamp: Date.now()
      }
    ]
    manager.queue = items
    
    global.uni.request.mockImplementation((options) => {
      options.success({ data: { code: 1 } })
    })
    
    const result = await manager.syncAll()
    
    expect(result.success).toBe(2) // 包括已成功的
    expect(global.uni.request).toHaveBeenCalledTimes(1) // 只调用pending的
  })
  
  it('应该在同步完成后更新同步时间', async () => {
    manager.addAttendancePunch({ userId: 1 })
    
    global.uni.request.mockImplementation((options) => {
      options.success({ data: { code: 1 } })
    })
    
    await manager.syncAll()
    
    expect(global.uni.setStorageSync).toHaveBeenCalledWith(
      'LAST_SYNC_TIME',
      expect.any(Number)
    )
  })
})

describe('OfflineDataManager - 队列状态', () => {
  let manager
  
  beforeEach(() => {
    jest.clearAllMocks()
    global.uni.getStorageSync.mockReturnValue([])
    manager = new OfflineDataManager()
  })
  
  it('应该正确统计队列状态', () => {
    manager.queue = [
      { id: '1', status: 'pending' },
      { id: '2', status: 'pending' },
      { id: '3', status: 'failed' },
      { id: '4', status: 'error' }
    ]
    
    const status = manager.getQueueStatus()
    
    expect(status.total).toBe(4)
    expect(status.pending).toBe(2)
    expect(status.failed).toBe(1)
    expect(status.error).toBe(1)
  })
  
  it('应该正确清除所有数据', () => {
    manager.queue = [
      { id: '1', status: 'pending' },
      { id: '2', status: 'success' }
    ]
    
    manager.clearAll()
    
    expect(manager.queue.length).toBe(0)
    expect(global.uni.setStorageSync).toHaveBeenCalled()
  })
  
  it('应该只清除成功的记录', () => {
    manager.queue = [
      { id: '1', status: 'success' },
      { id: '2', status: 'pending' },
      { id: '3', status: 'success' }
    ]
    
    manager.clearSuccess()
    
    expect(manager.queue.length).toBe(1)
    expect(manager.queue[0].id).toBe('2')
  })
})

describe('OfflineDataManager - 网络监听', () => {
  let manager
  let networkCallback
  
  beforeEach(() => {
    jest.clearAllMocks()
    global.uni.getStorageSync.mockReturnValue([])
    
    // 捕获网络监听回调
    global.uni.onNetworkStatusChange.mockImplementation((cb) => {
      networkCallback = cb
    })
    
    manager = new OfflineDataManager()
    manager.getUserToken = jest.fn().mockReturnValue('test-token')
  })
  
  it('应该注册网络状态监听', () => {
    expect(global.uni.onNetworkStatusChange).toHaveBeenCalled()
    expect(networkCallback).toBeDefined()
  })
  
  it('应该在网络恢复时自动同步', (done) => {
    manager.addAttendancePunch({ userId: 1 })
    
    global.uni.request.mockImplementation((options) => {
      options.success({ data: { code: 1 } })
    })
    
    // 模拟网络恢复
    networkCallback({ isConnected: true })
    
    // 等待延迟同步完成
    setTimeout(() => {
      expect(global.uni.request).toHaveBeenCalled()
      expect(global.uni.showToast).toHaveBeenCalled()
      done()
    }, 2500)
  })
  
  it('应该在网络断开时不触发同步', () => {
    manager.addAttendancePunch({ userId: 1 })
    
    // 模拟网络断开
    networkCallback({ isConnected: false })
    
    expect(global.uni.request).not.toHaveBeenCalled()
  })
})

describe('OfflineDataManager - ID生成', () => {
  let manager
  
  beforeEach(() => {
    jest.clearAllMocks()
    global.uni.getStorageSync.mockReturnValue([])
    manager = new OfflineDataManager()
  })
  
  it('应该生成唯一的ID', () => {
    const ids = new Set()
    
    for (let i = 0; i < 100; i++) {
      const id = manager.generateId()
      expect(id).toMatch(/^offline_\d+_[a-z0-9]+$/)
      ids.add(id)
    }
    
    // 所有ID应该是唯一的
    expect(ids.size).toBe(100)
  })
  
  it('ID应该包含时间戳', () => {
    const beforeTime = Date.now()
    const id = manager.generateId()
    const afterTime = Date.now()
    
    const timestamp = parseInt(id.split('_')[1])
    
    expect(timestamp).toBeGreaterThanOrEqual(beforeTime)
    expect(timestamp).toBeLessThanOrEqual(afterTime)
  })
})

describe('OfflineDataManager - 综合场景测试', () => {
  let manager
  
  beforeEach(() => {
    jest.clearAllMocks()
    global.uni.getStorageSync.mockReturnValue([])
    manager = new OfflineDataManager()
    manager.getUserToken = jest.fn().mockReturnValue('test-token')
  })
  
  it('应该正确处理混合操作场景', async () => {
    // 添加多种类型的离线操作
    manager.addAttendancePunch({ userId: 1 })
    manager.addConsumeTransaction({ accountId: 1, amount: 10 })
    manager.addVisitorCheckIn({ visitorId: 1 })
    
    expect(manager.queue.length).toBe(3)
    
    // 模拟部分成功、部分失败
    let callCount = 0
    global.uni.request.mockImplementation((options) => {
      callCount++
      if (callCount === 1 || callCount === 3) {
        options.success({ data: { code: 1 } })
      } else {
        options.fail(new Error('Network error'))
      }
    })
    
    const result = await manager.syncAll()
    
    expect(result.success).toBe(2)
    expect(result.failed).toBe(1)
    expect(manager.queue.length).toBe(1) // 只剩失败的
  })
  
  it('应该在多次同步后正确管理状态', async () => {
    manager.addAttendancePunch({ userId: 1 })
    manager.addAttendancePunch({ userId: 2 })
    
    // 第一次同步：全部失败
    global.uni.request.mockImplementation((options) => {
      options.fail(new Error('Network error'))
    })
    
    await manager.syncAll()
    expect(manager.queue.length).toBe(2)
    expect(manager.queue.every(item => item.retryCount === 1)).toBe(true)
    
    // 第二次同步：第一个成功
    let callCount = 0
    global.uni.request.mockImplementation((options) => {
      callCount++
      if (callCount === 1) {
        options.success({ data: { code: 1 } })
      } else {
        options.fail(new Error('Network error'))
      }
    })
    
    await manager.syncAll()
    expect(manager.queue.length).toBe(1)
    expect(manager.queue[0].retryCount).toBe(2)
  })
})

