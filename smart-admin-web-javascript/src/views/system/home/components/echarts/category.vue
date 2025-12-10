<!--
  * Category Chart Component
  * 企业级分类图表组件
-->
<template>
  <div class="category-chart">
    <v-chart class="chart" :option="chartOption" :style="{ height: '300px', width: '100%' }" />
  </div>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';

  const chartData = ref([]);

  const chartOption = computed(() => ({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: 'Category',
        type: 'pie',
        radius: '50%',
        data: chartData.value,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }));

  const loadChartData = () => {
    chartData.value = [
      { value: 335, name: 'Technology' },
      { value: 310, name: 'Business' },
      { value: 234, name: 'Marketing' },
      { value: 135, name: 'Development' },
      { value: 548, name: 'Support' }
    ];
  };

  onMounted(() => {
    loadChartData();
  });
</script>

<style lang="less" scoped>
  .category-chart {
    .chart {
      width: 100%;
    }
  }
</style>