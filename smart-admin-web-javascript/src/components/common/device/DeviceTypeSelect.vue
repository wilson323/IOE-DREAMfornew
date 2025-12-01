<template>
  <a-select
    v-model:value="selectedType"
    :placeholder="placeholder"
    :allow-clear="allowClear"
    :disabled="disabled"
    style="width: 100%"
    @change="handleChange"
  >
    <a-select-option
      v-for="type in deviceTypes"
      :key="type.value"
      :value="type.value"
    >
      {{ type.label }}
    </a-select-option>
  </a-select>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择设备类型'
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:value', 'change'])

const deviceTypes = [
  { value: 'CAMERA', label: '摄像头' },
  { value: 'ACCESS_CONTROLLER', label: '门禁控制器' },
  { value: 'CONSUMPTION_TERMINAL', label: '消费终端' },
  { value: 'ATTENDANCE_MACHINE', label: '考勤机' },
  { value: 'ALARM_DEVICE', label: '报警器' },
  { value: 'INTERCOM', label: '对讲机' },
  { value: 'LED_SCREEN', label: 'LED屏' },
  { value: 'DOOR_MAGNET', label: '门磁' }
]

const selectedType = computed({
  get: () => props.value,
  set: (value) => emit('update:value', value)
})

const handleChange = (value) => {
  emit('update:value', value)
  emit('change', value)
}
</script>