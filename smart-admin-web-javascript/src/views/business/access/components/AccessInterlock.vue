<template>
  <div class="access-interlock-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增互锁规则
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
      row-key="ruleId"
    >
      <!-- 互锁模式 -->
      <template #interlockMode="{ record }">
        <a-tag :color="getInterlockModeColor(record.interlockMode)">
          {{ getInterlockModeDesc(record.interlockMode) }}
        </a-tag>
      </template>

      <!-- 解锁条件 -->
      <template #unlockCondition="{ record }">
        <a-tag color="blue">
          {{ getUnlockConditionDesc(record.unlockCondition) }}
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
          <a-button type="link" size="small" @click="handleTest(record)">
            测试
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
        <a-form-item label="规则名称" name="ruleName">
          <a-input v-model:value="formData.ruleName" placeholder="请输入规则名称" />
        </a-form-item>

        <a-form-item label="规则编码" name="ruleCode">
          <a-input v-model:value="formData.ruleCode" placeholder="请输入规则编码" />
        </a-form-item>

        <a-form-item label="区域A ID" name="areaAId">
          <a-input-number v-model:value="formData.areaAId" placeholder="请输入区域A ID" style="width: 100%" />
        </a-form-item>

        <a-form-item label="区域B ID" name="areaBId">
          <a-input-number v-model:value="formData.areaBId" placeholder="请输入区域B ID" style="width: 100%" />
        </a-form-item>

        <a-form-item label="互锁模式" name="interlockMode">
          <a-select v-model:value="formData.interlockMode" placeholder="请选择互锁模式">
            <a-select-option value="BIDIRECTIONAL">双向互锁</a-select-option>
            <a-select-option value="UNIDIRECTIONAL">单向互锁</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="解锁条件" name="unlockCondition">
          <a-select v-model:value="formData.unlockCondition" placeholder="请选择解锁条件">
            <a-select-option value="MANUAL">手动解锁</a-select-option>
            <a-select-option value="TIMER">定时解锁</a-select-option>
            <a-select-option value="AUTO">自动解锁</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="解锁延迟(秒)" name="unlockDelaySeconds">
          <a-input-number v-model:value="formData.unlockDelaySeconds" :min="0" placeholder="请输入延迟秒数" style="width: 100%" />
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
const modalTitle = ref('新增互锁规则');
const formRef = ref();

const formData = reactive({
  ruleId: null,
  ruleName: '',
  ruleCode: '',
  areaAId: null,
  areaBId: null,
  interlockMode: 'BIDIRECTIONAL',
  areaADoorIds: '',
  areaBDoorIds: '',
  unlockCondition: 'MANUAL',
  unlockDelaySeconds: 0,
  priority: 100,
  description: ''
});

const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  areaAId: [{ required: true, message: '请输入区域A ID', trigger: 'blur' }],
  areaBId: [{ required: true, message: '请输入区域B ID', trigger: 'blur' }],
  interlockMode: [{ required: true, message: '请选择互锁模式', trigger: 'change' }],
  unlockCondition: [{ required: true, message: '请选择解锁条件', trigger: 'change' }]
};

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

const columns = [
  { title: '规则ID', dataIndex: 'ruleId', width: 100 },
  { title: '规则名称', dataIndex: 'ruleName', width: 150 },
  { title: '规则编码', dataIndex: 'ruleCode', width: 150 },
  { title: '区域A ID', dataIndex: 'areaAId', width: 120 },
  { title: '区域B ID', dataIndex: 'areaBId', width: 120 },
  { title: '互锁模式', dataIndex: 'interlockMode', slots: { customRender: 'interlockMode' }, width: 120 },
  { title: '解锁条件', dataIndex: 'unlockCondition', slots: { customRender: 'unlockCondition' }, width: 120 },
  { title: '解锁延迟(秒)', dataIndex: 'unlockDelaySeconds', width: 120 },
  { title: '优先级', dataIndex: 'priority', width: 80 },
  { title: '启用状态', dataIndex: 'enabled', slots: { customRender: 'enabled' }, width: 100 },
  { title: '操作', key: 'action', slots: { customRender: 'action' }, width: 200, fixed: 'right' }
];

const loadData = async () => {
  loading.value = true;
  try {
    // TODO: 调用API查询数据
    // const res = await advancedApi.queryInterlockRules({
    //   pageNum: pagination.current,
    //   pageSize: pagination.pageSize
    // });
    // if (res.code === 200) {
    //   dataSource.value = res.data.list || [];
    //   pagination.total = res.data.total || 0;
    // }

    // 模拟数据
    dataSource.value = [];
    pagination.total = 0;
  } catch (error) {
    console.error('[互锁管理] 加载数据失败:', error);
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
  modalTitle.value = '新增互锁规则';
  Object.assign(formData, {
    ruleId: null,
    ruleName: '',
    ruleCode: '',
    areaAId: null,
    areaBId: null,
    interlockMode: 'BIDIRECTIONAL',
    areaADoorIds: '',
    areaBDoorIds: '',
    unlockCondition: 'MANUAL',
    unlockDelaySeconds: 0,
    priority: 100,
    description: ''
  });
  modalVisible.value = true;
};

const handleEdit = (record) => {
  modalTitle.value = '编辑互锁规则';
  Object.assign(formData, record);
  modalVisible.value = true;
};

const handleTest = async (record) => {
  try {
    // TODO: 调用API测试规则
    message.success('测试成功');
  } catch (error) {
    console.error('[互锁管理] 测试失败:', error);
    message.error('测试失败');
  }
};

const handleToggleEnabled = async (record, checked) => {
  try {
    // TODO: 调用API更新状态
    message.success(checked ? '已启用' : '已禁用');
    loadData();
  } catch (error) {
    console.error('[互锁管理] 更新状态失败:', error);
    message.error('更新状态失败');
  }
};

const handleDelete = async (record) => {
  try {
    // TODO: 调用API删除规则
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('[互锁管理] 删除失败:', error);
    message.error('删除失败');
  }
};

const handleModalOk = async () => {
  try {
    await formRef.value.validate();
    // TODO: 调用API保存数据
    message.success(formData.ruleId ? '更新成功' : '新增成功');
    modalVisible.value = false;
    loadData();
  } catch (error) {
    console.error('[互锁管理] 保存失败:', error);
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

const getInterlockModeDesc = (mode) => {
  const map = {
    'BIDIRECTIONAL': '双向互锁',
    'UNIDIRECTIONAL': '单向互锁'
  };
  return map[mode] || mode;
};

const getInterlockModeColor = (mode) => {
  const map = {
    'BIDIRECTIONAL': 'red',
    'UNIDIRECTIONAL': 'orange'
  };
  return map[mode] || 'default';
};

const getUnlockConditionDesc = (condition) => {
  const map = {
    'MANUAL': '手动解锁',
    'TIMER': '定时解锁',
    'AUTO': '自动解锁'
  };
  return map[condition] || condition;
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.access-interlock-container {
  .action-bar {
    margin-bottom: 16px;
  }
}
</style>
