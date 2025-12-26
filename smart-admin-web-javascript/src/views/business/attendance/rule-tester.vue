<template>
  <div class="rule-tester">
    <!-- 页面标题 -->
    <a-page-header
      title="规则测试工具"
      sub-title="测试考勤规则的执行效果"
      @back="handleBack"
    >
      <template #extra>
        <a-space>
          <a-button @click="loadTestScenarios">
            <template #icon><ReloadOutlined /></template>
            刷新场景
          </a-button>
          <a-button type="primary" @click="showBatchTestModal">
            批量测试
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <a-divider />

    <!-- 主要内容区 -->
    <a-row :gutter="16">
      <!-- 左侧：规则编辑区 -->
      <a-col :span="12">
        <a-card title="规则配置" :bordered="false">
          <!-- 规则类型选择 -->
          <a-form layout="vertical">
            <a-form-item label="测试模式">
              <a-radio-group v-model:value="testMode" @change="handleTestModeChange">
                <a-radio value="existing">测试已有规则</a-radio>
                <a-radio value="custom">自定义规则</a-radio>
              </a-radio-group>
            </a-form-item>

            <!-- 已有规则选择 -->
            <a-form-item v-if="testMode === 'existing'" label="选择规则">
              <a-select
                v-model:value="selectedRuleId"
                placeholder="请选择要测试的规则"
                show-search
                :filter-option="filterRuleOption"
                @change="handleRuleSelect"
                :loading="loadingRules"
              >
                <a-select-option
                  v-for="rule in ruleList"
                  :key="rule.ruleId"
                  :value="rule.ruleId"
                >
                  {{ rule.ruleName }} (ID: {{ rule.ruleId }})
                </a-select-option>
              </a-select>
            </a-form-item>

            <!-- 自定义规则条件 -->
            <a-form-item label="规则条件 (JSON格式)">
              <a-textarea
                v-model:value="ruleCondition"
                placeholder='例如: {"lateMinutes": 5, "isHoliday": false}'
                :rows="6"
                @blur="validateRuleSyntax"
              />
              <div class="form-help">
                支持Aviator表达式语法。变量：lateMinutes, earlyLeaveMinutes, overtimeHours等
              </div>
            </a-form-item>

            <!-- 自定义规则动作 -->
            <a-form-item label="规则动作 (JSON格式)">
              <a-textarea
                v-model:value="ruleAction"
                placeholder='例如: {"deductAmount": 50, "sendMessage": true}'
                :rows="6"
                @blur="validateRuleSyntax"
              />
              <div class="form-help">
                定义规则匹配后的执行动作，支持扣款、发送通知等
              </div>
            </a-form-item>

            <!-- 语法验证结果 -->
            <a-alert
              v-if="syntaxValidationResult !== null"
              :type="syntaxValidationResult ? 'success' : 'error'"
              :message="syntaxValidationResult ? 'JSON格式正确' : 'JSON格式错误'"
              :description="syntaxValidationErrorMessage"
              show-icon
              style="margin-bottom: 16px"
            />
          </a-form>
        </a-card>

        <!-- 测试场景选择 -->
        <a-card title="测试场景" style="margin-top: 16px" :bordered="false">
          <a-form layout="vertical">
            <a-form-item label="选择预设场景">
              <a-row :gutter="8">
                <a-col
                  v-for="scenario in testScenarios"
                  :key="scenario.scenarioCode"
                  :span="8"
                  style="margin-bottom: 8px"
                >
                  <a-button
                    :type="selectedScenario === scenario.scenarioCode ? 'primary' : 'default'"
                    block
                    @click="selectScenario(scenario.scenarioCode)"
                  >
                    {{ scenario.scenarioName }}
                  </a-button>
                </a-col>
              </a-row>
            </a-form-item>

            <a-form-item>
              <a-button type="link" @click="showCustomData = !showCustomData">
                {{ showCustomData ? '隐藏' : '显示' }}自定义测试数据
              </a-button>
            </a-form-item>

            <!-- 自定义测试数据 -->
            <a-collapse v-if="showCustomData" v-model:activeKey="customDataActiveKey">
              <a-collapse-panel key="1" header="自定义测试数据">
                <a-form layout="vertical">
                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="用户ID">
                        <a-input-number
                          v-model:value="testData.userId"
                          :min="1"
                          style="width: 100%"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="用户名">
                        <a-input v-model:value="testData.userName" />
                      </a-form-item>
                    </a-col>
                  </a-row>

                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="部门ID">
                        <a-input-number
                          v-model:value="testData.departmentId"
                          :min="1"
                          style="width: 100%"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="部门名称">
                        <a-input v-model:value="testData.departmentName" />
                      </a-form-item>
                    </a-col>
                  </a-row>

                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="打卡时间">
                        <a-time-picker
                          v-model:value="testData.punchTime"
                          format="HH:mm:ss"
                          style="width: 100%"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="打卡类型">
                        <a-select v-model:value="testData.punchType">
                          <a-select-option value="IN">上班打卡</a-select-option>
                          <a-select-option value="OUT">下班打卡</a-select-option>
                        </a-select>
                      </a-form-item>
                    </a-col>
                  </a-row>

                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="排班开始时间">
                        <a-time-picker
                          v-model:value="testData.scheduleStartTime"
                          format="HH:mm:ss"
                          style="width: 100%"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="排班结束时间">
                        <a-time-picker
                          v-model:value="testData.scheduleEndTime"
                          format="HH:mm:ss"
                          style="width: 100%"
                        />
                      </a-form-item>
                    </a-col>
                  </a-row>

                  <a-form-item label="考勤属性 (JSON)">
                    <a-textarea
                      v-model:value="testData.attendanceAttributesJson"
                      placeholder='例如: {"lateMinutes": 5, "overtimeHours": 2}'
                      :rows="4"
                    />
                  </a-form-item>

                  <a-form-item>
                    <a-space>
                      <a-button type="primary" @click="applyCustomData">
                        应用自定义数据
                      </a-button>
                      <a-button @click="resetCustomData">
                        重置为场景数据
                      </a-button>
                    </a-space>
                  </a-form-item>
                </a-form>
              </a-collapse-panel>
            </a-collapse>
          </a-form>
        </a-card>

        <!-- 执行测试按钮 -->
        <a-card style="margin-top: 16px" :bordered="false">
          <a-space style="width: 100%" direction="vertical">
            <a-button
              type="primary"
              block
              size="large"
              :loading="testing"
              @click="executeTest"
            >
              <template #icon><PlayCircleOutlined /></template>
              执行测试
            </a-button>
            <a-button block @click="quickTest">
              <template #icon><ThunderboltOutlined /></template>
              快速测试（使用默认数据）
            </a-button>
          </a-space>
        </a-card>
      </a-col>

      <!-- 右侧：测试结果区 -->
      <a-col :span="12">
        <!-- 测试结果卡片 -->
        <a-card title="测试结果" :bordered="false">
          <template v-if="testResult">
            <a-result
              :status="testResultStatus"
              :title="testResultTitle"
              :sub-title="testResultSubtitle"
            >
              <template #extra>
                <a-descriptions bordered :column="1" size="small">
                  <a-descriptions-item label="测试ID">
                    {{ testResult.testId }}
                  </a-descriptions-item>
                  <a-descriptions-item label="测试结果">
                    <a-tag :color="getTestResultColor(testResult.testResult)">
                      {{ testResult.testResult }}
                    </a-tag>
                  </a-descriptions-item>
                  <a-descriptions-item label="条件匹配">
                    <a-tag :color="testResult.conditionMatched ? 'success' : 'default'">
                      {{ testResult.conditionMatched ? '匹配' : '不匹配' }}
                    </a-tag>
                  </a-descriptions-item>
                  <a-descriptions-item label="执行时间">
                    {{ testResult.executionTime }} ms
                  </a-descriptions-item>
                  <a-descriptions-item label="测试时间">
                    {{ testResult.testTimestamp }}
                  </a-descriptions-item>
                  <a-descriptions-item v-if="testResult.errorMessage" label="错误信息">
                    <span style="color: #ff4d4f">{{ testResult.errorMessage }}</span>
                  </a-descriptions-item>
                </a-descriptions>
              </template>
            </a-result>

            <!-- 执行的动作 -->
            <a-divider v-if="testResult.executedActions && testResult.executedActions.length > 0">
              执行的动作
            </a-divider>
            <a-list
              v-if="testResult.executedActions && testResult.executedActions.length > 0"
              size="small"
              :data-source="testResult.executedActions"
              style="margin-bottom: 16px"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <a-space>
                        <span>{{ item.actionName }}</span>
                        <a-tag :color="getExecutionStatusColor(item.executionStatus)">
                          {{ item.executionStatus }}
                        </a-tag>
                      </a-space>
                    </template>
                    <template #description>
                      <div>值: {{ formatActionValue(item.actionValue) }}</div>
                      <div>消息: {{ item.executionMessage }}</div>
                      <div class="text-gray text-sm">时间: {{ item.executionTimestamp }}</div>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>

            <!-- 测试输入数据 -->
            <a-divider>测试输入数据</a-divider>
            <a-card size="small" style="background-color: #f5f5f5">
              <pre class="json-display">{{ formatJson(testResult.testInputData) }}</pre>
            </a-card>
          </template>

          <template v-else>
            <a-empty description="暂无测试结果，请执行测试" />
          </template>
        </a-card>

        <!-- 执行日志 -->
        <a-card title="执行日志" style="margin-top: 16px" :bordered="false">
          <template v-if="testResult && testResult.executionLogs && testResult.executionLogs.length > 0">
            <a-timeline>
              <a-timeline-item
                v-for="(log, index) in testResult.executionLogs"
                :key="index"
                :color="getLogLevelColor(log.logLevel)"
              >
                <div>
                  <a-tag :color="getLogLevelColor(log.logLevel)" size="small">
                    {{ log.logLevel }}
                  </a-tag>
                  <span class="ml-2">{{ log.logMessage }}</span>
                </div>
                <div class="text-gray text-sm mt-1">{{ log.logTimestamp }}</div>
              </a-timeline-item>
            </a-timeline>
          </template>
          <template v-else>
            <a-empty description="暂无执行日志" />
          </template>
        </a-card>
      </a-col>
    </a-row>

    <!-- 批量测试Modal -->
    <a-modal
      v-model:visible="batchTestModalVisible"
      title="批量测试规则"
      width="600px"
      @ok="executeBatchTest"
      :confirm-loading="batchTesting"
    >
      <a-form layout="vertical">
        <a-form-item label="选择要测试的规则">
          <a-select
            v-model:value="batchTestRuleIds"
            mode="multiple"
            placeholder="请选择多个规则"
            show-search
            :filter-option="filterRuleOption"
            :loading="loadingRules"
          >
            <a-select-option
              v-for="rule in ruleList"
              :key="rule.ruleId"
              :value="rule.ruleId"
            >
              {{ rule.ruleName }} (ID: {{ rule.ruleId }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="测试场景">
          <a-select v-model:value="batchTestScenario" placeholder="请选择测试场景">
            <a-select-option
              v-for="scenario in testScenarios"
              :key="scenario.scenarioCode"
              :value="scenario.scenarioCode"
            >
              {{ scenario.scenarioName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="已选择">
          <a-tag color="blue">{{ batchTestRuleIds.length }} 条规则</a-tag>
        </a-form-item>
      </a-form>

      <a-divider>批量测试结果</a-divider>

      <a-list
        v-if="batchTestResults.length > 0"
        size="small"
        :data-source="batchTestResults"
        style="max-height: 300px; overflow-y: auto"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <a-space>
                  <span>规则ID: {{ item.ruleId }}</span>
                  <a-tag :color="getTestResultColor(item.testResult)">
                    {{ item.testResult }}
                  </a-tag>
                  <span class="text-gray text-sm">{{ item.executionTime }}ms</span>
                </a-space>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
      <a-empty v-else description="暂无测试结果" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  ReloadOutlined,
  PlayCircleOutlined,
  ThunderboltOutlined
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { ruleTestApi } from '@/api/business/attendance/rule-test-api';
import { smartScheduleApi } from '@/api/business/attendance/smart-schedule-api';

const router = useRouter();

// 测试模式：existing-已有规则 custom-自定义规则
const testMode = ref('existing');
const selectedRuleId = ref(null);
const ruleCondition = ref('');
const ruleAction = ref('');

// 测试场景
const testScenarios = ref([]);
const selectedScenario = ref('NORMAL');
const showCustomData = ref(false);
const customDataActiveKey = ref(['1']);

// 规则列表
const ruleList = ref([]);
const loadingRules = ref(false);

// 测试数据
const testData = reactive({
  userId: 1001,
  userName: '张三',
  departmentId: 10,
  departmentName: '研发部',
  attendanceDate: dayjs(),
  punchTime: dayjs().hour(8).minute(28).second(0),
  punchType: 'IN',
  scheduleStartTime: dayjs().hour(8).minute(30).second(0),
  scheduleEndTime: dayjs().hour(17).minute(30).second(0),
  workLocation: '总部大楼',
  deviceId: 'DEV001',
  deviceName: '1号门禁',
  userAttributes: {},
  attendanceAttributes: {},
  attendanceAttributesJson: '{"lateMinutes": 0}'
});

// 测试状态
const testing = ref(false);
const testResult = ref(null);

// 语法验证
const syntaxValidationResult = ref(null);
const syntaxValidationErrorMessage = ref('');

// 批量测试
const batchTestModalVisible = ref(false);
const batchTestRuleIds = ref([]);
const batchTestScenario = ref('NORMAL');
const batchTesting = ref(false);
const batchTestResults = ref([]);

// 返回上一页
const handleBack = () => {
  router.back();
};

// 加载规则列表
const loadRuleList = async () => {
  loadingRules.value = true;
  try {
    const res = await smartScheduleApi.queryRulePage({
      pageNum: 1,
      pageSize: 1000,
      enabled: 1
    });
    ruleList.value = res.data.list || [];
  } catch (error) {
    message.error('加载规则列表失败');
  } finally {
    loadingRules.value = false;
  }
};

// 加载测试场景
const loadTestScenarios = async () => {
  try {
    const res = await ruleTestApi.getTestScenarios();
    testScenarios.value = res.data || [];
  } catch (error) {
    message.error('加载测试场景失败');
  }
};

// 选择测试场景
const selectScenario = async (scenarioCode) => {
  selectedScenario.value = scenarioCode;
  try {
    const res = await ruleTestApi.generateTestData(scenarioCode);
    const data = res.data;

    // 更新测试数据
    testData.userId = data.userId;
    testData.userName = data.userName;
    testData.departmentId = data.departmentId;
    testData.departmentName = data.departmentName;
    testData.attendanceDate = dayjs(data.attendanceDate);
    testData.punchTime = data.punchTime ? dayjs(data.punchTime, 'HH:mm:ss') : null;
    testData.punchType = data.punchType;
    testData.scheduleStartTime = dayjs(data.scheduleStartTime, 'HH:mm:ss');
    testData.scheduleEndTime = dayjs(data.scheduleEndTime, 'HH:mm:ss');
    testData.workLocation = data.workLocation;
    testData.deviceId = data.deviceId;
    testData.deviceName = data.deviceName;
    testData.userAttributes = data.userAttributes || {};
    testData.attendanceAttributes = data.attendanceAttributes || {};
    testData.attendanceAttributesJson = JSON.stringify(testData.attendanceAttributes, null, 2);

    message.success(`已加载"${scenarioCode}"场景数据`);
  } catch (error) {
    message.error('加载场景数据失败');
  }
};

// 测试模式切换
const handleTestModeChange = (e) => {
  if (e.target.value === 'custom') {
    selectedRuleId.value = null;
  }
};

// 选择规则
const handleRuleSelect = (ruleId) => {
  const rule = ruleList.value.find(r => r.ruleId === ruleId);
  if (rule) {
    ruleCondition.value = rule.expression || '';
    ruleAction.value = rule.action || '';
  }
};

// 过滤规则选项
const filterRuleOption = (input, option) => {
  const rule = ruleList.value.find(r => r.ruleId === option.value);
  return (
    rule &&
    (rule.ruleName.toLowerCase().includes(input.toLowerCase()) ||
      rule.ruleId.toString().includes(input))
  );
};

// 验证规则语法
const validateRuleSyntax = async () => {
  if (!ruleCondition.value) {
    syntaxValidationResult.value = null;
    return;
  }

  try {
    const res = await ruleTestApi.validateRuleSyntax(
      ruleCondition.value,
      ruleAction.value
    );
    syntaxValidationResult.value = res.data;
    syntaxValidationErrorMessage.value = res.data
      ? ''
      : 'JSON格式错误，请检查语法';
  } catch (error) {
    syntaxValidationResult.value = false;
    syntaxValidationErrorMessage.value = '语法验证失败';
  }
};

// 应用自定义数据
const applyCustomData = () => {
  try {
    if (testData.attendanceAttributesJson) {
      testData.attendanceAttributes = JSON.parse(testData.attendanceAttributesJson);
    }
    message.success('自定义数据已应用');
  } catch (error) {
    message.error('考勤属性JSON格式错误');
  }
};

// 重置自定义数据
const resetCustomData = () => {
  selectScenario(selectedScenario.value);
};

// 执行测试
const executeTest = async () => {
  if (!ruleCondition.value) {
    message.warning('请输入或选择规则条件');
    return;
  }

  testing.value = true;
  testResult.value = null;

  try {
    const request = {
      ruleId: selectedRuleId.value,
      ruleCondition: ruleCondition.value,
      ruleAction: ruleAction.value,
      userId: testData.userId,
      userName: testData.userName,
      departmentId: testData.departmentId,
      departmentName: testData.departmentName,
      attendanceDate: testData.attendanceDate.format('YYYY-MM-DD'),
      punchTime: testData.punchTime
        ? testData.punchTime.format('HH:mm:ss')
        : null,
      punchType: testData.punchType,
      scheduleStartTime: testData.scheduleStartTime.format('HH:mm:ss'),
      scheduleEndTime: testData.scheduleEndTime.format('HH:mm:ss'),
      workLocation: testData.workLocation,
      deviceId: testData.deviceId,
      deviceName: testData.deviceName,
      userAttributes: testData.userAttributes,
      attendanceAttributes: testData.attendanceAttributes,
      environmentParams: {}
    };

    let res;
    if (testMode.value === 'existing' && selectedRuleId.value) {
      res = await ruleTestApi.testRule(selectedRuleId.value, request);
    } else {
      res = await ruleTestApi.testCustomRule(request);
    }

    testResult.value = res.data;
    message.success('测试执行成功');
  } catch (error) {
    message.error('测试执行失败');
  } finally {
    testing.value = false;
  }
};

// 快速测试
const quickTest = async () => {
  if (!ruleCondition.value) {
    message.warning('请输入规则条件');
    return;
  }

  testing.value = true;
  testResult.value = null;

  try {
    const res = await ruleTestApi.quickTest(
      ruleCondition.value,
      ruleAction.value || '{}'
    );
    testResult.value = res.data;
    message.success('快速测试成功');
  } catch (error) {
    message.error('快速测试失败');
  } finally {
    testing.value = false;
  }
};

// 显示批量测试Modal
const showBatchTestModal = () => {
  batchTestModalVisible.value = true;
  batchTestResults.value = [];
};

// 执行批量测试
const executeBatchTest = async () => {
  if (batchTestRuleIds.value.length === 0) {
    message.warning('请选择要测试的规则');
    return;
  }

  batchTesting.value = true;

  try {
    const request = {
      userId: testData.userId,
      userName: testData.userName,
      departmentId: testData.departmentId,
      departmentName: testData.departmentName,
      attendanceDate: testData.attendanceDate.format('YYYY-MM-DD'),
      punchTime: testData.punchTime ? testData.punchTime.format('HH:mm:ss') : null,
      punchType: testData.punchType,
      scheduleStartTime: testData.scheduleStartTime.format('HH:mm:ss'),
      scheduleEndTime: testData.scheduleEndTime.format('HH:mm:ss')
    };

    const res = await ruleTestApi.batchTestRules(batchTestRuleIds.value, request);
    batchTestResults.value = res.data || [];
    message.success('批量测试完成');
  } catch (error) {
    message.error('批量测试失败');
  } finally {
    batchTesting.value = false;
  }
};

// 计算属性：测试结果状态
const testResultStatus = computed(() => {
  if (!testResult.value) return 'info';
  const result = testResult.value.testResult;
  if (result === 'MATCH') return 'success';
  if (result === 'NOT_MATCH') return 'warning';
  if (result === 'ERROR') return 'error';
  return 'info';
});

// 计算属性：测试结果标题
const testResultTitle = computed(() => {
  if (!testResult.value) return '';
  const result = testResult.value.testResult;
  if (result === 'MATCH') return '规则匹配成功';
  if (result === 'NOT_MATCH') return '规则不匹配';
  if (result === 'ERROR') return '规则执行错误';
  return '测试完成';
});

// 计算属性：测试结果副标题
const testResultSubtitle = computed(() => {
  if (!testResult.value) return '';
  return testResult.value.resultMessage || '';
});

// 获取测试结果颜色
const getTestResultColor = (result) => {
  const colors = {
    MATCH: 'success',
    NOT_MATCH: 'warning',
    ERROR: 'error'
  };
  return colors[result] || 'default';
};

// 获取执行状态颜色
const getExecutionStatusColor = (status) => {
  const colors = {
    SUCCESS: 'success',
    FAILED: 'error'
  };
  return colors[status] || 'default';
};

// 获取日志级别颜色
const getLogLevelColor = (level) => {
  const colors = {
    INFO: 'blue',
    WARN: 'orange',
    ERROR: 'red',
    DEBUG: 'default'
  };
  return colors[level] || 'default';
};

// 格式化动作值
const formatActionValue = (value) => {
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2);
  }
  return String(value);
};

// 格式化JSON
const formatJson = (data) => {
  return JSON.stringify(data, null, 2);
};

// 初始化
onMounted(() => {
  loadRuleList();
  loadTestScenarios();
  selectScenario('NORMAL');
});
</script>

<style lang="scss" scoped>
.rule-tester {
  padding: 16px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.form-help {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.text-gray {
  color: #999;
}

.text-sm {
  font-size: 12px;
}

.ml-2 {
  margin-left: 8px;
}

.mt-1 {
  margin-top: 4px;
}

.json-display {
  background-color: #fff;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  margin: 0;
}
</style>
