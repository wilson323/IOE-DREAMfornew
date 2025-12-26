<template>
  <view class="access-device-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">设备管理</view>
      <view class="nav-right">
        <uni-icons type="plus" size="20" color="#1890ff" @click="gotoAddDevice"></uni-icons>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-card">
        <view class="stats-item">
          <text class="stats-value">{{ totalCount }}</text>
          <text class="stats-label">总设备</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-value online">{{ onlineCount }}</text>
          <text class="stats-label">在线</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-value offline">{{ offlineCount }}</text>
          <text class="stats-label">离线</text>
        </view>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-section">
      <!-- 状态筛选 -->
      <scroll-view class="filter-tabs" scroll-x>
        <view
          class="filter-tab"
          :class="{ active: statusFilter === 'all' }"
          @click="switchStatusFilter('all')"
        >
          全部
        </view>
        <view
          class="filter-tab"
          :class="{ active: statusFilter === '1' }"
          @click="switchStatusFilter('1')"
        >
          在线
        </view>
        <view
          class="filter-tab"
          :class="{ active: statusFilter === '0' }"
          @click="switchStatusFilter('0')"
        >
          离线
        </view>
      </scroll-view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <view class="action-btn" @click="showSearchModal">
          <uni-icons type="search" size="16" color="#666"></uni-icons>
          <text class="action-text">搜索</text>
        </view>
        <view class="action-btn" @click="showAreaFilter">
          <uni-icons type="location" size="16" color="#666"></uni-icons>
          <text class="action-text">区域</text>
        </view>
        <view class="action-btn" @click="toggleBatchMode" v-if="!batchMode">
          <uni-icons type="list" size="16" color="#666"></uni-icons>
          <text class="action-text">批量</text>
        </view>
        <view class="action-btn danger" @click="toggleBatchMode" v-else>
          <uni-icons type="close" size="16" color="#fff"></uni-icons>
          <text class="action-text">取消</text>
        </view>
      </view>
    </view>

    <!-- 批量操作栏（批量模式下显示） -->
    <view class="batch-actions" v-if="batchMode">
      <view class="batch-info">
        <text>已选择 {{ selectedDevices.length }} 项</text>
      </view>
      <view class="batch-buttons">
        <view class="batch-btn" @click="batchEnable">
          <uni-icons type="checkmarkempty" size="16" color="#52c41a"></uni-icons>
          <text class="batch-text">启用</text>
        </view>
        <view class="batch-btn" @click="batchDisable">
          <uni-icons type="closeempty" size="16" color="#ff4d4f"></uni-icons>
          <text class="batch-text">禁用</text>
        </view>
        <view class="batch-btn danger" @click="batchDelete">
          <uni-icons type="trash" size="16" color="#fff"></uni-icons>
          <text class="batch-text">删除</text>
        </view>
      </view>
    </view>

    <!-- 设备列表 -->
    <view class="device-list">
      <view
        class="device-item"
        v-for="(device, index) in deviceList"
        :key="device.deviceId || index"
        @click="handleDeviceClick(device)"
      >
        <!-- 批量选择复选框 -->
        <view class="device-checkbox" v-if="batchMode" @click.stop="toggleSelectDevice(device)">
          <uni-icons
            :type="isDeviceSelected(device) ? 'checkbox-filled' : 'circle'"
            :color="isDeviceSelected(device) ? '#1890ff' : '#d9d9d9'"
            size="20"
          ></uni-icons>
        </view>

        <view class="device-info">
          <view class="device-name-row">
            <view class="device-name">{{ device.deviceName }}</view>
            <view :class="['status-badge', device.online ? 'online' : 'offline']">
              <text class="status-text">{{ device.online ? '在线' : '离线' }}</text>
            </view>
          </view>
          <view class="device-location">
            <uni-icons type="location" size="12" color="#999"></uni-icons>
            <text class="location-text">{{ device.areaName || '未分配区域' }}</text>
          </view>
          <view class="device-detail">
            <text class="detail-text">型号：{{ device.deviceModel || '-' }}</text>
            <text class="detail-text">IP：{{ device.ipAddress || '-' }}</text>
          </view>
        </view>

        <view class="device-arrow" v-if="!batchMode">
          <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-more" v-if="loading">
        <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 没有更多数据 -->
      <view class="no-more" v-if="!hasMore && deviceList.length > 0 && !loading">
        <text>没有更多数据了</text>
      </view>

      <!-- 空状态 -->
      <view class="no-data" v-if="deviceList.length === 0 && !loading">
        <image class="no-data-image" src="/static/images/no-data.png" mode="aspectFit"></image>
        <text class="no-data-text">暂无设备</text>
        <view class="no-data-action">
          <button class="add-device-btn" type="primary" size="mini" @click="gotoAddDevice">
            添加设备
          </button>
        </view>
      </view>
    </view>

    <!-- 搜索弹窗 -->
    <view class="search-modal" v-if="searchModalVisible" @click="closeSearchModal">
      <view class="search-content" @click.stop>
        <view class="search-header">
          <text class="search-title">搜索设备</text>
          <uni-icons type="close" size="20" color="#666" @click="closeSearchModal"></uni-icons>
        </view>
        <view class="search-body">
          <view class="search-input">
            <uni-icons type="search" size="18" color="#999"></uni-icons>
            <input
              class="input-field"
              type="text"
              placeholder="请输入设备名称或IP地址"
              v-model="searchKeyword"
              confirm-type="search"
              @confirm="handleSearch"
            />
          </view>
          <button class="search-btn" type="primary" @click="handleSearch">搜索</button>
        </view>
      </view>
    </view>

    <!-- 区域筛选弹窗 -->
    <view class="area-filter-modal" v-if="areaFilterVisible" @click="closeAreaFilter">
      <view class="area-filter-content" @click.stop>
        <view class="filter-header">
          <text class="filter-title">选择区域</text>
          <uni-icons type="close" size="20" color="#666" @click="closeAreaFilter"></uni-icons>
        </view>
        <view class="filter-body">
          <scroll-view class="area-list" scroll-y>
            <view
              class="area-item"
              :class="{ active: selectedAreaId === null }"
              @click="selectArea(null)"
            >
              <text class="area-name">全部区域</text>
              <uni-icons
                v-if="selectedAreaId === null"
                type="checkmarkempty"
                size="18"
                color="#1890ff"
              ></uni-icons>
            </view>
            <view
              class="area-item"
              :class="{ active: selectedAreaId === area.areaId }"
              v-for="area in areaList"
              :key="area.areaId"
              @click="selectArea(area.areaId)"
            >
              <text class="area-name">{{ area.areaName }}</text>
              <uni-icons
                v-if="selectedAreaId === area.areaId"
                type="checkmarkempty"
                size="18"
                color="#1890ff"
              ></uni-icons>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 响应式数据
const deviceList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = ref(true)

// 筛选条件
const statusFilter = ref('all') // all, 1(在线), 0(离线)
const selectedAreaId = ref(null)
const searchKeyword = ref('')

// 统计数据
const totalCount = ref(0)
const onlineCount = ref(0)
const offlineCount = ref(0)

// 批量操作
const batchMode = ref(false)
const selectedDevices = ref([])

// 弹窗状态
const searchModalVisible = ref(false)
const areaFilterVisible = ref(false)
const areaList = ref([])

// 页面生命周期
onMounted(() => {
  loadDevices()
  loadAreaList()
})

onShow(() => {
  // 页面显示时刷新列表
  refreshDeviceList()
})

onPullDownRefresh(() => {
  refreshDeviceList()
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (!loading.value && hasMore.value) {
    loadMoreDevices()
  }
})

// 计算属性
const filteredRecords = computed(() => {
  return deviceList.value
})

// 方法实现
/**
 * 加载设备列表
 */
const loadDevices = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  try {
    if (isRefresh) {
      pageNum.value = 1
      deviceList.value = []
      hasMore.value = true
    }

    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    // 添加筛选条件
    if (statusFilter.value !== 'all') {
      params.status = statusFilter.value
    }
    if (selectedAreaId.value) {
      params.areaId = selectedAreaId.value
    }
    if (searchKeyword.value) {
      params.deviceName = searchKeyword.value
    }

    const result = await accessApi.getDeviceList(params)
    if (result.success && result.data) {
      const newDevices = result.data.list || []

      if (isRefresh) {
        deviceList.value = newDevices
      } else {
        deviceList.value = [...deviceList.value, ...newDevices]
      }

      total.value = result.data.total || 0
      hasMore.value = deviceList.value.length < total.value

      // 更新统计数据
      updateStatistics(result.data)
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载更多设备
 */
const loadMoreDevices = () => {
  pageNum.value++
  loadDevices()
}

/**
 * 刷新设备列表
 */
const refreshDeviceList = () => {
  loadDevices(true)
}

/**
 * 加载区域列表
 */
const loadAreaList = async () => {
  try {
    // 这里需要调用区域列表API
    // 暂时使用模拟数据
    areaList.value = [
      { areaId: 1, areaName: 'A栋1楼' },
      { areaId: 2, areaName: 'A栋2楼' },
      { areaId: 3, areaName: 'B栋1楼' },
      { areaId: 4, areaName: 'B栋2楼' },
      { areaId: 5, areaName: 'C栋1楼' }
    ]
  } catch (error) {
    console.error('加载区域列表失败:', error)
  }
}

/**
 * 更新统计数据
 */
const updateStatistics = (data) => {
  totalCount.value = data.total || 0
  onlineCount.value = data.onlineCount || 0
  offlineCount.value = data.offlineCount || 0
}

/**
 * 切换状态筛选
 */
const switchStatusFilter = (status) => {
  statusFilter.value = status
  refreshDeviceList()
}

/**
 * 显示搜索弹窗
 */
const showSearchModal = () => {
  searchModalVisible.value = true
}

/**
 * 关闭搜索弹窗
 */
const closeSearchModal = () => {
  searchModalVisible.value = false
}

/**
 * 执行搜索
 */
const handleSearch = () => {
  closeSearchModal()
  refreshDeviceList()
}

/**
 * 显示区域筛选
 */
const showAreaFilter = () => {
  areaFilterVisible.value = true
}

/**
 * 关闭区域筛选
 */
const closeAreaFilter = () => {
  areaFilterVisible.value = false
}

/**
 * 选择区域
 */
const selectArea = (areaId) => {
  selectedAreaId.value = areaId
  closeAreaFilter()
  refreshDeviceList()
}

/**
 * 切换批量模式
 */
const toggleBatchMode = () => {
  batchMode.value = !batchMode.value
  if (!batchMode.value) {
    selectedDevices.value = []
  }
}

/**
 * 切换设备选择状态
 */
const toggleSelectDevice = (device) => {
  const index = selectedDevices.value.findIndex(d => d.deviceId === device.deviceId)
  if (index > -1) {
    selectedDevices.value.splice(index, 1)
  } else {
    selectedDevices.value.push(device)
  }
}

/**
 * 判断设备是否已选择
 */
const isDeviceSelected = (device) => {
  return selectedDevices.value.some(d => d.deviceId === device.deviceId)
}

/**
 * 处理设备点击事件
 */
const handleDeviceClick = (device) => {
  if (batchMode.value) {
    toggleSelectDevice(device)
  } else {
    uni.navigateTo({
      url: `/pages/access/device-detail?id=${device.deviceId}`
    })
  }
}

/**
 * 跳转到新增设备页面
 */
const gotoAddDevice = () => {
  uni.navigateTo({
    url: '/pages/access/device-add'
  })
}

/**
 * 批量启用设备
 */
const batchEnable = async () => {
  if (selectedDevices.value.length === 0) {
    uni.showToast({
      title: '请选择设备',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认启用',
    content: `确定要启用选中的 ${selectedDevices.value.length} 个设备吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const deviceIds = selectedDevices.value.map(d => d.deviceId)
          const result = await accessApi.batchUpdateDeviceStatus(deviceIds, 1)

          if (result.success) {
            uni.showToast({
              title: '启用成功',
              icon: 'success'
            })
            refreshDeviceList()
            toggleBatchMode()
          } else {
            uni.showToast({
              title: result.message || '启用失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('批量启用失败:', error)
          uni.showToast({
            title: '启用失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 批量禁用设备
 */
const batchDisable = async () => {
  if (selectedDevices.value.length === 0) {
    uni.showToast({
      title: '请选择设备',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认禁用',
    content: `确定要禁用选中的 ${selectedDevices.value.length} 个设备吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const deviceIds = selectedDevices.value.map(d => d.deviceId)
          const result = await accessApi.batchUpdateDeviceStatus(deviceIds, 0)

          if (result.success) {
            uni.showToast({
              title: '禁用成功',
              icon: 'success'
            })
            refreshDeviceList()
            toggleBatchMode()
          } else {
            uni.showToast({
              title: result.message || '禁用失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('批量禁用失败:', error)
          uni.showToast({
            title: '禁用失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 批量删除设备
 */
const batchDelete = async () => {
  if (selectedDevices.value.length === 0) {
    uni.showToast({
      title: '请选择设备',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedDevices.value.length} 个设备吗？此操作不可恢复！`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          const deviceIds = selectedDevices.value.map(d => d.deviceId)
          const result = await accessApi.batchDeleteDevices(deviceIds)

          if (result.success) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
            refreshDeviceList()
            toggleBatchMode()
          } else {
            uni.showToast({
              title: result.message || '删除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('批量删除失败:', error)
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
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.access-device-page {
  min-height: 100vh;
  background-color: #f5f5f5;
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

// 统计卡片
.stats-section {
  padding: 15px;

  .stats-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 20px 15px;
    display: flex;
    justify-content: space-around;
    align-items: center;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

    .stats-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stats-value {
        font-size: 24px;
        font-weight: bold;
        color: #fff;
        margin-bottom: 4px;

        &.online {
          color: #52c41a;
        }

        &.offline {
          color: #ff4d4f;
        }
      }

      .stats-label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.9);
      }
    }

    .stats-divider {
      width: 1px;
      height: 30px;
      background-color: rgba(255, 255, 255, 0.3);
    }
  }
}

// 筛选栏
.filter-section {
  background-color: #fff;
  margin-bottom: 10px;

  .filter-tabs {
    display: flex;
    white-space: nowrap;
    padding: 10px 15px;
    border-bottom: 1px solid #f0f0f0;

    .filter-tab {
      display: inline-block;
      padding: 6px 16px;
      margin-right: 10px;
      border-radius: 20px;
      font-size: 14px;
      color: #666;
      background-color: #f5f5f5;
      transition: all 0.3s;

      &.active {
        color: #fff;
        background-color: #1890ff;
      }
    }
  }

  .action-buttons {
    display: flex;
    justify-content: space-around;
    padding: 10px 15px;
    border-top: 1px solid #f0f0f0;

    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      font-size: 12px;
      color: #666;

      &.danger {
        color: #ff4d4f;
        background-color: #fff1f0;
        padding: 8px 16px;
        border-radius: 4px;
      }

      .action-text {
        margin-top: 4px;
      }
    }
  }
}

// 批量操作栏
.batch-actions {
  background-color: #fff;
  padding: 12px 15px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .batch-info {
    font-size: 14px;
    color: #666;
  }

  .batch-buttons {
    display: flex;
    gap: 10px;

    .batch-btn {
      display: flex;
      align-items: center;
      padding: 6px 12px;
      border-radius: 4px;
      background-color: #f5f5f5;
      font-size: 13px;
      color: #666;

      .batch-text {
        margin-left: 4px;
      }

      &.danger {
        background-color: #ff4d4f;
        color: #fff;
      }
    }
  }
}

// 设备列表
.device-list {
  padding: 15px;
  min-height: calc(100vh - 300px);

  .device-item {
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s;

    &:active {
      transform: scale(0.98);
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    }

    .device-checkbox {
      margin-right: 12px;
    }

    .device-info {
      flex: 1;
      overflow: hidden;

      .device-name-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .device-name {
          font-size: 16px;
          font-weight: 500;
          color: #333;
        }

        .status-badge {
          padding: 2px 8px;
          border-radius: 10px;
          font-size: 11px;

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

      .device-location {
        display: flex;
        align-items: center;
        margin-bottom: 6px;

        .location-text {
          font-size: 13px;
          color: #999;
          margin-left: 4px;
        }
      }

      .device-detail {
        display: flex;
        justify-content: space-between;

        .detail-text {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .device-arrow {
      margin-left: 8px;
    }
  }

  // 加载状态
  .loading-more {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20px 0;
    color: #999;

    .loading-text {
      margin-left: 8px;
      font-size: 14px;
    }
  }

  // 没有更多
  .no-more {
    text-align: center;
    padding: 20px 0;
    color: #999;
    font-size: 13px;
  }

  // 空状态
  .no-data {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;

    .no-data-image {
      width: 200px;
      height: 200px;
      margin-bottom: 20px;
    }

    .no-data-text {
      font-size: 14px;
      color: #999;
      margin-bottom: 20px;
    }

    .no-data-action {
      .add-device-btn {
        background-color: #1890ff;
        color: #fff;
        border: none;
        border-radius: 20px;
        padding: 8px 24px;
        font-size: 14px;
      }
    }
  }
}

// 搜索弹窗
.search-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 100px;
  z-index: 1000;

  .search-content {
    width: 80%;
    background-color: #fff;
    border-radius: 12px;
    overflow: hidden;

    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      border-bottom: 1px solid #f0f0f0;

      .search-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
    }

    .search-body {
      padding: 20px 15px;

      .search-input {
        display: flex;
        align-items: center;
        background-color: #f5f5f5;
        border-radius: 8px;
        padding: 10px 12px;
        margin-bottom: 15px;

        .input-field {
          flex: 1;
          margin-left: 8px;
          font-size: 14px;
        }
      }

      .search-btn {
        width: 100%;
        background-color: #1890ff;
        color: #fff;
        border: none;
        border-radius: 8px;
        padding: 12px;
        font-size: 15px;
      }
    }
  }
}

// 区域筛选弹窗
.area-filter-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 100px;
  z-index: 1000;

  .area-filter-content {
    width: 80%;
    max-height: 60vh;
    background-color: #fff;
    border-radius: 12px;
    overflow: hidden;

    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      border-bottom: 1px solid #f0f0f0;

      .filter-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
    }

    .filter-body {
      .area-list {
        max-height: 400px;

        .area-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px;
          border-bottom: 1px solid #f0f0f0;

          &:active {
            background-color: #f5f5f5;
          }

          &.active {
            background-color: #e6f7ff;
          }

          .area-name {
            font-size: 15px;
            color: #333;
          }
        }
      }
    }
  }
}
</style>
