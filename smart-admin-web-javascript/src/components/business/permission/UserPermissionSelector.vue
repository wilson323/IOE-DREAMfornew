<template>
  <div class="user-permission-selector">
    <div class="selector-header" v-if="showHeader">
      <div class="selector-title">
        <slot name="title">
          <span>{{ title }}</span>
        </slot>
      </div>
      <div class="selector-actions">
        <slot name="actions">
          <a-button
            v-if="showRefresh"
            type="link"
            size="small"
            @click="handleRefresh"
            :loading="loading"
          >
            <ReloadOutlined />
            刷新
          </a-button>
          <a-button
            v-if="multiple && allowBatch"
            type="link"
            size="small"
            @click="handleBatchSelect"
          >
            <TeamOutlined />
            批量选择
          </a-button>
        </slot>
      </div>
    </div>

    <div class="selector-content">
      <!-- 搜索和过滤 -->
      <div class="search-filter" v-if="searchable || showFilter">
        <a-row :gutter="12">
          <a-col :span="searchable ? 12 : showFilter ? 8 : 0" v-if="searchable">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索用户姓名、编号、部门"
              allowClear
              @search="handleSearch"
              @change="handleSearchChange"
            />
          </a-col>
          <a-col :span="showFilter ? (searchable ? 12 : 24) : 0" v-if="showFilter">
            <div class="filter-actions">
              <a-select
                v-if="showDepartmentFilter"
                v-model:value="filters.departmentId"
                placeholder="选择部门"
                allowClear
                style="width: 150px"
                @change="handleFilterChange"
              >
                <a-select-option
                  v-for="dept in departmentOptions"
                  :key="dept.value"
                  :value="dept.value"
                >
                  {{ dept.label }}
                </a-select-option>
              </a-select>

              <a-select
                v-if="showPositionFilter"
                v-model:value="filters.positionId"
                placeholder="选择职位"
                allowClear
                style="width: 120px"
                @change="handleFilterChange"
              >
                <a-select-option
                  v-for="pos in positionOptions"
                  :key="pos.value"
                  :value="pos.value"
                >
                  {{ pos.label }}
                </a-select-option>
              </a-select>

              <a-select
                v-if="showStatusFilter"
                v-model:value="filters.status"
                placeholder="用户状态"
                allowClear
                style="width: 100px"
                @change="handleFilterChange"
              >
                <a-select-option value="ACTIVE">在职</a-select-option>
                <a-select-option value="INACTIVE">离职</a-select-option>
                <a-select-option value="SUSPENDED">停用</a-select-option>
              </a-select>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 用户列表 -->
      <div class="user-list" :style="{ height: listHeight }">
        <a-spin :spinning="loading">
          <div class="list-container">
            <!-- 单选模式 -->
            <template v-if="!multiple">
              <a-radio-group
                v-model:value="selectedUserId"
                @change="handleSingleSelect"
              >
                <div class="user-item" v-for="user in filteredUsers" :key="user.userId">
                  <a-radio :value="user.userId" :disabled="disabled || user.disabled">
                    <div class="user-info">
                      <a-avatar :src="user.avatar" :size="32">
                        {{ user.userName?.charAt(0) }}
                      </a-avatar>
                      <div class="user-details">
                        <div class="user-name">
                          {{ user.userName }}
                          <a-tag
                            v-if="showPermission && user.permissionCount"
                            size="small"
                            color="blue"
                          >
                            {{ user.permissionCount }}个权限
                          </a-tag>
                        </div>
                        <div class="user-meta">
                          <span class="user-code">{{ user.userCode }}</span>
                          <span class="department" v-if="user.departmentName">
                            {{ user.departmentName }}
                          </span>
                          <span class="position" v-if="user.positionName">
                            {{ user.positionName }}
                          </span>
                        </div>
                        <!-- 权限预览 -->
                        <div class="permission-preview" v-if="showPermission && user.permissions">
                          <a-tag
                            v-for="permission in user.permissions.slice(0, 3)"
                            :key="permission.permissionId"
                            :color="getSecurityLevelColor(permission.securityLevel)"
                            size="small"
                          >
                            {{ permission.permissionName }}
                          </a-tag>
                          <a-tag
                            v-if="user.permissions.length > 3"
                            color="default"
                            size="small"
                          >
                            +{{ user.permissions.length - 3 }}
                          </a-tag>
                        </div>
                      </div>
                    </div>
                  </a-radio>
                </div>
              </a-radio-group>
            </template>

            <!-- 多选模式 -->
            <template v-else>
              <a-checkbox-group
                v-model:value="selectedUserIds"
                @change="handleMultipleSelect"
              >
                <div class="user-item" v-for="user in filteredUsers" :key="user.userId">
                  <a-checkbox
                    :value="user.userId"
                    :disabled="disabled || user.disabled"
                    :indeterminate="user.indeterminate"
                  >
                    <div class="user-info">
                      <a-avatar :src="user.avatar" :size="32">
                        {{ user.userName?.charAt(0) }}
                      </a-avatar>
                      <div class="user-details">
                        <div class="user-name">
                          {{ user.userName }}
                          <a-tag
                            v-if="showPermission && user.permissionCount"
                            size="small"
                            color="blue"
                          >
                            {{ user.permissionCount }}个权限
                          </a-tag>
                        </div>
                        <div class="user-meta">
                          <span class="user-code">{{ user.userCode }}</span>
                          <span class="department" v-if="user.departmentName">
                            {{ user.departmentName }}
                          </span>
                          <span class="position" v-if="user.positionName">
                            {{ user.positionName }}
                          </span>
                        </div>
                        <!-- 权限预览 -->
                        <div class="permission-preview" v-if="showPermission && user.permissions">
                          <a-tag
                            v-for="permission in user.permissions.slice(0, 3)"
                            :key="permission.permissionId"
                            :color="getSecurityLevelColor(permission.securityLevel)"
                            size="small"
                          >
                            {{ permission.permissionName }}
                          </a-tag>
                          <a-tag
                            v-if="user.permissions.length > 3"
                            color="default"
                            size="small"
                          >
                            +{{ user.permissions.length - 3 }}
                          </a-tag>
                        </div>
                      </div>
                    </div>
                  </a-checkbox>
                </div>
              </a-checkbox-group>
            </template>
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="showPagination && pagination.total > 0">
            <a-pagination
              v-model:current="pagination.current"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :show-size-changer="showSizeChanger"
              :show-quick-jumper="showQuickJumper"
              :show-total="showTotal"
              size="small"
              @change="handlePageChange"
              @show-size-change="handlePageSizeChange"
            />
          </div>
        </a-spin>
      </div>

      <!-- 空状态 -->
      <a-empty
        v-if="!loading && (!filteredUsers || filteredUsers.length === 0)"
        :description="emptyDescription"
      >
        <template #image>
          <UserOutlined />
        </template>
        <a-button v-if="allowAdd" type="primary" @click="handleAddUser">
          添加用户
        </a-button>
      </a-empty>
    </div>

    <!-- 批量选择弹窗 -->
    <a-modal
      v-model:open="batchSelectVisible"
      title="批量选择用户"
      :width="800"
      @ok="handleBatchSelectConfirm"
      @cancel="handleBatchSelectCancel"
    >
      <div class="batch-select-content">
        <a-table
          :columns="batchSelectColumns"
          :data-source="filteredUsers"
          :row-selection="batchSelectRowSelection"
          :pagination="false"
          size="small"
          :scroll="{ y: 300 }"
        />
      </div>
    </a-modal>

    <!-- 用户详情弹窗 -->
    <a-modal
      v-model:open="userDetailVisible"
      :title="`${currentUser?.userName} - 权限详情`"
      :width="600"
      :footer="null"
    >
      <div class="user-detail-content">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="用户姓名">
            {{ currentUser?.userName }}
          </a-descriptions-item>
          <a-descriptions-item label="用户编号">
            {{ currentUser?.userCode }}
          </a-descriptions-item>
          <a-descriptions-item label="所属部门">
            {{ currentUser?.departmentName }}
          </a-descriptions-item>
          <a-descriptions-item label="职位">
            {{ currentUser?.positionName }}
          </a-descriptions-item>
          <a-descriptions-item label="权限数量" :span="2">
            {{ currentUser?.permissions?.length || 0 }} 个
          </a-descriptions-item>
        </a-descriptions>

        <div class="permission-list" v-if="currentUser?.permissions?.length > 0">
          <a-divider>权限列表</a-divider>
          <div class="permission-tags">
            <a-tag
              v-for="permission in currentUser.permissions"
              :key="permission.permissionId"
              :color="getSecurityLevelColor(permission.securityLevel)"
              class="permission-tag"
            >
              <div class="permission-info">
                <div class="permission-name">{{ permission.permissionName }}</div>
                <div class="permission-meta">
                  <span class="security-level">
                    {{ getSecurityLevelText(permission.securityLevel) }}
                  </span>
                  <span class="permission-type">
                    {{ getPermissionTypeText(permission.type) }}
                  </span>
                  <span class="expire-time" v-if="permission.expireTime">
                    {{ formatDate(permission.expireTime) }}
                  </span>
                </div>
              </div>
            </a-tag>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  ReloadOutlined,
  TeamOutlined,
  UserOutlined,
  SearchOutlined,
  FilterOutlined
} from '@ant-design/icons-vue';
import { SecurityLevel, PermissionType } from '/@/types/permission';
import dayjs from 'dayjs';

// Props定义
const props = defineProps({
  // 值相关
  value: {
    type: [Number, Array],
    default: undefined
  },
  users: {
    type: Array,
    default: () => []
  },

  // 功能控制
  multiple: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },

  // 显示控制
  showHeader: {
    type: Boolean,
    default: true
  },
  showRefresh: {
    type: Boolean,
    default: true
  },
  showPermission: {
    type: Boolean,
    default: true
  },
  showFilter: {
    type: Boolean,
    default: true
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  showSizeChanger: {
    type: Boolean,
    default: true
  },
  showQuickJumper: {
    type: Boolean,
    default: true
  },
  showTotal: {
    type: Boolean,
    default: true
  },
  searchable: {
    type: Boolean,
    default: true
  },
  showDepartmentFilter: {
    type: Boolean,
    default: true
  },
  showPositionFilter: {
    type: Boolean,
    default: true
  },
  showStatusFilter: {
    type: Boolean,
    default: true
  },

  // 功能限制
  allowAdd: {
    type: Boolean,
    default: false
  },
  allowBatch: {
    type: Boolean,
    default: true
  },

  // 过滤条件
  departmentId: {
    type: Number,
    default: undefined
  },
  positionId: {
    type: Number,
    default: undefined
  },
  securityLevel: {
    type: String,
    default: undefined
  },

  // 样式控制
  height: {
    type: [String, Number],
    default: '400px'
  },
  title: {
    type: String,
    default: '用户权限选择器'
  },
  emptyDescription: {
    type: String,
    default: '暂无用户数据'
  },

  // 部门和职位选项
  departmentOptions: {
    type: Array,
    default: () => []
  },
  positionOptions: {
    type: Array,
    default: () => []
  }
});

// Emits定义
const emit = defineEmits([
  'update:value',
  'select',
  'change',
  'search',
  'filter',
  'refresh',
  'pageChange',
  'pageSizeChange',
  'addUser',
  'viewUser'
]);

// 响应式数据
const searchKeyword = ref('');
const selectedUserId = ref(null);
const selectedUserIds = ref([]);
const batchSelectVisible = ref(false);
const userDetailVisible = ref(false);
const currentUser = ref(null);

// 过滤条件
const filters = reactive({
  departmentId: undefined,
  positionId: undefined,
  status: undefined
});

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
});

// 计算属性
const listHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`;
  }
  return props.height;
});

const filteredUsers = computed(() => {
  let users = [...props.users];

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    users = users.filter(user =>
      user.userName?.toLowerCase().includes(keyword) ||
      user.userCode?.toLowerCase().includes(keyword) ||
      user.departmentName?.toLowerCase().includes(keyword) ||
      user.positionName?.toLowerCase().includes(keyword)
    );
  }

  // 部门过滤
  if (filters.departmentId) {
    users = users.filter(user => user.departmentId === filters.departmentId);
  }

  // 职位过滤
  if (filters.positionId) {
    users = users.filter(user => user.positionId === filters.positionId);
  }

  // 状态过滤
  if (filters.status) {
    users = users.filter(user => user.status === filters.status);
  }

  // 安全级别过滤
  if (props.securityLevel) {
    users = users.filter(user => {
      if (!user.permissions || user.permissions.length === 0) return false;
      return user.permissions.some(p => p.securityLevel === props.securityLevel);
    });
  }

  return users;
});

// 批量选择表格列
const batchSelectColumns = [
  {
    title: '用户姓名',
    dataIndex: 'userName',
    width: 100,
    ellipsis: true
  },
  {
    title: '用户编号',
    dataIndex: 'userCode',
    width: 120,
    ellipsis: true
  },
  {
    title: '所属部门',
    dataIndex: 'departmentName',
    width: 120,
    ellipsis: true
  },
  {
    title: '职位',
    dataIndex: 'positionName',
    width: 100,
    ellipsis: true
  },
  {
    title: '权限数量',
    dataIndex: 'permissionCount',
    width: 80,
    align: 'center'
  }
];

// 批量选择行选择配置
const batchSelectRowSelection = computed(() => ({
  selectedRowKeys: selectedUserIds.value,
  onChange: (selectedRowKeys) => {
    selectedUserIds.value = selectedRowKeys;
  },
  getCheckboxProps: (record) => ({
    disabled: record.disabled
  })
}));

// 监听器
watch(() => props.value, (newVal) => {
  if (props.multiple) {
    selectedUserIds.value = Array.isArray(newVal) ? newVal : (newVal ? [newVal] : []);
  } else {
    selectedUserId.value = newVal;
  }
}, { immediate: true });

watch(selectedUserId, (newVal) => {
  if (!props.multiple) {
    emit('update:value', newVal);
    emit('change', newVal);
  }
});

watch(selectedUserIds, (newVal) => {
  if (props.multiple) {
    emit('update:value', newVal);
    emit('change', newVal);
  }
});

watch(() => props.departmentId, (newVal) => {
  filters.departmentId = newVal;
});

watch(() => props.positionId, (newVal) => {
  filters.positionId = newVal;
});

// 生命周期
onMounted(() => {
  // 初始化过滤条件
  filters.departmentId = props.departmentId;
  filters.positionId = props.positionId;
});

// 方法
/**
 * 处理单选
 */
const handleSingleSelect = (e) => {
  const userId = e.target.value;
  emit('select', userId);
};

/**
 * 处理多选
 */
const handleMultipleSelect = (checkedValues) => {
  emit('select', checkedValues);
};

/**
 * 处理搜索
 */
const handleSearch = (value) => {
  emit('search', value);
};

/**
 * 处理搜索输入变化
 */
const handleSearchChange = (e) => {
  if (!e.target.value) {
    handleSearch('');
  }
};

/**
 * 处理过滤变化
 */
const handleFilterChange = () => {
  emit('filter', { ...filters });
};

/**
 * 处理刷新
 */
const handleRefresh = () => {
  emit('refresh');
};

/**
 * 处理批量选择
 */
const handleBatchSelect = () => {
  batchSelectVisible.value = true;
};

/**
 * 处理批量选择确认
 */
const handleBatchSelectConfirm = () => {
  batchSelectVisible.value = false;
  emit('select', selectedUserIds.value);
};

/**
 * 处理批量选择取消
 */
const handleBatchSelectCancel = () => {
  batchSelectVisible.value = false;
};

/**
 * 处理页面变化
 */
const handlePageChange = (page, pageSize) => {
  pagination.current = page;
  pagination.pageSize = pageSize;
  emit('pageChange', page, pageSize);
};

/**
 * 处理页面大小变化
 */
const handlePageSizeChange = (current, size) => {
  pagination.current = current;
  pagination.pageSize = size;
  emit('pageSizeChange', current, size);
};

/**
 * 处理添加用户
 */
const handleAddUser = () => {
  emit('addUser');
};

/**
 * 查看用户详情
 */
const handleViewUser = (user) => {
  currentUser.value = user;
  userDetailVisible.value = true;
  emit('viewUser', user);
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
 * 格式化日期
 */
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm');
};

/**
 * 获取选中的用户
 */
const getSelectedUsers = () => {
  if (props.multiple) {
    return props.users.filter(user => selectedUserIds.value.includes(user.userId));
  } else {
    return props.users.find(user => user.userId === selectedUserId.value);
  }
};

/**
 * 设置选中的用户
 */
const setSelectedUsers = (userIds) => {
  if (props.multiple) {
    selectedUserIds.value = Array.isArray(userIds) ? userIds : [];
  } else {
    selectedUserId.value = Array.isArray(userIds) ? userIds[0] : userIds;
  }
};

/**
 * 清空选择
 */
const clearSelection = () => {
  selectedUserId.value = null;
  selectedUserIds.value = [];
};

/**
 * 重置搜索和过滤
 */
const resetFilters = () => {
  searchKeyword.value = '';
  filters.departmentId = undefined;
  filters.positionId = undefined;
  filters.status = undefined;
};

/**
 * 搜索用户
 */
const searchUsers = (keyword) => {
  searchKeyword.value = keyword;
};

/**
 * 按部门过滤
 */
const filterByDepartment = (departmentId) => {
  filters.departmentId = departmentId;
};

/**
 * 按职位过滤
 */
const filterByPosition = (positionId) => {
  filters.positionId = positionId;
};

// 暴露方法给父组件
defineExpose({
  getSelectedUsers,
  setSelectedUsers,
  clearSelection,
  resetFilters,
  searchUsers,
  filterByDepartment,
  filterByPosition,
  handleViewUser
});
</script>

<style scoped lang="less">
.user-permission-selector {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .selector-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    margin-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;

    .selector-title {
      font-size: 14px;
      font-weight: 500;
      color: #262626;
    }

    .selector-actions {
      display: flex;
      gap: 8px;
      align-items: center;
    }
  }

  .search-filter {
    margin-bottom: 12px;

    .filter-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .user-list {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .list-container {
      flex: 1;
      overflow-y: auto;
      border: 1px solid #f0f0f0;
      border-radius: 6px;
      background-color: #fafafa;
    }

    .user-item {
      padding: 8px 12px;
      border-bottom: 1px solid #f0f0f0;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f5f5f5;
      }

      &:last-child {
        border-bottom: none;
      }

      .user-info {
        display: flex;
        gap: 12px;
        align-items: flex-start;

        .user-details {
          flex: 1;
          min-width: 0;

          .user-name {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 500;
            color: #262626;
            margin-bottom: 4px;
          }

          .user-meta {
            display: flex;
            gap: 12px;
            font-size: 12px;
            color: #8c8c8c;
            margin-bottom: 6px;

            .user-code {
              color: #595959;
            }

            .department,
            .position {
              color: #8c8c8c;
            }
          }

          .permission-preview {
            display: flex;
            gap: 4px;
            flex-wrap: wrap;
          }
        }
      }
    }

    .pagination {
      padding: 12px 0 0 0;
      text-align: center;
      border-top: 1px solid #f0f0f0;
      margin-top: 8px;
    }
  }

  .batch-select-content {
    .permission-tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-top: 12px;
    }
  }

  .user-detail-content {
    .permission-list {
      margin-top: 16px;

      .permission-tags {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .permission-tag {
          margin: 0;
          padding: 8px 12px;

          .permission-info {
            .permission-name {
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 4px;
            }

            .permission-meta {
              display: flex;
              gap: 12px;
              font-size: 12px;
              color: #8c8c8c;

              .security-level {
                color: #1890ff;
              }

              .permission-type {
                color: #52c41a;
              }

              .expire-time {
                color: #fa8c16;
              }
            }
          }
        }
      }
    }
  }

  // 单选和多选组件样式重写
  :deep(.ant-radio-wrapper),
  :deep(.ant-checkbox-wrapper) {
    width: 100%;
    display: flex;
    align-items: flex-start;
    padding: 4px 0;

    .ant-radio,
    .ant-checkbox {
      margin-top: 8px;
    }
  }

  // 表格样式
  :deep(.ant-table) {
    .ant-table-tbody > tr {
      cursor: pointer;

      &:hover {
        background-color: #f5f5f5;
      }
    }
  }
}
</style>