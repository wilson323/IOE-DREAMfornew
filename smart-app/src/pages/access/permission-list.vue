<template>
  <view class="permission-list-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <uni-icons type="arrowleft" size="20" color="#333" @tap="goBack"></uni-icons>
        <text class="nav-title">我的权限</text>
        <view class="nav-actions">
          <uni-icons type="plus" size="20" color="#1890ff" @tap="goToApply"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 权限统计卡片 -->
    <view class="stats-container">
      <view class="stat-card valid">
        <view class="stat-icon">
          <uni-icons type="checkmarkempty" size="24" color="#52c41a"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.valid }}</text>
          <text class="stat-label">有效权限</text>
        </view>
      </view>
      <view class="stat-card expiring">
        <view class="stat-icon">
          <uni-icons type="alert" size="24" color="#fa8c16"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.expiring }}</text>
          <text class="stat-label">即将过期</text>
        </view>
      </view>
      <view class="stat-card expired">
        <view class="stat-icon">
          <uni-icons type="closeempty" size="24" color="#ff4d4f"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.expired }}</text>
          <text class="stat-label">已过期</text>
        </view>
      </view>
    </view>

    <!-- 搜索和筛选 -->
    <view class="filter-bar">
      <view class="search-wrapper">
        <uni-search-bar
          v-model="searchKeyword"
          placeholder="搜索区域名称"
          :radius="100"
          :clear-button="true"
          @confirm="onSearch"
          @clear="onSearchClear"
        ></uni-search-bar>
      </view>
      <view class="filter-tabs">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: currentTab === tab.value }"
          @tap="switchTab(tab.value)"
        >
          <text>{{ tab.label }}</text>
          <view v-if="currentTab === tab.value" class="tab-indicator"></view>
        </view>
      </view>
    </view>

    <!-- 权限列表 -->
    <scroll-view
      class="permission-list-container"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 空状态 -->
      <view v-if="!loading && permissionList.length === 0" class="empty-state">
        <image src="/static/images/empty-permission.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">{{ getEmptyText() }}</text>
        <button v-if="currentTab === 'valid'" class="apply-btn" @tap="goToApply">
          申请权限
        </button>
      </view>

      <!-- 权限卡片列表 -->
      <view v-for="permission in permissionList" :key="permission.permissionId" class="permission-card" @tap="goToDetail(permission)">
        <!-- 权限类型标识 -->
        <view class="permission-type" :class="getPermissionTypeClass(permission)">
          <uni-icons :type="getPermissionTypeIcon(permission)" size="16" color="#fff"></uni-icons>
          <text class="type-text">{{ getPermissionTypeText(permission) }}</text>
        </view>

        <!-- 权限信息 -->
        <view class="permission-info">
          <view class="permission-header">
            <view class="area-name-row">
              <uni-icons type="home" size="18" color="#1890ff"></uni-icons>
              <text class="area-name">{{ permission.areaName }}</text>
            </view>
            <view class="permission-status" :class="getStatusClass(permission)">
              {{ getStatusText(permission) }}
            </view>
          </view>

          <view class="permission-details">
            <view class="detail-item">
              <uni-icons type="calendar" size="14" color="#666"></uni-icons>
              <text>有效期：{{ formatValidityPeriod(permission) }}</text>
            </view>
            <view class="detail-item">
              <uni-icons type="clock" size="14" color="#666"></uni-icons>
              <text>时段：{{ getTimeRangeText(permission) }}</text>
            </view>
            <view v-if="permission.passType" class="detail-item">
              <uni-icons type="eye" size="14" color="#666"></uni-icons>
              <text>通行方式：{{ getPassTypeText(permission) }}</text>
            </view>
          </view>

          <!-- 使用统计 -->
          <view class="usage-stats">
            <view class="stat-item">
              <text class="stat-label">本月通行</text>
              <text class="stat-value">{{ permission.monthlyPassCount || 0 }}次</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">最后通行</text>
              <text class="stat-value">{{ formatLastPassTime(permission.lastPassTime) }}</text>
            </view>
          </view>

          <!-- 即将过期提醒 -->
          <view v-if="permission.expiringSoon" class="expiring-notice">
            <uni-icons type="alert" size="14" color="#fa8c16"></uni-icons>
            <text class="expiring-text">还剩 {{ permission.daysUntilExpiry }} 天过期</text>
          </view>

          <!-- 已过期标识 -->
          <view v-if="permission.permissionStatus === 3" class="expired-notice">
            <uni-icons type="close" size="14" color="#ff4d4f"></uni-icons>
            <text class="expired-text">已于 {{ formatDate(permission.expireTime) }} 过期</text>
          </view>
        </view>

        <!-- 快速操作 -->
        <view class="permission-actions">
          <button
            v-if="permission.permissionStatus === 1"
            class="action-btn qrcode-btn"
            @tap.stop="showQRCode(permission)"
          >
            <uni-icons type="qrcode" size="16" color="#1890ff"></uni-icons>
            <text>二维码</text>
          </button>
          <button
            v-if="permission.canRenew"
            class="action-btn renew-btn"
            @tap.stop="renewPermission(permission)"
          >
            <uni-icons type="loop" size="16" color="#52c41a"></uni-icons>
            <text>续期</text>
          </button>
          <button
            class="action-btn record-btn"
            @tap.stop="viewRecords(permission)"
          >
            <uni-icons type="list" size="16" color="#1890ff"></uni-icons>
            <text>记录</text>
          </button>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="hasMore" class="load-more">
        <uni-load-more :status="loadMoreStatus" :content-text="loadingText"></uni-load-more>
      </view>
    </scroll-view>

    <!-- 加载状态 -->
    <view v-if="loading && permissionList.length === 0" class="loading-container">
      <uni-load-more status="loading" :content-text="loadingText"></uni-load-more>
    </view>

    <!-- 二维码弹窗 -->
    <uni-popup ref="qrcodePopup" type="center">
      <view class="qrcode-popup">
        <view class="popup-header">
          <text class="popup-title">权限二维码</text>
          <uni-icons type="close" size="20" color="#999" @tap="closeQRCode"></uni-icons>
        </view>
        <view class="popup-content">
          <view class="qrcode-area">
            <image v-if="currentQRCode" :src="currentQRCode" mode="aspectFit" class="qrcode-image"></image>
            <view v-else class="qrcode-loading">
              <uni-load-more status="loading"></uni-load-more>
            </view>
          </view>
          <view class="qrcode-info">
            <text class="area-name">{{ currentPermission.areaName }}</text>
            <text class="validity-text">有效期至：{{ formatDate(currentPermission.expireTime) }}</text>
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

    <!-- 续期弹窗 -->
    <uni-popup ref="renewPopup" type="bottom">
      <view class="renew-popup">
        <view class="popup-header">
          <text class="popup-title">续期申请</text>
          <uni-icons type="close" size="20" color="#999" @tap="closeRenew"></uni-icons>
        </view>
        <view class="popup-content">
          <view class="renew-form">
            <view class="form-item">
              <text class="form-label">续期时长</text>
              <view class="duration-options">
                <view
                  v-for="option in renewDurationOptions"
                  :key="option.value"
                  class="duration-option"
                  :class="{ active: renewDuration === option.value }"
                  @tap="selectRenewDuration(option.value)"
                >
                  <text>{{ option.label }}</text>
                </view>
              </view>
            </view>
            <view class="form-item">
              <text class="form-label">续期原因</text>
              <textarea
                v-model="renewReason"
                class="form-textarea"
                placeholder="请输入续期原因"
                :maxlength="200"
              ></textarea>
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <button class="cancel-btn" @tap="closeRenew">取消</button>
          <button class="confirm-btn" @tap="submitRenew">提交申请</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { accessApi } from '@/api/business/access/access-api.js'

/**
 * 用户权限列表页面
 * 功能：
 * 1. 展示用户的所有门禁权限
 * 2. 权限状态筛选（有效/即将过期/已过期）
 * 3. 权限二维码展示
 * 4. 权限续期申请
 * 5. 使用记录查看
 */

// ==================== 状态数据 ====================

const loading = ref(false)
const refreshing = ref(false)
const searchKeyword = ref('')
const currentTab = ref('valid')
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 权限列表
const permissionList = ref([])

// 统计数据
const statistics = ref({
  valid: 0,
  expiring: 0,
  expired: 0
})

// Tab选项
const tabs = [
  { value: 'valid', label: '有效' },
  { value: 'expiring', label: '即将过期' },
  { value: 'expired', label: '已过期' }
]

// 加载文本
const loadingText = ref({
  contentdown: '上拉加载更多',
  contentrefresh: '加载中...',
  contentnomore: '没有更多数据了'
})

// 加载更多状态
const loadMoreStatus = ref('more')

// 二维码相关
const qrcodePopup = ref(null)
const currentQRCode = ref('')
const currentPermission = ref({})

// 续期相关
const renewPopup = ref(null)
const renewDuration = ref(30)
const renewReason = ref('')
const renewDurationOptions = [
  { value: 7, label: '7天' },
  { value: 15, label: '15天' },
  { value: 30, label: '30天' },
  { value: 90, label: '90天' },
  { value: 180, label: '180天' }
]

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[权限列表] 页面加载')
  loadStatistics()
  loadPermissionList()
})

// ==================== 数据加载 ====================

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const result = await accessApi.getUserPermissionStatistics()

    if (result.success && result.data) {
      statistics.value = result.data
    }
  } catch (error) {
    console.error('[权限列表] 加载统计失败:', error)
  }
}

/**
 * 加载权限列表
 */
const loadPermissionList = async () => {
  if (loading.value) return

  loading.value = true

  try {
    const params = {
      permissionStatus: getPermissionStatusParam(),
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const result = await accessApi.getUserPermissions(params)

    if (result.success && result.data) {
      const list = result.data.list || []

      if (pageNum.value === 1) {
        permissionList.value = list
      } else {
        permissionList.value.push(...list)
      }

      hasMore.value = list.length >= pageSize.value
      loadMoreStatus.value = hasMore.value ? 'more' : 'noMore'
    }
  } catch (error) {
    console.error('[权限列表] 加载失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  pageNum.value = 1
  loadStatistics()
  loadPermissionList().finally(() => {
    refreshing.value = false
  })
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!hasMore.value || loading.value) return

  pageNum.value++
  loadPermissionList()
}

// ==================== 交互事件 ====================

/**
 * 切换Tab
 */
const switchTab = (tab) => {
  currentTab.value = tab
  pageNum.value = 1
  permissionList.value = []
  hasMore.value = true
  loadPermissionList()
}

/**
 * 搜索
 */
const onSearch = () => {
  pageNum.value = 1
  permissionList.value = []
  loadPermissionList()
}

/**
 * 清除搜索
 */
const onSearchClear = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  permissionList.value = []
  loadPermissionList()
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 跳转到申请
 */
const goToApply = () => {
  uni.navigateTo({
    url: '/pages/access/permission-apply'
  })
}

/**
 * 跳转到详情
 */
const goToDetail = (permission) => {
  uni.navigateTo({
    url: `/pages/access/permission-detail?permissionId=${permission.permissionId}`
  })
}

// ==================== 权限操作 ====================

/**
 * 显示二维码
 */
const showQRCode = async (permission) => {
  currentPermission.value = permission
  currentQRCode.value = ''
  qrcodePopup.value?.open()

  try {
    const result = await accessApi.getPermissionQRCode(permission.permissionId)

    if (result.success && result.data) {
      currentQRCode.value = result.data.qrCode
    }
  } catch (error) {
    console.error('[权限列表] 获取二维码失败:', error)
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
  if (!currentQRCode.value) {
    uni.showToast({
      title: '二维码未加载完成',
      icon: 'none'
    })
    return
  }

  uni.saveImageToPhotosAlbum({
    filePath: currentQRCode.value,
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
const renewPermission = (permission) => {
  currentPermission.value = permission
  renewDuration.value = 30
  renewReason.value = ''
  renewPopup.value?.open()
}

/**
 * 关闭续期
 */
const closeRenew = () => {
  renewPopup.value?.close()
}

/**
 * 选择续期时长
 */
const selectRenewDuration = (duration) => {
  renewDuration.value = duration
}

/**
 * 提交续期
 */
const submitRenew = async () => {
  if (!renewReason.value || renewReason.value.trim().length === 0) {
    uni.showToast({
      title: '请输入续期原因',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '提交中...',
    mask: true
  })

  try {
    const result = await accessApi.renewPermission(currentPermission.value.permissionId, {
      duration: renewDuration.value,
      reason: renewReason.value
    })

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '申请已提交',
        icon: 'success'
      })
      closeRenew()
      onRefresh()
    }
  } catch (error) {
    uni.hideLoading()
    console.error('[权限列表] 续期失败:', error)
    uni.showToast({
      title: '提交失败',
      icon: 'none'
    })
  }
}

/**
 * 查看记录
 */
const viewRecords = (permission) => {
  uni.navigateTo({
    url: `/pages/access/access-record?permissionId=${permission.permissionId}`
  })
}

// ==================== 工具方法 ====================

/**
 * 获取权限状态参数
 */
const getPermissionStatusParam = () => {
  const statusMap = {
    'valid': 1,
    'expiring': 2,
    'expired': 3
  }
  return statusMap[currentTab.value]
}

/**
 * 获取权限类型类
 */
const getPermissionTypeClass = (permission) => {
  if (permission.permissionType === 1) {
    return 'type-permanent'
  } else if (permission.permissionType === 2) {
    return 'type-temporary'
  }
  return 'type-time'
}

/**
 * 获取权限类型图标
 */
const getPermissionTypeIcon = (permission) => {
  if (permission.permissionType === 1) {
    return 'locked'
  } else if (permission.permissionType === 2) {
    return 'calendar'
  }
  return 'clock'
}

/**
 * 获取权限类型文本
 */
const getPermissionTypeText = (permission) => {
  const typeMap = {
    1: '永久',
    2: '临时',
    3: '定时'
  }
  return typeMap[permission.permissionType] || '未知'
}

/**
 * 获取状态类
 */
const getStatusClass = (permission) => {
  if (permission.permissionStatus === 1) {
    return 'status-valid'
  } else if (permission.permissionStatus === 2) {
    return 'status-expiring'
  }
  return 'status-expired'
}

/**
 * 获取状态文本
 */
const getStatusText = (permission) => {
  if (permission.permissionStatus === 1) {
    return '有效'
  } else if (permission.permissionStatus === 2) {
    return '即将过期'
  }
  return '已过期'
}

/**
 * 格式化有效期
 */
const formatValidityPeriod = (permission) => {
  if (permission.permissionType === 1) {
    return '永久有效'
  }
  const start = formatDate(permission.effectiveTime)
  const end = formatDate(permission.expireTime)
  return `${start} 至 ${end}`
}

/**
 * 获取时段文本
 */
const getTimeRangeText = (permission) => {
  if (!permission.startTime || !permission.endTime) {
    return '全天'
  }
  return `${permission.startTime} - ${permission.endTime}`
}

/**
 * 获取通行方式文本
 */
const getPassTypeText = (permission) => {
  const types = {
    1: '人脸',
    2: '指纹',
    3: 'IC卡',
    4: '密码',
    5: '二维码'
  }

  if (!permission.passType || permission.passType.length === 0) {
    return '不限'
  }

  return permission.passType.map(type => types[type]).join('、')
}

/**
 * 格式化最后通行时间
 */
const formatLastPassTime = (timestamp) => {
  if (!timestamp) return '暂无'

  const now = Date.now()
  const diff = now - timestamp

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
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
 * 获取空状态文本
 */
const getEmptyText = () => {
  const textMap = {
    'valid': '暂无有效权限',
    'expiring': '暂无即将过期的权限',
    'expired': '暂无已过期的权限'
  }
  return textMap[currentTab.value]
}
</script>

<style lang="scss" scoped>
.permission-list-page {
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

// 统计卡片
.stats-container {
  display: flex;
  gap: 20rpx;
  margin: 30rpx;

  .stat-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 24rpx;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    .stat-icon {
      width: 56rpx;
      height: 56rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: #f5f5f5;
    }

    .stat-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4rpx;

      .stat-value {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }

    &.valid .stat-icon {
      background: rgba(82, 196, 26, 0.1);
    }

    &.expiring .stat-icon {
      background: rgba(250, 140, 22, 0.1);
    }

    &.expired .stat-icon {
      background: rgba(255, 77, 79, 0.1);
    }
  }
}

// 筛选栏
.filter-bar {
  margin: 0 30rpx 30rpx;

  .search-wrapper {
    margin-bottom: 20rpx;
  }

  .filter-tabs {
    display: flex;
    gap: 30rpx;

    .filter-tab {
      position: relative;
      padding: 12rpx 0;
      font-size: 28rpx;
      color: #666;

      &.active {
        color: #1890ff;
        font-weight: 500;
      }

      .tab-indicator {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 4rpx;
        background: #1890ff;
        border-radius: 2rpx;
      }
    }
  }
}

// 权限列表容器
.permission-list-container {
  flex: 1;
  padding: 0 30rpx 30rpx;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-image {
    width: 320rpx;
    height: 320rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 32rpx;
    color: #999;
    margin-bottom: 40rpx;
  }

  .apply-btn {
    width: 240rpx;
    height: 72rpx;
    line-height: 72rpx;
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    color: #fff;
    font-size: 28rpx;
    border-radius: 36rpx;
    border: none;
  }
}

// 权限卡片
.permission-card {
  position: relative;
  margin-bottom: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);

  // 权限类型标识
  .permission-type {
    position: absolute;
    top: 30rpx;
    right: 30rpx;
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    font-size: 24rpx;

    &.type-permanent {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }

    &.type-temporary {
      background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      color: #fff;
    }

    &.type-time {
      background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
      color: #fff;
    }
  }

  // 权限信息
  .permission-info {
    margin-bottom: 30rpx;

    .permission-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .area-name-row {
        display: flex;
        align-items: center;
        gap: 12rpx;

        .area-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
        }
      }

      .permission-status {
        padding: 6rpx 16rpx;
        border-radius: 20rpx;
        font-size: 24rpx;

        &.status-valid {
          background: rgba(82, 196, 26, 0.1);
          color: #52c41a;
        }

        &.status-expiring {
          background: rgba(250, 140, 22, 0.1);
          color: #fa8c16;
        }

        &.status-expired {
          background: rgba(255, 77, 79, 0.1);
          color: #ff4d4f;
        }
      }
    }

    .permission-details {
      display: flex;
      flex-direction: column;
      gap: 12rpx;
      margin-bottom: 20rpx;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 26rpx;
        color: #666;
      }
    }

    .usage-stats {
      display: flex;
      gap: 30rpx;
      padding: 16rpx;
      background: #f5f5f5;
      border-radius: 8rpx;

      .stat-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4rpx;

        .stat-label {
          font-size: 24rpx;
          color: #999;
        }

        .stat-value {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
        }
      }
    }

    .expiring-notice {
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin-top: 16rpx;
      padding: 12rpx;
      background: rgba(250, 140, 22, 0.08);
      border-radius: 8rpx;

      .expiring-text {
        flex: 1;
        font-size: 24rpx;
        color: #fa8c16;
      }
    }

    .expired-notice {
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin-top: 16rpx;
      padding: 12rpx;
      background: rgba(255, 77, 79, 0.08);
      border-radius: 8rpx;

      .expired-text {
        flex: 1;
        font-size: 24rpx;
        color: #ff4d4f;
      }
    }
  }

  // 快速操作
  .permission-actions {
    display: flex;
    gap: 16rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      height: 64rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 26rpx;
      color: #666;
      border: none;

      &.qrcode-btn {
        background: rgba(24, 144, 255, 0.1);
        color: #1890ff;
      }

      &.renew-btn {
        background: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }

      &.record-btn {
        background: rgba(24, 144, 255, 0.1);
        color: #1890ff;
      }
    }
  }
}

// 加载容器
.loading-container,
.load-more {
  padding: 60rpx 0;
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
      margin-bottom: 30rpx;
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

    .qrcode-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;
      margin-bottom: 20rpx;

      .area-name {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }

      .validity-text {
        font-size: 26rpx;
        color: #666;
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

// 续期弹窗
.renew-popup {
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
    padding: 30rpx;

    .renew-form {
      .form-item {
        margin-bottom: 30rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .form-label {
          display: block;
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
          margin-bottom: 16rpx;
        }

        .duration-options {
          display: grid;
          grid-template-columns: repeat(5, 1fr);
          gap: 16rpx;

          .duration-option {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 64rpx;
            background: #f5f5f5;
            border-radius: 12rpx;
            font-size: 26rpx;
            color: #666;
            border: 2rpx solid transparent;

            &.active {
              background: #e6f7ff;
              color: #1890ff;
              border-color: #1890ff;
            }
          }
        }

        .form-textarea {
          width: 100%;
          min-height: 160rpx;
          padding: 20rpx;
          background: #f5f5f5;
          border-radius: 12rpx;
          font-size: 28rpx;
          color: #333;
          border: none;
        }
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;

    .cancel-btn,
    .confirm-btn {
      flex: 1;
      height: 88rpx;
      font-size: 32rpx;
      font-weight: 600;
      border-radius: 12rpx;
      border: none;
    }

    .cancel-btn {
      background: #f5f5f5;
      color: #666;
    }

    .confirm-btn {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }
  }
}
</style>
