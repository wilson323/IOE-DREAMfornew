<template>
  <div class="rule-config-container">
    <!-- 页面标题 -->
    <a-page-header
      title="考勤规则配置"
      sub-title="配置考勤异常判定规则"
    />

    <!-- 操作按钮栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="showCreateModal">
          <template #icon><PlusOutlined /></template>
          新增规则
        </a-button>
        <a-button @click="fetchRules">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 规则配置表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="configId"
        :scroll="{ x: 2000 }"
      >
        <!-- 规则状态 -->
        <template #ruleStatus="{ record }">
          <a-switch
            :checked="record.ruleStatus === 1"
            @change="handleStatusChange(record)"
          />
        </template>

        <!-- 迟到规则 -->
        <template #lateRule="{ record }">
          <div v-if="record.lateCheckEnabled">
            <div>判定: {{ record.lateMinutes }}分钟</div>
            <div>严重: {{ record.seriousLateMinutes }}分钟</div>
          </div>
          <span v-else>-</span>
        </template>

        <!-- 早退规则 -->
        <template #earlyRule="{ record }">
          <div v-if="record.earlyCheckEnabled">
            <div>判定: {{ record.earlyMinutes }}分钟</div>
            <div>严重: {{ record.seriousEarlyMinutes }}分钟</div>
          </div>
          <span v-else>-</span>
        </template>

        <!-- 弹性时间 -->
        <template #flexibleRule="{ record }">
          <div v-if="record.flexibleStartEnabled || record.flexibleEndEnabled">
            <div v-if="record.flexibleStartEnabled">
              上班: +{{ record.flexibleStartMinutes }}分钟
            </div>
            <div v-if="record.flexibleEndEnabled">
              下班: +{{ record.flexibleEndMinutes }}分钟
            </div>
          </div>
          <span v-else>-</span>
        </template>

        <!-- 补卡规则 -->
        <template #supplementRule="{ record }">
          <div v-if="record.missingCardCheckEnabled">
            <div>每月: {{ record.allowedSupplementTimes }}次</div>
            <div>限制: {{ record.supplementDaysLimit }}天内</div>
          </div>
          <span v-else>-</span>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleView(record)">
              查看
            </a-button>
            <a-button type="link" size="small" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              danger
              @click="handleDelete(record)"
              :disabled="record.applyScope === 'ALL'"
            >
              删除
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑规则弹窗 -->
    <a-modal
      v-model:visible="ruleModalVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="800px"
      @ok="handleSubmitRule"
    >
      <a-form
        ref="ruleFormRef"
        :model="ruleForm"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 14 }"
        :rules="ruleFormRules"
      >
        <a-tabs>
          <!-- 基本信息 -->
          <a-tab-pane key="basic" tab="基本信息">
            <a-form-item label="规则名称" name="ruleName" required>
              <a-input v-model:value="ruleForm.ruleName" placeholder="请输入规则名称" />
            </a-form-item>

            <a-form-item label="适用范围" name="applyScope" required>
              <a-select v-model:value="ruleForm.applyScope" placeholder="请选择适用范围">
                <a-select-option value="ALL">全局</a-select-option>
                <a-select-option value="DEPARTMENT">部门</a-select-option>
                <a-select-option value="SHIFT">班次</a-select-option>
                <a-select-option value="EMPLOYEE">员工</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="适用范围ID" name="scopeId" v-if="ruleForm.applyScope !== 'ALL'">
              <a-input-number v-model:value="ruleForm.scopeId" placeholder="请输入范围ID" style="width: 100%" />
            </a-form-item>

            <a-form-item label="规则描述" name="description">
              <a-textarea v-model:value="ruleForm.description" placeholder="请输入规则描述" :rows="3" />
            </a-form-item>
          </a-tab-pane>

          <!-- 迟到规则 -->
          <a-tab-pane key="late" tab="迟到规则">
            <a-form-item label="启用迟到检测">
              <a-switch v-model:checked="ruleForm.lateCheckEnabled" />
            </a-form-item>

            <template v-if="ruleForm.lateCheckEnabled">
              <a-form-item label="迟到判定分钟数">
                <a-input-number v-model:value="ruleForm.lateMinutes" :min="0" :max="60" />
                <span style="margin-left: 8px">分钟</span>
              </a-form-item>

              <a-form-item label="严重迟到分钟数">
                <a-input-number v-model:value="ruleForm.seriousLateMinutes" :min="0" :max="120" />
                <span style="margin-left: 8px">分钟</span>
              </a-form-item>

              <a-form-item label="处理方式">
                <a-select v-model:value="ruleForm.lateHandleType">
                  <a-select-option value="IGNORE">忽略</a-select-option>
                  <a-select-option value="WARNING">警告</a-select-option>
                  <a-select-option value="DEDUCT">扣款</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="扣款金额（元/小时）" v-if="ruleForm.lateHandleType === 'DEDUCT'">
                <a-input-number v-model:value="ruleForm.lateDeductAmount" :min="0" :precision="2" />
              </a-form-item>
            </template>
          </a-tab-pane>

          <!-- 早退规则 -->
          <a-tab-pane key="early" tab="早退规则">
            <a-form-item label="启用早退检测">
              <a-switch v-model:checked="ruleForm.earlyCheckEnabled" />
            </a-form-item>

            <template v-if="ruleForm.earlyCheckEnabled">
              <a-form-item label="早退判定分钟数">
                <a-input-number v-model:value="ruleForm.earlyMinutes" :min="0" :max="60" />
                <span style="margin-left: 8px">分钟</span>
              </a-form-item>

              <a-form-item label="严重早退分钟数">
                <a-input-number v-model:value="ruleForm.seriousEarlyMinutes" :min="0" :max="120" />
                <span style="margin-left: 8px">分钟</span>
              </a-form-item>

              <a-form-item label="处理方式">
                <a-select v-model:value="ruleForm.earlyHandleType">
                  <a-select-option value="IGNORE">忽略</a-select-option>
                  <a-select-option value="WARNING">警告</a-select-option>
                  <a-select-option value="DEDUCT">扣款</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="扣款金额（元/小时）" v-if="ruleForm.earlyHandleType === 'DEDUCT'">
                <a-input-number v-model:value="ruleForm.earlyDeductAmount" :min="0" :precision="2" />
              </a-form-item>
            </template>
          </a-tab-pane>

          <!-- 弹性时间 -->
          <a-tab-pane key="flexible" tab="弹性时间">
            <a-form-item label="启用弹性上班时间">
              <a-switch v-model:checked="ruleForm.flexibleStartEnabled" />
            </a-form-item>

            <a-form-item label="弹性上班分钟数" v-if="ruleForm.flexibleStartEnabled">
              <a-input-number v-model:value="ruleForm.flexibleStartMinutes" :min="0" :max="60" />
              <span style="margin-left: 8px">分钟</span>
            </a-form-item>

            <a-form-item label="启用弹性下班时间">
              <a-switch v-model:checked="ruleForm.flexibleEndEnabled" />
            </a-form-item>

            <a-form-item label="弹性下班分钟数" v-if="ruleForm.flexibleEndEnabled">
              <a-input-number v-model:value="ruleForm.flexibleEndMinutes" :min="0" :max="60" />
              <span style="margin-left: 8px">分钟</span>
            </a-form-item>
          </a-tab-pane>

          <!-- 补卡规则 -->
          <a-tab-pane key="supplement" tab="补卡规则">
            <a-form-item label="启用缺卡检测">
              <a-switch v-model:checked="ruleForm.missingCardCheckEnabled" />
            </a-form-item>

            <template v-if="ruleForm.missingCardCheckEnabled">
              <a-form-item label="每月允许补卡次数">
                <a-input-number v-model:value="ruleForm.allowedSupplementTimes" :min="-1" :max="10" />
                <span style="margin-left: 8px">次（-1表示不限制）</span>
              </a-form-item>

              <a-form-item label="补卡时间限制（天）">
                <a-input-number v-model:value="ruleForm.supplementDaysLimit" :min="1" :max="30" />
                <span style="margin-left: 8px">天（逾期不允许补卡）</span>
              </a-form-item>
            </template>
          </a-tab-pane>
        </a-tabs>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { anomalyApi } from '@/api/business/attendance/anomaly-api';

// 数据源
const dataSource = ref([]);
const loading = ref(false);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 规则弹窗
const ruleModalVisible = ref(false);
const isEdit = ref(false);
const ruleFormRef = ref(null);

const ruleForm = reactive({
  configId: null,
  ruleName: '',
  applyScope: 'ALL',
  scopeId: null,
  description: '',
  lateCheckEnabled: true,
  lateMinutes: 5,
  seriousLateMinutes: 30,
  lateHandleType: 'WARNING',
  lateDeductAmount: 50,
  earlyCheckEnabled: true,
  earlyMinutes: 5,
  seriousEarlyMinutes: 30,
  earlyHandleType: 'WARNING',
  earlyDeductAmount: 50,
  flexibleStartEnabled: false,
  flexibleStartMinutes: 15,
  flexibleEndEnabled: false,
  flexibleEndMinutes: 15,
  missingCardCheckEnabled: true,
  allowedSupplementTimes: 3,
  supplementDaysLimit: 3
});

const ruleFormRules = {
  ruleName: [{ required: true, message: '请输入规则名称' }],
  applyScope: [{ required: true, message: '请选择适用范围' }]
};

// 表格列配置
const columns = [
  {
    title: '规则名称',
    dataIndex: 'ruleName',
    key: 'ruleName',
    width: 150,
    fixed: 'left'
  },
  {
    title: '适用范围',
    dataIndex: 'applyScope',
    key: 'applyScope',
    width: 100
  },
  {
    title: '状态',
    key: 'ruleStatus',
    width: 80,
    slots: { customRender: 'ruleStatus' }
  },
  {
    title: '迟到规则',
    key: 'lateRule',
    width: 150,
    slots: { customRender: 'lateRule' }
  },
  {
    title: '早退规则',
    key: 'earlyRule',
    width: 150,
    slots: { customRender: 'earlyRule' }
  },
  {
    title: '弹性时间',
    key: 'flexibleRule',
    width: 150,
    slots: { customRender: 'flexibleRule' }
  },
  {
    title: '补卡规则',
    key: 'supplementRule',
    width: 150,
    slots: { customRender: 'supplementRule' }
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    slots: { customRender: 'action' }
  }
];

// 查询规则列表
const fetchRules = async () => {
  loading.value = true;
  try {
    const response = await anomalyApi.getRuleConfigPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    });

    if (response.data) {
      dataSource.value = response.data.list || [];
      pagination.total = response.data.total || 0;
    }
  } catch (error) {
    message.error('查询规则配置失败');
  } finally {
    loading.value = false;
  }
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchRules();
};

// 显示新增弹窗
const showCreateModal = () => {
  isEdit.value = false;
  Object.assign(ruleForm, {
    configId: null,
    ruleName: '',
    applyScope: 'ALL',
    scopeId: null,
    description: '',
    lateCheckEnabled: true,
    lateMinutes: 5,
    seriousLateMinutes: 30,
    lateHandleType: 'WARNING',
    lateDeductAmount: 50,
    earlyCheckEnabled: true,
    earlyMinutes: 5,
    seriousEarlyMinutes: 30,
    earlyHandleType: 'WARNING',
    earlyDeductAmount: 50,
    flexibleStartEnabled: false,
    flexibleStartMinutes: 15,
    flexibleEndEnabled: false,
    flexibleEndMinutes: 15,
    missingCardCheckEnabled: true,
    allowedSupplementTimes: 3,
    supplementDaysLimit: 3
  });
  ruleModalVisible.value = true;
};

// 查看规则
const handleView = (record) => {
  message.info('查看功能开发中');
};

// 编辑规则
const handleEdit = (record) => {
  isEdit.value = true;
  Object.assign(ruleForm, record);
  ruleModalVisible.value = true;
};

// 提交规则
const handleSubmitRule = async () => {
  try {
    await ruleFormRef.value.validate();

    if (isEdit.value) {
      await anomalyApi.updateRule(ruleForm.configId, ruleForm);
      message.success('更新成功');
    } else {
      await anomalyApi.createRule(ruleForm);
      message.success('创建成功');
    }

    ruleModalVisible.value = false;
    fetchRules();
  } catch (error) {
    message.error(isEdit.value ? '更新失败' : '创建失败');
  }
};

// 切换规则状态
const handleStatusChange = (record) => {
  const newStatus = record.ruleStatus === 1 ? 0 : 1;
  Modal.confirm({
    title: '确认修改',
    content: `确定要${newStatus === 1 ? '启用' : '禁用'}此规则吗？`,
    onOk: async () => {
      try {
        await anomalyApi.updateRuleStatus(record.configId, newStatus);
        record.ruleStatus = newStatus;
        message.success('状态修改成功');
      } catch (error) {
        message.error('状态修改失败');
      }
    }
  });
};

// 删除规则
const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除此规则吗？',
    onOk: async () => {
      try {
        await anomalyApi.deleteRule(record.configId);
        message.success('删除成功');
        fetchRules();
      } catch (error) {
        message.error('删除失败');
      }
    }
  });
};

// 初始化
onMounted(() => {
  fetchRules();
});
</script>

<style scoped lang="less">
.rule-config-container {
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
}

.table-card {
  .ant-table {
    font-size: 14px;
  }
}
</style>
