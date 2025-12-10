/**
 * 用户体验增强工具类
 * 统一管理交互反馈、加载状态、错误处理等
 */

import { debounce } from './performance.js'

// 交互反馈管理
class InteractionManager {
  constructor() {
    this.feedbackQueue = []
    this.isShowingFeedback = false
  }

  /**
   * 显示成功反馈
   * @param {string} message 成功消息
   * @param {Object} options 配置选项
   */
  showSuccess(message, options = {}) {
    this.showFeedback({
      type: 'success',
      message,
      duration: options.duration || 2000,
      icon: options.icon || 'success-filled',
      position: options.position || 'center'
    })
  }

  /**
   * 显示错误反馈
   * @param {string} message 错误消息
   * @param {Object} options 配置选项
   */
  showError(message, options = {}) {
    this.showFeedback({
      type: 'error',
      message,
      duration: options.duration || 3000,
      icon: options.icon || 'error-filled',
      position: options.position || 'center'
    })
  }

  /**
   * 显示加载反馈
   * @param {string} message 加载消息
   * @param {Object} options 配置选项
   */
  showLoading(message = '加载中...', options = {}) {
    uni.showLoading({
      title: message,
      mask: options.mask !== false,
      ...options
    })
  }

  /**
   * 隐藏加载反馈
   */
  hideLoading() {
    uni.hideLoading()
  }

  /**
   * 显示反馈
   * @param {Object} feedback 反馈配置
   */
  showFeedback(feedback) {
    if (this.isShowingFeedback) {
      this.feedbackQueue.push(feedback)
      return
    }

    this.isShowingFeedback = true
    this._displayFeedback(feedback)
  }

  /**
   * 显示反馈内容
   * @private
   */
  _displayFeedback(feedback) {
    uni.showToast({
      title: feedback.message,
      icon: feedback.type === 'success' ? 'success' : 'error',
      duration: feedback.duration,
      position: feedback.position,
      complete: () => {
        setTimeout(() => {
          this.isShowingFeedback = false
          if (this.feedbackQueue.length > 0) {
            const nextFeedback = this.feedbackQueue.shift()
            this.showFeedback(nextFeedback)
          }
        }, 100)
      }
    })
  }
}

// 加载状态管理
class LoadingStateManager {
  constructor() {
    this.loadingStates = new Map()
    this.globalLoading = false
  }

  /**
   * 设置加载状态
   * @param {string} key 状态键
   * @param {boolean} loading 是否加载中
   * @param {string} message 加载消息
   */
  setLoading(key, loading, message = '') {
    this.loadingStates.set(key, {
      loading,
      message,
      timestamp: Date.now()
    })

    // 如果有任何加载状态，显示全局加载
    this.updateGlobalLoading()
  }

  /**
   * 获取加载状态
   * @param {string} key 状态键
   */
  getLoading(key) {
    return this.loadingStates.get(key)?.loading || false
  }

  /**
   * 清除所有加载状态
   */
  clearAllLoading() {
    this.loadingStates.clear()
    this.globalLoading = false
    uni.hideLoading()
  }

  /**
   * 更新全局加载状态
   */
  updateGlobalLoading() {
    const hasAnyLoading = Array.from(this.loadingStates.values())
      .some(state => state.loading)

    if (hasAnyLoading && !this.globalLoading) {
      this.globalLoading = true
      uni.showLoading({
        title: '加载中...',
        mask: true
      })
    } else if (!hasAnyLoading && this.globalLoading) {
      this.globalLoading = false
      uni.hideLoading()
    }
  }
}

// 错误处理管理
class ErrorHandler {
  constructor() {
    this.errorHandlers = new Map()
    this.defaultHandler = this._defaultErrorHandler.bind(this)
  }

  /**
   * 注册错误处理器
   * @param {string} type 错误类型
   * @param {Function} handler 处理函数
   */
  registerHandler(type, handler) {
    this.errorHandlers.set(type, handler)
  }

  /**
   * 处理错误
   * @param {Error} error 错误对象
   * @param {string} type 错误类型
   * @param {Object} context 上下文信息
   */
  handleError(error, type = 'default', context = {}) {
    const handler = this.errorHandlers.get(type) || this.defaultHandler
    handler(error, context)
  }

  /**
   * 默认错误处理器
   * @private
   */
  _defaultErrorHandler(error, context) {
    console.error('未处理的错误:', error, context)

    let message = '操作失败，请稍后重试'

    // 根据错误类型设置不同的消息
    if (error.message) {
      if (error.message.includes('Network Error')) {
        message = '网络连接失败，请检查网络设置'
      } else if (error.message.includes('timeout')) {
        message = '请求超时，请稍后重试'
      } else if (error.message.includes('401')) {
        message = '登录已过期，请重新登录'
      } else if (error.message.includes('403')) {
        message = '没有权限进行此操作'
      } else if (error.message.includes('404')) {
        message = '请求的资源不存在'
      } else if (error.message.includes('500')) {
        message = '服务器内部错误，请联系管理员'
      } else {
        message = error.message
      }
    }

    // 显示错误提示
    uni.showToast({
      title: message,
      icon: 'error',
      duration: 3000
    })
  }
}

// 数据验证工具
class DataValidator {
  /**
   * 验证手机号
   */
  static validatePhone(phone) {
    const phoneRegex = /^1[3-9]\d{9}$/
    return phoneRegex.test(phone)
  }

  /**
   * 验证邮箱
   */
  static validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  }

  /**
   * 验证身份证号
   */
  static validateIdCard(idCard) {
    const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    return idCardRegex.test(idCard)
  }

  /**
   * 验证必填字段
   */
  static validateRequired(value, fieldName) {
    if (!value || value.toString().trim() === '') {
      return {
        valid: false,
        message: `${fieldName}不能为空`
      }
    }
    return { valid: true }
  }

  /**
   * 验证表单数据
   */
  static validateForm(formData, rules) {
    const errors = []

    Object.keys(rules).forEach(field => {
      const value = formData[field]
      const fieldRules = rules[field]

      fieldRules.forEach(rule => {
        let result = { valid: true }

        if (rule.required) {
          result = this.validateRequired(value, rule.label || field)
        }

        if (result.valid && rule.type && value) {
          switch (rule.type) {
            case 'phone':
              result = { valid: this.validatePhone(value), message: '手机号格式不正确' }
              break
            case 'email':
              result = { valid: this.validateEmail(value), message: '邮箱格式不正确' }
              break
            case 'idCard':
              result = { valid: this.validateIdCard(value), message: '身份证号格式不正确' }
              break
            case 'length':
              if (rule.min && value.length < rule.min) {
                result = { valid: false, message: `${rule.label || field}长度不能少于${rule.min}位` }
              } else if (rule.max && value.length > rule.max) {
                result = { valid: false, message: `${rule.label || field}长度不能超过${rule.max}位` }
              }
              break
          }
        }

        if (!result.valid) {
          errors.push(result.message)
        }
      })
    })

    return {
      valid: errors.length === 0,
      errors
    }
  }
}

// 动画效果工具
class AnimationHelper {
  /**
   * 淡入动画
   */
  static fadeIn(element, duration = 300) {
    return new Promise(resolve => {
      element.style.opacity = '0'
      element.style.transition = `opacity ${duration}ms`

      requestAnimationFrame(() => {
        element.style.opacity = '1'
        setTimeout(resolve, duration)
      })
    })
  }

  /**
   * 淡出动画
   */
  static fadeOut(element, duration = 300) {
    return new Promise(resolve => {
      element.style.opacity = '1'
      element.style.transition = `opacity ${duration}ms`

      requestAnimationFrame(() => {
        element.style.opacity = '0'
        setTimeout(resolve, duration)
      })
    })
  }

  /**
   * 滑动动画
   */
  static slideIn(element, direction = 'up', duration = 300) {
    return new Promise(resolve => {
      const transforms = {
        up: 'translateY(100%)',
        down: 'translateY(-100%)',
        left: 'translateX(100%)',
        right: 'translateX(-100%)'
      }

      element.style.transform = transforms[direction]
      element.style.transition = `transform ${duration}ms`

      requestAnimationFrame(() => {
        element.style.transform = 'translate(0, 0)'
        setTimeout(resolve, duration)
      })
    })
  }
}

// 创建全局实例
const interactionManager = new InteractionManager()
const loadingStateManager = new LoadingStateManager()
const errorHandler = new ErrorHandler()

export {
  InteractionManager,
  LoadingStateManager,
  ErrorHandler,
  DataValidator,
  AnimationHelper,
  interactionManager,
  loadingStateManager,
  errorHandler
}