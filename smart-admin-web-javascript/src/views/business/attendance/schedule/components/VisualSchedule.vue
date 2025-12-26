<!--
  * 可视化智能排班组件
  *
  * 功能：
  * 1. 可视化排班界面（拖拽排班）
  * 2. 实时预览功能
  * 3. 冲突检测和提示
  * 4. 统计信息面板
-->
<template>
  <div class="visual-schedule-container">
    <!-- 顶部工具栏 -->
    <a-card size="small" :bordered="false" class="toolbar-card">
      <a-row :gutter="16" align="middle">
        <a-col :span="8">
          <a-space>
            <span style="font-weight: 600">排班时间：</span>
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              @change="handleDateChange"
            />
          </a-space>
        </a-col>

        <a-col :span="8" style="text-align: center">
          <a-space>
            <a-button type="primary" @click="handleAutoSchedule">
              <template #icon><ThunderboltOutlined /></template>
              智能排班
            </a-button>
            <a-button @click="handleClearAll">
              <template #icon><ClearOutlined /></template>
              清空
            </a-button>
            <a-button @click="handleExport">
              <template #icon><DownloadOutlined /></template>
              导出
            </a-button>
          </a-space>
        </a-col>

        <a-col :span="8" style="text-align: right">
          <a-space>
            <a-tag color="blue">总班次: {{ totalShifts }}</a-tag>
            <a-tag color="green">已排班: {{ scheduledShifts }}</a-tag>
            <a-tag color="orange">冲突: {{ conflictCount }}</a-tag>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 主内容区域 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <!-- 左侧：员工列表 -->
      <a-col :span="6">
        <a-card
          title="员工列表"
          size="small"
          :bordered="false"
          style="height: 600px"
          :body-style="{ padding: '8px', height: '540px', overflow: 'auto' }"
        >
          <template #extra>
            <a-input-search
              v-model:value="employeeSearch"
              placeholder="搜索员工"
              style="width: 150px"
              allow-clear
            />
          </template>

          <a-tree
            v-model:selectedKeys="selectedEmployeeIds"
            :tree-data="employeeTreeData"
            :field-names="{ title: 'label', key: 'value', children: 'children' }"
            show-line
            draggable
            @drop="handleEmployeeDrop"
          >
            <template #title="{ label, statistics }">
              <a-space>
                <span>{{ label }}</span>
                <a-tag v-if="statistics" size="small" color="blue">
                  {{ statistics.scheduled }}/{{ statistics.total }}
                </a-tag>
              </a-space>
            </template>
          </a-tree>
        </a-card>
      </a-col>

      <!-- 中间：排班日历 -->
      <a-col :span="12">
        <a-card
          title="排班日历"
          size="small"
          :bordered="false"
          style="height: 600px"
          :body-style="{ padding: '8px', height: '540px', overflow: 'auto' }"
        >
          <template #extra>
            <a-radio-group v-model:value="calendarViewMode" size="small">
              <a-radio-button value="week">周视图</a-radio-button>
              <a-radio-button value="month">月视图</a-radio-button>
            </a-radio-group>
          </template>

          <!-- 拖拽排班日历 -->
          <div class="schedule-calendar">
            <a-row :gutter="[8, 8]">
              <a-col
                v-for="day in calendarDays"
                :key="day.date"
                :span="calendarViewMode === 'week' ? 3 : 8"
              >
                <div
                  class="calendar-day"
                  :class="{ 'is-today': day.isToday, 'is-weekend': day.isWeekend }"
                  @dragover.prevent
                  @drop="handleShiftDrop($event, day.date)"
                >
                  <div class="day-header">
                    <span class="day-date">{{ day.date }}</span>
                    <span class="day-weekday">{{ day.weekday }}</span>
                  </div>

                  <div class="day-content">
                    <draggable
                      v-model="day.shifts"
                      group="shifts"
                      item-key="shiftId"
                      @change="handleShiftChange($event, day.date)"
                    >
                      <template #item="{ element: shift }">
                        <div
                          class="shift-item"
                          :class="getShiftClass(shift)"
                          draggable="true"
                          @dragstart="handleShiftDragStart($event, shift, day.date)"
                        >
                          <div class="shift-time">{{ shift.startTime }}-{{ shift.endTime }}</div>
                          <div class="shift-name">{{ shift.shiftName }}</div>
                          <div class="shift-employee">{{ shift.employeeName }}</div>
                          <a-button
                            type="text"
                            size="small"
                            class="shift-delete"
                            @click.stop="handleRemoveShift(shift, day.date)"
                          >
                            <CloseOutlined />
                          </a-button>
                        </div>
                      </template>
                    </draggable>

                    <!-- 空白提示 -->
                    <div v-if="day.shifts.length === 0" class="empty-hint">
                      拖拽班次到此
                    </div>
                  </div>
                </div>
              </a-col>
            </a-row>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：班次库和统计 -->
      <a-col :span="6">
        <!-- 班次库 -->
        <a-card
          title="班次库"
          size="small"
          :bordered="false"
          style="margin-bottom: 16px"
          :body-style="{ padding: '8px', height: '240px', overflow: 'auto' }"
        >
          <template #extra>
            <a-button size="small" type="link" @click="handleAddShift">
              <template #icon><PlusOutlined /></template>
              添加
            </a-button>
          </template>

          <div class="shift-palette">
            <div
              v-for="shift in shiftPalette"
              :key="shift.shiftId"
              class="palette-shift-item"
              draggable="true"
              @dragstart="handlePaletteDragStart($event, shift)"
            >
              <div class="shift-time">{{ shift.startTime }}-{{ shift.endTime }}</div>
              <div class="shift-name">{{ shift.shiftName }}</div>
            </div>
          </div>
        </a-card>

        <!-- 冲突检测面板 -->
        <a-card
          title="冲突检测"
          size="small"
          :bordered="false"
          style="height: 264px"
          :body-style="{ padding: '8px', height: '200px', overflow: 'auto' }"
        >
          <template #extra>
            <a-button size="small" type="primary" ghost @click="handleDetectConflicts">
              <template #icon><SearchOutlined /></template>
              检测
            </a-button>
          </template>

          <a-list
            size="small"
            :data-source="conflicts"
            :locale="{ emptyText: '暂无冲突' }"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: getConflictColor(item.type) }">
                      {{ getConflictIcon(item.type) }}
                    </a-avatar>
                  </template>
                  <template #title>
                    <a-space>
                      <span>{{ item.title }}</span>
                      <a-tag :color="getConflictColor(item.type)" size="small">
                        {{ item.type }}
                      </a-tag>
                    </a-space>
                  </template>
                  <template #description>{{ item.description }}</template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="link" size="small" @click="handleResolveConflict(item)">
                    解决
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 冲突解决弹窗 -->
    <ConflictResolutionModal
      v-model:open="conflictModalVisible"
      :conflict="selectedConflict"
      @resolve="handleConflictResolved"
    />

    <!-- 智能排班弹窗 -->
    <IntelligentScheduleModal
      v-model:open="intScheduleModalVisible"
      :date-range="dateRange"
      @confirm="handleIntScheduleConfirm"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import draggable from 'vuedraggable';
import {
  ThunderboltOutlined,
  ClearOutlined,
  DownloadOutlined,
  SearchOutlined,
  PlusOutlined,
  CloseOutlined
} from '@ant-design/icons-vue';
import ConflictResolutionModal from './ConflictResolutionModal.vue';
import IntelligentScheduleModal from './IntelligentScheduleModal.vue';

// Props
const props = defineProps({
  scheduleData: {
    type: Array,
    default: () => []
  },
  employeeData: {
    type: Array,
    default: () => []
  },
  shiftData: {
    type: Array,
    default: () => []
  }
});

// Emits
const emit = defineEmits(['schedule-change', 'conflict-resolve']);

// 状态
const dateRange = ref([]);
const employeeSearch = ref('');
const selectedEmployeeIds = ref([]);
const calendarViewMode = ref('week');
const calendarDays = ref([]);
const shiftPalette = ref([]);
const conflicts = ref([]);

const conflictModalVisible = ref(false);
const intScheduleModalVisible = ref(false);
const selectedConflict = ref(null);

// 计算属性
const totalShifts = computed(() => {
  return calendarDays.value.reduce((sum, day) => sum + day.shifts.length, 0);
});

const scheduledShifts = computed(() => {
  return calendarDays.value.reduce((sum, day) => {
    return sum + day.shifts.filter(s => s.employeeId).length;
  }, 0);
});

const conflictCount = computed(() => conflicts.value.length);

// 员工树数据
const employeeTreeData = computed(() => {
  return props.employeeData.map(emp => ({
    key: emp.employeeId,
    value: emp.employeeId,
    label: emp.employeeName,
    statistics: {
      total: 20, // 工作日数量
      scheduled: calendarDays.value.reduce((sum, day) => {
        return sum + day.shifts.filter(s => s.employeeId === emp.employeeId).length;
      }, 0)
    }
  }));
});

// 方法：生成日历数据
const generateCalendarDays = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return;
  }

  const [start, end] = dateRange.value;
  const days = [];
  const current = new Date(start);

  while (current <= new Date(end)) {
    const dateStr = current.toISOString().split('T')[0];
    const weekday = ['日', '一', '二', '三', '四', '五', '六'][current.getDay()];
    const isToday = dateStr === new Date().toISOString().split('T')[0];
    const isWeekend = current.getDay() === 0 || current.getDay() === 6;

    days.push({
      date: dateStr,
      weekday,
      isToday,
      isWeekend,
      shifts: []
    });

    current.setDate(current.getDate() + 1);
  }

  calendarDays.value = days;
};

// 方法：日期范围变化
const handleDateChange = (dates) => {
  console.log('[排班] 日期范围变化', dates);
  generateCalendarDays();
  // 加载该日期范围的排班数据
  loadScheduleData();
};

// 方法：拖拽班次到日期
const handleShiftDrop = (event, date) => {
  event.preventDefault();
  const shiftData = event.dataTransfer.getData('shiftData');

  if (!shiftData) {
    console.warn('[排班] 无效的拖拽数据');
    return;
  }

  const shift = JSON.parse(shiftData);
  const day = calendarDays.value.find(d => d.date === date);

  if (day) {
    // 检查冲突
    const conflict = checkShiftConflict(shift, date);
    if (conflict) {
      conflicts.value.push(conflict);
      message.warning(`检测到冲突: ${conflict.description}`);
    } else {
      day.shifts.push({
        ...shift,
        shiftId: `${shift.shiftId}_${Date.now()}`,
        date
      });
      message.success(`已添加班次: ${shift.shiftName}`);
      emit('schedule-change', calendarDays.value);
    }
  }
};

// 方法：从班次库拖拽
const handlePaletteDragStart = (event, shift) => {
  event.dataTransfer.setData('shiftData', JSON.stringify(shift));
};

// 方法：从日历拖拽
const handleShiftDragStart = (event, shift, from) => {
  event.dataTransfer.setData('shiftData', JSON.stringify({
    ...shift,
    from
  }));
};

// 方法：移除班次
const handleRemoveShift = (shift, date) => {
  const day = calendarDays.value.find(d => d.date === date);
  if (day) {
    const index = day.shifts.findIndex(s => s.shiftId === shift.shiftId);
    if (index > -1) {
      day.shifts.splice(index, 1);
      message.success(`已移除班次: ${shift.shiftName}`);
      emit('schedule-change', calendarDays.value);
    }
  }
};

// 方法：检查班次冲突
const checkShiftConflict = (shift, date) => {
  const day = calendarDays.value.find(d => d.date === date);
  if (!day) return null;

  // 时间重叠检测
  const overlapShift = day.shifts.find(s => {
    if (s.employeeId !== shift.employeeId) return false;
    // 检查时间是否重叠
    return !(shift.endTime <= s.startTime || shift.startTime >= s.endTime);
  });

  if (overlapShift) {
    return {
      type: '时间重叠',
      title: '班次时间冲突',
      description: `员工 ${shift.employeeName} 在 ${date} 已有班次 ${overlapShift.shiftName}`,
      date,
      shifts: [shift, overlapShift]
    };
  }

  // 最多工作时间检测
  const dayShifts = day.shifts.filter(s => s.employeeId === shift.employeeId);
  const totalHours = dayShifts.reduce((sum, s) => {
    const [sh, sm] = s.startTime.split(':');
    const [eh, em] = s.endTime.split(':');
    return sum + (parseInt(eh) * 60 + parseInt(em)) - (parseInt(sh) * 60 + parseInt(sm));
  }, 0);

  if (totalHours > 8 * 60) { // 超过8小时
    return {
      type: '超时工作',
      title: '工作时间过长',
      description: `员工 ${shift.employeeName} 在 ${date} 工作时间将达到 ${Math.round(totalHours / 60)} 小时`,
      date,
      shifts: [...dayShifts, shift]
    };
  }

  return null;
};

// 方法：检测所有冲突
const handleDetectConflicts = () => {
  conflicts.value = [];

  calendarDays.value.forEach(day => {
    day.shifts.forEach(shift => {
      const conflict = checkShiftConflict(shift, day.date);
      if (conflict && !conflicts.value.find(c => c.date === conflict.date && c.employeeId === shift.employeeId)) {
        conflicts.value.push(conflict);
      }
    });
  });

  if (conflicts.value.length === 0) {
    message.success('未检测到冲突');
  } else {
    message.warning(`检测到 ${conflicts.value.length} 个冲突`);
  }
};

// 方法：解决冲突
const handleResolveConflict = (conflict) => {
  selectedConflict.value = conflict;
  conflictModalVisible.value = true;
};

// 方法：冲突已解决
const handleConflictResolved = (resolution) => {
  console.log('[排班] 冲突已解决', resolution);
  conflictModalVisible.value = false;

  // 从冲突列表中移除
  const index = conflicts.value.findIndex(c => c.date === selectedConflict.value.date);
  if (index > -1) {
    conflicts.value.splice(index, 1);
  }

  emit('conflict-resolve', resolution);
  message.success('冲突已解决');
};

// 方法：智能排班
const handleAutoSchedule = () => {
  intScheduleModalVisible.value = true;
};

// 方法：确认智能排班
const handleIntScheduleConfirm = (scheduleResult) => {
  console.log('[排班] 智能排班结果', scheduleResult);

  // 应用排班结果
  scheduleResult.forEach(item => {
    const day = calendarDays.value.find(d => d.date === item.date);
    if (day) {
      day.shifts.push(item);
    }
  });

  emit('schedule-change', calendarDays.value);
  message.success('智能排班已完成');
};

// 方法：清空排班
const handleClearAll = () => {
  calendarDays.value.forEach(day => {
    day.shifts = [];
  });
  conflicts.value = [];
  emit('schedule-change', calendarDays.value);
  message.success('已清空所有排班');
};

// 方法：导出排班
const handleExport = () => {
  const data = calendarDays.value.map(day => ({
    date: day.date,
    shifts: day.shifts
  }));

  console.log('[排班] 导出数据', data);
  message.success('排班数据已导出（模拟）');
};

// 方法：加载排班数据
const loadScheduleData = () => {
  // TODO: 从后端 API 加载
  shiftPalette.value = props.shiftData;
};

// 方法：获取班次样式
const getShiftClass = (shift) => {
  return {
    'shift-morning': shift.shiftType === '早班',
    'shift-afternoon': shift.shiftType === '中班',
    'shift-night': shift.shiftType === '晚班'
  };
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

// 方法：获取冲突图标
const getConflictIcon = (type) => {
  const iconMap = {
    '时间重叠': '⚠',
    '超时工作': '⏰',
    '人员不足': '👥'
  };
  return iconMap[type] || '!';
};

// 监听排班数据变化
watch(() => props.scheduleData, (newData) => {
  if (newData && newData.length > 0) {
    // 后端数据映射到日历
    calendarDays.value.forEach(day => {
      day.shifts = newData.filter(item => item.date === day.date);
    });
  }
}, { immediate: true });

// 初始化
generateCalendarDays();
</script>

<style scoped>
.visual-schedule-container {
  padding: 16px;
}

.calendar-day {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s;
}

.calendar-day:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.calendar-day.is-today {
  background-color: #e6f7ff;
  border-color: #1890ff;
}

.calendar-day.is-weekend {
  background-color: #fafafa;
}

.day-header {
  display: flex;
  justify-content: space-between;
  padding: 8px;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
}

.day-content {
  min-height: 120px;
  padding: 8px;
}

.shift-item {
  position: relative;
  padding: 8px;
  margin-bottom: 8px;
  background-color: #f0f9ff;
  border: 1px solid #adc6ff;
  border-left: 4px solid #1890ff;
  border-radius: 4px;
  cursor: move;
  transition: all 0.3s;
}

.shift-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.shift-item.shift-morning {
  background-color: #f6ffed;
  border-color: #b7eb8f;
  border-left-color: #52c41a;
}

.shift-item.shift-afternoon {
  background-color: #fff7e6;
  border-color: #ffd591;
  border-left-color: #fa8c16;
}

.shift-item.shift-night {
  background-color: #f9f0ff;
  border-color: #d3adf7;
  border-left-color: #722ed1;
}

.shift-time {
  font-size: 12px;
  font-weight: 600;
  color: #262626;
}

.shift-name {
  font-size: 14px;
  color: #595959;
  margin-top: 2px;
}

.shift-employee {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}

.shift-delete {
  position: absolute;
  top: 4px;
  right: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.shift-item:hover .shift-delete {
  opacity: 1;
}

.empty-hint {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 12px;
  border: 2px dashed #d9d9d9;
  border-radius: 4px;
}

.shift-palette {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.palette-shift-item {
  padding: 8px 12px;
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: move;
  transition: all 0.3s;
}

.palette-shift-item:hover {
  border-color: #1890ff;
  background-color: #e6f7ff;
}

.palette-shift-item .shift-time {
  font-weight: 600;
  color: #262626;
}

.palette-shift-item .shift-name {
  color: #595959;
  margin-top: 2px;
}
</style>
