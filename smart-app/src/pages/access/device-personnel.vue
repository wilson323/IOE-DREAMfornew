<template>
  <view class="device-personnel-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="nav-back" @click="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">设备人员</text>
        <view class="nav-action" @click="showBatchActions">
          <text class="action-text">管理</text>
        </view>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <uni-icons type="search" size="16" color="#999"></uni-icons>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索人员姓名、工号"
          @input="onSearchInput"
        />
        <uni-icons
          v-if="searchKeyword"
          type="clear"
          size="16"
          color="#999"
          @click="clearSearch"
        ></uni-icons>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="stat-card">
        <text class="stat-value">{{ personnelList.length || 0 }}</text>
        <text class="stat-label">总人数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ faceCount || 0 }}</text>
        <text class="stat-label">人脸</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ fingerprintCount || 0 }}</text>
        <text class="stat-label">指纹</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ cardCount || 0 }}</text>
        <text class="stat-label">卡片</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        :class="['filter-tab', { active: activeTab === tab.value }]"
        @click="switchTab(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <text v-if="tab.count !== undefined" class="tab-count">({{ tab.count }})</text>
      </view>
    </view>

    <!-- 人员列表 -->
    <scroll-view
      scroll-y
      class="personnel-list"
      :style="{ paddingTop: navbarHeight + 180 + 'rpx' }"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && personnelList.length === 0" class="loading-state">
        <uni-load-more status="loading"></uni-load-more>
      </view>

      <view v-else-if="personnelList.length === 0" class="empty-state">
        <image src="/static/images/empty.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无人员数据</text>
      </view>

      <view v-else>
        <view
          v-for="person in filteredList"
          :key="person.userId"
          :class="['personnel-item', { selected: selectedIds.includes(person.userId) }]"
          @click="toggleSelect(person.userId)"
        >
          <view class="personnel-header">
            <view class="personnel-info">
              <view class="avatar-wrap">
                <image
                  v-if="person.avatar"
                  :src="person.avatar"
                  class="avatar"
                  mode="aspectFill"
                ></image>
                <uni-icons v-else type="person" size="32" color="#999"></uni-icons>
              </view>
              <view class="info-wrap">
                <text class="person-name">{{ person.userName || '-' }}</text>
                <text class="person-code">工号: {{ person.userCode || '-' }}</text>
              </view>
            </view>
            <view class="select-checkbox">
              <uni-icons
                :type="selectedIds.includes(person.userId) ? 'checkbox' : 'circle'"
                :color="selectedIds.includes(person.userId) ? '#667eea' : '#d9d9d9'"
                size="24"
              ></uni-icons>
            </view>
          </view>

          <view class="personnel-details">
            <view class="detail-tag" v-if="person.hasFace">
              <uni-icons type="person" size="12" color="#667eea"></uni-icons>
              <text class="tag-text">人脸</text>
            </view>
            <view class="detail-tag" v-if="person.hasFingerprint">
              <uni-icons type="scan" size="12" color="#1890ff"></uni-icons>
              <text class="tag-text">指纹</text>
            </view>
            <view class="detail-tag" v-if="person.hasCard">
              <uni-icons type="contact" size="12" color="#52c41a"></uni-icons>
              <text class="tag-text">卡片</text>
            </view>
            <view class="detail-tag" v-if="person.hasPassword">
              <uni-icons type="locked" size="12" color="#faad14"></uni-icons>
              <text class="tag-text">密码</text>
            </view>
          </view>

          <view class="personnel-footer">
            <text class="add-time">
              添加时间: {{ person.addTime ? formatDate(person.addTime) : '-' }}
            </text>
            <view class="footer-actions" @click.stop>
              <view class="action-btn" @click="removePersonnel(person)">
                <uni-icons type="trash" size="14" color="#ff4d4f"></uni-icons>
                <text class="action-text">移除</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="load-more">
          <uni-load-more :status="loadingMore ? 'loading' : 'more'"></uni-load-more>
        </view>

        <view v-else-if="personnelList.length > 0" class="no-more">
          <text class="no-more-text">没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部批量操作栏 -->
    <view v-if="selectedIds.length > 0" class="batch-actions">
      <view class="selected-info">
        <text class="selected-text">已选择 {{ selectedIds.length }} 人</text>
      </view>
      <view class="batch-buttons">
        <button class="batch-btn danger" @click="batchRemove">批量移除</button>
        <button class="batch-btn secondary" @click="clearSelection">取消</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'
import dayjs from 'dayjs'

// 导航栏高度
const navbarHeight = ref(44)

// 设备ID
const deviceId = ref('')

// 人员列表
const personnelList = ref([])

// 搜索关键词
const searchKeyword = ref('')

// 当前激活的标签
const activeTab = ref('all')

// 筛选标签
const filterTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '人脸', value: 'face', count: 0 },
  { label: '指纹', value: 'fingerprint', count: 0 },
  { label: '卡片', value: 'card', count: 0 }
])

// 选中的ID
const selectedIds = ref([])

// 加载状态
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(false)

// 分页参数
const pageNum = ref(1)
const pageSize = ref(20)

// 统计数据
const faceCount = ref(0)
const fingerprintCount = ref(0)
const cardCount = ref(0)

// 过滤后的列表
const filteredList = computed(() => {
  let list = personnelList.value

  // 按标签筛选
  if (activeTab.value === 'face') {
    list = list.filter(p => p.hasFace)
  } else if (activeTab.value === 'fingerprint') {
    list = list.filter(p => p.hasFingerprint)
  } else if (activeTab.value === 'card') {
    list = list.filter(p => p.hasCard)
  }

  // 按关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(p => {
      const name = (p.userName || '').toLowerCase()
      const code = (p.userCode || '').toLowerCase()
      return name.includes(keyword) || code.includes(keyword)
    })
  }

  return list
})

// 搜索输入（防抖）
let searchTimer = null
const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    // 搜索逻辑在computed中处理
  }, 300)
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
}

// 切换选中状态
const toggleSelect = (userId) => {
  const index = selectedIds.value.indexOf(userId)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(userId)
  }
}

// 显示批量操作
const showBatchActions = () => {
  if (selectedIds.value.length === 0) {
    uni.showToast({ title: '请先选择人员', icon: 'none' })
    return
  }
}

// 清除选择
const clearSelection = () => {
  selectedIds.value = []
}

// 移除单个人员
const removePersonnel = (person) => {
  uni.showModal({
    title: '确认移除',
    content: `确定要从设备移除"${person.userName}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '移除中...' })
          // TODO: 调用移除人员API
          uni.hideLoading()
          uni.showToast({ title: '移除成功', icon: 'success' })
          loadPersonnel(true)
        } catch (error) {
          uni.hideLoading()
          uni.showToast({ title: '移除失败', icon: 'none' })
        }
      }
    }
  })
}

// 批量移除
const batchRemove = () => {
  uni.showModal({
    title: '批量移除',
    content: `确定要移除选中的 ${selectedIds.value.length} 人吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '移除中...' })
          // TODO: 调用批量移除API
          await new Promise(resolve => setTimeout(resolve, 1000))
          uni.hideLoading()
          uni.showToast({ title: '移除成功', icon: 'success' })
          selectedIds.value = []
          loadPersonnel(true)
        } catch (error) {
          uni.hideLoading()
          uni.showToast({ title: '移除失败', icon: 'none' })
        }
      }
    }
  })
}

// 加载人员列表
const loadPersonnel = async (refresh = false) => {
  if (refresh) {
    pageNum.value = 1
    personnelList.value = []
  }

  if (loading.value) return
  loading.value = true

  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    const res = await deviceApi.getDevicePersonnel(deviceId.value, params)

    if (res.code === 200 && res.data) {
      const list = res.data.list || []

      if (refresh) {
        personnelList.value = list
      } else {
        personnelList.value.push(...list)
      }

      hasMore.value = list.length >= pageSize.value

      // 更新统计
      updateStatistics()
    }
  } catch (error) {
    console.error('加载人员列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return

  loadingMore.value = true
  pageNum.value++

  loadPersonnel().finally(() => {
    loadingMore.value = false
  })
}

// 更新统计数据
const updateStatistics = () => {
  const list = personnelList.value

  faceCount.value = list.filter(p => p.hasFace).length
  fingerprintCount.value = list.filter(p => p.hasFingerprint).length
  cardCount.value = list.filter(p => p.hasCard).length

  // 更新标签计数
  filterTabs.value[0].count = list.length
  filterTabs.value[1].count = faceCount.value
  filterTabs.value[2].count = fingerprintCount.value
  filterTabs.value[3].count = cardCount.value
}

// 格式化日期
const formatDate = (dateStr) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  navbarHeight.value = systemInfo.statusBarHeight + 44

  // 获取设备ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  if (options.deviceId) {
    deviceId.value = options.deviceId
    loadPersonnel(true)
  }
})
</script>

<style lang="scss" scoped>
.device-personnel-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 30rpx;

    .nav-back {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .nav-title {
      font-size: 36rpx;
      font-weight: 600;
      color: white;
    }

    .nav-action {
      .action-text {
        font-size: 28rpx;
        color: white;
        font-weight: 500;
      }
    }
  }
}

// 搜索栏
.search-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  padding: 20rpx 30rpx;
  padding-top: calc(44px + var(--status-bar-height));
  background: white;

  .search-input-wrap {
    display: flex;
    align-items: center;
    background: #f6f8fb;
    border-radius: 40rpx;
    padding: 16rpx 24rpx;

    .search-input {
      flex: 1;
      font-size: 28rpx;
      color: #333;
      margin: 0 16rpx;
    }
  }
}

// 统计卡片
.stats-cards {
  position: fixed;
  top: calc(44px + var(--status-bar-height) + 80rpx);
  left: 0;
  right: 0;
  z-index: 998;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  background: white;

  .stat-card {
    flex: 1;
    background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
    border-radius: 16rpx;
    padding: 20rpx;
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-value {
      font-size: 32rpx;
      font-weight: 700;
      color: #667eea;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 筛选标签
.filter-tabs {
  position: fixed;
  top: calc(44px + var(--status-bar-height) + 180rpx);
  left: 0;
  right: 0;
  z-index: 997;
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 20rpx;
  background: white;

  .filter-tab {
    display: flex;
    align-items: center;
    padding: 12rpx 24rpx;
    background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
    border-radius: 40rpx;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      .tab-text {
        color: white;
      }

      .tab-count {
        color: white;
      }
    }

    .tab-text {
      font-size: 28rpx;
      color: #666;
    }

    .tab-count {
      font-size: 24rpx;
      color: #999;
      margin-left: 8rpx;
    }
  }
}

// 人员列表
.personnel-list {
  height: 100vh;
  padding: 0 30rpx 20rpx;
}

// 人员项
.personnel-item {
  background: white;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;

  &.selected {
    border: 2rpx solid #667eea;
    box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
  }

  .personnel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .personnel-info {
      flex: 1;
      display: flex;
      align-items: center;

      .avatar-wrap {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        background: #f0f0f0;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
        overflow: hidden;

        .avatar {
          width: 100%;
          height: 100%;
        }
      }

      .info-wrap {
        .person-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
          display: block;
          margin-bottom: 8rpx;
        }

        .person-code {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }

  .personnel-details {
    display: flex;
    gap: 16rpx;
    margin-bottom: 20rpx;

    .detail-tag {
      display: flex;
      align-items: center;
      padding: 8rpx 16rpx;
      background: #f6f8fb;
      border-radius: 8rpx;

      .tag-text {
        font-size: 24rpx;
        color: #666;
        margin-left: 8rpx;
      }
    }
  }

  .personnel-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;

    .add-time {
      font-size: 24rpx;
      color: #999;
    }

    .footer-actions {
      display: flex;
      gap: 20rpx;

      .action-btn {
        display: flex;
        align-items: center;

        .action-text {
          font-size: 24rpx;
          margin-left: 8rpx;
        }
      }
    }
  }
}

// 批量操作栏
.batch-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: white;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);

  .selected-info {
    .selected-text {
      font-size: 28rpx;
      color: #333;
      font-weight: 600;
    }
  }

  .batch-buttons {
    display: flex;
    gap: 20rpx;

    .batch-btn {
      padding: 16rpx 32rpx;
      border-radius: 40rpx;
      font-size: 28rpx;
      border: none;

      &.danger {
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        color: white;
      }

      &.secondary {
        background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
        color: #666;
      }
    }
  }
}

// 空状态
.empty-state,
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .empty-image {
    width: 300rpx;
    height: 300rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

// 加载更多/没有更多
.load-more,
.no-more {
  padding: 40rpx 0;
  text-align: center;

  .no-more-text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
