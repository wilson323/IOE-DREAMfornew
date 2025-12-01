<!--
 * 设备详情弹窗组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="modalVisible"
    title="设备详情"
    :width="900"
    :footer="null"
  >
    <a-spin :spinning="loading">
      <div class="device-detail">
        <!-- 设备基础信息 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">基础信息</span>
          </template>
          <a-descriptions :column="3" bordered>
            <a-descriptions-item label="设备名称">
              {{ deviceData?.deviceName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="设备编码">
              <a-tag color="blue">{{ deviceData?.deviceCode || '-' }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="设备类型">
              <a-tag :color="getDeviceTypeColor(deviceData?.deviceType)">
                {{ getDeviceTypeText(deviceData?.deviceType) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="厂商">
              {{ deviceData?.manufacturer || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="型号">
              {{ deviceData?.model || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-badge :status="getStatusBadge(deviceData?.status)" :text="getStatusText(deviceData?.status)" />
            </a-descriptions-item>
            <a-descriptions-item label="IP地址" :span="2">
              {{ deviceData?.ipAddress || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="端口">
              {{ deviceData?.port || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="所属区域" :span="2">
              {{ deviceData?.areaName || '未分配' }}
            </a-descriptions-item>
            <a-descriptions-item label="安装位置" :span="1">
              {{ deviceData?.location || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ formatDateTime(deviceData?.createTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="最后更新">
              {{ formatDateTime(deviceData?.updateTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="描述" :span="3">
              {{ deviceData?.description || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 实时状态 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">实时状态</span>
            <a-button type="link" size="small" @click="refreshRealtimeStatus" class="ml-2">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </template>
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic
                title="连接状态"
                :value="realtimeStatus.connectionStatus === 'online' ? '在线' : '离线'"
                :value-style="{ color: realtimeStatus.connectionStatus === 'online' ? '#3f8600' : '#cf1322' }"
              >
                <template #prefix>
                  <WifiOutlined v-if="realtimeStatus.connectionStatus === 'online'" />
                  <DisconnectOutlined v-else />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="最后心跳"
                :value="formatLastHeartbeat(realtimeStatus.lastHeartbeat)"
                :value-style="{ fontSize: '14px' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="今日打卡次数"
                :value="realtimeStatus.todayPunchCount || 0"
                suffix="次"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="设备温度"
                :value="realtimeStatus.temperature || 'N/A'"
                suffix="°C"
                :value-style="{ color: getTemperatureColor(realtimeStatus.temperature) }"
              />
            </a-col>
          </a-row>

          <!-- 设备健康指标 -->
          <a-divider>设备健康指标</a-divider>
          <a-row :gutter="16">
            <a-col :span="8">
              <div class="health-item">
                <div class="health-label">CPU使用率</div>
                <a-progress
                  :percent="realtimeStatus.cpuUsage || 0"
                  :status="getHealthStatus(realtimeStatus.cpuUsage, 80)"
                  :stroke-color="getHealthColor(realtimeStatus.cpuUsage, 80)"
                />
              </div>
            </a-col>
            <a-col :span="8">
              <div class="health-item">
                <div class="health-label">内存使用率</div>
                <a-progress
                  :percent="realtimeStatus.memoryUsage || 0"
                  :status="getHealthStatus(realtimeStatus.memoryUsage, 85)"
                  :stroke-color="getHealthColor(realtimeStatus.memoryUsage, 85)"
                />
              </div>
            </a-col>
            <a-col :span="8">
              <div class="health-item">
                <div class="health-label">存储使用率</div>
                <a-progress
                  :percent="realtimeStatus.storageUsage || 0"
                  :status="getHealthStatus(realtimeStatus.storageUsage, 90)"
                  :stroke-color="getHealthColor(realtimeStatus.storageUsage, 90)"
                />
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 配置信息 -->
        <a-card :bordered="false" class="mb-4">
          <template #title>
            <span class="text-lg font-medium">配置信息</span>
          </template>
          <a-tabs v-model:activeKey="configTabActiveKey">
            <!-- 打卡配置 -->
            <a-tab-pane key="punch" tab="打卡配置">
              <div class="config-section">
                <div class="mb-4">
                  <h4 class="font-medium mb-2">支持的打卡方式</h4>
                  <a-tag v-for="mode in deviceConfig.punchModes" :key="mode" class="mr-2 mb-2">
                    {{ getPunchModeText(mode) }}
                  </a-tag>
                  <span v-if="!deviceConfig.punchModes?.length" class="text-gray-400">未配置</span>
                </div>

                <a-row :gutter="16">
                  <a-col :span="8">
                    <div class="config-item">
                      <span class="config-label">GPS验证：</span>
                      <a-switch :checked="deviceConfig.gpsEnabled" disabled />
                      <span class="ml-2">{{ deviceConfig.gpsEnabled ? '启用' : '禁用' }}</span>
                    </div>
                  </a-col>
                  <a-col :span="8">
                    <div class="config-item">
                      <span class="config-label">拍照验证：</span>
                      <a-switch :checked="deviceConfig.photoEnabled" disabled />
                      <span class="ml-2">{{ deviceConfig.photoEnabled ? '启用' : '禁用' }}</span>
                    </div>
                  </a-col>
                  <a-col :span="8">
                    <div class="config-item">
                      <span class="config-label">活体检测：</span>
                      <a-switch :checked="deviceConfig.livenessEnabled" disabled />
                      <span class="ml-2">{{ deviceConfig.livenessEnabled ? '启用' : '禁用' }}</span>
                    </div>
                  </a-col>
                </a-row>
              </div>
            </a-tab-pane>

            <!-- 人脸识别配置 -->
            <a-tab-pane key="face" tab="人脸识别" v-if="deviceConfig.punchModes?.includes('FACE')">
              <div class="config-section">
                <a-descriptions :column="2" bordered size="small">
                  <a-descriptions-item label="识别阈值">
                    {{ (deviceConfig.faceSettings?.recognitionThreshold || 0.8) * 100 }}%
                  </a-descriptions-item>
                  <a-descriptions-item label="活体阈值">
                    {{ (deviceConfig.faceSettings?.livenessThreshold || 0.7) * 100 }}%
                  </a-descriptions-item>
                  <a-descriptions-item label="匹配超时">
                    {{ deviceConfig.faceSettings?.matchTimeout || 5 }}秒
                  </a-descriptions-item>
                  <a-descriptions-item label="活体算法">
                    {{ deviceConfig.faceSettings?.livenessAlgorithm || '默认' }}
                  </a-descriptions-item>
                </a-descriptions>
              </div>
            </a-tab-pane>

            <!-- 工作时间配置 -->
            <a-tab-pane key="worktime" tab="工作时间">
              <div class="config-section">
                <div v-for="(shift, index) in deviceConfig.workTimeConfig" :key="index" class="mb-4">
                  <h5 class="font-medium mb-2">班次 {{ index + 1 }}</h5>
                  <a-descriptions size="small" :column="3" bordered>
                    <a-descriptions-item label="开始时间">
                      {{ shift.startTime || '未设置' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="结束时间">
                      {{ shift.endTime || '未设置' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="迟到宽限">
                      {{ shift.lateGracePeriod || 0 }}分钟
                    </a-descriptions-item>
                    <a-descriptions-item label="允许迟到" :span="2">
                      <a-tag :color="shift.allowLate ? 'green' : 'red'">
                        {{ shift.allowLate ? '允许' : '不允许' }}
                      </a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="允许早退">
                      <a-tag :color="shift.allowEarlyLeave ? 'green' : 'red'">
                        {{ shift.allowEarlyLeave ? '允许' : '不允许' }}
                      </a-tag>
                    </a-descriptions-item>
                  </a-descriptions>
                </div>
              </div>
            </a-tab-pane>

            <!-- 安全配置 -->
            <a-tab-pane key="security" tab="安全配置">
              <div class="config-section">
                <a-descriptions :column="2" bordered size="small">
                  <a-descriptions-item label="最大失败次数">
                    {{ deviceConfig.securitySettings?.maxFailedAttempts || 3 }}次
                  </a-descriptions-item>
                  <a-descriptions-item label="锁定时间">
                    {{ deviceConfig.securitySettings?.lockoutDuration || 5 }}分钟
                  </a-descriptions-item>
                  <a-descriptions-item label="管理员密码">
                    <span class="text-gray-400">已设置</span>
                  </a-descriptions-item>
                  <a-descriptions-item label="远程控制">
                    <a-tag :color="deviceConfig.advancedSettings?.includes('remoteControl') ? 'green' : 'red'">
                      {{ deviceConfig.advancedSettings?.includes('remoteControl') ? '启用' : '禁用' }}
                    </a-tag>
                  </a-descriptions-item>
                </a-descriptions>
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>

        <!-- 统计信息 -->
        <a-card :bordered="false">
          <template #title>
            <span class="text-lg font-medium">统计信息</span>
          </template>
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic
                title="注册用户数"
                :value="statistics.registeredUsers || 0"
                suffix="人"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="今日打卡"
                :value="statistics.todayPunches || 0"
                suffix="次"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="本月打卡"
                :value="statistics.monthPunches || 0"
                suffix="次"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="异常次数"
                :value="statistics.errorCount || 0"
                suffix="次"
                :value-style="{ color: statistics.errorCount > 0 ? '#cf1322' : '#3f8600' }"
              />
            </a-col>
          </a-row>
        </a-card>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    ReloadOutlined,
    WifiOutlined,
    DisconnectOutlined
  } from '@ant-design/icons-vue';
  import { attendanceDeviceApi } from '/@/api/business/attendance/attendance-device-api';

  // Props定义
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false
    },
    deviceData: {
      type: Object,
      default: null
    }
  });

  // Emits定义
  const emit = defineEmits(['update:visible']);

  // 响应式数据
  const modalVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  });

  const loading = ref(false);
  const configTabActiveKey = ref('punch');

  // 实时状态数据
  const realtimeStatus = reactive({
    connectionStatus: 'offline',
    lastHeartbeat: null,
    temperature: null,
    cpuUsage: 0,
    memoryUsage: 0,
    storageUsage: 0,
    todayPunchCount: 0
  });

  // 设备配置数据
  const deviceConfig = reactive({
    punchModes: [],
    gpsEnabled: false,
    photoEnabled: false,
    livenessEnabled: false,
    faceSettings: {
      recognitionThreshold: 0.8,
      livenessThreshold: 0.7,
      matchTimeout: 5
    },
    workTimeConfig: [],
    securitySettings: {
      maxFailedAttempts: 3,
      lockoutDuration: 5
    },
    advancedSettings: []
  });

  // 统计数据
  const statistics = reactive({
    registeredUsers: 0,
    todayPunches: 0,
    monthPunches: 0,
    errorCount: 0
  });

  // 监听弹窗显示状态
  watch(
    modalVisible,
    (newVisible) => {
      if (newVisible && props.deviceData) {
        loadDeviceDetail();
      }
    }
  );

  // ============ 方法 ============

  // 加载设备详情
  const loadDeviceDetail = async () => {
    if (!props.deviceData?.deviceId) return;

    try {
      loading.value = true;

      // 并行加载各种数据
      await Promise.all([
        loadRealtimeStatus(),
        loadDeviceConfig(),
        loadDeviceStatistics()
      ]);
    } catch (error) {
      console.error('加载设备详情失败:', error);
      message.error('加载设备详情失败');
    } finally {
      loading.value = false;
    }
  };

  // 加载实时状态
  const loadRealtimeStatus = async () => {
    try {
      const response = await attendanceDeviceApi.getDeviceRealtimeStatus(props.deviceData.deviceId);
      if (response.success) {
        Object.assign(realtimeStatus, response.data);
      }
    } catch (error) {
      console.error('加载实时状态失败:', error);
    }
  };

  // 加载设备配置
  const loadDeviceConfig = async () => {
    try {
      const response = await attendanceDeviceApi.getDeviceConfig(props.deviceData.deviceId);
      if (response.success) {
        Object.assign(deviceConfig, response.data);
      }
    } catch (error) {
      console.error('加载设备配置失败:', error);
    }
  };

  // 加载设备统计
  const loadDeviceStatistics = async () => {
    try {
      const response = await attendanceDeviceApi.getDeviceStatistics({
        deviceId: props.deviceData.deviceId,
        statisticsType: 'daily'
      });
      if (response.success) {
        Object.assign(statistics, response.data);
      }
    } catch (error) {
      console.error('加载设备统计失败:', error);
    }
  };

  // 刷新实时状态
  const refreshRealtimeStatus = () => {
    loadRealtimeStatus();
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

  // 获取打卡方式文本
  const getPunchModeText = (mode) => {
    const textMap = {
      'FINGERPRINT': '指纹打卡',
      'FACE': '人脸识别',
      'CARD': 'IC卡打卡',
      'PASSWORD': '密码打卡'
    };
    return textMap[mode] || mode;
  };

  // 格式化日期时间
  const formatDateTime = (dateTime) => {
    if (!dateTime) return '-';
    return new Date(dateTime).toLocaleString();
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

  // 获取温度颜色
  const getTemperatureColor = (temp) => {
    if (!temp) return undefined;
    if (temp > 60) return '#cf1322'; // 红色
    if (temp > 45) return '#fa8c16'; // 橙色
    return '#3f8600'; // 绿色
  };

  // 获取健康状态
  const getHealthStatus = (value, threshold) => {
    if (value >= threshold) return 'exception';
    if (value >= threshold * 0.8) return 'active';
    return 'normal';
  };

  // 获取健康颜色
  const getHealthColor = (value, threshold) => {
    if (value >= threshold) return '#cf1322'; // 红色
    if (value >= threshold * 0.8) return '#fa8c16'; // 橙色
    return '#52c41a'; // 绿色
  };
</script>

<style lang="less" scoped>
.device-detail {
  .mb-4 {
    margin-bottom: 16px;
  }

  .mr-2 {
    margin-right: 8px;
  }

  .ml-2 {
    margin-left: 8px;
  }

  .config-section {
    .config-item {
      margin-bottom: 16px;

      .config-label {
        font-weight: 500;
        margin-right: 8px;
      }
    }

    .health-item {
      .health-label {
        font-size: 12px;
        color: #8c8c8c;
        margin-bottom: 8px;
      }
    }
  }

  .text-gray-400 {
    color: #bfbfbf;
  }

  .text-lg {
    font-size: 16px;
  }

  .font-medium {
    font-weight: 500;
  }
}
</style>