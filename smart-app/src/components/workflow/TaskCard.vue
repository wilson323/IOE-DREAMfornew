<!--
  * 任务卡片组件 - 移动端
  * 展示任务信息，支持快速操作
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <view class="task-card" @click="handleClick">
    <view class="card-header">
      <view class="task-title-row">
        <text class="task-name">{{ task.taskName }}</text>
        <view class="priority-tag" :class="getPriorityClass(task.priority)">
          {{ getPriorityText(task.priority) }}
        </view>
      </view>
      <text class="process-name">{{ task.processName }}</text>
    </view>

    <view class="card-content">
      <view class="info-row">
        <text class="info-label">发起人:</text>
        <text class="info-value">{{ task.startUserName }}</text>
      </view>
      <view class="info-row" v-if="task.dueDate">
        <text class="info-label">到期时间:</text>
        <text class="info-value" :class="{ 'overdue': task.isOverdue }">
          {{ task.dueDate }}
          <text v-if="task.isOverdue" class="overdue-badge">已过期</text>
        </text>
      </view>
      <view class="info-row">
        <text class="info-label">创建时间:</text>
        <text class="info-value">{{ task.createTime }}</text>
      </view>
    </view>

    <view class="card-footer">
      <button class="quick-btn" @click.stop="handleQuickApprove">快速同意</button>
      <button class="detail-btn" @click.stop="handleViewDetail">查看详情</button>
    </view>
  </view>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({
    /**
     * 任务对象
     * @type {Object}
     */
    task: {
      type: Object,
      required: true,
    },
  });

  const emit = defineEmits(['click', 'quick-approve']);

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
   * 点击卡片
   */
  function handleClick() {
    emit('click', props.task.taskId);
  }

  /**
   * 查看详情
   */
  function handleViewDetail() {
    emit('click', props.task.taskId);
  }

  /**
   * 快速审批
   */
  function handleQuickApprove() {
    emit('quick-approve', props.task.taskId);
  }
</script>

<style lang="scss" scoped>
  .task-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

    .card-header {
      margin-bottom: 20rpx;

      .task-title-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10rpx;

        .task-name {
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
          flex: 1;
        }

        .priority-tag {
          padding: 4rpx 16rpx;
          border-radius: 8rpx;
          font-size: 22rpx;
          margin-left: 20rpx;

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

      .process-name {
        font-size: 24rpx;
        color: #999;
      }
    }

    .card-content {
      margin-bottom: 20rpx;

      .info-row {
        display: flex;
        margin-bottom: 10rpx;
        font-size: 26rpx;

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
      }
    }

    .card-footer {
      display: flex;
      gap: 20rpx;

      .quick-btn,
      .detail-btn {
        flex: 1;
        height: 64rpx;
        line-height: 64rpx;
        text-align: center;
        border-radius: 8rpx;
        font-size: 26rpx;
        border: none;
      }

      .quick-btn {
        background: #1890ff;
        color: #fff;
      }

      .detail-btn {
        background: #f5f5f5;
        color: #666;
      }
    }
  }
</style>

