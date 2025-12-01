<!--
  * 移动端门禁管理主页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <view class="access-management">
    <!-- 头部状态栏 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="status-content">
        <text class="page-title">门禁管理</text>
        <view class="sync-status" @click="refreshData">
          <text :class="['sync-icon', syncLoading ? 'rotating' : '']">🔄</text>
          <text class="sync-text">{{ syncLoading ? '同步中...' : '已同步' }}</text>
        </view>
      </view>
    </view>

    <!-- 概览卡片 -->
    <view class="overview-section">
      <view class="overview-grid">
        <view class="overview-card online" @click="navigateTo('/pages/access/device-list')">
          <view class="card-icon">📱</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.onlineDevices || 0 }}</text>
            <text class="card-label">在线设备</text>
          </view>
        </view>

        <view class="overview-card access" @click="navigateTo('/pages/access/record-list')">
          <view class="card-icon">🔓</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.todayAccess || 0 }}</text>
            <text class="card-label">今日通行</text>
          </view>
        </view>

        <view class="overview-card alerts" @click="navigateTo('/pages/access/alert-list')">
          <view class="card-icon">⚠️</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.activeAlerts || 0 }}</text>
            <text class="card-label">活跃告警</text>
          </view>
        </view>

        <view class="overview-card permissions" @click="navigateTo('/pages/access/permission-list')">
          <view class="card-icon">👤</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.userPermissions || 0 }}</text>
            <text class="card-label">用户权限</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="section-title">快捷操作</view>
      <view class="actions-grid">
        <view class="action-item" @click="remoteOpenDoor">
          <view class="action-icon">🔓</view>
          <text class="action-text">远程开门</text>
        </view>
        <view class="action-item" @click="scanQRCode">
          <view class="action-icon">📱</view>
          <text class="action-text">扫码通行</text>
        </view>
        <view class="action-item" @click="viewMonitor">
          <view class="action-icon">👁️</view>
          <text class="action-text">实时监控</text>
        </view>
        <view class="action-item" @click="addDevice">
          <view class="action-icon">➕</view>
          <text class="action-text">添加设备</text>
        </view>
      </view>
    </view>

    <!-- 最近通行记录 -->
    <view class="recent-records">
      <view class="section-header">
        <text class="section-title">最近通行</text>
        <text class="view-more" @click="navigateTo('/pages/access/record-list')">查看更多</text>
      </view>

      <view class="records-list" v-if="recentRecords.length > 0">
        <view
          class="record-item"
          v-for="(record, index) in recentRecords"
          :key="index"
          @click="viewRecordDetail(record)"
        >
          <view class="record-avatar">
            <image :src="record.userAvatar || '/static/images/default-avatar.png'" class="avatar-img" />
          </view>
          <view class="record-info">
            <view class="record-user">
              <text class="user-name">{{ record.userName }}</text>
              <text :class="['record-status', record.status.toLowerCase()]">
                {{ getRecordStatusText(record.status) }}
              </text>
            </view>
            <view class="record-detail">
              <text class="device-name">{{ record.deviceName }}</text>
              <text class="record-time">{{ formatTime(record.accessTime) }}</text>
            </view>
          </view>
          <view class="record-arrow">›</view>
        </view>
      </view>

      <view class="empty-records" v-else>
        <text class="empty-text">暂无通行记录</text>
      </view>
    </view>

    <!-- 设备状态概览 -->
    <view class="device-status">
      <view class="section-header">
        <text class="section-title">设备状态</text>
        <text class="view-more" @click="navigateTo('/pages/access/device-list')">查看全部</text>
      </view>

      <view class="device-grid">
        <view
          class="device-item"
          v-for="(device, index) in deviceStatusList"
          :key="index"
          @click="viewDeviceDetail(device)"
        >
          <view :class="['device-status-dot', device.status.toLowerCase()]"></view>
          <view class="device-info">
            <text class="device-name">{{ device.deviceName }}</text>
            <text class="device-location">{{ device.location }}</text>
          </view>
          <view class="device-control" v-if="device.status === 'ONLINE'" @click.stop="controlDevice(device)">
            <text class="control-btn">控制</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 远程开门弹窗 -->
    <uni-popup ref="remoteOpenPopup" type="dialog">
      <uni-popup-dialog
        title="远程开门"
        content="确定要远程开门吗？"
        :duration="0"
        @confirm="confirmRemoteOpen"
        @close="closeRemoteOpen"
      />
    </uni-popup>

    <!-- 设备选择弹窗 -->
    <uni-popup ref="deviceSelectPopup" type="bottom">
      <view class="device-select-popup">
        <view class="popup-header">
          <text class="popup-title">选择设备</text>
          <text class="popup-close" @click="closeDeviceSelect">×</text>
        </view>
        <scroll-view class="device-list" scroll-y>
          <view
            class="device-option"
            v-for="device in onlineDevices"
            :key="device.deviceId"
            @click="selectDevice(device)"
          >
            <view class="device-option-info">
              <text class="device-option-name">{{ device.deviceName }}</text>
              <text class="device-option-location">{{ device.location }}</text>
            </view>
            <view class="device-option-status online">在线</view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>

    <!-- 加载状态 -->
    <uni-load-more :status="loadMoreStatus" />
  </view>
</template>

<script>
import { accessDeviceApi } from '@/api/access/device-api';
import { accessRecordApi } from '@/api/access/record-api';
import { accessMonitorApi } from '@/api/access/monitor-api';

export default {
  name: 'AccessManagement',
  data() {
    return {
      statusBarHeight: 0,
      syncLoading: false,
      loadMoreStatus: 'more',

      // 概览统计
      overviewStats: {
        onlineDevices: 0,
        todayAccess: 0,
        activeAlerts: 0,
        userPermissions: 0,
      },

      // 最近通行记录
      recentRecords: [],

      // 设备状态列表
      deviceStatusList: [],

      // 在线设备列表（用于远程开门）
      onlineDevices: [],

      // 当前选择的设备
      selectedDevice: null,
    };
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
      // 获取状态栏高度
      const systemInfo = uni.getSystemInfoSync();
      this.statusBarHeight = systemInfo.statusBarHeight || 44;

      await this.refreshData();
    },

    /**
     * 刷新数据
     */
    async refreshData() {
      try {
        this.syncLoading = true;
        await Promise.all([
          this.fetchOverviewStats(),
          this.fetchRecentRecords(),
          this.fetchDeviceStatus(),
        ]);
      } catch (error) {
        console.error('刷新数据失败:', error);
        uni.showToast({
          title: '刷新失败',
          icon: 'none',
        });
      } finally {
        this.syncLoading = false;
        uni.stopPullDownRefresh();
      }
    },

    /**
     * 获取概览统计
     */
    async fetchOverviewStats() {
      try {
        const response = await accessMonitorApi.getRealTimeStats();
        if (response.code === 200) {
          this.overviewStats = {
            onlineDevices: response.data.onlineDevices || 0,
            todayAccess: response.data.todayAccess || 0,
            activeAlerts: response.data.activeAlerts || 0,
            userPermissions: response.data.userPermissions || 0,
          };
        }
      } catch (error) {
        console.error('获取概览统计失败:', error);
      }
    },

    /**
     * 获取最近通行记录
     */
    async fetchRecentRecords() {
      try {
        const response = await accessRecordApi.queryRecordList({
          pageNum: 1,
          pageSize: 5,
          orderBy: 'accessTime',
          orderDirection: 'DESC',
        });

        if (response.code === 200) {
          this.recentRecords = response.data.records || [];
        }
      } catch (error) {
        console.error('获取通行记录失败:', error);
      }
    },

    /**
     * 获取设备状态
     */
    async fetchDeviceStatus() {
      try {
        const response = await accessDeviceApi.queryDeviceList({
          pageNum: 1,
          pageSize: 10,
        });

        if (response.code === 200) {
          this.deviceStatusList = response.data.records || [];
          this.onlineDevices = this.deviceStatusList.filter(device => device.status === 'ONLINE');
        }
      } catch (error) {
        console.error('获取设备状态失败:', error);
      }
    },

    /**
     * 加载更多数据
     */
    loadMoreData() {
      this.loadMoreStatus = 'loading';
      // 模拟加载
      setTimeout(() => {
        this.loadMoreStatus = 'noMore';
      }, 1000);
    },

    /**
     * 远程开门
     */
    remoteOpenDoor() {
      if (this.onlineDevices.length === 0) {
        uni.showToast({
          title: '没有在线设备',
          icon: 'none',
        });
        return;
      }

      if (this.onlineDevices.length === 1) {
        this.selectedDevice = this.onlineDevices[0];
        this.$refs.remoteOpenPopup.open();
      } else {
        this.$refs.deviceSelectPopup.open();
      }
    },

    /**
     * 确认远程开门
     */
    async confirmRemoteOpen() {
      if (!this.selectedDevice) return;

      try {
        uni.showLoading({
          title: '开门中...',
        });

        const response = await accessDeviceApi.remoteOpenDoor(this.selectedDevice.deviceId);

        if (response.code === 200) {
          uni.showToast({
            title: '开门成功',
            icon: 'success',
          });
        } else {
          uni.showToast({
            title: response.message || '开门失败',
            icon: 'none',
          });
        }
      } catch (error) {
        console.error('远程开门失败:', error);
        uni.showToast({
          title: '开门失败',
          icon: 'none',
        });
      } finally {
        uni.hideLoading();
        this.$refs.remoteOpenPopup.close();
      }
    },

    /**
     * 关闭远程开门弹窗
     */
    closeRemoteOpen() {
      this.selectedDevice = null;
    },

    /**
     * 关闭设备选择弹窗
     */
    closeDeviceSelect() {
      this.$refs.deviceSelectPopup.close();
    },

    /**
     * 选择设备
     */
    selectDevice(device) {
      this.selectedDevice = device;
      this.$refs.deviceSelectPopup.close();
      this.$refs.remoteOpenPopup.open();
    },

    /**
     * 扫码通行
     */
    scanQRCode() {
      uni.scanCode({
        success: (res) => {
          console.log('扫码结果:', res.result);
          // 处理扫码逻辑
          this.handleScanResult(res.result);
        },
        fail: (error) => {
          console.error('扫码失败:', error);
          uni.showToast({
            title: '扫码失败',
            icon: 'none',
          });
        },
      });
    },

    /**
     * 处理扫码结果
     */
    handleScanResult(result) {
      // TODO: 实现扫码逻辑
      uni.showToast({
        title: '扫码功能开发中',
        icon: 'none',
      });
    },

    /**
     * 查看实时监控
     */
    viewMonitor() {
      uni.navigateTo({
        url: '/pages/access/monitor',
      });
    },

    /**
     * 添加设备
     */
    addDevice() {
      uni.navigateTo({
        url: '/pages/access/device-form',
      });
    },

    /**
     * 查看通行记录详情
     */
    viewRecordDetail(record) {
      uni.navigateTo({
        url: `/pages/access/record-detail?id=${record.recordId}`,
      });
    },

    /**
     * 查看设备详情
     */
    viewDeviceDetail(device) {
      uni.navigateTo({
        url: `/pages/access/device-detail?id=${device.deviceId}`,
      });
    },

    /**
     * 控制设备
     */
    controlDevice(device) {
      uni.showActionSheet({
        itemList: ['远程开门', '重启设备', '设备配置'],
        success: (res) => {
          switch (res.tapIndex) {
            case 0:
              this.selectedDevice = device;
              this.$refs.remoteOpenPopup.open();
              break;
            case 1:
              this.restartDevice(device);
              break;
            case 2:
              this.viewDeviceDetail(device);
              break;
          }
        },
      });
    },

    /**
     * 重启设备
     */
    async restartDevice(device) {
      try {
        uni.showLoading({
          title: '重启中...',
        });

        const response = await accessDeviceApi.restartDevice(device.deviceId);

        if (response.code === 200) {
          uni.showToast({
            title: '设备重启成功',
            icon: 'success',
          });
          this.refreshData();
        } else {
          uni.showToast({
            title: response.message || '设备重启失败',
            icon: 'none',
          });
        }
      } catch (error) {
        console.error('设备重启失败:', error);
        uni.showToast({
          title: '设备重启失败',
          icon: 'none',
        });
      } finally {
        uni.hideLoading();
      }
    },

    /**
     * 页面导航
     */
    navigateTo(url) {
      uni.navigateTo({
        url: url,
      });
    },

    /**
     * 获取记录状态文本
     */
    getRecordStatusText(status) {
      const statusMap = {
        SUCCESS: '成功',
        FAILED: '失败',
        ABNORMAL: '异常',
      };
      return statusMap[status] || status;
    },

    /**
     * 格式化时间
     */
    formatTime(time) {
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
.access-management {
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

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: white;
}

.sync-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sync-icon {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

.sync-icon.rotating {
  animation: rotate 1s linear infinite;
}

.sync-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.overview-section {
  padding: 16px;
  margin-top: -8px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.overview-card:active {
  transform: scale(0.98);
}

.card-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.overview-card.online .card-icon {
  background: rgba(52, 199, 89, 0.1);
}

.overview-card.access .card-icon {
  background: rgba(52, 144, 255, 0.1);
}

.overview-card.alerts .card-icon {
  background: rgba(255, 59, 48, 0.1);
}

.overview-card.permissions .card-icon {
  background: rgba(255, 149, 0, 0.1);
}

.card-info {
  flex: 1;
}

.card-number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  line-height: 1.2;
}

.card-label {
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

.recent-records,
.device-status {
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

.view-more {
  font-size: 14px;
  color: #667eea;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.record-info {
  flex: 1;
}

.record-user {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.record-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.record-status.success {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.record-status.failed,
.record-status.abnormal {
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.record-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-name {
  font-size: 14px;
  color: #666;
}

.record-time {
  font-size: 12px;
  color: #999;
}

.record-arrow {
  font-size: 18px;
  color: #ccc;
}

.empty-records {
  text-align: center;
  padding: 40px 0;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

.device-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.device-item:last-child {
  border-bottom: none;
}

.device-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 4px;
}

.device-status-dot.online {
  background: #34c759;
}

.device-status-dot.offline {
  background: #8e8e93;
}

.device-status-dot.fault {
  background: #ff3b30;
}

.device-info {
  flex: 1;
}

.device-item .device-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 2px;
}

.device-location {
  font-size: 12px;
  color: #666;
}

.device-control {
  padding: 6px 12px;
  border: 1px solid #667eea;
  border-radius: 6px;
}

.control-btn {
  font-size: 12px;
  color: #667eea;
}

.device-select-popup {
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

.device-list {
  max-height: 60vh;
}

.device-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.device-option:last-child {
  border-bottom: none;
}

.device-option-info .device-option-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 2px;
}

.device-option-location {
  font-size: 14px;
  color: #666;
}

.device-option-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.device-option-status.online {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}
</style>