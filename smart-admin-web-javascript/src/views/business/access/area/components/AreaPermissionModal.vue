<!--
  * Area Permission Modal Component
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    :title="`Permission Management - ${areaName}`"
    width="900px"
    :footer="null"
  >
    <div class="area-permission-content">
      <!-- 权限类型选择 -->
      <div class="permission-types" style="margin-bottom: 16px;">
        <a-radio-group v-model:value="permissionType" @change="handleTypeChange">
          <a-radio-button value="user">User Permissions</a-radio-button>
          <a-radio-button value="role">Role Permissions</a-radio-button>
          <a-radio-button value="time">Time Permissions</a-radio-button>
        </a-radio-group>
      </div>

      <!-- 操作工具栏 -->
      <div class="toolbar" style="margin-bottom: 16px;">
        <a-space>
          <a-button type="primary" @click="handleAddPermission">
            <template #icon><PlusOutlined /></template>
            Add Permission
          </a-button>
          <a-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length">
            <template #icon><DeleteOutlined /></template>
            Batch Delete
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            Refresh
          </a-button>
        </a-space>
      </div>

      <!-- 权限列表 -->
      <a-table
        :columns="columns"
        :data-source="permissionList"
        :pagination="{ pageSize: 10, showSizeChanger: true }"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="permissionId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'targetInfo'">
            <div class="target-info">
              <a-avatar :size="32" style="margin-right: 8px;">
                {{ permissionType === 'user' ? record.userName?.charAt(0) : record.roleName?.charAt(0) }}
              </a-avatar>
              <div>
                <div class="name">{{ permissionType === 'user' ? record.userName : record.roleName }}</div>
                <div class="code">{{ permissionType === 'user' ? record.userCode : record.roleCode }}</div>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'permissionLevel'">
            <a-tag :color="getPermissionLevelColor(record.permissionLevel)">
              {{ getPermissionLevelText(record.permissionLevel) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? 'Active' : 'Inactive'"
            />
          </template>

          <template v-else-if="column.key === 'validTime'">
            <span v-if="record.startTime && record.endTime">
              {{ record.startTime }} ~ {{ record.endTime }}
            </span>
            <span v-else>Permanent</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEditPermission(record)">
                Edit
              </a-button>
              <a-button type="link" size="small" danger @click="handleDeletePermission(record)">
                Delete
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { PlusOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons-vue';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    areaId: {
      type: [String, Number],
      default: null,
    },
    areaName: {
      type: String,
      default: '',
    },
  });

  const emit = defineEmits(['update:visible']);

  const loading = ref(false);
  const permissionType = ref('user');
  const selectedRowKeys = ref([]);
  const permissionList = ref([]);

  const rowSelection = {
    selectedRowKeys: selectedRowKeys,
    onChange: (keys) => {
      selectedRowKeys.value = keys;
    },
  };

  // 根据权限类型显示不同的列
  const columns = computed(() => {
    const baseColumns = [
      {
        title: permissionType.value === 'user' ? 'User' : 'Role',
        key: 'targetInfo',
        width: 200,
      },
      {
        title: 'Permission Level',
        key: 'permissionLevel',
        width: 120,
      },
      {
        title: 'Status',
        key: 'status',
        width: 80,
      },
      {
        title: 'Valid Time',
        key: 'validTime',
        width: 200,
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
        width: 150,
      },
    ];

    return baseColumns;
  });

  // 模拟权限数据
  const mockPermissions = {
    user: [
      {
        permissionId: '1',
        userName: 'John Doe',
        userCode: 'EMP001',
        permissionLevel: 'FULL_ACCESS',
        status: 1,
        startTime: '2025-01-01',
        endTime: '2025-12-31',
        createTime: '2025-01-01 09:00:00',
      },
      {
        permissionId: '2',
        userName: 'Jane Smith',
        userCode: 'EMP002',
        permissionLevel: 'READ_ONLY',
        status: 1,
        startTime: null,
        endTime: null,
        createTime: '2025-01-02 10:00:00',
      },
    ],
    role: [
      {
        permissionId: '3',
        roleName: 'Security Team',
        roleCode: 'SECURITY',
        permissionLevel: 'FULL_ACCESS',
        status: 1,
        startTime: null,
        endTime: null,
        createTime: '2025-01-01 09:00:00',
      },
    ],
    time: [
      {
        permissionId: '4',
        userName: 'All Users',
        userCode: 'ALL',
        permissionLevel: 'TIME_BASED',
        status: 1,
        startTime: '09:00',
        endTime: '18:00',
        createTime: '2025-01-01 09:00:00',
      },
    ],
  };

  const loadPermissionList = async () => {
    loading.value = true;
    try {
      // TODO: 调用API获取权限列表
      // const result = await permissionApi.getAreaPermissions(props.areaId, permissionType.value);

      // 使用模拟数据
      await new Promise(resolve => setTimeout(resolve, 500));
      permissionList.value = mockPermissions[permissionType.value] || [];
    } catch (error) {
      message.error('Failed to load permission list');
    } finally {
      loading.value = false;
    }
  };

  const getPermissionLevelColor = (level) => {
    const colorMap = {
      FULL_ACCESS: 'green',
      READ_ONLY: 'blue',
      TIME_BASED: 'orange',
      EMERGENCY: 'red',
    };
    return colorMap[level] || 'default';
  };

  const getPermissionLevelText = (level) => {
    const textMap = {
      FULL_ACCESS: 'Full Access',
      READ_ONLY: 'Read Only',
      TIME_BASED: 'Time Based',
      EMERGENCY: 'Emergency',
    };
    return textMap[level] || level;
  };

  const handleTypeChange = () => {
    selectedRowKeys.value = [];
    loadPermissionList();
  };

  const handleAddPermission = () => {
    message.info(`${permissionType.value} permission feature under development`);
  };

  const handleEditPermission = (record) => {
    Modal.info({
      title: 'Edit Permission',
      content: `Edit permission for ${permissionType.value} is under development`,
    });
  };

  const handleDeletePermission = (record) => {
    Modal.confirm({
      title: 'Confirm Delete',
      content: `Are you sure to delete this ${permissionType.value} permission?`,
      onOk: async () => {
        try {
          // TODO: 调用API删除权限
          message.success('Permission deleted successfully');
          loadPermissionList();
        } catch (error) {
          message.error('Failed to delete permission');
        }
      },
    });
  };

  const handleBatchDelete = () => {
    Modal.confirm({
      title: 'Confirm Batch Delete',
      content: `Are you sure to delete ${selectedRowKeys.value.length} selected permissions?`,
      onOk: async () => {
        try {
          // TODO: 调用API批量删除
          message.success('Permissions deleted successfully');
          selectedRowKeys.value = [];
          loadPermissionList();
        } catch (error) {
          message.error('Failed to delete permissions');
        }
      },
    });
  };

  const handleRefresh = () => {
    loadPermissionList();
  };

  watch(() => props.visible, (visible) => {
    if (visible && props.areaId) {
      loadPermissionList();
    }
  });
</script>

<style lang="less" scoped>
  .area-permission-content {
    .target-info {
      display: flex;
      align-items: center;

      .name {
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
      }

      .code {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }
</style>