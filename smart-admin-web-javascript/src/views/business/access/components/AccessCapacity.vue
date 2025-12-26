<template>
  <div class="access-capacity-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增容量控制
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
      row-key="controlId"
    >
      <!-- 控制模式 -->
      <template #controlMode="{ record }">
        <a-tag :color="getControlModeColor(record.controlMode)">
          {{ getControlModeDesc(record.controlMode) }}
        </a-tag>
      </template>

      <!-- 容量使用情况 -->
      <template #capacity="{ record }">
        <a-progress
          :percent="getCapacityPercent(record)"
          :status="getCapacityPercent(record) >= 100 ? 'exception' : 'normal'"
        />
        <span style="margin-left: 8px">{{ record.currentCount }}/{{ record.maxCapacity }}</span>
      </template>

      <!-- 禁止进入 -->
      <template #entryBlocked="{ record }">
        <a-tag :color="record.entryBlocked === 1 ? 'red' : 'green'">
          {{ record.entryBlocked === 1 ? '禁止' : '允许' }}
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
          <a-button type="link" size="small" @click="handleReset(record)">
            重置计数
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
        <a-form-item label="区域ID" name="areaId">
          <a-input-number v-model:value="formData.areaId" placeholder="请输入区域ID" style="width: 100%" />
        </a-form-item>

        <a-form-item label="最大容量" name="maxCapacity">
          <a-input-number v-model:value="formData.maxCapacity" :min="1" placeholder="请输入最大容量" style="width: 100%" />
        </a-form-item>

        <a-form-item label="控制模式" name="controlMode">
          <a-select v-model:value="formData.controlMode" placeholder="请选择控制模式">
            <a-select-option value="STRICT">严格模式(满员禁止进入)</a-select-option>
            <a-select-option value="WARNING">警告模式(满员警告)</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="告警阈值(%)" name="alertThreshold">
          <a-input-number v-model:value="formData.alertThreshold" :min="1" :max="100" placeholder="请输入告警阈值" style="width: 100%" />
        </a-form-item>

        <a-form-item label="自动重置" name="autoReset">
          <a-switch v-model:checked="formData.autoReset" />
        </a-form-item>

        <a-form-item label="重置时间" name="resetTime" v-if="formData.autoReset">
          <a-time-picker
            v-model:value="formData.resetTime"
            format="HH:mm:ss"
            placeholder="请选择重置时间"
            style="width: 100%"
          />
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
const modalTitle = ref('新增容量控制');
const formRef = ref();

const formData = reactive({
  controlId: null,
  areaId: null,
  areaName: '',
  doorIds: '',
  maxCapacity: 100,
  currentCount: 0,
  controlMode: 'STRICT',
  entryBlocked: 0,
  alertThreshold: 80,
  autoReset: 0,
  resetTime: null,
  priority: 100,
  enabled: 1,
  description: ''
});

const rules = {
  areaId: [{ required: true, message: '请输入区域ID', trigger: 'blur' }],
  maxCapacity: [{ required: true, message: '请输入最大容量', trigger: 'blur' }],
  controlMode: [{ required: true, message: '请选择控制模式', trigger: 'change' }],
  alertThreshold: [{ required: true, message: '请输入告警阈值', trigger: 'blur' }]
};

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

const columns = [
  { title: '控制ID', dataIndex: 'controlId', width: 100 },
  { title: '区域名称', dataIndex: 'areaName', width: 150 },
  { title: '最大容量', dataIndex: 'maxCapacity', width: 100 },
  { title: '当前人数', dataIndex: 'currentCount', width: 100 },
  { title: '容量使用', dataIndex: '', slots: { customRender: 'capacity' }, width: 200 },
  { title: '控制模式', dataIndex: 'controlMode', slots: { customRender: 'controlMode' }, width: 150 },
  { title: '进入限制', dataIndex: '', slots: { customRender: 'entryBlocked' }, width: 100 },
  { title: '告警阈值', dataIndex: 'alertThreshold', width: 100, customRender: ({ record }) => record.alertThreshold + '%' },
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
    console.error('[容量控制] 加载数据失败:', error);
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
  modalTitle.value = '新增容量控制';
  Object.assign(formData, {
    controlId: null,
    areaId: null,
    areaName: '',
    doorIds: '',
    maxCapacity: 100,
    currentCount: 0,
    controlMode: 'STRICT',
    entryBlocked: 0,
    alertThreshold: 80,
    autoReset: 0,
    resetTime: null,
    priority: 100,
    enabled: 1,
    description: ''
  });
  modalVisible.value = true;
};

const handleEdit = (record) => {
  modalTitle.value = '编辑容量控制';
  Object.assign(formData, record);
  modalVisible.value = true;
};

const handleReset = async (record) => {
  try {
    // TODO: 调用API重置计数
    message.success('重置成功');
    loadData();
  } catch (error) {
    console.error('[容量控制] 重置失败:', error);
    message.error('重置失败');
  }
};

const handleToggleEnabled = async (record, checked) => {
  try {
    // TODO: 调用API更新状态
    message.success(checked ? '已启用' : '已禁用');
    loadData();
  } catch (error) {
    console.error('[容量控制] 更新状态失败:', error);
    message.error('更新状态失败');
  }
};

const handleDelete = async (record) => {
  try {
    // TODO: 调用API删除
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('[容量控制] 删除失败:', error);
    message.error('删除失败');
  }
};

const handleModalOk = async () => {
  try {
    await formRef.value.validate();
    // TODO: 调用API保存
    message.success(formData.controlId ? '更新成功' : '新增成功');
    modalVisible.value = false;
    loadData();
  } catch (error) {
    console.error('[容量控制] 保存失败:', error);
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

const getControlModeDesc = (mode) => {
  const map = {
    'STRICT': '严格模式',
    'WARNING': '警告模式'
  };
  return map[mode] || mode;
};

const getControlModeColor = (mode) => {
  const map = {
    'STRICT': 'red',
    'WARNING': 'orange'
  };
  return map[mode] || 'default';
};

const getCapacityPercent = (record) => {
  if (record.maxCapacity === 0) return 0;
  return Math.round((record.currentCount / record.maxCapacity) * 100);
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.access-capacity-container {
  .action-bar {
    margin-bottom: 16px;
  }
}
</style>
