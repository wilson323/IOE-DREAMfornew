<!--
 * 排班详情抽屉组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-17
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-drawer
    v-model:open="visible"
    title="排班详情"
    :width="800"
    placement="right"
    @close="handleClose"
  >
    <div class="schedule-detail-container">
      <a-spin :spinning="loading">
        <div v-if="scheduleData">
          <!-- 基本信息 -->
          <a-card title="基本信息" size="small" :bordered="false" class="mb-4">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="员工姓名">
                {{ scheduleData.employeeName }}
              </a-descriptions-item>
              <a-descriptions-item label="所属部门">
                {{ scheduleData.departmentName }}
              </a-descriptions-item>
              <a-descriptions-item label="排班日期">
                {{ format_date(scheduleData.scheduleDate) }}
              </a-descriptions-item>
              <a-descriptions-item label="排班类型">
                <a-tag :color="getScheduleTypeColor(scheduleData.scheduleType)">
                  {{ getScheduleTypeText(scheduleData.scheduleType) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="scheduleData.status === 'ACTIVE' ? 'green' : 'orange'">
                  {{ scheduleData.status === 'ACTIVE' ? '生效中' : '已失效' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="优先级">
                <a-tag color="blue">{{ scheduleData.priority || '普通' }}</a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <!-- 工作时间配置 -->
          <a-card title="工作时间配置" size="small" :bordered="false" class="mb-4">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="工作开始时间">
                {{ scheduleData.workStartTime || '--:--' }}
              </a-descriptions-item>
              <a-descriptions-item label="工作结束时间">
                {{ scheduleData.workEndTime || '--:--' }}
              </a-descriptions-item>
              <a-descriptions-item label="休息开始时间">
                {{ scheduleData.breakStartTime || '无休息' }}
              </a-descriptions-item>
              <a-descriptions-item label="休息结束时间">
                {{ scheduleData.breakEndTime || '无休息' }}
              </a-descriptions-item>
              <a-descriptions-item label="工作时长">
                {{ calculateWorkHours() }} 小时
              </a-descriptions-item>
              <a-descriptions-item label="休息时长">
                {{ calculateBreakHours() }} 小时
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <!-- 特殊规则 -->
          <a-card title="特殊规则" size="small" :bordered="false" class="mb-4">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="是否需要位置验证">
                <a-tag :color="scheduleData.locationRequired ? 'red' : 'green'">
                  {{ scheduleData.locationRequired ? '是' : '否' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="最大允许距离">
                {{ scheduleData.maxDistance ? `${scheduleData.maxDistance}米` : '无限制' }}
              </a-descriptions-item>
              <a-descriptions-item label="是否需要设备验证">
                <a-tag :color="scheduleData.deviceRequired ? 'red' : 'green'">
                  {{ scheduleData.deviceRequired ? '是' : '否' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="迟到宽限分钟">
                {{ scheduleData.lateGraceMinutes || 0 }} 分钟
              </a-descriptions-item>
              <a-descriptions-item label="早退宽限分钟">
                {{ scheduleData.earlyLeaveGraceMinutes || 0 }} 分钟
              </a-descriptions-item>
              <a-descriptions-item label="自动审批">
                <a-tag :color="scheduleData.autoApproval ? 'green' : 'orange'">
                  {{ scheduleData.autoApproval ? '是' : '否' }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <!-- 生效时间 -->
          <a-card title="生效时间" size="small" :bordered="false" class="mb-4">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="生效开始日期">
                {{ format_date(scheduleData.effectiveDate) }}
              </a-descriptions-item>
              <a-descriptions-item label="生效结束日期">
                {{ scheduleData.expiryDate || '永久生效' }}
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                {{ format_datetime(scheduleData.createTime) }}
              </a-descriptions-item>
              <a-descriptions-item label="更新时间">
                {{ format_datetime(scheduleData.updateTime) }}
              </a-descriptions-item>
              <a-descriptions-item label="创建人">
                {{ scheduleData.createUserName }}
              </a-descriptions-item>
              <a-descriptions-item label="更新人">
                {{ scheduleData.updateUserName }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <!-- 备注信息 -->
          <a-card title="备注信息" size="small" :bordered="false" class="mb-4">
            <div class="remark-content">
              {{ scheduleData.remark || '无备注信息' }}
            </div>
          </a-card>

          <!-- 操作历史 -->
          <a-card title="操作历史" size="small" :bordered="false">
            <a-timeline>
              <a-timeline-item
                v-for="history in operationHistory"
                :key="history.id"
                :color="history.color"
              >
                <div class="history-item">
                  <div class="history-title">{{ history.title }}</div>
                  <div class="history-desc">{{ history.description }}</div>
                  <div class="history-time">{{ format_datetime(history.createTime) }}</div>
                </div>
              </a-timeline-item>
            </a-timeline>
          </a-card>

          <!-- 操作按钮 -->
          <div class="schedule-actions">
            <a-space>
              <a-button type="primary" @click="handleEdit" v-privilege="'attendance:schedule:update'">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-button @click="handleCopy" v-privilege="'attendance:schedule:add'">
                <template #icon>
                  <CopyOutlined />
                </template>
                复制
              </a-button>
              <a-button danger @click="handleDelete" v-privilege="'attendance:schedule:delete'">
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
            </a-space>
          </div>
        </div>
        <a-empty v-else description="暂无数据" />
      </a-spin>
    </div>
  </a-drawer>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  EditOutlined,
  CopyOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue';
import { attendanceApi } from '/@/api/business/attendance/attendance-api';
import { format_date, format_datetime } from '/@/utils/format';
import { smartSentry } from '/@/lib/smart-sentry';

// Props
const props = defineProps({
  scheduleId: {
    type: Number,
    default: null
  }
});

// Emits
const emit = defineEmits(['edit', 'copy', 'delete']);

// 响应式数据
const visible = ref(false);
const loading = ref(false);
const scheduleData = ref(null);
const operationHistory = ref([]);

// 显示抽屉
function showModal(scheduleId) {
  if (scheduleId) {
    visible.value = true;
    loadScheduleDetail(scheduleId);
    loadOperationHistory(scheduleId);
  }
}

// 隐藏抽屉
function handleClose() {
  visible.value = false;
  scheduleData.value = null;
  operationHistory.value = [];
}

// 加载排班详情
async function loadScheduleDetail(scheduleId) {
  try {
    loading.value = true;
    const result = await attendanceApi.getDepartmentSchedule({ scheduleId });
    scheduleData.value = result.data;
  } catch (err) {
    smartSentry.captureError(err);
    message.error('加载排班详情失败');
  } finally {
    loading.value = false;
  }
}

// 加载操作历史
async function loadOperationHistory(scheduleId) {
  try {
    // 这里应该调用操作历史API，暂时使用模拟数据
    operationHistory.value = [
      {
        id: 1,
        title: '创建排班',
        description: '用户 创建了固定排班',
        color: 'green',
        createTime: '2025-11-17 10:00:00'
      },
      {
        id: 2,
        title: '修改排班',
        description: '用户 修改了工作时间',
        color: 'blue',
        createTime: '2025-11-17 14:00:00'
      }
    ];
  } catch (err) {
    smartSentry.captureError(err);
  }
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

// 计算工作时长
function calculateWorkHours() {
  if (!scheduleData.value?.workStartTime || !scheduleData.value?.workEndTime) {
    return 0;
  }

  const [startHour, startMinute] = scheduleData.value.workStartTime.split(':').map(Number);
  const [endHour, endMinute] = scheduleData.value.workEndTime.split(':').map(Number);

  let workMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
  if (workMinutes < 0) {
    workMinutes += 24 * 60; // 跨天计算
  }

  // 减去休息时间
  if (scheduleData.value.breakStartTime && scheduleData.value.breakEndTime) {
    const [breakStartHour, breakStartMinute] = scheduleData.value.breakStartTime.split(':').map(Number);
    const [breakEndHour, breakEndMinute] = scheduleData.value.breakEndTime.split(':').map(Number);
    const breakMinutes = (breakEndHour * 60 + breakEndMinute) - (breakStartHour * 60 + breakStartMinute);
    workMinutes -= breakMinutes;
  }

  return (workMinutes / 60).toFixed(2);
}

// 计算休息时长
function calculateBreakHours() {
  if (!scheduleData.value?.breakStartTime || !scheduleData.value?.breakEndTime) {
    return 0;
  }

  const [startHour, startMinute] = scheduleData.value.breakStartTime.split(':').map(Number);
  const [endHour, endMinute] = scheduleData.value.breakEndTime.split(':').map(Number);

  let breakMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
  if (breakMinutes < 0) {
    breakMinutes += 24 * 60;
  }

  return (breakMinutes / 60).toFixed(2);
}

// 编辑
function handleEdit() {
  emit('edit', scheduleData.value);
  handleClose();
}

// 复制
function handleCopy() {
  emit('copy', scheduleData.value);
  handleClose();
}

// 删除
function handleDelete() {
  Modal.confirm({
    title: '提示',
    content: '确认删除此排班记录吗？',
    onOk() {
      emit('delete', scheduleData.value.scheduleId);
      handleClose();
    },
  });
}

// 暴露方法给父组件
defineExpose({
  showModal
});
</script>

<style lang="less" scoped>
.schedule-detail-container {
  padding: 16px 0;
}

.mb-4 {
  margin-bottom: 16px;
}

.remark-content {
  min-height: 80px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
  color: #666;
  line-height: 1.6;
}

.schedule-actions {
  margin-top: 24px;
  text-align: center;
}

.history-item {
  .history-title {
    font-weight: 500;
    color: #262626;
    margin-bottom: 4px;
  }

  .history-desc {
    color: #8c8c8c;
    font-size: 12px;
    margin-bottom: 4px;
  }

  .history-time {
    color: #bfbfbf;
    font-size: 12px;
  }
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  color: #262626;
}

:deep(.ant-card-head-title) {
  font-weight: 600;
  color: #262626;
}

:deep(.ant-timeline-item-content) {
  margin-bottom: 16px;
}
</style>