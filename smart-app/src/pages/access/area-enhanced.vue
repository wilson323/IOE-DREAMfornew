<template>
  <view class="area-enhanced">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">区域管理</text>
        </view>
        <view class="navbar-right">
          <view class="navbar-icon" @click="handleAdd">
            <uni-icons type="plus" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stat-card">
        <view class="stat-value">{{ statistics.totalAreas }}</view>
        <view class="stat-label">总区域数</view>
      </view>
      <view class="stat-card">
        <view class="stat-value text-primary">{{ statistics.onlineDoors }}</view>
        <view class="stat-label">在线门禁</view>
      </view>
      <view class="stat-card">
        <view class="stat-value text-success">{{ statistics.totalPersonnel }}</view>
        <view class="stat-label">授权人员</view>
      </view>
      <view class="stat-card">
        <view class="stat-value text-warning">{{ statistics.todayPasses }}</view>
        <view class="stat-label">今日通行</view>
      </view>
    </view>

    <!-- 快速操作按钮 -->
    <view class="quick-actions">
      <view class="action-btn" @click="handleRefresh">
        <uni-icons type="refreshempty" size="18" color="#667eea"></uni-icons>
        <text>刷新</text>
      </view>
      <view class="action-btn" @click="handleSearch">
        <uni-icons type="search" size="18" color="#667eea"></uni-icons>
        <text>搜索</text>
      </view>
      <view class="action-btn" @click="handleFilter">
        <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
        <text>筛选</text>
      </view>
      <view class="action-btn" @click="handleExport">
        <uni-icons type="download" size="18" color="#667eea"></uni-icons>
        <text>导出</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tags">
      <view
        v-for="(tag, index) in filterTags"
        :key="index"
        class="filter-tag"
        :class="{ active: activeFilter === index }"
        @click="handleFilterChange(index)"
      >
        {{ tag.label }}
      </view>
    </view>

    <!-- 区域列表 -->
    <scroll-view
      class="area-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view
        v-for="area in areaList"
        :key="area.areaId"
        class="area-card"
        @click="goToDetail(area)"
      >
        <!-- 顶部渐变条 -->
        <view class="card-top-bar"></view>

        <!-- 区域头部 -->
        <view class="area-header">
          <view class="area-info">
            <view class="area-name-row">
              <text class="area-name">{{ area.areaName }}</text>
              <view
                class="area-badge"
                :class="{
                  'badge-primary': area.areaType === 1,
                  'badge-success': area.areaType === 2,
                  'badge-warning': area.areaType === 3
                }"
              >
                {{ getAreaTypeName(area.areaType) }}
              </view>
            </view>
            <text class="area-code">{{ area.areaCode }}</text>
          </view>
          <view class="area-actions">
            <view class="action-icon" @click.stop="showAreaMenu(area)">
              <uni-icons type="more-filled" size="20" color="#999"></uni-icons>
            </view>
          </view>
        </view>

        <!-- 区域信息 -->
        <view class="area-details">
          <view class="detail-row">
            <uni-icons type="location" size="14" color="#999"></uni-icons>
            <text class="detail-text">{{ area.location || '未设置位置' }}</text>
          </view>
          <view class="detail-row">
            <uni-icons type="home" size="14" color="#999"></uni-icons>
            <text class="detail-text">上级: {{ area.parentAreaName || '无' }}</text>
          </view>
        </view>

        <!-- 统计信息 -->
        <view class="area-stats">
          <view class="stat-item">
            <text class="stat-num">{{ area.doorCount || 0 }}</text>
            <text class="stat-label">门禁</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ area.personnelCount || 0 }}</text>
            <text class="stat-label">人员</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ area.onlineDoorCount || 0 }}</text>
            <text class="stat-label">在线</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ area.todayPasses || 0 }}</text>
            <text class="stat-label">今日</text>
          </view>
        </view>

        <!-- 状态指示器 -->
        <view class="area-status">
          <view
            class="status-dot"
            :class="{
              'status-online': area.status === 1,
              'status-offline': area.status === 0
            }"
          ></view>
          <text class="status-text">{{ area.status === 1 ? '正常' : '停用' }}</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore">
        <uni-icons type="spinner-cycle" size="16" color="#999"></uni-icons>
        <text>加载中...</text>
      </view>
      <view class="load-more" v-else-if="areaList.length > 0">
        <text>没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view v-if="areaList.length === 0 && !loading" class="empty-state">
        <image src="/static/images/empty-area.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无区域数据</text>
        <button class="empty-btn" @click="handleAdd">添加第一个区域</button>
      </view>
    </scroll-view>

    <!-- 批量操作栏 -->
    <view class="batch-bar" v-if="batchMode">
      <checkbox-group @change="handleSelectAll">
        <label class="select-all">
          <checkbox value="all" :checked="allSelected" />
          <text>全选</text>
        </label>
      </checkbox-group>
      <view class="batch-actions">
        <view class="batch-btn" @click="handleBatchDelete">
          <uni-icons type="trash" size="16" color="#ff4d4f"></uni-icons>
          <text>删除</text>
        </view>
        <view class="batch-btn" @click="handleBatchExport">
          <uni-icons type="download" size="16" color="#667eea"></uni-icons>
          <text>导出</text>
        </view>
      </view>
    </view>

    <!-- 区域操作菜单 -->
    <uni-popup ref="areaMenuPopup" type="bottom">
      <view class="area-menu-popup">
        <view class="menu-title">区域操作</view>
        <view class="menu-item" @click="handleViewDetail">
          <uni-icons type="eye" size="20" color="#667eea"></uni-icons>
          <text>查看详情</text>
        </view>
        <view class="menu-item" @click="handleEdit">
          <uni-icons type="compose" size="20" color="#52c41a"></uni-icons>
          <text>编辑区域</text>
        </view>
        <view class="menu-item" @click="handleManageDoors">
          <uni-icons type="home" size="20" color="#1890ff"></uni-icons>
          <text>门禁管理</text>
        </view>
        <view class="menu-item" @click="handleManagePersonnel">
          <uni-icons type="person" size="20" color="#faad14"></uni-icons>
          <text>人员管理</text>
        </view>
        <view class="menu-item" @click="handlePermissions">
          <uni-icons type="locked" size="20" color="#722ed1"></uni-icons>
          <text>权限管理</text>
        </view>
        <view class="menu-item danger" @click="handleDelete">
          <uni-icons type="trash" size="20" color="#ff4d4f"></uni-icons>
          <text>删除区域</text>
        </view>
        <view class="menu-cancel" @click="closeAreaMenu">取消</view>
      </view>
    </uni-popup>

    <!-- 搜索弹窗 -->
    <uni-popup ref="searchPopup" type="right">
      <view class="search-popup">
        <view class="search-header">
          <text class="search-title">搜索区域</text>
          <view class="search-close" @click="closeSearch">
            <uni-icons type="closeempty" size="24" color="#333"></uni-icons>
          </view>
        </view>
        <view class="search-content">
          <view class="search-input-wrapper">
            <uni-icons type="search" size="18" color="#999"></uni-icons>
            <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="请输入区域名称或编码"
              @input="onSearchInput"
            />
            <view v-if="searchKeyword" class="search-clear" @click="clearSearch">
              <uni-icons type="clear" size="16" color="#999"></uni-icons>
            </view>
          </view>
          <view class="search-history" v-if="searchHistory.length > 0 && !searchKeyword">
            <view class="history-header">
              <text class="history-title">搜索历史</text>
              <text class="history-clear" @click="clearSearchHistory">清空</text>
            </view>
            <view class="history-tags">
              <view
                v-for="(item, index) in searchHistory"
                :key="index"
                class="history-tag"
                @click="applySearchHistory(item)"
              >
                {{ item }}
              </view>
            </view>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="right">
      <view class="filter-popup">
        <view class="filter-header">
          <text class="filter-title">筛选条件</text>
          <view class="filter-close" @click="closeFilter">
            <uni-icons type="closeempty" size="24" color="#333"></uni-icons>
          </view>
        </view>
        <view class="filter-content">
          <view class="filter-section">
            <view class="filter-section-title">区域类型</view>
            <view class="filter-options">
              <view
                v-for="type in areaTypes"
                :key="type.value"
                class="filter-option"
                :class="{ active: filterParams.areaType === type.value }"
                @click="selectAreaType(type.value)"
              >
                {{ type.label }}
              </view>
            </view>
          </view>
          <view class="filter-section">
            <view class="filter-section-title">区域状态</view>
            <view class="filter-options">
              <view
                v-for="status in areaStatuses"
                :key="status.value"
                class="filter-option"
                :class="{ active: filterParams.status === status.value }"
                @click="selectStatus(status.value)"
              >
                {{ status.label }}
              </view>
            </view>
          </view>
          <view class="filter-section">
            <view class="filter-section-title">上级区域</view>
            <picker mode="selector" :range="parentAreas" range-key="areaName" @change="onParentAreaChange">
              <view class="picker-value">
                {{ selectedParentName || '请选择上级区域' }}
                <uni-icons type="arrowright" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
        <view class="filter-footer">
          <view class="filter-btn filter-btn-reset" @click="resetFilter">重置</view>
          <view class="filter-btn filter-btn-confirm" @click="applyFilter">确定</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'

// 状态栏高度
const statusBarHeight = ref(0)

// 统计数据
const statistics = reactive({
  totalAreas: 0,
  onlineDoors: 0,
  totalPersonnel: 0,
  todayPasses: 0
})

// 区域列表
const areaList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)

// 筛选标签
const filterTags = [
  { label: '全部', value: null },
  { label: '办公区', value: 1 },
  { label: '生产区', value: 2 },
  { label: '公共区', value: 3 }
]
const activeFilter = ref(0)

// 批量操作
const batchMode = ref(false)
const selectedIds = ref([])
const allSelected = ref(false)

// 当前选中的区域
const currentArea = ref(null)

// 搜索
const searchKeyword = ref('')
const searchHistory = ref([])

// 筛选参数
const filterParams = reactive({
  areaType: null,
  status: null,
  parentAreaId: null
})

// 区域类型
const areaTypes = [
  { label: '全部', value: null },
  { label: '办公区', value: 1 },
  { label: '生产区', value: 2 },
  { label: '公共区', value: 3 }
]

// 区域状态
const areaStatuses = [
  { label: '全部', value: null },
  { label: '正常', value: 1 },
  { label: '停用', value: 0 }
]

// 上级区域列表
const parentAreas = ref([])
const selectedParentName = ref('')

// 弹窗引用
const areaMenuPopup = ref(null)
const searchPopup = ref(null)
const filterPopup = ref(null)

// 获取状态栏高度
onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  loadStatistics()
  loadAreaList()

  // 加载搜索历史
  const history = uni.getStorageSync('area_search_history')
  if (history) {
    searchHistory.value = JSON.parse(history)
  }
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAreaStatistics()
    // Object.assign(statistics, res.data)

    // 模拟数据
    statistics.totalAreas = 12
    statistics.onlineDoors = 45
    statistics.totalPersonnel = 238
    statistics.todayPasses = 567
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载区域列表
const loadAreaList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    // TODO: 调用实际的API
    // const params = {
    //   pageNum: pageNum.value,
    //   pageSize: pageSize.value,
    //   areaType: filterParams.areaType,
    //   status: filterParams.status,
    //   parentAreaId: filterParams.parentAreaId,
    //   keyword: searchKeyword.value
    // }
    // const res = await areaApi.getAreaList(params)

    // 模拟数据
    const mockData = [
      {
        areaId: 1,
        areaName: 'A栋办公楼',
        areaCode: 'AREA-A-001',
        areaType: 1,
        location: 'A栋1-5层',
        parentAreaName: '总部园区',
        doorCount: 8,
        personnelCount: 86,
        onlineDoorCount: 7,
        todayPasses: 234,
        status: 1
      },
      {
        areaId: 2,
        areaName: 'B栋生产车间',
        areaCode: 'AREA-B-001',
        areaType: 2,
        location: 'B栋1-3层',
        parentAreaName: '总部园区',
        doorCount: 12,
        personnelCount: 152,
        onlineDoorCount: 11,
        todayPasses: 333,
        status: 1
      }
    ]

    if (isRefresh) {
      areaList.value = mockData
      pageNum.value = 1
    } else {
      areaList.value.push(...mockData)
    }

    hasMore.value = mockData.length >= pageSize.value
  } catch (error) {
    console.error('加载区域列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  pageNum.value = 1
  loadAreaList(true).then(() => {
    setTimeout(() => {
      refreshing.value = false
    }, 500)
  })
}

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loading.value) return
  pageNum.value++
  loadAreaList()
}

// 下拉刷新（页面生命周期）
onPullDownRefresh(() => {
  onRefresh()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})

// 上拉加载（页面生命周期）
onReachBottom(() => {
  loadMore()
})

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 添加区域
const handleAdd = () => {
  uni.navigateTo({
    url: '/pages/access/area-add'
  })
}

// 刷新
const handleRefresh = () => {
  onRefresh()
}

// 搜索
const handleSearch = () => {
  searchPopup.value.open()
}

// 筛选
const handleFilter = () => {
  filterPopup.value.open()
}

// 导出
const handleExport = () => {
  uni.showToast({
    title: '导出功能开发中',
    icon: 'none'
  })
}

// 筛选标签切换
const handleFilterChange = (index) => {
  activeFilter.value = index
  const tag = filterTags[index]
  filterParams.areaType = tag.value
  onRefresh()
}

// 查看详情
const goToDetail = (area) => {
  uni.navigateTo({
    url: `/pages/access/area-detail?areaId=${area.areaId}`
  })
}

// 显示区域菜单
const showAreaMenu = (area) => {
  currentArea.value = area
  areaMenuPopup.value.open()
}

// 关闭区域菜单
const closeAreaMenu = () => {
  areaMenuPopup.value.close()
  currentArea.value = null
}

// 查看详情
const handleViewDetail = () => {
  closeAreaMenu()
  goToDetail(currentArea.value)
}

// 编辑区域
const handleEdit = () => {
  closeAreaMenu()
  uni.navigateTo({
    url: `/pages/access/area-add?areaId=${currentArea.value.areaId}`
  })
}

// 门禁管理
const handleManageDoors = () => {
  closeAreaMenu()
  uni.navigateTo({
    url: `/pages/access/area-doors?areaId=${currentArea.value.areaId}`
  })
}

// 人员管理
const handleManagePersonnel = () => {
  closeAreaMenu()
  uni.navigateTo({
    url: `/pages/access/area-personnel?areaId=${currentArea.value.areaId}`
  })
}

// 权限管理
const handlePermissions = () => {
  closeAreaMenu()
  uni.navigateTo({
    url: `/pages/access/area-permissions?areaId=${currentArea.value.areaId}`
  })
}

// 删除区域
const handleDelete = () => {
  closeAreaMenu()
  uni.showModal({
    title: '确认删除',
    content: `确定要删除区域"${currentArea.value.areaName}"吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用删除API
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
        onRefresh()
      }
    }
  })
}

// 搜索输入
const onSearchInput = () => {
  // 实时搜索
  onRefresh()
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  onRefresh()
}

// 关闭搜索
const closeSearch = () => {
  searchPopup.value.close()
}

// 应用搜索历史
const applySearchHistory = (keyword) => {
  searchKeyword.value = keyword
  onRefresh()
  closeSearch()
}

// 清空搜索历史
const clearSearchHistory = () => {
  searchHistory.value = []
  uni.removeStorageSync('area_search_history')
}

// 保存搜索历史
const saveSearchHistory = (keyword) => {
  if (!keyword || keyword.trim() === '') return

  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
  }
  searchHistory.value.unshift(keyword)

  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10)
  }

  uni.setStorageSync('area_search_history', JSON.stringify(searchHistory.value))
}

// 选择区域类型
const selectAreaType = (value) => {
  filterParams.areaType = value
}

// 选择状态
const selectStatus = (value) => {
  filterParams.status = value
}

// 上级区域变更
const onParentAreaChange = (e) => {
  const index = e.detail.value
  const area = parentAreas.value[index]
  filterParams.parentAreaId = area.areaId
  selectedParentName.value = area.areaName
}

// 重置筛选
const resetFilter = () => {
  filterParams.areaType = null
  filterParams.status = null
  filterParams.parentAreaId = null
  selectedParentName.value = ''
}

// 应用筛选
const applyFilter = () => {
  closeFilter()
  onRefresh()
}

// 关闭筛选
const closeFilter = () => {
  filterPopup.value.close()
}

// 全选
const handleSelectAll = (e) => {
  const checked = e.detail.value.includes('all')
  allSelected.value = checked
  if (checked) {
    selectedIds.value = areaList.value.map(item => item.areaId)
  } else {
    selectedIds.value = []
  }
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    uni.showToast({
      title: '请选择要删除的区域',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认删除',
    content: `确定要删除选中的${selectedIds.value.length}个区域吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用批量删除API
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
        batchMode.value = false
        selectedIds.value = []
        onRefresh()
      }
    }
  })
}

// 批量导出
const handleBatchExport = () => {
  if (selectedIds.value.length === 0) {
    uni.showToast({
      title: '请选择要导出的区域',
      icon: 'none'
    })
    return
  }

  uni.showToast({
    title: '导出功能开发中',
    icon: 'none'
  })
}

// 获取区域类型名称
const getAreaTypeName = (type) => {
  const typeMap = {
    1: '办公区',
    2: '生产区',
    3: '公共区'
  }
  return typeMap[type] || '未知'
}
</script>

<style lang="scss" scoped>
.area-enhanced {
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
    display: flex;
    gap: 20rpx;

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

// 统计卡片
.stats-section {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  margin-top: 158rpx;

  .stat-card {
    flex: 1;
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx 20rpx;
    text-align: center;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .stat-value {
      font-size: 44rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 10rpx;

      &.text-primary {
        color: #667eea;
      }

      &.text-success {
        color: #52c41a;
      }

      &.text-warning {
        color: #faad14;
      }
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
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
    justify-content: center;
    background: #fff;
    border-radius: 16rpx;
    padding: 20rpx 0;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    font-size: 24rpx;
    color: #666;
    gap: 10rpx;
  }
}

// 筛选标签
.filter-tags {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 20rpx;
  overflow-x: auto;

  .filter-tag {
    flex-shrink: 0;
    padding: 12rpx 32rpx;
    background: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
    color: #666;
    border: 2rpx solid transparent;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
    }
  }
}

// 区域列表
.area-list {
  padding: 0 30rpx;
  height: calc(100vh - 500rpx);
}

.area-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
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
    align-items: flex-start;
    margin-bottom: 20rpx;

    .area-info {
      flex: 1;

      .area-name-row {
        display: flex;
        align-items: center;
        gap: 16rpx;
        margin-bottom: 10rpx;

        .area-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
        }

        .area-badge {
          padding: 6rpx 16rpx;
          border-radius: 8rpx;
          font-size: 22rpx;
          color: #fff;

          &.badge-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.badge-success {
            background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
          }

          &.badge-warning {
            background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
          }
        }
      }

      .area-code {
        font-size: 24rpx;
        color: #999;
      }
    }

    .area-actions {
      .action-icon {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .area-details {
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

  .area-stats {
    display: flex;
    gap: 30rpx;
    margin-bottom: 20rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-num {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 6rpx;
      }

      .stat-label {
        font-size: 22rpx;
        color: #999;
      }
    }
  }

  .area-status {
    display: flex;
    align-items: center;
    gap: 10rpx;

    .status-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;

      &.status-online {
        background: #52c41a;
      }

      &.status-offline {
        background: #d9d9d9;
      }
    }

    .status-text {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 加载更多
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  padding: 30rpx 0;
  font-size: 24rpx;
  color: #999;
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

// 批量操作栏
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

  .select-all {
    display: flex;
    align-items: center;
    gap: 10rpx;
    font-size: 28rpx;
    color: #333;
  }

  .batch-actions {
    display: flex;
    gap: 20rpx;

    .batch-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 32rpx;
      background: #f5f7fa;
      border-radius: 40rpx;
      font-size: 26rpx;
      color: #666;
    }
  }
}

// 区域操作菜单
.area-menu-popup {
  padding: 30rpx;

  .menu-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    text-align: center;
    margin-bottom: 40rpx;
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 30rpx;
    background: #f5f7fa;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    font-size: 30rpx;
    color: #333;

    &.danger {
      color: #ff4d4f;
    }
  }

  .menu-cancel {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 30rpx;
    background: #f5f7fa;
    border-radius: 16rpx;
    margin-top: 20rpx;
    font-size: 30rpx;
    color: #666;
  }
}

// 搜索弹窗
.search-popup {
  width: 600rpx;
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;

  .search-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .search-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .search-content {
    flex: 1;
    padding: 30rpx;

    .search-input-wrapper {
      display: flex;
      align-items: center;
      gap: 10rpx;
      padding: 20rpx 30rpx;
      background: #f5f7fa;
      border-radius: 40rpx;
      margin-bottom: 40rpx;

      .search-input {
        flex: 1;
        font-size: 28rpx;
      }
    }

    .search-history {
      .history-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;

        .history-title {
          font-size: 28rpx;
          color: #999;
        }

        .history-clear {
          font-size: 26rpx;
          color: #667eea;
        }
      }

      .history-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;

        .history-tag {
          padding: 12rpx 24rpx;
          background: #f5f7fa;
          border-radius: 8rpx;
          font-size: 26rpx;
          color: #666;
        }
      }
    }
  }
}

// 筛选弹窗
.filter-popup {
  width: 600rpx;
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .filter-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .filter-content {
    flex: 1;
    padding: 30rpx;
    overflow-y: auto;

    .filter-section {
      margin-bottom: 40rpx;

      .filter-section-title {
        font-size: 28rpx;
        color: #999;
        margin-bottom: 20rpx;
      }

      .filter-options {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;

        .filter-option {
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

      .picker-value {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 30rpx;
        background: #f5f7fa;
        border-radius: 12rpx;
        font-size: 28rpx;
        color: #333;
      }
    }
  }

  .filter-footer {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 2rpx solid #f0f0f0;

    .filter-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24rpx;
      border-radius: 12rpx;
      font-size: 28rpx;

      &.filter-btn-reset {
        background: #f5f7fa;
        color: #666;
      }

      &.filter-btn-confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}
</style>
