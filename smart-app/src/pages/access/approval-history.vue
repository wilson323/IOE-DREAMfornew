<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px', height: (statusBarHeight + 44) + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">
          <text class="title-text">审批历史</text>
        </view>
        <view class="navbar-right">
          <view class="filter-btn" @tap="showFilterPopup">
            <uni-icons type="filter" size="18" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 统计概览 -->
    <view class="overview-section">
      <view class="overview-item">
        <text class="overview-value">{{ statistics.total }}</text>
        <text class="overview-label">总申请数</text>
      </view>
      <view class="overview-item">
        <text class="overview-value success">{{ statistics.approved }}</text>
        <text class="overview-label">已通过</text>
      </view>
      <view class="overview-item">
        <text class="overview-value rejected">{{ statistics.rejected }}</text>
        <text class="overview-label">已拒绝</text>
      </view>
      <view class="overview-item">
        <text class="overview-value pending">{{ statistics.pending }}</text>
        <text class="overview-label">审批中</text>
      </view>
    </view>

    <!-- 快速筛选标签 -->
    <view class="quick-filters">
      <scroll-view scroll-x class="filter-scroll" show-scrollbar>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'all' }"
          @tap="selectStatus('all')"
        >
          <text>全部</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'approved' }"
          @tap="selectStatus('approved')"
        >
          <view class="chip-dot approved"></view>
          <text>已通过</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'rejected' }"
          @tap="selectStatus('rejected')"
        >
          <view class="chip-dot rejected"></view>
          <text>已拒绝</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'pending' }"
          @tap="selectStatus('pending')"
        >
          <view class="chip-dot pending"></view>
          <text>审批中</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'withdrawn' }"
          @tap="selectStatus('withdrawn')"
        >
          <view class="chip-dot withdrawn"></view>
          <text>已撤回</text>
        </view>
      </scroll-view>
    </view>

    <!-- 历史记录列表 -->
    <scroll-view
      class="history-list"
      scroll-y
      enable-back-to-top
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view class="list-content" v-if="historyList.length > 0">
        <!-- 日期分组 -->
        <view
          class="date-group"
          v-for="(group, dateKey) in groupedHistory"
          :key="dateKey"
        >
          <view class="date-header">
            <text class="date-text">{{ formatDateGroup(dateKey) }}</text>
            <text class="date-count">{{ group.length }}条</text>
          </view>

          <!-- 记录项 -->
          <view
            class="history-item"
            v-for="item in group"
            :key="item.id"
            @tap="gotoDetail(item)"
          >
            <!-- 状态图标 -->
            <view class="item-icon" :class="item.status">
              <uni-icons :type="getStatusIcon(item.status)" size="20" color="#fff"></uni-icons>
            </view>

            <!-- 内容区域 -->
            <view class="item-content">
              <view class="item-top">
                <text class="item-title">{{ item.title }}</text>
                <view class="item-status" :class="item.status">
                  <text>{{ getStatusText(item.status) }}</text>
                </view>
              </view>
              <text class="item-type">{{ getTypeName(item.type) }}</text>
              <text class="item-time">{{ formatFullTime(item.createTime) }}</text>
            </view>

            <!-- 操作信息 -->
            <view class="item-action">
              <text class="action-text">{{ item.lastAction || '等待审批' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="historyList.length === 0 && !loading">
        <image src="/static/empty-history.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无审批记录</text>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="historyList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="hasMore">上拉加载更多</text>
        <text v-else>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom">
      <view class="filter-popup">
        <view class="popup-header">
          <text class="popup-title">筛选条件</text>
          <view class="close-btn" @tap="closeFilterPopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <!-- 申请类型 -->
          <view class="filter-section">
            <view class="section-title">申请类型</view>
            <view class="type-checkboxes">
              <view
                class="checkbox-item"
                v-for="type in approvalTypes"
                :key="type.value"
              >
                <checkbox
                  :checked="filterTypes.includes(type.value)"
                  @tap="toggleFilterType(type.value)"
                  color="#667eea"
                />
                <text class="checkbox-label">{{ type.label }}</text>
              </view>
            </view>
          </view>

          <!-- 时间范围 -->
          <view class="filter-section">
            <view class="section-title">申请时间</view>
            <view class="time-presets">
              <view
                class="preset-chip"
                :class="{ active: selectedTimePreset === 'all' }"
                @tap="selectTimePreset('all')"
              >
                全部
              </view>
              <view
                class="preset-chip"
                :class="{ active: selectedTimePreset === 'week' }"
                @tap="selectTimePreset('week')"
              >
                最近一周
              </view>
              <view
                class="preset-chip"
                :class="{ active: selectedTimePreset === 'month' }"
                @tap="selectTimePreset('month')"
              >
                最近一月
              </view>
              <view
                class="preset-chip"
                :class="{ active: selectedTimePreset === 'quarter' }"
                @tap="selectTimePreset('quarter')"
              >
                最近三月
              </view>
            </view>
          </view>

          <!-- 审批状态 -->
          <view class="filter-section">
            <view class="section-title">审批状态</view>
            <view class="status-checkboxes">
              <view class="checkbox-item">
                <checkbox
                  :checked="filterStatus.includes('approved')"
                  @tap="toggleFilterStatus('approved')"
                  color="#667eea"
                />
                <text class="checkbox-label">已通过</text>
              </view>
              <view class="checkbox-item">
                <checkbox
                  :checked="filterStatus.includes('rejected')"
                  @tap="toggleFilterStatus('rejected')"
                  color="#667eea"
                />
                <text class="checkbox-label">已拒绝</text>
              </view>
              <view class="checkbox-item">
                <checkbox
                  :checked="filterStatus.includes('pending')"
                  @tap="toggleFilterStatus('pending')"
                  color="#667eea"
                />
                <text class="checkbox-label">审批中</text>
              </view>
              <view class="checkbox-item">
                <checkbox
                  :checked="filterStatus.includes('withdrawn')"
                  @tap="toggleFilterStatus('withdrawn')"
                  color="#667eea"
                />
                <text class="checkbox-label">已撤回</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <!-- 底部按钮 -->
        <view class="popup-footer">
          <view class="footer-btn secondary" @tap="resetFilters">
            <text>重置</text>
          </view>
          <view class="footer-btn primary" @tap="applyFilters">
            <text>确定</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 选中的状态
const selectedStatus = ref('all')

// 筛选条件
const filterTypes = ref([])
const selectedTimePreset = ref('all')
const filterStatus = ref(['approved', 'rejected', 'pending', 'withdrawn'])

// 统计数据
const statistics = reactive({
  total: 156,
  approved: 98,
  rejected: 25,
  pending: 18,
  withdrawn: 15,
})

// 审批类型
const approvalTypes = [
  { value: 'access', label: '门禁权限' },
  { value: 'visitor', label: '访客预约' },
  { value: 'overtime', label: '加班申请' },
  { value: 'leave', label: '请假申请' },
]

// 历史记录列表
const historyList = ref([
  {
    id: 1,
    title: '门禁权限申请 - A栋办公区',
    type: 'access',
    status: 'approved',
    createTime: '2025-01-20 15:30:00',
    lastAction: '已通过 - 李经理',
  },
  {
    id: 2,
    title: '访客预约申请 - 张三',
    type: 'visitor',
    status: 'rejected',
    createTime: '2025-01-19 10:20:00',
    lastAction: '已拒绝 - 理由不符合规定',
  },
  {
    id: 3,
    title: '加班申请 - 项目紧急处理',
    type: 'overtime',
    status: 'approved',
    createTime: '2025-01-18 18:00:00',
    lastAction: '已通过 - 人事审批',
  },
  {
    id: 4,
    title: '请假申请 - 事假1天',
    type: 'leave',
    status: 'pending',
    createTime: '2025-01-17 09:00:00',
    lastAction: '等待审批 - 部门主管',
  },
  {
    id: 5,
    title: '门禁权限申请 - B栋会议室',
    type: 'access',
    status: 'withdrawn',
    createTime: '2025-01-16 14:30:00',
    lastAction: '已撤回 - 用户主动撤回',
  },
  {
    id: 6,
    title: '访客预约申请 - 李四',
    type: 'visitor',
    status: 'approved',
    createTime: '2025-01-15 11:00:00',
    lastAction: '已通过 - 人事审批',
  },
])

// 分页和加载
const refreshing = ref(false)
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 20

// 弹窗引用
const filterPopup = ref(null)

// 按日期分组
const groupedHistory = computed(() => {
  const groups = {}
  historyList.value.forEach(item => {
    const date = item.createTime.substring(0, 10)
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(item)
  })
  return groups
})

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 前往详情
const gotoDetail = (item) => {
  uni.navigateTo({
    url: `/pages/access/approval-detail?id=${item.id}`,
  })
}

// 选择状态
const selectStatus = (status) => {
  selectedStatus.value = status
  loadHistoryList()
}

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    approved: 'checkmarkempty',
    rejected: 'closeempty',
    pending: 'clock',
    withdrawn: 'refreshempty',
  }
  return iconMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    approved: '已通过',
    rejected: '已拒绝',
    pending: '审批中',
    withdrawn: '已撤回',
  }
  return textMap[status] || '未知'
}

// 获取类型名称
const getTypeName = (type) => {
  const nameMap = {
    access: '门禁权限',
    visitor: '访客预约',
    overtime: '加班申请',
    leave: '请假申请',
  }
  return nameMap[type] || '其他'
}

// 格式化完整时间
const formatFullTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr
}

// 格式化日期分组
const formatDateGroup = (dateStr) => {
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (dateStr === today.toISOString().substring(0, 10)) {
    return '今天'
  } else if (dateStr === yesterday.toISOString().substring(0, 10)) {
    return '昨天'
  } else {
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekDays = ['日', '一', '二', '三', '四', '五', '六']
    const weekDay = weekDays[date.getDay()]
    return `${month}月${day}日 星期${weekDay}`
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  currentPage.value = 1
  setTimeout(() => {
    loadHistoryList()
    refreshing.value = false
  }, 1000)
}

// 上拉加载
const loadMore = () => {
  if (loading.value || !hasMore.value) return
  currentPage.value++
  loadHistoryList(true)
}

// 加载历史列表
const loadHistoryList = (isLoadMore = false) => {
  if (!isLoadMore) {
    currentPage.value = 1
  }

  loading.value = true

  // TODO: 调用API加载数据
  setTimeout(() => {
    loading.value = false
    hasMore.value = currentPage.value < 3
  }, 500)
}

// 显示筛选弹窗
const showFilterPopup = () => {
  filterPopup.value?.open()
}

// 关闭筛选弹窗
const closeFilterPopup = () => {
  filterPopup.value?.close()
}

// 切换筛选类型
const toggleFilterType = (type) => {
  const index = filterTypes.value.indexOf(type)
  if (index > -1) {
    filterTypes.value.splice(index, 1)
  } else {
    filterTypes.value.push(type)
  }
}

// 选择时间预设
const selectTimePreset = (preset) => {
  selectedTimePreset.value = preset
}

// 切换筛选状态
const toggleFilterStatus = (status) => {
  const index = filterStatus.value.indexOf(status)
  if (index > -1) {
    filterStatus.value.splice(index, 1)
  } else {
    filterStatus.value.push(status)
  }
}

// 重置筛选条件
const resetFilters = () => {
  filterTypes.value = []
  selectedTimePreset.value = 'all'
  filterStatus.value = ['approved', 'rejected', 'pending', 'withdrawn']
}

// 应用筛选条件
const applyFilters = () => {
  closeFilterPopup()
  loadHistoryList()
}

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

onMounted(() => {
  getStatusBarHeight()
  loadHistoryList()
})
</script>

<style lang="scss" scoped>
.page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e4e8ec 100%);
}

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
      gap: 8rpx;

      .back-text {
        font-size: 28rpx;
        color: #fff;
      }
    }

    .navbar-title {
      flex: 1;
      text-align: center;

      .title-text {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .navbar-right {
      width: 100rpx;
      display: flex;
      justify-content: flex-end;
    }
  }
}

.overview-section {
  position: fixed;
  top: calc(var(--status-bar-height) + 44px);
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  background: #fff;
  z-index: 999;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

  .overview-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;

    .overview-value {
      font-size: 36rpx;
      font-weight: 700;
      color: #333;

      &.success {
        color: #22c55e;
      }

      &.rejected {
        color: #ef4444;
      }

      &.pending {
        color: #f59e0b;
      }
    }

    .overview-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.quick-filters {
  position: fixed;
  top: calc(var(--status-bar-height) + 44px + 120rpx);
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 0;
  z-index: 998;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

  .filter-scroll {
    white-space: nowrap;
    padding: 0 30rpx;

    .filter-chip {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 24rpx;
      margin-right: 16rpx;
      border-radius: 40rpx;
      background: #f5f7fa;
      font-size: 26rpx;
      color: #666;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border-color: #667eea;
      }

      .chip-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;

        &.approved {
          background: #22c55e;
        }

        &.rejected {
          background: #ef4444;
        }

        &.pending {
          background: #f59e0b;
        }

        &.withdrawn {
          background: #999;
        }
      }
    }
  }
}

.history-list {
  padding-top: calc(var(--status-bar-height) + 44px + 120rpx + 80rpx);
  padding-bottom: 40rpx;
  min-height: 100vh;
}

.list-content {
  padding: 20rpx 30rpx;
}

.date-group {
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .date-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    padding: 0 10rpx;

    .date-text {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }

    .date-count {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.history-item {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);

  .item-icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &.approved {
      background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
    }

    &.rejected {
      background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    }

    &.pending {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    }

    &.withdrawn {
      background: linear-gradient(135deg, #999 0%, #666 100%);
    }
  }

  .item-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12rpx;

    .item-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;

      .item-title {
        flex: 1;
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        line-height: 1.5;
        padding-right: 16rpx;
      }

      .item-status {
        padding: 6rpx 12rpx;
        border-radius: 8rpx;
        font-size: 22rpx;
        font-weight: 500;
        flex-shrink: 0;

        &.approved {
          background: rgba(34, 197, 94, 0.1);
          color: #22c55e;
        }

        &.rejected {
          background: rgba(239, 68, 68, 0.1);
          color: #ef4444;
        }

        &.pending {
          background: rgba(245, 158, 11, 0.1);
          color: #f59e0b;
        }

        &.withdrawn {
          background: rgba(153, 153, 153, 0.1);
          color: #999;
        }
      }
    }

    .item-type {
      font-size: 26rpx;
      color: #667eea;
    }

    .item-time {
      font-size: 24rpx;
      color: #999;
    }
  }

  .item-action {
    .action-text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;

  .empty-image {
    width: 320rpx;
    height: 320rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.load-more {
  padding: 30rpx;
  text-align: center;
  font-size: 26rpx;
  color: #999;
}

// 筛选弹窗
.filter-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));

  .popup-header {
    position: relative;
    text-align: center;
    margin-bottom: 30rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    max-height: 60vh;

    .filter-section {
      margin-bottom: 30rpx;

      .section-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 20rpx;
      }

      .type-checkboxes,
      .status-checkboxes {
        display: flex;
        flex-direction: column;
        gap: 20rpx;

        .checkbox-item {
          display: flex;
          align-items: center;
          gap: 16rpx;

          .checkbox-label {
            font-size: 28rpx;
            color: #333;
          }
        }
      }

      .time-presets {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;

        .preset-chip {
          padding: 12rpx 24rpx;
          border-radius: 40rpx;
          font-size: 26rpx;
          color: #666;
          background: #f5f7fa;
          border: 2rpx solid transparent;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            border-color: #667eea;
          }
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
      height: 88rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16rpx;
      font-size: 30rpx;
      font-weight: 600;

      &.secondary {
        background: #f5f7fa;
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}
</style>
