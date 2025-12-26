<template>
  <view class="restriction-mobile-page">
    <!-- 操作栏 -->
    <view class="action-bar">
      <u-button type="primary" size="small" @click="handleAdd">
        <u-icon name="plus" />
        新增限制规则
      </u-button>
      <u-button size="small" @click="handleRefresh">
        <u-icon name="reload" />
        刷新
      </u-button>
    </view>

    <!-- 限制规则列表 -->
    <view class="restriction-list">
      <view class="restriction-card" v-for="restriction in restrictionList" :key="restriction.restrictionId">
        <!-- 规则头部 -->
        <view class="rule-header">
          <view class="user-info">
            <text class="user-name">{{ restriction.userName || `用户${restriction.userId}` }}</text>
            <u-tag :text="getRestrictionTypeDesc(restriction.restrictionType)"
                   :type="getRestrictionTypeColor(restriction.restrictionType)" size="mini" />
          </view>
          <u-switch :checked="restriction.enabled === 1"
                   @change="(checked) => handleToggleEnabled(restriction, checked)" />
        </view>

        <!-- 规则信息 -->
        <view class="rule-info">
          <view class="info-item">
            <text class="label">限制区域:</text>
            <text class="area-text">{{ restriction.areaNames || '-' }}</text>
          </view>

          <view class="info-item" v-if="restriction.timeStart && restriction.timeEnd">
            <text class="label">时段限制:</text>
            <text>{{ restriction.timeStart }} - {{ restriction.timeEnd }}</text>
          </view>

          <view class="info-item" v-if="restriction.effectiveDate && restriction.expireDate">
            <text class="label">有效期:</text>
            <text class="date-text">{{ restriction.effectiveDate }} 至 {{ restriction.expireDate }}</text>
          </view>

          <view class="info-item" v-if="restriction.reason">
            <text class="label">限制原因:</text>
            <text class="reason-text">{{ restriction.reason }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="rule-actions">
          <u-button size="mini" @click="handleEdit(restriction)">编辑</u-button>
          <u-button size="mini" type="error" @click="handleDelete(restriction)">删除</u-button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <u-empty v-if="restrictionList.length === 0" mode="data" text="暂无限制规则" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const restrictionList = ref([]);

const loadData = async () => {
  try {
    // TODO: 调用API查询数据
    restrictionList.value = [];
  } catch (error) {
    console.error('[人员限制] 加载失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

const handleAdd = () => {
  uni.navigateTo({ url: '/pages/access/restriction-add' });
};

const handleEdit = (restriction) => {
  uni.navigateTo({ url: `/pages/access/restriction-add?id=${restriction.restrictionId}` });
};

const handleToggleEnabled = async (restriction, checked) => {
  try {
    // TODO: 调用API更新状态
    uni.showToast({ title: checked ? '已启用' : '已禁用', icon: 'success' });
    loadData();
  } catch (error) {
    console.error('[人员限制] 更新状态失败:', error);
    uni.showToast({ title: '更新失败', icon: 'none' });
  }
};

const handleDelete = async (restriction) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该限制规则吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用API删除
          uni.showToast({ title: '删除成功', icon: 'success' });
          loadData();
        } catch (error) {
          console.error('[人员限制] 删除失败:', error);
          uni.showToast({ title: '删除失败', icon: 'none' });
        }
      }
    }
  });
};

const handleRefresh = () => {
  loadData();
};

const getRestrictionTypeDesc = (type) => {
  const map = {
    'BLACKLIST': '黑名单',
    'WHITELIST': '白名单',
    'TIME_BASED': '时段限制'
  };
  return map[type] || type;
};

const getRestrictionTypeColor = (type) => {
  const map = {
    'BLACKLIST': 'error',
    'WHITELIST': 'success',
    'TIME_BASED': 'warning'
  };
  return map[type] || 'info';
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.restriction-mobile-page {
  padding: 20rpx;

  .action-bar {
    display: flex;
    gap: 20rpx;
    margin-bottom: 30rpx;
  }

  .restriction-list {
    .restriction-card {
      background: white;
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

      .rule-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;

        .user-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 12rpx;

          .user-name {
            font-size: 32rpx;
            font-weight: bold;
          }
        }
      }

      .rule-info {
        margin-bottom: 20rpx;

        .info-item {
          display: flex;
          align-items: flex-start;
          margin-bottom: 16rpx;
          font-size: 28rpx;

          .label {
            color: #999;
            margin-right: 16rpx;
            flex-shrink: 0;
          }

          .area-text,
          .date-text,
          .reason-text {
            flex: 1;
            word-break: break-all;
          }

          .reason-text {
            color: #ff4d4f;
          }
        }
      }

      .rule-actions {
        display: flex;
        gap: 16rpx;
      }
    }
  }
}
</style>
