<template>
  <view class="evacuation-mobile-page">
    <!-- 操作栏 -->
    <view class="action-bar">
      <u-button type="primary" size="small" @click="handleAdd">
        <u-icon name="plus" />
        新增疏散点
      </u-button>
      <u-button size="small" @click="handleRefresh">
        <u-icon name="reload" />
        刷新
      </u-button>
    </view>

    <!-- 疏散点列表 -->
    <view class="point-list">
      <view class="point-card" v-for="point in pointList" :key="point.pointId">
        <!-- 疏散点头部 -->
        <view class="point-header">
          <view class="point-name">{{ point.pointName }}</view>
          <u-tag :text="getEvacuationTypeDesc(point.evacuationType)" type="error" size="mini" />
        </view>

        <!-- 疏散点信息 -->
        <view class="point-info">
          <view class="info-item">
            <text class="label">关联区域:</text>
            <text>{{ point.areaName || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="label">关联门数:</text>
            <text>{{ getDoorCount(point.doorIds) }}个</text>
          </view>
          <view class="info-item">
            <text class="label">优先级:</text>
            <text>{{ point.priority }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="point-actions">
          <u-button size="mini" type="error" @click="handleTrigger(point)">一键疏散</u-button>
          <u-button size="mini" @click="handleEdit(point)">编辑</u-button>
          <u-button size="mini" type="info" @click="handleDelete(point)">删除</u-button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <u-empty v-if="pointList.length === 0" mode="data" text="暂无疏散点" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const pointList = ref([]);

const loadData = async () => {
  try {
    // TODO: 调用API查询数据
    pointList.value = [];
  } catch (error) {
    console.error('[疏散管理] 加载失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

const handleAdd = () => {
  uni.navigateTo({ url: '/pages/access/evacuation-add' });
};

const handleEdit = (point) => {
  uni.navigateTo({ url: `/pages/access/evacuation-add?id=${point.pointId}` });
};

const handleTrigger = async (point) => {
  uni.showModal({
    title: '确认疏散',
    content: `确定要触发"${point.pointName}"一键疏散吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用API触发疏散
          uni.showToast({ title: '疏散成功', icon: 'success' });
        } catch (error) {
          console.error('[疏散管理] 疏散失败:', error);
          uni.showToast({ title: '疏散失败', icon: 'none' });
        }
      }
    }
  });
};

const handleDelete = async (point) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该疏散点吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用API删除
          uni.showToast({ title: '删除成功', icon: 'success' });
          loadData();
        } catch (error) {
          console.error('[疏散管理] 删除失败:', error);
          uni.showToast({ title: '删除失败', icon: 'none' });
        }
      }
    }
  });
};

const handleRefresh = () => {
  loadData();
};

const getEvacuationTypeDesc = (type) => {
  const map = { 'FIRE': '火灾', 'EARTHQUAKE': '地震', 'EMERGENCY': '紧急' };
  return map[type] || type;
};

const getDoorCount = (doorIds) => {
  if (!doorIds) return 0;
  try {
    return JSON.parse(doorIds).length;
  } catch {
    return 0;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.evacuation-mobile-page {
  padding: 20rpx;

  .action-bar {
    display: flex;
    gap: 20rpx;
    margin-bottom: 30rpx;
  }

  .point-list {
    .point-card {
      background: white;
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

      .point-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;

        .point-name {
          font-size: 32rpx;
          font-weight: bold;
        }
      }

      .point-info {
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
        }
      }

      .point-actions {
        display: flex;
        gap: 16rpx;
      }
    }
  }
}
</style>
