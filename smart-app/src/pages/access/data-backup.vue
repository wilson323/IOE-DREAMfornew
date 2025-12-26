<template>
  <view class="backup-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">数据备份恢复</text>
        </view>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 存储空间概览 -->
      <view class="storage-section">
        <view class="storage-card">
          <view class="storage-header">
            <text class="storage-title">存储空间</text>
            <text class="storage-usage">{{ storageInfo.used }}/{{ storageInfo.total }}</text>
          </view>
          <view class="storage-bar">
            <view
              class="storage-fill"
              :style="{ width: storageInfo.percent + '%' }"
            ></view>
          </view>
          <view class="storage-details">
            <view class="detail-item">
              <view class="detail-dot data"></view>
              <text class="detail-label">业务数据 {{ storageInfo.dataSize }}</text>
            </view>
            <view class="detail-item">
              <view class="detail-dot backup"></view>
              <text class="detail-label">备份数据 {{ storageInfo.backupSize }}</text>
            </view>
            <view class="detail-item">
              <view class="detail-dot free"></view>
              <text class="detail-label">可用 {{ storageInfo.freeSize }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 快速操作 -->
      <view class="quick-actions">
        <view class="action-card" @tap="createBackup">
          <view class="action-icon backup">
            <uni-icons type="cloud-upload" size="28" color="#fff"></uni-icons>
          </view>
          <text class="action-title">立即备份</text>
          <text class="action-desc">创建新的数据备份</text>
        </view>
        <view class="action-card" @tap="restoreBackup">
          <view class="action-icon restore">
            <uni-icons type="cloud-download" size="28" color="#fff"></uni-icons>
          </view>
          <text class="action-title">数据恢复</text>
          <text class="action-desc">从备份恢复数据</text>
        </view>
        <view class="action-card" @tap="autoBackupSettings">
          <view class="action-icon schedule">
            <uni-icons type="calendar" size="28" color="#fff"></uni-icons>
          </view>
          <text class="action-title">自动备份</text>
          <text class="action-desc">设置定时备份任务</text>
        </view>
        <view class="action-card" @tap="cleanOldBackups">
          <view class="action-icon clean">
            <uni-icons type="trash" size="28" color="#fff"></uni-icons>
          </view>
          <text class="action-title">清理备份</text>
          <text class="action-desc">删除旧的备份文件</text>
        </view>
      </view>

      <!-- 最近备份 -->
      <view class="backup-list-section">
        <view class="section-header">
          <text class="section-title">最近备份</text>
          <text class="section-more" @tap="viewAllBackups">查看全部</text>
        </view>
        <view class="backup-list">
          <view
            class="backup-item"
            v-for="(backup, index) in backupList"
            :key="index"
          >
            <view class="backup-icon">
              <uni-icons type="folder" size="24" color="#667eea"></uni-icons>
            </view>
            <view class="backup-info">
              <text class="backup-name">{{ backup.name }}</text>
              <view class="backup-details">
                <text class="backup-size">{{ backup.size }}</text>
                <text class="backup-separator">·</text>
                <text class="backup-time">{{ backup.createTime }}</text>
              </view>
            </view>
            <view class="backup-actions">
              <view class="action-btn" @tap="restoreFromBackup(backup)">
                <text>恢复</text>
              </view>
              <view class="action-btn danger" @tap="deleteBackup(backup, index)">
                <text>删除</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 自动备份配置 -->
      <view class="auto-backup-section">
        <view class="section-header">
          <text class="section-title">自动备份配置</text>
        </view>
        <view class="auto-backup-config">
          <view class="config-item">
            <text class="config-label">启用自动备份</text>
            <switch
              :checked="autoBackupConfig.enabled"
              color="#667eea"
              @change="onAutoBackupToggle"
            ></switch>
          </view>
          <view class="config-item selector" v-if="autoBackupConfig.enabled">
            <text class="config-label">备份频率</text>
            <picker
              mode="selector"
              :range="frequencyOptions"
              :value="frequencyIndex"
              @change="onFrequencyChange"
            >
              <view class="selector-value">
                <text>{{ frequencyOptions[frequencyIndex] }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
          <view class="config-item selector" v-if="autoBackupConfig.enabled">
            <text class="config-label">保留份数</text>
            <picker
              mode="selector"
              :range="retainOptions"
              :value="retainIndex"
              @change="onRetainChange"
            >
              <view class="selector-value">
                <text>{{ retainOptions[retainIndex] }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
          <view class="config-item" v-if="autoBackupConfig.enabled">
            <text class="config-label">下次备份时间</text>
            <text class="config-value">{{ nextBackupTime }}</text>
          </view>
        </view>
      </view>

      <!-- 备份设置 -->
      <view class="backup-settings-section">
        <view class="section-header">
          <text class="section-title">备份设置</text>
        </view>
        <view class="settings-list">
          <view class="setting-item" @tap="selectBackupPath">
            <view class="setting-info">
              <text class="setting-label">备份存储路径</text>
              <text class="setting-desc">{{ backupSettings.storagePath }}</text>
            </view>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="setting-item" @tap="selectBackupType">
            <view class="setting-info">
              <text class="setting-label">备份内容</text>
              <text class="setting-desc">{{ backupSettings.backupType }}</text>
            </view>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="setting-item" @tap="compressionSettings">
            <view class="setting-info">
              <text class="setting-label">压缩备份文件</text>
              <text class="setting-desc">{{ backupSettings.compression ? '已启用' : '未启用' }}</text>
            </view>
            <switch
              :checked="backupSettings.compression"
              color="#667eea"
              @change="onCompressionToggle"
            ></switch>
          </view>
          <view class="setting-item" @tap="encryptionSettings">
            <view class="setting-info">
              <text class="setting-label">加密备份文件</text>
              <text class="setting-desc">{{ backupSettings.encryption ? '已启用' : '未启用' }}</text>
            </view>
            <switch
              :checked="backupSettings.encryption"
              color="#667eea"
              @change="onEncryptionToggle"
            ></switch>
          </view>
        </view>
      </view>

      <!-- 备份统计 -->
      <view class="backup-statistics-section">
        <view class="section-header">
          <text class="section-title">备份统计</text>
        </view>
        <view class="statistics-grid">
          <view class="statistics-item">
            <text class="statistics-value">{{ statistics.totalBackups }}</text>
            <text class="statistics-label">总备份数</text>
          </view>
          <view class="statistics-item">
            <text class="statistics-value">{{ statistics.totalSize }}</text>
            <text class="statistics-label">占用空间</text>
          </view>
          <view class="statistics-item">
            <text class="statistics-value">{{ statistics.lastBackup }}</text>
            <text class="statistics-label">上次备份</text>
          </view>
          <view class="statistics-item">
            <text class="statistics-value">{{ statistics.successRate }}%</text>
            <text class="statistics-label">成功率</text>
          </view>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 删除确认弹窗 -->
    <uni-popup ref="deletePopup" type="center">
      <view class="delete-popup">
        <view class="popup-header">
          <text class="popup-title">确认删除</text>
          <view class="popup-close" @tap="closeDeletePopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>
        <view class="popup-content">
          <text class="content-text">确定要删除该备份文件吗？此操作不可撤销。</text>
        </view>
        <view class="popup-actions">
          <view class="action-btn cancel" @tap="closeDeletePopup">取消</view>
          <view class="action-btn confirm" @tap="confirmDelete">删除</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 存储信息
const storageInfo = reactive({
  total: '100GB',
  used: '45.8GB',
  free: '54.2GB',
  percent: 45.8,
  dataSize: '28.5GB',
  backupSize: '17.3GB',
  freeSize: '54.2GB'
})

// 备份列表
const backupList = ref([
  {
    id: 1,
    name: '完整备份_20241224',
    size: '12.8GB',
    createTime: '2024-12-24 02:00'
  },
  {
    id: 2,
    name: '增量备份_20241223',
    size: '2.3GB',
    createTime: '2024-12-23 02:00'
  },
  {
    id: 3,
    name: '完整备份_20241220',
    size: '11.5GB',
    createTime: '2024-12-20 02:00'
  },
  {
    id: 4,
    name: '增量备份_20241219',
    size: '1.8GB',
    createTime: '2024-12-19 02:00'
  }
])

// 自动备份配置
const autoBackupConfig = reactive({
  enabled: true,
  frequency: 'daily',
  retainCount: 7
})

const frequencyOptions = ['每天', '每周', '每月']
const frequencyIndex = ref(0)
const retainOptions = ['保留5份', '保留7份', '保留10份', '保留15份', '保留30份']
const retainIndex = ref(1)

// 下次备份时间
const nextBackupTime = computed(() => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  tomorrow.setHours(2, 0, 0, 0)
  return tomorrow.toLocaleString('zh-CN', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
})

// 备份设置
const backupSettings = reactive({
  storagePath: '/data/backups',
  backupType: '完整备份（业务数据+配置文件）',
  compression: true,
  encryption: true
})

// 统计数据
const statistics = reactive({
  totalBackups: 24,
  totalSize: '156.8GB',
  lastBackup: '2小时前',
  successRate: 99.2
})

// 待删除的备份
const backupToDelete = ref(null)

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 创建备份
const createBackup = () => {
  uni.showModal({
    title: '创建备份',
    content: '确定要创建新的数据备份吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '备份中...' })
        // TODO: 调用实际备份API
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '备份成功',
            icon: 'success'
          })
        }, 2000)
      }
    }
  })
}

// 恢复备份
const restoreBackup = () => {
  uni.navigateTo({
    url: '/pages/access/backup-select'
  })
}

// 自动备份设置
const autoBackupSettings = () => {
  uni.navigateTo({
    url: '/pages/access/auto-backup-settings'
  })
}

// 清理旧备份
const cleanOldBackups = () => {
  uni.navigateTo({
    url: '/pages/access/backup-clean'
  })
}

// 从备份恢复
const restoreFromBackup = (backup) => {
  uni.showModal({
    title: '恢复数据',
    content: `确定要从"${backup.name}"恢复数据吗？\n\n当前数据将被覆盖，建议先创建备份。`,
    confirmText: '恢复',
    confirmColor: '#ff4d4f',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '恢复中...' })
        // TODO: 调用实际恢复API
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '恢复成功',
            icon: 'success'
          })
        }, 3000)
      }
    }
  })
}

// 删除备份
const deleteBackup = (backup, index) => {
  backupToDelete.value = { backup, index }
  uni.$emit('showDeletePopup')
}

// 关闭删除弹窗
const closeDeletePopup = () => {
  backupToDelete.value = null
  uni.$emit('hideDeletePopup')
}

// 确认删除
const confirmDelete = () => {
  if (backupToDelete.value) {
    const { index } = backupToDelete.value
    backupList.value.splice(index, 1)
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    })
    closeDeletePopup()
    // TODO: 调用实际删除API
  }
}

// 查看全部备份
const viewAllBackups = () => {
  uni.navigateTo({
    url: '/pages/access/backup-list'
  })
}

// 自动备份开关
const onAutoBackupToggle = (e) => {
  autoBackupConfig.enabled = e.detail.value
  // TODO: 调用API更新设置
}

// 频率变更
const onFrequencyChange = (e) => {
  frequencyIndex.value = e.detail.value
  autoBackupConfig.frequency = ['daily', 'weekly', 'monthly'][e.detail.value]
  // TODO: 调用API更新设置
}

// 保留份数变更
const onRetainChange = (e) => {
  retainIndex.value = e.detail.value
  autoBackupConfig.retainCount = [5, 7, 10, 15, 30][e.detail.value]
  // TODO: 调用API更新设置
}

// 选择备份路径
const selectBackupPath = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 选择备份类型
const selectBackupType = () => {
  uni.navigateTo({
    url: '/pages/access/backup-type-select'
  })
}

// 压缩设置
const compressionSettings = () => {
  // 切换开关已处理
}

// 压缩开关
const onCompressionToggle = (e) => {
  backupSettings.compression = e.detail.value
  // TODO: 调用API更新设置
}

// 加密设置
const encryptionSettings = () => {
  // 切换开关已处理
}

// 加密开关
const onEncryptionToggle = (e) => {
  backupSettings.encryption = e.detail.value
  // TODO: 调用API更新设置
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.backup-container {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1000;

  .navbar-content {
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 30rpx;

    .navbar-left {
      display: flex;
      align-items: center;

      .navbar-title {
        margin-left: 20rpx;
        font-size: 18px;
        font-weight: 500;
        color: #fff;
      }
    }
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 44px);
}

// 存储空间
.storage-section {
  padding: 30rpx;

  .storage-card {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .storage-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .storage-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }

      .storage-usage {
        font-size: 14px;
        color: #667eea;
        font-weight: 500;
      }
    }

    .storage-bar {
      width: 100%;
      height: 12rpx;
      background: #f5f5f5;
      border-radius: 6rpx;
      overflow: hidden;
      margin-bottom: 20rpx;

      .storage-fill {
        height: 100%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 6rpx;
        transition: width 0.3s;
      }
    }

    .storage-details {
      display: flex;
      justify-content: space-around;

      .detail-item {
        display: flex;
        align-items: center;

        .detail-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 6rpx;
          margin-right: 10rpx;

          &.data {
            background: #667eea;
          }

          &.backup {
            background: #52c41a;
          }

          &.free {
            background: #e0e0e0;
          }
        }

        .detail-label {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

// 快速操作
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 0 30rpx 30rpx;

  .action-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30rpx 20rpx;
    background: #fff;
    border-radius: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .action-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 20rpx;

      &.backup {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.restore {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }

      &.schedule {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }

      &.clean {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
    }

    .action-title {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 6rpx;
    }

    .action-desc {
      font-size: 11px;
      color: #999;
      text-align: center;
    }
  }
}

// 备份列表
.backup-list-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .section-more {
      font-size: 13px;
      color: #667eea;
    }
  }

  .backup-list {
    .backup-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .backup-icon {
        width: 56rpx;
        height: 56rpx;
        border-radius: 12rpx;
        background: #f9f9f9;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .backup-info {
        flex: 1;

        .backup-name {
          display: block;
          font-size: 14px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .backup-details {
          display: flex;
          align-items: center;
          font-size: 12px;
          color: #999;

          .backup-separator {
            margin: 0 10rpx;
          }
        }
      }

      .backup-actions {
        display: flex;
        gap: 10rpx;

        .action-btn {
          padding: 8rpx 20rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 12rpx;
          font-size: 12px;
          color: #fff;

          &.danger {
            background: #fff1f0;
            color: #ff4d4f;
          }
        }
      }
    }
  }
}

// 自动备份配置
.auto-backup-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .auto-backup-config {
    .config-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &.selector {
        padding: 30rpx 20rpx;
        background: #f9f9f9;
        border-radius: 12rpx;
        margin-bottom: 20rpx;

        &:last-of-type {
          margin-bottom: 0;
        }
      }

      .config-label {
        font-size: 14px;
        color: #333;
      }

      .config-value {
        font-size: 13px;
        color: #667eea;
      }

      .selector-value {
        display: flex;
        align-items: center;
        font-size: 13px;
        color: #666;

        text {
          margin-right: 10rpx;
        }
      }
    }
  }
}

// 备份设置
.backup-settings-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .settings-list {
    .setting-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .setting-info {
        flex: 1;

        .setting-label {
          display: block;
          font-size: 14px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .setting-desc {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

// 备份统计
.backup-statistics-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .statistics-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;

    .statistics-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .statistics-value {
        font-size: 24px;
        font-weight: 600;
        color: #333;
        margin-bottom: 10rpx;
      }

      .statistics-label {
        font-size: 11px;
        color: #999;
      }
    }
  }
}

// 删除确认弹窗
.delete-popup {
  width: 560rpx;
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
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      padding: 10rpx;
    }
  }

  .popup-content {
    padding: 40rpx 30rpx;

    .content-text {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
    }
  }

  .popup-actions {
    display: flex;
    border-top: 1rpx solid #f0f0f0;

    .action-btn {
      flex: 1;
      height: 88rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 15px;
      font-weight: 500;

      &.cancel {
        color: #666;
        border-right: 1rpx solid #f0f0f0;
      }

      &.confirm {
        color: #ff4d4f;
      }
    }
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
