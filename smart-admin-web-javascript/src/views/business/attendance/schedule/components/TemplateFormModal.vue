<!--
  @fileoverview 排班模板表单对话框组件
  @author IOE-DREAM Team
  @description 新增或编辑排班模板，包含班次模式配置
-->
<template>
  <a-modal
    :visible="visible"
    :title="title"
    :width="900"
    :confirm-loading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <!-- 基本信息 -->
      <a-divider orientation="left">基本信息</a-divider>

      <a-form-item label="模板名称" name="templateName">
        <a-input
          v-model:value="formData.templateName"
          placeholder="请输入模板名称"
          maxlength="100"
        />
      </a-form-item>

      <a-form-item label="模板类型" name="templateType">
        <a-select
          v-model:value="formData.templateType"
          placeholder="请选择模板类型"
        >
          <a-select-option value="DEPARTMENT">部门模板</a-select-option>
          <a-select-option value="POSITION">岗位模板</a-select-option>
          <a-select-option value="PERSONAL">个人模板</a-select-option>
          <a-select-option value="GLOBAL">全局模板</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        v-if="formData.templateType === 'DEPARTMENT'"
        label="适用部门"
        name="departmentId"
      >
        <a-select
          v-model:value="formData.departmentId"
          placeholder="请选择部门"
        >
          <a-select-option
            v-for="dept in departmentOptions"
            :key="dept.departmentId"
            :value="dept.departmentId"
          >
            {{ dept.departmentName }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-text-area
          v-model:value="formData.description"
          placeholder="请输入模板描述"
          :rows="3"
          :maxlength="500"
        />
      </a-form-item>

      <!-- 周期配置 -->
      <a-divider orientation="left">周期配置</a-divider>

      <a-form-item label="周期类型" name="cycleType">
        <a-select
          v-model:value="formData.templateConfig.cycleType"
          placeholder="请选择周期类型"
        >
          <a-select-option value="WEEKLY">每周</a-select-option>
          <a-select-option value="MONTHLY">每月</a-select-option>
          <a-select-option value="CUSTOM">自定义</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        v-if="formData.templateConfig.cycleType === 'CUSTOM'"
        label="工作日"
        name="effectiveDays"
      >
        <a-checkbox-group v-model:value="formData.templateConfig.effectiveDays">
          <a-checkbox :value="1">周一</a-checkbox>
          <a-checkbox :value="2">周二</a-checkbox>
          <a-checkbox :value="3">周三</a-checkbox>
          <a-checkbox :value="4">周四</a-checkbox>
          <a-checkbox :value="5">周五</a-checkbox>
          <a-checkbox :value="6">周六</a-checkbox>
          <a-checkbox :value="7">周日</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <!-- 班次模式配置 -->
      <a-divider orientation="left">
        班次模式配置
        <a-button type="link" size="small" @click="addShiftPattern">
          <template #icon><PlusOutlined /></template>
          添加班次
        </a-button>
      </a-divider>

      <a-form-item
        v-for="(pattern, index) in formData.templateConfig.shiftPattern"
        :key="index"
        :label="`班次 ${index + 1}`"
      >
        <a-card size="small" style="background: #fafafa;">
          <a-row :gutter="[16, 16]">
            <a-col :span="12">
              <a-form-item
                :name="['templateConfig', 'shiftPattern', index, 'dayOfWeek']"
                label="星期"
                :label-col="{ span: 8 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-select
                  v-model:value="pattern.dayOfWeek"
                  placeholder="请选择"
                >
                  <a-select-option :value="1">周一</a-select-option>
                  <a-select-option :value="2">周二</a-select-option>
                  <a-select-option :value="3">周三</a-select-option>
                  <a-select-option :value="4">周四</a-select-option>
                  <a-select-option :value="5">周五</a-select-option>
                  <a-select-option :value="6">周六</a-select-option>
                  <a-select-option :value="7">周日</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :span="12">
              <a-form-item
                :name="['templateConfig', 'shiftPattern', index, 'shiftId']"
                label="班次"
                :label-col="{ span: 8 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-select
                  v-model:value="pattern.shiftId"
                  placeholder="请选择班次"
                  @change="handleShiftChange(index, $event)"
                >
                  <a-select-option
                    v-for="shift in shiftOptions"
                    :key="shift.shiftId"
                    :value="shift.shiftId"
                  >
                    <a-tag :color="shift.colorCode">{{ shift.shiftName }}</a-tag>
                    <span style="margin-left: 8px;">{{ shift.startTime }} ~ {{ shift.endTime }}</span>
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :span="12">
              <a-form-item
                :name="['templateConfig', 'shiftPattern', index, 'requiredEmployees']"
                label="需要人数"
                :label-col="{ span: 8 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-input-number
                  v-model:value="pattern.requiredEmployees"
                  :min="1"
                  :max="100"
                  placeholder="请输入"
                  style="width: 100%;"
                />
              </a-form-item>
            </a-col>

            <a-col :span="12" style="text-align: right;">
              <a-button
                type="text"
                danger
                @click="removeShiftPattern(index)"
              >
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-col>
          </a-row>
        </a-card>
      </a-form-item>

      <a-empty
        v-if="!formData.templateConfig.shiftPattern || formData.templateConfig.shiftPattern.length === 0"
        description="暂无班次配置，请点击上方添加班次按钮"
      />

      <!-- 约束配置 -->
      <a-divider orientation="left">约束配置</a-divider>

      <a-form-item label="最少人数">
        <a-input-number
          v-model:value="minEmployees"
          :min="1"
          :max="100"
          placeholder="最少人数"
          style="width: 200px;"
        />
        <span style="margin-left: 8px; color: #8c8c8c;">每个班次至少需要的员工数</span>
      </a-form-item>

      <a-form-item label="最多人数">
        <a-input-number
          v-model:value="maxEmployees"
          :min="1"
          :max="100"
          placeholder="最多人数"
          style="width: 200px;"
        />
        <span style="margin-left: 8px; color: #8c8c8c;">每个班次最多允许的员工数</span>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import { scheduleApi, type TemplateForm, type TemplateConfig, type ShiftPattern } from '@/api/business/attendance/schedule';
import { shiftApi } from '@/api/business/attendance/shift';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  templateId?: number;
}

/**
 * 组件事件
 */
interface Emits {
  (e: 'update:visible', visible: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// 表单引用
const formRef = ref();

// 部门选项
const departmentOptions = ref([
  { departmentId: 1, departmentName: '技术部' },
  { departmentId: 2, departmentName: '市场部' },
  { departmentId: 3, departmentName: '行政部' },
  { departmentId: 4, departmentName: '财务部' },
  { departmentId: 5, departmentName: '人事部' }
]);

// 班次选项
const shiftOptions = ref<any[]>([]);

// 约束配置
const minEmployees = ref<number>(1);
const maxEmployees = ref<number>(10);

// 表单数据
const formData = ref<TemplateForm>({
  templateId: undefined,
  templateName: '',
  templateType: 'DEPARTMENT',
  departmentId: undefined,
  templateConfig: {
    cycleType: 'WEEKLY',
    shiftPattern: [],
    effectiveDays: [1, 2, 3, 4, 5]
  },
  status: 1,
  applicableDepartments: [],
  description: undefined
});

// 确认加载
const confirmLoading = ref(false);

// 标题
const title = computed(() => props.templateId ? '编辑排班模板' : '新增排班模板');

// 表单验证规则
const rules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  templateType: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  cycleType: [
    { required: true, message: '请选择周期类型', trigger: 'change' }
  ],
  effectiveDays: [
    { required: true, message: '请选择工作日', trigger: 'change' },
    { type: 'array', min: 1, message: '请至少选择一个工作日', trigger: 'change' }
  ]
};

/**
 * 添加班次模式
 */
const addShiftPattern = () => {
  if (!formData.value.templateConfig.shiftPattern) {
    formData.value.templateConfig.shiftPattern = [];
  }

  formData.value.templateConfig.shiftPattern.push({
    dayOfWeek: 1,
    shiftId: undefined,
    shiftName: '',
    requiredEmployees: 1,
    optionalEmployees: []
  });
};

/**
 * 删除班次模式
 */
const removeShiftPattern = (index: number) => {
  formData.value.templateConfig.shiftPattern?.splice(index, 1);
};

/**
 * 班次选择变化处理
 */
const handleShiftChange = (index: number, shiftId: number) => {
  const shift = shiftOptions.value.find(s => s.shiftId === shiftId);
  if (shift) {
    formData.value.templateConfig.shiftPattern![index].shiftName = shift.shiftName;
  }
};

/**
 * 构建约束配置
 */
const buildConstraints = () => {
  const constraints: any[] = [];

  if (minEmployees.value) {
    constraints.push({
      type: 'MIN_EMPLOYEES',
      value: minEmployees.value
    });
  }

  if (maxEmployees.value) {
    constraints.push({
      type: 'MAX_EMPLOYEES',
      value: maxEmployees.value
    });
  }

  return constraints;
};

/**
 * 加载班次选项
 */
const loadShiftOptions = async () => {
  try {
    const res = await shiftApi.getAll();
    if (res.data) {
      shiftOptions.value = res.data || [];
    }
  } catch (error) {
    console.error('加载班次选项失败:', error);
  }
};

/**
 * 加载模板详情
 */
const loadTemplateDetail = async () => {
  if (!props.templateId) return;

  try {
    const res = await scheduleApi.getTemplateById(props.templateId);
    if (res.data) {
      const data = res.data;

      formData.value = {
        templateId: data.templateId,
        templateName: data.templateName,
        templateType: data.templateType,
        departmentId: data.departmentId,
        templateConfig: data.templateConfig,
        status: data.status,
        applicableDepartments: [],
        description: data.description
      };

      // 加载约束配置
      if (data.templateConfig.constraints) {
        data.templateConfig.constraints.forEach((constraint: any) => {
          if (constraint.type === 'MIN_EMPLOYEES') {
            minEmployees.value = constraint.value;
          } else if (constraint.type === 'MAX_EMPLOYEES') {
            maxEmployees.value = constraint.value;
          }
        });
      }
    }
  } catch (error) {
    console.error('加载模板详情失败:', error);
    message.error('加载模板详情失败');
  }
};

/**
 * 重置表单
 */
const resetForm = () => {
  formData.value = {
    templateId: undefined,
    templateName: '',
    templateType: 'DEPARTMENT',
    departmentId: undefined,
    templateConfig: {
      cycleType: 'WEEKLY',
      shiftPattern: [],
      effectiveDays: [1, 2, 3, 4, 5]
    },
    status: 1,
    applicableDepartments: [],
    description: undefined
  };
  minEmployees.value = 1;
  maxEmployees.value = 10;
  formRef.value?.clearValidate();
};

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    confirmLoading.value = true;

    // 添加约束配置
    formData.value.templateConfig.constraints = buildConstraints();

    if (props.templateId) {
      await scheduleApi.updateTemplate(props.templateId, formData.value);
      message.success('更新成功');
    } else {
      await scheduleApi.addTemplate(formData.value);
      message.success('新增成功');
    }

    emit('success');
    handleCancel();
  } catch (error: any) {
    if (error.errorFields) {
      return;
    }
    console.error('提交失败:', error);
    message.error('提交失败');
  } finally {
    confirmLoading.value = false;
  }
};

/**
 * 取消
 */
const handleCancel = () => {
  emit('update:visible', false);
};

/**
 * 监听visible变化
 */
watch(() => props.visible, (visible) => {
  if (visible) {
    loadShiftOptions();
    if (props.templateId) {
      loadTemplateDetail();
    } else {
      resetForm();
    }
  }
});
</script>

<style scoped lang="less">
:deep(.ant-divider) {
  margin: 16px 0;
  font-size: 14px;
  font-weight: 600;
}
</style>
