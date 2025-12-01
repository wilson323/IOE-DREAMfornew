<!--
  * 门禁权限管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-permission-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h2>门禁权限管理</h2>
        <p>管理用户门禁权限分配、权限模板和临时权限</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button type="primary" @click="handleAssignPermission">
            <template #icon><UserAddOutlined /></template>
            分配权限
          </a-button>
          <a-button @click="handleCreateTemplate">
            <template #icon><FileAddOutlined /></template>
            创建模板
          </a-button>
          <a-button @click="handleCreateTemporary">
            <template #icon><ClockCircleOutlined /></template>
            临时权限
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 权限统计卡片 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="总权限数"
              :value="permissionStore.permissionTotal"
              :loading="permissionStore.statsLoading"
            >
              <template #prefix><SafetyCertificateOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card active">
            <a-statistic
              title="有效权限"
              :value="permissionStore.activePermissionCount"
              :loading="permissionStore.statsLoading"
            >
              <template #prefix><CheckCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card temporary">
            <a-statistic
              title="临时权限"
              :value="permissionStore.temporaryPermissionCount"
              :loading="permissionStore.statsLoading"
            >
              <template #icon><ClockCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card expiring">
            <a-statistic
              title="即将过期"
              :value="permissionStore.expiringPermissions.length"
              :loading="permissionStore.statsLoading"
            >
              <template #icon><ExclamationCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 查询表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form
        :model="queryForm"
        layout="inline"
        @finish="handleSearch"
      >
        <a-form-item label="用户姓名" name="userName">
          <a-input
            v-model:value="queryForm.userName"
            placeholder="请输入用户姓名"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="卡号" name="userCardNumber">
          <a-input
            v-model:value="queryForm.userCardNumber"
            placeholder="请输入卡号"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="权限类型" name="permissionType">
          <a-select
            v-model:value="queryForm.permissionType"
            placeholder="请选择权限类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="area">区域权限</a-select-option>
            <a-select-option value="device">设备权限</a-select-option>
            <a-select-option value="time_limited">时限权限</a-select-option>
            <a-select-option value="permanent">永久权限</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="权限状态" name="status">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择权限状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="active">有效</a-select-option>
            <a-select-option value="inactive">无效</a-select-option>
            <a-select-option value="expired">过期</a-select-option>
            <a-select-option value="revoked">已撤销</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="permissionStore.permissionLoading">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ClearOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 权限列表 -->
    <a-card :bordered="false">
      <!-- 表格工具栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <a-space>
            <a-button
              type="primary"
              danger
              :disabled="!permissionStore.selectedPermissionIds.length"
              @click="handleBatchRevoke"
            >
              <template #icon><StopOutlined /></template>
              批量撤销
            </a-button>
            <a-button
              :disabled="!permissionStore.selectedPermissionIds.length"
              @click="handleBatchUpdate"
            >
              <template #icon><EditOutlined /></template>
              批量更新
            </a-button>
          </a-space>
        </div>
        <div class="toolbar-right">
          <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
            <a-tab-pane key="all" tab="全部权限" />
            <a-tab-pane key="temporary" tab="临时权限" />
            <a-tab-pane key="expiring" tab="即将过期" />
          </a-tabs>
        </div>
      </div>

      <!-- 权限表格 -->
      <a-table
        :columns="tableColumns"
        :data-source="displayPermissions"
        :loading="permissionStore.permissionLoading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="permissionId"
        @change="handleTableChange"
      >
        <!-- 权限状态 -->
        <template #status="{ record }">
          <a-tag
            :color="getStatusColor(record.status)"
            :icon="getStatusIcon(record.status)"
          >
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <!-- 权限类型 -->
        <template #permissionType="{ record }">
          <a-tag :color="getTypeColor(record.permissionType)">
            {{ getTypeText(record.permissionType) }}
          </a-tag>
          <a-tag v-if="record.isTemporary" color="orange" size="small">临时</a-tag>
        </template>

        <!-- 有效时间 -->
        <template #validTime="{ record }">
          <div class="valid-time">
            <div>开始：{{ formatDateTime(record.validStartTime) }}</div>
            <div>结束：{{ formatDateTime(record.validEndTime) }}</div>
            <div
              v-if="isExpiringSoon(record)"
              class="expiring-warning"
            >
              <ExclamationCircleOutlined /> 即将过期
            </div>
          </div>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              <template #icon><EyeOutlined /></template>
              详情
            </a-button>
            <a-button
              type="link"
              size="small"
              :disabled="record.status !== 'active'"
              @click="handleEditPermission(record)"
            >
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              danger
              :disabled="record.status !== 'active'"
              @click="handleRevokePermission(record)"
            >
              <template #icon><StopOutlined /></template>
              撤销
            </a-button>
            <a-dropdown>
              <a-button type="link" size="small">
                更多
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleCopyPermission(record)">
                    <CopyOutlined />
                    复制权限
                  </a-menu-item>
                  <a-menu-item
                    v-if="record.isTemporary"
                    @click="handleExtendPermission(record)"
                  >
                    <ClockCircleOutlined />
                    延长时间
                  </a-menu-item>
                  <a-menu-item @click="handleViewUserPermissions(record.userId)">
                    <UserOutlined />
                    查看用户权限
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 权限分配弹窗 -->
    <PermissionAssignModal
      v-model:visible="assignModalVisible"
      @success="handleAssignSuccess"
    />

    <!-- 权限详情弹窗 -->
    <PermissionDetailModal
      v-model:visible="detailModalVisible"
      :permission="currentPermission"
    />

    <!-- 权限模板弹窗 -->
    <PermissionTemplateModal
      v-model:visible="templateModalVisible"
      :mode="templateMode"
      :template="currentTemplate"
      @success="handleTemplateSuccess"
    />

    <!-- 临时权限弹窗 -->
    <TemporaryPermissionModal
      v-model:visible="temporaryModalVisible"
      @success="handleTemporarySuccess"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    UserAddOutlined,
    FileAddOutlined,
    ClockCircleOutlined,
    ReloadOutlined,
    SearchOutlined,
    ClearOutlined,
    StopOutlined,
    EditOutlined,
    EyeOutlined,
    DownOutlined,
    CopyOutlined,
    UserOutlined,
    SafetyCertificateOutlined,
    CheckCircleOutlined,
    ExclamationCircleOutlined,
  } from '@ant-design/icons-vue';
  import { useAccessPermissionStore } from '/@/store/modules/business/access-permission';
  import { formatDateTime } from '/@/lib/format';
  import PermissionAssignModal from './components/PermissionAssignModal.vue';
  import PermissionDetailModal from './components/PermissionDetailModal.vue';
  import PermissionTemplateModal from './components/PermissionTemplateModal.vue';
  import TemporaryPermissionModal from './components/TemporaryPermissionModal.vue';

  // 状态管理
  const permissionStore = useAccessPermissionStore();

  // 响应式数据
  const activeTab = ref('all');
  const assignModalVisible = ref(false);
  const detailModalVisible = ref(false);
  const templateModalVisible = ref(false);
  const temporaryModalVisible = ref(false);
  const currentPermission = ref(null);
  const currentTemplate = ref(null);
  const templateMode = ref('add');

  // 查询表单
  const queryForm = reactive({
    userName: '',
    userCardNumber: '',
    permissionType: undefined,
    status: undefined,
  });

  // 表格列定义
  const tableColumns = [
    {
      title: '用户姓名',
      dataIndex: 'userName',
      key: 'userName',
      width: 120,
    },
    {
      title: '卡号',
      dataIndex: 'userCardNumber',
      key: 'userCardNumber',
      width: 150,
    },
    {
      title: '权限区域',
      dataIndex: 'areaName',
      key: 'areaName',
      width: 150,
    },
    {
      title: '权限类型',
      dataIndex: 'permissionType',
      key: 'permissionType',
      width: 150,
      slots: { customRender: 'permissionType' },
    },
    {
      title: '权限状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      slots: { customRender: 'status' },
    },
    {
      title: '有效时间',
      key: 'validTime',
      width: 200,
      slots: { customRender: 'validTime' },
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
      customRender: ({ text }) => formatDateTime(text),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      slots: { customRender: 'action' },
    },
  ];

  // 分页配置
  const pagination = computed(() => ({
    current: permissionStore.queryParams.pageNum,
    pageSize: permissionStore.queryParams.pageSize,
    total: permissionStore.permissionTotal,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条记录`,
  }));

  // 行选择配置
  const rowSelection = computed(() => ({
    selectedRowKeys: permissionStore.selectedPermissionIds,
    onChange: (selectedRowKeys) => {
      permissionStore.setSelectedPermissionIds(selectedRowKeys);
    },
    getCheckboxProps: (record) => ({
      disabled: record.status !== 'active',
    }),
  }));

  // 显示的权限数据
  const displayPermissions = computed(() => {
    let permissions = permissionStore.permissionList;

    if (activeTab.value === 'temporary') {
      permissions = permissions.filter(p => p.isTemporary);
    } else if (activeTab.value === 'expiring') {
      permissions = permissionStore.expiringPermissions;
    }

    return permissions;
  });

  // 方法
  const handleSearch = () => {
    permissionStore.setQueryParams({
      ...queryForm,
      pageNum: 1,
    });
    permissionStore.fetchPermissionList();
  };

  const handleReset = () => {
    Object.assign(queryForm, {
      userName: '',
      userCardNumber: '',
      permissionType: undefined,
      status: undefined,
    });
    permissionStore.resetQueryParams();
    permissionStore.fetchPermissionList();
  };

  const handleRefresh = async () => {
    await Promise.all([
      permissionStore.fetchPermissionList(),
      permissionStore.fetchPermissionStats(),
      permissionStore.fetchExpiringPermissions(),
    ]);
  };

  const handleAssignPermission = () => {
    assignModalVisible.value = true;
  };

  const handleCreateTemplate = () => {
    currentTemplate.value = null;
    templateMode.value = 'add';
    templateModalVisible.value = true;
  };

  const handleCreateTemporary = () => {
    temporaryModalVisible.value = true;
  };

  const handleViewDetail = (permission) => {
    currentPermission.value = permission;
    detailModalVisible.value = true;
  };

  const handleEditPermission = (permission) => {
    currentPermission.value = permission;
    // 实现编辑权限逻辑
    message.info('编辑功能开发中...');
  };

  const handleRevokePermission = (permission) => {
    Modal.confirm({
      title: '撤销权限确认',
      content: `确定要撤销用户 "${permission.userName}" 的权限吗？`,
      okText: '确认撤销',
      cancelText: '取消',
      onOk: async () => {
        const success = await permissionStore.revokePermission(permission.permissionId);
        if (success) {
          message.success('权限撤销成功');
        }
      },
    });
  };

  const handleBatchRevoke = () => {
    Modal.confirm({
      title: '批量撤销确认',
      content: `确定要撤销选中的 ${permissionStore.selectedPermissionIds.length} 个权限吗？`,
      okText: '确认撤销',
      cancelText: '取消',
      onOk: async () => {
        const success = await permissionStore.batchRevokePermissions(
          permissionStore.selectedPermissionIds
        );
        if (success) {
          message.success('批量撤销成功');
        }
      },
    });
  };

  const handleBatchUpdate = () => {
    message.info('批量更新功能开发中...');
  };

  const handleCopyPermission = (permission) => {
    message.info('复制权限功能开发中...');
  };

  const handleExtendPermission = (permission) => {
    message.info('延长时间功能开发中...');
  };

  const handleViewUserPermissions = (userId) => {
    message.info('查看用户权限功能开发中...');
  };

  const handleTabChange = (key) => {
    activeTab.value = key;
    // 根据标签页加载不同的数据
    if (key === 'expiring') {
      permissionStore.fetchExpiringPermissions();
    }
  };

  const handleTableChange = (paginationConfig) => {
    permissionStore.setQueryParams({
      pageNum: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
    });
    permissionStore.fetchPermissionList();
  };

  const handleAssignSuccess = () => {
    assignModalVisible.value = false;
    permissionStore.fetchPermissionList();
    permissionStore.fetchPermissionStats();
  };

  const handleTemplateSuccess = () => {
    templateModalVisible.value = false;
    permissionStore.fetchPermissionTemplates();
  };

  const handleTemporarySuccess = () => {
    temporaryModalVisible.value = false;
    permissionStore.fetchPermissionList();
    permissionStore.fetchExpiringPermissions();
  };

  // 辅助方法
  const getStatusColor = (status) => {
    const colorMap = {
      active: 'green',
      inactive: 'orange',
      expired: 'red',
      revoked: 'default',
      pending: 'blue',
    };
    return colorMap[status] || 'default';
  };

  const getStatusIcon = (status) => {
    const iconMap = {
      active: 'check-circle',
      inactive: 'exclamation-circle',
      expired: 'close-circle',
      revoked: 'stop',
      pending: 'clock-circle',
    };
    return iconMap[status] || 'question-circle';
  };

  const getStatusText = (status) => {
    const textMap = {
      active: '有效',
      inactive: '无效',
      expired: '过期',
      revoked: '已撤销',
      pending: '待审批',
    };
    return textMap[status] || status;
  };

  const getTypeColor = (type) => {
    const colorMap = {
      area: 'blue',
      device: 'green',
      time_limited: 'orange',
      permanent: 'purple',
    };
    return colorMap[type] || 'default';
  };

  const getTypeText = (type) => {
    const textMap = {
      area: '区域权限',
      device: '设备权限',
      time_limited: '时限权限',
      permanent: '永久权限',
    };
    return textMap[type] || type;
  };

  const isExpiringSoon = (permission) => {
    if (!permission.validEndTime) return false;
    const endTime = new Date(permission.validEndTime);
    const now = new Date();
    const diffDays = Math.ceil((endTime.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays > 0;
  };

  // 生命周期
  onMounted(async () => {
    await Promise.all([
      permissionStore.fetchPermissionList(),
      permissionStore.fetchPermissionStats(),
      permissionStore.fetchExpiringPermissions(),
      permissionStore.fetchPermissionTemplates(),
      permissionStore.fetchTimeStrategies(),
    ]);
  });
</script>

<style lang="less" scoped>
  .access-permission-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: 100vh;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .page-title {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 14px;
        }
      }
    }

    .stats-cards {
      margin-bottom: 24px;

      .stat-card {
        text-align: center;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &.active {
          border-left: 4px solid #52c41a;
        }

        &.temporary {
          border-left: 4px solid #faad14;
        }

        &.expiring {
          border-left: 4px solid #ff4d4f;
        }
      }
    }

    .search-card {
      margin-bottom: 24px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .toolbar-left {
        // 左侧工具栏样式
      }

      .toolbar-right {
        // 右侧工具栏样式
      }
    }

    .valid-time {
      font-size: 12px;
      line-height: 1.4;

      .expiring-warning {
        color: #ff4d4f;
        font-weight: 600;
        margin-top: 4px;
      }
    }

    // 响应式布局
    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .stats-cards {
        .ant-col {
          margin-bottom: 16px;
        }
      }

      .table-toolbar {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }
    }
  }
</style>