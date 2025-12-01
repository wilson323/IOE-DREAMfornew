<template>
  <a-tag :color="getStatusColor(status)" :class="statusClass">
    <template #icon>
      <component :is="getStatusIcon(status)" />
    </template>
    {{ getStatusText(status) }}
  </a-tag>
</template>

<script setup>
import { computed } from 'vue'
import {
  WifiOutlined,
  DisconnectOutlined,
  ExclamationCircleOutlined,
  ToolOutlined
} from '@ant-design/icons-vue'

const props = defineProps({
  status: {
    type: Number,
    required: true
  },
  size: {
    type: String,
    default: 'default'
  }
})

const getStatusColor = (status) => {
  const colorMap = {
    1: 'green',    // 在线
    0: 'red',      // 离线
    2: 'orange',   // 故障
    3: 'blue'      // 维护中
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    1: '在线',
    0: '离线',
    2: '故障',
    3: '维护中'
  }
  return textMap[status] || '未知'
}

const getStatusIcon = (status) => {
  const iconMap = {
    1: WifiOutlined,
    0: DisconnectOutlined,
    2: ExclamationCircleOutlined,
    3: ToolOutlined
  }
  return iconMap[status] || WifiOutlined
}

const statusClass = computed(() => {
  return [
    'device-status-tag',
    `device-status-tag-${props.size}`
  ]
})
</script>

<style lang="less" scoped>
.device-status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &.device-status-tag-small {
    font-size: 12px;
    padding: 2px 6px;
  }

  .anticon {
    font-size: 12px;
  }
}
</style>