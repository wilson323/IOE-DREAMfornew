<!--
  * 消费设备管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025/11/17
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="consume-device-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <h1>消费设备管理</h1>
        <p>管理园区内所有消费终端设备，包括POS机、自助消费机等</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            添加设备
          </a-button>
          <a-button @click="handleBatchImport">
            <template #icon><UploadOutlined /></template>
            批量导入
          </a-button>
          <a-button @click="handleExport">
            <template #icon><ExportOutlined /></template>
            导出数据
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="searchForm" layout="inline">
        <a-form-item label="设备名称">
          <a-input
            v-model:value="searchForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="设备编号">
          <a-input
            v-model:value="searchForm.deviceCode"
            placeholder="请输入设备编号"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="设备类型">
          <a-select
            v-model:value="searchForm.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="POS">POS机</a-select-option>
            <a-select-option value="SELF_SERVICE">自助消费机</a-select-option>
            <a-select-option value="CANTEEN">食堂消费机</a-select-option>
            <a-select-option value="MOBILE">移动消费终端</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="ONLINE">在线</a-select-option>
            <a-select-option value="OFFLINE">离线</a-select-option>
            <a-select-option value="MAINTENANCE">维护中</a-select-option>
            <a-select-option value="DISABLED">已禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 数据表格 -->
    <a-card :bordered="false">
      <template #title>
        <div class="table-title-wrapper">
          <span>设备列表</span>
          <a-tag color="blue" v-if="tableData.length > 0">
            共 {{ pagination.total }} 台设备
          </a-tag>
        </div>
      </template>

      <template #extra>
        <a-space>
          <a-button
            type="text"
            :icon="h(resolveComponent, 'SyncOutlined')"
            @click="handleRefresh"
            :loading="loading"
          >
            刷新
          </a-button>
          <a-dropdown>
            <template #overlay>
              <a-menu @click="handleBatchAction">
                <a-menu-item key="enable">批量启用</a-menu-item>
                <a-menu-item key="disable">批量禁用</a-menu-item>
                <a-menu-item key="delete">批量删除</a-menu-item>
              </a-menu>
            </template>
            <a-button type="text" :disabled="selectedRowKeys.length === 0">
              批量操作 <DownOutlined />
            </a-button>
          </a-dropdown>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <!-- 设备信息 -->
        <template #deviceInfo="{ record }">
          <div class="device-info">
            <div class="device-name">{{ record.deviceName }}</div>
            <div class="device-code">编号: {{ record.deviceCode }}</div>
          </div>
        </template>

        <!-- 设备类型 -->
        <template #deviceType="{ record }">
          <a-tag :color="getDeviceTypeColor(record.deviceType)">
            {{ getDeviceTypeText(record.deviceType) }}
          </a-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            <component :is="getStatusIcon(record.status)" />
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <!-- 连接状态 -->
        <template #connectionStatus="{ record }">
          <div class="connection-status">
            <div class="status-indicator" :class="record.online ? 'online' : 'offline'"></div>
            <span>{{ record.online ? '在线' : '离线' }}</span>
          </div>
        </template>

        <!-- 最后通信时间 -->
        <template #lastCommunicateTime="{ record }">
          <div v-if="record.lastCommunicateTime">
            {{ formatDateTime(record.lastCommunicateTime) }}
            <div class="communication-ago">
              {{ formatTimeAgo(record.lastCommunicateTime) }}
            </div>
          </div>
          <span v-else class="text-gray">从未通信</span>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleView(record)">
              <template #icon><EyeOutlined /></template>
              详情
            </a-button>
            <a-button type="link" size="small" @click="handleEdit(record)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="handleControl(record)"
              :disabled="!record.online"
            >
              <template #icon><ControlOutlined /></template>
              控制
            </a-button>
            <a-popconfirm
              title="确定要删除这个设备吗？"
              @confirm="handleDelete(record)"
              ok-text="确定"
              cancel-text="取消"
            >
              <a-button type="link" size="small" danger>
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
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

    <!-- 设备控制弹窗 -->
    <DeviceControlModal
      v-model:visible="controlModalVisible"
      :device="currentDevice"
    />

    <!-- 批量导入弹窗 -->
    <BatchImportModal
      v-model:visible="importModalVisible"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, h, resolveComponent, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    UploadOutlined,
    ExportOutlined,
    SearchOutlined,
    ReloadOutlined,
    DownOutlined,
    EyeOutlined,
    EditOutlined,
    ControlOutlined,
    DeleteOutlined,
    WifiOutlined,
    DisconnectOutlined,
    ExclamationCircleOutlined,
    CheckCircleOutlined,
    LoadingOutlined,
  } from '@ant-design/icons-vue';
  import { consumeDeviceApi } from '/@/api/business/consume/consume-device-api';
  import { formatDateTime, formatTimeAgo } from '/@/lib/format';
  import DeviceFormModal from './components/DeviceFormModal.vue';
  import DeviceControlModal from './components/DeviceControlModal.vue';
  import BatchImportModal from './components/BatchImportModal.vue';

  // ----------------------- 响应式数据 -----------------------
  const loading = ref(false);
  const tableData = ref([]);
  const selectedRowKeys = ref([]);

  // 搜索表单
  const searchForm = reactive({
    deviceName: '',
    deviceCode: '',
    deviceType: undefined,
    status: undefined,
  });

  // 分页配置
  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条数据`,
  });

  // 弹窗状态
  const formModalVisible = ref(false);
  const controlModalVisible = ref(false);
  const importModalVisible = ref(false);
  const currentDevice = ref(null);
  const formMode = ref('add'); // add | edit

  // ----------------------- 表格配置 -----------------------
  const columns = [
    {
      title: '设备信息',
      dataIndex: 'deviceInfo',
      key: 'deviceInfo',
      width: 200,
      slots: { customRender: 'deviceInfo' },
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
      width: 120,
      slots: { customRender: 'deviceType' },
      filters: [
        { text: 'POS机', value: 'POS' },
        { text: '自助消费机', value: 'SELF_SERVICE' },
        { text: '食堂消费机', value: 'CANTEEN' },
        { text: '移动消费终端', value: 'MOBILE' },
      ],
    },
    {
      title: '安装位置',
      dataIndex: 'location',
      key: 'location',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      slots: { customRender: 'status' },
      filters: [
        { text: '在线', value: 'ONLINE' },
        { text: '离线', value: 'OFFLINE' },
        { text: '维护中', value: 'MAINTENANCE' },
        { text: '已禁用', value: 'DISABLED' },
      ],
    },
    {
      title: '连接状态',
      dataIndex: 'connectionStatus',
      key: 'connectionStatus',
      width: 120,
      slots: { customRender: 'connectionStatus' },
    },
    {
      title: '最后通信时间',
      dataIndex: 'lastCommunicateTime',
      key: 'lastCommunicateTime',
      width: 160,
      slots: { customRender: 'lastCommunicateTime' },
      sorter: true,
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
      dataIndex: 'action',
      key: 'action',
      width: 200,
      fixed: 'right',
      slots: { customRender: 'action' },
    },
  ];

  // 行选择配置
  const rowSelection = computed(() => ({
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys) => {
      selectedRowKeys.value = keys;
    },
    onSelectAll: (selected, selectedRows, changeRows) => {
      console.log('全选', selected, selectedRows, changeRows);
    },
  }));

  // ----------------------- 生命周期 -----------------------
  onMounted(() => {
    queryTableData();
  });

  // ----------------------- 数据查询 -----------------------
  const queryTableData = async () => {
    try {
      loading.value = true;

      const params = {
        ...searchForm,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };

      // TODO: 调用后端API
      // const res = await consumeDeviceApi.queryDeviceList(params);
      // if (res.data) {
      //   tableData.value = res.data.list;
      //   pagination.total = res.data.total;
      // }

      // 模拟数据
      setTimeout(() => {
        tableData.value = generateMockData();
        pagination.total = tableData.value.length;
      }, 500);

    } catch (error) {
      console.error('查询设备列表失败:', error);
      message.error('查询设备列表失败');
    } finally {
      loading.value = false;
    }
  };

  // 生成模拟数据
  const generateMockData = () => {
    const mockData = [];
    const deviceTypes = ['POS', 'SELF_SERVICE', 'CANTEEN', 'MOBILE'];
    const statuses = ['ONLINE', 'OFFLINE', 'MAINTENANCE', 'DISABLED'];
    const locations = ['第一食堂', '第二食堂', '便利店', '超市', '咖啡厅', '餐厅'];

    for (let i = 1; i <= 50; i++) {
      const status = statuses[Math.floor(Math.random() * statuses.length)];
      mockData.push({
        id: i,
        deviceName: `消费终端${i.toString().padStart(3, '0')}`,
        deviceCode: `CON${i.toString().padStart(6, '0')}`,
        deviceType: deviceTypes[Math.floor(Math.random() * deviceTypes.length)],
        location: locations[Math.floor(Math.random() * locations.length)],
        status: status,
        online: status === 'ONLINE',
        lastCommunicateTime: status === 'ONLINE' ? new Date(Date.now() - Math.random() * 300000) : new Date(Date.now() - Math.random() * 86400000),
        createTime: new Date(Date.now() - Math.random() * 2592000000),
        description: `设备${i}的描述信息`,
      });
    }
    return mockData;
  };

  // ----------------------- 事件处理 -----------------------
  const handleSearch = () => {
    pagination.current = 1;
    queryTableData();
  };

  const handleReset = () => {
    Object.keys(searchForm).forEach(key => {
      searchForm[key] = undefined;
    });
    searchForm.deviceName = '';
    searchForm.deviceCode = '';
    handleSearch();
  };

  const handleRefresh = () => {
    queryTableData();
  };

  const handleTableChange = (pag, filters, sorter) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    queryTableData();
  };

  const handleAdd = () => {
    currentDevice.value = null;
    formMode.value = 'add';
    formModalVisible.value = true;
  };

  const handleEdit = (record) => {
    currentDevice.value = { ...record };
    formMode.value = 'edit';
    formModalVisible.value = true;
  };

  const handleView = (record) => {
    currentDevice.value = record;
    // 跳转到详情页面或打开详情弹窗
    message.info(`查看设备详情: ${record.deviceName}`);
  };

  const handleControl = (record) => {
    if (!record.online) {
      message.warning('设备离线，无法进行远程控制');
      return;
    }
    currentDevice.value = record;
    controlModalVisible.value = true;
  };

  const handleDelete = (record) => {
    // TODO: 调用删除API
    message.success(`删除设备: ${record.deviceName}`);
    queryTableData();
  };

  const handleBatchAction = ({ key }) => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要操作的设备');
      return;
    }

    const actionText = {
      enable: '启用',
      disable: '禁用',
      delete: '删除',
    }[key];

    Modal.confirm({
      title: `批量${actionText}确认`,
      content: `确定要批量${actionText}选中的 ${selectedRowKeys.value.length} 台设备吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        // TODO: 调用批量操作API
        message.success(`批量${actionText}成功`);
        selectedRowKeys.value = [];
        queryTableData();
      },
    });
  };

  const handleBatchImport = () => {
    importModalVisible.value = true;
  };

  const handleExport = () => {
    // TODO: 调用导出API
    message.info('导出功能开发中...');
  };

  const handleFormSuccess = () => {
    formModalVisible.value = false;
    queryTableData();
    message.success(formMode.value === 'add' ? '添加设备成功' : '编辑设备成功');
  };

  const handleImportSuccess = () => {
    importModalVisible.value = false;
    queryTableData();
    message.success('批量导入成功');
  };

  // ----------------------- 工具方法 -----------------------
  const getDeviceTypeText = (type) => {
    const typeMap = {
      POS: 'POS机',
      SELF_SERVICE: '自助消费机',
      CANTEEN: '食堂消费机',
      MOBILE: '移动消费终端',
    };
    return typeMap[type] || type;
  };

  const getDeviceTypeColor = (type) => {
    const colorMap = {
      POS: 'blue',
      SELF_SERVICE: 'green',
      CANTEEN: 'orange',
      MOBILE: 'purple',
    };
    return colorMap[type] || 'default';
  };

  const getStatusText = (status) => {
    const statusMap = {
      ONLINE: '在线',
      OFFLINE: '离线',
      MAINTENANCE: '维护中',
      DISABLED: '已禁用',
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status) => {
    const colorMap = {
      ONLINE: 'success',
      OFFLINE: 'default',
      MAINTENANCE: 'warning',
      DISABLED: 'error',
    };
    return colorMap[status] || 'default';
  };

  const getStatusIcon = (status) => {
    const iconMap = {
      ONLINE: WifiOutlined,
      OFFLINE: DisconnectOutlined,
      MAINTENANCE: ExclamationCircleOutlined,
      DISABLED: CheckCircleOutlined,
    };
    return iconMap[status] || WifiOutlined;
  };
</script>

<style lang="less" scoped>
  .consume-device-page {
    padding: 24px;
    background: #f5f5f5;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .page-title {
        h1 {
          margin: 0 0 8px 0;
          font-size: 28px;
          font-weight: 700;
          color: #262626;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 16px;
        }
      }
    }

    .search-card {
      margin-bottom: 16px;
    }

    .table-title-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;
      font-weight: 600;
    }

    .device-info {
      .device-name {
        font-weight: 600;
        color: #262626;
        margin-bottom: 4px;
      }

      .device-code {
        font-size: 12px;
        color: #8c8c8c;
      }
    }

    .connection-status {
      display: flex;
      align-items: center;
      gap: 8px;

      .status-indicator {
        width: 8px;
        height: 8px;
        border-radius: 50%;

        &.online {
          background-color: #52c41a;
        }

        &.offline {
          background-color: #ff4d4f;
        }
      }
    }

    .communication-ago {
      font-size: 12px;
      color: #8c8c8c;
      margin-top: 4px;
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

      .device-info {
        .device-name {
          font-size: 14px;
        }

        .device-code {
          font-size: 11px;
        }
      }
    }
  }
</style>