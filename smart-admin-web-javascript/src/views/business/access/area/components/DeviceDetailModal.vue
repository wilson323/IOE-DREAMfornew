<template>
  <a-modal
    v-model:open="visible"
    title="设备详情"
    width="800px"
    :footer="null"
    @cancel="handleCancel"
  >
    <a-descriptions :column="2" bordered v-if="deviceInfo">
      <a-descriptions-item label="设备名称">{{ deviceInfo.deviceName }}</a-descriptions-item>
      <a-descriptions-item label="设备编码">{{ deviceInfo.deviceCode }}</a-descriptions-item>
      <a-descriptions-item label="设备类型">
        <a-tag :color="getDeviceTypeColor(deviceInfo.deviceType)">
          {{ getDeviceTypeLabel(deviceInfo.deviceType) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="设备状态">
        <a-badge :status="deviceInfo.status === 1 ? 'success' : 'default'" :text="deviceInfo.status === 1 ? '启用' : '禁用'" />
      </a-descriptions-item>
      <a-descriptions-item label="IP地址">{{ deviceInfo.ipAddress }}</a-descriptions-item>
      <a-descriptions-item label="MAC地址">{{ deviceInfo.macAddress }}</a-descriptions-item>
      <a-descriptions-item label="厂商">{{ deviceInfo.manufacturer || '-' }}</a-descriptions-item>
      <a-descriptions-item label="型号">{{ deviceInfo.model || '-' }}</a-descriptions-item>
      <a-descriptions-item label="固件版本">{{ deviceInfo.firmwareVersion || '-' }}</a-descriptions-item>
      <a-descriptions-item label="安装位置">{{ deviceInfo.installLocation || '-' }}</a-descriptions-item>
    </a-descriptions>

    <a-divider>实时状态</a-divider>

    <a-row :gutter="16">
      <a-col :span="8">
        <a-statistic title="在线状态" :value="deviceInfo.onlineStatus ? '在线' : '离线'" :value-style="{ color: deviceInfo.onlineStatus ? '#3f8600' : '#cf1322' }">
          <template #prefix>
            <WifiOutlined v-if="deviceInfo.onlineStatus" />
            <DisconnectOutlined v-else />
          </template>
        </a-statistic>
      </a-col>
      <a-col :span="8">
        <a-statistic title="通信状态" :value="deviceInfo.communicationStatus ? '正常' : '异常'" :value-style="{ color: deviceInfo.communicationStatus ? '#3f8600' : '#cf1322' }">
          <template #prefix>
            <CheckCircleOutlined v-if="deviceInfo.communicationStatus" />
            <ExclamationCircleOutlined v-else />
          </template>
        </a-statistic>
      </a-col>
      <a-col :span="8">
        <a-statistic title="最后通信" :value="formatDateTime(deviceInfo.lastCommunication)" :value-style="{ color: '#666' }" />
      </a-col>
    </a-row>

    <a-divider>设备配置</a-divider>

    <a-descriptions :column="1" bordered>
      <a-descriptions-item label="网络配置">
        <a-space direction="vertical" size="small">
          <div>网关: {{ deviceInfo.gateway || '-' }}</div>
          <div>子网掩码: {{ deviceInfo.subnetMask || '-' }}</div>
          <div>DNS服务器: {{ deviceInfo.dnsServer || '-' }}</div>
        </a-space>
      </a-descriptions-item>
      <a-descriptions-item label="安全配置">
        <a-space direction="vertical" size="small">
          <div>加密方式: {{ deviceInfo.encryptionType || 'WPA2' }}</div>
          <div>访问密钥: {{ deviceInfo.accessKey ? '已配置' : '未配置' }}</div>
          <div>证书: {{ deviceInfo.certificate ? '已安装' : '未安装' }}</div>
        </a-descriptions>
      </a-descriptions-item>
      <a-descriptions-item label="功能配置">
        <a-space direction="vertical" size="small">
          <div>读卡功能: {{ deviceInfo.cardReaderEnabled ? '启用' : '禁用' }}</div>
          <div>人脸识别: {{ deviceInfo.faceRecognitionEnabled ? '启用' : '禁用' }}</div>
          <div>指纹识别: {{ deviceInfo.fingerprintEnabled ? '启用' : '禁用' }}</div>
          <div>密码键盘: {{ deviceInfo.keypadEnabled ? '启用' : '禁用' }}</div>
          <div>出门按钮: {{ deviceInfo.exitButtonEnabled ? '启用' : '禁用' }}</div>
        </a-space>
      </a-descriptions-item>
    </a-descriptions>

    <a-divider>操作日志</a-divider>

    <a-table
      :columns="logColumns"
      :data-source="operationLogs"
      :loading="logLoading"
      :pagination="logPagination"
      size="small"
      :row-key="record => record.id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'operationType'">
          <a-tag :color="getOperationTypeColor(record.operationType)">
            {{ getOperationTypeLabel(record.operationType) }}
          </a-tag>
        </template>

        <template v-else-if="column.key === 'result'">
          <a-tag :color="record.success ? 'success' : 'error'">
            {{ record.success ? '成功' : '失败' }}
          </a-tag>
        </template>

        <template v-else-if="column.key === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  WifiOutlined,
  DisconnectOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue';
import { formatDateTime } from '/@/utils/format.js';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  deviceInfo: {
    type: Object,
    default: () => ({})
  }
});

// Emits
const emit = defineEmits(['update:visible']);

// 响应式数据
const logLoading = ref(false);
const operationLogs = ref([]);

// 分页配置
const logPagination = reactive({
  current: 1,
  pageSize: 5,
  total: 0,
  showSizeChanger: false,
  showQuickJumper: false
});

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

// 操作日志列定义
const logColumns = [
  {
    title: '操作类型',
    dataIndex: 'operationType',
    key: 'operationType',
    width: 100
  },
  {
    title: '操作描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '执行结果',
    dataIndex: 'result',
    key: 'result',
    width: 80,
    align: 'center'
  },
  {
    title: '操作时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150
  },
  {
    title: '操作人',
    dataIndex: 'operatorName',
    key: 'operatorName',
    width: 100
  }
];

// 获取设备类型标签
const getDeviceTypeLabel = (type) => {
  const labelMap = {
    'DOOR_CONTROLLER': '门禁控制器',
    'CARD_READER': '读卡器',
    'FACE_RECOGNITION': '人脸识别',
    'FINGERPRINT': '指纹识别',
    'PASSWORD_KEYPAD': '密码键盘',
    'EXIT_BUTTON': '出门按钮'
  };
  return labelMap[type] || '未知';
};

// 获取设备类型颜色
const getDeviceTypeColor = (type) => {
  const colorMap = {
    'DOOR_CONTROLLER': 'blue',
    'CARD_READER': 'green',
    'FACE_RECOGNITION': 'orange',
    'FINGERPRINT': 'purple',
    'PASSWORD_KEYPAD': 'cyan',
    'EXIT_BUTTON': 'red'
  };
  return colorMap[type] || 'default';
};

// 获取操作类型标签
const getOperationTypeLabel = (type) => {
  const labelMap = {
    'REMOTE_OPEN': '远程开门',
    'RESTART': '重启设备',
    'CONFIG_UPDATE': '配置更新',
    'STATUS_CHANGE': '状态变更',
    'TEST_CONNECTION': '连接测试'
  };
  return labelMap[type] || '未知';
};

// 获取操作类型颜色
const getOperationTypeColor = (type) => {
  const colorMap = {
    'REMOTE_OPEN': 'green',
    'RESTART': 'orange',
    'CONFIG_UPDATE': 'blue',
    'STATUS_CHANGE': 'purple',
    'TEST_CONNECTION': 'cyan'
  };
  return colorMap[type] || 'default';
};

// 加载操作日志
const loadOperationLogs = async () => {
  if (!props.deviceInfo?.deviceId) return;

  logLoading.value = true;
  try {
    // 这里应该调用获取设备操作日志的API
    // const response = await accessDeviceApi.getDeviceOperationLogs(props.deviceInfo.deviceId);

    // 模拟数据
    operationLogs.value = [
      {
        id: 1,
        operationType: 'REMOTE_OPEN',
        description: '远程开门操作',
        success: true,
        createTime: new Date(Date.now() - 5 * 60000).toISOString(),
        operatorName: '系统管理员'
      },
      {
        id: 2,
        operationType: 'CONFIG_UPDATE',
        description: '更新设备配置参数',
        success: true,
        createTime: new Date(Date.now() - 10 * 60000).toISOString(),
        operatorName: '技术员'
      },
      {
        id: 3,
        operationType: 'RESTART',
        description: '设备重启',
        success: true,
        createTime: new Date(Date.now() - 2 * 3600000).toISOString(),
        operatorName: '系统管理员'
      }
    ];

    logPagination.total = operationLogs.value.length;
  } catch (error) {
    console.error('加载操作日志失败:', error);
  } finally {
    logLoading.value = false;
  }
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
};

// 监听弹窗显示
watch(visible, (newVal) => {
  if (newVal && props.deviceInfo?.deviceId) {
    loadOperationLogs();
  }
});
</script>

<style lang="less" scoped>
:deep(.ant-descriptions-item-label) {
  font-weight: 500;
}
</style>