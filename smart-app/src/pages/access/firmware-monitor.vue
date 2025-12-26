<template>
  <view class="firmware-monitor-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">固件升级监控</view>
        <view class="navbar-right" @tap="refreshData">
          <uni-icons type="refresh" size="20" color="#ffffff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view class="main-scroll" scroll-y>
      <!-- 统计概览 -->
      <view class="stats-section">
        <view class="stats-card">
          <view class="stat-item">
            <text class="stat-value">{{ totalTasks }}</text>
            <text class="stat-label">总任务数</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value running">{{ runningTasks }}</text>
            <text class="stat-label">进行中</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value success">{{ successTasks }}</text>
            <text class="stat-label">已完成</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value failed">{{ failedTasks }}</text>
            <text class="stat-label">失败</text>
          </view>
        </view>
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

      <!-- 任务列表 -->
      <view class="tasks-section">
        <view
          v-for="task in filteredTasks"
          :key="task.taskId"
          class="task-card"
          @tap="viewTaskDetail(task)"
        >
          <!-- 任务头部 -->
          <view class="task-header">
            <view class="task-info">
              <text class="task-name">{{ task.firmwareName }}</text>
              <text class="task-version">v{{ task.targetVersion }}</text>
            </view>
            <view class="task-status" :class="getStatusClass(task.taskStatus)">
              <text class="status-text">{{ getStatusText(task.taskStatus) }}</text>
            </view>
          </view>

          <!-- 进度条 -->
          <view class="progress-section">
            <view class="progress-info">
              <text class="progress-text">升级进度</text>
              <text class="progress-percent">{{ task.progress }}%</text>
            </view>
            <view class="progress-bar-bg">
              <view
                class="progress-bar-fill"
                :style="{ width: task.progress + '%' }"
              ></view>
            </view>
            <view class="progress-detail">
              <text class="detail-text">成功: {{ task.successCount }}</text>
              <text class="detail-text">失败: {{ task.failedCount }}</text>
              <text class="detail-text">待升级: {{ task.pendingCount }}</text>
            </view>
          </view>

          <!-- 任务信息 -->
          <view class="task-meta">
            <view class="meta-item">
              <uni-icons type="calendar" size="14" color="#999999"></uni-icons>
              <text class="meta-text">{{ task.createTime }}</text>
            </view>
            <view class="meta-item">
              <uni-icons type="person" size="14" color="#999999"></uni-icons>
              <text class="meta-text">{{ task.deviceCount }}台设备</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="task-actions" v-if="task.taskStatus === 2 || task.taskStatus === 4">
            <button
              v-if="task.taskStatus === 2"
              class="action-btn pause-btn"
              size="mini"
              @tap.stop="pauseTask(task)"
            >
              暂停
            </button>
            <button
              v-if="task.taskStatus === 4"
              class="action-btn resume-btn"
              size="mini"
              @tap.stop="resumeTask(task)"
            >
              继续
            </button>
            <button
              v-if="task.taskStatus === 2 || task.taskStatus === 4"
              class="action-btn retry-btn"
              size="mini"
              @tap.stop="retryFailedDevices(task)"
            >
              重试
            </button>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="filteredTasks.length === 0">
          <uni-icons type="loop" size="60" color="#CCCCCC"></uni-icons>
          <text class="empty-text">暂无升级任务</text>
        </view>
      </view>
    </scroll-view>

    <!-- 任务详情弹窗 -->
    <uni-popup ref="taskDetailPopup" type="bottom" :safe-area="false">
      <view class="task-detail-popup">
        <view class="popup-header">
          <text class="popup-title">任务详情</text>
          <view class="popup-close" @tap="closeTaskDetail">
            <uni-icons type="close" size="20" color="#666666"></uni-icons>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <!-- 基本信息 -->
          <view class="detail-section">
            <view class="section-title">基本信息</view>
            <view class="detail-item">
              <text class="detail-label">任务编号</text>
              <text class="detail-value">{{ currentTask.taskNo }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">固件名称</text>
              <text class="detail-value">{{ currentTask.firmwareName }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">目标版本</text>
              <text class="detail-value">v{{ currentTask.targetVersion }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">升级策略</text>
              <text class="detail-value">{{ getStrategyText(currentTask.upgradeStrategy) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">创建时间</text>
              <text class="detail-value">{{ currentTask.createTime }}</text>
            </view>
          </view>

          <!-- 进度统计 -->
          <view class="detail-section">
            <view class="section-title">进度统计</view>
            <view class="progress-grid">
              <view class="progress-card total">
                <text class="card-value">{{ currentTask.deviceCount }}</text>
                <text class="card-label">总设备数</text>
              </view>
              <view class="progress-card success">
                <text class="card-value">{{ currentTask.successCount }}</text>
                <text class="card-label">成功</text>
              </view>
              <view class="progress-card failed">
                <text class="card-value">{{ currentTask.failedCount }}</text>
                <text class="card-label">失败</text>
              </view>
              <view class="progress-card pending">
                <text class="card-value">{{ currentTask.pendingCount }}</text>
                <text class="card-label">待升级</text>
              </view>
            </view>
          </view>

          <!-- 设备列表 -->
          <view class="detail-section">
            <view class="section-title">设备列表</view>
            <view class="device-list">
              <view
                v-for="device in currentTask.devices"
                :key="device.deviceId"
                class="device-item"
                :class="getDeviceStatusClass(device.upgradeStatus)"
              >
                <view class="device-info">
                  <text class="device-name">{{ device.deviceName }}</text>
                  <text class="device-code">{{ device.deviceCode }}</text>
                </view>
                <view class="device-status">
                  <text class="status-label">{{ getDeviceStatusText(device.upgradeStatus) }}</text>
                </view>
                <view class="device-error" v-if="device.errorMessage">
                  <text class="error-text">{{ device.errorMessage }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>

        <!-- 操作按钮 -->
        <view class="popup-actions">
          <button class="popup-btn close-btn" @tap="closeTaskDetail">关闭</button>
          <button
            v-if="currentTask.taskStatus === 2 || currentTask.taskStatus === 4"
            class="popup-btn retry-btn"
            @tap="retryCurrentTask"
          >
            重试失败设备
          </button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';

// 状态筛选标签
const statusTabs = [
  { label: '全部', value: 0 },
  { label: '待升级', value: 1 },
  { label: '升级中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已暂停', value: 4 },
  { label: '已停止', value: 5 },
  { label: '已回滚', value: 6 }
];

const activeStatus = ref(0);
const taskList = ref([]);
const currentTask = ref({});
const taskDetailPopup = ref(null);

let refreshTimer = null;

// 计算属性
const filteredTasks = computed(() => {
  if (activeStatus.value === 0) {
    return taskList.value;
  }
  return taskList.value.filter(task => task.taskStatus === activeStatus.value);
});

const totalTasks = computed(() => taskList.value.length);
const runningTasks = computed(() => taskList.value.filter(t => t.taskStatus === 2).length);
const successTasks = computed(() => taskList.value.filter(t => t.taskStatus === 3).length);
const failedTasks = computed(() => taskList.value.filter(t => t.failedCount > 0).length);

// 生命周期
onMounted(() => {
  loadTaskList();
  // 每10秒刷新一次
  refreshTimer = setInterval(() => {
    loadTaskList();
  }, 10000);
});

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }
});

// 加载任务列表
async function loadTaskList() {
  try {
    // TODO: 调用实际API
    // const response = await accessApi.queryUpgradeTasksPage({
    //   pageNum: 1,
    //   pageSize: 100
    // });
    // taskList.value = response.data.list;

    // 模拟数据
    taskList.value = [
      {
        taskId: 1,
        taskNo: 'UPG202501250001',
        firmwareName: '门禁控制器固件',
        targetVersion: '2.1.0',
        taskStatus: 2, // 升级中
        upgradeStrategy: 1, // 立即升级
        progress: 65,
        deviceCount: 20,
        successCount: 13,
        failedCount: 2,
        pendingCount: 5,
        createTime: '2025-01-25 14:00:00',
        devices: []
      },
      {
        taskId: 2,
        taskNo: 'UPG202501250002',
        firmwareName: '考勤机固件',
        targetVersion: '1.5.2',
        taskStatus: 3, // 已完成
        upgradeStrategy: 2, // 定时升级
        progress: 100,
        deviceCount: 15,
        successCount: 15,
        failedCount: 0,
        pendingCount: 0,
        createTime: '2025-01-25 13:00:00',
        devices: []
      },
      {
        taskId: 3,
        taskNo: 'UPG202501250003',
        firmwareName: '消费机固件',
        targetVersion: '3.0.1',
        taskStatus: 4, // 已暂停
        upgradeStrategy: 3, // 分批升级
        progress: 35,
        deviceCount: 30,
        successCount: 10,
        failedCount: 3,
        pendingCount: 17,
        createTime: '2025-01-25 12:00:00',
        devices: []
      }
    ];
  } catch (error) {
    console.error('加载任务列表失败:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  }
}

// 状态切换
function handleStatusChange(status) {
  activeStatus.value = status;
}

// 获取状态样式类
function getStatusClass(status) {
  const classMap = {
    1: 'pending',
    2: 'running',
    3: 'success',
    4: 'paused',
    5: 'stopped',
    6: 'rolled-back'
  };
  return classMap[status] || '';
}

// 获取状态文本
function getStatusText(status) {
  const textMap = {
    1: '待升级',
    2: '升级中',
    3: '已完成',
    4: '已暂停',
    5: '已停止',
    6: '已回滚'
  };
  return textMap[status] || '未知';
}

// 获取策略文本
function getStrategyText(strategy) {
  const textMap = {
    1: '立即升级',
    2: '定时升级',
    3: '分批升级'
  };
  return textMap[strategy] || '未知';
}

// 获取设备状态样式类
function getDeviceStatusClass(status) {
  const classMap = {
    1: 'pending',
    2: 'upgrading',
    3: 'success',
    4: 'failed'
  };
  return classMap[status] || '';
}

// 获取设备状态文本
function getDeviceStatusText(status) {
  const textMap = {
    1: '待升级',
    2: '升级中',
    3: '成功',
    4: '失败'
  };
  return textMap[status] || '未知';
}

// 查看任务详情
function viewTaskDetail(task) {
  currentTask.value = task;
  // TODO: 加载设备详情
  taskDetailPopup.value.open();
}

// 关闭任务详情
function closeTaskDetail() {
  taskDetailPopup.value.close();
}

// 暂停任务
async function pauseTask(task) {
  try {
    uni.showLoading({ title: '暂停中...' });
    // TODO: 调用实际API
    // await accessApi.pauseUpgradeTask(task.taskId);

    uni.hideLoading();
    uni.showToast({
      title: '已暂停',
      icon: 'success'
    });
    loadTaskList();
  } catch (error) {
    uni.hideLoading();
    uni.showToast({
      title: '暂停失败',
      icon: 'none'
    });
  }
}

// 恢复任务
async function resumeTask(task) {
  try {
    uni.showLoading({ title: '恢复中...' });
    // TODO: 调用实际API
    // await accessApi.resumeUpgradeTask(task.taskId);

    uni.hideLoading();
    uni.showToast({
      title: '已恢复',
      icon: 'success'
    });
    loadTaskList();
  } catch (error) {
    uni.hideLoading();
    uni.showToast({
      title: '恢复失败',
      icon: 'none'
    });
  }
}

// 重试失败设备
async function retryFailedDevices(task) {
  try {
    uni.showLoading({ title: '重试中...' });
    // TODO: 调用实际API
    // await accessApi.retryFailedDevices(task.taskId);

    uni.hideLoading();
    uni.showToast({
      title: '重试成功',
      icon: 'success'
    });
    loadTaskList();
  } catch (error) {
    uni.hideLoading();
    uni.showToast({
      title: '重试失败',
      icon: 'none'
    });
  }
}

// 重试当前任务
async function retryCurrentTask() {
  await retryFailedDevices(currentTask.value);
  closeTaskDetail();
}

// 刷新数据
function refreshData() {
  loadTaskList();
  uni.showToast({
    title: '已刷新',
    icon: 'success'
  });
}

// 返回
function goBack() {
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
.firmware-monitor-page {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 15px;

    .navbar-left {
      display: flex;
      align-items: center;
      width: 60px;

      .back-text {
        font-size: 14px;
        color: #ffffff;
        margin-left: 4px;
      }
    }

    .navbar-title {
      font-size: 17px;
      font-weight: 600;
      color: #ffffff;
    }

    .navbar-right {
      width: 60px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 主内容区域
.main-scroll {
  height: 100vh;
  padding-top: 44px;
}

// 统计概览
.stats-section {
  padding: 15px;

  .stats-card {
    display: flex;
    background: #ffffff;
    border-radius: 12px;
    padding: 20px 15px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .stat-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #333333;
        margin-bottom: 4px;

        &.running {
          color: #1890ff;
        }

        &.success {
          color: #52c41a;
        }

        &.failed {
          color: #ff4d4f;
        }
      }

      .stat-label {
        font-size: 12px;
        color: #999999;
      }
    }

    .stat-divider {
      width: 1px;
      background: #e8e8e8;
      margin: 0 10px;
    }
  }
}

// 筛选栏
.filter-bar {
  background: #ffffff;
  padding: 10px 15px;
  margin-bottom: 10px;

  .filter-tabs {
    display: flex;

    .filter-tab {
      position: relative;
      padding: 8px 16px;
      margin-right: 10px;

      .tab-text {
        font-size: 14px;
        color: #666666;
      }

      &.active {
        .tab-text {
          color: #1890ff;
          font-weight: 600;
        }

        .tab-indicator {
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 20px;
          height: 2px;
          background: #1890ff;
          border-radius: 1px;
        }
      }
    }
  }
}

// 任务列表
.tasks-section {
  padding: 0 15px 15px;

  .task-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 15px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .task-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .task-info {
        flex: 1;
        display: flex;
        align-items: center;

        .task-name {
          font-size: 16px;
          font-weight: 600;
          color: #333333;
          margin-right: 8px;
        }

        .task-version {
          font-size: 13px;
          color: #1890ff;
          background: #e6f7ff;
          padding: 2px 8px;
          border-radius: 4px;
        }
      }

      .task-status {
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;

        &.pending {
          background: #f5f5f5;
          color: #999999;
        }

        &.running {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.success {
          background: #f6ffed;
          color: #52c41a;
        }

        &.paused {
          background: #fff7e6;
          color: #fa8c16;
        }

        &.stopped {
          background: #fff1f0;
          color: #ff4d4f;
        }

        &.rolled-back {
          background: #f9f0ff;
          color: #722ed1;
        }

        .status-text {
          font-size: 12px;
        }
      }
    }

    .progress-section {
      margin-bottom: 12px;

      .progress-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;

        .progress-text {
          font-size: 13px;
          color: #666666;
        }

        .progress-percent {
          font-size: 14px;
          font-weight: 600;
          color: #1890ff;
        }
      }

      .progress-bar-bg {
        height: 6px;
        background: #f0f0f0;
        border-radius: 3px;
        overflow: hidden;
        margin-bottom: 6px;

        .progress-bar-fill {
          height: 100%;
          background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%);
          border-radius: 3px;
          transition: width 0.3s;
        }
      }

      .progress-detail {
        display: flex;

        .detail-text {
          font-size: 12px;
          color: #999999;
          margin-right: 15px;
        }
      }
    }

    .task-meta {
      display: flex;
      margin-bottom: 10px;

      .meta-item {
        display: flex;
        align-items: center;
        margin-right: 20px;

        .meta-text {
          font-size: 12px;
          color: #999999;
          margin-left: 4px;
        }
      }
    }

    .task-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding-top: 10px;
      border-top: 1px solid #f0f0f0;

      .action-btn {
        font-size: 13px;
        padding: 0 15px;
        height: 28px;
        line-height: 28px;
        border: none;
        border-radius: 4px;

        &.pause-btn {
          background: #fff7e6;
          color: #fa8c16;
        }

        &.resume-btn {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.retry-btn {
          background: #fff1f0;
          color: #ff4d4f;
        }
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;

  .empty-text {
    font-size: 14px;
    color: #999999;
    margin-top: 15px;
  }
}

// 任务详情弹窗
.task-detail-popup {
  background: #ffffff;
  border-radius: 20px 20px 0 0;
  max-height: 80vh;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 15px 20px;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 16px;
      font-weight: 600;
      color: #333333;
    }

    .popup-close {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    max-height: calc(80vh - 120px);
    padding: 15px 20px;

    .detail-section {
      margin-bottom: 20px;

      .section-title {
        font-size: 15px;
        font-weight: 600;
        color: #333333;
        margin-bottom: 12px;
      }

      .detail-item {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 1px solid #f5f5f5;

        .detail-label {
          font-size: 14px;
          color: #666666;
        }

        .detail-value {
          font-size: 14px;
          color: #333333;
          font-weight: 500;
        }
      }

      .progress-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 10px;

        .progress-card {
          background: #f9f9f9;
          border-radius: 8px;
          padding: 12px;
          text-align: center;

          &.total {
            background: #e6f7ff;
          }

          &.success {
            background: #f6ffed;
          }

          &.failed {
            background: #fff1f0;
          }

          &.pending {
            background: #f5f5f5;
          }

          .card-value {
            display: block;
            font-size: 20px;
            font-weight: 600;
            color: #333333;
            margin-bottom: 4px;
          }

          .card-label {
            font-size: 12px;
            color: #999999;
          }
        }
      }

      .device-list {
        .device-item {
          background: #f9f9f9;
          border-radius: 8px;
          padding: 12px;
          margin-bottom: 10px;

          &.success {
            background: #f6ffed;
          }

          &.failed {
            background: #fff1f0;
          }

          .device-info {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 6px;

            .device-name {
              font-size: 14px;
              font-weight: 500;
              color: #333333;
            }

            .device-code {
              font-size: 12px;
              color: #999999;
            }
          }

          .device-status {
            margin-bottom: 6px;

            .status-label {
              font-size: 12px;
              color: #1890ff;
            }
          }

          .device-error {
            .error-text {
              font-size: 12px;
              color: #ff4d4f;
            }
          }
        }
      }
    }
  }

  .popup-actions {
    display: flex;
    gap: 10px;
    padding: 15px 20px;
    border-top: 1px solid #f0f0f0;

    .popup-btn {
      flex: 1;
      height: 40px;
      line-height: 40px;
      border: none;
      border-radius: 8px;
      font-size: 15px;

      &.close-btn {
        background: #f5f5f5;
        color: #666666;
      }

      &.retry-btn {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #ffffff;
      }
    }
  }
}
</style>
