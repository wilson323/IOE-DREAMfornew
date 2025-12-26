<template>
  <view class="permission-detail-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <uni-icons type="arrowleft" size="20" color="#333" @tap="goBack"></uni-icons>
        <text class="nav-title">权限详情</text>
        <view class="nav-actions">
          <uni-icons type="more" size="20" color="#1890ff" @tap="showMoreActions"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <uni-load-more status="loading" :content-text="loadingText"></uni-load-more>
    </view>

    <template v-else>
      <!-- 权限状态卡片 -->
      <view class="status-card" :class="getStatusClass()">
        <view class="status-header">
          <view class="status-icon" :class="getStatusIconClass()">
            <uni-icons :type="getStatusIcon()" size="32" color="#fff"></uni-icons>
          </view>
          <view class="status-info">
            <text class="status-text">{{ getStatusText() }}</text>
            <text class="status-desc">{{ getStatusDesc() }}</text>
          </view>
        </view>

        <!-- 倒计时 -->
        <view v-if="permissionDetail.permissionType !== 1 && permissionDetail.permissionStatus === 1" class="countdown">
          <view class="countdown-item">
            <text class="countdown-value">{{ countdown.days }}</text>
            <text class="countdown-label">天</text>
          </view>
          <text class="countdown-separator">:</text>
          <view class="countdown-item">
            <text class="countdown-value">{{ countdown.hours }}</text>
            <text class="countdown-label">时</text>
          </view>
          <text class="countdown-separator">:</text>
          <view class="countdown-item">
            <text class="countdown-value">{{ countdown.minutes }}</text>
            <text class="countdown-label">分</text>
          </view>
          <text class="countdown-separator">:</text>
          <view class="countdown-item">
            <text class="countdown-value">{{ countdown.seconds }}</text>
            <text class="countdown-label">秒</text>
          </view>
        </view>
      </view>

      <!-- 权限基本信息 -->
      <view class="info-section">
        <view class="section-title">基本信息</view>
        <view class="info-card">
          <view class="info-item">
            <text class="info-label">权限类型</text>
            <text class="info-value">{{ getPermissionTypeText() }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">访问区域</text>
            <text class="info-value area-name">{{ permissionDetail.areaName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">生效时间</text>
            <text class="info-value">{{ formatDateTime(permissionDetail.effectiveTime) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">失效时间</text>
            <text class="info-value">{{ formatDateTime(permissionDetail.expireTime) }}</text>
          </view>
          <view v-if="permissionDetail.startTime" class="info-item">
            <text class="info-label">每日时段</text>
            <text class="info-value">{{ permissionDetail.startTime }} - {{ permissionDetail.endTime }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">通行方式</text>
            <view class="pass-types">
              <view
                v-for="type in getPassTypes()"
                :key="type.value"
                class="pass-type-tag"
              >
                <uni-icons :type="type.icon" size="14" color="#1890ff"></uni-icons>
                <text>{{ type.label }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 区域信息 -->
      <view class="area-section">
        <view class="section-title">
          <text>区域信息</text>
          <uni-icons type="arrowright" size="16" color="#999" @tap="goToArea"></uni-icons>
        </view>
        <view class="area-card" @tap="goToArea">
          <view class="area-image-wrapper">
            <image
              v-if="permissionDetail.areaImage"
              :src="permissionDetail.areaImage"
              mode="aspectFill"
              class="area-image"
            ></image>
            <view v-else class="area-placeholder">
              <uni-icons type="home" size="32" color="#999"></uni-icons>
            </view>
          </view>
          <view class="area-info">
            <text class="area-name">{{ permissionDetail.areaName }}</text>
            <text class="area-location">{{ permissionDetail.areaLocation || '暂无位置信息' }}</text>
          </view>
          <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
        </view>
      </view>

      <!-- 使用统计 -->
      <view class="stats-section">
        <view class="section-title">使用统计</view>
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{ permissionDetail.totalPassCount || 0 }}</text>
            <text class="stat-label">总通行次数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ permissionDetail.monthlyPassCount || 0 }}</text>
            <text class="stat-label">本月通行</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ permissionDetail.weeklyPassCount || 0 }}</text>
            <text class="stat-label">本周通行</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ formatLastPassTime(permissionDetail.lastPassTime) }}</text>
            <text class="stat-label">最后通行</text>
          </view>
        </view>
      </view>

      <!-- 最近通行记录 -->
      <view class="records-section">
        <view class="section-title">
          <text>最近通行</text>
          <text class="more-btn" @tap="viewAllRecords">查看全部</text>
        </view>
        <scroll-view class="records-list" scroll-y>
          <!-- 空状态 -->
          <view v-if="recentRecords.length === 0" class="empty-records">
            <text>暂无通行记录</text>
          </view>

          <!-- 记录列表 -->
          <view v-for="record in recentRecords" :key="record.recordId" class="record-item">
            <view class="record-avatar">
              <uni-icons type="person" size="18" color="#1890ff"></uni-icons>
            </view>
            <view class="record-info">
              <view class="record-header">
                <text class="record-device">{{ record.deviceName }}</text>
                <text class="record-time">{{ formatRecordTime(record.passTime) }}</text>
              </view>
              <view class="record-details">
                <text class="record-result" :class="record.passResult === 1 ? 'success' : 'fail'">
                  {{ record.passResult === 1 ? '通过' : '拒绝' }}
                </text>
                <text v-if="record.passType" class="record-type">{{ getRecordPassTypeText(record.passType) }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 权限操作 -->
      <view v-if="permissionDetail.permissionStatus === 1" class="actions-section">
        <view class="section-title">快捷操作</view>
        <view class="action-buttons">
          <button class="action-btn qrcode-btn" @tap="showQRCode">
            <uni-icons type="qrcode" size="20" color="#fff"></uni-icons>
            <text>二维码</text>
          </button>
          <button
            v-if="permissionDetail.canRenew"
            class="action-btn renew-btn"
            @tap="renewPermission"
          >
            <uni-icons type="loop" size="20" color="#fff"></uni-icons>
            <text>续期</text>
          </button>
          <button
            v-if="permissionDetail.canTransfer"
            class="action-btn transfer-btn"
            @tap="transferPermission"
          >
            <uni-icons type="redo" size="20" color="#fff"></uni-icons>
            <text>转让</text>
          </button>
          <button
            v-if="permissionDetail.canFreeze"
            class="action-btn freeze-btn"
            @tap="freezePermission"
          >
            <uni-icons type="snow" size="20" color="#fff"></uni-icons>
            <text>冻结</text>
          </button>
        </view>
      </view>

      <!-- 权限历史 -->
      <view class="history-section">
        <view class="section-title">权限历史</view>
        <view class="history-timeline">
          <view
            v-for="(history, index) in permissionHistory"
            :key="index"
            class="history-item"
          >
            <view class="history-dot" :class="getHistoryClass(history.actionType)"></view>
            <view class="history-content">
              <text class="history-action">{{ getHistoryActionText(history) }}</text>
              <text class="history-time">{{ formatDateTime(history.actionTime) }}</text>
              <text v-if="history.remark" class="history-remark">{{ history.remark }}</text>
            </view>
          </view>
        </view>
      </view>
    </template>

    <!-- 二维码弹窗 -->
    <uni-popup ref="qrcodePopup" type="center">
      <view class="qrcode-popup">
        <view class="popup-header">
          <text class="popup-title">权限二维码</text>
          <uni-icons type="close" size="20" color="#999" @tap="closeQRCode"></uni-icons>
        </view>
        <view class="popup-content">
          <view class="qrcode-area">
            <image v-if="qrcodeData" :src="qrcodeData" mode="aspectFit" class="qrcode-image"></image>
            <view v-else class="qrcode-loading">
              <uni-load-more status="loading"></uni-load-more>
            </view>
          </view>
          <view class="qrcode-tips">
            <uni-icons type="info" size="14" color="#999"></uni-icons>
            <text>请在门禁设备前出示二维码进行验证</text>
          </view>
        </view>
        <view class="popup-footer">
          <button class="save-btn" @tap="saveQRCode">保存到相册</button>
        </view>
      </view>
    </uni-popup>

    <!-- 更多操作弹窗 -->
    <uni-popup ref="morePopup" type="bottom">
      <view class="more-popup">
        <view class="popup-header">
          <text class="popup-title">更多操作</text>
          <uni-icons type="close" size="20" color="#999" @tap="closeMoreActions"></uni-icons>
        </view>
        <view class="popup-content">
          <view class="action-list">
            <view v-if="permissionDetail.canShare" class="action-item" @tap="sharePermission">
              <uni-icons type="redo" size="20" color="#1890ff"></uni-icons>
              <text>分享权限</text>
            </view>
            <view class="action-item" @tap="exportPermission">
              <uni-icons type="download" size="20" color="#1890ff"></uni-icons>
              <text>导出凭证</text>
            </view>
            <view v-if="permissionDetail.permissionStatus === 1" class="action-item danger" @tap="freezePermission">
              <uni-icons type="snow" size="20" color="#ff4d4f"></uni-icons>
              <text>冻结权限</text>
            </view>
            <view v-if="permissionDetail.permissionStatus === 4" class="action-item" @tap="unfreezePermission">
              <uni-icons type="redo" size="20" color="#52c41a"></uni-icons>
              <text>解冻权限</text>
            </view>
            <view class="action-item danger" @tap="reportIssue">
              <uni-icons type="alert" size="20" color="#ff4d4f"></uni-icons>
              <text>反馈问题</text>
            </view>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { accessApi } from '@/api/business/access/access-api.js'

/**
 * 权限详情页面
 * 功能：
 * 1. 展示权限完整信息
 * 2. 实时倒计时
 * 3. 使用统计展示
 * 4. 最近通行记录
 * 5. 权限操作（二维码、续期、转让、冻结）
 * 6. 权限历史记录
 */

// ==================== 状态数据 ====================

const loading = ref(true)
const permissionId = ref('')

// 权限详情
const permissionDetail = ref({})

// 倒计时
const countdown = ref({
  days: '00',
  hours: '00',
  minutes: '00',
  seconds: '00'
})

// 最近通行记录
const recentRecords = ref([])

// 权限历史
const permissionHistory = ref([])

// 二维码数据
const qrcodePopup = ref(null)
const qrcodeData = ref('')

// 更多操作弹窗
const morePopup = ref(null)

// 加载文本
const loadingText = ref({
  contentdown: '上拉加载更多',
  contentrefresh: '加载中...',
  contentnomore: '没有更多数据了'
})

// 定时器
let countdownTimer = null

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[权限详情] 页面加载')

  // 获取权限ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.permissionId) {
    permissionId.value = options.permissionId
    loadPermissionDetail()
    loadRecentRecords()
    loadPermissionHistory()
    startCountdown()
  }
})

onUnmounted(() => {
  console.log('[权限详情] 页面卸载')
  stopCountdown()
})

// ==================== 数据加载 ====================

/**
 * 加载权限详情
 */
const loadPermissionDetail = async () => {
  loading.value = true

  try {
    const result = await accessApi.getPermissionDetail(permissionId.value)

    if (result.success && result.data) {
      permissionDetail.value = result.data
    }
  } catch (error) {
    console.error('[权限详情] 加载失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载最近通行记录
 */
const loadRecentRecords = async () => {
  try {
    const result = await accessApi.getPermissionRecords(permissionId.value, {
      pageNum: 1,
      pageSize: 5
    })

    if (result.success && result.data) {
      recentRecords.value = result.data.list || []
    }
  } catch (error) {
    console.error('[权限详情] 加载记录失败:', error)
  }
}

/**
 * 加载权限历史
 */
const loadPermissionHistory = async () => {
  try {
    const result = await accessApi.getPermissionHistory(permissionId.value)

    if (result.success && result.data) {
      permissionHistory.value = result.data || []
    }
  } catch (error) {
    console.error('[权限详情] 加载历史失败:', error)
  }
}

// ==================== 倒计时功能 ====================

/**
 * 启动倒计时
 */
const startCountdown = () => {
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

/**
 * 停止倒计时
 */
const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

/**
 * 更新倒计时
 */
const updateCountdown = () => {
  if (!permissionDetail.value.expireTime) return

  const now = Date.now()
  const expireTime = new Date(permissionDetail.value.expireTime).getTime()
  const diff = expireTime - now

  if (diff <= 0) {
    countdown.value = {
      days: '00',
      hours: '00',
      minutes: '00',
      seconds: '00'
    }
    return
  }

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)

  countdown.value = {
    days: String(days).padStart(2, '0'),
    hours: String(hours).padStart(2, '0'),
    minutes: String(minutes).padStart(2, '0'),
    seconds: String(seconds).padStart(2, '0')
  }
}

// ==================== 导航操作 ====================

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 查看全部记录
 */
const viewAllRecords = () => {
  uni.navigateTo({
    url: `/pages/access/access-record?permissionId=${permissionId.value}`
  })
}

/**
 * 跳转到区域
 */
const goToArea = () => {
  uni.navigateTo({
    url: `/pages/access/area-detail?areaId=${permissionDetail.value.areaId}`
  })
}

// ==================== 权限操作 ====================

/**
 * 显示二维码
 */
const showQRCode = async () => {
  qrcodeData.value = ''
  qrcodePopup.value?.open()

  try {
    const result = await accessApi.getPermissionQRCode(permissionId.value)

    if (result.success && result.data) {
      qrcodeData.value = result.data.qrCode
    }
  } catch (error) {
    console.error('[权限详情] 获取二维码失败:', error)
    uni.showToast({
      title: '获取二维码失败',
      icon: 'none'
    })
  }
}

/**
 * 关闭二维码
 */
const closeQRCode = () => {
  qrcodePopup.value?.close()
}

/**
 * 保存二维码
 */
const saveQRCode = () => {
  if (!qrcodeData.value) {
    uni.showToast({
      title: '二维码未加载完成',
      icon: 'none'
    })
    return
  }

  uni.saveImageToPhotosAlbum({
    filePath: qrcodeData.value,
    success: () => {
      uni.showToast({
        title: '已保存到相册',
        icon: 'success'
      })
    },
    fail: () => {
      uni.showToast({
        title: '保存失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 续期权限
 */
const renewPermission = () => {
  uni.navigateTo({
    url: `/pages/access/permission-renew?permissionId=${permissionId.value}`
  })
}

/**
 * 转让权限
 */
const transferPermission = () => {
  uni.navigateTo({
    url: `/pages/access/permission-transfer?permissionId=${permissionId.value}`
  })
}

/**
 * 冻结权限
 */
const freezePermission = () => {
  uni.showModal({
    title: '确认冻结',
    content: '冻结后该权限将暂时无法使用，确定要冻结吗？',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '冻结中...',
          mask: true
        })

        try {
          const result = await accessApi.freezePermission(permissionId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '已冻结',
              icon: 'success'
            })
            loadPermissionDetail()
            loadPermissionHistory()
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[权限详情] 冻结失败:', error)
        }
      }
    }
  })
}

/**
 * 解冻权限
 */
const unfreezePermission = () => {
  uni.showModal({
    title: '确认解冻',
    content: '确定要解冻该权限吗？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '解冻中...',
          mask: true
        })

        try {
          const result = await accessApi.unfreezePermission(permissionId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '已解冻',
              icon: 'success'
            })
            loadPermissionDetail()
            loadPermissionHistory()
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[权限详情] 解冻失败:', error)
        }
      }
    }
  })
}

/**
 * 分享权限
 */
const sharePermission = () => {
  closeMoreActions()

  // TODO: 实现分享功能
  uni.showToast({
    title: '分享功能开发中',
    icon: 'none'
  })
}

/**
 * 导出凭证
 */
const exportPermission = () => {
  closeMoreActions()

  uni.showLoading({
    title: '导出中...',
    mask: true
  })

  // TODO: 实现导出功能
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '导出功能开发中',
      icon: 'none'
    })
  }, 1000)
}

/**
 * 反馈问题
 */
const reportIssue = () => {
  closeMoreActions()

  uni.navigateTo({
    url: `/pages/access/permission-feedback?permissionId=${permissionId.value}`
  })
}

/**
 * 显示更多操作
 */
const showMoreActions = () => {
  morePopup.value?.open()
}

/**
 * 关闭更多操作
 */
const closeMoreActions = () => {
  morePopup.value?.close()
}

// ==================== 工具方法 ====================

/**
 * 获取状态类
 */
const getStatusClass = () => {
  const status = permissionDetail.value.permissionStatus
  if (status === 1) return 'status-valid'
  if (status === 2) return 'status-expiring'
  if (status === 3) return 'status-expired'
  if (status === 4) return 'status-frozen'
  return ''
}

/**
 * 获取状态图标类
 */
const getStatusIconClass = () => {
  const status = permissionDetail.value.permissionStatus
  if (status === 1) return 'icon-valid'
  if (status === 2) return 'icon-expiring'
  if (status === 3) return 'icon-expired'
  if (status === 4) return 'icon-frozen'
  return ''
}

/**
 * 获取状态图标
 */
const getStatusIcon = () => {
  const status = permissionDetail.value.permissionStatus
  if (status === 1) return 'checkmarkempty'
  if (status === 2) return 'alert'
  if (status === 3) return 'closeempty'
  if (status === 4) return 'snow'
  return 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = () => {
  const status = permissionDetail.value.permissionStatus
  const statusMap = {
    1: '有效',
    2: '即将过期',
    3: '已过期',
    4: '已冻结'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态描述
 */
const getStatusDesc = () => {
  const status = permissionDetail.value.permissionStatus
  const descMap = {
    1: '权限正常使用中',
    2: '权限即将到期，请及时续期',
    3: '权限已过期，无法使用',
    4: '权限已冻结，暂时无法使用'
  }
  return descMap[status] || ''
}

/**
 * 获取权限类型文本
 */
const getPermissionTypeText = () => {
  const type = permissionDetail.value.permissionType
  const typeMap = {
    1: '永久权限',
    2: '临时权限',
    3: '定时权限'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取通行方式列表
 */
const getPassTypes = () => {
  const passTypeMap = {
    1: { value: 1, label: '人脸', icon: 'person' },
    2: { value: 2, label: '指纹', icon: 'fingerprint' },
    3: { value: 3, label: 'IC卡', icon: 'card' },
    4: { value: 4, label: '密码', icon: 'locked' },
    5: { value: 5, label: '二维码', icon: 'qrcode' }
  }

  if (!permissionDetail.value.passType || permissionDetail.value.passType.length === 0) {
    return [{ value: 0, label: '不限', icon: 'checkmarkempty' }]
  }

  return permissionDetail.value.passType
    .map(type => passTypeMap[type])
    .filter(Boolean)
}

/**
 * 获取记录通行方式文本
 */
const getRecordPassTypeText = (passType) => {
  const typeMap = {
    1: '人脸',
    2: '指纹',
    3: 'IC卡',
    4: '密码',
    5: '二维码'
  }
  return typeMap[passType] || '其他'
}

/**
 * 格式化日期时间
 */
const formatDateTime = (timestamp) => {
  if (!timestamp) return '-'

  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化记录时间
 */
const formatRecordTime = (timestamp) => {
  if (!timestamp) return '-'

  const date = new Date(timestamp)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return `今天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } else if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } else {
    return formatDateTime(timestamp)
  }
}

/**
 * 格式化最后通行时间
 */
const formatLastPassTime = (timestamp) => {
  if (!timestamp) return '暂无'

  const now = Date.now()
  const diff = now - timestamp

  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return formatDate(timestamp)
  }
}

/**
 * 格式化日期
 */
const formatDate = (timestamp) => {
  if (!timestamp) return '-'

  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 获取历史记录类
 */
const getHistoryClass = (actionType) => {
  const classMap = {
    1: 'history-create',
    2: 'history-renew',
    3: 'history-freeze',
    4: 'history-unfreeze',
    5: 'history-expire',
    6: 'history-transfer'
  }
  return classMap[actionType] || 'history-other'
}

/**
 * 获取历史操作文本
 */
const getHistoryActionText = (history) => {
  const actionMap = {
    1: '创建权限',
    2: '续期成功',
    3: '冻结权限',
    4: '解冻权限',
    5: '权限过期',
    6: '转让权限'
  }
  return actionMap[history.actionType] || history.actionName || '未知操作'
}
</script>

<style lang="scss" scoped>
.permission-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 导航栏
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  padding-bottom: 20rpx;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;

    .nav-title {
      flex: 1;
      text-align: center;
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
    }

    .nav-actions {
      width: 80rpx;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 加载容器
.loading-container {
  padding: 120rpx 0;
}

// 状态卡片
.status-card {
  margin: 30rpx;
  padding: 40rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);

  &.status-valid {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);

    .status-icon {
      background: rgba(255, 255, 255, 0.2);
    }

    .status-text,
    .status-desc {
      color: #fff;
    }
  }

  &.status-expiring {
    background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);

    .status-icon {
      background: rgba(255, 255, 255, 0.2);
    }

    .status-text,
    .status-desc {
      color: #fff;
    }
  }

  &.status-expired {
    background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);

    .status-icon {
      background: rgba(255, 255, 255, 0.2);
    }

    .status-text,
    .status-desc {
      color: #fff;
    }
  }

  &.status-frozen {
    background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);

    .status-icon {
      background: rgba(255, 255, 255, 0.2);
    }

    .status-text,
    .status-desc {
      color: #fff;
    }
  }

  .status-header {
    display: flex;
    align-items: center;
    gap: 24rpx;
    margin-bottom: 30rpx;

    .status-icon {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
    }

    .status-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .status-text {
        font-size: 36rpx;
        font-weight: 600;
      }

      .status-desc {
        font-size: 26rpx;
        opacity: 0.9;
      }
    }
  }

  .countdown {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;

    .countdown-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4rpx;

      .countdown-value {
        font-size: 48rpx;
        font-weight: 600;
        color: #fff;
      }

      .countdown-label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
      }
    }

    .countdown-separator {
      font-size: 32rpx;
      font-weight: 600;
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

// 信息区域
.info-section,
.area-section,
.stats-section,
.records-section,
.actions-section,
.history-section {
  margin: 0 30rpx 30rpx;

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;

    .more-btn {
      font-size: 26rpx;
      color: #1890ff;
      font-weight: 400;
    }
  }
}

// 信息卡片
.info-card {
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .info-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .info-label {
      font-size: 28rpx;
      color: #666;
    }

    .info-value {
      font-size: 28rpx;
      color: #333;
      text-align: right;

      &.area-name {
        font-weight: 500;
      }
    }

    .pass-types {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;
      justify-content: flex-end;

      .pass-type-tag {
        display: flex;
        align-items: center;
        gap: 6rpx;
        padding: 6rpx 12rpx;
        background: #e6f7ff;
        border-radius: 6rpx;
        font-size: 24rpx;
        color: #1890ff;
      }
    }
  }
}

// 区域卡片
.area-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .area-image-wrapper {
    width: 120rpx;
    height: 120rpx;
    flex-shrink: 0;
    border-radius: 12rpx;
    overflow: hidden;
    background: #f5f5f5;

    .area-image {
      width: 100%;
      height: 100%;
    }

    .area-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .area-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;

    .area-name {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;
    }

    .area-location {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 统计网格
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 30rpx;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    .stat-value {
      font-size: 40rpx;
      font-weight: 600;
      color: #1890ff;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 记录列表
.records-list {
  max-height: 500rpx;

  .empty-records {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 80rpx 0;
    font-size: 28rpx;
    color: #999;
  }

  .record-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 24rpx;
    background: #fff;
    border-radius: 12rpx;
    margin-bottom: 16rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .record-avatar {
      width: 56rpx;
      height: 56rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: #e6f7ff;
    }

    .record-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .record-header {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .record-device {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
        }

        .record-time {
          font-size: 24rpx;
          color: #999;
        }
      }

      .record-details {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .record-result {
          font-size: 24rpx;
          font-weight: 500;

          &.success {
            color: #52c41a;
          }

          &.fail {
            color: #ff4d4f;
          }
        }

        .record-type {
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }
}

// 操作按钮
.action-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;

  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 24rpx 0;
    background: #fff;
    border-radius: 16rpx;
    border: none;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    &.qrcode-btn {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }

    &.renew-btn {
      background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      color: #fff;
    }

    &.transfer-btn {
      background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
      color: #fff;
    }

    &.freeze-btn {
      background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
      color: #fff;
    }
  }
}

// 历史记录时间线
.history-timeline {
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .history-item {
    display: flex;
    gap: 20rpx;
    padding-bottom: 30rpx;

    &:last-child {
      padding-bottom: 0;

      .history-dot {
        background: #d9d9d9;
      }
    }

    .history-dot {
      position: relative;
      width: 24rpx;
      height: 24rpx;
      border-radius: 50%;
      flex-shrink: 0;
      margin-top: 4rpx;

      &::after {
        content: '';
        position: absolute;
        top: 24rpx;
        left: 50%;
        transform: translateX(-50%);
        width: 2rpx;
        height: calc(100% + 30rpx);
        background: #f0f0f0;
      }

      &:last-child::after {
        display: none;
      }

      &.history-create {
        background: #1890ff;
      }

      &.history-renew {
        background: #52c41a;
      }

      &.history-freeze {
        background: #8c8c8c;
      }

      &.history-unfreeze {
        background: #722ed1;
      }

      &.history-expire {
        background: #ff4d4f;
      }

      &.history-transfer {
        background: #fa8c16;
      }

      &.history-other {
        background: #d9d9d9;
      }
    }

    .history-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .history-action {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }

      .history-time {
        font-size: 24rpx;
        color: #999;
      }

      .history-remark {
        font-size: 26rpx;
        color: #666;
      }
    }
  }
}

// 二维码弹窗
.qrcode-popup {
  width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .popup-content {
    padding: 40rpx 30rpx;

    .qrcode-area {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 480rpx;
      height: 480rpx;
      margin-bottom: 20rpx;
      background: #f5f5f5;
      border-radius: 16rpx;

      .qrcode-image {
        width: 400rpx;
        height: 400rpx;
      }

      .qrcode-loading {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .qrcode-tips {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      padding: 16rpx;
      background: #fffbe6;
      border-radius: 8rpx;

      text {
        font-size: 24rpx;
        color: #fa8c16;
      }
    }
  }

  .popup-footer {
    padding: 30rpx;

    .save-btn {
      width: 100%;
      height: 88rpx;
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
      font-size: 32rpx;
      font-weight: 600;
      border-radius: 12rpx;
      border: none;
    }
  }
}

// 更多操作弹窗
.more-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .popup-content {
    padding: 20rpx 0;

    .action-list {
      .action-item {
        display: flex;
        align-items: center;
        gap: 24rpx;
        padding: 30rpx;
        font-size: 30rpx;
        color: #333;

        &:not(.danger):active {
          background: #f5f5f5;
        }

        &.danger {
          color: #ff4d4f;
        }

        & + .action-item {
          border-top: 1rpx solid #f0f0f0;
        }
      }
    }
  }
}
</style>
