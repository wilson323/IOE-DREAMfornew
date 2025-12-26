<template>
  <view class="interlock-mobile-page">
    <!-- 操作栏 -->
    <view class="action-bar">
      <u-button type="primary" size="small" @click="handleAdd">
        <u-icon name="plus" />
        新增互锁
      </u-button>
      <u-button size="small" @click="handleRefresh">
        <u-icon name="reload" />
        刷新
      </u-button>
    </view>

    <!-- 规则列表 -->
    <view class="rule-list">
      <view class="rule-card" v-for="rule in ruleList" :key="rule.ruleId">
        <!-- 规则头部 -->
        <view class="rule-header">
          <view class="rule-name">{{ rule.ruleName }}</view>
          <u-switch :checked="rule.enabled === 1" @change="(checked) => handleToggleEnabled(rule, checked)" />
        </view>

        <!-- 规则信息 -->
        <view class="rule-info">
          <view class="info-item">
            <text class="label">互锁模式:</text>
            <u-tag :text="getInterlockModeDesc(rule.interlockMode)" type="error" size="mini" />
          </view>
          <view class="info-item">
            <text class="label">区域A-B:</text>
            <text>{{ rule.areaAId }} → {{ rule.areaBId }}</text>
          </view>
          <view class="info-item">
            <text class="label">解锁条件:</text>
            <u-tag :text="getUnlockConditionDesc(rule.unlockCondition)" type="primary" size="mini" />
          </view>
          <view class="info-item" v-if="rule.unlockDelaySeconds">
            <text class="label">延迟:</text>
            <text>{{ rule.unlockDelaySeconds }}秒</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="rule-actions">
          <u-button size="mini" type="primary" @click="handleTest(rule)">测试</u-button>
          <u-button size="mini" @click="handleEdit(rule)">编辑</u-button>
          <u-button size="mini" type="error" @click="handleDelete(rule)">删除</u-button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <u-empty v-if="ruleList.length === 0" mode="data" text="暂无互锁规则" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// import { advancedApi } from '@/api/business/access/access-api';

const ruleList = ref([]);

const loadData = async () => {
  try {
    // TODO: 调用API查询数据
    // const res = await advancedApi.queryInterlockRules({ pageNum: 1, pageSize: 100 });
    // if (res.code === 200) {
    //   ruleList.value = res.data.list || [];
    // }
    ruleList.value = [];
  } catch (error) {
    console.error('[互锁管理] 加载失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

const handleAdd = () => {
  uni.navigateTo({ url: '/pages/access/interlock-add' });
};

const handleEdit = (rule) => {
  uni.navigateTo({ url: `/pages/access/interlock-add?id=${rule.ruleId}` });
};

const handleTest = async (rule) => {
  try {
    // TODO: 调用API测试
    uni.showToast({ title: '测试成功', icon: 'success' });
  } catch (error) {
    console.error('[互锁管理] 测试失败:', error);
    uni.showToast({ title: '测试失败', icon: 'none' });
  }
};

const handleToggleEnabled = async (rule, checked) => {
  try {
    // TODO: 调用API更新状态
    uni.showToast({ title: checked ? '已启用' : '已禁用', icon: 'success' });
    loadData();
  } catch (error) {
    console.error('[互锁管理] 更新状态失败:', error);
    uni.showToast({ title: '更新失败', icon: 'none' });
  }
};

const handleDelete = async (rule) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该互锁规则吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用API删除
          uni.showToast({ title: '删除成功', icon: 'success' });
          loadData();
        } catch (error) {
          console.error('[互锁管理] 删除失败:', error);
          uni.showToast({ title: '删除失败', icon: 'none' });
        }
      }
    }
  });
};

const handleRefresh = () => {
  loadData();
};

const getInterlockModeDesc = (mode) => {
  const map = { 'BIDIRECTIONAL': '双向互锁', 'UNIDIRECTIONAL': '单向互锁' };
  return map[mode] || mode;
};

const getUnlockConditionDesc = (condition) => {
  const map = { 'MANUAL': '手动', 'TIMER': '定时', 'AUTO': '自动' };
  return map[condition] || condition;
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.interlock-mobile-page {
  padding: 20rpx;

  .action-bar {
    display: flex;
    gap: 20rpx;
    margin-bottom: 30rpx;
  }

  .rule-list {
    .rule-card {
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

        .rule-name {
          font-size: 32rpx;
          font-weight: bold;
        }
      }

      .rule-info {
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

      .rule-actions {
        display: flex;
        gap: 16rpx;
      }
    }
  }
}
</style>
