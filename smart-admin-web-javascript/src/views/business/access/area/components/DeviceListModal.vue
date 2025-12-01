<template>
  <a-modal
    v-model:open="visible"
    :title="`${areaInfo.areaName} - 设备列表`"
    width="900px"
    :footer="null"
    @cancel="handleCancel"
  >
    <!-- 搜索区 -->
    <div class="search-section" style="margin-bottom: 16px;">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="设备名称">
          <a-input
            v-model:value="searchForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="设备类型">
          <a-select
            v-model:value="searchForm.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="DOOR_CONTROLLER">门禁控制器</a-select-option>
            <a-select-option value="CARD_READER">读卡器</a-select-option>
            <a-select-option value="FACE_RECOGNITION">人脸识别</a-select-option>
            <a-select-option value="FINGERPRINT">指纹识别</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="设备状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 100px"
          >
            <a-select-option value="1">在线</a-select-option>
            <a-select-option value="0">离线</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 设备表格 -->
    <a-table
      :columns="columns"
      :data-source="deviceData"
      :loading="loading"
      :pagination="pagination"
      :row-key="record => record.deviceId"
      :scroll="{ x: 800 }"
      size="small"
      @change="handleTableChange"
    >
      <!-- 设备状态 -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-badge :status="record.status === 1 ? 'success' : 'default'" :text="getStatusText(record.status)" />
        </template>

        <!-- 设备类型 -->
        <template v-else-if="column.key === 'deviceType'">
          <a-tag :color="getDeviceTypeColor(record.deviceType)">
            {{ getDeviceTypeLabel(record.deviceType) }}
          </a-tag>
        </template>

        <!-- 在线状态 -->
        <template v-else-if="column.key === 'onlineStatus'">
          <a-switch
            :checked="record.onlineStatus"
            :disabled="true"
            :checked-children="record.onlineStatus ? '在线' : '离线'"
          />
        </template>

        <!-- 最后通信时间 -->
        <template v-else-if="column.key === 'lastCommunication'">
          {{ formatDateTime(record.lastCommunication) }}
        </template>

        <!-- 操作 -->
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              详情
            </a-button>
            <a-button type="link" size="small" @click="handleRemoteControl(record)">
              远程控制
            </a-button>
            <a-popconfirm
              title="确定要移除该设备吗？"
              @confirm="handleRemoveDevice(record)"
              ok-text="确定"
              cancel-text="取消"
            >
              <a-button type="link" size="small" danger>移除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 设备详情弹窗 -->
    <DeviceDetailModal
      v-model:visible="detailVisible"
      :device-info="selectedDevice"
    />

    <!-- 远程控制弹窗 -->
    <RemoteControlModal
      v-model:visible="remoteControlVisible"
      :device-info="selectedDevice"
      @success="handleRemoteSuccess"
    />
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';
import { accessDeviceApi } from '/@/api/business/access/device-api.js';
import { formatDateTime } from '/@/utils/format.js';
import DeviceDetailModal from './DeviceDetailModal.vue';
import RemoteControlModal from './RemoteControlModal.vue';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  areaInfo: {
    type: Object,
    default: () => ({})
  }
});

// Emits
const emit = defineEmits(['update:visible', 'refresh']);

// 响应式数据
const loading = ref(false);
const deviceData = ref([]);
const detailVisible = ref(false);
const remoteControlVisible = ref(false);
const selectedDevice = ref(null);
const searchForm = reactive({
  deviceName: '',
  deviceType: '',
  status: null
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
});

// 表格列定义
const columns = [
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
    title: '设备类型',
    dataIndex: 'deviceType',
    key: 'deviceType',
    width: 120
  },
  {
    title: '设备状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '在线状态',
    dataIndex: 'onlineStatus',
    key: 'onlineStatus',
    width: 100,
    align: 'center'
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 120
  },
  {
    title: '最后通信',
    dataIndex: 'lastCommunication',
    key: 'lastCommunication',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right'
  }
];

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

// 状态相关方法
const getStatusText = (status) => {
  return status === 1 ? '启用' : '禁用';
};

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

// 加载设备数据
const loadDevices = async () => {
  if (!props.areaInfo?.areaId) return;

  loading.value = true;
  try {
    const response = await accessDeviceApi.getAreaDevices(props.areaInfo.areaId);

    if (response && response.data) {
      // 模拟分页数据（实际应该有后端分页API）
      let filteredData = response.data;

      // 应用搜索过滤
      if (searchForm.deviceName) {
        filteredData = filteredData.filter(device =>
          device.deviceName.toLowerCase().includes(searchForm.deviceName.toLowerCase())
        );
      }

      if (searchForm.deviceType) {
        filteredData = filteredData.filter(device => device.deviceType === searchForm.deviceType);
      }

      if (searchForm.status !== null) {
        filteredData = filteredData.filter(device => device.status === searchForm.status);
      }

      // 分页处理
      const startIndex = (pagination.current - 1) * pagination.pageSize;
      const endIndex = startIndex + pagination.pageSize;
      deviceData.value = filteredData.slice(startIndex, endIndex);
      pagination.total = filteredData.length;
    }
  } catch (error) {
    console.error('加载设备数据失败:', error);
    message.error('加载设备数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadDevices();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    deviceName: '',
    deviceType: '',
    status: null
  });
  pagination.current = 1;
  loadDevices();
};

// 表格变化
const handleTableChange = (pag) => {
  Object.assign(pagination, pag);
  loadDevices();
};

// 查看设备详情
const handleViewDetail = (record) => {
  selectedDevice.value = record;
  detailVisible.value = true;
};

// 远程控制
const handleRemoteControl = (record) => {
  selectedDevice.value = record;
  remoteControlVisible.value = true;
};

// 移除设备
const handleRemoveDevice = async (record) => {
  try {
    // 这里应该调用取消设备关联的API
    message.success('设备移除成功');
    emit('refresh');
  } catch (error) {
    console.error('移除设备失败:', error);
    message.error('移除设备失败');
  }
};

// 远程控制成功回调
const handleRemoteSuccess = () => {
  remoteControlVisible.value = false;
  loadDevices();
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
};

// 监听弹窗显示
watch(visible, (newVal) => {
  if (newVal) {
    loadDevices();
  }
});
</script>

<style lang="less" scoped>
.search-section {
  background: #fafafa;
  padding: 16px;
  border-radius: 6px;
  margin: -16px -16px 16px -16px;
}
</style>