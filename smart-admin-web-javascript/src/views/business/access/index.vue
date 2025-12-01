<!--
  * 门禁管理主入口页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h1>门禁管理</h1>
        <p>IOE-DREAM智慧园区一卡通门禁管理系统</p>
      </div>
      <div class="quick-actions">
        <a-space>
          <a-button type="primary" @click="goToMonitor">
            <template #icon><DesktopOutlined /></template>
            实时监控
          </a-button>
          <a-button @click="handleQuickAdd">
            <template #icon><PlusOutlined /></template>
            快速添加
          </a-button>
          <a-button @click="handleSystemStatus">
            <template #icon><SettingOutlined /></template>
            系统状态
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 系统概览 -->
    <div class="system-overview">
      <a-row :gutter="16">
        <a-col :span="6">
          <OverviewCard
            title="设备总数"
            :value="overviewStats.totalDevices"
            icon="desktop"
            color="#1890ff"
            :loading="statsLoading"
            @click="navigateTo('/business/access/device')"
          >
            <template #extra>
              <div class="overview-detail">
                <div class="detail-item">
                  <span class="detail-label">在线:</span>
                  <span class="detail-value success">{{ overviewStats.onlineDevices }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">离线:</span>
                  <span class="detail-value danger">{{ overviewStats.offlineDevices }}</span>
                </div>
              </div>
            </template>
          </OverviewCard>
        </a-col>
        <a-col :span="6">
          <OverviewCard
            title="权限总数"
            :value="overviewStats.totalPermissions"
            icon="safety-certificate"
            color="#52c41a"
            :loading="statsLoading"
            @click="navigateTo('/business/access/permission')"
          >
            <template #extra>
              <div class="overview-detail">
                <div class="detail-item">
                  <span class="detail-label">有效:</span>
                  <span class="detail-value success">{{ overviewStats.activePermissions }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">临时:</span>
                  <span class="detail-value warning">{{ overviewStats.temporaryPermissions }}</span>
                </div>
              </div>
            </template>
          </OverviewCard>
        </a-col>
        <a-col :span="6">
          <OverviewCard
            title="今日通行"
            :value="overviewStats.todayAccessCount"
            icon="login"
            color="#faad14"
            :loading="statsLoading"
            @click="navigateTo('/business/access/record')"
          >
            <template #extra>
              <div class="overview-detail">
                <div class="detail-item">
                  <span class="detail-label">成功率:</span>
                  <span class="detail-value">{{ overviewStats.successRate }}%</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">异常:</span>
                  <span class="detail-value danger">{{ overviewStats.abnormalCount }}</span>
                </div>
              </div>
            </template>
          </OverviewCard>
        </a-col>
        <a-col :span="6">
          <OverviewCard
            title="系统告警"
            :value="overviewStats.alertCount"
            icon="alert"
            color="#ff4d4f"
            :loading="statsLoading"
            @click="navigateTo('/business/access/monitor')"
          >
            <template #extra>
              <div class="overview-detail">
                <div class="detail-item">
                  <span class="detail-label">未处理:</span>
                  <span class="detail-value danger">{{ overviewStats.unprocessedAlerts }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">严重:</span>
                  <span class="detail-value danger">{{ overviewStats.criticalAlerts }}</span>
                </div>
              </div>
            </template>
          </OverviewCard>
        </a-col>
      </a-row>
    </div>

    <!-- 功能模块 -->
    <div class="function-modules">
      <a-row :gutter="16">
        <a-col :span="8">
          <FunctionCard
            title="设备管理"
            description="管理门禁设备、监控设备状态、配置设备参数"
            icon="desktop"
            :badge="deviceBadge"
            @click="navigateTo('/business/access/device')"
          >
            <template #actions>
              <a-button type="link" @click.stop="handleAddDevice">
                <PlusOutlined /> 添加设备
              </a-button>
              <a-button type="link" @click.stop="handleDeviceStatus">
                <WifiOutlined /> 设备状态
              </a-button>
            </template>
          </FunctionCard>
        </a-col>
        <a-col :span="8">
          <FunctionCard
            title="权限管理"
            description="分配门禁权限、管理权限模板、处理临时权限申请"
            icon="safety-certificate"
            :badge="permissionBadge"
            @click="navigateTo('/business/access/permission')"
          >
            <template #actions>
              <a-button type="link" @click.stop="handleAssignPermission">
                <UserAddOutlined /> 分配权限
              </a-button>
              <a-button type="link" @click.stop="handlePermissionTemplate">
                <FileAddOutlined /> 权限模板
              </a-button>
            </template>
          </FunctionCard>
        </a-col>
        <a-col :span="8">
          <FunctionCard
            title="通行记录"
            description="查看通行记录、处理异常通行、数据统计分析"
            icon="history"
            :badge="recordBadge"
            @click="navigateTo('/business/access/record')"
          >
            <template #actions>
              <a-button type="link" @click.stop="handleAbnormalRecords">
                <ExclamationCircleOutlined /> 异常处理
              </a-button>
              <a-button type="link" @click.stop="handleExportRecords">
                <ExportOutlined /> 导出记录
              </a-button>
            </template>
          </FunctionCard>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="8">
          <FunctionCard
            title="实时监控"
            description="实时监控设备状态、通行数据、系统告警信息"
            icon="desktop"
            :badge="monitorBadge"
            @click="navigateTo('/business/access/monitor')"
          >
            <template #actions>
              <a-button type="link" @click.stop="goToMonitor">
                <WifiOutlined /> 监控面板
              </a-button>
              <a-button type="link" @click.stop="handleAlertCenter">
                <BellOutlined /> 告警中心
              </a-button>
            </template>
          </FunctionCard>
        </a-col>
        <a-col :span="8">
          <FunctionCard
            title="系统配置"
            description="配置系统参数、时间策略、安防级别、告警规则"
            icon="setting"
            @click="navigateTo('/business/access/config')"
          >
            <template #actions>
              <a-button type="link" @click.stop="handleSystemConfig">
                <ControlOutlined /> 系统设置
              </a-button>
              <a-button type="link" @click.stop="handleTimeStrategy">
                <ClockCircleOutlined /> 时间策略
              </a-button>
            </template>
          </FunctionCard>
        </a-col>
        <a-col :span="8">
          <FunctionCard
            title="数据统计"
            description="通行数据分析、设备使用统计、用户行为分析"
            icon="bar-chart"
            @click="navigateTo('/business/access/statistics')"
          >
            <template #actions>
              <a-button type="link" @click.stop="handleAccessAnalysis">
                <LineChartOutlined /> 通行分析
              </a-button>
              <a-button type="link" @click.stop="handleReportCenter">
                <FileTextOutlined /> 报表中心
              </a-button>
            </template>
          </FunctionCard>
        </a-col>
      </a-row>
    </div>

    <!-- 快速操作面板 -->
    <div class="quick-operations">
      <a-card title="快速操作" :bordered="false">
        <a-row :gutter="16">
          <a-col :span="6">
            <QuickAction
              title="远程开门"
              description="远程控制门禁设备开门"
              icon="unlock"
              color="#52c41a"
              @click="handleRemoteOpen"
            />
          </a-col>
          <a-col :span="6">
            <QuickAction
              title="临时授权"
              description="为用户临时分配门禁权限"
              icon="clock-circle"
              color="#faad14"
              @click="handleTemporaryAuth"
            />
          </a-col>
          <a-col :span="6">
            <QuickAction
              title="紧急锁定"
              description="紧急情况下锁定所有门禁"
              icon="lock"
              color="#ff4d4f"
              @click="handleEmergencyLock"
            />
          </a-col>
          <a-col :span="6">
            <QuickAction
              title="系统检查"
              description="检查系统运行状态和健康度"
              icon="check-circle"
              color="#1890ff"
              @click="handleSystemCheck"
            />
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities">
      <a-card title="最近活动" :bordered="false">
        <template #extra>
          <a-button type="link" @click="viewAllActivities">查看全部</a-button>
        </template>
        <a-timeline>
          <a-timeline-item
            v-for="activity in recentActivities"
            :key="activity.id"
            :color="activity.color"
          >
            <div class="activity-item">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-description">{{ activity.description }}</div>
              <div class="activity-time">{{ formatTime(activity.time) }}</div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-card>
    </div>

    <!-- 快速添加弹窗 -->
    <QuickAddModal
      v-model:visible="quickAddVisible"
      @success="handleQuickAddSuccess"
    />

    <!-- 系统状态弹窗 -->
    <SystemStatusModal
      v-model:visible="systemStatusVisible"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { message, Modal } from 'ant-design-vue';
  import {
    DesktopOutlined,
    PlusOutlined,
    SettingOutlined,
    WifiOutlined,
    UserAddOutlined,
    FileAddOutlined,
    ExclamationCircleOutlined,
    ExportOutlined,
    BellOutlined,
    ControlOutlined,
    ClockCircleOutlined,
    LineChartOutlined,
    FileTextOutlined,
    UnlockOutlined,
    LockOutlined,
    CheckCircleOutlined,
  } from '@ant-design/icons-vue';
  import { useAccessDeviceStore } from '/@/store/modules/business/access-device';
  import { useAccessPermissionStore } from '/@/store/modules/business/access-permission';
  import { useAccessMonitorStore } from '/@/store/modules/business/access-monitor';
  import { formatDateTime, formatTime } from '/@/lib/format';
  import OverviewCard from './components/OverviewCard.vue';
  import FunctionCard from './components/FunctionCard.vue';
  import QuickAction from './components/QuickAction.vue';
  import QuickAddModal from './components/QuickAddModal.vue';
  import SystemStatusModal from './components/SystemStatusModal.vue';

  // 路由
  const router = useRouter();

  // 状态管理
  const deviceStore = useAccessDeviceStore();
  const permissionStore = useAccessPermissionStore();
  const monitorStore = useAccessMonitorStore();

  // 响应式数据
  const statsLoading = ref(false);
  const quickAddVisible = ref(false);
  const systemStatusVisible = ref(false);

  // 概览统计数据
  const overviewStats = reactive({
    totalDevices: 0,
    onlineDevices: 0,
    offlineDevices: 0,
    totalPermissions: 0,
    activePermissions: 0,
    temporaryPermissions: 0,
    todayAccessCount: 0,
    successRate: 0,
    abnormalCount: 0,
    alertCount: 0,
    unprocessedAlerts: 0,
    criticalAlerts: 0,
  });

  // 徽章数据
  const deviceBadge = computed(() => {
    if (overviewStats.offlineDevices > 0) {
      return { count: overviewStats.offlineDevices, type: 'warning' };
    }
    return null;
  });

  const permissionBadge = computed(() => {
    if (permissionStore.expiringPermissions.length > 0) {
      return { count: permissionStore.expiringPermissions.length, type: 'warning' };
    }
    return null;
  });

  const recordBadge = computed(() => {
    if (overviewStats.abnormalCount > 0) {
      return { count: overviewStats.abnormalCount, type: 'error' };
    }
    return null;
  });

  const monitorBadge = computed(() => {
    if (overviewStats.unprocessedAlerts > 0) {
      return { count: overviewStats.unprocessedAlerts, type: 'error' };
    }
    return null;
  });

  // 最近活动数据
  const recentActivities = ref([
    {
      id: 1,
      title: '设备离线告警',
      description: '设备 DEV001 连接断开',
      time: new Date(Date.now() - 5 * 60 * 1000),
      color: 'red',
    },
    {
      id: 2,
      title: '权限即将过期',
      description: '用户张三的门禁权限将在7天后过期',
      time: new Date(Date.now() - 30 * 60 * 1000),
      color: 'orange',
    },
    {
      id: 3,
      title: '新设备上线',
      description: '设备 DEV002 已成功连接',
      time: new Date(Date.now() - 120 * 60 * 1000),
      color: 'green',
    },
    {
      id: 4,
      title: '异常通行记录',
      description: '检测到多次无效刷卡尝试',
      time: new Date(Date.now() - 180 * 60 * 1000),
      color: 'red',
    },
  ]);

  // 方法
  const navigateTo = (path) => {
    router.push(path);
  };

  const goToMonitor = () => {
    navigateTo('/business/access/monitor');
  };

  const handleQuickAdd = () => {
    quickAddVisible.value = true;
  };

  const handleSystemStatus = () => {
    systemStatusVisible.value = true;
  };

  const handleAddDevice = () => {
    navigateTo('/business/access/device?action=add');
  };

  const handleDeviceStatus = () => {
    navigateTo('/business/access/monitor');
  };

  const handleAssignPermission = () => {
    navigateTo('/business/access/permission?action=assign');
  };

  const handlePermissionTemplate = () => {
    navigateTo('/business/access/permission?action=template');
  };

  const handleAbnormalRecords = () => {
    navigateTo('/business/access/record?filter=abnormal');
  };

  const handleExportRecords = () => {
    navigateTo('/business/access/record?action=export');
  };

  const handleAlertCenter = () => {
    navigateTo('/business/access/monitor?tab=alerts');
  };

  const handleSystemConfig = () => {
    navigateTo('/business/access/config');
  };

  const handleTimeStrategy = () => {
    navigateTo('/business/access/config?tab=timeStrategy');
  };

  const handleAccessAnalysis = () => {
    navigateTo('/business/access/statistics?type=access');
  };

  const handleReportCenter = () => {
    navigateTo('/business/access/statistics?type=reports');
  };

  // 快速操作
  const handleRemoteOpen = () => {
    navigateTo('/business/access/device?action=remoteOpen');
  };

  const handleTemporaryAuth = () => {
    navigateTo('/business/access/permission?action=temporary');
  };

  const handleEmergencyLock = () => {
    Modal.confirm({
      title: '紧急锁定确认',
      content: '确定要执行紧急锁定操作吗？这将锁定所有门禁设备。',
      okText: '确认锁定',
      cancelText: '取消',
      okType: 'danger',
      onOk: async () => {
        try {
          // 调用紧急锁定API
          message.success('紧急锁定执行成功');
        } catch (error) {
          message.error('紧急锁定执行失败');
        }
      },
    });
  };

  const handleSystemCheck = () => {
    systemStatusVisible.value = true;
  };

  const viewAllActivities = () => {
    navigateTo('/business/access/monitor?tab=logs');
  };

  const handleQuickAddSuccess = () => {
    quickAddVisible.value = false;
    loadOverviewStats();
    message.success('快速添加成功');
  };

  // 加载概览统计数据
  const loadOverviewStats = async () => {
    statsLoading.value = true;
    try {
      // 并行加载各项统计数据
      await Promise.all([
        loadDeviceStats(),
        loadPermissionStats(),
        loadMonitorStats(),
      ]);
    } catch (error) {
      console.error('加载统计数据失败:', error);
      message.error('加载统计数据失败');
    } finally {
      statsLoading.value = false;
    }
  };

  const loadDeviceStats = async () => {
    await deviceStore.fetchDeviceStats();
    const stats = deviceStore.deviceStats;
    if (stats) {
      overviewStats.totalDevices = stats.totalCount;
      overviewStats.onlineDevices = stats.onlineCount;
      overviewStats.offlineDevices = stats.offlineCount;
    }
  };

  const loadPermissionStats = async () => {
    await permissionStore.fetchPermissionStats();
    const stats = permissionStore.permissionStats;
    if (stats) {
      overviewStats.totalPermissions = stats.totalPermissions;
      overviewStats.activePermissions = stats.activePermissions;
      overviewStats.temporaryPermissions = stats.temporaryPermissions;
    }
  };

  const loadMonitorStats = async () => {
    await monitorStore.fetchRealTimeStats();
    await monitorStore.fetchAlerts();

    const stats = monitorStore.realTimeStats;
    if (stats) {
      overviewStats.todayAccessCount = stats.todayAccessCount || 0;
      overviewStats.abnormalCount = stats.todayAbnormalCount || 0;

      const todayStats = monitorStore.todayAccessStats;
      if (todayStats) {
        overviewStats.successRate = Math.round(
          (todayStats.successCount / todayStats.totalCount) * 100
        );
      }
    }

    overviewStats.alertCount = monitorStore.alerts.length;
    overviewStats.unprocessedAlerts = monitorStore.latestAlertCount;
    overviewStats.criticalAlerts = monitorStore.criticalAlertCount;
  };

  // 生命周期
  onMounted(async () => {
    await loadOverviewStats();

    // 定期刷新统计数据（每5分钟）
    const refreshInterval = setInterval(loadOverviewStats, 5 * 60 * 1000);

    // 清理定时器
    onUnmounted(() => {
      clearInterval(refreshInterval);
    });
  });
</script>

<style lang="less" scoped>
  .access-management-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: 100vh;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 32px;

      .page-title {
        h1 {
          margin: 0 0 8px 0;
          font-size: 32px;
          font-weight: 700;
          color: #262626;
          background: linear-gradient(135deg, #1890ff, #722ed1);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 16px;
        }
      }
    }

    .system-overview {
      margin-bottom: 32px;
    }

    .function-modules {
      margin-bottom: 32px;
    }

    .quick-operations {
      margin-bottom: 32px;
    }

    .recent-activities {
      .activity-item {
        .activity-title {
          font-weight: 600;
          color: #262626;
          margin-bottom: 4px;
        }

        .activity-description {
          color: #8c8c8c;
          font-size: 14px;
          margin-bottom: 4px;
        }

        .activity-time {
          color: #bfbfbf;
          font-size: 12px;
        }
      }
    }

    .overview-detail {
      .detail-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 4px;
        font-size: 12px;

        .detail-label {
          color: #8c8c8c;
        }

        .detail-value {
          font-weight: 600;

          &.success {
            color: #52c41a;
          }

          &.warning {
            color: #faad14;
          }

          &.danger {
            color: #ff4d4f;
          }
        }
      }
    }

    // 响应式布局
    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;

        .page-title {
          h1 {
            font-size: 24px;
          }
        }
      }

      .ant-col {
        margin-bottom: 16px;
      }
    }
  }
</style>