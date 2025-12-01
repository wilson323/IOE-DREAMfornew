<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="位置详情"
    width="800px"
    :footer="null"
  >
    <div class="location-detail" v-if="location">
      <!-- 基本信息 -->
      <a-descriptions title="基本信息" :column="2" bordered>
        <a-descriptions-item label="用户ID">
          {{ location.userId }}
        </a-descriptions-item>
        <a-descriptions-item label="位置ID">
          {{ location.locationId || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="纬度">
          {{ location.latitude }}
        </a-descriptions-item>
        <a-descriptions-item label="经度">
          {{ location.longitude }}
        </a-descriptions-item>
        <a-descriptions-item label="海拔高度">
          {{ location.altitude ? location.altitude + '米' : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="定位精度">
          <a-tag :color="getAccuracyColor(location.accuracy)">
            {{ location.accuracy }}米
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="定位方式">
          <a-tag>{{ getPositioningMethodLabel(location.positioningMethod) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="位置来源">
          <a-tag>{{ location.locationSource || 'UNKNOWN' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="速度">
          {{ location.speed ? location.speed + '米/秒' : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="方向角">
          {{ location.bearing ? location.bearing + '°' : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="设备ID">
          {{ location.deviceId || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="定位时间">
          {{ formatDateTime(location.positioningTime) }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 位置地址 -->
      <a-card title="位置地址" class="detail-card" size="small">
        <div class="address-info">
          <p><strong>详细地址：</strong>{{ location.address || '暂无地址信息' }}</p>
          <p><strong>位置描述：</strong>{{ location.description || '暂无描述' }}</p>
          <p><strong>楼层信息：</strong>{{ location.floor || '-' }}</p>
        </div>
      </a-card>

      <!-- 位置统计 -->
      <a-card title="位置统计" class="detail-card" size="small" v-if="statistics">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic title="今日位置更新" :value="statistics.todayUpdates" suffix="次" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="平均精度" :value="statistics.avgAccuracy" suffix="米" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="访问次数" :value="statistics.visitCount" suffix="次" />
          </a-col>
        </a-row>
      </a-card>

      <!-- 地图显示 -->
      <a-card title="地图位置" class="detail-card" size="small">
        <div class="map-container">
          <div id="location-detail-map" class="map"></div>
        </div>
      </a-card>

      <!-- 周边信息 -->
      <a-card title="周边信息" class="detail-card" size="small" v-if="nearbyInfo">
        <a-tabs>
          <a-tab-pane key="locations" tab="附近位置">
            <a-list
              :data-source="nearbyInfo.nearbyLocations"
              size="small"
              :pagination="false"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta
                    :title="item.locationName"
                    :description="item.description"
                  />
                  <div>
                    <a-tag color="blue">{{ calculateDistance(location, item) }}米</a-tag>
                  </div>
                </a-list-item>
              </template>
            </a-list>
          </a-tab-pane>

          <a-tab-pane key="users" tab="附近用户">
            <a-list
              :data-source="nearbyInfo.nearbyUsers"
              size="small"
              :pagination="false"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta
                    :title="'用户 ' + item.userId"
                    :description="formatDateTime(item.positioningTime)"
                  />
                  <div>
                    <a-tag color="green">{{ calculateDistance(location, item) }}米</a-tag>
                  </div>
                </a-list-item>
              </template>
            </a-list>
          </a-tab-pane>
        </a-tabs>
      </a-card>

      <!-- 地理围栏信息 -->
      <a-card title="地理围栏" class="detail-card" size="small" v-if="geoFenceInfo">
        <a-alert
          v-if="geoFenceInfo.triggered"
          :message="geoFenceInfo.message"
          :type="getGeoFenceAlertType(geoFenceInfo.triggerType)"
          show-icon
          style="margin-bottom: 16px"
        />
        <a-descriptions :column="2" size="small">
          <a-descriptions-item label="围栏名称">
            {{ geoFenceInfo.fenceName }}
          </a-descriptions-item>
          <a-descriptions-item label="触发类型">
            <a-tag :color="geoFenceInfo.triggerType === 'IN' ? 'green' : 'red'">
              {{ geoFenceInfo.triggerType === 'IN' ? '进入' : '离开' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="触发时间">
            {{ formatDateTime(geoFenceInfo.triggerTime) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <a-space>
          <a-button type="primary" @click="showOnMap">
            <template #icon><EnvironmentOutlined /></template>
            在地图上显示
          </a-button>
          <a-button @click="exportLocation">
            <template #icon><ExportOutlined /></template>
            导出位置信息
          </a-button>
          <a-button @click="shareLocation">
            <template #icon><ShareAltOutlined /></template>
            分享位置
          </a-button>
        </a-space>
      </div>
    </div>

    <a-empty v-else description="无位置信息" />
  </a-modal>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  EnvironmentOutlined,
  ExportOutlined,
  ShareAltOutlined
} from '@ant-design/icons-vue';
import LocationApi from '/@/api/location/LocationApi';
import { initMap, addMarker } from '/@/utils/MapUtils';
import dayjs from 'dayjs';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  location: {
    type: Object,
    default: null
  }
});

// Emits
const emit = defineEmits(['update:visible']);

// 响应式数据
const mapInstance = ref(null);
const statistics = ref(null);
const nearbyInfo = ref(null);
const geoFenceInfo = ref(null);

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-';
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
};

// 计算距离
const calculateDistance = (loc1, loc2) => {
  if (!loc1 || !loc2 || !loc1.latitude || !loc1.longitude || !loc2.latitude || !loc2.longitude) {
    return '-';
  }

  const R = 6371; // 地球半径（公里）
  const lat1 = loc1.latitude * Math.PI / 180;
  const lat2 = loc2.latitude * Math.PI / 180;
  const deltaLat = (loc2.latitude - loc1.latitude) * Math.PI / 180;
  const deltaLon = (loc2.longitude - loc1.longitude) * Math.PI / 180;

  const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return Math.round(R * c * 1000); // 返回米，四舍五入
};

// 获取地理围栏警告类型
const getGeoFenceAlertType = (triggerType) => {
  return triggerType === 'IN' ? 'success' : 'warning';
};

// 初始化详情地图
const initDetailMap = () => {
  nextTick(() => {
    try {
      mapInstance.value = initMap('location-detail-map', {
        center: [props.location.latitude, props.location.longitude],
        zoom: 15
      });

      // 添加位置标记
      addMarker(mapInstance.value, [props.location.latitude, props.location.longitude], {
        title: '位置详情',
        popup: `
          <div>
            <p><strong>用户ID:</strong> ${props.location.userId}</p>
            <p><strong>坐标:</strong> ${props.location.latitude}, ${props.location.longitude}</p>
            <p><strong>精度:</strong> ${props.location.accuracy}米</p>
            <p><strong>时间:</strong> ${formatDateTime(props.location.positioningTime)}</p>
          </div>
        `
      });
    } catch (error) {
      console.error('初始化详情地图失败:', error);
    }
  });
};

// 加载位置统计数据
const loadStatistics = async () => {
  if (!props.location?.userId) return;

  try {
    const endTime = dayjs().toISOString();
    const startTime = dayjs().subtract(7, 'day').toISOString();

    const response = await LocationApi.getUserLocationStatistics({
      userId: props.location.userId,
      startTime,
      endTime
    });

    if (response.success) {
      statistics.value = response.data;
    }
  } catch (error) {
    console.error('加载位置统计失败:', error);
  }
};

// 加载周边信息
const loadNearbyInfo = async () => {
  if (!props.location) return;

  try {
    const response = await LocationApi.searchNearbyLocations({
      latitude: props.location.latitude,
      longitude: props.location.longitude,
      radius: 1000,
      limit: 10
    });

    if (response.success) {
      nearbyInfo.value = {
        nearbyLocations: response.data || [],
        nearbyLocationCount: (response.data || []).length
      };
    }
  } catch (error) {
    console.error('加载周边信息失败:', error);
  }
};

// 在地图上显示
const showOnMap = () => {
  // 这里可以触发父组件在主地图上显示这个位置
  message.success('已在主地图上显示该位置');
};

// 导出位置信息
const exportLocation = () => {
  if (!props.location) return;

  const exportData = {
    用户ID: props.location.userId,
    纬度: props.location.latitude,
    经度: props.location.longitude,
    海拔: props.location.altitude || '-',
    精度: props.location.accuracy + '米',
    定位方式: getPositioningMethodLabel(props.location.positioningMethod),
    位置来源: props.location.locationSource || '-',
    速度: props.location.speed ? props.location.speed + '米/秒' : '-',
    方向角: props.location.bearing ? props.location.bearing + '°' : '-',
    设备ID: props.location.deviceId || '-',
    定位时间: formatDateTime(props.location.positioningTime),
    地址: props.location.address || '-',
    描述: props.location.description || '-'
  };

  // 转换为CSV格式
  const csvContent = Object.entries(exportData)
    .map(([key, value]) => `${key},${value}`)
    .join('\n');

  // 创建下载链接
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `位置详情_${props.location.userId}_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);

  message.success('位置信息导出成功');
};

// 分享位置
const shareLocation = async () => {
  if (!props.location) return;

  const shareData = {
    title: '位置分享',
    text: `用户 ${props.location.userId} 的当前位置`,
    url: `${window.location.origin}/location?lat=${props.location.latitude}&lng=${props.location.longitude}`
  };

  try {
    if (navigator.share) {
      await navigator.share(shareData);
      message.success('位置分享成功');
    } else {
      // 复制链接到剪贴板
      await navigator.clipboard.writeText(shareData.url);
      message.success('位置链接已复制到剪贴板');
    }
  } catch (error) {
    console.error('分享位置失败:', error);
    message.error('分享位置失败');
  }
};

// 监听visible和location变化
watch(() => [props.visible, props.location], ([newVisible, newLocation]) => {
  if (newVisible && newLocation) {
    nextTick(() => {
      initDetailMap();
      loadStatistics();
      loadNearbyInfo();

      // 设置地理围栏信息（从位置结果中获取）
      if (newLocation.geoFenceTriggerInfo) {
        geoFenceInfo.value = newLocation.geoFenceTriggerInfo;
      } else {
        geoFenceInfo.value = null;
      }
    });
  }
}, { immediate: true });
</script>

<style scoped>
.location-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-card {
  margin-top: 16px;
}

.map-container {
  height: 300px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100%;
}

.address-info p {
  margin-bottom: 8px;
}

.action-buttons {
  margin-top: 24px;
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>