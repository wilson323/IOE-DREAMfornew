<!--
  * 实例列表页面
  * 工作流实例管理
-->
<template>
  <view class="instance-list">
    <!-- 自定义导航栏 -->
    <uni-nav-bar
      :fixed="true"
      :shadow="false"
      :border="false"
      background-color="#ffffff"
      title="流程实例"
      left-icon="left"
      @clickLeft="goBack"
    />

    <!-- 搜索和筛选 -->
    <view class="search-filter-section">
      <view class="search-box">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input
          class="search-input"
          placeholder="搜索流程名称、发起人"
          v-model="searchText"
          @input="handleSearch"
          confirm-type="search"
          @confirm="handleSearch"
        />
      </view>

      <view class="filter-tabs">
        <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
          <view class="filter-list">
            <view
              class="filter-item"
              :class="{ active: currentFilter === filter.value }"
              v-for="filter in filterOptions"
              :key="filter.value"
              @tap="changeFilter(filter.value)"
            >
              {{ filter.label }}
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 流程实例列表 -->
    <view class="instance-list">
      <view
        class="instance-item"
        v-for="instance in filteredInstances"
        :key="instance.instanceId"
        @tap="goToInstanceDetail(instance.instanceId)"
      >
        <!-- 实例头部 -->
        <view class="instance-header">
          <view class="instance-title">{{ instance.title }}</view>
          <view class="instance-status" :class="'status-' + instance.status">
            {{ getStatusText(instance.status) }}
          </view>
        </view>

        <!-- 流程信息 -->
        <view class="workflow-info">
          <view class="info-row">
            <text class="label">流程名称：</text>
            <text class="value">{{ instance.workflowName }}</text>
          </view>
          <view class="info-row">
            <text class="label">发起人：</text>
            <text class="value">{{ instance.initiatorName }}</text>
          </view>
          <view class="info-row">
            <text class="label">当前节点：</text>
            <text class="value">{{ instance.currentNodeName }}</text>
          </view>
        </view>

        <!-- 时间信息 -->
        <view class="time-info">
          <view class="time-row">
            <text class="label">发起时间：</text>
            <text class="value">{{ formatDateTime(instance.startTime) }}</text>
          </view>
          <view class="time-row" v-if="instance.endTime">
            <text class="label">完成时间：</text>
            <text class="value">{{ formatDateTime(instance.endTime) }}</text>
          </view>
          <view class="time-row">
            <text class="label">持续时间：</text>
            <text class="value">{{ calculateDuration(instance.startTime, instance.endTime) }}</text>
          </view>
        </view>

        <!-- 进度条 -->
        <view class="progress-section" v-if="instance.progress && instance.progress.total > 0">
          <view class="progress-header">
            <text class="progress-label">流程进度</text>
            <text class="progress-text">{{ instance.progress.completed }}/{{ instance.progress.total }}</text>
          </view>
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: (instance.progress.completed / instance.progress.total * 100) + '%' }"
            ></view>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="instance-actions" v-if="instance.status === 'running'">
          <button
            class="action-btn pause-btn"
            @tap.stop="handlePause(instance.instanceId)"
          >
            暂停
          </button>
          <button
            class="action-btn cancel-btn"
            @tap.stop="handleCancel(instance.instanceId)"
          >
            终止
          </button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="filteredInstances.length === 0 && !loading">
      <image class="empty-image" src="/static/images/empty/no-data.png" mode="aspectFit"></image>
      <text class="empty-text">暂无流程实例</text>
    </view>

    <!-- 加载状态 -->
    <uni-load-more :status="loadMoreStatus" v-if="filteredInstances.length > 0"></uni-load-more>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'InstanceList',
  setup() {
    // 响应式数据
    const loading = ref(false)
    const searchText = ref('')
    const currentFilter = ref('all')
    const instanceList = ref([])
    const page = ref(1)
    const pageSize = ref(20)
    const loadMoreStatus = ref('more')

    // 筛选选项
    const filterOptions = ref([
      { label: '全部', value: 'all' },
      { label: '运行中', value: 'running' },
      { label: '已完成', value: 'completed' },
      { label: '已暂停', value: 'paused' },
      { label: '已终止', value: 'cancelled' }
    ])

    // 计算属性
    const filteredInstances = computed(() => {
      let instances = instanceList.value

      // 搜索过滤
      if (searchText.value) {
        instances = instances.filter(instance =>
          instance.title.includes(searchText.value) ||
          instance.workflowName.includes(searchText.value) ||
          instance.initiatorName.includes(searchText.value)
        )
      }

      // 状态过滤
      if (currentFilter.value !== 'all') {
        instances = instances.filter(instance => instance.status === currentFilter.value)
      }

      return instances
    })

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const handleSearch = () => {
      loadInstanceList()
    }

    const changeFilter = (value) => {
      currentFilter.value = value
      loadInstanceList()
    }

    const loadInstanceList = async (reset = true) => {
      if (reset) {
        page.value = 1
        instanceList.value = []
        loadMoreStatus.value = 'more'
      }

      loading.value = true

      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))

        const mockInstances = generateMockInstances()
        instanceList.value = reset ? mockInstances : [...instanceList.value, ...mockInstances]

        if (mockInstances.length < pageSize.value) {
          loadMoreStatus.value = 'noMore'
        } else {
          loadMoreStatus.value = 'more'
        }

      } catch (error) {
        console.error('加载实例列表失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        loading.value = false
      }
    }

    const generateMockInstances = () => {
      const instances = []
      const workflows = [
        { name: '员工请假申请流程', id: 'leave_request' },
        { name: '费用报销审批流程', id: 'expense_approval' },
        { name: '加班申请流程', id: 'overtime_request' },
        { name: '出差申请流程', id: 'business_trip' },
        { name: '采购审批流程', id: 'purchase_approval' },
        { name: '合同审批流程', id: 'contract_approval' },
        { name: '招聘审批流程', id: 'recruitment_approval' },
        { name: '资产领用流程', id: 'asset_request' }
      ]
      const statuses = ['running', 'completed', 'paused', 'cancelled']
      const nodes = [
        '发起申请', '部门审批', '主管审批', 'HR审批', '财务审批', '总经理审批', '完成'
      ]

      for (let i = 0; i < 10; i++) {
        const workflow = workflows[Math.floor(Math.random() * workflows.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        const isCompleted = status === 'completed'
        const totalNodes = Math.floor(Math.random() * 5) + 3
        const completedNodes = isCompleted ? totalNodes : Math.floor(Math.random() * totalNodes)
        const startTime = new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
        const endTime = isCompleted ? new Date(startTime.getTime() + Math.random() * 7 * 24 * 60 * 60 * 1000) : null

        instances.push({
          instanceId: `instance_${page.value}_${i + 1}`,
          title: `${workflow.name}_${page.value}_${i + 1}`,
          workflowName: workflow.name,
          workflowId: workflow.id,
          status: status,
          initiatorName: ['张三', '李四', '王五', '赵六'][Math.floor(Math.random() * 4)],
          initiatorDept: ['技术研发部', '市场部', '人力资源部', '财务部'][Math.floor(Math.random() * 4)],
          currentNodeName: nodes[completedNodes],
          startTime: startTime.toISOString(),
          endTime: endTime ? endTime.toISOString() : null,
          progress: {
            total: totalNodes,
            completed: completedNodes
          },
          description: `${workflow.name}的详细描述信息`
        })
      }

      return instances.sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
    }

    const getStatusText = (status) => {
      const map = {
        running: '运行中',
        completed: '已完成',
        paused: '已暂停',
        cancelled: '已终止'
      }
      return map[status] || '未知'
    }

    const calculateDuration = (startTime, endTime) => {
      const start = new Date(startTime)
      const end = endTime ? new Date(endTime) : new Date()
      const diff = end - start
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(hours / 24)

      if (days > 0) {
        return `${days}天${hours % 24}小时`
      } else if (hours > 0) {
        return `${hours}小时`
      } else {
        const minutes = Math.floor(diff / (1000 * 60))
        return `${minutes}分钟`
      }
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

    const goToInstanceDetail = (instanceId) => {
      uni.navigateTo({
        url: `/pages/workflow/instance-detail?instanceId=${instanceId}`
      })
    }

    const handlePause = async (instanceId) => {
      try {
        uni.showLoading({
          title: '处理中...'
        })

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1500))

        // 更新实例状态
        const instanceIndex = instanceList.value.findIndex(instance => instance.instanceId === instanceId)
        if (instanceIndex > -1) {
          instanceList.value[instanceIndex].status = 'paused'
        }

        uni.hideLoading()
        uni.showToast({
          title: '已暂停',
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

    const handleCancel = async (instanceId) => {
      try {
        uni.showModal({
          title: '确认终止',
          content: '确认终止此流程实例吗？',
          success: async (res) => {
            if (res.confirm) {
              uni.showLoading({
                title: '处理中...'
              })

              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1500))

              // 更新实例状态
              const instanceIndex = instanceList.value.findIndex(instance => instance.instanceId === instanceId)
              if (instanceIndex > -1) {
                instanceList.value[instanceIndex].status = 'cancelled'
                instanceList.value[instanceIndex].endTime = new Date().toISOString()
              }

              uni.hideLoading()
              uni.showToast({
                title: '已终止',
                icon: 'success'
              })
            }
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
      loadInstanceList()
    })

    // 返回
    return {
      loading,
      searchText,
      currentFilter,
      instanceList,
      filteredInstances,
      filterOptions,
      loadMoreStatus,
      goBack,
      handleSearch,
      changeFilter,
      goToInstanceDetail,
      handlePause,
      handleCancel,
      getStatusText,
      calculateDuration,
      formatDateTime
    }
  }
}
</script>

<style lang="scss" scoped>
.instance-list {
  background-color: #f5f5f5;
  min-height: 100vh;

  .search-filter-section {
    background-color: #fff;

    .search-box {
      display: flex;
      align-items: center;
      background-color: #f8f8f8;
      padding: 16rpx 32rpx;
      margin-bottom: 2rpx;

      .search-input {
        flex: 1;
        font-size: 28rpx;
        margin-left: 16rpx;
        height: 100%;
      }
    }

    .filter-tabs {
      padding: 0 32rpx;
      margin-bottom: 2rpx;

      .filter-scroll {
        white-space: nowrap;
      }

      .filter-list {
        display: flex;

        .filter-item {
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
  }

  .instance-list {
    padding: 0 16rpx;

    .instance-item {
      background-color: #fff;
      border-radius: 16rpx;
      padding: 24rpx;
      margin-bottom: 16rpx;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

      .instance-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16rpx;

        .instance-title {
          flex: 1;
          font-size: 32rpx;
          font-weight: 500;
          color: #333;
          line-height: 1.4;
          margin-right: 16rpx;
        }

        .instance-status {
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
          font-size: 24rpx;
          white-space: nowrap;

          &.status-running {
            background-color: #e6f7ff;
            color: #1890ff;
          }

          &.status-completed {
            background-color: #f6ffed;
            color: #52c41a;
          }

          &.status-paused {
            background-color: #fff7e6;
            color: #fa8c16;
          }

          &.status-cancelled {
            background-color: #fff2f0;
            color: #ff4d4f;
          }
        }
      }

      .workflow-info {
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

      .time-info {
        margin-bottom: 20rpx;

        .time-row {
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

      .progress-section {
        margin-bottom: 20rpx;

        .progress-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8rpx;

          .progress-label {
            font-size: 26rpx;
            color: #333;
            font-weight: 500;
          }

          .progress-text {
            font-size: 24rpx;
            color: #1890ff;
            font-weight: 500;
          }
        }

        .progress-bar {
          height: 8rpx;
          background-color: #f0f0f0;
          border-radius: 4rpx;
          overflow: hidden;

          .progress-fill {
            height: 100%;
            background-color: #1890ff;
            transition: width 0.3s ease;
          }
        }
      }

      .instance-actions {
        display: flex;
        gap: 16rpx;

        .action-btn {
          flex: 1;
          padding: 12rpx 0;
          border-radius: 8rpx;
          font-size: 26rpx;
          border: none;
          font-weight: 500;

          &.pause-btn {
            background-color: #fa8c16;
            color: #fff;
          }

          &.cancel-btn {
            background-color: #ff4d4f;
            color: #fff;
          }

          &:active {
            opacity: 0.8;
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