<!--
  * 已办任务列表页 - 移动端
  * 提供工作流已办任务的查询和查看功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <view class="completed-task-list-page">
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
        <view
          class="task-item"
          v-for="task in taskList"
          :key="task.taskId"
          @click="handleViewDetail(task.taskId)"
        >
          <view class="task-header">
            <text class="task-name">{{ task.taskName }}</text>
            <view class="outcome-tag" :class="getOutcomeClass(task.outcome)">
              {{ getOutcomeText(task.outcome) }}
            </view>
          </view>
          <view class="task-content">
            <text class="process-name">{{ task.processName }}</text>
            <text class="task-info">发起人：{{ task.startUserName }}</text>
            <text class="task-info" v-if="task.comment">处理意见：{{ task.comment }}</text>
            <text class="task-time">{{ task.endTime }}</text>
          </view>
        </view>
      </view>
      <view class="empty-state" v-else>
        <text class="empty-text">暂无已办任务</text>
      </view>
      <uni-load-more :status="loadMoreStatus" />
    </scroll-view>
  </view>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import { useWorkflowStore } from '@/store/workflow';

  const workflowStore = useWorkflowStore();

  const taskList = ref([]);
  const refreshing = ref(false);
  const loadMoreStatus = ref('more');
  const currentFilter = ref('all');

  const filterTabs = ref([
    { key: 'all', label: '全部' },
    { key: 'approved', label: '同意' },
    { key: 'rejected', label: '驳回' },
  ]);

  /**
   * 查询任务列表
   */
  async function queryTaskList(reset = false) {
    try {
      if (reset) {
        workflowStore.completedTaskQueryParams.pageNum = 1;
      }

      const params = {
        ...workflowStore.completedTaskQueryParams,
        outcome: currentFilter.value === 'approved' ? '1' : currentFilter.value === 'rejected' ? '2' : null,
      };

      await workflowStore.fetchCompletedTaskList(params);
      if (reset) {
        taskList.value = workflowStore.completedTaskList;
      } else {
        taskList.value = [...taskList.value, ...workflowStore.completedTaskList];
      }

      const total = workflowStore.completedTaskTotal;
      const current = taskList.value.length;
      if (current >= total) {
        loadMoreStatus.value = 'noMore';
      } else {
        loadMoreStatus.value = 'more';
      }
    } catch (err) {
      console.error('查询已办任务列表失败:', err);
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
    workflowStore.completedTaskQueryParams.pageNum++;
    await queryTaskList(false);
  }

  /**
   * 切换筛选
   * @param {String} key - 筛选键
   */
  function changeFilter(key) {
    currentFilter.value = key;
    queryTaskList(true);
  }

  /**
   * 获取处理结果文本
   * @param {String} outcome - 处理结果
   * @returns {String}
   */
  function getOutcomeText(outcome) {
    const map = { '1': '同意', '2': '驳回', '3': '转办', '4': '委派' };
    return map[outcome] || '未知';
  }

  /**
   * 获取处理结果样式类
   * @param {String} outcome - 处理结果
   * @returns {String}
   */
  function getOutcomeClass(outcome) {
    const map = { '1': 'approved', '2': 'rejected', '3': 'transferred', '4': 'delegated' };
    return map[outcome] || '';
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

  onLoad(() => {
    queryTaskList(true);
  });

  onShow(() => {
    queryTaskList(true);
  });
</script>

<style lang="scss" scoped>
  .completed-task-list-page {
    min-height: 100vh;
    background-color: #f5f5f5;

    .filter-section {
      padding: 20rpx;
      background: #fff;
      margin-bottom: 20rpx;

      .filter-tabs {
        display: flex;
        gap: 20rpx;

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
    }

    .task-list-scroll {
      height: calc(100vh - 120rpx);

      .task-list {
        padding: 0 20rpx;

        .task-item {
          background: #fff;
          border-radius: 16rpx;
          padding: 30rpx;
          margin-bottom: 20rpx;

          .task-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20rpx;

            .task-name {
              font-size: 32rpx;
              font-weight: bold;
              color: #333;
              flex: 1;
            }

            .outcome-tag {
              padding: 4rpx 16rpx;
              border-radius: 8rpx;
              font-size: 22rpx;

              &.approved {
                background: #f6ffed;
                color: #52c41a;
              }

              &.rejected {
                background: #fff1f0;
                color: #ff4d4f;
              }

              &.transferred,
              &.delegated {
                background: #e6f7ff;
                color: #1890ff;
              }
            }
          }

          .task-content {
            .process-name {
              display: block;
              font-size: 24rpx;
              color: #999;
              margin-bottom: 10rpx;
            }

            .task-info {
              display: block;
              font-size: 26rpx;
              color: #666;
              margin-bottom: 8rpx;
            }

            .task-time {
              display: block;
              font-size: 24rpx;
              color: #999;
              margin-top: 10rpx;
            }
          }
        }
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
  }
</style>

