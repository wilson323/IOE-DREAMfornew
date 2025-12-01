<!--
 * 冲突列表组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-17
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <div class="conflict-list-container">
    <a-collapse v-model:activeKey="activeKeys" :bordered="false">
      <a-collapse-panel
        v-for="conflict in conflicts"
        :key="conflict.conflictId"
        :header="getConflictTitle(conflict)"
        :class="getConflictPanelClass(conflict)"
      >
        <template #extra>
          <a-space>
            <a-tag :color="getConflictTypeColor(conflict.conflictType)">
              {{ getConflictTypeText(conflict.conflictType) }}
            </a-tag>
            <a-tag :color="getConflictSeverityColor(conflict.severity)">
              {{ getConflictSeverityText(conflict.severity) }}
            </a-tag>
          </a-space>
        </template>

        <!-- 冲突详情 -->
        <div class="conflict-details">
          <a-row :gutter="16">
            <a-col :span="12">
              <div class="detail-item">
                <div class="detail-label">涉及员工：</div>
                <div class="detail-value">
                  <a-tag color="blue">{{ conflict.employeeName }}</a-tag>
                  <span class="employee-info">{{ conflict.departmentName }}</span>
                </div>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="detail-item">
                <div class="detail-label">冲突日期：</div>
                <div class="detail-value">
                  <a-tag color="green">{{ format_date(conflict.conflictDate) }}</a-tag>
                </div>
              </div>
            </a-col>
          </a-row>

          <a-row :gutter="16" class="mt-3">
            <a-col :span="24">
              <div class="detail-item">
                <div class="detail-label">冲突描述：</div>
                <div class="detail-value conflict-description">
                  {{ conflict.description }}
                </div>
              </div>
            </a-col>
          </a-row>

          <!-- 冲突的时间段信息 -->
          <div v-if="conflict.timeSlots && conflict.timeSlots.length > 0" class="time-slots-section mt-3">
            <div class="detail-label">冲突时间段：</div>
            <div class="time-slots">
              <div
                v-for="(slot, index) in conflict.timeSlots"
                :key="index"
                class="time-slot-item"
              >
                <a-tag color="orange">
                  {{ slot.startTime }} - {{ slot.endTime }}
                </a-tag>
                <span class="slot-desc">{{ slot.description }}</span>
              </div>
            </div>
          </div>

          <!-- 冲突的排班信息 -->
          <div v-if="conflict.conflictingSchedules" class="conflicting-schedules-section mt-3">
            <div class="detail-label">冲突排班：</div>
            <a-table
              :columns="scheduleColumns"
              :dataSource="conflict.conflictingSchedules"
              :pagination="false"
              size="small"
              bordered
            >
              <template #bodyCell="{ column, record, text }">
                <template v-if="column.dataIndex === 'scheduleType'">
                  <a-tag :color="getScheduleTypeColor(record.scheduleType)">
                    {{ getScheduleTypeText(record.scheduleType) }}
                  </a-tag>
                </template>
                <template v-else-if="column.dataIndex === 'workTime'">
                  <div>
                    <div>上班: {{ record.workStartTime }}</div>
                    <div>下班: {{ record.workEndTime }}</div>
                  </div>
                </template>
              </template>
            </a-table>
          </div>

          <!-- 建议解决方案 -->
          <div v-if="conflict.suggestions && conflict.suggestions.length > 0" class="suggestions-section mt-3">
            <div class="detail-label">建议解决方案：</div>
            <div class="suggestions">
              <div
                v-for="(suggestion, index) in conflict.suggestions"
                :key="index"
                class="suggestion-item"
              >
                <a-radio
                  v-model:value="selectedSolutions[conflict.conflictId]"
                  :value="suggestion.id"
                >
                  <div class="suggestion-content">
                    <div class="suggestion-title">{{ suggestion.title }}</div>
                    <div class="suggestion-desc">{{ suggestion.description }}</div>
                    <div class="suggestion-impact">
                      <a-tag :color="getImpactColor(suggestion.impact)">
                        影响: {{ suggestion.impact }}
                      </a-tag>
                    </div>
                  </div>
                </a-radio>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="conflict-actions mt-4">
            <a-space>
              <a-button
                type="primary"
                size="small"
                @click="applySolution(conflict)"
                :disabled="!selectedSolutions[conflict.conflictId]"
              >
                应用解决方案
              </a-button>
              <a-button size="small" @click="manualResolve(conflict)">
                手动解决
              </a-button>
              <a-button size="small" @click="ignoreConflict(conflict)">
                忽略冲突
              </a-button>
            </a-space>
          </div>
        </div>
      </a-collapse-panel>
    </a-collapse>

    <!-- 手动解决模态框 -->
    <a-modal
      v-model:open="showManualResolveModal"
      title="手动解决冲突"
      :width="600"
      @ok="confirmManualResolve"
      @cancel="cancelManualResolve"
    >
      <div v-if="selectedConflict">
        <a-form :model="manualResolveForm" layout="vertical">
          <a-form-item label="解决方式" required>
            <a-radio-group v-model:value="manualResolveForm.resolveType">
              <a-radio value="DELETE_SCHEDULE">删除排班</a-radio>
              <a-radio value="MODIFY_TIME">修改时间</a-radio>
              <a-radio value="CHANGE_EMPLOYEE">更换员工</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item
            v-if="manualResolveForm.resolveType === 'MODIFY_TIME'"
            label="新的工作时间"
            required
          >
            <a-row :gutter="16">
              <a-col :span="12">
                <a-time-picker
                  v-model:value="manualResolveForm.newStartTime"
                  format="HH:mm"
                  placeholder="开始时间"
                  style="width: 100%"
                />
              </a-col>
              <a-col :span="12">
                <a-time-picker
                  v-model:value="manualResolveForm.newEndTime"
                  format="HH:mm"
                  placeholder="结束时间"
                  style="width: 100%"
                />
              </a-col>
            </a-row>
          </a-form-item>

          <a-form-item
            v-if="manualResolveForm.resolveType === 'CHANGE_EMPLOYEE'"
            label="更换员工"
            required
          >
            <a-select
              v-model:value="manualResolveForm.newEmployeeId"
              placeholder="选择新员工"
              :showSearch="true"
            >
              <a-select-option
                v-for="employee in availableEmployees"
                :key="employee.employeeId"
                :value="employee.employeeId"
              >
                {{ employee.employeeName }} - {{ employee.departmentName }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="解决说明">
            <a-textarea
              v-model:value="manualResolveForm.resolveNote"
              :rows="3"
              placeholder="请输入解决说明"
            />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import { format_date } from '/@/utils/format';
import { smartSentry } from '/@/lib/smart-sentry';

// Props
const props = defineProps({
  conflicts: {
    type: Array,
    default: () => []
  }
});

// Emits
const emit = defineEmits(['resolve']);

// 响应式数据
const activeKeys = ref([]);
const selectedSolutions = ref({});
const showManualResolveModal = ref(false);
const selectedConflict = ref(null);
const availableEmployees = ref([]);

// 手动解决表单
const manualResolveFormState = {
  resolveType: 'DELETE_SCHEDULE',
  newStartTime: null,
  newEndTime: null,
  newEmployeeId: null,
  resolveNote: ''
};
const manualResolveForm = reactive({ ...manualResolveFormState });

// 表格列定义
const scheduleColumns = [
  {
    title: '排班类型',
    dataIndex: 'scheduleType',
    width: 100,
  },
  {
    title: '工作时间',
    dataIndex: 'workTime',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  }
];

// 获取冲突标题
function getConflictTitle(conflict) {
  return `${conflict.employeeName} - ${conflict.conflictDate} 的排班冲突`;
}

// 获取冲突面板样式类
function getConflictPanelClass(conflict) {
  return `conflict-panel-${conflict.severity.toLowerCase()}`;
}

// 获取冲突类型颜色
function getConflictTypeColor(type) {
  const colorMap = {
    'OVERLAP': 'red',
    'RULE_CONFLICT': 'orange',
    'RESOURCE_CONFLICT': 'purple'
  };
  return colorMap[type] || 'default';
}

// 获取冲突类型文本
function getConflictTypeText(type) {
  const textMap = {
    'OVERLAP': '时间重叠',
    'RULE_CONFLICT': '规则冲突',
    'RESOURCE_CONFLICT': '资源冲突'
  };
  return textMap[type] || type;
}

// 获取冲突严重程度颜色
function getConflictSeverityColor(severity) {
  const colorMap = {
    'HIGH': 'red',
    'MEDIUM': 'orange',
    'LOW': 'green'
  };
  return colorMap[severity] || 'default';
}

// 获取冲突严重程度文本
function getConflictSeverityText(severity) {
  const textMap = {
    'HIGH': '严重',
    'MEDIUM': '中等',
    'LOW': '轻微'
  };
  return textMap[severity] || severity;
}

// 获取排班类型颜色
function getScheduleTypeColor(type) {
  const colorMap = {
    'FIXED': 'blue',
    'FLEXIBLE': 'green',
    'SHIFT': 'orange',
    'REST': 'gray'
  };
  return colorMap[type] || 'default';
}

// 获取排班类型文本
function getScheduleTypeText(type) {
  const textMap = {
    'FIXED': '固定排班',
    'FLEXIBLE': '弹性排班',
    'SHIFT': '轮班制',
    'REST': '休息'
  };
  return textMap[type] || type;
}

// 获取影响程度颜色
function getImpactColor(impact) {
  const colorMap = {
    'HIGH': 'red',
    'MEDIUM': 'orange',
    'LOW': 'green'
  };
  return colorMap[impact] || 'default';
}

// 应用解决方案
function applySolution(conflict) {
  const solutionId = selectedSolutions.value[conflict.conflictId];
  if (!solutionId) {
    message.warning('请选择解决方案');
    return;
  }

  const solution = conflict.suggestions.find(s => s.id === solutionId);
  if (!solution) {
    message.error('解决方案不存在');
    return;
  }

  emit('resolve', conflict.conflictId, {
    type: 'AUTO_SUGGESTION',
    solution,
    resolveTime: new Date().toISOString()
  });
}

// 手动解决
function manualResolve(conflict) {
  selectedConflict.value = conflict;
  showManualResolveModal.value = true;
  loadAvailableEmployees();
}

// 确认手动解决
function confirmManualResolve() {
  if (!selectedConflict.value) {
    return;
  }

  const resolution = {
    type: 'MANUAL',
    resolveType: manualResolveForm.resolveType,
    newStartTime: manualResolveForm.newStartTime?.format('HH:mm'),
    newEndTime: manualResolveForm.newEndTime?.format('HH:mm'),
    newEmployeeId: manualResolveForm.newEmployeeId,
    resolveNote: manualResolveForm.resolveNote,
    resolveTime: new Date().toISOString()
  };

  emit('resolve', selectedConflict.value.conflictId, resolution);
  showManualResolveModal.value = false;
  resetManualResolveForm();
}

// 取消手动解决
function cancelManualResolve() {
  showManualResolveModal.value = false;
  resetManualResolveForm();
}

// 重置手动解决表单
function resetManualResolveForm() {
  Object.assign(manualResolveForm, manualResolveFormState);
  selectedConflict.value = null;
}

// 加载可用员工
async function loadAvailableEmployees() {
  try {
    // 这里应该调用获取可用员工的API
    // const result = await attendanceApi.getAvailableEmployees();
    // availableEmployees.value = result.data;

    // 暂时使用模拟数据
    availableEmployees.value = [
      { employeeId: 1, employeeName: '张三', departmentName: '技术部' },
      { employeeId: 2, employeeName: '李四', departmentName: '人事部' }
    ];
  } catch (err) {
    smartSentry.captureError(err);
  }
}

// 忽略冲突
function ignoreConflict(conflict) {
  emit('resolve', conflict.conflictId, {
    type: 'IGNORE',
    resolveNote: '用户选择忽略此冲突',
    resolveTime: new Date().toISOString()
  });
}
</script>

<style lang="less" scoped>
.conflict-list-container {
  .mt-3 {
    margin-top: 16px;
  }

  .mt-4 {
    margin-top: 20px;
  }

  .conflict-panel-high {
    border-left: 4px solid #ff4d4f;
  }

  .conflict-panel-medium {
    border-left: 4px solid #fa8c16;
  }

  .conflict-panel-low {
    border-left: 4px solid #52c41a;
  }
}

.conflict-details {
  .detail-item {
    margin-bottom: 12px;

    .detail-label {
      font-weight: 500;
      color: #262626;
      margin-bottom: 4px;
    }

    .detail-value {
      color: #595959;
    }
  }

  .employee-info {
    margin-left: 8px;
    color: #8c8c8c;
    font-size: 12px;
  }

  .conflict-description {
    background: #fff2e8;
    padding: 12px;
    border-radius: 6px;
    border-left: 3px solid #fa8c16;
    color: #d46b08;
  }
}

.time-slots-section {
  .time-slots {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .time-slot-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .slot-desc {
        color: #8c8c8c;
        font-size: 12px;
      }
    }
  }
}

.conflicting-schedules-section {
  :deep(.ant-table-small) {
    .ant-table-thead > tr > th {
      background: #fafafa;
      font-weight: 500;
    }
  }
}

.suggestions-section {
  .suggestions {
    .suggestion-item {
      margin-bottom: 16px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 6px;
      border: 1px solid #d9d9d9;

      &:hover {
        background: #e6f7ff;
        border-color: #91d5ff;
      }

      .suggestion-content {
        margin-left: 24px;

        .suggestion-title {
          font-weight: 500;
          color: #262626;
          margin-bottom: 4px;
        }

        .suggestion-desc {
          color: #8c8c8c;
          font-size: 12px;
          margin-bottom: 8px;
        }

        .suggestion-impact {
          margin-top: 8px;
        }
      }
    }
  }
}

.conflict-actions {
  text-align: center;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 6px;
}

:deep(.ant-collapse) {
  background: transparent;

  .ant-collapse-item {
    border: 1px solid #d9d9d9;
    margin-bottom: 8px;
    border-radius: 6px;

    .ant-collapse-header {
      background: #fafafa;
      border-radius: 6px 6px 0 0;
    }

    .ant-collapse-content {
      border-top: 1px solid #d9d9d9;
      border-radius: 0 0 6px 6px;
    }
  }
}
</style>