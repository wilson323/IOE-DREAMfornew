<!--
 * 司机管理组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="driver-management">
    <!-- 搜索表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="司机姓名">
          <a-input
            v-model:value="searchForm.driverName"
            placeholder="请输入司机姓名"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="驾照号">
          <a-input
            v-model:value="searchForm.licenseNumber"
            placeholder="请输入驾照号"
            allow-clear
            style="width: 200px"
          />
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
              添加司机
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 司机列表 -->
    <a-card :bordered="false" class="table-card" style="margin-top: 16px">
      <a-table
        :columns="driverColumns"
        :data-source="driverList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
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

    <!-- 添加/编辑司机弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑司机' : '添加司机'"
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
        <a-form-item label="司机姓名" name="driverName">
          <a-input
            v-model:value="formData.driverName"
            placeholder="请输入司机姓名"
          />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input
            v-model:value="formData.phone"
            placeholder="请输入手机号"
          />
        </a-form-item>
        <a-form-item label="身份证号" name="idCard">
          <a-input
            v-model:value="formData.idCard"
            placeholder="请输入身份证号"
          />
        </a-form-item>
        <a-form-item label="驾照号" name="licenseNumber">
          <a-input
            v-model:value="formData.licenseNumber"
            placeholder="请输入驾照号"
          />
        </a-form-item>
        <a-form-item label="驾照类型" name="licenseType">
          <a-select
            v-model:value="formData.licenseType"
            placeholder="请选择驾照类型"
          >
            <a-select-option value="A1">A1</a-select-option>
            <a-select-option value="A2">A2</a-select-option>
            <a-select-option value="B1">B1</a-select-option>
            <a-select-option value="B2">B2</a-select-option>
            <a-select-option value="C1">C1</a-select-option>
            <a-select-option value="C2">C2</a-select-option>
          </a-select>
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
  driverName: '',
  phone: '',
  licenseNumber: '',
});

// 表格数据
const driverList = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列定义
const driverColumns = [
  {
    title: '司机姓名',
    dataIndex: 'driverName',
    key: 'driverName',
    width: 120,
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
    width: 130,
  },
  {
    title: '身份证号',
    dataIndex: 'idCard',
    key: 'idCard',
    width: 180,
  },
  {
    title: '驾照号',
    dataIndex: 'licenseNumber',
    key: 'licenseNumber',
    width: 150,
  },
  {
    title: '驾照类型',
    dataIndex: 'licenseType',
    key: 'licenseType',
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
  driverName: '',
  phone: '',
  idCard: '',
  licenseNumber: '',
  licenseType: undefined,
  remark: '',
});

const formRules = {
  driverName: [{ required: true, message: '请输入司机姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  licenseNumber: [
    { required: true, message: '请输入驾照号', trigger: 'blur' },
  ],
  licenseType: [
    { required: true, message: '请选择驾照类型', trigger: 'change' },
  ],
};

// 查询司机列表
const queryDriverList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
    };
    const res = await visitorApi.queryDrivers(params);
    if (res.data) {
      driverList.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    message.error('查询司机列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  queryDriverList();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    driverName: '',
    phone: '',
    licenseNumber: '',
  });
  handleSearch();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryDriverList();
};

// 显示添加弹窗
const showAddModal = () => {
  isEdit.value = false;
  modalVisible.value = true;
  Object.assign(formData, {
    id: undefined,
    driverName: '',
    phone: '',
    idCard: '',
    licenseNumber: '',
    licenseType: undefined,
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
      ? await visitorApi.updateDriver(formData)
      : await visitorApi.addDriver(formData);
    if (res.success) {
      message.success(isEdit.value ? '编辑成功' : '添加成功');
      modalVisible.value = false;
      queryDriverList();
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
    content: `确定要删除司机"${record.driverName}"吗？`,
    onOk: async () => {
      try {
        const res = await visitorApi.deleteDriver(record.id);
        if (res.success) {
          message.success('删除成功');
          queryDriverList();
        } else {
          message.error(res.msg || '删除失败');
        }
      } catch (error) {
        message.error('删除失败：' + error.message);
      }
    },
  });
};

// 暴露方法供父组件调用
defineExpose({
  showAddModal,
});

// 初始化
onMounted(() => {
  queryDriverList();
});
</script>

<style scoped lang="less">
.driver-management {
  .search-card {
    background: #fff;
  }

  .table-card {
    background: #fff;
  }
}
</style>

