<!--
  @fileoverview 排班日历视图组件
  @author IOE-DREAM Team
  @description 支持月/周/日视图切换，拖拽排班，冲突检测
-->
<template>
  <div class="schedule-calendar-view">
    <!-- 工具栏 -->
    <div class="calendar-toolbar">
      <a-space>
        <!-- 视图切换 -->
        <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
          <a-radio-button value="MONTH">月视图</a-radio-button>
          <a-radio-button value="WEEK">周视图</a-radio-button>
          <a-radio-button value="DAY">日视图</a-radio-button>
        </a-radio-group>

        <!-- 日期导航 -->
        <a-space>
          <a-button size="small" @click="navigateDate(-1)">
            <template #icon><LeftOutlined /></template>
          </a-button>
          <a-button size="small" @click="goToToday">今天</a-button>
          <a-button size="small" @click="navigateDate(1)">
            <template #icon><RightOutlined /></template>
          </a-button>
          <span class="current-date-range">{{ currentDateRangeText }}</span>
        </a-space>

        <!-- 部门筛选 -->
        <a-select
          v-model:value="selectedDepartments"
          mode="multiple"
          placeholder="选择部门"
          style="width: 200px"
          :max-tag-count="2"
          size="small"
          allow-clear
          @change="handleDepartmentChange"
        >
          <a-select-option v-for="dept in departments" :key="dept.departmentId" :value="dept.departmentId">
            {{ dept.departmentName }}
          </a-select-option>
        </a-select>

        <!-- 班次筛选 -->
        <a-select
          v-model:value="selectedShifts"
          mode="multiple"
          placeholder="选择班次"
          style="width: 180px"
          :max-tag-count="2"
          size="small"
          allow-clear
          @change="handleShiftChange"
        >
          <a-select-option v-for="shift in shifts" :key="shift.shiftId" :value="shift.shiftId">
            <a-tag :color="shift.colorCode">{{ shift.shiftName }}</a-tag>
          </a-select-option>
        </a-select>

        <!-- 冲突检测 -->
        <a-button size="small" type="primary" ghost @click="handleConflictDetection" :loading="conflictLoading">
          <template #icon><AlertOutlined /></template>
          检测冲突
        </a-button>

        <!-- 自动排班 -->
        <a-button size="small" type="primary" @click="handleAutoSchedule">
          <template #icon><ThunderboltOutlined /></template>
          智能排班
        </a-button>
      </a-space>
    </div>

    <!-- 月视图日历 -->
    <div v-if="viewMode === 'MONTH'" class="month-view-container">
      <a-spin :spinning="loading">
        <!-- 星期标题 -->
        <div class="week-header">
          <div v-for="day in weekDays" :key="day" class="week-day">
            {{ day }}
          </div>
        </div>

        <!-- 日历网格 -->
        <div class="calendar-grid">
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            class="calendar-day"
            :class="{
              'is-other-month': !day.isCurrentMonth,
              'is-today': day.isToday,
              'is-weekend': day.isWeekend,
              'is-holiday': day.isHoliday
            }"
            @click="handleDayClick(day)"
            @dragover.prevent
            @drop="handleDrop(day, $event)"
          >
            <div class="day-header">
              <span class="day-number">{{ day.dayNumber }}</span>
              <span v-if="day.isHoliday" class="holiday-name">{{ day.holidayName }}</span>
              <a-badge :count="day.shiftCount" :number-style="{ backgroundColor: '#52c41a' }" />
            </div>

            <div class="day-content">
              <div
                v-for="record in day.records"
                :key="record.recordId"
                class="shift-record"
                :class="{ 'has-conflict': record.hasConflict }"
                :style="{ borderColor: record.shiftColor }"
                draggable="true"
                @dragstart="handleDragStart(record, $event)"
                @click="handleRecordClick(record)"
              >
                <div class="record-header">
                  <a-tag :color="record.shiftColor" size="small">{{ record.shiftName }}</a-tag>
                  <a-tag v-if="record.hasConflict" color="red" size="small">冲突</a-tag>
                </div>
                <div class="record-employees">
                  <a-avatar-group :max-count="3" size="small">
                    <a-tooltip v-for="emp in record.employees" :key="emp.employeeId" :title="emp.employeeName">
                      <a-avatar :style="{ backgroundColor: emp.color }">
                        {{ emp.employeeName.charAt(0) }}
                      </a-avatar>
                    </a-tooltip>
                  </a-avatar-group>
                  <span v-if="record.employeeCount > 3" class="more-count">+{{ record.employeeCount - 3 }}</span>
                </div>
                <div class="record-time">
                  <ClockCircleOutlined /> {{ record.startTime }}-{{ record.endTime }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>

    <!-- 周视图 -->
    <div v-else-if="viewMode === 'WEEK'" class="week-view-container">
      <a-spin :spinning="loading">
        <div class="week-grid">
          <div class="time-column">
            <div v-for="hour in 24" :key="hour" class="time-slot">
              {{ String(hour - 1).padStart(2, '0') }}:00
            </div>
          </div>
          <div v-for="day in weekDays" :key="day.date" class="day-column">
            <div class="day-column-header">
              <div class="day-name">{{ day.dayName }}</div>
              <div class="day-date">{{ day.date }}</div>
            </div>
            <div class="day-slots">
              <div
                v-for="hour in 24"
                :key="hour"
                class="hour-slot"
                @dragover.prevent
                @drop="handleDropToTimeSlot(day.date, hour, $event)"
              ></div>
              <div
                v-for="record in getRecordsForDate(day.date)"
                :key="record.recordId"
                class="week-record"
                :class="{ 'has-conflict': record.hasConflict }"
                :style="{
                  top: calculateRecordTop(record.startTime),
                  height: calculateRecordHeight(record.startTime, record.endTime),
                  backgroundColor: record.shiftColor + '20',
                  borderColor: record.shiftColor
                }"
                draggable="true"
                @dragstart="handleDragStart(record, $event)"
                @click="handleRecordClick(record)"
              >
                <div class="week-record-content">
                  <div class="record-shift">{{ record.shiftName }}</div>
                  <div class="record-time">{{ record.startTime }}-{{ record.endTime }}</div>
                  <div class="record-employees">
                    <a-avatar-group :max-count="2" size="small">
                      <a-tooltip v-for="emp in record.employees" :key="emp.employeeId" :title="emp.employeeName">
                        <a-avatar :style="{ backgroundColor: emp.color }" size="small">
                          {{ emp.employeeName.charAt(0) }}
                        </a-avatar>
                      </a-tooltip>
                    </a-avatar-group>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>

    <!-- 日视图 -->
    <div v-else-if="viewMode === 'DAY'" class="day-view-container">
      <a-spin :spinning="loading">
        <div class="day-timeline">
          <div class="timeline-header">
            <h3>{{ selectedDate }}</h3>
            <a-space>
              <a-statistic title="班次数" :value="dayStats.totalShifts" />
              <a-statistic title="人员数" :value="dayStats.totalEmployees" />
              <a-statistic title="覆盖率" :value="dayStats.coverageRate" suffix="%" />
            </a-space>
          </div>

          <div class="timeline-body">
            <div class="time-axis">
              <div v-for="hour in 24" :key="hour" class="time-axis-mark">
                {{ String(hour - 1).padStart(2, '0') }}:00
              </div>
            </div>

            <div class="timeline-content">
              <div
                v-for="record in dayRecords"
                :key="record.recordId"
                class="timeline-record"
                :class="{ 'has-conflict': record.hasConflict }"
                :style="{
                  top: calculateRecordTop(record.startTime),
                  height: calculateRecordHeight(record.startTime, record.endTime),
                  backgroundColor: record.shiftColor + '20',
                  borderColor: record.shiftColor
                }"
                draggable="true"
                @dragstart="handleDragStart(record, $event)"
                @click="handleRecordClick(record)"
              >
                <div class="timeline-record-content">
                  <div class="record-header-row">
                    <a-tag :color="record.shiftColor">{{ record.shiftName }}</a-tag>
                    <a-tag v-if="record.hasConflict" color="red">冲突</a-tag>
                  </div>
                  <div class="record-time-row">
                    <ClockCircleOutlined /> {{ record.startTime }} - {{ record.endTime }}
                  </div>
                  <div class="record-employees-row">
                    <a-avatar-group :max-count="5">
                      <a-tooltip v-for="emp in record.employees" :key="emp.employeeId">
                        <template #title>
                          {{ emp.employeeName }} - {{ emp.departmentName }}
                        </template>
                        <a-avatar :style="{ backgroundColor: emp.color }">
                          {{ emp.employeeName.charAt(0) }}
                        </a-avatar>
                      </a-tooltip>
                    </a-avatar-group>
                    <span v-if="record.employeeCount > 5" class="more-count">+{{ record.employeeCount - 5 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>

    <!-- 冲突检测结果对话框 -->
    <ConflictDetectionModal
      v-model:visible="conflictModalVisible"
      :conflicts="detectedConflicts"
      @resolve="handleResolveConflicts"
    />

    <!-- 排班记录详情/编辑对话框 -->
    <ScheduleRecordModal
      v-model:visible="recordModalVisible"
      :record-id="editingRecordId"
      :date="selectedDate"
      @success="handleRecordSaved"
    />

    <!-- 智能排班对话框 -->
    <IntelligentScheduleModal
      v-model:visible="autoScheduleModalVisible"
      @success="handleAutoScheduleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  LeftOutlined,
  RightOutlined,
  AlertOutlined,
  ThunderboltOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue';
import { scheduleApi, type CalendarViewData, type ScheduleRecord, type ConflictDetectionResult, type ScheduleConflict } from '@/api/business/attendance/schedule';
import ConflictDetectionModal from './ConflictDetectionModal.vue';
import ScheduleRecordModal from './ScheduleRecordModal.vue';
import IntelligentScheduleModal from './IntelligentScheduleModal.vue';

/**
 * 组件属性
 */
interface Props {
  planId: number;
}

const props = defineProps<Props>();

/**
 * 组件事件
 */
interface Emits {
  (e: 'dateChange', date: string): void;
  (e: 'recordClick', record: ScheduleRecord): void;
}

const emit = defineEmits<Emits>();

// 视图模式
const viewMode = ref<'MONTH' | 'WEEK' | 'DAY'>('MONTH');

// 当前日期
const currentDate = ref(dayjs());

// 选择的日期
const selectedDate = ref(dayjs().format('YYYY-MM-DD'));

// 筛选条件
const selectedDepartments = ref<number[]>([]);
const selectedShifts = ref<number[]>([]);

// 加载状态
const loading = ref(false);
const conflictLoading = ref(false);

// 对话框状态
const conflictModalVisible = ref(false);
const recordModalVisible = ref(false);
const autoScheduleModalVisible = ref(false);

// 编辑的记录ID
const editingRecordId = ref<number>();

// 检测到的冲突
const detectedConflicts = ref<ScheduleConflict[]>([]);

// 日历数据
const calendarData = ref<CalendarViewData>({
  dates: [],
  records: [],
  statistics: {
    totalShifts: 0,
    totalEmployees: 0,
    coverageRate: 0,
    conflictCount: 0
  }
});

// 部门列表
const departments = ref([
  { departmentId: 1, departmentName: '技术部' },
  { departmentId: 2, departmentName: '市场部' },
  { departmentId: 3, departmentName: '行政部' }
]);

// 班次列表
const shifts = ref([
  { shiftId: 1, shiftName: '正常班', colorCode: '#1890ff' },
  { shiftId: 2, shiftName: '早班', colorCode: '#52c41a' },
  { shiftId: 3, shiftName: '中班', colorCode: '#faad14' },
  { shiftId: 4, shiftName: '晚班', colorCode: '#722ed1' }
]);

// 拖拽中的记录
const draggedRecord = ref<ScheduleRecord>();

// 星期标题
const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

// 当前日期范围文本
const currentDateRangeText = computed(() => {
  if (viewMode.value === 'MONTH') {
    return currentDate.value.format('YYYY年MM月');
  } else if (viewMode.value === 'WEEK') {
    const start = currentDate.value.startOf('week');
    const end = currentDate.value.endOf('week');
    return `${start.format('MM/DD')} - ${end.format('MM/DD')}`;
  } else {
    return currentDate.value.format('YYYY年MM月DD日');
  }
});

// 日历天数
const calendarDays = computed(() => {
  if (viewMode.value !== 'MONTH') return [];

  const year = currentDate.value.year();
  const month = currentDate.value.month();
  const firstDay = dayjs().year(year).month(month).date(1);
  const lastDay = firstDay.endOf('month');
  const startDay = firstDay.day();

  const days = [];
  const today = dayjs();

  // 上个月的日期
  for (let i = startDay; i > 0; i--) {
    const date = firstDay.subtract(i, 'day');
    days.push(createDayInfo(date, false));
  }

  // 当月日期
  for (let i = 1; i <= lastDay.date(); i++) {
    const date = dayjs().year(year).month(month).date(i);
    days.push(createDayInfo(date, true));
  }

  // 下个月的日期
  const remainingDays = 42 - days.length;
  for (let i = 1; i <= remainingDays; i++) {
    const date = lastDay.add(i, 'day');
    days.push(createDayInfo(date, false));
  }

  return days;
});

// 周视图数据
const weekDaysData = computed(() => {
  if (viewMode.value !== 'WEEK') return [];

  const start = currentDate.value.startOf('week');
  const days = [];
  for (let i = 0; i < 7; i++) {
    const date = start.add(i, 'day');
    days.push({
      date: date.format('YYYY-MM-DD'),
      dayName: weekDays[date.day()],
      isToday: date.isSame(dayjs(), 'day')
    });
  }
  return days;
});

// 日统计
const dayStats = computed(() => ({
  totalShifts: calendarData.value.statistics.totalShifts,
  totalEmployees: calendarData.value.statistics.totalEmployees,
  coverageRate: calendarData.value.statistics.coverageRate
}));

// 日视图记录
const dayRecords = computed(() => {
  return calendarData.value.records.filter(r => r.workDate === selectedDate.value);
});

// 创建日期信息
const createDayInfo = (date: Dayjs, isCurrentMonth: boolean) => {
  const dateStr = date.format('YYYY-MM-DD');
  const dayRecords = calendarData.value.records.filter(r => r.workDate === dateStr);

  return {
    date: dateStr,
    dayNumber: date.date(),
    isCurrentMonth,
    isToday: date.isSame(dayjs(), 'day'),
    isWeekend: date.day() === 0 || date.day() === 6,
    isHoliday: false,
    holidayName: undefined,
    records: dayRecords,
    shiftCount: dayRecords.length
  };
};

// 获取指定日期的记录
const getRecordsForDate = (date: string) => {
  return calendarData.value.records.filter(r => r.workDate === date);
};

// 计算记录顶部位置（小时转分钟）
const calculateRecordTop = (startTime: string) => {
  const [hours, minutes] = startTime.split(':').map(Number);
  return (hours * 60 + minutes) + 'px';
};

// 计算记录高度
const calculateRecordHeight = (startTime: string, endTime: string) => {
  const [startHours, startMinutes] = startTime.split(':').map(Number);
  const [endHours, endMinutes] = endTime.split(':').map(Number);
  const startTotal = startHours * 60 + startMinutes;
  const endTotal = endHours * 60 + endMinutes;
  return Math.max(endTotal - startTotal, 30) + 'px';
};

// 日期导航
const navigateDate = (direction: number) => {
  if (viewMode.value === 'MONTH') {
    currentDate.value = currentDate.value.add(direction, 'month');
  } else if (viewMode.value === 'WEEK') {
    currentDate.value = currentDate.value.add(direction, 'week');
  } else {
    currentDate.value = currentDate.value.add(direction, 'day');
  }
  loadCalendarData();
};

const goToToday = () => {
  currentDate.value = dayjs();
  selectedDate.value = dayjs().format('YYYY-MM-DD');
  loadCalendarData();
};

// 部门变化
const handleDepartmentChange = () => {
  loadCalendarData();
};

// 班次变化
const handleShiftChange = () => {
  loadCalendarData();
};

// 点击日期
const handleDayClick = (day: any) => {
  selectedDate.value = day.date;
  viewMode.value = 'DAY';
  emit('dateChange', day.date);
};

// 点击记录
const handleRecordClick = (record: ScheduleRecord) => {
  editingRecordId.value = record.recordId;
  recordModalVisible.value = true;
  emit('recordClick', record);
};

// 拖拽开始
const handleDragStart = (record: ScheduleRecord, event: DragEvent) => {
  draggedRecord.value = record;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', String(record.recordId));
  }
};

// 拖拽放置
const handleDrop = (day: any, event: DragEvent) => {
  event.preventDefault();
  if (!draggedRecord.value) return;

  const newDate = day.date;
  // TODO: 调用API更新排班日期
  message.success(`已将排班移动到 ${newDate}`);
  draggedRecord.value = undefined;
};

// 拖拽到时间段
const handleDropToTimeSlot = (date: string, hour: number, event: DragEvent) => {
  event.preventDefault();
  if (!draggedRecord.value) return;

  const newStartTime = String(hour - 1).padStart(2, '0') + ':00:00';
  // TODO: 调用API更新排班时间
  message.success(`已移动到 ${date} ${newStartTime}`);
  draggedRecord.value = undefined;
};

// 冲突检测
const handleConflictDetection = async () => {
  conflictLoading.value = true;
  try {
    const result = await scheduleApi.validateConflicts({
      planId: props.planId,
      records: calendarData.value.records,
      startDate: currentDate.value.startOf(viewMode.value === 'MONTH' ? 'month' : viewMode.value === 'WEEK' ? 'week' : 'day').format('YYYY-MM-DD'),
      endDate: currentDate.value.endOf(viewMode.value === 'MONTH' ? 'month' : viewMode.value === 'WEEK' ? 'week' : 'day').format('YYYY-MM-DD')
    });

    if (result.data) {
      detectedConflicts.value = result.data.conflicts || [];
      if (detectedConflicts.value.length > 0) {
        conflictModalVisible.value = true;
        message.warning(`检测到 ${detectedConflicts.value.length} 个冲突`);
      } else {
        message.success('未检测到冲突');
      }
    }
  } catch (error) {
    console.error('冲突检测失败:', error);
    message.error('冲突检测失败');
  } finally {
    conflictLoading.value = false;
  }
};

// 解决冲突
const handleResolveConflicts = async (strategy: string) => {
  try {
    const result = await scheduleApi.resolveConflicts(detectedConflicts.value, strategy);
    if (result.data) {
      message.success(`已解决 ${result.data.resolvedConflicts} 个冲突`);
      conflictModalVisible.value = false;
      loadCalendarData();
    }
  } catch (error) {
    console.error('解决冲突失败:', error);
    message.error('解决冲突失败');
  }
};

// 智能排班
const handleAutoSchedule = () => {
  autoScheduleModalVisible.value = true;
};

// 智能排班成功
const handleAutoScheduleSuccess = () => {
  autoScheduleModalVisible.value = false;
  loadCalendarData();
};

// 记录保存成功
const handleRecordSaved = () => {
  recordModalVisible.value = false;
  loadCalendarData();
};

// 加载日历数据
const loadCalendarData = async () => {
  loading.value = true;
  try {
    const startDate = currentDate.value.startOf(viewMode.value === 'MONTH' ? 'month' : viewMode.value === 'WEEK' ? 'week' : 'day').format('YYYY-MM-DD');
    const endDate = currentDate.value.endOf(viewMode.value === 'MONTH' ? 'month' : viewMode.value === 'WEEK' ? 'week' : 'day').format('YYYY-MM-DD');

    const result = await scheduleApi.getCalendarView({
      viewMode: viewMode.value,
      startDate,
      endDate,
      departmentIds: selectedDepartments.value,
      employeeIds: [],
      shiftIds: selectedShifts.value
    });

    if (result.data) {
      calendarData.value = result.data;
    }
  } catch (error) {
    console.error('加载日历数据失败:', error);
    message.error('加载日历数据失败');
  } finally {
    loading.value = false;
  }
};

// 监听视图模式变化
watch(viewMode, () => {
  loadCalendarData();
});

// 初始化
onMounted(() => {
  loadCalendarData();
});
</script>

<style scoped lang="less">
.schedule-calendar-view {
  .calendar-toolbar {
    margin-bottom: 16px;
    padding: 12px;
    background: #fff;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .current-date-range {
      font-weight: 600;
      font-size: 14px;
      min-width: 120px;
      text-align: center;
    }
  }

  // 月视图
  .month-view-container {
    background: #fff;
    border-radius: 4px;
    padding: 16px;

    .week-header {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 1px;
      background: #f0f0f0;
      margin-bottom: 1px;

      .week-day {
        padding: 12px;
        text-align: center;
        font-weight: 600;
        background: #fafafa;
      }
    }

    .calendar-grid {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 1px;
      background: #f0f0f0;
      border: 1px solid #f0f0f0;

      .calendar-day {
        background: #fff;
        min-height: 120px;
        padding: 8px;
        cursor: pointer;
        transition: background 0.2s;

        &:hover {
          background: #f5f5f5;
        }

        &.is-today {
          background: #e6f7ff;
        }

        &.is-weekend {
          background: #fafafa;
        }

        &.is-holiday {
          background: #fff1f0;
        }

        .day-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .day-number {
            font-weight: 600;
            font-size: 14px;
          }

          .holiday-name {
            font-size: 12px;
            color: #ff4d4f;
          }
        }

        .day-content {
          .shift-record {
            padding: 6px 8px;
            margin-bottom: 4px;
            border-left: 3px solid;
            border-radius: 2px;
            background: #fafafa;
            cursor: move;
            transition: all 0.2s;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            }

            &.has-conflict {
              background: #fff1f0;
              border-left-width: 4px;
            }

            .record-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 4px;
            }

            .record-employees {
              display: flex;
              align-items: center;
              gap: 4px;
              margin-bottom: 4px;

              .more-count {
                font-size: 11px;
                color: #8c8c8c;
              }
            }

            .record-time {
              font-size: 11px;
              color: #8c8c8c;
            }
          }
        }
      }
    }
  }

  // 周视图
  .week-view-container {
    background: #fff;
    border-radius: 4px;
    padding: 16px;
    overflow-x: auto;

    .week-grid {
      display: grid;
      grid-template-columns: 60px repeat(7, 1fr);
      min-width: 1000px;

      .time-column {
        .time-slot {
          height: 60px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 11px;
          color: #8c8c8c;
          border-bottom: 1px solid #f0f0f0;
        }
      }

      .day-column {
        border-left: 1px solid #f0f0f0;

        .day-column-header {
          padding: 12px;
          text-align: center;
          border-bottom: 1px solid #f0f0f0;
          background: #fafafa;

          .day-name {
            font-weight: 600;
            margin-bottom: 4px;
          }

          .day-date {
            font-size: 12px;
            color: #8c8c8c;
          }
        }

        .day-slots {
          position: relative;
          height: 1440px; // 24 hours * 60px

          .hour-slot {
            height: 60px;
            border-bottom: 1px dashed #f0f0f0;
          }

          .week-record {
            position: absolute;
            left: 4px;
            right: 4px;
            padding: 6px;
            border-left: 3px solid;
            border-radius: 2px;
            cursor: move;
            overflow: hidden;

            &.has-conflict {
              border-left-width: 4px;
            }

            .week-record-content {
              .record-shift {
                font-weight: 600;
                font-size: 12px;
                margin-bottom: 4px;
              }

              .record-time {
                font-size: 11px;
                color: #8c8c8c;
                margin-bottom: 4px;
              }
            }
          }
        }
      }
    }
  }

  // 日视图
  .day-view-container {
    background: #fff;
    border-radius: 4px;
    padding: 16px;

    .day-timeline {
      .timeline-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 1px solid #f0f0f0;

        h3 {
          margin: 0;
        }
      }

      .timeline-body {
        display: flex;
        position: relative;

        .time-axis {
          width: 60px;

          .time-axis-mark {
            height: 60px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            color: #8c8c8c;
            border-bottom: 1px solid #f0f0f0;
          }
        }

        .timeline-content {
          flex: 1;
          position: relative;
          height: 1440px;
          border-left: 1px solid #f0f0f0;

          .timeline-record {
            position: absolute;
            left: 8px;
            right: 8px;
            padding: 12px;
            border-left: 4px solid;
            border-radius: 4px;
            background: #fff;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            cursor: move;

            &.has-conflict {
              border-left-width: 6px;
              background: #fff1f0;
            }

            .timeline-record-content {
              .record-header-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 8px;
              }

              .record-time-row {
                font-size: 12px;
                color: #8c8c8c;
                margin-bottom: 8px;
              }

              .record-employees-row {
                display: flex;
                align-items: center;
                gap: 8px;

                .more-count {
                  font-size: 12px;
                  color: #8c8c8c;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
