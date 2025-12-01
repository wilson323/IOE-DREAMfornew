<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="地理围栏管理"
    width="1000px"
    :footer="null"
  >
    <div class="geo-fence-modal">
      <!-- 围栏列表 -->
      <div class="fence-list">
        <a-table
          :columns="fenceColumns"
          :data-source="geoFenceList"
          :loading="loading"
          size="small"
          row-key="fenceId"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
                {{ record.status === 'ACTIVE' ? '启用' : '禁用' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'fenceType'">
              <a-tag>{{ getFenceTypeLabel(record.fenceType) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'triggerType'">
              <a-tag>{{ getTriggerTypeLabel(record.triggerType) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'alertLevel'">
              <a-tag :color="getAlertLevelColor(record.alertLevel)">
                {{ getAlertLevelLabel(record.alertLevel) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="editGeoFence(record)">
                  编辑
                </a-button>
                <a-button type="link" size="small" @click="viewOnMap(record)">
                  地图
                </a-button>
                <a-popconfirm
                  title="确定要删除这个地理围栏吗？"
                  @confirm="deleteGeoFence(record.fenceId)"
                >
                  <a-button type="link" size="small" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>

        <div class="add-button">
          <a-button type="dashed" @click="showCreateForm" block>
            <template #icon><PlusOutlined /></template>
            添加地理围栏
          </a-button>
        </div>
      </div>

      <!-- 地图区域 -->
      <div class="map-section">
        <div class="map-container">
          <div id="geo-fence-map" class="map"></div>
        </div>
      </div>

      <!-- 创建/编辑表单 -->
      <a-modal
        v-model:open="formVisible"
        :title="editingFence ? '编辑地理围栏' : '创建地理围栏'"
        @ok="handleSubmit"
        @cancel="cancelEdit"
        width="600px"
      >
        <a-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          layout="vertical"
        >
          <a-form-item label="围栏名称" name="fenceName">
            <a-input :value="formData.fenceName" @update:value="val => emit('update:value', val)" placeholder="请输入围栏名称" />
          </a-form-item>

          <a-form-item label="围栏描述" name="description">
            <a-textarea :value="formData.description" @update:value="val => emit('update:value', val)" :rows="2" placeholder="请输入围栏描述" />
          </a-form-item>

          <a-form-item label="围栏类型" name="fenceType">
            <a-select :value="formData.fenceType" @update:value="val => emit('update:value', val)" @change="handleFenceTypeChange">
              <a-select-option value="CIRCULAR">圆形围栏</a-select-option>
              <a-select-option value="POLYGON">多边形围栏</a-select-option>
              <a-select-option value="RECTANGLE">矩形围栏</a-select-option>
            </a-select>
          </a-form-item>

          <!-- 圆形围栏参数 -->
          <template v-if="formData.fenceType === 'CIRCULAR'">
            <a-form-item label="中心坐标" name="centerCoordinates">
              <a-input-group compact>
                <a-input-number
                  :value="formData.centerLatitude" @update:value="val => emit('update:value', val)"
                  placeholder="纬度"
                  style="width: 50%"
                  :precision="6"
                />
                <a-input-number
                  :value="formData.centerLongitude" @update:value="val => emit('update:value', val)"
                  placeholder="经度"
                  style="width: 50%"
                  :precision="6"
                />
              </a-input-group>
            </a-form-item>

            <a-form-item label="半径(米)" name="radius">
              <a-input-number
                :value="formData.radius" @update:value="val => emit('update:value', val)"
                placeholder="请输入半径"
                :min="1"
                :max="10000"
                style="width: 100%"
              />
            </a-form-item>
          </template>

          <!-- 矩形围栏参数 -->
          <template v-if="formData.fenceType === 'RECTANGLE'">
            <a-form-item label="左上角坐标" name="topLeftCoordinates">
              <a-input-group compact>
                <a-input-number
                  :value="formData.maxLatitude" @update:value="val => emit('update:value', val)"
                  placeholder="纬度"
                  style="width: 50%"
                  :precision="6"
                />
                <a-input-number
                  :value="formData.minLongitude" @update:value="val => emit('update:value', val)"
                  placeholder="经度"
                  style="width: 50%"
                  :precision="6"
                />
              </a-input-group>
            </a-form-item>

            <a-form-item label="右下角坐标" name="bottomRightCoordinates">
              <a-input-group compact>
                <a-input-number
                  :value="formData.minLatitude" @update:value="val => emit('update:value', val)"
                  placeholder="纬度"
                  style="width: 50%"
                  :precision="6"
                />
                <a-input-number
                  :value="formData.maxLongitude" @update:value="val => emit('update:value', val)"
                  placeholder="经度"
                  style="width: 50%"
                  :precision="6"
                />
              </a-input-group>
            </a-form-item>
          </template>

          <!-- 多边形围栏参数 -->
          <template v-if="formData.fenceType === 'POLYGON'">
            <a-form-item label="顶点坐标">
              <div class="polygon-coordinates">
                <div
                  v-for="(point, index) in polygonPoints"
                  :key="index"
                  class="coordinate-item"
                >
                  <a-input-group compact>
                    <a-input-number
                      :value="point.lat" @update:value="val => emit('update:value', val)"
                      placeholder="纬度"
                      style="width: 40%"
                      :precision="6"
                    />
                    <a-input-number
                      :value="point.lng" @update:value="val => emit('update:value', val)"
                      placeholder="经度"
                      style="width: 40%"
                      :precision="6"
                    />
                    <a-button
                      @click="removePolygonPoint(index)"
                      danger
                      style="width: 20%"
                    >
                      删除
                    </a-button>
                  </a-input-group>
                </div>
                <a-button @click="addPolygonPoint" type="dashed" block>
                  <template #icon><PlusOutlined /></template>
                  添加顶点
                </a-button>
              </div>
            </a-form-item>
          </template>

          <a-form-item label="触发类型" name="triggerType">
            <a-select :value="formData.triggerType" @update:value="val => emit('update:value', val)">
              <a-select-option value="IN">进入时触发</a-select-option>
              <a-select-option value="OUT">离开时触发</a-select-option>
              <a-select-option value="BOTH">进入和离开都触发</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="告警等级" name="alertLevel">
            <a-select :value="formData.alertLevel" @update:value="val => emit('update:value', val)">
              <a-select-option value="LOW">低</a-select-option>
              <a-select-option value="MEDIUM">中</a-select-option>
              <a-select-option value="HIGH">高</a-select-option>
              <a-select-option value="CRITICAL">紧急</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="通知方式" name="notificationMethods">
            <a-checkbox-group :value="notificationMethods" @update:value="val => emit('update:value', val)">
              <a-checkbox value="SMS">短信</a-checkbox>
              <a-checkbox value="EMAIL">邮件</a-checkbox>
              <a-checkbox value="APP">应用推送</a-checkbox>
              <a-checkbox value="WECHAT">微信</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="生效时间">
            <a-range-picker
              :value="formData.activeTimeRange" @update:value="val => emit('update:value', val)"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
            />
          </a-form-item>
        </a-form>
      </a-modal>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch, onMounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import LocationApi from '/@/api/location/LocationApi';
import { initMap, addCircle, addPolygon, addRectangle } from '/@/utils/MapUtils';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['update:visible', 'success']);

// 响应式数据
const loading = ref(false);
const formVisible = ref(false);
const editingFence = ref(null);
const mapInstance = ref(null);
const formRef = ref(null);
const polygonPoints = ref([{ lat: null, lng: null }]);

// 地理围栏列表
const geoFenceList = ref([]);

// 表单数据
const formData = reactive({
  fenceName: '',
  description: '',
  fenceType: 'CIRCULAR',
  centerLatitude: null,
  centerLongitude: null,
  radius: null,
  minLatitude: null,
  maxLatitude: null,
  minLongitude: null,
  maxLongitude: null,
  polygonCoordinates: '',
  triggerType: 'BOTH',
  alertLevel: 'MEDIUM',
  notificationMethods: [],
  activeTimeRange: null
});

const notificationMethods = ref([]);

// 表格列定义
const fenceColumns = [
  {
    title: '围栏名称',
    dataIndex: 'fenceName',
    key: 'fenceName',
    width: 120
  },
  {
    title: '类型',
    dataIndex: 'fenceType',
    key: 'fenceType',
    width: 80
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 60
  },
  {
    title: '触发类型',
    dataIndex: 'triggerType',
    key: 'triggerType',
    width: 80
  },
  {
    title: '告警等级',
    dataIndex: 'alertLevel',
    key: 'alertLevel',
    width: 80
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
];

// 表单验证规则
const formRules = {
  fenceName: [
    { required: true, message: '请输入围栏名称', trigger: 'blur' }
  ],
  fenceType: [
    { required: true, message: '请选择围栏类型', trigger: 'change' }
  ],
  centerLatitude: [
    { required: true, message: '请输入中心纬度', trigger: 'blur' },
    { type: 'number', min: -90, max: 90, message: '纬度必须在-90到90之间', trigger: 'blur' }
  ],
  centerLongitude: [
    { required: true, message: '请输入中心经度', trigger: 'blur' },
    { type: 'number', min: -180, max: 180, message: '经度必须在-180到180之间', trigger: 'blur' }
  ],
  radius: [
    { required: true, message: '请输入半径', trigger: 'blur' },
    { type: 'number', min: 1, message: '半径必须大于0', trigger: 'blur' }
  ]
};

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

// 获取围栏类型标签
const getFenceTypeLabel = (type) => {
  const typeMap = {
    'CIRCULAR': '圆形',
    'POLYGON': '多边形',
    'RECTANGLE': '矩形'
  };
  return typeMap[type] || type;
};

// 获取触发类型标签
const getTriggerTypeLabel = (type) => {
  const typeMap = {
    'IN': '进入',
    'OUT': '离开',
    'BOTH': '双向'
  };
  return typeMap[type] || type;
};

// 获取告警等级颜色
const getAlertLevelColor = (level) => {
  const colorMap = {
    'LOW': 'green',
    'MEDIUM': 'orange',
    'HIGH': 'red',
    'CRITICAL': 'purple'
  };
  return colorMap[level] || 'default';
};

// 获取告警等级标签
const getAlertLevelLabel = (level) => {
  const levelMap = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'CRITICAL': '紧急'
  };
  return levelMap[level] || level;
};

// 初始化地图
const initGeoFenceMap = () => {
  nextTick(() => {
    try {
      mapInstance.value = initMap('geo-fence-map', {
        center: [39.9042, 116.4074],
        zoom: 12
      });

      // 地图点击事件，用于设置围栏坐标
      mapInstance.value.on('click', (e) => {
        if (formVisible.value) {
          handleMapClick(e.latlng);
        }
      });
    } catch (error) {
      console.error('初始化地理围栏地图失败:', error);
    }
  });
};

// 处理地图点击
const handleMapClick = (latlng) => {
  if (formData.fenceType === 'CIRCULAR') {
    formData.centerLatitude = latlng.lat;
    formData.centerLongitude = latlng.lng;
    if (!formData.radius) {
      formData.radius = 500; // 默认半径500米
    }
    previewFence();
  } else if (formData.fenceType === 'POLYGON') {
    // 添加多边形顶点
    polygonPoints.value.push({ lat: latlng.lat, lng: latlng.lng });
    previewFence();
  }
};

// 预览围栏
const previewFence = () => {
  if (!mapInstance.value) return;

  // 清除现有预览
  // mapInstance.value.clearLayers();

  switch (formData.fenceType) {
    case 'CIRCULAR':
      if (formData.centerLatitude && formData.centerLongitude && formData.radius) {
        addCircle(mapInstance.value, [formData.centerLatitude, formData.centerLongitude], formData.radius, {
          color: 'blue',
          fillOpacity: 0.2
        });
      }
      break;
    case 'POLYGON':
      if (polygonPoints.value.length >= 3) {
        const coordinates = polygonPoints.value.filter(p => p.lat && p.lng).map(p => [p.lat, p.lng]);
        if (coordinates.length >= 3) {
          addPolygon(mapInstance.value, coordinates, {
            color: 'green',
            fillOpacity: 0.2
          });
        }
      }
      break;
    case 'RECTANGLE':
      if (formData.minLatitude && formData.maxLatitude && formData.minLongitude && formData.maxLongitude) {
        addRectangle(mapInstance.value, [
          [formData.minLatitude, formData.minLongitude],
          [formData.maxLatitude, formData.maxLongitude]
        ], {
          color: 'red',
          fillOpacity: 0.2
        });
      }
      break;
  }
};

// 围栏类型变化处理
const handleFenceTypeChange = (value) => {
  // 重置相关参数
  polygonPoints.value = [{ lat: null, lng: null }];
  previewFence();
};

// 添加多边形顶点
const addPolygonPoint = () => {
  polygonPoints.value.push({ lat: null, lng: null });
};

// 删除多边形顶点
const removePolygonPoint = (index) => {
  if (polygonPoints.value.length > 3) {
    polygonPoints.value.splice(index, 1);
    previewFence();
  } else {
    message.warning('多边形至少需要3个顶点');
  }
};

// 加载地理围栏列表
const loadGeoFenceList = async () => {
  loading.value = true;
  try {
    const response = await LocationApi.getGeoFenceList();
    if (response.success) {
      geoFenceList.value = response.data || [];
      // 在地图上显示围栏
      updateFencesOnMap();
    } else {
      message.error(response.message || '获取地理围栏列表失败');
    }
  } catch (error) {
    console.error('加载地理围栏列表失败:', error);
    message.error('加载地理围栏列表失败');
  } finally {
    loading.value = false;
  }
};

// 在地图上更新围栏显示
const updateFencesOnMap = () => {
  if (!mapInstance.value || !geoFenceList.value.length) return;

  geoFenceList.value.forEach(fence => {
    if (fence.status !== 'ACTIVE') return;

    switch (fence.fenceType) {
      case 'CIRCULAR':
        if (fence.centerLatitude && fence.centerLongitude && fence.radius) {
          addCircle(mapInstance.value, [fence.centerLatitude, fence.centerLongitude], fence.radius, {
            color: fence.alertLevel === 'CRITICAL' ? 'red' : 'blue',
            fillOpacity: 0.1,
            weight: 2
          });
        }
        break;
      case 'POLYGON':
        if (fence.polygonCoordinates) {
          try {
            const coordinates = JSON.parse(fence.polygonCoordinates);
            if (Array.isArray(coordinates) && coordinates.length >= 3) {
              addPolygon(mapInstance.value, coordinates, {
                color: fence.alertLevel === 'CRITICAL' ? 'red' : 'green',
                fillOpacity: 0.1,
                weight: 2
              });
            }
          } catch (error) {
            console.error('解析多边形坐标失败:', error);
          }
        }
        break;
      case 'RECTANGLE':
        if (fence.minLatitude && fence.maxLatitude && fence.minLongitude && fence.maxLongitude) {
          addRectangle(mapInstance.value, [
            [fence.minLatitude, fence.minLongitude],
            [fence.maxLatitude, fence.maxLongitude]
          ], {
            color: fence.alertLevel === 'CRITICAL' ? 'red' : 'orange',
            fillOpacity: 0.1,
            weight: 2
          });
        }
        break;
    }
  });
};

// 显示创建表单
const showCreateForm = () => {
  editingFence.value = null;
  resetForm();
  formVisible.value = true;
};

// 编辑地理围栏
const editGeoFence = (fence) => {
  editingFence.value = fence;
  Object.assign(formData, {
    fenceName: fence.fenceName,
    description: fence.description,
    fenceType: fence.fenceType,
    centerLatitude: fence.centerLatitude,
    centerLongitude: fence.centerLongitude,
    radius: fence.radius,
    minLatitude: fence.minLatitude,
    maxLatitude: fence.maxLatitude,
    minLongitude: fence.minLongitude,
    maxLongitude: fence.maxLongitude,
    triggerType: fence.triggerType,
    alertLevel: fence.alertLevel,
    notificationMethods: fence.notificationMethods ? fence.notificationMethods.split(',') : []
  });

  // 设置通知方式
  notificationMethods.value = formData.notificationMethods;

  // 设置多边形坐标
  if (fence.fenceType === 'POLYGON' && fence.polygonCoordinates) {
    try {
      const coordinates = JSON.parse(fence.polygonCoordinates);
      polygonPoints.value = coordinates.map(coord => ({ lat: coord[0], lng: coord[1] }));
    } catch (error) {
      polygonPoints.value = [{ lat: null, lng: null }];
    }
  }

  formVisible.value = true;
};

// 在地图上查看围栏
const viewOnMap = (fence) => {
  if (!mapInstance.value) return;

  switch (fence.fenceType) {
    case 'CIRCULAR':
      if (fence.centerLatitude && fence.centerLongitude) {
        mapInstance.value.setView([fence.centerLatitude, fence.centerLongitude], 15);
      }
      break;
    case 'POLYGON':
      // 计算多边形中心点
      if (fence.polygonCoordinates) {
        try {
          const coordinates = JSON.parse(fence.polygonCoordinates);
          if (coordinates.length > 0) {
            const centerLat = coordinates.reduce((sum, coord) => sum + coord[0], 0) / coordinates.length;
            const centerLng = coordinates.reduce((sum, coord) => sum + coord[1], 0) / coordinates.length;
            mapInstance.value.setView([centerLat, centerLng], 13);
          }
        } catch (error) {
          console.error('计算多边形中心失败:', error);
        }
      }
      break;
    case 'RECTANGLE':
      if (fence.minLatitude && fence.minLongitude) {
        mapInstance.value.setView([fence.minLatitude, fence.minLongitude], 13);
      }
      break;
  }
};

// 删除地理围栏
const deleteGeoFence = async (fenceId) => {
  try {
    const response = await LocationApi.deleteGeoFence(fenceId);
    if (response.success) {
      message.success('删除成功');
      loadGeoFenceList();
    } else {
      message.error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除地理围栏失败:', error);
    message.error('删除地理围栏失败');
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    // 构建提交数据
    const submitData = { ...formData };

    // 处理多边形坐标
    if (formData.fenceType === 'POLYGON') {
      const validPoints = polygonPoints.value.filter(p => p.lat != null && p.lng != null);
      if (validPoints.length < 3) {
        message.error('多边形至少需要3个有效顶点');
        return;
      }
      submitData.polygonCoordinates = JSON.stringify(validPoints.map(p => [p.lat, p.lng]));
    }

    // 处理通知方式
    submitData.notificationMethods = notificationMethods.value.join(',');

    // 处理生效时间
    if (formData.activeTimeRange && formData.activeTimeRange.length === 2) {
      submitData.activeTimeStart = formData.activeTimeRange[0].toISOString();
      submitData.activeTimeEnd = formData.activeTimeRange[1].toISOString();
    }

    let response;
    if (editingFence.value) {
      submitData.fenceId = editingFence.value.fenceId;
      response = await LocationApi.updateGeoFence(submitData);
    } else {
      response = await LocationApi.createGeoFence(submitData);
    }

    if (response.success) {
      message.success(editingFence.value ? '更新成功' : '创建成功');
      formVisible.value = false;
      loadGeoFenceList();
      emit('success', response.data);
    } else {
      message.error(response.message || (editingFence.value ? '更新失败' : '创建失败'));
    }
  } catch (error) {
    console.error('提交地理围栏表单失败:', error);
    message.error('提交失败，请检查表单数据');
  }
};

// 取消编辑
const cancelEdit = () => {
  formVisible.value = false;
  resetForm();
};

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    fenceName: '',
    description: '',
    fenceType: 'CIRCULAR',
    centerLatitude: null,
    centerLongitude: null,
    radius: null,
    minLatitude: null,
    maxLatitude: null,
    minLongitude: null,
    maxLongitude: null,
    polygonCoordinates: '',
    triggerType: 'BOTH',
    alertLevel: 'MEDIUM',
    notificationMethods: [],
    activeTimeRange: null
  });
  notificationMethods.value = [];
  polygonPoints.value = [{ lat: null, lng: null }];
  editingFence.value = null;

  // 清除表单验证
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadGeoFenceList();
    nextTick(() => {
      initGeoFenceMap();
    });
  }
});
</script>

<style scoped>
.geo-fence-modal {
  display: flex;
  gap: 16px;
}

.fence-list {
  flex: 1;
  min-height: 400px;
}

.map-section {
  width: 500px;
}

.map-container {
  height: 400px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100%;
}

.add-button {
  margin-top: 16px;
}

.polygon-coordinates {
  max-height: 300px;
  overflow-y: auto;
}

.coordinate-item {
  margin-bottom: 8px;
}
</style>