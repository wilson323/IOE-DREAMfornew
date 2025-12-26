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
          <text class="title-text">审批流程</text>
        </view>
        <view class="navbar-right">
          <view class="add-btn" @tap="gotoApply">
            <uni-icons type="plus" size="18" color="#fff"></uni-icons>
            <text class="add-text">申请</text>
          </view>
        </view>
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
          <text class="chip-count">({{ totalCount }})</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'pending' }"
          @tap="selectStatus('pending')"
        >
          <view class="chip-dot pending"></view>
          <text>待审批</text>
          <text class="chip-count">({{ pendingCount }})</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'approved' }"
          @tap="selectStatus('approved')"
        >
          <view class="chip-dot approved"></view>
          <text>已通过</text>
          <text class="chip-count">({{ approvedCount }})</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'rejected' }"
          @tap="selectStatus('rejected')"
        >
          <view class="chip-dot rejected"></view>
          <text>已拒绝</text>
          <text class="chip-count">({{ rejectedCount }})</text>
        </view>
        <view
          class="filter-chip"
          :class="{ active: selectedStatus === 'processing' }"
          @tap="selectStatus('processing')"
        >
          <view class="chip-dot processing"></view>
          <text>审批中</text>
          <text class="chip-count">({{ processingCount }})</text>
        </view>
      </scroll-view>
    </view>

    <!-- 活动筛选条件 -->
    <view class="active-filters" v-if="hasActiveFilters()">
      <scroll-view scroll-x class="filters-scroll" show-scrollbar>
        <view class="filter-tag" v-for="(filter, index) in activeFilters" :key="index">
          <text class="tag-text">{{ filter.label }}</text>
          <view class="tag-close" @tap="removeFilter(filter.key)">
            <uni-icons type="close" size="12" color="#999"></uni-icons>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 审批列表 -->
    <scroll-view
      class="approval-list"
      scroll-y
      enable-back-to-top
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 审批卡片列表 -->
      <view class="card-list" v-if="approvalList.length > 0">
        <view
          class="approval-card"
          v-for="approval in approvalList"
          :key="approval.approvalId"
          @tap="gotoDetail(approval)"
        >
          <!-- 卡片头部 -->
          <view class="card-header">
            <view class="approval-type" :class="approval.type">
              <uni-icons :type="getTypeIcon(approval.type)" size="16" color="#fff"></uni-icons>
              <text class="type-text">{{ getTypeName(approval.type) }}</text>
            </view>
            <view class="approval-status" :class="approval.status">
              <text>{{ getStatusText(approval.status) }}</text>
            </view>
          </view>

          <!-- 卡片内容 -->
          <view class="card-content">
            <view class="approval-title">{{ approval.title }}</view>
            <view class="approval-info">
              <view class="info-row">
                <uni-icons type="person" size="14" color="#999"></uni-icons>
                <text class="info-text">申请人：{{ approval.applicantName }}</text>
              </view>
              <view class="info-row">
                <uni-icons type="calendar" size="14" color="#999"></uni-icons>
                <text class="info-text">申请时间：{{ formatTime(approval.applyTime) }}</text>
              </view>
              <view class="info-row">
                <uni-icons type="loop" size="14" color="#999"></uni-icons>
                <text class="info-text">当前节点：{{ approval.currentNode }}</text>
              </view>
            </view>
          </view>

          <!-- 进度条 -->
          <view class="approval-progress" v-if="approval.status === 'processing' || approval.status === 'pending'">
            <view class="progress-bar">
              <view
                class="progress-fill"
                :style="{ width: approval.progress + '%' }"
              ></view>
            </view>
            <text class="progress-text">{{ approval.progress }}%</text>
          </view>

          <!-- 卡片底部 -->
          <view class="card-footer">
            <view class="footer-left">
              <text class="urgency-label" :class="approval.urgency">
                {{ getUrgencyText(approval.urgency) }}
              </text>
            </view>
            <view class="footer-right">
              <view
                class="action-btn"
                v-if="approval.status === 'pending' && approval.canApprove"
                @tap.stop="handleApproval(approval, 'approve')"
              >
                <uni-icons type="checkmarkempty" size="16" color="#22c55e"></uni-icons>
                <text>通过</text>
              </view>
              <view
                class="action-btn reject"
                v-if="approval.status === 'pending' && approval.canApprove"
                @tap.stop="handleApproval(approval, 'reject')"
              >
                <uni-icons type="closeempty" size="16" color="#ef4444"></uni-icons>
                <text>拒绝</text>
              </view>
              <view class="detail-btn" @tap.stop="gotoDetail(approval)">
                <text>详情</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="approvalList.length === 0 && !loading">
        <image src="/static/empty-approval.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无审批记录</text>
        <view class="empty-btn" @tap="gotoApply">
          <text>发起申请</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="approvalList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="hasMore">上拉加载更多</text>
        <text v-else>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 高级筛选弹窗 -->
    <uni-popup ref="filterPopup" type="right">
      <view class="filter-popup">
        <view class="popup-header">
          <text class="popup-title">筛选条件</text>
          <view class="close-btn" @tap="closeFilterPopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <!-- 审批类型 -->
          <view class="filter-section">
            <view class="section-title">审批类型</view>
            <view class="type-grid">
              <view
                class="type-item"
                :class="{ active: filterTypes.includes('access') }"
                @tap="toggleFilterType('access')"
              >
                <uni-icons type="locked" size="20" :color="filterTypes.includes('access') ? '#667eea' : '#999'"></uni-icons>
                <text>门禁权限</text>
              </view>
              <view
                class="type-item"
                :class="{ active: filterTypes.includes('visitor') }"
                @tap="toggleFilterType('visitor')"
              >
                <uni-icons type="person" size="20" :color="filterTypes.includes('visitor') ? '#667eea' : '#999'"></uni-icons>
                <text>访客预约</text>
              </view>
              <view
                class="type-item"
                :class="{ active: filterTypes.includes('overtime') }"
                @tap="toggleFilterType('overtime')"
              >
                <uni-icons type="clock" size="20" :color="filterTypes.includes('overtime') ? '#667eea' : '#999'"></uni-icons>
                <text>加班申请</text>
              </view>
              <view
                class="type-item"
                :class="{ active: filterTypes.includes('leave') }"
                @tap="toggleFilterType('leave')"
              >
                <uni-icons type="calendar" size="20" :color="filterTypes.includes('leave') ? '#667eea' : '#999'"></uni-icons>
                <text>请假申请</text>
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
                :class="{ active: selectedTimePreset === 'today' }"
                @tap="selectTimePreset('today')"
              >
                今天
              </view>
              <view
                class="preset-chip"
                :class="{ active: selectedTimePreset === 'week' }"
                @tap="selectTimePreset('week')"
              >
                本周
              </view>
              <view
                class="preset-chip"
                :class="{ active: selectedTimePreset === 'month' }"
                @tap="selectTimePreset('month')"
              >
                本月
              </view>
            </view>
          </view>

          <!-- 紧急程度 -->
          <view class="filter-section">
            <view class="section-title">紧急程度</view>
            <view class="urgency-options">
              <view
                class="urgency-item"
                :class="{ active: filterUrgency.includes('normal') }"
                @tap="toggleFilterUrgency('normal')"
              >
                <view class="urgency-dot normal"></view>
                <text>普通</text>
              </view>
              <view
                class="urgency-item"
                :class="{ active: filterUrgency.includes('urgent') }"
                @tap="toggleFilterUrgency('urgent')"
              >
                <view class="urgency-dot urgent"></view>
                <text>紧急</text>
              </view>
              <view
                class="urgency-item"
                :class="{ active: filterUrgency.includes('very-urgent') }"
                @tap="toggleFilterUrgency('very-urgent')"
              >
                <view class="urgency-dot very-urgent"></view>
                <text>非常紧急</text>
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
            <text>应用筛选</text>
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
const filterUrgency = ref([])

// 统计数据
const totalCount = ref(156)
const pendingCount = ref(23)
const approvedCount = ref(98)
const rejectedCount = ref(25)
const processingCount = ref(10)

// 审批列表
const approvalList = ref([
  {
    approvalId: 1,
    type: 'access',
    title: '门禁权限申请 - A栋办公区',
    applicantName: '张三',
    applyTime: '2025-01-20 10:30:00',
    status: 'pending',
    urgency: 'urgent',
    currentNode: '部门主管审批',
    progress: 33,
    canApprove: true,
  },
  {
    approvalId: 2,
    type: 'visitor',
    title: '访客预约申请 - 李四',
    applicantName: '王五',
    applyTime: '2025-01-20 09:15:00',
    status: 'approved',
    urgency: 'normal',
    currentNode: '已完成',
    progress: 100,
    canApprove: false,
  },
  {
    approvalId: 3,
    type: 'overtime',
    title: '加班申请 - 2025年1月20日',
    applicantName: '赵六',
    applyTime: '2025-01-19 18:20:00',
    status: 'processing',
    urgency: 'normal',
    currentNode: '人事审批',
    progress: 66,
    canApprove: false,
  },
  {
    approvalId: 4,
    type: 'leave',
    title: '请假申请 - 事假2天',
    applicantName: '钱七',
    applyTime: '2025-01-19 14:10:00',
    status: 'rejected',
    urgency: 'normal',
    currentNode: '部门主管审批',
    progress: 33,
    canApprove: true,
  },
  {
    approvalId: 5,
    type: 'access',
    title: '门禁权限申请 - B栋会议室',
    applicantName: '孙八',
    applyTime: '2025-01-19 11:25:00',
    status: 'pending',
    urgency: 'very-urgent',
    currentNode: '部门主管审批',
    progress: 33,
    canApprove: true,
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

// 活动筛选条件
const activeFilters = computed(() => {
  const filters = []
  if (filterTypes.value.length > 0) {
    filters.push({ key: 'types', label: `类型: ${filterTypes.value.length}项` })
  }
  if (selectedTimePreset.value !== 'all') {
    const timeMap = { today: '今天', week: '本周', month: '本月' }
    filters.push({ key: 'time', label: timeMap[selectedTimePreset.value] })
  }
  if (filterUrgency.value.length > 0) {
    filters.push({ key: 'urgency', label: `紧急: ${filterUrgency.value.length}项` })
  }
  return filters
})

// 是否有活动筛选
const hasActiveFilters = () => {
  return activeFilters.value.length > 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 发起申请
const gotoApply = () => {
  uni.navigateTo({
    url: '/pages/access/approval-apply',
  })
}

// 前往详情
const gotoDetail = (approval) => {
  uni.navigateTo({
    url: `/pages/access/approval-detail?id=${approval.approvalId}`,
  })
}

// 选择状态
const selectStatus = (status) => {
  selectedStatus.value = status
  loadApprovalList()
}

// 获取类型图标
const getTypeIcon = (type) => {
  const iconMap = {
    access: 'locked',
    visitor: 'person',
    overtime: 'clock',
    leave: 'calendar',
  }
  return iconMap[type] || 'doc'
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待审批',
    processing: '审批中',
    approved: '已通过',
    rejected: '已拒绝',
  }
  return statusMap[status] || '未知'
}

// 获取紧急程度文本
const getUrgencyText = (urgency) => {
  const urgencyMap = {
    'normal': '普通',
    'urgent': '紧急',
    'very-urgent': '非常紧急',
  }
  return urgencyMap[urgency] || '普通'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return timeStr.substring(0, 10)
  }
}

// 处理审批
const handleApproval = (approval, action) => {
  const title = action === 'approve' ? '通过审批' : '拒绝审批'
  const content = `确认${title}「${approval.title}」吗？`

  uni.showModal({
    title,
    content,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API处理审批
        uni.showToast({
          title: title + '成功',
          icon: 'success',
        })
        loadApprovalList()
      }
    },
  })
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  currentPage.value = 1
  setTimeout(() => {
    loadApprovalList()
    refreshing.value = false
  }, 1000)
}

// 上拉加载
const loadMore = () => {
  if (loading.value || !hasMore.value) return
  currentPage.value++
  loadApprovalList(true)
}

// 加载审批列表
const loadApprovalList = (isLoadMore = false) => {
  if (!isLoadMore) {
    currentPage.value = 1
  }

  loading.value = true

  // TODO: 调用API加载数据
  setTimeout(() => {
    if (!isLoadMore) {
      // 模拟数据
      approvalList.value = [
        {
          approvalId: 1,
          type: 'access',
          title: '门禁权限申请 - A栋办公区',
          applicantName: '张三',
          applyTime: '2025-01-20 10:30:00',
          status: 'pending',
          urgency: 'urgent',
          currentNode: '部门主管审批',
          progress: 33,
          canApprove: true,
        },
      ]
    }
    loading.value = false
    hasMore.value = currentPage.value < 3
  }, 500)
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

// 切换筛选紧急程度
const toggleFilterUrgency = (urgency) => {
  const index = filterUrgency.value.indexOf(urgency)
  if (index > -1) {
    filterUrgency.value.splice(index, 1)
  } else {
    filterUrgency.value.push(urgency)
  }
}

// 移除筛选条件
const removeFilter = (key) => {
  if (key === 'types') {
    filterTypes.value = []
  } else if (key === 'time') {
    selectedTimePreset.value = 'all'
  } else if (key === 'urgency') {
    filterUrgency.value = []
  }
  loadApprovalList()
}

// 重置筛选条件
const resetFilters = () => {
  filterTypes.value = []
  selectedTimePreset.value = 'all'
  filterUrgency.value = []
}

// 应用筛选条件
const applyFilters = () => {
  closeFilterPopup()
  loadApprovalList()
}

// 关闭筛选弹窗
const closeFilterPopup = () => {
  filterPopup.value?.close()
}

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

onMounted(() => {
  getStatusBarHeight()
  loadApprovalList()
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

      .add-btn {
        display: flex;
        align-items: center;
        gap: 6rpx;

        .add-text {
          font-size: 26rpx;
          color: #fff;
        }
      }
    }
  }
}

.quick-filters {
  position: fixed;
  top: calc(var(--status-bar-height) + 44px);
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 0;
  z-index: 999;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

  .filter-scroll {
    white-space: nowrap;
    padding: 0 30rpx;

    .filter-chip {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 24rpx;
      margin-right: 16rpx;
      border-radius: 40rpx;
      background: #f5f7fa;
      font-size: 28rpx;
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

        &.pending {
          background: #f59e0b;
        }

        &.approved {
          background: #22c55e;
        }

        &.rejected {
          background: #ef4444;
        }

        &.processing {
          background: #3b82f6;
        }
      }

      .chip-count {
        font-size: 24rpx;
        opacity: 0.8;
      }
    }
  }
}

.active-filters {
  position: fixed;
  top: calc(var(--status-bar-height) + 44px + 88rpx);
  left: 0;
  right: 0;
  background: #fff;
  padding: 16rpx 0;
  z-index: 998;
  border-top: 1rpx solid #f0f0f0;

  .filters-scroll {
    white-space: nowrap;
    padding: 0 30rpx;

    .filter-tag {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 20rpx;
      margin-right: 16rpx;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      border-radius: 8rpx;
      font-size: 26rpx;
      color: #667eea;
      border: 1rpx solid rgba(102, 126, 234, 0.3);

      .tag-text {
        max-width: 200rpx;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .tag-close {
        display: flex;
        align-items: center;
      }
    }
  }
}

.approval-list {
  padding-top: calc(var(--status-bar-height) + 44px + 88rpx + 60rpx);
  padding-bottom: 40rpx;
  min-height: 100vh;

  &.has-filters {
    padding-top: calc(var(--status-bar-height) + 44px + 88rpx + 60rpx + 72rpx);
  }
}

.card-list {
  padding: 20rpx 30rpx;
}

.approval-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .approval-type {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
      font-size: 24rpx;
      color: #fff;

      &.access {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.visitor {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      }

      &.overtime {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.leave {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
      }

      .type-text {
        font-size: 24rpx;
      }
    }

    .approval-status {
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
      font-size: 24rpx;
      font-weight: 500;

      &.pending {
        background: rgba(245, 158, 11, 0.1);
        color: #f59e0b;
      }

      &.processing {
        background: rgba(59, 130, 246, 0.1);
        color: #3b82f6;
      }

      &.approved {
        background: rgba(34, 197, 94, 0.1);
        color: #22c55e;
      }

      &.rejected {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
      }
    }
  }

  .card-content {
    margin-bottom: 20rpx;

    .approval-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
      line-height: 1.5;
    }

    .approval-info {
      display: flex;
      flex-direction: column;
      gap: 12rpx;

      .info-row {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .info-text {
          font-size: 26rpx;
          color: #666;
        }
      }
    }
  }

  .approval-progress {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 20rpx;
    padding: 20rpx;
    background: #f5f7fa;
    border-radius: 12rpx;

    .progress-bar {
      flex: 1;
      height: 12rpx;
      background: #e5e7eb;
      border-radius: 6rpx;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 6rpx;
        transition: width 0.3s;
      }
    }

    .progress-text {
      font-size: 24rpx;
      color: #667eea;
      font-weight: 600;
      min-width: 80rpx;
      text-align: right;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;

    .footer-left {
      .urgency-label {
        padding: 8rpx 16rpx;
        border-radius: 8rpx;
        font-size: 22rpx;

        &.normal {
          background: rgba(102, 126, 234, 0.1);
          color: #667eea;
        }

        &.urgent {
          background: rgba(245, 158, 11, 0.1);
          color: #f59e0b;
        }

        &.very-urgent {
          background: rgba(239, 68, 68, 0.1);
          color: #ef4444;
        }
      }
    }

    .footer-right {
      display: flex;
      gap: 16rpx;

      .action-btn {
        display: flex;
        align-items: center;
        gap: 6rpx;
        padding: 12rpx 20rpx;
        border-radius: 8rpx;
        font-size: 26rpx;
        background: #f5f7fa;
        color: #666;
        border: 2rpx solid #e5e7eb;

        &.reject {
          border-color: #ef4444;
          color: #ef4444;
        }
      }

      .detail-btn {
        display: flex;
        align-items: center;
        gap: 4rpx;
        font-size: 26rpx;
        color: #999;
      }
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
    margin-bottom: 40rpx;
  }

  .empty-btn {
    padding: 20rpx 60rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 40rpx;
    color: #fff;
    font-size: 28rpx;
    font-weight: 600;
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
  width: 600rpx;
  height: 100vh;
  background: #fff;

  .popup-header {
    position: relative;
    padding: 40rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      text-align: center;
    }

    .close-btn {
      position: absolute;
      right: 30rpx;
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
    height: calc(100vh - 200rpx);
    padding: 30rpx;

    .filter-section {
      margin-bottom: 40rpx;

      .section-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 20rpx;
      }

      .type-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16rpx;

        .type-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 12rpx;
          padding: 30rpx 20rpx;
          border-radius: 16rpx;
          border: 2rpx solid #e5e7eb;
          font-size: 26rpx;
          color: #666;
          transition: all 0.3s;

          &.active {
            border-color: #667eea;
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
            color: #667eea;
          }
        }
      }

      .time-presets {
        display: flex;
        gap: 12rpx;
        flex-wrap: wrap;

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

      .urgency-options {
        display: flex;
        gap: 16rpx;

        .urgency-item {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 12rpx;
          padding: 24rpx;
          border-radius: 12rpx;
          border: 2rpx solid #e5e7eb;
          font-size: 26rpx;
          color: #666;
          transition: all 0.3s;

          &.active {
            border-color: #667eea;
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
            color: #667eea;
          }

          .urgency-dot {
            width: 24rpx;
            height: 24rpx;
            border-radius: 50%;

            &.normal {
              background: #667eea;
            }

            &.urgent {
              background: #f59e0b;
            }

            &.very-urgent {
              background: #ef4444;
            }
          }
        }
      }
    }
  }

  .popup-footer {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    gap: 20rpx;
    padding: 20rpx 30rpx;
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
    background: #fff;
    border-top: 1rpx solid #f0f0f0;

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
