<!--
  智能视频-过热图页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="热力图分析" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card title="分析设置" :bordered="false" size="small">
            <a-form layout="vertical">
              <a-form-item label="选择设备">
                <a-select v-model:value="selectedDevice" placeholder="请选择设备">
                  <a-select-option value="device1">前门摄像头-001</a-select-option>
                  <a-select-option value="device2">停车场摄像头-01</a-select-option>
                  <a-select-option value="device3">大厅摄像头-002</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="时间范围">
                <a-range-picker v-model:value="timeRange" show-time style="width: 100%;" />
              </a-form-item>

              <a-form-item label="热力类型">
                <a-radio-group v-model:value="heatmapType">
                  <a-radio value="person">人员密度</a-radio>
                  <a-radio value="vehicle">车辆密度</a-radio>
                  <a-radio value="stay">停留时长</a-radio>
                </a-radio-group>
              </a-form-item>

              <a-form-item label="时间粒度">
                <a-select v-model:value="timeGranularity">
                  <a-select-option value="hour">按小时</a-select-option>
                  <a-select-option value="day">按天</a-select-option>
                  <a-select-option value="week">按周</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item>
                <a-button type="primary" block @click="handleGenerate">
                  生成热力图
                </a-button>
              </a-form-item>

              <a-form-item>
                <a-button block @click="handleExport">
                  <DownloadOutlined />
                  导出图片
                </a-button>
              </a-form-item>
            </a-form>
          </a-card>

          <a-card title="图例说明" :bordered="false" size="small" style="margin-top: 16px;">
            <div class="legend-item" v-for="item in legendData" :key="item.level">
              <div class="legend-color" :style="{ backgroundColor: item.color }"></div>
              <span>{{ item.label }}</span>
            </div>
          </a-card>
        </a-col>

        <a-col :span="18">
          <a-card title="热力图展示" :bordered="false">
            <template #extra>
              <a-space>
                <a-button @click="handleZoomIn">
                  <ZoomInOutlined />
                  放大
                </a-button>
                <a-button @click="handleZoomOut">
                  <ZoomOutOutlined />
                  缩小
                </a-button>
                <a-button @click="handleReset">
                  <SyncOutlined />
                  重置
                </a-button>
              </a-space>
            </template>

            <div class="heatmap-container">
              <div ref="heatmapRef" class="heatmap-canvas"></div>
              <a-empty v-if="!hasData" description="请选择设备和时间范围生成热力图" />
            </div>
          </a-card>

          <a-card title="热点区域统计" :bordered="false" size="small" style="margin-top: 16px;">
            <a-table
              :columns="hotspotColumns"
              :data-source="hotspotData"
              :pagination="false"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'level'">
                  <a-tag :color="getHotspotLevelColor(record.level)">{{ record.level }}</a-tag>
                </template>

                <template v-if="column.key === 'heatValue'">
                  <a-progress :percent="record.heatValue" size="small" />
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import {
  DownloadOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  SyncOutlined,
} from '@ant-design/icons-vue';

const selectedDevice = ref(undefined);
const timeRange = ref([]);
const heatmapType = ref('person');
const timeGranularity = ref('hour');
const hasData = ref(false);
const heatmapRef = ref(null);

const legendData = [
  { level: 5, label: '极高密度', color: '#d32f2f' },
  { level: 4, label: '高密度', color: '#f57c00' },
  { level: 3, label: '中密度', color: '#fbc02d' },
  { level: 2, label: '低密度', color: '#689f38' },
  { level: 1, label: '极低密度', color: '#1976d2' },
];

const hotspotColumns = [
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName' },
  { title: '热度等级', key: 'level' },
  { title: '热度值', key: 'heatValue' },
  { title: '平均停留时长', dataIndex: 'avgStayTime', key: 'avgStayTime' },
  { title: '峰值人数', dataIndex: 'peakCount', key: 'peakCount' },
];

const hotspotData = ref([
  {
    id: 1,
    areaName: '入口区域',
    level: '极高',
    heatValue: 95,
    avgStayTime: '3分15秒',
    peakCount: 156,
  },
  {
    id: 2,
    areaName: '等候区',
    level: '高',
    heatValue: 82,
    avgStayTime: '5分42秒',
    peakCount: 98,
  },
  {
    id: 3,
    areaName: '走廊',
    level: '中',
    heatValue: 65,
    avgStayTime: '1分20秒',
    peakCount: 45,
  },
]);

const getHotspotLevelColor = (level) => {
  const colorMap = {
    '极高': 'red',
    '高': 'orange',
    '中': 'blue',
    '低': 'green',
  };
  return colorMap[level] || 'default';
};

const handleGenerate = () => {
  console.log('生成热力图');
  hasData.value = true;
};

const handleExport = () => {
  console.log('导出热力图');
};

const handleZoomIn = () => {
  console.log('放大');
};

const handleZoomOut = () => {
  console.log('缩小');
};

const handleReset = () => {
  console.log('重置视图');
};

onMounted(() => {
  // 初始化热力图
});

onUnmounted(() => {
  // 清理资源
});
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  .legend-item {
    display: flex;
    align-items: center;
    margin-bottom: 8px;

    .legend-color {
      width: 20px;
      height: 20px;
      margin-right: 8px;
      border-radius: 2px;
    }
  }

  .heatmap-container {
    position: relative;
    min-height: 500px;
    background-color: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;

    .heatmap-canvas {
      width: 100%;
      height: 500px;
    }
  }
}
</style>
