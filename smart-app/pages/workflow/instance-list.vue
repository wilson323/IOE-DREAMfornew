<!--
  * 流程实例列表页 - 移动端
  * 提供工作流流程实例的查询和查看功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <view class="instance-list-page">
    <!-- 状态筛选 -->
    <view class="status-tabs">
      <text
        v-for="tab in statusTabs"
        :key="tab.key"
        :class="['status-tab', currentStatus === tab.key ? 'active' : '']"
        @click="changeStatus(tab.key)"
      >
        {{ tab.label }}
      </text>
    </view>

    <!-- 流程实例列表 -->
    <scroll-view
      class="instance-list-scroll"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="instance-list" v-if="instanceList.length > 0">
        <view
          class="instance-item"
          v-for="instance in instanceList"
          :key="instance.instanceId"
          @click="handleViewDetail(instance.instanceId)"
        >
          <view class="instance-header">
            <text class="instance-name">{{ instance.instanceName }}</text>
            <view class="status-tag" :class="getStatusClass(instance.status)">
              {{ getStatusText(instance.status) }}
            </view>
          </view>
          <view class="instance-content">
            <text class="process-name">{{ instance.processName }}</text>
            <text class="instance-info">发起人：{{ instance.startUserName }}</text>
            <text class="instance-time">开始时间：{{ instance.startTime }}</text>
          </view>
        </view>
      </view>
      <view class="empty-state" v-else>
        <text class="empty-text">暂无流程实例</text>
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

  const instanceList = ref([]);
  const refreshing = ref(false);
  const loadMoreStatus = ref('more');
  const currentStatus = ref(null);

  const statusTabs = ref([
    { key: null, label: '全部' },
    { key: 1, label: '进行中' },
    { key: 2, label: '已完成' },
    { key: 3, label: '已终止' },
    { key: 4, label: '已挂起' },
  ]);

  /**
   * 查询流程实例列表
   */
  async function queryInstanceList(reset = false) {
    try {
      if (reset) {
        workflowStore.instanceQueryParams.pageNum = 1;
      }

      const params = {
        ...workflowStore.instanceQueryParams,
        status: currentStatus.value,
      };

      await workflowStore.fetchInstanceList(params);
      if (reset) {
        instanceList.value = workflowStore.instanceList;
      } else {
        instanceList.value = [...instanceList.value, ...workflowStore.instanceList];
      }

      const total = workflowStore.instanceTotal;
      const current = instanceList.value.length;
      if (current >= total) {
        loadMoreStatus.value = 'noMore';
      } else {
        loadMoreStatus.value = 'more';
      }
    } catch (err) {
      console.error('查询流程实例列表失败:', err);
      loadMoreStatus.value = 'more';
    }
  }

  /**
   * 下拉刷新
   */
  async function onRefresh() {
    refreshing.value = true;
    await queryInstanceList(true);
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
    workflowStore.instanceQueryParams.pageNum++;
    await queryInstanceList(false);
  }

  /**
   * 切换状态
   * @param {Number} status - 状态值
   */
  function changeStatus(status) {
    currentStatus.value = status;
    queryInstanceList(true);
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
   * 查看详情
   * @param {Number} instanceId - 流程实例ID
   */
  function handleViewDetail(instanceId) {
    uni.navigateTo({
      url: `/pages/workflow/instance-detail?instanceId=${instanceId}`,
    });
  }

  onLoad(() => {
    queryInstanceList(true);
  });

  onShow(() => {
    queryInstanceList(true);
  });
</script>

<style lang="scss" scoped>
  .instance-list-page {
    min-height: 100vh;
    background-color: #f5f5f5;

    .status-tabs {
      display: flex;
      padding: 20rpx;
      background: #fff;
      margin-bottom: 20rpx;
      overflow-x: auto;

      .status-tab {
        padding: 10rpx 20rpx;
        border-radius: 8rpx;
        font-size: 28rpx;
        color: #666;
        background: #f5f5f5;
        white-space: nowrap;
        margin-right: 20rpx;

        &.active {
          color: #1890ff;
          background: #e6f7ff;
        }
      }
    }

    .instance-list-scroll {
      height: calc(100vh - 120rpx);

      .instance-list {
        padding: 0 20rpx;

        .instance-item {
          background: #fff;
          border-radius: 16rpx;
          padding: 30rpx;
          margin-bottom: 20rpx;

          .instance-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20rpx;

            .instance-name {
              font-size: 32rpx;
              font-weight: bold;
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

          .instance-content {
            .process-name {
              display: block;
              font-size: 24rpx;
              color: #999;
              margin-bottom: 10rpx;
            }

            .instance-info,
            .instance-time {
              display: block;
              font-size: 26rpx;
              color: #666;
              margin-bottom: 8rpx;
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

