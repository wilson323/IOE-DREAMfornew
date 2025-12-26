<template>
  <div class="device-map-container">
    <!-- 百度地图容器 -->
    <baidu-map
      class="map"
      :center="mapCenter"
      :zoom="mapZoom"
      :scroll-wheel-zoom="true"
      @ready="mapReadyHandler"
    >
      <!-- 缩放控件 -->
      <bm-navigation anchor="BMAP_ANCHOR_TOP_RIGHT" />

      <!-- 地图类型控件 -->
      <bm-map-type :map-types="['BMAP_NORMAL_MAP', 'BMAP_HYBRID_MAP']" />

      <!-- 设备标记点 -->
      <bm-marker
        v-for="device in filteredDevices"
        :key="device.deviceId"
        :position="{ lng: device.longitude, lat: device.latitude }"
        :title="device.deviceName"
        @click="handleMarkerClick(device)"
      >
        <!-- 自定义标记样式 -->
        <bm-label
          :content="device.deviceName"
          :label-style="{
            color: device.status === 'online' ? '#00ff00' : '#ff0000',
            fontSize: '12px',
            backgroundColor: 'rgba(0,0,0,0.7)',
            padding: '5px 10px',
            borderRadius: '4px'
          }"
        />
      </bm-marker>

      <!-- 区域多边形 -->
      <bm-polygon
        v-for="area in areas"
        :key="area.areaId"
        :path="area.boundary"
        stroke-color="blue"
        :stroke-opacity="0.5"
        :stroke-weight="2"
        fill-color="blue"
        :fill-opacity="0.1"
      />
    </baidu-map>

    <!-- 设备筛选器 -->
    <div class="map-controls">
      <a-space>
        <a-radio-group v-model:value="statusFilter" button-style="solid">
          <a-radio-button value="all">全部</a-radio-button>
          <a-radio-button value="online">在线</a-radio-button>
          <a-radio-button value="offline">离线</a-radio-button>
        </a-radio-group>

        <a-select
          v-model:value="selectedArea"
          style="width: 200px"
          placeholder="选择区域"
          allowClear
        >
          <a-select-option value="">全部区域</a-select-option>
          <a-select-option
            v-for="area in areas"
            :key="area.areaId"
            :value="area.areaId"
          >
            {{ area.areaName }}
          </a-select-option>
        </a-select>
      </a-space>
    </div>

    <!-- 设备详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="设备详情"
      :footer="null"
      width="600px"
    >
      <a-descriptions v-if="selectedDevice" :column="2" bordered>
        <a-descriptions-item label="设备ID">{{ selectedDevice.deviceId }}</a-descriptions-item>
        <a-descriptions-item label="设备名称">{{ selectedDevice.deviceName }}</a-descriptions-item>
        <a-descriptions-item label="设备类型">{{ selectedDevice.deviceType }}</a-descriptions-item>
        <a-descriptions-item label="设备状态">
          <a-tag :color="selectedDevice.status === 'online' ? 'success' : 'error'">
            {{ selectedDevice.status === 'online' ? '在线' : '离线' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="所属区域">{{ selectedDevice.areaName }}</a-descriptions-item>
        <a-descriptions-item label="安装位置">{{ selectedDevice.installLocation }}</a-descriptions-item>
        <a-descriptions-item label="经度">{{ selectedDevice.longitude }}</a-descriptions-item>
        <a-descriptions-item label="纬度">{{ selectedDevice.latitude }}</a-descriptions-item>
        <a-descriptions-item label="最后在线" :span="2">
          {{ selectedDevice.lastOnlineTime }}
        </a-descriptions-item>
      </a-descriptions>

      <div style="margin-top: 16px; text-align: right">
        <a-button @click="detailVisible = false">关闭</a-button>
        <a-button type="primary" style="margin-left: 8px" @click="viewDeviceDetail">
          查看详情
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';

// Props
const props = defineProps({
  devices: {
    type: Array,
    default: () => []
  },
  areas: {
    type: Array,
    default: () => []
  }
});

// Emits
const emit = defineEmits(['device-click']);

// 地图状态
const mapCenter = ref({ lng: 116.404, lat: 39.915 }); // 默认中心：北京
const mapZoom = ref(15);
const mapReady = ref(false);

// 筛选器状态
const statusFilter = ref('all');
const selectedArea = ref('');

// 设备详情
const detailVisible = ref(false);
const selectedDevice = ref(null);

// 计算属性：筛选后的设备列表
const filteredDevices = computed(() => {
  let result = props.devices;

  // 状态筛选
  if (statusFilter.value !== 'all') {
    result = result.filter(device => device.status === statusFilter.value);
  }

  // 区域筛选
  if (selectedArea.value) {
    result = result.filter(device => device.areaId === selectedArea.value);
  }

  return result;
});

// 地图准备完成回调
const mapReadyHandler = ({ map }) => {
  mapReady.value = true;
  console.log('[设备地图] 地图初始化完成', map);

  // 如果有设备，自动调整地图视野
  if (props.devices.length > 0) {
    adjustMapView(map);
  }
};

// 调整地图视野以包含所有设备
const adjustMapView = (map) => {
  if (props.devices.length === 0) return;

  const points = props.devices.map(device => ({
    lng: device.longitude,
    lat: device.latitude
  }));

  // 使用百度地图 API 调整视野
  const view = map.getViewport(points);
  mapCenter.value = view.center;
  mapZoom.value = view.zoom;
};

// 标记点点击事件
const handleMarkerClick = (device) => {
  console.log('[设备地图] 点击设备标记', device);
  selectedDevice.value = device;
  detailVisible.value = true;
  emit('device-click', device);
};

// 查看设备详情
const viewDeviceDetail = () => {
  emit('device-click', selectedDevice.value);
  detailVisible.value = false;
};

// 组件挂载时加载设备数据
onMounted(() => {
  console.log('[设备地图] 组件已挂载', {
    deviceCount: props.devices.length,
    areaCount: props.areas.length
  });
});
</script>

<style scoped>
.device-map-container {
  width: 100%;
  height: 600px;
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
}

.map-controls {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 999;
  background: rgba(255, 255, 255, 0.95);
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
</style>
