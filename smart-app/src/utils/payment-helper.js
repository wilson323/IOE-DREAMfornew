/**
 * 移动端支付工具类
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 *
 * 功能说明：
 * - 微信支付调起
 * - 支付宝支付调起
 * - 支付结果监听
 * - 支付状态查询
 */

import { postRequest } from '@/lib/smart-request'

/**
 * 发起微信支付
 *
 * @param {Object} options 支付参数
 * @param {String} options.orderId 订单ID
 * @param {Number} options.amount 金额（元）
 * @param {String} options.description 商品描述
 * @returns {Promise} 支付结果
 */
export const wechatPay = async (options) => {
  try {
    console.log('[微信支付] 开始支付', options)

    // 1. 调用后端创建支付订单
    const result = await postRequest('/api/v1/consume/payment/wechat/create', {
      orderId: options.orderId,
      amount: options.amount,
      description: options.description
    })

    if (result.code !== 1) {
      throw new Error(result.message || '创建支付订单失败')
    }

    const payParams = result.data

    // 2. 调起微信支付
    return new Promise((resolve, reject) => {
      // #ifdef MP-WEIXIN
      // 微信小程序支付
      uni.requestPayment({
        provider: 'wxpay',
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType,
        paySign: payParams.paySign,
        success: (res) => {
          console.log('[微信支付] 支付成功', res)
          resolve({ success: true, ...res })

          // 震动反馈
          uni.vibrateShort()

          // 显示成功提示
          uni.showToast({
            title: '支付成功',
            icon: 'success',
            duration: 2000
          })
        },
        fail: (err) => {
          console.error('[微信支付] 支付失败', err)

          // 用户取消支付
          if (err.errMsg && err.errMsg.includes('cancel')) {
            reject({ success: false, cancelled: true, message: '用户取消支付' })
          } else {
            reject({ success: false, message: err.errMsg || '支付失败' })
          }

          uni.showToast({
            title: err.errMsg || '支付失败',
            icon: 'none'
          })
        }
      })
      // #endif

      // #ifdef APP-PLUS
      // APP微信支付
      uni.requestPayment({
        provider: 'wxpay',
        orderInfo: payParams,
        success: (res) => {
          console.log('[微信支付] 支付成功', res)
          resolve({ success: true, ...res })

          uni.vibrateShort()
          uni.showToast({ title: '支付成功', icon: 'success' })
        },
        fail: (err) => {
          console.error('[微信支付] 支付失败', err)
          reject({ success: false, message: err.errMsg })

          uni.showToast({ title: err.errMsg, icon: 'none' })
        }
      })
      // #endif

      // #ifdef H5
      // H5暂不支持
      uni.showModal({
        title: '提示',
        content: 'H5暂不支持微信支付，请使用支付宝支付',
        showCancel: false
      })
      reject({ success: false, message: 'H5不支持微信支付' })
      // #endif
    })

  } catch (error) {
    console.error('[微信支付] 支付异常', error)
    throw error
  }
}

/**
 * 发起支付宝支付
 *
 * @param {Object} options 支付参数
 * @param {String} options.orderId 订单ID
 * @param {Number} options.amount 金额（元）
 * @param {String} options.subject 商品标题
 * @returns {Promise} 支付结果
 */
export const alipay = async (options) => {
  try {
    console.log('[支付宝] 开始支付', options)

    // 1. 调用后端创建支付订单
    const result = await postRequest('/api/v1/consume/payment/alipay/create', {
      orderId: options.orderId,
      amount: options.amount,
      subject: options.subject
    })

    if (result.code !== 1) {
      throw new Error(result.message || '创建支付订单失败')
    }

    const payParams = result.data

    // 2. 调起支付宝支付
    return new Promise((resolve, reject) => {
      // #ifdef MP-ALIPAY
      // 支付宝小程序支付
      my.tradePay({
        orderStr: payParams.orderString,
        success: (res) => {
          console.log('[支付宝] 支付成功', res)

          if (res.resultCode === '9000') {
            resolve({ success: true, ...res })

            uni.vibrateShort()
            uni.showToast({ title: '支付成功', icon: 'success' })
          } else {
            reject({ success: false, message: '支付失败', resultCode: res.resultCode })
            uni.showToast({ title: '支付失败', icon: 'none' })
          }
        },
        fail: (err) => {
          console.error('[支付宝] 支付失败', err)
          reject({ success: false, message: err.errorMessage })

          uni.showToast({ title: err.errorMessage, icon: 'none' })
        }
      })
      // #endif

      // #ifdef APP-PLUS
      // APP支付宝支付
      uni.requestPayment({
        provider: 'alipay',
        orderInfo: payParams.orderString,
        success: (res) => {
          console.log('[支付宝] 支付成功', res)
          resolve({ success: true, ...res })

          uni.vibrateShort()
          uni.showToast({ title: '支付成功', icon: 'success' })
        },
        fail: (err) => {
          console.error('[支付宝] 支付失败', err)
          reject({ success: false, message: err.errMsg })

          uni.showToast({ title: err.errMsg, icon: 'none' })
        }
      })
      // #endif

      // #ifdef H5
      // H5支付宝支付（跳转网页）
      window.location.href = payParams.orderString
      // H5支付结果需要通过回调URL获取
      resolve({ success: true, redirected: true })
      // #endif
    })

  } catch (error) {
    console.error('[支付宝] 支付异常', error)
    throw error
  }
}

/**
 * 查询支付结果
 *
 * @param {String} orderId 订单ID
 * @returns {Promise} 支付状态
 */
export const queryPaymentStatus = async (orderId) => {
  try {
    const result = await postRequest('/api/v1/consume/payment/query', { orderId })

    if (result.code === 1 && result.data) {
      return {
        success: true,
        status: result.data.status,      // SUCCESS/FAILED/PENDING
        payTime: result.data.payTime,
        tradeNo: result.data.tradeNo
      }
    }

    return { success: false, status: 'UNKNOWN' }
  } catch (error) {
    console.error('[支付查询] 查询失败', error)
    return { success: false, status: 'ERROR', error }
  }
}

/**
 * 统一支付入口（自动选择支付方式）
 *
 * @param {Object} options 支付参数
 * @param {String} options.paymentMethod 支付方式（wechat/alipay）
 * @param {String} options.orderId 订单ID
 * @param {Number} options.amount 金额
 * @param {String} options.description 描述
 * @returns {Promise} 支付结果
 */
export const unifiedPay = async (options) => {
  const { paymentMethod, orderId, amount, description } = options

  console.log('[统一支付] 支付方式:', paymentMethod, '金额:', amount)

  // 显示加载提示
  uni.showLoading({ title: '正在调起支付...', mask: true })

  try {
    let result

    if (paymentMethod === 'wechat') {
      // 微信支付
      result = await wechatPay({ orderId, amount, description })
    } else if (paymentMethod === 'alipay') {
      // 支付宝支付
      result = await alipay({ orderId, amount, subject: description })
    } else {
      throw new Error('不支持的支付方式')
    }

    uni.hideLoading()
    return result

  } catch (error) {
    uni.hideLoading()

    console.error('[统一支付] 支付失败', error)

    // 显示错误提示
    if (!error.cancelled) {
      uni.showModal({
        title: '支付失败',
        content: error.message || '支付过程中出现错误',
        showCancel: false
      })
    }

    throw error
  }
}

/**
 * 申请退款
 *
 * @param {Object} options 退款参数
 * @param {String} options.orderId 订单ID
 * @param {Number} options.refundAmount 退款金额
 * @param {String} options.reason 退款原因
 * @returns {Promise} 退款结果
 */
export const refund = async (options) => {
  try {
    console.log('[申请退款]', options)

    uni.showLoading({ title: '正在申请退款...', mask: true })

    const result = await postRequest('/api/v1/consume/payment/refund', {
      orderId: options.orderId,
      refundAmount: options.refundAmount,
      reason: options.reason
    })

    uni.hideLoading()

    if (result.code === 1) {
      uni.showToast({ title: '退款申请已提交', icon: 'success' })
      return { success: true, ...result.data }
    } else {
      throw new Error(result.message || '退款申请失败')
    }

  } catch (error) {
    uni.hideLoading()

    console.error('[申请退款] 失败', error)

    uni.showModal({
      title: '退款失败',
      content: error.message || '退款申请失败',
      showCancel: false
    })

    return { success: false, error }
  }
}

export default {
  wechatPay,
  alipay,
  unifiedPay,
  queryPaymentStatus,
  refund
}

