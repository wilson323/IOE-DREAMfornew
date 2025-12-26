<template>
  <view class="offline-settings-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-back" @click="goBack">
        <uni-icons type="arrowleft" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">离线设置</view>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 设置列表 -->
    <view class="settings-section">
      <view class="section-title">同步设置</view>

      <!-- 自动同步开关 -->
      <view class="setting-item">
        <view class="item-left">
          <uni-icons type="cloud-upload" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">自动同步</view>
            <view class="item-desc">网络恢复时自动同步数据</view>
          </view>
        </view>
        <switch
          :checked="settings.autoSync"
          color="#1890ff"
          @change="handleAutoSyncChange"
        ></switch>
      </view>

      <!-- 同步间隔 -->
      <view class="setting-item">
        <view class="item-left">
          <uni-icons type="calendar" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">同步间隔</view>
            <view class="item-desc">定时同步的时间间隔</view>
          </view>
        </view>
        <picker
          mode="selector"
          :range="syncIntervalOptions"
          :value="syncIntervalIndex"
          @change="handleSyncIntervalChange"
        >
          <view class="picker-value">
            {{ syncIntervalOptions[syncIntervalIndex] }}
            <uni-icons type="arrowright" size="14" color="#999"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 仅Wi-Fi同步 -->
      <view class="setting-item">
        <view class="item-left">
          <uni-icons type="wifi" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">仅Wi-Fi同步</view>
            <view class="item-desc">只在连接Wi-Fi时同步数据</view>
          </view>
        </view>
        <switch
          :checked="settings.wifiOnly"
          color="#1890ff"
          @change="handleWifiOnlyChange"
        ></switch>
      </view>
    </view>

    <view class="settings-section">
      <view class="section-title">数据管理</view>

      <!-- 权限数据缓存 -->
      <view class="setting-item" @click="showPermissionCache">
        <view class="item-left">
          <uni-icons type="locked" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">权限数据缓存</view>
            <view class="item-desc">{{ permissionCacheInfo }}</view>
          </view>
        </view>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>

      <!-- 通行记录缓存 -->
      <view class="setting-item" @click="showRecordCache">
        <view class="item-left">
          <uni-icons type="list" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">通行记录缓存</view>
            <view class="item-desc">{{ recordCacheInfo }}</view>
          </view>
        </view>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>

      <!-- 清除过期数据 -->
      <view class="setting-item" @click="handleClearExpired">
        <view class="item-left">
          <uni-icons type="trash" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">清除过期数据</view>
            <view class="item-desc">删除已过期的权限和记录</view>
          </view>
        </view>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>

      <!-- 导出离线数据 -->
      <view class="setting-item" @click="handleExportData">
        <view class="item-left">
          <uni-icons type="download" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">导出离线数据</view>
            <view class="item-desc">导出本地缓存的数据</view>
          </view>
        </view>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <view class="settings-section">
      <view class="section-title">高级设置</view>

      <!-- 冲突解决策略 -->
      <view class="setting-item">
        <view class="item-left">
          <uni-icons type="gear" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">冲突解决策略</view>
            <view class="item-desc">数据冲突时的处理方式</view>
          </view>
        </view>
        <picker
          mode="selector"
          :range="conflictStrategyOptions"
          :value="conflictStrategyIndex"
          @change="handleConflictStrategyChange"
        >
          <view class="picker-value">
            {{ conflictStrategyOptions[conflictStrategyIndex] }}
            <uni-icons type="arrowright" size="14" color="#999"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 最大重试次数 -->
      <view class="setting-item">
        <view class="item-left">
          <uni-icons type="refresh" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">最大重试次数</view>
            <view class="item-desc">同步失败后的重试次数</view>
          </view>
        </view>
        <picker
          mode="selector"
          :range="retryOptions"
          :value="retryIndex"
          @change="handleRetryChange"
        >
          <view class="picker-value">
            {{ retryOptions[retryIndex] }}次
            <uni-icons type="arrowright" size="14" color="#999"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 离线模式 -->
      <view class="setting-item">
        <view class="item-left">
          <uni-icons type="eye" size="20" color="#666"></uni-icons>
          <view class="item-info">
            <view class="item-title">纯离线模式</view>
            <view class="item-desc">不进行任何同步，仅使用本地数据</view>
          </view>
        </view>
        <switch
          :checked="settings.pureOfflineMode"
          color="#1890ff"
          @change="handlePureOfflineModeChange"
        ></switch>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="bottom-actions">
      <button class="action-btn primary" @click="handleSaveSettings">
        <uni-icons type="checkmarkempty" size="18" color="#fff"></uni-icons>
        <text>保存设置</text>
      </button>
      <button class="action-btn secondary" @click="handleResetSettings">
        <uni-icons type="refreshempty" size="18" color="#666"></uni-icons>
        <text>重置默认</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { storage, STORAGE_KEYS } from '@/utils/offline-storage.js'
import { offlinePermission } from '@/utils/offline-permission.js'
import { offlineRecord } from '@/utils/offline-record.js'

// 设置数据
const settings = ref({
  autoSync: true,
  syncInterval: 60000,  // 60秒
  wifiOnly: false,
  pureOfflineMode: false,
  conflictStrategy: 'server_win',
  maxRetry: 3
})

// 选项数据
const syncIntervalOptions = ref(['30秒', '1分钟', '5分钟', '10分钟', '30分钟'])
const syncIntervalIndex = ref(1)  // 默认1分钟

const conflictStrategyOptions = ref(['服务端优先', '客户端优先', '智能合并', '手动处理'])
const conflictStrategyIndex = ref(0)

const retryOptions = ref(['1', '3', '5', '10'])
const retryIndex = ref(1)

// 缓存信息
const permissionCount = ref(0)
const recordCount = ref(0)

// 计算属性
const permissionCacheInfo = computed(() => {
  return `${permissionCount.value} 条权限`
})

const recordCacheInfo = computed(() => {
  return `${recordCount.value} 条记录`
})

// 初始化
onMounted(async () => {
  console.log('[离线设置页] 页面加载')

  // 加载设置
  await loadSettings()

  // 加载缓存统计
  await loadCacheStatistics()
})

// 加载设置
const loadSettings = async () => {
  try {
    const savedSettings = await storage.get(STORAGE_KEYS.OFFLINE_SETTINGS, {})
    if (savedSettings && Object.keys(savedSettings).length > 0) {
      settings.value = { ...settings.value, ...savedSettings }

      // 更新选项索引
      if (savedSettings.syncInterval) {
        const index = [30000, 60000, 300000, 600000, 1800000].indexOf(savedSettings.syncInterval)
        if (index > -1) {
          syncIntervalIndex.value = index
        }
      }

      if (savedSettings.conflictStrategy) {
        const index = ['server_win', 'client_win', 'merge', 'manual'].indexOf(savedSettings.conflictStrategy)
        if (index > -1) {
          conflictStrategyIndex.value = index
        }
      }

      if (savedSettings.maxRetry) {
        const index = [1, 3, 5, 10].indexOf(savedSettings.maxRetry)
        if (index > -1) {
          retryIndex.value = index
        }
      }
    }

    console.log('[离线设置页] 加载设置:', settings.value)
  } catch (error) {
    console.error('[离线设置页] 加载设置失败:', error)
  }
}

// 加载缓存统计
const loadCacheStatistics = async () => {
  try {
    // 权限统计
    const permissionStats = await offlinePermission.getStatistics()
    permissionCount.value = permissionStats ? permissionStats.total : 0

    // 记录统计
    const recordStats = await offlineRecord.getStatistics()
    recordCount.value = recordStats ? recordStats.total : 0

    console.log('[离线设置页] 缓存统计:', {
      permission: permissionCount.value,
      record: recordCount.value
    })
  } catch (error) {
    console.error('[离线设置页] 加载统计失败:', error)
  }
}

// 处理自动同步开关
const handleAutoSyncChange = (e) => {
  settings.value.autoSync = e.detail.value
  console.log('[离线设置页] 自动同步:', settings.value.autoSync)
}

// 处理同步间隔选择
const handleSyncIntervalChange = (e) => {
  syncIntervalIndex.value = e.detail.value
  const intervals = [30000, 60000, 300000, 600000, 1800000]
  settings.value.syncInterval = intervals[e.detail.value]
  console.log('[离线设置页] 同步间隔:', settings.value.syncInterval)
}

// 处理仅Wi-Fi同步
const handleWifiOnlyChange = (e) => {
  settings.value.wifiOnly = e.detail.value
  console.log('[离线设置页] 仅Wi-Fi:', settings.value.wifiOnly)
}

// 处理冲突策略选择
const handleConflictStrategyChange = (e) => {
  conflictStrategyIndex.value = e.detail.value
  const strategies = ['server_win', 'client_win', 'merge', 'manual']
  settings.value.conflictStrategy = strategies[e.detail.value]
  console.log('[离线设置页] 冲突策略:', settings.value.conflictStrategy)
}

// 处理重试次数选择
const handleRetryChange = (e) => {
  retryIndex.value = e.detail.value
  const retries = [1, 3, 5, 10]
  settings.value.maxRetry = retries[e.detail.value]
  console.log('[离线设置页] 重试次数:', settings.value.maxRetry)
}

// 处理纯离线模式
const handlePureOfflineModeChange = (e) => {
  settings.value.pureOfflineMode = e.detail.value
  console.log('[离线设置页] 纯离线模式:', settings.value.pureOfflineMode)
}

// 显示权限缓存详情
const showPermissionCache = () => {
  uni.showModal({
    title: '权限数据缓存',
    content: `当前缓存 ${permissionCount.value} 条权限数据\n这些数据用于离线通行验证`,
    showCancel: false
  })
}

// 显示记录缓存详情
const showRecordCache = () => {
  uni.showModal({
    title: '通行记录缓存',
    content: `当前缓存 ${recordCount.value} 条通行记录\n这些记录将在网络恢复后上传`,
    showCancel: false
  })
}

// 清除过期数据
const handleClearExpired = async () => {
  uni.showModal({
    title: '确认清除',
    content: '将清除所有已过期的权限数据，此操作不可恢复',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await offlinePermission.clearExpired()
          if (result.success) {
            uni.showToast({
              title: `已清除${result.cleared}条`,
              icon: 'success'
            })
            await loadCacheStatistics()
          } else {
            uni.showToast({
              title: result.message || '清除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('[离线设置页] 清除过期数据失败:', error)
          uni.showToast({
            title: '清除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 导出离线数据
const handleExportData = () => {
  uni.showActionSheet({
    itemList: ['导出为JSON', '导出为CSV'],
    success: async (res) => {
      try {
        let data = null
        let fileName = ''

        if (res.tapIndex === 0) {
          // JSON格式
          data = await offlineRecord.exportRecords('json')
          fileName = `pass_records_${Date.now()}.json`
        } else if (res.tapIndex === 1) {
          // CSV格式
          data = await offlineRecord.exportRecords('csv')
          fileName = `pass_records_${Date.now()}.csv`
        }

        if (data) {
          // TODO: 实际导出文件到本地或分享
          console.log('[离线设置页] 导出数据:', fileName, data.length)

          uni.showToast({
            title: '导出成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: '导出失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('[离线设置页] 导出失败:', error)
        uni.showToast({
          title: '导出失败',
          icon: 'none'
        })
      }
    }
  })
}

// 保存设置
const handleSaveSettings = async () => {
  try {
    console.log('[离线设置页] 保存设置:', settings.value)

    const result = await storage.set(STORAGE_KEYS.OFFLINE_SETTINGS, settings.value)

    if (result) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })

      // TODO: 通知其他模块更新设置
      // offlineSync.updateSettings(settings.value)
    } else {
      uni.showToast({
        title: '保存失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('[离线设置页] 保存设置失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

// 重置设置
const handleResetSettings = () => {
  uni.showModal({
    title: '确认重置',
    content: '将恢复所有设置为默认值',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        settings.value = {
          autoSync: true,
          syncInterval: 60000,
          wifiOnly: false,
          pureOfflineMode: false,
          conflictStrategy: 'server_win',
          maxRetry: 3
        }

        syncIntervalIndex.value = 1
        conflictStrategyIndex.value = 0
        retryIndex.value = 1

        uni.showToast({
          title: '已重置',
          icon: 'success'
        })
      }
    }
  })
}

// 导航
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.offline-settings-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 180rpx;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;

  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
  }

  .nav-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }

  .nav-placeholder {
    width: 60rpx;
  }
}

.settings-section {
  margin-top: 24rpx;

  .section-title {
    padding: 24rpx 32rpx;
    font-size: 24rpx;
    color: #999;
    font-weight: 500;
  }

  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 32rpx;
    background: #fff;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .item-left {
      flex: 1;
      display: flex;
      align-items: center;
      margin-right: 16rpx;

      ::v-deep .uni-icons {
        margin-right: 16rpx;
      }

      .item-info {
        flex: 1;

        .item-title {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 4rpx;
        }

        .item-desc {
          font-size: 24rpx;
          color: #999;
        }
      }
    }

    .picker-value {
      display: flex;
      align-items: center;
      font-size: 28rpx;
      color: #666;

      ::v-deep .uni-icons {
        margin-left: 8rpx;
      }
    }
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
  display: flex;
  gap: 16rpx;

  .action-btn {
    flex: 1;
    height: 88rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    border: none;

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }

    &.secondary {
      background: #f5f5f5;
      color: #666;
    }

    ::v-deep .uni-icons {
      margin-right: 8rpx;
    }
  }
}
</style>
