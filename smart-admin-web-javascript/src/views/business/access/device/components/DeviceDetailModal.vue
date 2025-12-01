<!--
  * 设备详情弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="设备详情"
    :width="1000"
    :footer="null"
  >
    <div v-if="device" class="device-detail">
      <!-- 设备基本信息 -->
      <a-card title="基本信息" class="detail-card">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="设备编码">
            {{ device.deviceCode }}
          </a-descriptions-item>
          <a-descriptions-item label="设备名称">
            {{ device.deviceName }}
          </a-descriptions-item>
          <a-descriptions-item label="设备类型">
            <a-tag color="blue">{{ device.deviceTypeName || device.deviceType }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="所属区域">
            {{ device.areaName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="安装位置">
            {{ device.location }}
          </a-descriptions-item>
          <a-descriptions-item label="设备状态">
            <a-tag
              :color="getStatusColor(device.status)"
              :icon="getStatusIcon(device.status)"
            >
              {{ getStatusText(device.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="在线状态">
            <a-badge
              :status="device.onlineStatus === 'online' ? 'success' : 'error'"
              :text="device.onlineStatus === 'online' ? '在线' : '离线'"
            />
          </a-descriptions-item>
          <a-descriptions-item label="最后心跳">
            {{ device.lastHeartbeatTime ? formatDateTime(device.lastHeartbeatTime) : '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 网络配置 -->
      <a-card title="网络配置" class="detail-card">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="IP地址">
            {{ device.ip }}
          </a-descriptions-item>
          <a-descriptions-item label="端口号">
            {{ device.port }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 硬件信息 -->
      <a-card title="硬件信息" class="detail-card">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="制造商">
            {{ device.manufacturer || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="设备型号">
            {{ device.model || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="固件版本">
            {{ device.firmwareVersion || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="安装日期">
            {{ device.installDate ? formatDate(device.installDate) : '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 实时状态监控 -->
      <a-card title="实时状态监控" class="detail-card">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic
              title="CPU使用率"
              :value="realTimeStatus?.cpuUsage || 0"
              suffix="%"
              :value-style="{ color: getCpuUsageColor(realTimeStatus?.cpuUsage) }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="内存使用率"
              :value="realTimeStatus?.memoryUsage || 0"
              suffix="%"
              :value-style="{ color: getMemoryUsageColor(realTimeStatus?.memoryUsage) }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="设备温度"
              :value="realTimeStatus?.temperature || 0"
              suffix="°C"
              :value-style="{ color: getTemperatureColor(realTimeStatus?.temperature) }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="电源状态"
              :value="realTimeStatus?.powerStatus || '未知'"
              :value-style="{ color: getPowerStatusColor(realTimeStatus?.powerStatus) }"
            />
          </a-col>
        </a-row>

        <!-- 状态图表 -->
        <div class="status-charts" v-if="showCharts">
          <a-row :gutter="16">
            <a-col :span="12">
              <div class="chart-container">
                <h4>CPU使用率趋势</h4>
                <div ref="cpuChartRef" style="height: 200px;"></div>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="chart-container">
                <h4>内存使用率趋势</h4>
                <div ref="memoryChartRef" style="height: 200px;"></div>
              </div>
            </a-col>
          </a-row>
        </div>
      </a-card>

      <!-- 操作日志 -->
      <a-card title="最近操作日志" class="detail-card">
        <a-table
          :columns="logColumns"
          :data-source="operationLogs"
          :loading="logLoading"
          :pagination="false"
          size="small"
        >
          <template #operationType="{ record }">
            <a-tag :color="getOperationTypeColor(record.operationType)">
              {{ getOperationTypeText(record.operationType) }}
            </a-tag>
          </template>
          <template #operationTime="{ record }">
            {{ formatDateTime(record.operationTime) }}
          </template>
          <template #operationResult="{ record }">
            <a-badge
              :status="record.operationResult === 'success' ? 'success' : 'error'"
              :text="record.operationResult === 'success' ? '成功' : '失败'"
            />
          </template>
        </a-table>
      </a-card>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue';
  import { formatDateTime, formatDate } from '/@/lib/format';
  import { useAccessDeviceStore } from '/@/store/modules/business/access-device';

  // Props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    device: {
      type: Object,
      default: null,
    },
  });

  // Emits
  const emit = defineEmits(['update:visible']);

  // 状态管理
  const deviceStore = useAccessDeviceStore();

  // 响应式数据
  const cpuChartRef = ref();
  const memoryChartRef = ref();
  const logLoading = ref(false);
  const operationLogs = ref([]);
  const realTimeStatus = ref(null);
  const showCharts = ref(false);
  let statusUpdateTimer = null;

  // 计算属性
  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  // 操作日志表格列
  const logColumns = [
    {
      title: '操作类型',
      dataIndex: 'operationType',
      key: 'operationType',
      width: 120,
      slots: { customRender: 'operationType' },
    },
    {
      title: '操作结果',
      dataIndex: 'operationResult',
      key: 'operationResult',
      width: 100,
      slots: { customRender: 'operationResult' },
    },
    {
      title: '操作人',
      dataIndex: 'operatorName',
      key: 'operatorName',
      width: 120,
    },
    {
      title: '操作时间',
      dataIndex: 'operationTime',
      key: 'operationTime',
      width: 160,
      slots: { customRender: 'operationTime' },
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
    },
  ];

  // 监听设备变化
  watch(() => props.device, (newDevice) => {
    if (newDevice) {
      loadDeviceDetail(newDevice);
      startStatusUpdate();
    } else {
      stopStatusUpdate();
    }
  });

  // 监听弹窗显示状态
  watch(() => props.visible, (visible) => {
    if (!visible) {
      stopStatusUpdate();
    }
  });

  // 方法
  const loadDeviceDetail = async (device) => {
    // 加载设备实时状态
    await loadRealTimeStatus(device.deviceId);
    // 加载操作日志
    await loadOperationLogs(device.deviceId);
  };

  const loadRealTimeStatus = async (deviceId) => {
    try {
      const status = deviceStore.getDeviceRealTimeStatus(deviceId);
      if (status) {
        realTimeStatus.value = status;
      } else {
        // 如果没有缓存状态，则实时获取
        const statusList = await deviceStore.fetchRealTimeStatus([deviceId]);
        if (statusList && statusList.length > 0) {
          realTimeStatus.value = statusList[0];
        }
      }
    } catch (error) {
      console.error('加载设备实时状态失败:', error);
    }
  };

  const loadOperationLogs = async (deviceId) => {
    logLoading.value = true;
    try {
      // 模拟操作日志数据
      operationLogs.value = [
        {
          operationId: 1,
          operationType: 'remote_open',
          operationResult: 'success',
          operatorName: '管理员',
          operationTime: '2025-01-13 14:30:25',
          remark: '远程开门成功',
        },
        {
          operationId: 2,
          operationType: 'config_sync',
          operationResult: 'success',
          operatorName: '系统',
          operationTime: '2025-01-13 10:15:30',
          remark: '配置同步完成',
        },
        {
          operationId: 3,
          operationType: 'restart',
          operationResult: 'success',
          operatorName: '管理员',
          operationTime: '2025-01-12 18:45:12',
          remark: '设备重启完成',
        },
      ];
    } catch (error) {
      console.error('加载操作日志失败:', error);
    } finally {
      logLoading.value = false;
    }
  };

  const startStatusUpdate = () => {
    statusUpdateTimer = setInterval(() => {
      if (props.device) {
        loadRealTimeStatus(props.device.deviceId);
      }
    }, 10000); // 每10秒更新一次
  };

  const stopStatusUpdate = () => {
    if (statusUpdateTimer) {
      clearInterval(statusUpdateTimer);
      statusUpdateTimer = null;
    }
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

  const getCpuUsageColor = (usage) => {
    if (usage >= 80) return '#ff4d4f';
    if (usage >= 60) return '#faad14';
    return '#52c41a';
  };

  const getMemoryUsageColor = (usage) => {
    if (usage >= 80) return '#ff4d4f';
    if (usage >= 60) return '#faad14';
    return '#52c41a';
  };

  const getTemperatureColor = (temp) => {
    if (temp >= 70) return '#ff4d4f';
    if (temp >= 50) return '#faad14';
    return '#52c41a';
  };

  const getPowerStatusColor = (status) => {
    const colorMap = {
      '正常': '#52c41a',
      '低电量': '#faad14',
      '故障': '#ff4d4f',
    };
    return colorMap[status] || '#8c8c8c';
  };

  const getOperationTypeColor = (type) => {
    const colorMap = {
      remote_open: 'blue',
      config_sync: 'green',
      restart: 'orange',
      update: 'purple',
      delete: 'red',
    };
    return colorMap[type] || 'default';
  };

  const getOperationTypeText = (type) => {
    const textMap = {
      remote_open: '远程开门',
      config_sync: '配置同步',
      restart: '设备重启',
      update: '设备更新',
      delete: '设备删除',
    };
    return textMap[type] || type;
  };

  // 生命周期
  onMounted(() => {
    showCharts.value = true;
  });

  onUnmounted(() => {
    stopStatusUpdate();
  });
</script>

<style lang="less" scoped>
  .device-detail {
    .detail-card {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .status-charts {
      margin-top: 24px;

      .chart-container {
        border: 1px solid #f0f0f0;
        border-radius: 6px;
        padding: 16px;

        h4 {
          margin: 0 0 16px 0;
          font-size: 14px;
          font-weight: 600;
          color: #262626;
        }
      }
    }
  }
</style>