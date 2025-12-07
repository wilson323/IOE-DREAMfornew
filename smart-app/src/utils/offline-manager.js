/**
 * 离线数据管理器
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

const STORAGE_KEYS = {
  OFFLINE_QUEUE: 'OFFLINE_DATA_QUEUE',
  SYNC_STATUS: 'OFFLINE_SYNC_STATUS',
  LAST_SYNC_TIME: 'LAST_SYNC_TIME'
}

/**
 * 离线数据管理器类
 */
export class OfflineDataManager {
  constructor() {
    this.queue = this.loadQueue()
    this.isSyncing = false
    this.setupNetworkListener()
  }

  /**
   * 加载离线队列
   */
  loadQueue() {
    try {
      const queue = uni.getStorageSync(STORAGE_KEYS.OFFLINE_QUEUE)
      return Array.isArray(queue) ? queue : []
    } catch (error) {
      console.error('加载离线队列失败:', error)
      return []
    }
  }

  /**
   * 保存离线队列
   */
  saveQueue() {
    try {
      uni.setStorageSync(STORAGE_KEYS.OFFLINE_QUEUE, this.queue)
    } catch (error) {
      console.error('保存离线队列失败:', error)
    }
  }

  /**
   * 添加离线操作
   */
  addOperation(operation) {
    const item = {
      id: this.generateId(),
      ...operation,
      timestamp: Date.now(),
      status: 'pending',
      retryCount: 0
    }

    this.queue.push(item)
    this.saveQueue()

    console.log('离线操作已保存:', item.type, item.id)

    return item.id
  }

  /**
   * 添加离线考勤打卡
   */
  addAttendancePunch(punchData) {
    return this.addOperation({
      type: 'attendance_punch',
      module: 'attendance',
      data: punchData,
      api: '/api/v1/mobile/attendance/punch',
      method: 'POST'
    })
  }

  /**
   * 添加离线消费
   */
  addConsumeTransaction(consumeData) {
    return this.addOperation({
      type: 'consume_transaction',
      module: 'consume',
      data: consumeData,
      api: '/api/v1/consume/mobile/transaction/quick',
      method: 'POST'
    })
  }

  /**
   * 添加离线访客签到
   */
  addVisitorCheckIn(checkInData) {
    return this.addOperation({
      type: 'visitor_checkin',
      module: 'visitor',
      data: checkInData,
      api: '/api/v1/mobile/visitor/checkin/qrcode',
      method: 'POST'
    })
  }

  /**
   * 添加离线门禁记录
   */
  addAccessRecord(recordData) {
    return this.addOperation({
      type: 'access_record',
      module: 'access',
      data: recordData,
      api: '/api/v1/access/record/create',
      method: 'POST'
    })
  }

  /**
   * 同步所有离线数据
   */
  async syncAll() {
    if (this.isSyncing) {
      console.log('同步正在进行中...')
      return
    }

    if (this.queue.length === 0) {
      console.log('没有待同步的离线数据')
      return
    }

    this.isSyncing = true
    console.log(`开始同步 ${this.queue.length} 条离线数据...`)

    const results = {
      success: 0,
      failed: 0,
      total: this.queue.length
    }

    // 逐个同步
    for (let i = 0; i < this.queue.length; i++) {
      const item = this.queue[i]

      if (item.status === 'success') {
        results.success++
        continue
      }

      try {
        await this.syncOperation(item)
        item.status = 'success'
        item.syncTime = Date.now()
        results.success++
        console.log('同步成功:', item.type, item.id)
      } catch (error) {
        item.status = 'failed'
        item.error = error.message
        item.retryCount++
        results.failed++
        console.error('同步失败:', item.type, item.id, error)

        // 超过最大重试次数则标记为失败
        if (item.retryCount >= 3) {
          item.status = 'error'
        }
      }
    }

    // 移除已成功的项
    this.queue = this.queue.filter(item => item.status !== 'success')
    this.saveQueue()

    // 更新同步状态
    uni.setStorageSync(STORAGE_KEYS.LAST_SYNC_TIME, Date.now())

    this.isSyncing = false

    console.log('同步完成:', results)
    return results
  }

  /**
   * 同步单个操作
   */
  async syncOperation(item) {
    return new Promise((resolve, reject) => {
      uni.request({
        url: (import.meta.env.VITE_APP_API_URL || '') + item.api,
        method: item.method,
        data: item.data,
        header: {
          'Authorization': 'Bearer ' + this.getUserToken(),
          'X-Offline-Sync': 'true',
          'X-Offline-Timestamp': item.timestamp
        },
        success: (res) => {
          if (res.data.code === 1) {
            resolve(res.data)
          } else {
            reject(new Error(res.data.message || '同步失败'))
          }
        },
        fail: (error) => {
          reject(error)
        }
      })
    })
  }

  /**
   * 获取用户Token
   */
  getUserToken() {
    return uni.getStorageSync('USER_TOKEN') || ''
  }

  /**
   * 设置网络监听（网络恢复时自动同步）
   */
  setupNetworkListener() {
    uni.onNetworkStatusChange(async (res) => {
      if (res.isConnected && this.queue.length > 0) {
        console.log('网络已恢复，开始自动同步离线数据...')

        // 延迟2秒后同步（等待连接稳定）
        setTimeout(async () => {
          const results = await this.syncAll()

          if (results && results.success > 0) {
            uni.showToast({
              title: `同步成功 ${results.success} 条记录`,
              icon: 'success',
              duration: 2000
            })
          }
        }, 2000)
      }
    })
  }

  /**
   * 获取队列状态
   */
  getQueueStatus() {
    return {
      total: this.queue.length,
      pending: this.queue.filter(i => i.status === 'pending').length,
      failed: this.queue.filter(i => i.status === 'failed').length,
      error: this.queue.filter(i => i.status === 'error').length
    }
  }

  /**
   * 清除所有离线数据
   */
  clearAll() {
    this.queue = []
    this.saveQueue()
    console.log('所有离线数据已清除')
  }

  /**
   * 清除已成功的记录
   */
  clearSuccess() {
    this.queue = this.queue.filter(item => item.status !== 'success')
    this.saveQueue()
  }

  /**
   * 生成唯一ID
   */
  generateId() {
    return `offline_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
}

/**
 * 全局离线数据管理器实例
 */
export const offlineManager = new OfflineDataManager()

export default offlineManager

