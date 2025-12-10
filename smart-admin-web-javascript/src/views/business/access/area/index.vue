<!--
  * Area Management
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM
-->
<template>
  <div class="access-area-page">
    <!-- Statistic cards -->
    <div class="statistics-cards" style="margin-bottom: 24px;">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card" :bordered="false">
            <a-statistic
              title="Total Areas"
              :value="statistics.totalAreas"
              :prefix="h(BankOutlined)"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card" :bordered="false">
            <a-statistic
              title="Enabled Areas"
              :value="statistics.enabledAreas"
              :prefix="h(CheckCircleOutlined)"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card" :bordered="false">
            <a-statistic
              title="Devices"
              :value="statistics.totalDevices"
              :prefix="h(DesktopOutlined)"
              :value-style="{ color: '#722ed1' }"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-card class="stat-card" :bordered="false">
            <a-statistic
              title="Authorized Users"
              :value="statistics.authorizedUsers"
              :prefix="h(UserOutlined)"
              :value-style="{ color: '#fa8c16' }"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- Query form -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row" :gutter="[16, 16]">
          <a-form-item label="Keyword" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.keyword"
              placeholder="Area name / code"
              style="width: 220px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="Area Type" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.areaType"
              placeholder="Select area type"
              style="width: 150px"
              :allow-clear="true"
            >
              <a-select-option value="BUILDING">Building</a-select-option>
              <a-select-option value="FLOOR">Floor</a-select-option>
              <a-select-option value="ROOM">Room</a-select-option>
              <a-select-option value="ENTRANCE">Entrance</a-select-option>
              <a-select-option value="PARKING">Parking</a-select-option>
              <a-select-option value="OTHER">Other</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="Security Level" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.securityLevel"
              placeholder="Select level"
              style="width: 120px"
              :allow-clear="true"
            >
              <a-select-option value="LOW">Low</a-select-option>
              <a-select-option value="MEDIUM">Medium</a-select-option>
              <a-select-option value="HIGH">High</a-select-option>
              <a-select-option value="CRITICAL">Critical</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="Status" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              placeholder="Select status"
              style="width: 100px"
              :allow-clear="true"
            >
              <a-select-option :value="1">Enabled</a-select-option>
              <a-select-option :value="0">Disabled</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryAreaList">
                <template #icon><SearchOutlined /></template>
                Search
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                Reset
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- Table -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>Area List</span>
          <a-tag v-if="selectedRowKeys.length > 0" color="blue">
            Selected {{ selectedRowKeys.length }}
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            New Area
          </a-button>
          <a-button @click="handleBatchEnable" :disabled="selectedRowKeys.length === 0">
            <template #icon><CheckCircleOutlined /></template>
            Enable
          </a-button>
          <a-button @click="handleBatchDisable" :disabled="selectedRowKeys.length === 0" danger>
            <template #icon><StopOutlined /></template>
            Disable
          </a-button>
          <a-dropdown>
            <a-button>
              More <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleExport">
                  <template #icon><ExportOutlined /></template>
                  Export
                </a-menu-item>
                <a-menu-item @click="handleRefreshStatistics">
                  <template #icon><ReloadOutlined /></template>
                  Refresh Stats
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="areaId"
        @change="handleTableChange"
        :scroll="{ x: 1600 }"
        :default-expand-all="true"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'areaInfo'">
            <div class="area-info">
              <div class="area-name">
                <a-avatar shape="square" :size="32" style="margin-right: 8px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                  <HomeOutlined />
                </a-avatar>
                <div class="area-details">
                  <div class="name">{{ record.areaName }}</div>
                  <div class="code">Code: {{ record.areaCode }}</div>
                </div>
              </div>
              <div v-if="record.description" class="description">{{ record.description }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'areaType'">
            <a-tag :color="getAreaTypeColor(record.areaType)">
              {{ getAreaTypeText(record.areaType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'securityLevel'">
            <a-progress
              :percent="getSecurityPercent(record.securityLevel)"
              :stroke-color="getSecurityColor(record.securityLevel)"
              :show-info="false"
              style="width: 60px; margin-right: 8px;"
            />
            <a-tag :color="getSecurityTagColor(record.securityLevel)">
              {{ getSecurityText(record.securityLevel) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'deviceCount'">
            <a-space direction="vertical" style="width: 100%;">
              <div>
                <a-badge :count="record.deviceCount" :number-style="{ backgroundColor: '#52c41a' }" />
                <span style="margin-left: 8px;">Devices</span>
              </div>
              <div>
                <a-badge :count="record.onlineDeviceCount" :number-style="{ backgroundColor: '#1890ff' }" />
                <span style="margin-left: 8px;">Online</span>
              </div>
            </a-space>
          </template>

          <template v-else-if="column.key === 'userPermissionCount'">
            <a-space direction="vertical" style="width: 100%;">
              <div>
                <a-badge :count="record.totalPermissions" :number-style="{ backgroundColor: '#722ed1' }" />
                <span style="margin-left: 8px;">Permissions</span>
              </div>
              <div>
                <a-badge :count="record.activePermissions" :number-style="{ backgroundColor: '#52c41a' }" />
                <span style="margin-left: 8px;">Active</span>
              </div>
            </a-space>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? 'Enabled' : 'Disabled'"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                <template #icon><EyeOutlined /></template>
                View
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>
                Edit
              </a-button>
              <a-dropdown>
                <a-button type="link" size="small">
                  <template #icon><MoreOutlined /></template>
                  More
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleViewDevices(record)">
                      <template #icon><DesktopOutlined /></template>
                      View Devices
                    </a-menu-item>
                    <a-menu-item @click="handleViewPermissions(record)">
                      <template #icon><SafetyOutlined /></template>
                      View Permissions
                    </a-menu-item>
                    <a-menu-item @click="handleDeviceConfig(record)">
                      <template #icon><SettingOutlined /></template>
                      Device Settings
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="handleToggleStatus(record)" :danger="record.status === 1">
                      <template #icon>
                        <component :is="record.status === 1 ? 'StopOutlined' : 'CheckCircleOutlined'" />
                      </template>
                      {{ record.status === 1 ? 'Disable' : 'Enable' }}
                    </a-menu-item>
                    <a-menu-item @click="handleDelete(record)" danger>
                      <template #icon><DeleteOutlined /></template>
                      Delete
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Form Modal -->
    <AreaFormModal
      v-model:visible="formModalVisible"
      :form-data="currentArea"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />

    <!-- Detail Modal -->
    <AreaDetailModal
      v-model:visible="detailModalVisible"
      :area-id="currentAreaId"
    />

    <!-- Device Modal -->
    <AreaDeviceModal
      v-model:visible="deviceModalVisible"
      :area-id="currentAreaId"
      :area-name="currentAreaName"
    />

    <!-- Permission Modal -->
    <AreaPermissionModal
      v-model:visible="permissionModalVisible"
      :area-id="currentAreaId"
      :area-name="currentAreaName"
    />
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted, watch, h } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import dayjs from 'dayjs';
  import {
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    DownOutlined,
    BankOutlined,
    CheckCircleOutlined,
    DesktopOutlined,
    UserOutlined,
    EyeOutlined,
    EditOutlined,
    MoreOutlined,
    StopOutlined,
    DeleteOutlined,
    ExportOutlined,
    HomeOutlined,
    SafetyOutlined,
    SettingOutlined,
  } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { accessApi } from '/@/api/business/access/access-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import AreaFormModal from './components/AreaFormModal.vue';
  import AreaDetailModal from './components/AreaDetailModal.vue';
  import AreaDeviceModal from './components/AreaDeviceModal.vue';
  import AreaPermissionModal from './components/AreaPermissionModal.vue';

  // Statistics
  const statistics = reactive({
    totalAreas: 0,
    enabledAreas: 0,
    totalDevices: 0,
    authorizedUsers: 0,
  });

  // Query form
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    keyword: null,
    areaType: null,
    securityLevel: null,
    status: null,
  };
  const queryForm = reactive({ ...queryFormState });

  // Table data
  const tableData = ref([]);
  const loading = ref(false);
  const selectedRowKeys = ref([]);

  // Pagination
  const pagination = reactive({
    current: 1,
    pageSize: PAGE_SIZE,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: PAGE_SIZE_OPTIONS,
    showTotal: (total) => `Total ${total} items`,
  });

  // Columns
  const columns = ref([
    {
      title: 'Area',
      key: 'areaInfo',
      width: 280,
      fixed: 'left',
    },
    {
      title: 'Type',
      key: 'areaType',
      width: 120,
    },
    {
      title: 'Security',
      key: 'securityLevel',
      width: 160,
    },
    {
      title: 'Devices',
      key: 'deviceCount',
      width: 140,
    },
    {
      title: 'Permissions',
      key: 'userPermissionCount',
      width: 140,
    },
    {
      title: 'Manager',
      dataIndex: 'managerName',
      key: 'managerName',
      width: 100,
    },
    {
      title: 'Status',
      key: 'status',
      width: 80,
    },
    {
      title: 'Created At',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
    },
    {
      title: 'Actions',
      key: 'action',
      width: 200,
      fixed: 'right',
    },
  ]);

  // Modals
  const formModalVisible = ref(false);
  const detailModalVisible = ref(false);
  const deviceModalVisible = ref(false);
  const permissionModalVisible = ref(false);
  const isEdit = ref(false);
  const currentArea = ref(null);
  const currentAreaId = ref(null);
  const currentAreaName = ref(null);

  // Row selection
  const rowSelection = {
    selectedRowKeys: selectedRowKeys,
    onChange: (keys) => {
      selectedRowKeys.value = keys;
    },
  };

  // Load statistics
  const queryStatistics = async () => {
    try {
      const result = await accessApi.getAreaStatistics();
      if (result.code === 200 && result.data) {
        Object.assign(statistics, result.data);
      }
    } catch (error) {
      console.error('Failed to load statistics:', error);
    }
  };

  // Load area list
  const queryAreaList = async () => {
    loading.value = true;
    try {
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        keyword: queryForm.keyword || undefined,
        areaType: queryForm.areaType || undefined,
        securityLevel: queryForm.securityLevel || undefined,
        status: queryForm.status || undefined,
      };

      const result = await accessApi.queryAreas(params);
      if (result.code === 200 && result.data) {
        tableData.value = result.data.list || [];
        pagination.total = result.data.total || 0;
        pagination.current = result.data.pageNum || 1;
        pagination.pageSize = result.data.pageSize || PAGE_SIZE;
      } else {
        message.error(result.message || 'Failed to load areas');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('Failed to load areas');
    } finally {
      loading.value = false;
    }
  };

  // Reset filters
  const resetQuery = () => {
    Object.assign(queryForm, queryFormState);
    selectedRowKeys.value = [];
    queryAreaList();
  };

  // Pagination change
  const handleTableChange = (pag) => {
    queryForm.pageNum = pag.current;
    queryForm.pageSize = pag.pageSize;
    queryAreaList();
  };

  // Add area
  const handleAdd = () => {
    isEdit.value = false;
    currentArea.value = null;
    formModalVisible.value = true;
  };

  // Edit area
  const handleEdit = (record) => {
    isEdit.value = true;
    currentArea.value = { ...record };
    formModalVisible.value = true;
  };

  // View detail
  const handleView = (record) => {
    currentAreaId.value = record.areaId;
    detailModalVisible.value = true;
  };

  // View devices
  const handleViewDevices = (record) => {
    currentAreaId.value = record.areaId;
    currentAreaName.value = record.areaName;
    deviceModalVisible.value = true;
  };

  // View permissions
  const handleViewPermissions = (record) => {
    currentAreaId.value = record.areaId;
    currentAreaName.value = record.areaName;
    permissionModalVisible.value = true;
  };

  // Device config placeholder
  const handleDeviceConfig = () => {
    message.info('Device configuration is coming soon.');
  };

  // Toggle status
  const handleToggleStatus = (record) => {
    const actionText = record.status === 1 ? 'Disable' : 'Enable';
    Modal.confirm({
      title: `${actionText} area`,
      content: `Confirm to ${actionText.toLowerCase()} "${record.areaName}"?`,
      onOk: async () => {
        try {
          const newStatus = record.status === 1 ? 0 : 1;
          const result = await accessApi.updateAreaStatus(record.areaId, newStatus);
          if (result.code === 200) {
            message.success(`${actionText}d`);
            queryAreaList();
            queryStatistics();
          } else {
            message.error(result.message || `${actionText} failed`);
          }
        } catch (error) {
          smartSentry.captureError(error);
          message.error(`${actionText} failed`);
        }
      },
    });
  };

  // Batch enable
  const handleBatchEnable = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('Please select areas first');
      return;
    }

    Modal.confirm({
      title: 'Enable selected',
      content: `Enable ${selectedRowKeys.value.length} areas?`,
      onOk: async () => {
        try {
          const result = await accessApi.batchUpdateAreaStatus(selectedRowKeys.value, 1);
          if (result.code === 200) {
            message.success('Enabled successfully');
            selectedRowKeys.value = [];
            queryAreaList();
            queryStatistics();
          } else {
            message.error(result.message || 'Enable failed');
          }
        } catch (error) {
          smartSentry.captureError(error);
          message.error('Enable failed');
        }
      },
    });
  };

  // Batch disable
  const handleBatchDisable = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('Please select areas first');
      return;
    }

    Modal.confirm({
      title: 'Disable selected',
      content: `Disable ${selectedRowKeys.value.length} areas?`,
      onOk: async () => {
        try {
          const result = await accessApi.batchUpdateAreaStatus(selectedRowKeys.value, 0);
          if (result.code === 200) {
            message.success('Disabled successfully');
            selectedRowKeys.value = [];
            queryAreaList();
            queryStatistics();
          } else {
            message.error(result.message || 'Disable failed');
          }
        } catch (error) {
          smartSentry.captureError(error);
          message.error('Disable failed');
        }
      },
    });
  };

  // Delete
  const handleDelete = (record) => {
    Modal.confirm({
      title: 'Delete area',
      content: `Confirm to delete "${record.areaName}"?`,
      onOk: async () => {
        try {
          const result = await accessApi.deleteArea(record.areaId);
          if (result.code === 200) {
            message.success('Deleted successfully');
            queryAreaList();
            queryStatistics();
          } else {
            message.error(result.message || 'Delete failed');
          }
        } catch (error) {
          smartSentry.captureError(error);
          message.error('Delete failed');
        }
      },
    });
  };

  // Export
  const handleExport = async () => {
    try {
      const result = await accessApi.exportAreas(queryForm);
      if (result.code === 200) {
        const blob = new Blob([result.data]);
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `area_list_${dayjs().format('YYYY-MM-DD')}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        message.success('Exported');
      } else {
        message.error(result.message || 'Export failed');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('Export failed');
    }
  };

  // Refresh stats
  const handleRefreshStatistics = () => {
    queryStatistics();
  };

  // Form success
  const handleFormSuccess = () => {
    queryAreaList();
    queryStatistics();
  };

  // Area type color
  const getAreaTypeColor = (type) => {
    const colorMap = {
      'BUILDING': 'blue',
      'FLOOR': 'green',
      'ROOM': 'orange',
      'ENTRANCE': 'purple',
      'PARKING': 'cyan',
      'OTHER': 'default',
    };
    return colorMap[type] || 'default';
  };

  // Area type text
  const getAreaTypeText = (type) => {
    const textMap = {
      'BUILDING': 'Building',
      'FLOOR': 'Floor',
      'ROOM': 'Room',
      'ENTRANCE': 'Entrance',
      'PARKING': 'Parking',
      'OTHER': 'Other',
    };
    return textMap[type] || type;
  };

  // Security percent
  const getSecurityPercent = (level) => {
    const percentMap = {
      'LOW': 25,
      'MEDIUM': 50,
      'HIGH': 75,
      'CRITICAL': 100,
    };
    return percentMap[level] || 0;
  };

  // Security progress color
  const getSecurityColor = (level) => {
    const colorMap = {
      'LOW': '#52c41a',
      'MEDIUM': '#fa8c16',
      'HIGH': '#fa541c',
      'CRITICAL': '#f5222d',
    };
    return colorMap[level] || '#d9d9d9';
  };

  // Security tag color
  const getSecurityTagColor = (level) => {
    const colorMap = {
      'LOW': 'green',
      'MEDIUM': 'orange',
      'HIGH': 'red',
      'CRITICAL': 'magenta',
    };
    return colorMap[level] || 'default';
  };

  // Security text
  const getSecurityText = (level) => {
    const textMap = {
      'LOW': 'Low',
      'MEDIUM': 'Medium',
      'HIGH': 'High',
      'CRITICAL': 'Critical',
    };
    return textMap[level] || level;
  };

  // Lifecycle
  onMounted(() => {
    queryAreaList();
    queryStatistics();
  });
</script>

<style lang="less" scoped>
  .access-area-page {
    .statistics-cards {
      .stat-card {
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
          transform: translateY(-2px);
        }

        :deep(.ant-statistic-title) {
          font-size: 14px;
          color: rgba(0, 0, 0, 0.65);
          margin-bottom: 8px;
        }

        :deep(.ant-statistic-content) {
          font-size: 24px;
          font-weight: 600;
        }
      }
    }

    .query-card {
      margin-bottom: 16px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .area-info {
      .area-name {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .area-details {
          .name {
            font-weight: 600;
            color: rgba(0, 0, 0, 0.85);
            font-size: 14px;
          }

          .code {
            font-size: 12px;
            color: rgba(0, 0, 0, 0.45);
            margin-top: 2px;
          }
        }
      }

      .description {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.65);
        padding-left: 40px;
      }
    }
  }
</style>
