<!--
  设备选择弹窗组件

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <a-modal
    :open="visible"
    title="选择设备"
    width="800px"
    @cancel="handleCancel"
    @ok="handleConfirm"
  >
    <div class="device-select-modal">
      <!-- 搜索框 -->
      <div class="search-box">
        <a-input
          v-model:value="searchText"
          placeholder="搜索设备名称或编码..."
          allowClear
        >
          <template #prefix>
            <SearchOutlined />
          </template>
        </a-input>
      </div>

      <!-- 设备列表 -->
      <div class="device-list">
        <a-checkbox-group v-model:value="selectedDeviceIds" style="width: 100%;">
          <a-row :gutter="[16, 16]">
            <a-col
              :span="12"
              v-for="device in filteredDevices"
              :key="device.id"
            >
              <a-card
                size="small"
                :class="['device-card', { 'selected': selectedDeviceIds.includes(device.id) }]"
              >
                <div class="device-card-content">
                  <a-checkbox :value="device.id">
                    <div class="device-info">
                      <div class="device-header">
                        <VideoCameraOutlined class="device-icon" />
                        <span class="device-name">{{ device.name }}</span>
                        <a-tag
                          :color="device.status === 'online' ? 'success' : 'error'"
                          size="small"
                        >
                          {{ device.status === 'online' ? '在线' : '离线' }}
                        </a-tag>
                      </div>
                      <div class="device-details">
                        <div class="detail-item">
                          <span class="label">编码:</span>
                          <span class="value">{{ device.code }}</span>
                        </div>
                        <div class="detail-item">
                          <span class="label">位置:</span>
                          <span class="value">{{ device.location }}</span>
                        </div>
                      </div>
                    </div>
                  </a-checkbox>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </a-checkbox-group>
      </div>

      <!-- 已选设备提示 -->
      <div class="selected-info" v-if="selectedDeviceIds.length > 0">
        已选择 <span class="count">{{ selectedDeviceIds.length }}</span> 个设备
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, computed } from 'vue';
import { SearchOutlined, VideoCameraOutlined } from '@ant-design/icons-vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  selectedDevices: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['close', 'confirm']);

// 搜索文本
const searchText = ref('');

// 选中的设备ID列表
const selectedDeviceIds = ref([...props.selectedDevices]);

// 模拟设备数据
const mockDevices = ref([
  {
    id: 1,
    name: '前门摄像头-001',
    code: 'CAM001',
    location: '一号楼入口',
    status: 'online',
  },
  {
    id: 2,
    name: '大厅摄像头-002',
    code: 'CAM002',
    location: '一号楼大厅',
    status: 'online',
  },
  {
    id: 3,
    name: '走廊摄像头-003',
    code: 'CAM003',
    location: '一号楼走廊',
    status: 'offline',
  },
  {
    id: 4,
    name: '会议室摄像头-001',
    code: 'CAM004',
    location: '一号楼会议室',
    status: 'online',
  },
  {
    id: 5,
    name: '财务室摄像头-001',
    code: 'CAM005',
    location: '二楼财务室',
    status: 'online',
  },
  {
    id: 6,
    name: '停车场摄像头-01',
    code: 'CAM006',
    location: '停车场入口',
    status: 'online',
  },
  {
    id: 7,
    name: '车间摄像头-001',
    code: 'CAM007',
    location: '生产线A',
    status: 'online',
  },
  {
    id: 8,
    name: '仓库摄像头-001',
    code: 'CAM008',
    location: '仓库入口',
    status: 'online',
  },
]);

// 过滤后的设备列表
const filteredDevices = computed(() => {
  if (!searchText.value) {
    return mockDevices.value;
  }

  return mockDevices.value.filter(
    (device) =>
      device.name.includes(searchText.value) ||
      device.code.includes(searchText.value) ||
      device.location.includes(searchText.value)
  );
});

// 取消
const handleCancel = () => {
  selectedDeviceIds.value = [];
  emit('close');
};

// 确认
const handleConfirm = () => {
  const selectedDevices = mockDevices.value.filter((device) =>
    selectedDeviceIds.value.includes(device.id)
  );
  emit('confirm', selectedDevices);
  selectedDeviceIds.value = [];
};
</script>

<style scoped lang="less">
.device-select-modal {
  .search-box {
    margin-bottom: 16px;
  }

  .device-list {
    max-height: 500px;
    overflow-y: auto;
    padding: 8px 0;
  }

  .device-card {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #1890ff;
    }

    &.selected {
      border-color: #1890ff;
      background-color: #f0f5ff;
    }
  }

  .device-card-content {
    :deep(.ant-checkbox-wrapper) {
      width: 100%;
    }
  }

  .device-info {
    width: 100%;
  }

  .device-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .device-icon {
      font-size: 18px;
      color: #1890ff;
    }

    .device-name {
      flex: 1;
      font-weight: 600;
      font-size: 14px;
    }
  }

  .device-details {
    margin-left: 26px;
    font-size: 12px;
    color: #666;

    .detail-item {
      display: flex;
      margin-bottom: 4px;

      .label {
        width: 40px;
        color: #999;
      }

      .value {
        flex: 1;
      }
    }
  }

  .selected-info {
    margin-top: 16px;
    padding: 12px;
    background-color: #f0f5ff;
    border-radius: 4px;
    text-align: center;
    font-size: 14px;

    .count {
      font-weight: 600;
      color: #1890ff;
      font-size: 16px;
      margin: 0 4px;
    }
  }
}
</style>
