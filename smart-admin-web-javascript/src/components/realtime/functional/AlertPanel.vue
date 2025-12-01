<template>
  <div class="alert-panel">
    <!-- 告警统计卡片 -->
    <a-row :gutter="16" class="alert-statistics">
      <a-col :span="6">
        <a-card class="stat-card critical">
          <a-statistic
            title="严重告警"
            :value="alertStatistics.critical"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #prefix>
              <ExclamationCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card error">
          <a-statistic
            title="错误告警"
            :value="alertStatistics.error"
            :value-style="{ color: '#fa8c16' }"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card warning">
          <a-statistic
            title="警告告警"
            :value="alertStatistics.warning"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <InfoCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card info">
          <a-statistic
            title="信息告警"
            :value="alertStatistics.info"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <MessageOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 告警列表 -->
    <a-card title="告警列表" class="alert-list">
      <template #extra>
        <a-space>
          <a-select
            v-model:value="filterLevel"
            placeholder="告警级别"
            style="width: 120px"
            allow-clear
            @change="handleFilterChange"
          >
            <a-select-option value="critical">严重</a-select-option>
            <a-select-option value="error">错误</a-select-option>
            <a-select-option value="warning">警告</a-select-option>
            <a-select-option value="info">信息</a-select-option>
          </a-select>
          <a-select
            v-model:value="filterStatus"
            placeholder="告警状态"
            style="width: 120px"
            allow-clear
            @change="handleFilterChange"
          >
            <a-select-option value="active">活跃</a-select-option>
            <a-select-option value="resolved">已解决</a-select-option>
            <a-select-option value="suppressed">已抑制</a-select-option>
          </a-select>
          <a-button
            type="primary"
            :disabled="selectedAlerts.length === 0"
            @click="handleBatchAcknowledge"
          >
            批量确认 ({{ selectedAlerts.length }})
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon>
              <ReloadOutlined />
            </template>
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="alertColumns"
        :data-source="filteredAlerts"
        :row-selection="rowSelection"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        size="small"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'level'">
            <a-tag :color="getAlertLevelColor(record.level)">
              {{ getAlertLevelText(record.level) }}
            </a-tag>
          </template>

          <template v-if="column.key === 'title'">
            <div class="alert-title">
              <a
                type="link"
                @click="handleViewAlert(record)"
              >
                {{ record.title }}
              </a>
              <a-tag v-if="!record.read" color="red" size="small">未读</a-tag>
            </div>
          </template>

          <template v-if="column.key === 'source'">
            <a-tag color="blue">{{ record.source }}</a-tag>
          </template>

          <template v-if="column.key === 'status'">
            <a-tag :color="getAlertStatusColor(record.status)">
              {{ getAlertStatusText(record.status) }}
            </a-tag>
          </template>

          <template v-if="column.key === 'timestamp'">
            {{ formatDateTime(record.timestamp) }}
          </template>

          <template v-if="column.key === 'actions'">
            <a-space>
              <a-button
                v-if="!record.acknowledged"
                type="link"
                size="small"
                @click="handleAcknowledge(record)"
              >
                确认
              </a-button>
              <a-button
                v-if="record.status === 'active'"
                type="link"
                size="small"
                @click="handleResolve(record)"
              >
                解决
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu @click="({ key }) => handleMenuAction(key, record)">
                    <a-menu-item key="view">查看详情</a-menu-item>
                    <a-menu-item key="suppress">抑制</a-menu-item>
                    <a-menu-item key="mark-read">标记已读</a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="delete" danger>删除</a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  更多
                  <DownOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 告警详情弹窗 -->
    <a-modal
      v-model:open="showAlertDetailModal"
      :title="`告警详情 - ${selectedAlert?.title}`"
      :width="800"
      :footer="null"
    >
      <AlertDetail
        v-if="selectedAlert"
        :alert="selectedAlert"
        @acknowledge="handleAcknowledge"
        @resolve="handleResolve"
        @suppress="handleSuppress"
      />
    </a-modal>

    <!-- 确认告警弹窗 -->
    <a-modal
      v-model:open="showAcknowledgeModal"
      title="确认告警"
      @ok="handleConfirmAcknowledge"
    >
      <a-form
        ref="acknowledgeForm"
        :model="acknowledgeForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="确认备注" name="comment">
          <a-textarea
            v-model:value="acknowledgeForm.comment"
            placeholder="请输入确认备注..."
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 解决告警弹窗 -->
    <a-modal
      v-model:open="showResolveModal"
      title="解决告警"
      @ok="handleConfirmResolve"
    >
      <a-form
        ref="resolveForm"
        :model="resolveForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="解决方式" name="resolution">
          <a-select
            v-model:value="resolveForm.resolution"
            placeholder="选择解决方式"
          >
            <a-select-option value="fixed">已修复</a-select-option>
            <a-select-option value="false-positive">误报</a-select-option>
            <a-select-option value="suppressed">已抑制</a-select-option>
            <a-select-option value="other">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="解决备注" name="comment">
          <a-textarea
            v-model:value="resolveForm.comment"
            placeholder="请输入解决备注..."
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 实时告警通知 -->
    <div class="alert-notifications">
      <transition-group name="alert-slide">
        <div
          v-for="notification in alertNotifications"
          :key="notification.id"
          :class="['alert-notification', notification.level]"
        >
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-message">{{ notification.content }}</div>
            <div class="notification-time">{{ formatDateTime(notification.timestamp) }}</div>
          </div>
          <a-button
            type="text"
            size="small"
            @click="dismissNotification(notification.id)"
          >
            <CloseOutlined />
          </a-button>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { useRealtimeStore } from '@/store/modules/realtime';
import { useEventBus } from '@/utils/event-bus';
import { formatDateTime } from '@/utils/format';
import AlertDetail from './modals/AlertDetail.vue';

// Icons
import {
  ExclamationCircleOutlined,
  WarningOutlined,
  InfoCircleOutlined,
  MessageOutlined,
  ReloadOutlined,
  DownOutlined,
  CloseOutlined
} from '@ant-design/icons-vue';

// Props
const props = defineProps({
  // 自动刷新间隔(毫秒)
  refreshInterval: {
    type: Number,
    default: 30000
  },
  // 是否显示实时通知
  showNotifications: {
    type: Boolean,
    default: true
  },
  // 通知显示时间(毫秒)
  notificationDuration: {
    type: Number,
    default: 5000
  },
  // 最大通知数量
  maxNotifications: {
    type: Number,
    default: 5
  }
});

// Emits
const emit = defineEmits([
  'alert-clicked',
  'alert-acknowledged',
  'alert-resolved',
  'alert-suppressed'
]);

// Store
const realtimeStore = useRealtimeStore();
const eventBus = useEventBus('alert-panel');

// 响应式数据
const loading = ref(false);
const filterLevel = ref('');
const filterStatus = ref('');
const selectedAlerts = ref([]);
const showAlertDetailModal = ref(false);
const showAcknowledgeModal = ref(false);
const showResolveModal = ref(false);
const selectedAlert = ref(null);
const alertNotifications = ref([]);

// 表单数据
const acknowledgeForm = ref({
  comment: ''
});

const resolveForm = ref({
  resolution: '',
  comment: ''
});

// 分页配置
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条告警`
});

// 表格列定义
const alertColumns = [
  {
    title: '级别',
    key: 'level',
    width: 80,
    filters: [
      { text: '严重', value: 'critical' },
      { text: '错误', value: 'error' },
      { text: '警告', value: 'warning' },
      { text: '信息', value: 'info' }
    ]
  },
  {
    title: '告警标题',
    key: 'title',
    ellipsis: true
  },
  {
    title: '来源',
    key: 'source',
    width: 120
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    filters: [
      { text: '活跃', value: 'active' },
      { text: '已解决', value: 'resolved' },
      { text: '已抑制', value: 'suppressed' }
    ]
  },
  {
    title: '时间',
    key: 'timestamp',
    width: 160,
    sorter: true
  },
  {
    title: '操作',
    key: 'actions',
    width: 150
  }
];

// 行选择配置
const rowSelection = {
  selectedRowKeys: computed(() => selectedAlerts.value.map(alert => alert.id)),
  onChange: (selectedRowKeys, selectedRows) => {
    selectedAlerts.value = selectedRows;
  }
};

// 计算属性
const alerts = computed(() => realtimeStore.alerts);
const alertStatistics = computed(() => realtimeStore.alertStatistics);

const filteredAlerts = computed(() => {
  let filtered = [...alerts.value];

  if (filterLevel.value) {
    filtered = filtered.filter(alert => alert.level === filterLevel.value);
  }

  if (filterStatus.value) {
    filtered = filtered.filter(alert => alert.status === filterStatus.value);
  }

  // 更新分页总数
  pagination.value.total = filtered.length;

  // 分页
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return filtered.slice(start, end);
});

// 方法
/**
 * 获取告警级别颜色
 */
function getAlertLevelColor(level) {
  const colorMap = {
    critical: 'red',
    error: 'orange',
    warning: 'gold',
    info: 'blue'
  };
  return colorMap[level] || 'default';
}

/**
 * 获取告警级别文本
 */
function getAlertLevelText(level) {
  const textMap = {
    critical: '严重',
    error: '错误',
    warning: '警告',
    info: '信息'
  };
  return textMap[level] || level;
}

/**
 * 获取告警状态颜色
 */
function getAlertStatusColor(status) {
  const colorMap = {
    active: 'red',
    resolved: 'green',
    suppressed: 'gray'
  };
  return colorMap[status] || 'default';
}

/**
 * 获取告警状态文本
 */
function getAlertStatusText(status) {
  const textMap = {
    active: '活跃',
    resolved: '已解决',
    suppressed: '已抑制'
  };
  return textMap[status] || status;
}

/**
 * 处理过滤变化
 */
function handleFilterChange() {
  pagination.value.current = 1;
}

/**
 * 处理表格变化
 */
function handleTableChange(paginationConfig, filters, sorter) {
  pagination.value.current = paginationConfig.current;
  pagination.value.pageSize = paginationConfig.pageSize;

  if (filters.level) {
    filterLevel.value = filters.level[0] || '';
  }

  if (filters.status) {
    filterStatus.value = filters.status[0] || '';
  }
}

/**
 * 处理查看告警详情
 */
function handleViewAlert(alert) {
  selectedAlert.value = alert;
  showAlertDetailModal.value = true;
  emit('alert-clicked', alert);

  // 标记为已读
  if (!alert.read) {
    realtimeStore.markAlertAsRead(alert.id);
  }
}

/**
 * 处理确认告警
 */
function handleAcknowledge(alert) {
  selectedAlert.value = alert;
  acknowledgeForm.value.comment = '';
  showAcknowledgeModal.value = true;
}

/**
 * 处理确认操作
 */
async function handleConfirmAcknowledge() {
  if (!selectedAlert.value) return;

  try {
    await realtimeStore.acknowledgeAlert(
      selectedAlert.value.id,
      acknowledgeForm.value.comment
    );

    showAcknowledgeModal.value = false;
    message.success('告警确认成功');
    emit('alert-acknowledged', selectedAlert.value);
  } catch (error) {
    message.error('告警确认失败: ' + error.message);
  }
}

/**
 * 处理解决告警
 */
function handleResolve(alert) {
  selectedAlert.value = alert;
  resolveForm.value = { resolution: '', comment: '' };
  showResolveModal.value = true;
}

/**
 * 处理确认解决
 */
async function handleConfirmResolve() {
  if (!selectedAlert.value) return;

  try {
    await realtimeStore.resolveAlert(
      selectedAlert.value.id,
      {
        resolution: resolveForm.value.resolution,
        comment: resolveForm.value.comment
      }
    );

    showResolveModal.value = false;
    message.success('告警解决成功');
    emit('alert-resolved', selectedAlert.value);
  } catch (error) {
    message.error('告警解决失败: ' + error.message);
  }
}

/**
 * 处理抑制告警
 */
async function handleSuppress(alert) {
  try {
    // 这里应该调用抑制告警的API
    message.success('告警抑制成功');
    emit('alert-suppressed', alert);
  } catch (error) {
    message.error('告警抑制失败: ' + error.message);
  }
}

/**
 * 处理菜单操作
 */
function handleMenuAction(action, alert) {
  switch (action) {
    case 'view':
      handleViewAlert(alert);
      break;
    case 'suppress':
      handleSuppress(alert);
      break;
    case 'mark-read':
      realtimeStore.markAlertAsRead(alert.id);
      break;
    case 'delete':
      handleDeleteAlert(alert);
      break;
  }
}

/**
 * 处理删除告警
 */
function handleDeleteAlert(alert) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除此告警吗？删除后无法恢复。',
    onOk() {
      // 这里应该调用删除告警的API
      message.success('告警删除成功');
    }
  });
}

/**
 * 处理批量确认
 */
async function handleBatchAcknowledge() {
  if (selectedAlerts.value.length === 0) return;

  Modal.confirm({
    title: '批量确认告警',
    content: `确定要确认选中的 ${selectedAlerts.value.length} 个告警吗？`,
    onOk: async () => {
      try {
        // 这里应该调用批量确认的API
        message.success('批量确认成功');
        selectedAlerts.value = [];
      } catch (error) {
        message.error('批量确认失败: ' + error.message);
      }
    }
  });
}

/**
 * 处理刷新
 */
async function handleRefresh() {
  loading.value = true;
  try {
    // 这里可以添加刷新逻辑
    await new Promise(resolve => setTimeout(resolve, 1000));
    message.success('刷新成功');
  } catch (error) {
    message.error('刷新失败: ' + error.message);
  } finally {
    loading.value = false;
  }
}

/**
 * 显示实时通知
 */
function showAlertNotification(alert) {
  if (!props.showNotifications) return;

  const notification = {
    ...alert,
    id: `${alert.id}_${Date.now()}`,
    timestamp: Date.now()
  };

  alertNotifications.value.unshift(notification);

  // 限制通知数量
  if (alertNotifications.value.length > props.maxNotifications) {
    alertNotifications.value = alertNotifications.value.slice(0, props.maxNotifications);
  }

  // 自动移除通知
  setTimeout(() => {
    dismissNotification(notification.id);
  }, props.notificationDuration);
}

/**
 * 移除通知
 */
function dismissNotification(notificationId) {
  const index = alertNotifications.value.findIndex(n => n.id === notificationId);
  if (index > -1) {
    alertNotifications.value.splice(index, 1);
  }
}

/**
 * 设置事件监听
 */
function setupEventListeners() {
  // 监听新告警
  eventBus.on('alert-received', (alert) => {
    showAlertNotification(alert);
  });
}

// 生命周期
onMounted(() => {
  setupEventListeners();
});

onUnmounted(() => {
  // 清理监听器
  eventBus.off('alert-received');
});

// 暴露方法
defineExpose({
  refresh: handleRefresh,
  acknowledge: handleAcknowledge,
  resolve: handleResolve,
  getSelectedAlerts: () => selectedAlerts.value,
  clearSelection: () => { selectedAlerts.value = []; }
});
</script>

<style lang="less" scoped>
.alert-panel {
  .alert-statistics {
    margin-bottom: 24px;

    .stat-card {
      text-align: center;

      &.critical {
        border-left: 4px solid #ff4d4f;
      }

      &.error {
        border-left: 4px solid #fa8c16;
      }

      &.warning {
        border-left: 4px solid #faad14;
      }

      &.info {
        border-left: 4px solid #1890ff;
      }
    }
  }

  .alert-list {
    .alert-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .alert-notifications {
    position: fixed;
    top: 80px;
    right: 20px;
    z-index: 1000;
    max-width: 400px;

    .alert-notification {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 12px 16px;
      margin-bottom: 8px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border-left: 4px solid #1890ff;

      &.critical {
        border-left-color: #ff4d4f;
      }

      &.error {
        border-left-color: #fa8c16;
      }

      &.warning {
        border-left-color: #faad14;
      }

      &.info {
        border-left-color: #1890ff;
      }

      .notification-content {
        flex: 1;

        .notification-title {
          font-weight: bold;
          margin-bottom: 4px;
        }

        .notification-message {
          font-size: 12px;
          color: #666;
          margin-bottom: 4px;
        }

        .notification-time {
          font-size: 11px;
          color: #999;
        }
      }
    }
  }
}

// 过渡动画
.alert-slide-enter-active,
.alert-slide-leave-active {
  transition: all 0.3s ease;
}

.alert-slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.alert-slide-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.alert-slide-move {
  transition: transform 0.3s ease;
}
</style>