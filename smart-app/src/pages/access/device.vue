<template>
  <view class="access-device-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">设备管理</view>
      <view class="nav-right"></view>
    </view>

    <!-- 设备列表 -->
    <view class="device-list">
      <view
        class="device-item"
        v-for="(device, index) in deviceList"
        :key="index"
        @click="viewDeviceDetail(device)"
      >
        <view class="device-info">
          <view class="device-name">{{ device.deviceName }}</view>
          <view class="device-location">{{ device.areaName }}</view>
        </view>
        <view class="device-status">
          <text :class="['status-dot', device.online ? 'online' : 'offline']"></text>
          <text :class="['status-text', device.online ? 'online' : 'offline']">
            {{ device.online ? '在线' : '离线' }}
          </text>
        </view>
      </view>

      <view class="no-data" v-if="deviceList.length === 0 && !loading">
        <text>暂无设备</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import accessApi from '@/api/access.js'

// 响应式数据
const deviceList = ref([])
const loading = ref(false)

// 页面生命周期
onMounted(() => {
  loadDevices()
})

onShow(() => {
  // 页面显示时可以刷新设备列表
})

onPullDownRefresh(() => {
  loadDevices()
  uni.stopPullDownRefresh()
})

// 方法实现
const loadDevices = async () => {
  loading.value = true
  try {
    const result = await accessApi.getDeviceInfo()
    if (result.success && result.data) {
      deviceList.value = result.data
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDeviceDetail = (device) => {
  uni.navigateTo({ url: `/pages/device/device-detail?id=${device.deviceId}` })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.access-device-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
  }

  .icon-back {
    font-size: 20px;
    color: #333;
  }
}

.device-list {
  padding: 15px;

  .device-item {
    background-color: #fff;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .device-info {
      flex: 1;

      .device-name {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6px;
      }

      .device-location {
        font-size: 12px;
        color: #999;
      }
    }

    .device-status {
      display: flex;
      align-items: center;

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-right: 6px;

        &.online { background-color: #52c41a; }
        &.offline { background-color: #ff4d4f; }
      }

      .status-text {
        font-size: 13px;

        &.online { color: #52c41a; }
        &.offline { color: #ff4d4f; }
      }
    }
  }

  .no-data {
    text-align: center;
    padding: 40px 0;
    color: #999;
    font-size: 14px;
  }
}
</style>

