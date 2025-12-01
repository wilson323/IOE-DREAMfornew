<template>
  <div class="map-container" :id="mapId">
    <div class="map-wrapper" ref="mapWrapper">
      <!-- 地图控制按钮 -->
      <div class="map-controls">
        <a-space direction="vertical" size="small">
          <!-- 缩放控制 -->
          <div class="zoom-controls">
            <a-button size="small" @click="zoomIn" :disabled="zoom >= maxZoom">
              <template #icon><PlusOutlined /></template>
            </a-button>
            <a-button size="small" @click="zoomOut" :disabled="zoom <= minZoom">
              <template #icon><MinusOutlined /></template>
            </a-button>
          </div>

          <!-- 地图类型切换 -->
          <a-select
            v-model:value="currentMapType"
            size="small"
            @change="handleMapTypeChange"
            style="width: 80px"
          >
            <a-select-option value="standard">标准</a-select-option>
            <a-select-option value="satellite">卫星</a-select-option>
            <a-select-option value="terrain">地形</a-select-option>
          </a-select>

          <!-- 工具按钮 -->
          <a-tooltip title="重置视图">
            <a-button size="small" @click="resetView">
              <template #icon><AimOutlined /></template>
            </a-button>
          </a-tooltip>

          <a-tooltip title="全屏">
            <a-button size="small" @click="toggleFullscreen">
              <template #icon><FullscreenOutlined /></template>
            </a-button>
          </a-tooltip>

          <a-tooltip title="测距">
            <a-button
              size="small"
              @click="toggleDistanceMeasure"
              :type="measureDistance ? 'primary' : 'default'"
            >
              <template #icon><SwapOutlined /></template>
            </a-button>
          </a-tooltip>
        </a-space>
      </div>

      <!-- 比例尺 -->
      <div class="map-scale" v-if="scale">
        {{ scaleText }}
      </div>

      <!-- 鼠标坐标 -->
      <div class="mouse-coordinates" v-if="showCoordinates && mousePosition">
        纬度: {{ mousePosition.lat.toFixed(6) }}, 经度: {{ mousePosition.lng.toFixed(6) }}
      </div>

      <!-- 加载中提示 -->
      <div class="map-loading" v-if="loading">
        <a-spin size="large" tip="地图加载中..." />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  MinusOutlined,
  AimOutlined,
  FullscreenOutlined,
  SwapOutlined
} from '@ant-design/icons-vue';

// Props
const props = defineProps({
  id: {
    type: String,
    required: true
  },
  center: {
    type: Array,
    default: () => [39.9042, 116.4074] // 默认北京坐标
  },
  zoom: {
    type: Number,
    default: 12
  },
  minZoom: {
    type: Number,
    default: 3
  },
  maxZoom: {
    type: Number,
    default: 18
  },
  markers: {
    type: Array,
    default: () => []
  },
  polylines: {
    type: Array,
    default: () => []
  },
  polygons: {
    type: Array,
    default: () => []
  },
  circles: {
    type: Array,
    default: () => []
  },
  heatpoints: {
    type: Array,
    default: () => []
  },
  geoFences: {
    type: Array,
    default: () => []
  },
  mapType: {
    type: String,
    default: 'standard'
  },
  showControls: {
    type: Boolean,
    default: true
  },
  showCoordinates: {
    type: Boolean,
    default: true
  },
  enableDrawing: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits([
  'ready',
  'map-click',
  'marker-click',
  'polygon-click',
  'circle-click',
  'drawing-complete',
  'bounds-change',
  'zoom-change'
]);

// 响应式数据
const mapWrapper = ref(null);
const mapInstance = ref(null);
const loading = ref(true);
const currentMapType = ref(props.mapType);
const zoom = ref(props.zoom);
const scale = ref(null);
const mousePosition = ref(null);
const measureDistance = ref(false);
const isFullscreen = ref(false);
const drawingLayer = ref(null);
const measureLayer = ref(null);

// 地图ID
const mapId = computed(() => `map-${props.id}`);

// 比例尺文本
const scaleText = computed(() => {
  if (!scale.value) return '';
  if (scale.value < 1) {
    return `1:${Math.round(1 / scale.value)}`;
  }
  return `1:${Math.round(scale.value)}`;
});

// 初始化地图
const initMap = async () => {
  if (!mapWrapper.value) return;

  try {
    loading.value = true;

    // 这里初始化具体的地图实例
    // 可以根据项目需求选择不同的地图SDK
    mapInstance.value = await createMapInstance();

    // 设置地图事件监听
    setupMapEvents();

    // 更新初始数据
    updateMapData();

    loading.value = false;
    emit('ready', mapInstance.value);
  } catch (error) {
    console.error('初始化地图失败:', error);
    message.error('地图初始化失败');
    loading.value = false;
  }
};

// 创建地图实例（示例实现）
const createMapInstance = () => {
  return new Promise((resolve) => {
    // 这里应该根据实际使用的地图SDK来创建实例
    // 例如：高德地图、百度地图、Leaflet等
    const map = {
      id: mapId.value,
      center: props.center,
      zoom: props.zoom,
      mapType: currentMapType.value,

      // 基础方法
      setCenter: (center) => {
        map.center = center;
        // 实际SDK调用
      },

      setZoom: (zoom) => {
        map.zoom = zoom;
        // 实际SDK调用
      },

      addMarker: (marker) => {
        // 添加标记逻辑
        console.log('添加标记:', marker);
      },

      removeMarker: (markerId) => {
        // 移除标记逻辑
        console.log('移除标记:', markerId);
      },

      addPolyline: (polyline) => {
        // 添加折线逻辑
        console.log('添加折线:', polyline);
      },

      addPolygon: (polygon) => {
        // 添加多边形逻辑
        console.log('添加多边形:', polygon);
      },

      addCircle: (circle) => {
        // 添加圆形逻辑
        console.log('添加圆形:', circle);
      },

      fitBounds: (bounds) => {
        // 适配边界逻辑
        console.log('适配边界:', bounds);
      },

      getBounds: () => {
        // 获取当前边界
        return {
          southwest: [props.center[0] - 0.1, props.center[1] - 0.1],
          northeast: [props.center[0] + 0.1, props.center[1] + 0.1]
        };
      },

      setMapType: (type) => {
        map.mapType = type;
        // 实际SDK调用
      },

      destroy: () => {
        // 销毁地图实例
        console.log('销毁地图实例');
      }
    };

    // 模拟地图加载完成
    setTimeout(() => {
      resolve(map);
    }, 1000);
  });
};

// 设置地图事件监听
const setupMapEvents = () => {
  if (!mapInstance.value) return;

  // 地图点击事件
  mapInstance.value.on('click', (e) => {
    mousePosition.value = { lat: e.lat, lng: e.lng };
    emit('map-click', e);
  });

  // 鼠标移动事件
  mapInstance.value.on('mousemove', (e) => {
    mousePosition.value = { lat: e.lat, lng: e.lng };
    updateScale();
  });

  // 缩放变化事件
  mapInstance.value.on('zoomchange', (newZoom) => {
    zoom.value = newZoom;
    updateScale();
    emit('zoom-change', newZoom);
  });

  // 边界变化事件
  mapInstance.value.on('boundschange', (bounds) => {
    emit('bounds-change', bounds);
  });
};

// 更新地图数据
const updateMapData = () => {
  if (!mapInstance.value) return;

  // 清除现有图层
  clearLayers();

  // 添加标记
  props.markers.forEach(marker => {
    addMarker(marker);
  });

  // 添加折线
  props.polylines.forEach(polyline => {
    addPolyline(polyline);
  });

  // 添加多边形
  props.polygons.forEach(polygon => {
    addPolygon(polygon);
  });

  // 添加圆形
  props.circles.forEach(circle => {
    addCircle(circle);
  });

  // 添加地理围栏
  props.geoFences.forEach(fence => {
    addGeoFence(fence);
  });

  // 添加热力图数据
  if (props.heatpoints.length > 0) {
    addHeatmap(props.heatpoints);
  }
};

// 清除图层
const clearLayers = () => {
  // 清除所有标记和图层
  // 实际实现应根据具体地图SDK
};

// 添加标记
const addMarker = (marker) => {
  if (!mapInstance.value) return;

  const markerOptions = {
    id: marker.id,
    position: marker.position,
    title: marker.title,
    icon: marker.icon || 'default',
    popup: marker.popup,
    data: marker.data,
    onClick: () => emit('marker-click', marker)
  };

  mapInstance.value.addMarker(markerOptions);
};

// 添加折线
const addPolyline = (polyline) => {
  if (!mapInstance.value) return;

  const polylineOptions = {
    id: polyline.id,
    positions: polyline.positions,
    color: polyline.color || '#1890ff',
    weight: polyline.weight || 3,
    opacity: polyline.opacity || 0.8,
    dashArray: polyline.dashArray,
    onClick: () => emit('polyline-click', polyline)
  };

  mapInstance.value.addPolyline(polylineOptions);
};

// 添加多边形
const addPolygon = (polygon) => {
  if (!mapInstance.value) return;

  const polygonOptions = {
    id: polygon.id,
    positions: polygon.positions,
    color: polygon.color || '#1890ff',
    fillColor: polygon.fillColor || '#1890ff',
    fillOpacity: polygon.fillOpacity || 0.2,
    weight: polygon.weight || 2,
    onClick: () => emit('polygon-click', polygon)
  };

  mapInstance.value.addPolygon(polygonOptions);
};

// 添加圆形
const addCircle = (circle) => {
  if (!mapInstance.value) return;

  const circleOptions = {
    id: circle.id,
    center: circle.center,
    radius: circle.radius,
    color: circle.color || '#1890ff',
    fillColor: circle.fillColor || '#1890ff',
    fillOpacity: circle.fillOpacity || 0.2,
    weight: circle.weight || 2,
    onClick: () => emit('circle-click', circle)
  };

  mapInstance.value.addCircle(circleOptions);
};

// 添加地理围栏
const addGeoFence = (fence) => {
  if (!mapInstance.value || fence.status !== 'ACTIVE') return;

  const fenceOptions = {
    id: fence.fenceId,
    name: fence.fenceName,
    type: fence.fenceType,
    color: fence.alertLevel === 'CRITICAL' ? '#ff4d4f' : '#1890ff'
  };

  switch (fence.fenceType) {
    case 'CIRCULAR':
      addCircle({
        ...fenceOptions,
        center: [fence.centerLatitude, fence.centerLongitude],
        radius: fence.radius
      });
      break;
    case 'POLYGON':
      if (fence.polygonCoordinates) {
        try {
          const coordinates = JSON.parse(fence.polygonCoordinates);
          addPolygon({
            ...fenceOptions,
            positions: coordinates
          });
        } catch (error) {
          console.error('解析多边形坐标失败:', error);
        }
      }
      break;
    case 'RECTANGLE':
      addPolygon({
        ...fenceOptions,
        positions: [
          [fence.minLatitude, fence.minLongitude],
          [fence.minLatitude, fence.maxLongitude],
          [fence.maxLatitude, fence.maxLongitude],
          [fence.maxLatitude, fence.minLongitude]
        ]
      });
      break;
  }
};

// 添加热力图
const addHeatmap = (points) => {
  if (!mapInstance.value) return;

  // 热力图实现
  console.log('添加热力图数据:', points);
};

// 更新比例尺
const updateScale = () => {
  if (!mapInstance.value) return;

  // 根据当前缩放级别计算比例尺
  const baseScale = 591657527.591555; // 缩放级别0的比例尺
  scale.value = baseScale / Math.pow(2, zoom.value);
};

// 地图操作方法
const zoomIn = () => {
  if (zoom.value < props.maxZoom) {
    const newZoom = Math.min(zoom.value + 1, props.maxZoom);
    mapInstance.value?.setZoom(newZoom);
  }
};

const zoomOut = () => {
  if (zoom.value > props.minZoom) {
    const newZoom = Math.max(zoom.value - 1, props.minZoom);
    mapInstance.value?.setZoom(newZoom);
  }
};

const resetView = () => {
  if (mapInstance.value) {
    mapInstance.value.setCenter(props.center);
    mapInstance.value.setZoom(props.zoom);
  }
};

const setMapType = (type) => {
  currentMapType.value = type;
  mapInstance.value?.setMapType(type);
};

const fitBounds = (bounds) => {
  mapInstance.value?.fitBounds(bounds);
};

const toggleFullscreen = () => {
  const container = document.getElementById(mapId.value);
  if (!isFullscreen.value) {
    if (container.requestFullscreen) {
      container.requestFullscreen();
    }
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    }
  }
  isFullscreen.value = !isFullscreen.value;
};

const toggleDistanceMeasure = () => {
  measureDistance.value = !measureDistance.value;
  // 实现测距功能
};

// 暴露给父组件的方法
defineExpose({
  mapInstance,
  zoomIn,
  zoomOut,
  resetView,
  setMapType,
  fitBounds,
  addMarker,
  removeMarker,
  addPolyline,
  addPolygon,
  addCircle,
  clearLayers
});

// 监听数据变化
watch(() => props.markers, () => {
  updateMapData();
}, { deep: true });

watch(() => props.polylines, () => {
  updateMapData();
}, { deep: true });

watch(() => props.polygons, () => {
  updateMapData();
}, { deep: true });

watch(() => props.circles, () => {
  updateMapData();
}, { deep: true });

watch(() => props.geoFences, () => {
  updateMapData();
}, { deep: true });

watch(() => props.heatpoints, () => {
  if (props.heatpoints.length > 0) {
    addHeatmap(props.heatpoints);
  }
}, { deep: true });

watch(() => props.mapType, (newType) => {
  setMapType(newType);
});

// 组件挂载
onMounted(() => {
  nextTick(() => {
    initMap();
  });

  // 监听全屏变化
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement;
  });
});

// 组件卸载
onUnmounted(() => {
  if (mapInstance.value) {
    mapInstance.value.destroy();
    mapInstance.value = null;
  }

  document.removeEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement;
  });
});
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: #f0f0f0;
}

.map-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 6px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.zoom-controls {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.map-scale {
  position: absolute;
  bottom: 20px;
  left: 20px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.mouse-coordinates {
  position: absolute;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  font-family: monospace;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 2000;
}

/* 全屏模式样式 */
:global(.fullscreen) {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 9999 !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .map-controls {
    top: 5px;
    right: 5px;
    padding: 4px;
  }

  .map-scale {
    bottom: 10px;
    left: 10px;
    font-size: 11px;
  }

  .mouse-coordinates {
    bottom: 10px;
    right: 10px;
    font-size: 11px;
  }
}
</style>