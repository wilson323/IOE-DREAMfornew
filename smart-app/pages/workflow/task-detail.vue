<!--
  * 任务详情页 - 移动端
  * 提供工作流任务详情查看和审批功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <view class="task-detail-page">
    <!-- 任务基本信息 -->
    <view class="info-card">
      <view class="card-title">任务信息</view>
      <view class="info-row">
        <text class="info-label">任务名称:</text>
        <text class="info-value">{{ taskDetail.taskName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">流程名称:</text>
        <text class="info-value">{{ taskDetail.processName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">发起人:</text>
        <text class="info-value">{{ taskDetail.startUserName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">优先级:</text>
        <view class="priority-tag" :class="getPriorityClass(taskDetail.priority)">
          {{ getPriorityText(taskDetail.priority) }}
        </view>
      </view>
      <view class="info-row" v-if="taskDetail.dueDate">
        <text class="info-label">到期时间:</text>
        <text class="info-value" :class="{ 'overdue': taskDetail.isOverdue }">
          {{ taskDetail.dueDate }}
          <text v-if="taskDetail.isOverdue" class="overdue-badge">已过期</text>
        </text>
      </view>
    </view>

    <!-- 流程进度 -->
    <view class="progress-card">
      <view class="card-title">流程进度</view>
      <uni-steps
        v-if="historyList.length > 0"
        :options="stepOptions"
        :active="currentStep"
        active-color="#1890ff"
      />
      <view v-else class="empty-text">暂无流程进度信息</view>
    </view>

    <!-- 申请内容 -->
    <view class="content-card">
      <view class="card-title">申请内容</view>
      <view class="content-text" v-html="taskDetail.formData || '暂无申请内容'"></view>
    </view>

    <!-- 审批历史 -->
    <view class="history-card">
      <view class="card-title">审批历史</view>
      <uni-timeline v-if="historyList.length > 0">
        <uni-timeline-item
          v-for="(item, index) in historyList"
          :key="index"
          :timestamp="item.endTime || item.startTime"
        >
          <view class="history-item">
            <text class="history-node">{{ item.nodeName }}</text>
            <text class="history-user">处理人：{{ item.assigneeName }}</text>
            <text class="history-comment" v-if="item.comment">{{ item.comment }}</text>
          </view>
        </uni-timeline-item>
      </uni-timeline>
      <view v-else class="empty-text">暂无审批历史</view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions" v-if="!taskDetail.completed">
      <button class="action-btn reject" @click="handleReject">驳回</button>
      <button class="action-btn approve" @click="handleApprove">同意</button>
      <button class="action-btn detail" @click="goToApproval">详细审批</button>
    </view>
  </view>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { useWorkflowStore } from '@/store/workflow';

  const workflowStore = useWorkflowStore();

  const taskDetail = ref({});
  const historyList = ref([]);
  const loading = ref(false);

  /**
   * 当前步骤索引
   */
  const currentStep = computed(() => {
    if (!historyList.value || historyList.value.length === 0) {
      return 0;
    }
    const index = historyList.value.findIndex((item) => !item.endTime);
    return index >= 0 ? index : historyList.value.length - 1;
  });

  /**
   * 步骤选项
   */
  const stepOptions = computed(() => {
    return historyList.value.map((item) => ({
      title: item.nodeName,
      desc: item.assigneeName,
    }));
  });

  /**
   * 查询任务详情
   */
  async function queryTaskDetail() {
    const taskId = uni.getStorageSync('currentTaskId') || getQueryParam('taskId');
    if (!taskId) {
      return;
    }

    try {
      loading.value = true;
      await workflowStore.fetchTaskDetail(taskId);
      taskDetail.value = workflowStore.currentTask || {};

      if (taskDetail.value.instanceId) {
        await queryProcessHistory(taskDetail.value.instanceId);
      }
    } catch (err) {
      console.error('查询任务详情失败:', err);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 查询流程历史
   * @param {Number} instanceId - 流程实例ID
   */
  async function queryProcessHistory(instanceId) {
    try {
      await workflowStore.fetchProcessHistory(instanceId);
      historyList.value = workflowStore.processHistory || [];
    } catch (err) {
      console.error('查询流程历史失败:', err);
    }
  }

  /**
   * 获取优先级文本
   * @param {Number} priority - 优先级
   * @returns {String}
   */
  function getPriorityText(priority) {
    const map = { 1: '低', 2: '普通', 3: '高', 4: '紧急' };
    return map[priority] || '普通';
  }

  /**
   * 获取优先级样式类
   * @param {Number} priority - 优先级
   * @returns {String}
   */
  function getPriorityClass(priority) {
    const map = { 1: 'low', 2: 'normal', 3: 'high', 4: 'urgent' };
    return map[priority] || 'normal';
  }

  /**
   * 同意审批
   */
  function handleApprove() {
    uni.navigateTo({
      url: `/pages/workflow/task-approval?taskId=${taskDetail.value.taskId}&action=approve`,
    });
  }

  /**
   * 驳回
   */
  function handleReject() {
    uni.navigateTo({
      url: `/pages/workflow/task-approval?taskId=${taskDetail.value.taskId}&action=reject`,
    });
  }

  /**
   * 详细审批
   */
  function goToApproval() {
    uni.navigateTo({
      url: `/pages/workflow/task-approval?taskId=${taskDetail.value.taskId}`,
    });
  }

  /**
   * 获取URL参数
   * @param {String} key - 参数名
   * @returns {String}
   */
  function getQueryParam(key) {
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    return currentPage.options[key];
  }

  onLoad((options) => {
    if (options.taskId) {
      uni.setStorageSync('currentTaskId', options.taskId);
    }
    queryTaskDetail();
  });
</script>

<style lang="scss" scoped>
  .task-detail-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding-bottom: 120rpx;

    .info-card,
    .progress-card,
    .content-card,
    .history-card {
      background: #fff;
      border-radius: 16rpx;
      padding: 30rpx;
      margin: 20rpx;

      .card-title {
        font-size: 32rpx;
        font-weight: bold;
        margin-bottom: 30rpx;
        padding-bottom: 20rpx;
        border-bottom: 1px solid #f0f0f0;
      }

      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;
        font-size: 28rpx;

        .info-label {
          color: #666;
          width: 140rpx;
        }

        .info-value {
          color: #333;
          flex: 1;

          &.overdue {
            color: #ff4d4f;

            .overdue-badge {
              margin-left: 10rpx;
              padding: 2rpx 8rpx;
              background: #ff4d4f;
              color: #fff;
              border-radius: 4rpx;
              font-size: 20rpx;
            }
          }
        }

        .priority-tag {
          padding: 4rpx 16rpx;
          border-radius: 8rpx;
          font-size: 22rpx;

          &.low {
            background: #f0f0f0;
            color: #999;
          }

          &.normal {
            background: #e6f7ff;
            color: #1890ff;
          }

          &.high {
            background: #fff7e6;
            color: #fa8c16;
          }

          &.urgent {
            background: #fff1f0;
            color: #ff4d4f;
          }
        }
      }
    }

    .content-text {
      font-size: 28rpx;
      color: #333;
      line-height: 1.6;
    }

    .history-item {
      .history-node {
        display: block;
        font-size: 28rpx;
        font-weight: bold;
        margin-bottom: 8rpx;
      }

      .history-user {
        display: block;
        font-size: 24rpx;
        color: #666;
        margin-bottom: 8rpx;
      }

      .history-comment {
        display: block;
        font-size: 26rpx;
        color: #333;
      }
    }

    .empty-text {
      font-size: 28rpx;
      color: #999;
      text-align: center;
      padding: 40rpx 0;
    }

    .bottom-actions {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      display: flex;
      gap: 20rpx;
      padding: 20rpx;
      background: #fff;
      box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.1);

      .action-btn {
        flex: 1;
        height: 80rpx;
        line-height: 80rpx;
        text-align: center;
        border-radius: 8rpx;
        font-size: 28rpx;
        border: none;

        &.approve {
          background: #1890ff;
          color: #fff;
        }

        &.reject {
          background: #ff4d4f;
          color: #fff;
        }

        &.detail {
          background: #f5f5f5;
          color: #666;
        }
      }
    }
  }
</style>

