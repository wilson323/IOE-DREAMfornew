<!--
  @fileoverview 冲突检测结果对话框组件
  @author IOE-DREAM Team
  @description 展示排班冲突列表，支持查看详情和解决冲突
-->
<template>
  <a-modal
    :visible="visible"
    :title="`冲突检测结果 (${conflicts.length}个)`"
    :width="900"
    :footer="null"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <!-- 冲突统计 -->
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="6">
          <a-statistic
            title="高危冲突"
            :value="highSeverityCount"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #prefix>
              <ExclamationCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="中危冲突"
            :value="mediumSeverityCount"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="低危冲突"
            :value="lowSeverityCount"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <InfoCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="6">
          <a-space direction="vertical" :size="4">
            <span>解决策略:</span>
            <a-select v-model:value="selectedStrategy" style="width: 100%;" size="small">
              <a-select-option value="AUTO">自动解决</a-select-option>
              <a-select-option value="MANUAL">手动处理</a-select-option>
              <a-select-option value="PRIORITY">优先级处理</a-select-option>
            </a-select>
          </a-space>
        </a-col>
      </a-row>

      <!-- 冲突列表 -->
      <a-collapse v-model:active-key="activeKeys" style="margin-bottom: 16px;">
        <a-collapse-panel
          v-for="conflict in conflicts"
          :key="conflict.conflictId"
          :header="renderConflictHeader(conflict)"
        >
          <template #extra>
            <a-tag :color="getSeverityColor(conflict.severity)">
              {{ getSeverityText(conflict.severity) }}
            </a-tag>
          </template>

          <a-descriptions bordered :column="1" size="small">
            <a-descriptions-item label="冲突类型">
              <a-tag>{{ getConflictTypeText(conflict.type) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="冲突描述">
              {{ conflict.description }}
            </a-descriptions-item>
            <a-descriptions-item label="影响记录">
              <div style="max-height: 200px; overflow-y: auto;">
                <a-timeline size="small">
                  <a-timeline-item
                    v-for="(record, index) in conflict.affectedRecords"
                    :key="index"
                  >
                    <div>
                      <strong>{{ record.employeeName }}</strong>
                      <a-divider type="vertical" />
                      <a-tag :color="getShiftColor(record.shiftId)">{{ record.shiftName }}</a-tag>
                      <a-divider type="vertical" />
                      {{ record.workDate }}
                      <a-divider type="vertical" />
                      {{ record.startTime }}-{{ record.endTime }}
                    </div>
                  </a-timeline-item>
                </a-timeline>
              </div>
            </a-descriptions-item>
            <a-descriptions-item v-if="conflict.suggestedResolution" label="建议解决方案">
              {{ conflict.suggestedResolution }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 操作按钮 -->
          <div style="margin-top: 12px; text-align: right;">
            <a-space>
              <a-button size="small" @click="handleViewDetail(conflict)">
                查看详情
              </a-button>
              <a-button
                size="small"
                type="primary"
                @click="handleResolveSingle(conflict)"
              >
                解决此冲突
              </a-button>
            </a-space>
          </div>
        </a-collapse-panel>
      </a-collapse>

      <!-- 批量操作 -->
      <div style="text-align: right; padding-top: 16px; border-top: 1px solid #f0f0f0;">
        <a-space>
          <a-button @click="handleCancel">关闭</a-button>
          <a-button type="primary" :loading="resolving" @click="handleResolveAll">
            批量解决 ({{ conflicts.length }}个)
          </a-button>
        </a-space>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import {
  ExclamationCircleOutlined,
  WarningOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue';
import type { ScheduleConflict } from '@/api/business/attendance/schedule';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  conflicts: ScheduleConflict[];
}

/**
 * 组件事件
 */
interface Emits {
  (e: 'update:visible', visible: boolean): void;
  (e: 'resolve', strategy: string): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// 激活的折叠面板
const activeKeys = ref<string[]>([]);

// 解决策略
const selectedStrategy = ref<string>('AUTO');

// 加载状态
const loading = ref(false);
const resolving = ref(false);

// 冲突统计
const highSeverityCount = computed(() =>
  props.conflicts.filter(c => c.severity === 'HIGH').length
);

const mediumSeverityCount = computed(() =>
  props.conflicts.filter(c => c.severity === 'MEDIUM').length
);

const lowSeverityCount = computed(() =>
  props.conflicts.filter(c => c.severity === 'LOW').length
);

// 渲染冲突标题
const renderConflictHeader = (conflict: ScheduleConflict) => {
  const employeeNames = conflict.affectedRecords
    .map(r => r.employeeName)
    .slice(0, 3)
    .join(', ');

  const moreText = conflict.affectedRecords.length > 3
    ? ` 等${conflict.affectedRecords.length}人`
    : '';

  return `${conflict.type}: ${employeeNames}${moreText}`;
};

// 获取严重程度颜色
const getSeverityColor = (severity: string) => {
  const colors = {
    'HIGH': 'red',
    'MEDIUM': 'orange',
    'LOW': 'green'
  };
  return colors[severity] || 'default';
};

// 获取严重程度文本
const getSeverityText = (severity: string) => {
  const texts = {
    'HIGH': '高危',
    'MEDIUM': '中危',
    'LOW': '低危'
  };
  return texts[severity] || severity;
};

// 获取冲突类型文本
const getConflictTypeText = (type: string) => {
  const texts = {
    'EMPLOYEE_CONFLICT': '人员冲突',
    'SHIFT_CONFLICT': '班次冲突',
    'SKILL_MISMATCH': '技能不匹配',
    'AVAILABILITY_CONFLICT': '可用性冲突',
    'REST_PERIOD_VIOLATION': '休息时间违规'
  };
  return texts[type] || type;
};

// 获取班次颜色
const getShiftColor = (shiftId: number) => {
  const colors = ['#1890ff', '#52c41a', '#faad14', '#722ed1', '#eb2f96'];
  return colors[shiftId % colors.length] || '#1890ff';
};

// 查看详情
const handleViewDetail = (conflict: ScheduleConflict) => {
  console.log('查看冲突详情:', conflict);
  // TODO: 打开详情对话框
};

// 解决单个冲突
const handleResolveSingle = (conflict: ScheduleConflict) => {
  console.log('解决冲突:', conflict);
  message.success('已标记为待处理');
};

// 批量解决冲突
const handleResolveAll = () => {
  resolving.value = true;
  setTimeout(() => {
    resolving.value = false;
    emit('resolve', selectedStrategy.value);
  }, 1000);
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
};

// 监听冲突变化，默认展开高危冲突
watch(() => props.conflicts, (newConflicts) => {
  if (newConflicts && newConflicts.length > 0) {
    activeKeys.value = newConflicts
      .filter(c => c.severity === 'HIGH')
      .map(c => c.conflictId);
  }
}, { immediate: true });
</script>

<style scoped lang="less">
:deep(.ant-collapse-header) {
  font-weight: 600;
}

:deep(.ant-descriptions-item-label) {
  width: 100px;
  font-weight: 600;
}
</style>
