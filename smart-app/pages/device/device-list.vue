<template>
  <view class="device-list">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <uni-search-bar
        v-model="searchForm.deviceName"
        placeholder="搜索设备名称"
        @confirm="handleSearch"
        @clear="handleReset"
        :focus="false"
        :show-action="false"
      />
    </view>

    <!-- 筛选条件 -->
    <view class="filter-bar">
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-item">
          <uni-data-select
            v-model="searchForm.deviceType"
            :localdata="deviceTypeOptions"
            placeholder="设备类型"
            @change="handleSearch"
          />
        </view>
        <view class="filter-item">
          <uni-data-select
            v-model="searchForm.status"
            :localdata="statusOptions"
            placeholder="设备状态"
            @change="handleSearch"
          />
        </view>
      </scroll-view>
    </view>

    <!-- 统计信息 -->
    <view class="statistics-section">
      <view class="stat-card">
        <view class="stat-number">{{ statistics.totalDevices }}</view>
        <view class="stat-label">设备总数</view>
      </view>
      <view class="stat-card">
        <view class="stat-number online">{{ statistics.onlineDevices }}</view>
        <view class="stat-label">在线</view>
      </view>
      <view class="stat-card">
        <view class="stat-number offline">{{ statistics.offlineDevices }}</view>
        <view class="stat-label">离线</view>
      </view>
      <view class="stat-card">
        <view class="stat-number fault">{{ statistics.faultDevices }}</view>
        <view class="stat-label">故障</view>
      </view>
    </view>

    <!-- 设备列表 -->
    <view class="device-section">
      <view class="section-header">
        <text class="section-title">设备列表</text>
        <text class="section-count">共 {{ tableData.length }} 个设备</text>
      </view>

      <scroll-view
        scroll-y
        class="device-scroll"
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view
          v-for="device in tableData"
          :key="device.deviceId"
          class="device-card"
          @click="handleViewDevice(device.deviceId)"
        >
          <view class="device-header">
            <view class="device-info">
              <text class="device-name">{{ device.deviceName }}</text>
              <text class="device-code">{{ device.deviceCode }}</text>
            </view>
            <view class="device-status">
              <view :class="['status-tag', getStatusClass(device.status)]">
                {{ getStatusText(device.status) }}
              </view>
            </view>
          </view>

          <view class="device-details">
            <view class="detail-row">
              <text class="detail-label">类型：</text>
              <text class="detail-value">{{ device.deviceTypeName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">位置：</text>
              <text class="detail-value">{{ device.areaName || '-' }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">IP：</text>
              <text class="detail-value">{{ device.ipAddress || '-' }}</text>
            </view>
            <view class="detail-row" v-if="device.contactPerson">
              <text class="detail-label">联系人：</text>
              <text class="detail-value">{{ device.contactPerson }}</text>
            </view>
          </view>

          <view class="device-actions">
            <button
              class="action-btn primary"
              size="mini"
              @click.stop="handleViewDevice(device.deviceId)"
            >
              查看
            </button>
            <button
              class="action-btn"
              size="mini"
              @click.stop="handleControl(device)"
            >
              控制
            </button>
          </view>
        </view>

        <!-- 加载状态 -->
        <view class="load-more" v-if="loading">
          <uni-load-more :status="loadStatus" />
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="!loading && tableData.length === 0">
          <uni-icons type="equipment" size="64" color="#ccc" />
          <text class="empty-text">暂无设备数据</text>
        </view>
      </scroll-view>
    </view>

    <!-- 悬浮按钮 -->
    <view class="fab" @click="handleAddDevice">
      <uni-icons type="plus" size="24" color="#fff" />
    </view>

    <!-- 设备控制弹窗 -->
    <uni-popup ref="controlPopup" type="bottom">
      <view class="control-popup">
        <view class="popup-header">
          <text class="popup-title">设备控制</text>
          <uni-icons type="close" size="20" @click="closeControlPopup" />
        </view>
        <view class="control-content">
          <view class="control-item" v-if="currentDevice?.status === 0">
            <button class="control-btn success" @click="handleDeviceOnline">
              <uni-icons type="wifi" size="16" />
              设备上线
            </button>
          </view>
          <view class="control-item" v-if="currentDevice?.status === 1">
            <button class="control-btn warning" @click="handleDeviceOffline">
              <uni-icons type="minus" size="16" />
              设备离线
            </button>
          </view>
          <view class="control-item" v-if="currentDevice?.isEnabled === 0">
            <button class="control-btn success" @click="handleDeviceEnable">
              <uni-icons type="checkmarkempty" size="16" />
              启用设备
            </button>
          </view>
          <view class="control-item" v-if="currentDevice?.isEnabled === 1">
            <button class="control-btn danger" @click="handleDeviceDisable">
              <uni-icons type="closeempty" size="16" />
              禁用设备
            </button>
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
      refreshing: false,
      loadStatus: 'more',

      // 搜索表单
      searchForm: {
        deviceName: '',
        deviceType: '',
        status: '',
        current: 1,
        pageSize: 20
      },

      // 设备数据
      tableData: [],
      statistics: {
        totalDevices: 0,
        onlineDevices: 0,
        offlineDevices: 0,
        faultDevices: 0
      },

      // 筛选选项
      deviceTypeOptions: [
        { value: 'CAMERA', text: '摄像头' },
        { value: 'ACCESS_CONTROLLER', text: '门禁控制器' },
        { value: 'CONSUMPTION_TERMINAL', text: '消费终端' },
        { value: 'ATTENDANCE_MACHINE', text: '考勤机' },
        { value: 'ALARM_DEVICE', text: '报警器' },
        { value: 'INTERCOM', text: '对讲机' },
        { value: 'LED_SCREEN', text: 'LED屏' },
        { value: 'DOOR_MAGNET', text: '门磁' }
      ],

      statusOptions: [
        { value: 1, text: '在线' },
        { value: 0, text: '离线' },
        { value: 2, text: '故障' },
        { value: 3, text: '维护中' }
      ],

      // 当前控制的设备
      currentDevice: null,
      hasMore: true
    }
  },

  onLoad() {
    this.loadData()
    this.loadStatistics()
  },

  methods: {
    // 加载数据
    async loadData() {
      if (this.loading) return

      try {
        this.loading = true
        const response = await deviceApi.queryPage(this.searchForm)

        if (this.searchForm.current === 1) {
          this.tableData = response.data.records
        } else {
          this.tableData.push(...response.data.records)
        }

        this.hasMore = this.tableData.length < response.data.total
        this.loadStatus = this.hasMore ? 'more' : 'noMore'
      } catch (error) {
        console.error('加载设备数据失败:', error)
        uni.showToast({
          title: '加载数据失败',
          icon: 'error'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    // 加载统计数据
    async loadStatistics() {
      try {
        const response = await deviceApi.getStatistics()
        this.statistics = response.data
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },

    // 搜索
    handleSearch() {
      this.searchForm.current = 1
      this.loadData()
    },

    // 重置
    handleReset() {
      this.searchForm = {
        deviceName: '',
        deviceType: '',
        status: '',
        current: 1,
        pageSize: 20
      }
      this.loadData()
    },

    // 刷新
    onRefresh() {
      this.refreshing = true
      this.searchForm.current = 1
      this.loadData()
      this.loadStatistics()
    },

    // 加载更多
    loadMore() {
      if (!this.hasMore || this.loading) return

      this.searchForm.current++
      this.loadData()
    },

    // 查看设备详情
    handleViewDevice(deviceId) {
      uni.navigateTo({
        url: `/pages/device/device-detail?id=${deviceId}`
      })
    },

    // 新增设备
    handleAddDevice() {
      uni.navigateTo({
        url: '/pages/device/device-form'
      })
    },

    // 控制设备
    handleControl(device) {
      this.currentDevice = device
      this.$refs.controlPopup.open()
    },

    // 关闭控制弹窗
    closeControlPopup() {
      this.$refs.controlPopup.close()
      this.currentDevice = null
    },

    // 设备上线
    async handleDeviceOnline() {
      try {
        await deviceApi.online(this.currentDevice.deviceId)
        uni.showToast({
          title: '设备上线成功',
          icon: 'success'
        })
        this.closeControlPopup()
        this.loadData()
        this.loadStatistics()
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
        await deviceApi.offline(this.currentDevice.deviceId)
        uni.showToast({
          title: '设备离线成功',
          icon: 'success'
        })
        this.closeControlPopup()
        this.loadData()
        this.loadStatistics()
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
        await deviceApi.enable(this.currentDevice.deviceId)
        uni.showToast({
          title: '启用成功',
          icon: 'success'
        })
        this.closeControlPopup()
        this.loadData()
        this.loadStatistics()
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
        await deviceApi.disable(this.currentDevice.deviceId)
        uni.showToast({
          title: '禁用成功',
          icon: 'success'
        })
        this.closeControlPopup()
        this.loadData()
        this.loadStatistics()
      } catch (error) {
        uni.showToast({
          title: '禁用失败',
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
    }
  }
}
</script>

<style lang="scss" scoped>
.device-list {
  min-height: 100vh;
  background-color: #f5f5f5;

  .search-bar {
    background: #fff;
    padding: 15rpx;
    border-bottom: 1px solid #f0f0f0;
  }

  .filter-bar {
    background: #fff;
    padding: 15rpx;
    border-bottom: 1px solid #f0f0f0;

    .filter-scroll {
      white-space: nowrap;

      .filter-item {
        display: inline-block;
        margin-right: 20rpx;
        min-width: 180rpx;
      }
    }
  }

  .statistics-section {
    display: flex;
    padding: 20rpx;
    background: #fff;
    margin-bottom: 20rpx;

    .stat-card {
      flex: 1;
      text-align: center;

      .stat-number {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;

        &.online {
          color: #52c41a;
        }

        &.offline {
          color: #ff4d4f;
        }

        &.fault {
          color: #fa8c16;
        }
      }

      .stat-label {
        font-size: 24rpx;
        color: #666;
        margin-top: 8rpx;
      }
    }
  }

  .device-section {
    flex: 1;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx;
      background: #fff;

      .section-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }

      .section-count {
        font-size: 28rpx;
        color: #666;
      }
    }

    .device-scroll {
      height: calc(100vh - 400rpx);
    }

    .device-card {
      margin: 20rpx;
      padding: 30rpx;
      background: #fff;
      border-radius: 12rpx;
      box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);

      .device-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 20rpx;

        .device-info {
          flex: 1;

          .device-name {
            display: block;
            font-size: 32rpx;
            font-weight: bold;
            color: #333;
            margin-bottom: 8rpx;
          }

          .device-code {
            font-size: 24rpx;
            color: #666;
          }
        }

        .device-status {
          .status-tag {
            padding: 8rpx 16rpx;
            border-radius: 8rpx;
            font-size: 24rpx;

            &.online {
              background: #f6ffed;
              color: #52c41a;
              border: 1px solid #b7eb8f;
            }

            &.offline {
              background: #fff2f0;
              color: #ff4d4f;
              border: 1px solid #ffccc7;
            }

            &.fault {
              background: #fff7e6;
              color: #fa8c16;
              border: 1px solid #ffd591;
            }

            &.maintenance {
              background: #e6f7ff;
              color: #1890ff;
              border: 1px solid #91d5ff;
            }
          }
        }
      }

      .device-details {
        margin-bottom: 24rpx;

        .detail-row {
          display: flex;
          margin-bottom: 12rpx;

          .detail-label {
            width: 100rpx;
            font-size: 28rpx;
            color: #666;
          }

          .detail-value {
            flex: 1;
            font-size: 28rpx;
            color: #333;
          }
        }
      }

      .device-actions {
        display: flex;
        justify-content: flex-end;
        gap: 20rpx;

        .action-btn {
          padding: 12rpx 24rpx;
          border-radius: 8rpx;
          font-size: 28rpx;
          border: 1px solid #d9d9d9;
          background: #fff;
          color: #333;

          &.primary {
            background: #1890ff;
            color: #fff;
            border-color: #1890ff;
          }
        }
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 100rpx;

      .empty-text {
        margin-top: 20rpx;
        font-size: 28rpx;
        color: #999;
      }
    }
  }

  .fab {
    position: fixed;
    right: 40rpx;
    bottom: 100rpx;
    width: 100rpx;
    height: 100rpx;
    background: #1890ff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(24, 144, 255, 0.4);
    z-index: 1000;
  }
}

.control-popup {
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

  .control-content {
    padding: 30rpx;

    .control-item {
      margin-bottom: 20rpx;

      .control-btn {
        width: 100%;
        padding: 24rpx;
        border-radius: 12rpx;
        font-size: 30rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 16rpx;

        &.success {
          background: #f6ffed;
          color: #52c41a;
          border: 1px solid #b7eb8f;
        }

        &.warning {
          background: #fff7e6;
          color: #fa8c16;
          border: 1px solid #ffd591;
        }

        &.danger {
          background: #fff2f0;
          color: #ff4d4f;
          border: 1px solid #ffccc7;
        }
      }
    }
  }
}
</style>