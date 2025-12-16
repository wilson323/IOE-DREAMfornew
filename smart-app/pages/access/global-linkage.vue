<!--
  * 移动端全局联动管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-12-01
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <view class="global-linkage">
    <!-- 头部状态栏 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="status-content">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="page-title">全局联动</text>
        <view class="toggle-btn" @click="toggleGlobalLinkage">
          <text :class="['toggle-text', globalLinkageEnabled ? 'enabled' : 'disabled']">
            {{ globalLinkageEnabled ? '已启用' : '已禁用' }}
          </text>
        </view>
      </view>
    </view>

    <!-- 联动状态卡片 -->
    <view class="status-card">
      <view class="status-header">
        <text class="status-title">联动系统状态</text>
        <view :class="['status-indicator', globalLinkageEnabled ? 'active' : 'inactive']">
          <text class="status-dot"></text>
          <text class="status-text">{{ globalLinkageEnabled ? '运行中' : '已停止' }}</text>
        </view>
      </view>
      <view class="status-stats">
        <view class="stat-item">
          <text class="stat-number">{{ stats.activeRules || 0 }}</text>
          <text class="stat-label">活跃规则</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.todayExecutions || 0 }}</text>
          <text class="stat-label">今日执行</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.totalRules || 0 }}</text>
          <text class="stat-label">总规则数</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="section-title">快捷操作</view>
      <view class="actions-grid">
        <view class="action-item" @click="addRule">
          <view class="action-icon">➕</view>
          <text class="action-text">添加规则</text>
        </view>
        <view class="action-item" @click="viewHistory">
          <view class="action-icon">📋</view>
          <text class="action-text">执行历史</text>
        </view>
        <view class="action-item" @click="testAllRules">
          <view class="action-icon">🧪</view>
          <text class="action-text">批量测试</text>
        </view>
        <view class="action-item" @click="viewLogs">
          <view class="action-icon">📝</view>
          <text class="action-text">系统日志</text>
        </view>
      </view>
    </view>

    <!-- 联动规则列表 -->
    <view class="rules-section">
      <view class="section-header">
        <text class="section-title">联动规则</text>
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

      <view class="rules-list" v-if="filteredRules.length > 0">
        <view
          class="rule-item"
          v-for="(rule, index) in filteredRules"
          :key="rule.ruleId"
          @click="viewRuleDetail(rule)"
        >
          <view class="rule-header">
            <view class="rule-info">
              <text class="rule-name">{{ rule.ruleName }}</text>
              <view :class="['rule-status', rule.status.toLowerCase()]">
                <text class="status-dot-small"></text>
                <text class="status-text-small">{{ rule.status === 'ACTIVE' ? '启用' : '禁用' }}</text>
              </view>
            </view>
            <view class="rule-actions">
              <text class="action-btn" @click.stop="toggleRuleStatus(rule)">
                {{ rule.status === 'ACTIVE' ? '禁用' : '启用' }}
              </text>
              <text class="action-btn" @click.stop="testRule(rule)">测试</text>
            </view>
          </view>

          <view class="rule-content">
            <view class="rule-type">
              <text class="type-label">类型:</text>
              <text class="type-value">{{ getRuleTypeText(rule.ruleType) }}</text>
            </view>
            <view class="rule-description" v-if="rule.description">
              <text class="desc-text">{{ rule.description }}</text>
            </view>
          </view>

          <view class="rule-footer">
            <view class="execution-info">
              <text class="exec-count">执行 {{ rule.executionCount || 0 }} 次</text>
              <text class="last-exec">最后执行: {{ formatTime(rule.lastExecuteTime) }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-rules" v-else>
        <text class="empty-text">{{ getEmptyText() }}</text>
        <button class="add-rule-btn" @click="addRule" v-if="currentFilter === 'all'">
          添加第一个联动规则
        </button>
      </view>
    </view>

    <!-- 加载状态 -->
    <uni-load-more :status="loadMoreStatus" />

    <!-- 规则测试弹窗 -->
    <uni-popup ref="testPopup" type="dialog">
      <uni-popup-dialog
        title="测试规则"
        :content="testDialogContent"
        :duration="0"
        @confirm="confirmTest"
        @close="closeTest"
      />
    </uni-popup>

    <!-- 规则状态切换弹窗 -->
    <uni-popup ref="statusPopup" type="dialog">
      <uni-popup-dialog
        :title="statusDialogTitle"
        :content="statusDialogContent"
        :duration="0"
        @confirm="confirmStatusChange"
        @close="closeStatusChange"
      />
    </uni-popup>
  </view>
</template>

<script>
import { getGlobalLinkageStatus, triggerMobileLinkage, getMobileLinkageRules } from '@/api/access';

export default {
  name: 'GlobalLinkage',
  data() {
    return {
      statusBarHeight: 0,
      loadMoreStatus: 'more',

      // 联动状态
      globalLinkageEnabled: true,
      stats: {
        activeRules: 0,
        todayExecutions: 0,
        totalRules: 0,
      },

      // 规则列表
      rulesList: [],
      currentFilter: 'all',
      filterTabs: [
        { key: 'all', label: '全部' },
        { key: 'active', label: '启用' },
        { key: 'inactive', label: '禁用' },
      ],

      // 测试相关
      currentTestRule: null,
      testDialogContent: '',

      // 状态切换相关
      currentStatusRule: null,
      statusDialogTitle: '',
      statusDialogContent: '',
    };
  },

  computed: {
    /**
     * 过滤后的规则列表
     */
    filteredRules() {
      if (this.currentFilter === 'all') {
        return this.rulesList;
      } else if (this.currentFilter === 'active') {
        return this.rulesList.filter(rule => rule.status === 'ACTIVE');
      } else if (this.currentFilter === 'inactive') {
        return this.rulesList.filter(rule => rule.status === 'INACTIVE');
      }
      return this.rulesList;
    },
  },

  onLoad() {
    this.initData();
  },

  onShow() {
    this.refreshData();
  },

  onPullDownRefresh() {
    this.refreshData();
  },

  onReachBottom() {
    this.loadMoreData();
  },

  methods: {
    /**
     * 初始化数据
     */
    async initData() {
      const systemInfo = uni.getSystemInfoSync();
      this.statusBarHeight = systemInfo.statusBarHeight || 44;

      await this.refreshData();
    },

    /**
     * 刷新数据
     */
    async refreshData() {
      try {
        uni.showLoading({
          title: '加载中...',
        });

        await Promise.all([
          this.fetchGlobalLinkageStatus(),
          this.fetchRulesList(),
        ]);
      } catch (error) {
        console.error('刷新数据失败:', error);
        uni.showToast({
          title: '加载失败',
          icon: 'none',
        });
      } finally {
        uni.hideLoading();
        uni.stopPullDownRefresh();
      }
    },

    /**
     * 获取全局联动状态
     */
    async fetchGlobalLinkageStatus() {
      try {
        const response = await getGlobalLinkageStatus();
        if (response.code === 200) {
          this.globalLinkageEnabled = response.data.enabled || false;
          this.stats = {
            activeRules: response.data.activeRules || 0,
            todayExecutions: response.data.todayExecutions || 0,
            totalRules: response.data.totalRules || 0,
          };
        }
      } catch (error) {
        console.error('获取全局联动状态失败:', error);
      }
    },

    /**
     * 获取规则列表
     */
    async fetchRulesList() {
      try {
        const response = await getMobileLinkageRules({
          pageSize: 50,
        });

        if (response.code === 200) {
          this.rulesList = response.data.records || [];
        }
      } catch (error) {
        console.error('获取规则列表失败:', error);
      }
    },

    /**
     * 加载更多数据
     */
    loadMoreData() {
      this.loadMoreStatus = 'loading';
      setTimeout(() => {
        this.loadMoreStatus = 'noMore';
      }, 1000);
    },

    /**
     * 切换全局联动
     */
    async toggleGlobalLinkage() {
      try {
        uni.showLoading({
          title: '切换中...',
        });

        const newStatus = !this.globalLinkageEnabled;
        // 全局联动状态API - 待后端接口完成后对接
        this.globalLinkageEnabled = newStatus;

        uni.showToast({
          title: newStatus ? '已启用全局联动' : '已禁用全局联动',
          icon: 'success',
        });
      } catch (error) {
        console.error('切换全局联动失败:', error);
        uni.showToast({
          title: '操作失败',
          icon: 'none',
        });
      } finally {
        uni.hideLoading();
      }
    },

    /**
     * 切换过滤器
     */
    changeFilter(filter) {
      this.currentFilter = filter;
    },

    /**
     * 添加规则
     */
    addRule() {
      uni.navigateTo({
        url: '/pages/access/linkage-rule-form',
      });
    },

    /**
     * 查看执行历史
     */
    viewHistory() {
      uni.navigateTo({
        url: '/pages/access/linkage-history',
      });
    },

    /**
     * 批量测试
     */
    testAllRules() {
      const activeRules = this.rulesList.filter(rule => rule.status === 'ACTIVE');
      if (activeRules.length === 0) {
        uni.showToast({
          title: '没有启用的规则',
          icon: 'none',
        });
        return;
      }

      uni.showModal({
        title: '批量测试',
        content: `确定要测试 ${activeRules.length} 个启用的规则吗？`,
        success: (res) => {
          if (res.confirm) {
            this.executeBatchTest(activeRules);
          }
        },
      });
    },

    /**
     * 执行批量测试
     */
    async executeBatchTest(rules) {
      try {
        uni.showLoading({
          title: '测试中...',
        });

        const promises = rules.map(rule => this.testRuleById(rule.ruleId));
        const results = await Promise.all(promises);

        const successCount = results.filter(r => r).length;

        uni.hideLoading();
        uni.showModal({
          title: '测试完成',
          content: `成功: ${successCount}, 失败: ${results.length - successCount}`,
          showCancel: false,
        });
      } catch (error) {
        console.error('批量测试失败:', error);
        uni.hideLoading();
        uni.showToast({
          title: '测试失败',
          icon: 'none',
        });
      }
    },

    /**
     * 查看系统日志
     */
    viewLogs() {
      uni.navigateTo({
        url: '/pages/access/system-logs',
      });
    },

    /**
     * 查看规则详情
     */
    viewRuleDetail(rule) {
      uni.navigateTo({
        url: `/pages/access/linkage-rule-detail?id=${rule.ruleId}`,
      });
    },

    /**
     * 切换规则状态
     */
    toggleRuleStatus(rule) {
      this.currentStatusRule = rule;
      this.statusDialogTitle = rule.status === 'ACTIVE' ? '禁用规则' : '启用规则';
      this.statusDialogContent = `确定要${rule.status === 'ACTIVE' ? '禁用' : '启用'}规则"${rule.ruleName}"吗？`;
      this.$refs.statusPopup.open();
    },

    /**
     * 确认状态切换
     */
    async confirmStatusChange() {
      if (!this.currentStatusRule) return;

      try {
        uni.showLoading({
          title: '更新中...',
        });

        const newStatus = this.currentStatusRule.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
        // 规则状态更新API - 待后端接口完成后对接
        // await updateRuleStatus(this.currentStatusRule.ruleId, newStatus);

        this.currentStatusRule.status = newStatus;

        uni.showToast({
          title: '状态更新成功',
          icon: 'success',
        });
      } catch (error) {
        console.error('更新规则状态失败:', error);
        uni.showToast({
          title: '更新失败',
          icon: 'none',
        });
      } finally {
        uni.hideLoading();
        this.closeStatusChange();
      }
    },

    /**
     * 关闭状态切换弹窗
     */
    closeStatusChange() {
      this.currentStatusRule = null;
      this.$refs.statusPopup.close();
    },

    /**
     * 测试规则
     */
    testRule(rule) {
      this.currentTestRule = rule;
      this.testDialogContent = `确定要测试规则"${rule.ruleName}"吗？`;
      this.$refs.testPopup.open();
    },

    /**
     * 确认测试
     */
    async confirmTest() {
      if (!this.currentTestRule) return;

      try {
        uni.showLoading({
          title: '测试中...',
        });

        const success = await this.testRuleById(this.currentTestRule.ruleId);

        uni.hideLoading();
        uni.showToast({
          title: success ? '测试成功' : '测试失败',
          icon: success ? 'success' : 'none',
        });
      } catch (error) {
        console.error('测试规则失败:', error);
        uni.hideLoading();
        uni.showToast({
          title: '测试失败',
          icon: 'none',
        });
      } finally {
        this.closeTest();
      }
    },

    /**
     * 根据ID测试规则
     */
    async testRuleById(ruleId) {
      try {
        const response = await triggerMobileLinkage({
          ruleId: ruleId,
          testMode: 'dry_run',
        });

        return response.code === 200;
      } catch (error) {
        console.error('测试规则失败:', error);
        return false;
      }
    },

    /**
     * 关闭测试弹窗
     */
    closeTest() {
      this.currentTestRule = null;
      this.$refs.testPopup.close();
    },

    /**
     * 返回
     */
    goBack() {
      uni.navigateBack();
    },

    /**
     * 获取规则类型文本
     */
    getRuleTypeText(type) {
      const typeMap = {
        'DEVICE_STATUS': '设备状态',
        'ACCESS_RESULT': '通行结果',
        'TIME_BASED': '时间触发',
        'EMERGENCY': '紧急情况',
      };
      return typeMap[type] || type;
    },

    /**
     * 获取空状态文本
     */
    getEmptyText() {
      const textMap = {
        'all': '暂无联动规则',
        'active': '暂无启用的规则',
        'inactive': '暂无禁用的规则',
      };
      return textMap[this.currentFilter] || '暂无数据';
    },

    /**
     * 格式化时间
     */
    formatTime(time) {
      if (!time) return '从未';

      const date = new Date(time);
      const now = new Date();
      const diff = now - date;

      if (diff < 60000) {
        return '刚刚';
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前';
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前';
      } else {
        return date.toLocaleDateString();
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.global-linkage {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.status-bar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 16px;
  padding-bottom: 16px;
}

.status-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  padding: 8px;
}

.back-icon {
  font-size: 24px;
  color: white;
  font-weight: bold;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.toggle-btn {
  padding: 6px 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
}

.toggle-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.toggle-text.enabled {
  color: #34c759;
}

.toggle-text.disabled {
  color: #ff3b30;
}

.status-card {
  background: white;
  margin: 16px;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.status-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 4px;
}

.status-indicator.active .status-dot {
  background: #34c759;
}

.status-indicator.inactive .status-dot {
  background: #8e8e93;
}

.status-text {
  font-size: 12px;
  color: #666;
}

.status-stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.quick-actions {
  padding: 0 16px 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.action-item {
  background: white;
  border-radius: 12px;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.action-item:active {
  transform: scale(0.95);
}

.action-icon {
  font-size: 20px;
}

.action-text {
  font-size: 12px;
  color: #333;
  text-align: center;
}

.rules-section {
  background: white;
  margin: 0 16px 16px;
  border-radius: 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tab {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
}

.filter-tab.active {
  background: #667eea;
  color: white;
}

.rule-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.rule-item:last-child {
  border-bottom: none;
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.rule-info {
  flex: 1;
}

.rule-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 4px;
}

.rule-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot-small {
  width: 6px;
  height: 6px;
  border-radius: 3px;
}

.rule-status.active .status-dot-small {
  background: #34c759;
}

.rule-status.inactive .status-dot-small {
  background: #8e8e93;
}

.status-text-small {
  font-size: 12px;
  color: #666;
}

.rule-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.rule-content {
  margin-bottom: 8px;
}

.rule-type {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.type-label {
  font-size: 12px;
  color: #666;
}

.type-value {
  font-size: 12px;
  color: #667eea;
  font-weight: 500;
}

.rule-description {
  padding: 8px 0;
}

.desc-text {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.rule-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.execution-info {
  display: flex;
  gap: 12px;
}

.exec-count,
.last-exec {
  font-size: 12px;
  color: #999;
}

.empty-rules {
  text-align: center;
  padding: 40px 20px;
}

.empty-text {
  font-size: 14px;
  color: #999;
  display: block;
  margin-bottom: 16px;
}

.add-rule-btn {
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  font-size: 14px;
}
</style>
