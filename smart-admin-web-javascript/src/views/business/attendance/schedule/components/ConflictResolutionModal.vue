<!--
  * 冲突解决弹窗
-->
<template>
  <a-modal
    v-model:open="visible"
    title="解决排班冲突"
    width="700px"
    @ok="handleResolve"
    @cancel="handleCancel"
  >
    <a-alert
      v-if="conflict"
      :message="conflict.title"
      :description="conflict.description"
      :type="getAlertType(conflict.type)"
      show-icon
      style="margin-bottom: 16px"
    />

    <a-form layout="vertical">
      <a-form-item label="冲突类型">
        <a-tag :color="getConflictColor(conflict.type)">
          {{ conflict.type }}
        </a-tag>
      </a-form-item>

      <a-form-item label="涉及班次">
        <a-list size="small" :data-source="conflict.shifts">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>{{ item.shiftName }}</template>
                <template #description>
                  {{ item.startTime }}-{{ item.endTime }} | {{ item.employeeName }}
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </a-form-item>

      <a-form-item label="解决方案">
        <a-radio-group v-model:value="resolutionType">
          <a-radio value="keep_first">保留第一个班次</a-radio>
          <a-radio value="keep_second">保留第二个班次</a-radio>
          <a-radio value="merge">合并班次</a-radio>
          <a-radio value="split">拆分班次</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item v-if="resolutionType === 'split'" label="拆分时间点">
        <a-time-picker
          v-model:value="splitTime"
          format="HH:mm"
          placeholder="选择拆分时间"
        />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleResolve" :loading="loading">
          应用解决方案
        </a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
import { ref, computed } from 'vue';
import { message } from 'ant-design-vue';

// Props
const props = defineProps({
  open: Boolean,
  conflict: Object
});

// Emits
const emit = defineEmits(['update:open', 'resolve']);

// 状态
const visible = computed({
  get: () => props.open,
  set: (val) => emit('update:open', val)
});

const resolutionType = ref('keep_first');
const splitTime = ref(null);
const loading = ref(false);

// 方法：获取警告类型
const getAlertType = (type) => {
  const typeMap = {
    '时间重叠': 'error',
    '超时工作': 'warning',
    '人员不足': 'info'
  };
  return typeMap[type] || 'warning';
};

// 方法：获取冲突颜色
const getConflictColor = (type) => {
  const colorMap = {
    '时间重叠': 'red',
    '超时工作': 'orange',
    '人员不足': 'blue'
  };
  return colorMap[type] || 'default';
};

// 方法：应用解决方案
const handleResolve = () => {
  loading.value = true;

  setTimeout(() => {
    const resolution = {
      conflict: props.conflict,
      type: resolutionType.value,
      splitTime: splitTime.value,
      timestamp: new Date().toISOString()
    };

    emit('resolve', resolution);
    loading.value = false;
    resolutionType.value = 'keep_first';
    splitTime.value = null;

    message.success('解决方案已应用');
  }, 500);
};

// 方法：取消
const handleCancel = () => {
  visible.value = false;
  resolutionType.value = 'keep_first';
  splitTime.value = null;
};
</script>
