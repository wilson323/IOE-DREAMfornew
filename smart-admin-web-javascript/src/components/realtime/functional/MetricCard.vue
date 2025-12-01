<template>
  <a-card
    :class="['metric-card', `status-${status}`]"
    :size="size"
    :loading="loading"
  >
    <template #title>
      <span class="metric-title">{{ title }}</span>
    </template>

    <template #extra>
      <a-tooltip :title="tooltip">
        <info-circle-outlined v-if="tooltip" />
      </a-tooltip>
    </template>

    <div class="metric-content">
      <div class="metric-value">
        <span :class="['value-text', trend]">
          {{ formattedValue }}
        </span>
        <span v-if="unit" class="value-unit">{{ unit }}</span>
      </div>

      <div v-if="showTrend && previousValue !== null" class="metric-trend">
        <a-tag :color="trendColor">
          <component :is="trendIcon" />
          {{ trendPercentage }}%
        </a-tag>
        <span class="trend-text">较上期</span>
      </div>

      <div v-if="showProgress && targetValue !== null" class="metric-progress">
        <a-progress
          :percent="progressPercent"
          :status="progressStatus"
          :size="size === 'small' ? 'small' : 'default'"
        />
        <div class="progress-text">
          目标: {{ formattedTargetValue }}
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup>
import { computed } from 'vue'
import {
  InfoCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  MinusOutlined
} from '@ant-design/icons-vue'

// Props
const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  previousValue: {
    type: [Number, String],
    default: null
  },
  targetValue: {
    type: [Number, String],
    default: null
  },
  unit: {
    type: String,
    default: ''
  },
  formatter: {
    type: Function,
    default: null
  },
  status: {
    type: String,
    default: 'normal', // normal, success, warning, error
    validator: (value) => ['normal', 'success', 'warning', 'error'].includes(value)
  },
  size: {
    type: String,
    default: 'default', // small, default
    validator: (value) => ['small', 'default'].includes(value)
  },
  showTrend: {
    type: Boolean,
    default: true
  },
  showProgress: {
    type: Boolean,
    default: false
  },
  tooltip: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Computed
const numericValue = computed(() => {
  const val = parseFloat(props.value)
  return isNaN(val) ? 0 : val
})

const numericPreviousValue = computed(() => {
  if (props.previousValue === null) return null
  const val = parseFloat(props.previousValue)
  return isNaN(val) ? 0 : val
})

const numericTargetValue = computed(() => {
  if (props.targetValue === null) return null
  const val = parseFloat(props.targetValue)
  return isNaN(val) ? 0 : val
})

const formattedValue = computed(() => {
  if (props.formatter) {
    return props.formatter(numericValue.value)
  }

  if (numericValue.value >= 1000000) {
    return (numericValue.value / 1000000).toFixed(1) + 'M'
  } else if (numericValue.value >= 1000) {
    return (numericValue.value / 1000).toFixed(1) + 'K'
  }
  return numericValue.value.toString()
})

const formattedTargetValue = computed(() => {
  if (props.formatter && numericTargetValue.value !== null) {
    return props.formatter(numericTargetValue.value)
  }

  if (numericTargetValue.value === null) return ''

  if (numericTargetValue.value >= 1000000) {
    return (numericTargetValue.value / 1000000).toFixed(1) + 'M'
  } else if (numericTargetValue.value >= 1000) {
    return (numericTargetValue.value / 1000).toFixed(1) + 'K'
  }
  return numericTargetValue.value.toString()
})

const trend = computed(() => {
  if (numericPreviousValue.value === null) return 'neutral'

  if (numericValue.value > numericPreviousValue.value) return 'up'
  if (numericValue.value < numericPreviousValue.value) return 'down'
  return 'neutral'
})

const trendPercentage = computed(() => {
  if (numericPreviousValue.value === null || numericPreviousValue.value === 0) return 0

  const change = numericValue.value - numericPreviousValue.value
  return Math.abs((change / numericPreviousValue.value * 100)).toFixed(1)
})

const trendIcon = computed(() => {
  switch (trend.value) {
    case 'up': return ArrowUpOutlined
    case 'down': return ArrowDownOutlined
    default: return MinusOutlined
  }
})

const trendColor = computed(() => {
  switch (trend.value) {
    case 'up': return 'green'
    case 'down': return 'red'
    default: return 'default'
  }
})

const progressPercent = computed(() => {
  if (numericTargetValue.value === null || numericTargetValue.value === 0) return 0

  const percent = (numericValue.value / numericTargetValue.value) * 100
  return Math.min(Math.max(percent, 0), 100)
})

const progressStatus = computed(() => {
  const percent = progressPercent.value
  if (percent >= 100) return 'success'
  if (percent >= 80) return 'normal'
  if (percent >= 60) return 'active'
  return 'exception'
})
</script>

<style scoped>
.metric-card {
  text-align: center;
}

.metric-card.status-success {
  border-color: #52c41a;
}

.metric-card.status-warning {
  border-color: #faad14;
}

.metric-card.status-error {
  border-color: #ff4d4f;
}

.metric-title {
  font-weight: 500;
  color: #333;
}

.metric-content {
  padding: 16px 0;
}

.metric-value {
  margin-bottom: 12px;
}

.value-text {
  font-size: 32px;
  font-weight: 600;
  line-height: 1;
}

.value-text.up {
  color: #52c41a;
}

.value-text.down {
  color: #ff4d4f;
}

.value-text.neutral {
  color: #333;
}

.value-unit {
  font-size: 16px;
  color: #666;
  margin-left: 4px;
}

.metric-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.trend-text {
  font-size: 12px;
  color: #666;
}

.metric-progress {
  text-align: center;
}

.progress-text {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

/* 小尺寸样式调整 */
.metric-card :deep(.ant-card-head) {
  min-height: 40px;
  padding: 0 16px;
}

.metric-card :deep(.ant-card-head-title) {
  font-size: 14px;
}

.metric-card :deep(.ant-card-body) {
  padding: 16px;
}

.metric-card.size-small .value-text {
  font-size: 24px;
}

.metric-card.size-small .value-unit {
  font-size: 14px;
}
</style>