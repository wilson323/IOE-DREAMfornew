<!--
 * 考勤设备管理页面
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <div class="attendance-device-management">
    <!-- 页面头部 -->
    <a-page-header
      title="考勤设备管理"
      sub-title="管理考勤设备注册、配置和监控"
      @back="handleBack"
    >
      <template #extra>
        <a-space>
          <a-button @click="handleBatchImport">
            <template #icon><ImportOutlined /></template>
            批量导入
          </a-button>
          <a-button @click="handleExportDevices">
            <template #icon><ExportOutlined /></template>
            导出设备
          </a-button>
          <a-button type="primary" @click="handleAddDevice">
            <template #icon><PlusOutlined /></template>
            新增设备
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <!-- 搜索和筛选 -->
    <a-card :bordered="false" class="search-card mb-4">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="设备名称">
          <a-input
            v-model:value="queryForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="设备编码">
          <a-input
            v-model:value="queryForm.deviceCode"
            placeholder="请输入设备编码"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="设备类型">
          <a-select
            v-model:value="queryForm.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="FINGERPRINT">指纹考勤机</a-select-option>
            <a-select-option value="FACE">人脸识别考勤机</a-select-option>
            <a-select-option value="CARD">IC卡考勤机</a-select-option>
            <a-select-option value="PASSWORD">密码考勤机</a-select-option>
            <a-select-option value="MOBILE">移动端打卡</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="所属区域">
          <a-tree-select
            v-model:value="queryForm.areaId"
            :tree-data="areaTreeData"
            placeholder="请选择所属区域"
            allow-clear
            style="width: 200px"
            :field-names="{ label: 'title', value: 'key', children: 'children' }"
          />
        </a-form-item>
        <a-form-item label="设备状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择设备状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">在线</a-select-option>
            <a-select-option :value="0">离线</a-select-option>
            <a-select-option :value="2">故障</a-select-option>
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
    </a-card>

    <!-- 设备列表 -->
    <a-card :bordered="false">
      <a-table
        :columns="tableColumns"
        :data-source="tableData"
        :loading="tableLoading"
        :pagination="pagination"
        :row-key="record => record.deviceId"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
      >
        <!-- 设备信息列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'deviceInfo'">
            <div class="device-info">
              <div class="device-name">{{ record.deviceName }}</div>
              <div class="device-code text-gray-500 text-sm">{{ record.deviceCode }}</div>
            </div>
          </template>

          <!-- 设备状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-badge :status="getStatusBadge(record.status)" :text="getStatusText(record.status)" />
          </template>

          <!-- 连接状态列 -->
          <template v-else-if="column.key === 'connectionStatus'">
            <div class="connection-status">
              <a-badge
                :status="record.lastHeartbeatTime && isOnline(record.lastHeartbeatTime) ? 'success' : 'error'"
                :text="record.lastHeartbeatTime && isOnline(record.lastHeartbeatTime) ? '在线' : '离线'"
              />
              <div class="last-heartbeat text-xs text-gray-400">
                {{ formatLastHeartbeat(record.lastHeartbeatTime) }}
              </div>
            </div>
          </template>

          <!-- 设备类型列 -->
          <template v-else-if="column.key === 'deviceType'">
            <a-tag :color="getDeviceTypeColor(record.deviceType)">
              {{ getDeviceTypeText(record.deviceType) }}
            </a-tag>
          </template>

          <!-- 区域信息列 -->
          <template v-else-if="column.key === 'areaName'">
            <span v-if="record.areaName">{{ record.areaName }}</span>
            <span v-else class="text-gray-400">未分配</span>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewDevice(record)">
                详情
              </a-button>
              <a-button type="link" size="small" @click="handleEditDevice(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handleConfigureDevice(record)">
                配置
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleTestDevice(record)">
                      <TestOutlined />
                      测试连接
                    </a-menu-item>
                    <a-menu-item @click="handleRestartDevice(record)">
                      <ReloadOutlined />
                      重启设备
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="handleDeleteDevice(record)" danger>
                      <DeleteOutlined />
                      删除设备
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>

        <!-- 批量操作工具栏 -->
        <template #title>
          <div class="table-toolbar">
            <div class="toolbar-left">
              <a-alert
                v-if="selectedRowKeys.length > 0"
                :message="`已选择 ${selectedRowKeys.length} 个设备`"
                type="info"
                show-icon
                class="mb-2"
              />
            </div>
            <div class="toolbar-right">
              <a-space v-if="selectedRowKeys.length > 0">
                <a-button @click="handleBatchConfigure">批量配置</a-button>
                <a-button @click="handleBatchRestart">批量重启</a-button>
                <a-button danger @click="handleBatchDelete">批量删除</a-button>
              </a-space>
            </div>
          </div>
        </template>
      </a-table>
    </a-card>

    <!-- 设备配置弹窗 -->
    <DeviceConfigModal
      v-model:visible="deviceConfigModalVisible"
      :device-data="currentDeviceData"
      :mode="deviceConfigMode"
      @success="handleDeviceConfigSuccess"
    />

    <!-- 设备导入弹窗 -->
    <DeviceImportModal
      v-model:visible="deviceImportModalVisible"
      @success="handleDeviceImportSuccess"
    />

    <!-- 设备详情弹窗 -->
    <DeviceDetailModal
      v-model:visible="deviceDetailModalVisible"
      :device-data="currentDeviceData"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    ExportOutlined,
    ImportOutlined,
    DownOutlined,
    TestOutlined
  } from '@ant-design/icons-vue';

  // 组件引入
  import DeviceConfigModal from './components/DeviceConfigModal.vue';
  import DeviceImportModal from './components/DeviceImportModal.vue';
  import DeviceDetailModal from './components/DeviceDetailModal.vue';

  // API 引入
  import { attendanceDeviceApi } from '/@/api/business/attendance/attendance-device-api';

  // ============ 响应式数据 ============
  const queryForm = reactive({
    deviceName: '',
    deviceCode: '',
    deviceType: undefined,
    areaId: null,
    status: undefined
  });

  // 表格数据
  const tableData = ref([]);
  const tableLoading = ref(false);
  const selectedRowKeys = ref([]);

  // 分页配置
  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
  });

  // 区域树数据
  const areaTreeData = ref([]);

  // 弹窗状态
  const deviceConfigModalVisible = ref(false);
  const deviceImportModalVisible = ref(false);
  const deviceDetailModalVisible = ref(false);
  const deviceConfigMode = ref('add');
  const currentDeviceData = ref(null);

  // 定时器
  const heartbeatTimer = ref(null);

  // ============ 计算属性 ============
  const tableColumns = [
    {
      title: '设备信息',
      key: 'deviceInfo',
      width: 200,
      fixed: 'left'
    },
    {
      title: '设备类型',
      key: 'deviceType',
      width: 120
    },
    {
      title: '所属区域',
      dataIndex: 'areaName',
      key: 'areaName',
      width: 150
    },
    {
      title: '设备状态',
      key: 'status',
      width: 100
    },
    {
      title: '连接状态',
      key: 'connectionStatus',
      width: 120
    },
    {
      title: '最后心跳',
      dataIndex: 'lastHeartbeatTime',
      key: 'lastHeartbeatTime',
      width: 150,
      customRender: ({ text }) => formatLastHeartbeat(text)
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 130
    },
    {
      title: '固件版本',
      dataIndex: 'firmwareVersion',
      key: 'firmwareVersion',
      width: 100
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 150
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right'
    }
  ];

  // ============ 生命周期 ============
  onMounted(() => {
    loadDeviceList();
    loadAreaTreeData();
    startHeartbeatMonitor();
  });

  onUnmounted(() => {
    stopHeartbeatMonitor();
  });

  // ============ 方法 ============

  // 加载设备列表
  const loadDeviceList = async () => {
    try {
      tableLoading.value = true;
      const params = {
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
        ...queryForm
      };

      const response = await attendanceDeviceApi.getDeviceList(params);

      if (response.success) {
        tableData.value = response.data.list || [];
        pagination.total = response.data.total || 0;
      } else {
        message.error(response.message || '获取设备列表失败');
      }
    } catch (error) {
      console.error('获取设备列表失败:', error);
      message.error('获取设备列表失败');
    } finally {
      tableLoading.value = false;
    }
  };

  // 加载区域树数据
  const loadAreaTreeData = async () => {
    try {
      const response = await attendanceDeviceApi.getAreaTreeOptions();
      if (response.success) {
        areaTreeData.value = response.data || [];
      }
    } catch (error) {
      console.error('获取区域树数据失败:', error);
    }
  };

  // 搜索
  const handleSearch = () => {
    pagination.current = 1;
    loadDeviceList();
  };

  // 重置
  const handleReset = () => {
    Object.assign(queryForm, {
      deviceName: '',
      deviceCode: '',
      deviceType: undefined,
      areaId: null,
      status: undefined
    });
    pagination.current = 1;
    loadDeviceList();
  };

  // 表格变化处理
  const handleTableChange = (pag, filters, sorter) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadDeviceList();
  };

  // 选择变化处理
  const onSelectChange = (keys) => {
    selectedRowKeys.value = keys;
  };

  // 新增设备
  const handleAddDevice = () => {
    currentDeviceData.value = null;
    deviceConfigMode.value = 'add';
    deviceConfigModalVisible.value = true;
  };

  // 编辑设备
  const handleEditDevice = (record) => {
    currentDeviceData.value = { ...record };
    deviceConfigMode.value = 'edit';
    deviceConfigModalVisible.value = true;
  };

  // 配置设备
  const handleConfigureDevice = (record) => {
    currentDeviceData.value = { ...record };
    deviceConfigMode.value = 'config';
    deviceConfigModalVisible.value = true;
  };

  // 查看设备详情
  const handleViewDevice = (record) => {
    currentDeviceData.value = { ...record };
    deviceDetailModalVisible.value = true;
  };

  // 测试设备连接
  const handleTestDevice = async (record) => {
    try {
      message.loading('正在测试设备连接...', 0);
      const response = await attendanceDeviceApi.testDeviceConnection({ deviceId: record.deviceId });
      message.destroy();

      if (response.success) {
        message.success('设备连接测试成功');
      } else {
        message.error(response.message || '设备连接测试失败');
      }
    } catch (error) {
      message.destroy();
      console.error('测试设备连接失败:', error);
      message.error('设备连接测试失败');
    }
  };

  // 重启设备
  const handleRestartDevice = (record) => {
    Modal.confirm({
      title: '确认重启设备',
      content: `确定要重启设备 "${record.deviceName}" 吗？设备重启后可能需要几分钟时间恢复。`,
      onOk: async () => {
        try {
          const response = await attendanceDeviceApi.restartDevice({ deviceId: record.deviceId });
          if (response.success) {
            message.success('设备重启命令已发送');
            loadDeviceList(); // 刷新列表
          } else {
            message.error(response.message || '设备重启失败');
          }
        } catch (error) {
          console.error('重启设备失败:', error);
          message.error('设备重启失败');
        }
      }
    });
  };

  // 删除设备
  const handleDeleteDevice = (record) => {
    Modal.confirm({
      title: '确认删除设备',
      content: `确定要删除设备 "${record.deviceName}" 吗？删除后将无法恢复。`,
      okText: '确认删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          const response = await attendanceDeviceApi.deleteDevice(record.deviceId);
          if (response.success) {
            message.success('设备删除成功');
            loadDeviceList(); // 刷新列表
          } else {
            message.error(response.message || '设备删除失败');
          }
        } catch (error) {
          console.error('删除设备失败:', error);
          message.error('设备删除失败');
        }
      }
    });
  };

  // 批量导入
  const handleBatchImport = () => {
    deviceImportModalVisible.value = true;
  };

  // 导出设备
  const handleExportDevices = async () => {
    try {
      const params = {
        deviceIds: selectedRowKeys.value.length > 0 ? selectedRowKeys.value : undefined,
        ...queryForm
      };

      const response = await attendanceDeviceApi.exportDevices(params);

      // 创建下载链接
      const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `考勤设备列表_${new Date().toISOString().slice(0, 10)}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      message.success('设备列表导出成功');
    } catch (error) {
      console.error('导出设备列表失败:', error);
      message.error('导出设备列表失败');
    }
  };

  // 批量配置
  const handleBatchConfigure = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请先选择要配置的设备');
      return;
    }
    // 实现批量配置逻辑
    message.info('批量配置功能开发中...');
  };

  // 批量重启
  const handleBatchRestart = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请先选择要重启的设备');
      return;
    }

    Modal.confirm({
      title: '确认批量重启设备',
      content: `确定要重启选中的 ${selectedRowKeys.value.length} 个设备吗？`,
      onOk: async () => {
        try {
          const response = await attendanceDeviceApi.batchRestartDevices({ deviceIds: selectedRowKeys.value });
          if (response.success) {
            message.success('批量重启命令已发送');
            loadDeviceList(); // 刷新列表
            selectedRowKeys.value = []; // 清空选择
          } else {
            message.error(response.message || '批量重启失败');
          }
        } catch (error) {
          console.error('批量重启设备失败:', error);
          message.error('批量重启设备失败');
        }
      }
    });
  };

  // 批量删除
  const handleBatchDelete = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请先选择要删除的设备');
      return;
    }

    Modal.confirm({
      title: '确认批量删除设备',
      content: `确定要删除选中的 ${selectedRowKeys.value.length} 个设备吗？删除后将无法恢复。`,
      okText: '确认删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          const response = await attendanceDeviceApi.batchDeleteDevices({ deviceIds: selectedRowKeys.value });
          if (response.success) {
            message.success('批量删除成功');
            loadDeviceList(); // 刷新列表
            selectedRowKeys.value = []; // 清空选择
          } else {
            message.error(response.message || '批量删除失败');
          }
        } catch (error) {
          console.error('批量删除设备失败:', error);
          message.error('批量删除设备失败');
        }
      }
    });
  };

  // 设备配置成功回调
  const handleDeviceConfigSuccess = () => {
    deviceConfigModalVisible.value = false;
    loadDeviceList(); // 刷新列表
  };

  // 设备导入成功回调
  const handleDeviceImportSuccess = () => {
    deviceImportModalVisible.value = false;
    loadDeviceList(); // 刷新列表
  };

  // 返回按钮处理
  const handleBack = () => {
    // 返回上级页面
    window.history.back();
  };

  // ============ 辅助方法 ============

  // 获取状态徽标
  const getStatusBadge = (status) => {
    const statusMap = {
      1: 'success',
      0: 'default',
      2: 'error'
    };
    return statusMap[status] || 'default';
  };

  // 获取状态文本
  const getStatusText = (status) => {
    const statusMap = {
      1: '正常',
      0: '禁用',
      2: '故障'
    };
    return statusMap[status] || '未知';
  };

  // 判断是否在线
  const isOnline = (lastHeartbeatTime) => {
    if (!lastHeartbeatTime) return false;
    const now = new Date();
    const heartbeat = new Date(lastHeartbeatTime);
    return (now - heartbeat) < 5 * 60 * 1000; // 5分钟内在线
  };

  // 格式化最后心跳时间
  const formatLastHeartbeat = (time) => {
    if (!time) return '从未';

    const now = new Date();
    const heartbeat = new Date(time);
    const diff = now - heartbeat;

    if (diff < 60 * 1000) {
      return '刚刚';
    } else if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000));
      return `${minutes}分钟前`;
    } else if (diff < 24 * 60 * 60 * 1000) {
      const hours = Math.floor(diff / (60 * 60 * 1000));
      return `${hours}小时前`;
    } else {
      return heartbeat.toLocaleDateString();
    }
  };

  // 获取设备类型颜色
  const getDeviceTypeColor = (type) => {
    const colorMap = {
      'FINGERPRINT': 'blue',
      'FACE': 'green',
      'CARD': 'orange',
      'PASSWORD': 'purple',
      'MOBILE': 'cyan'
    };
    return colorMap[type] || 'default';
  };

  // 获取设备类型文本
  const getDeviceTypeText = (type) => {
    const textMap = {
      'FINGERPRINT': '指纹考勤机',
      'FACE': '人脸识别考勤机',
      'CARD': 'IC卡考勤机',
      'PASSWORD': '密码考勤机',
      'MOBILE': '移动端打卡'
    };
    return textMap[type] || '未知类型';
  };

  // 启动心跳监控
  const startHeartbeatMonitor = () => {
    heartbeatTimer.value = setInterval(() => {
      loadDeviceList();
    }, 30000); // 30秒刷新一次
  };

  // 停止心跳监控
  const stopHeartbeatMonitor = () => {
    if (heartbeatTimer.value) {
      clearInterval(heartbeatTimer.value);
      heartbeatTimer.value = null;
    }
  };
</script>

<style lang="less" scoped>
.attendance-device-management {
  .search-card {
    margin-bottom: 16px;
  }

  .device-info {
    .device-name {
      font-weight: 500;
      color: #262626;
    }

    .device-code {
      margin-top: 2px;
    }
  }

  .connection-status {
    .last-heartbeat {
      margin-top: 2px;
    }
  }

  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .toolbar-left {
      flex: 1;
    }

    .toolbar-right {
      flex-shrink: 0;
    }
  }

  .text-gray-400 {
    color: #bfbfbf;
  }

  .text-gray-500 {
    color: #8c8c8c;
  }

  .text-sm {
    font-size: 12px;
  }

  .text-xs {
    font-size: 11px;
  }

  .mb-2 {
    margin-bottom: 8px;
  }
}
</style>