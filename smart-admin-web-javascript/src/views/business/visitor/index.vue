<!--
 * 访客管理主入口页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h1>访客管理</h1>
        <p>IOE-DREAM智慧园区访客管理系统</p>
      </div>
      <div class="quick-actions">
        <a-space>
          <a-button type="primary" @click="showAppointmentModal">
            <template #icon><PlusOutlined /></template>
            创建预约
          </a-button>
          <a-button @click="showRegistrationModal">
            <template #icon><UserAddOutlined /></template>
            访客登记
          </a-button>
          <a-button @click="goToStatistics">
            <template #icon><BarChartOutlined /></template>
            统计分析
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 系统概览 -->
    <div class="system-overview">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="今日预约"
              :value="overviewStats.todayAppointments || 0"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <CalendarOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="在访访客"
              :value="overviewStats.activeVisitors || 0"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="待审批"
              :value="overviewStats.pendingApprovals || 0"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <ClockCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="今日签到"
              :value="overviewStats.todayCheckIns || 0"
              :value-style="{ color: '#13c2c2' }"
            >
              <template #prefix>
                <CheckCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 快捷功能 -->
    <div class="quick-functions">
      <h2 class="section-title">快捷功能</h2>
      <a-row :gutter="16">
        <a-col :span="8">
          <a-card hoverable @click="goToAppointment">
            <template #cover>
              <div class="function-icon">
                <CalendarOutlined style="font-size: 48px; color: #1890ff;" />
              </div>
            </template>
            <a-card-meta
              title="预约管理"
              description="创建、查询、管理访客预约"
            />
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card hoverable @click="goToVerification">
            <template #cover>
              <div class="function-icon">
                <SafetyCertificateOutlined style="font-size: 48px; color: #52c41a;" />
              </div>
            </template>
            <a-card-meta
              title="访客验证"
              description="二维码、身份证验证"
            />
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card hoverable @click="goToRecord">
            <template #cover>
              <div class="function-icon">
                <HistoryOutlined style="font-size: 48px; color: #faad14;" />
              </div>
            </template>
            <a-card-meta
              title="访客记录"
              description="查询访客历史记录"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 最近预约 -->
    <div class="recent-appointments">
      <div class="section-header">
        <h2 class="section-title">最近预约</h2>
        <a-button type="link" @click="goToAppointment">查看全部</a-button>
      </div>
      <a-table
        :columns="appointmentColumns"
        :data-source="recentAppointments"
        :loading="loadingAppointments"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewAppointmentDetail(record)">
                详情
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                v-if="record.status === 'PENDING'"
                @click="handleCancelAppointment(record)"
              >
                取消
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 预约表单弹窗 -->
    <AppointmentFormModal
      v-model:visible="appointmentModalVisible"
      :appointment-data="currentAppointment"
      @success="handleAppointmentSuccess"
    />

    <!-- 访客详情弹窗 -->
    <VisitorDetailModal
      v-model:visible="detailModalVisible"
      :appointment-id="currentAppointmentId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  UserAddOutlined,
  BarChartOutlined,
  CalendarOutlined,
  UserOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  SafetyCertificateOutlined,
  HistoryOutlined
} from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { visitorApi } from '/@/api/business/visitor/visitor-api';
import AppointmentFormModal from './components/AppointmentFormModal.vue';
import VisitorDetailModal from './components/VisitorDetailModal.vue';

const router = useRouter();

// 响应式数据
const overviewStats = reactive({
  todayAppointments: 0,
  activeVisitors: 0,
  pendingApprovals: 0,
  todayCheckIns: 0
});

const recentAppointments = ref([]);
const loadingAppointments = ref(false);
const appointmentModalVisible = ref(false);
const detailModalVisible = ref(false);
const currentAppointment = ref(null);
const currentAppointmentId = ref(null);

// 表格列配置
const appointmentColumns = [
  {
    title: '访客姓名',
    dataIndex: 'visitorName',
    key: 'visitorName',
  },
  {
    title: '被访人',
    dataIndex: 'visiteeName',
    key: 'visiteeName',
  },
  {
    title: '预约时间',
    dataIndex: 'appointmentTime',
    key: 'appointmentTime',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
  },
];

// 获取概览统计
const loadOverviewStats = async () => {
  try {
    const userId = 1; // 从用户状态中获取
    const result = await visitorApi.getPersonalStatistics(userId);
    if (result.code === 200 && result.data) {
      Object.assign(overviewStats, result.data);
    }
  } catch (error) {
    console.error('加载概览统计失败:', error);
  }
};

// 加载最近预约
const loadRecentAppointments = async () => {
  loadingAppointments.value = true;
  try {
    const userId = 1; // 从用户状态中获取
    const result = await visitorApi.getMyAppointments(userId);
    if (result.code === 200 && result.data) {
      recentAppointments.value = result.data.slice(0, 5);
    }
  } catch (error) {
    message.error('加载预约列表失败');
    console.error('加载预约列表失败:', error);
  } finally {
    loadingAppointments.value = false;
  }
};

// 显示预约表单
const showAppointmentModal = () => {
  currentAppointment.value = null;
  appointmentModalVisible.value = true;
};

// 显示登记表单
const showRegistrationModal = () => {
  router.push('/business/visitor/registration');
};

// 预约成功回调
const handleAppointmentSuccess = () => {
  loadRecentAppointments();
  loadOverviewStats();
};

// 查看预约详情
const viewAppointmentDetail = (record) => {
  currentAppointmentId.value = record.appointmentId;
  detailModalVisible.value = true;
};

// 取消预约
const handleCancelAppointment = async (record) => {
  try {
    const userId = 1; // 从用户状态中获取
    const result = await visitorApi.cancelAppointment(record.appointmentId, userId);
    if (result.code === 200) {
      message.success('取消预约成功');
      loadRecentAppointments();
      loadOverviewStats();
    } else {
      message.error(result.message || '取消预约失败');
    }
  } catch (error) {
    message.error('取消预约失败');
    console.error('取消预约失败:', error);
  }
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'orange',
    'APPROVED': 'green',
    'REJECTED': 'red',
    'CANCELLED': 'default',
    'CHECKED_IN': 'blue',
    'CHECKED_OUT': 'cyan'
  };
  return colorMap[status] || 'default';
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消',
    'CHECKED_IN': '已签到',
    'CHECKED_OUT': '已签退'
  };
  return textMap[status] || status;
};

// 页面导航
const goToAppointment = () => {
  router.push('/business/visitor/appointment');
};

const goToVerification = () => {
  router.push('/business/visitor/verification');
};

const goToRecord = () => {
  router.push('/business/visitor/record');
};

const goToStatistics = () => {
  router.push('/business/visitor/statistics');
};

// 初始化
onMounted(() => {
  loadOverviewStats();
  loadRecentAppointments();
});
</script>

<style lang="scss" scoped>
.visitor-management-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.page-title {
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: #1890ff;
  }

  p {
    margin: 0;
    color: #666;
    font-size: 14px;
  }
}

.system-overview {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.quick-functions {
  margin-bottom: 24px;

  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #333;
  }

  .function-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 120px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  }

  :deep(.ant-card-hoverable:hover) {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: all 0.3s;
  }
}

.recent-appointments {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .section-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
      color: #333;
    }
  }
}
</style>

