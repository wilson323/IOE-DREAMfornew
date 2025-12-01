<!--
  排班详情组件

  @Author:    Claude Code
  @Date:      2025-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="shift-detail">
    <template v-if="data">
      <!-- 排班安排 -->
      <div class="section">
        <h4 class="section-title">排班安排</h4>
        <div v-if="data.shifts && data.shifts.length > 0" class="shifts-list">
          <div v-for="shift in data.shifts" :key="shift.type" class="shift-item">
            <div class="shift-header">
              <a-tag :color="getShiftColor(shift.type)">{{ shift.type }}</a-tag>
              <span class="employee-count">{{ shift.employeeCount }}人</span>
            </div>
            <div class="employee-tags">
              <a-tag v-for="emp in shift.employees || []" :key="emp" class="employee-tag">
                {{ emp }}
              </a-tag>
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无排班安排" />
      </div>

      <!-- 异常申请 -->
      <div v-if="data.exceptions && data.exceptions.length > 0" class="section">
        <h4 class="section-title">异常申请</h4>
        <div class="exceptions-list">
          <div v-for="exception in data.exceptions" :key="exception.employee" class="exception-item">
            <div class="exception-header">
              <a-tag color="orange">{{ exception.type }}</a-tag>
              <span class="employee-name">{{ exception.employee }}</span>
            </div>
            <div class="exception-info">
              <p class="exception-reason">{{ exception.reason }}</p>
              <a-tag :color="getStatusColor(exception.status)">
                {{ exception.status }}
              </a-tag>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <a-empty description="暂无数据" />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import dayjs from 'dayjs';

const props = defineProps({
  data: {
    type: Object,
    default: () => ({}),
  },
});

const formattedDate = computed(() => {
  if (!props.data?.date) return '';
  return dayjs(props.data.date).format('YYYY-MM-DD');
});

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
    '待审批': 'orange',
    '已批准': 'green',
    '已确认': 'blue',
    '待处理': 'gold',
  };
  return colors[status] || 'default';
};
</script>

<style lang="less" scoped>
.shift-detail {
  .section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      margin: 0 0 12px;
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .shifts-list,
    .exceptions-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .shift-item {
      padding: 12px;
      background: #fafafa;
      border-radius: 8px;

      .shift-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .employee-count {
          font-size: 14px;
          color: #666;
        }
      }

      .employee-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .employee-tag {
          margin: 0;
        }
      }
    }

    .exception-item {
      padding: 12px;
      background: #fff7e6;
      border-radius: 8px;

      .exception-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .employee-name {
          font-weight: 600;
          color: #333;
        }
      }

      .exception-info {
        .exception-reason {
          margin: 0 0 8px;
          font-size: 14px;
          color: #666;
        }
      }
    }
  }
}
</style>
