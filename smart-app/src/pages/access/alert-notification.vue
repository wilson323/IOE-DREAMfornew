<!--
 * 告警通知页面 - 移动端
 *
 * 功能：
 * 1. 实时告警推送：WebSocket实时接收告警消息
 * 2. 告警列表展示：显示最新告警消息
 * 3. 告警筛选：按类型、级别、状态筛选
 * 4. 告警操作：确认、处理、忽略
 * 5. 统计信息：显示未确认告警数量
 * 6. 下拉刷新：支持下拉刷新和上拉加载更多
 * 7. WebSocket连接状态：显示连接状态
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <view class="alert-notification-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">告警通知</view>
        <view class="navbar-right" @tap="refreshData">
          <uni-icons type="refresh" size="20" color="#ffffff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view
      class="main-scroll"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 统计概览 -->
      <view class="stats-section">
        <view class="stats-card">
          <view class="stat-item" @tap="filterByStatus('')">
            <text class="stat-value">{{ totalCount }}</text>
            <text class="stat-label">全部</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @tap="filterByStatus(1)">
            <text class="stat-value pending">{{ unconfirmedCount }}</text>
            <text class="stat-label">待处理</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @tap="filterByStatus(3)">
            <text class="stat-value success">{{ handledCount }}</text>
            <text class="stat-label">已处理</text>
          </view>
        </view>
      </view>

      <!-- WebSocket连接状态 -->
      <view class="connection-status" :class="{ connected: wsConnected }">
        <uni-icons
          :type="wsConnected ? 'checkbox-filled' : 'close-filled'"
          :color="wsConnected ? '#52c41a' : '#ff4d4f'"
          size="14"
        ></uni-icons>
        <text class="status-text">{{ wsConnected ? '实时推送已连接' : '实时推送未连接' }}</text>
      </view>

      <!-- 筛选栏 -->
      <view class="filter-bar">
        <view class="filter-tabs">
          <view
            v-for="tab in statusTabs"
            :key="tab.value"
            class="filter-tab"
            :class="{ active: activeStatus === tab.value }"
            @tap="handleStatusChange(tab.value)"
          >
            <text class="tab-text">{{ tab.label }}</text>
            <view v-if="activeStatus === tab.value" class="tab-indicator"></view>
          </view>
        </view>
      </view>

      <!-- 告警列表 -->
      <view class="alerts-section">
        <view
          v-for="alert in alertList"
          :key="alert.alertId"
          class="alert-card"
          :class="getAlertCardClass(alert)"
          @tap="viewAlertDetail(alert)"
        >
          <!-- 告警头部 -->
          <view class="alert-header">
            <view class="alert-info">
              <view class="level-badge" :class="getLevelClass(alert.alertLevel)">
                <text class="level-text">{{ getLevelText(alert.alertLevel) }}</text>
              </view>
              <text class="alert-type">{{ getRuleTypeText(alert.ruleType) }}</text>
            </view>
            <view class="alert-status" :class="getStatusClass(alert.alertStatus)">
              <text class="status-text">{{ getStatusText(alert.alertStatus) }}</text>
            </view>
          </view>

          <!-- 告警内容 -->
          <view class="alert-content">
            <text class="alert-message">{{ alert.alertMessage }}</text>
          </view>

          <!-- 告警详情 -->
          <view class="alert-detail">
            <view class="detail-item">
              <uni-icons type="home" size="14" color="#999999"></uni-icons>
              <text class="detail-text">设备ID: {{ alert.deviceId }}</text>
            </view>
            <view class="detail-item">
              <uni-icons type="calendar" size="14" color="#999999"></uni-icons>
              <text class="detail-text">{{ formatTime(alert.alertTime) }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="alert-actions" @tap.stop>
            <button
              v-if="alert.alertStatus === 1"
              class="action-btn confirm-btn"
              size="mini"
              @tap="handleConfirm(alert)"
            >
              确认
            </button>
            <button
              v-if="alert.alertStatus === 2"
              class="action-btn handle-btn"
              size="mini"
              @tap="handleHandle(alert)"
            >
              处理
            </button>
            <button
              v-if="alert.alertStatus !== 4"
              class="action-btn ignore-btn"
              size="mini"
              @tap="handleIgnore(alert)"
            >
              忽略
            </button>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="hasMore">
          <text class="load-text">{{ loading ? '加载中...' : '上拉加载更多' }}</text>
        </view>

        <!-- 无更多数据 -->
        <view class="no-more" v-if="!hasMore && alertList.length > 0">
          <text class="no-more-text">没有更多数据了</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="alertList.length === 0 && !loading">
          <uni-icons type="info" size="80" color="#cccccc"></uni-icons>
          <text class="empty-text">暂无告警通知</text>
        </view>
      </scroll-view>

    <!-- 告警详情弹窗 -->
    <uni-popup ref="detailPopup" type="bottom" :safe-area="false">
      <view class="detail-popup" v-if="currentAlert">
        <view class="popup-header">
          <text class="popup-title">告警详情</text>
          <view class="popup-close" @tap="closeDetailPopup">
            <uni-icons type="close" size="20" color="#999999"></uni-icons>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <view class="detail-section">
            <view class="detail-row">
              <text class="detail-label">告警ID</text>
              <text class="detail-value">{{ currentAlert.alertId }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">告警级别</text>
              <view
                class="level-badge detail"
                :class="getLevelClass(currentAlert.alertLevel)"
              >
                <text class="level-text">{{ getLevelText(currentAlert.alertLevel) }}</text>
              </view>
            </view>
            <view class="detail-row">
              <text class="detail-label">告警类型</text>
              <text class="detail-value">{{ getRuleTypeText(currentAlert.ruleType) }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">处理状态</text>
              <view
                class="status-badge detail"
                :class="getStatusClass(currentAlert.alertStatus)"
              >
                <text class="status-text">{{ getStatusText(currentAlert.alertStatus) }}</text>
              </view>
            </view>
            <view class="detail-row">
              <text class="detail-label">设备ID</text>
              <text class="detail-value">{{ currentAlert.deviceId }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">告警时间</text>
              <text class="detail-value">{{ formatTime(currentAlert.alertTime) }}</text>
            </view>
            <view class="detail-row full">
              <text class="detail-label">告警消息</text>
              <text class="detail-value full">{{ currentAlert.alertMessage }}</text>
            </view>
            <view class="detail-row full" v-if="currentAlert.alertDetails">
              <text class="detail-label">告警详情</text>
              <text class="detail-value full json">{{
                JSON.stringify(currentAlert.alertDetails, null, 2)
              }}</text>
            </view>
            <view class="detail-row" v-if="currentAlert.confirmTime">
              <text class="detail-label">确认时间</text>
              <text class="detail-value">{{ formatTime(currentAlert.confirmTime) }}</text>
            </view>
            <view class="detail-row" v-if="currentAlert.confirmBy">
              <text class="detail-label">确认人</text>
              <text class="detail-value">{{ currentAlert.confirmBy }}</text>
            </view>
            <view class="detail-row" v-if="currentAlert.handleTime">
              <text class="detail-label">处理时间</text>
              <text class="detail-value">{{ formatTime(currentAlert.handleTime) }}</text>
            </view>
            <view class="detail-row" v-if="currentAlert.handleBy">
              <text class="detail-label">处理人</text>
              <text class="detail-value">{{ currentAlert.handleBy }}</text>
            </view>
            <view
              class="detail-row full"
              v-if="currentAlert.handleRemark"
            >
              <text class="detail-label">处理备注</text>
              <text class="detail-value full">{{ currentAlert.handleRemark }}</text>
            </view>
          </view>
        </scroll-view>

        <view class="popup-actions">
          <button class="popup-btn cancel-btn" @tap="closeDetailPopup">关闭</button>
          <button
            v-if="currentAlert.alertStatus === 1"
            class="popup-btn primary-btn"
            @tap="handleConfirmFromPopup"
          >
            确认告警
          </button>
          <button
            v-if="currentAlert.alertStatus === 2"
            class="popup-btn primary-btn"
            @tap="handleHandleFromPopup"
          >
            处理告警
          </button>
        </view>
      </view>
    </uni-popup>

    <!-- 处理告警弹窗 -->
    <uni-popup ref="handlePopup" type="dialog">
      <uni-popup-dialog
        type="input"
        title="处理告警"
        :value="handleRemark"
        placeholder="请输入处理备注"
        @confirm="handleHandleSubmit"
        @close="handlePopup.close()"
      ></uni-popup-dialog>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useWebSocket } from '@/utils/websocket';
import { getRequest, postRequest } from '@/lib/smart-request';

// ==================== 状态管理 ====================

const refreshing = ref(false);
const loading = ref(false);
const wsConnected = ref(false);

// 统计信息
const totalCount = ref(0);
const unconfirmedCount = ref(0);
const handledCount = ref(0);

// 告警列表
const alertList = ref([]);
const currentAlert = ref(null);
const handleRemark = ref('');

// 分页信息
const pageNum = ref(1);
const pageSize = ref(20);
const hasMore = ref(true);

// 筛选条件
const activeStatus = ref('');

// 状态标签
const statusTabs = [
  { label: '全部', value: '' },
  { label: '待处理', value: 1 },
  { label: '已确认', value: 2 },
  { label: '已处理', value: 3 },
  { label: '已忽略', value: 4 },
];

// ==================== WebSocket 集成 ====================

let wsClient = null;

/**
 * 初始化 WebSocket 连接
 */
const initWebSocket = () => {
  // 获取WebSocket地址
  const wsUrl = `ws://${uni.getStorageSync('serverHost') || 'localhost'}:8090/ws/alert`;

  wsClient = useWebSocket({
    url: wsUrl,
    heartbeatInterval: 30000,
    reconnectInterval: 3000,
    maxReconnectAttempts: 10,
    onOpen: () => {
      console.log('[告警通知] WebSocket连接成功');
      wsConnected.value = true;

      // 订阅告警推送
      wsClient.send({ type: 'subscribe', topic: 'alert' });

      uni.showToast({
        title: '实时推送已连接',
        icon: 'success',
        duration: 2000,
      });
    },
    onMessage: (data) => {
      console.log('[告警通知] 收到消息:', data);
      handleAlertPush(data);
    },
    onClose: () => {
      console.log('[告警通知] WebSocket连接关闭');
      wsConnected.value = false;
    },
    onError: (error) => {
      console.error('[告警通知] WebSocket连接错误:', error);
      wsConnected.value = false;
    },
  });
};

/**
 * 处理告警推送
 */
const handleAlertPush = (data) => {
  console.log('[告警推送] 收到新告警:', data);

  // 显示通知
  uni.showToast({
    title: data.alertMessage || '新告警',
    icon: 'none',
    duration: 3000,
  });

  // 刷新列表和统计
  loadAlertList(true);
  loadStatistics();
};

/**
 * 关闭 WebSocket 连接
 */
const closeWebSocket = () => {
  if (wsClient) {
    wsClient.close();
    wsClient = null;
  }
};

// ==================== API 调用 ====================

/**
 * 加载告警列表
 */
const loadAlertList = async (refresh = false) => {
  if (loading.value) return;

  try {
    loading.value = true;

    if (refresh) {
      pageNum.value = 1;
      hasMore.value = true;
    }

    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    };

    if (activeStatus.value !== '') {
      params.alertStatus = activeStatus.value;
    }

    const res = await getRequest('/api/v1/access/alerts', params);

    if (res.data && res.code === 200) {
      const list = res.data.list || [];

      if (refresh) {
        alertList.value = list;
      } else {
        alertList.value = [...alertList.value, ...list];
      }

      hasMore.value = list.length >= pageSize.value;
      totalCount.value = res.data.total || 0;
    }
  } catch (error) {
    console.error('加载告警列表失败:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
};

/**
 * 加载统计信息
 */
const loadStatistics = async () => {
  try {
    const res = await getRequest('/api/v1/access/alerts/statistics');

    if (res.data && res.code === 200) {
      unconfirmedCount.value = res.data.unconfirmedCount || 0;
      handledCount.value = res.data.handledCount || 0;
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
  }
};

/**
 * 查询今日告警数量
 */
const loadTodayCount = async () => {
  try {
    const res = await getRequest('/api/v1/access/alerts/today-count');

    if (res.data && res.code === 200) {
      // 今日告警数量可以用于统计展示
      console.log('[今日告警] 数量:', res.data);
    }
  } catch (error) {
    console.error('查询今日告警数量失败:', error);
  }
};

// ==================== 告警操作 ====================

/**
 * 查看告警详情
 */
const viewAlertDetail = async (alert) => {
  currentAlert.value = alert;
  // 打开弹窗
  uni.$refs.detailPopup?.open();
};

/**
 * 关闭详情弹窗
 */
const closeDetailPopup = () => {
  uni.$refs.detailPopup?.close();
};

/**
 * 确认告警
 */
const handleConfirm = async (alert) => {
  try {
    const res = await postRequest('/api/v1/access/alerts/handle', {
      alertId: alert.alertId,
      action: 'confirm',
    });

    if (res.code === 200) {
      uni.showToast({
        title: '确认成功',
        icon: 'success',
      });

      // 刷新列表和统计
      loadAlertList(true);
      loadStatistics();
    }
  } catch (error) {
    console.error('确认告警失败:', error);
    uni.showToast({
      title: '确认失败',
      icon: 'none',
    });
  }
};

/**
 * 处理告警
 */
const handleHandle = (alert) => {
  currentAlert.value = alert;
  handleRemark.value = '';
  uni.$refs.handlePopup?.open();
};

/**
 * 提交处理
 */
const handleHandleSubmit = async (remark) => {
  if (!remark) {
    uni.showToast({
      title: '请输入处理备注',
      icon: 'none',
    });
    return;
  }

  try {
    const res = await postRequest('/api/v1/access/alerts/handle', {
      alertId: currentAlert.value.alertId,
      action: 'handle',
      handleRemark: remark,
    });

    if (res.code === 200) {
      uni.showToast({
        title: '处理成功',
        icon: 'success',
      });

      // 关闭弹窗并刷新
      uni.$refs.handlePopup?.close();
      uni.$refs.detailPopup?.close();
      loadAlertList(true);
      loadStatistics();
    }
  } catch (error) {
    console.error('处理告警失败:', error);
    uni.showToast({
      title: '处理失败',
      icon: 'none',
    });
  }
};

/**
 * 忽略告警
 */
const handleIgnore = (alert) => {
  uni.showModal({
    title: '确认忽略',
    content: '确定要忽略此告警吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const response = await postRequest('/api/v1/access/alerts/handle', {
            alertId: alert.alertId,
            action: 'ignore',
          });

          if (response.code === 200) {
            uni.showToast({
              title: '忽略成功',
              icon: 'success',
            });

            loadAlertList(true);
            loadStatistics();
          }
        } catch (error) {
          console.error('忽略告警失败:', error);
          uni.showToast({
            title: '忽略失败',
            icon: 'none',
          });
        }
      }
    },
  });
};

/**
 * 从详情弹窗确认告警
 */
const handleConfirmFromPopup = () => {
  handleConfirm(currentAlert.value);
  closeDetailPopup();
};

/**
 * 从详情弹窗处理告警
 */
const handleHandleFromPopup = () => {
  closeDetailPopup();
  handleHandle(currentAlert.value);
};

// ==================== 筛选和刷新 ====================

/**
 * 状态标签切换
 */
const handleStatusChange = (status) => {
  activeStatus.value = status;
  loadAlertList(true);
};

/**
 * 按状态筛选
 */
const filterByStatus = (status) => {
  activeStatus.value = status;
  loadAlertList(true);
};

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true;
  loadAlertList(true).then(() => {
    setTimeout(() => {
      refreshing.value = false;
    }, 500);
  });
};

/**
 * 上拉加载更多
 */
const loadMore = () => {
  if (!hasMore.value || loading.value) return;

  pageNum.value++;
  loadAlertList();
};

/**
 * 刷新数据
 */
const refreshData = () => {
  loadAlertList(true);
  loadStatistics();
  loadTodayCount();
};

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack();
};

// ==================== 工具方法 ====================

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return '-';

  const date = new Date(time);
  const now = new Date();
  const diff = now - date;

  // 1小时内显示"刚刚"或"X分钟前"
  if (diff < 3600000) {
    if (diff < 60000) {
      return '刚刚';
    }
    return `${Math.floor(diff / 60000)}分钟前`;
  }

  // 24小时内显示"X小时前"
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`;
  }

  // 7天内显示"X天前"
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`;
  }

  // 其他显示完整日期时间
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');

  return `${year}-${month}-${day} ${hour}:${minute}`;
};

/**
 * 获取告警级别颜色类
 */
const getLevelClass = (level) => {
  const classMap = {
    CRITICAL: 'critical',
    HIGH: 'high',
    MEDIUM: 'medium',
    LOW: 'low',
  };
  return classMap[level] || '';
};

/**
 * 获取告警级别文本
 */
const getLevelText = (level) => {
  const textMap = {
    CRITICAL: '紧急',
    HIGH: '重要',
    MEDIUM: '中等',
    LOW: '低级',
  };
  return textMap[level] || level;
};

/**
 * 获取告警状态颜色类
 */
const getStatusClass = (status) => {
  const classMap = {
    1: 'pending',
    2: 'confirmed',
    3: 'handled',
    4: 'ignored',
  };
  return classMap[status] || '';
};

/**
 * 获取告警状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    1: '待处理',
    2: '已确认',
    3: '已处理',
    4: '已忽略',
  };
  return textMap[status] || status;
};

/**
 * 获取规则类型文本
 */
const getRuleTypeText = (type) => {
  const textMap = {
    DEVICE_OFFLINE: '设备离线',
    DEVICE_FAULT: '设备故障',
    ANTI_PASSBACK: '反潜回',
    INTERLOCK: '互锁冲突',
    MULTI_PERSON: '多人验证',
  };
  return textMap[type] || type;
};

/**
 * 获取告警卡片样式类
 */
const getAlertCardClass = (alert) => {
  const classes = [];

  if (alert.alertStatus === 1) {
    classes.push('unread');
  }

  if (alert.alertLevel === 'CRITICAL') {
    classes.push('critical');
  }

  return classes.join(' ');
};

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[告警通知] 页面初始化');

  // 加载初始数据
  loadAlertList();
  loadStatistics();
  loadTodayCount();

  // 初始化 WebSocket
  initWebSocket();
});

onUnmounted(() => {
  console.log('[告警通知] 页面销毁');

  // 关闭 WebSocket
  closeWebSocket();
});
</script>

<style scoped>
.alert-notification-page {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

/* 自定义导航栏 */
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-top: var(--status-bar-height);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 16px;
}

.navbar-left,
.navbar-right {
  display: flex;
  align-items: center;
  width: 80px;
}

.navbar-left {
  cursor: pointer;
}

.back-text {
  margin-left: 4px;
  font-size: 14px;
  color: #ffffff;
}

.navbar-title {
  font-size: 18px;
  font-weight: 500;
  color: #ffffff;
}

/* 主内容区域 */
.main-scroll {
  height: 100vh;
  padding-top: calc(44px + var(--status-bar-height));
}

/* 统计概览 */
.stats-section {
  padding: 16px;
}

.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333333;
  margin-bottom: 4px;
}

.stat-value.pending {
  color: #ff4d4f;
}

.stat-value.success {
  color: #52c41a;
}

.stat-label {
  font-size: 12px;
  color: #999999;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e8e8e8;
}

/* WebSocket连接状态 */
.connection-status {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  margin: 0 16px 16px;
  background: #fff1f0;
  border-radius: 8px;
  border: 1px solid #ffccc7;
}

.connection-status.connected {
  background: #f6ffed;
  border-color: #b7eb8f;
}

.status-text {
  margin-left: 6px;
  font-size: 12px;
  color: #666666;
}

/* 筛选栏 */
.filter-bar {
  padding: 0 16px 16px;
}

.filter-tabs {
  display: flex;
  background: #ffffff;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-tab {
  flex: 1;
  position: relative;
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
}

.tab-text {
  font-size: 14px;
  color: #666666;
}

.filter-tab.active .tab-text {
  color: #667eea;
  font-weight: 500;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #667eea;
  border-radius: 2px;
}

/* 告警列表 */
.alerts-section {
  padding: 0 16px 16px;
}

.alert-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.alert-card.unread {
  border-left: 4px solid #667eea;
}

.alert-card.critical {
  background: #fff2f0;
  border-left-color: #ff4d4f;
}

/* 告警头部 */
.alert-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.alert-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.level-badge {
  padding: 4px 8px;
  border-radius: 4px;
  margin-right: 8px;
}

.level-badge.critical {
  background: #ff4d4f;
  color: #ffffff;
}

.level-badge.high {
  background: #fa8c16;
  color: #ffffff;
}

.level-badge.medium {
  background: #fadb14;
  color: #333333;
}

.level-badge.low {
  background: #1890ff;
  color: #ffffff;
}

.level-badge.detail {
  display: inline-block;
  padding: 4px 12px;
}

.level-text {
  font-size: 12px;
  font-weight: 500;
}

.alert-type {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
}

.alert-status {
  padding: 4px 8px;
  border-radius: 4px;
}

.alert-status.pending {
  background: #fff1f0;
  border: 1px solid #ffccc7;
}

.alert-status.confirmed {
  background: #ffe7ba;
  border: 1px solid #ffd591;
}

.alert-status.handled {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.alert-status.ignored {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
}

.status-badge.detail {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
}

.status-text {
  font-size: 12px;
}

/* 告警内容 */
.alert-content {
  margin-bottom: 12px;
}

.alert-message {
  font-size: 14px;
  color: #333333;
  line-height: 1.6;
}

/* 告警详情 */
.alert-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-text {
  font-size: 12px;
  color: #999999;
}

/* 操作按钮 */
.alert-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.action-btn {
  flex: 1;
  height: 32px;
  line-height: 32px;
  border-radius: 6px;
  font-size: 14px;
  border: none;
}

.confirm-btn {
  background: #1890ff;
  color: #ffffff;
}

.handle-btn {
  background: #52c41a;
  color: #ffffff;
}

.ignore-btn {
  background: #f5f5f5;
  color: #666666;
}

/* 加载更多 */
.load-more,
.no-more {
  padding: 16px;
  text-align: center;
}

.load-text,
.no-more-text {
  font-size: 14px;
  color: #999999;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.empty-text {
  margin-top: 16px;
  font-size: 14px;
  color: #999999;
}

/* 详情弹窗 */
.detail-popup {
  background: #ffffff;
  border-radius: 16px 16px 0 0;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.popup-title {
  font-size: 18px;
  font-weight: 500;
  color: #333333;
}

.popup-close {
  cursor: pointer;
  padding: 4px;
}

.popup-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  padding: 8px 0;
}

.detail-row.full {
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  min-width: 80px;
  font-size: 14px;
  color: #999999;
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: #333333;
}

.detail-value.full {
  width: 100%;
}

.detail-value.json {
  font-family: monospace;
  font-size: 12px;
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.popup-actions {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.popup-btn {
  flex: 1;
  height: 40px;
  line-height: 40px;
  border-radius: 8px;
  font-size: 16px;
  border: none;
  cursor: pointer;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666666;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}
</style>
