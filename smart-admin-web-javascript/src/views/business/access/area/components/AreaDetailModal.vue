<!--
  * Area Detail Modal Component
-->
<template>
  <a-modal
    :visible="visible"
    @update:visible="(val) => $emit('update:visible', val)"
    title="Area Details"
    width="900px"
    :footer="null"
  >
    <div v-if="areaData" class="area-detail">
      <!-- 基本信息 -->
      <a-descriptions title="Basic Information" :column="2" bordered>
        <a-descriptions-item label="Area Name">{{ areaData.areaName }}</a-descriptions-item>
        <a-descriptions-item label="Area Code">{{ areaData.areaCode }}</a-descriptions-item>
        <a-descriptions-item label="Area Type">
          <a-tag :color="getAreaTypeColor(areaData.areaType)">
            {{ getAreaTypeText(areaData.areaType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Security Level">
          <a-progress
            :percent="getSecurityPercent(areaData.securityLevel)"
            :stroke-color="getSecurityColor(areaData.securityLevel)"
            style="width: 100px"
          />
        </a-descriptions-item>
        <a-descriptions-item label="Manager">{{ areaData.managerName || 'N/A' }}</a-descriptions-item>
        <a-descriptions-item label="Status">
          <a-badge
            :status="areaData.status === 1 ? 'success' : 'error'"
            :text="areaData.status === 1 ? 'Enabled' : 'Disabled'"
          />
        </a-descriptions-item>
        <a-descriptions-item label="Description" :span="2">
          {{ areaData.description || 'No description' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 统计信息 -->
      <a-descriptions title="Statistics" :column="3" bordered style="margin-top: 24px">
        <a-descriptions-item label="Total Devices">
          <a-statistic :value="areaData.deviceCount || 0" />
        </a-descriptions-item>
        <a-descriptions-item label="Online Devices">
          <a-statistic :value="areaData.onlineDeviceCount || 0" :value-style="{ color: '#3f8600' }" />
        </a-descriptions-item>
        <a-descriptions-item label="Total Permissions">
          <a-statistic :value="areaData.totalPermissions || 0" />
        </a-descriptions-item>
      </a-descriptions>

      <!-- 时间信息 -->
      <a-descriptions title="Time Information" :column="2" bordered style="margin-top: 24px">
        <a-descriptions-item label="Created At">{{ areaData.createTime }}</a-descriptions-item>
        <a-descriptions-item label="Updated At">{{ areaData.updateTime }}</a-descriptions-item>
      </a-descriptions>

      <!-- 操作按钮 -->
      <div style="margin-top: 24px; text-align: center;">
        <a-space>
          <a-button @click="$emit('update:visible', false)">Close</a-button>
          <a-button type="primary" @click="handleEdit">Edit</a-button>
        </a-space>
      </div>
    </div>

    <a-empty v-else description="No area data available" />
  </a-modal>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    areaData: {
      type: Object,
      default: null,
    },
  });

  const emit = defineEmits(['update:visible', 'edit']);

  const getAreaTypeColor = (type) => {
    const colorMap = {
      BUILDING: 'blue',
      FLOOR: 'green',
      ROOM: 'orange',
      ENTRANCE: 'purple',
      PARKING: 'cyan',
      OTHER: 'default',
    };
    return colorMap[type] || 'default';
  };

  const getAreaTypeText = (type) => {
    const textMap = {
      BUILDING: 'Building',
      FLOOR: 'Floor',
      ROOM: 'Room',
      ENTRANCE: 'Entrance',
      PARKING: 'Parking',
      OTHER: 'Other',
    };
    return textMap[type] || type;
  };

  const getSecurityPercent = (level) => {
    const percentMap = {
      LOW: 25,
      MEDIUM: 50,
      HIGH: 75,
      CRITICAL: 100,
    };
    return percentMap[level] || 0;
  };

  const getSecurityColor = (level) => {
    const colorMap = {
      LOW: '#52c41a',
      MEDIUM: '#faad14',
      HIGH: '#fa8c16',
      CRITICAL: '#f5222d',
    };
    return colorMap[level] || '#d9d9d9';
  };

  const handleEdit = () => {
    emit('edit', props.areaData);
    emit('update:visible', false);
  };
</script>

<style lang="less" scoped>
  .area-detail {
    .ant-statistic {
      text-align: center;
    }
  }
</style>