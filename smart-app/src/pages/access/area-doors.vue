<template>
  <view class="area-doors">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">门禁管理</text>
        </view>
        <view class="navbar-right">
          <view class="navbar-icon" @click="handleAddDoor">
            <uni-icons type="plus" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 区域信息卡片 -->
    <view class="area-info-card">
      <view class="card-top-bar"></view>
      <view class="area-header">
        <text class="area-name">{{ areaInfo.areaName }}</text>
        <text class="area-code">{{ areaInfo.areaCode }}</text>
      </view>
      <view class="area-stats">
        <view class="stat-item">
          <text class="stat-value">{{ doorList.length }}</text>
          <text class="stat-label">关联门禁</text>
        </view>
        <view class="stat-item">
          <text class="stat-value text-success">{{ onlineDoorCount }}</text>
          <text class="stat-label">在线</text>
        </view>
      </view>
    </view>

    <!-- 快速操作 -->
    <view class="quick-actions">
      <view class="action-btn" @click="handleRefresh">
        <uni-icons type="refreshempty" size="18" color="#667eea"></uni-icons>
        <text>刷新</text>
      </view>
      <view class="action-btn" @click="handleBatchRemove">
        <uni-icons type="minus" size="18" color="#ff4d4f"></uni-icons>
        <text>批量移除</text>
      </view>
      <view class="action-btn" @click="handleAutoAssign">
        <uni-icons type="checkmarkempty" size="18" color="#52c41a"></uni-icons>
        <text>自动分配</text>
      </view>
    </view>

    <!-- 门禁列表 -->
    <scroll-view
      class="door-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        v-for="door in doorList"
        :key="door.deviceId"
        class="door-card"
        :class="{ selected: selectedDoors.includes(door.deviceId) }"
        @click="toggleSelect(door.deviceId)"
      >
        <view class="card-top-bar"></view>

        <!-- 门禁图标和信息 -->
        <view class="door-header">
          <view class="door-icon-wrapper">
            <view class="door-icon" :class="{ online: door.online }">
              <uni-icons type="home" size="22" color="#fff"></uni-icons>
            </view>
          </view>
          <view class="door-info">
            <text class="door-name">{{ door.deviceName }}</text>
            <text class="door-code">{{ door.deviceCode }}</text>
          </view>
          <view class="door-status">
            <view
              class="status-badge"
              :class="{ online: door.online }"
            >
              {{ door.online ? '在线' : '离线' }}
            </view>
          </view>
        </view>

        <!-- 门禁详情 -->
        <view class="door-details">
          <view class="detail-row">
            <uni-icons type="location" size="14" color="#999"></uni-icons>
            <text class="detail-text">{{ door.location || '未设置位置' }}</text>
          </view>
          <view class="detail-row">
            <uni-icons type="settings" size="14" color="#999"></uni-icons>
            <text class="detail-text">{{ getDeviceTypeLabel(door.deviceType) }}</text>
          </view>
        </view>

        <!-- 权限信息 -->
        <view class="door-permission">
          <view class="permission-label">
            <uni-icons type="locked" size="14" color="#667eea"></uni-icons>
            <text>权限模式</text>
          </view>
          <view class="permission-value">
            <text>{{ getPermissionModeLabel(door.permissionMode) }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="door-actions">
          <view class="action-item" @click.stop="viewDoorDetail(door)">
            <uni-icons type="eye" size="16" color="#667eea"></uni-icons>
            <text>详情</text>
          </view>
          <view class="action-item" @click.stop="configDoor(door)">
            <uni-icons type="settings" size="16" color="#52c41a"></uni-icons>
            <text>配置</text>
          </view>
          <view class="action-item danger" @click.stop="removeDoor(door)">
            <uni-icons type="trash" size="16" color="#ff4d4f"></uni-icons>
            <text>移除</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="doorList.length === 0 && !loading" class="empty-state">
        <image src="/static/images/empty-door.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">该区域暂未关联门禁</text>
        <button class="empty-btn" @click="handleAddDoor">添加门禁</button>
      </view>
    </scroll-view>

    <!-- 底部操作栏（批量选择时显示） -->
    <view class="batch-bar" v-if="selectedDoors.length > 0">
      <view class="batch-info">
        <text>已选择 {{ selectedDoors.length }} 个门禁</text>
      </view>
      <view class="batch-actions">
        <view class="batch-btn" @click="handleBatchRemove">
          <uni-icons type="trash" size="16" color="#fff"></uni-icons>
          <text>批量移除</text>
        </view>
        <view class="batch-btn secondary" @click="clearSelection">
          <text>取消</text>
        </view>
      </view>
    </view>

    <!-- 添加门禁弹窗 -->
    <uni-popup ref="addDoorPopup" type="bottom">
      <view class="add-door-popup">
        <view class="popup-header">
          <text class="popup-title">添加门禁</text>
          <view class="popup-close" @click="closeAddDoor">
            <uni-icons type="closeempty" size="24" color="#333"></uni-icons>
          </view>
        </view>
        <view class="popup-content">
          <!-- 门禁选择器 -->
          <view class="form-section">
            <view class="form-label">选择门禁</view>
            <view class="door-selector">
              <scroll-view scroll-y class="available-doors">
                <view
                  v-for="door in availableDoors"
                  :key="door.deviceId"
                  class="available-door-item"
                  :class="{ selected: tempSelectedDoors.includes(door.deviceId) }"
                  @click="toggleDoorSelection(door.deviceId)"
                >
                  <view class="door-checkbox">
                    <checkbox
                      :value="door.deviceId"
                      :checked="tempSelectedDoors.includes(door.deviceId)"
                    />
                  </view>
                  <view class="door-item-info">
                    <text class="item-name">{{ door.deviceName }}</text>
                    <text class="item-code">{{ door.deviceCode }}</text>
                  </view>
                  <view class="item-status">
                    <view
                      class="status-dot"
                      :class="{ online: door.online }"
                    ></view>
                  </view>
                </view>
              </scroll-view>
            </view>
          </view>

          <!-- 权限设置 -->
          <view class="form-section">
            <view class="form-label">权限模式</view>
            <view class="permission-modes">
              <view
                v-for="mode in permissionModes"
                :key="mode.value"
                class="mode-option"
                :class="{ active: permissionMode === mode.value }"
                @click="selectPermissionMode(mode.value)"
              >
                <text class="mode-text">{{ mode.label }}</text>
              </view>
            </view>
          </view>

          <!-- 时间段设置 -->
          <view class="form-section" v-if="permissionMode === 'time_limit'">
            <view class="form-label">访问时间</view>
            <view class="time-picker">
              <picker mode="time" :value="startTime" @change="onStartTimeChange">
                <view class="time-picker-value">
                  <text>开始时间</text>
                  <text class="time-value">{{ startTime || '请选择' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
              <picker mode="time" :value="endTime" @change="onEndTimeChange">
                <view class="time-picker-value">
                  <text>结束时间</text>
                  <text class="time-value">{{ endTime || '请选择' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <view class="footer-btn cancel" @click="closeAddDoor">取消</view>
          <view class="footer-btn confirm" @click="confirmAddDoor">确定</view>
        </view>
      </view>
    </uni-popup>

    <!-- 自动分配配置弹窗 -->
    <uni-popup ref="autoAssignPopup" type="center">
      <view class="auto-assign-popup">
        <view class="popup-header">
          <text class="popup-title">自动权限分配</text>
        </view>
        <view class="popup-content">
          <view class="assign-option" @click="autoAssignByArea">
            <view class="option-icon bg-primary">
              <uni-icons type="home" size="24" color="#fff"></uni-icons>
            </view>
            <view class="option-info">
              <text class="option-title">按区域分配</text>
              <text class="option-desc">自动为该区域所有人员分配门禁权限</text>
            </view>
          </view>
          <view class="assign-option" @click="autoAssignByDepartment">
            <view class="option-icon bg-success">
              <uni-icons type="person" size="24" color="#fff"></uni-icons>
            </view>
            <view class="option-info">
              <text class="option-title">按部门分配</text>
              <text class="option-desc">根据部门自动分配门禁权限</text>
            </view>
          </view>
          <view class="assign-option" @click="autoAssignByRole">
            <view class="option-icon bg-warning">
              <uni-icons type="settings" size="24" color="#fff"></uni-icons>
            </view>
            <view class="option-info">
              <text class="option-title">按角色分配</text>
              <text class="option-desc">根据角色自动分配门禁权限</text>
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <view class="footer-btn cancel" @click="closeAutoAssign">取消</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 区域ID
const areaId = ref('')

// 区域信息
const areaInfo = reactive({
  areaId: '',
  areaName: '',
  areaCode: ''
})

// 门禁列表
const doorList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const onlineDoorCount = ref(0)

// 选中的门禁
const selectedDoors = ref([])

// 可用的门禁列表
const availableDoors = ref([])

// 临时选中的门禁（添加弹窗中）
const tempSelectedDoors = ref([])

// 权限模式
const permissionMode = ref('full')
const permissionModes = [
  { value: 'full', label: '全部权限' },
  { value: 'time_limit', label: '时间段限制' },
  { value: 'week_limit', label: '工作日限制' }
]

// 时间设置
const startTime = ref('')
const endTime = ref('')

// 弹窗引用
const addDoorPopup = ref(null)
const autoAssignPopup = ref(null)

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  areaId.value = options.areaId

  if (areaId.value) {
    loadAreaInfo()
    loadDoorList()
    loadAvailableDoors()
  }
})

// 加载区域信息
const loadAreaInfo = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAreaInfo(areaId.value)
    // Object.assign(areaInfo, res.data)

    // 模拟数据
    Object.assign(areaInfo, {
      areaId: '1',
      areaName: 'A栋办公楼',
      areaCode: 'AREA-A-001'
    })
  } catch (error) {
    console.error('加载区域信息失败:', error)
  }
}

// 加载门禁列表
const loadDoorList = async () => {
  loading.value = true

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAreaDoors(areaId.value)
    // doorList.value = res.data

    // 模拟数据
    doorList.value = [
      {
        deviceId: '1',
        deviceName: 'A栋主入口门禁',
        deviceCode: 'DOOR-A-001',
        deviceType: 1,
        location: 'A栋1层大厅',
        online: true,
        permissionMode: 'full'
      },
      {
        deviceId: '2',
        deviceName: 'A栋电梯门禁',
        deviceCode: 'DOOR-A-002',
        deviceType: 1,
        location: 'A栋电梯厅',
        online: true,
        permissionMode: 'time_limit'
      },
      {
        deviceId: '3',
        deviceName: 'A栋会议室门禁',
        deviceCode: 'DOOR-A-003',
        deviceType: 2,
        location: 'A栋3层',
        online: false,
        permissionMode: 'week_limit'
      }
    ]

    onlineDoorCount.value = doorList.value.filter(d => d.online).length
  } catch (error) {
    console.error('加载门禁列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载可用的门禁列表
const loadAvailableDoors = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await deviceApi.getAvailableDevices(areaId.value)
    // availableDoors.value = res.data

    // 模拟数据
    availableDoors.value = [
      {
        deviceId: '4',
        deviceName: 'B栋门禁1',
        deviceCode: 'DOOR-B-001',
        deviceType: 1,
        online: true
      },
      {
        deviceId: '5',
        deviceName: 'B栋门禁2',
        deviceCode: 'DOOR-B-002',
        deviceType: 1,
        online: false
      }
    ]
  } catch (error) {
    console.error('加载可用门禁失败:', error)
  }
}

// 刷新
const onRefresh = () => {
  refreshing.value = true
  loadDoorList().then(() => {
    setTimeout(() => {
      refreshing.value = false
    }, 500)
  })
}

const handleRefresh = () => {
  onRefresh()
}

// 切换选择
const toggleSelect = (doorId) => {
  const index = selectedDoors.value.indexOf(doorId)
  if (index > -1) {
    selectedDoors.value.splice(index, 1)
  } else {
    selectedDoors.value.push(doorId)
  }
}

// 清除选择
const clearSelection = () => {
  selectedDoors.value = []
}

// 添加门禁
const handleAddDoor = () => {
  tempSelectedDoors.value = []
  permissionMode.value = 'full'
  startTime.value = ''
  endTime.value = ''
  addDoorPopup.value.open()
}

// 关闭添加门禁弹窗
const closeAddDoor = () => {
  addDoorPopup.value.close()
}

// 切换门禁选择
const toggleDoorSelection = (doorId) => {
  const index = tempSelectedDoors.value.indexOf(doorId)
  if (index > -1) {
    tempSelectedDoors.value.splice(index, 1)
  } else {
    tempSelectedDoors.value.push(doorId)
  }
}

// 选择权限模式
const selectPermissionMode = (mode) => {
  permissionMode.value = mode
}

// 开始时间变更
const onStartTimeChange = (e) => {
  startTime.value = e.detail.value
}

// 结束时间变更
const onEndTimeChange = (e) => {
  endTime.value = e.detail.value
}

// 确认添加门禁
const confirmAddDoor = async () => {
  if (tempSelectedDoors.value.length === 0) {
    uni.showToast({
      title: '请选择门禁',
      icon: 'none'
    })
    return
  }

  uni.showLoading({ title: '添加中...' })

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.addDoorsToArea(areaId.value, {
    //   deviceIds: tempSelectedDoors.value,
    //   permissionMode: permissionMode.value,
    //   startTime: startTime.value,
    //   endTime: endTime.value
    // })

    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({
        title: '添加成功',
        icon: 'success'
      })
      closeAddDoor()
      loadDoorList()
    }, 500)
  } catch (error) {
    uni.hideLoading()
    console.error('添加门禁失败:', error)
    uni.showToast({
      title: '添加失败',
      icon: 'none'
    })
  }
}

// 批量移除
const handleBatchRemove = () => {
  if (selectedDoors.value.length === 0) {
    uni.showToast({
      title: '请先选择门禁',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认移除',
    content: `确定要移除选中的${selectedDoors.value.length}个门禁吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用实际的API
        uni.showToast({
          title: '移除成功',
          icon: 'success'
        })
        clearSelection()
        loadDoorList()
      }
    }
  })
}

// 自动分配
const handleAutoAssign = () => {
  autoAssignPopup.value.open()
}

// 关闭自动分配弹窗
const closeAutoAssign = () => {
  autoAssignPopup.value.close()
}

// 按区域分配
const autoAssignByArea = () => {
  closeAutoAssign()
  // TODO: 实现按区域自动分配
  uni.showToast({
    title: '按区域分配功能开发中',
    icon: 'none'
  })
}

// 按部门分配
const autoAssignByDepartment = () => {
  closeAutoAssign()
  // TODO: 实现按部门自动分配
  uni.showToast({
    title: '按部门分配功能开发中',
    icon: 'none'
  })
}

// 按角色分配
const autoAssignByRole = () => {
  closeAutoAssign()
  // TODO: 实现按角色自动分配
  uni.showToast({
    title: '按角色分配功能开发中',
    icon: 'none'
  })
}

// 查看门禁详情
const viewDoorDetail = (door) => {
  uni.navigateTo({
    url: `/pages/access/device-detail?deviceId=${door.deviceId}`
  })
}

// 配置门禁
const configDoor = (door) => {
  uni.navigateTo({
    url: `/pages/access/device-settings?deviceId=${door.deviceId}`
  })
}

// 移除门禁
const removeDoor = (door) => {
  uni.showModal({
    title: '确认移除',
    content: `确定要将"${door.deviceName}"从该区域移除吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用实际的API
        uni.showToast({
          title: '移除成功',
          icon: 'success'
        })
        loadDoorList()
      }
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 获取设备类型标签
const getDeviceTypeLabel = (type) => {
  const typeMap = {
    1: '门禁控制器',
    2: '门禁一体机'
  }
  return typeMap[type] || '未知'
}

// 获取权限模式标签
const getPermissionModeLabel = (mode) => {
  const modeMap = {
    full: '全部权限',
    time_limit: '时间段限制',
    week_limit: '工作日限制'
  }
  return modeMap[mode] || '未知'
}
</script>

<style lang="scss" scoped>
.area-doors {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 120rpx;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);

  .navbar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 88rpx;
    padding: 0 30rpx;
  }

  .navbar-left {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .navbar-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
  }

  .navbar-right {
    .navbar-icon {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
    }
  }
}

// 区域信息卡片
.area-info-card {
  margin: 30rpx;
  margin-top: 158rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;

  .card-top-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  }

  .area-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .area-name {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
    }

    .area-code {
      font-size: 24rpx;
      color: #999;
    }
  }

  .area-stats {
    display: flex;
    gap: 40rpx;

    .stat-item {
      display: flex;
      flex-direction: column;

      .stat-value {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;

        &.text-success {
          color: #52c41a;
        }
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

// 快速操作
.quick-actions {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 20rpx;

  .action-btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
    font-size: 24rpx;
    color: #666;
  }
}

// 门禁列表
.door-list {
  padding: 0 30rpx;
  height: calc(100vh - 420rpx);
}

.door-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;

  &.selected {
    border: 2rpx solid #667eea;
  }

  .card-top-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  }

  .door-header {
    display: flex;
    align-items: center;
    gap: 20rpx;
    margin-bottom: 20rpx;

    .door-icon-wrapper {
      .door-icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 16rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;

        &.online {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
        }
      }
    }

    .door-info {
      flex: 1;

      .door-name {
        display: block;
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }

      .door-code {
        font-size: 24rpx;
        color: #999;
      }
    }

    .door-status {
      .status-badge {
        padding: 8rpx 20rpx;
        border-radius: 20rpx;
        font-size: 24rpx;
        color: #fff;
        background: #d9d9d9;

        &.online {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
        }
      }
    }
  }

  .door-details {
    margin-bottom: 20rpx;

    .detail-row {
      display: flex;
      align-items: center;
      gap: 10rpx;
      margin-bottom: 10rpx;

      .detail-text {
        font-size: 26rpx;
        color: #666;
      }
    }
  }

  .door-permission {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx;
    background: #f5f7fa;
    border-radius: 12rpx;
    margin-bottom: 20rpx;

    .permission-label {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 26rpx;
      color: #666;
    }

    .permission-value {
      font-size: 26rpx;
      color: #667eea;
      font-weight: 500;
    }
  }

  .door-actions {
    display: flex;
    gap: 20rpx;

    .action-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 0;
      font-size: 24rpx;
      color: #666;

      &.danger {
        color: #ff4d4f;
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  .empty-image {
    width: 400rpx;
    height: 300rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 40rpx;
  }

  .empty-btn {
    padding: 20rpx 60rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
    border: none;
  }
}

// 底部批量操作栏
.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
  z-index: 999;

  .batch-info {
    font-size: 26rpx;
    color: #333;
  }

  .batch-actions {
    display: flex;
    gap: 16rpx;

    .batch-btn {
      padding: 16rpx 32rpx;
      border-radius: 40rpx;
      font-size: 26rpx;
      color: #fff;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      &.secondary {
        background: #f5f7fa;
        color: #666;
      }
    }
  }
}

// 添加门禁弹窗
.add-door-popup {
  padding: 30rpx;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .popup-content {
    max-height: 800rpx;
    overflow-y: auto;

    .form-section {
      margin-bottom: 30rpx;

      .form-label {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 16rpx;
      }
    }

    .door-selector {
      .available-doors {
        max-height: 400rpx;

      }
    }

    .available-door-item {
      display: flex;
      align-items: center;
      gap: 20rpx;
      padding: 24rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      margin-bottom: 16rpx;

      &.selected {
        background: rgba(102, 126, 234, 0.1);
        border: 2rpx solid #667eea;
      }

      .door-item-info {
        flex: 1;

        .item-name {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 8rpx;
        }

        .item-code {
          font-size: 24rpx;
          color: #999;
        }
      }

      .item-status {
        .status-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 50%;

          &.online {
            background: #52c41a;
          }
        }
      }
    }

    .permission-modes {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;

      .mode-option {
        padding: 12rpx 32rpx;
        background: #f5f7fa;
        border-radius: 8rpx;
        font-size: 26rpx;
        color: #666;
        border: 2rpx solid transparent;

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
        }
      }
    }

    .time-picker {
      display: flex;
      flex-direction: column;
      gap: 16rpx;

      .time-picker-value {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx;
        background: #f5f7fa;
        border-radius: 12rpx;
        font-size: 26rpx;
        color: #333;

        .time-value {
          color: #667eea;
        }
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 20rpx;
    margin-top: 30rpx;

    .footer-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24rpx;
      border-radius: 12rpx;
      font-size: 28rpx;

      &.cancel {
        background: #f5f7fa;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

// 自动分配弹窗
.auto-assign-popup {
  padding: 30rpx;
  border-radius: 24rpx;
  width: 600rpx;

  .popup-header {
    margin-bottom: 30rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      text-align: center;
    }
  }

  .popup-content {
    .assign-option {
      display: flex;
      align-items: center;
      gap: 20rpx;
      padding: 24rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      margin-bottom: 16rpx;

      .option-icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        &.bg-primary {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.bg-success {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
        }

        &.bg-warning {
          background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
        }
      }

      .option-info {
        flex: 1;

        .option-title {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 8rpx;
          font-weight: 500;
        }

        .option-desc {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }

  .popup-footer {
    margin-top: 30rpx;

    .footer-btn {
      padding: 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      text-align: center;
      font-size: 28rpx;
      color: #666;
    }
  }
}
</style>
