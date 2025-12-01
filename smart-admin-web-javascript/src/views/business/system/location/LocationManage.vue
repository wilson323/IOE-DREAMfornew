<template>
  <div class="location-manage">
    <a-card title="位置管理" :bordered="false">
      <!-- 搜索区域 -->
      <div class="search-form">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="用户/设备">
            <a-select
              v-model:value="searchForm.targetType"
              style="width: 100px"
              @change="handleTargetTypeChange"
            >
              <a-select-option value="user">用户</a-select-option>
              <a-select-option value="device">设备</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="ID">
            <a-input
              v-model:value="searchForm.targetId"
              placeholder="请输入用户/设备ID"
              style="width: 150px"
            />
          </a-form-item>
          <a-form-item label="定位方式">
            <a-select
              v-model:value="searchForm.positioningMethod"
              style="width: 120px"
              allow-clear
            >
              <a-select-option value="GPS">GPS</a-select-option>
              <a-select-option value="WIFI">WiFi</a-select-option>
              <a-select-option value="CELLULAR">基站</a-select-option>
              <a-select-option value="BLUETOOTH">蓝牙</a-select-option>
              <a-select-option value="MANUAL">手动</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="时间范围">
            <a-range-picker
              v-model:value="searchForm.timeRange"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              :placeholder="['开始时间', '结束时间']"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" :loading="loading">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset" style="margin-left: 8px">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 地图和操作按钮 -->
      <div class="map-section">
        <div class="map-container">
          <MapContainer
            id="location-map"
            ref="mapContainerRef"
            :center="mapConfig.center"
            :zoom="mapConfig.zoom"
            :markers="mapMarkers"
            :polylines="mapPolylines"
            :heatpoints="heatpoints"
            :geo-fences="geoFences"
            @map-click="handleMapClick"
            @marker-click="handleMarkerClick"
            @ready="onMapReady"
          />
        </div>

        <div class="map-controls">
          <a-space direction="vertical">
            <a-button type="primary" @click="showCurrentLocation">
              <template #icon><EnvironmentOutlined /></template>
              我的位置
            </a-button>
            <a-button @click="startRealTimeTracking" :loading="tracking">
              <template #icon><WifiOutlined /></template>
              {{ tracking ? '追踪中...' : '实时追踪' }}
            </a-button>
            <a-button @click="showGeoFenceModal">
              <template #icon><AimOutlined /></template>
              地理围栏
            </a-button>
            <a-button @click="toggleHeatmap" :type="showHeatmap ? 'primary' : 'default'">
              <template #icon><FireOutlined /></template>
              热力图
            </a-button>
            <a-button @click="showTrajectoryModal">
              <template #icon><RouteOutlined /></template>
              轨迹回放
            </a-button>
            <a-button @click="exportLocationData">
              <template #icon><ExportOutlined /></template>
              导出数据
            </a-button>
            <a-divider>地图工具</a-divider>
            <a-select v-model:value="mapConfig.mapType" @change="handleMapTypeChange" style="width: 100%">
              <a-select-option value="standard">标准</a-select-option>
              <a-select-option value="satellite">卫星</a-select-option>
              <a-select-option value="terrain">地形</a-select-option>
            </a-select>
            <a-button @click="resetMapView">
              <template #icon><CompressOutlined /></template>
              重置视图
            </a-button>
          </a-space>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="statistics-section">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic title="今日位置更新" :value="statistics.todayUpdates" suffix="次" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="活跃用户" :value="statistics.activeUsers" suffix="人" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="地理围栏" :value="statistics.geoFenceCount" suffix="个" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="触发事件" :value="statistics.triggerEvents" suffix="次" />
          </a-col>
        </a-row>
      </div>

      <!-- 位置历史表格 -->
      <div class="table-section">
        <a-table
          :columns="locationColumns"
          :data-source="locationHistory"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          row-key="recordId"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'location'">
              {{ record.latitude }}, {{ record.longitude }}
            </template>
            <template v-else-if="column.key === 'accuracy'">
              <a-tag :color="getAccuracyColor(record.accuracy)">
                {{ record.accuracy }}米
              </a-tag>
            </template>
            <template v-else-if="column.key === 'positioningMethod'">
              <a-tag>{{ getPositioningMethodLabel(record.positioningMethod) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="showLocationDetail(record)">
                  详情
                </a-button>
                <a-button type="link" size="small" @click="showLocationOnMap(record)">
                  地图
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <!-- 地理围栏弹窗 -->
    <GeoFenceModal
      v-model:visible="geoFenceModalVisible"
      @success="handleGeoFenceSuccess"
    />

    <!-- 位置详情弹窗 -->
    <LocationDetailModal
      v-model:visible="locationDetailVisible"
      :location="selectedLocation"
    />

    <!-- 轨迹回放弹窗 -->
    <TrajectoryPlayer
      v-model:visible="trajectoryModalVisible"
      :trajectory-data="trajectoryData"
      :map-instance="mapInstance"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, h } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  EnvironmentOutlined,
  WifiOutlined,
  AimOutlined,
  ExportOutlined,
  FireOutlined,
  RouteOutlined,
  CompressOutlined
} from '@ant-design/icons-vue';
import LocationApi from '/@/api/location/LocationApi';
import GeoFenceModal from './components/GeoFenceModal.vue';
import LocationDetailModal from './components/LocationDetailModal.vue';
import TrajectoryPlayer from './components/TrajectoryPlayer.vue';
import MapContainer from './components/MapContainer.vue';
import { locationStore } from '/@/store/modules/location';

// 响应式数据
const loading = ref(false);
const tracking = ref(false);
const geoFenceModalVisible = ref(false);
const locationDetailVisible = ref(false);
const trajectoryModalVisible = ref(false);
const selectedLocation = ref(null);
const mapInstance = ref(null);
const mapContainerRef = ref(null);
const trackingWs = ref(null);
const showHeatmap = ref(false);

// 地图配置
const mapConfig = reactive({
  center: [39.9042, 116.4074], // 默认北京坐标
  zoom: 12,
  mapType: 'standard'
});

// 地图数据
const mapMarkers = ref([]);
const mapPolylines = ref([]);
const heatpoints = ref([]);
const geoFences = ref([]);
const trajectoryData = ref([]);

// 搜索表单
const searchForm = reactive({
  targetType: 'user',
  targetId: null,
  positioningMethod: null,
  timeRange: null
});

// 统计数据
const statistics = reactive({
  todayUpdates: 0,
  activeUsers: 0,
  geoFenceCount: 0,
  triggerEvents: 0
});

// 位置历史数据
const locationHistory = ref([]);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
});

// 表格列定义
const locationColumns = [
  {
    title: searchForm.targetType === 'user' ? '用户ID' : '设备ID',
    dataIndex: searchForm.targetType === 'user' ? 'userId' : 'deviceId',
    key: 'targetId',
    width: 100
  },
  {
    title: '位置',
    key: 'location',
    width: 160,
    customRender: ({ record }) => `${record.latitude?.toFixed(6) || '-'}, ${record.longitude?.toFixed(6) || '-'}`
  },
  {
    title: '精度',
    dataIndex: 'accuracy',
    key: 'accuracy',
    width: 100,
    customRender: ({ record }) => record.accuracy ? `${record.accuracy}m` : '-'
  },
  {
    title: '定位方式',
    dataIndex: 'positioningMethod',
    key: 'positioningMethod',
    width: 100,
    customRender: ({ record }) => {
      const method = record.positioningMethod;
      const methodMap = {
        'GPS': { color: 'green', text: 'GPS' },
        'WIFI': { color: 'blue', text: 'WiFi' },
        'CELLULAR': { color: 'orange', text: '基站' },
        'BLUETOOTH': { color: 'purple', text: '蓝牙' },
        'MANUAL': { color: 'default', text: '手动' }
      };
      const config = methodMap[method] || { color: 'default', text: method };
      return h('a-tag', { color: config.color }, config.text);
    }
  },
  {
    title: '定位时间',
    dataIndex: 'positioningTime',
    key: 'positioningTime',
    width: 180,
    customRender: ({ record }) => {
      if (!record.positioningTime) return '-';
      return new Date(record.positioningTime).toLocaleString();
    }
  },
  {
    title: '地址',
    dataIndex: 'address',
    key: 'address',
    width: 200,
    ellipsis: true
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
];

// 处理地图点击
const handleMapClick = (e) => {
  console.log('地图点击:', e);
  // 可以在这里处理地图点击事件，比如添加标记
};

// 处理标记点击
const handleMarkerClick = (marker) => {
  console.log('标记点击:', marker);
  selectedLocation.value = marker.data;
  locationDetailVisible.value = true;
};

// 地图准备就绪
const onMapReady = (map) => {
  mapInstance.value = map;
  console.log('地图准备就绪');
};

// 处理目标类型变化
const handleTargetTypeChange = () => {
  searchForm.targetId = null;
  locationHistory.value = [];
  pagination.current = 1;
};

// 处理地图类型变化
const handleMapTypeChange = (type) => {
  if (mapContainerRef.value) {
    mapContainerRef.value.setMapType(type);
  }
};

// 重置地图视图
const resetMapView = () => {
  if (mapContainerRef.value) {
    mapContainerRef.value.resetView();
  }
};

// 切换热力图显示
const toggleHeatmap = () => {
  showHeatmap.value = !showHeatmap.value;
  if (showHeatmap.value) {
    loadHeatmapData();
  }
};

// 获取精度颜色
const getAccuracyColor = (accuracy) => {
  if (accuracy <= 10) return 'green';
  if (accuracy <= 50) return 'orange';
  return 'red';
};

// 获取定位方式标签
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

// 搜索位置数据
const handleSearch = async () => {
  loading.value = true;
  try {
    const params = {
      targetType: searchForm.targetType,
      targetId: searchForm.targetId,
      positioningMethod: searchForm.positioningMethod,
      startTime: searchForm.timeRange?.[0]?.toISOString(),
      endTime: searchForm.timeRange?.[1]?.toISOString(),
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    };

    let response;
    if (searchForm.targetType === 'user') {
      response = await LocationApi.getUserLocationHistory(params);
    } else {
      response = await LocationApi.getDeviceLocationHistory(params);
    }

    if (response.success) {
      locationHistory.value = response.data || [];
      pagination.total = response.total || 0;

      // 更新地图标记
      updateMapMarkers();

      // 更新状态管理
      locationStore.setLocationHistory(locationHistory.value);
    } else {
      message.error(response.message || '获取位置历史失败');
    }
  } catch (error) {
    console.error('搜索位置数据失败:', error);
    message.error('搜索位置数据失败');
  } finally {
    loading.value = false;
  }
};

// 重置搜索
const handleReset = () => {
  searchForm.targetId = null;
  searchForm.positioningMethod = null;
  searchForm.timeRange = null;
  locationHistory.value = [];
  mapMarkers.value = [];
  mapPolylines.value = [];
  pagination.current = 1;
  handleSearch();
};

// 加载热力图数据
const loadHeatmapData = async () => {
  try {
    const params = {
      startTime: searchForm.timeRange?.[0]?.toISOString(),
      endTime: searchForm.timeRange?.[1]?.toISOString()
    };

    const response = await LocationApi.getHeatmapData(params);
    if (response.success) {
      heatpoints.value = response.data || [];
    }
  } catch (error) {
    console.error('加载热力图数据失败:', error);
  }
};

// 显示当前位置
const showCurrentLocation = () => {
  if (!navigator.geolocation) {
    message.error('浏览器不支持地理定位');
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords;

      // 更新用户位置到服务器
      LocationApi.updateUserPosition({
        userId: 1, // 这里应该从用户信息中获取
        latitude: latitude,
        longitude: longitude,
        accuracy: Math.round(position.coords.accuracy),
        positioningMethod: 'GPS',
        locationSource: 'BROWSER'
      }).then(response => {
        if (response.success) {
          message.success('位置更新成功');

          // 在地图上显示当前位置
          if (mapInstance.value) {
            mapInstance.value.setView([latitude, longitude], 15);
            addMarker(mapInstance.value, [latitude, longitude], {
              title: '当前位置',
              icon: 'current-location'
            });
          }
        }
      });
    },
    (error) => {
      message.error('获取当前位置失败: ' + error.message);
    }
  );
};

// 开始实时追踪
const startRealTimeTracking = () => {
  if (tracking.value) {
    // 停止追踪
    if (trackingWs.value) {
      trackingWs.value.close();
      trackingWs.value = null;
    }
    tracking.value = false;
    return;
  }

  tracking.value = true;

  // 开始WebSocket实时追踪
  const userId = searchForm.userId || 1; // 默认用户ID
  trackingWs.value = LocationApi.startRealTimeTracking(userId, (data) => {
    // 处理实时位置数据
    if (data.latitude && data.longitude) {
      // 在地图上添加位置点
      if (mapInstance.value) {
        addMarker(mapInstance.value, [data.latitude, data.longitude], {
          title: '实时位置',
          icon: 'real-time'
        });
      }

      // 更新位置历史
      locationHistory.value.unshift({
        ...data,
        positioningTime: new Date().toISOString()
      });
    }
  });
};

// 显示地理围栏弹窗
const showGeoFenceModal = () => {
  geoFenceModalVisible.value = true;
};

// 处理地理围栏创建成功
const handleGeoFenceSuccess = (geoFence) => {
  message.success('地理围栏创建成功');

  // 在地图上显示围栏
  if (mapInstance.value && geoFence) {
    // 根据围栏类型在地图上绘制
    // 这里需要根据具体的地图API来实现
  }
};

// 显示位置详情
const showLocationDetail = (location) => {
  selectedLocation.value = location;
  locationDetailVisible.value = true;
};

// 在地图上显示位置
const showLocationOnMap = (location) => {
  if (mapInstance.value) {
    mapInstance.value.setView([location.latitude, location.longitude], 15);
    addMarker(mapInstance.value, [location.latitude, location.longitude], {
      title: '位置详情',
      popup: `
        <div>
          <p>用户ID: ${location.userId}</p>
          <p>坐标: ${location.latitude}, ${location.longitude}</p>
          <p>精度: ${location.accuracy}米</p>
          <p>时间: ${location.positioningTime}</p>
        </div>
      `
    });
  }
};

// 更新地图标记
const updateMapMarkers = () => {
  if (!locationHistory.value.length) {
    mapMarkers.value = [];
    mapPolylines.value = [];
    return;
  }

  // 生成地图标记数据
  const markers = locationHistory.value.map((location, index) => {
    const isLatest = index === 0;
    return {
      id: location.recordId || index,
      position: [location.latitude, location.longitude],
      title: isLatest ? '最新位置' : '历史位置',
      icon: isLatest ? 'current-location' : 'history-location',
      data: location,
      popup: `
        <div class="location-popup">
          <h4>${isLatest ? '最新位置' : '历史位置'}</h4>
          <p>坐标: ${location.latitude?.toFixed(6)}, ${location.longitude?.toFixed(6)}</p>
          <p>精度: ${location.accuracy}米</p>
          <p>时间: ${new Date(location.positioningTime).toLocaleString()}</p>
          <p>方式: ${getPositioningMethodLabel(location.positioningMethod)}</p>
          ${location.address ? `<p>地址: ${location.address}</p>` : ''}
        </div>
      `
    };
  });

  mapMarkers.value = markers;

  // 生成轨迹线数据
  if (locationHistory.value.length > 1) {
    const polyline = {
      id: 'trajectory',
      positions: locationHistory.value.map(loc => [loc.latitude, loc.longitude]),
      color: '#1890ff',
      weight: 3,
      opacity: 0.8,
      dashArray: '5, 10'
    };
    mapPolylines.value = [polyline];
  } else {
    mapPolylines.value = [];
  }

  // 自动调整地图视野
  if (mapContainerRef.value && markers.length > 0) {
    mapContainerRef.value.fitBounds(markers.map(m => m.position));
  }
};

// 导出位置数据
const exportLocationData = () => {
  // 实现数据导出逻辑
  const dataToExport = locationHistory.value.map(item => ({
    用户ID: item.userId,
    纬度: item.latitude,
    经度: item.longitude,
    精度: item.accuracy + '米',
    定位方式: getPositioningMethodLabel(item.positioningMethod),
    定位时间: item.positioningTime,
    设备ID: item.deviceId
  }));

  // 转换为CSV并下载
  const csv = convertToCSV(dataToExport);
  downloadCSV(csv, `位置数据_${new Date().toISOString().split('T')[0]}.csv`);

  message.success('数据导出成功');
};

// 转换为CSV格式
const convertToCSV = (data) => {
  if (!data.length) return '';

  const headers = Object.keys(data[0]);
  const csvHeaders = headers.join(',');
  const csvRows = data.map(row =>
    headers.map(header => `"${row[header] || ''}"`).join(',')
  );

  return [csvHeaders, ...csvRows].join('\n');
};

// 下载CSV文件
const downloadCSV = (csv, filename) => {
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  handleSearch();
};

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 这里可以调用统计API获取数据
    // 暂时使用模拟数据
    statistics.todayUpdates = 1234;
    statistics.activeUsers = 567;
    statistics.geoFenceCount = 89;
    statistics.triggerEvents = 234;
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// 显示轨迹回放弹窗
const showTrajectoryModal = () => {
  if (!locationHistory.value.length) {
    message.warning('没有轨迹数据可供回放');
    return;
  }

  trajectoryData.value = locationHistory.value;
  trajectoryModalVisible.value = true;
};

// 加载地理围栏数据
const loadGeoFences = async () => {
  try {
    const response = await LocationApi.getGeoFenceList();
    if (response.success) {
      geoFences.value = response.data || [];
    }
  } catch (error) {
    console.error('加载地理围栏失败:', error);
  }
};

// 组件挂载
onMounted(() => {
  loadStatistics();
  loadGeoFences();
  handleSearch(); // 加载默认数据
});

// 组件卸载
onUnmounted(() => {
  // 清理WebSocket连接
  if (trackingWs.value) {
    trackingWs.value.close();
    trackingWs.value = null;
  }

  // 清理地图实例
  if (mapInstance.value) {
    mapInstance.value = null;
  }
});
</script>

<style scoped>
.location-manage {
  padding: 16px;
}

.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.map-section {
  display: flex;
  margin-bottom: 16px;
  gap: 16px;
}

.map-container {
  flex: 1;
  height: 500px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
}

.map-controls {
  width: 220px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.statistics-section {
  margin-bottom: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 1px solid #e3f2fd;
}

.table-section {
  margin-top: 16px;
}

/* 地图弹窗样式 */
:deep(.location-popup) {
  padding: 8px;
  max-width: 200px;
}

:deep(.location-popup h4) {
  margin: 0 0 8px 0;
  color: #1890ff;
  font-size: 14px;
}

:deep(.location-popup p) {
  margin: 4px 0;
  font-size: 12px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .map-section {
    flex-direction: column;
  }

  .map-controls {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .map-container {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .location-manage {
    padding: 8px;
  }

  .map-container {
    height: 300px;
  }

  .search-form {
    padding: 12px;
  }

  .statistics-section {
    padding: 12px;
  }
}
</style>