<!--
  * Subsidy Management
-->
<template>
  <div class="subsidy-list-page">
    <a-card :bordered="false">
      <template #title>
        <span>Subsidy Management</span>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            Add Subsidy
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            Refresh
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="subsidyId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            <span class="amount-text">${{ record.amount.toFixed(2) }}</span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? 'Active' : 'Inactive'"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                View
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                Edit
              </a-button>
              <a-button type="link" size="small" @click="handleToggleStatus(record)">
                {{ record.status === 1 ? 'Disable' : 'Enable' }}
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                Delete
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    ReloadOutlined,
  } from '@ant-design/icons-vue';

  const tableData = ref([]);
  const loading = ref(false);

  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `Total ${total} items`,
  });

  const columns = [
    {
      title: 'Subsidy Name',
      dataIndex: 'subsidyName',
      key: 'subsidyName',
    },
    {
      title: 'Amount',
      key: 'amount',
      width: 100,
    },
    {
      title: 'Type',
      dataIndex: 'subsidyType',
      key: 'subsidyType',
    },
    {
      title: 'Target Group',
      dataIndex: 'targetGroup',
      key: 'targetGroup',
    },
    {
      title: 'Status',
      key: 'status',
      width: 80,
    },
    {
      title: 'Create Time',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
    },
    {
      title: 'Actions',
      key: 'action',
      width: 200,
    },
  ];

  const loadSubsidyList = async () => {
    loading.value = true;
    try {
      tableData.value = [];
      pagination.total = 0;
    } catch (error) {
      message.error('Failed to load subsidy list');
    } finally {
      loading.value = false;
    }
  };

  const handleTableChange = (pag) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadSubsidyList();
  };

  const handleAdd = () => {
    message.info('Add subsidy feature under development');
  };

  const handleView = (record) => {
    Modal.info({
      title: 'Subsidy Details',
      content: `Subsidy Name: ${record.subsidyName}`,
    });
  };

  const handleEdit = (record) => {
    message.info('Edit subsidy feature under development');
  };

  const handleToggleStatus = (record) => {
    const action = record.status === 1 ? 'disable' : 'enable';
    Modal.confirm({
      title: `Confirm ${action}`,
      content: `Are you sure to ${action} subsidy ${record.subsidyName}?`,
      onOk: async () => {
        try {
          message.success(`${action} successful`);
          loadSubsidyList();
        } catch (error) {
          message.error(`${action} failed`);
        }
      },
    });
  };

  const handleDelete = (record) => {
    Modal.confirm({
      title: 'Confirm Delete',
      content: `Are you sure to delete subsidy ${record.subsidyName}?`,
      onOk: async () => {
        try {
          message.success('Delete successful');
          loadSubsidyList();
        } catch (error) {
          message.error('Delete failed');
        }
      },
    });
  };

  const handleRefresh = () => {
    loadSubsidyList();
  };

  onMounted(() => {
    loadSubsidyList();
  });
</script>

<style lang="less" scoped>
  .subsidy-list-page {
    .amount-text {
      font-weight: 600;
      color: #52c41a;
    }
  }
</style>