/**
 * 离线消费IndexedDB封装工具
 * 用于管理离线消费记录的本地存储和同步
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */

import { request } from '@/api/request'

const DB_NAME = 'IOE_DREAM_OFFLINE_CONSUME'
const DB_VERSION = 1
const STORE_RECORDS = 'consume_records'
const STORE_SYNC_LOG = 'sync_logs'
const STORE_WHITELIST = 'whitelist'

/**
 * 离线消费数据库管理类
 */
class OfflineConsumeDB {
  constructor() {
    this.db = null
    this.initPromise = null
  }

  /**
   * 初始化数据库
   */
  async init() {
    if (this.initPromise) {
      return this.initPromise
    }

    this.initPromise = new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION)

      request.onerror = () => {
        console.error('[离线消费DB] 数据库打开失败:', request.error)
        reject(request.error)
      }

      request.onsuccess = () => {
        this.db = request.result
        console.log('[离线消费DB] 数据库初始化成功')
        resolve(this.db)
      }

      request.onupgradeneeded = (event) => {
        const db = event.target.result

        // 创建消费记录存储
        if (!db.objectStoreNames.contains(STORE_RECORDS)) {
          const recordStore = db.createObjectStore(STORE_RECORDS, { keyPath: 'id' })
          recordStore.createIndex('userId', 'userId', { unique: false })
          recordStore.createIndex('deviceId', 'deviceId', { unique: false })
          recordStore.createIndex('syncStatus', 'syncStatus', { unique: false })
          recordStore.createIndex('consumeTime', 'consumeTime', { unique: false })
          console.log('[离线消费DB] 消费记录存储创建成功')
        }

        // 创建同步日志存储
        if (!db.objectStoreNames.contains(STORE_SYNC_LOG)) {
          const logStore = db.createObjectStore(STORE_SYNC_LOG, { keyPath: 'id', autoIncrement: true })
          logStore.createIndex('syncBatchNo', 'syncBatchNo', { unique: false })
          logStore.createIndex('syncTime', 'syncTime', { unique: false })
          console.log('[离线消费DB] 同步日志存储创建成功')
        }

        // 创建白名单存储
        if (!db.objectStoreNames.contains(STORE_WHITELIST)) {
          const whitelistStore = db.createObjectStore(STORE_WHITELIST, { keyPath: 'id', autoIncrement: true })
          whitelistStore.createIndex('userId', 'userId', { unique: false })
          whitelistStore.createIndex('deviceId', 'deviceId', { unique: false })
          console.log('[离线消费DB] 白名单存储创建成功')
        }
      }
    })

    return this.initPromise
  }

  /**
   * 添加消费记录
   */
  async addRecord(record) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_RECORDS], 'readwrite')
      const store = transaction.objectStore(STORE_RECORDS)
      const request = store.add({
        ...record,
        syncStatus: 0, // 待同步
        createTime: new Date().toISOString()
      })

      request.onsuccess = () => {
        console.log('[离线消费DB] 添加记录成功:', record.id)
        resolve(record)
      }

      request.onerror = () => {
        console.error('[离线消费DB] 添加记录失败:', request.error)
        reject(request.error)
      }
    })
  }

  /**
   * 批量添加消费记录
   */
  async batchAddRecords(records) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_RECORDS], 'readwrite')
      const store = transaction.objectStore(STORE_RECORDS)

      let completed = 0
      const results = []

      records.forEach((record) => {
        const request = store.add({
          ...record,
          syncStatus: 0,
          createTime: new Date().toISOString()
        })

        request.onsuccess = () => {
          results.push(record)
          completed++
          if (completed === records.length) {
            console.log(`[离线消费DB] 批量添加记录成功: ${results.length}条`)
            resolve(results)
          }
        }

        request.onerror = () => {
          console.error('[离线消费DB] 批量添加失败:', request.error)
          reject(request.error)
        }
      })
    })
  }

  /**
   * 查询待同步记录
   */
  async getPendingRecords() {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_RECORDS], 'readonly')
      const store = transaction.objectStore(STORE_RECORDS)
      const index = store.index('syncStatus')
      const request = index.getAll(0) // syncStatus = 0 待同步

      request.onsuccess = () => {
        const records = request.result || []
        console.log(`[离线消费DB] 查询待同步记录: ${records.length}条`)
        resolve(records)
      }

      request.onerror = () => {
        console.error('[离线消费DB] 查询待同步记录失败:', request.error)
        reject(request.error)
      }
    })
  }

  /**
   * 查询用户待同步记录
   */
  async getPendingRecordsByUserId(userId) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_RECORDS], 'readonly')
      const store = transaction.objectStore(STORE_RECORDS)
      const userIdIndex = store.index('userId')
      const syncStatusIndex = store.index('syncStatus')

      const results = []
      let pendingCompleted = false

      // 先通过userId索引查询
      const userIdRequest = userIdIndex.openCursor(IDBKeyRange.only(userId))

      userIdRequest.onsuccess = (event) => {
        const cursor = event.target.result
        if (cursor) {
          if (cursor.value.syncStatus === 0) {
            results.push(cursor.value)
          }
          cursor.continue()
        } else {
          console.log(`[离线消费DB] 用户待同步记录: userId=${userId}, count=${results.length}`)
          resolve(results)
        }
      }

      userIdRequest.onerror = () => {
        console.error('[离线消费DB] 查询用户待同步记录失败:', userIdRequest.error)
        reject(userIdRequest.error)
      }
    })
  }

  /**
   * 更新记录同步状态
   */
  async updateRecordSyncStatus(recordId, syncStatus, errorMessage = null) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_RECORDS], 'readwrite')
      const store = transaction.objectStore(STORE_RECORDS)
      const getRequest = store.get(recordId)

      getRequest.onsuccess = () => {
        const record = getRequest.result
        if (record) {
          record.syncStatus = syncStatus
          record.syncTime = new Date().toISOString()
          if (errorMessage) {
            record.errorMessage = errorMessage
          }

          const putRequest = store.put(record)
          putRequest.onsuccess = () => {
            console.log(`[离线消费DB] 更新同步状态: recordId=${recordId}, status=${syncStatus}`)
            resolve(record)
          }
          putRequest.onerror = () => reject(putRequest.error)
        } else {
          reject(new Error('记录不存在'))
        }
      }

      getRequest.onerror = () => reject(getRequest.error)
    })
  }

  /**
   * 删除已同步记录
   */
  async deleteSyncedRecords(daysToKeep = 7) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_RECORDS], 'readwrite')
      const store = transaction.objectStore(STORE_RECORDS)
      const syncStatusIndex = store.index('syncStatus')

      const cutoffDate = new Date()
      cutoffDate.setDate(cutoffDate.getDate() - daysToKeep)

      const results = []
      const request = syncStatusIndex.openCursor(IDBKeyRange.only(2)) // 已同步

      request.onsuccess = (event) => {
        const cursor = event.target.result
        if (cursor) {
          const record = cursor.value
          const syncTime = new Date(record.syncTime)

          if (syncTime < cutoffDate) {
            cursor.delete()
            results.push(record.id)
          }
          cursor.continue()
        } else {
          console.log(`[离线消费DB] 清理已同步记录: ${results.length}条`)
          resolve(results)
        }
      }

      request.onerror = () => reject(request.error)
    })
  }

  /**
   * 保存同步日志
   */
  async saveSyncLog(syncLog) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_SYNC_LOG], 'readwrite')
      const store = transaction.objectStore(STORE_SYNC_LOG)
      const request = store.add({
        ...syncLog,
        createTime: new Date().toISOString()
      })

      request.onsuccess = () => {
        console.log('[离线消费DB] 保存同步日志成功:', syncLog.syncBatchNo)
        resolve(syncLog)
      }

      request.onerror = () => {
        console.error('[离线消费DB] 保存同步日志失败:', request.error)
        reject(request.error)
      }
    })
  }

  /**
   * 查询最近同步日志
   */
  async getRecentSyncLogs(limit = 10) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_SYNC_LOG], 'readonly')
      const store = transaction.objectStore(STORE_SYNC_LOG)
      const syncTimeIndex = store.index('syncTime')
      const request = syncTimeIndex.openCursor(null, 'prev') // 倒序

      const results = []
      let count = 0

      request.onsuccess = (event) => {
        const cursor = event.target.result
        if (cursor && count < limit) {
          results.push(cursor.value)
          count++
          cursor.continue()
        } else {
          console.log(`[离线消费DB] 查询最近同步日志: ${results.length}条`)
          resolve(results)
        }
      }

      request.onerror = () => reject(request.error)
    })
  }

  /**
   * 保存白名单
   */
  async saveWhitelist(whitelist) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_WHITELIST], 'readwrite')
      const store = transaction.objectStore(STORE_WHITELIST)

      // 先清除旧白名单
      const clearRequest = store.clear()

      clearRequest.onsuccess = () => {
        // 批量添加新白名单
        let completed = 0
        const results = []

        whitelist.forEach((item) => {
          const addRequest = store.add(item)

          addRequest.onsuccess = () => {
            results.push(item)
            completed++
            if (completed === whitelist.length) {
              console.log(`[离线消费DB] 保存白名单成功: ${results.length}条`)
              resolve(results)
            }
          }

          addRequest.onerror = () => reject(addRequest.error)
        })
      }

      clearRequest.onerror = () => reject(clearRequest.error)
    })
  }

  /**
   * 查询用户白名单
   */
  async getWhitelistByUserId(userId) {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_WHITELIST], 'readonly')
      const store = transaction.objectStore(STORE_WHITELIST)
      const index = store.index('userId')
      const request = index.getAll(userId)

      request.onsuccess = () => {
        const whitelist = request.result || []
        console.log(`[离线消费DB] 查询用户白名单: userId=${userId}, count=${whitelist.length}`)
        resolve(whitelist)
      }

      request.onerror = () => {
        console.error('[离线消费DB] 查询白名单失败:', request.error)
        reject(request.error)
      }
    })
  }

  /**
   * 验证用户是否在白名单中
   */
  async isWhitelistUser(userId, deviceId) {
    await this.init()
    const whitelist = await this.getWhitelistByUserId(userId)
    return whitelist.some(item => item.deviceId === deviceId || item.deviceId === 0)
  }

  /**
   * 清空所有数据
   */
  async clearAll() {
    await this.init()
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_RECORDS, STORE_SYNC_LOG, STORE_WHITELIST], 'readwrite')

      let completed = 0

      transaction.oncomplete = () => {
        console.log('[离线消费DB] 清空所有数据成功')
        resolve()
      }

      transaction.onerror = () => {
        console.error('[离线消费DB] 清空所有数据失败:', transaction.error)
        reject(transaction.error)
      }

      // 清空各个存储
      [STORE_RECORDS, STORE_SYNC_LOG, STORE_WHITELIST].forEach((storeName) => {
        const store = transaction.objectStore(storeName)
        store.clear().onsuccess = () => {
          completed++
        }
      })
    })
  }
}

// 创建单例实例
const offlineConsumeDB = new OfflineConsumeDB()

/**
 * 同步服务
 */
export const OfflineSyncService = {
  /**
   * 同步到服务器
   */
  async syncToServer() {
    try {
      console.log('[离线同步] 开始同步...')

      // 获取待同步记录
      const pendingRecords = await offlineConsumeDB.getPendingRecords()

      if (pendingRecords.length === 0) {
        console.log('[离线同步] 没有待同步记录')
        return { success: true, message: '没有待同步记录' }
      }

      console.log(`[离线同步] 待同步记录: ${pendingRecords.length}条`)

      // 调用服务器API
      const response = await request({
        url: '/api/consume/offline/sync',
        method: 'post',
        data: pendingRecords
      })

      if (response.code === 200) {
        const result = response.data

        // 保存同步日志
        await offlineConsumeDB.saveSyncLog({
          syncBatchNo: result.syncBatchNo,
          totalCount: result.totalCount,
          successCount: result.successCount,
          failedCount: result.failedCount,
          conflictCount: result.conflictCount,
          durationMs: result.durationMs
        })

        // 更新记录状态
        for (const record of pendingRecords) {
          await offlineConsumeDB.updateRecordSyncStatus(record.id, 2) // 已同步
        }

        console.log('[离线同步] 同步成功:', result)
        return { success: true, data: result }
      } else {
        throw new Error(response.message || '同步失败')
      }
    } catch (error) {
      console.error('[离线同步] 同步异常:', error)

      // 更新失败记录状态
      const pendingRecords = await offlineConsumeDB.getPendingRecords()
      for (const record of pendingRecords) {
        await offlineConsumeDB.updateRecordSyncStatus(record.id, 3, error.message) // 冲突/失败
      }

      return { success: false, error: error.message }
    }
  },

  /**
   * 添加消费记录
   */
  async addRecord(record) {
    return offlineConsumeDB.addRecord(record)
  },

  /**
   * 批量添加消费记录
   */
  async batchAddRecords(records) {
    return offlineConsumeDB.batchAddRecords(records)
  },

  /**
   * 获取待同步记录
   */
  async getPendingRecords() {
    return offlineConsumeDB.getPendingRecords()
  },

  /**
   * 获取用户待同步记录
   */
  async getPendingRecordsByUserId(userId) {
    return offlineConsumeDB.getPendingRecordsByUserId(userId)
  },

  /**
   * 获取最近同步日志
   */
  async getRecentSyncLogs(limit = 10) {
    return offlineConsumeDB.getRecentSyncLogs(limit)
  },

  /**
   * 保存白名单
   */
  async saveWhitelist(whitelist) {
    return offlineConsumeDB.saveWhitelist(whitelist)
  },

  /**
   * 验证用户是否在白名单中
   */
  async isWhitelistUser(userId, deviceId) {
    return offlineConsumeDB.isWhitelistUser(userId, deviceId)
  },

  /**
   * 清空所有数据
   */
  async clearAll() {
    return offlineConsumeDB.clearAll()
  }
}

export default offlineConsumeDB
