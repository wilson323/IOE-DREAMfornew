import { message } from 'ant-design-vue'

/**
 * 统一错误处理工具
 */
export const errorHandler = {
  /**
   * 处理API错误
   * @param {Error} error 错误对象
   * @param {String} defaultMessage 默认错误消息
   */
  handleApiError(error, defaultMessage = '操作失败') {
    console.error('API错误:', error)

    let errorMessage = defaultMessage

    if (error.response) {
      // 服务器响应错误
      const { status, data } = error.response

      if (status === 400) {
        errorMessage = data?.message || '请求参数错误'
      } else if (status === 401) {
        errorMessage = '未授权访问，请重新登录'
      } else if (status === 403) {
        errorMessage = '权限不足，无法执行此操作'
      } else if (status === 404) {
        errorMessage = '请求的资源不存在'
      } else if (status === 500) {
        errorMessage = '服务器内部错误，请稍后重试'
      } else {
        errorMessage = data?.message || `请求失败 (${status})`
      }
    } else if (error.request) {
      // 网络错误
      errorMessage = '网络连接失败，请检查网络设置'
    } else if (error.message) {
      // 其他错误
      errorMessage = error.message
    }

    message.error(errorMessage)
    return errorMessage
  },

  /**
   * 处理业务逻辑错误
   * @param {String} errorCode 错误代码
   * @param {String} errorMessage 错误消息
   */
  handleBusinessError(errorCode, errorMessage) {
    const errorMap = {
      'INSUFFICIENT_BALANCE': '账户余额不足',
      'ACCOUNT_FROZEN': '账户已被冻结',
      'DEVICE_OFFLINE': '设备离线，无法使用',
      'INVALID_AMOUNT': '消费金额无效',
      'DUPLICATE_ORDER': '重复的订单号',
      'PAYMENT_FAILED': '支付失败，请重试',
      'REFUND_FAILED': '退款失败，请联系客服',
      'PERMISSION_DENIED': '权限不足',
      'DATA_NOT_FOUND': '数据不存在',
      'VALIDATION_FAILED': '数据验证失败'
    }

    const message = errorMap[errorCode] || errorMessage
    message.error(message)
    return message
  },

  /**
   * 显示成功消息
   * @param {String} successMessage 成功消息
   */
  showSuccess(successMessage = '操作成功') {
    message.success(successMessage)
  },

  /**
   * 显示警告消息
   * @param {String} warningMessage 警告消息
   */
  showWarning(warningMessage) {
    message.warning(warningMessage)
  },

  /**
   * 显示信息消息
   * @param {String} infoMessage 信息消息
   */
  showInfo(infoMessage) {
    message.info(infoMessage)
  }
}

/**
 * 加载状态管理
 */
export const loadingManager = {
  loadingStates: new Map(),

  /**
   * 开始加载
   * @param {String} key 加载键
   * @param {Function} setLoading 设置加载状态的函数
   */
  startLoading(key, setLoading) {
    this.loadingStates.set(key, true)
    if (setLoading) {
      setLoading(true)
    }
  },

  /**
   * 结束加载
   * @param {String} key 加载键
   * @param {Function} setLoading 设置加载状态的函数
   */
  endLoading(key, setLoading) {
    this.loadingStates.set(key, false)
    if (setLoading) {
      setLoading(false)
    }
  },

  /**
   * 检查是否正在加载
   * @param {String} key 加载键
   * @returns {Boolean}
   */
  isLoading(key) {
    return this.loadingStates.get(key) || false
  },

  /**
   * 清除所有加载状态
   */
  clearAllLoading() {
    this.loadingStates.clear()
  }
}

/**
 * 通用API请求包装器
 */
export const apiWrapper = {
  /**
   * 包装API调用，统一处理错误和加载状态
   * @param {Function} apiCall API调用函数
   * @param {String} loadingKey 加载键
   * @param {Function} setLoading 设置加载状态的函数
   * @param {String} successMessage 成功消息
   * @param {String} errorMessage 错误消息
   * @returns {Promise}
   */
  async call(apiCall, loadingKey, setLoading, successMessage, errorMessage) {
    try {
      // 开始加载
      if (loadingKey && setLoading) {
        loadingManager.startLoading(loadingKey, setLoading)
      }

      // 调用API
      const result = await apiCall()

      // 处理成功响应
      if (result.success !== false) {
        if (successMessage) {
          errorHandler.showSuccess(successMessage)
        }
        return result
      } else {
        // 业务错误
        const errorMsg = result.message || errorMessage
        errorHandler.handleBusinessError(result.code, errorMsg)
        return { success: false, message: errorMsg }
      }
    } catch (error) {
      // 处理系统错误
      const errorMsg = errorHandler.handleApiError(error, errorMessage)
      return { success: false, message: errorMsg }
    } finally {
      // 结束加载
      if (loadingKey && setLoading) {
        loadingManager.endLoading(loadingKey, setLoading)
      }
    }
  },

  /**
   * 批量API调用
   * @param {Array} apiCalls API调用数组
   * @param {String} loadingKey 加载键
   * @param {Function} setLoading 设置加载状态的函数
   * @returns {Promise}
   */
  async batchCall(apiCalls, loadingKey, setLoading) {
    const results = []

    try {
      if (loadingKey && setLoading) {
        loadingManager.startLoading(loadingKey, setLoading)
      }

      // 并行执行所有API调用
      const promises = apiCalls.map(async (apiCall, index) => {
        try {
          const result = await apiCall()
          return { index, success: true, data: result }
        } catch (error) {
          const errorMsg = errorHandler.handleApiError(error)
          return { index, success: false, error: errorMsg }
        }
      })

      const batchResults = await Promise.all(promises)

      // 处理结果
      batchResults.forEach(result => {
        results[result.index] = result
      })

      return results
    } finally {
      if (loadingKey && setLoading) {
        loadingManager.endLoading(loadingKey, setLoading)
      }
    }
  }
}