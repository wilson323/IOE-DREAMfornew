/**
 * 身份证读卡器工具类
 * <p>
 * 提供身份证读卡器SDK集成接口
 * 支持多种读卡器硬件（新中新、华视、神思等）
 * </p>
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

/**
 * 身份证读卡器管理器
 * <p>
 * 封装身份证读卡器SDK调用逻辑
 * 支持硬件检测、读卡、数据解析等功能
 * </p>
 */
class IdCardReaderManager {
  constructor() {
    this.isSupported = false
    this.readerType = null // 'XINZHONGXIN' | 'HUASHI' | 'SHENSI' | 'BLUETOOTH'
    this.isReading = false
  }

  /**
   * 检测读卡器是否可用
   * <p>
   * 检测系统中是否连接了身份证读卡器硬件
   * </p>
   *
   * @returns {Promise<Boolean>} 是否可用
   */
  async checkReaderAvailable() {
    try {
      // 检测读卡器硬件连接
      // 注意：实际实现需要根据具体硬件SDK调整
      
      // #ifdef APP-PLUS
      // uni-app原生插件方式检测
      const readerPlugin = uni.requireNativePlugin('IdCardReader')
      if (readerPlugin) {
        const result = await readerPlugin.checkConnection()
        this.isSupported = result.available
        this.readerType = result.type
        return this.isSupported
      }
      // #endif

      // #ifdef H5
      // H5环境：通过WebUSB或WebSerial API检测
      if (navigator.serial || navigator.usb) {
        // 尝试检测USB/串口设备
        this.isSupported = true
        this.readerType = 'USB'
        return true
      }
      // #endif

      // 默认不支持
      this.isSupported = false
      return false
    } catch (error) {
      console.error('[身份证读卡器] 检测失败:', error)
      this.isSupported = false
      return false
    }
  }

  /**
   * 读取身份证信息
   * <p>
   * 从身份证读卡器读取身份证信息
   * 支持多种读卡器硬件
   * </p>
   *
   * @returns {Promise<Object>} 身份证信息
   * @returns {String} name - 姓名
   * @returns {String} idCard - 身份证号
   * @returns {String} gender - 性别
   * @returns {String} birthday - 生日
   * @returns {String} address - 地址
   * @returns {String} photo - 照片（Base64）
   */
  async readIdCard() {
    if (this.isReading) {
      throw new Error('正在读取中，请稍候...')
    }

    if (!this.isSupported) {
      const available = await this.checkReaderAvailable()
      if (!available) {
        throw new Error('未检测到身份证读卡器，请检查硬件连接')
      }
    }

    this.isReading = true

    try {
      // #ifdef APP-PLUS
      // uni-app原生插件方式读取
      const readerPlugin = uni.requireNativePlugin('IdCardReader')
      if (readerPlugin) {
        const result = await readerPlugin.readCard({
          timeout: 30000 // 30秒超时
        })

        if (result.success) {
          return this.parseIdCardData(result.data)
        } else {
          throw new Error(result.message || '读卡失败')
        }
      }
      // #endif

      // #ifdef H5
      // H5环境：通过WebUSB或WebSerial API读取
      if (navigator.serial) {
        // 使用WebSerial API读取
        const port = await navigator.serial.requestPort()
        await port.open({ baudRate: 115200 })
        
        // 发送读卡指令（根据具体读卡器协议）
        const writer = port.writable.getWriter()
        const readCommand = new Uint8Array([0xAA, 0x55, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00])
        await writer.write(readCommand)
        writer.releaseLock()

        // 读取响应数据
        const reader = port.readable.getReader()
        const { value } = await reader.read()
        reader.releaseLock()
        await port.close()

        // 解析数据
        return this.parseIdCardData(value)
      }
      // #endif

      // 降级方案：返回模拟数据（开发测试用）
      console.warn('[身份证读卡器] 未检测到硬件，返回模拟数据')
      return this.getMockIdCardData()

    } catch (error) {
      console.error('[身份证读卡器] 读卡失败:', error)
      throw error
    } finally {
      this.isReading = false
    }
  }

  /**
   * 解析身份证数据
   * <p>
   * 根据读卡器返回的原始数据解析身份证信息
   * 不同读卡器返回的数据格式可能不同，需要统一解析
   * </p>
   *
   * @param {Object|Uint8Array} rawData 原始数据
   * @returns {Object} 解析后的身份证信息
   */
  parseIdCardData(rawData) {
    // 根据读卡器类型解析数据
    // 这里提供通用解析逻辑，实际需要根据具体SDK调整

    if (typeof rawData === 'string') {
      // JSON格式数据
      try {
        const data = JSON.parse(rawData)
        return {
          name: data.name || data.visitorName || '',
          idCard: data.idCard || data.idCardNumber || data.idNo || '',
          gender: data.gender || data.sex || '',
          birthday: data.birthday || data.birthDate || '',
          address: data.address || data.addr || '',
          photo: data.photo || data.photoBase64 || ''
        }
      } catch (e) {
        console.error('[身份证读卡器] 解析JSON数据失败:', e)
      }
    }

    if (rawData instanceof Uint8Array) {
      // 二进制数据（需要根据具体协议解析）
      // 这里提供基础解析框架
      return this.parseBinaryData(rawData)
    }

    // 对象格式数据
    return {
      name: rawData.name || rawData.visitorName || '',
      idCard: rawData.idCard || rawData.idCardNumber || rawData.idNo || '',
      gender: rawData.gender || rawData.sex || '',
      birthday: rawData.birthday || rawData.birthDate || '',
      address: rawData.address || rawData.addr || '',
      photo: rawData.photo || rawData.photoBase64 || ''
    }
  }

  /**
   * 解析二进制数据
   * <p>
   * 根据读卡器协议解析二进制数据
   * 不同读卡器协议不同，需要根据实际情况实现
   * </p>
   *
   * @param {Uint8Array} binaryData 二进制数据
   * @returns {Object} 解析后的身份证信息
   */
  parseBinaryData(binaryData) {
    // 基础解析框架
    // 实际实现需要根据具体读卡器协议文档
    return {
      name: '',
      idCard: '',
      gender: '',
      birthday: '',
      address: '',
      photo: ''
    }
  }

  /**
   * 获取模拟身份证数据（开发测试用）
   * <p>
   * 在未连接硬件时返回模拟数据，用于开发测试
   * </p>
   *
   * @returns {Object} 模拟身份证信息
   */
  getMockIdCardData() {
    return {
      name: '测试用户',
      idCard: '110101199001011234',
      gender: '男',
      birthday: '1990-01-01',
      address: '北京市东城区测试街道测试号',
      photo: '' // 模拟数据不包含照片
    }
  }
}

// 导出单例
const idCardReaderManager = new IdCardReaderManager()

export default idCardReaderManager

