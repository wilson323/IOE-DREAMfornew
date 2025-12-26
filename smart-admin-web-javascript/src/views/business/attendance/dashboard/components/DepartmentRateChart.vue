<!--
  @fileoverview 部门出勤率图表组件
  @author IOE-DREAM Team
  @description 使用 ECharts 展示部门出勤率对比
  @example
  <DepartmentRateChart :data="departmentRate" :loading="rateLoading" />
-->
<template>
  <div class="department-rate-chart">
    <v-chart
      v-if="!loading && chartData.length > 0"
      class="chart"
      :option="chartOption"
      :autoresize="true"
    />
    <a-spin v-else-if="loading" :spinning="loading" class="chart-loading">
      <div class="chart-loading__content"></div>
    </a-spin>
    <a-empty v-else description="暂无数据" class="chart-empty" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { BarChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components';

// 注册 ECharts 组件
use([
  CanvasRenderer,
  BarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent
]);

/**
 * 部门出勤率数据
 */
interface DepartmentRateData {
  /** 部门ID */
  departmentId: number;
  /** 部门名称 */
  departmentName: string;
  /** 出勤率 */
  attendanceRate: number;
  /** 应出勤人数 */
  totalEmployees: number;
  /** 实际出勤人数 */
  actualEmployees: number;
}

/**
 * 组件属性
 */
interface Props {
  /** 图表数据 */
  data?: DepartmentRateData[];
  /** 加载状态 */
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  loading: false
});

/** 图表数据 */
const chartData = computed(() => {
  const data = props.data || [];
  // 按出勤率降序排序
  return data
    .slice()
    .sort((a, b) => b.attendanceRate - a.attendanceRate)
    .slice(0, 10); // 只显示前10名
});

/** 出勤率颜色 */
const getRateColor = (rate: number) => {
  if (rate >= 98) return '#52c41a';
  if (rate >= 95) return '#1890ff';
  if (rate >= 90) return '#faad14';
  return '#f5222d';
};

/** 图表配置 */
const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: (params: any) => {
      if (!params || params.length === 0) return '';

      const param = params[0];
      const data = chartData.value[param.dataIndex];
      const rate = data.attendanceRate;
      const color = getRateColor(rate);

      return `
        <div style="padding: 4px 0;">
          <div style="margin-bottom: 8px; font-weight: 600;">${data.departmentName}</div>
          <div style="display: flex; justify-content: space-between; margin: 4px 0;">
            <span>出勤率:</span>
            <span style="color: ${color}; font-weight: 600; margin-left: 24px;">${rate.toFixed(1)}%</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin: 4px 0;">
            <span>应出勤:</span>
            <span style="margin-left: 24px;">${data.totalEmployees}人</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin: 4px 0;">
            <span>实出勤:</span>
            <span style="margin-left: 24px;">${data.actualEmployees}人</span>
          </div>
        </div>
      `;
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    top: '5%',
    containLabel: true
  },
  dataZoom: [
    {
      type: 'inside',
      start: 0,
      end: 100
    },
    {
      start: 0,
      end: 100,
      height: 20,
      bottom: '5%',
      textStyle: {
        fontSize: 12
      }
    }
  ],
  xAxis: {
    type: 'category',
    data: chartData.value.map(item => item.departmentName),
    axisLabel: {
      interval: 0,
      rotate: chartData.value.length > 5 ? 30 : 0,
      formatter: (value: string) => {
        // 部门名称过长时截断
        return value.length > 6 ? value.substring(0, 6) + '...' : value;
      }
    }
  },
  yAxis: {
    type: 'value',
    name: '出勤率 (%)',
    min: 0,
    max: 100,
    axisLabel: {
      formatter: '{value}%'
    },
    splitLine: {
      lineStyle: {
        type: 'dashed'
      }
    }
  },
  series: [
    {
      name: '出勤率',
      type: 'bar',
      data: chartData.value.map(item => ({
        value: item.attendanceRate,
        itemStyle: {
          color: getRateColor(item.attendanceRate)
        }
      })),
      barWidth: '50%',
      itemStyle: {
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: (params: any) => {
          return params.value.toFixed(1) + '%';
        },
        fontSize: 12
      }
    }
  ]
}));
</script>

<style scoped lang="less">
.department-rate-chart {
  width: 100%;
  height: 100%;
  min-height: 320px;

  .chart {
    width: 100%;
    height: 100%;
  }

  &-loading,
  &-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 320px;
    width: 100%;
  }

  &-loading__content {
    width: 100%;
    height: 320px;
  }
}
</style>
