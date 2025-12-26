<!--
 * 实时告警监控页面
 *
 * 功能：
 * 1. 实时告警展示：WebSocket实时推送告警消息
 * 2. 告警筛选：按类型、级别、状态筛选
 * 3. 告警操作：确认、处理、忽略
 * 4. 统计信息：告警数量、趋势图表
 * 5. WebSocket连接状态监控
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="alert-monitoring-page">
    <a-card :bordered="false">
      <!-- 顶部统计卡片 -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="6">
          <a-statistic
            title="今日告警"
            :value="statistics.todayCount || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <AlertOutlined style="font-size: 20px" />
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="未确认"
            :value="statistics.unconfirmedCount || 0"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #prefix>
              <ClockCircleOutlined style="font-size: 20px" />
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="已处理"
            :value="statistics.handledCount || 0"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined style="font-size: 20px" />
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="6">
          <a-card size="small" :body-style="{ padding: '8px 12px' }">
            <a-space>
              <span style="font-size: 12px; color: rgba(0, 0, 0, 0.45)">WebSocket:</span>
              <a-tag :color="wsConnected ? 'green' : 'red'">
                {{ wsConnected ? '已连接' : '未连接' }}
              </a-tag>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <!-- 选项卡 -->
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 实时告警 -->
        <a-tab-pane key="realtime" tab="实时告警">
          <!-- 筛选表单 -->
          <a-form class="smart-query-form" layout="inline" style="margin-bottom: 16px">
            <a-row class="smart-query-form-row">
              <a-form-item label="告警类型" class="smart-query-form-item">
                <a-select
                  v-model:value="alertQueryForm.ruleType"
                  style="width: 150px"
                  :allow-clear="true"
                  placeholder="请选择类型"
                >
                  <a-select-option value="DEVICE_OFFLINE">设备离线</a-select-option>
                  <a-select-option value="DEVICE_FAULT">设备故障</a-select-option>
                  <a-select-option value="ANTI_PASSBACK">反潜回</a-select-option>
                  <a-select-option value="INTERLOCK">互锁冲突</a-select-option>
                  <a-select-option value="MULTI_PERSON">多人验证</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="告警级别" class="smart-query-form-item">
                <a-select
                  v-model:value="alertQueryForm.alertLevel"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择级别"
                >
                  <a-select-option value="CRITICAL">紧急</a-select-option>
                  <a-select-option value="HIGH">重要</a-select-option>
                  <a-select-option value="MEDIUM">中等</a-select-option>
                  <a-select-option value="LOW">低级</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="处理状态" class="smart-query-form-item">
                <a-select
                  v-model:value="alertQueryForm.alertStatus"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择状态"
                >
                  <a-select-option :value="1">待处理</a-select-option>
                  <a-select-option :value="2">已确认</a-select-option>
                  <a-select-option :value="3">已处理</a-select-option>
                  <a-select-option :value="4">已忽略</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item class="smart-query-form-item smart-margin-left10">
                <a-space>
                  <a-button type="primary" @click="queryAlertList">
                    <template #icon><SearchOutlined /></template>
                    查询
                  </a-button>
                  <a-button @click="resetQuery">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-row>
          </a-form>

          <!-- 操作按钮 -->
          <a-space style="margin-bottom: 16px">
            <a-button
              type="primary"
              :disabled="selectedRowKeys.length === 0"
              @click="handleBatchConfirm"
            >
              <template #icon><CheckOutlined /></template>
              批量确认 ({{ selectedRowKeys.length }})
            </a-button>
            <a-button @click="queryAlertList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>

          <!-- 实时告警表格 -->
          <a-table
            :columns="alertColumns"
            :data-source="alertTableData"
            :pagination="alertPagination"
            :loading="alertLoading"
            :row-selection="rowSelection"
            row-key="alertId"
            @change="handleAlertTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'alertLevel'">
                <a-tag :color="getAlertLevelColor(record.alertLevel)">
                  {{ getAlertLevelText(record.alertLevel) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'alertStatus'">
                <a-tag :color="getAlertStatusColor(record.alertStatus)">
                  {{ getAlertStatusText(record.alertStatus) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'ruleType'">
                <a-tag color="blue">
                  {{ getRuleTypeText(record.ruleType) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'alertTime'">
                {{ formatTime(record.alertTime) }}
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleViewAlert(record)"
                  >
                    详情
                  </a-button>
                  <a-dropdown>
                    <a-button type="link" size="small">
                      处理 <DownOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item
                          @click="handleConfirmAlert(record)"
                          v-if="record.alertStatus === 1"
                        >
                          <CheckOutlined /> 确认告警
                        </a-menu-item>
                        <a-menu-item
                          @click="handleHandleAlert(record)"
                          v-if="record.alertStatus === 2"
                        >
                          <ToolOutlined /> 处理告警
                        </a-menu-item>
                        <a-menu-item
                          @click="handleIgnoreAlert(record)"
                          v-if="record.alertStatus !== 4"
                        >
                          <StopOutlined /> 忽略告警
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 告警统计 -->
        <a-tab-pane key="statistics" tab="告警统计">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-card title="告警趋势" :bordered="false">
                <div ref="trendChartRef" style="height: 300px"></div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card title="告警类型分布" :bordered="false">
                <div ref="typeChartRef" style="height: 300px"></div>
              </a-card>
            </a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 告警详情对话框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="告警详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions
        v-if="currentAlert"
        :column="2"
        bordered
        size="small"
      >
        <a-descriptions-item label="告警ID">
          {{ currentAlert.alertId }}
        </a-descriptions-item>
        <a-descriptions-item label="告警级别">
          <a-tag :color="getAlertLevelColor(currentAlert.alertLevel)">
            {{ getAlertLevelText(currentAlert.alertLevel) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="告警类型">
          <a-tag color="blue">
            {{ getRuleTypeText(currentAlert.ruleType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="处理状态">
          <a-tag :color="getAlertStatusColor(currentAlert.alertStatus)">
            {{ getAlertStatusText(currentAlert.alertStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="设备ID">
          {{ currentAlert.deviceId }}
        </a-descriptions-item>
        <a-descriptions-item label="告警时间">
          {{ formatTime(currentAlert.alertTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="告警消息" :span="2">
          {{ currentAlert.alertMessage }}
        </a-descriptions-item>
        <a-descriptions-item label="告警详情" :span="2">
          <pre style="max-height: 200px; overflow: auto">{{
            JSON.stringify(currentAlert.alertDetails, null, 2)
          }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="确认时间" v-if="currentAlert.confirmTime">
          {{ formatTime(currentAlert.confirmTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="确认人" v-if="currentAlert.confirmBy">
          {{ currentAlert.confirmBy }}
        </a-descriptions-item>
        <a-descriptions-item label="处理时间" v-if="currentAlert.handleTime">
          {{ formatTime(currentAlert.handleTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="处理人" v-if="currentAlert.handleBy">
          {{ currentAlert.handleBy }}
        </a-descriptions-item>
        <a-descriptions-item label="处理备注" :span="2" v-if="currentAlert.handleRemark">
          {{ currentAlert.handleRemark }}
        </a-descriptions-item>
      </a-descriptions>

      <div style="margin-top: 16px; text-align: right">
        <a-space>
          <a-button @click="detailModalVisible = false">关闭</a-button>
          <a-button
            type="primary"
            v-if="currentAlert && currentAlert.alertStatus === 1"
            @click="handleConfirmFromModal"
          >
            确认告警
          </a-button>
          <a-button
            type="primary"
            v-if="currentAlert && currentAlert.alertStatus === 2"
            @click="handleHandleFromModal"
          >
            处理告警
          </a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 处理告警对话框 -->
    <a-modal
      v-model:open="handleModalVisible"
      title="处理告警"
      width="600px"
      @ok="handleHandleSubmit"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="告警ID">
          <a-input v-model:value="handleForm.alertId" disabled />
        </a-form-item>
        <a-form-item label="处理备注" required>
          <a-textarea
            v-model:value="handleForm.handleRemark"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import {
  AlertOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  SearchOutlined,
  ReloadOutlined,
  CheckOutlined,
  ToolOutlined,
  StopOutlined,
  DownOutlined,
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import * as alertApi from '/@/api/business/access/alert-api';
import websocketService from '/@/utils/websocket';
import dayjs from 'dayjs';

// ==================== 状态管理 ====================

const activeTab = ref('realtime');
const wsConnected = ref(false);

// 统计信息
const statistics = reactive({
  todayCount: 0,
  unconfirmedCount: 0,
  handledCount: 0,
  totalCount: 0,
});

// 告警列表
const alertTableData = ref([]);
const alertLoading = ref(false);
const selectedRowKeys = ref([]);

const alertQueryForm = reactive({
  ruleType: undefined,
  alertLevel: undefined,
  alertStatus: undefined,
  pageNum: 1,
  pageSize: 10,
});

const alertPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 行选择
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
};

// 表格列定义
const alertColumns = [
  {
    title: '告警ID',
    dataIndex: 'alertId',
    key: 'alertId',
    width: 100,
  },
  {
    title: '告警级别',
    dataIndex: 'alertLevel',
    key: 'alertLevel',
    width: 100,
  },
  {
    title: '告警类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 120,
  },
  {
    title: '设备ID',
    dataIndex: 'deviceId',
    key: 'deviceId',
    width: 100,
  },
  {
    title: '告警消息',
    dataIndex: 'alertMessage',
    key: 'alertMessage',
    ellipsis: true,
  },
  {
    title: '告警时间',
    dataIndex: 'alertTime',
    key: 'alertTime',
    width: 160,
  },
  {
    title: '处理状态',
    dataIndex: 'alertStatus',
    key: 'alertStatus',
    width: 100,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
  },
];

// 当前告警
const currentAlert = ref(null);
const detailModalVisible = ref(false);

// 处理表单
const handleModalVisible = ref(false);
const handleForm = reactive({
  alertId: null,
  handleRemark: '',
});

// ==================== WebSocket 集成 ====================

/**
 * 初始化 WebSocket 连接
 */
const initWebSocket = () => {
  const wsUrl = `ws://${window.location.hostname}:8090/ws/alert`;
  websocketService.connect(wsUrl, {
    maxReconnectAttempts: 10,
    reconnectInterval: 3000,
    heartbeatInterval: 30000,
    onMessage: handleWebSocketMessage,
    onStatusChange: handleWebSocketStatusChange,
  });

  // 订阅告警推送
  websocketService.on('alert', handleAlertPush);
  websocketService.send({ type: 'subscribe', topic: 'alert' });
};

/**
 * 处理 WebSocket 消息
 */
const handleWebSocketMessage = (data) => {
  console.log('[WebSocket] 收到消息:', data);
};

/**
 * 处理告警推送
 */
const handleAlertPush = (data) => {
  console.log('[告警推送] 收到新告警:', data);

  // 显示通知
  notification.warning({
    message: '新告警',
    description: data.alertMessage || '设备告警',
    duration: 0,
  });

  // 刷新列表和统计
  queryAlertList();
  loadStatistics();
};

/**
 * 处理 WebSocket 状态变化
 */
const handleWebSocketStatusChange = (status) => {
  console.log('[WebSocket] 状态变化:', status);
  wsConnected.value = status === 1; // 1 = OPEN
};

/**
 * 关闭 WebSocket 连接
 */
const closeWebSocket = () => {
  websocketService.send({ type: 'unsubscribe', topic: 'alert' });
  websocketService.off('alert', handleAlertPush);
  websocketService.close();
};

// ==================== API 调用 ====================

/**
 * 查询告警列表
 */
const queryAlertList = async () => {
  try {
    alertLoading.value = true;
    const res = await alertApi.queryAlerts(alertQueryForm);
    if (res.data && res.code === 200) {
      alertTableData.value = res.data.list || [];
      alertPagination.total = res.data.total || 0;
    }
  } catch (error) {
    console.error('查询告警列表失败:', error);
    message.error('查询告警列表失败');
  } finally {
    alertLoading.value = false;
  }
};

/**
 * 加载统计信息
 */
const loadStatistics = async () => {
  try {
    const res = await alertApi.getStatistics();
    if (res.data && res.code === 200) {
      Object.assign(statistics, res.data);
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
  }
};

/**
 * 查询今日告警数量
 */
const loadTodayCount = async () => {
  try {
    const res = await alertApi.getTodayAlertCount();
    if (res.data && res.code === 200) {
      statistics.todayCount = res.data;
    }
  } catch (error) {
    console.error('查询今日告警数量失败:', error);
  }
};

// ==================== 告警操作 ====================

/**
 * 查看告警详情
 */
const handleViewAlert = async (record) => {
  try {
    const res = await alertApi.getAlertDetail(record.alertId);
    if (res.data && res.code === 200) {
      currentAlert.value = res.data;
      detailModalVisible.value = true;
    }
  } catch (error) {
    console.error('查询告警详情失败:', error);
    message.error('查询告警详情失败');
  }
};

/**
 * 确认告警
 */
const handleConfirmAlert = async (record) => {
  try {
    const res = await alertApi.handleAlert({
      alertId: record.alertId,
      action: 'confirm',
    });
    if (res.code === 200) {
      message.success('确认成功');
      queryAlertList();
      loadStatistics();
    }
  } catch (error) {
    console.error('确认告警失败:', error);
    message.error('确认告警失败');
  }
};

/**
 * 处理告警（打开对话框）
 */
const handleHandleAlert = (record) => {
  handleForm.alertId = record.alertId;
  handleForm.handleRemark = '';
  handleModalVisible.value = true;
};

/**
 * 提交处理
 */
const handleHandleSubmit = async () => {
  if (!handleForm.handleRemark) {
    message.warning('请输入处理备注');
    return;
  }

  try {
    const res = await alertApi.handleAlert({
      alertId: handleForm.alertId,
      action: 'handle',
      handleRemark: handleForm.handleRemark,
    });
    if (res.code === 200) {
      message.success('处理成功');
      handleModalVisible.value = false;
      queryAlertList();
      loadStatistics();
    }
  } catch (error) {
    console.error('处理告警失败:', error);
    message.error('处理告警失败');
  }
};

/**
 * 忽略告警
 */
const handleIgnoreAlert = async (record) => {
  Modal.confirm({
    title: '确认忽略',
    content: '确定要忽略此告警吗？',
    onOk: async () => {
      try {
        const res = await alertApi.handleAlert({
          alertId: record.alertId,
          action: 'ignore',
        });
        if (res.code === 200) {
          message.success('忽略成功');
          queryAlertList();
          loadStatistics();
        }
      } catch (error) {
        console.error('忽略告警失败:', error);
        message.error('忽略告警失败');
      }
    },
  });
};

/**
 * 批量确认
 */
const handleBatchConfirm = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要确认的告警');
    return;
  }

  Modal.confirm({
    title: '批量确认',
    content: `确定要确认 ${selectedRowKeys.value.length} 条告警吗？`,
    onOk: async () => {
      try {
        const res = await alertApi.batchConfirm({
          alertIds: selectedRowKeys.value,
        });
        if (res.code === 200) {
          message.success('批量确认成功');
          selectedRowKeys.value = [];
          queryAlertList();
          loadStatistics();
        }
      } catch (error) {
        console.error('批量确认失败:', error);
        message.error('批量确认失败');
      }
    },
  });
};

/**
 * 从详情对话框确认告警
 */
const handleConfirmFromModal = () => {
  handleConfirmAlert(currentAlert.value);
  detailModalVisible.value = false;
};

/**
 * 从详情对话框处理告警
 */
const handleHandleFromModal = () => {
  detailModalVisible.value = false;
  handleHandleAlert(currentAlert.value);
};

// ==================== 表格事件 ====================

/**
 * 表格变化事件
 */
const handleAlertTableChange = (pagination) => {
  alertQueryForm.pageNum = pagination.current;
  alertQueryForm.pageSize = pagination.pageSize;
  alertPagination.current = pagination.current;
  alertPagination.pageSize = pagination.pageSize;
  queryAlertList();
};

/**
 * 标签页变化事件
 */
const handleTabChange = (key) => {
  if (key === 'statistics') {
    loadStatistics();
  }
};

/**
 * 重置查询条件
 */
const resetQuery = () => {
  alertQueryForm.ruleType = undefined;
  alertQueryForm.alertLevel = undefined;
  alertQueryForm.alertStatus = undefined;
  alertQueryForm.pageNum = 1;
  queryAlertList();
};

// ==================== 工具方法 ====================

/**
 * 格式化时间
 */
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

/**
 * 获取告警级别颜色
 */
const getAlertLevelColor = (level) => {
  const colorMap = {
    CRITICAL: 'red',
    HIGH: 'orange',
    MEDIUM: 'gold',
    LOW: 'blue',
  };
  return colorMap[level] || 'default';
};

/**
 * 获取告警级别文本
 */
const getAlertLevelText = (level) => {
  const textMap = {
    CRITICAL: '紧急',
    HIGH: '重要',
    MEDIUM: '中等',
    LOW: '低级',
  };
  return textMap[level] || level;
};

/**
 * 获取告警状态颜色
 */
const getAlertStatusColor = (status) => {
  const colorMap = {
    1: 'red', // 待处理
    2: 'orange', // 已确认
    3: 'green', // 已处理
    4: 'default', // 已忽略
  };
  return colorMap[status] || 'default';
};

/**
 * 获取告警状态文本
 */
const getAlertStatusText = (status) => {
  const textMap = {
    1: '待处理',
    2: '已确认',
    3: '已处理',
    4: '已忽略',
  };
  return textMap[status] || status;
};

/**
 * 获取规则类型文本
 */
const getRuleTypeText = (type) => {
  const textMap = {
    DEVICE_OFFLINE: '设备离线',
    DEVICE_FAULT: '设备故障',
    ANTI_PASSBACK: '反潜回',
    INTERLOCK: '互锁冲突',
    MULTI_PERSON: '多人验证',
  };
  return textMap[type] || type;
};

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[告警监控] 页面初始化');

  // 加载初始数据
  queryAlertList();
  loadStatistics();
  loadTodayCount();

  // 初始化 WebSocket
  initWebSocket();
});

onUnmounted(() => {
  console.log('[告警监控] 页面销毁');

  // 关闭 WebSocket
  closeWebSocket();
});
</script>

<style scoped>
.alert-monitoring-page {
  padding: 16px;
}

.smart-query-form {
  background: #fafafa;
  padding: 16px;
  border-radius: 4px;
}

.smart-query-form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.smart-query-form-item {
  margin-bottom: 0;
}

.smart-margin-left10 {
  margin-left: auto;
}
</style>
