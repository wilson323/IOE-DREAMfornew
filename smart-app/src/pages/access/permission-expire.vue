<template>
  <view class="permission-expire-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <uni-icons type="arrowleft" size="20" color="#333" @tap="goBack"></uni-icons>
        <text class="nav-title">即将过期</text>
        <view class="nav-actions">
          <uni-icons type="settings" size="20" color="#1890ff" @tap="showSettings"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 提醒设置横幅 -->
    <view class="settings-banner">
      <uni-icons type="notification" size="18" color="#1890ff"></uni-icons>
      <text class="banner-text">提前 {{ advanceDays }} 天提醒</text>
      <view class="banner-actions" @tap="showSettings">
        <text>修改设置</text>
        <uni-icons type="arrowright" size="14" color="#1890ff"></uni-icons>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stat-item urgent">
        <text class="stat-value">{{ statistics.urgent || 0 }}</text>
        <text class="stat-label">7天内过期</text>
      </view>
      <view class="stat-item warning">
        <text class="stat-value">{{ statistics.warning || 0 }}</text>
        <text class="stat-label">30天内过期</text>
      </view>
      <view class="stat-item normal">
        <text class="stat-value">{{ statistics.normal || 0 }}</text>
        <text class="stat-label">30天后过期</text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <uni-load-more status="loading" :content-text="loadingText"></uni-load-more>
    </view>

    <template v-else>
      <!-- 筛选标签 -->
      <view class="filter-tabs">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: currentTab === tab.value }"
          @tap="switchTab(tab.value)"
        >
          <text>{{ tab.label }}</text>
          <view v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</view>
        </view>
      </view>

      <!-- 过期列表 -->
      <scroll-view
        class="expire-list-container"
        scroll-y
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <!-- 空状态 -->
        <view v-if="expireList.length === 0" class="empty-state">
          <image src="/static/images/empty-expire.png" mode="aspectFit" class="empty-image"></image>
          <text class="empty-text">{{ getEmptyText() }}</text>
          <text class="empty-hint">暂时没有即将过期的权限</text>
        </view>

        <!-- 过期卡片列表 -->
        <view v-for="item in expireList" :key="item.permissionId" class="expire-card" :class="getCardClass(item)">
          <!-- 紧急程度标识 -->
          <view class="urgency-badge" :class="getUrgencyClass(item)">
            <uni-icons :type="getUrgencyIcon(item)" size="14" color="#fff"></uni-icons>
            <text>{{ getUrgencyText(item) }}</text>
          </view>

          <!-- 权限信息 -->
          <view class="permission-info" @tap="goToDetail(item)">
            <view class="info-header">
              <view class="area-row">
                <uni-icons type="home" size="18" color="#1890ff"></uni-icons>
                <text class="area-name">{{ item.areaName }}</text>
              </view>
              <view class="days-remaining" :class="getDaysClass(item)">
                <text>还剩 {{ item.daysUntilExpiry }} 天</text>
              </view>
            </view>

            <view class="info-details">
              <view class="detail-item">
                <uni-icons type="calendar" size="14" color="#666"></uni-icons>
                <text>有效期至：{{ formatDate(item.expireTime) }}</text>
              </view>
              <view class="detail-item">
                <uni-icons type="loop" size="14" color="#666"></uni-icons>
                <text>续期次数：{{ item.renewCount || 0 }}次</text>
              </view>
              <view class="detail-item">
                <uni-icons type="eye" size="14" color="#666"></uni-icons>
                <text>本月通行：{{ item.monthlyPassCount || 0 }}次</text>
              </view>
            </view>

            <!-- 倒计时条 -->
            <view class="countdown-bar">
              <view class="countdown-progress" :style="{ width: getProgressWidth(item) }"></view>
            </view>
          </view>

          <!-- 快捷操作 -->
          <view class="card-actions">
            <button
              v-if="item.canOneClickRenew"
              class="action-btn quick-renew-btn"
              @tap="quickRenew(item)"
            >
              <uni-icons type="loop" size="16" color="#fff"></uni-icons>
              <text>一键续期</text>
            </button>
            <button
              class="action-btn renew-btn"
              @tap="renewPermission(item)"
            >
              <uni-icons type="compose" size="16" color="#1890ff"></uni-icons>
              <text>申请续期</text>
            </button>
            <button
              class="action-btn qrcode-btn"
              @tap="showQRCode(item)"
            >
              <uni-icons type="qrcode" size="16" color="#1890ff"></uni-icons>
              <text>二维码</text>
            </button>
          </view>
        </view>
      </scroll-view>
    </template>

    <!-- 设置弹窗 -->
    <uni-popup ref="settingsPopup" type="bottom">
      <view class="settings-popup">
        <view class="popup-header">
          <text class="popup-title">提醒设置</text>
          <uni-icons type="close" size="20" color="#999" @tap="closeSettings"></uni-icons>
        </view>
        <view class="popup-content">
          <view class="setting-group">
            <view class="group-title">提前提醒天数</view>
            <view class="days-options">
              <view
                v-for="option in reminderDaysOptions"
                :key="option.value"
                class="days-option"
                :class="{ active: advanceDays === option.value }"
                @tap="selectReminderDays(option.value)"
              >
                <text>{{ option.label }}</text>
              </view>
            </view>
          </view>
          <view class="setting-group">
            <view class="group-title">提醒方式</view>
            <view class="setting-item">
              <text class="setting-label">推送通知</text>
              <switch
                :checked="settings.pushEnabled"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="pushEnabled"
              />
            </view>
            <view class="setting-item">
              <text class="setting-label">短信提醒</text>
              <switch
                :checked="settings.smsEnabled"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="smsEnabled"
              />
            </view>
            <view class="setting-item">
              <text class="setting-label">应用内提醒</text>
              <switch
                :checked="settings.appEnabled"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="appEnabled"
              />
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <button class="save-btn" @tap="saveSettings">保存设置</button>
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
              <text class="form-label">当前权限</text>
              <view class="current-permission">
                <text class="area-name">{{ currentPermission.areaName }}</text>
                <text class="expire-time">有效期至：{{ formatDate(currentPermission.expireTime) }}</text>
              </view>
            </view>
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
              <view class="reason-chips">
                <view
                  v-for="chip in reasonChips"
                  :key="chip"
                  class="reason-chip"
                  :class="{ active: renewReason === chip }"
                  @tap="selectReason(chip)"
                >
                  <text>{{ chip }}</text>
                </view>
              </view>
              <textarea
                v-if="renewReason === '其他'"
                v-model="customReason"
                class="custom-reason-input"
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
 * 权限过期提醒页面
 * 功能：
 * 1. 展示即将过期的权限列表
 * 2. 按紧急程度分级显示
 * 3. 快捷续期操作
 * 4. 提醒设置管理
 * 5. 续期申请提交
 */

// ==================== 状态数据 ====================

const loading = ref(false)
const refreshing = ref(false)
const currentTab = ref('all')

// 过期列表
const expireList = ref([])

// 统计数据
const statistics = ref({
  urgent: 0,
  warning: 0,
  normal: 0
})

// Tab选项
const tabs = ref([
  { value: 'all', label: '全部', count: 0 },
  { value: 'urgent', label: '紧急', count: 0 },
  { value: 'warning', label: '提醒', count: 0 },
  { value: 'normal', label: '一般', count: 0 }
])

// 提前提醒天数
const advanceDays = ref(7)

// 提醒设置
const settings = ref({
  pushEnabled: true,
  smsEnabled: false,
  appEnabled: true
})

// 提醒天数选项
const reminderDaysOptions = [
  { value: 3, label: '提前3天' },
  { value: 7, label: '提前7天' },
  { value: 15, label: '提前15天' },
  { value: 30, label: '提前30天' }
]

// 续期相关
const renewPopup = ref(null)
const currentPermission = ref({})
const renewDuration = ref(30)
const renewReason = ref('')
const customReason = ref('')

const renewDurationOptions = [
  { value: 7, label: '7天' },
  { value: 15, label: '15天' },
  { value: 30, label: '30天' },
  { value: 90, label: '90天' },
  { value: 180, label: '180天' },
  { value: 365, label: '1年' }
]

const reasonChips = ['工作需要', '项目延期', '长期使用', '其他']

// 加载文本
const loadingText = ref({
  contentdown: '上拉加载更多',
  contentrefresh: '加载中...',
  contentnomore: '没有更多数据了'
})

// 弹窗引用
const settingsPopup = ref(null)

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[权限过期] 页面加载')
  loadStatistics()
  loadExpireList()
  loadSettings()
})

// ==================== 数据加载 ====================

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const result = await accessApi.getExpiringStatistics()

    if (result.success && result.data) {
      statistics.value = result.data
      updateTabCounts()
    }
  } catch (error) {
    console.error('[权限过期] 加载统计失败:', error)
  }
}

/**
 * 加载过期列表
 */
const loadExpireList = async () => {
  loading.value = true

  try {
    const params = {
      advanceDays: advanceDays.value,
      urgentLevel: getUrgencyLevelParam()
    }

    const result = await accessApi.getExpiringPermissions(params)

    if (result.success && result.data) {
      expireList.value = result.data.list || []
      updateTabCounts()
    }
  } catch (error) {
    console.error('[权限过期] 加载列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载设置
 */
const loadSettings = async () => {
  try {
    const result = await accessApi.getExpireReminderSettings()

    if (result.success && result.data) {
      settings.value = result.data.settings || settings.value
      advanceDays.value = result.data.advanceDays || 7
    }
  } catch (error) {
    console.error('[权限过期] 加载设置失败:', error)
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadStatistics()
  loadExpireList().finally(() => {
    refreshing.value = false
  })
}

// ==================== 交互事件 ====================

/**
 * 切换Tab
 */
const switchTab = (tab) => {
  currentTab.value = tab
  loadExpireList()
}

/**
 * 获取紧急等级参数
 */
const getUrgencyLevelParam = () => {
  if (currentTab.value === 'all') return null
  return currentTab.value
}

/**
 * 更新Tab计数
 */
const updateTabCounts = () => {
  const list = expireList.value

  tabs.value[0].count = list.length
  tabs.value[1].count = list.filter(item => item.daysUntilExpiry <= 7).length
  tabs.value[2].count = list.filter(item => item.daysUntilExpiry > 7 && item.daysUntilExpiry <= 30).length
  tabs.value[3].count = list.filter(item => item.daysUntilExpiry > 30).length
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 跳转到详情
 */
const goToDetail = (item) => {
  uni.navigateTo({
    url: `/pages/access/permission-detail?permissionId=${item.permissionId}`
  })
}

/**
 * 一键续期
 */
const quickRenew = (item) => {
  currentPermission.value = item
  renewDuration.value = 30
  renewReason.value = '工作需要'
  renewPopup.value?.open()
}

/**
 * 申请续期
 */
const renewPermission = (item) => {
  currentPermission.value = item
  renewDuration.value = 30
  renewReason.value = ''
  customReason.value = ''
  renewPopup.value?.open()
}

/**
 * 显示二维码
 */
const showQRCode = (item) => {
  uni.navigateTo({
    url: `/pages/access/permission-qrcode?permissionId=${item.permissionId}`
  })
}

// ==================== 设置操作 ====================

/**
 * 显示设置
 */
const showSettings = () => {
  settingsPopup.value?.open()
}

/**
 * 关闭设置
 */
const closeSettings = () => {
  settingsPopup.value?.close()
}

/**
 * 选择提醒天数
 */
const selectReminderDays = (days) => {
  advanceDays.value = days
}

/**
 * 设置变更
 */
const onSettingChange = (e) => {
  const setting = e.currentTarget.dataset.setting
  settings.value[setting] = e.detail.value
}

/**
 * 保存设置
 */
const saveSettings = async () => {
  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    const result = await accessApi.updateExpireReminderSettings({
      advanceDays: advanceDays.value,
      settings: settings.value
    })

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
      closeSettings()
      onRefresh()
    }
  } catch (error) {
    uni.hideLoading()
    console.error('[权限过期] 保存设置失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

// ==================== 续期操作 ====================

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
 * 选择续期原因
 */
const selectReason = (reason) => {
  if (renewReason.value === reason) {
    renewReason.value = ''
  } else {
    renewReason.value = reason
  }
}

/**
 * 提交续期
 */
const submitRenew = async () => {
  const finalReason = renewReason.value === '其他' ? customReason.value : renewReason.value

  if (!finalReason || finalReason.trim().length === 0) {
    uni.showToast({
      title: '请选择或输入续期原因',
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
      reason: finalReason
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
    console.error('[权限过期] 续期失败:', error)
    uni.showToast({
      title: '提交失败',
      icon: 'none'
    })
  }
}

// ==================== 工具方法 ====================

/**
 * 获取卡片类
 */
const getCardClass = (item) => {
  if (item.daysUntilExpiry <= 7) return 'card-urgent'
  if (item.daysUntilExpiry <= 30) return 'card-warning'
  return 'card-normal'
}

/**
 * 获取紧急程度类
 */
const getUrgencyClass = (item) => {
  if (item.daysUntilExpiry <= 7) return 'urgency-urgent'
  if (item.daysUntilExpiry <= 30) return 'urgency-warning'
  return 'urgency-normal'
}

/**
 * 获取紧急程度图标
 */
const getUrgencyIcon = (item) => {
  if (item.daysUntilExpiry <= 7) return 'alert'
  if (item.daysUntilExpiry <= 30) return 'info'
  return 'checkbox'
}

/**
 * 获取紧急程度文本
 */
const getUrgencyText = (item) => {
  if (item.daysUntilExpiry <= 7) return '紧急'
  if (item.daysUntilExpiry <= 30) return '提醒'
  return '一般'
}

/**
 * 获取天数类
 */
const getDaysClass = (item) => {
  if (item.daysUntilExpiry <= 7) return 'days-urgent'
  if (item.daysUntilExpiry <= 30) return 'days-warning'
  return 'days-normal'
}

/**
 * 获取进度条宽度
 */
const getProgressWidth = (item) => {
  const totalDays = 30
  const remaining = Math.min(item.daysUntilExpiry, totalDays)
  const percentage = ((totalDays - remaining) / totalDays) * 100
  return `${Math.max(0, Math.min(100, percentage))}%`
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
    'all': '暂无即将过期的权限',
    'urgent': '暂无紧急过期的权限',
    'warning': '暂无需要提醒的权限',
    'normal': '暂无一般过期的权限'
  }
  return textMap[currentTab.value]
}
</script>

<style lang="scss" scoped>
.permission-expire-page {
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

// 设置横幅
.settings-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 30rpx;
  padding: 20rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .banner-text {
    flex: 1;
    font-size: 26rpx;
    color: #666;
  }

  .banner-actions {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 26rpx;
    color: #1890ff;
  }
}

// 统计卡片
.stats-card {
  display: flex;
  gap: 20rpx;
  margin: 0 30rpx 30rpx;

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
    padding: 30rpx 20rpx;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    .stat-value {
      font-size: 48rpx;
      font-weight: 600;
      color: #333;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }

    &.urgent .stat-value {
      color: #ff4d4f;
    }

    &.warning .stat-value {
      color: #fa8c16;
    }

    &.normal .stat-value {
      color: #1890ff;
    }
  }
}

// 加载容器
.loading-container {
  padding: 120rpx 0;
}

// 筛选标签
.filter-tabs {
  display: flex;
  gap: 20rpx;
  margin: 0 30rpx 30rpx;
  overflow-x: auto;

  .filter-tab {
    position: relative;
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 20rpx;
    background: #fff;
    border-radius: 24rpx;
    font-size: 26rpx;
    color: #666;
    white-space: nowrap;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    &.active {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }

    .tab-badge {
      min-width: 32rpx;
      height: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 8rpx;
      background: rgba(0, 0, 0, 0.2);
      border-radius: 16rpx;
      font-size: 20rpx;
    }
  }
}

// 过期列表容器
.expire-list-container {
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
    margin-bottom: 16rpx;
  }

  .empty-hint {
    font-size: 26rpx;
    color: #bbb;
  }
}

// 过期卡片
.expire-card {
  margin-bottom: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  border-left: 6rpx solid transparent;

  &.card-urgent {
    border-left-color: #ff4d4f;
  }

  &.card-warning {
    border-left-color: #fa8c16;
  }

  &.card-normal {
    border-left-color: #1890ff;
  }

  // 紧急程度标识
  .urgency-badge {
    position: absolute;
    top: 30rpx;
    right: 30rpx;
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 6rpx 12rpx;
    border-radius: 20rpx;
    font-size: 22rpx;

    &.urgency-urgent {
      background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
      color: #fff;
    }

    &.urgency-warning {
      background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
      color: #fff;
    }

    &.urgency-normal {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }
  }

  // 权限信息
  .permission-info {
    margin-bottom: 20rpx;

    .info-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .area-row {
        display: flex;
        align-items: center;
        gap: 12rpx;

        .area-name {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
        }
      }

      .days-remaining {
        padding: 6rpx 16rpx;
        border-radius: 20rpx;
        font-size: 24rpx;

        &.days-urgent {
          background: rgba(255, 77, 79, 0.1);
          color: #ff4d4f;
        }

        &.days-warning {
          background: rgba(250, 140, 22, 0.1);
          color: #fa8c16;
        }

        &.days-normal {
          background: rgba(24, 144, 255, 0.1);
          color: #1890ff;
        }
      }
    }

    .info-details {
      display: flex;
      flex-direction: column;
      gap: 12rpx;
      margin-bottom: 16rpx;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 26rpx;
        color: #666;
      }
    }

    .countdown-bar {
      width: 100%;
      height: 8rpx;
      background: #f0f0f0;
      border-radius: 4rpx;
      overflow: hidden;

      .countdown-progress {
        height: 100%;
        background: linear-gradient(90deg, #1890ff 0%, #096dd9 100%);
        transition: width 0.3s ease;
      }
    }
  }

  // 快捷操作
  .card-actions {
    display: flex;
    gap: 12rpx;
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

      &.quick-renew-btn {
        background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
        color: #fff;
      }

      &.renew-btn,
      &.qrcode-btn {
        background: rgba(24, 144, 255, 0.1);
        color: #1890ff;
      }
    }
  }
}

// 设置弹窗
.settings-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;

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
    max-height: 60vh;
    overflow-y: auto;

    .setting-group {
      margin-bottom: 40rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .group-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 20rpx;
      }

      .days-options {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16rpx;

        .days-option {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 72rpx;
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

      .setting-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx 0;
        border-bottom: 1rpx solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .setting-label {
          font-size: 28rpx;
          color: #333;
        }
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

        .current-permission {
          display: flex;
          flex-direction: column;
          gap: 8rpx;
          padding: 20rpx;
          background: #f5f5f5;
          border-radius: 12rpx;

          .area-name {
            font-size: 28rpx;
            font-weight: 500;
            color: #333;
          }

          .expire-time {
            font-size: 24rpx;
            color: #666;
          }
        }

        .duration-options {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 16rpx;

          .duration-option {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 72rpx;
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

        .reason-chips {
          display: flex;
          flex-wrap: wrap;
          gap: 12rpx;

          .reason-chip {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 12rpx 20rpx;
            background: #f5f5f5;
            border-radius: 20rpx;
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

        .custom-reason-input {
          width: 100%;
          min-height: 120rpx;
          padding: 16rpx;
          margin-top: 12rpx;
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
