<!--
 * 车辆管理组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="vehicle-management">
    <!-- 搜索表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="车牌号">
          <a-input
            v-model:value="searchForm.vehicleNumber"
            placeholder="请输入车牌号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="车辆类型">
          <a-select
            v-model:value="searchForm.vehicleType"
            placeholder="请选择车辆类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="TRUCK">货车</a-select-option>
            <a-select-option value="VAN">面包车</a-select-option>
            <a-select-option value="CAR">轿车</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" @click="showAddModal">
              <template #icon><PlusOutlined /></template>
              添加车辆
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 车辆列表 -->
    <a-card :bordered="false" class="table-card" style="margin-top: 16px">
      <a-table
        :columns="vehicleColumns"
        :data-source="vehicleList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'vehicleType'">
            <a-tag :color="getVehicleTypeColor(record.vehicleType)">
              {{ getVehicleTypeText(record.vehicleType) }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-badge
              :status="record.status === 'ACTIVE' ? 'success' : 'default'"
              :text="record.status === 'ACTIVE' ? '启用' : '禁用'"
            />
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="handleDelete(record)"
              >
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑车辆弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑车辆' : '添加车辆'"
      :width="600"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="车牌号" name="vehicleNumber">
          <a-input
            v-model:value="formData.vehicleNumber"
            placeholder="请输入车牌号"
          />
        </a-form-item>
        <a-form-item label="车辆类型" name="vehicleType">
          <a-select
            v-model:value="formData.vehicleType"
            placeholder="请选择车辆类型"
          >
            <a-select-option value="TRUCK">货车</a-select-option>
            <a-select-option value="VAN">面包车</a-select-option>
            <a-select-option value="CAR">轿车</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="车辆品牌" name="brand">
          <a-input
            v-model:value="formData.brand"
            placeholder="请输入车辆品牌"
          />
        </a-form-item>
        <a-form-item label="车辆型号" name="model">
          <a-input
            v-model:value="formData.model"
            placeholder="请输入车辆型号"
          />
        </a-form-item>
        <a-form-item label="车辆颜色" name="color">
          <a-input
            v-model:value="formData.color"
            placeholder="请输入车辆颜色"
          />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            placeholder="请输入备注"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { SearchOutlined, PlusOutlined } from '@ant-design/icons-vue';
import visitorApi from '/@/api/business/visitor/visitor-api';

// 搜索表单
const searchForm = reactive({
  vehicleNumber: '',
  vehicleType: undefined,
});

// 表格数据
const vehicleList = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列定义
const vehicleColumns = [
  {
    title: '车牌号',
    dataIndex: 'vehicleNumber',
    key: 'vehicleNumber',
    width: 150,
  },
  {
    title: '车辆类型',
    dataIndex: 'vehicleType',
    key: 'vehicleType',
    width: 120,
  },
  {
    title: '车辆品牌',
    dataIndex: 'brand',
    key: 'brand',
    width: 120,
  },
  {
    title: '车辆型号',
    dataIndex: 'model',
    key: 'model',
    width: 150,
  },
  {
    title: '车辆颜色',
    dataIndex: 'color',
    key: 'color',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
  },
];

// 弹窗
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();
const formData = reactive({
  id: undefined,
  vehicleNumber: '',
  vehicleType: undefined,
  brand: '',
  model: '',
  color: '',
  remark: '',
});

const formRules = {
  vehicleNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
  ],
  vehicleType: [
    { required: true, message: '请选择车辆类型', trigger: 'change' },
  ],
};

// 查询车辆列表
const queryVehicleList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
    };
    const res = await visitorApi.queryVehicles(params);
    if (res.data) {
      vehicleList.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    message.error('查询车辆列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  queryVehicleList();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    vehicleNumber: '',
    vehicleType: undefined,
  });
  handleSearch();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryVehicleList();
};

// 显示添加弹窗
const showAddModal = () => {
  isEdit.value = false;
  modalVisible.value = true;
  Object.assign(formData, {
    id: undefined,
    vehicleNumber: '',
    vehicleType: undefined,
    brand: '',
    model: '',
    color: '',
    remark: '',
  });
};

// 编辑
const handleEdit = (record) => {
  isEdit.value = true;
  modalVisible.value = true;
  Object.assign(formData, record);
};

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    const res = isEdit.value
      ? await visitorApi.updateVehicle(formData)
      : await visitorApi.addVehicle(formData);
    if (res.success) {
      message.success(isEdit.value ? '编辑成功' : '添加成功');
      modalVisible.value = false;
      queryVehicleList();
    } else {
      message.error(res.msg || '操作失败');
    }
  } catch (error) {
    if (error.errorFields) {
      return;
    }
    message.error('操作失败：' + error.message);
  }
};

// 取消
const handleCancel = () => {
  modalVisible.value = false;
};

// 删除
const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除车辆"${record.vehicleNumber}"吗？`,
    onOk: async () => {
      try {
        const res = await visitorApi.deleteVehicle(record.id);
        if (res.success) {
          message.success('删除成功');
          queryVehicleList();
        } else {
          message.error(res.msg || '删除失败');
        }
      } catch (error) {
        message.error('删除失败：' + error.message);
      }
    },
  });
};

// 获取车辆类型颜色
const getVehicleTypeColor = (type) => {
  const colorMap = {
    TRUCK: 'blue',
    VAN: 'green',
    CAR: 'orange',
    OTHER: 'default',
  };
  return colorMap[type] || 'default';
};

// 获取车辆类型文本
const getVehicleTypeText = (type) => {
  const textMap = {
    TRUCK: '货车',
    VAN: '面包车',
    CAR: '轿车',
    OTHER: '其他',
  };
  return textMap[type] || type;
};

// 暴露方法供父组件调用
defineExpose({
  showAddModal,
});

// 初始化
onMounted(() => {
  queryVehicleList();
});
</script>

<style scoped lang="less">
.vehicle-management {
  .search-card {
    background: #fff;
  }

  .table-card {
    background: #fff;
  }
}
</style>

