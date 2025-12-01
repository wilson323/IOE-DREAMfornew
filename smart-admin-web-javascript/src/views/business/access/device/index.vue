<!--
  * 门禁设备管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-device-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h2>门禁设备管理</h2>
        <p>管理所有门禁设备的状态、配置和监控</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button type="primary" @click="handleAddDevice">
            <template #icon><PlusOutlined /></template>
            添加设备
          </a-button>
          <a-button @click="handleExport">
            <template #icon><ExportOutlined /></template>
            导出数据
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 设备统计卡片 -->
    <div class="stats-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="设备总数"
              :value="deviceStore.deviceTotal"
              :loading="deviceStore.statsLoading"
            >
              <template #prefix><DesktopOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card online">
            <a-statistic
              title="在线设备"
              :value="deviceStore.onlineDeviceCount"
              :loading="deviceStore.statsLoading"
            >
              <template #prefix><WifiOutlined /></template>
            </a-statistic>
            <div class="rate">{{ deviceStore.onlineRate }}%</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card offline">
            <a-statistic
              title="离线设备"
              :value="deviceStore.offlineDeviceCount"
              :loading="deviceStore.statsLoading"
            >
              <template #prefix><DisconnectOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card fault">
            <a-statistic
              title="故障设备"
              :value="deviceStore.faultDeviceCount"
              :loading="deviceStore.statsLoading"
            >
              <template #prefix><ExclamationCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 查询表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form
        :model="queryForm"
        layout="inline"
        @finish="handleSearch"
      >
        <a-form-item label="设备编码" name="deviceCode">
          <a-input
            v-model:value="queryForm.deviceCode"
            placeholder="请输入设备编码"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="设备名称" name="deviceName">
          <a-input
            v-model:value="queryForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="设备类型" name="deviceType">
          <a-select
            v-model:value="queryForm.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 180px"
          >
            <a-select-option value="door_controller">门禁控制器</a-select-option>
            <a-select-option value="turnstile">闸机</a-select-option>
            <a-select-option value="gate">门禁门</a-select-option>
            <a-select-option value="barrier">道闸</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="设备状态" name="status">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择设备状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="normal">正常</a-select-option>
            <a-select-option value="maintenance">维护中</a-select-option>
            <a-select-option value="fault">故障</a-select-option>
            <a-select-option value="offline">离线</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="deviceStore.deviceLoading">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ClearOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 设备列表 -->
    <a-card :bordered="false">
      <!-- 表格工具栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <a-space>
            <a-button
              type="primary"
              danger
              :disabled="!deviceStore.selectedDeviceIds.length"
              @click="handleBatchDelete"
            >
              <template #icon><DeleteOutlined /></template>
              批量删除
            </a-button>
            <a-button
              :disabled="!deviceStore.selectedDeviceIds.length"
              @click="handleBatchStatusUpdate"
            >
              <template #icon><EditOutlined /></template>
              批量更新状态
            </a-button>
          </a-space>
        </div>
        <div class="toolbar-right">
          <a-radio-group v-model:value="tableSize" button-style="solid">
            <a-radio-button value="small">紧凑</a-radio-button>
            <a-radio-button value="middle">默认</a-radio-button>
            <a-radio-button value="large">宽松</a-radio-button>
          </a-radio-group>
        </div>
      </div>

      <!-- 设备表格 -->
      <a-table
        :columns="tableColumns"
        :data-source="deviceStore.deviceList"
        :loading="deviceStore.deviceLoading"
        :pagination="pagination"
        :row-selection="rowSelection"
        :size="tableSize"
        row-key="deviceId"
        @change="handleTableChange"
      >
        <!-- 设备状态 -->
        <template #status="{ record }">
          <a-tag
            :color="getStatusColor(record.status)"
            :icon="getStatusIcon(record.status)"
          >
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <!-- 在线状态 -->
        <template #onlineStatus="{ record }">
          <a-badge
            :status="record.onlineStatus === 'online' ? 'success' : 'error'"
            :text="record.onlineStatus === 'online' ? '在线' : '离线'"
          />
        </template>

        <!-- 设备类型 -->
        <template #deviceType="{ record }">
          <a-tag color="blue">{{ record.deviceTypeName || record.deviceType }}</a-tag>
        </template>

        <!-- 最后心跳时间 -->
        <template #lastHeartbeatTime="{ record }">
          <span v-if="record.lastHeartbeatTime">
            {{ formatDateTime(record.lastHeartbeatTime) }}
          </span>
          <span v-else class="text-gray">-</span>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              <template #icon><EyeOutlined /></template>
              详情
            </a-button>
            <a-button type="link" size="small" @click="handleEditDevice(record)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              :disabled="record.onlineStatus !== 'online'"
              @click="handleRemoteOpen(record)"
            >
              <template #icon><UnlockOutlined /></template>
              远程开门
            </a-button>
            <a-dropdown>
              <a-button type="link" size="small">
                更多
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleRestartDevice(record)">
                    <RedoOutlined />
                    重启设备
                  </a-menu-item>
                  <a-menu-item @click="handleSyncDeviceTime(record)">
                    <ClockCircleOutlined />
                    时间同步
                  </a-menu-item>
                  <a-menu-item @click="handleSyncConfig(record)">
                    <SyncOutlined />
                    同步配置
                  </a-menu-item>
                  <a-menu-item @click="handleTestConnection(record)">
                    <ApiOutlined />
                    测试连接
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item
                    @click="handleDeleteDevice(record)"
                    :disabled="record.status !== 'offline'"
                  >
                    <DeleteOutlined />
                    删除设备
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 设备表单弹窗 -->
    <DeviceFormModal
      v-model:visible="formModalVisible"
      :device="currentDevice"
      :mode="formMode"
      @success="handleFormSuccess"
    />

    <!-- 设备详情弹窗 -->
    <DeviceDetailModal
      v-model:visible="detailModalVisible"
      :device="currentDevice"
    />

    <!-- 批量更新状态弹窗 -->
    <BatchStatusUpdateModal
      v-model:visible="batchStatusModalVisible"
      :device-ids="deviceStore.selectedDeviceIds"
      @success="handleBatchStatusSuccess"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    ExportOutlined,
    ReloadOutlined,
    SearchOutlined,
    ClearOutlined,
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    UnlockOutlined,
    DownOutlined,
    RedoOutlined,
    ClockCircleOutlined,
    SyncOutlined,
    ApiOutlined,
    DesktopOutlined,
    WifiOutlined,
    DisconnectOutlined,
    ExclamationCircleOutlined,
  } from '@ant-design/icons-vue';
  import { useAccessDeviceStore } from '/@/store/modules/business/access-device';
  import { formatDateTime } from '/@/lib/format';
  import DeviceFormModal from './components/DeviceFormModal.vue';
  import DeviceDetailModal from './components/DeviceDetailModal.vue';
  import BatchStatusUpdateModal from './components/BatchStatusUpdateModal.vue';

  // 状态管理
  const deviceStore = useAccessDeviceStore();

  // 响应式数据
  const tableSize = ref('middle');
  const formModalVisible = ref(false);
  const detailModalVisible = ref(false);
  const batchStatusModalVisible = ref(false);
  const currentDevice = ref(null);
  const formMode = ref('add');

  // 查询表单
  const queryForm = reactive({
    deviceCode: '',
    deviceName: '',
    deviceType: undefined,
    status: undefined,
  });

  // 表格列定义
  const tableColumns = [
    {
      title: '设备编码',
      dataIndex: 'deviceCode',
      key: 'deviceCode',
      width: 150,
      fixed: 'left',
    },
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      key: 'deviceName',
      width: 180,
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
      width: 120,
      slots: { customRender: 'deviceType' },
    },
    {
      title: '安装位置',
      dataIndex: 'location',
      key: 'location',
      width: 200,
    },
    {
      title: '设备状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      slots: { customRender: 'status' },
    },
    {
      title: '在线状态',
      dataIndex: 'onlineStatus',
      key: 'onlineStatus',
      width: 100,
      slots: { customRender: 'onlineStatus' },
    },
    {
      title: 'IP地址',
      dataIndex: 'ip',
      key: 'ip',
      width: 130,
    },
    {
      title: '最后心跳',
      dataIndex: 'lastHeartbeatTime',
      key: 'lastHeartbeatTime',
      width: 160,
      slots: { customRender: 'lastHeartbeatTime' },
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
      customRender: ({ text }) => formatDateTime(text),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      slots: { customRender: 'action' },
    },
  ];

  // 分页配置
  const pagination = computed(() => ({
    current: deviceStore.queryParams.pageNum,
    pageSize: deviceStore.queryParams.pageSize,
    total: deviceStore.deviceTotal,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条记录`,
  }));

  // 行选择配置
  const rowSelection = computed(() => ({
    selectedRowKeys: deviceStore.selectedDeviceIds,
    onChange: (selectedRowKeys) => {
      deviceStore.setSelectedDeviceIds(selectedRowKeys);
    },
    getCheckboxProps: (record) => ({
      disabled: record.status === 'fault',
    }),
  }));

  // 方法
  const handleSearch = () => {
    deviceStore.setQueryParams({
      ...queryForm,
      pageNum: 1,
    });
    deviceStore.fetchDeviceList();
  };

  const handleReset = () => {
    Object.assign(queryForm, {
      deviceCode: '',
      deviceName: '',
      deviceType: undefined,
      status: undefined,
    });
    deviceStore.resetQueryParams();
    deviceStore.fetchDeviceList();
  };

  const handleRefresh = () => {
    deviceStore.fetchDeviceList();
    deviceStore.fetchDeviceStats();
  };

  const handleAddDevice = () => {
    currentDevice.value = null;
    formMode.value = 'add';
    formModalVisible.value = true;
  };

  const handleEditDevice = (device) => {
    currentDevice.value = device;
    formMode.value = 'edit';
    formModalVisible.value = true;
  };

  const handleViewDetail = (device) => {
    currentDevice.value = device;
    detailModalVisible.value = true;
  };

  const handleDeleteDevice = (device) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除设备 "${device.deviceName}" 吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        const success = await deviceStore.deleteDevice(device.deviceId);
        if (success) {
          message.success('删除成功');
        }
      },
    });
  };

  const handleBatchDelete = () => {
    Modal.confirm({
      title: '批量删除确认',
      content: `确定要删除选中的 ${deviceStore.selectedDeviceIds.length} 个设备吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        const success = await deviceStore.batchDeleteDevices(deviceStore.selectedDeviceIds);
        if (success) {
          message.success('批量删除成功');
        }
      },
    });
  };

  const handleBatchStatusUpdate = () => {
    batchStatusModalVisible.value = true;
  };

  const handleRemoteOpen = async (device) => {
    Modal.confirm({
      title: '远程开门确认',
      content: `确定要远程打开设备 "${device.deviceName}" 吗？`,
      okText: '确认开门',
      cancelText: '取消',
      onOk: async () => {
        const success = await deviceStore.remoteOpenDoor(device.deviceId);
        if (success) {
          message.success('远程开门成功');
        }
      },
    });
  };

  const handleRestartDevice = (device) => {
    Modal.confirm({
      title: '重启设备确认',
      content: `确定要重启设备 "${device.deviceName}" 吗？重启过程可能需要几分钟时间。`,
      okText: '确认重启',
      cancelText: '取消',
      onOk: async () => {
        const success = await deviceStore.restartDevice(device.deviceId);
        if (success) {
          message.success('设备重启成功');
        }
      },
    });
  };

  const handleSyncDeviceTime = (device) => {
    Modal.confirm({
      title: '时间同步确认',
      content: `确定要将服务器时间同步到设备 "${device.deviceName}" 吗？`,
      okText: '确认同步',
      cancelText: '取消',
      onOk: async () => {
        const success = await deviceStore.syncDeviceTime(device.deviceId);
        if (success) {
          message.success('设备时间同步成功');
        }
      },
    });
  };

  const handleSyncConfig = async (device) => {
    const success = await deviceStore.syncDeviceConfig(device.deviceId);
    if (success) {
      message.success('配置同步成功');
    }
  };

  const handleTestConnection = async (device) => {
    try {
      const response = await accessDeviceApi.testDeviceConnection(device.deviceId);
      if (response.code === 1) {
        message.success('设备连接正常');
      } else {
        message.error('设备连接失败');
      }
    } catch (error) {
      message.error('连接测试失败');
    }
  };

  const handleExport = () => {
    message.info('导出功能开发中...');
  };

  const handleTableChange = (paginationConfig) => {
    deviceStore.setQueryParams({
      pageNum: paginationConfig.current,
      pageSize: paginationConfig.pageSize,
    });
    deviceStore.fetchDeviceList();
  };

  const handleFormSuccess = () => {
    formModalVisible.value = false;
    deviceStore.fetchDeviceList();
  };

  const handleBatchStatusSuccess = () => {
    batchStatusModalVisible.value = false;
    deviceStore.fetchDeviceList();
  };

  // 辅助方法
  const getStatusColor = (status) => {
    const colorMap = {
      normal: 'green',
      maintenance: 'orange',
      fault: 'red',
      offline: 'default',
    };
    return colorMap[status] || 'default';
  };

  const getStatusIcon = (status) => {
    const iconMap = {
      normal: 'check-circle',
      maintenance: 'exclamation-circle',
      fault: 'close-circle',
      offline: 'stop',
    };
    return iconMap[status] || 'question-circle';
  };

  const getStatusText = (status) => {
    const textMap = {
      normal: '正常',
      maintenance: '维护中',
      fault: '故障',
      offline: '离线',
    };
    return textMap[status] || status;
  };

  // 生命周期
  onMounted(async () => {
    await Promise.all([
      deviceStore.fetchDeviceList(),
      deviceStore.fetchDeviceStats(),
      deviceStore.fetchGroupTree(),
    ]);
  });

  onUnmounted(() => {
    deviceStore.clearDeviceList();
  });
</script>

<style lang="less" scoped>
  .access-device-page {
    padding: 24px;
    background: #f5f5f5;
    min-height: 100vh;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .page-title {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 14px;
        }
      }
    }

    .stats-cards {
      margin-bottom: 24px;

      .stat-card {
        text-align: center;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &.online {
          border-left: 4px solid #52c41a;
        }

        &.offline {
          border-left: 4px solid #ff4d4f;
        }

        &.fault {
          border-left: 4px solid #faad14;
        }

        .rate {
          margin-top: 8px;
          font-size: 16px;
          font-weight: 600;
          color: #52c41a;
        }
      }
    }

    .search-card {
      margin-bottom: 24px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .toolbar-left {
        // 左侧工具栏样式
      }

      .toolbar-right {
        // 右侧工具栏样式
      }
    }

    .text-gray {
      color: #8c8c8c;
    }

    // 响应式布局
    @media (max-width: 768px) {
      padding: 16px;

      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .stats-cards {
        .ant-col {
          margin-bottom: 16px;
        }
      }
    }
  }
</style>
