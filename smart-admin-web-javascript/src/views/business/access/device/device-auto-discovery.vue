<!--
  * 设备自动发现
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="device-auto-discovery-page">
    <!-- 发现配置区域 -->
    <a-card size="small" :bordered="false" class="discovery-config-card">
      <template #title>
        <a-space>
          <SearchOutlined />
          <span>设备发现配置</span>
        </a-space>
      </template>

      <a-form class="discovery-form" layout="vertical">
        <a-row :gutter="16">
          <!-- 子网地址 -->
          <a-col :span="12">
            <a-form-item label="子网地址" required>
              <a-input
                v-model:value="discoveryForm.subnet"
                placeholder="例如: 192.168.1.0/24"
                allow-clear
              >
                <template #prefix>
                  <ApiOutlined />
                </template>
              </a-input>
              <div class="form-item-hint">支持CIDR格式，例如 192.168.1.0/24</div>
            </a-form-item>
          </a-col>

          <!-- 超时时间 -->
          <a-col :span="12">
            <a-form-item label="超时时间（秒）">
              <a-input-number
                v-model:value="discoveryForm.timeout"
                :min="30"
                :max="600"
                :step="30"
                style="width: 100%"
              >
                <template #addonAfter>秒</template>
              </a-input-number>
              <div class="form-item-hint">建议范围: 30-600秒</div>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 发现协议 -->
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="发现协议">
              <a-checkbox-group v-model:value="discoveryForm.protocols">
                <a-checkbox value="SSDP" :disabled="isScanning">
                  <a-space>
                    <WifiOutlined />
                    <span>SSDP (通用设备发现)</span>
                  </a-space>
                </a-checkbox>
                <a-checkbox value="ONVIF" :disabled="isScanning">
                  <a-space>
                    <VideoCameraOutlined />
                    <span>ONVIF (网络摄像头)</span>
                  </a-space>
                </a-checkbox>
                <a-checkbox value="SNMP" :disabled="isScanning">
                  <a-space>
                    <NodeIndexOutlined />
                    <span>SNMP (网络设备)</span>
                  </a-space>
                </a-checkbox>
                <a-checkbox value="PRIVATE" :disabled="isScanning">
                  <a-space>
                    <LockOutlined />
                    <span>私有协议 (门禁/考勤机)</span>
                  </a-space>
                </a-checkbox>
              </a-checkbox-group>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 操作按钮 -->
        <a-row>
          <a-col :span="24">
            <a-space>
              <a-button
                type="primary"
                :loading="isScanning"
                :disabled="!canStartDiscovery"
                @click="handleStartDiscovery"
              >
                <template #icon><RadarOutlined /></template>
                {{ isScanning ? '扫描中...' : '开始发现' }}
              </a-button>
              <a-button
                v-if="isScanning"
                danger
                @click="handleCancelDiscovery"
              >
                <template #icon><StopOutlined /></template>
                停止扫描
              </a-button>
              <a-button
                v-if="discoveredDevices.length > 0"
                @click="handleReset"
              >
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- 扫描进度区域 -->
    <a-card
      v-if="scanProgress.scanId"
      size="small"
      :bordered="false"
      style="margin-top: 16px"
      class="progress-card"
    >
      <template #title>
        <a-space>
          <SyncOutlined :spin="isScanning" />
          <span>扫描进度</span>
        </a-space>
      </template>

      <a-space direction="vertical" style="width: 100%">
        <!-- 进度条 -->
        <div>
          <a-row type="flex" justify="space-between" align="middle">
            <a-col>
              <a-tag :color="getStatusColor(scanProgress.status)">
                {{ getStatusText(scanProgress.status) }}
              </a-tag>
              <span class="progress-text">
                已发现 {{ scanProgress.discoveredDevices || 0 }} / {{ scanProgress.totalDevices || 0 }} 台设备
              </span>
            </a-col>
            <a-col v-if="scanProgress.progress !== undefined">
              <span class="progress-percent">{{ scanProgress.progress }}%</span>
            </a-col>
          </a-row>
          <a-progress
            :percent="scanProgress.progress || 0"
            :status="getProgressStatus(scanProgress.status)"
            :stroke-color="{
              '0%': '#108ee9',
              '100%': '#87d068',
            }"
          />
        </div>

        <!-- 扫描ID -->
        <div class="scan-info">
          <a-typography-text type="secondary">
            扫描ID: {{ scanProgress.scanId }}
          </a-typography-text>
        </div>
      </a-space>
    </a-card>

    <!-- 发现的设备列表 -->
    <a-card
      v-if="discoveredDevices.length > 0"
      size="small"
      :bordered="false"
      style="margin-top: 16px"
      class="devices-card"
    >
      <template #title>
        <a-space>
          <AppstoreOutlined />
          <span>发现的设备 ({{ discoveredDevices.length }})</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button
            type="primary"
            :disabled="selectedDevices.length === 0"
            @click="handleBatchAdd"
          >
            <template #icon><PlusOutlined /></template>
            批量添加 ({{ selectedDevices.length }})
          </a-button>
          <a-button @click="handleExportResult">
            <template #icon><DownloadOutlined /></template>
            导出结果
          </a-button>
          <a-dropdown>
            <a-button>
              更多操作 <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleSelectAll">
                  <CheckOutlined /> 全选
                </a-menu-item>
                <a-menu-item @click="handleDeselectAll">
                  <MinusOutlined /> 取消全选
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleFilterNewDevices">
                  <FilterOutlined /> 只显示新设备
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </template>

      <a-table
        :columns="deviceColumns"
        :data-source="displayDevices"
        :row-selection="rowSelection"
        :pagination="devicePagination"
        :scroll="{ x: 1200 }"
        row-key="ipAddress"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <!-- 设备图片 -->
          <template v-if="column.key === 'deviceImage'">
            <a-avatar :src="getDeviceImage(record)" shape="square" :size="40">
              <template #icon><DesktopOutlined /></template>
            </a-avatar>
          </template>

          <!-- 设备信息 -->
          <template v-else-if="column.key === 'deviceInfo'">
            <div class="device-info">
              <div class="device-name">
                {{ record.deviceName || record.deviceBrand + ' ' + record.deviceModel }}
              </div>
              <div class="device-ip">{{ record.ipAddress }}:{{ record.port }}</div>
            </div>
          </template>

          <!-- 设备类型 -->
          <template v-else-if="column.key === 'deviceType'">
            <a-tag :color="getDeviceTypeColor(record.deviceType)">
              {{ getDeviceTypeName(record.deviceType) }}
            </a-tag>
          </template>

          <!-- 协议 -->
          <template v-else-if="column.key === 'protocol'">
            <a-tag color="blue">{{ record.protocol }}</a-tag>
          </template>

          <!-- 设备状态 -->
          <template v-else-if="column.key === 'deviceStatus'">
            <a-badge
              :status="getDeviceStatusBadge(record.deviceStatus)"
              :text="getDeviceStatusText(record.deviceStatus)"
            />
          </template>

          <!-- 是否已存在 -->
          <template v-else-if="column.key === 'existsInSystem'">
            <a-tag v-if="record.existsInSystem" color="green">
              <CheckCircleOutlined /> 已存在
            </a-tag>
            <a-tag v-else color="blue">
              <PlusCircleOutlined /> 新设备
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                :disabled="record.existsInSystem"
                @click="handleAddSingle(record)"
              >
                添加
              </a-button>
              <a-button type="link" size="small" @click="handleViewDetail(record)">
                详情
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 设备详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="设备详细信息"
      :footer="null"
      width="600px"
    >
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="设备名称">
          {{ currentDetail.deviceName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="IP地址">
          {{ currentDetail.ipAddress }}
        </a-descriptions-item>
        <a-descriptions-item label="端口">
          {{ currentDetail.port }}
        </a-descriptions-item>
        <a-descriptions-item label="MAC地址">
          {{ currentDetail.macAddress || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="设备类型">
          {{ getDeviceTypeName(currentDetail.deviceType) }}
        </a-descriptions-item>
        <a-descriptions-item label="设备型号">
          {{ currentDetail.deviceModel || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="设备厂商">
          {{ currentDetail.deviceBrand || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="固件版本">
          {{ currentDetail.firmwareVersion || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="发现协议">
          {{ currentDetail.protocol }}
        </a-descriptions-item>
        <a-descriptions-item label="设备状态">
          <a-badge
            :status="getDeviceStatusBadge(currentDetail.deviceStatus)"
            :text="getDeviceStatusText(currentDetail.deviceStatus)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="设备位置">
          {{ currentDetail.location || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="设备信息" :span="2">
          <pre style="max-height: 200px; overflow: auto;">{{ formatDeviceInfo(currentDetail.deviceInfo) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
  import { reactive, ref, computed, onMounted, onUnmounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    SearchOutlined,
    ApiOutlined,
    RadarOutlined,
    StopOutlined,
    ReloadOutlined,
    SyncOutlined,
    AppstoreOutlined,
    PlusOutlined,
    DownloadOutlined,
    DownOutlined,
    CheckOutlined,
    MinusOutlined,
    FilterOutlined,
    DesktopOutlined,
    CheckCircleOutlined,
    PlusCircleOutlined,
    WifiOutlined,
    VideoCameraOutlined,
    NodeIndexOutlined,
    LockOutlined,
  } from '@ant-design/icons-vue';
  import { accessApi } from '/@/api/business/access/access-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';

  // 发现表单
  const discoveryForm = reactive({
    subnet: '192.168.1.0/24',
    timeout: 180,
    protocols: ['SSDP', 'ONVIF', 'SNMP', 'PRIVATE'],
  });

  // 扫描进度
  const scanProgress = reactive({
    scanId: null,
    status: null,      // PENDING, SCANNING, COMPLETED, FAILED
    progress: 0,
    totalDevices: 0,
    discoveredDevices: [],
  });

  // 发现的设备列表
  const discoveredDevices = ref([]);
  const selectedDevices = ref([]);
  const displayDevices = ref([]);

  // 是否正在扫描
  const isScanning = ref(false);
  const progressTimer = ref(null);

  // 详情弹窗
  const detailVisible = ref(false);
  const currentDetail = ref({});

  // 过滤器
  const showNewDevicesOnly = ref(false);

  // 是否可以开始发现
  const canStartDiscovery = computed(() => {
    return discoveryForm.subnet &&
           discoveryForm.protocols.length > 0 &&
           !isScanning.value;
  });

  // 设备列表表格列
  const deviceColumns = ref([
    {
      title: '设备',
      key: 'deviceImage',
      width: 80,
      fixed: 'left',
    },
    {
      title: '设备信息',
      key: 'deviceInfo',
      width: 200,
    },
    {
      title: '类型',
      key: 'deviceType',
      width: 120,
    },
    {
      title: '协议',
      key: 'protocol',
      width: 100,
    },
    {
      title: '厂商',
      dataIndex: 'deviceBrand',
      key: 'deviceBrand',
      width: 120,
    },
    {
      title: '型号',
      dataIndex: 'deviceModel',
      key: 'deviceModel',
      width: 150,
    },
    {
      title: 'MAC地址',
      dataIndex: 'macAddress',
      key: 'macAddress',
      width: 150,
    },
    {
      title: '状态',
      key: 'deviceStatus',
      width: 100,
    },
    {
      title: '系统状态',
      key: 'existsInSystem',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
    },
  ]);

  // 设备列表分页
  const devicePagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: PAGE_SIZE_OPTIONS,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 行选择配置
  const rowSelection = computed(() => ({
    selectedRowKeys: selectedDevices.value.map(d => d.ipAddress),
    onChange: (selectedKeys, selectedRows) => {
      selectedDevices.value = selectedRows;
    },
    getCheckboxProps: (record) => ({
      disabled: record.existsInSystem,
    }),
  }));

  // 开始设备发现
  const handleStartDiscovery = async () => {
    if (!discoveryForm.subnet) {
      message.warning('请输入子网地址');
      return;
    }
    if (discoveryForm.protocols.length === 0) {
      message.warning('请至少选择一种发现协议');
      return;
    }

    try {
      isScanning.value = true;
      discoveredDevices.value = [];
      selectedDevices.value = [];
      scanProgress.scanId = null;
      scanProgress.progress = 0;
      scanProgress.discoveredDevices = [];

      const result = await accessApi.startDeviceDiscovery({
        subnet: discoveryForm.subnet,
        protocols: discoveryForm.protocols,
        timeout: discoveryForm.timeout,
      });

      if (result.code === 200 && result.data) {
        scanProgress.scanId = result.data.scanId;
        scanProgress.status = result.data.status;
        message.success('设备发现已启动');

        // 开始轮询进度
        startProgressPolling();
      } else {
        message.error(result.message || '启动设备发现失败');
        isScanning.value = false;
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('启动设备发现失败');
      isScanning.value = false;
    }
  };

  // 轮询扫描进度
  const startProgressPolling = () => {
    if (progressTimer.value) {
      clearInterval(progressTimer.value);
    }

    progressTimer.value = setInterval(async () => {
      try {
        const result = await accessApi.getDiscoveryProgress(scanProgress.scanId);
        if (result.code === 200 && result.data) {
          Object.assign(scanProgress, result.data);

          // 更新发现的设备列表
          if (result.data.discoveredDevices && result.data.discoveredDevices.length > 0) {
            discoveredDevices.value = result.data.discoveredDevices;
            updateDisplayDevices();
          }

          // 检查是否完成
          if (result.data.status === 'COMPLETED' || result.data.status === 'FAILED') {
            stopProgressPolling();
            isScanning.value = false;

            if (result.data.status === 'COMPLETED') {
              message.success(`设备发现完成，共发现 ${discoveredDevices.value.length} 台设备`);
            } else {
              message.error('设备发现失败');
            }
          }
        }
      } catch (error) {
        console.error('获取扫描进度失败', error);
      }
    }, 2000); // 每2秒轮询一次
  };

  // 停止轮询
  const stopProgressPolling = () => {
    if (progressTimer.value) {
      clearInterval(progressTimer.value);
      progressTimer.value = null;
    }
  };

  // 取消发现任务
  const handleCancelDiscovery = async () => {
    Modal.confirm({
      title: '确认停止',
      content: '确定要停止当前的设备发现任务吗？',
      onOk: async () => {
        try {
          const result = await accessApi.cancelDiscovery(scanProgress.scanId);
          if (result.code === 200) {
            message.success('已停止扫描');
            stopProgressPolling();
            isScanning.value = false;
            scanProgress.status = 'CANCELLED';
          } else {
            message.error(result.message || '停止扫描失败');
          }
        } catch (error) {
          smartSentry.captureError(error);
          message.error('停止扫描失败');
        }
      },
    });
  };

  // 重置
  const handleReset = () => {
    stopProgressPolling();
    discoveredDevices.value = [];
    selectedDevices.value = [];
    displayDevices.value = [];
    scanProgress.scanId = null;
    scanProgress.status = null;
    scanProgress.progress = 0;
    isScanning.value = false;
    showNewDevicesOnly.value = false;
  };

  // 更新显示设备列表
  const updateDisplayDevices = () => {
    if (showNewDevicesOnly.value) {
      displayDevices.value = discoveredDevices.value.filter(d => !d.existsInSystem);
    } else {
      displayDevices.value = discoveredDevices.value;
    }
    devicePagination.total = displayDevices.value.length;
  };

  // 批量添加设备
  const handleBatchAdd = async () => {
    if (selectedDevices.value.length === 0) {
      message.warning('请至少选择一台设备');
      return;
    }

    // 过滤已存在的设备
    const newDevices = selectedDevices.value.filter(d => !d.existsInSystem);
    if (newDevices.length === 0) {
      message.warning('所选设备均已存在');
      return;
    }

    if (newDevices.length < selectedDevices.value.length) {
      message.info(`已跳过 ${selectedDevices.value.length - newDevices.length} 台已存在的设备`);
    }

    try {
      const result = await accessApi.batchAddDiscoveredDevices(newDevices);
      if (result.code === 200 && result.data) {
        const { totalDevices, discoveredDevices: added } = result.data;
        message.success(`成功添加 ${added} 台设备`);

        // 更新设备状态
        handleStartDiscovery(); // 重新扫描以更新状态
      } else {
        message.error(result.message || '批量添加失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('批量添加失败');
    }
  };

  // 导出发现结果
  const handleExportResult = async () => {
    try {
      const result = await accessApi.exportDiscoveryResult(scanProgress.scanId);
      if (result.code === 200 && result.data) {
        const { fileName, fileData } = result.data;

        // 解码Base64数据
        const binaryString = atob(fileData);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
          bytes[i] = binaryString.charCodeAt(i);
        }

        // 创建Blob并下载
        const blob = new Blob([bytes], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName || `设备发现结果_${Date.now()}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);

        message.success('导出成功');
      } else {
        message.error(result.message || '导出失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('导出失败');
    }
  };

  // 全选
  const handleSelectAll = () => {
    const newDevices = discoveredDevices.value.filter(d => !d.existsInSystem);
    selectedDevices.value = [...newDevices];
  };

  // 取消全选
  const handleDeselectAll = () => {
    selectedDevices.value = [];
  };

  // 只显示新设备
  const handleFilterNewDevices = () => {
    showNewDevicesOnly.value = !showNewDevicesOnly.value;
    updateDisplayDevices();
  };

  // 添加单个设备
  const handleAddSingle = (record) => {
    selectedDevices.value = [record];
    handleBatchAdd();
  };

  // 查看详情
  const handleViewDetail = (record) => {
    currentDetail.value = { ...record };
    detailVisible.value = true;
  };

  // 辅助方法
  const getStatusColor = (status) => {
    const colorMap = {
      'PENDING': 'default',
      'SCANNING': 'processing',
      'COMPLETED': 'success',
      'FAILED': 'error',
      'CANCELLED': 'warning',
    };
    return colorMap[status] || 'default';
  };

  const getStatusText = (status) => {
    const textMap = {
      'PENDING': '等待中',
      'SCANNING': '扫描中',
      'COMPLETED': '已完成',
      'FAILED': '失败',
      'CANCELLED': '已取消',
    };
    return textMap[status] || status;
  };

  const getProgressStatus = (status) => {
    const statusMap = {
      'PENDING': 'normal',
      'SCANNING': 'active',
      'COMPLETED': 'success',
      'FAILED': 'exception',
      'CANCELLED': 'normal',
    };
    return statusMap[status] || 'normal';
  };

  const getDeviceTypeName = (type) => {
    const typeMap = {
      1: '门禁设备',
      2: '考勤设备',
      3: '消费设备',
      4: '视频设备',
      5: '访客设备',
      6: '生物识别设备',
    };
    return typeMap[type] || '未知设备';
  };

  const getDeviceTypeColor = (type) => {
    const colorMap = {
      1: 'blue',
      2: 'green',
      3: 'orange',
      4: 'purple',
      5: 'cyan',
      6: 'red',
    };
    return colorMap[type] || 'default';
  };

  const getDeviceStatusBadge = (status) => {
    const badgeMap = {
      1: 'success',
      2: 'default',
      3: 'error',
      4: 'warning',
    };
    return badgeMap[status] || 'default';
  };

  const getDeviceStatusText = (status) => {
    const textMap = {
      1: '在线',
      2: '离线',
      3: '故障',
      4: '维护中',
    };
    return textMap[status] || '未知';
  };

  const getDeviceImage = (record) => {
    // 根据设备厂商返回不同的图片
    if (record.deviceBrand) {
      return `/images/device-brands/${record.deviceBrand.toLowerCase()}.png`;
    }
    return null;
  };

  const formatDeviceInfo = (info) => {
    if (!info) return '-';
    try {
      return JSON.stringify(JSON.parse(info), null, 2);
    } catch {
      return info;
    }
  };

  // 组件挂载
  onMounted(() => {
    // 可以在这里初始化一些数据
  });

  // 组件卸载
  onUnmounted(() => {
    stopProgressPolling();
  });
</script>

<style scoped>
  .device-auto-discovery-page {
    padding: 16px;
  }

  .discovery-config-card .ant-form-item {
    margin-bottom: 16px;
  }

  .form-item-hint {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }

  .progress-card {
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  }

  .progress-text {
    margin-left: 8px;
    font-size: 14px;
  }

  .progress-percent {
    font-size: 16px;
    font-weight: bold;
    color: #1890ff;
  }

  .scan-info {
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
  }

  .device-info {
    line-height: 1.5;
  }

  .device-name {
    font-weight: 500;
    font-size: 14px;
  }

  .device-ip {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
  }

  .devices-card :deep(.ant-table) {
    font-size: 13px;
  }

  pre {
    background: #f5f5f5;
    padding: 8px;
    border-radius: 4px;
    font-size: 12px;
  }
</style>
