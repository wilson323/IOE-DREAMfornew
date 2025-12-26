<template>
  <div class="device-detail">
    <a-descriptions title="基本信息" bordered :column="1">
      <a-descriptions-item label="设备名称">
        {{ device.deviceName }}
      </a-descriptions-item>
      <a-descriptions-item label="设备编码">
        {{ device.deviceCode }}
      </a-descriptions-item>
      <a-descriptions-item label="设备类型">
        <a-tag :color="getDeviceTypeColor(device.deviceType)">
          {{ getDeviceTypeName(device.deviceType) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="设备状态">
        <a-badge :status="getStatusBadge(device.status)" :text="getDeviceStatusName(device.status)" />
      </a-descriptions-item>
      <a-descriptions-item label="所在区域">
        {{ device.areaName || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="设备地址">
        {{ device.deviceAddress || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="经纬度">
        {{ device.longitude }}, {{ device.latitude }}
      </a-descriptions-item>
    </a-descriptions>

    <a-divider />

    <a-descriptions title="运行信息" bordered :column="1">
      <a-descriptions-item label="最后上线">
        {{ device.lastOnlineTime || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="固件版本">
        {{ device.firmwareVersion || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="IP地址">
        {{ device.ipAddress || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="在线时长">
        {{ device.onlineDuration || '-' }}
      </a-descriptions-item>
    </a-descriptions>

    <a-divider />

    <a-descriptions title="业务统计" bordered :column="1">
      <a-descriptions-item label="今日通行次数">
        {{ device.todayPassCount || 0 }}
      </a-descriptions-item>
      <a-descriptions-item label="今日告警次数">
        {{ device.todayAlarmCount || 0 }}
      </a-descriptions-item>
      <a-descriptions-item label="设备健康度">
        <a-progress
          :percent="device.healthScore || 0"
          :status="device.healthScore > 80 ? 'success' : device.healthScore > 60 ? 'normal' : 'exception'"
        />
      </a-descriptions-item>
    </a-descriptions>

    <a-divider />

    <a-space style="width: 100%; justify-content: flex-end">
      <a-button @click="handleViewLog">查看日志</a-button>
      <a-button @click="handleViewAlarm">告警记录</a-button>
      <a-button type="primary" @click="handleRefresh">刷新</a-button>
      <a-button danger @click="handleMaintain">报修</a-button>
    </a-space>
  </div>
</template>

<script setup>
import { message } from 'ant-design-vue';

const props = defineProps({
  device: {
    type: Object,
    required: true
  }
});

/**
 * 获取设备类型名称
 */
const getDeviceTypeName = (type) => {
  const typeMap = {
    1: '门禁设备',
    2: '考勤设备',
    3: '消费设备',
    4: '视频设备',
    5: '访客设备'
  };
  return typeMap[type] || '未知设备';
};

/**
 * 获取设备类型颜色
 */
const getDeviceTypeColor = (type) => {
  const colorMap = {
    1: 'blue',    // 门禁
    2: 'green',   // 考勤
    3: 'orange',  // 消费
    4: 'purple',  // 视频
    5: 'pink'     // 访客
  };
  return colorMap[type] || 'default';
};

/**
 * 获取设备状态名称
 */
const getDeviceStatusName = (status) => {
  const statusMap = {
    1: '在线',
    2: '离线',
    3: '故障',
    4: '停用'
  };
  return statusMap[status] || '未知';
};

/**
 * 获取状态徽标
 */
const getStatusBadge = (status) => {
  const badgeMap = {
    1: 'success',  // 在线
    2: 'default',  // 离线
    3: 'error',    // 故障
    4: 'default'   // 停用
  };
  return badgeMap[status] || 'default';
};

/**
 * 查看日志
 */
const handleViewLog = () => {
  message.info('查看设备日志功能开发中...');
};

/**
 * 查看告警
 */
const handleViewAlarm = () => {
  message.info('查看告警记录功能开发中...');
};

/**
 * 刷新
 */
const handleRefresh = () => {
  message.success('刷新成功');
};

/**
 * 报修
 */
const handleMaintain = () => {
  message.info('设备报修功能开发中...');
};
</script>

<style scoped lang="less">
.device-detail {
  padding: 0;
}
</style>
