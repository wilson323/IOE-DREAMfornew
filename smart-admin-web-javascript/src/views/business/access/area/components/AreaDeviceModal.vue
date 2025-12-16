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
  import { accessApi } from '/@/api/business/access/access-api';

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

  const loadDeviceList = async () => {
    loading.value = true;
    try {
      const areaId = Number(props.areaId);
      const result = await accessApi.getAreaDevices(areaId);
      if (result.code !== 200 || !Array.isArray(result.data)) {
        deviceList.value = [];
        return;
      }

      const keyword = searchKeyword.value?.trim()?.toLowerCase();
      const list = result.data
        .map((item) => {
          const relationStatus = item.relationStatus;
          const enabled = item.enabled !== false;
          const status = enabled && relationStatus === 1 ? 'ONLINE' : 'OFFLINE';

          const location = item.locationDesc || item.installLocation || '-';
          const lastOnlineTime = item.updateTime || item.createTime || '-';

          return {
            deviceId: item.deviceId,
            deviceName: item.deviceName,
            deviceCode: item.deviceCode,
            deviceType: item.deviceType,
            status,
            location,
            lastOnlineTime,
          };
        })
        .filter((item) => {
          if (!keyword) return true;
          return (
            String(item.deviceName || '').toLowerCase().includes(keyword) ||
            String(item.deviceCode || '').toLowerCase().includes(keyword) ||
            String(item.deviceId || '').toLowerCase().includes(keyword)
          );
        });

      deviceList.value = list;
    } catch (error) {
      message.error('Failed to load device list');
    } finally {
      loading.value = false;
    }
  };

  const getDeviceTypeColor = (type) => {
    const colorMap = {
      1: 'blue',
      2: 'green',
      3: 'orange',
      4: 'purple',
      5: 'cyan',
      6: 'red',
      7: 'geekblue',
      8: 'gold',
    };
    return colorMap[type] || 'default';
  };

  const getDeviceTypeText = (type) => {
    const textMap = {
      1: 'Access',
      2: 'Attendance',
      3: 'Consume',
      4: 'Video',
      5: 'Visitor',
      6: 'Alarm',
      7: 'Display',
      8: 'Network',
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
          const areaId = Number(props.areaId);
          await accessApi.removeAreaDevice(areaId, record.deviceId);
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
    loadDeviceList();
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
