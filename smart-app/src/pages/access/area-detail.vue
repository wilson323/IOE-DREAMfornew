<template>
  <view class="area-detail-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">区域详情</view>
      <view class="nav-right" @click="showMoreMenu">
        <uni-icons type="more-filled" size="20" color="#333"></uni-icons>
      </view>
    </view>

    <!-- 区域信息卡片 -->
    <view class="info-card">
      <view class="card-header">
        <view class="area-name">{{ areaInfo.areaName }}</view>
        <view :class="['status-badge', areaInfo.enabled ? 'enabled' : 'disabled']">
          <text class="status-text">{{ areaInfo.enabled ? '已启用' : '已禁用' }}</text>
        </view>
      </view>
      <view class="card-meta">
        <view class="meta-item">
          <uni-icons type="home" size="14" color="#999"></uni-icons>
          <text class="meta-text">{{ getAreaTypeLabel(areaInfo.areaType) }}</text>
        </view>
        <view class="meta-item">
          <uni-icons type="location" size="14" color="#999"></uni-icons>
          <text class="meta-text">{{ areaInfo.location || '未设置位置' }}</text>
        </view>
      </view>
      <view class="card-path" v-if="areaInfo.areaPath">
        <uni-icons type="paperplane" size="14" color="#999"></uni-icons>
        <text class="path-text">{{ areaInfo.areaPath }}</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-grid">
      <view class="stat-item" @click="viewDevices">
        <view class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <uni-icons type="settings" size="20" color="#fff"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.deviceCount || 0 }}</text>
          <text class="stat-label">关联设备</text>
        </view>
      </view>
      <view class="stat-item" @click="viewPermissions">
        <view class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <uni-icons type="person" size="20" color="#fff"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.userCount || 0 }}</text>
          <text class="stat-label">授权用户</text>
        </view>
      </view>
      <view class="stat-item">
        <view class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <uni-icons type="checkmarkempty" size="20" color="#fff"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.accessCount || 0 }}</text>
          <text class="stat-label">通行次数</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-item" @click="editArea">
        <view class="action-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <uni-icons type="compose" size="18" color="#fff"></uni-icons>
        </view>
        <text class="action-text">编辑</text>
      </view>
      <view class="action-item" @click="managePermissions">
        <view class="action-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <uni-icons type="personadd" size="18" color="#fff"></uni-icons>
        </view>
        <text class="action-text">权限管理</text>
      </view>
      <view class="action-item" @click="viewSubAreas">
        <view class="action-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
          <uni-icons type="folder" size="18" color="#fff"></uni-icons>
        </view>
        <text class="action-text">子区域</text>
      </view>
      <view class="action-item" @click="viewDevices">
        <view class="action-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
          <uni-icons type="settings" size="18" color="#fff"></uni-icons>
        </view>
        <text class="action-text">关联设备</text>
      </view>
    </view>

    <!-- 设备列表 -->
    <view class="list-section" v-if="deviceList.length > 0">
      <view class="section-header">
        <text class="section-title">关联设备（{{ deviceList.length }}）</text>
        <text class="section-more" @click="viewDevices">查看全部</text>
      </view>
      <view class="device-list">
        <view
          class="device-item"
          v-for="device in deviceList.slice(0, 3)"
          :key="device.deviceId"
          @click="viewDeviceDetail(device)"
        >
          <view class="device-left">
            <view class="device-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <uni-icons type="settings" size="16" color="#fff"></uni-icons>
            </view>
            <view class="device-info">
              <text class="device-name">{{ device.deviceName }}</text>
              <text class="device-type">{{ getDeviceTypeLabel(device.deviceType) }}</text>
            </view>
          </view>
          <view :class="['device-status', device.online ? 'online' : 'offline']">
            <text class="status-text">{{ device.online ? '在线' : '离线' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 子区域列表 -->
    <view class="list-section" v-if="subAreaList.length > 0">
      <view class="section-header">
        <text class="section-title">子区域（{{ subAreaList.length }}）</text>
        <text class="section-more" @click="viewSubAreas">查看全部</text>
      </view>
      <view class="area-list">
        <view
          class="sub-area-item"
          v-for="area in subAreaList.slice(0, 3)"
          :key="area.areaId"
          @click="viewSubAreaDetail(area)"
        >
          <view class="area-left">
            <view class="area-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <uni-icons type="home" size="16" color="#fff"></uni-icons>
            </view>
            <view class="area-info">
              <text class="area-name">{{ area.areaName }}</text>
              <text class="area-meta">{{ getAreaTypeLabel(area.areaType) }}</text>
            </view>
          </view>
          <view class="area-devices">
            <text class="device-count">{{ area.deviceCount || 0 }}个设备</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 权限用户列表 -->
    <view class="list-section" v-if="permissionList.length > 0">
      <view class="section-header">
        <text class="section-title">授权用户（{{ permissionList.length }}）</text>
        <text class="section-more" @click="viewPermissions">查看全部</text>
      </view>
      <view class="user-list">
        <view
          class="user-item"
          v-for="user in permissionList.slice(0, 5)"
          :key="user.userId"
        >
          <image class="user-avatar" :src="user.avatar || '/static/images/default-avatar.png'" mode="aspectFill"></image>
          <view class="user-info">
            <text class="user-name">{{ user.userName }}</text>
            <text class="user-dept">{{ user.departmentName || '未分配部门' }}</text>
          </view>
          <view class="user-time">
            <text class="time-text">{{ formatTime(user.grantTime) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <button class="action-btn secondary" @click="editArea">
        <uni-icons type="compose" size="16" color="#666"></uni-icons>
        <text class="btn-text">编辑</text>
      </button>
      <button class="action-btn primary" @click="managePermissions">
        <uni-icons type="personadd" size="16" color="#fff"></uni-icons>
        <text class="btn-text">权限管理</text>
      </button>
    </view>

    <!-- 更多菜单弹窗 -->
    <uni-popup ref="morePopup" type="bottom">
      <view class="more-menu">
        <view class="menu-item" @click="editArea">
          <uni-icons type="compose" size="18" color="#333"></uni-icons>
          <text class="menu-text">编辑区域</text>
        </view>
        <view class="menu-item" @click="copyArea">
          <uni-icons type="copy" size="18" color="#333"></uni-icons>
          <text class="menu-text">复制区域</text>
        </view>
        <view class="menu-item" @click="toggleStatus">
          <uni-icons :type="areaInfo.enabled ? 'eye-slash' : 'eye'" size="18" color="#333"></uni-icons>
          <text class="menu-text">{{ areaInfo.enabled ? '禁用区域' : '启用区域' }}</text>
        </view>
        <view class="menu-item danger" @click="deleteArea">
          <uni-icons type="trash" size="18" color="#ff4d4f"></uni-icons>
          <text class="menu-text danger">删除区域</text>
        </view>
        <view class="menu-cancel" @click="closeMoreMenu">
          <text class="cancel-text">取消</text>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 区域信息
const areaInfo = ref({})
const statistics = ref({
  deviceCount: 0,
  userCount: 0,
  accessCount: 0
})

// 列表数据
const deviceList = ref([])
const subAreaList = ref([])
const permissionList = ref([])

// 弹窗引用
const morePopup = ref(null)

// 区域ID
const areaId = ref(null)

// 页面生命周期
onLoad((options) => {
  if (options.id) {
    areaId.value = parseInt(options.id)
    loadAreaDetail()
  }
})

/**
 * 加载区域详情
 */
const loadAreaDetail = async () => {
  uni.showLoading({
    title: '加载中...',
    mask: true
  })

  try {
    const result = await accessApi.getAreaDetail(areaId.value)
    uni.hideLoading()

    if (result.success && result.data) {
      areaInfo.value = result.data.area || {}
      deviceList.value = result.data.devices || []
      subAreaList.value = result.data.subAreas || []
      permissionList.value = result.data.permissions || []
      statistics.value = result.data.statistics || statistics.value
    } else {
      uni.showToast({
        title: result.message || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('加载区域详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 获取区域类型标签
 */
const getAreaTypeLabel = (type) => {
  const types = {
    'building': '楼宇',
    'floor': '楼层',
    'room': '房间'
  }
  return types[type] || '未知'
}

/**
 * 获取设备类型标签
 */
const getDeviceTypeLabel = (type) => {
  const types = {
    1: '门禁控制器',
    2: '门禁一体机'
  }
  return types[type] || '未知'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1小时
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }

  // 小于24小时
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }

  // 格式化为日期
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

/**
 * 编辑区域
 */
const editArea = () => {
  uni.navigateTo({
    url: `/pages/access/area-edit?id=${areaId.value}`
  })
  closeMoreMenu()
}

/**
 * 权限管理
 */
const managePermissions = () => {
  uni.navigateTo({
    url: `/pages/access/area-permission?id=${areaId.value}`
  })
}

/**
 * 查看设备
 */
const viewDevices = () => {
  uni.navigateTo({
    url: `/pages/access/area-devices?id=${areaId.value}`
  })
}

/**
 * 查看设备详情
 */
const viewDeviceDetail = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-detail?id=${device.deviceId}`
  })
}

/**
 * 查看子区域
 */
const viewSubAreas = () => {
  uni.navigateTo({
    url: `/pages/access/area-sublist?id=${areaId.value}`
  })
}

/**
 * 查看子区域详情
 */
const viewSubAreaDetail = (area) => {
  uni.navigateTo({
    url: `/pages/access/area-detail?id=${area.areaId}`
  })
}

/**
 * 查看权限
 */
const viewPermissions = () => {
  uni.navigateTo({
    url: `/pages/access/area-permission?id=${areaId.value}`
  })
}

/**
 * 复制区域
 */
const copyArea = () => {
  uni.navigateTo({
    url: `/pages/access/area-add?copyId=${areaId.value}`
  })
  closeMoreMenu()
}

/**
 * 切换状态
 */
const toggleStatus = () => {
  const action = areaInfo.value.enabled ? '禁用' : '启用'

  uni.showModal({
    title: `确认${action}`,
    content: `确定要${action}此区域吗？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: `${action}中...`,
          mask: true
        })

        try {
          const result = await accessApi.updateArea(areaId.value, {
            enabled: !areaInfo.value.enabled
          })

          uni.hideLoading()

          if (result.success) {
            areaInfo.value.enabled = !areaInfo.value.enabled
            uni.showToast({
              title: `${action}成功`,
              icon: 'success'
            })
          } else {
            uni.showToast({
              title: result.message || `${action}失败`,
              icon: 'none'
            })
          }
        } catch (error) {
          uni.hideLoading()
          console.error(`${action}区域失败:`, error)
          uni.showToast({
            title: `${action}失败`,
            icon: 'none'
          })
        }
      }
    }
  })

  closeMoreMenu()
}

/**
 * 删除区域
 */
const deleteArea = () => {
  // 检查是否有子区域
  if (subAreaList.value.length > 0) {
    uni.showToast({
      title: '该区域下有子区域，无法删除',
      icon: 'none'
    })
    return
  }

  // 检查是否有设备
  if (deviceList.value.length > 0) {
    uni.showToast({
      title: '该区域下有关联设备，无法删除',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认删除',
    content: '删除后数据将无法恢复，确定要删除吗？',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '删除中...',
          mask: true
        })

        try {
          const result = await accessApi.deleteArea(areaId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })

            // 返回列表
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
          console.error('删除区域失败:', error)
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })

  closeMoreMenu()
}

/**
 * 显示更多菜单
 */
const showMoreMenu = () => {
  morePopup.value.open()
}

/**
 * 关闭更多菜单
 */
const closeMoreMenu = () => {
  morePopup.value.close()
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.area-detail-page {
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

// 区域信息卡片
.info-card {
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .area-name {
      font-size: 22px;
      font-weight: bold;
      color: #fff;
    }

    .status-badge {
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;

      &.enabled {
        background-color: rgba(82, 196, 26, 0.2);
        color: #52c41a;
      }

      &.disabled {
        background-color: rgba(255, 77, 79, 0.2);
        color: #ff4d4f;
      }

      .status-text {
        color: inherit;
      }
    }
  }

  .card-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 10px;

    .meta-item {
      display: flex;
      align-items: center;

      .meta-text {
        margin-left: 6px;
        font-size: 14px;
        color: rgba(255, 255, 255, 0.9);
      }
    }
  }

  .card-path {
    display: flex;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);

    .path-text {
      flex: 1;
      margin-left: 6px;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

// 统计卡片
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin: 0 15px 15px;

  .stat-item {
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .stat-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
    }

    .stat-info {
      flex: 1;

      .stat-value {
        display: block;
        font-size: 20px;
        font-weight: bold;
        color: #333;
        line-height: 1;
        margin-bottom: 6px;
      }

      .stat-label {
        display: block;
        font-size: 12px;
        color: #999;
      }
    }
  }
}

// 快捷操作
.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 0 15px 15px;

  .action-item {
    background-color: #fff;
    border-radius: 12px;
    padding: 15px 10px;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .action-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 10px;
    }

    .action-text {
      font-size: 12px;
      color: #666;
    }
  }
}

// 列表区块
.list-section {
  margin: 0 15px 15px;
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .section-more {
      font-size: 14px;
      color: #1890ff;
    }
  }
}

// 设备列表
.device-list {
  .device-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .device-left {
      flex: 1;
      display: flex;
      align-items: center;
      overflow: hidden;

      .device-icon {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
      }

      .device-info {
        flex: 1;
        overflow: hidden;

        .device-name {
          display: block;
          font-size: 15px;
          color: #333;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .device-type {
          display: block;
          font-size: 12px;
          color: #999;
        }
      }
    }

    .device-status {
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
}

// 子区域列表
.area-list {
  .sub-area-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .area-left {
      flex: 1;
      display: flex;
      align-items: center;

      .area-icon {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
      }

      .area-info {
        flex: 1;

        .area-name {
          display: block;
          font-size: 15px;
          color: #333;
          margin-bottom: 4px;
        }

        .area-meta {
          display: block;
          font-size: 12px;
          color: #999;
        }
      }
    }

    .area-devices {
      .device-count {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

// 用户列表
.user-list {
  .user-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      margin-right: 12px;
    }

    .user-info {
      flex: 1;

      .user-name {
        display: block;
        font-size: 15px;
        color: #333;
        margin-bottom: 4px;
      }

      .user-dept {
        display: block;
        font-size: 12px;
        color: #999;
      }
    }

    .user-time {
      .time-text {
        font-size: 12px;
        color: #999;
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
  display: flex;
  gap: 12px;
  padding: 10px 15px;
  background-color: #fff;
  border-top: 1px solid #eee;
  z-index: 99;

  .action-btn {
    flex: 1;
    height: 48px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 500;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;

    &.secondary {
      background-color: #f5f5f5;
      color: #666;
    }

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }

    .btn-text {
      margin-left: 6px;
    }
  }
}

// 更多菜单
.more-menu {
  background-color: #fff;
  border-radius: 20px 20px 0 0;
  padding: 10px 0;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #f0f0f0;

    &:last-of-type {
      border-bottom: none;
    }

    &:active {
      background-color: #f5f5f5;
    }

    &.danger {
      .menu-text {
        color: #ff4d4f;
      }
    }

    .menu-text {
      flex: 1;
      margin-left: 12px;
      font-size: 15px;
      color: #333;
    }
  }

  .menu-cancel {
    padding: 15px;
    text-align: center;
    border-top: 1px solid #f0f0f0;

    .cancel-text {
      font-size: 16px;
      color: #666;
    }
  }
}
</style>
