<!--
  考勤规则可视化编辑器组件
  支持规则条件和动作的可视化配置
  包含JSON语法高亮、实时验证和错误提示

  @author IOE-DREAM Team
  @date 2025-12-26
-->
<template>
  <div class="rule-config-editor">
    <a-card :bordered="false" :body-style="{ padding: '16px' }">
      <!-- 编辑器模式切换 -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="12">
          <a-radio-group v-model:value="editorMode" button-style="solid">
            <a-radio-button value="visual">
              <AppstoreOutlined />
              可视化编辑
            </a-radio-button>
            <a-radio-button value="code">
              <CodeOutlined />
              代码编辑
            </a-radio-button>
          </a-radio-group>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-space>
            <a-tag :color="validationStatus.color">
              {{ validationStatus.text }}
            </a-tag>
            <a-button size="small" @click="showHelpModal">
              <QuestionCircleOutlined />
              帮助
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <!-- 可视化编辑模式 -->
      <div v-show="editorMode === 'visual'">
        <!-- 规则条件编辑器 -->
        <a-card title="规则条件配置" size="small" :bordered="false" style="margin-bottom: 16px">
          <template #extra>
            <a-button size="small" type="primary" @click="addCondition">
              <PlusOutlined />
              添加条件
            </a-button>
          </template>

          <!-- 条件组合方式 -->
          <a-form-item label="条件组合" v-if="conditions.length > 1">
            <a-radio-group v-model:value="conditionLogic">
              <a-radio value="AND">AND (且)</a-radio>
              <a-radio value="OR">OR (或)</a-radio>
            </a-radio-group>
          </a-form-item>

          <!-- 条件列表 -->
          <div v-for="(condition, index) in conditions" :key="index" class="condition-item">
            <a-row :gutter="8">
              <a-col :span="6">
                <a-select
                  v-model:value="condition.field"
                  placeholder="选择字段"
                  style="width: 100%"
                  @change="handleFieldChange(index)"
                >
                  <a-select-opt-group label="时间相关">
                    <a-select-option value="lateMinutes">迟到分钟数</a-select-option>
                    <a-select-option value="earlyLeaveMinutes">早退分钟数</a-select-option>
                    <a-select-option value="absentHours">缺勤小时数</a-select-option>
                    <a-select-option value="overtimeHours">加班小时数</a-select-option>
                    <a-select-option value="workHours">工作小时数</a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group label="次数相关">
                    <a-select-option value="lateCount">迟到次数</a-select-option>
                    <a-select-option value="absentCount">缺勤次数</a-select-option>
                    <a-select-option value="overtimeCount">加班次数</a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group label="状态相关">
                    <a-select-option value="isWorkday">是否工作日</a-select-option>
                    <a-select-option value="isWeekend">是否周末</a-select-option>
                    <a-select-option value="isHoliday">是否节假日</a-select-option>
                  </a-select-opt-group>
                </a-select>
              </a-col>

              <a-col :span="4">
                <a-select
                  v-model:value="condition.operator"
                  placeholder="运算符"
                  style="width: 100%"
                >
                  <a-select-option value=">">大于</a-select-option>
                  <a-select-option value=">=">大于等于</a-select-option>
                  <a-select-option value="<">小于</a-select-option>
                  <a-select-option value="<=">小于等于</a-select-option>
                  <a-select-option value="==">等于</a-select-option>
                  <a-select-option value="!=">不等于</a-select-option>
                  <a-select-option value="in">包含</a-select-option>
                  <a-select-option value="notIn">不包含</a-select-option>
                </a-select>
              </a-col>

              <a-col :span="6">
                <!-- 布尔值字段 -->
                <a-select
                  v-if="isBooleanField(condition.field)"
                  v-model:value="condition.value"
                  placeholder="选择值"
                  style="width: 100%"
                >
                  <a-select-option :value="true">是</a-select-option>
                  <a-select-option :value="false">否</a-select-option>
                </a-select>

                <!-- 数字值字段 -->
                <a-input-number
                  v-else-if="isNumberField(condition.field)"
                  v-model:value="condition.value"
                  placeholder="输入数值"
                  :min="0"
                  style="width: 100%"
                />

                <!-- 文本值字段 -->
                <a-input
                  v-else
                  v-model:value="condition.value"
                  placeholder="输入值"
                  style="width: 100%"
                />
              </a-col>

              <a-col :span="2">
                <a-button
                  type="text"
                  danger
                  size="small"
                  @click="removeCondition(index)"
                >
                  <DeleteOutlined />
                </a-button>
              </a-col>
            </a-row>
          </div>

          <!-- 空状态 -->
          <a-empty
            v-if="conditions.length === 0"
            description="暂无规则条件，请点击上方按钮添加"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            :image-style="{ height: '60px' }"
          >
            <a-button type="primary" size="small" @click="addCondition">
              添加第一个条件
            </a-button>
          </a-empty>
        </a-card>

        <!-- 规则动作编辑器 -->
        <a-card title="规则动作配置" size="small" :bordered="false" style="margin-bottom: 16px">
          <template #extra>
            <a-button size="small" type="primary" @click="addAction">
              <PlusOutlined />
              添加动作
            </a-button>
          </template>

          <!-- 动作列表 -->
          <div v-for="(action, index) in actions" :key="index" class="action-item">
            <a-row :gutter="8" align="middle">
              <a-col :span="8">
                <a-select
                  v-model:value="action.type"
                  placeholder="选择动作类型"
                  style="width: 100%"
                  @change="handleActionTypeChange(index)"
                >
                  <a-select-opt-group label="扣款类">
                    <a-select-option value="deductAmount">扣款金额</a-select-option>
                    <a-select-option value="deductScore">扣分</a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group label="扣时类">
                    <a-select-option value="deductAnnualLeave">扣除年假</a-select-option>
                    <a-select-option value="deductSickLeave">扣除病假</a-select-option>
                    <a-select-option value="deductCompensatoryLeave">扣除调休</a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group label="标记类">
                    <a-select-option value="markLate">标记迟到</a-select-option>
                    <a-select-option value="markEarlyLeave">标记早退</a-select-option>
                    <a-select-option value="markAbsent">标记缺勤</a-select-option>
                    <a-select-option value="markOvertime">标记加班</a-select-option>
                  </a-select-opt-group>
                  <a-select-opt-group label="通知类">
                    <a-select-option value="notifyUser">通知用户</a-select-option>
                    <a-select-option value="notifyManager">通知管理员</a-select-option>
                    <a-select-option value="sendEmail">发送邮件</a-select-option>
                  </a-select-opt-group>
                </a-select>
              </a-col>

              <a-col :span="10">
                <a-input
                  v-model:value="action.params"
                  placeholder="参数配置 (JSON格式)"
                  style="width: 100%"
                />
                <div class="text-hint" style="font-size: 12px; color: #999; margin-top: 4px">
                  {{ getActionHint(action.type) }}
                </div>
              </a-col>

              <a-col :span="2">
                <a-button
                  type="text"
                  danger
                  size="small"
                  @click="removeAction(index)"
                >
                  <DeleteOutlined />
                </a-button>
              </a-col>
            </a-row>
          </div>

          <!-- 空状态 -->
          <a-empty
            v-if="actions.length === 0"
            description="暂无规则动作，请点击上方按钮添加"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            :image-style="{ height: '60px' }"
          >
            <a-button type="primary" size="small" @click="addAction">
              添加第一个动作
            </a-button>
          </a-empty>
        </a-card>

        <!-- JSON预览 -->
        <a-card title="JSON预览" size="small" :bordered="false">
          <template #extra>
            <a-space>
              <a-tag v-if="jsonValid" color="success">
                <CheckCircleOutlined />
                格式正确
              </a-tag>
              <a-tag v-else color="error">
                <CloseCircleOutlined />
                格式错误
              </a-tag>
              <a-button size="small" @click="copyJson">
                <CopyOutlined />
                复制
              </a-button>
            </a-space>
          </template>

          <!-- 条件JSON -->
          <div class="json-preview-section">
            <div class="json-label">规则条件 (ruleCondition):</div>
            <pre class="json-content" v-html="highlightedConditionJson"></pre>
          </div>

          <!-- 动作JSON -->
          <div class="json-preview-section">
            <div class="json-label">规则动作 (ruleAction):</div>
            <pre class="json-content" v-html="highlightedActionJson"></pre>
          </div>
        </a-card>
      </div>

      <!-- 代码编辑模式 -->
      <div v-show="editorMode === 'code'">
        <a-card title="JSON代码编辑" size="small" :bordered="false">
          <template #extra>
            <a-space>
              <a-tag v-if="jsonValid" color="success">
                <CheckCircleOutlined />
                格式正确
              </a-tag>
              <a-tag v-else color="error">
                <CloseCircleOutlined />
                格式错误
              </a-tag>
              <a-button size="small" @click="formatJson">
                <BorderedRectangleOutlined />
                格式化
              </a-button>
              <a-button size="small" @click="copyJson">
                <CopyOutlined />
                复制
              </a-button>
            </a-space>
          </template>

          <a-tabs>
            <a-tab-pane key="condition" tab="规则条件 (ruleCondition)">
              <textarea
                ref="conditionCodeRef"
                v-model="conditionCode"
                class="code-editor"
                placeholder='请输入规则条件JSON，例如: {"lateMinutes": 5}'
                @input="handleCodeChange"
                @blur="validateCode"
              />
            </a-tab-pane>

            <a-tab-pane key="action" tab="规则动作 (ruleAction)">
              <textarea
                ref="actionCodeRef"
                v-model="actionCode"
                class="code-editor"
                placeholder='请输入规则动作JSON，例如: {"deductAmount": 50}'
                @input="handleCodeChange"
                @blur="validateCode"
              />
            </a-tab-pane>
          </a-tabs>

          <!-- 错误提示 -->
          <a-alert
            v-if="errorMessage"
            :message="errorMessage"
            type="error"
            show-icon
            closable
            style="margin-top: 16px"
          />
        </a-card>
      </div>

      <!-- 快速模板 -->
      <a-card title="快速模板" size="small" :bordered="false" style="margin-top: 16px">
        <a-row :gutter="8">
          <a-col :span="6" v-for="template in quickTemplates" :key="template.name">
            <a-card
              size="small"
              :title="template.name"
              hoverable
              @click="applyTemplate(template)"
              style="cursor: pointer; text-align: center"
            >
              <template #cover>
                <component :is="template.icon" style="font-size: 24px" />
              </template>
              <div class="template-desc">{{ template.desc }}</div>
            </a-card>
          </a-col>
        </a-row>
      </a-card>
    </a-card>

    <!-- 帮助Modal -->
    <a-modal
      v-model:visible="helpModalVisible"
      title="规则编辑器帮助"
      width="800px"
      :footer="null"
    >
      <a-collapse>
        <a-collapse-panel key="1" header="规则条件说明">
          <p>规则条件用于定义触发规则的前提条件，支持多种字段和运算符：</p>
          <ul>
            <li><strong>时间字段</strong>: lateMinutes(迟到分钟), earlyLeaveMinutes(早退分钟), workHours(工作小时)</li>
            <li><strong>运算符</strong>: &gt;(大于), &gt;=(大于等于), &lt;(小于), &lt;=(小于等于), ==(等于), !=(不等于)</li>
            <li><strong>组合条件</strong>: AND(且), OR(或)</li>
            <li><strong>示例</strong>: lateMinutes &gt; 5 表示"迟到超过5分钟"</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="规则动作说明">
          <p>规则动作用于定义满足条件后执行的操作：</p>
          <ul>
            <li><strong>扣款类</strong>: deductAmount(扣款金额), deductScore(扣分)</li>
            <li><strong>扣时类</strong>: deductAnnualLeave(扣除年假), deductSickLeave(扣除病假)</li>
            <li><strong>标记类</strong>: markLate(标记迟到), markAbsent(标记缺勤)</li>
            <li><strong>通知类</strong>: notifyUser(通知用户), notifyManager(通知管理员)</li>
            <li><strong>示例</strong>: {"deductAmount": 50} 表示"扣款50元"</li>
          </ul>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="JSON格式说明">
          <p>规则条件和动作最终都会转换为JSON格式存储：</p>
          <pre class="json-example"><code>// 规则条件JSON示例
{
  "lateMinutes": 5,
  "earlyLeaveMinutes": 10
}

// 规则动作JSON示例
{
  "deductAmount": 50,
  "notifyUser": true
}</code></pre>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="常见场景示例">
          <p><strong>迟到扣款规则</strong>:</p>
          <pre class="json-example"><code>// 条件: 迟到超过5分钟
{"lateMinutes": 5}

// 动作: 扣款50元
{"deductAmount": 50}</code></pre>

          <p><strong>加班补休规则</strong>:</p>
          <pre class="json-example"><code>// 条件: 工作日加班超过2小时
{"overtimeHours": 2, "isWorkday": true}

// 动作: 给予调休
{"addCompensatoryLeave": 2}</code></pre>

          <p><strong>连续缺勤警告</strong>:</p>
          <pre class="json-example"><code>// 条件: 连续缺勤3天
{"absentCount": 3}

// 动作: 通知管理员
{"notifyManager": true}</code></pre>
        </a-collapse-panel>
      </a-collapse>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  AppstoreOutlined,
  CodeOutlined,
  PlusOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  CopyOutlined,
  BorderedRectangleOutlined,
  QuestionCircleOutlined,
} from '@ant-design/icons-vue';
import {
  ClockCircleOutlined,
  DollarOutlined,
  ExclamationCircleOutlined,
  BellOutlined,
} from '@ant-design/icons-vue';
import { Empty } from 'ant-design-vue';

// Props
const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({
      ruleCondition: '',
      ruleAction: '',
    }),
  },
});

// Emits
const emit = defineEmits(['update:modelValue', 'change']);

// Editor mode
const editorMode = ref('visual');

// Validation status
const validationStatus = computed(() => {
  if (jsonValid.value) {
    return { color: 'success', text: '验证通过' };
  }
  return { color: 'error', text: '验证失败' };
});

// Data
const conditions = ref([]);
const conditionLogic = ref('AND');
const actions = ref([]);
const conditionCode = ref('');
const actionCode = ref('');
const errorMessage = ref('');
const jsonValid = ref(true);
const helpModalVisible = ref(false);

// Watch for modelValue changes to initialize editor
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue.ruleCondition) {
      try {
        const parsed = JSON.parse(newValue.ruleCondition);
        conditions.value = Object.entries(parsed).map(([field, value]) => ({
          field,
          operator: '==',
          value,
        }));
        conditionCode.value = newValue.ruleCondition;
      } catch (e) {
        conditionCode.value = newValue.ruleCondition;
      }
    }

    if (newValue.ruleAction) {
      try {
        const parsed = JSON.parse(newValue.ruleAction);
        actions.value = Object.entries(parsed).map(([type, params]) => ({
          type,
          params: typeof params === 'object' ? JSON.stringify(params) : params,
        }));
        actionCode.value = newValue.ruleAction;
      } catch (e) {
        actionCode.value = newValue.ruleAction;
      }
    }
  },
  { immediate: true }
);

// Computed
const conditionJson = computed(() => {
  if (editorMode.value === 'visual') {
    const obj = {};
    if (conditionLogic.value === 'AND') {
      // AND逻辑：所有条件都需满足（转换为Aviator表达式）
      conditions.value.forEach((cond) => {
        obj[cond.field] = cond.value;
      });
    } else {
      // OR逻辑：任一条件满足（简化处理，实际需要更复杂逻辑）
      conditions.value.forEach((cond) => {
        obj[`${cond.field}_or`] = cond.value;
      });
    }
    return obj;
  } else {
    try {
      return JSON.parse(conditionCode.value || '{}');
    } catch {
      return {};
    }
  }
});

const actionJson = computed(() => {
  if (editorMode.value === 'visual') {
    const obj = {};
    actions.value.forEach((action) => {
      try {
        obj[action.type] = typeof action.params === 'string'
          ? JSON.parse(action.params || '{}')
          : action.params;
      } catch {
        obj[action.type] = action.params;
      }
    });
    return obj;
  } else {
    try {
      return JSON.parse(actionCode.value || '{}');
    } catch {
      return {};
    }
  }
});

const highlightedConditionJson = computed(() => {
  return syntaxHighlight(JSON.stringify(conditionJson.value, null, 2));
});

const highlightedActionJson = computed(() => {
  return syntaxHighlight(JSON.stringify(actionJson.value, null, 2));
});

// Methods
const isBooleanField = (field) => {
  return field.startsWith('is');
};

const isNumberField = (field) => {
  return field.includes('Minutes') || field.includes('Hours') || field.includes('Count');
};

const handleFieldChange = (index) => {
  // 清空值以确保类型正确
  conditions[index].value = undefined;
  emitChange();
};

const handleActionTypeChange = (index) => {
  actions[index].params = '';
  emitChange();
};

const addCondition = () => {
  conditions.value.push({
    field: undefined,
    operator: '>',
    value: undefined,
  });
  emitChange();
};

const removeCondition = (index) => {
  conditions.value.splice(index, 1);
  emitChange();
};

const addAction = () => {
  actions.value.push({
    type: undefined,
    params: '',
  });
  emitChange();
};

const removeAction = (index) => {
  actions.value.splice(index, 1);
  emitChange();
};

const getActionHint = (type) => {
  const hints = {
    deductAmount: '格式: {"amount": 50} - 扣款金额（元）',
    deductScore: '格式: {"score": 10} - 扣分',
    deductAnnualLeave: '格式: {"days": 0.5} - 扣除年假（天）',
    deductSickLeave: '格式: {"days": 1} - 扣除病假（天）',
    deductCompensatoryLeave: '格式: {"days": 2} - 扣除调休（天）',
    markLate: '直接标记，无需参数',
    markEarlyLeave: '直接标记，无需参数',
    markAbsent: '直接标记，无需参数',
    markOvertime: '格式: {"hours": 2} - 标记加班时长',
    notifyUser: '格式: {"message": "您已迟到"} - 通知消息',
    notifyManager: '格式: {"message": "员工迟到"} - 通知内容',
    sendEmail: '格式: {"subject": "迟到提醒", "body": "..."} - 邮件内容',
  };
  return hints[type] || '';
};

const syntaxHighlight = (json) => {
  json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  return json.replace(/("(\\u[a-f0-9]{4}|\\[^\\u\\n])*"/g, (match) => {
    let cls = 'number';
    if (/^"/.test(match)) {
      cls = 'string';
    }
    return '<span class="json-' + cls + '">' + match + '</span>';
  });
};

const validateCode = () => {
  errorMessage.value = '';
  try {
    if (conditionCode.value) JSON.parse(conditionCode.value);
    if (actionCode.value) JSON.parse(actionCode.value);
    jsonValid.value = true;
  } catch (e) {
    jsonValid.value = false;
    errorMessage.value = `JSON格式错误: ${e.message}`;
  }
};

const formatJson = () => {
  try {
    if (conditionCode.value) {
      const parsed = JSON.parse(conditionCode.value);
      conditionCode.value = JSON.stringify(parsed, null, 2);
    }
    if (actionCode.value) {
      const parsed = JSON.parse(actionCode.value);
      actionCode.value = JSON.stringify(parsed, null, 2);
    }
    validateCode();
  } catch (e) {
    message.error('JSON格式错误，无法格式化');
  }
};

const copyJson = () => {
  const text = `规则条件:\n${conditionCode.value}\n\n规则动作:\n${actionCode.value}`;
  navigator.clipboard.writeText(text).then(() => {
    message.success('复制成功');
  });
};

const emitChange = () => {
  const ruleCondition = JSON.stringify(conditionJson.value);
  const ruleAction = JSON.stringify(actionJson.value);

  emit('update:modelValue', {
    ruleCondition,
    ruleAction,
  });

  emit('change', {
    ruleCondition,
    ruleAction,
  });
};

const handleCodeChange = () => {
  validateCode();
  emitChange();
};

// Quick templates
const quickTemplates = [
  {
    name: '迟到扣款',
    desc: '迟到超过5分钟扣款50元',
    icon: ClockCircleOutlined,
    condition: { lateMinutes: 5 },
    action: { deductAmount: 50 },
  },
  {
    name: '早退警告',
    desc: '早退通知管理员',
    icon: ExclamationCircleOutlined,
    condition: { earlyLeaveMinutes: 10 },
    action: { notifyManager: true },
  },
  {
    name: '加班补休',
    desc: '加班2小时给予调休',
    icon: DollarOutlined,
    condition: { overtimeHours: 2 },
    action: { addCompensatoryLeave: 2 },
  },
  {
    name: '缺勤通知',
    desc: '缺勤通知用户和管理员',
    icon: BellOutlined,
    condition: { absentHours: 4 },
    action: { notifyUser: true, notifyManager: true },
  },
];

const applyTemplate = (template) => {
  try {
    conditions.value = Object.entries(template.condition).map(([field, value]) => ({
      field,
      operator: '==',
      value,
    }));
    actions.value = Object.entries(template.action).map(([type, params]) => ({
      type,
      params: typeof params === 'object' ? JSON.stringify(params) : params,
    }));
    emitChange();
    message.success(`已应用模板: ${template.name}`);
  } catch (e) {
    message.error('模板应用失败');
  }
};
</script>

<style scoped>
.rule-config-editor {
  width: 100%;
}

.condition-item,
.action-item {
  margin-bottom: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.json-preview-section {
  margin-bottom: 16px;
}

.json-label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.json-content {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  max-height: 300px;
  overflow: auto;
}

.code-editor {
  width: 100%;
  min-height: 200px;
  padding: 12px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  resize: vertical;
}

.code-editor:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.text-hint {
  font-size: 12px;
  color: #999;
}

.template-desc {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

/* JSON syntax highlighting */
.json-string {
  color: #22863a;
}

.json-number {
  color: #005cc5;
}

.json-boolean {
  color: #cf222e;
}

.json-null {
  color: #8b949e;
}

.json-key {
  color: #d73a49;
}
</style>
