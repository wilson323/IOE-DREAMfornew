<!--
  * Area Device Modal Component
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    :title="`Device Management - ${areaName}`"
    width="1000px"
    :footer="null"
  >
    <div class="area-device-content">
      <!-- 工具栏 -->
      <div class="toolbar" style="margin-bottom: 16px;">
        <a-space>
          <a-button type="primary" @click="handleAddDevice">
            <template #icon><PlusOutlined /></template>
            Add Device
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            Refresh
          </a-button>
          <a-input
            v-model:value="searchKeyword"
            placeholder="Search devices"
            style="width: 200px"
            @pressEnter="handleSearch"
          />
        </a-space>
      </div>

      <!-- 设备列表 -->
      <a-table
        :columns="columns"
        :data-source="deviceList"
        :pagination="{ pageSize: 10, showSizeChanger: true }"
        :loading="loading"
        row-key="deviceId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'deviceStatus'">
            <a-badge
              :status="record.status === 'ONLINE' ? 'success' : 'error'"
              :text="record.status === 'ONLINE' ? 'Online' : 'Offline'"
            />
          </template>

          <template v-else-if="column.key === 'deviceType'">
            <a-tag :color="getDeviceTypeColor(record.deviceType)">
              {{ getDeviceTypeText(record.deviceType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleConfigDevice(record)">
                Config
              </a-button>
              <a-button type="link" size="small" danger @click="handleRemoveDevice(record)">
                Remove
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, watch } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue';

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
  const searchKeyword = ref('');
  const deviceList = ref([]);

  const columns = [
    {
      title: 'Device Name',
      dataIndex: 'deviceName',
      key: 'deviceName',
    },
    {
      title: 'Device Code',
      dataIndex: 'deviceCode',
      key: 'deviceCode',
    },
    {
      title: 'Type',
      key: 'deviceType',
      width: 120,
    },
    {
      title: 'Status',
      key: 'deviceStatus',
      width: 100,
    },
    {
      title: 'Location',
      dataIndex: 'location',
      key: 'location',
    },
    {
      title: 'Last Online',
      dataIndex: 'lastOnlineTime',
      key: 'lastOnlineTime',
      width: 160,
    },
    {
      title: 'Actions',
      key: 'action',
      width: 150,
    },
  ];

  // 模拟数据
  const mockDevices = [
    {
      deviceId: '1',
      deviceName: 'Main Entrance Gate',
      deviceCode: 'GATE-001',
      deviceType: 'GATE',
      status: 'ONLINE',
      location: 'Building A - Main Entrance',
      lastOnlineTime: '2025-01-30 10:30:00',
    },
    {
      deviceId: '2',
      deviceName: 'Floor 1 Door Lock',
      deviceCode: 'DOOR-001',
      deviceType: 'DOOR',
      status: 'OFFLINE',
      location: 'Building A - Floor 1',
      lastOnlineTime: '2025-01-29 15:20:00',
    },
  ];

  const loadDeviceList = async () => {
    loading.value = true;
    try {
      // TODO: 调用API获取设备列表
      // const result = await deviceApi.getAreaDevices(props.areaId);

      // 使用模拟数据
      await new Promise(resolve => setTimeout(resolve, 500));
      deviceList.value = mockDevices;
    } catch (error) {
      message.error('Failed to load device list');
    } finally {
      loading.value = false;
    }
  };

  const getDeviceTypeColor = (type) => {
    const colorMap = {
      GATE: 'blue',
      DOOR: 'green',
      CAMERA: 'purple',
      READER: 'orange',
      SENSOR: 'cyan',
    };
    return colorMap[type] || 'default';
  };

  const getDeviceTypeText = (type) => {
    const textMap = {
      GATE: 'Gate',
      DOOR: 'Door',
      CAMERA: 'Camera',
      READER: 'Reader',
      SENSOR: 'Sensor',
    };
    return textMap[type] || type;
  };

  const handleAddDevice = () => {
    message.info('Add device feature under development');
  };

  const handleConfigDevice = (record) => {
    Modal.info({
      title: 'Device Configuration',
      content: `Configuration for ${record.deviceName} is under development`,
    });
  };

  const handleRemoveDevice = (record) => {
    Modal.confirm({
      title: 'Confirm Remove',
      content: `Are you sure to remove device ${record.deviceName} from this area?`,
      onOk: async () => {
        try {
          // TODO: 调用API移除设备
          message.success('Device removed successfully');
          loadDeviceList();
        } catch (error) {
          message.error('Failed to remove device');
        }
      },
    });
  };

  const handleRefresh = () => {
    loadDeviceList();
  };

  const handleSearch = () => {
    // TODO: 实现搜索逻辑
    console.log('Search:', searchKeyword.value);
  };

  watch(() => props.visible, (visible) => {
    if (visible && props.areaId) {
      loadDeviceList();
    }
  });
</script>

<style lang="less" scoped>
  .area-device-content {
    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
</style>