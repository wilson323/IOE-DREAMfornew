<template>
  <div class="access-evacuation-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增疏散点
        </a-button>
        <a-button @click="handleRefresh">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 数据表格 -->
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
      row-key="pointId"
    >
      <!-- 疏散类型 -->
      <template #evacuationType="{ record }">
        <a-tag :color="getEvacuationTypeColor(record.evacuationType)">
          {{ getEvacuationTypeDesc(record.evacuationType) }}
        </a-tag>
      </template>

      <!-- 启用状态 -->
      <template #enabled="{ record }">
        <a-switch
          :checked="record.enabled === 1"
          @change="(checked) => handleToggleEnabled(record, checked)"
        />
      </template>

      <!-- 操作 -->
      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleTrigger(record)">
            一键疏散
          </a-button>
          <a-button type="link" size="small" @click="handleEdit(record)">
            编辑
          </a-button>
          <a-button type="link" size="small" danger @click="handleDelete(record)">
            删除
          </a-button>
        </a-space>
      </template>
    </a-table>

    <!-- 新增/编辑对话框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      width="800px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="疏散点名称" name="pointName">
          <a-input v-model:value="formData.pointName" placeholder="请输入疏散点名称" />
        </a-form-item>

        <a-form-item label="疏散点编码" name="pointCode">
          <a-input v-model:value="formData.pointCode" placeholder="请输入疏散点编码" />
        </a-form-item>

        <a-form-item label="关联区域ID" name="areaId">
          <a-input-number v-model:value="formData.areaId" placeholder="请输入区域ID" style="width: 100%" />
        </a-form-item>

        <a-form-item label="疏散类型" name="evacuationType">
          <a-select v-model:value="formData.evacuationType" placeholder="请选择疏散类型">
            <a-select-option value="FIRE">火灾疏散</a-select-option>
            <a-select-option value="EARTHQUAKE">地震疏散</a-select-option>
            <a-select-option value="EMERGENCY">紧急疏散</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="优先级" name="priority">
          <a-input-number v-model:value="formData.priority" :min="1" :max="100" placeholder="请输入优先级(1-100)" style="width: 100%" />
        </a-form-item>

        <a-form-item label="规则描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入规则描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue';

const loading = ref(false);
const dataSource = ref([]);
const modalVisible = ref(false);
const modalTitle = ref('新增疏散点');
const formRef = ref();

const formData = reactive({
  pointId: null,
  pointName: '',
  pointCode: '',
  areaId: null,
  areaName: '',
  doorIds: '',
  doorNames: '',
  deviceIds: '',
  evacuationType: 'FIRE',
  priority: 100,
  enabled: 1,
  description: ''
});

const rules = {
  pointName: [{ required: true, message: '请输入疏散点名称', trigger: 'blur' }],
  pointCode: [{ required: true, message: '请输入疏散点编码', trigger: 'blur' }],
  areaId: [{ required: true, message: '请输入区域ID', trigger: 'blur' }],
  evacuationType: [{ required: true, message: '请选择疏散类型', trigger: 'change' }]
};

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

const columns = [
  { title: '疏散点ID', dataIndex: 'pointId', width: 100 },
  { title: '疏散点名称', dataIndex: 'pointName', width: 150 },
  { title: '疏散点编码', dataIndex: 'pointCode', width: 150 },
  { title: '关联区域', dataIndex: 'areaName', width: 150 },
  { title: '疏散类型', dataIndex: 'evacuationType', slots: { customRender: 'evacuationType' }, width: 120 },
  { title: '关联门数', dataIndex: 'doorIds', width: 100, customRender: ({ record }) => JSON.parse(record.doorIds || '[]').length },
  { title: '优先级', dataIndex: 'priority', width: 80 },
  { title: '启用状态', dataIndex: 'enabled', slots: { customRender: 'enabled' }, width: 100 },
  { title: '操作', key: 'action', slots: { customRender: 'action' }, width: 200, fixed: 'right' }
];

const loadData = async () => {
  loading.value = true;
  try {
    // TODO: 调用API查询数据
    dataSource.value = [];
    pagination.total = 0;
  } catch (error) {
    console.error('[疏散管理] 加载数据失败:', error);
    message.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadData();
};

const handleAdd = () => {
  modalTitle.value = '新增疏散点';
  Object.assign(formData, {
    pointId: null,
    pointName: '',
    pointCode: '',
    areaId: null,
    areaName: '',
    doorIds: '',
    doorNames: '',
    deviceIds: '',
    evacuationType: 'FIRE',
    priority: 100,
    enabled: 1,
    description: ''
  });
  modalVisible.value = true;
};

const handleEdit = (record) => {
  modalTitle.value = '编辑疏散点';
  Object.assign(formData, record);
  modalVisible.value = true;
};

const handleTrigger = async (record) => {
  try {
    // TODO: 调用API触发疏散
    message.success('一键疏散成功');
  } catch (error) {
    console.error('[疏散管理] 疏散失败:', error);
    message.error('疏散失败');
  }
};

const handleToggleEnabled = async (record, checked) => {
  try {
    // TODO: 调用API更新状态
    message.success(checked ? '已启用' : '已禁用');
    loadData();
  } catch (error) {
    console.error('[疏散管理] 更新状态失败:', error);
    message.error('更新状态失败');
  }
};

const handleDelete = async (record) => {
  try {
    // TODO: 调用API删除
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('[疏散管理] 删除失败:', error);
    message.error('删除失败');
  }
};

const handleModalOk = async () => {
  try {
    await formRef.value.validate();
    // TODO: 调用API保存
    message.success(formData.pointId ? '更新成功' : '新增成功');
    modalVisible.value = false;
    loadData();
  } catch (error) {
    console.error('[疏散管理] 保存失败:', error);
    message.error('保存失败');
  }
};

const handleModalCancel = () => {
  modalVisible.value = false;
  formRef.value.resetFields();
};

const handleRefresh = () => {
  loadData();
};

const getEvacuationTypeDesc = (type) => {
  const map = {
    'FIRE': '火灾疏散',
    'EARTHQUAKE': '地震疏散',
    'EMERGENCY': '紧急疏散'
  };
  return map[type] || type;
};

const getEvacuationTypeColor = (type) => {
  const map = {
    'FIRE': 'red',
    'EARTHQUAKE': 'orange',
    'EMERGENCY': 'blue'
  };
  return map[type] || 'default';
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.access-evacuation-container {
  .action-bar {
    margin-bottom: 16px;
  }
}
</style>
