<template>
  <view class="approval-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">审批流程</view>
      <view class="nav-right" @click="showFilterPopup">
        <uni-icons type="settings" size="20" color="#1890ff"></uni-icons>
      </view>
    </view>

    <!-- Tab切换 -->
    <view class="tab-container">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        :class="['tab-item', { active: currentTab === index }]"
        @click="switchTab(index)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="tab.count > 0" class="tab-badge">{{ tab.count > 99 ? '99+' : tab.count }}</view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-container">
      <view class="stat-card gradient-blue">
        <view class="stat-icon">
          <uni-icons type="clock" size="24" color="#fff"></uni-icons>
        </view>
        <view class="stat-content">
          <view class="stat-value">{{ statistics.pending }}</view>
          <view class="stat-label">待审批</view>
        </view>
      </view>
      <view class="stat-card gradient-green">
        <view class="stat-icon">
          <uni-icons type="checkmarkempty" size="24" color="#fff"></uni-icons>
        </view>
        <view class="stat-content">
          <view class="stat-value">{{ statistics.approved }}</view>
          <view class="stat-label">已通过</view>
        </view>
      </view>
      <view class="stat-card gradient-red">
        <view class="stat-icon">
          <uni-icons type="closeempty" size="24" color="#fff"></uni-icons>
        </view>
        <view class="stat-content">
          <view class="stat-value">{{ statistics.rejected }}</view>
          <view class="stat-label">已拒绝</view>
        </view>
      </view>
    </view>

    <!-- 快速筛选 -->
    <view class="filter-bar">
      <scroll-view scroll-x class="filter-scroll">
        <view
          v-for="(filter, index) in filters"
          :key="index"
          :class="['filter-chip', { active: selectedFilter === index }]"
          @click="selectFilter(index)"
        >
          <text class="filter-text">{{ filter.label }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 申请列表 -->
    <view class="list-container">
      <scroll-view
        scroll-y
        class="list-scroll"
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <!-- 空状态 -->
        <view v-if="approvalList.length === 0 && !loading" class="empty-state">
          <uni-icons type="document" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无审批记录</text>
          <button class="empty-btn" @click="createApplication">发起申请</button>
        </view>

        <!-- 申请列表 -->
        <view v-for="item in approvalList" :key="item.approvalId" class="approval-item" @click="viewDetail(item)">
          <!-- 申请人信息 -->
          <view class="item-header">
            <view class="applicant-info">
              <image :src="item.applicantAvatar || '/static/images/default-avatar.png'" class="avatar"></image>
              <view class="applicant-text">
                <view class="applicant-name">{{ item.applicantName }}</view>
                <view class="apply-time">{{ formatTime(item.createTime) }}</view>
              </view>
            </view>
            <view :class="['status-badge', getStatusClass(item.approvalStatus)]">
              {{ getStatusText(item.approvalStatus) }}
            </view>
          </view>

          <!-- 申请类型 -->
          <view class="item-type">
            <uni-icons :type="getTypeIcon(item.approvalType)" size="18" color="#1890ff"></uni-icons>
            <text class="type-text">{{ getTypeText(item.approvalType) }}</text>
          </view>

          <!-- 申请内容 -->
          <view class="item-content">
            <view class="content-row">
              <text class="content-label">申请理由：</text>
              <text class="content-value">{{ item.applyReason || '无' }}</text>
            </view>
            <view v-if="item.areaName" class="content-row">
              <text class="content-label">访问区域：</text>
              <text class="content-value">{{ item.areaName }}</text>
            </view>
            <view v-if="item.expectedTime" class="content-row">
              <text class="content-label">预期时间：</text>
              <text class="content-value">{{ formatExpectedTime(item.expectedTime) }}</text>
            </view>
          </view>

          <!-- 当前审批人 -->
          <view v-if="currentTab === 0 && item.currentApprover" class="item-footer">
            <uni-icons type="person" size="14" color="#999"></uni-icons>
            <text class="footer-text">当前审批人：{{ item.currentApprover }}</text>
          </view>

          <!-- 审批操作 -->
          <view v-if="currentTab === 1 && item.approvalStatus === 1" class="item-actions">
            <button class="action-btn reject-btn" @click.stop="rejectApproval(item)">拒绝</button>
            <button class="action-btn approve-btn" @click.stop="approveApproval(item)">通过</button>
          </view>

          <!-- 审批意见 -->
          <view v-if="item.approvalComment && currentTab === 2" class="item-comment">
            <text class="comment-label">审批意见：</text>
            <text class="comment-text">{{ item.approvalComment }}</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="approvalList.length > 0" class="load-more">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="!hasMore" class="no-more-text">没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom">
      <view class="filter-popup">
        <view class="popup-header">
          <view class="popup-title">筛选条件</view>
          <view class="popup-close" @click="closeFilterPopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <!-- 申请类型筛选 -->
          <view class="filter-section">
            <view class="filter-section-title">申请类型</view>
            <view class="filter-options">
              <view
                v-for="(type, index) in approvalTypes"
                :key="index"
                :class="['option-chip', { active: selectedType === type.value }]"
                @click="selectType(type.value)"
              >
                <text class="option-text">{{ type.label }}</text>
              </view>
            </view>
          </view>

          <!-- 时间范围筛选 -->
          <view class="filter-section">
            <view class="filter-section-title">时间范围</view>
            <view class="filter-options">
              <view
                v-for="(range, index) in timeRanges"
                :key="index"
                :class="['option-chip', { active: selectedTimeRange === range.value }]"
                @click="selectTimeRange(range.value)"
              >
                <text class="option-text">{{ range.label }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="popup-footer">
          <button class="popup-btn reset-btn" @click="resetFilters">重置</button>
          <button class="popup-btn confirm-btn" @click="applyFilters">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- 快速操作按钮 -->
    <view class="fab-container">
      <button class="fab-btn" @click="showActionMenu">
        <uni-icons type="plus" size="24" color="#fff"></uni-icons>
      </button>
    </view>

    <!-- 快速操作菜单 -->
    <uni-popup ref="actionMenuPopup" type="bottom">
      <view class="action-menu">
        <view class="menu-item" @click="createPermissionApply">
          <uni-icons type="locked" size="20" color="#1890ff"></uni-icons>
          <text class="menu-text">权限申请</text>
        </view>
        <view class="menu-item" @click="createVisitorAppointment">
          <uni-icons type="person" size="20" color="#52c41a"></uni-icons>
          <text class="menu-text">访客预约</text>
        </view>
        <view class="menu-item" @click="createEmergencyPermission">
          <uni-icons type="fire" size="20" color="#ff4d4f"></uni-icons>
          <text class="menu-text">紧急权限</text>
        </view>
        <view class="menu-cancel" @click="closeActionMenu">
          <text class="cancel-text">取消</text>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// Tab选项
const tabs = ref([
  { label: '我的申请', count: 0, type: 'myApply' },
  { label: '待我审批', count: 0, type: 'pending' },
  { label: '已审批', count: 0, type: 'approved' }
])
const currentTab = ref(0)

// 统计数据
const statistics = ref({
  pending: 0,      // 待审批
  approved: 0,     // 已通过
  rejected: 0      // 已拒绝
})

// 快速筛选选项
const filters = ref([
  { label: '全部', value: '' },
  { label: '待审批', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已拒绝', value: 3 }
])
const selectedFilter = ref(0)

// 申请类型筛选
const approvalTypes = [
  { label: '全部', value: '' },
  { label: '权限申请', value: 1 },
  { label: '访客预约', value: 2 },
  { label: '紧急权限', value: 3 }
]
const selectedType = ref('')

// 时间范围筛选
const timeRanges = [
  { label: '全部', value: '' },
  { label: '今天', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' }
]
const selectedTimeRange = ref('')

// 申请列表
const approvalList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)

// 页面生命周期
onLoad((options) => {
  // 如果有tab参数，切换到对应tab
  if (options.tab) {
    currentTab.value = parseInt(options.tab)
  }

  loadStatistics()
  loadApprovals()
})

/**
 * 切换Tab
 */
const switchTab = (index) => {
  currentTab.value = index
  pageNum.value = 1
  approvalList.value = []
  hasMore.value = true
  loadApprovals()
}

/**
 * 选择快速筛选
 */
const selectFilter = (index) => {
  selectedFilter.value = index
  pageNum.value = 1
  approvalList.value = []
  hasMore.value = true
  loadApprovals()
}

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const tabType = tabs.value[currentTab.value].type
    const result = await accessApi.getApprovalStatistics({
      tabType: tabType
    })

    if (result.success && result.data) {
      statistics.value = result.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

/**
 * 加载审批列表
 */
const loadApprovals = async () => {
  if (loading.value) return

  loading.value = true

  try {
    const tabType = tabs.value[currentTab.value].type
    const params = {
      tabType: tabType,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    // 添加筛选条件
    if (selectedFilter.value > 0) {
      params.approvalStatus = filters.value[selectedFilter.value].value
    }
    if (selectedType.value) {
      params.approvalType = selectedType.value
    }
    if (selectedTimeRange.value) {
      params.timeRange = selectedTimeRange.value
    }

    const result = await accessApi.getApprovalList(params)

    if (result.success && result.data) {
      const newList = result.data.list || []

      if (pageNum.value === 1) {
        approvalList.value = newList
      } else {
        approvalList.value.push(...newList)
      }

      hasMore.value = newList.length >= pageSize.value

      // 更新Tab数量
      if (result.data.statistics) {
        tabs.value.forEach((tab, index) => {
          const key = tab.type === 'myApply' ? 'myApplyCount' :
                     tab.type === 'pending' ? 'pendingCount' : 'approvedCount'
          tab.count = result.data.statistics[key] || 0
        })
      }
    }
  } catch (error) {
    console.error('加载审批列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  pageNum.value = 1
  hasMore.value = true
  loadStatistics()
  loadApprovals()
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!hasMore.value || loading.value) return
  pageNum.value++
  loadApprovals()
}

/**
 * 查看详情
 */
const viewDetail = (item) => {
  uni.navigateTo({
    url: `/pages/access/approval-detail?id=${item.approvalId}`
  })
}

/**
 * 审批通过
 */
const approveApproval = async (item) => {
  uni.showModal({
    title: '审批通过',
    content: '确定要通过该申请吗？',
    confirmColor: '#52c41a',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '提交中...', mask: true })

        try {
          const result = await accessApi.approveRequest(item.approvalId, {
            action: 'approve',
            comment: ''
          })

          uni.hideLoading()

          if (result.success) {
            uni.showToast({ title: '已通过', icon: 'success' })
            onRefresh()
          } else {
            uni.showToast({ title: result.message || '操作失败', icon: 'none' })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('审批失败:', error)
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 审批拒绝
 */
const rejectApproval = (item) => {
  uni.showModal({
    title: '审批拒绝',
    content: '确定要拒绝该申请吗？',
    editable: true,
    placeholderText: '请输入拒绝原因',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '提交中...', mask: true })

        try {
          const result = await accessApi.approveRequest(item.approvalId, {
            action: 'reject',
            comment: res.content || ''
          })

          uni.hideLoading()

          if (result.success) {
            uni.showToast({ title: '已拒绝', icon: 'success' })
            onRefresh()
          } else {
            uni.showToast({ title: result.message || '操作失败', icon: 'none' })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('审批失败:', error)
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 显示筛选弹窗
 */
const showFilterPopup = () => {
  $refs.filterPopup.open()
}

/**
 * 关闭筛选弹窗
 */
const closeFilterPopup = () => {
  $refs.filterPopup.close()
}

/**
 * 选择申请类型
 */
const selectType = (value) => {
  selectedType.value = value
}

/**
 * 选择时间范围
 */
const selectTimeRange = (value) => {
  selectedTimeRange.value = value
}

/**
 * 重置筛选
 */
const resetFilters = () => {
  selectedType.value = ''
  selectedTimeRange.value = ''
}

/**
 * 应用筛选
 */
const applyFilters = () => {
  closeFilterPopup()
  pageNum.value = 1
  approvalList.value = []
  hasMore.value = true
  loadApprovals()
}

/**
 * 显示快速操作菜单
 */
const showActionMenu = () => {
  $refs.actionMenuPopup.open()
}

/**
 * 关闭快速操作菜单
 */
const closeActionMenu = () => {
  $refs.actionMenuPopup.close()
}

/**
 * 创建权限申请
 */
const createPermissionApply = () => {
  closeActionMenu()
  uni.navigateTo({
    url: '/pages/access/permission-apply'
  })
}

/**
 * 创建访客预约
 */
const createVisitorAppointment = () => {
  closeActionMenu()
  uni.navigateTo({
    url: '/pages/access/visitor-appointment'
  })
}

/**
 * 创建紧急权限
 */
const createEmergencyPermission = () => {
  closeActionMenu()
  uni.navigateTo({
    url: '/pages/access/emergency-permission'
  })
}

/**
 * 发起申请（空状态按钮）
 */
const createApplication = () => {
  showActionMenu()
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 24小时内
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
}

/**
 * 格式化预期时间
 */
const formatExpectedTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    1: 'status-pending',   // 待审批
    2: 'status-approved',  // 已通过
    3: 'status-rejected'   // 已拒绝
  }
  return classMap[status] || 'status-pending'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    1: '待审批',
    2: '已通过',
    3: '已拒绝'
  }
  return textMap[status] || '未知'
}

/**
 * 获取类型图标
 */
const getTypeIcon = (type) => {
  const iconMap = {
    1: 'locked',        // 权限申请
    2: 'person',        // 访客预约
    3: 'fire'           // 紧急权限
  }
  return iconMap[type] || 'document'
}

/**
 * 获取类型文本
 */
const getTypeText = (type) => {
  const textMap = {
    1: '权限申请',
    2: '访客预约',
    3: '紧急权限'
  }
  return textMap[type] || '其他申请'
}
</script>

<style lang="scss" scoped>
.approval-page {
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

// Tab切换
.tab-container {
  display: flex;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px 0;
    position: relative;

    .tab-text {
      font-size: 15px;
      color: #666;
    }

    &.active {
      .tab-text {
        color: #1890ff;
        font-weight: 500;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40px;
        height: 3px;
        background-color: #1890ff;
        border-radius: 2px;
      }
    }

    .tab-badge {
      position: absolute;
      top: 8px;
      right: 25%;
      min-width: 16px;
      height: 16px;
      line-height: 16px;
      padding: 0 4px;
      background-color: #ff4d4f;
      color: #fff;
      font-size: 10px;
      text-align: center;
      border-radius: 8px;
    }
  }
}

// 统计卡片
.stats-container {
  display: flex;
  gap: 10px;
  padding: 15px;

  .stat-card {
    flex: 1;
    display: flex;
    align-items: center;
    padding: 15px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    &.gradient-blue {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.gradient-green {
      background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
    }

    &.gradient-red {
      background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
    }

    .stat-icon {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: rgba(255, 255, 255, 0.3);
      border-radius: 8px;
      margin-right: 10px;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 20px;
        font-weight: bold;
        color: #fff;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.9);
        margin-top: 4px;
      }
    }
  }
}

// 快速筛选
.filter-bar {
  background-color: #fff;
  padding: 10px 15px;
  border-bottom: 1px solid #eee;

  .filter-scroll {
    white-space: nowrap;

    .filter-chip {
      display: inline-block;
      padding: 6px 16px;
      margin-right: 10px;
      background-color: #f5f5f5;
      border-radius: 16px;
      font-size: 14px;
      color: #666;

      &.active {
        background-color: #e6f7ff;
        color: #1890ff;
      }

      .filter-text {
        font-size: 14px;
      }
    }
  }
}

// 列表容器
.list-container {
  padding: 15px;

  .list-scroll {
    height: calc(100vh - 350px);
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;

  .empty-text {
    font-size: 15px;
    color: #999;
    margin: 20px 0 30px;
  }

  .empty-btn {
    padding: 10px 30px;
    background-color: #1890ff;
    color: #fff;
    border-radius: 20px;
    font-size: 15px;
  }
}

// 审批项
.approval-item {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .applicant-info {
      display: flex;
      align-items: center;

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        margin-right: 10px;
      }

      .applicant-text {
        .applicant-name {
          font-size: 15px;
          font-weight: 500;
          color: #333;
          margin-bottom: 4px;
        }

        .apply-time {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .status-badge {
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;

      &.status-pending {
        background-color: #fff7e6;
        color: #fa8c16;
      }

      &.status-approved {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.status-rejected {
        background-color: #fff1f0;
        color: #ff4d4f;
      }
    }
  }

  .item-type {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background-color: #f5f5f5;
    border-radius: 8px;
    margin-bottom: 12px;

    .type-text {
      margin-left: 8px;
      font-size: 14px;
      color: #1890ff;
      font-weight: 500;
    }
  }

  .item-content {
    margin-bottom: 12px;

    .content-row {
      display: flex;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .content-label {
        font-size: 14px;
        color: #666;
        min-width: 70px;
      }

      .content-value {
        flex: 1;
        font-size: 14px;
        color: #333;
        word-break: break-all;
      }
    }
  }

  .item-footer {
    display: flex;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid #f0f0f0;

    .footer-text {
      margin-left: 6px;
      font-size: 12px;
      color: #999;
    }
  }

  .item-actions {
    display: flex;
    gap: 10px;
    padding-top: 10px;
    border-top: 1px solid #f0f0f0;

    .action-btn {
      flex: 1;
      height: 36px;
      line-height: 36px;
      border-radius: 8px;
      font-size: 14px;
      text-align: center;
      border: none;

      &.reject-btn {
        background-color: #fff1f0;
        color: #ff4d4f;
      }

      &.approve-btn {
        background-color: #f6ffed;
        color: #52c41a;
      }
    }
  }

  .item-comment {
    padding: 10px;
    background-color: #f5f5f5;
    border-radius: 8px;
    margin-top: 10px;

    .comment-label {
      font-size: 13px;
      color: #666;
      margin-right: 8px;
    }

    .comment-text {
      font-size: 13px;
      color: #333;
    }
  }
}

// 加载更多
.load-more {
  padding: 20px;
  text-align: center;

  .loading-text,
  .no-more-text {
    font-size: 13px;
    color: #999;
  }
}

// 筛选弹窗
.filter-popup {
  background-color: #fff;
  border-radius: 16px 16px 0 0;
  max-height: 60vh;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #eee;

    .popup-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    padding: 15px;
    max-height: 40vh;
    overflow-y: auto;

    .filter-section {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-section-title {
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 12px;
      }

      .filter-options {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .option-chip {
          padding: 6px 16px;
          background-color: #f5f5f5;
          border-radius: 16px;
          font-size: 14px;
          color: #666;

          &.active {
            background-color: #e6f7ff;
            color: #1890ff;
          }

          .option-text {
            font-size: 14px;
          }
        }
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 10px;
    padding: 15px;
    border-top: 1px solid #eee;

    .popup-btn {
      flex: 1;
      height: 40px;
      line-height: 40px;
      border-radius: 8px;
      font-size: 15px;
      text-align: center;
      border: none;

      &.reset-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm-btn {
        background-color: #1890ff;
        color: #fff;
      }
    }
  }
}

// 快速操作按钮
.fab-container {
  position: fixed;
  right: 20px;
  bottom: 80px;
  z-index: 99;

  .fab-btn {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
  }
}

// 快速操作菜单
.action-menu {
  background-color: #fff;
  border-radius: 16px 16px 0 0;
  padding-bottom: env(safe-area-inset-bottom);

  .menu-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #eee;

    &:last-of-type {
      border-bottom: none;
    }

    .menu-text {
      margin-left: 12px;
      font-size: 15px;
      color: #333;
    }
  }

  .menu-cancel {
    padding: 15px;
    text-align: center;
    border-top: 8px solid #f5f5f5;

    .cancel-text {
      font-size: 15px;
      color: #666;
    }
  }
}
</style>
