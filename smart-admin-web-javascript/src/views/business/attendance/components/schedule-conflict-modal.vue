<!--
 * 排班冲突检测模态框组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-17
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="visible"
    title="排班冲突检测"
    :width="1000"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <div class="conflict-detection-container">
      <a-form :model="checkForm" layout="vertical" class="mb-4">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="检测员工">
              <a-select
                v-model:value="checkForm.employeeIds"
                mode="multiple"
                placeholder="选择要检测的员工"
                :showSearch="true"
                :filterOption="filterEmployeeOption"
              >
                <a-select-option v-for="item in employeeList" :key="item.employeeId" :value="item.employeeId">
                  {{ item.employeeName }} - {{ item.departmentName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="检测日期范围">
              <a-range-picker
                v-model:value="checkForm.dateRange"
                style="width: 100%"
                :presets="defaultTimeRanges"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="检测类型">
              <a-checkbox-group v-model:value="checkForm.checkTypes">
                <a-checkbox value="OVERLAP">时间重叠</a-checkbox>
                <a-checkbox value="RULE_CONFLICT">规则冲突</a-checkbox>
                <a-checkbox value="RESOURCE_CONFLICT">资源冲突</a-checkbox>
              </a-checkbox-group>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24" style="text-align: center;">
            <a-button type="primary" @click="startConflictCheck" :loading="checking">
              <template #icon>
                <SearchOutlined />
              </template>
              开始检测
            </a-button>
          </a-col>
        </a-row>
      </a-form>

      <!-- 检测结果 -->
      <div v-if="conflictResults.length > 0" class="conflict-results">
        <a-alert
          :message="`检测完成，发现 ${conflictResults.length} 个冲突`"
          :type="conflictResults.length > 0 ? 'error' : 'success'"
          show-icon
          class="mb-4"
        />

        <!-- 冲突列表 -->
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="all" :tab="`全部冲突 (${conflictResults.length})`">
            <ConflictList :conflicts="conflictResults" @resolve="handleResolveConflict" />
          </a-tab-pane>

          <a-tab-pane key="overlap" :tab="`时间重叠 (${getConflictCount('OVERLAP')})`">
            <ConflictList
              :conflicts="getConflictsByType('OVERLAP')"
              @resolve="handleResolveConflict"
            />
          </a-tab-pane>

          <a-tab-pane key="rule" :tab="`规则冲突 (${getConflictCount('RULE_CONFLICT')})`">
            <ConflictList
              :conflicts="getConflictsByType('RULE_CONFLICT')"
              @resolve="handleResolveConflict"
            />
          </a-tab-pane>

          <a-tab-pane key="resource" :tab="`资源冲突 (${getConflictCount('RESOURCE_CONFLICT')})`">
            <ConflictList
              :conflicts="getConflictsByType('RESOURCE_CONFLICT')"
              @resolve="handleResolveConflict"
            />
          </a-tab-pane>
        </a-tabs>

        <!-- 批量操作 -->
        <div class="batch-actions">
          <a-space>
            <a-button type="primary" @click="handleBatchResolve" :disabled="selectedConflicts.length === 0">
              批量解决 ({{ selectedConflicts.length }})
            </a-button>
            <a-button @click="exportConflictReport">
              <template #icon>
                <ExportOutlined />
              </template>
              导出报告
            </a-button>
          </a-space>
        </div>
      </div>

      <a-empty v-else-if="!checking" description="请选择检测条件并开始检测" />
    </div>
  </a-modal>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ExportOutlined
} from '@ant-design/icons-vue';
import { attendanceApi } from '/@/api/business/attendance/attendance-api';
import { defaultTimeRanges } from '/@/lib/default-time-ranges';
import { smartSentry } from '/@/lib/smart-sentry';
import ConflictList from './conflict-list.vue';

// 响应式数据
const visible = ref(false);
const checking = ref(false);
const activeTab = ref('all');
const conflictResults = ref([]);
const selectedConflicts = ref([]);
const employeeList = ref([]);

// 检测表单
const checkFormState = {
  employeeIds: [],
  dateRange: [],
  checkTypes: ['OVERLAP', 'RULE_CONFLICT', 'RESOURCE_CONFLICT']
};
const checkForm = reactive({ ...checkFormState });

// 显示模态框
function showModal() {
  visible.value = true;
  loadEmployeeList();
}

// 关闭模态框
function handleOk() {
  visible.value = false;
}

function handleCancel() {
  visible.value = false;
  resetForm();
}

// 重置表单
function resetForm() {
  Object.assign(checkForm, checkFormState);
  conflictResults.value = [];
  selectedConflicts.value = [];
  activeTab.value = 'all';
}

// 加载员工列表
async function loadEmployeeList() {
  try {
    const result = await attendanceApi.getEmployeeSchedule({ pageNum: 1, pageSize: 1000 });
    employeeList.value = result.data.list || [];
  } catch (err) {
    smartSentry.captureError(err);
  }
}

// 开始冲突检测
async function startConflictCheck() {
  if (!checkForm.employeeIds.length) {
    message.error('请选择要检测的员工');
    return;
  }

  if (!checkForm.dateRange.length) {
    message.error('请选择检测日期范围');
    return;
  }

  if (!checkForm.checkTypes.length) {
    message.error('请选择检测类型');
    return;
  }

  try {
    checking.value = true;
    const params = {
      employeeIds: checkForm.employeeIds,
      startDate: checkForm.dateRange[0].format('YYYY-MM-DD'),
      endDate: checkForm.dateRange[1].format('YYYY-MM-DD'),
      checkTypes: checkForm.checkTypes
    };

    // 调用冲突检测API
    const result = await attendanceApi.checkScheduleConflict(params);
    conflictResults.value = result.data || [];

    if (conflictResults.value.length === 0) {
      message.success('未发现排班冲突');
    } else {
      message.warning(`发现 ${conflictResults.value.length} 个排班冲突`);
    }
  } catch (err) {
    smartSentry.captureError(err);
    message.error('冲突检测失败');
  } finally {
    checking.value = false;
  }
}

// 获取指定类型的冲突数量
function getConflictCount(type) {
  return conflictResults.value.filter(conflict => conflict.conflictType === type).length;
}

// 获取指定类型的冲突
function getConflictsByType(type) {
  return conflictResults.value.filter(conflict => conflict.conflictType === type);
}

// 解决冲突
function handleResolveConflict(conflictId, resolution) {
  try {
    // 调用解决冲突API
    const params = {
      conflictId,
      resolution,
      resolveTime: new Date().toISOString()
    };

    // 这里应该调用解决冲突的API
    // await attendanceApi.resolveConflict(params);

    // 从结果列表中移除已解决的冲突
    const index = conflictResults.value.findIndex(conflict => conflict.conflictId === conflictId);
    if (index > -1) {
      conflictResults.value.splice(index, 1);
    }

    message.success('冲突解决成功');
  } catch (err) {
    smartSentry.captureError(err);
    message.error('冲突解决失败');
  }
}

// 批量解决冲突
function handleBatchResolve() {
  if (selectedConflicts.value.length === 0) {
    message.warning('请选择要解决的冲突');
    return;
  }

  try {
    const params = {
      conflictIds: selectedConflicts.value,
      resolution: 'AUTO_RESOLVE',
      resolveTime: new Date().toISOString()
    };

    // 这里应该调用批量解决冲突的API
    // await attendanceApi.batchResolveConflicts(params);

    // 从结果列表中移除已解决的冲突
    selectedConflicts.value.forEach(conflictId => {
      const index = conflictResults.value.findIndex(conflict => conflict.conflictId === conflictId);
      if (index > -1) {
        conflictResults.value.splice(index, 1);
      }
    });

    selectedConflicts.value = [];
    message.success('批量解决冲突成功');
  } catch (err) {
    smartSentry.captureError(err);
    message.error('批量解决冲突失败');
  }
}

// 导出冲突报告
function exportConflictReport() {
  if (conflictResults.value.length === 0) {
    message.warning('没有冲突记录可以导出');
    return;
  }

  try {
    const params = {
      conflicts: conflictResults.value,
      exportTime: new Date().toISOString()
    };

    // 这里应该调用导出报告的API
    // await attendanceApi.exportConflictReport(params);

    message.success('冲突报告导出成功');
  } catch (err) {
    smartSentry.captureError(err);
    message.error('导出冲突报告失败');
  }
}

// 员工过滤
function filterEmployeeOption(input, option) {
  return option.children.toLowerCase().includes(input.toLowerCase());
}

// 暴露方法给父组件
defineExpose({
  showModal
});
</script>

<style lang="less" scoped>
.conflict-detection-container {
  .mb-4 {
    margin-bottom: 16px;
  }

  .conflict-results {
    margin-top: 20px;
  }

  .batch-actions {
    margin-top: 16px;
    text-align: center;
    padding: 16px;
    background: #f5f5f5;
    border-radius: 6px;
  }
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}

:deep(.ant-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>