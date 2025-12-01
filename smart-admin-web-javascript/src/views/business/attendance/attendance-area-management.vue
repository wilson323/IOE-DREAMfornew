<!--
 * 考勤区域配置管理页面
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <div class="attendance-area-management">
    <!-- 页面头部 -->
    <a-page-header
      title="考勤区域配置管理"
      sub-title="配置考勤区域相关参数和规则"
      @back="handleBack"
    >
      <template #extra>
        <a-space>
          <a-button @click="handleExportConfig">
            <template #icon><ExportOutlined /></template>
            导出配置
          </a-button>
          <a-button @click="handleImportConfig">
            <template #icon><ImportOutlined /></template>
            导入配置
          </a-button>
          <a-button type="primary" @click="handleAddArea">
            <template #icon><PlusOutlined /></template>
            新增区域配置
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <!-- 搜索和筛选 -->
    <a-card :bordered="false" class="search-card mb-4">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="区域名称">
          <a-input
            v-model:value="queryForm.areaName"
            placeholder="请输入区域名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="区域类型">
          <a-select
            v-model:value="queryForm.areaType"
            placeholder="请选择区域类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="OFFICE">办公区域</a-select-option>
            <a-select-option value="FACTORY">工厂区域</a-select-option>
            <a-select-option value="WAREHOUSE">仓库区域</a-select-option>
            <a-select-option value="OUTDOOR">户外区域</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 主要内容区域 -->
    <a-row :gutter="16">
      <!-- 左侧：区域树形结构 -->
      <a-col :span="8">
        <a-card :bordered="false" title="区域树形结构" class="area-tree-card">
          <template #extra>
            <a-button size="small" @click="handleRefreshTree">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </template>

          <a-tree
            v-model:selectedKeys="selectedAreaKeys"
            v-model:expandedKeys="expandedKeys"
            :tree-data="areaTreeData"
            :field-names="fieldNames"
            :show-line="true"
            @select="handleAreaSelect"
            @expand="handleAreaExpand"
          >
            <template #title="{ title, data }">
              <a-space>
                <span>{{ title }}</span>
                <a-tag v-if="data.areaType" :color="getAreaTypeColor(data.areaType)" size="small">
                  {{ getAreaTypeText(data.areaType) }}
                </a-tag>
                <a-tag v-if="data.status === 1" color="green" size="small">启用</a-tag>
                <a-tag v-else color="red" size="small">禁用</a-tag>
              </a-space>
            </template>
          </a-tree>
        </a-card>
      </a-col>

      <!-- 右侧：区域配置详情 -->
      <a-col :span="16">
        <a-card
          :bordered="false"
          :title="selectedArea ? `区域配置：${selectedArea.areaName}` : '区域配置详情'"
          class="area-config-card"
        >
          <template #extra v-if="selectedArea">
            <a-space>
              <a-button size="small" @click="handleEditArea">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button size="small" @click="handleDeleteArea" danger>
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-space>
          </template>

          <!-- 未选择区域时的提示 -->
          <div v-if="!selectedArea" class="no-selection">
            <a-empty description="请从左侧选择一个区域查看配置详情" />
          </div>

          <!-- 区域配置表单 -->
          <div v-else class="area-config-form">
            <a-form
              ref="areaConfigFormRef"
              :model="areaConfigForm"
              :rules="areaConfigRules"
              layout="vertical"
            >
              <!-- 基础信息 -->
              <a-divider orientation="left">基础信息</a-divider>

              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="区域名称" name="areaName">
                    <a-input
                      v-model:value="areaConfigForm.areaName"
                      placeholder="请输入区域名称"
                      :disabled="!isEditing"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="区域编码" name="areaCode">
                    <a-input
                      v-model:value="areaConfigForm.areaCode"
                      placeholder="请输入区域编码"
                      :disabled="!isEditing"
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="区域类型" name="areaType">
                    <a-select
                      v-model:value="areaConfigForm.areaType"
                      placeholder="请选择区域类型"
                      :disabled="!isEditing"
                    >
                      <a-select-option value="OFFICE">办公区域</a-select-option>
                      <a-select-option value="FACTORY">工厂区域</a-select-option>
                      <a-select-option value="WAREHOUSE">仓库区域</a-select-option>
                      <a-select-option value="OUTDOOR">户外区域</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="状态" name="status">
                    <a-switch
                      v-model:checked="areaConfigForm.status"
                      :checkedValue="1"
                      :unCheckedValue="0"
                      :disabled="!isEditing"
                    />
                    <span class="ml-2">{{ areaConfigForm.status === 1 ? '启用' : '禁用' }}</span>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-form-item label="描述" name="description">
                <a-textarea
                  v-model:value="areaConfigForm.description"
                  placeholder="请输入区域描述"
                  :rows="3"
                  :disabled="!isEditing"
                />
              </a-form-item>

              <!-- 考勤配置 -->
              <a-divider orientation="left">考勤配置</a-divider>

              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="是否启用GPS验证" name="gpsRequired">
                    <a-switch
                      v-model:checked="areaConfigForm.gpsRequired"
                      :disabled="!isEditing"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="是否启用照片验证" name="photoRequired">
                    <a-switch
                      v-model:checked="areaConfigForm.photoRequired"
                      :disabled="!isEditing"
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16" v-if="areaConfigForm.gpsRequired">
                <a-col :span="24">
                  <a-form-item label="GPS范围设置（米）" name="gpsRange">
                    <a-input-number
                      v-model:value="areaConfigForm.gpsRange"
                      :min="10"
                      :max="1000"
                      placeholder="请输入GPS验证范围（米）"
                      style="width: 200px"
                      :disabled="!isEditing"
                    />
                    <span class="ml-2 text-gray-500">员工必须在此范围内打卡</span>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="工作日打卡次数要求" name="workdayPunchRequired">
                    <a-input-number
                      v-model:value="areaConfigForm.workdayPunchRequired"
                      :min="1"
                      :max="4"
                      placeholder="请输入打卡次数"
                      style="width: 120px"
                      :disabled="!isEditing"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="周末打卡次数要求" name="weekendPunchRequired">
                    <a-input-number
                      v-model:value="areaConfigForm.weekendPunchRequired"
                      :min="0"
                      :max="4"
                      placeholder="请输入打卡次数"
                      style="width: 120px"
                      :disabled="!isEditing"
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <!-- 地理围栏配置 -->
              <a-divider orientation="left">地理围栏配置</a-divider>

              <a-form-item label="地理围栏顶点" name="geofencePoints">
                <div class="geofence-config">
                  <div
                    v-for="(point, index) in areaConfigForm.geofencePoints"
                    :key="index"
                    class="geofence-point-item"
                  >
                    <a-space>
                      <a-input-number
                        :model-value="point.longitude"
                        placeholder="经度"
                        style="width: 150px"
                        :precision="6"
                        :disabled="!isEditing"
                        @change="(value) => updateGeofencePoint(index, 'longitude', value)"
                      />
                      <a-input-number
                        :model-value="point.latitude"
                        placeholder="纬度"
                        style="width: 150px"
                        :precision="6"
                        :disabled="!isEditing"
                        @change="(value) => updateGeofencePoint(index, 'latitude', value)"
                      />
                      <a-button
                        size="small"
                        danger
                        @click="removeGeofencePoint(index)"
                        :disabled="!isEditing"
                      >
                        <template #icon><MinusOutlined /></template>
                      </a-button>
                    </a-space>
                  </div>
                  <a-button
                    v-if="isEditing"
                    type="dashed"
                    @click="addGeofencePoint"
                    style="width: 100%; margin-top: 8px;"
                  >
                    <template #icon><PlusOutlined /></template>
                    添加围栏顶点
                  </a-button>
                </div>
              </a-form-item>

              <!-- 操作按钮 -->
              <a-form-item v-if="isEditing">
                <a-space>
                  <a-button type="primary" @click="handleSaveConfig">
                    <template #icon><SaveOutlined /></template>
                    保存配置
                  </a-button>
                  <a-button @click="handleCancelEdit">
                    <template #icon><CloseOutlined /></template>
                    取消编辑
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 新增/编辑区域配置弹窗 -->
    <AreaConfigModal
      v-model:visible="modalVisible"
      :mode="modalMode"
      :areaData="currentAreaData"
      @success="handleModalSuccess"
    />

    <!-- 导入配置弹窗 -->
    <ImportConfigModal
      v-model:visible="importModalVisible"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, onMounted, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    ExportOutlined,
    ImportOutlined,
    ClockCircleOutlined,
    EnvironmentOutlined
  } from '@ant-design/icons-vue';

  // 组件引入
  import AreaConfigModal from './components/AreaConfigModal.vue';
  import ImportConfigModal from './components/ImportConfigModal.vue';

  // API 引入
  import { attendanceAreaApi } from '/@/api/business/attendance/attendance-area-api';

  // ============ 响应式数据 ============
  const queryForm = reactive({
    areaName: '',
    areaType: undefined,
    status: undefined
  });

  // 区域树数据
  const areaTreeData = ref([]);
  const selectedAreaKeys = ref([]);
  const expandedKeys = ref([]);

  // 选中的区域
  const selectedArea = ref(null);

  // 表单相关
  const areaConfigFormRef = ref();
  const areaConfigForm = reactive({
    areaId: null,
    areaName: '',
    areaCode: '',
    areaType: undefined,
    description: '',
    status: 1,
    gpsRequired: false,
    photoRequired: false,
    gpsRange: 100,
    workdayPunchRequired: 2,
    weekendPunchRequired: 0,
    geofencePoints: []
  });

  // 编辑状态
  const isEditing = ref(false);

  // 弹窗相关
  const modalVisible = ref(false);
  const modalMode = ref('add');
  const currentAreaData = ref(null);
  const importModalVisible = ref(false);

  // 表单验证规则
  const areaConfigRules = {
    areaName: [
      { required: true, message: '请输入区域名称', trigger: 'blur' }
    ],
    areaCode: [
      { required: true, message: '请输入区域编码', trigger: 'blur' }
    ],
    areaType: [
      { required: true, message: '请选择区域类型', trigger: 'change' }
    ],
    gpsRange: [
      { type: 'number', min: 10, max: 1000, message: 'GPS范围应在10-1000米之间', trigger: 'blur' }
    ],
    workdayPunchRequired: [
      { type: 'number', min: 1, max: 4, message: '工作日打卡次数应在1-4次之间', trigger: 'blur' }
    ],
    weekendPunchRequired: [
      { type: 'number', min: 0, max: 4, message: '周末打卡次数应在0-4次之间', trigger: 'blur' }
    ]
  };

  // 树形结构字段映射
  const fieldNames = {
    children: 'children',
    title: 'title',
    key: 'key',
    value: 'value'
  };

  // ============ 计算属性 ============
  const hasGeofencePoints = computed(() => {
    return areaConfigForm.geofencePoints && areaConfigForm.geofencePoints.length > 0;
  });

  // ============ 方法 ============

  // 获取区域类型颜色
  const getAreaTypeColor = (areaType) => {
    const colorMap = {
      'OFFICE': 'blue',
      'FACTORY': 'orange',
      'WAREHOUSE': 'purple',
      'OUTDOOR': 'green'
    };
    return colorMap[areaType] || 'default';
  };

  // 获取区域类型文本
  const getAreaTypeText = (areaType) => {
    const textMap = {
      'OFFICE': '办公区域',
      'FACTORY': '工厂区域',
      'WAREHOUSE': '仓库区域',
      'OUTDOOR': '户外区域'
    };
    return textMap[areaType] || areaType;
  };

  // 查询区域列表
  const handleSearch = async () => {
    try {
      const params = { ...queryForm };
      const result = await attendanceAreaApi.getAreaTree(params);
      areaTreeData.value = result.data || [];
      // 自动展开第一层
      if (areaTreeData.value.length > 0) {
        expandedKeys.value = areaTreeData.value.map(item => item.key);
      }
    } catch (error) {
      console.error('查询区域列表失败:', error);
      message.error('查询区域列表失败');
    }
  };

  // 重置查询条件
  const handleReset = () => {
    Object.assign(queryForm, {
      areaName: '',
      areaType: undefined,
      status: undefined
    });
    handleSearch();
  };

  // 刷新区域树
  const handleRefreshTree = () => {
    handleSearch();
  };

  // 区域选择
  const handleAreaSelect = (selectedKeys, { selected, selectedNodes }) => {
    if (selectedNodes.length > 0) {
      const node = selectedNodes[0];
      selectedArea.value = node;
      loadAreaConfig(node.key);
    } else {
      selectedArea.value = null;
      resetAreaConfigForm();
    }
  };

  // 区域展开
  const handleAreaExpand = (expandedKeys) => {
    // 可以在这里处理展开逻辑
  };

  // 加载区域配置
  const loadAreaConfig = async (areaId) => {
    try {
      const result = await attendanceAreaApi.getAreaConfig(areaId);
      if (result.data) {
        Object.assign(areaConfigForm, result.data);
        isEditing.value = false;
      }
    } catch (error) {
      console.error('加载区域配置失败:', error);
      message.error('加载区域配置失败');
    }
  };

  // 重置表单
  const resetAreaConfigForm = () => {
    Object.assign(areaConfigForm, {
      areaId: null,
      areaName: '',
      areaCode: '',
      areaType: undefined,
      description: '',
      status: 1,
      gpsRequired: false,
      photoRequired: false,
      gpsRange: 100,
      workdayPunchRequired: 2,
      weekendPunchRequired: 0,
      geofencePoints: []
    });
    isEditing.value = false;
  };

  // 新增区域
  const handleAddArea = () => {
    modalMode.value = 'add';
    currentAreaData.value = null;
    modalVisible.value = true;
  };

  // 编辑区域
  const handleEditArea = () => {
    if (!selectedArea.value) return;

    modalMode.value = 'edit';
    currentAreaData.value = { ...selectedArea.value, ...areaConfigForm };
    modalVisible.value = true;
  };

  // 删除区域
  const handleDeleteArea = async () => {
    if (!selectedArea.value) return;

    try {
      await attendanceAreaApi.deleteArea(selectedArea.value.key);
      message.success('删除区域成功');
      handleSearch();
      selectedArea.value = null;
      resetAreaConfigForm();
    } catch (error) {
      console.error('删除区域失败:', error);
      message.error('删除区域失败');
    }
  };

  // 开始编辑配置
  const startEditConfig = () => {
    isEditing.value = true;
  };

  // 保存配置
  const handleSaveConfig = async () => {
    try {
      await areaConfigFormRef.value.validate();

      const configData = {
        ...areaConfigForm,
        areaId: selectedArea.value.key
      };

      await attendanceAreaApi.updateAreaConfig(configData);
      message.success('保存配置成功');
      isEditing.value = false;
      await loadAreaConfig(selectedArea.value.key);
    } catch (error) {
      console.error('保存配置失败:', error);
      message.error('保存配置失败');
    }
  };

  // 取消编辑
  const handleCancelEdit = () => {
    isEditing.value = false;
    if (selectedArea.value) {
      loadAreaConfig(selectedArea.value.key);
    }
  };

  // 添加地理围栏顶点
  const addGeofencePoint = () => {
    areaConfigForm.geofencePoints.push({
      longitude: null,
      latitude: null
    });
  };

  // 更新地理围栏顶点
  const updateGeofencePoint = (index, field, value) => {
    areaConfigForm.geofencePoints[index][field] = value;
  };

  // 删除地理围栏顶点
  const removeGeofencePoint = (index) => {
    areaConfigForm.geofencePoints.splice(index, 1);
  };

  // 导出配置
  const handleExportConfig = async () => {
    try {
      const result = await attendanceAreaApi.exportConfig();
      // 创建下载链接
      const blob = new Blob([result.data], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `attendance-area-config-${new Date().toISOString().slice(0, 10)}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      message.success('配置导出成功');
    } catch (error) {
      console.error('导出配置失败:', error);
      message.error('导出配置失败');
    }
  };

  // 导入配置
  const handleImportConfig = () => {
    importModalVisible.value = true;
  };

  // 弹窗成功回调
  const handleModalSuccess = () => {
    modalVisible.value = false;
    handleSearch();
  };

  const handleImportSuccess = () => {
    importModalVisible.value = false;
    handleSearch();
  };

  // 返回上一页
  const handleBack = () => {
    window.history.back();
  };

  // ============ 生命周期 ============
  onMounted(() => {
    handleSearch();
  });
</script>

<style lang="less" scoped>
.attendance-area-management {
  padding: 24px;
  background: #f0f2f5;

  .search-card {
    margin-bottom: 16px;
  }

  .area-tree-card {
    height: calc(100vh - 250px);
    overflow-y: auto;
  }

  .area-config-card {
    height: calc(100vh - 250px);
    overflow-y: auto;
  }

  .no-selection {
    height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .area-config-form {
    .ant-divider {
      margin: 16px 0;
    }
  }

  .geofence-config {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    padding: 16px;

    .geofence-point-item {
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .status-header {
    text-align: center;
    margin-bottom: 16px;

    .current-time {
      font-size: 24px;
      font-weight: bold;
      color: #1890ff;
    }

    .current-date {
      font-size: 16px;
      color: #666;
    }

    .current-weekday {
      font-size: 14px;
      color: #999;
    }
  }

  .status-content {
    .today-records {
      .record-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .record-type {
          &.punch-in {
            color: #52c41a;
          }

          &.punch-out {
            color: #fa8c16;
          }
        }

        .record-time {
          margin: 0 12px;
          font-weight: bold;
        }

        .record-location {
          color: #666;
          font-size: 12px;
        }
      }
    }

    .no-records {
      text-align: center;
      color: #999;

      .no-records-icon {
        font-size: 32px;
        margin-bottom: 8px;
        color: #d9d9d9;
      }

      .no-records-text {
        font-size: 14px;
      }
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .attendance-area-management {
    padding: 16px;

    .area-tree-card,
    .area-config-card {
      height: auto;
      margin-bottom: 16px;
    }
  }
}
</style>