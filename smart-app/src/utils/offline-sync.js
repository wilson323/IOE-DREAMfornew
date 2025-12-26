/**
 * 离线同步核心模块
 * 负责离线数据同步、冲突解决、同步队列管理
 */

import { storage, STORAGE_KEYS } from './offline-storage.js'

/**
 * 同步状态枚举
 */
export const SYNC_STATUS = {
  IDLE: 'idle',           // 空闲
  SYNCING: 'syncing',     // 同步中
  SUCCESS: 'success',     // 成功
  ERROR: 'error',         // 失败
  CONFLICT: 'conflict'    // 冲突
}

/**
 * 数据类型枚举
 */
export const DATA_TYPES = {
  PERMISSION: 'permission',       // 权限数据
  PASS_RECORD: 'pass_record',     // 通行记录
  USER_INFO: 'user_info',         // 用户信息
  AREA_DATA: 'area_data'          // 区域数据
}

/**
 * 冲突解决策略
 */
export const CONFLICT_STRATEGY = {
  SERVER_WIN: 'server_win',     // 服务端优先
  CLIENT_WIN: 'client_win',     // 客户端优先
  MERGE: 'merge',               // 合并
  MANUAL: 'manual'              // 手动处理
}

/**
 * 离线同步管理类
 */
class OfflineSync {
  constructor() {
    this.syncStatus = SYNC_STATUS.IDLE
    this.syncQueue = []          // 同步队列
    this.maxRetry = 3            // 最大重试次数
    this.retryDelay = 5000       // 重试延迟(ms)
    this.syncInterval = 60000    // 同步间隔(ms)
    this.syncTimer = null        // 同步定时器

    this.listeners = new Map()   // 事件监听器
  }

  /**
   * 初始化同步模块
   */
  async init() {
    console.log('[离线同步] 初始化同步模块')

    // 恢复同步队列
    await this.restoreSyncQueue()

    // 监听网络状态
    this.watchNetworkStatus()

    // 启动定时同步
    this.startPeriodicSync()

    // 检查是否需要立即同步
    const lastSyncTime = await storage.get(STORAGE_KEYS.LAST_SYNC_TIME, 0)
    const now = Date.now()
    if (now - lastSyncTime > this.syncInterval) {
      this.sync()
    }
  }

  /**
   * 监听网络状态
   */
  watchNetworkStatus() {
    uni.onNetworkStatusChange((res) => {
      console.log('[离线同步] 网络状态变化:', res.isConnected)

      if (res.isConnected) {
        // 网络恢复，触发同步
        console.log('[离线同步] 网络已恢复，开始同步')
        this.sync()
      }
    })
  }

  /**
   * 启动定时同步
   */
  startPeriodicSync() {
    this.syncTimer = setInterval(() => {
      this.sync()
    }, this.syncInterval)

    console.log('[离线同步] 启动定时同步，间隔:', this.syncInterval / 1000, '秒')
  }

  /**
   * 停止定时同步
   */
  stopPeriodicSync() {
    if (this.syncTimer) {
      clearInterval(this.syncTimer)
      this.syncTimer = null
      console.log('[离线同步] 停止定时同步')
    }
  }

  /**
   * 开始同步
   * @param {Boolean} force 是否强制同步
   * @returns {Promise<Object>}
   */
  async sync(force = false) {
    // 检查网络状态
    const networkResult = await this.checkNetworkStatus()
    if (!networkResult.isConnected) {
      console.log('[离线同步] 无网络连接，跳过同步')
      return {
        success: false,
        message: '无网络连接',
        status: SYNC_STATUS.IDLE
      }
    }

    // 检查是否正在同步
    if (this.syncStatus === SYNC_STATUS.SYNCING && !force) {
      console.log('[离线同步] 正在同步中，跳过')
      return {
        success: false,
        message: '正在同步中',
        status: SYNC_STATUS.SYNCING
      }
    }

    console.log('[离线同步] 开始同步...')
    this.syncStatus = SYNC_STATUS.SYNCING
    this.emit('statusChange', SYNC_STATUS.SYNCING)

    try {
      // 1. 上传离线数据
      const uploadResult = await this.uploadOfflineData()
      console.log('[离线同步] 上传结果:', uploadResult)

      // 2. 下载服务端数据
      const downloadResult = await this.downloadServerData()
      console.log('[离线同步] 下载结果:', downloadResult)

      // 3. 解决数据冲突
      const conflictResult = await this.resolveConflicts()
      console.log('[离线同步] 冲突解决:', conflictResult)

      // 4. 更新同步时间
      await storage.set(STORAGE_KEYS.LAST_SYNC_TIME, Date.now())

      // 清空同步队列
      this.syncQueue = []
      await storage.set(STORAGE_KEYS.SYNC_QUEUE, [])

      this.syncStatus = SYNC_STATUS.SUCCESS
      this.emit('statusChange', SYNC_STATUS.SUCCESS)
      this.emit('syncComplete', {
        upload: uploadResult,
        download: downloadResult,
        conflict: conflictResult
      })

      console.log('[离线同步] 同步成功')
      return {
        success: true,
        message: '同步成功',
        status: SYNC_STATUS.SUCCESS,
        data: {
          upload: uploadResult,
          download: downloadResult,
          conflict: conflictResult
        }
      }
    } catch (error) {
      console.error('[离线同步] 同步失败:', error)

      this.syncStatus = SYNC_STATUS.ERROR
      this.emit('statusChange', SYNC_STATUS.ERROR)
      this.emit('syncError', error)

      // 加入重试队列
      this.addToRetryQueue()

      return {
        success: false,
        message: error.message || '同步失败',
        status: SYNC_STATUS.ERROR,
        error
      }
    }
  }

  /**
   * 检查网络状态
   * @returns {Promise<Object>}
   */
  async checkNetworkStatus() {
    return new Promise((resolve) => {
      uni.getNetworkType({
        success: (res) => {
          resolve({
            isConnected: res.networkType !== 'none',
            networkType: res.networkType
          })
        },
        fail: () => {
          resolve({ isConnected: false, networkType: 'none' })
        }
      })
    })
  }

  /**
   * 上传离线数据
   * @returns {Promise<Object>}
   */
  async uploadOfflineData() {
    console.log('[离线同步] 开始上传离线数据...')

    const result = {
      success: 0,
      failed: 0,
      details: []
    }

    try {
      // 1. 上传待同步的通行记录
      const pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])
      if (pendingRecords.length > 0) {
        console.log(`[离线同步] 上传 ${pendingRecords.length} 条通行记录`)

        // TODO: 调用后端接口上传
        // const uploadResult = await accessApi.uploadOfflineRecords(pendingRecords)

        result.success += pendingRecords.length
        result.details.push({
          type: DATA_TYPES.PASS_RECORD,
          count: pendingRecords.length
        })

        // 清空待上传记录
        await storage.set(STORAGE_KEYS.PENDING_RECORDS, [])
      }

      // 2. 上传其他离线数据（如用户操作日志）
      // TODO: 添加其他数据类型的上传逻辑

      console.log('[离线同步] 上传完成:', result)
      return result
    } catch (error) {
      console.error('[离线同步] 上传失败:', error)
      throw error
    }
  }

  /**
   * 下载服务端数据
   * @returns {Promise<Object>}
   */
  async downloadServerData() {
    console.log('[离线同步] 开始下载服务端数据...')

    const result = {
      permission: 0,
      userInfo: 0,
      areaData: 0,
      details: []
    }

    try {
      // 获取本地时间戳
      const lastSyncTime = await storage.get(STORAGE_KEYS.LAST_SYNC_TIME, 0)

      // 1. 下载权限数据（增量）
      // TODO: 调用后端接口获取增量数据
      // const permissionData = await accessApi.getPermissionUpdates(lastSyncTime)

      // 2. 下载用户信息
      // TODO: 调用后端接口

      // 3. 下载区域数据
      // TODO: 调用后端接口

      console.log('[离线同步] 下载完成:', result)
      return result
    } catch (error) {
      console.error('[离线同步] 下载失败:', error)
      throw error
    }
  }

  /**
   * 解决数据冲突
   * @returns {Promise<Object>}
   */
  async resolveConflicts() {
    console.log('[离线同步] 开始解决数据冲突...')

    const result = {
      resolved: 0,
      conflicts: []
    }

    try {
      // 比较本地和服务端数据的时间戳
      const localTimestamp = await storage.get(STORAGE_KEYS.PERMISSION_TIMESTAMP, 0)

      // TODO: 获取服务端时间戳
      // const serverTimestamp = await accessApi.getServerTimestamp()

      // 根据策略解决冲突
      // const strategy = await this.getConflictStrategy()
      // if (localTimestamp < serverTimestamp) {
      //   // 服务端数据更新，使用服务端数据
      //   await this.useServerData()
      // } else {
      //   // 本地数据更新，使用本地数据
      //   await this.useLocalData()
      // }

      console.log('[离线同步] 冲突解决完成:', result)
      return result
    } catch (error) {
      console.error('[离线同步] 冲突解决失败:', error)
      throw error
    }
  }

  /**
   * 添加到重试队列
   */
  async addToRetryQueue() {
    console.log('[离线同步] 添加到重试队列')

    // 检查重试次数
    const retryCount = this.syncQueue.filter(item => item.type === 'sync').length
    if (retryCount >= this.maxRetry) {
      console.log('[离线同步] 超过最大重试次数，放弃同步')
      return
    }

    // 添加到队列
    this.syncQueue.push({
      type: 'sync',
      timestamp: Date.now(),
      retryCount: retryCount + 1
    })

    await storage.set(STORAGE_KEYS.SYNC_QUEUE, this.syncQueue)

    // 延迟重试
    setTimeout(() => {
      this.sync()
    }, this.retryDelay)
  }

  /**
   * 恢复同步队列
   */
  async restoreSyncQueue() {
    const queue = await storage.get(STORAGE_KEYS.SYNC_QUEUE, [])
    this.syncQueue = queue || []
    console.log('[离线同步] 恢复同步队列:', this.syncQueue.length, '个任务')
  }

  /**
   * 获取同步状态
   * @returns {Object}
   */
  getSyncStatus() {
    return {
      status: this.syncStatus,
      queueLength: this.syncQueue.length,
      lastSyncTime: storage.get(STORAGE_KEYS.LAST_SYNC_TIME, 0),
      syncInterval: this.syncInterval
    }
  }

  /**
   * 事件监听
   * @param {String} event 事件名称
   * @param {Function} callback 回调函数
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 移除事件监听
   * @param {String} event 事件名称
   * @param {Function} callback 回调函数
   */
  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  /**
   * 触发事件
   * @param {String} event 事件名称
   * @param {*} data 事件数据
   */
  emit(event, data) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      callbacks.forEach(callback => callback(data))
    }
  }

  /**
   * 销毁同步模块
   */
  destroy() {
    this.stopPeriodicSync()
    this.listeners.clear()
    console.log('[离线同步] 销毁同步模块')
  }
}

// 导出单例
export const offlineSync = new OfflineSync()

// 导出工具函数
export const sync = {
  init: () => offlineSync.init(),
  sync: (force) => offlineSync.sync(force),
  getStatus: () => offlineSync.getSyncStatus(),
  on: (event, callback) => offlineSync.on(event, callback),
  off: (event, callback) => offlineSync.off(event, callback),
  destroy: () => offlineSync.destroy()
}

export default offlineSync
