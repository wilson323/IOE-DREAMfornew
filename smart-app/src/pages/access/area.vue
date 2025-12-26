<template>
  <view class="area-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">区域管理</view>
      <view class="nav-right" @click="addArea">
        <uni-icons type="plus" size="20" color="#1890ff"></uni-icons>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stats-item">
        <text class="stats-value">{{ totalCount }}</text>
        <text class="stats-label">总区域</text>
      </view>
      <view class="stats-divider"></view>
      <view class="stats-item">
        <text class="stats-value online">{{ enabledCount }}</text>
        <text class="stats-label">已启用</text>
      </view>
      <view class="stats-divider"></view>
      <view class="stats-item">
        <text class="stats-value">{{ deviceCount }}</text>
        <text class="stats-label">关联设备</text>
      </view>
    </view>

    <!-- 操作栏 -->
    <view class="action-bar">
      <view class="search-box" @click="openSearchModal">
        <uni-icons type="search" size="16" color="#999"></uni-icons>
        <text class="search-placeholder">搜索区域名称</text>
      </view>
      <view class="filter-btn" @click="openFilterModal">
        <uni-icons type="filter" size="18" color="#666"></uni-icons>
        <text class="filter-text">筛选</text>
      </view>
    </view>

    <!-- 区域树列表 -->
    <view class="area-tree">
      <view
        class="area-node"
        v-for="area in filteredAreaList"
        :key="area.areaId"
      >
        <!-- 父节点 -->
        <view
          class="node-item"
          :class="{ expanded: area.expanded }"
          @click="toggleExpand(area)"
        >
          <view class="node-left">
            <uni-icons
              :type="area.expanded ? 'down' : 'right'"
              size="16"
              color="#999"
              v-if="area.children && area.children.length > 0"
            ></uni-icons>
            <view class="node-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <uni-icons type="home" size="18" color="#fff"></uni-icons>
            </view>
            <view class="node-info">
              <text class="node-name">{{ area.areaName }}</text>
              <text class="node-path">{{ area.areaPath || area.areaName }}</text>
            </view>
          </view>
          <view class="node-right">
            <view class="device-badge">
              <text class="device-count">{{ area.deviceCount || 0 }}</text>
              <text class="device-label">设备</text>
            </view>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>

        <!-- 子节点列表 -->
        <view class="child-nodes" v-if="area.expanded && area.children && area.children.length > 0">
          <view
            class="child-node"
            v-for="child in area.children"
            :key="child.areaId"
            @click="viewAreaDetail(child)"
          >
            <view class="child-left">
              <view class="child-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <uni-icons type="paperplane" size="14" color="#fff"></uni-icons>
              </view>
              <view class="child-info">
                <text class="child-name">{{ child.areaName }}</text>
                <text class="child-location">{{ child.location || '未设置位置' }}</text>
              </view>
            </view>
            <view class="child-right">
              <view class="device-badge small">
                <text class="device-count">{{ child.deviceCount || 0 }}</text>
              </view>
              <uni-icons type="right" size="12" color="#d9d9d9"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredAreaList.length === 0 && !loading">
        <image class="empty-icon" src="/static/images/empty.png" mode="aspectFit"></image>
        <text class="empty-text">暂无区域数据</text>
        <button class="empty-btn" @click="addArea">新增区域</button>
      </view>
    </view>

    <!-- 搜索弹窗 -->
    <uni-popup ref="searchPopup" type="top">
      <view class="search-modal">
        <view class="search-input-box">
          <uni-icons type="search" size="18" color="#999"></uni-icons>
          <input
            class="search-input"
            type="text"
            placeholder="搜索区域名称"
            v-model="searchKeyword"
            @input="onSearchInput"
            @confirm="doSearch"
          />
          <text class="search-clear" v-if="searchKeyword" @click="clearSearch">清除</text>
        </view>
        <view class="search-actions">
          <text class="search-cancel" @click="closeSearchModal">取消</text>
        </view>
      </view>
    </uni-popup>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom">
      <view class="filter-container">
        <view class="filter-header">
          <text class="filter-title">筛选条件</text>
          <view class="filter-close" @click="closeFilterModal">
            <uni-icons type="close" size="18" color="#999"></uni-icons>
          </view>
        </view>
        <view class="filter-content">
          <!-- 区域类型 -->
          <view class="filter-section">
            <view class="filter-label">区域类型</view>
            <view class="filter-tags">
              <view
                class="filter-tag"
                :class="{ active: filterType === '' }"
                @click="selectFilterType('')"
              >
                全部
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterType === 'building' }"
                @click="selectFilterType('building')"
              >
                楼宇
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterType === 'floor' }"
                @click="selectFilterType('floor')"
              >
                楼层
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterType === 'room' }"
                @click="selectFilterType('room')"
              >
                房间
              </view>
            </view>
          </view>

          <!-- 状态筛选 -->
          <view class="filter-section">
            <view class="filter-label">区域状态</view>
            <view class="filter-tags">
              <view
                class="filter-tag"
                :class="{ active: filterStatus === '' }"
                @click="selectFilterStatus('')"
              >
                全部
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterStatus === '1' }"
                @click="selectFilterStatus('1')"
              >
                已启用
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterStatus === '0' }"
                @click="selectFilterStatus('0')"
              >
                已禁用
              </view>
            </view>
          </view>
        </view>
        <view class="filter-footer">
          <button class="filter-btn reset" @click="resetFilter">重置</button>
          <button class="filter-btn confirm" @click="applyFilter">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 响应式数据
const areaList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)
const hasMore = ref(true)

// 统计数据
const enabledCount = ref(0)
const deviceCount = ref(0)

// 搜索和筛选
const searchPopup = ref(null)
const filterPopup = ref(null)
const searchKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')

// 过滤后的区域列表
const filteredAreaList = computed(() => {
  let list = areaList.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(area => {
      const matchName = area.areaName && area.areaName.toLowerCase().includes(keyword)
      const matchPath = area.areaPath && area.areaPath.toLowerCase().includes(keyword)
      return matchName || matchPath
    })
  }

  // 类型过滤
  if (filterType.value) {
    list = list.filter(area => area.areaType === filterType.value)
  }

  // 状态过滤
  if (filterStatus.value !== '') {
    list = list.filter(area => area.enabled === parseInt(filterStatus.value))
  }

  return list
})

// 页面生命周期
onMounted(() => {
  loadAreaList()
})

onShow(() => {
  // 页面显示时刷新列表
  loadAreaList()
})

onPullDownRefresh(() => {
  pageNum.value = 1
  hasMore.value = true
  loadAreaList(true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    pageNum.value++
    loadAreaList()
  }
})

/**
 * 加载区域列表
 */
const loadAreaList = async (refresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    const result = await accessApi.getAreaList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })

    if (result.success && result.data) {
      const areas = (result.data.list || []).map(area => ({
        ...area,
        expanded: false,
        children: area.children || []
      }))

      if (refresh || pageNum.value === 1) {
        areaList.value = areas
      } else {
        areaList.value.push(...areas)
      }

      totalCount.value = result.data.total || 0
      hasMore.value = areas.length >= pageSize.value

      // 计算统计数据
      updateStatistics()
    } else {
      uni.showToast({
        title: result.message || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载区域列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 更新统计数据
 */
const updateStatistics = () => {
  enabledCount.value = areaList.value.filter(a => a.enabled).length
  deviceCount.value = areaList.value.reduce((sum, area) => sum + (area.deviceCount || 0), 0)
}

/**
 * 展开/收起子节点
 */
const toggleExpand = (area) => {
  if (area.children && area.children.length > 0) {
    area.expanded = !area.expanded
  } else {
    // 没有子节点，直接进入详情
    viewAreaDetail(area)
  }
}

/**
 * 查看区域详情
 */
const viewAreaDetail = (area) => {
  uni.navigateTo({
    url: `/pages/access/area-detail?id=${area.areaId}`
  })
}

/**
 * 新增区域
 */
const addArea = () => {
  uni.navigateTo({
    url: '/pages/access/area-add'
  })
}

/**
 * 打开搜索弹窗
 */
const openSearchModal = () => {
  searchPopup.value.open()
}

/**
 * 关闭搜索弹窗
 */
const closeSearchModal = () => {
  searchPopup.value.close()
}

/**
 * 搜索输入
 */
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
}

/**
 * 执行搜索
 */
const doSearch = () => {
  closeSearchModal()
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
}

/**
 * 打开筛选弹窗
 */
const openFilterModal = () => {
  filterPopup.value.open()
}

/**
 * 关闭筛选弹窗
 */
const closeFilterModal = () => {
  filterPopup.value.close()
}

/**
 * 选择筛选类型
 */
const selectFilterType = (type) => {
  filterType.value = type
}

/**
 * 选择筛选状态
 */
const selectFilterStatus = (status) => {
  filterStatus.value = status
}

/**
 * 应用筛选
 */
const applyFilter = () => {
  closeFilterModal()
}

/**
 * 重置筛选
 */
const resetFilter = () => {
  filterType.value = ''
  filterStatus.value = ''
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.area-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 30px;
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

  .nav-right {
    cursor: pointer;
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
.stats-card {
  display: flex;
  align-items: center;
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .stats-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .stats-value {
      font-size: 24px;
      font-weight: bold;
      color: #fff;
      margin-bottom: 6px;

      &.online {
        color: #52c41a;
      }
    }

    .stats-label {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .stats-divider {
    width: 1px;
    height: 30px;
    background-color: rgba(255, 255, 255, 0.3);
  }
}

// 操作栏
.action-bar {
  display: flex;
  align-items: center;
  padding: 0 15px 15px;
  gap: 12px;

  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    height: 40px;
    padding: 0 12px;
    background-color: #fff;
    border-radius: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .search-placeholder {
      flex: 1;
      margin-left: 8px;
      font-size: 14px;
      color: #999;
    }
  }

  .filter-btn {
    display: flex;
    align-items: center;
    padding: 0 16px;
    height: 40px;
    background-color: #fff;
    border-radius: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .filter-text {
      margin-left: 6px;
      font-size: 14px;
      color: #666;
    }
  }
}

// 区域树列表
.area-tree {
  padding: 0 15px;

  .area-node {
    margin-bottom: 12px;
  }

  .node-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s;

    &:active {
      transform: scale(0.98);
    }

    .node-left {
      flex: 1;
      display: flex;
      align-items: center;
      overflow: hidden;

      .node-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        flex-shrink: 0;
      }

      .node-info {
        flex: 1;
        overflow: hidden;

        .node-name {
          display: block;
          font-size: 16px;
          font-weight: 500;
          color: #333;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .node-path {
          display: block;
          font-size: 12px;
          color: #999;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .node-right {
      display: flex;
      align-items: center;

      .device-badge {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 6px 10px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 8px;
        margin-right: 8px;

        .device-count {
          font-size: 16px;
          font-weight: bold;
          color: #fff;
          line-height: 1;
          margin-bottom: 2px;
        }

        .device-label {
          font-size: 10px;
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }

  // 子节点列表
  .child-nodes {
    margin-top: 8px;
    background-color: #fff;
    border-radius: 12px;
    padding: 8px 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .child-node {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      &:first-child {
        padding-top: 0;
      }

      .child-left {
        flex: 1;
        display: flex;
        align-items: center;
        overflow: hidden;

        .child-icon {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 10px;
          flex-shrink: 0;
        }

        .child-info {
          flex: 1;
          overflow: hidden;

          .child-name {
            display: block;
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .child-location {
            display: block;
            font-size: 12px;
            color: #999;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }

      .child-right {
        display: flex;
        align-items: center;

        .device-badge {
          display: flex;
          align-items: center;
          padding: 4px 8px;
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          border-radius: 6px;
          margin-right: 6px;

          &.small {
            padding: 4px 8px;
          }

          .device-count {
            font-size: 12px;
            font-weight: bold;
            color: #fff;
            line-height: 1;
          }
        }
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;

  .empty-icon {
    width: 120px;
    height: 120px;
    margin-bottom: 20px;
  }

  .empty-text {
    font-size: 15px;
    color: #999;
    margin-bottom: 30px;
  }

  .empty-btn {
    width: 120px;
    height: 44px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 22px;
    color: #fff;
    font-size: 15px;
    border: none;
  }
}

// 搜索弹窗
.search-modal {
  background-color: #fff;
  padding: 15px;
  border-bottom: 1px solid #eee;

  .search-input-box {
    display: flex;
    align-items: center;
    height: 40px;
    padding: 0 12px;
    background-color: #f5f5f5;
    border-radius: 20px;

    .search-input {
      flex: 1;
      height: 100%;
      margin-left: 8px;
      font-size: 15px;
      border: none;
      background: transparent;
    }

    .search-clear {
      font-size: 14px;
      color: #1890ff;
      margin-left: 8px;
    }
  }

  .search-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;

    .search-cancel {
      font-size: 15px;
      color: #666;
    }
  }
}

// 筛选弹窗
.filter-container {
  background-color: #fff;
  border-radius: 20px 20px 0 0;
  padding: 20px;

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .filter-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .filter-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 16px;
    }
  }

  .filter-content {
    .filter-section {
      margin-bottom: 25px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-label {
        font-size: 15px;
        color: #333;
        margin-bottom: 12px;
        font-weight: 500;
      }

      .filter-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .filter-tag {
          padding: 8px 16px;
          background-color: #f5f5f5;
          border-radius: 20px;
          font-size: 14px;
          color: #666;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
          }

          &:active {
            transform: scale(0.95);
          }
        }
      }
    }
  }

  .filter-footer {
    display: flex;
    gap: 12px;
    margin-top: 20px;

    .filter-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      border: none;

      &.reset {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}
</style>
