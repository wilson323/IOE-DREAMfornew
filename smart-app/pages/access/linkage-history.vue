<!--
  * 移动端联动执行历史页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-12-01
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <view class="linkage-history">
    <!-- 头部状态栏 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="status-content">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="page-title">执行历史</text>
        <view class="filter-btn" @click="showFilterPopup">
          <text class="filter-icon">⚙</text>
        </view>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs" v-if="hasActiveFilters">
      <view class="filter-tags">
        <view
          v-for="(tag, index) in activeFilterTags"
          :key="index"
          class="filter-tag"
          @click="removeFilter(tag.key)"
        >
          <text class="tag-text">{{ tag.label }}</text>
          <text class="tag-close">×</text>
        </view>
        <text class="clear-all" @click="clearAllFilters">清除全部</text>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-number">{{ stats.totalCount || 0 }}</text>
        <text class="stat-label">总执行次数</text>
      </view>
      <view class="stat-item">
        <text class="stat-number">{{ stats.successCount || 0 }}</text>
        <text class="stat-label">成功次数</text>
      </view>
      <view class="stat-item">
        <text class="stat-number">{{ stats.failureCount || 0 }}</text>
        <text class="stat-label">失败次数</text>
      </view>
      <view class="stat-item">
        <text class="stat-number">{{ stats.todayCount || 0 }}</text>
        <text class="stat-label">今日执行</text>
      </view>
    </view>

    <!-- 历史列表 -->
    <view class="history-list" v-if="historyList.length > 0">
      <view
        class="history-item"
        v-for="(item, index) in historyList"
        :key="item.id"
        @click="viewHistoryDetail(item)"
      >
        <view class="item-header">
          <view class="rule-info">
            <text class="rule-name">{{ item.ruleName }}</text>
            <view :class="['status-badge', getStatusClass(item.status)]">
              <text class="status-text">{{ getStatusText(item.status) }}</text>
            </view>
          </view>
          <text class="execution-time">{{ formatDateTime(item.executeTime) }}</text>
        </view>

        <view class="item-content">
          <view class="trigger-info">
            <text class="info-label">触发条件:</text>
            <text class="info-value">{{ item.triggerCondition }}</text>
          </view>
          <view class="action-info">
            <text class="info-label">执行动作:</text>
            <text class="info-value">{{ item.linkageAction }}</text>
          </view>
        </view>

        <view class="item-footer">
          <view class="affected-devices" v-if="item.affectedDevices && item.affectedDevices.length > 0">
            <text class="devices-label">影响设备:</text>
            <view class="device-tags">
              <text
                v-for="device in item.affectedDevices.slice(0, 3)"
                :key="device.deviceId"
                class="device-tag"
              >
                {{ device.deviceName }}
              </text>
              <text
                v-if="item.affectedDevices.length > 3"
                class="more-devices"
              >
                +{{ item.affectedDevices.length - 3 }}
              </text>
            </view>
          </view>

          <view class="execution-duration" v-if="item.duration">
            <text class="duration-text">耗时 {{ item.duration }}ms</text>
          </view>
        </view>

        <view class="error-info" v-if="item.status === 'FAILED' && item.errorMessage">
          <text class="error-message">{{ item.errorMessage }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else-if="!loading">
      <view class="empty-icon">📋</view>
      <text class="empty-title">暂无执行历史</text>
      <text class="empty-desc">{{ getEmptyDesc() }}</text>
      <button class="refresh-btn" @click="refreshData">刷新数据</button>
    </view>

    <!-- 加载状态 -->
    <view class="loading-state" v-else>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 加载更多 -->
    <uni-load-more :status="loadMoreStatus" />

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom">
      <view class="filter-popup">
        <view class="popup-header">
          <text class="popup-title">筛选条件</text>
          <text class="popup-close" @click="closeFilterPopup">×</text>
        </view>

        <scroll-view class="filter-content" scroll-y>
          <!-- 规则筛选 -->
          <view class="filter-section">
            <view class="filter-section-title">规则状态</view>
            <view class="filter-options">
              <view
                v-for="option in statusOptions"
                :key="option.value"
                :class="['filter-option', filterParams.status === option.value ? 'active' : '']"
                @click="selectStatus(option.value)"
              >
                <text class="option-text">{{ option.label }}</text>
              </view>
            </view>
          </view>

          <!-- 时间范围 -->
          <view class="filter-section">
            <view class="filter-section-title">时间范围</view>
            <view class="filter-options">
              <view
                v-for="option in timeRangeOptions"
                :key="option.value"
                :class="['filter-option', filterParams.timeRange === option.value ? 'active' : '']"
                @click="selectTimeRange(option.value)"
              >
                <text class="option-text">{{ option.label }}</text>
              </view>
            </view>
          </view>

          <!-- 规则类型 -->
          <view class="filter-section">
            <view class="filter-section-title">规则类型</view>
            <view class="filter-options">
              <view
                v-for="option in ruleTypeOptions"
                :key="option.value"
                :class="['filter-option', filterParams.ruleType === option.value ? 'active' : '']"
                @click="selectRuleType(option.value)"
              >
                <text class="option-text">{{ option.label }}</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="filter-actions">
          <button class="reset-btn" @click="resetFilters">重置</button>
          <button class="confirm-btn" @click="applyFilters">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- 详情弹窗 -->
    <uni-popup ref="detailPopup" type="center">
      <view class="detail-popup" v-if="selectedHistory">
        <view class="detail-header">
          <text class="detail-title">执行详情</text>
          <text class="detail-close" @click="closeDetailPopup">×</text>
        </view>

        <scroll-view class="detail-content" scroll-y>
          <view class="detail-item">
            <text class="detail-label">规则名称</text>
            <text class="detail-value">{{ selectedHistory.ruleName }}</text>
          </view>

          <view class="detail-item">
            <text class="detail-label">执行状态</text>
            <view :class="['status-badge', getStatusClass(selectedHistory.status)]">
              <text class="status-text">{{ getStatusText(selectedHistory.status) }}</text>
            </view>
          </view>

          <view class="detail-item">
            <text class="detail-label">执行时间</text>
            <text class="detail-value">{{ formatDateTime(selectedHistory.executeTime) }}</text>
          </view>

          <view class="detail-item" v-if="selectedHistory.duration">
            <text class="detail-label">执行耗时</text>
            <text class="detail-value">{{ selectedHistory.duration }}ms</text>
          </view>

          <view class="detail-item">
            <text class="detail-label">触发条件</text>
            <text class="detail-value">{{ selectedHistory.triggerCondition }}</text>
          </view>

          <view class="detail-item">
            <text class="detail-label">执行动作</text>
            <text class="detail-value">{{ selectedHistory.linkageAction }}</text>
          </view>

          <view class="detail-item" v-if="selectedHistory.errorMessage">
            <text class="detail-label">错误信息</text>
            <text class="detail-value error">{{ selectedHistory.errorMessage }}</text>
          </view>

          <view class="detail-item" v-if="selectedHistory.affectedDevices">
            <text class="detail-label">影响设备</text>
            <view class="affected-devices-detail">
              <view
                v-for="device in selectedHistory.affectedDevices"
                :key="device.deviceId"
                class="affected-device-item"
              >
                <text class="device-name">{{ device.deviceName }}</text>
                <text class="device-location">{{ device.location }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>

    <!-- 底部安全区域 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script>
export default {
  name: 'LinkageHistory',
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      loadMoreStatus: 'more',

      // 筛选参数
      filterParams: {
        status: '',
        timeRange: '',
        ruleType: '',
        pageNum: 1,
        pageSize: 20,
      },

      // 历史列表
      historyList: [],
      selectedHistory: null,

      // 统计信息
      stats: {
        totalCount: 0,
        successCount: 0,
        failureCount: 0,
        todayCount: 0,
      },

      // 筛选选项
      statusOptions: [
        { value: '', label: '全部' },
        { value: 'SUCCESS', label: '成功' },
        { value: 'FAILED', label: '失败' },
        { value: 'PARTIAL', label: '部分成功' },
      ],

      timeRangeOptions: [
        { value: '', label: '全部' },
        { value: 'today', label: '今天' },
        { value: 'week', label: '本周' },
        { value: 'month', label: '本月' },
      ],

      ruleTypeOptions: [
        { value: '', label: '全部' },
        { value: 'DEVICE_STATUS', label: '设备状态' },
        { value: 'ACCESS_RESULT', label: '通行结果' },
        { value: 'TIME_BASED', label: '时间触发' },
        { value: 'EMERGENCY', label: '紧急情况' },
      ],
    };
  },

  computed: {
    /**
     * 是否有激活的筛选条件
     */
    hasActiveFilters() {
      return this.filterParams.status || this.filterParams.timeRange || this.filterParams.ruleType;
    },

    /**
     * 激活的筛选标签
     */
    activeFilterTags() {
      const tags = [];
      if (this.filterParams.status) {
        const option = this.statusOptions.find(opt => opt.value === this.filterParams.status);
        if (option) tags.push({ key: 'status', label: option.label });
      }
      if (this.filterParams.timeRange) {
        const option = this.timeRangeOptions.find(opt => opt.value === this.filterParams.timeRange);
        if (option) tags.push({ key: 'timeRange', label: option.label });
      }
      if (this.filterParams.ruleType) {
        const option = this.ruleTypeOptions.find(opt => opt.value === this.filterParams.ruleType);
        if (option) tags.push({ key: 'ruleType', label: option.label });
      }
      return tags;
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
    initData() {
      const systemInfo = uni.getSystemInfoSync();
      this.statusBarHeight = systemInfo.statusBarHeight || 44;

      this.refreshData();
    },

    /**
     * 刷新数据
     */
    async refreshData() {
      try {
        this.loading = true;
        this.filterParams.pageNum = 1;

        await Promise.all([
          this.fetchHistoryList(),
          this.fetchStats(),
        ]);
      } catch (error) {
        console.error('刷新数据失败:', error);
        uni.showToast({
          title: '加载失败',
          icon: 'none',
        });
      } finally {
        this.loading = false;
        uni.stopPullDownRefresh();
      }
    },

    /**
     * 获取历史列表
     */
    async fetchHistoryList() {
      try {
        // TODO: 调用实际API
        // const response = await getLinkageHistory(this.filterParams);

        // 模拟数据
        const mockData = this.generateMockData();
        this.historyList = mockData.records || [];

        this.loadMoreStatus = this.historyList.length < this.filterParams.pageSize ? 'noMore' : 'more';
      } catch (error) {
        console.error('获取历史列表失败:', error);
      }
    },

    /**
     * 获取统计信息
     */
    async fetchStats() {
      try {
        // TODO: 调用实际API
        // const response = await getLinkageHistoryStats(this.filterParams);

        // 模拟数据
        this.stats = {
          totalCount: 156,
          successCount: 142,
          failureCount: 14,
          todayCount: 8,
        };
      } catch (error) {
        console.error('获取统计信息失败:', error);
      }
    },

    /**
     * 生成模拟数据
     */
    generateMockData() {
      const records = [];
      const now = new Date();

      for (let i = 0; i < 15; i++) {
        const status = ['SUCCESS', 'FAILED', 'PARTIAL'][Math.floor(Math.random() * 3)];
        const time = new Date(now.getTime() - Math.random() * 7 * 24 * 60 * 60 * 1000);

        records.push({
          id: i + 1,
          ruleName: `门禁联动规则${i + 1}`,
          status: status,
          executeTime: time.toISOString(),
          triggerCondition: '设备状态变化',
          linkageAction: '远程开门',
          duration: Math.floor(Math.random() * 1000) + 100,
          affectedDevices: [
            { deviceId: '1', deviceName: '前门禁1', location: '大楼前门' },
            { deviceId: '2', deviceName: '侧门禁2', location: '大楼侧门' },
          ].slice(0, Math.floor(Math.random() * 3) + 1),
          errorMessage: status === 'FAILED' ? '设备连接超时' : null,
        });
      }

      return { records };
    },

    /**
     * 加载更多数据
     */
    async loadMoreData() {
      if (this.loadMoreStatus === 'noMore') return;

      try {
        this.loadMoreStatus = 'loading';
        this.filterParams.pageNum++;

        // TODO: 加载更多数据
        await new Promise(resolve => setTimeout(resolve, 1000));

        this.loadMoreStatus = 'noMore';
      } catch (error) {
        console.error('加载更多失败:', error);
        this.loadMoreStatus = 'more';
        this.filterParams.pageNum--;
      }
    },

    /**
     * 显示筛选弹窗
     */
    showFilterPopup() {
      this.$refs.filterPopup.open();
    },

    /**
     * 关闭筛选弹窗
     */
    closeFilterPopup() {
      this.$refs.filterPopup.close();
    },

    /**
     * 选择状态
     */
    selectStatus(value) {
      this.filterParams.status = value;
    },

    /**
     * 选择时间范围
     */
    selectTimeRange(value) {
      this.filterParams.timeRange = value;
    },

    /**
     * 选择规则类型
     */
    selectRuleType(value) {
      this.filterParams.ruleType = value;
    },

    /**
     * 应用筛选
     */
    async applyFilters() {
      this.closeFilterPopup();
      await this.refreshData();
    },

    /**
     * 重置筛选
     */
    resetFilters() {
      this.filterParams = {
        status: '',
        timeRange: '',
        ruleType: '',
        pageNum: 1,
        pageSize: 20,
      };
    },

    /**
     * 移除筛选
     */
    removeFilter(key) {
      this.filterParams[key] = '';
      this.refreshData();
    },

    /**
     * 清除全部筛选
     */
    clearAllFilters() {
      this.resetFilters();
      this.refreshData();
    },

    /**
     * 查看历史详情
     */
    viewHistoryDetail(item) {
      this.selectedHistory = item;
      this.$refs.detailPopup.open();
    },

    /**
     * 关闭详情弹窗
     */
    closeDetailPopup() {
      this.selectedHistory = null;
      this.$refs.detailPopup.close();
    },

    /**
     * 获取状态样式类
     */
    getStatusClass(status) {
      return status.toLowerCase();
    },

    /**
     * 获取状态文本
     */
    getStatusText(status) {
      const statusMap = {
        'SUCCESS': '成功',
        'FAILED': '失败',
        'PARTIAL': '部分成功',
      };
      return statusMap[status] || status;
    },

    /**
     * 获取空状态描述
     */
    getEmptyDesc() {
      if (this.hasActiveFilters) {
        return '没有符合筛选条件的执行记录';
      }
      return '联动规则还没有执行过，尝试触发一些规则吧';
    },

    /**
     * 格式化日期时间
     */
    formatDateTime(time) {
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
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5);
      }
    },

    /**
     * 返回
     */
    goBack() {
      uni.navigateBack();
    },
  },
};
</script>

<style lang="scss" scoped>
.linkage-history {
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

.filter-btn {
  padding: 8px;
}

.filter-icon {
  font-size: 20px;
  color: white;
}

.filter-tabs {
  padding: 16px;
  background: white;
  margin-bottom: 8px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.filter-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #667eea;
  color: white;
  border-radius: 12px;
  font-size: 12px;
}

.tag-close {
  font-size: 14px;
  line-height: 1;
}

.clear-all {
  color: #667eea;
  font-size: 12px;
}

.stats-card {
  display: flex;
  background: white;
  margin: 8px 16px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-item {
  flex: 1;
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

.history-list {
  padding: 0 16px;
}

.history-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
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

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge.success {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.status-badge.failed {
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.status-badge.partial {
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.execution-time {
  font-size: 12px;
  color: #999;
}

.item-content {
  margin-bottom: 12px;
}

.trigger-info,
.action-info {
  display: flex;
  margin-bottom: 4px;
}

.info-label {
  font-size: 12px;
  color: #666;
  min-width: 60px;
}

.info-value {
  font-size: 12px;
  color: #333;
  flex: 1;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.affected-devices {
  flex: 1;
}

.devices-label {
  font-size: 12px;
  color: #666;
  margin-right: 4px;
}

.device-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
}

.device-tag {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 4px;
}

.more-devices {
  font-size: 10px;
  color: #999;
}

.execution-duration {
  font-size: 12px;
  color: #999;
}

.error-info {
  margin-top: 8px;
  padding: 8px;
  background: rgba(255, 59, 48, 0.1);
  border-radius: 6px;
}

.error-message {
  font-size: 12px;
  color: #ff3b30;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #666;
  text-align: center;
  margin-bottom: 24px;
  line-height: 1.4;
}

.refresh-btn {
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  font-size: 14px;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.loading-text {
  font-size: 14px;
  color: #666;
}

.filter-popup {
  background: white;
  border-radius: 12px 12px 0 0;
  max-height: 80vh;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.popup-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.popup-close {
  font-size: 24px;
  color: #999;
  padding: 4px;
}

.filter-content {
  max-height: 60vh;
  padding: 16px;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-option {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  font-size: 14px;
  color: #666;
  background: white;
}

.filter-option.active {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.filter-actions {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.reset-btn,
.confirm-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
}

.reset-btn {
  background: #f0f0f0;
  color: #666;
}

.confirm-btn {
  background: #667eea;
  color: white;
}

.detail-popup {
  background: white;
  border-radius: 12px;
  width: 90vw;
  max-width: 400px;
  max-height: 80vh;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.detail-close {
  font-size: 24px;
  color: #999;
  padding: 4px;
}

.detail-content {
  max-height: 60vh;
  padding: 16px;
}

.detail-item {
  margin-bottom: 16px;
}

.detail-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
  display: block;
}

.detail-value {
  font-size: 14px;
  color: #333;
  display: block;
}

.detail-value.error {
  color: #ff3b30;
}

.affected-devices-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.affected-device-item {
  padding: 8px;
  background: #f8f9fa;
  border-radius: 6px;
}

.device-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 2px;
}

.device-location {
  font-size: 12px;
  color: #666;
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
  height: constant(safe-area-inset-bottom);
}
</style>