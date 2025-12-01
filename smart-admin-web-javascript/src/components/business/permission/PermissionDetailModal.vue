<template>
  <a-modal
    v-model:open="open"
    :title="modalTitle"
    :width="700"
    :footer="userPermission ? actionButtons : null"
    @cancel="handleCancel"
  >
    <div class="permission-detail-modal">
      <!-- 用户信息 -->
      <div class="user-section" v-if="user">
        <a-descriptions title="用户信息" :column="2" bordered size="small">
          <a-descriptions-item label="用户姓名">
            <div class="user-info">
              <a-avatar :src="user.avatar" :size="24">
                {{ user.userName?.charAt(0) }}
              </a-avatar>
              <span>{{ user.userName }}</span>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="用户编号">
            {{ user.userCode }}
          </a-descriptions-item>
          <a-descriptions-item label="所属部门">
            {{ user.departmentName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="职位">
            {{ user.positionName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="联系电话">
            {{ user.phone || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="邮箱">
            {{ user.email || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 权限信息 -->
      <div class="permission-section" v-if="permission">
        <a-descriptions title="权限信息" :column="2" bordered size="small">
          <a-descriptions-item label="权限名称">
            <div class="permission-info">
              <span>{{ permission.name }}</span>
              <a-tag
                :color="getSecurityLevelColor(permission.securityLevel)"
                size="small"
              >
                {{ getSecurityLevelText(permission.securityLevel) }}
              </a-tag>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="权限编码">
            <a-typography-text copyable>{{ permission.code }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="权限类型">
            <a-tag :color="getPermissionTypeColor(permission.type)">
              {{ getPermissionTypeText(permission.type) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="权限路径">
            {{ permission.path || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间" :span="2">
            {{ formatDate(permission.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="权限描述" :span="2">
            {{ permission.description || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 用户权限详情 -->
      <div class="user-permission-section" v-if="userPermission">
        <a-descriptions title="权限分配详情" :column="2" bordered size="small">
          <a-descriptions-item label="分配状态">
            <a-badge
              :status="getStatusBadgeType(userPermission.status)"
              :text="getStatusText(userPermission.status)"
            />
          </a-descriptions-item>
          <a-descriptions-item label="分配类型">
            <a-tag>{{ getAssignTypeText(userPermission.assignType) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="分配时间">
            {{ formatDate(userPermission.assignTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="分配人">
            {{ userPermission.assignUserName }}
          </a-descriptions-item>
          <a-descriptions-item label="过期时间">
            <span :class="{ 'expired-text': isExpired }">
              {{ userPermission.expireTime ? formatDate(userPermission.expireTime) : '永不过期' }}
            </span>
            <a-tag
              v-if="isExpired"
              color="red"
              size="small"
            >
              已过期
            </a-tag>
            <a-tag
              v-else-if="isExpiring"
              color="orange"
              size="small"
            >
              即将过期
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="剩余时间">
            <span :class="{ 'expired-text': isExpired }">
              {{ remainingTimeText }}
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="分配原因" :span="2">
            {{ userPermission.reason || '-' }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 时间策略信息 -->
        <div class="time-strategy-section" v-if="userPermission.timeStrategy">
          <a-descriptions title="时间策略" :column="2" bordered size="small">
            <a-descriptions-item label="策略名称">
              {{ userPermission.timeStrategy.name }}
            </a-descriptions-item>
            <a-descriptions-item label="策略类型">
              <a-tag>{{ getTimeStrategyTypeText(userPermission.timeStrategy.type) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="策略描述" :span="2">
              {{ userPermission.timeStrategy.description || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 地理围栏信息 -->
        <div class="geo-fence-section" v-if="userPermission.geoFences && userPermission.geoFences.length > 0">
          <a-descriptions title="地理围栏" :column="1" bordered size="small">
            <a-descriptions-item
              v-for="geoFence in userPermission.geoFences"
              :key="geoFence.id"
              :label="geoFence.name"
            >
              <div class="geo-fence-info">
                <a-tag :color="getGeoFenceTypeColor(geoFence.type)">
                  {{ getGeoFenceTypeText(geoFence.type) }}
                </a-tag>
                <span class="geo-fence-description">{{ geoFence.description || '-' }}</span>
              </div>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 设备权限信息 -->
        <div class="device-section" v-if="userPermission.devices && userPermission.devices.length > 0">
          <a-descriptions title="设备权限" :column="1" bordered size="small">
            <a-descriptions-item
              v-for="device in userPermission.devices"
              :key="device.id"
              :label="device.name"
            >
              <div class="device-info">
                <a-tag>{{ device.deviceType }}</a-tag>
                <span class="device-code">{{ device.deviceCode }}</span>
                <a-tag :color="getDevicePermissionTypeColor(device.permissionType)">
                  {{ getDevicePermissionTypeText(device.permissionType) }}
                </a-tag>
              </div>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 操作历史 -->
        <div class="history-section" v-if="operationHistory && operationHistory.length > 0">
          <a-divider>操作历史</a-divider>
          <a-timeline>
            <a-timeline-item
              v-for="history in operationHistory"
              :key="history.id"
              :color="getHistoryColor(history.action)"
            >
              <div class="history-item">
                <div class="history-header">
                  <span class="action-type">{{ getActionTypeText(history.action) }}</span>
                  <span class="action-time">{{ formatDate(history.createTime) }}</span>
                </div>
                <div class="history-content">
                  <span class="operator">{{ history.operatorName }}</span>
                  {{ getActionDescription(history) }}
                </div>
                <div class="history-reason" v-if="history.reason">
                  原因：{{ history.reason }}
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </div>
      </div>

      <!-- 无权限分配信息 -->
      <div class="no-permission-section" v-if="user && permission && !userPermission">
        <a-result
          status="info"
          title="用户未分配此权限"
          sub-title="该用户当前没有此权限，您可以为其分配权限"
        >
          <template #extra>
            <a-button type="primary" @click="handleAssign">
              <PlusOutlined />
              分配权限
            </a-button>
          </template>
        </a-result>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import {
  SecurityLevel,
  PermissionType,
  PermissionStatus,
  PermissionAssignType,
  TimeStrategyType
} from '/@/types/permission';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';

dayjs.extend(duration);

// Props定义
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  user: {
    type: Object,
    default: null
  },
  permission: {
    type: Object,
    default: null
  },
  userPermission: {
    type: Object,
    default: null
  },
  operationHistory: {
    type: Array,
    default: () => []
  }
});

// Emits定义
const emit = defineEmits([
  'update:open',
  'assign',
  'revoke',
  'edit',
  'cancel'
]);

// 计算属性
const modalTitle = computed(() => {
  if (props.user && props.permission) {
    return `${props.user.userName} - ${props.permission.name} 权限详情`;
  }
  return '权限详情';
});

const isExpired = computed(() => {
  if (!props.userPermission?.expireTime) return false;
  return new Date(props.userPermission.expireTime) < new Date();
});

const isExpiring = computed(() => {
  if (!props.userPermission?.expireTime || isExpired.value) return false;
  const expireTime = new Date(props.userPermission.expireTime);
  const now = new Date();
  const daysUntilExpiry = Math.ceil((expireTime - now) / (1000 * 60 * 60 * 24));
  return daysUntilExpiry > 0 && daysUntilExpiry <= 7;
});

const remainingTimeText = computed(() => {
  if (!props.userPermission?.expireTime) return '永不过期';

  const expireTime = dayjs(props.userPermission.expireTime);
  const now = dayjs();

  if (expireTime.isBefore(now)) {
    return '已过期';
  }

  const diff = expireTime.diff(now);
  const duration = dayjs.duration(diff);

  const days = Math.floor(duration.asDays());
  const hours = duration.hours();
  const minutes = duration.minutes();

  if (days > 0) {
    return `剩余 ${days} 天 ${hours} 小时`;
  } else if (hours > 0) {
    return `剩余 ${hours} 小时 ${minutes} 分钟`;
  } else {
    return `剩余 ${minutes} 分钟`;
  }
});

const actionButtons = computed(() => {
  const buttons = [];

  if (props.userPermission?.status === PermissionStatus.ACTIVE) {
    buttons.push({
      key: 'revoke',
      label: '撤销权限',
      type: 'default',
      danger: true,
      onClick: handleRevoke
    });

    if (props.userPermission.expireTime && !isExpired.value) {
      buttons.push({
        key: 'extend',
        label: '延长权限',
        type: 'default',
        onClick: handleExtend
      });
    }
  }

  buttons.push({
    key: 'edit',
    label: '编辑权限',
    type: 'primary',
    onClick: handleEdit
  });

  return buttons;
});

// 方法
/**
 * 格式化日期
 */
const formatDate = (dateString) => {
  if (!dateString) return '-';
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss');
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
    [SecurityLevel.PUBLIC]: '公开级',
    [SecurityLevel.INTERNAL]: '内部级',
    [SecurityLevel.CONFIDENTIAL]: '秘密级',
    [SecurityLevel.SECRET]: '机密级',
    [SecurityLevel.TOP_SECRET]: '绝密级'
  };
  return textMap[level] || '未知';
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
    [PermissionType.MENU]: '菜单权限',
    [PermissionType.BUTTON]: '按钮权限',
    [PermissionType.API]: '接口权限',
    [PermissionType.DATA]: '数据权限',
    [PermissionType.DEVICE]: '设备权限',
    [PermissionType.AREA]: '区域权限',
    [PermissionType.TIME]: '时间权限'
  };
  return textMap[type] || '未知';
};

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
 * 获取分配类型文本
 */
const getAssignTypeText = (type) => {
  const textMap = {
    [PermissionAssignType.USER]: '直接分配',
    [PermissionAssignType.ROLE]: '角色分配',
    [PermissionAssignType.DEPARTMENT]: '部门分配',
    [PermissionAssignType.POSITION]: '职位分配',
    [PermissionAssignType.TEMPLATE]: '模板分配'
  };
  return textMap[type] || '未知';
};

/**
 * 获取时间策略类型文本
 */
const getTimeStrategyTypeText = (type) => {
  const textMap = {
    [TimeStrategyType.ALWAYS]: '永久有效',
    [TimeStrategyType.PERIOD]: '时间段',
    [TimeStrategyType.WORKDAY]: '工作日',
    [TimeStrategyType.WEEKEND]: '周末',
    [TimeStrategyType.CUSTOM]: '自定义'
  };
  return textMap[type] || '未知';
};

/**
 * 获取地理围栏类型颜色
 */
const getGeoFenceTypeColor = (type) => {
  const colorMap = {
    'CIRCLE': 'blue',
    'POLYGON': 'green',
    'RECTANGLE': 'orange'
  };
  return colorMap[type] || 'default';
};

/**
 * 获取地理围栏类型文本
 */
const getGeoFenceTypeText = (type) => {
  const textMap = {
    'CIRCLE': '圆形围栏',
    'POLYGON': '多边形围栏',
    'RECTANGLE': '矩形围栏'
  };
  return textMap[type] || '未知';
};

/**
 * 获取设备权限类型颜色
 */
const getDevicePermissionTypeColor = (type) => {
  const colorMap = {
    'READ': 'blue',
    'WRITE': 'orange',
    'CONTROL': 'red',
    'ADMIN': 'purple'
  };
  return colorMap[type] || 'default';
};

/**
 * 获取设备权限类型文本
 */
const getDevicePermissionTypeText = (type) => {
  const textMap = {
    'READ': '只读',
    'WRITE': '读写',
    'CONTROL': '控制',
    'ADMIN': '管理'
  };
  return textMap[type] || '未知';
};

/**
 * 获取历史记录颜色
 */
const getHistoryColor = (action) => {
  const colorMap = {
    'GRANT': 'green',
    'REVOKE': 'red',
    'MODIFY': 'blue',
    'VIEW': 'gray'
  };
  return colorMap[action] || 'gray';
};

/**
 * 获取操作类型文本
 */
const getActionTypeText = (action) => {
  const textMap = {
    'GRANT': '分配权限',
    'REVOKE': '撤销权限',
    'MODIFY': '修改权限',
    'VIEW': '查看权限'
  };
  return textMap[action] || '未知操作';
};

/**
 * 获取操作描述
 */
const getActionDescription = (history) => {
  switch (history.action) {
    case 'GRANT':
      return '为用户分配了权限';
    case 'REVOKE':
      return '撤销了用户的权限';
    case 'MODIFY':
      return '修改了用户的权限设置';
    case 'VIEW':
      return '查看了用户的权限信息';
    default:
      return '执行了权限操作';
  }
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
 * 处理延长权限
 */
const handleExtend = () => {
  emit('extend', {
    user: props.user,
    permission: props.permission,
    userPermission: props.userPermission
  });
};

/**
 * 处理编辑权限
 */
const handleEdit = () => {
  emit('edit', {
    user: props.user,
    permission: props.permission,
    userPermission: props.userPermission
  });
};

/**
 * 处理取消
 */
const handleCancel = () => {
  emit('update:open', false);
  emit('cancel');
};
</script>

<style scoped lang="less">
.permission-detail-modal {
  .user-section,
  .permission-section,
  .user-permission-section {
    margin-bottom: 20px;

    :deep(.ant-descriptions) {
      .ant-descriptions-item-label {
        font-weight: 500;
        width: 120px;
      }
    }
  }

  .user-info,
  .permission-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .expired-text {
    color: #ff4d4f;
    font-weight: 500;
  }

  .time-strategy-section,
  .geo-fence-section,
  .device-section {
    margin-top: 16px;

    :deep(.ant-descriptions) {
      margin-top: 12px;
    }
  }

  .geo-fence-info,
  .device-info {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;

    .geo-fence-description,
    .device-code {
      color: #595959;
    }
  }

  .history-section {
    margin-top: 20px;

    .history-item {
      .history-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .action-type {
          font-weight: 500;
          color: #262626;
        }

        .action-time {
          font-size: 12px;
          color: #8c8c8c;
        }
      }

      .history-content {
        margin-bottom: 4px;

        .operator {
          font-weight: 500;
          color: #1890ff;
        }
      }

      .history-reason {
        font-size: 12px;
        color: #8c8c8c;
        font-style: italic;
      }
    }
  }

  .no-permission-section {
    text-align: center;
    padding: 20px 0;
  }

  // 结果页面样式
  :deep(.ant-result) {
    padding: 20px 0;
  }

  // 描述列表样式优化
  :deep(.ant-descriptions) {
    .ant-descriptions-item-content {
      word-break: break-all;
    }
  }
}
</style>