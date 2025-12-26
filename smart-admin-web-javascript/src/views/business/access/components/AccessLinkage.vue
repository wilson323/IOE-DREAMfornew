<template>
  <div class="access-linkage-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增联动规则
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
      <!-- 规则类型 -->
      <template #ruleType="{ record }">
        <a-tag :color="getRuleTypeColor(record.ruleType)">
          {{ getRuleTypeDesc(record.ruleType) }}
        </a-tag>
      </template>

      <!-- 触发类型 -->
      <template #triggerType="{ record }">
        <a-tag color="blue">
          {{ getTriggerTypeDesc(record.triggerType) }}
        </a-tag>
      </template>

      <!-- 执行动作 -->
      <template #actionType="{ record }">
        <a-tag :color="getActionTypeColor(record.actionType)">
          {{ getActionTypeDesc(record.actionType) }}
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

        <a-form-item label="联动类型" name="ruleType">
          <a-select v-model:value="formData.ruleType" placeholder="请选择联动类型">
            <a-select-option value="DOOR_LINKAGE">门联动</a-select-option>
            <a-select-option value="AREA_LINKAGE">区域联动</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="触发类型" name="triggerType">
          <a-select v-model:value="formData.triggerType" placeholder="请选择触发类型">
            <a-select-option value="OPEN_DOOR">开门</a-select-option>
            <a-select-option value="CLOSE_DOOR">关门</a-select-option>
            <a-select-option value="ALARM">告警</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="触发设备ID" name="triggerDeviceId">
          <a-input-number v-model:value="formData.triggerDeviceId" placeholder="请输入触发设备ID" style="width: 100%" />
        </a-form-item>

        <a-form-item label="执行动作" name="actionType">
          <a-select v-model:value="formData.actionType" placeholder="请选择执行动作">
            <a-select-option value="OPEN_DOOR">开门</a-select-option>
            <a-select-option value="CLOSE_DOOR">关门</a-select-option>
            <a-select-option value="LOCK">锁定</a-select-option>
            <a-select-option value="UNLOCK">解锁</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="目标设备ID" name="targetDeviceId">
          <a-input-number v-model:value="formData.targetDeviceId" placeholder="请输入目标设备ID" style="width: 100%" />
        </a-form-item>

        <a-form-item label="延迟秒数" name="delaySeconds">
          <a-input-number v-model:value="formData.delaySeconds" :min="0" placeholder="请输入延迟秒数" style="width: 100%" />
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
import { advancedApi } from '@/api/business/access/access-api';

const loading = ref(false);
const dataSource = ref([]);
const modalVisible = ref(false);
const modalTitle = ref('新增联动规则');
const formRef = ref();

const formData = reactive({
  ruleId: null,
  ruleName: '',
  ruleCode: '',
  ruleType: 'DOOR_LINKAGE',
  triggerType: 'OPEN_DOOR',
  triggerDeviceId: null,
  triggerDoorId: null,
  actionType: 'OPEN_DOOR',
  targetDeviceId: null,
  targetDoorId: null,
  delaySeconds: 0,
  priority: 100,
  description: ''
});

const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择联动类型', trigger: 'change' }],
  triggerType: [{ required: true, message: '请选择触发类型', trigger: 'change' }],
  triggerDeviceId: [{ required: true, message: '请输入触发设备ID', trigger: 'blur' }],
  actionType: [{ required: true, message: '请选择执行动作', trigger: 'change' }],
  targetDeviceId: [{ required: true, message: '请输入目标设备ID', trigger: 'blur' }]
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
  { title: '规则类型', dataIndex: 'ruleType', slots: { customRender: 'ruleType' }, width: 120 },
  { title: '触发类型', dataIndex: 'triggerType', slots: { customRender: 'triggerType' }, width: 120 },
  { title: '触发设备', dataIndex: 'triggerDeviceId', width: 120 },
  { title: '执行动作', dataIndex: 'actionType', slots: { customRender: 'actionType' }, width: 120 },
  { title: '目标设备', dataIndex: 'targetDeviceId', width: 120 },
  { title: '延迟(秒)', dataIndex: 'delaySeconds', width: 100 },
  { title: '优先级', dataIndex: 'priority', width: 80 },
  { title: '启用状态', dataIndex: 'enabled', slots: { customRender: 'enabled' }, width: 100 },
  { title: '操作', key: 'action', slots: { customRender: 'action' }, width: 200, fixed: 'right' }
];

const loadData = async () => {
  loading.value = true;
  try {
    const res = await advancedApi.queryLinkageRules({
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    });
    if (res.code === 200) {
      dataSource.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    console.error('[联动管理] 加载数据失败:', error);
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
  modalTitle.value = '新增联动规则';
  Object.assign(formData, {
    ruleId: null,
    ruleName: '',
    ruleCode: '',
    ruleType: 'DOOR_LINKAGE',
    triggerType: 'OPEN_DOOR',
    triggerDeviceId: null,
    triggerDoorId: null,
    actionType: 'OPEN_DOOR',
    targetDeviceId: null,
    targetDoorId: null,
    delaySeconds: 0,
    priority: 100,
    description: ''
  });
  modalVisible.value = true;
};

const handleEdit = (record) => {
  modalTitle.value = '编辑联动规则';
  Object.assign(formData, record);
  modalVisible.value = true;
};

const handleTest = async (record) => {
  try {
    const res = await advancedApi.testLinkage(record.ruleId);
    if (res.code === 200) {
      message.success('测试成功: ' + res.data);
    }
  } catch (error) {
    console.error('[联动管理] 测试失败:', error);
    message.error('测试失败');
  }
};

const handleToggleEnabled = async (record, checked) => {
  try {
    await advancedApi.updateLinkageEnabled(record.ruleId, checked ? 1 : 0);
    message.success(checked ? '已启用' : '已禁用');
    loadData();
  } catch (error) {
    console.error('[联动管理] 更新状态失败:', error);
    message.error('更新状态失败');
  }
};

const handleDelete = async (record) => {
  try {
    await advancedApi.deleteLinkage(record.ruleId);
    message.success('删除成功');
    loadData();
  } catch (error) {
    console.error('[联动管理] 删除失败:', error);
    message.error('删除失败');
  }
};

const handleModalOk = async () => {
  try {
    await formRef.value.validate();
    if (formData.ruleId) {
      await advancedApi.updateLinkage(formData.ruleId, formData);
      message.success('更新成功');
    } else {
      await advancedApi.addLinkage(formData);
      message.success('新增成功');
    }
    modalVisible.value = false;
    loadData();
  } catch (error) {
    console.error('[联动管理] 保存失败:', error);
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

const getRuleTypeDesc = (type) => {
  const map = {
    'DOOR_LINKAGE': '门联动',
    'AREA_LINKAGE': '区域联动'
  };
  return map[type] || type;
};

const getRuleTypeColor = (type) => {
  const map = {
    'DOOR_LINKAGE': 'green',
    'AREA_LINKAGE': 'blue'
  };
  return map[type] || 'default';
};

const getTriggerTypeDesc = (type) => {
  const map = {
    'OPEN_DOOR': '开门',
    'CLOSE_DOOR': '关门',
    'ALARM': '告警'
  };
  return map[type] || type;
};

const getActionTypeDesc = (type) => {
  const map = {
    'OPEN_DOOR': '开门',
    'CLOSE_DOOR': '关门',
    'LOCK': '锁定',
    'UNLOCK': '解锁'
  };
  return map[type] || type;
};

const getActionTypeColor = (type) => {
  const map = {
    'OPEN_DOOR': 'green',
    'CLOSE_DOOR': 'orange',
    'LOCK': 'red',
    'UNLOCK': 'blue'
  };
  return map[type] || 'default';
};

onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.access-linkage-container {
  .action-bar {
    margin-bottom: 16px;
  }
}
</style>
