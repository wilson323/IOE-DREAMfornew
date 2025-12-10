<template>
  <view class="oa-workflow-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">工作流</text>
        <text class="add-btn" @click="showProcessMenu = true">+</text>
      </view>
    </view>

    <!-- 工作流统计概览 -->
    <view class="workflow-overview">
      <view class="overview-grid">
        <view class="overview-card">
          <text class="card-value">{{ statistics.pendingCount || 0 }}</text>
          <text class="card-label">待办任务</text>
        </view>
        <view class="overview-card">
          <text class="card-value">{{ statistics.inProgressCount || 0 }}</text>
          <text class="card-label">进行中</text>
        </view>
        <view class="overview-card">
          <text class="card-value">{{ statistics.completedCount || 0 }}</text>
          <text class="card-label">已完成</text>
        </view>
        <view class="overview-card">
          <text class="card-value">{{ statistics.myProcessCount || 0 }}</text>
          <text class="card-label">我的流程</text>
        </view>
      </view>
    </view>

    <!-- 标签页 -->
    <view class="tabs-container">
      <view class="tabs">
        <view
          :class="['tab-item', activeTab === 'pending' ? 'active' : '']"
          @click="switchTab('pending')"
        >
          <text class="tab-text">待办任务</text>
          <view v-if="statistics.pendingCount > 0" class="tab-badge">
            {{ statistics.pendingCount }}
          </view>
          <view class="tab-indicator"></view>
        </view>
        <view
          :class="['tab-item', activeTab === 'completed' ? 'active' : '']"
          @click="switchTab('completed')"
        >
          <text class="tab-text">已办任务</text>
          <view class="tab-indicator"></view>
        </view>
        <view
          :class="['tab-item', activeTab === 'myProcess' ? 'active' : '']"
          @click="switchTab('myProcess')"
        >
          <text class="tab-text">我的流程</text>
          <view class="tab-indicator"></view>
        </view>
        <view
          :class="['tab-item', activeTab === 'initiated' ? 'active' : '']"
          @click="switchTab('initiated')"
        >
          <text class="tab-text">我发起的</text>
          <view class="tab-indicator"></view>
        </view>
      </view>
    </view>

    <!-- 任务列表 -->
    <scroll-view
      class="task-list-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 待办任务列表 -->
      <view v-if="activeTab === 'pending'" class="task-list">
        <view
          v-for="task in pendingTaskList"
          :key="task.taskId"
          class="task-card urgent"
          @click="handleTask(task)"
        >
          <view class="task-priority">
            <view :class="['priority-dot', task.priority]"></view>
          </view>
          <view class="task-content">
            <view class="task-header">
              <text class="task-title">{{ task.taskName }}</text>
              <view :class="['task-urgency', task.urgency]">
                {{ getUrgencyText(task.urgency) }}
              </view>
            </view>
            <text class="task-process">{{ task.processName }}</text>
            <view class="task-meta">
              <text class="meta-item">发起人：{{ task.startUser }}</text>
              <text class="meta-item">{{ formatTime(task.createTime) }}</text>
            </view>
          </view>
          <view class="task-actions" @click.stop>
            <button class="action-btn approve" @click="quickApprove(task)">
              通过
            </button>
            <button class="action-btn reject" @click="quickReject(task)">
              驳回
            </button>
          </view>
        </view>
      </view>

      <!-- 已办任务列表 -->
      <view v-if="activeTab === 'completed'" class="task-list">
        <view
          v-for="task in completedTaskList"
          :key="task.taskId"
          class="task-card"
          @click="viewTaskDetail(task)"
        >
          <view class="task-content">
            <view class="task-header">
              <text class="task-title">{{ task.taskName }}</text>
              <view :class="['task-status', task.status]">
                {{ getStatusText(task.status) }}
              </view>
            </view>
            <text class="task-process">{{ task.processName }}</text>
            <view class="task-meta">
              <text class="meta-item">完成时间：{{ formatTime(task.completeTime) }}</text>
              <text class="meta-item">{{ task.comment || '无备注' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 我的流程列表 -->
      <view v-if="activeTab === 'myProcess'" class="process-list">
        <view
          v-for="process in myProcessList"
          :key="process.processId"
          class="process-card"
          @click="viewProcessDetail(process)"
        >
          <view class="process-content">
            <view class="process-header">
              <text class="process-title">{{ process.processName }}</text>
              <view :class="['process-status', process.status]">
                {{ getProcessStatusText(process.status) }}
              </view>
            </view>
            <view class="process-progress">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: (process.progress || 0) + '%' }"
                ></view>
              </view>
              <text class="progress-text">{{ process.progress || 0 }}%</text>
            </view>
            <view class="process-meta">
              <text class="meta-item">{{ formatTime(process.createTime) }}</text>
              <text class="meta-item">{{ process.currentNode || '未知节点' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 我发起的列表 -->
      <view v-if="activeTab === 'initiated'" class="process-list">
        <view
          v-for="process in initiatedProcessList"
          :key="process.processId"
          class="process-card"
          @click="viewProcessDetail(process)"
        >
          <view class="process-content">
            <view class="process-header">
              <text class="process-title">{{ process.processName }}</text>
              <view :class="['process-status', process.status]">
                {{ getProcessStatusText(process.status) }}
              </view>
            </view>
            <text class="process-initiator">发起人：我</text>
            <view class="process-meta">
              <text class="meta-item">{{ formatTime(process.createTime) }}</text>
              <text class="meta-item">当前节点：{{ process.currentNode || '未知' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="getCurrentList().length === 0 && !loading" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无{{ getTabTitle() }}</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="hasMore" class="loading-more">
        <text>加载更多...</text>
      </view>
      <view v-else-if="getCurrentList().length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 发起流程菜单 -->
    <view class="process-menu-modal" v-if="showProcessMenu" @click="showProcessMenu = false">
      <view class="menu-content" @click.stop>
        <text class="menu-title">发起流程</text>
        <view class="menu-list">
          <view
            v-for="process in availableProcesses"
            :key="process.id"
            class="menu-item"
            @click="startProcess(process)"
          >
            <text class="menu-icon">{{ process.icon }}</text>
            <text class="menu-name">{{ process.name }}</text>
            <text class="menu-desc">{{ process.description }}</text>
          </view>
        </view>
        <button class="close-menu-btn" @click="showProcessMenu = false">取消</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import oaApi from '@/api/business/oa/oa-api.js'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const userStore = useUserStore()

// 页面状态
const activeTab = ref('pending')
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const showProcessMenu = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)

// 数据
const statistics = reactive({
  pendingCount: 0,
  inProgressCount: 0,
  completedCount: 0,
  myProcessCount: 0
})

const pendingTaskList = ref([])
const completedTaskList = ref([])
const myProcessList = ref([])
const initiatedProcessList = ref([])

// 可用流程
const availableProcesses = ref([
  { id: 'leave', name: '请假申请', icon: '🏖️', description: '请假流程申请' },
  { id: 'expense', name: '费用报销', icon: '💰', description: '费用报销申请' },
  { id: 'purchase', name: '采购申请', icon: '🛒', description: '物品采购申请' },
  { id: 'travel', name: '出差申请', icon: '✈️', description: '出差流程申请' },
  { id: 'overtime', name: '加班申请', icon: '⏰', description: '加班流程申请' },
  { id: 'resign', name: '离职申请', icon: '👋', description: '离职流程申请' }
])

// 计算属性
const getCurrentList = computed(() => {
  switch (activeTab.value) {
    case 'pending': return pendingTaskList.value
    case 'completed': return completedTaskList.value
    case 'myProcess': return myProcessList.value
    case 'initiated': return initiatedProcessList.value
    default: return []
  }
})

// 页面生命周期
onMounted(() => {
  init()
})

onShow(() => {
  loadStatistics()
  loadCurrentTabData()
})

onPullDownRefresh(() => {
  loadCurrentTabData()
  uni.stopPullDownRefresh()
})

// 初始化
const init = async () => {
  await loadStatistics()
  await loadCurrentTabData()
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await oaApi.getTaskStatistics()
    if (res.code === 1 && res.data) {
      Object.assign(statistics, res.data)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
  pageNum.value = 1
  loadCurrentTabData()
}

// 加载当前标签数据
const loadCurrentTabData = async () => {
  try {
    loading.value = true

    switch (activeTab.value) {
      case 'pending':
        await loadPendingTasks()
        break
      case 'completed':
        await loadCompletedTasks()
        break
      case 'myProcess':
        await loadMyProcesses()
        break
      case 'initiated':
        await loadInitiatedProcesses()
        break
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载待办任务
const loadPendingTasks = async (append = false) => {
  try {
    const res = await oaApi.getPendingTaskList(pageNum.value, pageSize.value)
    if (res.code === 1 && res.data) {
      const newTasks = res.data.records || []

      if (append) {
        pendingTaskList.value = [...pendingTaskList.value, ...newTasks]
      } else {
        pendingTaskList.value = newTasks
      }

      hasMore.value = newTasks.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载待办任务失败:', error)
  }
}

// 加载已办任务
const loadCompletedTasks = async (append = false) => {
  try {
    const res = await oaApi.getCompletedTaskList(pageNum.value, pageSize.value)
    if (res.code === 1 && res.data) {
      const newTasks = res.data.records || []

      if (append) {
        completedTaskList.value = [...completedTaskList.value, ...newTasks]
      } else {
        completedTaskList.value = newTasks
      }

      hasMore.value = newTasks.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载已办任务失败:', error)
  }
}

// 加载我的流程
const loadMyProcesses = async (append = false) => {
  try {
    const res = await oaApi.getMyProcessList(pageNum.value, pageSize.value)
    if (res.code === 1 && res.data) {
      const newProcesses = res.data.records || []

      if (append) {
        myProcessList.value = [...myProcessList.value, ...newProcesses]
      } else {
        myProcessList.value = newProcesses
      }

      hasMore.value = newProcesses.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载我的流程失败:', error)
  }
}

// 加载我发起的流程
const loadInitiatedProcesses = async (append = false) => {
  try {
    const res = await oaApi.getInitiatedProcessList(pageNum.value, pageSize.value)
    if (res.code === 1 && res.data) {
      const newProcesses = res.data.records || []

      if (append) {
        initiatedProcessList.value = [...initiatedProcessList.value, ...newProcesses]
      } else {
        initiatedProcessList.value = newProcesses
      }

      hasMore.value = newProcesses.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载我发起的流程失败:', error)
  }
}

// 处理任务
const handleTask = (task) => {
  uni.navigateTo({
    url: `/pages/oa/task-detail?taskId=${task.taskId}`
  })
}

// 快速通过
const quickApprove = async (task) => {
  try {
    const res = await oaApi.approveTask(task.taskId, {
      comment: '快速通过',
      approved: true
    })

    if (res.code === 1) {
      uni.showToast({ title: '审批成功', icon: 'success' })
      uni.vibrateShort()
      loadPendingTasks()
      loadStatistics()
    }
  } catch (error) {
    console.error('审批失败:', error)
    uni.showToast({ title: '审批失败', icon: 'none' })
  }
}

// 快速驳回
const quickReject = async (task) => {
  try {
    const res = await oaApi.approveTask(task.taskId, {
      comment: '快速驳回',
      approved: false
    })

    if (res.code === 1) {
      uni.showToast({ title: '驳回成功', icon: 'success' })
      uni.vibrateShort()
      loadPendingTasks()
      loadStatistics()
    }
  } catch (error) {
    console.error('驳回失败:', error)
    uni.showToast({ title: '驳回失败', icon: 'none' })
  }
}

// 查看任务详情
const viewTaskDetail = (task) => {
  uni.navigateTo({
    url: `/pages/oa/task-detail?taskId=${task.taskId}`
  })
}

// 查看流程详情
const viewProcessDetail = (process) => {
  uni.navigateTo({
    url: `/pages/oa/process-detail?processId=${process.processId}`
  })
}

// 发起流程
const startProcess = (process) => {
  showProcessMenu.value = false
  uni.navigateTo({
    url: `/pages/oa/process-start?processId=${process.id}&processName=${process.name}`
  })
}

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  pageNum.value = 1
  await loadCurrentTabData()
}

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    pageNum.value++
    loadCurrentTabData()
  }
}

// 获取标签标题
const getTabTitle = () => {
  const titles = {
    pending: '待办任务',
    completed: '已办任务',
    myProcess: '我的流程',
    initiated: '我发起的流程'
  }
  return titles[activeTab.value] || ''
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  }
}

// 获取紧急程度文本
const getUrgencyText = (urgency) => {
  const map = {
    URGENT: '紧急',
    HIGH: '重要',
    MEDIUM: '一般',
    LOW: '低'
  }
  return map[urgency] || '一般'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    APPROVED: '已通过',
    REJECTED: '已驳回',
    COMPLETED: '已完成',
    TERMINATED: '已终止'
  }
  return map[status] || '未知'
}

// 获取流程状态文本
const getProcessStatusText = (status) => {
  const map = {
    RUNNING: '进行中',
    COMPLETED: '已完成',
    TERMINATED: '已终止',
    SUSPENDED: '已暂停'
  }
  return map[status] || '未知'
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.oa-workflow-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-bar {
  background: #fff;
}

.nav-bar {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 32rpx;
  }

  .back-btn {
    font-size: 48rpx;
    color: rgba(0, 0, 0, 0.85);
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }

  .add-btn {
    font-size: 40rpx;
    color: #1890ff;
    font-weight: bold;
  }
}

.workflow-overview {
  padding: 24rpx 32rpx;

  .overview-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;
  }

  .overview-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 16rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .card-value {
      font-size: 40rpx;
      font-weight: 600;
      margin-bottom: 8rpx;
      color: #1890ff;
    }

    .card-label {
      font-size: 22rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }
}

.tabs-container {
  background: #fff;
  border-radius: 0 0 32rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.tabs {
  display: flex;
  padding: 0 32rpx;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 32rpx 0 24rpx;
    position: relative;
    transition: all 0.3s ease;

    .tab-text {
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.65);
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .tab-badge {
      position: absolute;
      top: 20rpx;
      right: 50%;
      transform: translateX(20rpx);
      background: #ff4d4f;
      color: #fff;
      font-size: 20rpx;
      padding: 2rpx 8rpx;
      border-radius: 10rpx;
      min-width: 32rpx;
      height: 32rpx;
      line-height: 28rpx;
      text-align: center;
    }

    .tab-indicator {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 6rpx;
      background: transparent;
      border-radius: 3rpx;
      transition: all 0.3s ease;
    }

    &.active {
      .tab-text {
        color: #1890ff;
        font-weight: 700;
      }

      .tab-indicator {
        background: linear-gradient(90deg, #1890ff 0%, #096dd9 100%);
        width: 64rpx;
      }
    }
  }
}

.task-list-scroll {
  height: calc(100vh - 400rpx);
  padding: 0 32rpx 32rpx;
}

.task-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;

  &.urgent {
    border-left: 8rpx solid #ff4d4f;
  }

  &:active {
    transform: scale(0.98);
  }

  .task-priority {
    position: absolute;
    top: 32rpx;
    right: 32rpx;

    .priority-dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;

      &.URGENT { background: #ff4d4f; }
      &.HIGH { background: #fa8c16; }
      &.MEDIUM { background: #faad14; }
      &.LOW { background: #52c41a; }
    }
  }

  .task-content {
    padding-right: 80rpx;

    .task-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .task-title {
        font-size: 32rpx;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
        flex: 1;
        margin-right: 16rpx;
      }

      .task-urgency {
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 22rpx;
        color: #fff;

        &.URGENT { background: #ff4d4f; }
        &.HIGH { background: #fa8c16; }
        &.MEDIUM { background: #faad14; }
        &.LOW { background: #52c41a; }
      }

      .task-status {
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 22rpx;

        &.APPROVED {
          background: #f6ffed;
          color: #52c41a;
        }

        &.REJECTED {
          background: #fff1f0;
          color: #ff4d4f;
        }

        &.COMPLETED {
          background: #e6f7ff;
          color: #1890ff;
        }
      }
    }

    .task-process {
      font-size: 26rpx;
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 16rpx;
      display: block;
    }

    .task-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 24rpx;

      .meta-item {
        font-size: 24rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }

  .task-actions {
    position: absolute;
    bottom: 32rpx;
    right: 32rpx;
    display: flex;
    gap: 12rpx;

    .action-btn {
      padding: 8rpx 16rpx;
      border: none;
      border-radius: 8rpx;
      font-size: 24rpx;

      &.approve {
        background: #e6fffb;
        color: #00b96b;
      }

      &.reject {
        background: #fff1f0;
        color: #ff4d4f;
      }
    }
  }
}

.process-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  &:active {
    transform: scale(0.98);
  }

  .process-content {
    .process-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      .process-title {
        font-size: 32rpx;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
        flex: 1;
        margin-right: 16rpx;
      }

      .process-status {
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 22rpx;

        &.RUNNING {
          background: #e6fffb;
          color: #00b96b;
        }

        &.COMPLETED {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.TERMINATED {
          background: #fff1f0;
          color: #ff4d4f;
        }

        &.SUSPENDED {
          background: #f0f0f0;
          color: rgba(0, 0, 0, 0.45);
        }
      }
    }

    .process-initiator {
      font-size: 26rpx;
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 16rpx;
      display: block;
    }

    .process-progress {
      display: flex;
      align-items: center;
      margin-bottom: 16rpx;

      .progress-bar {
        flex: 1;
        height: 8rpx;
        background: #f0f0f0;
        border-radius: 4rpx;
        overflow: hidden;
        margin-right: 16rpx;

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #1890ff 0%, #096dd9 100%);
          transition: width 0.3s ease;
        }
      }

      .progress-text {
        font-size: 24rpx;
        color: #1890ff;
        font-weight: 600;
        width: 80rpx;
        text-align: right;
      }
    }

    .process-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 24rpx;

      .meta-item {
        font-size: 24rpx;
        color: rgba(0, 0, 0, 0.45);
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

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 24rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: rgba(0, 0, 0, 0.45);
  }
}

.loading-more,
.no-more {
  text-align: center;
  padding: 32rpx;
  font-size: 28rpx;
  color: rgba(0, 0, 0, 0.45);
}

.process-menu-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
  z-index: 200;

  .menu-content {
    width: 100%;
    max-height: 80vh;
    background: #fff;
    border-radius: 32rpx 32rpx 0 0;
    padding: 48rpx 32rpx;

    .menu-title {
      font-size: 36rpx;
      font-weight: 600;
      display: block;
      margin-bottom: 32rpx;
      text-align: center;
    }

    .menu-list {
      max-height: 60vh;
      overflow-y: auto;

      .menu-item {
        display: flex;
        align-items: center;
        padding: 24rpx 0;
        border-bottom: 1px solid #f0f0f0;

        &:active {
          background: #f5f5f5;
        }

        .menu-icon {
          font-size: 48rpx;
          margin-right: 24rpx;
        }

        .menu-name {
          font-size: 32rpx;
          color: rgba(0, 0, 0, 0.85);
          font-weight: 500;
        }

        .menu-desc {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
          margin-left: auto;
        }
      }
    }

    .close-menu-btn {
      width: 100%;
      height: 88rpx;
      background: #f0f0f0;
      border: none;
      border-radius: 16rpx;
      font-size: 32rpx;
      color: rgba(0, 0, 0, 0.65);
      margin-top: 32rpx;
    }
  }
}
</style>