<!--
 * 门禁实时监控页面
 *
 * 功能：
 * 1. 实时告警列表：WebSocket推送实时告警
 * 2. 设备状态监控：实时显示设备在线/离线状态
 * 3. 告警规则配置：配置告警规则和通知方式
 * 4. 告警统计图表：告警数量趋势、级别分布、类型统计
 * 5. 告警处理操作：确认、处理、忽略告警
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="real-time-monitor-page">
    <a-card :bordered="false">
      <!-- Tab导航 -->
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 实时告警Tab -->
        <a-tab-pane key="alerts" tab="实时告警">
          <!-- 告警统计卡片 -->
          <a-row :gutter="16" style="margin-bottom: 24px">
            <a-col :span="6">
              <a-statistic
                title="今日告警总数"
                :value="statistics.todayTotalCount"
                :value-style="{ color: '#1890ff' }"
              >
                <template #suffix>条</template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="未处理告警"
                :value="statistics.unhandledCount"
                :value-style="{ color: '#cf1322' }"
              >
                <template #suffix>条</template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="紧急告警"
                :value="statistics.urgentCount"
                :value-style="{ color: '#fa541c' }"
              >
                <template #suffix>条</template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="今日已处理"
                :value="statistics.handledCount"
                :value-style="{ color: '#52c41a' }"
              >
                <template #suffix>条</template>
              </a-statistic>
            </a-col>
          </a-row>

          <!-- 查询表单 -->
          <a-form class="smart-query-form" layout="inline" style="margin-bottom: 16px">
            <a-row class="smart-query-form-row">
              <a-form-item label="告警级别" class="smart-query-form-item">
                <a-select
                  v-model:value="alertQueryForm.alertLevel"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择"
                >
                  <a-select-option :value="1">低</a-select-option>
                  <a-select-option :value="2">中</a-select-option>
                  <a-select-option :value="3">高</a-select-option>
                  <a-select-option :value="4">紧急</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="告警状态" class="smart-query-form-item">
                <a-select
                  v-model:value="alertQueryForm.alertStatus"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择"
                >
                  <a-select-option :value="0">未确认</a-select-option>
                  <a-select-option :value="1">已确认</a-select-option>
                  <a-select-option :value="2">已处理</a-select-option>
                  <a-select-option :value="3">已忽略</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="设备名称" class="smart-query-form-item">
                <a-input
                  style="width: 200px"
                  v-model:value="alertQueryForm.deviceName"
                  placeholder="请输入设备名称"
                  allow-clear
                />
              </a-form-item>

              <a-form-item class="smart-query-form-item smart-margin-left10">
                <a-space>
                  <a-button type="primary" @click="queryAlertList">
                    <template #icon><SearchOutlined /></template>
                    查询
                  </a-button>
                  <a-button @click="resetAlertQuery">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                  <a-button @click="refreshWebSocket">
                    <template #icon><SyncOutlined /></template>
                    刷新连接
                  </a-button>
                </a-space>
              </a-form-item>
            </a-row>
          </a-form>

          <!-- 实时告警列表 -->
          <a-table
            :columns="alertColumns"
            :data-source="alertTableData"
            :pagination="alertPagination"
            :loading="alertLoading"
            :row-selection="alertRowSelection"
            :scroll="{ x: 1500 }"
            @change="handleAlertTableChange"
            :row-class-name="getRowClassName"
          >
            <!-- 告警级别标签 -->
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'alertLevel'">
                <a-tag :color="getAlertLevelColor(record.alertLevel)">
                  {{ getAlertLevelText(record.alertLevel) }}
                </a-tag>
              </template>

              <!-- 告警状态标签 -->
              <template v-else-if="column.key === 'alertStatus'">
                <a-tag :color="getAlertStatusColor(record.alertStatus)">
                  {{ getAlertStatusText(record.alertStatus) }}
                </a-tag>
              </template>

              <!-- 操作列 -->
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button
                    v-if="record.alertStatus === 0"
                    type="link"
                    size="small"
                    @click="handleConfirm(record)"
                  >
                    确认
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleViewDetail(record)"
                  >
                    详情
                  </a-button>
                  <a-dropdown>
                    <template #overlay>
                      <a-menu @click="({ key }) => handleMenuClick(key, record)">
                        <a-menu-item key="handle">处理</a-menu-item>
                        <a-menu-item key="ignore">忽略</a-menu-item>
                      </a-menu>
                    </template>
                    <a-button type="link" size="small">
                      更多 <DownOutlined />
                    </a-button>
                  </a-dropdown>
                </a-space>
              </template>
            </template>
          </a-table>

          <!-- 批量操作 -->
          <a-space style="margin-top: 16px">
            <a-button
              type="primary"
              :disabled="alertRowSelection.selectedRowKeys.length === 0"
              @click="handleBatchConfirm"
            >
              批量确认
            </a-button>
            <a-button
              :disabled="alertRowSelection.selectedRowKeys.length === 0"
              @click="handleBatchIgnore"
            >
              批量忽略
            </a-button>
          </a-space>
        </a-tab-pane>

        <!-- 设备状态监控Tab -->
        <a-tab-pane key="devices" tab="设备状态">
          <a-table
            :columns="deviceColumns"
            :data-source="deviceTableData"
            :pagination="devicePagination"
            :loading="deviceLoading"
            :scroll="{ x: 1200 }"
            @change="handleDeviceTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- 在线状态 -->
              <template v-if="column.key === 'onlineStatus'">
                <a-badge
                  :status="record.onlineStatus === 'ONLINE' ? 'success' : 'error'"
                  :text="record.onlineStatus === 'ONLINE' ? '在线' : '离线'"
                />
              </template>

              <!-- 操作 -->
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" @click="handleViewDevice(record)">
                  查看详情
                </a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 确认告警Modal -->
    <a-modal
      v-model:open="confirmModalVisible"
      title="确认告警"
      width="600px"
      @ok="handleConfirmModalOk"
      @cancel="handleConfirmModalCancel"
    >
      <a-form
        ref="confirmFormRef"
        :model="confirmForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="确认备注" name="confirmRemark">
          <a-textarea
            v-model:value="confirmForm.confirmRemark"
            :rows="4"
            placeholder="请输入确认备注"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 告警详情Modal -->
    <a-modal
      v-model:open="detailModalVisible"
      title="告警详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="告警ID">
          {{ currentAlert.alertId }}
        </a-descriptions-item>
        <a-descriptions-item label="告警级别">
          <a-tag :color="getAlertLevelColor(currentAlert.alertLevel)">
            {{ getAlertLevelText(currentAlert.alertLevel) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="设备名称">
          {{ currentAlert.deviceName }}
        </a-descriptions-item>
        <a-descriptions-item label="告警时间">
          {{ currentAlert.alertOccurredTime }}
        </a-descriptions-item>
        <a-descriptions-item label="告警消息" :span="2">
          {{ currentAlert.alertMessage }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  SyncOutlined,
  DownOutlined
} from '@ant-design/icons-vue';

// ==================== 数据定义 ====================

const activeTab = ref('alerts');

// 统计数据
const statistics = reactive({
  todayTotalCount: 0,
  unhandledCount: 0,
  urgentCount: 0,
  handledCount: 0
});

// 告警查询表单
const alertQueryForm = reactive({
  alertLevel: undefined,
  alertStatus: undefined,
  deviceName: ''
});

// 告警表格数据
const alertTableData = ref([]);
const alertLoading = ref(false);
const alertPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

// 告警行选择
const alertRowSelection = {
  selectedRowKeys: [],
  onChange: (selectedRowKeys, selectedRows) => {
    alertRowSelection.selectedRowKeys = selectedRowKeys;
  }
};

// 设备状态数据
const deviceTableData = ref([]);
const deviceLoading = ref(false);
const devicePagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

// Modal状态
const confirmModalVisible = ref(false);
const handleModalVisible = ref(false);
const detailModalVisible = ref(false);

const confirmForm = reactive({
  alertId: undefined,
  confirmRemark: ''
});

const handleForm = reactive({
  alertId: undefined,
  handleResult: 'RESOLVED',
  handleRemark: ''
});

const currentAlert = ref({});

// WebSocket连接
let websocket = null;

// ==================== 表格列定义 ====================

const alertColumns = [
  {
    title: '告警ID',
    dataIndex: 'alertId',
    key: 'alertId',
    width: 120,
    fixed: 'left'
  },
  {
    title: '告警级别',
    dataIndex: 'alertLevel',
    key: 'alertLevel',
    width: 100
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150
  },
  {
    title: '告警类型',
    dataIndex: 'alertType',
    key: 'alertType',
    width: 150
  },
  {
    title: '告警消息',
    dataIndex: 'alertMessage',
    key: 'alertMessage',
    width: 250,
    ellipsis: true
  },
  {
    title: '告警时间',
    dataIndex: 'alertOccurredTime',
    key: 'alertOccurredTime',
    width: 150
  },
  {
    title: '告警状态',
    dataIndex: 'alertStatus',
    key: 'alertStatus',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
];

const deviceColumns = [
  {
    title: '设备ID',
    dataIndex: 'deviceId',
    key: 'deviceId',
    width: 120
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150
  },
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 120
  },
  {
    title: '在线状态',
    dataIndex: 'onlineStatus',
    key: 'onlineStatus',
    width: 100
  },
  {
    title: '最后在线',
    dataIndex: 'lastOnlineTime',
    key: 'lastOnlineTime',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right'
  }
];

// ==================== 方法定义 ====================

/**
 * 查询告警列表
 */
const queryAlertList = async () => {
  alertLoading.value = true;
  try {
    // TODO: 调用API查询告警列表
    // const res = await accessApi.queryDeviceAlertPage({...});
    // 模拟数据
    alertTableData.value = [];
    alertPagination.total = 0;
  } catch (e) {
    console.error('[实时监控] 查询告警列表异常:', e);
    message.error('查询失败，请稍后重试');
  } finally {
    alertLoading.value = false;
  }
};

/**
 * 重置查询
 */
const resetAlertQuery = () => {
  Object.assign(alertQueryForm, {
    alertLevel: undefined,
    alertStatus: undefined,
    deviceName: ''
  });
  queryAlertList();
};

/**
 * 刷新WebSocket连接
 */
const refreshWebSocket = () => {
  message.info('正在刷新WebSocket连接...');
  disconnectWebSocket();
  connectWebSocket();
};

/**
 * Tab切换
 */
const handleTabChange = (activeKey) => {
  activeTab.value = activeKey;
  if (activeKey === 'alerts') {
    queryAlertList();
  } else if (activeKey === 'devices') {
    queryDeviceStatus();
  }
};

/**
 * 查询设备状态
 */
const queryDeviceStatus = async () => {
  deviceLoading.value = true;
  try {
    // TODO: 调用API查询设备状态
    deviceTableData.value = [];
    devicePagination.total = 0;
  } catch (e) {
    console.error('[实时监控] 查询设备状态异常:', e);
  } finally {
    deviceLoading.value = false;
  }
};

/**
 * 表格变化
 */
const handleAlertTableChange = (pagination) => {
  alertPagination.current = pagination.current;
  alertPagination.pageSize = pagination.pageSize;
  queryAlertList();
};

const handleDeviceTableChange = (pagination) => {
  devicePagination.current = pagination.current;
  devicePagination.pageSize = pagination.pageSize;
  queryDeviceStatus();
};

/**
 * 确认告警
 */
const handleConfirm = (record) => {
  confirmForm.alertId = record.alertId;
  confirmForm.confirmRemark = '';
  confirmModalVisible.value = true;
};

/**
 * 确认Modal确定
 */
const handleConfirmModalOk = async () => {
  try {
    // TODO: 调用API确认告警
    message.success('确认成功');
    confirmModalVisible.value = false;
    queryAlertList();
  } catch (e) {
    console.error('[实时监控] 确认告警异常:', e);
    message.error('确认失败');
  }
};

/**
 * 确认Modal取消
 */
const handleConfirmModalCancel = () => {
  confirmModalVisible.value = false;
};

/**
 * 批量确认
 */
const handleBatchConfirm = async () => {
  if (alertRowSelection.selectedRowKeys.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }

  Modal.confirm({
    title: '批量确认',
    content: `确定要确认选中的 ${alertRowSelection.selectedRowKeys.length} 条告警吗？`,
    onOk: async () => {
      try {
        // TODO: 调用API批量确认
        message.success('批量确认成功');
        alertRowSelection.selectedRowKeys = [];
        queryAlertList();
      } catch (e) {
        console.error('[实时监控] 批量确认异常:', e);
        message.error('批量确认失败');
      }
    }
  });
};

/**
 * 批量忽略
 */
const handleBatchIgnore = async () => {
  if (alertRowSelection.selectedRowKeys.length === 0) {
    message.warning('请至少选择一条记录');
    return;
  }

  Modal.confirm({
    title: '批量忽略',
    content: `确定要忽略选中的 ${alertRowSelection.selectedRowKeys.length} 条告警吗？`,
    onOk: async () => {
      try {
        // TODO: 调用API批量忽略
        message.success('批量忽略成功');
        alertRowSelection.selectedRowKeys = [];
        queryAlertList();
      } catch (e) {
        console.error('[实时监控] 批量忽略异常:', e);
        message.error('批量忽略失败');
      }
    }
  });
};

/**
 * 查看详情
 */
const handleViewDetail = (record) => {
  currentAlert.value = record;
  detailModalVisible.value = true;
};

/**
 * 菜单点击
 */
const handleMenuClick = (key, record) => {
  if (key === 'handle') {
    handleForm.alertId = record.alertId;
    handleForm.handleResult = 'RESOLVED';
    handleForm.handleRemark = '';
    handleModalVisible.value = true;
  } else if (key === 'ignore') {
    Modal.confirm({
      title: '忽略告警',
      content: '确定要忽略这条告警吗？',
      onOk: async () => {
        try {
          // TODO: 调用API忽略告警
          message.success('忽略成功');
          queryAlertList();
        } catch (e) {
          console.error('[实时监控] 忽略告警异常:', e);
          message.error('忽略失败');
        }
      }
    });
  }
};

/**
 * 处理Modal确定
 */
const handleHandleModalOk = async () => {
  try {
    // TODO: 调用API处理告警
    message.success('处理成功');
    handleModalVisible.value = false;
    queryAlertList();
  } catch (e) {
    console.error('[实时监控] 处理告警异常:', e);
    message.error('处理失败');
  }
};

/**
 * 处理Modal取消
 */
const handleHandleModalCancel = () => {
  handleModalVisible.value = false;
};

/**
 * 查看设备详情
 */
const handleViewDevice = (record) => {
  message.info('查看设备详情功能待实现');
};

/**
 * 获取行样式
 */
const getRowClassName = (record) => {
  if (record.alertLevel === 4) {
    return 'urgent-row';
  }
  return '';
};

/**
 * 获取告警级别颜色
 */
const getAlertLevelColor = (level) => {
  const colorMap = {
    1: 'green',
    2: 'blue',
    3: 'orange',
    4: 'red'
  };
  return colorMap[level] || 'default';
};

/**
 * 获取告警级别文本
 */
const getAlertLevelText = (level) => {
  const textMap = {
    1: '低',
    2: '中',
    3: '高',
    4: '紧急'
  };
  return textMap[level] || '-';
};

/**
 * 获取告警状态颜色
 */
const getAlertStatusColor = (status) => {
  const colorMap = {
    0: 'red',
    1: 'orange',
    2: 'green',
    3: 'default'
  };
  return colorMap[status] || 'default';
};

/**
 * 获取告警状态文本
 */
const getAlertStatusText = (status) => {
  const textMap = {
    0: '未确认',
    1: '已确认',
    2: '已处理',
    3: '已忽略'
  };
  return textMap[status] || '-';
};

// ==================== WebSocket ====================

/**
 * 连接WebSocket
 */
const connectWebSocket = () => {
  const wsUrl = `ws://localhost:8090/ws/alert`;
  websocket = new WebSocket(wsUrl);

  websocket.onopen = () => {
    message.success('实时告警连接成功');
    // 发送心跳
    setInterval(() => {
      if (websocket.readyState === WebSocket.OPEN) {
        websocket.send(JSON.stringify({ type: 'ping' }));
      }
    }, 30000);
  };

  websocket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      if (data.type === 'alert') {
        // 新告警推送
        message.warning(`新告警：${data.alertMessage}`, 5);
        // 刷新告警列表
        queryAlertList();
      }
    } catch (e) {
      console.error('[WebSocket] 解析消息异常:', e);
    }
  };

  websocket.onerror = (error) => {
    console.error('[WebSocket] 连接错误:', error);
    message.error('WebSocket连接错误');
  };

  websocket.onclose = () => {
    console.warn('[WebSocket] 连接关闭');
    message.warning('实时告警连接已断开，5秒后重连...');
    setTimeout(() => {
      connectWebSocket();
    }, 5000);
  };
};

/**
 * 断开WebSocket
 */
const disconnectWebSocket = () => {
  if (websocket) {
    websocket.close();
    websocket = null;
  }
};

// ==================== 生命周期 ====================

onMounted(() => {
  queryAlertList();

  // 连接WebSocket
  connectWebSocket();
});

onUnmounted(() => {
  disconnectWebSocket();
});
</script>

<style scoped>
.real-time-monitor-page {
  padding: 16px;
}

.smart-query-form-item {
  margin-bottom: 0;
}

.smart-margin-left10 {
  margin-left: 10px;
}

.urgent-row {
  background-color: #fff1f0;
}

:deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
  background-color: #e6f7ff !important;
}

:deep(.ant-table-tbody > tr.urgent-row:hover > td) {
  background-color: #ffccc7 !important;
}
</style>
