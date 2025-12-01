<template>
  <div class="permission-matrix">
    <div class="matrix-header" v-if="showHeader">
      <div class="header-left">
        <div class="matrix-title">
          <slot name="title">
            <span>{{ title }}</span>
          </slot>
        </div>
        <div class="matrix-stats" v-if="showStats">
          <a-tag color="blue">
            {{ users.length }} 用户
          </a-tag>
          <a-tag color="green">
            {{ permissions.length }} 权限
          </a-tag>
          <a-tag color="orange" v-if="assignedCount > 0">
            {{ assignedCount }} 已分配
          </a-tag>
        </div>
      </div>
      <div class="header-right">
        <slot name="actions">
          <a-space>
            <a-button
              v-if="showBatchAssign"
              type="primary"
              size="small"
              @click="handleBatchAssign"
              :disabled="!hasSelection"
            >
              <AppstoreAddOutlined />
              批量分配
            </a-button>
            <a-button
              v-if="showBatchRevoke"
              danger
              size="small"
              @click="handleBatchRevoke"
              :disabled="!hasSelection"
            >
              <DeleteOutlined />
              批量撤销
            </a-button>
            <a-button
              v-if="showExport"
              size="small"
              @click="handleExport"
            >
              <ExportOutlined />
              导出
            </a-button>
            <a-button
              v-if="showRefresh"
              size="small"
              @click="handleRefresh"
              :loading="loading"
            >
              <ReloadOutlined />
              刷新
            </a-button>
          </a-space>
        </slot>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="matrix-toolbar" v-if="showToolbar">
      <a-row :gutter="12">
        <a-col :span="8">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索用户或权限"
            allowClear
            @search="handleSearch"
          />
        </a-col>
        <a-col :span="6">
          <a-select
            v-model:value="filterType"
            placeholder="筛选权限类型"
            allowClear
            style="width: 100%"
            @change="handleFilterTypeChange"
          >
            <a-select-option
              v-for="type in permissionTypeOptions"
              :key="type.value"
              :value="type.value"
            >
              {{ type.label }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-select
            v-model:value="filterSecurityLevel"
            placeholder="筛选安全级别"
            allowClear
            style="width: 100%"
            @change="handleFilterSecurityLevelChange"
          >
            <a-select-option
              v-for="level in securityLevelOptions"
              :key="level.value"
              :value="level.value"
            >
              <a-tag :color="level.color" size="small">{{ level.label }}</a-tag>
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-button-group>
            <a-button
              :type="viewMode === 'table' ? 'primary' : 'default'"
              size="small"
              @click="setViewMode('table')"
            >
              <TableOutlined />
            </a-button>
            <a-button
              :type="viewMode === 'card' ? 'primary' : 'default'"
              size="small"
              @click="setViewMode('card')"
            >
              <AppstoreOutlined />
            </a-button>
          </a-button-group>
        </a-col>
      </a-row>
    </div>

    <!-- 矩阵内容 -->
    <div class="matrix-content" :style="{ height: contentHeight }">
      <a-spin :spinning="loading">
        <!-- 表格视图 -->
        <div v-if="viewMode === 'table'" class="matrix-table">
          <a-table
            :columns="tableColumns"
            :data-source="tableData"
            :pagination="false"
            :scroll="{ x: tableWidth, y: tableHeight }"
            size="small"
            bordered
            :row-selection="rowSelection"
            @change="handleTableChange"
          >
            <!-- 用户信息列 -->
            <template #bodyCell="{ column, record }">
              <!-- 用户信息 -->
              <template v-if="column.key === 'userInfo'">
                <div class="user-info-cell">
                  <a-avatar :src="record.avatar" :size="32">
                    {{ record.userName?.charAt(0) }}
                  </a-avatar>
                  <div class="user-details">
                    <div class="user-name">{{ record.userName }}</div>
                    <div class="user-meta">
                      <span class="user-code">{{ record.userCode }}</span>
                      <span class="department">{{ record.departmentName }}</span>
                    </div>
                  </div>
                </div>
              </template>

              <!-- 权限列 -->
              <template v-else-if="column.permission">
                <PermissionCell
                  :user="record"
                  :permission="column.permission"
                  :editable="editable"
                  @assign="handlePermissionAssign"
                  @revoke="handlePermissionRevoke"
                  @view="handlePermissionView"
                />
              </template>
            </template>

            <!-- 用户信息列标题 -->
            <template #title="{ column }">
              <template v-if="column.key === 'userInfo'">
                <div class="user-info-header">
                  <span>用户信息</span>
                  <a-checkbox
                    v-model:value="allUsersSelected"
                    @change="handleSelectAllUsers"
                    indeterminate="usersSelectIndeterminate"
                  />
                </div>
              </template>
              <template v-else-if="column.permission">
                <div class="permission-header">
                  <a-tooltip :title="column.permission.name">
                    <span class="permission-title">{{ column.permission.name }}</span>
                  </a-tooltip>
                  <a-checkbox
                    v-model:value="column.selected"
                    @change="handleSelectPermission(column.permission)"
                  />
                  <div class="permission-meta">
                    <a-tag
                      :color="getPermissionTypeColor(column.permission.type)"
                      size="small"
                    >
                      {{ getPermissionTypeText(column.permission.type) }}
                    </a-tag>
                    <a-tag
                      :color="getSecurityLevelColor(column.permission.securityLevel)"
                      size="small"
                    >
                      {{ getSecurityLevelText(column.permission.securityLevel) }}
                    </a-tag>
                  </div>
                </div>
              </template>
            </template>
          </a-table>
        </div>

        <!-- 卡片视图 -->
        <div v-else class="matrix-cards">
          <div class="cards-container">
            <div
              v-for="user in filteredUsers"
              :key="user.userId"
              class="user-card"
              :class="{ selected: selectedUsers.includes(user.userId) }"
            >
              <div class="card-header">
                <a-checkbox
                  :checked="selectedUsers.includes(user.userId)"
                  @change="handleUserCardSelect(user.userId)"
                />
                <div class="user-info">
                  <a-avatar :src="user.avatar" :size="40">
                    {{ user.userName?.charAt(0) }}
                  </a-avatar>
                  <div class="user-details">
                    <div class="user-name">{{ user.userName }}</div>
                    <div class="user-meta">
                      <span>{{ record.userCode }}</span>
                      <span>{{ user.departmentName }}</span>
                    </div>
                  </div>
                </div>
                <div class="card-actions">
                  <a-dropdown>
                    <a-button type="link" size="small">
                      <MoreOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu @click="handleUserAction($event, user)">
                        <a-menu-item key="view">查看详情</a-menu-item>
                        <a-menu-item key="assign">批量分配</a-menu-item>
                        <a-menu-item key="revoke">批量撤销</a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </div>
              </div>
              <div class="card-content">
                <div class="permission-grid">
                  <div
                    v-for="permission in filteredPermissions"
                    :key="permission.id"
                    class="permission-item"
                  >
                    <PermissionCell
                      :user="user"
                      :permission="permission"
                      :editable="editable"
                      compact
                      @assign="handlePermissionAssign"
                      @revoke="handlePermissionRevoke"
                      @view="handlePermissionView"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-spin>

      <!-- 空状态 -->
      <a-empty
        v-if="!loading && (filteredUsers.length === 0 || filteredPermissions.length === 0)"
        :description="emptyDescription"
      >
        <template #image>
          <TableOutlined />
        </template>
      </a-empty>
    </div>

    <!-- 批量分配弹窗 -->
    <BatchAssignModal
      v-model:open="batchAssignVisible"
      :users="selectedUsersData"
      :permissions="selectedPermissionsData"
      @confirm="handleBatchAssignConfirm"
    />

    <!-- 权限详情弹窗 -->
    <PermissionDetailModal
      v-model:open="permissionDetailVisible"
      :user="currentUser"
      :permission="currentPermission"
      @assign="handlePermissionAssign"
      @revoke="handlePermissionRevoke"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  TableOutlined,
  AppstoreOutlined,
  AppstoreAddOutlined,
  DeleteOutlined,
  ExportOutlined,
  ReloadOutlined,
  MoreOutlined
} from '@ant-design/icons-vue';
import { SecurityLevel, PermissionType } from '/@/types/permission';
import PermissionCell from './PermissionCell.vue';
import BatchAssignModal from './BatchAssignModal.vue';
import PermissionDetailModal from './PermissionDetailModal.vue';

// Props定义
const props = defineProps({
  // 数据
  users: {
    type: Array,
    default: () => []
  },
  permissions: {
    type: Array,
    default: () => []
  },
  userPermissions: {
    type: Array,
    default: () => []
  },

  // 状态控制
  loading: {
    type: Boolean,
    default: false
  },
  editable: {
    type: Boolean,
    default: true
  },

  // 显示控制
  showHeader: {
    type: Boolean,
    default: true
  },
  showStats: {
    type: Boolean,
    default: true
  },
  showToolbar: {
    type: Boolean,
    default: true
  },
  showBatchAssign: {
    type: Boolean,
    default: true
  },
  showBatchRevoke: {
    type: Boolean,
    default: true
  },
  showExport: {
    type: Boolean,
    default: true
  },
  showRefresh: {
    type: Boolean,
    default: true
  },

  // 样式控制
  height: {
    type: [String, Number],
    default: '500px'
  },
  title: {
    type: String,
    default: '权限矩阵'
  },
  emptyDescription: {
    type: String,
    default: '暂无数据'
  }
});

// Emits定义
const emit = defineEmits([
  'assign',
  'revoke',
  'batchAssign',
  'batchRevoke',
  'export',
  'refresh',
  'search',
  'filter',
  'selectUsers',
  'selectPermissions',
  'viewUser',
  'viewPermission'
]);

// 响应式数据
const viewMode = ref('table');
const searchKeyword = ref('');
const filterType = ref(null);
const filterSecurityLevel = ref(null);
const selectedUsers = ref([]);
const selectedPermissions = ref([]);
const allUsersSelected = ref(false);
const allPermissionsSelected = ref(false);
const usersSelectIndeterminate = ref(false);
const permissionsSelectIndeterminate = ref(false);

// 弹窗状态
const batchAssignVisible = ref(false);
const permissionDetailVisible = ref(false);
const currentUser = ref(null);
const currentPermission = ref(null);

// 计算属性
const contentHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`;
  }
  return props.height;
});

const filteredUsers = computed(() => {
  let users = [...props.users];

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    users = users.filter(user =>
      user.userName?.toLowerCase().includes(keyword) ||
      user.userCode?.toLowerCase().includes(keyword) ||
      user.departmentName?.toLowerCase().includes(keyword)
    );
  }

  return users;
});

const filteredPermissions = computed(() => {
  let permissions = [...props.permissions];

  // 类型过滤
  if (filterType.value) {
    permissions = permissions.filter(p => p.type === filterType.value);
  }

  // 安全级别过滤
  if (filterSecurityLevel.value) {
    permissions = permissions.filter(p => p.securityLevel === filterSecurityLevel.value);
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    permissions = permissions.filter(p =>
      p.name?.toLowerCase().includes(keyword) ||
      p.code?.toLowerCase().includes(keyword) ||
      p.description?.toLowerCase().includes(keyword)
    );
  }

  return permissions;
});

const tableData = computed(() => {
  return filteredUsers.value.map(user => ({
    ...user,
    key: user.userId
  }));
});

const tableColumns = computed(() => {
  const columns = [
    {
      key: 'userInfo',
      title: '用户信息',
      width: 200,
      fixed: 'left',
      align: 'left'
    }
  ];

  // 添加权限列
  filteredPermissions.value.forEach(permission => {
    columns.push({
      key: `permission_${permission.id}`,
      title: permission.name,
      width: 120,
      align: 'center',
      permission: permission
    });
  });

  return columns;
});

const tableWidth = computed(() => {
  return 200 + filteredPermissions.value.length * 120;
});

const tableHeight = computed(() => {
  const headerHeight = 100;
  const toolbarHeight = props.showToolbar ? 50 : 0;
  const contentHeightNum = typeof props.height === 'number' ? props.height : parseInt(props.height);
  return contentHeightNum - headerHeight - toolbarHeight - 100;
});

const selectedUsersData = computed(() => {
  return filteredUsers.value.filter(user => selectedUsers.value.includes(user.userId));
});

const selectedPermissionsData = computed(() => {
  return filteredPermissions.value.filter(permission => selectedPermissions.value.includes(permission.id));
});

const hasSelection = computed(() => {
  return selectedUsers.value.length > 0 && selectedPermissions.value.length > 0;
});

const assignedCount = computed(() => {
  return props.userPermissions.filter(up => up.status === 'ACTIVE').length;
});

const permissionTypeOptions = computed(() => {
  return [
    { value: PermissionType.MENU, label: '菜单权限' },
    { value: PermissionType.BUTTON, label: '按钮权限' },
    { value: PermissionType.API, label: '接口权限' },
    { value: PermissionType.DATA, label: '数据权限' },
    { value: PermissionType.DEVICE, label: '设备权限' },
    { value: PermissionType.AREA, label: '区域权限' },
    { value: PermissionType.TIME, label: '时间权限' }
  ];
});

const securityLevelOptions = computed(() => {
  return [
    {
      value: SecurityLevel.PUBLIC,
      label: '公开级',
      color: 'green'
    },
    {
      value: SecurityLevel.INTERNAL,
      label: '内部级',
      color: 'blue'
    },
    {
      value: SecurityLevel.CONFIDENTIAL,
      label: '秘密级',
      color: 'orange'
    },
    {
      value: SecurityLevel.SECRET,
      label: '机密级',
      color: 'red'
    },
    {
      value: SecurityLevel.TOP_SECRET,
      label: '绝密级',
      color: 'purple'
    }
  ];
});

const rowSelection = computed(() => ({
  selectedRowKeys: selectedUsers.value,
  onChange: handleUserSelectionChange,
  getCheckboxProps: (record) => ({
    disabled: record.disabled
  })
}));

// 监听器
watch(filteredUsers, (newUsers) => {
  updateSelectAllStatus();
}, { deep: true });

watch(selectedUsers, () => {
  updateSelectAllStatus();
});

// 生命周期
onMounted(() => {
  updateSelectAllStatus();
});

// 方法
/**
 * 设置视图模式
 */
const setViewMode = (mode) => {
  viewMode.value = mode;
};

/**
 * 处理用户选择变化
 */
const handleUserSelectionChange = (selectedRowKeys) => {
  selectedUsers.value = selectedRowKeys;
  emit('selectUsers', selectedRowKeys);
};

/**
 * 处理权限选择
 */
const handleSelectPermission = (permission) => {
  const permissionId = permission.id;
  if (selectedPermissions.value.includes(permissionId)) {
    selectedPermissions.value = selectedPermissions.value.filter(id => id !== permissionId);
  } else {
    selectedPermissions.value.push(permissionId);
  }
  emit('selectPermissions', selectedPermissions.value);
};

/**
 * 处理全选用户
 */
const handleSelectAllUsers = (e) => {
  const checked = e.target.checked;
  if (checked) {
    selectedUsers.value = filteredUsers.value.map(user => user.userId);
  } else {
    selectedUsers.value = [];
  }
  emit('selectUsers', selectedUsers.value);
};

/**
 * 处理全选权限
 */
const handleSelectAllPermissions = (checked) => {
  if (checked) {
    selectedPermissions.value = filteredPermissions.value.map(permission => permission.id);
  } else {
    selectedPermissions.value = [];
  }
  emit('selectPermissions', selectedPermissions.value);
};

/**
 * 更新全选状态
 */
const updateSelectAllStatus = () => {
  const userCount = filteredUsers.value.length;
  const selectedUserCount = selectedUsers.value.length;
  allUsersSelected.value = userCount > 0 && selectedUserCount === userCount;
  usersSelectIndeterminate.value = selectedUserCount > 0 && selectedUserCount < userCount;

  const permissionCount = filteredPermissions.value.length;
  const selectedPermissionCount = selectedPermissions.value.length;
  allPermissionsSelected.value = permissionCount > 0 && selectedPermissionCount === permissionCount;
  permissionsSelectIndeterminate.value = selectedPermissionCount > 0 && selectedPermissionCount < permissionCount;
};

/**
 * 处理搜索
 */
const handleSearch = (value) => {
  emit('search', value);
};

/**
 * 处理权限类型过滤
 */
const handleFilterTypeChange = (value) => {
  emit('filter', { type: 'permissionType', value });
};

/**
 * 处理安全级别过滤
 */
const handleFilterSecurityLevelChange = (value) => {
  emit('filter', { type: 'securityLevel', value });
};

/**
 * 处理表格变化
 */
const handleTableChange = (pagination, filters, sorter) => {
  // 处理表格变化事件
};

/**
 * 处理权限分配
 */
const handlePermissionAssign = (user, permission) => {
  emit('assign', { user, permission });
};

/**
 * 处理权限撤销
 */
const handlePermissionRevoke = (user, permission) => {
  emit('revoke', { user, permission });
};

/**
 * 处理权限查看
 */
const handlePermissionView = (user, permission) => {
  currentUser.value = user;
  currentPermission.value = permission;
  permissionDetailVisible.value = true;
};

/**
 * 处理批量分配
 */
const handleBatchAssign = () => {
  if (!hasSelection.value) {
    message.warning('请选择用户和权限');
    return;
  }
  batchAssignVisible.value = true;
};

/**
 * 处理批量分配确认
 */
const handleBatchAssignConfirm = (data) => {
  emit('batchAssign', data);
  batchAssignVisible.value = false;
};

/**
 * 处理批量撤销
 */
const handleBatchRevoke = () => {
  if (!hasSelection.value) {
    message.warning('请选择要撤销权限的用户');
    return;
  }

  const data = {
    users: selectedUsersData.value,
    permissions: selectedPermissionsData.value
  };
  emit('batchRevoke', data);
};

/**
 * 处理导出
 */
const handleExport = () => {
  emit('export', {
    users: filteredUsers.value,
    permissions: filteredPermissions.value,
    format: 'excel'
  });
};

/**
 * 处理刷新
 */
const handleRefresh = () => {
  emit('refresh');
};

/**
 * 处理用户卡片选择
 */
const handleUserCardSelect = (userId) => {
  const index = selectedUsers.value.indexOf(userId);
  if (index > -1) {
    selectedUsers.value.splice(index, 1);
  } else {
    selectedUsers.value.push(userId);
  }
  emit('selectUsers', selectedUsers.value);
};

/**
 * 处理用户操作
 */
const handleUserAction = ({ key }, user) => {
  switch (key) {
    case 'view':
      emit('viewUser', user);
      break;
    case 'assign':
      currentUser.value = user;
      // 打开批量分配弹窗，预选当前用户
      batchAssignVisible.value = true;
      break;
    case 'revoke':
      currentUser.value = user;
      // 打开批量撤销弹窗，预选当前用户
      handleBatchRevoke();
      break;
  }
};

/**
 * 获取权限类型颜色
 */
const getPermissionTypeColor = (type) => {
  const colorMap = {
    [PermissionType.MENU]: 'blue',
    [PermissionType.BUTTON]: 'green',
    [PermissionType.API]: 'orange',
    [PermissionType.DATA]: 'purple',
    [PermissionType.DEVICE]: 'cyan',
    [PermissionType.AREA]: 'pink',
    [PermissionType.TIME]: 'geekblue'
  };
  return colorMap[type] || 'default';
};

/**
 * 获取权限类型文本
 */
const getPermissionTypeText = (type) => {
  const textMap = {
    [PermissionType.MENU]: '菜单',
    [PermissionType.BUTTON]: '按钮',
    [PermissionType.API]: '接口',
    [PermissionType.DATA]: '数据',
    [PermissionType.DEVICE]: '设备',
    [PermissionType.AREA]: '区域',
    [PermissionType.TIME]: '时间'
  };
  return textMap[type] || '未知';
};

/**
 * 获取安全级别颜色
 */
const getSecurityLevelColor = (level) => {
  const colorMap = {
    [SecurityLevel.PUBLIC]: 'green',
    [SecurityLevel.INTERNAL]: 'blue',
    [SecurityLevel.CONFIDENTIAL]: 'orange',
    [SecurityLevel.SECRET]: 'red',
    [SecurityLevel.TOP_SECRET]: 'purple'
  };
  return colorMap[level] || 'default';
};

/**
 * 获取安全级别文本
 */
const getSecurityLevelText = (level) => {
  const textMap = {
    [SecurityLevel.PUBLIC]: '公开',
    [SecurityLevel.INTERNAL]: '内部',
    [SecurityLevel.CONFIDENTIAL]: '秘密',
    [SecurityLevel.SECRET]: '机密',
    [SecurityLevel.TOP_SECRET]: '绝密'
  };
  return textMap[level] || '未知';
};

/**
 * 清空选择
 */
const clearSelection = () => {
  selectedUsers.value = [];
  selectedPermissions.value = [];
};

/**
 * 全选用户
 */
const selectAllUsers = () => {
  selectedUsers.value = filteredUsers.value.map(user => user.userId);
};

/**
 * 全选权限
 */
const selectAllPermissions = () => {
  selectedPermissions.value = filteredPermissions.value.map(permission => permission.id);
};

// 暴露方法给父组件
defineExpose({
  setViewMode,
  clearSelection,
  selectAllUsers,
  selectAllPermissions,
  handleBatchAssign,
  handleBatchRevoke,
  handleExport
});
</script>

<style scoped lang="less">
.permission-matrix {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .matrix-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    margin-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .matrix-title {
        font-size: 16px;
        font-weight: 500;
        color: #262626;
      }

      .matrix-stats {
        display: flex;
        gap: 8px;
      }
    }
  }

  .matrix-toolbar {
    margin-bottom: 12px;
    padding: 8px;
    background-color: #fafafa;
    border-radius: 6px;
  }

  .matrix-content {
    flex: 1;
    overflow: hidden;
  }

  .matrix-table {
    height: 100%;

    :deep(.ant-table) {
      height: 100%;

      .ant-table-container {
        height: 100%;
        display: flex;
        flex-direction: column;
      }

      .ant-table-header {
        flex-shrink: 0;
      }

      .ant-table-body {
        flex: 1;
        overflow-y: auto;
      }
    }

    :deep(.user-info-header) {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }

    :deep(.permission-header) {
      text-align: center;
      padding: 8px 4px;

      .permission-title {
        display: block;
        font-weight: 500;
        margin-bottom: 4px;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .permission-meta {
        display: flex;
        flex-direction: column;
        gap: 2px;
        margin-top: 4px;
      }
    }

    :deep(.user-info-cell) {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-details {
        flex: 1;
        min-width: 0;

        .user-name {
          font-weight: 500;
          margin-bottom: 4px;
        }

        .user-meta {
          display: flex;
          gap: 8px;
          font-size: 12px;
          color: #8c8c8c;

          .user-code {
            color: #595959;
          }
        }
      }
    }
  }

  .matrix-cards {
    height: 100%;
    overflow-y: auto;
    padding: 8px;

    .cards-container {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 16px;
    }

    .user-card {
      border: 1px solid #f0f0f0;
      border-radius: 8px;
      background-color: #fff;
      overflow: hidden;
      transition: all 0.2s;

      &:hover {
        border-color: #d9d9d9;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      &.selected {
        border-color: #1890ff;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
      }

      .card-header {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        background-color: #fafafa;
        border-bottom: 1px solid #f0f0f0;

        .user-info {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 12px;

          .user-details {
            .user-name {
              font-weight: 500;
              margin-bottom: 2px;
            }

            .user-meta {
              display: flex;
              gap: 8px;
              font-size: 12px;
              color: #8c8c8c;
            }
          }
        }

        .card-actions {
          flex-shrink: 0;
        }
      }

      .card-content {
        padding: 12px;

        .permission-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
          gap: 8px;
        }
      }
    }
  }
}
</style>