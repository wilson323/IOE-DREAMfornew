<!--
  @fileoverview 班次表单对话框组件
  @author IOE-DREAM Team
  @description 新增或编辑班次的表单对话框
-->
<template>
  <a-modal
    :visible="visible"
    :title="title"
    :width="800"
    :confirm-loading="confirmLoading"
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
      <a-divider orientation="left">基本信息</a-divider>

      <a-form-item label="班次名称" name="shiftName">
        <a-input
          v-model:value="formData.shiftName"
          placeholder="请输入班次名称"
          maxlength="100"
        />
      </a-form-item>

      <a-form-item label="班次类型" name="shiftType">
        <a-select
          v-model:value="formData.shiftType"
          placeholder="请选择班次类型"
        >
          <a-select-option
            v-for="(name, type) in ShiftTypeNames"
            :key="type"
            :value="type"
          >
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="颜色标识" name="colorCode">
        <a-space>
          <a-input
            v-model:value="formData.colorCode"
            placeholder="请输入颜色代码，如 #FF5722"
            style="width: 200px"
          />
          <input
            type="color"
            v-model="formData.colorCode"
            style="width: 50px; height: 32px; cursor: pointer"
          />
        </a-space>
      </a-form-item>

      <!-- 工作时间 -->
      <a-divider orientation="left">工作时间</a-divider>

      <a-row>
        <a-col :span="12">
          <a-form-item label="上班时间" name="startTime">
            <a-time-picker
              v-model:value="startTimeValue"
              format="HH:mm:ss"
              placeholder="请选择上班时间"
              style="width: 100%"
              @change="handleStartTimeChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="下班时间" name="endTime">
            <a-time-picker
              v-model:value="endTimeValue"
              format="HH:mm:ss"
              placeholder="请选择下班时间"
              style="width: 100%"
              @change="handleEndTimeChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="是否跨天" name="isOvernight">
        <a-switch
          v-model:checked="formData.isOvernight"
          checked-children="是"
          un-checked-children="否"
        />
      </a-form-item>

      <!-- 休息时间 -->
      <a-divider orientation="left">休息时间（可选）</a-divider>

      <a-row>
        <a-col :span="12">
          <a-form-item label="休息时长" name="breakMinutes">
            <a-input-number
              v-model:value="formData.breakMinutes"
              :min="0"
              :max="300"
              placeholder="分钟"
              style="width: 100%"
              addon-after="分钟"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="工作时长" name="workHours">
            <a-input-number
              v-model:value="formData.workHours"
              :min="0"
              :max="24"
              :step="0.5"
              placeholder="小时"
              style="width: 100%"
              addon-after="小时"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row>
        <a-col :span="12">
          <a-form-item label="休息开始" name="breakStartTime">
            <a-time-picker
              v-model:value="breakStartTimeValue"
              format="HH:mm:ss"
              placeholder="请选择休息开始时间"
              style="width: 100%"
              @change="handleBreakStartTimeChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="休息结束" name="breakEndTime">
            <a-time-picker
              v-model:value="breakEndTimeValue"
              format="HH:mm:ss"
              placeholder="请选择休息结束时间"
              style="width: 100%"
              @change="handleBreakEndTimeChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 弹性时间 -->
      <a-divider orientation="left">弹性时间（可选）</a-divider>

      <a-form-item label="弹性班次" name="isFlexible">
        <a-switch
          v-model:checked="formData.isFlexible"
          checked-children="是"
          un-checked-children="否"
        />
      </a-form-item>

      <template v-if="formData.isFlexible">
        <a-row>
          <a-col :span="12">
            <a-form-item label="弹性提前" name="flexibleStartTime">
              <a-input-number
                v-model:value="formData.flexibleStartTime"
                :min="0"
                :max="120"
                placeholder="分钟"
                style="width: 100%"
                addon-after="分钟"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="弹性延后" name="flexibleEndTime">
              <a-input-number
                v-model:value="formData.flexibleEndTime"
                :min="0"
                :max="120"
                placeholder="分钟"
                style="width: 100%"
                addon-after="分钟"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </template>

      <!-- 加班规则 -->
      <a-divider orientation="left">加班规则（可选）</a-divider>

      <a-row>
        <a-col :span="12">
          <a-form-item label="加班开始" name="overtimeStartTime">
            <a-time-picker
              v-model:value="overtimeStartTimeValue"
              format="HH:mm:ss"
              placeholder="请选择加班开始时间"
              style="width: 100%"
              @change="handleOvertimeStartTimeChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="最小加班" name="minOvertimeMinutes">
            <a-input-number
              v-model:value="formData.minOvertimeMinutes"
              :min="0"
              :max="300"
              placeholder="分钟"
              style="width: 100%"
              addon-after="分钟"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 其他信息 -->
      <a-divider orientation="left">其他信息</a-divider>

      <a-form-item label="排序号" name="sortOrder">
        <a-input-number
          v-model:value="formData.sortOrder"
          :min="0"
          :max="9999"
          placeholder="请输入排序号"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="备注" name="remarks">
        <a-text-area
          v-model:value="formData.remarks"
          placeholder="请输入备注"
          :rows="3"
          :maxlength="500"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  shiftApi,
  type ShiftForm,
  ShiftType,
  ShiftTypeNames
} from '@/api/business/attendance/shift';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  shiftId?: number;
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

// 表单数据
const formData = ref<ShiftForm>({
  shiftName: '',
  shiftType: ShiftType.DAY_SHIFT,
  startTime: '',
  endTime: '',
  workHours: undefined,
  breakMinutes: undefined,
  breakStartTime: undefined,
  breakEndTime: undefined,
  isOvernight: false,
  isFlexible: false,
  flexibleStartTime: undefined,
  flexibleEndTime: undefined,
  overtimeStartTime: undefined,
  minOvertimeMinutes: undefined,
  colorCode: '#1890ff',
  remarks: undefined,
  sortOrder: 0
});

// 时间选择器值
const startTimeValue = ref<Dayjs>();
const endTimeValue = ref<Dayjs>();
const breakStartTimeValue = ref<Dayjs>();
const breakEndTimeValue = ref<Dayjs>();
const overtimeStartTimeValue = ref<Dayjs>();

// 确认加载
const confirmLoading = ref(false);

// 标题
const title = computed(() => props.shiftId ? '编辑班次' : '新增班次');

// 表单验证规则
const rules = {
  shiftName: [
    { required: true, message: '请输入班次名称', trigger: 'blur' },
    { max: 100, message: '班次名称长度不能超过100个字符', trigger: 'blur' }
  ],
  shiftType: [
    { required: true, message: '请选择班次类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择上班时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择下班时间', trigger: 'change' }
  ]
};

// 时间处理函数
const handleStartTimeChange = (time: Dayjs) => {
  formData.value.startTime = time ? time.format('HH:mm:ss') : '';
};

const handleEndTimeChange = (time: Dayjs) => {
  formData.value.endTime = time ? time.format('HH:mm:ss') : '';
};

const handleBreakStartTimeChange = (time: Dayjs) => {
  formData.value.breakStartTime = time ? time.format('HH:mm:ss') : '';
};

const handleBreakEndTimeChange = (time: Dayjs) => {
  formData.value.breakEndTime = time ? time.format('HH:mm:ss') : '';
};

const handleOvertimeStartTimeChange = (time: Dayjs) => {
  formData.value.overtimeStartTime = time ? time.format('HH:mm:ss') : '';
};

// 加载班次详情
const loadShiftDetail = async () => {
  if (!props.shiftId) return;

  try {
    const res = await shiftApi.getById(props.shiftId);
    if (res.data) {
      const data = res.data;
      formData.value = {
        shiftId: data.shiftId,
        shiftName: data.shiftName,
        shiftType: data.shiftType,
        startTime: data.startTime,
        endTime: data.endTime,
        workHours: data.workHours,
        breakMinutes: data.breakMinutes,
        breakStartTime: data.breakStartTime,
        breakEndTime: data.breakEndTime,
        isOvernight: data.isOvernight,
        isFlexible: data.isFlexible,
        flexibleStartTime: data.flexibleStartTime,
        flexibleEndTime: data.flexibleEndTime,
        overtimeStartTime: data.overtimeStartTime,
        minOvertimeMinutes: data.minOvertimeMinutes,
        colorCode: data.colorCode,
        remarks: data.remarks,
        sortOrder: data.sortOrder
      };

      // 设置时间选择器值
      startTimeValue.value = data.startTime ? dayjs(data.startTime, 'HH:mm:ss') : undefined;
      endTimeValue.value = data.endTime ? dayjs(data.endTime, 'HH:mm:ss') : undefined;
      breakStartTimeValue.value = data.breakStartTime ? dayjs(data.breakStartTime, 'HH:mm:ss') : undefined;
      breakEndTimeValue.value = data.breakEndTime ? dayjs(data.breakEndTime, 'HH:mm:ss') : undefined;
      overtimeStartTimeValue.value = data.overtimeStartTime ? dayjs(data.overtimeStartTime, 'HH:mm:ss') : undefined;
    }
  } catch (error) {
    console.error('加载班次详情失败:', error);
    message.error('加载班次详情失败');
  }
};

// 重置表单
const resetForm = () => {
  formData.value = {
    shiftName: '',
    shiftType: ShiftType.DAY_SHIFT,
    startTime: '',
    endTime: '',
    workHours: undefined,
    breakMinutes: undefined,
    breakStartTime: undefined,
    breakEndTime: undefined,
    isOvernight: false,
    isFlexible: false,
    flexibleStartTime: undefined,
    flexibleEndTime: undefined,
    overtimeStartTime: undefined,
    minOvertimeMinutes: undefined,
    colorCode: '#1890ff',
    remarks: undefined,
    sortOrder: 0
  };
  startTimeValue.value = undefined;
  endTimeValue.value = undefined;
  breakStartTimeValue.value = undefined;
  breakEndTimeValue.value = undefined;
  overtimeStartTimeValue.value = undefined;
  formRef.value?.clearValidate();
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    confirmLoading.value = true;

    const data = { ...formData.value };

    if (props.shiftId) {
      await shiftApi.update(props.shiftId, data);
      message.success('更新成功');
    } else {
      await shiftApi.add(data);
      message.success('新增成功');
    }

    emit('success');
    handleCancel();
  } catch (error: any) {
    if (error.errorFields) {
      // 表单验证失败
      return;
    }
    console.error('提交失败:', error);
    message.error('提交失败');
  } finally {
    confirmLoading.value = false;
  }
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
};

// 监听visible变化
watch(() => props.visible, (visible) => {
  if (visible) {
    if (props.shiftId) {
      loadShiftDetail();
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
