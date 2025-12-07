<!--
  联动规则编辑弹窗组件
  基于SmartAdmin框架的Vue3企业级联动规则配置界面

  @Author: IOE-DREAM Team
  @Date: 2025-12-01
  @Copyright IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑联动规则' : '添加联动规则'"
    :width="900"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <!-- 基本信息 -->
      <a-card title="基本信息" size="small" :bordered="false">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规则名称" name="ruleName">
              <a-input
                v-model:value="formData.ruleName"
                placeholder="请输入规则名称"
                :max-length="50"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规则类型" name="ruleType">
              <a-select
                v-model:value="formData.ruleType"
                placeholder="请选择规则类型"
                @change="handleRuleTypeChange"
              >
                <a-select-option value="DEVICE_STATUS">设备状态联动</a-select-option>
                <a-select-option value="ACCESS_RESULT">通行结果联动</a-select-option>
                <a-select-option value="TIME_BASED">时间触发联动</a-select-option>
                <a-select-option value="EMERGENCY">紧急情况联动</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="执行顺序" name="executionOrder">
              <a-input-number
                v-model:value="formData.executionOrder"
                :min="1"
                :max="100"
                placeholder="执行顺序，数字越小优先级越高"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="延迟执行" name="delayMs">
              <a-input-number
                v-model:value="formData.delayMs"
                :min="0"
                :max="60000"
                placeholder="延迟执行时间（毫秒）"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="规则描述" name="description">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入规则描述"
            :rows="3"
            :max-length="200"
          />
        </a-form-item>
      </a-card>

      <!-- 触发条件 -->
      <a-card title="触发条件" size="small" :bordered="false" style="margin-top: 16px">
        <a-form-item label="触发条件" name="triggerCondition">
          <div class="condition-builder">
            <div
              v-for="(condition, index) in triggerConditions"
              :key="index"
              class="condition-item"
            >
              <a-row :gutter="8">
                <a-col :span="6">
                  <a-select
                    v-model:value="condition.field"
                    placeholder="选择字段"
                    @change="handleConditionFieldChange(index)"
                  >
                    <a-select-option value="deviceStatus">设备状态</a-select-option>
                    <a-select-option value="accessResult">通行结果</a-select-option>
                    <a-select-option value="timeCondition">时间条件</a-select-option>
                    <a-select-option value="emergencyLevel">紧急级别</a-select-option>
                  </a-select>
                </a-col>
                <a-col :span="6">
                  <a-select v-model:value="condition.operator" placeholder="选择操作符">
                    <a-select-option value="eq">等于</a-select-option>
                    <a-select-option value="ne">不等于</a-select-option>
                    <a-select-option value="gt">大于</a-select-option>
                    <a-select-option value="lt">小于</a-select-option>
                    <a-select-option value="in">包含</a-select-option>
                    <a-select-option value="not_in">不包含</a-select-option>
                  </a-select>
                </a-col>
                <a-col :span="8">
                  <a-input
                    v-model:value="condition.value"
                    placeholder="输入值"
                  />
                </a-col>
                <a-col :span="4">
                  <a-button
                    type="text"
                    danger
                    @click="removeCondition(index)"
                    :disabled="triggerConditions.length === 1"
                  >
                    <DeleteOutlined />
                  </a-button>
                </a-col>
              </a-row>
            </div>
            <a-button type="dashed" @click="addCondition" style="width: 100%; margin-top: 8px">
              <PlusOutlined /> 添加条件
            </a-button>
          </div>
        </a-form-item>

        <a-form-item label="条件关系" name="conditionRelation">
          <a-radio-group v-model:value="formData.conditionRelation">
            <a-radio value="AND">且（AND）- 所有条件都满足</a-radio>
            <a-radio value="OR">或（OR）- 任一条件满足</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-card>

      <!-- 联动动作 -->
      <a-card title="联动动作" size="small" :bordered="false" style="margin-top: 16px">
        <a-form-item label="联动动作" name="linkageAction">
          <div class="action-builder">
            <div
              v-for="(action, index) in linkageActions"
              :key="index"
              class="action-item"
            >
              <a-row :gutter="8">
                <a-col :span="8">
                  <a-select
                    v-model:value="action.type"
                    placeholder="选择动作类型"
                    @change="handleActionTypeChange(index)"
                  >
                    <a-select-option value="openDoor">远程开门</a-select-option>
                    <a-select-option value="closeDoor">远程关门</a-select-option>
                    <a-select-option value="lockDevice">锁定设备</a-select-option>
                    <a-select-option value="unlockDevice">解锁设备</a-select-option>
                    <a-select-option value="sendAlert">发送告警</a-select-option>
                    <a-select-option value="startRecording">开始录像</a-select-option>
                    <a-select-option value="triggerAlarm">触发警报</a-select-option>
                  </a-select>
                </a-col>
                <a-col :span="12">
                  <a-input
                    v-model:value="action.parameters"
                    placeholder="动作参数（JSON格式）"
                  />
                </a-col>
                <a-col :span="4">
                  <a-button
                    type="text"
                    danger
                    @click="removeAction(index)"
                    :disabled="linkageActions.length === 1"
                  >
                    <DeleteOutlined />
                  </a-button>
                </a-col>
              </a-row>
            </div>
            <a-button type="dashed" @click="addAction" style="width: 100%; margin-top: 8px">
              <PlusOutlined /> 添加动作
            </a-button>
          </div>
        </a-form-item>
      </a-card>

      <!-- 联动设备 -->
      <a-card title="联动设备" size="small" :bordered="false" style="margin-top: 16px">
        <a-form-item label="选择设备" name="linkageDevices">
          <a-transfer
            v-model:target-keys="selectedDeviceIds"
            :data-source="deviceOptions"
            :titles="['可选设备', '已选设备']"
            :render="item => item.title"
            :list-style="{
              width: '45%',
              height: '300px',
            }"
            @change="handleDeviceTransferChange"
          />
        </a-form-item>
      </a-card>

      <!-- 执行设置 -->
      <a-card title="执行设置" size="small" :bordered="false" style="margin-top: 16px">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="最大执行次数" name="maxExecutions">
              <a-input-number
                v-model:value="formData.maxExecutions"
                :min="-1"
                placeholder="-1表示无限制"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="执行间隔" name="executionInterval">
              <a-input-number
                v-model:value="formData.executionInterval"
                :min="0"
                placeholder="执行间隔（秒）"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="启用状态" name="status">
          <a-switch
            v-model:checked="statusEnabled"
            checked-children="启用"
            un-checked-children="禁用"
          />
        </a-form-item>
      </a-card>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import { useGlobalLinkageStore } from '/@/store/modules/business/global-linkage';

// Props & Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  ruleData: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['update:visible', 'success']);

// Store
const globalLinkageStore = useGlobalLinkageStore();

// 响应式数据
const formRef = ref();
const loading = ref(false);
const deviceOptions = ref([]);
const selectedDeviceIds = ref([]);
const triggerConditions = ref([{ field: '', operator: '', value: '' }]);
const linkageActions = ref([{ type: '', parameters: '' }]);
const statusEnabled = ref(true);

// 表单数据
const formData = reactive({
  ruleName: '',
  ruleType: '',
  description: '',
  executionOrder: 1,
  delayMs: 0,
  conditionRelation: 'AND',
  maxExecutions: -1,
  executionInterval: 60,
  status: 'ACTIVE',
});

// 表单验证规则
const rules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { max: 50, message: '规则名称不能超过50个字符', trigger: 'blur' },
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' },
  ],
  executionOrder: [
    { required: true, message: '请输入执行顺序', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '执行顺序必须在1-100之间', trigger: 'blur' },
  ],
};

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
});

// 方法

/**
 * 添加条件
 */
const addCondition = () => {
  triggerConditions.value.push({ field: '', operator: '', value: '' });
};

/**
 * 移除条件
 */
const removeCondition = (index) => {
  triggerConditions.value.splice(index, 1);
};

/**
 * 添加动作
 */
const addAction = () => {
  linkageActions.value.push({ type: '', parameters: '' });
};

/**
 * 移除动作
 */
const removeAction = (index) => {
  linkageActions.value.splice(index, 1);
};

/**
 * 规则类型改变
 */
const handleRuleTypeChange = (value) => {
  // 根据规则类型设置默认条件
  if (value === 'DEVICE_STATUS') {
    triggerConditions.value = [{ field: 'deviceStatus', operator: 'eq', value: 'OFFLINE' }];
  } else if (value === 'ACCESS_RESULT') {
    triggerConditions.value = [{ field: 'accessResult', operator: 'eq', value: 'DENIED' }];
  } else if (value === 'TIME_BASED') {
    triggerConditions.value = [{ field: 'timeCondition', operator: 'eq', value: '09:00' }];
  } else if (value === 'EMERGENCY') {
    triggerConditions.value = [{ field: 'emergencyLevel', operator: 'gt', value: '3' }];
  }
};

/**
 * 条件字段改变
 */
const handleConditionFieldChange = (index) => {
  // 可以根据字段类型设置默认值
};

/**
 * 动作类型改变
 */
const handleActionTypeChange = (index) => {
  // 可以根据动作类型设置默认参数
  const action = linkageActions.value[index];
  if (action.type === 'openDoor' || action.type === 'closeDoor') {
    action.parameters = JSON.stringify({ duration: 5000 });
  } else if (action.type === 'sendAlert') {
    action.parameters = JSON.stringify({ level: 'high', message: '联动告警' });
  }
};

/**
 * 设备转移改变
 */
const handleDeviceTransferChange = (targetKeys) => {
  selectedDeviceIds.value = targetKeys;
};

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    // 构建提交数据
    const submitData = {
      ...formData,
      status: statusEnabled.value ? 'ACTIVE' : 'INACTIVE',
      triggerCondition: JSON.stringify({
        conditions: triggerConditions.value,
        relation: formData.conditionRelation
      }),
      linkageAction: JSON.stringify({
        actions: linkageActions.value
      }),
      linkageDevices: selectedDeviceIds.value.map(deviceId => ({ deviceId })),
    };

    let success;
    if (props.isEdit) {
      submitData.ruleId = props.ruleData.ruleId;
      success = await globalLinkageStore.updateRule(submitData);
    } else {
      success = await globalLinkageStore.addRule(submitData);
    }

    if (success) {
      emit('success');
      handleCancel();
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  } finally {
    loading.value = false;
  }
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
  formRef.value?.resetFields();
  triggerConditions.value = [{ field: '', operator: '', value: '' }];
  linkageActions.value = [{ type: '', parameters: '' }];
  selectedDeviceIds.value = [];
  statusEnabled.value = true;
};

/**
 * 初始化编辑数据
 */
const initEditData = () => {
  if (props.isEdit && props.ruleData) {
    Object.assign(formData, props.ruleData);

    // 解析触发条件
    if (props.ruleData.triggerCondition) {
      try {
        const condition = JSON.parse(props.ruleData.triggerCondition);
        triggerConditions.value = condition.conditions || [];
        formData.conditionRelation = condition.relation || 'AND';
      } catch (error) {
        console.error('解析触发条件失败:', error);
      }
    }

    // 解析联动动作
    if (props.ruleData.linkageAction) {
      try {
        const action = JSON.parse(props.ruleData.linkageAction);
        linkageActions.value = action.actions || [];
      } catch (error) {
        console.error('解析联动动作失败:', error);
      }
    }

    // 设置选中的设备
    if (props.ruleData.linkageDevices) {
      selectedDeviceIds.value = props.ruleData.linkageDevices.map(device => device.deviceId);
    }

    // 设置启用状态
    statusEnabled.value = props.ruleData.status === 'ACTIVE';
  }
};

/**
 * 加载设备选项
 */
const loadDeviceOptions = async () => {
  await globalLinkageStore.fetchLinkageDevices();
  deviceOptions.value = globalLinkageStore.linkageDevices.map(device => ({
    key: device.deviceId,
    title: `${device.deviceName} (${device.location})`,
    description: device.location,
  }));
};

// 监听器
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      loadDeviceOptions();
      if (props.isEdit) {
        initEditData();
      }
    }
  }
);

// 生命周期
onMounted(() => {
  loadDeviceOptions();
});
</script>

<style scoped lang="less">
.condition-builder,
.action-builder {
  .condition-item,
  .action-item {
    margin-bottom: 8px;
    padding: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    background: #fafafa;
  }
}
</style>