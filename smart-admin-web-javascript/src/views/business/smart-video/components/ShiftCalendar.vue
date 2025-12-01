<!--
  排班日历组件

  @Author:    Claude Code
  @Date:      2025-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="shift-calendar">
    <!-- 日历控制栏 -->
    <div class="calendar-controls">
      <div class="calendar-header">
        <div class="calendar-nav">
          <a-button @click="handlePreviousMonth">
            <template #icon><LeftOutlined /></template>
          </a-button>
          <h3 class="current-month">{{ currentMonthText }}</h3>
          <a-button @click="handleNextMonth">
            <template #icon><RightOutlined /></template>
          </a-button>
          <a-button @click="handleToday">今天</a-button>
        </div>
        <div class="calendar-actions">
          <a-select
            v-model:value="selectedDepartment"
            placeholder="选择部门"
            style="width: 120px; margin-right: 8px"
            @change="handleDepartmentChange"
          >
            <a-select-option value="">全部部门</a-select-option>
            <a-select-option value="tech">技术研发部</a-select-option>
            <a-select-option value="market">市场部</a-select-option>
            <a-select-option value="sales">销售部</a-select-option>
          </a-select>
          <a-button @click="handleExport">
            <template #icon><DownloadOutlined /></template>
            导出排班
          </a-button>
        </div>
      </div>

      <!-- 星期标题 -->
      <div class="calendar-weekdays">
        <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
      </div>

      <!-- 日历网格 -->
      <div class="calendar-grid">
        <div
          v-for="cell in calendarCells"
          :key="cell.date"
          class="calendar-cell"
          :class="{
            'other-month': !cell.isCurrentMonth,
            today: cell.isToday,
            weekend: cell.isWeekend,
          }"
          @click="handleCellClick(cell)"
        >
          <div class="cell-header">
            <span class="cell-date">{{ cell.day }}</span>
            <span v-if="cell.exceptionCount > 0" class="exception-dot"></span>
          </div>
          <div class="cell-content">
            <div
              v-for="shift in cell.shifts.slice(0, 2)"
              :key="shift.type"
              class="shift-item"
              :class="`shift-${shift.typeClass}`"
            >
              {{ shift.type }} ({{ shift.employeeCount }}人)
            </div>
            <div v-if="cell.shifts.length > 2" class="more-shifts">
              +{{ cell.shifts.length - 2 }} 更多
            </div>
          </div>
        </div>
      </div>

      <!-- 图例 -->
      <div class="calendar-legend">
        <div class="legend-section">
          <span class="legend-label">班次图例:</span>
          <div class="legend-items">
            <span class="legend-item shift-early">早班</span>
            <span class="legend-item shift-regular">标准班</span>
            <span class="legend-item shift-late">中班</span>
            <span class="legend-item shift-night">夜班</span>
            <span class="legend-item shift-rest">休息</span>
          </div>
        </div>
        <div class="legend-section">
          <span class="legend-label">异常标识:</span>
          <div class="legend-items">
            <span class="legend-item exception-leave">请假</span>
            <span class="legend-item exception-overtime">加班</span>
            <span class="legend-item exception-late">迟到</span>
            <span class="legend-item exception-early-leave">早退</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 排班记录表格 -->
    <div class="schedule-table">
      <div class="table-header">
        <h4>排班记录</h4>
        <div class="table-actions">
          <a-date-picker v-model:value="dateRange" range-picker @change="handleDateRangeChange" />
          <a-button type="primary" @click="handleSearch">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
        </div>
      </div>
      <a-table
        :columns="columns"
        :data-source="scheduleRecords"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'shiftType'">
            <a-tag :color="getShiftColor(record.shiftType)">{{ record.shiftType }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">{{ record.status }}</a-tag>
          </template>
          <template v-else-if="column.key === 'exception'">
            <span v-if="record.exception">
              <a-tag color="orange">{{ record.exception }}</a-tag>
            </span>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="handleView(record)">查看</a-button>
              <a-button size="small" @click="handleEdit(record)">编辑</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { message } from 'ant-design-vue';
import {
  LeftOutlined,
  RightOutlined,
  DownloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';

const emit = defineEmits(['view-detail']);

// 响应式数据
const currentDate = ref(dayjs());
const selectedDepartment = ref('');
const loading = ref(false);
const dateRange = ref([]);

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
const scheduleRecords = ref([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 计算属性
const currentMonthText = computed(() => {
  return currentDate.value.format('YYYY年MM月');
});

// 表格列定义
const columns = [
  {
    title: '日期',
    dataIndex: 'date',
    key: 'date',
  },
  {
    title: '员工姓名',
    dataIndex: 'employeeName',
    key: 'employeeName',
  },
  {
    title: '部门',
    dataIndex: 'department',
    key: 'department',
  },
  {
    title: '班次类型',
    dataIndex: 'shiftType',
    key: 'shiftType',
  },
  {
    title: '工作时长',
    dataIndex: 'workHours',
    key: 'workHours',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '异常情况',
    dataIndex: 'exception',
    key: 'exception',
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
  },
];

// 日历单元格数据
const calendarCells = computed(() => {
  const cells = [];
  const year = currentDate.value.year();
  const month = currentDate.value.month();
  const firstDay = dayjs(new Date(year, month, 1));
  const lastDay = firstDay.endOf('month');
  const prevLastDay = lastDay.subtract(1, 'month');
  const startDate = firstDay.startOf('week');

  // 生成42个单元格（6周）
  for (let i = 0; i < 42; i++) {
    const date = startDate.add(i, 'day');
    const dateStr = date.format('YYYY-MM-DD');
    const isCurrentMonth = date.month() === month;
    const isToday = date.isSame(dayjs(), 'day');
    const isWeekend = date.day() === 0 || date.day() === 6;

    // 获取排班数据
    const shifts = getShiftsForDate(dateStr);

    cells.push({
      date: dateStr,
      day: date.date(),
      isCurrentMonth,
      isToday,
      isWeekend,
      shifts,
      exceptionCount: getExceptionCount(dateStr),
    });
  }

  return cells;
});

// 方法
const getShiftsForDate = (dateStr) => {
  // Mock 数据
  const mockShifts = {
    '2025-11-03': [
      { type: '早班', typeClass: 'early', employeeCount: 4 },
    ],
    '2025-11-05': [
      { type: '标准班', typeClass: 'regular', employeeCount: 3 },
      { type: '中班', typeClass: 'late', employeeCount: 2 },
    ],
    '2025-11-07': [
      { type: '夜班', typeClass: 'night', employeeCount: 2 },
      { type: '早班', typeClass: 'early', employeeCount: 5 },
    ],
  };

  return mockShifts[dateStr] || [];
};

const getExceptionCount = (dateStr) => {
  const exceptions = {
    '2025-11-03': 1,
    '2025-11-05': 2,
    '2025-11-07': 1,
  };
  return exceptions[dateStr] || 0;
};

const getShiftColor = (shiftType) => {
  const colors = {
    '早班': 'blue',
    '标准班': 'green',
    '中班': 'orange',
    '夜班': 'purple',
    '休息': 'default',
  };
  return colors[shiftType] || 'default';
};

const getStatusColor = (status) => {
  const colors = {
    '正常': 'green',
    '异常': 'orange',
    '请假': 'red',
  };
  return colors[status] || 'default';
};

// 事件处理
const handlePreviousMonth = () => {
  currentDate.value = currentDate.value.subtract(1, 'month');
};

const handleNextMonth = () => {
  currentDate.value = currentDate.value.add(1, 'month');
};

const handleToday = () => {
  currentDate.value = dayjs();
};

const handleDepartmentChange = () => {
  loadScheduleRecords();
};

const handleExport = () => {
  message.info('导出功能开发中...');
};

const handleCellClick = (cell) => {
  emit('view-detail', cell);
};

const handleDateRangeChange = () => {
  loadScheduleRecords();
};

const handleSearch = () => {
  loadScheduleRecords();
};

const handleTableChange = (pag) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadScheduleRecords();
};

const handleView = (record) => {
  emit('view-detail', record);
};

const handleEdit = (record) => {
  message.info('编辑功能开发中...');
};

// 加载数据
const loadScheduleRecords = async () => {
  loading.value = true;
  try {
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 300));

    // Mock 数据
    scheduleRecords.value = [
      {
        id: 1,
        date: '2025-11-15',
        employeeName: '张三',
        department: '技术研发部',
        shiftType: '标准班',
        workHours: '8小时',
        status: '正常',
        exception: '',
      },
      {
        id: 2,
        date: '2025-11-15',
        employeeName: '李四',
        department: '技术研发部',
        shiftType: '早班',
        workHours: '6小时',
        status: '异常',
        exception: '迟到15分钟',
      },
      {
        id: 3,
        date: '2025-11-16',
        employeeName: '王五',
        department: '市场部',
        shiftType: '休息',
        workHours: '-',
        status: '正常',
        exception: '',
      },
    ];
    pagination.value.total = scheduleRecords.value.length;
  } catch (error) {
    console.error('加载失败:', error);
    message.error('加载排班记录失败');
  } finally {
    loading.value = false;
  }
};

// 公共方法
const refresh = () => {
  loadScheduleRecords();
};

// 初始化
onMounted(() => {
  loadScheduleRecords();
});

// 监听日期变化
watch(currentDate, () => {
  loadScheduleRecords();
});

defineExpose({
  refresh,
});
</script>

<style lang="less" scoped>
.shift-calendar {
  .calendar-controls {
    margin-bottom: 24px;
  }

  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .calendar-nav {
      display: flex;
      align-items: center;
      gap: 12px;

      .current-month {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .calendar-actions {
      display: flex;
      align-items: center;
    }
  }

  .calendar-weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 1px;
    background: #f0f0f0;
    border: 1px solid #f0f0f0;
    margin-bottom: 1px;

    .weekday {
      background: #fafafa;
      padding: 8px;
      text-align: center;
      font-weight: 600;
    }
  }

  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 1px;
    background: #f0f0f0;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
    overflow: hidden;

    .calendar-cell {
      background: white;
      min-height: 100px;
      padding: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #f5f5f5;
      }

      &.other-month {
        background: #fafafa;
        color: #999;
      }

      &.today {
        background: #fff7e6;
      }

      &.weekend {
        background: #f9f9f9;
      }

      .cell-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .cell-date {
          font-weight: 600;
        }

        .exception-dot {
          width: 6px;
          height: 6px;
          background: #faad14;
          border-radius: 50%;
        }
      }

      .cell-content {
        .shift-item {
          font-size: 12px;
          padding: 2px 4px;
          border-radius: 2px;
          margin-bottom: 2px;

          &.shift-early {
            background: #fff7e6;
            color: #ad6800;
          }

          &.shift-regular {
            background: #e6f7ff;
            color: #0050b3;
          }

          &.shift-late {
            background: #f9f0ff;
            color: #531dab;
          }

          &.shift-night {
            background: #262626;
            color: #fff;
          }

          &.shift-rest {
            background: #f6ffed;
            color: #389e0d;
          }
        }

        .more-shifts {
          font-size: 12px;
          color: #999;
          text-align: center;
        }
      }
    }
  }

  .calendar-legend {
    display: flex;
    gap: 24px;
    margin-top: 16px;
    padding: 12px;
    background: #fafafa;
    border-radius: 4px;

    .legend-section {
      display: flex;
      align-items: center;

      .legend-label {
        font-weight: 600;
        margin-right: 8px;
      }

      .legend-items {
        display: flex;
        gap: 8px;

        .legend-item {
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;

          &.shift-early {
            background: #fff7e6;
            color: #ad6800;
          }

          &.shift-regular {
            background: #e6f7ff;
            color: #0050b3;
          }

          &.shift-late {
            background: #f9f0ff;
            color: #531dab;
          }

          &.shift-night {
            background: #262626;
            color: #fff;
          }

          &.shift-rest {
            background: #f6ffed;
            color: #389e0d;
          }

          &.exception-leave {
            background: #fff1f0;
            color: #cf1322;
          }

          &.exception-overtime {
            background: #e6f7ff;
            color: #0050b3;
          }

          &.exception-late {
            background: #f9f0ff;
            color: #531dab;
          }

          &.exception-early-leave {
            background: #fff1f0;
            color: #cf1322;
          }
        }
      }
    }
  }

  .schedule-table {
    margin-top: 24px;

    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h4 {
        margin: 0;
      }

      .table-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
}
</style>
