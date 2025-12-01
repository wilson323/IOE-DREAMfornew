/**
 * 网络请求工具
 */

// 基础配置
const config = {
  baseURL: 'http://localhost:1024', // 后端服务地址
  timeout: 30000,
  header: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
}

/**
 * 网络请求
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
const request = (options) => {
  // 合并配置
  options = {
    ...config,
    ...options,
    url: config.baseURL + options.url
  }

  // 获取token
  const token = uni.getStorageSync('token')
  if (token) {
    options.header.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      success: (response) => {
        const { data, statusCode } = response

        if (statusCode === 200) {
          // 请求成功
          if (data.code === 200) {
            resolve(data)
          } else {
            // 业务错误
            uni.showToast({
              title: data.message || '请求失败',
              icon: 'none'
            })
            reject(new Error(data.message || '请求失败'))
          }
        } else if (statusCode === 401) {
          // 未授权，跳转登录
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }, 1500)
          reject(new Error('未授权'))
        } else {
          // 其他错误
          uni.showToast({
            title: '网络请求失败',
            icon: 'none'
          })
          reject(new Error('网络请求失败'))
        }
      },
      fail: (error) => {
        console.error('请求失败:', error)
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

/**
 * GET请求
 * @param {String} url 请求地址
 * @param {Object} params 请求参数
 * @param {Object} options 其他配置
 * @returns {Promise}
 */
request.get = (url, params = {}, options = {}) => {
  // 将参数拼接到URL
  if (Object.keys(params).length > 0) {
    const queryString = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    url += (url.includes('?') ? '&' : '?') + queryString
  }

  return request({
    url,
    method: 'GET',
    ...options
  })
}

/**
 * POST请求
 * @param {String} url 请求地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他配置
 * @returns {Promise}
 */
request.post = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT请求
 * @param {String} url 请求地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他配置
 * @returns {Promise}
 */
request.put = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE请求
 * @param {String} url 请求地址
 * @param {Object} params 请求参数
 * @param {Object} options 其他配置
 * @returns {Promise}
 */
request.delete = (url, params = {}, options = {}) => {
  // 将参数拼接到URL
  if (Object.keys(params).length > 0) {
    const queryString = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    url += (url.includes('?') ? '&' : '?') + queryString
  }

  return request({
    url,
    method: 'DELETE',
    ...options
  })
}

/**
 * 文件上传
 * @param {String} url 上传地址
 * @param {String} filePath 文件路径
 * @param {Object} options 其他配置
 * @returns {Promise}
 */
request.upload = (url, filePath, options = {}) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    uni.uploadFile({
      url: config.baseURL + url,
      filePath,
      name: 'file',
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        ...options.header
      },
      formData: options.formData || {},
      success: (response) => {
        try {
          const data = JSON.parse(response.data)
          if (data.code === 200) {
            resolve(data)
          } else {
            uni.showToast({
              title: data.message || '上传失败',
              icon: 'none'
            })
            reject(new Error(data.message || '上传失败'))
          }
        } catch (error) {
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
          reject(error)
        }
      },
      fail: (error) => {
        console.error('上传失败:', error)
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

export default request