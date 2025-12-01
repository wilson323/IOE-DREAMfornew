<template>
  <view class="device-detail">
    <!-- 设备头部信息 -->
    <view class="device-header">
      <view class="header-content">
        <view class="device-basic">
          <text class="device-name">{{ device?.deviceName }}</text>
          <text class="device-code">{{ device?.deviceCode }}</text>
        </view>
        <view :class="['status-badge', getStatusClass(device?.status)]">
          {{ getStatusText(device?.status) }}
        </view>
      </view>
    </view>

    <!-- 设备信息卡片 -->
    <view class="info-section">
      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <uni-icons type="info" size="20" color="#1890ff" />
          <text class="card-title">基本信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">设备类型</text>
            <text class="info-value">{{ device?.deviceTypeName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">设备品牌</text>
            <text class="info-value">{{ device?.deviceBrand || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">设备型号</text>
            <text class="info-value">{{ device?.deviceModel || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">设备序列号</text>
            <text class="info-value">{{ device?.deviceSerial || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">所属区域</text>
            <text class="info-value">{{ device?.areaName || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">位置描述</text>
            <text class="info-value">{{ device?.locationDesc || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 网络信息 -->
      <view class="info-card">
        <view class="card-header">
          <uni-icons type="wifi" size="20" color="#1890ff" />
          <text class="card-title">网络信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">IP地址</text>
            <text class="info-value">{{ device?.ipAddress || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">端口号</text>
            <text class="info-value">{{ device?.port || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">MAC地址</text>
            <text class="info-value">{{ device?.macAddress || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">通信协议</text>
            <text class="info-value">{{ device?.protocolType || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">是否启用</text>
            <view class="info-value">
              <view :class="['enable-tag', device?.isEnabled === 1 ? 'enabled' : 'disabled']">
                {{ device?.isEnabled === 1 ? '启用' : '禁用' }}
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 联系信息 -->
      <view class="info-card">
        <view class="card-header">
          <uni-icons type="contact" size="20" color="#1890ff" />
          <text class="card-title">联系信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">联系人</text>
            <text class="info-value">{{ device?.contactPerson || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">联系电话</text>
            <text class="info-value">{{ device?.contactPhone || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">供应商信息</text>
            <text class="info-value">{{ device?.vendorInfo || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">安装时间</text>
            <text class="info-value">{{ device?.installTime || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">保修到期</text>
            <text class="info-value">{{ device?.warrantyEndTime || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 系统信息 -->
      <view class="info-card">
        <view class="card-header">
          <uni-icons type="gear" size="20" color="#1890ff" />
          <text class="card-title">系统信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">创建时间</text>
            <text class="info-value">{{ device?.createTime }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">更新时间</text>
            <text class="info-value">{{ device?.updateTime }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">创建人</text>
            <text class="info-value">{{ device?.createUserName || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">版本号</text>
            <text class="info-value">{{ device?.version || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 设备配置 -->
      <view class="info-card" v-if="device?.configJson">
        <view class="card-header">
          <uni-icons type="settings" size="20" color="#1890ff" />
          <text class="card-title">设备配置</text>
        </view>
        <view class="config-content">
          <text class="config-text">{{ formatJson(device.configJson) }}</text>
        </view>
      </view>

      <!-- 扩展信息 -->
      <view class="info-card" v-if="device?.extendInfo">
        <view class="card-header">
          <uni-icons type="more" size="20" color="#1890ff" />
          <text class="card-title">扩展信息</text>
        </view>
        <view class="config-content">
          <text class="config-text">{{ formatJson(device.extendInfo) }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-section">
      <view class="action-buttons">
        <button class="action-btn primary" @click="handleEdit">
          <uni-icons type="compose" size="16" />
          编辑设备
        </button>
        <button class="action-btn" @click="showControlActions">
          <uni-icons type="gear" size="16" />
          设备控制
        </button>
        <button class="action-btn danger" @click="handleDelete">
          <uni-icons type="trash" size="16" />
          删除设备
        </button>
      </view>
    </view>

    <!-- 操作弹窗 -->
    <uni-popup ref="actionPopup" type="bottom">
      <view class="action-popup">
        <view class="popup-header">
          <text class="popup-title">设备操作</text>
          <uni-icons type="close" size="20" @click="closeActionPopup" />
        </view>
        <view class="action-list">
          <view
            v-if="device?.status === 0"
            class="action-item"
            @click="handleDeviceOnline"
          >
            <uni-icons type="wifi" size="20" color="#52c41a" />
            <text class="action-text">设备上线</text>
          </view>
          <view
            v-if="device?.status === 1"
            class="action-item"
            @click="handleDeviceOffline"
          >
            <uni-icons type="wifi-off" size="20" color="#fa8c16" />
            <text class="action-text">设备离线</text>
          </view>
          <view
            v-if="device?.isEnabled === 0"
            class="action-item"
            @click="handleDeviceEnable"
          >
            <uni-icons type="checkmarkempty" size="20" color="#52c41a" />
            <text class="action-text">启用设备</text>
          </view>
          <view
            v-if="device?.isEnabled === 1"
            class="action-item"
            @click="handleDeviceDisable"
          >
            <uni-icons type="closeempty" size="20" color="#ff4d4f" />
            <text class="action-text">禁用设备</text>
          </view>
          <view class="action-item" @click="handleRefreshStatus">
            <uni-icons type="refresh" size="20" color="#1890ff" />
            <text class="action-text">刷新状态</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { deviceApi } from '@/api/device/deviceApi'

export default {
  data() {
    return {
      loading: false,
      deviceId: null,
      device: null
    }
  },

  onLoad(options) {
    this.deviceId = options.id
    if (this.deviceId) {
      this.loadDeviceDetail()
    }
  },

  methods: {
    // 加载设备详情
    async loadDeviceDetail() {
      try {
        this.loading = true
        const response = await deviceApi.getDetail(this.deviceId)
        this.device = response.data
      } catch (error) {
        console.error('加载设备详情失败:', error)
        uni.showToast({
          title: '加载设备详情失败',
          icon: 'error'
        })
      } finally {
        this.loading = false
      }
    },

    // 编辑设备
    handleEdit() {
      uni.navigateTo({
        url: `/pages/device/device-form?id=${this.deviceId}`
      })
    },

    // 删除设备
    handleDelete() {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个设备吗？删除后无法恢复。',
        success: async (res) => {
          if (res.confirm) {
            try {
              await deviceApi.delete(this.deviceId)
              uni.showToast({
                title: '删除成功',
                icon: 'success'
              })
              setTimeout(() => {
                uni.navigateBack()
              }, 1500)
            } catch (error) {
              uni.showToast({
                title: '删除失败',
                icon: 'error'
              })
            }
          }
        }
      })
    },

    // 显示控制操作
    showControlActions() {
      this.$refs.actionPopup.open()
    },

    // 关闭操作弹窗
    closeActionPopup() {
      this.$refs.actionPopup.close()
    },

    // 设备上线
    async handleDeviceOnline() {
      try {
        await deviceApi.online(this.deviceId)
        uni.showToast({
          title: '设备上线成功',
          icon: 'success'
        })
        this.closeActionPopup()
        this.loadDeviceDetail()
      } catch (error) {
        uni.showToast({
          title: '设备上线失败',
          icon: 'error'
        })
      }
    },

    // 设备离线
    async handleDeviceOffline() {
      try {
        await deviceApi.offline(this.deviceId)
        uni.showToast({
          title: '设备离线成功',
          icon: 'success'
        })
        this.closeActionPopup()
        this.loadDeviceDetail()
      } catch (error) {
        uni.showToast({
          title: '设备离线失败',
          icon: 'error'
        })
      }
    },

    // 启用设备
    async handleDeviceEnable() {
      try {
        await deviceApi.enable(this.deviceId)
        uni.showToast({
          title: '启用成功',
          icon: 'success'
        })
        this.closeActionPopup()
        this.loadDeviceDetail()
      } catch (error) {
        uni.showToast({
          title: '启用失败',
          icon: 'error'
        })
      }
    },

    // 禁用设备
    async handleDeviceDisable() {
      try {
        await deviceApi.disable(this.deviceId)
        uni.showToast({
          title: '禁用成功',
          icon: 'success'
        })
        this.closeActionPopup()
        this.loadDeviceDetail()
      } catch (error) {
        uni.showToast({
          title: '禁用失败',
          icon: 'error'
        })
      }
    },

    // 刷新状态
    async handleRefreshStatus() {
      try {
        await deviceApi.refreshStatus()
        uni.showToast({
          title: '状态刷新成功',
          icon: 'success'
        })
        this.closeActionPopup()
        this.loadDeviceDetail()
      } catch (error) {
        uni.showToast({
          title: '状态刷新失败',
          icon: 'error'
        })
      }
    },

    // 获取状态样式
    getStatusClass(status) {
      const classMap = {
        1: 'online',
        0: 'offline',
        2: 'fault',
        3: 'maintenance'
      }
      return classMap[status] || 'unknown'
    },

    // 获取状态文本
    getStatusText(status) {
      const textMap = {
        1: '在线',
        0: '离线',
        2: '故障',
        3: '维护中'
      }
      return textMap[status] || '未知'
    },

    // 格式化JSON
    formatJson(data) {
      try {
        return JSON.stringify(data, null, 2)
      } catch (error) {
        return '-'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.device-detail {
  min-height: 100vh;
  background-color: #f5f5f5;

  .device-header {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
    padding: 60rpx 40rpx 40rpx;
    color: #fff;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;

      .device-basic {
        flex: 1;

        .device-name {
          display: block;
          font-size: 36rpx;
          font-weight: bold;
          margin-bottom: 12rpx;
        }

        .device-code {
          font-size: 28rpx;
          opacity: 0.8;
        }
      }

      .status-badge {
        padding: 12rpx 24rpx;
        border-radius: 20rpx;
        font-size: 24rpx;
        font-weight: bold;

        &.online {
          background: rgba(82, 196, 26, 0.2);
          border: 1px solid rgba(82, 196, 26, 0.3);
        }

        &.offline {
          background: rgba(255, 77, 79, 0.2);
          border: 1px solid rgba(255, 77, 79, 0.3);
        }

        &.fault {
          background: rgba(250, 140, 22, 0.2);
          border: 1px solid rgba(250, 140, 22, 0.3);
        }

        &.maintenance {
          background: rgba(24, 144, 255, 0.2);
          border: 1px solid rgba(24, 144, 255, 0.3);
        }
      }
    }
  }

  .info-section {
    padding: 20rpx;
    margin-bottom: 120rpx;

    .info-card {
      background: #fff;
      border-radius: 16rpx;
      margin-bottom: 20rpx;
      overflow: hidden;

      .card-header {
        display: flex;
        align-items: center;
        padding: 30rpx;
        background: #fafafa;
        border-bottom: 1px solid #f0f0f0;

        .card-title {
          margin-left: 16rpx;
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
        }
      }

      .info-list {
        padding: 30rpx;

        .info-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 20rpx 0;
          border-bottom: 1px solid #f5f5f5;

          &:last-child {
            border-bottom: none;
          }

          .info-label {
            font-size: 28rpx;
            color: #666;
            min-width: 140rpx;
          }

          .info-value {
            flex: 1;
            text-align: right;
            font-size: 28rpx;
            color: #333;

            .enable-tag {
              padding: 8rpx 16rpx;
              border-radius: 8rpx;
              font-size: 24rpx;

              &.enabled {
                background: #f6ffed;
                color: #52c41a;
                border: 1px solid #b7eb8f;
              }

              &.disabled {
                background: #fff2f0;
                color: #ff4d4f;
                border: 1px solid #ffccc7;
              }
            }
          }
        }
      }

      .config-content {
        padding: 30rpx;

        .config-text {
          font-family: 'Courier New', monospace;
          font-size: 24rpx;
          line-height: 1.6;
          color: #333;
          background: #f8f8f8;
          padding: 20rpx;
          border-radius: 8rpx;
          white-space: pre-wrap;
          word-break: break-all;
        }
      }
    }
  }

  .action-section {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 20rpx;
    border-top: 1px solid #f0f0f0;
    box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.1);

    .action-buttons {
      display: flex;
      gap: 20rpx;

      .action-btn {
        flex: 1;
        padding: 24rpx;
        border-radius: 12rpx;
        font-size: 28rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12rpx;

        &.primary {
          background: #1890ff;
          color: #fff;
          border: none;
        }

        &.danger {
          background: #ff4d4f;
          color: #fff;
          border: none;
        }

        &:not(.primary):not(.danger) {
          background: #fff;
          color: #333;
          border: 1px solid #d9d9d9;
        }
      }
    }
  }
}

.action-popup {
  background: #fff;
  border-radius: 20rpx 20rpx 0 0;
  max-height: 80vh;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .action-list {
    padding: 30rpx;

    .action-item {
      display: flex;
      align-items: center;
      padding: 30rpx 0;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .action-text {
        margin-left: 24rpx;
        font-size: 30rpx;
        color: #333;
      }
    }
  }
}
</style>