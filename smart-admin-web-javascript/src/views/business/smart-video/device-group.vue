/*
 * 智能视频-设备分组页面
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Wechat:
 * @Email:
 * @Copyright  1024创新实验室
 */
<template>
  <div class="smart-video-group">
    <a-row :gutter="16" style="height: 100%">
      <!-- 左侧分组树 -->
      <a-col :span="8">
        <a-card title="设备分组列表" class="group-tree-card" :bordered="false">
          <template #extra>
            <a-button type="text" @click="refreshGroups">
              <template #icon><ReloadOutlined /></template>
            </a-button>
          </template>

          <!-- 搜索 -->
          <div class="group-search">
            <a-input
              v-model:value="groupSearchText"
              placeholder="搜索分组..."
              @change="searchGroups"
              allowClear
            >
              <template #prefix><SearchOutlined /></template>
            </a-input>
          </div>

          <!-- 操作按钮 -->
          <div class="group-actions">
            <a-space>
              <a-button type="primary" @click="showAddGroupModal" block>
                <template #icon><PlusOutlined /></template>
                新增分组
              </a-button>
              <a-button danger @click="deleteSelectedGroups" block>
                <template #icon><DeleteOutlined /></template>
                删除分组
              </a-button>
            </a-space>
          </div>

          <!-- 分组树 -->
          <div class="group-tree">
            <a-tree
              v-model:selectedKeys="selectedGroupKeys"
              v-model:checkedKeys="checkedGroupKeys"
              :tree-data="groupTreeData"
              :field-names="{ title: 'name', key: 'id', children: 'children' }"
              :checkable="true"
              :show-line="true"
              :expanded-keys="expandedKeys"
              @select="onGroupSelect"
              @expand="onGroupExpand"
            >
              <template #title="{ name, deviceCount, onlineCount }">
                <span class="group-title">
                  {{ name }}
                  <a-tag size="small" :color="onlineCount === deviceCount ? 'success' : 'warning'">
                    {{ onlineCount }}/{{ deviceCount }}
                  </a-tag>
                </span>
              </template>
            </a-tree>
          </div>

          <!-- 分组统计 -->
          <div class="group-stats">
            <a-descriptions size="small" :column="1" bordered>
              <a-descriptions-item label="分组总数">
                <span class="stat-value">{{ groupStats.totalGroups }}个</span>
              </a-descriptions-item>
              <a-descriptions-item label="设备总数">
                <span class="stat-value">{{ groupStats.totalDevices }}台</span>
              </a-descriptions-item>
              <a-descriptions-item label="在线设备">
                <span class="stat-value text-success">{{ groupStats.onlineDevices }}台</span>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧设备列表 -->
      <a-col :span="16">
        <a-card :title="currentGroupInfo.name" class="device-list-card" :bordered="false">
          <template #extra>
            <div class="device-list-extra">
              <a-input
                v-model:value="deviceSearchText"
                placeholder="搜索设备..."
                style="width: 200px; margin-right: 8px"
                @change="searchDevices"
                allowClear
              >
                <template #prefix><SearchOutlined /></template>
              </a-input>
              <a-space>
                <a-button @click="moveToGroup">
                  <template #icon><SwapOutlined /></template>
                  移动到分组
                </a-button>
                <a-button @click="removeFromGroup">
                  <template #icon><CloseOutlined /></template>
                  移出分组
                </a-button>
                <a-button @click="refreshDevices">
                  <template #icon><ReloadOutlined /></template>
                </a-button>
              </a-space>
            </div>
          </template>

          <template #titleExtra>
            <div class="device-count">
              共 <span class="count-number">{{ currentGroupDevices.length }}</span> 台设备
            </div>
          </template>

          <!-- 设备卡片列表 -->
          <div class="device-cards">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="device in currentGroupDevices"
                :key="device.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
                :xl="6"
              >
                <div class="device-card" :class="{ 'selected': selectedDeviceKeys.includes(device.id) }">
                  <div class="device-card-header">
                    <div class="device-select">
                      <a-checkbox
                        :checked="selectedDeviceKeys.includes(device.id)"
                        @change="onDeviceSelect(device.id, $event)"
                      />
                    </div>
                    <div class="device-icon">
                      <VideoCameraOutlined />
                    </div>
                    <div class="device-status" :class="device.status">
                      <span class="status-dot"></span>
                    </div>
                  </div>

                  <div class="device-card-content">
                    <div class="device-name">{{ device.deviceName }}</div>
                    <div class="device-code">{{ device.deviceCode }}</div>
                    <div class="device-location">
                      <EnvironmentOutlined />
                      {{ device.location }}
                    </div>
                    <div class="device-info">
                      <div class="info-item">
                        <span class="label">IP:</span>
                        <span class="value">{{ device.ipAddress }}</span>
                      </div>
                      <div class="info-item">
                        <span class="label">类型:</span>
                        <span class="value">{{ getDeviceTypeText(device.deviceType) }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="device-card-footer">
                    <a-space size="small">
                      <a-button type="link" size="small" @click="editDevice(device)">编辑</a-button>
                      <a-button type="link" size="small" @click="configDevice(device)">配置</a-button>
                      <a-button type="link" size="small" @click="previewDevice(device)">预览</a-button>
                    </a-space>
                  </div>
                </div>
              </a-col>
            </a-row>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 新增/编辑分组弹窗 -->
    <a-modal
      v-model:open="groupModalVisible"
      :title="groupModalTitle"
      @ok="handleGroupSubmit"
      @cancel="handleGroupCancel"
    >
      <a-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="分组名称" name="name">
          <a-input v-model:value="groupForm.name" placeholder="请输入分组名称" />
        </a-form-item>
        <a-form-item label="上级分组" name="parentId">
          <a-tree-select
            v-model:value="groupForm.parentId"
            :tree-data="groupTreeSelectData"
            placeholder="请选择上级分组"
            allowClear
          />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="groupForm.description" placeholder="请输入分组描述" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 移动设备弹窗 -->
    <a-modal
      v-model:open="moveModalVisible"
      title="移动设备到分组"
      @ok="handleMoveSubmit"
      @cancel="handleMoveCancel"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="目标分组">
          <a-tree-select
            v-model:value="moveTargetGroup"
            :tree-data="groupTreeSelectData"
            placeholder="请选择目标分组"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  SwapOutlined,
  CloseOutlined,
  VideoCameraOutlined,
  EnvironmentOutlined,
} from '@ant-design/icons-vue';

// 搜索相关
const groupSearchText = ref('');
const deviceSearchText = ref('');

// 选中状态
const selectedGroupKeys = ref([]);
const checkedGroupKeys = ref([]);
const selectedDeviceKeys = ref([]);
const expandedKeys = ref(['root', 'office', 'production']);

// 弹窗相关
const groupModalVisible = ref(false);
const groupModalTitle = ref('新增分组');
const groupFormRef = ref();
const moveModalVisible = ref(false);
const moveTargetGroup = ref('');

// 分组表单
const groupForm = reactive({
  name: '',
  parentId: undefined,
  description: '',
});

// 分组表单验证规则
const groupFormRules = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }],
};

// 模拟分组树数据
const groupTreeData = ref([
  {
    id: 'root',
    name: '全部设备',
    deviceCount: 158,
    onlineCount: 156,
    children: [
      {
        id: 'office',
        name: '办公区域',
        deviceCount: 42,
        onlineCount: 41,
        children: [
          {
            id: 'office-1f',
            name: '一楼',
            deviceCount: 15,
            onlineCount: 15,
          },
          {
            id: 'office-2f',
            name: '二楼',
            deviceCount: 18,
            onlineCount: 18,
          },
          {
            id: 'office-3f',
            name: '三楼',
            deviceCount: 9,
            onlineCount: 8,
          },
        ],
      },
      {
        id: 'production',
        name: '生产区域',
        deviceCount: 38,
        onlineCount: 38,
        children: [
          {
            id: 'production-workshop',
            name: '车间',
            deviceCount: 22,
            onlineCount: 22,
          },
          {
            id: 'production-warehouse',
            name: '仓库',
            deviceCount: 16,
            onlineCount: 16,
          },
        ],
      },
      {
        id: 'parking',
        name: '停车区域',
        deviceCount: 28,
        onlineCount: 27,
      },
      {
        id: 'public',
        name: '公共区域',
        deviceCount: 35,
        onlineCount: 35,
      },
      {
        id: 'security',
        name: '周界防范',
        deviceCount: 15,
        onlineCount: 15,
      },
    ],
  },
]);

// 分组统计数据
const groupStats = computed(() => {
  return {
    totalGroups: 6,
    totalDevices: 158,
    onlineDevices: 156,
  };
});

// 当前选中的分组信息
const currentGroupInfo = computed(() => {
  const groupId = selectedGroupKeys.value[0] || 'root';
  const findGroup = (groups, id) => {
    for (const group of groups) {
      if (group.id === id) return group;
      if (group.children) {
        const found = findGroup(group.children, id);
        if (found) return found;
      }
    }
    return { name: '未分组', deviceCount: 0 };
  };
  return findGroup(groupTreeData.value, groupId);
});

// 模拟设备数据
const allDevices = ref([
  {
    id: 1,
    deviceName: '前门摄像头-001',
    deviceCode: 'CAM001',
    deviceType: 'ipc',
    ipAddress: '192.168.1.101',
    port: 8000,
    status: 'online',
    location: '前门-入口',
    groupId: 'office-1f',
  },
  {
    id: 2,
    deviceName: '大厅摄像头-002',
    deviceCode: 'CAM002',
    deviceType: 'ipc',
    ipAddress: '192.168.1.102',
    port: 8000,
    status: 'online',
    location: '大厅中央',
    groupId: 'office-1f',
  },
  {
    id: 3,
    deviceName: '走廊摄像头-003',
    deviceCode: 'CAM003',
    deviceType: 'ipc',
    ipAddress: '192.168.1.103',
    port: 8000,
    status: 'offline',
    location: '一楼走廊',
    groupId: 'office-1f',
  },
  {
    id: 4,
    deviceName: '会议室摄像头-001',
    deviceCode: 'CAM004',
    deviceType: 'ipc',
    ipAddress: '192.168.1.104',
    port: 8000,
    status: 'online',
    location: '一楼会议室',
    groupId: 'office-1f',
  },
  {
    id: 5,
    deviceName: '财务室摄像头-001',
    deviceCode: 'CAM005',
    deviceType: 'ipc',
    ipAddress: '192.168.1.105',
    port: 8000,
    status: 'online',
    location: '二楼财务室',
    groupId: 'office-2f',
  },
  {
    id: 6,
    deviceName: 'NVR存储设备-01',
    deviceCode: 'NVR001',
    deviceType: 'nvr',
    ipAddress: '192.168.1.200',
    port: 8000,
    status: 'online',
    location: '三楼机房',
    groupId: 'office-3f',
  },
  {
    id: 7,
    deviceName: '停车场摄像头-01',
    deviceCode: 'CAM006',
    deviceType: 'ipc',
    ipAddress: '192.168.1.106',
    port: 8000,
    status: 'online',
    location: '停车场入口',
    groupId: 'parking',
  },
  {
    id: 8,
    deviceName: '车间摄像头-001',
    deviceCode: 'CAM007',
    deviceType: 'ipc',
    ipAddress: '192.168.1.107',
    port: 8000,
    status: 'online',
    location: '生产线A',
    groupId: 'production-workshop',
  },
]);

// 当前分组的设备
const currentGroupDevices = computed(() => {
  const groupId = selectedGroupKeys.value[0] || 'root';
  let devices = allDevices.value;

  if (groupId !== 'root') {
    devices = devices.filter(device => device.groupId === groupId);
  }

  if (deviceSearchText.value) {
    devices = devices.filter(device =>
      device.deviceName.includes(deviceSearchText.value) ||
      device.deviceCode.includes(deviceSearchText.value)
    );
  }

  return devices;
});

// 树选择数据
const groupTreeSelectData = computed(() => {
  const convertToSelectData = (groups) => {
    return groups.map(group => ({
      title: group.name,
      value: group.id,
      children: group.children ? convertToSelectData(group.children) : undefined,
    }));
  };
  return convertToSelectData(groupTreeData.value);
});

// 获取设备类型文本
const getDeviceTypeText = (type) => {
  const typeMap = {
    ipc: '网络摄像机',
    nvr: 'NVR',
    dvr: 'DVR',
    decoder: '解码器',
  };
  return typeMap[type] || type;
};

// 搜索分组
const searchGroups = () => {
  console.log('搜索分组:', groupSearchText.value);
};

// 搜索设备
const searchDevices = () => {
  console.log('搜索设备:', deviceSearchText.value);
};

// 刷新分组
const refreshGroups = () => {
  message.info('刷新分组列表');
};

// 刷新设备
const refreshDevices = () => {
  message.info('刷新设备列表');
};

// 分组选择
const onGroupSelect = (keys) => {
  selectedGroupKeys.value = keys;
  selectedDeviceKeys.value = [];
};

// 分组展开
const onGroupExpand = (keys) => {
  expandedKeys.value = keys;
};

// 设备选择
const onDeviceSelect = (deviceId, event) => {
  if (event.target.checked) {
    selectedDeviceKeys.value.push(deviceId);
  } else {
    const index = selectedDeviceKeys.value.indexOf(deviceId);
    if (index > -1) {
      selectedDeviceKeys.value.splice(index, 1);
    }
  }
};

// 显示新增分组弹窗
const showAddGroupModal = () => {
  groupModalTitle.value = '新增分组';
  groupModalVisible.value = true;
  resetGroupForm();
};

// 删除选中分组
const deleteSelectedGroups = () => {
  if (checkedGroupKeys.value.length === 0) {
    message.warning('请选择要删除的分组');
    return;
  }
  if (checkedGroupKeys.value.includes('root')) {
    message.warning('不能删除根分组');
    return;
  }
  message.success(`成功删除 ${checkedGroupKeys.value.length} 个分组`);
  checkedGroupKeys.value = [];
};

// 移动到分组
const moveToGroup = () => {
  if (selectedDeviceKeys.value.length === 0) {
    message.warning('请选择要移动的设备');
    return;
  }
  moveModalVisible.value = true;
};

// 移出分组
const removeFromGroup = () => {
  if (selectedDeviceKeys.value.length === 0) {
    message.warning('请选择要移出的设备');
    return;
  }
  message.success(`成功将 ${selectedDeviceKeys.value.length} 个设备移出分组`);
  selectedDeviceKeys.value = [];
};

// 编辑设备
const editDevice = (device) => {
  message.info(`编辑设备: ${device.deviceName}`);
};

// 配置设备
const configDevice = (device) => {
  message.info(`配置设备: ${device.deviceName}`);
};

// 预览设备
const previewDevice = (device) => {
  message.info(`预览设备: ${device.deviceName}`);
};

// 分组表单提交
const handleGroupSubmit = () => {
  groupFormRef.value.validate().then(() => {
    message.success(groupModalTitle.value + '成功');
    groupModalVisible.value = false;
  });
};

// 分组表单取消
const handleGroupCancel = () => {
  groupModalVisible.value = false;
  resetGroupForm();
};

// 移动表单提交
const handleMoveSubmit = () => {
  if (!moveTargetGroup.value) {
    message.warning('请选择目标分组');
    return;
  }
  message.success(`成功将 ${selectedDeviceKeys.value.length} 个设备移动到分组`);
  moveModalVisible.value = false;
  selectedDeviceKeys.value = [];
  moveTargetGroup.value = '';
};

// 移动表单取消
const handleMoveCancel = () => {
  moveModalVisible.value = false;
  moveTargetGroup.value = '';
};

// 重置分组表单
const resetGroupForm = () => {
  Object.assign(groupForm, {
    name: '',
    parentId: undefined,
    description: '',
  });
};

// 初始化
onMounted(() => {
  selectedGroupKeys.value = ['root'];
});
</script>

<style scoped>
.smart-video-group {
  height: calc(100vh - 120px);
  padding: 24px;
}

.group-tree-card {
  height: 100%;
}

.group-search {
  margin-bottom: 16px;
}

.group-actions {
  margin-bottom: 16px;
}

.group-tree {
  height: calc(100vh - 400px);
  overflow-y: auto;
  margin-bottom: 16px;
}

.group-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.group-stats {
  margin-top: auto;
}

.stat-value {
  font-weight: bold;
  color: #1890ff;
}

.text-success {
  color: #52c41a;
}

.device-list-card {
  height: 100%;
}

.device-list-extra {
  display: flex;
  align-items: center;
}

.device-count {
  margin-left: 16px;
  font-size: 14px;
  color: #666;
}

.count-number {
  font-weight: bold;
  color: #1890ff;
}

.device-cards {
  height: calc(100vh - 280px);
  overflow-y: auto;
}

.device-card {
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
  cursor: pointer;
  height: 200px;
  display: flex;
  flex-direction: column;
}

.device-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.device-card.selected {
  border-color: #1890ff;
  background-color: #f6ffed;
}

.device-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.device-icon {
  font-size: 20px;
  color: #1890ff;
}

.device-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.device-status.online {
  background-color: #52c41a;
}

.device-status.offline {
  background-color: #f5222d;
}

.device-card-content {
  flex: 1;
}

.device-name {
  font-weight: bold;
  font-size: 14px;
  color: #262626;
  margin-bottom: 4px;
}

.device-code {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.device-location {
  font-size: 12px;
  color: #595959;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.device-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item {
  font-size: 12px;
  display: flex;
  justify-content: space-between;
}

.info-item .label {
  color: #8c8c8c;
}

.info-item .value {
  color: #595959;
}

.device-card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.device-card-footer :deep(.ant-btn-link) {
  padding: 0;
  height: auto;
  line-height: 1.2;
}
</style>