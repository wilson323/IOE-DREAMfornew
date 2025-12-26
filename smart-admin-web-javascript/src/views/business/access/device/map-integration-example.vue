<!--
  * 门禁设备管理 - 地图集成示例
  *
  * 说明：此文件展示如何在设备管理页面中集成地图组件
  * 实际使用时需要将相关代码合并到 index.vue 中
-->
<template>
  <div class="access-device-page-with-map">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row">
          <a-form-item label="关键词" class="smart-query-form-item">
            <a-input
              style="width: 200px"
              v-model:value="queryForm.keyword"
              placeholder="设备名称、设备编号"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="区域" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.areaId"
              style="width: 150px"
              :allow-clear="true"
              placeholder="请选择区域"
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
          </a-form-item>

          <a-form-item label="设备状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择状态"
            >
              <a-select-option value="all">全部</a-select-option>
              <a-select-option value="online">在线</a-select-option>
              <a-select-option value="offline">离线</a-select-option>
              <a-select-option value="error">故障</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryDeviceList">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 视图切换 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-radio-group v-model:value="viewMode" button-style="solid">
          <a-radio-button value="table">
            <template #icon><TableOutlined /></template>
            表格视图
          </a-radio-button>
          <a-radio-button value="map">
            <template #icon><EnvironmentOutlined /></template>
            地图视图
          </a-radio-button>
        </a-radio-group>
      </template>

      <!-- 表格视图 -->
      <div v-show="viewMode === 'table'">
        <a-table
          :columns="columns"
          :data-source="tableData"
          :pagination="pagination"
          :loading="loading"
          row-key="deviceId"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-badge
                :status="getStatusBadge(record.status)"
                :text="getStatusText(record.status)"
              />
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
                <a-button type="link" size="small" @click="locateOnMap(record)">
                  <template #icon><EnvironmentOutlined /></template>
                  定位
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>

      <!-- 地图视图 -->
      <div v-show="viewMode === 'map'">
        <DeviceMap
          :devices="mapDevices"
          :areas="mapAreas"
          @device-click="handleMapDeviceClick"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  TableOutlined,
  EnvironmentOutlined,
  DownOutlined,
  PlusOutlined
} from '@ant-design/icons-vue';
import DeviceMap from '../components/DeviceMap.vue';
import { getDeviceList, getDeviceLocations, getAreaBoundaries } from '/@/api/access/access-api';

// 查询表单
const queryForm = ref({
  keyword: '',
  areaId: '',
  status: 'all'
});

// 视图模式
const viewMode = ref('table');

// 表格数据
const tableData = ref([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

// 地图数据
const mapDevices = ref([]);
const mapAreas = ref([]);

// 区域列表
const areas = ref([]);

// 表格列定义
const columns = [
  {
    title: '设备ID',
    dataIndex: 'deviceId',
    key: 'deviceId',
    width: 120
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150
  },
  {
    title: '设备类型',
    dataIndex: 'deviceType',
    key: 'deviceType',
    width: 120
  },
  {
    title: '所属区域',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 120
  },
  {
    title: '安装位置',
    dataIndex: 'installLocation',
    key: 'installLocation',
    width: 150
  },
  {
    title: '经度',
    dataIndex: 'longitude',
    key: 'longitude',
    width: 120
  },
  {
    title: '纬度',
    dataIndex: 'latitude',
    key: 'latitude',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 150
  }
];

// 方法：查询设备列表
const queryDeviceList = async () => {
  loading.value = true;
  try {
    const res = await getDeviceList({
      ...queryForm.value,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize
    });

    if (res.code === 200) {
      tableData.value = res.data.list || [];
      pagination.value.total = res.data.total || 0;
    }
  } catch (e) {
    message.error('查询失败: ' + e.message);
  } finally {
    loading.value = false;
  }
};

// 方法：重置查询
const resetQuery = () => {
  queryForm.value = {
    keyword: '',
    areaId: '',
    status: 'all'
  };
  pagination.value.current = 1;
  queryDeviceList();
};

// 方法：获取地图数据
const loadMapData = async () => {
  try {
    // 获取设备位置
    const deviceRes = await getDeviceLocations();
    if (deviceRes.code === 200) {
      mapDevices.value = deviceRes.data || [];
    }

    // 获取区域边界
    const areaRes = await getAreaBoundaries();
    if (areaRes.code === 200) {
      mapAreas.value = areaRes.data || [];
      areas.value = areaRes.data || [];
    }
  } catch (e) {
    message.error('加载地图数据失败: ' + e.message);
  }
};

// 方法：在地图上定位设备
const locateOnMap = (device) => {
  if (!device.longitude || !device.latitude) {
    message.warning('该设备未配置位置信息');
    return;
  }

  viewMode.value = 'map';
  message.success(`已定位到设备: ${device.deviceName}`);
};

// 方法：处理地图设备点击
const handleMapDeviceClick = (device) => {
  console.log('[地图] 点击设备', device);
  message.info(`选中设备: ${device.deviceName}`);
};

// 方法：获取状态徽标
const getStatusBadge = (status) => {
  const statusMap = {
    online: 'success',
    offline: 'default',
    error: 'error'
  };
  return statusMap[status] || 'default';
};

// 方法：获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    online: '在线',
    offline: '离线',
    error: '故障'
  };
  return statusMap[status] || '未知';
};

// 方法：查看详情
const handleView = (record) => {
  console.log('[设备管理] 查看详情', record);
  // 跳转到详情页面或打开弹窗
};

// 方法：表格变化
const handleTableChange = (pag) => {
  pagination.value.current = pag.current;
  queryDeviceList();
};

// 组件挂载
onMounted(() => {
  queryDeviceList();
  loadMapData();
});
</script>

<style scoped>
.access-device-page-with-map {
  padding: 16px;
}
</style>
