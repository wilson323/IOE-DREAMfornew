<template>
  <div class="trajectory-container">
    <!-- 时间轴控制 -->
    <div class="timeline-control">
      <a-space>
        <a-button type="primary" @click="startPlayback" :disabled="isPlaying">
          <template #icon><PlayCircleOutlined /></template>
          播放
        </a-button>
        <a-button @click="pausePlayback" :disabled="!isPlaying">
          <template #icon><PauseCircleOutlined /></template>
          暂停
        </a-button>
        <a-button @click="resetPlayback">
          <template #icon><ReloadOutlined /></template>
          重置
        </a-button>

        <a-slider
          v-model:value="currentTimeIndex"
          :min="0"
          :max="trajectoryPoints.length - 1"
          :step="1"
          style="width: 300px"
          @change="handleTimeChange"
        />

        <span class="time-display">
          {{ currentTimeDisplay }}
        </span>
      </a-space>
    </div>

    <!-- 百度地图 -->
    <baidu-map
      class="map"
      :center="mapCenter"
      :zoom="mapZoom"
      :scroll-wheel-zoom="true"
    >
      <!-- 缩放控件 -->
      <bm-navigation anchor="BMAP_ANCHOR_TOP_RIGHT" />

      <!-- 轨迹线 -->
      <bm-polyline
        :path="displayedTrajectory"
        stroke-color="blue"
        :stroke-opacity="0.6"
        :stroke-weight="4"
      />

      <!-- 当前位置标记 -->
      <bm-marker
        v-if="currentPosition"
        :position="currentPosition"
        :title="`当前位置: ${currentDeviceName}`"
      >
        <bm-label
          :content="`当前: ${currentDeviceName}`"
          :label-style="{
            color: '#ffffff',
            fontSize: '14px',
            backgroundColor: 'rgba(0,0,0,0.8)',
            padding: '5px 10px',
            borderRadius: '4px',
            border: '2px solid #1890ff'
          }"
        />
      </bm-marker>

      <!-- 历史轨迹点（淡化显示） -->
      <bm-marker
        v-for="(point, index) in trajectoryPoints.slice(0, currentTimeIndex)"
        :key="index"
        :position="{ lng: point.longitude, lat: point.latitude }"
        :icon="{
          url: 'https://api.map.baidu.com/images/marker_red_sprite.png',
          size: { width: 20, height: 30 }
        }"
      />
    </baidu-map>

    <!-- 轨迹统计 -->
    <div class="trajectory-stats">
      <a-statistic-group>
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic title="总轨迹点" :value="trajectoryPoints.length" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="通行次数" :value="accessCount" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="涉及区域" :value="areaCount" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="移动距离" :value="totalDistance" suffix="米" />
          </a-col>
        </a-row>
      </a-statistic-group>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue';

// Props
const props = defineProps({
  trajectoryPoints: {
    type: Array,
    default: () => []
  }
});

// 状态
const isPlaying = ref(false);
const currentTimeIndex = ref(0);
const playbackTimer = ref(null);

// 计算属性
const mapCenter = computed(() => {
  if (props.trajectoryPoints.length === 0) {
    return { lng: 116.404, lat: 39.915 };
  }
  // 使用第一个点作为中心
  const firstPoint = props.trajectoryPoints[0];
  return { lng: firstPoint.longitude, lat: firstPoint.latitude };
});

const mapZoom = ref(16);

const currentPosition = computed(() => {
  if (currentTimeIndex.value >= props.trajectoryPoints.length) {
    return null;
  }
  const point = props.trajectoryPoints[currentTimeIndex.value];
  return { lng: point.longitude, lat: point.latitude };
});

const currentDeviceName = computed(() => {
  if (currentTimeIndex.value >= props.trajectoryPoints.length) {
    return '';
  }
  return props.trajectoryPoints[currentTimeIndex.value].deviceName;
});

const displayedTrajectory = computed(() => {
  return props.trajectoryPoints.slice(0, currentTimeIndex.value + 1).map(point => ({
    lng: point.longitude,
    lat: point.latitude
  }));
});

const currentTimeDisplay = computed(() => {
  if (currentTimeIndex.value >= props.trajectoryPoints.length) {
    return '--:--:--';
  }
  return props.trajectoryPoints[currentTimeIndex.value].time;
});

const accessCount = computed(() => {
  return props.trajectoryPoints.length;
});

const areaCount = computed(() => {
  const areas = new Set(props.trajectoryPoints.map(p => p.areaName));
  return areas.size;
});

const totalDistance = computed(() => {
  let distance = 0;
  for (let i = 1; i < props.trajectoryPoints.length; i++) {
    const prev = props.trajectoryPoints[i - 1];
    const curr = props.trajectoryPoints[i];
    distance += calculateDistance(
      prev.latitude, prev.longitude,
      curr.latitude, curr.longitude
    );
  }
  return Math.round(distance);
});

// 方法：计算两点间距离（米）
const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const R = 6371000; // 地球半径（米）
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

// 方法：开始播放
const startPlayback = () => {
  isPlaying.value = true;
  playbackTimer.value = setInterval(() => {
    if (currentTimeIndex.value < props.trajectoryPoints.length - 1) {
      currentTimeIndex.value++;
    } else {
      pausePlayback();
    }
  }, 1000); // 每秒前进一个点
};

// 方法：暂停播放
const pausePlayback = () => {
  isPlaying.value = false;
  if (playbackTimer.value) {
    clearInterval(playbackTimer.value);
    playbackTimer.value = null;
  }
};

// 方法：重置播放
const resetPlayback = () => {
  pausePlayback();
  currentTimeIndex.value = 0;
};

// 方法：时间轴拖动
const handleTimeChange = (value) => {
  currentTimeIndex.value = value;
};

// 监听轨迹数据变化
watch(() => props.trajectoryPoints, (newPoints) => {
  if (newPoints.length > 0) {
    currentTimeIndex.value = 0;
  }
}, { immediate: true });
</script>

<style scoped>
.trajectory-container {
  width: 100%;
  height: 600px;
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
}

.timeline-control {
  position: absolute;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;
  background: rgba(255, 255, 255, 0.95);
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.time-display {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  margin-left: 16px;
}

.trajectory-stats {
  position: absolute;
  bottom: 16px;
  left: 16px;
  right: 16px;
  z-index: 999;
  background: rgba(255, 255, 255, 0.95);
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
</style>
