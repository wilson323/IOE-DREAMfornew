<template>
  <div class="access-restriction-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增限制规则
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
      row-key="restrictionId"
    >
      <!-- 限制类型 -->
      <template #restrictionType="{ record }">
        <a-tag :color="getRestrictionTypeColor(record.restrictionType)">
          {{ getRestrictionTypeDesc(record.restrictionType) }}
        </a-tag>
      </template>

      <!-- 有效期 -->
      <template #validPeriod="{ record }">
        <span v-if="record.effectiveDate && record.expireDate">
          {{ record.effectiveDate }} 至 {{ record.expireDate }}
        </span>
        <span v-else>-</span>
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
        <a-form-item label="用户ID" name="userId">
          <a-input-number v-model:value="formData.userId" placeholder="请输入用户ID" style="width: 100%" />
        </a-form-item>

        <a-form-item label="限制类型" name="restrictionType">
          <a-select v-model:value="formData.restrictionType" placeholder="请选择限制类型">
            <a-select-option value="BLACKLIST">黑名单</a-select-option>
            <a-select-option value="WHITELIST">白名单</a-select-option>
            <a-select-option value="TIME_BASED">时段限制</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="限制区域" name="areaIds">
          <a-select
            v-model:value="formData.areaIds"
            mode="multiple"
            placeholder="请选择限制区域"
            style="width: 100%"
          >
            <!-- TODO: 从API加载区域列表 -->
            <a-select-option value="1001">区域A</a-select-option>
            <a-select-option value="1002">区域B</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="限制开始时间" name="timeStart" v-if="formData.restrictionType === 'TIME_BASED'">
          <a-time-picker
            v-model:value="formData.timeStart"
            format="HH:mm:ss"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="限制结束时间" name="timeEnd" v-if="formData.restrictionType === 'TIME_BASED'">
          <a-time-picker
            v-model:value="formData.timeEnd"
            format="HH:mm:ss"
            placeholder="请选择结束时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="生效日期" name="effectiveDate">
          <a-date-picker
            v-model:value="formData.effectiveDate"
            placeholder="请选择生效日期"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="失效日期" name="expireDate">
          <a-date-picker
            v-model:value="formData.expireDate"
            placeholder="请选择失效日期"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="限制原因" name="reason">
          <a-textarea v-model:value="formData.reason" :rows="2" placeholder="请输入限制原因" />
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
const modalTitle = ref('新增限制规则');
const formRef = ref();

const formData = reactive({
  restrictionId: null,
  userId: null,
  userName: '',
  userPhone: '',
  restrictionType: 'BLACKLIST',
  areaIds: [],
  areaNames: '',
  doorIds: '',
  timeStart: null,
  timeEnd: null,
  effectiveDate: null,
  expireDate: null,
  reason: '',
  priority: 100,
  enabled: 1,
  description: ''
});

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  restrictionType: [{ required: true, message: '请选择限制类型', trigger: 'change' }],
  areaIds: [{ required: true, message: '请选择限制区域', trigger: 'change' }]
};

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

const columns = [
  { title: '规则ID', dataIndex: 'restrictionId', width: 100 },
  { title: '用户ID', dataIndex: 'userId', width: 100 },
  { title: '用户姓名', dataIndex: 'userName', width: 120 },
  { title: '限制类型', dataIndex: 'restrictionType', slots: { customRender: 'restrictionType' }, width: 120 },
  { title: '限制区域', dataIndex: 'areaNames', width: 200 },
  { title: '时段限制', dataIndex: '', width: 150, customRender: ({ record }) => record.timeStart && record.timeEnd ? `${record.timeStart}-${record.timeEnd}` : '-' },
  { title: '有效期', dataIndex: '', slots: { customRender: 'validPeriod' }, width: 200 },
  { title: '限制原因', dataIndex: 'reason', width: 200 },
  { title: '优先级', dataIndex: 'priority', width: 80 },
  { title: '启用状态', dataIndex: 'enabled', slots: { customRender: 'enabled' }, width: 100 },
  { title: '操作', key: 'action', slots: { customRender: 'action' }, width: 150, fixed: 'right' }
];

const loadData = async () => {
  loading.value = true;
  try {
    // TODO: 调用API查询数据
    dataSource.value = [];
    pagination.total = 0;
  } catch (error) {
    console.error('[人员限制] 加载数据失败:', error);
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
  modalTitle.value = '新增限制规则';
  Object.assign(formData, {
    restrictionId: null,
    userId: null,
    userName: '',
    userPhone: '',
    restrictionType: 'BLACKLIST',
    areaIds: [],
    areaNames: '',
    doorIds: '',
    timeStart: null,
    timeEnd: null,
    effectiveDate: null,
    expireDate: null,
    reason: '',
    priority: 100,
    enabled: 1,
    description: ''
  });
  modalVisible.value = true;
};

const handleEdit = (record) => {
  modalTitle.value = '编辑限制规则';
  Object.assign(formData, {
    ...record,
    areaIds: record.areaIds ? JSON.parse(record.areaIds) : []
  });
  modalVisible.value = true;
};

const handleToggleEnabled = async (record, checked) => {
  try {
    // TODO: 调用API更新状态
    message.success(checked ? '已启用' : '已禁用');
    loadData();
  } catch (error) {
    console.error('[人员限制] 更新状态失败:', error);
    message.error('更新状态失败');
  }
};

const handleDelete = async (record) => {
  try {
    // TODO: 调用API删除
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('[人员限制] 删除失败:', error);
    message.error('删除失败');
  }
};

const handleModalOk = async () => {
  try {
    await formRef.value.validate();
    // TODO: 调用API保存
    message.success(formData.restrictionId ? '更新成功' : '新增成功');
    modalVisible.value = false;
    loadData();
  } catch (error) {
    console.error('[人员限制] 保存失败:', error);
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

const getRestrictionTypeDesc = (type) => {
  const map = {
    'BLACKLIST': '黑名单',
    'WHITELIST': '白名单',
    'TIME_BASED': '时段限制'
  };
  return map[type] || type;
};

const getRestrictionTypeColor = (type) => {
  const map = {
    'BLACKLIST': 'red',
    'WHITELIST': 'green',
    'TIME_BASED': 'orange'
  };
  return map[type] || 'default';
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.access-restriction-container {
  .action-bar {
    margin-bottom: 16px;
  }
}
</style>
