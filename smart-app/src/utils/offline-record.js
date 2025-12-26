/**
 * 通行记录离线缓存模块
 * 负责离线通行记录的本地缓存、队列管理、批量上传
 */

import { storage, STORAGE_KEYS } from './offline-storage.js'
import { offlineSync } from './offline-sync.js'

/**
 * 通行结果枚举
 */
export const PASS_RESULT = {
  SUCCESS: 1,          // 成功
  FAILED: 2,           // 失败
  DENIED: 3,           // 拒绝
  TIMEOUT: 4           // 超时
}

/**
 * 通行方式枚举
 */
export const PASS_METHOD = {
  FACE: 1,            // 人脸识别
  FINGERPRINT: 2,      // 指纹
  IC_CARD: 3,          // IC卡
  PASSWORD: 4,         // 密码
  QR_CODE: 5,          // 二维码
  BLUETOOTH: 6         // 蓝牙
}

/**
 * 通行记录管理类
 */
class OfflineRecord {
  constructor() {
    this.maxPendingRecords = 1000  // 最大待上传记录数
    this.uploadBatchSize = 50       // 批量上传大小
  }

  /**
   * 初始化记录模块
   */
  async init() {
    console.log('[通行记录] 初始化离线记录模块')

    // 监听同步完成事件
    offlineSync.on('syncComplete', () => {
      console.log('[通行记录] 同步完成，清除已上传记录')
      this.clearUploadedRecords()
    })
  }

  /**
   * 添加通行记录
   * @param {Object} record 通行记录
   * @returns {Promise<Boolean>}
   */
  async addRecord(record) {
    try {
      // 构造完整记录对象
      const fullRecord = {
        recordId: this.generateRecordId(),
        userId: record.userId,
        userName: record.userName,
        deviceId: record.deviceId,
        deviceName: record.deviceName,
        areaId: record.areaId,
        areaName: record.areaName,
        permissionId: record.permissionId,
        passMethod: record.passMethod,
        passResult: record.passResult,
        passTime: record.passTime || Date.now(),
        offlineMode: true,  // 标记为离线记录
        location: record.location || null,
        photo: record.photo || null,
        reason: record.reason || null,
        createTime: Date.now()
      }

      // 获取待上传记录列表
      let pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])

      // 检查是否超过最大数量
      if (pendingRecords.length >= this.maxPendingRecords) {
        console.warn('[通行记录] 待上传记录已满，删除最旧的记录')
        pendingRecords = pendingRecords.slice(0, this.maxPendingRecords - 1)
      }

      // 添加到队列头部
      pendingRecords.unshift(fullRecord)

      // 保存到本地
      await storage.set(STORAGE_KEYS.PENDING_RECORDS, pendingRecords)

      console.log(`[通行记录] 添加离线记录: ${fullRecord.recordId}`)
      return true
    } catch (error) {
      console.error('[通行记录] 添加记录失败:', error)
      return false
    }
  }

  /**
   * 批量添加通行记录
   * @param {Array} records 通行记录数组
   * @returns {Promise<Object>}
   */
  async addRecords(records) {
    try {
      console.log(`[通行记录] 批量添加 ${records.length} 条记录`)

      let successCount = 0
      let failedCount = 0

      for (const record of records) {
        const result = await this.addRecord(record)
        if (result) {
          successCount++
        } else {
          failedCount++
        }
      }

      return {
        success: true,
        total: records.length,
        successCount,
        failedCount
      }
    } catch (error) {
      console.error('[通行记录] 批量添加失败:', error)
      return {
        success: false,
        message: error.message
      }
    }
  }

  /**
   * 获取待上传记录列表
   * @param {Number} limit 限制数量
   * @returns {Promise<Array>}
   */
  async getPendingRecords(limit = 0) {
    try {
      let pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])

      if (limit > 0) {
        pendingRecords = pendingRecords.slice(0, limit)
      }

      console.log(`[通行记录] 获取待上传记录: ${pendingRecords.length} 条`)
      return pendingRecords
    } catch (error) {
      console.error('[通行记录] 获取待上传记录失败:', error)
      return []
    }
  }

  /**
   * 上传待上传记录
   * @returns {Promise<Object>}
   */
  async uploadPendingRecords() {
    try {
      console.log('[通行记录] 开始上传待上传记录...')

      // 获取待上传记录
      const pendingRecords = await this.getPendingRecords(this.uploadBatchSize)

      if (pendingRecords.length === 0) {
        console.log('[通行记录] 没有待上传记录')
        return { success: true, uploaded: 0, failed: 0 }
      }

      // TODO: 调用后端接口上传
      // const result = await accessApi.uploadOfflineRecords(pendingRecords)

      // 模拟上传成功
      const uploadedCount = pendingRecords.length
      console.log(`[通行记录] 上传成功: ${uploadedCount} 条`)

      // 从待上传列表中移除已上传的记录
      await this.removeUploadedRecords(pendingRecords.map(r => r.recordId))

      return {
        success: true,
        uploaded: uploadedCount,
        failed: 0
      }
    } catch (error) {
      console.error('[通行记录] 上传失败:', error)
      return {
        success: false,
        uploaded: 0,
        failed: await this.getPendingRecords().then(r => r.length),
        message: error.message
      }
    }
  }

  /**
   * 移除已上传的记录
   * @param {Array<String>} recordIds 记录ID数组
   */
  async removeUploadedRecords(recordIds) {
    try {
      let pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])

      const beforeCount = pendingRecords.length
      pendingRecords = pendingRecords.filter(r =>
        !recordIds.includes(r.recordId)
      )

      await storage.set(STORAGE_KEYS.PENDING_RECORDS, pendingRecords)

      console.log(`[通行记录] 移除已上传记录: ${beforeCount} -> ${pendingRecords.length}`)
      return { success: true, removed: beforeCount - pendingRecords.length }
    } catch (error) {
      console.error('[通行记录] 移除记录失败:', error)
      return { success: false, message: error.message }
    }
  }

  /**
   * 清除所有已上传记录
   */
  async clearUploadedRecords() {
    try {
      await storage.set(STORAGE_KEYS.PENDING_RECORDS, [])
      console.log('[通行记录] 清空待上传记录')
      return { success: true }
    } catch (error) {
      console.error('[通行记录] 清空失败:', error)
      return { success: false, message: error.message }
    }
  }

  /**
   * 获取离线通行记录统计
   * @returns {Promise<Object>}
   */
  async getStatistics() {
    try {
      const pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])

      // 按结果统计
      const byResult = {
        success: 0,
        failed: 0,
        denied: 0,
        timeout: 0
      }

      // 按方式统计
      const byMethod = {
        face: 0,
        fingerprint: 0,
        icCard: 0,
        password: 0,
        qrcode: 0,
        bluetooth: 0
      }

      // 今日记录
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const todayRecords = []

      pendingRecords.forEach(record => {
        // 结果统计
        if (record.passResult === PASS_RESULT.SUCCESS) byResult.success++
        else if (record.passResult === PASS_RESULT.FAILED) byResult.failed++
        else if (record.passResult === PASS_RESULT.DENIED) byResult.denied++
        else if (record.passResult === PASS_RESULT.TIMEOUT) byResult.timeout++

        // 方式统计
        if (record.passMethod === PASS_METHOD.FACE) byMethod.face++
        else if (record.passMethod === PASS_METHOD.FINGERPRINT) byMethod.fingerprint++
        else if (record.passMethod === PASS_METHOD.IC_CARD) byMethod.icCard++
        else if (record.passMethod === PASS_METHOD.PASSWORD) byMethod.password++
        else if (record.passMethod === PASS_METHOD.QR_CODE) byMethod.qrcode++
        else if (record.passMethod === PASS_METHOD.BLUETOOTH) byMethod.bluetooth++

        // 今日记录
        const recordDate = new Date(record.passTime)
        if (recordDate >= today) {
          todayRecords.push(record)
        }
      })

      const stats = {
        total: pendingRecords.length,
        byResult,
        byMethod,
        todayCount: todayRecords.length,
        oldestRecord: pendingRecords.length > 0 ? pendingRecords[pendingRecords.length - 1].passTime : null,
        newestRecord: pendingRecords.length > 0 ? pendingRecords[0].passTime : null
      }

      console.log('[通行记录] 统计信息:', stats)
      return stats
    } catch (error) {
      console.error('[通行记录] 统计失败:', error)
      return null
    }
  }

  /**
   * 获取今日通行记录
   * @param {Number} limit 限制数量
   * @returns {Promise<Array>}
   */
  async getTodayRecords(limit = 50) {
    try {
      const pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])

      const today = new Date()
      today.setHours(0, 0, 0, 0)

      const todayRecords = pendingRecords.filter(record => {
        const recordDate = new Date(record.passTime)
        return recordDate >= today
      })

      if (limit > 0) {
        return todayRecords.slice(0, limit)
      }

      return todayRecords
    } catch (error) {
      console.error('[通行记录] 获取今日记录失败:', error)
      return []
    }
  }

  /**
   * 搜索通行记录
   * @param {Object} filters 搜索条件
   * @returns {Promise<Array>}
   */
  async searchRecords(filters = {}) {
    try {
      let pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])

      // 应用过滤条件
      if (filters.userId) {
        pendingRecords = pendingRecords.filter(r => r.userId === filters.userId)
      }
      if (filters.deviceId) {
        pendingRecords = pendingRecords.filter(r => r.deviceId === filters.deviceId)
      }
      if (filters.areaId) {
        pendingRecords = pendingRecords.filter(r => r.areaId === filters.areaId)
      }
      if (filters.passResult) {
        pendingRecords = pendingRecords.filter(r => r.passResult === filters.passResult)
      }
      if (filters.startTime && filters.endTime) {
        pendingRecords = pendingRecords.filter(r => {
          return r.passTime >= filters.startTime && r.passTime <= filters.endTime
        })
      }

      // 限制返回数量
      if (filters.limit) {
        pendingRecords = pendingRecords.slice(0, filters.limit)
      }

      console.log(`[通行记录] 搜索结果: ${pendingRecords.length} 条`)
      return pendingRecords
    } catch (error) {
      console.error('[通行记录] 搜索失败:', error)
      return []
    }
  }

  /**
   * 生成记录ID
   * @returns {String}
   */
  generateRecordId() {
    const timestamp = Date.now().toString(36)
    const random = Math.random().toString(36).substring(2, 9)
    return `offline_${timestamp}_${random}`
  }

  /**
   * 导出通行记录
   * @param {String} format 导出格式 (json/csv)
   * @returns {Promise<String>}
   */
  async exportRecords(format = 'json') {
    try {
      const pendingRecords = await storage.get(STORAGE_KEYS.PENDING_RECORDS, [])

      if (format === 'json') {
        return JSON.stringify(pendingRecords, null, 2)
      } else if (format === 'csv') {
        // 生成CSV格式
        const headers = ['记录ID', '用户', '设备', '区域', '通行方式', '结果', '时间']
        const rows = pendingRecords.map(r => [
          r.recordId,
          r.userName,
          r.deviceName,
          r.areaName,
          this.getPassMethodName(r.passMethod),
          this.getPassResultName(r.passResult),
          new Date(r.passTime).toLocaleString()
        ])

        const csv = [headers, ...rows]
          .map(row => row.join(','))
          .join('\n')

        return csv
      }

      throw new Error('不支持的导出格式')
    } catch (error) {
      console.error('[通行记录] 导出失败:', error)
      return null
    }
  }

  /**
   * 获取通行方式名称
   * @param {Number} method 通行方式
   * @returns {String}
   */
  getPassMethodName(method) {
    const names = {
      [PASS_METHOD.FACE]: '人脸识别',
      [PASS_METHOD.FINGERPRINT]: '指纹',
      [PASS_METHOD.IC_CARD]: 'IC卡',
      [PASS_METHOD.PASSWORD]: '密码',
      [PASS_METHOD.QR_CODE]: '二维码',
      [PASS_METHOD.BLUETOOTH]: '蓝牙'
    }
    return names[method] || '未知'
  }

  /**
   * 获取通行结果名称
   * @param {Number} result 通行结果
   * @returns {String}
   */
  getPassResultName(result) {
    const names = {
      [PASS_RESULT.SUCCESS]: '成功',
      [PASS_RESULT.FAILED]: '失败',
      [PASS_RESULT.DENIED]: '拒绝',
      [PASS_RESULT.TIMEOUT]: '超时'
    }
    return names[result] || '未知'
  }
}

// 导出单例
export const offlineRecord = new OfflineRecord()

// 导出工具函数
export const record = {
  init: () => offlineRecord.init(),
  add: (record) => offlineRecord.addRecord(record),
  addBatch: (records) => offlineRecord.addRecords(records),
  getPending: (limit) => offlineRecord.getPendingRecords(limit),
  upload: () => offlineRecord.uploadPendingRecords(),
  getStatistics: () => offlineRecord.getStatistics(),
  getToday: (limit) => offlineRecord.getTodayRecords(limit),
  search: (filters) => offlineRecord.searchRecords(filters),
  export: (format) => offlineRecord.exportRecords(format)
}

export default offlineRecord
