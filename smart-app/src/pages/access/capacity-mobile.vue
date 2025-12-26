<template>
  <view class="capacity-mobile-page">
    <!-- 操作栏 -->
    <view class="action-bar">
      <u-button type="primary" size="small" @click="handleAdd">
        <u-icon name="plus" />
        新增容量控制
      </u-button>
      <u-button size="small" @click="handleRefresh">
        <u-icon name="reload" />
        刷新
      </u-button>
    </view>

    <!-- 容量控制列表 -->
    <view class="capacity-list">
      <view class="capacity-card" v-for="capacity in capacityList" :key="capacity.controlId">
        <!-- 区域信息 -->
        <view class="area-header">
          <view class="area-name">{{ capacity.areaName || `区域${capacity.areaId}` }}</view>
          <u-tag :text="getControlModeDesc(capacity.controlMode)" type="warning" size="mini" />
        </view>

        <!-- 容量信息 -->
        <view class="capacity-info">
          <view class="info-item">
            <text class="label">当前人数:</text>
            <text class="count">{{ capacity.currentCount }}</text>
            <text class="separator">/</text>
            <text class="max">{{ capacity.maxCapacity }}</text>
          </view>

          <!-- 进度条 -->
          <view class="progress-bar">
            <u-line-progress
              :percentage="getCapacityPercent(capacity)"
              :activeColor="getProgressColor(capacity)"
              :inactiveColor="#f0f0f0"
              height="20"
            />
          </view>

          <view class="info-item">
            <text class="label">告警阈值:</text>
            <text>{{ capacity.alertThreshold }}%</text>
          </view>

          <view class="info-item">
            <text class="label">进入限制:</text>
            <u-tag :text="capacity.entryBlocked === 1 ? '禁止' : '允许'"
                   :type="capacity.entryBlocked === 1 ? 'error' : 'success'" size="mini" />
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="capacity-actions">
          <u-button size="mini" type="warning" @click="handleReset(capacity)">重置计数</u-button>
          <u-button size="mini" @click="handleEdit(capacity)">编辑</u-button>
          <u-button size="mini" type="error" @click="handleDelete(capacity)">删除</u-button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <u-empty v-if="capacityList.length === 0" mode="data" text="暂无容量控制规则" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const capacityList = ref([]);

const loadData = async () => {
  try {
    // TODO: 调用API查询数据
    capacityList.value = [];
  } catch (error) {
    console.error('[容量控制] 加载失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

const handleAdd = () => {
  uni.navigateTo({ url: '/pages/access/capacity-add' });
};

const handleEdit = (capacity) => {
  uni.navigateTo({ url: `/pages/access/capacity-add?id=${capacity.controlId}` });
};

const handleReset = async (capacity) => {
  uni.showModal({
    title: '确认重置',
    content: '确定要重置当前人数计数吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用API重置计数
          uni.showToast({ title: '重置成功', icon: 'success' });
          loadData();
        } catch (error) {
          console.error('[容量控制] 重置失败:', error);
          uni.showToast({ title: '重置失败', icon: 'none' });
        }
      }
    }
  });
};

const handleDelete = async (capacity) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该容量控制规则吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用API删除
          uni.showToast({ title: '删除成功', icon: 'success' });
          loadData();
        } catch (error) {
          console.error('[容量控制] 删除失败:', error);
          uni.showToast({ title: '删除失败', icon: 'none' });
        }
      }
    }
  });
};

const handleRefresh = () => {
  loadData();
};

const getControlModeDesc = (mode) => {
  const map = { 'STRICT': '严格', 'WARNING': '警告' };
  return map[mode] || mode;
};

const getCapacityPercent = (capacity) => {
  if (capacity.maxCapacity === 0) return 0;
  return Math.round((capacity.currentCount / capacity.maxCapacity) * 100);
};

const getProgressColor = (capacity) => {
  const percent = getCapacityPercent(capacity);
  if (percent >= 100) return '#f5222d';
  if (percent >= 80) return '#faad14';
  return '#52c41a';
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.capacity-mobile-page {
  padding: 20rpx;

  .action-bar {
    display: flex;
    gap: 20rpx;
    margin-bottom: 30rpx;
  }

  .capacity-list {
    .capacity-card {
      background: white;
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

      .area-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;

        .area-name {
          font-size: 32rpx;
          font-weight: bold;
        }
      }

      .capacity-info {
        margin-bottom: 20rpx;

        .info-item {
          display: flex;
          align-items: center;
          margin-bottom: 16rpx;
          font-size: 28rpx;

          .label {
            color: #999;
            margin-right: 16rpx;
          }

          .count {
            font-size: 36rpx;
            font-weight: bold;
            color: #1890ff;
          }

          .separator {
            margin: 0 8rpx;
            color: #999;
          }

          .max {
            color: #999;
          }
        }

        .progress-bar {
          margin-bottom: 20rpx;
        }
      }

      .capacity-actions {
        display: flex;
        gap: 16rpx;
      }
    }
  }
}
</style>
