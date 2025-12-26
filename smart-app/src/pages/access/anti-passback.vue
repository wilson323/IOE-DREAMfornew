<template>
  <view class="anti-passback-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">反潜回配置</text>
        </view>
        <view class="navbar-right">
          <view class="save-btn" @tap="saveConfig">
            <text>保存</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 功能总开关 -->
      <view class="switch-section">
        <view class="switch-card">
          <view class="switch-info">
            <view class="switch-icon">
              <uni-icons type="locked" size="28" color="#fff"></uni-icons>
            </view>
            <view class="switch-details">
              <text class="switch-title">启用反潜回功能</text>
              <text class="switch-desc">防止同一卡片在短时间内多次通行</text>
            </view>
          </view>
          <switch
            :checked="config.enabled"
            color="#667eea"
            @change="onEnabledChange"
          ></switch>
        </view>
      </view>

      <!-- 配置内容 -->
      <view v-if="config.enabled" class="config-content">
        <!-- 反潜回模式 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">反潜回模式</text>
            <text class="section-desc">选择反潜回检测模式</text>
          </view>
          <view class="mode-grid">
            <view
              class="mode-item"
              :class="{ active: config.mode === 'global' }"
              @tap="selectMode('global')"
            >
              <view class="mode-icon global">
                <uni-icons type="world" size="24" color="#fff"></uni-icons>
              </view>
              <text class="mode-name">全局反潜回</text>
              <text class="mode-desc">所有设备通用检测</text>
            </view>
            <view
              class="mode-item"
              :class="{ active: config.mode === 'area' }"
              @tap="selectMode('area')"
            >
              <view class="mode-icon area">
                <uni-icons type="location" size="24" color="#fff"></uni-icons>
              </view>
              <text class="mode-name">区域反潜回</text>
              <text class="mode-desc">按区域独立检测</text>
            </view>
            <view
              class="mode-item"
              :class="{ active: config.mode === 'soft' }"
              @tap="selectMode('soft')"
            >
              <view class="mode-icon soft">
                <uni-icons type="refresh" size="24" color="#fff"></uni-icons>
              </view>
              <text class="mode-name">软反潜回</text>
              <text class="mode-desc">仅提醒不阻止</text>
            </view>
            <view
              class="mode-item"
              :class="{ active: config.mode === 'hard' }"
              @tap="selectMode('hard')"
            >
              <view class="mode-icon hard">
                <uni-icons type="close" size="24" color="#fff"></uni-icons>
              </view>
              <text class="mode-name">硬反潜回</text>
              <text class="mode-desc">强制阻止通行</text>
            </view>
          </view>
        </view>

        <!-- 时间间隔设置 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">时间间隔设置</text>
            <text class="section-desc">设置两次通行的最小时间间隔</text>
          </view>
          <view class="time-config">
            <view class="time-item">
              <text class="time-label">最小间隔时间</text>
              <view class="time-input-wrapper">
                <input
                  class="time-input"
                  type="number"
                  v-model="config.minInterval"
                  placeholder="请输入"
                />
                <text class="time-unit">秒</text>
              </view>
            </view>
            <view class="time-item">
              <text class="time-label">最大间隔时间</text>
              <view class="time-input-wrapper">
                <input
                  class="time-input"
                  type="number"
                  v-model="config.maxInterval"
                  placeholder="请输入"
                />
                <text class="time-unit">秒</text>
              </view>
            </view>
          </view>
          <view class="time-tip">
            <uni-icons type="info" size="14" color="#999"></uni-icons>
            <text class="tip-text">系统将在最小间隔和最大间隔之间进行反潜回检测</text>
          </view>
        </view>

        <!-- 特殊人员白名单 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">白名单人员</text>
            <text class="section-desc">这些人员不受反潜回限制</text>
          </view>
          <view class="whitelist-list">
            <view
              class="whitelist-item"
              v-for="(item, index) in whitelist"
              :key="index"
            >
              <image
                class="whitelist-avatar"
                :src="item.avatar || '/static/default-avatar.png'"
                mode="aspectFill"
              ></image>
              <view class="whitelist-info">
                <text class="whitelist-name">{{ item.name }}</text>
                <text class="whitelist-desc">{{ item.department }}</text>
              </view>
              <view class="whitelist-remove" @tap="removeFromWhitelist(index)">
                <uni-icons type="close" size="16" color="#ff4d4f"></uni-icons>
              </view>
            </view>
            <view class="add-whitelist-btn" @tap="addToWhitelist">
              <uni-icons type="plus" size="18" color="#667eea"></uni-icons>
              <text class="add-text">添加白名单人员</text>
            </view>
          </view>
        </view>

        <!-- 区域反潜回规则 -->
        <view class="config-section" v-if="config.mode === 'area'">
          <view class="section-header">
            <text class="section-title">区域反潜回规则</text>
            <text class="section-desc">为不同区域设置独立的反潜回规则</text>
          </view>
          <view class="area-rules-list">
            <view
              class="area-rule-item"
              v-for="(item, index) in areaRules"
              :key="index"
              @tap="editAreaRule(item)"
            >
              <view class="area-rule-header">
                <view class="area-icon">
                  <uni-icons type="location" size="16" color="#fff"></uni-icons>
                </view>
                <text class="area-name">{{ item.areaName }}</text>
                <view class="rule-status" :class="{ active: item.enabled }">
                  <text>{{ item.enabled ? '已启用' : '已禁用' }}</text>
                </view>
              </view>
              <view class="area-rule-detail">
                <text class="rule-text">间隔：{{ item.minInterval }}-{{ item.maxInterval }}秒</text>
                <text class="rule-text">设备：{{ item.deviceCount }}台</text>
              </view>
            </view>
          </view>
          <view class="add-area-rule-btn" @tap="addAreaRule">
            <uni-icons type="plus" size="18" color="#667eea"></uni-icons>
            <text class="add-text">添加区域规则</text>
          </view>
        </view>

        <!-- 设备反潜回配置 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">设备反潜回配置</text>
            <text class="section-desc">查看和管理各设备的反潜回状态</text>
          </view>
          <view class="device-config-list">
            <view
              class="device-config-item"
              v-for="(item, index) in deviceConfigs"
              :key="index"
              @tap="viewDeviceConfig(item)"
            >
              <view class="device-info">
                <view class="device-icon" :class="{ online: item.online }">
                  <uni-icons type="locked" size="16" color="#fff"></uni-icons>
                </view>
                <view class="device-details">
                  <text class="device-name">{{ item.deviceName }}</text>
                  <text class="device-location">{{ item.location }}</text>
                </view>
              </view>
              <view class="device-status">
                <view class="status-dot" :class="{ active: item.antiPassbackEnabled }"></view>
                <text class="status-text">{{ item.antiPassbackEnabled ? '已启用' : '已禁用' }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 反潜回日志 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">最近检测记录</text>
            <text class="section-more" @tap="viewAllLogs">查看全部</text>
          </view>
          <view class="logs-list">
            <view
              class="log-item"
              v-for="(item, index) in recentLogs"
              :key="index"
            >
              <view class="log-icon" :class="item.result">
                <uni-icons
                  :type="item.result === 'blocked' ? 'close' : 'checkmark'"
                  size="16"
                  color="#fff"
                ></uni-icons>
              </view>
              <view class="log-info">
                <text class="log-title">{{ item.userName }}</text>
                <text class="log-desc">{{ item.deviceName }} · {{ item.time }}</text>
              </view>
              <view class="log-result" :class="item.result">
                <text>{{ item.result === 'blocked' ? '已阻止' : '已通过' }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 未启用提示 -->
      <view v-else class="disabled-tip">
        <view class="tip-icon">
          <uni-icons type="info" size="48" color="#ccc"></uni-icons>
        </view>
        <text class="tip-title">反潜回功能未启用</text>
        <text class="tip-desc">启用后可配置反潜回检测规则</text>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 配置数据
const config = reactive({
  enabled: true,
  mode: 'global', // global, area, soft, hard
  minInterval: 10,
  maxInterval: 300
})

// 白名单
const whitelist = ref([
  {
    userId: 1,
    name: '张三',
    department: '行政部',
    avatar: '/static/avatar1.png'
  },
  {
    userId: 2,
    name: '李四',
    department: '人事部',
    avatar: '/static/avatar2.png'
  },
  {
    userId: 3,
    name: '王五',
    department: '财务部',
    avatar: '/static/avatar3.png'
  }
])

// 区域反潜回规则
const areaRules = ref([
  {
    areaId: 1,
    areaName: 'A栋办公区',
    minInterval: 15,
    maxInterval: 300,
    deviceCount: 8,
    enabled: true
  },
  {
    areaId: 2,
    areaName: 'B栋生产区',
    minInterval: 10,
    maxInterval: 240,
    deviceCount: 12,
    enabled: true
  },
  {
    areaId: 3,
    areaName: 'C栋仓储区',
    minInterval: 20,
    maxInterval: 360,
    deviceCount: 6,
    enabled: false
  }
])

// 设备反潜回配置
const deviceConfigs = ref([
  {
    deviceId: 'DEV001',
    deviceName: '主入口门禁',
    location: 'A栋1楼大厅',
    online: true,
    antiPassbackEnabled: true
  },
  {
    deviceId: 'DEV002',
    deviceName: '侧门门禁',
    location: 'A栋2楼楼梯间',
    online: true,
    antiPassbackEnabled: true
  },
  {
    deviceId: 'DEV003',
    deviceName: '后门门禁',
    location: 'A栋1楼后门',
    online: false,
    antiPassbackEnabled: false
  },
  {
    deviceId: 'DEV004',
    deviceName: '办公区门禁',
    location: 'A栋3楼办公区',
    online: true,
    antiPassbackEnabled: true
  }
])

// 最近检测记录
const recentLogs = ref([
  {
    userId: 1,
    userName: '张三',
    deviceId: 'DEV001',
    deviceName: '主入口门禁',
    result: 'blocked',
    time: '2分钟前'
  },
  {
    userId: 2,
    userName: '李四',
    deviceId: 'DEV002',
    deviceName: '侧门门禁',
    result: 'passed',
    time: '5分钟前'
  },
  {
    userId: 3,
    userName: '王五',
    deviceId: 'DEV004',
    deviceName: '办公区门禁',
    result: 'blocked',
    time: '8分钟前'
  },
  {
    userId: 4,
    userName: '赵六',
    deviceId: 'DEV001',
    deviceName: '主入口门禁',
    result: 'passed',
    time: '12分钟前'
  },
  {
    userId: 5,
    userName: '孙七',
    deviceId: 'DEV003',
    deviceName: '后门门禁',
    result: 'passed',
    time: '15分钟前'
  }
])

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 启用状态变更
const onEnabledChange = (e) => {
  config.enabled = e.detail.value
}

// 选择反潜回模式
const selectMode = (mode) => {
  config.mode = mode
}

// 从白名单移除
const removeFromWhitelist = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要从白名单中移除该人员吗？',
    success: (res) => {
      if (res.confirm) {
        whitelist.value.splice(index, 1)
        uni.showToast({
          title: '移除成功',
          icon: 'success'
        })
      }
    }
  })
}

// 添加到白名单
const addToWhitelist = () => {
  uni.navigateTo({
    url: '/pages/access/whitelist-select'
  })
}

// 编辑区域规则
const editAreaRule = (rule) => {
  uni.navigateTo({
    url: `/pages/access/area-rule-edit?areaId=${rule.areaId}`
  })
}

// 添加区域规则
const addAreaRule = () => {
  uni.navigateTo({
    url: '/pages/access/area-rule-add'
  })
}

// 查看设备配置
const viewDeviceConfig = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-anti-passback-config?deviceId=${device.deviceId}`
  })
}

// 查看全部日志
const viewAllLogs = () => {
  uni.navigateTo({
    url: '/pages/access/anti-passback-logs'
  })
}

// 保存配置
const saveConfig = () => {
  uni.showLoading({ title: '保存中...' })

  // TODO: 调用实际API保存配置
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })
  }, 1000)
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.anti-passback-container {
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
    justify-content: space-between;
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

    .navbar-right {
      .save-btn {
        padding: 8rpx 24rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20rpx;
        font-size: 13px;
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

// 总开关
.switch-section {
  padding: 30rpx;

  .switch-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .switch-info {
      display: flex;
      align-items: center;
      flex: 1;

      .switch-icon {
        width: 64rpx;
        height: 64rpx;
        border-radius: 16rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .switch-details {
        display: flex;
        flex-direction: column;

        .switch-title {
          font-size: 16px;
          font-weight: 500;
          color: #333;
          margin-bottom: 6rpx;
        }

        .switch-desc {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

// 配置内容
.config-content {
  padding-bottom: 30rpx;
}

.config-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      display: block;
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin-bottom: 6rpx;
    }

    .section-desc {
      font-size: 12px;
      color: #999;
    }

    .section-more {
      float: right;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 模式网格
.mode-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .mode-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30rpx 20rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      border-color: #667eea;
    }

    .mode-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 20rpx;

      &.global {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.area {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.soft {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.hard {
        background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
      }
    }

    .mode-name {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 8rpx;
    }

    .mode-desc {
      font-size: 11px;
      color: #999;
      text-align: center;
    }
  }
}

// 时间配置
.time-config {
  .time-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .time-label {
      font-size: 14px;
      color: #333;
    }

    .time-input-wrapper {
      display: flex;
      align-items: center;

      .time-input {
        width: 150rpx;
        height: 60rpx;
        padding: 0 20rpx;
        background: #f5f5f5;
        border-radius: 8rpx;
        font-size: 14px;
        color: #333;
        text-align: center;
      }

      .time-unit {
        margin-left: 20rpx;
        font-size: 13px;
        color: #999;
      }
    }
  }
}

.time-tip {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fff7e6;
  border-radius: 12rpx;
  margin-top: 20rpx;

  .tip-text {
    margin-left: 10rpx;
    font-size: 12px;
    color: #faad14;
  }
}

// 白名单列表
.whitelist-list {
  .whitelist-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .whitelist-avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: #f5f5f5;
      margin-right: 20rpx;
    }

    .whitelist-info {
      flex: 1;

      .whitelist-name {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .whitelist-desc {
        font-size: 12px;
        color: #999;
      }
    }

    .whitelist-remove {
      padding: 10rpx;
    }
  }

  .add-whitelist-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;
    border: 1rpx dashed #667eea;
    border-radius: 12rpx;
    margin-top: 20rpx;

    .add-text {
      margin-left: 10rpx;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 区域规则列表
.area-rules-list {
  .area-rule-item {
    padding: 30rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .area-rule-header {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;

      .area-icon {
        width: 40rpx;
        height: 40rpx;
        border-radius: 10rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .area-name {
        flex: 1;
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .rule-status {
        padding: 4rpx 16rpx;
        background: #f0f0f0;
        border-radius: 12rpx;
        font-size: 10px;
        color: #999;

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
        }
      }
    }

    .area-rule-detail {
      display: flex;
      flex-direction: column;

      .rule-text {
        font-size: 12px;
        color: #999;
        margin-bottom: 6rpx;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .add-area-rule-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;
    border: 1rpx dashed #667eea;
    border-radius: 12rpx;
    margin-top: 20rpx;

    .add-text {
      margin-left: 10rpx;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 设备配置列表
.device-config-list {
  .device-config-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .device-info {
      display: flex;
      align-items: center;
      flex: 1;

      .device-icon {
        width: 48rpx;
        height: 48rpx;
        border-radius: 12rpx;
        background: #e0e0e0;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;

        &.online {
          background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        }
      }

      .device-details {
        display: flex;
        flex-direction: column;

        .device-name {
          font-size: 14px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .device-location {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .device-status {
      display: flex;
      align-items: center;

      .status-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 6rpx;
        background: #e0e0e0;
        margin-right: 10rpx;

        &.active {
          background: #52c41a;
        }
      }

      .status-text {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

// 检测记录
.logs-list {
  .log-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .log-icon {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;

      &.blocked {
        background: #ff4d4f;
      }

      &.passed {
        background: #52c41a;
      }
    }

    .log-info {
      flex: 1;

      .log-title {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .log-desc {
        font-size: 12px;
        color: #999;
      }
    }

    .log-result {
      padding: 4rpx 16rpx;
      border-radius: 12rpx;
      font-size: 11px;

      &.blocked {
        background: #fff1f0;
        color: #ff4d4f;
      }

      &.passed {
        background: #f6ffed;
        color: #52c41a;
      }
    }
  }
}

// 未启用提示
.disabled-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;

  .tip-icon {
    margin-bottom: 30rpx;
  }

  .tip-title {
    font-size: 16px;
    color: #999;
    margin-bottom: 10rpx;
  }

  .tip-desc {
    font-size: 13px;
    color: #ccc;
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
