<!--
  智能视频-设备列表管理页面

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-device">
    <!-- 顶部查询工具栏 -->
    <a-card :bordered="false">
      <a-form class="smart-query-table-form">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="设备名称">
              <a-input
                v-model:value="queryForm.deviceName"
                placeholder="请输入设备名称或编码"
                allowClear
              />
            </a-form-item>
          </a-col>

          <a-col :span="4">
            <a-form-item label="状态">
              <a-select
                v-model:value="queryForm.status"
                placeholder="请选择状态"
                allowClear
              >
                <a-select-option value="">全部</a-select-option>
                <a-select-option
                  v-for="item in Object.values(DEVICE_STATUS_ENUM)"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="5">
            <a-form-item label="设备类型">
              <a-select
                v-model:value="queryForm.deviceType"
                placeholder="请选择设备类型"
                allowClear
              >
                <a-select-option value="">全部类型</a-select-option>
                <a-select-option
                  v-for="item in Object.values(DEVICE_TYPE_ENUM)"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="9">
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="queryData">
                  <template #icon><SearchOutlined /></template>
                  查询
                </a-button>
                <a-button @click="resetQuery">
                  <template #icon><ReloadOutlined /></template>
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- 主操作按钮区 -->
    <a-card :bordered="false" class="smart-margin-top10">
      <div class="smart-query-table-operation">
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <template #icon><PlusOutlined /></template>
            新增设备
          </a-button>

          <a-button
            danger
            @click="batchDelete"
            :disabled="selectedRowKeys.length === 0"
          >
            <template #icon><DeleteOutlined /></template>
            删除
          </a-button>

          <a-button @click="searchDevices">
            <template #icon><SearchOutlined /></template>
            搜索设备
          </a-button>

          <a-button @click="syncDevices" :disabled="selectedRowKeys.length === 0">
            <template #icon><SyncOutlined /></template>
            同步设备
          </a-button>

          <a-button @click="exportDevices">
            <template #icon><DownloadOutlined /></template>
            导出
          </a-button>

          <a-button @click="importDevices">
            <template #icon><UploadOutlined /></template>
            导入
          </a-button>
        </a-space>
      </div>

      <!-- 更多操作区 -->
      <div class="smart-query-table-operation" style="margin-top: 16px;">
        <a-space>
          <span style="color: #999;">更多操作:</span>

          <a-button size="small" @click="subscribeDevices" :disabled="selectedRowKeys.length === 0">
            <template #icon><BellOutlined /></template>
            订阅
          </a-button>

          <a-button size="small" @click="unsubscribeDevices" :disabled="selectedRowKeys.length === 0">
            <template #icon><BellOutlined /></template>
            取消订阅
          </a-button>

          <a-button size="small" @click="syncTime" :disabled="selectedRowKeys.length === 0">
            <template #icon><ClockCircleOutlined /></template>
            同步时间
          </a-button>

          <a-button size="small" @click="maintenanceManage" :disabled="selectedRowKeys.length === 0">
            <template #icon><ToolOutlined /></template>
            维护管理
          </a-button>

          <a-button size="small" @click="viewDeviceInfo" :disabled="selectedRowKeys.length !== 1">
            <template #icon><InfoCircleOutlined /></template>
            查看信息
          </a-button>

          <a-button size="small" @click="targetList" :disabled="selectedRowKeys.length === 0">
            <template #icon><UnorderedListOutlined /></template>
            目标名单库
          </a-button>
        </a-space>
      </div>

      <!-- 设备列表表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="tableLoading"
        :row-selection="rowSelection"
        :scroll="{ x: 1600 }"
        row-key="id"
        bordered
        @change="handleTableChange"
        class="smart-margin-top10"
      >
        <template #bodyCell="{ column, record }">
          <!-- 设备类型 -->
          <template v-if="column.dataIndex === 'deviceType'">
            <a-tag :color="getDeviceTypeColor(record.deviceType)">
              {{ getDeviceTypeLabel(record.deviceType) }}
            </a-tag>
          </template>

          <!-- 厂商 -->
          <template v-else-if="column.dataIndex === 'manufacturer'">
            {{ getManufacturerLabel(record.manufacturer) }}
          </template>

          <!-- 状态 -->
          <template v-else-if="column.dataIndex === 'status'">
            <a-tag :color="getDeviceStatusColor(record.status)">
              <template #icon>
                <CheckCircleOutlined v-if="record.status === 'online'" />
                <CloseCircleOutlined v-else />
              </template>
              {{ getDeviceStatusLabel(record.status) }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
              <a-button type="link" size="small" @click="editDevice(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>

              <a-button type="link" size="small" @click="configDevice(record)">
                <template #icon><SettingOutlined /></template>
                配置
              </a-button>

              <a-button type="link" size="small" @click="viewDevice(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>

              <a-popconfirm
                title="确定要删除该设备吗?"
                @confirm="deleteDevice(record)"
              >
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 设备表单弹窗 -->
    <DeviceForm
      :visible="modalVisible"
      :is-edit="isEdit"
      :device-data="currentDevice"
      :group-tree-data="groupTreeData"
      @update:visible="modalVisible = $event"
      @success="handleFormSuccess"
      @cancel="handleFormCancel"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  SyncOutlined,
  BellOutlined,
  ClockCircleOutlined,
  ToolOutlined,
  InfoCircleOutlined,
  UnorderedListOutlined,
  DownloadOutlined,
  UploadOutlined,
  EditOutlined,
  SettingOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons-vue';
import DeviceForm from './components/DeviceForm.vue';
import {
  DEVICE_TYPE_ENUM,
  DEVICE_STATUS_ENUM,
  DEVICE_MANUFACTURER_ENUM,
  getDeviceTypeLabel,
  getDeviceTypeColor,
  getDeviceStatusLabel,
  getDeviceStatusColor,
  getManufacturerLabel,
} from '/@/constants/business/smart-video/device-const';
import deviceMockData, { mockGroupTreeData } from './mock/device-mock-data';

// 查询表单
const queryForm = reactive({
  deviceName: '',
  status: '',
  deviceType: '',
});

// 表格数据
const tableData = ref([]);
const tableLoading = ref(false);
const selectedRowKeys = ref([]);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列配置
const columns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 80,
    customRender: ({ index }) => index + 1,
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 180,
    ellipsis: true,
  },
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 150,
  },
  {
    title: '设备类型',
    dataIndex: 'deviceType',
    key: 'deviceType',
    width: 120,
  },
  {
    title: '厂商',
    dataIndex: 'manufacturer',
    key: 'manufacturer',
    width: 120,
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 140,
  },
  {
    title: '端口',
    dataIndex: 'port',
    key: 'port',
    width: 80,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '所属分组',
    dataIndex: 'groupName',
    key: 'groupName',
    width: 120,
    ellipsis: true,
  },
  {
    title: '注册时间',
    dataIndex: 'registerTime',
    key: 'registerTime',
    width: 160,
  },
  {
    title: '最后心跳',
    dataIndex: 'lastHeartbeat',
    key: 'lastHeartbeat',
    width: 160,
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 260,
    fixed: 'right',
  },
];

// 行选择
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
}));

// 弹窗相关
const modalVisible = ref(false);
const isEdit = ref(false);
const currentDevice = ref({});

// 分组树数据
const groupTreeData = ref(mockGroupTreeData);

// 查询数据
const queryData = () => {
  tableLoading.value = true;

  // 使用Mock数据
  setTimeout(() => {
    const params = {
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };

    const result = deviceMockData.mockQueryDeviceList(params);

    if (result.code === 1) {
      tableData.value = result.data.list;
      pagination.total = result.data.total;
    }

    tableLoading.value = false;
  }, 300);
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryForm, {
    deviceName: '',
    status: '',
    deviceType: '',
  });
  pagination.current = 1;
  queryData();
};

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryData();
};

// 显示新增弹窗
const showAddModal = () => {
  isEdit.value = false;
  currentDevice.value = {};
  modalVisible.value = true;
};

// 编辑设备
const editDevice = (record) => {
  isEdit.value = true;
  currentDevice.value = { ...record };
  modalVisible.value = true;
};

// 查看设备
const viewDevice = (record) => {
  message.info(`查看设备: ${record.deviceName}`);
};

// 删除设备
const deleteDevice = async (record) => {
  const result = deviceMockData.mockDeleteDevice(record.id);
  if (result.code === 1) {
    message.success('删除成功');
    queryData();
  }
};

// 批量删除
const batchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的设备');
    return;
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个设备吗?`,
    onOk: async () => {
      const result = deviceMockData.mockBatchDeleteDevice(selectedRowKeys.value);
      if (result.code === 1) {
        message.success(result.msg);
        selectedRowKeys.value = [];
        queryData();
      }
    },
  });
};

// 搜索设备
const searchDevices = () => {
  message.info('正在搜索在线设备...');
};

// 同步设备
const syncDevices = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要同步的设备');
    return;
  }
  message.loading('正在同步设备信息...', 1);
  setTimeout(() => {
    message.success('同步成功');
  }, 1000);
};

// 导出设备
const exportDevices = () => {
  message.info('导出功能开发中...');
};

// 导入设备
const importDevices = () => {
  message.info('导入功能开发中...');
};

// 订阅设备
const subscribeDevices = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要订阅的设备');
    return;
  }
  message.success('订阅成功');
};

// 取消订阅
const unsubscribeDevices = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要取消订阅的设备');
    return;
  }
  message.success('取消订阅成功');
};

// 同步时间
const syncTime = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要同步时间的设备');
    return;
  }
  message.success('时间同步成功');
};

// 维护管理
const maintenanceManage = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要维护的设备');
    return;
  }
  message.info('维护管理功能开发中...');
};

// 查看设备信息
const viewDeviceInfo = () => {
  if (selectedRowKeys.value.length !== 1) {
    message.warning('请选择一个设备查看信息');
    return;
  }
  message.info('查看设备信息功能开发中...');
};

// 目标名单库
const targetList = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择设备');
    return;
  }
  message.info('目标名单库功能开发中...');
};

// 配置设备
const configDevice = (record) => {
  message.info(`配置设备: ${record.deviceName}`);
};

// 表单提交成功
const handleFormSuccess = () => {
  queryData();
};

// 表单取消
const handleFormCancel = () => {
  currentDevice.value = {};
};

// 初始化
onMounted(() => {
  queryData();
});
</script>

<style scoped lang="less">
.smart-video-device {
  padding: 24px;
  background-color: #f0f2f5;
}

.smart-margin-top10 {
  margin-top: 10px;
}

.smart-query-table-form {
  margin-bottom: 0;
}

.smart-query-table-operation {
  margin-bottom: 16px;
}

.smart-table-operate {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;

  .ant-btn-link {
    padding: 0 4px;
  }
}
</style>
