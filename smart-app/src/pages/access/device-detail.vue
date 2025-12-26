<template>
  <view class="device-detail-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">设备详情</view>
      <view class="nav-right">
        <uni-icons type="more-filled" size="20" color="#666" @click="showActionMenu"></uni-icons>
      </view>
    </view>

    <!-- 设备状态卡片 -->
    <view class="status-card">
      <view class="status-header">
        <view class="device-info-header">
          <text class="device-name">{{ deviceDetail.deviceName }}</text>
          <view :class="['status-badge', deviceDetail.online ? 'online' : 'offline']">
            <text class="status-text">{{ deviceDetail.online ? '在线' : '离线' }}</text>
          </view>
        </view>
      </view>

      <view class="status-stats">
        <view class="stat-item">
          <uni-icons type="location" size="18" color="#1890ff"></uni-icons>
          <text class="stat-label">{{ deviceDetail.areaName || '未分配区域' }}</text>
        </view>
        <view class="stat-item">
          <uni-icons type="settings" size="18" color="#1890ff"></uni-icons>
          <text class="stat-label">{{ getDeviceTypeLabel() }}</text>
        </view>
        <view class="stat-item">
          <uni-icons type="link" size="18" color="#1890ff"></uni-icons>
          <text class="stat-label">{{ getCommTypeLabel() }}</text>
        </view>
      </view>
    </view>

    <!-- 快速操作 -->
    <view class="quick-actions">
      <view class="action-item" @click="remoteOpenDoor">
        <view class="action-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <uni-icons type="locked" size="24" color="#fff"></uni-icons>
        </view>
        <text class="action-text">远程开门</text>
      </view>
      <view class="action-item" @click="rebootDevice">
        <view class="action-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <uni-icons type="refreshempty" size="24" color="#fff"></uni-icons>
        </view>
        <text class="action-text">重启设备</text>
      </view>
      <view class="action-item" @click="syncData">
        <view class="action-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <uni-icons type="cloud-download" size="24" color="#fff"></uni-icons>
        </view>
        <text class="action-text">同步数据</text>
      </view>
      <view class="action-item" @click="gotoAdvancedSettings">
        <view class="action-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
          <uni-icons type="gear" size="24" color="#fff"></uni-icons>
        </view>
        <text class="action-text">高级设置</text>
      </view>
    </view>

    <!-- 详细信息 -->
    <view class="detail-sections">
      <!-- 基本信息 -->
      <view class="detail-section">
        <view class="section-header">
          <text class="section-title">基本信息</text>
        </view>
        <view class="section-body">
          <view class="detail-row">
            <text class="detail-label">设备名称</text>
            <text class="detail-value">{{ deviceDetail.deviceName || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">设备类型</text>
            <text class="detail-value">{{ getDeviceTypeLabel() }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">设备型号</text>
            <text class="detail-value">{{ deviceDetail.deviceModel || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">通讯方式</text>
            <text class="detail-value">{{ getCommTypeLabel() }}</text>
          </view>
          <view class="detail-row" v-if="deviceDetail.commType === 1">
            <text class="detail-label">IP地址</text>
            <text class="detail-value">{{ deviceDetail.ipAddress || '-' }}</text>
          </view>
          <view class="detail-row" v-if="deviceDetail.commType === 1">
            <text class="detail-label">端口号</text>
            <text class="detail-value">{{ deviceDetail.port || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">关联区域</text>
            <text class="detail-value">{{ deviceDetail.areaName || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">位置描述</text>
            <text class="detail-value">{{ deviceDetail.locationDesc || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 门信息 -->
      <view class="detail-section" v-if="doors.length > 0">
        <view class="section-header">
          <text class="section-title">门信息（{{ doors.length }}）</text>
          <text class="section-more" @click="viewAllDoors">查看全部 ></text>
        </view>
        <view class="section-body">
          <view
            class="door-item"
            v-for="door in doors.slice(0, 3)"
            :key="door.doorId"
            @click="viewDoorDetail(door)"
          >
            <view class="door-info">
              <text class="door-name">{{ door.doorName }}</text>
              <text class="door-location">{{ door.location || '未设置位置' }}</text>
            </view>
            <view :class="['door-status', door.open ? 'open' : 'closed']">
              <text class="door-status-text">{{ door.open ? '开启' : '关闭' }}</text>
            </view>
          </view>
          <view class="view-more" v-if="doors.length > 3" @click="viewAllDoors">
            <text class="view-more-text">还有 {{ doors.length - 3 }} 个门</text>
            <uni-icons type="right" size="14" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 读头信息 -->
      <view class="detail-section" v-if="readers.length > 0">
        <view class="section-header">
          <text class="section-title">读头信息（{{ readers.length }}）</text>
          <text class="section-more" @click="viewAllReaders">查看全部 ></text>
        </view>
        <view class="section-body">
          <view
            class="reader-item"
            v-for="reader in readers.slice(0, 3)"
            :key="reader.readerId"
          >
            <view class="reader-info">
              <text class="reader-name">{{ reader.readerName }}</text>
              <text class="reader-type">{{ reader.readerType || '普通读头' }}</text>
            </view>
            <view :class="['reader-status', reader.online ? 'online' : 'offline']">
              <text class="reader-status-text">{{ reader.online ? '在线' : '离线' }}</text>
            </view>
          </view>
          <view class="view-more" v-if="readers.length > 3" @click="viewAllReaders">
            <text class="view-more-text">还有 {{ readers.length - 3 }} 个读头</text>
            <uni-icons type="right" size="14" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 运行统计 -->
      <view class="detail-section">
        <view class="section-header">
          <text class="section-title">运行统计</text>
        </view>
        <view class="section-body">
          <view class="stats-grid">
            <view class="grid-item">
              <text class="grid-value">{{ runtimeStats.totalAccess || 0 }}</text>
              <text class="grid-label">通行次数</text>
            </view>
            <view class="grid-item">
              <text class="grid-value">{{ runtimeStats.todayAccess || 0 }}</text>
              <text class="grid-label">今日通行</text>
            </view>
            <view class="grid-item">
              <text class="grid-value">{{ runtimeStats.errorCount || 0 }}</text>
              <text class="grid-label">异常次数</text>
            </view>
            <view class="grid-item">
              <text class="grid-value">{{ runtimeStats.onlineRate || 0 }}%</text>
              <text class="grid-label">在线率</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <button class="bottom-btn secondary" @click="editDevice">编辑设备</button>
      <button class="bottom-btn danger" @click="deleteDevice">删除设备</button>
    </view>

    <!-- 操作菜单弹窗 -->
    <view class="action-menu" v-if="actionMenuVisible" @click="hideActionMenu">
      <view class="menu-content" @click.stop>
        <view class="menu-item" @click="editDevice">
          <uni-icons type="compose" size="18" color="#666"></uni-icons>
          <text class="menu-text">编辑设备</text>
        </view>
        <view class="menu-item" @click="syncData">
          <uni-icons type="cloud-download" size="18" color="#666"></uni-icons>
          <text class="menu-text">同步数据</text>
        </view>
        <view class="menu-item" @click="rebootDevice">
          <uni-icons type="refreshempty" size="18" color="#666"></uni-icons>
          <text class="menu-text">重启设备</text>
        </view>
        <view class="menu-divider"></view>
        <view class="menu-item danger" @click="deleteDevice">
          <uni-icons type="trash" size="18" color="#ff4d4f"></uni-icons>
          <text class="menu-text danger">删除设备</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 设备详情
const deviceDetail = ref({})
const deviceId = ref(null)

// 门列表
const doors = ref([])

// 读头列表
const readers = ref([])

// 运行统计
const runtimeStats = ref({
  totalAccess: 0,
  todayAccess: 0,
  errorCount: 0,
  onlineRate: 0
})

// 弹窗状态
const actionMenuVisible = ref(false)

// 页面生命周期
onLoad((options) => {
  if (options.id) {
    deviceId.value = parseInt(options.id)
    loadDeviceDetail()
  }
})

/**
 * 加载设备详情
 */
const loadDeviceDetail = async () => {
  uni.showLoading({
    title: '加载中...',
    mask: true
  })

  try {
    const result = await accessApi.getDeviceDetail(deviceId.value)

    uni.hideLoading()

    if (result.success && result.data) {
      deviceDetail.value = result.data.device || {}
      doors.value = result.data.doors || []
      readers.value = result.data.readers || []
      runtimeStats.value = result.data.statistics || runtimeStats.value
    } else {
      uni.showToast({
        title: result.message || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('加载设备详情失败:', error)
    uni.showToast({
      title: '加载失败，请稍后重试',
      icon: 'none'
    })
  }
}

/**
 * 获取设备类型标签
 */
const getDeviceTypeLabel = () => {
  const types = {
    1: '门禁控制器',
    2: '门禁一体机'
  }
  return types[deviceDetail.value.deviceType] || '-'
}

/**
 * 获取通讯方式标签
 */
const getCommTypeLabel = () => {
  const types = {
    1: 'TCP/IP',
    2: 'RS485'
  }
  return types[deviceDetail.value.commType] || '-'
}

/**
 * 远程开门
 */
const remoteOpenDoor = () => {
  uni.navigateTo({
    url: `/pages/access/device-control?id=${deviceId.value}&action=open`
  })
}

/**
 * 重启设备
 */
const rebootDevice = () => {
  uni.showModal({
    title: '确认重启',
    content: '确定要重启此设备吗？重启过程中设备将无法正常工作。',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '重启中...',
          mask: true
        })

        try {
          const result = await accessApi.deviceControl(deviceId.value, 'reboot')

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '重启成功',
              icon: 'success'
            })
            // 刷新设备详情
            setTimeout(() => {
              loadDeviceDetail()
            }, 2000)
          } else {
            uni.showToast({
              title: result.message || '重启失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('重启设备失败:', error)
          uni.showToast({
            title: '重启失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 同步数据
 */
const syncData = () => {
  uni.showLoading({
    title: '同步中...',
    mask: true
  })

  try {
    // 这里需要调用同步数据的API
    // 暂时模拟同步过程
    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({
        title: '同步成功',
        icon: 'success'
      })
      loadDeviceDetail()
    }, 2000)
  } catch (error) {
    uni.hideLoading()
    console.error('同步数据失败:', error)
    uni.showToast({
      title: '同步失败',
      icon: 'none'
    })
  }
}

/**
 * 进入高级设置
 */
const gotoAdvancedSettings = () => {
  uni.navigateTo({
    url: `/pages/access/device-advanced?id=${deviceId.value}`
  })
}

/**
 * 编辑设备
 */
const editDevice = () => {
  hideActionMenu()
  uni.navigateTo({
    url: `/pages/access/device-edit?id=${deviceId.value}`
  })
}

/**
 * 删除设备
 */
const deleteDevice = () => {
  hideActionMenu()

  uni.showModal({
    title: '确认删除',
    content: '确定要删除此设备吗？删除后将无法恢复！',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '删除中...',
          mask: true
        })

        try {
          const result = await accessApi.deleteDevice(deviceId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
            setTimeout(() => {
              uni.navigateBack()
            }, 500)
          } else {
            uni.showToast({
              title: result.message || '删除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('删除设备失败:', error)
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 查看全部门
 */
const viewAllDoors = () => {
  // 跳转到门列表页面
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

/**
 * 查看门详情
 */
const viewDoorDetail = (door) => {
  // 跳转到门详情页面
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

/**
 * 查看全部读头
 */
const viewAllReaders = () => {
  // 跳转到读头列表页面
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

/**
 * 显示操作菜单
 */
const showActionMenu = () => {
  actionMenuVisible.value = true
}

/**
 * 隐藏操作菜单
 */
const hideActionMenu = () => {
  actionMenuVisible.value = false
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
}

// 导航栏
.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;

  .nav-left,
  .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
    flex: 1;
    text-align: center;
  }
}

// 状态卡片
.status-card {
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .status-header {
    margin-bottom: 15px;

    .device-info-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .device-name {
        font-size: 20px;
        font-weight: bold;
        color: #fff;
      }

      .status-badge {
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;

        &.online {
          background-color: rgba(82, 196, 26, 0.2);
          color: #52c41a;
        }

        &.offline {
          background-color: rgba(255, 77, 79, 0.2);
          color: #ff4d4f;
        }
      }
    }
  }

  .status-stats {
    display: flex;
    justify-content: space-between;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-label {
        font-size: 13px;
        color: rgba(255, 255, 255, 0.9);
        margin-top: 6px;
      }
    }
  }
}

// 快速操作
.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fff;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .action-icon {
      width: 56px;
      height: 56px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 8px;
    }

    .action-text {
      font-size: 13px;
      color: #666;
    }
  }
}

// 详情区块
.detail-sections {
  padding: 0 15px;

  .detail-section {
    background-color: #fff;
    border-radius: 12px;
    margin-bottom: 15px;
    overflow: hidden;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      border-bottom: 1px solid #f0f0f0;

      .section-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }

      .section-more {
        font-size: 13px;
        color: #1890ff;
      }
    }

    .section-body {
      padding: 0 15px;

      .detail-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .detail-label {
          font-size: 15px;
          color: #666;
        }

        .detail-value {
          font-size: 15px;
          color: #333;
          text-align: right;
          max-width: 60%;
        }
      }

      // 门列表
      .door-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .door-info {
          flex: 1;

          .door-name {
            font-size: 15px;
            font-weight: 500;
            color: #333;
            display: block;
            margin-bottom: 6px;
          }

          .door-location {
            font-size: 13px;
            color: #999;
          }
        }

        .door-status {
          padding: 4px 10px;
          border-radius: 12px;
          font-size: 12px;

          &.open {
            background-color: #f6ffed;
            color: #52c41a;
          }

          &.closed {
            background-color: #fff1f0;
            color: #ff4d4f;
          }
        }
      }

      // 读头列表
      .reader-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .reader-info {
          flex: 1;

          .reader-name {
            font-size: 15px;
            font-weight: 500;
            color: #333;
            display: block;
            margin-bottom: 6px;
          }

          .reader-type {
            font-size: 13px;
            color: #999;
          }
        }

        .reader-status {
          padding: 4px 10px;
          border-radius: 12px;
          font-size: 12px;

          &.online {
            background-color: #f6ffed;
            color: #52c41a;
          }

          &.offline {
            background-color: #fff1f0;
            color: #ff4d4f;
          }
        }
      }

      // 查看更多
      .view-more {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 12px 0;

        .view-more-text {
          font-size: 13px;
          color: #999;
          margin-right: 4px;
        }
      }

      // 统计网格
      .stats-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 15px;
        padding: 15px 0;

        .grid-item {
          display: flex;
          flex-direction: column;
          align-items: center;

          .grid-value {
            font-size: 20px;
            font-weight: bold;
            color: #1890ff;
            margin-bottom: 6px;
          }

          .grid-label {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
  }
}

// 底部操作栏
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 15px;
  background-color: #fff;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;

  .bottom-btn {
    flex: 1;
    height: 44px;
    line-height: 44px;
    border-radius: 8px;
    font-size: 15px;
    border: none;

    &.secondary {
      background-color: #f5f5f5;
      color: #666;
    }

    &.danger {
      background-color: #ff4d4f;
      color: #fff;
    }
  }
}

// 操作菜单弹窗
.action-menu {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;

  .menu-content {
    width: 100%;
    background-color: #fff;
    border-radius: 16px 16px 0 0;
    overflow: hidden;
    padding-bottom: env(safe-area-inset-bottom);

    .menu-item {
      display: flex;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid #f0f0f0;

      &:active {
        background-color: #f5f5f5;
      }

      .menu-text {
        flex: 1;
        margin-left: 12px;
        font-size: 16px;
        color: #333;

        &.danger {
          color: #ff4d4f;
        }
      }
    }

    .menu-divider {
      height: 8px;
      background-color: #f5f5f5;
    }
  }
}
</style>
