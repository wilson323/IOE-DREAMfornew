<!--
  规则测试弹窗组件
  基于SmartAdmin框架的Vue3企业级规则测试界面

  @Author: IOE-DREAM Team
  @Date: 2025-12-01
  @Copyright IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <a-modal
    v-model:open="visible"
    title="规则测试"
    :width="800"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :ok-text="'执行测试'"
  >
    <div class="rule-test-content">
      <!-- 规则信息 -->
      <a-card title="规则信息" size="small" :bordered="false">
        <a-descriptions :column="2" size="small">
          <a-descriptions-item label="规则名称">
            {{ ruleData?.ruleName }}
          </a-descriptions-item>
          <a-descriptions-item label="规则类型">
            <a-tag :color="getRuleTypeColor(ruleData?.ruleType)">
              {{ getRuleTypeText(ruleData?.ruleType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="规则状态">
            <a-tag :color="ruleData?.status === 'ACTIVE' ? 'green' : 'default'">
              {{ ruleData?.status === 'ACTIVE' ? '启用' : '禁用' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="执行顺序">
            {{ ruleData?.executionOrder }}
          </a-descriptions-item>
          <a-descriptions-item label="规则描述" :span="2">
            {{ ruleData?.description || '无描述' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 测试参数配置 -->
      <a-card title="测试参数" size="small" :bordered="false" style="margin-top: 16px">
        <a-form
          ref="formRef"
          :model="formData"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 18 }"
        >
          <a-form-item label="测试模式">
            <a-radio-group v-model:value="formData.testMode">
              <a-radio value="dry_run">模拟测试（不执行实际动作）</a-radio>
              <a-radio value="real_test">实际测试（执行真实动作）</a-radio>
            </a-radio-group>
          </a-form-item>

          <!-- 模拟触发条件 -->
          <a-form-item label="触发条件">
            <div class="trigger-conditions">
              <div
                v-for="(condition, index) in mockConditions"
                :key="index"
                class="condition-item"
              >
                <a-row :gutter="8">
                  <a-col :span="6">
                    <a-input
                      v-model:value="condition.field"
                      placeholder="字段名"
                      readonly
                    />
                  </a-col>
                  <a-col :span="6">
                    <a-input
                      v-model:value="condition.operator"
                      placeholder="操作符"
                      readonly
                    />
                  </a-col>
                  <a-col :span="8">
                    <a-input
                      v-model:value="condition.testValue"
                      placeholder="测试值"
                      @change="handleConditionChange(index)"
                    />
                  </a-col>
                  <a-col :span="4">
                    <a-tag :color="condition.match ? 'green' : 'orange'">
                      {{ condition.match ? '匹配' : '测试' }}
                    </a-tag>
                  </a-col>
                </a-row>
              </div>
            </div>
          </a-form-item>

          <!-- 测试设备 -->
          <a-form-item label="测试设备" v-if="ruleData?.linkageDevices">
            <a-select
              v-model:value="formData.testDevices"
              mode="multiple"
              placeholder="选择要测试的设备（留空表示全部设备）"
              style="width: 100%"
            >
              <a-select-option
                v-for="device in ruleData.linkageDevices"
                :key="device.deviceId"
                :value="device.deviceId"
              >
                {{ device.deviceName }} ({{ device.location }})
              </a-select-option>
            </a-select>
          </a-form-item>

          <!-- 附加参数 -->
          <a-form-item label="附加参数">
            <a-textarea
              v-model:value="formData.extraParams"
              placeholder="附加测试参数（JSON格式）"
              :rows="3"
            />
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 预期执行结果 -->
      <a-card title="预期执行结果" size="small" :bordered="false" style="margin-top: 16px">
        <a-timeline>
          <a-timeline-item
            v-for="(action, index) in expectedActions"
            :key="index"
            color="blue"
          >
            <div class="action-item">
              <div class="action-title">{{ action.typeText }}</div>
              <div class="action-detail" v-if="action.parameters">
                参数: {{ action.parameters }}
              </div>
              <div class="action-target" v-if="action.targetDevices">
                目标设备: {{ action.targetDevices.join(', ') }}
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-card>

      <!-- 测试结果 -->
      <a-card
        v-if="testResult"
        title="测试结果"
        size="small"
        :bordered="false"
        style="margin-top: 16px"
      >
        <a-result
          :status="testResult.success ? 'success' : 'error'"
          :title="testResult.success ? '测试执行成功' : '测试执行失败'"
          :sub-title="testResult.message"
        >
          <template #extra>
            <a-space direction="vertical" style="width: 100%">
              <div v-if="testResult.data" class="test-details">
                <a-descriptions title="执行详情" size="small" bordered>
                  <a-descriptions-item
                    v-for="(value, key) in testResult.data"
                    :key="key"
                    :label="key"
                  >
                    {{ value }}
                  </a-descriptions-item>
                </a-descriptions>
              </div>

              <a-space>
                <a-button @click="handleExportResult">导出结果</a-button>
                <a-button type="primary" @click="handleViewLog">查看日志</a-button>
              </a-space>
            </a-space>
          </template>
        </a-result>
      </a-card>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import { useGlobalLinkageStore } from '/@/store/modules/business/global-linkage';

// Props & Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  ruleData: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['update:visible', 'result']);

// Store
const globalLinkageStore = useGlobalLinkageStore();

// 响应式数据
const formRef = ref();
const loading = ref(false);
const testResult = ref(null);

// 表单数据
const formData = reactive({
  testMode: 'dry_run',
  testDevices: [],
  extraParams: '{}',
});

// 模拟条件
const mockConditions = ref([]);

// 预期执行动作
const expectedActions = ref([]);

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
});

// 方法

/**
 * 获取规则类型颜色
 */
const getRuleTypeColor = (type) => {
  const colorMap = {
    'DEVICE_STATUS': 'blue',
    'ACCESS_RESULT': 'green',
    'TIME_BASED': 'orange',
    'EMERGENCY': 'red',
  };
  return colorMap[type] || 'default';
};

/**
 * 获取规则类型文本
 */
const getRuleTypeText = (type) => {
  const textMap = {
    'DEVICE_STATUS': '设备状态联动',
    'ACCESS_RESULT': '通行结果联动',
    'TIME_BASED': '时间触发联动',
    'EMERGENCY': '紧急情况联动',
  };
  return textMap[type] || type;
};

/**
 * 初始化模拟条件
 */
const initMockConditions = () => {
  if (!props.ruleData?.triggerCondition) return;

  try {
    const condition = JSON.parse(props.ruleData.triggerCondition);
    mockConditions.value = (condition.conditions || []).map(cond => ({
      ...cond,
      testValue: cond.value || '',
      match: false,
    }));
  } catch (error) {
    console.error('解析触发条件失败:', error);
    mockConditions.value = [];
  }
};

/**
 * 初始化预期动作
 */
const initExpectedActions = () => {
  if (!props.ruleData?.linkageAction) return;

  try {
    const action = JSON.parse(props.ruleData.linkageAction);
    const actionTypes = {
      'openDoor': '远程开门',
      'closeDoor': '远程关门',
      'lockDevice': '锁定设备',
      'unlockDevice': '解锁设备',
      'sendAlert': '发送告警',
      'startRecording': '开始录像',
      'triggerAlarm': '触发警报',
    };

    expectedActions.value = (action.actions || []).map(act => ({
      ...act,
      typeText: actionTypes[act.type] || act.type,
      targetDevices: props.ruleData?.linkageDevices?.map(d => d.deviceName) || [],
    }));
  } catch (error) {
    console.error('解析联动动作失败:', error);
    expectedActions.value = [];
  }
};

/**
 * 条件值改变
 */
const handleConditionChange = (index) => {
  const condition = mockConditions.value[index];
  if (!condition.value) return;

  // 简单的条件匹配判断
  const testValue = condition.testValue;
  const expectedValue = condition.value;

  switch (condition.operator) {
    case 'eq':
      condition.match = testValue === expectedValue;
      break;
    case 'ne':
      condition.match = testValue !== expectedValue;
      break;
    case 'gt':
      condition.match = Number(testValue) > Number(expectedValue);
      break;
    case 'lt':
      condition.match = Number(testValue) < Number(expectedValue);
      break;
    case 'in':
      condition.match = expectedValue.includes(testValue);
      break;
    case 'not_in':
      condition.match = !expectedValue.includes(testValue);
      break;
    default:
      condition.match = false;
  }
};

/**
 * 执行测试
 */
const handleSubmit = async () => {
  try {
    loading.value = true;
    testResult.value = null;

    // 构建测试参数
    const testParams = {
      testMode: formData.testMode,
      triggerConditions: mockConditions.value.map(cond => ({
        field: cond.field,
        operator: cond.operator,
        value: cond.testValue,
      })),
      testDevices: formData.testDevices,
      extraParams: formData.extraParams ? JSON.parse(formData.extraParams) : {},
    };

    // 执行测试
    const result = await globalLinkageStore.testRule(props.ruleData.ruleId, testParams);
    testResult.value = result;

    // 发送测试结果给父组件
    emit('result', result);

  } catch (error) {
    console.error('执行测试失败:', error);
    message.error('执行测试失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 导出测试结果
 */
const handleExportResult = () => {
  if (!testResult.value) return;

  const exportData = {
    rule: props.ruleData,
    testParams: formData,
    result: testResult.value,
    exportTime: new Date().toISOString(),
  };

  const blob = new Blob([JSON.stringify(exportData, null, 2)], {
    type: 'application/json',
  });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `rule_test_${props.ruleData.ruleId}_${Date.now()}.json`;
  a.click();
  URL.revokeObjectURL(url);

  message.success('测试结果已导出');
};

/**
 * 查看日志 - 待日志模块完成后对接
 */
const handleViewLog = () => {
  // 日志查看功能 - 待日志模块完成后对接
  message.info('查看日志功能开发中');
};

/**
 * 取消
 */
const handleCancel = () => {
  visible.value = false;
  resetForm();
};

/**
 * 重置表单
 */
const resetForm = () => {
  testResult.value = null;
  Object.assign(formData, {
    testMode: 'dry_run',
    testDevices: [],
    extraParams: '{}',
  });
  initMockConditions();
};

// 监听器
watch(
  () => props.visible,
  (newVal) => {
    if (newVal && props.ruleData) {
      nextTick(() => {
        initMockConditions();
        initExpectedActions();
      });
    }
  }
);

watch(
  () => props.ruleData,
  () => {
    if (props.visible && props.ruleData) {
      initMockConditions();
      initExpectedActions();
    }
  }
);
</script>

<style scoped lang="less">
.rule-test-content {
  .trigger-conditions {
    .condition-item {
      margin-bottom: 8px;
      padding: 8px;
      border: 1px solid #f0f0f0;
      border-radius: 4px;
      background: #fafafa;
    }
  }

  .action-item {
    .action-title {
      font-weight: 500;
      color: #262626;
    }

    .action-detail,
    .action-target {
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }
  }

  .test-details {
    margin-top: 16px;
    text-align: left;
    width: 100%;
  }
}
</style>
