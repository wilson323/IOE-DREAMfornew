<!--
  @fileoverview 打卡趋势图表组件
  @author IOE-DREAM Team
  @description 使用 ECharts 展示今日打卡趋势
  @example
  <PunchChart :data="trendData" :loading="trendLoading" />
-->
<template>
  <div class="punch-chart">
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
import { computed, ref, watch } from 'vue';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, BarChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components';

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
]);

/**
 * 打卡趋势数据点
 */
interface TrendDataPoint {
  /** 时间点 */
  time: string;
  /** 打卡人数 */
  count: number;
  /** 签到人数 */
  checkInCount: number;
  /** 签退人数 */
  checkOutCount: number;
}

/**
 * 组件属性
 */
interface Props {
  /** 图表数据 */
  data?: TrendDataPoint[];
  /** 加载状态 */
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  loading: false
});

/** 图表数据 */
const chartData = computed(() => props.data || []);

/** 图表配置 */
const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      label: {
        backgroundColor: '#6a7985'
      }
    },
    formatter: (params: any) => {
      if (!params || params.length === 0) return '';

      const time = params[0].axisValue;
      let tooltip = `<div style="margin-bottom: 4px; font-weight: 600;">${time}</div>`;

      params.forEach((param: any) => {
        tooltip += `
          <div style="display: flex; justify-content: space-between; align-items: center; margin: 4px 0;">
            <span style="display: inline-block; width: 10px; height: 10px; background: ${param.color}; border-radius: 50%; margin-right: 8px;"></span>
            <span style="flex: 1;">${param.seriesName}:</span>
            <span style="font-weight: 600; margin-left: 16px;">${param.value}</span>
          </div>
        `;
      });

      return tooltip;
    }
  },
  legend: {
    data: ['签到', '签退', '总计'],
    top: 0,
    right: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: chartData.value.map(item => item.time),
    axisLabel: {
      formatter: (value: string) => {
        // 只显示时:分
        return value.length > 5 ? value.substring(0, 5) : value;
      }
    }
  },
  yAxis: {
    type: 'value',
    name: '人数',
    axisLabel: {
      formatter: '{value}'
    }
  },
  series: [
    {
      name: '签到',
      type: 'line',
      smooth: true,
      data: chartData.value.map(item => item.checkInCount),
      itemStyle: {
        color: '#52c41a'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0)' }
          ]
        }
      }
    },
    {
      name: '签退',
      type: 'line',
      smooth: true,
      data: chartData.value.map(item => item.checkOutCount),
      itemStyle: {
        color: '#1890ff'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0)' }
          ]
        }
      }
    },
    {
      name: '总计',
      type: 'bar',
      data: chartData.value.map(item => item.count),
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#faad14' },
            { offset: 1, color: '#ffd666' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '20%'
    }
  ]
}));
</script>

<style scoped lang="less">
.punch-chart {
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
