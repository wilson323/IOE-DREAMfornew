<template>
  <div class="permission-cell" :class="cellClass">
    <!-- 加载状态 -->
    <a-spin :spinning="loading" size="small">
      <!-- 只读模式 -->
      <template v-if="!editable">
        <a-badge
          v-if="hasPermission"
          :status="getStatusBadgeType(userPermission?.status)"
          :text="getStatusText(userPermission?.status)"
        />
        <span v-else class="no-permission">-</span>
      </template>

      <!-- 编辑模式 -->
      <template v-else>
        <!-- 有权限状态 -->
        <div v-if="hasPermission" class="permission-assigned">
          <a-tooltip :title="getPermissionTooltip">
            <a-badge
              :status="getStatusBadgeType(userPermission?.status)"
              :text="compact ? '' : getStatusText(userPermission?.status)"
            />
          </a-tooltip>
          <div class="permission-actions" v-if="!compact">
            <a-button
              type="link"
              size="small"
              @click="handleView"
            >
              <EyeOutlined />
            </a-button>
            <a-popconfirm
              title="确定撤销此权限吗？"
              @confirm="handleRevoke"
            >
              <a-button
                type="link"
                size="small"
                danger
              >
                <CloseOutlined />
              </a-button>
            </a-popconfirm>
          </div>
          <div v-else-if="compact" class="compact-actions">
            <a-dropdown trigger="click">
              <a-button type="link" size="small">
                <MoreOutlined />
              </a-button>
              <template #overlay>
                <a-menu @click="handleMenuClick">
                  <a-menu-item key="view">
                    <EyeOutlined />
                    查看详情
                  </a-menu-item>
                  <a-menu-item key="revoke" danger>
                    <CloseOutlined />
                    撤销权限
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </div>

        <!-- 无权限状态 -->
        <div v-else class="permission-unassigned">
          <a-tooltip title="分配权限">
            <a-button
              type="dashed"
              size="small"
              :icon="h(PlusOutlined)"
              @click="handleAssign"
            >
              <span v-if="!compact">分配</span>
            </a-button>
          </a-tooltip>
        </div>
      </template>
    </a-spin>

    <!-- 权限详情弹窗 -->
    <PermissionDetailModal
      v-model:open="detailVisible"
      :user="user"
      :permission="permission"
      :userPermission="userPermission"
      @assign="handleAssign"
      @revoke="handleRevoke"
    />
  </div>
</template>

<script setup>
import { ref, computed, h } from 'vue';
import { message } from 'ant-design-vue';
import {
  EyeOutlined,
  CloseOutlined,
  PlusOutlined,
  MoreOutlined
} from '@ant-design/icons-vue';
import { PermissionStatus } from '/@/types/permission';
import PermissionDetailModal from './PermissionDetailModal.vue';

// Props定义
const props = defineProps({
  // 基础数据
  user: {
    type: Object,
    required: true
  },
  permission: {
    type: Object,
    required: true
  },
  userPermission: {
    type: Object,
    default: null
  },

  // 状态控制
  editable: {
    type: Boolean,
    default: true
  },
  compact: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
});

// Emits定义
const emit = defineEmits([
  'assign',
  'revoke',
  'view',
  'update'
]);

// 响应式数据
const detailVisible = ref(false);

// 计算属性
const hasPermission = computed(() => {
  return !!props.userPermission && props.userPermission.status !== PermissionStatus.REVOKED;
});

const cellClass = computed(() => {
  return {
    'editable': props.editable,
    'compact': props.compact,
    'has-permission': hasPermission.value,
    'no-permission': !hasPermission.value,
    'expired': isExpired.value,
    'expiring': isExpiring.value
  };
});

const isExpired = computed(() => {
  return hasPermission.value && props.userPermission?.status === PermissionStatus.EXPIRED;
});

const isExpiring = computed(() => {
  if (!hasPermission.value || !props.userPermission?.expireTime) {
    return false;
  }
  const expireTime = new Date(props.userPermission.expireTime);
  const now = new Date();
  const daysUntilExpiry = Math.ceil((expireTime - now) / (1000 * 60 * 60 * 24));
  return daysUntilExpiry > 0 && daysUntilExpiry <= 7;
});

const getPermissionTooltip = computed(() => {
  if (!props.userPermission) return '';

  const parts = [];
  parts.push(`状态: ${getStatusText(props.userPermission.status)}`);

  if (props.userPermission.expireTime) {
    parts.push(`过期时间: ${formatDate(props.userPermission.expireTime)}`);
  }

  if (props.userPermission.assignTime) {
    parts.push(`分配时间: ${formatDate(props.userPermission.assignTime)}`);
  }

  if (props.userPermission.assignUserName) {
    parts.push(`分配人: ${props.userPermission.assignUserName}`);
  }

  if (props.userPermission.remark) {
    parts.push(`备注: ${props.userPermission.remark}`);
  }

  return parts.join('\n');
});

// 方法
/**
 * 获取状态徽章类型
 */
const getStatusBadgeType = (status) => {
  const statusMap = {
    [PermissionStatus.ACTIVE]: 'success',
    [PermissionStatus.INACTIVE]: 'default',
    [PermissionStatus.EXPIRED]: 'warning',
    [PermissionStatus.REVOKED]: 'error'
  };
  return statusMap[status] || 'default';
};

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    [PermissionStatus.ACTIVE]: '生效中',
    [PermissionStatus.INACTIVE]: '未生效',
    [PermissionStatus.EXPIRED]: '已过期',
    [PermissionStatus.REVOKED]: '已撤销'
  };
  return textMap[status] || '未知';
};

/**
 * 格式化日期
 */
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

/**
 * 处理分配权限
 */
const handleAssign = () => {
  emit('assign', {
    user: props.user,
    permission: props.permission
  });
};

/**
 * 处理撤销权限
 */
const handleRevoke = () => {
  emit('revoke', {
    user: props.user,
    permission: props.permission,
    userPermission: props.userPermission
  });
};

/**
 * 处理查看详情
 */
const handleView = () => {
  if (props.compact) {
    detailVisible.value = true;
  } else {
    emit('view', {
      user: props.user,
      permission: props.permission,
      userPermission: props.userPermission
    });
  }
};

/**
 * 处理菜单点击
 */
const handleMenuClick = ({ key }) => {
  switch (key) {
    case 'view':
      handleView();
      break;
    case 'revoke':
      handleRevoke();
      break;
  }
};

// 暴露方法给父组件
defineExpose({
  hasPermission,
  isExpired,
  isExpiring,
  handleAssign,
  handleRevoke,
  handleView
});
</script>

<style scoped lang="less">
.permission-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 4px;
  transition: all 0.2s;

  &.editable {
    cursor: pointer;

    &:hover {
      background-color: #f5f5f5;
      border-radius: 4px;
    }
  }

  &.compact {
    padding: 2px;
    min-height: 24px;

    .permission-assigned {
      .permission-actions {
        opacity: 0;
        transition: opacity 0.2s;
      }

      &:hover .permission-actions {
        opacity: 1;
      }

      .compact-actions {
        opacity: 0;
        transition: opacity 0.2s;

        &:hover {
          opacity: 1;
        }
      }
    }
  }

  &.has-permission {
    .permission-assigned {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;

      .permission-actions {
        display: flex;
        gap: 2px;
      }
    }
  }

  &.no-permission {
    .permission-unassigned {
      width: 100%;
      text-align: center;

      .ant-btn {
        width: 100%;
        border-color: #d9d9d9;
        color: #8c8c8c;

        &:hover {
          color: #1890ff;
          border-color: #1890ff;
        }
      }
    }
  }

  &.expired {
    opacity: 0.6;
    background-color: #fff2e8;

    .ant-badge-status-text {
      color: #fa8c16;
    }
  }

  &.expiring {
    background-color: #fff7e6;

    .ant-badge-status-text {
      color: #fa8c16;
    }
  }

  .no-permission {
    color: #d9d9d9;
    font-size: 16px;
    font-weight: bold;
  }

  // 权限详情样式
  :deep(.ant-badge) {
    .ant-badge-status-text {
      font-size: 12px;
      margin-left: 4px;
    }
  }

  // 按钮样式
  :deep(.ant-btn) {
    &.ant-btn-dashed {
      border-width: 1px;
      height: 24px;
      padding: 0 8px;
      font-size: 12px;
      line-height: 22px;

      &:hover {
        border-color: #1890ff;
        color: #1890ff;
      }
    }

    &.ant-btn-link {
      padding: 0;
      height: auto;
      line-height: 1;
      font-size: 12px;

      &:hover {
        color: #1890ff;
      }

      &.ant-btn-dangerous:hover {
        color: #ff4d4f;
      }
    }
  }

  // 加载样式
  :deep(.ant-spin) {
    .ant-spin-dot {
      font-size: 12px;
    }
  }
}
</style>