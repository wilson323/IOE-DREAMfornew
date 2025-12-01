<template>
  <div class="access-area-management">
    <!-- 页面标题和操作区 -->
    <a-card :bordered="false" class="page-header">
      <template #title>
        <a-space>
          <HomeOutlined />
          <span>门禁区域管理</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增区域
          </a-button>
          <a-button @click="handleExpandAll">
            <template #icon><ExpandOutlined /></template>
            {{ expandedAll ? '收起全部' : '展开全部' }}
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </template>
    </a-card>

    <!-- 区域树形表格 -->
    <a-card :bordered="false" class="tree-card">
      <template #title>
        <a-space>
          <apartment-outlined />
          <span>区域树形结构</span>
          <a-tag color="blue">共 {{ totalAreas }} 个区域</a-tag>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="areaTreeData"
        :row-key="record => record.areaId"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1200 }"
        :default-expand-all="expandedAll"
        v-model:expandedRowKeys="expandedRowKeys"
      >
        <!-- 区域名称 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'areaName'">
            <a-space>
              <apartment-outlined :style="{ color: getAreaTypeColor(record.areaType) }" />
              <span>{{ record.areaName }}</span>
              <a-tag v-if="record.isDefault" color="gold" size="small">默认</a-tag>
            </a-space>
          </template>

          <!-- 区域类型 -->
          <template v-else-if="column.key === 'areaType'">
            <a-tag :color="getAreaTypeColor(record.areaType)">
              {{ getAreaTypeLabel(record.areaType) }}
            </a-tag>
          </template>

          <!-- 状态 -->
          <template v-else-if="column.key === 'status'">
            <a-switch
              :checked="record.status === 1"
              :loading="record.statusLoading"
              @change="(checked) => handleStatusChange(record, checked)"
            />
          </template>

          <!-- 设备数量 -->
          <template v-else-if="column.key === 'deviceCount'">
            <a-space>
              <span>{{ record.deviceCount || 0 }}</span>
              <a-button
                type="link"
                size="small"
                @click="handleViewDevices(record)"
                v-if="record.deviceCount > 0"
              >
                查看
              </a-button>
            </a-space>
          </template>

          <!-- 创建时间 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handleAddChild(record)">
                新增子区域
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleDeviceConfig(record)"
                v-if="record.deviceCount > 0"
              >
                设备配置
              </a-button>
              <a-popconfirm
                title="确定要删除该区域吗？"
                :description="record.hasChildren ? '该区域包含子区域，删除后子区域也会被删除。' : '删除后不可恢复。'"
                @confirm="handleDelete(record)"
                ok-text="确定"
                cancel-text="取消"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 区域表单弹窗 -->
    <AreaFormModal
      v-model:visible="formVisible"
      :form-data="formData"
      :area-tree-options="areaTreeOptions"
      @success="handleFormSuccess"
    />

    <!-- 设备列表弹窗 -->
    <DeviceListModal
      v-model:visible="deviceModalVisible"
      :area-info="selectedArea"
      @refresh="handleRefresh"
    />

    <!-- 设备配置弹窗 -->
    <DeviceConfigModal
      v-model:visible="deviceConfigVisible"
      :area-info="selectedArea"
      @success="handleConfigSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  HomeOutlined,
  PlusOutlined,
  ExpandOutlined,
  ReloadOutlined,
  ApartmentOutlined
} from '@ant-design/icons-vue';
import { accessAreaApi } from '/@/api/business/access/area-api.js';
import AreaFormModal from './components/AreaFormModal.vue';
import DeviceListModal from './components/DeviceListModal.vue';
import DeviceConfigModal from './components/DeviceConfigModal.vue';
import { formatDateTime } from '/@/utils/format.js';

// 响应式数据
const loading = ref(false);
const expandedAll = ref(true);
const areaTreeData = ref([]);
const expandedRowKeys = ref([]);
const totalAreas = ref(0);
const formVisible = ref(false);
const deviceModalVisible = ref(false);
const deviceConfigVisible = ref(false);
const selectedArea = ref(null);
const formData = ref(null);
const areaTreeOptions = ref([]);

// 表格列定义
const columns = [
  {
    title: '区域名称',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 250,
    fixed: 'left'
  },
  {
    title: '区域编码',
    dataIndex: 'areaCode',
    key: 'areaCode',
    width: 150
  },
  {
    title: '区域类型',
    dataIndex: 'areaType',
    key: 'areaType',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '设备数量',
    dataIndex: 'deviceCount',
    key: 'deviceCount',
    width: 120,
    align: 'center'
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    width: 200,
    ellipsis: true
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 250,
    fixed: 'right'
  }
];

// 区域类型相关方法
const getAreaTypeColor = (type) => {
  const colorMap = {
    'BUILDING': 'blue',
    'FLOOR': 'green',
    'ROOM': 'orange',
    'AREA': 'purple'
  };
  return colorMap[type] || 'default';
};

const getAreaTypeLabel = (type) => {
  const labelMap = {
    'BUILDING': '建筑',
    'FLOOR': '楼层',
    'ROOM': '房间',
    'AREA': '区域'
  };
  return labelMap[type] || '未知';
};

// 加载区域树数据
const loadAreaTree = async () => {
  loading.value = true;
  try {
    const response = await accessAreaApi.getAreaTree();
    if (response && response.data) {
      areaTreeData.value = response.data;
      totalAreas.value = countTotalAreas(response.data);

      // 默认展开所有节点
      if (expandedAll.value) {
        expandedRowKeys.value = getAllAreaIds(response.data);
      }
    }
  } catch (error) {
    console.error('加载区域树失败:', error);
    message.error('加载区域树数据失败');
  } finally {
    loading.value = false;
  }
};

// 递归统计区域总数
const countTotalAreas = (areas) => {
  let count = 0;
  areas.forEach(area => {
    count += 1;
    if (area.children && area.children.length > 0) {
      count += countTotalAreas(area.children);
    }
  });
  return count;
};

// 递归获取所有区域ID
const getAllAreaIds = (areas) => {
  const ids = [];
  areas.forEach(area => {
    ids.push(area.areaId);
    if (area.children && area.children.length > 0) {
      ids.push(...getAllAreaIds(area.children));
    }
  });
  return ids;
};

// 展开/收起全部
const handleExpandAll = () => {
  expandedAll.value = !expandedAll.value;
  if (expandedAll.value) {
    expandedRowKeys.value = getAllAreaIds(areaTreeData.value);
  } else {
    expandedRowKeys.value = [];
  }
};

// 刷新
const handleRefresh = () => {
  loadAreaTree();
};

// 新增区域
const handleAdd = () => {
  formData.value = {
    areaId: null,
    areaName: '',
    areaCode: '',
    areaType: 'AREA',
    parentAreaId: null,
    description: '',
    status: 1
  };
  formVisible.value = true;
};

// 新增子区域
const handleAddChild = (record) => {
  formData.value = {
    areaId: null,
    areaName: '',
    areaCode: '',
    areaType: 'AREA',
    parentAreaId: record.areaId,
    description: '',
    status: 1
  };
  formVisible.value = true;
};

// 编辑区域
const handleEdit = (record) => {
  formData.value = { ...record };
  formVisible.value = true;
};

// 删除区域
const handleDelete = async (record) => {
  try {
    const response = await accessAreaApi.deleteArea(record.areaId);
    if (response && response.success) {
      message.success('删除成功');
      handleRefresh();
    } else {
      message.error(response?.message || '删除失败');
    }
  } catch (error) {
    console.error('删除区域失败:', error);
    message.error('删除失败');
  }
};

// 状态变更
const handleStatusChange = async (record, checked) => {
  record.statusLoading = true;
  try {
    const response = await accessAreaApi.updateAreaStatus(record.areaId, checked ? 1 : 0);
    if (response && response.success) {
      record.status = checked ? 1 : 0;
      message.success(checked ? '启用成功' : '禁用成功');
    } else {
      message.error(response?.message || '状态更新失败');
    }
  } catch (error) {
    console.error('状态更新失败:', error);
    message.error('状态更新失败');
  } finally {
    record.statusLoading = false;
  }
};

// 查看设备
const handleViewDevices = (record) => {
  selectedArea.value = record;
  deviceModalVisible.value = true;
};

// 设备配置
const handleDeviceConfig = (record) => {
  selectedArea.value = record;
  deviceConfigVisible.value = true;
};

// 表单成功回调
const handleFormSuccess = () => {
  formVisible.value = false;
  handleRefresh();
};

// 配置成功回调
const handleConfigSuccess = () => {
  deviceConfigVisible.value = false;
  handleRefresh();
};

// 加载区域选项（用于父级选择）
const loadAreaOptions = async () => {
  try {
    const response = await accessAreaApi.getAreaTree();
    if (response && response.data) {
      areaTreeOptions.value = buildAreaTreeOptions(response.data);
    }
  } catch (error) {
    console.error('加载区域选项失败:', error);
  }
};

// 构建区域树形选项
const buildAreaTreeOptions = (areas) => {
  return areas.map(area => ({
    value: area.areaId,
    label: area.areaName,
    key: area.areaId,
    children: area.children ? buildAreaTreeOptions(area.children) : undefined
  }));
};

// 生命周期
onMounted(() => {
  loadAreaTree();
  loadAreaOptions();
});
</script>

<style lang="less" scoped>
.access-area-management {
  .page-header {
    margin-bottom: 16px;
  }

  .tree-card {
    min-height: 500px;
  }

  :deep(.ant-table-cell) {
    .ant-btn-link {
      padding: 0;
    }
  }

  :deep(.ant-switch) {
    min-width: 44px;
  }
}
</style>