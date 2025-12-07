<!--
  * 流程实例详情页 - 移动端
  * 提供工作流流程实例详情查看和管理功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <view class="instance-detail-page">
    <!-- 流程实例基本信息 -->
    <view class="info-card">
      <view class="card-title">流程信息</view>
      <view class="info-row">
        <text class="info-label">流程名称:</text>
        <text class="info-value">{{ instanceDetail.processName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">实例名称:</text>
        <text class="info-value">{{ instanceDetail.instanceName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">状态:</text>
        <view class="status-tag" :class="getStatusClass(instanceDetail.status)">
          {{ getStatusText(instanceDetail.status) }}
        </view>
      </view>
      <view class="info-row">
        <text class="info-label">发起人:</text>
        <text class="info-value">{{ instanceDetail.startUserName }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">开始时间:</text>
        <text class="info-value">{{ instanceDetail.startTime }}</text>
      </view>
      <view class="info-row" v-if="instanceDetail.endTime">
        <text class="info-label">结束时间:</text>
        <text class="info-value">{{ instanceDetail.endTime }}</text>
      </view>
    </view>

    <!-- 流程历史 -->
    <view class="history-card">
      <view class="card-title">流程历史</view>
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
      <view v-else class="empty-text">暂无流程历史</view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions" v-if="instanceDetail.status === 1 || instanceDetail.status === 4">
      <button
        class="action-btn suspend"
        v-if="instanceDetail.status === 1"
        @click="handleSuspend"
      >
        挂起
      </button>
      <button
        class="action-btn activate"
        v-if="instanceDetail.status === 4"
        @click="handleActivate"
      >
        激活
      </button>
      <button
        class="action-btn terminate"
        v-if="instanceDetail.status === 1"
        @click="handleTerminate"
      >
        终止
      </button>
    </view>
  </view>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { useWorkflowStore } from '@/store/workflow';
  import { workflowApi } from '@/api/workflow';

  const workflowStore = useWorkflowStore();

  const instanceDetail = ref({});
  const historyList = ref([]);
  const loading = ref(false);

  /**
   * 查询流程实例详情
   */
  async function queryInstanceDetail() {
    const instanceId = getQueryParam('instanceId');
    if (!instanceId) {
      return;
    }

    try {
      loading.value = true;
      await workflowStore.fetchInstanceDetail(instanceId);
      instanceDetail.value = workflowStore.currentInstance || {};

      await queryProcessHistory(instanceId);
    } catch (err) {
      console.error('查询流程实例详情失败:', err);
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
   * 获取状态文本
   * @param {Number} status - 状态值
   * @returns {String}
   */
  function getStatusText(status) {
    const map = { 1: '进行中', 2: '已完成', 3: '已终止', 4: '已挂起' };
    return map[status] || '未知';
  }

  /**
   * 获取状态样式类
   * @param {Number} status - 状态值
   * @returns {String}
   */
  function getStatusClass(status) {
    const map = { 1: 'running', 2: 'completed', 3: 'terminated', 4: 'suspended' };
    return map[status] || '';
  }

  /**
   * 挂起流程实例
   */
  async function handleSuspend() {
    uni.showModal({
      title: '提示',
      content: '确认挂起此流程实例吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const response = await workflowApi.suspendInstance(instanceDetail.value.instanceId);
            if (response.code === 1 || response.code === 200) {
              uni.showToast({
                title: '挂起成功',
                icon: 'success',
              });
              await queryInstanceDetail();
            }
          } catch (err) {
            console.error('挂起流程实例失败:', err);
          }
        }
      },
    });
  }

  /**
   * 激活流程实例
   */
  async function handleActivate() {
    try {
      const response = await workflowApi.activateInstance(instanceDetail.value.instanceId);
      if (response.code === 1 || response.code === 200) {
        uni.showToast({
          title: '激活成功',
          icon: 'success',
        });
        await queryInstanceDetail();
      }
    } catch (err) {
      console.error('激活流程实例失败:', err);
    }
  }

  /**
   * 终止流程实例
   */
  async function handleTerminate() {
    uni.showModal({
      title: '提示',
      content: '确认终止此流程实例吗？终止后无法恢复。',
      success: async (res) => {
        if (res.confirm) {
          try {
            const response = await workflowApi.terminateInstance(instanceDetail.value.instanceId);
            if (response.code === 1 || response.code === 200) {
              uni.showToast({
                title: '终止成功',
                icon: 'success',
              });
              await queryInstanceDetail();
            }
          } catch (err) {
            console.error('终止流程实例失败:', err);
          }
        }
      },
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

  onLoad(() => {
    queryInstanceDetail();
  });
</script>

<style lang="scss" scoped>
  .instance-detail-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding-bottom: 120rpx;

    .info-card,
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
        }

        .status-tag {
          padding: 4rpx 16rpx;
          border-radius: 8rpx;
          font-size: 22rpx;

          &.running {
            background: #e6f7ff;
            color: #1890ff;
          }

          &.completed {
            background: #f6ffed;
            color: #52c41a;
          }

          &.terminated {
            background: #fff1f0;
            color: #ff4d4f;
          }

          &.suspended {
            background: #fff7e6;
            color: #fa8c16;
          }
        }
      }
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

        &.suspend,
        &.activate {
          background: #fa8c16;
          color: #fff;
        }

        &.terminate {
          background: #ff4d4f;
          color: #fff;
        }
      }
    }
  }
</style>

