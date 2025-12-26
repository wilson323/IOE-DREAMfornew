<template>
  <div class="video-map-container">
    <!-- 地图容器 -->
    <div
      ref="mapContainer"
      class="video-map"
      :style="{ height: mapHeight, width: '100%' }"
    ></div>

    <!-- 地图控制栏 -->
    <div class="map-controls">
      <a-space>
        <!-- 楼层切换 -->
        <a-select
          v-model:value="currentFloor"
          style="width: 120px"
          placeholder="选择楼层"
          @change="handleFloorChange"
        >
          <a-select-option
            v-for="floor in floors"
            :key="floor.value"
            :value="floor.value"
          >
            {{ floor.label }}
          </a-select-option>
        </a-select>

        <!-- 缩放控制 -->
        <a-button-group>
          <a-button @click="zoomIn" size="small">
            <template #icon><PlusOutlined /></template>
          </a-button>
          <a-button @click="zoomOut" size="small">
            <template #icon><MinusOutlined /></template>
          </a-button>
        </a-button-group>

        <!-- 全屏 -->
        <a-button @click="toggleFullscreen" size="small">
          <template #icon><FullscreenOutlined /></template>
        </a-button>

        <!-- 刷新 -->
        <a-button @click="refreshMap" size="small" :loading="loading">
          <template #icon><ReloadOutlined /></template>
        </a-button>
      </a-space>
    </div>

    <!-- 设备弹窗 -->
    <a-modal
      v-model:open="showDeviceModal"
      :title="selectedDevice?.deviceName"
      :footer="null"
      width="600px"
    >
      <div v-if="selectedDevice" class="device-modal-content">
        <!-- 设备信息 -->
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="设备编码">
            {{ selectedDevice.deviceCode }}
          </a-descriptions-item>
          <a-descriptions-item label="设备名称">
            {{ selectedDevice.deviceName }}
          </a-descriptions-item>
          <a-descriptions-item label="所属区域">
            {{ selectedDevice.areaId }}
          </a-descriptions-item>
          <a-descriptions-item label="楼层">
            {{ selectedDevice.floorLevel }}
          </a-descriptions-item>
          <a-descriptions-item label="坐标">
            [{{ selectedDevice.xCoordinate }}, {{ selectedDevice.yCoordinate }}]
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(selectedDevice.displayStatus)">
              {{ getStatusText(selectedDevice.displayStatus) }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>

        <!-- 视频播放器 -->
        <div class="video-player-wrapper" style="margin-top: 16px">
          <video-player
            v-if="selectedDevice.clickAction === 1"
            :device-id="selectedDevice.deviceId"
            :width="560"
            :height="315"
            autoplay
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import {
  PlusOutlined,
  MinusOutlined,
  FullscreenOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { request } from '@/api/request'
import VideoPlayer from './VideoPlayer.vue'

/**
 * 视频地图组件
 * 支持设备标注、热点交互、视频联动
 */

// Props
const props = defineProps({
  // 区域ID
  areaId: {
    type: Number,
    required: true
  },
  // 楼层
  floorLevel: {
    type: Number,
    default: 1
  },
  // 地图高度
  mapHeight: {
    type: String,
    default: '600px'
  },
  // 是否显示设备标签
  showLabels: {
    type: Boolean,
    default: true
  },
  // 是否启用设备聚合
  enableCluster: {
    type: Boolean,
    default: true
  },
  // 点击行为
  clickAction: {
    type: Number,
    default: 1 // 1-播放视频 2-查看详情 3-云台控制
  }
})

// Emits
const emit = defineEmits(['device-click', 'map-ready', 'floor-change'])

// Refs
const mapContainer = ref(null)
const map = ref(null)
const currentFloor = ref(props.floorLevel)
const loading = ref(false)
const showDeviceModal = ref(false)
const selectedDevice = ref(null)

// Data
const floors = ref([
  { label: '地下一层', value: 0 },
  { label: '一楼', value: 1 },
  { label: '二楼', value: 2 },
  { label: '三楼', value: 3 },
  { label: '四楼', value: 4 },
  { label: '五楼', value: 5 }
])

const mapImage = ref(null)
const deviceMarkers = ref([])
const hotspotMarkers = ref([])

// Computed
const getStatusColor = (status) => {
  const colors = {
    0: 'default', // 隐藏
    1: 'success', // 显示
    2: 'warning'  // 维护
  }
  return colors[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    0: '隐藏',
    1: '显示',
    2: '维护'
  }
  return texts[status] || '未知'
}

/**
 * 初始化地图
 */
const initMap = async () => {
  try {
    loading.value = true

    // 销毁旧地图
    if (map.value) {
      map.value.remove()
      map.value = null
    }

    // 获取地图图片
    await loadMapImage()

    if (!mapImage.value) {
      message.warning('当前楼层暂无地图')
      return
    }

    // 创建地图实例
    map.value = L.map(mapContainer.value, {
      crs: L.CRS.Simple,
      minZoom: 1,
      maxZoom: 5,
      zoom: 3,
      zoomControl: false,
      attributionControl: false
    })

    // 设置地图边界
    const bounds = [
      [0, 0],
      [mapImage.value.mapHeight, mapImage.value.mapWidth]
    ]
    L.imageOverlay(mapImage.value.imageUrl, bounds).addTo(map.value)

    // 加载设备标注
    await loadDeviceMarkers()

    // 加载热点标注
    await loadHotspotMarkers()

    // 触发地图就绪事件
    emit('map-ready', { map: map.value, floorLevel: currentFloor.value })

    message.success(`加载${currentFloor.value}楼地图成功`)
  } catch (error) {
    console.error('[视频地图] 初始化失败:', error)
    message.error('地图加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

/**
 * 加载地图图片
 */
const loadMapImage = async () => {
  try {
    const response = await request({
      url: '/api/video/map/images/floor',
      method: 'get',
      params: {
        areaId: props.areaId,
        floorLevel: currentFloor.value
      }
    })

    if (response.code === 200 && response.data) {
      mapImage.value = response.data
    } else {
      mapImage.value = null
    }
  } catch (error) {
    console.error('[视频地图] 加载地图图片失败:', error)
    mapImage.value = null
  }
}

/**
 * 加载设备标注
 */
const loadDeviceMarkers = async () => {
  try {
    if (!mapImage.value) return

    const response = await request({
      url: '/api/video/map/devices',
      method: 'get',
      params: {
        mapImageId: mapImage.value.id
      }
    })

    if (response.code === 200 && response.data) {
      // 清除旧标注
      deviceMarkers.value.forEach(marker => marker.remove())
      deviceMarkers.value = []

      // 添加新标注
      response.data.forEach(device => {
        if (device.displayStatus !== 0) {
          const marker = createDeviceMarker(device)
          if (marker) {
            marker.addTo(map.value)
            deviceMarkers.value.push(marker)
          }
        }
      })

      console.log(`[视频地图] 加载设备标注: ${deviceMarkers.value.length}个`)
    }
  } catch (error) {
    console.error('[视频地图] 加载设备标注失败:', error)
  }
}

/**
 * 创建设备标注
 */
const createDeviceMarker = (device) => {
  const icon = L.divIcon({
    className: 'device-marker',
    html: `
      <div class="device-icon" style="
        width: ${device.markerSize * 10}px;
        height: ${device.markerSize * 10}px;
        background-color: ${device.markerColor};
        border-radius: 50%;
        border: 2px solid white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
      ">
        <svg viewBox="0 0 24 24" width="60%" height="60%" fill="white">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm0 10c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4z"/>
        </svg>
      </div>
      ${props.showLabels ? `<div class="device-label" style="position:absolute; top: 100%; left: 50%; transform: translateX(-50%); white-space: nowrap; font-size: 12px; color: #333; background: rgba(255,255,255,0.9); padding: 2px 6px; border-radius: 4px; margin-top: 4px;">${device.deviceName}</div>` : ''}
    `,
    iconSize: [device.markerSize * 10, device.markerSize * 10],
    iconAnchor: [device.markerSize * 5, device.markerSize * 5]
  })

  const marker = L.marker(
    [device.yCoordinate, device.xCoordinate],
    { icon }
  )

  // 点击事件
  marker.on('click', () => {
    handleDeviceClick(device)
  })

  // 悬停提示
  marker.bindTooltip(device.deviceName, {
    permanent: false,
    direction: 'top',
    offset: [0, -10]
  })

  return marker
}

/**
 * 加载热点标注
 */
const loadHotspotMarkers = async () => {
  try {
    if (!mapImage.value) return

    const response = await request({
      url: '/api/video/map/hotspots',
      method: 'get',
      params: {
        mapImageId: mapImage.value.id
      }
    })

    if (response.code === 200 && response.data) {
      // 清除旧热点
      hotspotMarkers.value.forEach(marker => marker.remove())
      hotspotMarkers.value = []

      // 添加新热点
      response.data.forEach(hotspot => {
        if (hotspot.displayStatus === 1) {
          const marker = createHotspotMarker(hotspot)
          if (marker) {
            marker.addTo(map.value)
            hotspotMarkers.value.push(marker)
          }
        }
      })

      console.log(`[视频地图] 加载热点标注: ${hotspotMarkers.value.length}个`)
    }
  } catch (error) {
    console.error('[视频地图] 加载热点标注失败:', error)
  }
}

/**
 * 创建热点标注
 */
const createHotspotMarker = (hotspot) => {
  const icon = L.divIcon({
    className: 'hotspot-marker',
    html: `
      <div style="
        width: 30px;
        height: 30px;
        background-color: ${hotspot.color || '#FF5722'};
        border-radius: 4px;
        border: 2px solid white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        font-size: 14px;
        color: white;
      ">
        ${hotspot.hotspotName.charAt(0)}
      </div>
    `,
    iconSize: [30, 30],
    iconAnchor: [15, 15]
  })

  const marker = L.marker(
    [hotspot.yCoordinate, hotspot.xCoordinate],
    { icon }
  )

  // 点击事件
  marker.on('click', () => {
    handleHotspotClick(hotspot)
  })

  // 悬停提示
  if (hotspot.tooltipText) {
    marker.bindTooltip(hotspot.tooltipText, {
      permanent: false,
      direction: 'top'
    })
  }

  return marker
}

/**
 * 处理设备点击
 */
const handleDeviceClick = (device) => {
  console.log('[视频地图] 设备点击:', device)
  selectedDevice.value = device
  emit('device-click', device)

  // 根据点击行为处理
  if (device.clickAction === 1) {
    // 播放视频
    showDeviceModal.value = true
  } else if (device.clickAction === 2) {
    // 查看详情
    showDeviceModal.value = true
  } else if (device.clickAction === 3) {
    // 云台控制
    // TODO: 打开云台控制面板
    message.info('云台控制功能开发中')
  }
}

/**
 * 处理热点点击
 */
const handleHotspotClick = (hotspot) => {
  console.log('[视频地图] 热点点击:', hotspot)

  if (hotspot.clickAction === 3 && hotspot.actionData) {
    // 跳转链接
    window.open(hotspot.actionData, '_blank')
  } else if (hotspot.deviceId) {
    // 关联设备，加载设备信息
    message.info(`关联设备ID: ${hotspot.deviceId}`)
  }
}

/**
 * 楼层切换
 */
const handleFloorChange = (floorLevel) => {
  console.log('[视频地图] 楼层切换:', floorLevel)
  currentFloor.value = floorLevel
  emit('floor-change', floorLevel)
  initMap()
}

/**
 * 放大
 */
const zoomIn = () => {
  map.value?.zoomIn()
}

/**
 * 缩小
 */
const zoomOut = () => {
  map.value?.zoomOut()
}

/**
 * 全屏切换
 */
const toggleFullscreen = () => {
  const container = mapContainer.value
  if (!document.fullscreenElement) {
    container.requestFullscreen().catch(err => {
      console.error('[视频地图] 全屏失败:', err)
    })
  } else {
    document.exitFullscreen()
  }
}

/**
 * 刷新地图
 */
const refreshMap = () => {
  initMap()
}

/**
 * 公开方法
 */
defineExpose({
  initMap,
  refreshMap,
  getMap: () => map.value
})

// Lifecycle
onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map.value) {
    map.value.remove()
    map.value = null
  }
})

// Watch
watch(() => props.areaId, () => {
  initMap()
})
</script>

<style scoped lang="less">
.video-map-container {
  position: relative;

  .video-map {
    background-color: #f5f5f5;
    border-radius: 4px;
    overflow: hidden;
  }

  .map-controls {
    position: absolute;
    top: 16px;
    right: 16px;
    z-index: 1000;
    background: rgba(255, 255, 255, 0.95);
    padding: 8px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .device-modal-content {
    .video-player-wrapper {
      border-radius: 4px;
      overflow: hidden;
    }
  }
}

// Leaflet 样式覆盖
:deep(.leaflet-container) {
  background-color: #f5f5f5;
}

:deep(.device-marker) {
  background: transparent;
  border: none;
}

:deep(.hotspot-marker) {
  background: transparent;
  border: none;
}
</style>
