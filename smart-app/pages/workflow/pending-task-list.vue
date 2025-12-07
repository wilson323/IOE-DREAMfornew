<!--
  * 待办任务列表页 - 移动端
  * 提供工作流待办任务的查询、统计、操作功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <view class="pending-task-list-page">
    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-row">
        <view class="stat-card urgent" @click="filterByPriority(4)">
          <text class="stat-value">{{ taskStats.urgent }}</text>
          <text class="stat-label">紧急</text>
        </view>
        <view class="stat-card high" @click="filterByPriority(3)">
          <text class="stat-value">{{ taskStats.high }}</text>
          <text class="stat-label">高</text>
        </view>
        <view class="stat-card overdue" @click="filterByDueStatus('OVERDUE')">
          <text class="stat-value">{{ taskStats.overdue }}</text>
          <text class="stat-label">已过期</text>
        </view>
        <view class="stat-card total" @click="clearFilter">
          <text class="stat-value">{{ taskStats.total }}</text>
          <text class="stat-label">全部</text>
        </view>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-section">
      <view class="filter-tabs">
        <text
          v-for="tab in filterTabs"
          :key="tab.key"
          :class="['filter-tab', currentFilter === tab.key ? 'active' : '']"
          @click="changeFilter(tab.key)"
        >
          {{ tab.label }}
        </text>
      </view>
      <view class="filter-btn" @click="showFilterModal">
        <text class="filter-icon">筛选</text>
      </view>
    </view>

    <!-- 任务列表 -->
    <scroll-view
      class="task-list-scroll"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="task-list" v-if="taskList.length > 0">
        <TaskCard
          v-for="task in taskList"
          :key="task.taskId"
          :task="task"
          @click="handleViewDetail(task.taskId)"
          @quick-approve="handleQuickApprove"
        />
      </view>
      <view class="empty-state" v-else>
        <text class="empty-text">暂无待办任务</text>
      </view>
      <uni-load-more :status="loadMoreStatus" />
    </scroll-view>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom">
      <view class="filter-modal">
        <view class="filter-modal-header">
          <text class="modal-title">筛选条件</text>
          <text class="modal-close" @click="closeFilterModal">关闭</text>
        </view>
        <view class="filter-modal-content">
          <view class="filter-item">
            <text class="filter-label">流程分类</text>
            <picker
              mode="selector"
              :range="categoryList"
              range-key="label"
              @change="onCategoryChange"
              :value="categoryIndex"
            >
              <view class="picker-view">
                {{ selectedCategory ? selectedCategory.label : '请选择分类' }}
              </view>
            </picker>
          </view>
          <view class="filter-item">
            <text class="filter-label">优先级</text>
            <picker
              mode="selector"
              :range="priorityList"
              range-key="label"
              @change="onPriorityChange"
              :value="priorityIndex"
            >
              <view class="picker-view">
                {{ selectedPriority ? selectedPriority.label : '请选择优先级' }}
              </view>
            </picker>
          </view>
        </view>
        <view class="filter-modal-footer">
          <button class="reset-btn" @click="resetFilter">重置</button>
          <button class="confirm-btn" @click="applyFilter">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import { useWorkflowStore } from '@/store/workflow';
  import TaskCard from '@/components/workflow/TaskCard.vue';

  const workflowStore = useWorkflowStore();

  const taskList = ref([]);
  const refreshing = ref(false);
  const loadMoreStatus = ref('more');
  const currentFilter = ref('all');

  // 筛选相关
  const filterTabs = ref([
    { key: 'all', label: '全部' },
    { key: 'urgent', label: '紧急' },
    { key: 'high', label: '高优先级' },
    { key: 'overdue', label: '已过期' },
  ]);

  const categoryList = ref([
    { label: '全部', value: null },
    { label: '请假申请', value: 'LEAVE' },
    { label: '报销申请', value: 'EXPENSE' },
    { label: '采购申请', value: 'PURCHASE' },
    { label: '合同审批', value: 'CONTRACT' },
    { label: '其他', value: 'OTHER' },
  ]);

  const priorityList = ref([
    { label: '全部', value: null },
    { label: '低', value: 1 },
    { label: '普通', value: 2 },
    { label: '高', value: 3 },
    { label: '紧急', value: 4 },
  ]);

  const selectedCategory = ref(null);
  const selectedPriority = ref(null);
  const categoryIndex = ref(0);
  const priorityIndex = ref(0);
  const filterPopup = ref(null);

  // 统计信息
  const taskStats = computed(() => {
    return workflowStore.pendingTaskStats;
  });

  /**
   * 查询任务列表
   */
  async function queryTaskList(reset = false) {
    try {
      if (reset) {
        workflowStore.pendingTaskQueryParams.pageNum = 1;
      }

      const params = {
        ...workflowStore.pendingTaskQueryParams,
        category: selectedCategory.value?.value,
        priority: selectedPriority.value?.value,
      };

      await workflowStore.fetchPendingTaskList(params);
      if (reset) {
        taskList.value = workflowStore.pendingTaskList;
      } else {
        taskList.value = [...taskList.value, ...workflowStore.pendingTaskList];
      }

      // 更新加载状态
      const total = workflowStore.pendingTaskTotal;
      const current = taskList.value.length;
      if (current >= total) {
        loadMoreStatus.value = 'noMore';
      } else {
        loadMoreStatus.value = 'more';
      }
    } catch (err) {
      console.error('查询待办任务列表失败:', err);
      loadMoreStatus.value = 'more';
    }
  }

  /**
   * 下拉刷新
   */
  async function onRefresh() {
    refreshing.value = true;
    await queryTaskList(true);
    refreshing.value = false;
  }

  /**
   * 加载更多
   */
  async function loadMore() {
    if (loadMoreStatus.value === 'loading' || loadMoreStatus.value === 'noMore') {
      return;
    }

    loadMoreStatus.value = 'loading';
    workflowStore.pendingTaskQueryParams.pageNum++;
    await queryTaskList(false);
  }

  /**
   * 切换筛选标签
   * @param {String} key - 筛选键
   */
  function changeFilter(key) {
    currentFilter.value = key;
    switch (key) {
      case 'urgent':
        filterByPriority(4);
        break;
      case 'high':
        filterByPriority(3);
        break;
      case 'overdue':
        filterByDueStatus('OVERDUE');
        break;
      default:
        clearFilter();
        break;
    }
  }

  /**
   * 按优先级筛选
   * @param {Number} priority - 优先级
   */
  function filterByPriority(priority) {
    selectedPriority.value = priorityList.value.find((p) => p.value === priority) || null;
    priorityIndex.value = priorityList.value.findIndex((p) => p.value === priority);
    queryTaskList(true);
  }

  /**
   * 按到期状态筛选
   * @param {String} dueStatus - 到期状态
   */
  function filterByDueStatus(dueStatus) {
    workflowStore.pendingTaskQueryParams.dueStatus = dueStatus;
    queryTaskList(true);
  }

  /**
   * 清除筛选
   */
  function clearFilter() {
    selectedCategory.value = null;
    selectedPriority.value = null;
    categoryIndex.value = 0;
    priorityIndex.value = 0;
    workflowStore.pendingTaskQueryParams.dueStatus = null;
    queryTaskList(true);
  }

  /**
   * 显示筛选弹窗
   */
  function showFilterModal() {
    filterPopup.value?.open();
  }

  /**
   * 关闭筛选弹窗
   */
  function closeFilterModal() {
    filterPopup.value?.close();
  }

  /**
   * 分类选择变化
   * @param {Event} e - 事件对象
   */
  function onCategoryChange(e) {
    const index = parseInt(e.detail.value);
    categoryIndex.value = index;
    selectedCategory.value = categoryList.value[index];
  }

  /**
   * 优先级选择变化
   * @param {Event} e - 事件对象
   */
  function onPriorityChange(e) {
    const index = parseInt(e.detail.value);
    priorityIndex.value = index;
    selectedPriority.value = priorityList.value[index];
  }

  /**
   * 应用筛选
   */
  function applyFilter() {
    closeFilterModal();
    queryTaskList(true);
  }

  /**
   * 重置筛选
   */
  function resetFilter() {
    selectedCategory.value = null;
    selectedPriority.value = null;
    categoryIndex.value = 0;
    priorityIndex.value = 0;
  }

  /**
   * 查看详情
   * @param {Number} taskId - 任务ID
   */
  function handleViewDetail(taskId) {
    uni.navigateTo({
      url: `/pages/workflow/task-detail?taskId=${taskId}`,
    });
  }

  /**
   * 快速审批
   * @param {Number} taskId - 任务ID
   */
  async function handleQuickApprove(taskId) {
    uni.showModal({
      title: '提示',
      content: '确认同意此任务吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await workflowStore.completeTask(taskId, {
              outcome: '1',
              comment: '快速审批通过',
            });
            await queryTaskList(true);
          } catch (err) {
            console.error('快速审批失败:', err);
          }
        }
      },
    });
  }

  onLoad(() => {
    queryTaskList(true);
  });

  onShow(() => {
    // 页面显示时刷新列表
    queryTaskList(true);
  });
</script>

<style lang="scss" scoped>
  .pending-task-list-page {
    min-height: 100vh;
    background-color: #f5f5f5;

    .stats-section {
      background: #fff;
      padding: 20rpx;
      margin-bottom: 20rpx;

      .stats-row {
        display: flex;
        gap: 20rpx;

        .stat-card {
          flex: 1;
          padding: 30rpx 20rpx;
          border-radius: 12rpx;
          text-align: center;

          &.urgent {
            background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
            color: #fff;
          }

          &.high {
            background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
            color: #fff;
          }

          &.overdue {
            background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
            color: #fff;
          }

          &.total {
            background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
            color: #fff;
          }

          .stat-value {
            display: block;
            font-size: 48rpx;
            font-weight: bold;
            margin-bottom: 10rpx;
          }

          .stat-label {
            display: block;
            font-size: 24rpx;
          }
        }
      }
    }

    .filter-section {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx;
      background: #fff;
      margin-bottom: 20rpx;

      .filter-tabs {
        display: flex;
        gap: 20rpx;
        flex: 1;

        .filter-tab {
          padding: 10rpx 20rpx;
          border-radius: 8rpx;
          font-size: 28rpx;
          color: #666;
          background: #f5f5f5;

          &.active {
            color: #1890ff;
            background: #e6f7ff;
          }
        }
      }

      .filter-btn {
        padding: 10rpx 20rpx;
        font-size: 28rpx;
        color: #1890ff;
      }
    }

    .task-list-scroll {
      height: calc(100vh - 300rpx);

      .task-list {
        padding: 0 20rpx;
      }

      .empty-state {
        padding: 200rpx 0;
        text-align: center;

        .empty-text {
          font-size: 28rpx;
          color: #999;
        }
      }
    }

    .filter-modal {
      background: #fff;
      border-radius: 20rpx 20rpx 0 0;
      padding: 40rpx;

      .filter-modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 40rpx;

        .modal-title {
          font-size: 32rpx;
          font-weight: bold;
        }

        .modal-close {
          font-size: 28rpx;
          color: #1890ff;
        }
      }

      .filter-modal-content {
        .filter-item {
          margin-bottom: 40rpx;

          .filter-label {
            display: block;
            font-size: 28rpx;
            margin-bottom: 20rpx;
            color: #333;
          }

          .picker-view {
            padding: 20rpx;
            border: 1px solid #e8e8e8;
            border-radius: 8rpx;
            font-size: 28rpx;
          }
        }
      }

      .filter-modal-footer {
        display: flex;
        gap: 20rpx;
        margin-top: 40rpx;

        .reset-btn,
        .confirm-btn {
          flex: 1;
          height: 80rpx;
          line-height: 80rpx;
          text-align: center;
          border-radius: 8rpx;
          font-size: 28rpx;
        }

        .reset-btn {
          background: #f5f5f5;
          color: #666;
        }

        .confirm-btn {
          background: #1890ff;
          color: #fff;
        }
      }
    }
  }
</style>

