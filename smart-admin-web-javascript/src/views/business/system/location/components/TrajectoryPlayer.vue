<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="轨迹回放"
    width="1200px"
    :footer="null"
    @cancel="handleCancel"
  >
    <div class="trajectory-player" v-if="trajectoryData && trajectoryData.length > 0">
      <!-- 控制面板 -->
      <div class="control-panel">
        <a-row :gutter="16" align="middle">
          <a-col :span="8">
            <div class="time-range">
              <span class="label">时间范围：</span>
              <span class="time">
                {{ formatTime(startTime) }} - {{ formatTime(endTime) }}
              </span>
            </div>
          </a-col>
          <a-col :span="10">
            <a-slider
              :value="currentTime" @update:value="val => emit('update:value', val)"
              :min="0"
              :max="duration"
              :step="step"
              @change="handleTimeChange"
              :tooltip-formatter="formatSliderTime"
            />
          </a-col>
          <a-col :span="6">
            <div class="current-time">
              {{ formatTime(currentTime) }}
            </div>
          </a-col>
        </a-row>

        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="12">
            <a-space>
              <a-button
                type="primary"
                :icon="playing ? h(PauseOutlined) : h(CaretRightOutlined)"
                @click="togglePlay"
                :loading="loading"
              >
                {{ playing ? '暂停' : '播放' }}
              </a-button>
              <a-button @click="stopPlayback" :disabled="!playing && currentTime === 0">
                <template #icon><StopOutlined /></template>
                停止
              </a-button>
              <a-select
                :value="playbackSpeed" @update:value="val => emit('update:value', val)"
                style="width: 100px"
                @change="handleSpeedChange"
              >
                <a-select-option :value="0.5">0.5x</a-select-option>
                <a-select-option :value="1">1x</a-select-option>
                <a-select-option :value="2">2x</a-select-option>
                <a-select-option :value="4">4x</a-select-option>
                <a-select-option :value="8">8x</a-select-option>
              </a-select>
            </a-space>
          </a-col>
          <a-col :span="12">
            <a-space style="float: right">
              <a-checkbox :checked="showTrail" @update:checked="val => emit('update:checked', val)" @change="handleTrailToggle">
                显示轨迹
              </a-checkbox>
              <a-checkbox :checked="showInfoWindow" @update:checked="val => emit('update:checked', val)" @change="handleInfoWindowToggle">
                显示信息窗口
              </a-checkbox>
              <a-button @click="resetView">
                <template #icon><AimOutlined /></template>
                重置视图
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 轨迹地图 -->
      <div class="trajectory-map">
        <div id="trajectory-map" class="map"></div>
      </div>

      <!-- 轨迹信息面板 -->
      <div class="trajectory-info" v-if="currentPoint">
        <a-descriptions title="当前位置信息" :column="3" size="small" bordered>
          <a-descriptions-item label="时间">
            {{ formatDateTime(currentPoint.positioningTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="坐标">
            {{ currentPoint.latitude?.toFixed(6) }}, {{ currentPoint.longitude?.toFixed(6) }}
          </a-descriptions-item>
          <a-descriptions-item label="精度">
            {{ currentPoint.accuracy }}米
          </a-descriptions-item>
          <a-descriptions-item label="定位方式">
            <a-tag>{{ getPositioningMethodLabel(currentPoint.positioningMethod) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="速度">
            {{ currentPoint.speed ? (currentPoint.speed * 3.6).toFixed(2) + 'km/h' : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="地址">
            {{ currentPoint.address || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 统计信息 -->
      <div class="trajectory-stats">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic title="总距离" :value="statistics.totalDistance" suffix="公里" :precision="2" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="平均速度" :value="statistics.avgSpeed" suffix="km/h" :precision="2" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="最高速度" :value="statistics.maxSpeed" suffix="km/h" :precision="2" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="停留点" :value="statistics.stopsCount" suffix="个" />
          </a-col>
        </a-row>
      </div>
    </div>

    <a-empty v-else description="无轨迹数据" />
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick, h } from 'vue';
import { message } from 'ant-design-vue';
import {
  CaretRightOutlined,
  PauseOutlined,
  StopOutlined,
  AimOutlined
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { initMap, addMarker, addPolyline, clearMap } from '/@/utils/MapUtils';

dayjs.extend(duration);

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  trajectoryData: {
    type: Array,
    default: () => []
  },
  mapInstance: {
    type: Object,
    default: null
  }
});

// Emits
const emit = defineEmits(['update:visible']);

// 响应式数据
const loading = ref(false);
const playing = ref(false);
const currentTime = ref(0);
const playbackSpeed = ref(1);
const showTrail = ref(true);
const showInfoWindow = ref(true);
const currentPoint = ref(null);
const currentMarker = ref(null);
const trajectoryPolyline = ref(null);
const playbackTimer = ref(null);
const trajectoryMap = ref(null);

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

const processedTrajectoryData = computed(() => {
  if (!props.trajectoryData || props.trajectoryData.length === 0) return [];

  // 按时间排序
  return props.trajectoryData
    .filter(point => point.positioningTime && point.latitude && point.longitude)
    .sort((a, b) => new Date(a.positioningTime) - new Date(b.positioningTime));
});

const startTime = computed(() => {
  if (processedTrajectoryData.value.length === 0) return 0;
  return new Date(processedTrajectoryData.value[0].positioningTime).getTime();
});

const endTime = computed(() => {
  if (processedTrajectoryData.value.length === 0) return 0;
  return new Date(processedTrajectoryData.value[processedTrajectoryData.value.length - 1].positioningTime).getTime();
});

const trajectoryDuration = computed(() => {
  return endTime.value - startTime.value;
});

const step = computed(() => {
  return trajectoryDuration.value / 1000; // 将轨迹分为1000个步骤
});

const statistics = computed(() => {
  if (processedTrajectoryData.value.length < 2) {
    return {
      totalDistance: 0,
      avgSpeed: 0,
      maxSpeed: 0,
      stopsCount: 0
    };
  }

  let totalDistance = 0;
  let maxSpeed = 0;
  let stopsCount = 0;
  const speedThreshold = 1; // 1km/h 以下认为是停留

  for (let i = 1; i < processedTrajectoryData.value.length; i++) {
    const prev = processedTrajectoryData.value[i - 1];
    const curr = processedTrajectoryData.value[i];

    // 计算距离
    const distance = calculateDistance(prev, curr);
    totalDistance += distance;

    // 计算速度
    const timeDiff = new Date(curr.positioningTime) - new Date(prev.positioningTime);
    if (timeDiff > 0) {
      const speed = (distance / timeDiff) * 3600; // km/h
      maxSpeed = Math.max(maxSpeed, speed);

      if (speed < speedThreshold) {
        stopsCount++;
      }
    }
  }

  const avgSpeed = totalDistance > 0 ? (totalDistance / (duration.value / 3600000)) : 0;

  return {
    totalDistance: totalDistance / 1000, // 转换为公里
    avgSpeed,
    maxSpeed,
    stopsCount
  };
});

// 方法
const initTrajectoryMap = async () => {
  if (!processedTrajectoryData.value.length) return;

  await nextTick();

  try {
    // 创建轨迹专用地图
    trajectoryMap.value = initMap('trajectory-map', {
      center: [
        processedTrajectoryData.value[0].latitude,
        processedTrajectoryData.value[0].longitude
      ],
      zoom: 14
    });

    // 绘制完整轨迹
    drawTrajectory();

    // 添加开始和结束标记
    addTrajectoryMarkers();

    message.success('轨迹地图初始化成功');
  } catch (error) {
    console.error('初始化轨迹地图失败:', error);
    message.error('轨迹地图初始化失败');
  }
};

const drawTrajectory = () => {
  if (!trajectoryMap.value || !showTrail.value) return;

  const positions = processedTrajectoryData.value.map(point => [
    point.latitude,
    point.longitude
  ]);

  trajectoryPolyline.value = addPolyline(trajectoryMap.value, positions, {
    color: '#1890ff',
    weight: 4,
    opacity: 0.8
  });
};

const addTrajectoryMarkers = () => {
  if (!trajectoryMap.value) return;

  const startPoint = processedTrajectoryData.value[0];
  const endPoint = processedTrajectoryData.value[processedTrajectoryData.value.length - 1];

  // 开始点标记
  addMarker(trajectoryMap.value, [startPoint.latitude, startPoint.longitude], {
    title: '起点',
    icon: 'start-point',
    popup: `
      <div>
        <h4>轨迹起点</h4>
        <p>时间: ${formatDateTime(startPoint.positioningTime)}</p>
        <p>坐标: ${startPoint.latitude}, ${startPoint.longitude}</p>
      </div>
    `
  });

  // 结束点标记
  addMarker(trajectoryMap.value, [endPoint.latitude, endPoint.longitude], {
    title: '终点',
    icon: 'end-point',
    popup: `
      <div>
        <h4>轨迹终点</h4>
        <p>时间: ${formatDateTime(endPoint.positioningTime)}</p>
        <p>坐标: ${endPoint.latitude}, ${endPoint.longitude}</p>
      </div>
    `
  });
};

const togglePlay = () => {
  if (playing.value) {
    pausePlayback();
  } else {
    startPlayback();
  }
};

const startPlayback = () => {
  if (currentTime.value >= duration.value) {
    currentTime.value = 0;
  }

  playing.value = true;
  playbackTimer.value = setInterval(() => {
    currentTime.value += playbackSpeed.value * 100; // 每100ms更新一次

    if (currentTime.value >= duration.value) {
      currentTime.value = duration.value;
      pausePlayback();
    }

    updateCurrentPosition();
  }, 100);
};

const pausePlayback = () => {
  playing.value = false;
  if (playbackTimer.value) {
    clearInterval(playbackTimer.value);
    playbackTimer.value = null;
  }
};

const stopPlayback = () => {
  pausePlayback();
  currentTime.value = 0;
  updateCurrentPosition();
};

const handleTimeChange = (value) => {
  currentTime.value = value;
  updateCurrentPosition();
};

const handleSpeedChange = (speed) => {
  if (playing.value) {
    pausePlayback();
    playbackSpeed.value = speed;
    startPlayback();
  } else {
    playbackSpeed.value = speed;
  }
};

const handleTrailToggle = (checked) => {
  if (trajectoryPolyline.value) {
    trajectoryPolyline.value.setOpacity(checked ? 0.8 : 0);
  }
};

const handleInfoWindowToggle = (checked) => {
  if (currentMarker.value) {
    if (checked) {
      currentMarker.value.openPopup();
    } else {
      currentMarker.value.closePopup();
    }
  }
};

const updateCurrentPosition = () => {
  if (!processedTrajectoryData.value.length) return;

  const targetTime = startTime.value + currentTime.value;

  // 找到当前时间点的位置
  let currentIdx = 0;
  for (let i = 0; i < processedTrajectoryData.value.length; i++) {
    if (new Date(processedTrajectoryData.value[i].positioningTime).getTime() >= targetTime) {
      currentIdx = i;
      break;
    }
    currentIdx = i;
  }

  currentPoint.value = processedTrajectoryData.value[currentIdx];

  // 更新地图上的位置标记
  updateMapMarker();

  // 如果需要，移动地图视角跟随当前位置
  if (playing.value && trajectoryMap.value) {
    trajectoryMap.value.setView([
      currentPoint.value.latitude,
      currentPoint.value.longitude
    ], trajectoryMap.value.getZoom());
  }
};

const updateMapMarker = () => {
  if (!trajectoryMap.value || !currentPoint.value) return;

  // 移除现有标记
  if (currentMarker.value) {
    trajectoryMap.value.removeLayer(currentMarker.value);
  }

  // 添加新的当前位置标记
  currentMarker.value = addMarker(trajectoryMap.value, [
    currentPoint.value.latitude,
    currentPoint.value.longitude
  ], {
    title: '当前位置',
    icon: 'current-position',
    popup: showInfoWindow.value ? `
      <div class="trajectory-popup">
        <h4>当前位置</h4>
        <p>时间: ${formatDateTime(currentPoint.value.positioningTime)}</p>
        <p>坐标: ${currentPoint.value.latitude?.toFixed(6)}, ${currentPoint.value.longitude?.toFixed(6)}</p>
        <p>精度: ${currentPoint.value.accuracy}米</p>
        <p>速度: ${currentPoint.value.speed ? (currentPoint.value.speed * 3.6).toFixed(2) + 'km/h' : '-'}</p>
        ${currentPoint.value.address ? `<p>地址: ${currentPoint.value.address}</p>` : ''}
      </div>
    ` : null
  });
};

const resetView = () => {
  if (!trajectoryMap.value) return;

  // 重置到显示完整轨迹的视野
  const bounds = processedTrajectoryData.value.map(point => [
    point.latitude,
    point.longitude
  ]);

  trajectoryMap.value.fitBounds(bounds);
};

const handleCancel = () => {
  stopPlayback();
  visible.value = false;
};

// 工具方法
const formatTime = (timestamp) => {
  return dayjs(timestamp).format('HH:mm:ss');
};

const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
};

const formatSliderTime = (value) => {
  return formatTime(startTime.value + value);
};

const getPositioningMethodLabel = (method) => {
  const methodMap = {
    'GPS': 'GPS',
    'WIFI': 'WiFi',
    'CELLULAR': '基站',
    'BLUETOOTH': '蓝牙',
    'MANUAL': '手动'
  };
  return methodMap[method] || method;
};

const calculateDistance = (point1, point2) => {
  const R = 6371; // 地球半径（公里）
  const lat1 = point1.latitude * Math.PI / 180;
  const lat2 = point2.latitude * Math.PI / 180;
  const deltaLat = (point2.latitude - point1.latitude) * Math.PI / 180;
  const deltaLon = (point2.longitude - point1.longitude) * Math.PI / 180;

  const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c * 1000; // 返回米
};

// 监听器
watch(() => props.visible, (newVisible) => {
  if (newVisible) {
    nextTick(() => {
      initTrajectoryMap();
    });
  } else {
    // 清理资源
    if (playbackTimer.value) {
      clearInterval(playbackTimer.value);
      playbackTimer.value = null;
    }

    if (trajectoryMap.value) {
      clearMap(trajectoryMap.value);
      trajectoryMap.value = null;
    }
  }
});

watch(() => processedTrajectoryData.value, () => {
  if (props.visible) {
    initTrajectoryMap();
  }
}, { deep: true });
</script>

<style scoped>
.trajectory-player {
  max-height: 80vh;
  overflow-y: auto;
}

.control-panel {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 6px;
}

.time-range {
  display: flex;
  align-items: center;
}

.time-range .label {
  font-weight: 500;
  margin-right: 8px;
}

.time-range .time {
  color: #666;
  font-family: monospace;
}

.current-time {
  text-align: center;
  font-weight: 500;
  color: #1890ff;
  font-family: monospace;
}

.trajectory-map {
  height: 400px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 16px;
}

.map {
  width: 100%;
  height: 100%;
}

.trajectory-info {
  margin-bottom: 16px;
}

.trajectory-stats {
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

:deep(.trajectory-popup) {
  padding: 8px;
  max-width: 200px;
}

:deep(.trajectory-popup h4) {
  margin: 0 0 8px 0;
  color: #1890ff;
  font-size: 14px;
}

:deep(.trajectory-popup p) {
  margin: 4px 0;
  font-size: 12px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .control-panel {
    padding: 12px;
  }

  .trajectory-map {
    height: 300px;
  }

  .trajectory-info {
    overflow-x: auto;
  }
}
</style>