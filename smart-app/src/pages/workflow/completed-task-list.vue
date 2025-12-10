<!--
  * 已完成任务列表页面
  * 工作流已完成任务管理
-->
<template>
  <view class="completed-task-list">
    <!-- 自定义导航栏 -->
    <uni-nav-bar
      :fixed="true"
      :shadow="false"
      :border="false"
      background-color="#ffffff"
      title="已完成任务"
      left-icon="left"
      @clickLeft="goBack"
    />

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-box">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input
          class="search-input"
          placeholder="搜索任务标题、发起人"
          v-model="searchText"
          @input="handleSearch"
          confirm-type="search"
          @confirm="handleSearch"
        />
      </view>
    </view>

    <!-- 时间筛选 -->
    <view class="date-filter-section">
      <scroll-view scroll-x class="date-filter-scroll" show-scrollbar="false">
        <view class="date-filter-tabs">
          <view
            class="date-filter-tab"
            :class="{ active: currentDateFilter === filter.value }"
            v-for="filter in dateFilterOptions"
            :key="filter.value"
            @tap="changeDateFilter(filter.value)"
          >
            {{ filter.label }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 任务列表 -->
    <view class="task-list">
      <view
        class="task-item"
        v-for="task in filteredTasks"
        :key="task.taskId"
        @tap="goToTaskDetail(task.taskId)"
      >
        <!-- 任务头部 -->
        <view class="task-header">
          <view class="task-title">{{ task.title }}</view>
          <view class="task-result" :class="'result-' + task.result">
            {{ getResultText(task.result) }}
          </view>
        </view>

        <!-- 任务信息 -->
        <view class="task-info">
          <view class="info-row">
            <text class="label">发起人：</text>
            <text class="value">{{ task.initiatorName }}</text>
          </view>
          <view class="info-row">
            <text class="label">类型：</text>
            <text class="value">{{ task.typeName }}</text>
          </view>
          <view class="info-row">
            <text class="label">完成时间：</text>
            <text class="value">{{ formatDateTime(task.completeTime) }}</text>
          </view>
        </view>

        <!-- 审批信息 -->
        <view class="approval-info" v-if="task.approverName">
          <view class="approval-header">
            <text class="approval-label">审批人</text>
            <text class="approver-name">{{ task.approverName }}</text>
            <text class="approval-time">{{ formatDateTime(task.approvalTime) }}</text>
          </view>
          <view class="approval-comment" v-if="task.approvalComment">
            <text class="comment-label">审批意见：</text>
            <text class="comment-text">{{ task.approvalComment }}</text>
          </view>
        </view>

        <!-- 任务底部 -->
        <view class="task-footer">
          <view class="task-metrics">
            <view class="metric-item">
              <text class="metric-label">处理时长</text>
              <text class="metric-value">{{ task.processingTime }}</text>
            </view>
            <view class="metric-item">
              <text class="metric-label">优先级</text>
              <text class="metric-value" :class="'priority-' + task.priority">
                {{ getPriorityText(task.priority) }}
              </text>
            </view>
          </view>
          <view class="task-status">
            <text class="status-text">{{ getStatusText(task.status) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="filteredTasks.length === 0 && !loading">
      <image class="empty-image" src="/static/images/empty/no-data.png" mode="aspectFit"></image>
      <text class="empty-text">暂无已完成任务</text>
    </view>

    <!-- 加载状态 -->
    <uni-load-more :status="loadMoreStatus" v-if="filteredTasks.length > 0"></uni-load-more>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'CompletedTaskList',
  setup() {
    // 响应式数据
    const loading = ref(false)
    const searchText = ref('')
    const currentDateFilter = ref('all')
    const taskList = ref([])
    const page = ref(1)
    const pageSize = ref(20)
    const loadMoreStatus = ref('more')

    // 时间筛选选项
    const dateFilterOptions = ref([
      { label: '全部', value: 'all' },
      { label: '今天', value: 'today' },
      { label: '本周', value: 'week' },
      { label: '本月', value: 'month' },
      { label: '更早', value: 'earlier' }
    ])

    // 计算属性
    const filteredTasks = computed(() => {
      let tasks = taskList.value

      // 搜索过滤
      if (searchText.value) {
        tasks = tasks.filter(task =>
          task.title.includes(searchText.value) ||
          task.initiatorName.includes(searchText.value) ||
          task.approverName.includes(searchText.value)
        )
      }

      // 时间过滤
      if (currentDateFilter.value !== 'all') {
        const now = new Date()
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        const weekStart = new Date(today)
        weekStart.setDate(today.getDate() - today.getDay())
        const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)

        tasks = tasks.filter(task => {
          const taskDate = new Date(task.completeTime)

          switch (currentDateFilter.value) {
            case 'today':
              return taskDate >= today
            case 'week':
              return taskDate >= weekStart
            case 'month':
              return taskDate >= monthStart
            case 'earlier':
              return taskDate < monthStart
            default:
              return true
          }
        })
      }

      return tasks
    })

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const handleSearch = () => {
      loadTaskList()
    }

    const changeDateFilter = (value) => {
      currentDateFilter.value = value
      loadTaskList()
    }

    const loadTaskList = async (reset = true) => {
      if (reset) {
        page.value = 1
        taskList.value = []
        loadMoreStatus.value = 'more'
      }

      loading.value = true

      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))

        const mockTasks = generateMockTasks()
        taskList.value = reset ? mockTasks : [...taskList.value, ...mockTasks]

        if (mockTasks.length < pageSize.value) {
          loadMoreStatus.value = 'noMore'
        } else {
          loadMoreStatus.value = 'more'
        }

      } catch (error) {
        console.error('加载任务列表失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        loading.value = false
      }
    }

    const generateMockTasks = () => {
      const tasks = []
      const taskTypes = ['leave', 'expense', 'overtime', 'business_trip', 'other']
      const priorities = ['high', 'medium', 'low']
      const results = ['approved', 'rejected', 'returned']
      const approvers = ['张经理', '李主管', '王总监', '赵主管']
      const comments = [
        '同意申请，请按流程执行',
        '申请材料不完整，请补充后重新提交',
        '符合规定，批准通过',
        '需要补充说明，请返回修改'
      ]

      const now = new Date()

      for (let i = 0; i < 10; i++) {
        const completeTime = new Date(now - Math.random() * 30 * 24 * 60 * 60 * 1000)
        const createTime = new Date(completeTime - Math.random() * 7 * 24 * 60 * 60 * 1000)
        const approvalTime = new Date(completeTime - Math.random() * 60 * 60 * 1000)
        const processingHours = Math.floor((completeTime - createTime) / (1000 * 60 * 60))

        tasks.push({
          taskId: `task_${page.value}_${i + 1}`,
          title: `请假申请 - 员工_${page.value}_${i + 1}`,
          type: taskTypes[Math.floor(Math.random() * taskTypes.length)],
          typeName: ['请假审批', '报销审批', '加班申请', '出差申请', '其他'][Math.floor(Math.random() * 5)],
          priority: priorities[Math.floor(Math.random() * priorities.length)],
          result: results[Math.floor(Math.random() * results.length)],
          status: 'completed',
          initiatorName: ['员工A', '员工B', '员工C', '员工D'][Math.floor(Math.random() * 4)],
          approverName: approvers[Math.floor(Math.random() * approvers.length)],
          approvalComment: comments[Math.floor(Math.random() * comments.length)],
          createTime: createTime.toISOString(),
          completeTime: completeTime.toISOString(),
          approvalTime: approvalTime.toISOString(),
          processingTime: `${processingHours}小时`,
          description: '这是任务的详细描述信息'
        })
      }

      return tasks.sort((a, b) => new Date(b.completeTime) - new Date(a.completeTime))
    }

    const getResultText = (result) => {
      const map = {
        approved: '已同意',
        rejected: '已拒绝',
        returned: '已退回'
      }
      return map[result] || '未知'
    }

    const getPriorityText = (priority) => {
      const map = {
        high: '紧急',
        medium: '普通',
        low: '低级'
      }
      return map[priority] || '普通'
    }

    const getStatusText = (status) => {
      const map = {
        completed: '已完成',
        cancelled: '已取消'
      }
      return map[status] || '未知'
    }

    const formatDateTime = (dateString) => {
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (days === 0) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else if (days === 1) {
        return '昨天'
      } else if (days < 7) {
        return `${days}天前`
      } else {
        return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
      }
    }

    const goToTaskDetail = (taskId) => {
      uni.navigateTo({
        url: `/pages/workflow/task-detail?taskId=${taskId}`
      })
    }

    // 生命周期
    onMounted(() => {
      loadTaskList()
    })

    // 返回
    return {
      loading,
      searchText,
      currentDateFilter,
      taskList,
      filteredTasks,
      dateFilterOptions,
      loadMoreStatus,
      goBack,
      handleSearch,
      changeDateFilter,
      goToTaskDetail,
      getResultText,
      getPriorityText,
      getStatusText,
      formatDateTime
    }
  }
}
</script>

<style lang="scss" scoped>
.completed-task-list {
  background-color: #f5f5f5;
  min-height: 100vh;

  .search-section {
    background-color: #fff;
    padding: 16rpx;
    margin-bottom: 2rpx;

    .search-box {
      display: flex;
      align-items: center;
      background-color: #f8f8f8;
      border-radius: 30rpx;
      padding: 0 30rpx;
      height: 72rpx;

      .search-input {
        flex: 1;
        font-size: 28rpx;
        margin-left: 20rpx;
        height: 100%;
      }
    }
  }

  .date-filter-section {
    background-color: #fff;
    padding: 0 16rpx;
    margin-bottom: 2rpx;

    .date-filter-scroll {
      white-space: nowrap;
    }

    .date-filter-tabs {
      display: flex;

      .date-filter-tab {
        padding: 24rpx 32rpx;
        font-size: 28rpx;
        color: #666;
        position: relative;
        white-space: nowrap;

        &.active {
          color: #1890ff;
          font-weight: 500;

          &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 40rpx;
            height: 4rpx;
            background-color: #1890ff;
            border-radius: 2rpx;
          }
        }
      }
    }
  }

  .task-list {
    padding: 0 16rpx;

    .task-item {
      background-color: #fff;
      border-radius: 16rpx;
      padding: 24rpx;
      margin-bottom: 16rpx;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

      .task-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16rpx;

        .task-title {
          flex: 1;
          font-size: 32rpx;
          font-weight: 500;
          color: #333;
          line-height: 1.4;
          margin-right: 16rpx;
        }

        .task-result {
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
          font-size: 24rpx;

          &.result-approved {
            background-color: #f6ffed;
            color: #52c41a;
          }

          &.result-rejected {
            background-color: #fff2f0;
            color: #ff4d4f;
          }

          &.result-returned {
            background-color: #fff7e6;
            color: #fa8c16;
          }
        }
      }

      .task-info {
        margin-bottom: 16rpx;

        .info-row {
          display: flex;
          margin-bottom: 8rpx;
          font-size: 26rpx;

          .label {
            color: #999;
            min-width: 120rpx;
          }

          .value {
            color: #666;
            flex: 1;
          }
        }
      }

      .approval-info {
        background-color: #f8f9fa;
        border-radius: 8rpx;
        padding: 16rpx;
        margin-bottom: 16rpx;

        .approval-header {
          display: flex;
          align-items: center;
          margin-bottom: 8rpx;
          font-size: 24rpx;

          .approval-label {
            color: #666;
            margin-right: 16rpx;
          }

          .approver-name {
            color: #1890ff;
            font-weight: 500;
            margin-right: 16rpx;
          }

          .approval-time {
            color: #999;
          }
        }

        .approval-comment {
          font-size: 24rpx;

          .comment-label {
            color: #666;
          }

          .comment-text {
            color: #333;
          }
        }
      }

      .task-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .task-metrics {
          display: flex;
          gap: 32rpx;

          .metric-item {
            display: flex;
            flex-direction: column;
            align-items: center;

            .metric-label {
              font-size: 22rpx;
              color: #999;
              margin-bottom: 4rpx;
            }

            .metric-value {
              font-size: 24rpx;
              color: #666;

              &.priority-high {
                color: #ff4d4f;
              }

              &.priority-medium {
                color: #1890ff;
              }

              &.priority-low {
                color: #52c41a;
              }
            }
          }
        }

        .task-status {
          .status-text {
            font-size: 26rpx;
            color: #52c41a;
            font-weight: 500;
          }
        }
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 0;

    .empty-image {
      width: 200rpx;
      height: 200rpx;
      margin-bottom: 32rpx;
    }

    .empty-text {
      color: #999;
      font-size: 28rpx;
    }
  }
}
</style>