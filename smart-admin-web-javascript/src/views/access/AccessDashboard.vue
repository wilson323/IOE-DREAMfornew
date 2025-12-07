<!--
  门禁控制台主界面
  基于SmartAdmin框架的Vue3企业级门禁管理系统

  @Author: 老王
  @Date: 2025-12-01
  @Wechat: 老王牛逼
  @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
-->

<template>
  <div class="access-dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <a-card :bordered="false">
        <div class="header-content">
          <div class="header-left">
            <h2>
              <SafetyCertificateOutlined />
              门禁控制台
            </h2>
            <p>企业级智能门禁管理系统</p>
          </div>
          <div class="header-right">
            <a-space>
              <a-button @click="refreshData" :loading="loading">
                <ReloadOutlined />
                刷新数据
              </a-button>
              <a-button type="primary">
                <PlusOutlined />
                快速操作
              </a-button>
            </a-space>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 主要内容区域 -->
    <div class="dashboard-content">
      <!-- 统计卡片行 -->
      <a-row :gutter="[16, 16]" class="stats-row">
        <a-col :xs="24" :sm="12" :md="6">
          <a-card>
            <a-statistic
              title="设备总数"
              :value="dashboardStats.totalDevices"
              :value-style="{ color: '#1890ff', fontSize: '24px', fontWeight: 'bold' }"
            >
              <template #prefix>
                <SecurityScanOutlined style="color: #1890ff" />
              </template>
            </a-statistic>
            <div class="stat-detail">
              在线: {{ dashboardStats.activeDevices }} / 离线: {{ dashboardStats.offlineDevices }}
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card>
            <a-statistic
              title="今日通行"
              :value="dashboardStats.todayRecords"
              :value-style="{ color: '#52c41a', fontSize: '24px', fontWeight: 'bold' }"
            >
              <template #prefix>
                <UserOutlined style="color: #52c41a" />
              </template>
            </a-statistic>
            <div class="stat-detail">
              本周: {{ dashboardStats.thisWeekRecords }} 次
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card>
            <a-statistic
              title="区域总数"
              :value="dashboardStats.totalAreas"
              :value-style="{ color: '#722ed1', fontSize: '24px', fontWeight: 'bold' }"
            >
              <template #prefix>
                <ApartmentOutlined style="color: #722ed1" />
              </template>
            </a-statistic>
            <div class="stat-detail">
              管理区域覆盖
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card>
            <a-statistic
              title="紧急告警"
              :value="dashboardStats.emergencyAlerts"
              :value-style="{ color: '#f5222d', fontSize: '24px', fontWeight: 'bold' }"
            >
              <template #prefix>
                <WarningOutlined style="color: #f5222d" />
              </template>
            </a-statistic>
            <div class="stat-detail">
              需要立即处理
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 系统健康度和功能卡片 -->
      <a-row :gutter="[16, 16]" style="margin-top: 16px">
        <!-- 系统健康度 -->
        <a-col :span="8">
          <a-card title="系统健康度" :bordered="false">
            <div class="health-monitor">
              <a-progress
                type="circle"
                :percent="dashboardStats.systemHealth"
                :stroke-color="getHealthColor(dashboardStats.systemHealth)"
                :width="120"
              />
              <div class="health-info">
                <div class="health-item">
                  <span>设备在线率:</span>
                  <a-progress
                    :percent="getDeviceOnlineRate()"
                    size="small"
                    :show-info="false"
                    :stroke-color="getHealthColor(getDeviceOnlineRate())"
                  />
                  <span class="health-value">{{ getDeviceOnlineRate() }}%</span>
                </div>
                <div class="health-item">
                  <span>API响应:</span>
                  <a-tag color="green">正常</a-tag>
                </div>
                <div class="health-item">
                  <span>数据同步:</span>
                  <a-tag color="green">正常</a-tag>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 快速功能 -->
        <a-col :span="16">
          <a-card title="快速功能" :bordered="false">
            <a-row :gutter="[16, 16]">
              <a-col :span="6" v-for="feature in quickFeatures" :key="feature.key">
                <div class="feature-card" @click="handleFeatureClick(feature)">
                  <div class="feature-icon" :style="{ backgroundColor: feature.color }">
                    <component :is="feature.icon" />
                  </div>
                  <div class="feature-title">{{ feature.title }}</div>
                  <div class="feature-desc">{{ feature.description }}</div>
                </div>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>

      <!-- 紧急告警 -->
      <a-row v-if="emergencyAlerts.length > 0" style="margin-top: 16px">
        <a-col :span="24">
          <a-alert
            type="error"
            show-icon
            closable
            @close="handleAlertClose"
          >
            <template #message>
              <span>紧急告警 ({{ emergencyAlerts.length }}条)</span>
            </template>
            <template #description>
              <div class="alert-list">
                <div v-for="alert in emergencyAlerts.slice(0, 3)" :key="alert.id" class="alert-item">
                  <ExclamationCircleOutlined />
                  {{ alert.title }} - {{ alert.description }}
                  <a-button size="small" type="link" @click="handleAlertClick(alert)">处理</a-button>
                </div>
              </div>
            </template>
          </a-alert>
        </a-col>
      </a-row>

      <!-- 详细信息选项卡 -->
      <a-card :bordered="false" style="margin-top: 16px">
        <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
          <!-- 设备监控 -->
          <a-tab-pane key="devices" tab="设备监控">
            <DeviceMonitoring :devices="activeDevices" @refresh="refreshDevices" />
          </a-tab-pane>

          <!-- 通行记录 -->
          <a-tab-pane key="records" tab="通行记录">
            <AccessRecords :records="recentRecords" @refresh="refreshRecords" />
          </a-tab-pane>

          <!-- 疏散管理 -->
          <a-tab-pane key="evacuation" tab="疏散管理">
            <EvacuationManagement :points="evacuationPoints" @refresh="refreshEvacuationPoints" />
          </a-tab-pane>

          <!-- 高级功能 -->
          <a-tab-pane key="advanced" tab="高级功能">
            <AdvancedFeatures @refresh="refreshData" />
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>

    <!-- 设备控制弹窗 -->
    <DeviceControlModal
      v-model:visible="deviceControlVisible"
      :device="selectedDevice"
      @success="handleDeviceControlSuccess"
    />

    <!-- 通行记录详情弹窗 -->
    <RecordDetailModal
      v-model:visible="recordDetailVisible"
      :record="selectedRecord"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SafetyCertificateOutlined,
  SecurityScanOutlined,
  UserOutlined,
  ApartmentOutlined,
  WarningOutlined,
  ReloadOutlined,
  PlusOutlined,
  ExclamationCircleOutlined,
  FireOutlined,
  SafetyCertificateOutlined as SafetyIcon,
  AlertOutlined,
  BarChartOutlined,
  TeamOutlined
} from '@ant-design/icons-vue';
import accessApi from '/@/api/access/access-api.js';
import DeviceMonitoring from './components/DeviceMonitoring.vue';
import AccessRecords from './components/AccessRecords.vue';
import EvacuationManagement from './components/EvacuationManagement.vue';
import AdvancedFeatures from './components/AdvancedFeatures.vue';
import DeviceControlModal from './components/DeviceControlModal.vue';
import RecordDetailModal from './components/RecordDetailModal.vue';

// 响应式数据
const loading = ref(false);
const activeTab = ref('devices');
const deviceControlVisible = ref(false);
const recordDetailVisible = ref(false);

// 仪表板统计数据
const dashboardStats = reactive({
  totalDevices: 0,
  activeDevices: 0,
  offlineDevices: 0,
  todayRecords: 0,
  thisWeekRecords: 0,
  totalAreas: 0,
  emergencyAlerts: 0,
  systemHealth: 0
});

// 数据列表
const activeDevices = ref([]);
const recentRecords = ref([]);
const emergencyAlerts = ref([]);
const evacuationPoints = ref([]);
const selectedDevice = ref(null);
const selectedRecord = ref(null);

// 快速功能配置
const quickFeatures = ref([
  {
    key: 'global-linkage',
    title: '全局联动',
    description: '设备联动控制',
    icon: 'FireOutlined',
    color: '#f5222d'
  },
  {
    key: 'global-interlock',
    title: '全局互锁',
    description: '安全互锁管理',
    icon: 'SafetyIcon',
    color: '#1890ff'
  },
  {
    key: 'emergency-response',
    title: '应急响应',
    description: '紧急情况处理',
    icon: 'AlertOutlined',
    color: '#faad14'
  },
  {
    key: 'data-analysis',
    title: '数据分析',
    description: '通行统计分析',
    icon: 'BarChartOutlined',
    color: '#52c41a'
  }
]);

// 定时刷新定时器
let refreshTimer = null;

// 生命周期
onMounted(() => {
  refreshData();
  startAutoRefresh();
});

onUnmounted(() => {
  stopAutoRefresh();
});

// 方法

/**
 * 刷新所有数据
 */
const refreshData = async () => {
  loading.value = true;
  try {
    const promises = [
      loadDashboardStats(),
      loadActiveDevices(),
      loadRecentRecords(),
      loadEmergencyAlerts(),
      loadEvacuationPoints()
    ];

    await Promise.all(promises);
    message.success('数据刷新成功');
  } catch (error) {
    console.error('刷新数据失败:', error);
    message.error('刷新数据失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载仪表板统计数据
 */
const loadDashboardStats = async () => {
  try {
    const response = await accessApi.getDashboardStats();
    if (response.code === 1 && response.data) {
      Object.assign(dashboardStats, response.data);
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

/**
 * 加载活跃设备
 */
const loadActiveDevices = async () => {
  try {
    const response = await accessApi.getActiveDevices();
    if (response.code === 1 && response.data) {
      activeDevices.value = response.data;
    }
  } catch (error) {
    console.error('加载设备数据失败:', error);
  }
};

/**
 * 加载最近通行记录
 */
const loadRecentRecords = async () => {
  try {
    const response = await accessApi.getRecentRecords({ pageSize: 20 });
    if (response.code === 1 && response.data) {
      recentRecords.value = response.data.records || [];
    }
  } catch (error) {
    console.error('加载通行记录失败:', error);
  }
};

/**
 * 加载紧急告警
 */
const loadEmergencyAlerts = async () => {
  try {
    const response = await accessApi.getEmergencyAlerts({ status: 'ACTIVE' });
    if (response.code === 1 && response.data) {
      emergencyAlerts.value = response.data.alerts || [];
    }
  } catch (error) {
    console.error('加载紧急告警失败:', error);
  }
};

/**
 * 加载疏散点
 */
const loadEvacuationPoints = async () => {
  try {
    const response = await accessApi.getEvacuationPoints();
    if (response.code === 1 && response.data) {
      evacuationPoints.value = response.data;
    }
  } catch (error) {
    console.error('加载疏散点失败:', error);
  }
};

/**
 * 刷新设备
 */
const refreshDevices = () => {
  loadActiveDevices();
};

/**
 * 刷新记录
 */
const refreshRecords = () => {
  loadRecentRecords();
};

/**
 * 刷新疏散点
 */
const refreshEvacuationPoints = () => {
  loadEvacuationPoints();
};

/**
 * 开始自动刷新
 */
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    refreshData();
  }, 30000); // 30秒刷新一次
};

/**
 * 停止自动刷新
 */
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
};

/**
 * 获取设备在线率
 */
const getDeviceOnlineRate = () => {
  if (dashboardStats.totalDevices === 0) return 0;
  return Math.round((dashboardStats.activeDevices / dashboardStats.totalDevices) * 100);
};

/**
 * 获取健康度颜色
 */
const getHealthColor = (percent) => {
  if (percent >= 80) return '#52c41a';
  if (percent >= 60) return '#faad14';
  return '#f5222d';
};

/**
 * 处理功能点击
 */
const handleFeatureClick = (feature) => {
  switch (feature.key) {
    case 'global-linkage':
      // 跳转到全局联动管理页面
      window.open('/business/access/advanced/global-linkage-management', '_blank');
      break;
    case 'global-interlock':
      activeTab.value = 'advanced';
      break;
    case 'emergency-response':
      activeTab.value = 'evacuation';
      break;
    case 'data-analysis':
      // 跳转到数据分析页面
      break;
    default:
      break;
  }
};

/**
 * 处理选项卡切换
 */
const handleTabChange = (key) => {
  activeTab.value = key;
};

/**
 * 处理告警关闭
 */
const handleAlertClose = () => {
  // 处理告警关闭逻辑
};

/**
 * 处理告警点击
 */
const handleAlertClick = (alert) => {
  // 处理告警详情逻辑
};

/**
 * 处理设备控制成功
 */
const handleDeviceControlSuccess = () => {
  refreshDevices();
  loadDashboardStats();
};
</script>

<style scoped lang="less">
.access-dashboard {
  .dashboard-header {
    margin-bottom: 16px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        h2 {
          margin: 0;
          color: #1890ff;
          font-size: 24px;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        p {
          margin: 4px 0 0 0;
          color: #666;
          font-size: 14px;
        }
      }
    }
  }

  .dashboard-content {
    .stats-row {
      .stat-detail {
        margin-top: 8px;
        font-size: 12px;
        color: #666;
      }
    }

    .health-monitor {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;

      .health-info {
        width: 100%;

        .health-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .health-value {
            margin-left: 8px;
            font-weight: 500;
            color: #1890ff;
          }
        }
      }
    }

    .feature-card {
      text-align: center;
      padding: 16px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      border: 1px solid #f0f0f0;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .feature-icon {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 12px;
        font-size: 24px;
        color: white;
      }

      .feature-title {
        font-size: 14px;
        font-weight: 500;
        color: #262626;
        margin-bottom: 4px;
      }

      .feature-desc {
        font-size: 12px;
        color: #666;
      }
    }

    .alert-list {
      .alert-item {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .access-dashboard {
    .dashboard-header {
      .header-content {
        flex-direction: column;
        gap: 16px;
        align-items: flex-start;
      }
    }

    .dashboard-content {
      .health-monitor {
        .health-info {
          .health-item {
            flex-direction: column;
            align-items: flex-start;
            gap: 4px;
          }
        }
      }

      .feature-card {
        .feature-icon {
          width: 40px;
          height: 40px;
          font-size: 20px;
        }
      }
    }
  }
}
</style>