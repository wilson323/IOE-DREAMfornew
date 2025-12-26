<template>
  <div class="schedule-rule-manage">
    <!-- 规则列表 -->
    <a-card title="排班规则管理" :bordered="false">
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
        <a-form-item label="规则类型">
          <a-select
            v-model:value="searchForm.ruleType"
            placeholder="全部类型"
            style="width: 150px"
            allowClear
          >
            <a-select-option :value="1">员工约束</a-select-option>
            <a-select-option :value="2">班次约束</a-select-option>
            <a-select-option :value="3">日期约束</a-select-option>
            <a-select-option :value="4">自定义规则</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.enabled"
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
        :scroll="{ x: 1500 }"
      >
        <!-- 自定义单元格渲染 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'ruleType'">
            <a-tag :color="getRuleTypeColor(record.ruleType)">
              {{ getRuleTypeName(record.ruleType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'priority'">
            <a-tag :color="getPriorityColor(record.priority)">
              {{ record.priority }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled === 1"
              @change="(checked) => handleToggleEnable(record.ruleId, checked)"
            />
          </template>

          <template v-else-if="column.key === 'effectiveTime'">
            <span v-if="record.effectiveTime && record.expireTime">
              {{ record.effectiveTime }} ~ {{ record.expireTime }}
            </span>
            <span v-else class="text-gray">永久有效</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewRule(record)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="editRule(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="testRule(record)">
                快速测试
              </a-button>
              <a-button type="link" size="small" @click="openAdvancedTester(record)">
                高级测试
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
      :title="isEditMode ? '编辑规则' : '新增规则'"
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
            <a-form-item label="规则类型" name="ruleType">
              <a-select v-model:value="ruleForm.ruleType" placeholder="请选择规则类型">
                <a-select-option :value="1">员工约束</a-select-option>
                <a-select-option :value="2">班次约束</a-select-option>
                <a-select-option :value="3">日期约束</a-select-option>
                <a-select-option :value="4">自定义规则</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="优先级" name="priority">
              <a-input-number
                v-model:value="ruleForm.priority"
                :min="1"
                :max="100"
                style="width: 100%"
              />
              <div class="text-gray text-sm">数字越大，优先级越高</div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="生效日期" name="effectiveDateRange">
              <a-range-picker
                v-model:value="ruleForm.effectiveDateRange"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="规则表达式" name="expression" required>
          <a-textarea
            v-model:value="ruleForm.expression"
            placeholder="请输入Aviator表达式，例如：consecutiveWorkDays >= 5 && consecutiveWorkDays <= 7"
            :rows="4"
          />
          <div class="text-gray text-sm mt-1">
            支持变量：employeeId, date, shiftId, consecutiveWorkDays, restDays, isWeekend, isHoliday
          </div>
        </a-form-item>

        <a-form-item label="规则描述">
          <a-textarea
            v-model:value="ruleForm.description"
            placeholder="请输入规则描述"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="错误提示">
          <a-input
            v-model:value="ruleForm.errorMessage"
            placeholder="违反规则时的提示信息"
          />
        </a-form-item>

        <a-form-item label="启用状态">
          <a-switch v-model:checked="ruleForm.enabledChecked" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看规则Modal -->
    <a-modal
      v-model:visible="viewModalVisible"
      title="规则详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered :column="2" v-if="currentRule">
        <a-descriptions-item label="规则名称" :span="2">
          {{ currentRule.ruleName }}
        </a-descriptions-item>
        <a-descriptions-item label="规则类型">
          <a-tag :color="getRuleTypeColor(currentRule.ruleType)">
            {{ getRuleTypeName(currentRule.ruleType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="优先级">
          <a-tag :color="getPriorityColor(currentRule.priority)">
            {{ currentRule.priority }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="规则表达式" :span="2">
          <pre class="expression-pre">{{ currentRule.expression }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="规则描述" :span="2">
          {{ currentRule.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="生效时间" :span="2">
          <span v-if="currentRule.effectiveTime && currentRule.expireTime">
            {{ currentRule.effectiveTime }} ~ {{ currentRule.expireTime }}
          </span>
          <span v-else>永久有效</span>
        </a-descriptions-item>
        <a-descriptions-item label="启用状态">
          <a-tag :color="currentRule.enabled === 1 ? 'success' : 'default'">
            {{ currentRule.enabled === 1 ? '已启用' : '已禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">
          {{ currentRule.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间" :span="2">
          {{ currentRule.updateTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 测试规则Modal -->
    <a-modal
      v-model:visible="testModalVisible"
      title="规则测试"
      width="800px"
      @ok="handleExecuteTest"
      :confirm-loading="testing"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="规则表达式">
          <a-textarea
            :value="testRuleExpression"
            readonly
            :rows="3"
          />
        </a-form-item>

        <a-divider>测试上下文</a-divider>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="员工ID">
              <a-input-number
                v-model:value="testContext.employeeId"
                :min="1"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="日期">
              <a-date-picker
                v-model:value="testContext.date"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="班次ID">
              <a-input-number
                v-model:value="testContext.shiftId"
                :min="1"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="连续工作天数">
              <a-input-number
                v-model:value="testContext.consecutiveWorkDays"
                :min="0"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="休息天数">
              <a-input-number
                v-model:value="testContext.restDays"
                :min="0"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="是否周末">
              <a-switch v-model:checked="testContext.isWeekend" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="是否节假日">
          <a-switch v-model:checked="testContext.isHoliday" />
        </a-form-item>
      </a-form>

      <a-divider>测试结果</a-divider>

      <a-alert
        v-if="testResult !== null"
        :type="testResult ? 'success' : 'warning'"
        :message="testResult ? '规则验证通过' : '规则验证失败'"
        show-icon
      >
        <template #description>
          <div>返回值：{{ testResult }}</div>
        </template>
      </a-alert>
    </a-modal>

    <!-- 批量操作Modal -->
    <a-modal
      v-model:visible="batchModalVisible"
      title="批量操作"
      width="600px"
      @ok="handleBatchOperation"
      :confirm-loading="batchOperating"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="操作类型">
          <a-radio-group v-model:value="batchOperation.type">
            <a-radio value="enable">批量启用</a-radio>
            <a-radio value="disable">批量禁用</a-radio>
            <a-radio value="delete">批量删除</a-radio>
            <a-radio value="priority">批量设置优先级</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item
          v-if="batchOperation.type === 'priority'"
          label="优先级"
        >
          <a-input-number
            v-model:value="batchOperation.priority"
            :min="1"
            :max="100"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="已选择">
          <a-tag color="blue">{{ selectedRowKeys.length }} 条规则</a-tag>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { smartScheduleApi } from '@/api/business/attendance/smart-schedule-api';

const router = useRouter();

// 搜索表单
const searchForm = reactive({
  ruleName: '',
  ruleType: undefined,
  enabled: undefined
});

// 规则列表数据
const ruleList = ref([]);
const loading = ref(false);
const selectedRowKeys = ref([]);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

// 规则表格列定义
const ruleColumns = ref([
  {
    title: '规则ID',
    dataIndex: 'ruleId',
    key: 'ruleId',
    width: 100
  },
  {
    title: '规则名称',
    dataIndex: 'ruleName',
    key: 'ruleName',
    width: 200
  },
  {
    title: '规则类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 120
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 100
  },
  {
    title: '规则表达式',
    dataIndex: 'expression',
    key: 'expression',
    width: 300,
    ellipsis: true
  },
  {
    title: '生效时间',
    dataIndex: 'effectiveTime',
    key: 'effectiveTime',
    width: 200
  },
  {
    title: '启用状态',
    dataIndex: 'enabled',
    key: 'enabled',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 200
  }
]);

// 规则表单
const ruleModalVisible = ref(false);
const isEditMode = ref(false);
const saving = ref(false);
const ruleFormRef = ref();
const ruleForm = reactive({
  ruleId: null,
  ruleName: '',
  ruleType: undefined,
  priority: 50,
  effectiveDateRange: [],
  expression: '',
  description: '',
  errorMessage: '',
  enabledChecked: true
});

const ruleFormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }],
  expression: [{ required: true, message: '请输入规则表达式', trigger: 'blur' }]
};

// 查看规则
const viewModalVisible = ref(false);
const currentRule = ref(null);

// 测试规则
const testModalVisible = ref(false);
const testRuleExpression = ref('');
const testing = ref(false);
const testResult = ref(null);
const testContext = reactive({
  employeeId: 1,
  date: dayjs(),
  shiftId: 1,
  consecutiveWorkDays: 5,
  restDays: 2,
  isWeekend: false,
  isHoliday: false
});

// 批量操作
const batchModalVisible = ref(false);
const batchOperating = ref(false);
const batchOperation = reactive({
  type: 'enable',
  priority: 50
});

// 加载规则列表
const loadRuleList = async () => {
  loading.value = true;
  try {
    const res = await smartScheduleApi.queryRulePage({
      ruleName: searchForm.ruleName,
      ruleType: searchForm.ruleType,
      enabled: searchForm.enabled,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    });
    ruleList.value = res.data.list;
    pagination.total = res.data.total;
  } catch (error) {
    message.error('加载规则列表失败');
  } finally {
    loading.value = false;
  }
};

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadRuleList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.ruleName = '';
  searchForm.ruleType = undefined;
  searchForm.enabled = undefined;
  loadRuleList();
};

// 显示新增Modal
const showAddModal = () => {
  isEditMode.value = false;
  resetRuleForm();
  ruleModalVisible.value = true;
};

// 重置规则表单
const resetRuleForm = () => {
  ruleForm.ruleId = null;
  ruleForm.ruleName = '';
  ruleForm.ruleType = undefined;
  ruleForm.priority = 50;
  ruleForm.effectiveDateRange = [];
  ruleForm.expression = '';
  ruleForm.description = '';
  ruleForm.errorMessage = '';
  ruleForm.enabledChecked = true;
};

// 编辑规则
const editRule = (record) => {
  isEditMode.value = true;
  ruleForm.ruleId = record.ruleId;
  ruleForm.ruleName = record.ruleName;
  ruleForm.ruleType = record.ruleType;
  ruleForm.priority = record.priority;
  ruleForm.effectiveDateRange = record.effectiveTime && record.expireTime
    ? [dayjs(record.effectiveTime), dayjs(record.expireTime)]
    : [];
  ruleForm.expression = record.expression;
  ruleForm.description = record.description;
  ruleForm.errorMessage = record.errorMessage;
  ruleForm.enabledChecked = record.enabled === 1;
  ruleModalVisible.value = true;
};

// 保存规则
const handleSaveRule = async () => {
  try {
    await ruleFormRef.value.validate();
    saving.value = true;

    const formData = {
      ruleName: ruleForm.ruleName,
      ruleType: ruleForm.ruleType,
      priority: ruleForm.priority,
      expression: ruleForm.expression,
      description: ruleForm.description,
      errorMessage: ruleForm.errorMessage,
      enabled: ruleForm.enabledChecked ? 1 : 0
    };

    if (ruleForm.effectiveDateRange.length === 2) {
      formData.effectiveTime = ruleForm.effectiveDateRange[0].format('YYYY-MM-DD');
      formData.expireTime = ruleForm.effectiveDateRange[1].format('YYYY-MM-DD');
    }

    if (isEditMode.value) {
      await smartScheduleApi.updateRule(ruleForm.ruleId, formData);
      message.success('规则更新成功');
    } else {
      await smartScheduleApi.createRule(formData);
      message.success('规则创建成功');
    }

    ruleModalVisible.value = false;
    loadRuleList();
  } catch (error) {
    if (error.errorFields) {
      message.error('请检查表单填写');
    } else {
      message.error('保存规则失败');
    }
  } finally {
    saving.value = false;
  }
};

// 取消编辑
const handleCancelEdit = () => {
  ruleModalVisible.value = false;
  resetRuleForm();
};

// 查看规则
const viewRule = (record) => {
  currentRule.value = record;
  viewModalVisible.value = true;
};

// 删除规则
const deleteRule = async (ruleId) => {
  try {
    await smartScheduleApi.deleteRule(ruleId);
    message.success('规则删除成功');
    loadRuleList();
  } catch (error) {
    message.error('删除规则失败');
  }
};

// 切换启用状态
const handleToggleEnable = async (ruleId, checked) => {
  try {
    if (checked) {
      await smartScheduleApi.enableRule(ruleId);
      message.success('规则已启用');
    } else {
      await smartScheduleApi.disableRule(ruleId);
      message.success('规则已禁用');
    }
    loadRuleList();
  } catch (error) {
    message.error('操作失败');
  }
};

// 测试规则
const testRule = (record) => {
  testRuleExpression.value = record.expression;
  testResult.value = null;
  testModalVisible.value = true;
};

// 打开高级测试工具
const openAdvancedTester = (record) => {
  // 跳转到专门的规则测试页面
  router.push({
    path: '/business/attendance/rule-tester',
    query: {
      ruleId: record.ruleId,
      ruleName: record.ruleName
    }
  });
};

// 执行测试
const handleExecuteTest = async () => {
  testing.value = true;
  try {
    const context = {
      employeeId: testContext.employeeId,
      date: testContext.date.format('YYYY-MM-DD'),
      shiftId: testContext.shiftId,
      consecutiveWorkDays: testContext.consecutiveWorkDays,
      restDays: testContext.restDays,
      isWeekend: testContext.isWeekend,
      isHoliday: testContext.isHoliday
    };

    const res = await smartScheduleApi.testRuleExecution(testRuleExpression.value, context);
    testResult.value = res.data;
  } catch (error) {
    message.error('测试执行失败');
  } finally {
    testing.value = false;
  }
};

// 显示批量操作
const showBatchOperations = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要操作的规则');
    return;
  }
  batchModalVisible.value = true;
};

// 执行批量操作
const handleBatchOperation = async () => {
  batchOperating.value = true;
  try {
    const ruleIds = selectedRowKeys.value;

    switch (batchOperation.type) {
      case 'enable':
        await Promise.all(ruleIds.map(id => smartScheduleApi.enableRule(id)));
        message.success('批量启用成功');
        break;
      case 'disable':
        await Promise.all(ruleIds.map(id => smartScheduleApi.disableRule(id)));
        message.success('批量禁用成功');
        break;
      case 'delete':
        await Promise.all(ruleIds.map(id => smartScheduleApi.deleteRule(id)));
        message.success('批量删除成功');
        break;
      case 'priority':
        await Promise.all(ruleIds.map(id =>
          smartScheduleApi.updateRule(id, { priority: batchOperation.priority })
        ));
        message.success('批量设置优先级成功');
        break;
    }

    batchModalVisible.value = false;
    selectedRowKeys.value = [];
    loadRuleList();
  } catch (error) {
    message.error('批量操作失败');
  } finally {
    batchOperating.value = false;
  }
};

// 获取规则类型名称
const getRuleTypeName = (type) => {
  const types = {
    1: '员工约束',
    2: '班次约束',
    3: '日期约束',
    4: '自定义规则'
  };
  return types[type] || '未知';
};

// 获取规则类型颜色
const getRuleTypeColor = (type) => {
  const colors = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'purple'
  };
  return colors[type] || 'default';
};

// 获取优先级颜色
const getPriorityColor = (priority) => {
  if (priority >= 80) return 'red';
  if (priority >= 60) return 'orange';
  if (priority >= 40) return 'blue';
  return 'default';
};

// 初始化
onMounted(() => {
  loadRuleList();
});
</script>

<style lang="scss" scoped>
.schedule-rule-manage {
  padding: 16px;
}

.expression-pre {
  background-color: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.text-gray {
  color: #999;
}

.text-sm {
  font-size: 12px;
}

.mt-1 {
  margin-top: 4px;
}
</style>
