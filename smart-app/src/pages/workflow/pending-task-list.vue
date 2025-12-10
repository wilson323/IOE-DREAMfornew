<!--
  * 待办任务列表页面
  * 工作流待办任务管理
-->
<template>
  <view class="pending-task-list">
    <!-- 自定义导航栏 -->
    <uni-nav-bar
      :fixed="true"
      :shadow="false"
      :border="false"
      background-color="#ffffff"
      title="待办任务"
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

    <!-- 筛选栏 -->
    <view class="filter-section">
      <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
        <view class="filter-tabs">
          <view
            class="filter-tab"
            :class="{ active: currentFilter === filter.value }"
            v-for="filter in filterOptions"
            :key="filter.value"
            @tap="changeFilter(filter.value)"
          >
            {{ filter.label }}
            <text v-if="filter.count > 0" class="filter-count">({{ filter.count }})</text>
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
          <view class="task-priority" :class="'priority-' + task.priority">
            {{ getPriorityText(task.priority) }}
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
            <text class="label">时间：</text>
            <text class="value">{{ formatDateTime(task.createTime) }}</text>
          </view>
        </view>

        <!-- 任务状态 -->
        <view class="task-footer">
          <view class="task-status">
            <text class="status-dot" :class="'status-' + task.status"></text>
            <text class="status-text">{{ getStatusText(task.status) }}</text>
          </view>
          <view class="task-actions">
            <button
              class="action-btn approve-btn"
              @tap.stop="handleApprove(task.taskId)"
            >
              同意
            </button>
            <button
              class="action-btn reject-btn"
              @tap.stop="handleReject(task.taskId)"
            >
              拒绝
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="filteredTasks.length === 0 && !loading">
      <image class="empty-image" src="/static/images/empty/no-data.png" mode="aspectFit"></image>
      <text class="empty-text">暂无待办任务</text>
    </view>

    <!-- 加载状态 -->
    <uni-load-more :status="loadMoreStatus" v-if="filteredTasks.length > 0"></uni-load-more>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'PendingTaskList',
  setup() {
    // 响应式数据
    const loading = ref(false)
    const searchText = ref('')
    const currentFilter = ref('all')
    const taskList = ref([])
    const page = ref(1)
    const pageSize = ref(20)
    const loadMoreStatus = ref('more')

    // 筛选选项
    const filterOptions = ref([
      { label: '全部', value: 'all', count: 0 },
      { label: '紧急', value: 'urgent', count: 3 },
      { label: '请假审批', value: 'leave', count: 2 },
      { label: '报销审批', value: 'expense', count: 1 },
      { label: '其他', value: 'other', count: 1 }
    ])

    // 计算属性
    const filteredTasks = computed(() => {
      let tasks = taskList.value

      // 搜索过滤
      if (searchText.value) {
        tasks = tasks.filter(task =>
          task.title.includes(searchText.value) ||
          task.initiatorName.includes(searchText.value)
        )
      }

      // 类型过滤
      if (currentFilter.value !== 'all') {
        tasks = tasks.filter(task => task.type === currentFilter.value)
      }

      return tasks
    })

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const handleSearch = () => {
      // 搜索逻辑
      loadTaskList()
    }

    const changeFilter = (value) => {
      currentFilter.value = value
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

        // 更新筛选计数
        updateFilterCounts()

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
      const statuses = ['pending', 'processing']

      for (let i = 0; i < 10; i++) {
        tasks.push({
          taskId: `task_${page.value}_${i + 1}`,
          title: `请假申请 - 张三_${page.value}_${i + 1}`,
          type: taskTypes[Math.floor(Math.random() * taskTypes.length)],
          typeName: ['请假审批', '报销审批', '加班申请', '出差申请', '其他'][Math.floor(Math.random() * 5)],
          priority: priorities[Math.floor(Math.random() * priorities.length)],
          status: statuses[Math.floor(Math.random() * statuses.length)],
          initiatorName: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
          createTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
          description: '这是任务详细描述信息'
        })
      }

      return tasks
    }

    const updateFilterCounts = () => {
      const counts = {
        all: taskList.value.length,
        urgent: taskList.value.filter(t => t.priority === 'high').length,
        leave: taskList.value.filter(t => t.type === 'leave').length,
        expense: taskList.value.filter(t => t.type === 'expense').length,
        other: taskList.value.filter(t => !['leave', 'expense'].includes(t.type)).length
      }

      filterOptions.value.forEach(option => {
        option.count = counts[option.value] || 0
      })
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
        pending: '待处理',
        processing: '处理中',
        completed: '已完成'
      }
      return map[status] || '未知'
    }

    const formatDateTime = (dateString) => {
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (days > 0) {
        return `${days}天前`
      } else if (hours > 0) {
        return `${hours}小时前`
      } else {
        return '刚刚'
      }
    }

    const goToTaskDetail = (taskId) => {
      uni.navigateTo({
        url: `/pages/workflow/task-detail?taskId=${taskId}`
      })
    }

    const handleApprove = async (taskId) => {
      try {
        uni.showLoading({
          title: '处理中...'
        })

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1500))

        // 更新任务状态
        const taskIndex = taskList.value.findIndex(task => task.taskId === taskId)
        if (taskIndex > -1) {
          taskList.value[taskIndex].status = 'completed'
        }

        uni.hideLoading()
        uni.showToast({
          title: '已同意',
          icon: 'success'
        })

      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    const handleReject = async (taskId) => {
      try {
        uni.showActionSheet({
          itemList: ['审批不通过', '返回修改'],
          success: async (res) => {
            uni.showLoading({
              title: '处理中...'
            })

            // 模拟API调用
            await new Promise(resolve => setTimeout(resolve, 1500))

            // 更新任务状态
            const taskIndex = taskList.value.findIndex(task => task.taskId === taskId)
            if (taskIndex > -1) {
              taskList.value[taskIndex].status = 'completed'
            }

            uni.hideLoading()
            uni.showToast({
              title: res.tapIndex === 0 ? '已拒绝' : '已退回',
              icon: 'success'
            })
          }
        })
      } catch (error) {
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    // 生命周期
    onMounted(() => {
      loadTaskList()
    })

    // 返回
    return {
      loading,
      searchText,
      currentFilter,
      taskList,
      filteredTasks,
      filterOptions,
      loadMoreStatus,
      goBack,
      handleSearch,
      changeFilter,
      goToTaskDetail,
      handleApprove,
      handleReject,
      getPriorityText,
      getStatusText,
      formatDateTime
    }
  }
}
</script>

<style lang="scss" scoped>
.pending-task-list {
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

  .filter-section {
    background-color: #fff;
    padding: 0 16rpx;
    margin-bottom: 2rpx;

    .filter-scroll {
      white-space: nowrap;
    }

    .filter-tabs {
      display: flex;

      .filter-tab {
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

        .filter-count {
          color: #ff4d4f;
          font-size: 24rpx;
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

        .task-priority {
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
          font-size: 24rpx;

          &.priority-high {
            background-color: #fff2e8;
            color: #ff4d4f;
          }

          &.priority-medium {
            background-color: #e6f7ff;
            color: #1890ff;
          }

          &.priority-low {
            background-color: #f6ffed;
            color: #52c41a;
          }
        }
      }

      .task-info {
        margin-bottom: 20rpx;

        .info-row {
          display: flex;
          margin-bottom: 8rpx;
          font-size: 26rpx;

          .label {
            color: #999;
            min-width: 100rpx;
          }

          .value {
            color: #666;
            flex: 1;
          }
        }
      }

      .task-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .task-status {
          display: flex;
          align-items: center;

          .status-dot {
            width: 12rpx;
            height: 12rpx;
            border-radius: 50%;
            margin-right: 8rpx;

            &.status-pending {
              background-color: #faad14;
            }

            &.status-processing {
              background-color: #1890ff;
            }

            &.status-completed {
              background-color: #52c41a;
            }
          }

          .status-text {
            font-size: 26rpx;
            color: #666;
          }
        }

        .task-actions {
          display: flex;
          gap: 16rpx;

          .action-btn {
            padding: 12rpx 24rpx;
            border-radius: 8rpx;
            font-size: 26rpx;
            border: none;

            &.approve-btn {
              background-color: #e6f7ff;
              color: #1890ff;
            }

            &.reject-btn {
              background-color: #fff2e8;
              color: #ff4d4f;
            }
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