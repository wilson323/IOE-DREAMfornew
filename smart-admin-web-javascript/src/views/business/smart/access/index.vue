<template>
  <div class="smart-permission-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-page-header
        title="智能权限管理"
        sub-title="基于5级安全权限体系的权限管理平台"
        @back="handleBack"
      >
        <template #extra>
          <a-space>
            <a-button type="primary" @click="showGrantModal">
              <template #icon><PlusOutlined /></template>
              授予权限
            </a-button>
            <a-button @click="refreshData">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </template>
      </a-page-header>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="总用户数"
              :value="statistics.totalUsers"
              :prefix="h(UserOutlined)"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="活跃权限"
              :value="statistics.activePermissions"
              :prefix="h(KeyOutlined)"
              :value-style="{ color: '#3f8600' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="今日权限操作"
              :value="statistics.todayOperations"
              :prefix="h(ThunderboltOutlined)"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="安全事件"
              :value="statistics.securityEvents"
              :prefix="h(ExclamationCircleOutlined)"
              :value-style="{ color: '#cf1322' }"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 权限管理选项卡 -->
    <div class="permission-tabs">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 用户权限概览 -->
        <a-tab-pane key="overview" tab="用户权限概览">
          <UserPermissionOverview @user-select="handleUserSelect" />
        </a-tab-pane>

        <!-- 权限分配 -->
        <a-tab-pane key="assignment" tab="权限分配">
          <PermissionAssignment />
        </a-tab-pane>

        <!-- 安全级别管理 -->
        <a-tab-pane key="security-level" tab="安全级别管理">
          <SecurityLevelManager />
        </a-tab-pane>

        <!-- 区域权限配置 -->
        <a-tab-pane key="area-permission" tab="区域权限配置">
          <AreaPermissionConfig />
        </a-tab-pane>

        <!-- 设备权限配置 -->
        <a-tab-pane key="device-permission" tab="设备权限配置">
          <DevicePermissionConfig />
        </a-tab-pane>

        <!-- 考勤权限配置 -->
        <a-tab-pane key="attendance-permission" tab="考勤权限配置">
          <AttendancePermissionConfig />
        </a-tab-pane>

        <!-- 门禁权限配置 -->
        <a-tab-pane key="access-permission" tab="门禁权限配置">
          <AccessPermissionConfig />
        </a-tab-pane>

        <!-- 权限审计日志 -->
        <a-tab-pane key="audit" tab="权限审计日志">
          <PermissionAudit />
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 授予权限模态框 -->
    <GrantPermissionModal
      v-model:visible="grantModalVisible"
      @success="handleGrantSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined,
  UserOutlined,
  KeyOutlined,
  ThunderboltOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'

// 导入子组件
import UserPermissionOverview from './components/UserPermissionOverview.vue'
import PermissionAssignment from './components/PermissionAssignment.vue'
import SecurityLevelManager from './components/SecurityLevelManager.vue'
import AreaPermissionConfig from './components/AreaPermissionConfig.vue'
import DevicePermissionConfig from './components/DevicePermissionConfig.vue'
import AttendancePermissionConfig from './components/AttendancePermissionConfig.vue'
import AccessPermissionConfig from './components/AccessPermissionConfig.vue'
import PermissionAudit from './components/PermissionAudit.vue'
import GrantPermissionModal from './components/GrantPermissionModal.vue'

// 导入API
import { permissionApi } from '@/api/smart-permission'

const router = useRouter()

// 响应式数据
const activeTab = ref('overview')
const grantModalVisible = ref(false)

// 统计数据
const statistics = reactive({
  totalUsers: 0,
  activePermissions: 0,
  todayOperations: 0,
  securityEvents: 0
})

// 页面方法
const handleBack = () => {
  router.go(-1)
}

const refreshData = async () => {
  try {
    await loadStatistics()
    message.success('数据刷新成功')
  } catch (error) {
    message.error('数据刷新失败')
  }
}

const showGrantModal = () => {
  grantModalVisible.value = true
}

const handleTabChange = (key) => {
  console.log('切换到选项卡:', key)
}

const handleUserSelect = (user) => {
  console.log('选择用户:', user)
  activeTab.value = 'assignment'
}

const handleGrantSuccess = () => {
  grantModalVisible.value = false
  refreshData()
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await permissionApi.getStatistics()
    if (response.data) {
      Object.assign(statistics, response.data)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadStatistics()
})
</script>

<style lang="less" scoped>
.smart-permission-container {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 64px);

  .page-header {
    background-color: #fff;
    padding: 16px 24px;
    margin-bottom: 16px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .statistics-cards {
    margin-bottom: 16px;

    .ant-card {
      text-align: center;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transition: all 0.3s ease;
      }
    }
  }

  .permission-tabs {
    background-color: #fff;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    :deep(.ant-tabs-content-holder) {
      padding-top: 16px;
    }

    :deep(.ant-tabs-tab) {
      font-weight: 500;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .smart-permission-container {
    padding: 16px;

    .statistics-cards {
      .ant-col {
        margin-bottom: 16px;
      }
    }
  }
}
</style>