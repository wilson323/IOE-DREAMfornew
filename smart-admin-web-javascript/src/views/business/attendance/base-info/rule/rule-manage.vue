<template>
  <div class="rule-manage">
    <!-- 规则列表 -->
    <a-card title="考勤规则管理" :bordered="false">
      <!-- 工具栏 -->
      <template #extra>
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <template #icon><PlusOutlined /></template>
            新增规则
          </a-button>
          <a-button @click="showBatchOperations">
            批量操作
          </a-button>
        </a-space>
      </template>

      <!-- 搜索表单 -->
      <a-form layout="inline" style="margin-bottom: 16px">
        <a-form-item label="规则名称">
          <a-input
            v-model:value="searchForm.ruleName"
            placeholder="请输入规则名称"
            style="width: 200px"
            allowClear
          />
        </a-form-item>
        <a-form-item label="规则分类">
          <a-select
            v-model:value="searchForm.ruleCategory"
            placeholder="全部分类"
            style="width: 150px"
            allowClear
          >
            <a-select-option value="TIME">时间规则</a-select-option>
            <a-select-option value="LOCATION">地点规则</a-select-option>
            <a-select-option value="ABSENCE">缺勤规则</a-select-option>
            <a-select-option value="OVERTIME">加班规则</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="作用域">
          <a-select
            v-model:value="searchForm.ruleScope"
            placeholder="全部作用域"
            style="width: 120px"
            allowClear
          >
            <a-select-option value="GLOBAL">全局</a-select-option>
            <a-select-option value="DEPARTMENT">部门</a-select-option>
            <a-select-option value="USER">个人</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.ruleStatus"
            placeholder="全部状态"
            style="width: 100px"
            allowClear
          >
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadRuleList">
              查询
            </a-button>
            <a-button @click="resetSearch">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 规则表格 -->
      <a-table
        :columns="ruleColumns"
        :data-source="ruleList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="ruleId"
        :row-selection="{
          selectedRowKeys: selectedKeys,
          onChange: onSelectChange
        }"
        :scroll="{ x: 1800 }"
      >
        <!-- 自定义单元格渲染 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'ruleCategory'">
            <a-tag :color="getCategoryColor(record.ruleCategory)">
              {{ getCategoryName(record.ruleCategory) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'ruleScope'">
            <a-tag :color="getScopeColor(record.ruleScope)">
              {{ getScopeName(record.ruleScope) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'rulePriority'">
            <a-tag :color="getPriorityColor(record.rulePriority)">
              {{ record.rulePriority }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'ruleStatus'">
            <a-switch
              :checked="record.ruleStatus === 1"
              @change="(checked) => handleToggleStatus(record.ruleId, checked)"
            />
          </template>

          <template v-else-if="column.key === 'effectiveTime'">
            <span v-if="record.effectiveStartTime && record.effectiveEndTime">
              {{ record.effectiveStartTime }} ~ {{ record.effectiveEndTime }}
            </span>
            <span v-else class="text-gray">全天有效</span>
          </template>

          <template v-else-if="column.key === 'effectiveDays'">
            <a-tag v-for="day in parseDays(record.effectiveDays)" :key="day" color="blue">
              {{ day }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewRule(record)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="editRule(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定删除该规则吗？"
                @confirm="deleteRule(record.ruleId)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑规则Modal -->
    <a-modal
      v-model:visible="ruleModalVisible"
      :title="isEditMode ? '编辑考勤规则' : '新增考勤规则'"
      width="900px"
      @ok="handleSaveRule"
      @cancel="handleCancelEdit"
      :confirm-loading="saving"
    >
      <a-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="ruleFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规则名称" name="ruleName">
              <a-input v-model:value="ruleForm.ruleName" placeholder="请输入规则名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规则分类" name="ruleCategory">
              <a-select v-model:value="ruleForm.ruleCategory" placeholder="请选择规则分类">
                <a-select-option value="TIME">时间规则</a-select-option>
                <a-select-option value="LOCATION">地点规则</a-select-option>
                <a-select-option value="ABSENCE">缺勤规则</a-select-option>
                <a-select-option value="OVERTIME">加班规则</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规则类型" name="ruleType">
              <a-input v-model:value="ruleForm.ruleType" placeholder="请输入规则类型" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规则作用域" name="ruleScope">
              <a-select v-model:value="ruleForm.ruleScope" placeholder="请选择作用域">
                <a-select-option value="GLOBAL">全局</a-select-option>
                <a-select-option value="DEPARTMENT">部门</a-select-option>
                <a-select-option value="USER">个人</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="优先级" name="rulePriority">
              <a-input-number
                v-model:value="ruleForm.rulePriority"
                :min="1"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="执行顺序" name="executionOrder">
              <a-input-number
                v-model:value="ruleForm.executionOrder"
                :min="1"
                :max="100"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="生效开始时间" name="effectiveStartTime">
              <a-time-picker
                v-model:value="ruleForm.effectiveStartTime"
                format="HH:mm"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="生效结束时间" name="effectiveEndTime">
              <a-time-picker
                v-model:value="ruleForm.effectiveEndTime"
                format="HH:mm"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="生效日期" name="effectiveDays">
          <a-checkbox-group v-model:value="ruleForm.effectiveDays">
            <a-checkbox :value="1">周一</a-checkbox>
            <a-checkbox :value="2">周二</a-checkbox>
            <a-checkbox :value="3">周三</a-checkbox>
            <a-checkbox :value="4">周四</a-checkbox>
            <a-checkbox :value="5">周五</a-checkbox>
            <a-checkbox :value="6">周六</a-checkbox>
            <a-checkbox :value="7">周日</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="规则配置" name="ruleConfig">
          <RuleConfigEditor
            v-model="ruleForm"
            @change="handleRuleConfigChange"
          />
        </a-form-item>

        <a-form-item label="规则状态" name="ruleStatus">
          <a-radio-group v-model:value="ruleForm.ruleStatus">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="规则描述" name="description">
          <a-textarea
            v-model:value="ruleForm.description"
            placeholder="请输入规则描述"
            :rows="2"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看规则详情Modal -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="规则详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered :column="2" v-if="currentRule">
        <a-descriptions-item label="规则名称" :span="2">
          {{ currentRule.ruleName }}
        </a-descriptions-item>
        <a-descriptions-item label="规则编码" :span="2">
          {{ currentRule.ruleCode }}
        </a-descriptions-item>
        <a-descriptions-item label="规则分类">
          <a-tag :color="getCategoryColor(currentRule.ruleCategory)">
            {{ getCategoryName(currentRule.ruleCategory) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="规则类型">
          {{ currentRule.ruleType }}
        </a-descriptions-item>
        <a-descriptions-item label="作用域">
          <a-tag :color="getScopeColor(currentRule.ruleScope)">
            {{ getScopeName(currentRule.ruleScope) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="优先级">
          <a-tag :color="getPriorityColor(currentRule.rulePriority)">
            {{ currentRule.rulePriority }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="生效时间" :span="2">
          {{ currentRule.effectiveStartTime }} ~ {{ currentRule.effectiveEndTime }}
        </a-descriptions-item>
        <a-descriptions-item label="生效日期" :span="2">
          <a-tag v-for="day in parseDays(currentRule.effectiveDays)" :key="day" color="blue">
            {{ day }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="规则条件" :span="2">
          <pre>{{ formatJson(currentRule.ruleCondition) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="规则动作" :span="2">
          <pre>{{ formatJson(currentRule.ruleAction) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="规则描述" :span="2">
          {{ currentRule.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">
          {{ currentRule.createTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import RuleConfigEditor from '/@/components/attendance/rule-config-editor.vue';

const selectedKeys = ref([]);
const ruleList = ref([]);
const loading = ref(false);
const saving = ref(false);
const ruleModalVisible = ref(false);
const detailModalVisible = ref(false);
const isEditMode = ref(false);
const currentRule = ref(null);
const ruleFormRef = ref();

const searchForm = reactive({
  ruleName: '',
  ruleCategory: undefined,
  ruleScope: undefined,
  ruleStatus: undefined,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

const ruleForm = reactive({
  ruleName: '',
  ruleCategory: undefined,
  ruleType: '',
  ruleScope: undefined,
  rulePriority: 1,
  executionOrder: 1,
  effectiveStartTime: null,
  effectiveEndTime: null,
  effectiveDays: [1, 2, 3, 4, 5],
  ruleCondition: '',
  ruleAction: '',
  ruleStatus: 1,
  description: '',
});

const ruleFormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleCategory: [{ required: true, message: '请选择规则分类', trigger: 'change' }],
  ruleType: [{ required: true, message: '请输入规则类型', trigger: 'blur' }],
  ruleScope: [{ required: true, message: '请选择作用域', trigger: 'change' }],
};

const ruleColumns = [
  { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName', width: 200 },
  { title: '规则编码', dataIndex: 'ruleCode', key: 'ruleCode', width: 180 },
  { title: '规则分类', dataIndex: 'ruleCategory', key: 'ruleCategory', width: 120 },
  { title: '作用域', dataIndex: 'ruleScope', key: 'ruleScope', width: 100 },
  { title: '优先级', dataIndex: 'rulePriority', key: 'rulePriority', width: 100 },
  { title: '生效时间', key: 'effectiveTime', width: 180 },
  { title: '生效日期', key: 'effectiveDays', width: 200 },
  { title: '状态', dataIndex: 'ruleStatus', key: 'ruleStatus', width: 80 },
  { title: '操作', key: 'action', fixed: 'right', width: 180 },
];

const getCategoryName = (category) => {
  const map = {
    TIME: '时间规则',
    LOCATION: '地点规则',
    ABSENCE: '缺勤规则',
    OVERTIME: '加班规则',
  };
  return map[category] || category;
};

const getCategoryColor = (category) => {
  const map = { TIME: 'blue', LOCATION: 'green', ABSENCE: 'orange', OVERTIME: 'purple' };
  return map[category] || 'default';
};

const getScopeName = (scope) => {
  const map = { GLOBAL: '全局', DEPARTMENT: '部门', USER: '个人' };
  return map[scope] || scope;
};

const getScopeColor = (scope) => {
  const map = { GLOBAL: 'red', DEPARTMENT: 'blue', USER: 'green' };
  return map[scope] || 'default';
};

const getPriorityColor = (priority) => {
  if (priority <= 3) return 'red';
  if (priority <= 6) return 'orange';
  return 'green';
};

const parseDays = (daysStr) => {
  if (!daysStr) return [];
  const days = daysStr.split(',').map(Number);
  const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  return days.map(d => dayNames[d]);
};

const formatJson = (jsonStr) => {
  if (!jsonStr) return '-';
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2);
  } catch {
    return jsonStr;
  }
};

const loadRuleList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
    };

    // TODO: 调用API
    // const result = await attendanceApi.queryRulePage(params);
    // if (result.code === 200) {
    //   ruleList.value = result.data.list;
    //   pagination.total = result.data.total;
    // }

    // 模拟数据
    ruleList.value = [];
    pagination.total = 0;
  } catch (error) {
    message.error('加载规则列表失败');
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadRuleList();
};

const resetSearch = () => {
  searchForm.ruleName = '';
  searchForm.ruleCategory = undefined;
  searchForm.ruleScope = undefined;
  searchForm.ruleStatus = undefined;
  pagination.current = 1;
  loadRuleList();
};

const onSelectChange = (keys) => {
  selectedKeys.value = keys;
};

const showAddModal = () => {
  isEditMode.value = false;
  resetRuleForm();
  ruleModalVisible.value = true;
};

const editRule = (record) => {
  isEditMode.value = true;
  Object.assign(ruleForm, {
    ...record,
    effectiveDays: record.effectiveDays ? record.effectiveDays.split(',').map(Number) : [1, 2, 3, 4, 5],
    effectiveStartTime: record.effectiveStartTime ? dayjs(record.effectiveStartTime, 'HH:mm') : null,
    effectiveEndTime: record.effectiveEndTime ? dayjs(record.effectiveEndTime, 'HH:mm') : null,
  });
  currentRule.value = record;
  ruleModalVisible.value = true;
};

const viewRule = (record) => {
  currentRule.value = record;
  detailModalVisible.value = true;
};

const handleSaveRule = async () => {
  try {
    await ruleFormRef.value.validateFields();
    saving.value = true;

    const params = {
      ...ruleForm,
      effectiveStartTime: ruleForm.effectiveStartTime ? ruleForm.effectiveStartTime.format('HH:mm') : null,
      effectiveEndTime: ruleForm.effectiveEndTime ? ruleForm.effectiveEndTime.format('HH:mm') : null,
      effectiveDays: ruleForm.effectiveDays.join(','),
    };

    // TODO: 调用API
    // if (isEditMode.value) {
    //   await attendanceApi.updateRule(currentRule.value.ruleId, params);
    // } else {
    //   await attendanceApi.createRule(params);
    // }

    message.success(isEditMode.value ? '更新成功' : '创建成功');
    ruleModalVisible.value = false;
    loadRuleList();
  } catch (error) {
    console.error('保存规则失败', error);
  } finally {
    saving.value = false;
  }
};

const handleCancelEdit = () => {
  ruleModalVisible.value = false;
  resetRuleForm();
};

const resetRuleForm = () => {
  Object.assign(ruleForm, {
    ruleName: '',
    ruleCategory: undefined,
    ruleType: '',
    ruleScope: undefined,
    rulePriority: 1,
    executionOrder: 1,
    effectiveStartTime: null,
    effectiveEndTime: null,
    effectiveDays: [1, 2, 3, 4, 5],
    ruleCondition: '',
    ruleAction: '',
    ruleStatus: 1,
    description: '',
  });
  currentRule.value = null;
};

const deleteRule = async (ruleId) => {
  try {
    // TODO: 调用API
    // await attendanceApi.deleteRule(ruleId);
    message.success('删除成功');
    loadRuleList();
  } catch (error) {
    message.error('删除失败');
  }
};

const handleToggleStatus = async (ruleId, checked) => {
  try {
    // TODO: 调用API
    // await attendanceApi.updateRule(ruleId, { ruleStatus: checked ? 1 : 0 });
    message.success('状态更新成功');
    loadRuleList();
  } catch (error) {
    message.error('状态更新失败');
  }
};

const showBatchOperations = () => {
  if (selectedKeys.value.length === 0) {
    message.warning('请先选择要操作的规则');
    return;
  }
  // TODO: 实现批量操作
  message.info(`批量删除功能开发中，已选择 ${selectedKeys.value.length} 条规则`);
};

// 处理规则配置变化
const handleRuleConfigChange = (config) => {
  // 更新 ruleCondition 和 ruleAction
  ruleForm.ruleCondition = config.ruleCondition || '';
  ruleForm.ruleAction = config.ruleAction || '';
};

// 初始化
loadRuleList();
</script>

<style scoped>
.rule-manage {
  padding: 16px;
}

.text-gray {
  color: #999;
}
</style>
