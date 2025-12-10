<!--
  * Permission Management
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
-->
<template>
  <div class="access-permission-page">
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row" :gutter="[16, 16]">
          <a-form-item label="Keyword" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.keyword"
              placeholder="User / area / code"
              style="width: 220px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="Status" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              placeholder="Select status"
              style="width: 120px"
              :allow-clear="true"
            >
              <a-select-option :value="1">Enabled</a-select-option>
              <a-select-option :value="2">Pending</a-select-option>
              <a-select-option :value="0">Disabled</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="Date Range" class="smart-query-form-item">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              style="width: 240px"
              :placeholder="['Start date', 'End date']"
            />
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryPermissionList">
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

    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>Permission List</span>
          <a-tag v-if="selectedRowKeys.length > 0" color="blue">
            Selected {{ selectedRowKeys.length }}
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="openBatchGrant">
            <template #icon><SafetyOutlined /></template>
            Batch Grant
          </a-button>
          <a-button @click="handleBatchRevoke" :disabled="selectedRowKeys.length === 0" danger>
            <template #icon><StopOutlined /></template>
            Batch Revoke
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="permissionId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'">
            <div class="user-info">
              <div class="user-name">{{ record.userName }}</div>
              <div class="user-code">{{ record.userCode }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'area'">
            <div class="area-info">
              <div>{{ record.areaName }}</div>
              <div class="area-code">{{ record.areaCode }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'permissionType'">
            <a-tag :color="getPermissionTypeColor(record.permissionType)">
              {{ getPermissionTypeText(record.permissionType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="getStatusBadge(record.status)"
              :text="getStatusText(record.status)"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openDetail(record)">View</a-button>
              <a-button type="link" size="small" @click="handleToggle(record)">
                {{ record.status === 1 ? 'Disable' : 'Enable' }}
              </a-button>
              <a-button type="link" size="small" @click="handleDelete(record)" danger>Delete</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <BatchGrantModal
      v-model:visible="batchGrantVisible"
      :selected-keys="selectedRowKeys"
      @success="handleGrantSuccess"
    />

    <PermissionDetailModal
      v-model:visible="detailVisible"
      :detail="currentPermission"
    />
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import dayjs from 'dayjs';
  import {
    SearchOutlined,
    ReloadOutlined,
    SafetyOutlined,
    StopOutlined,
  } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import BatchGrantModal from './components/BatchGrantModal.vue';
  import PermissionDetailModal from './components/PermissionDetailModal.vue';

  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    keyword: null,
    status: null,
    startDate: null,
    endDate: null,
  };

  const queryForm = reactive({ ...queryFormState });
  const dateRange = ref([]);
  const tableData = ref([]);
  const loading = ref(false);
  const selectedRowKeys = ref([]);
  const batchGrantVisible = ref(false);
  const detailVisible = ref(false);
  const currentPermission = ref(null);

  const pagination = reactive({
    current: 1,
    pageSize: PAGE_SIZE,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: PAGE_SIZE_OPTIONS,
    showTotal: (total) => `Total ${total} items`,
  });

  const columns = [
    { title: 'User', key: 'user', width: 200 },
    { title: 'Area', key: 'area', width: 180 },
    { title: 'Permission Type', key: 'permissionType', width: 140 },
    { title: 'Valid From', dataIndex: 'startTime', key: 'startTime', width: 160 },
    { title: 'Valid To', dataIndex: 'endTime', key: 'endTime', width: 160 },
    { title: 'Status', key: 'status', width: 100 },
    { title: 'Updated At', dataIndex: 'updateTime', key: 'updateTime', width: 160 },
    { title: 'Action', key: 'action', width: 180, fixed: 'right' },
  ];

  const rowSelection = {
    selectedRowKeys: selectedRowKeys,
    onChange: (keys) => {
      selectedRowKeys.value = keys;
    },
  };

  const mockData = () => {
    const now = dayjs();
    return Array.from({ length: 18 }).map((_, idx) => ({
      permissionId: idx + 1,
      userName: `User ${idx + 1}`,
      userCode: `EMP-${1100 + idx}`,
      areaName: `Area ${idx % 5 + 1}`,
      areaCode: `AR-${300 + idx}`,
      permissionType: ['FULL', 'TIME_LIMIT', 'CUSTOM'][idx % 3],
      startTime: now.subtract(3, 'day').format('YYYY-MM-DD'),
      endTime: now.add(7 + idx, 'day').format('YYYY-MM-DD'),
      status: idx % 3 === 0 ? 0 : 1,
      updateTime: now.subtract(idx, 'hour').format('YYYY-MM-DD HH:mm'),
    }));
  };

  const queryPermissionList = async () => {
    loading.value = true;
    try {
      // Replace with real API when available
      tableData.value = mockData();
      pagination.total = tableData.value.length;
      pagination.current = queryForm.pageNum;
    } catch (error) {
      smartSentry.captureError(error);
      message.error('Failed to load permissions');
    } finally {
      loading.value = false;
    }
  };

  const resetQuery = () => {
    Object.assign(queryForm, queryFormState);
    dateRange.value = [];
    selectedRowKeys.value = [];
    queryPermissionList();
  };

  const handleTableChange = (pag) => {
    queryForm.pageNum = pag.current;
    queryForm.pageSize = pag.pageSize;
    queryPermissionList();
  };

  const getPermissionTypeText = (type) => {
    const map = { FULL: 'Full Access', TIME_LIMIT: 'Time Limit', CUSTOM: 'Custom' };
    return map[type] || type;
  };

  const getPermissionTypeColor = (type) => {
    const map = { FULL: 'green', TIME_LIMIT: 'orange', CUSTOM: 'blue' };
    return map[type] || 'default';
  };

  const getStatusBadge = (status) => {
    if (status === 1) return 'success';
    if (status === 2) return 'processing';
    return 'default';
  };

  const getStatusText = (status) => {
    if (status === 1) return 'Enabled';
    if (status === 2) return 'Pending';
    return 'Disabled';
  };

  const openBatchGrant = () => {
    batchGrantVisible.value = true;
  };

  const handleGrantSuccess = () => {
    message.success('Permissions granted');
    queryPermissionList();
  };

  const handleBatchRevoke = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('Please select at least one record');
      return;
    }

    Modal.confirm({
      title: 'Revoke permissions',
      content: `Revoke ${selectedRowKeys.value.length} permissions?`,
      onOk: () => {
        message.success('Revoked');
        selectedRowKeys.value = [];
        queryPermissionList();
      },
    });
  };

  const handleToggle = (record) => {
    const action = record.status === 1 ? 'Disable' : 'Enable';
    Modal.confirm({
      title: `${action} permission`,
      content: `Confirm to ${action.toLowerCase()} ${record.userName}?`,
      onOk: () => {
        record.status = record.status === 1 ? 0 : 1;
        message.success(`${action}d`);
      },
    });
  };

  const handleDelete = (record) => {
    Modal.confirm({
      title: 'Delete permission',
      content: `Confirm to delete permission of ${record.userName}?`,
      onOk: () => {
        tableData.value = tableData.value.filter((item) => item.permissionId !== record.permissionId);
        message.success('Deleted');
      },
    });
  };

  const openDetail = (record) => {
    currentPermission.value = { ...record, phoneNumber: '18800002222', departmentName: 'Access Control' };
    detailVisible.value = true;
  };

  onMounted(() => {
    queryPermissionList();
  });
</script>

<style lang="less" scoped>
  .query-card {
    margin-bottom: 16px;
  }

  .user-info {
    display: flex;
    flex-direction: column;
    line-height: 1.2;
  }

  .area-info {
    display: flex;
    flex-direction: column;
    line-height: 1.2;

    .area-code {
      color: rgba(0, 0, 0, 0.45);
      font-size: 12px;
    }
  }
</style>
