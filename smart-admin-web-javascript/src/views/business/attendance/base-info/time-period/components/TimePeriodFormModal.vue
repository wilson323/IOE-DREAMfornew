<!--
  @fileoverview 时间段表单对话框组件
  @author IOE-DREAM Team
  @description 新增或编辑打卡时间段的表单对话框
-->
<template>
  <a-modal
    :visible="visible"
    :title="title"
    :width="700"
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

      <a-form-item label="时间段名称" name="periodName">
        <a-input
          v-model:value="formData.periodName"
          placeholder="请输入时间段名称"
          maxlength="100"
        />
      </a-form-item>

      <a-form-item label="时间段类型" name="periodType">
        <a-select
          v-model:value="formData.periodType"
          placeholder="请选择时间段类型"
        >
          <a-select-option
            v-for="(name, type) in TimePeriodTypeNames"
            :key="type"
            :value="type"
          >
            {{ name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <!-- 时间配置 -->
      <a-divider orientation="left">时间配置</a-divider>

      <a-row>
        <a-col :span="12">
          <a-form-item label="开始时间" name="startTime">
            <a-time-picker
              v-model:value="startTimeValue"
              format="HH:mm:ss"
              placeholder="请选择开始时间"
              style="width: 100%"
              @change="handleStartTimeChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="结束时间" name="endTime">
            <a-time-picker
              v-model:value="endTimeValue"
              format="HH:mm:ss"
              placeholder="请选择结束时间"
              style="width: 100%"
              @change="handleEndTimeChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 弹性时间配置 -->
      <a-divider orientation="left">弹性时间配置（可选）</a-divider>

      <a-form-item label="启用弹性" name="isFlexible">
        <a-switch
          v-model:checked="formData.isFlexible"
          checked-children="是"
          un-checked-children="否"
        />
      </a-form-item>

      <template v-if="formData.isFlexible">
        <a-row>
          <a-col :span="12">
            <a-form-item label="允许提前" name="allowEarlyMinutes">
              <a-input-number
                v-model:value="formData.allowEarlyMinutes"
                :min="0"
                :max="120"
                placeholder="分钟"
                style="width: 100%"
                addon-after="分钟"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="允许延后" name="allowLateMinutes">
              <a-input-number
                v-model:value="formData.allowLateMinutes"
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

      <!-- 最小工作时长 -->
      <a-form-item label="最小工作时长" name="minWorkMinutes">
        <a-input-number
          v-model:value="formData.minWorkMinutes"
          :min="0"
          :max="1440"
          placeholder="分钟"
          style="width: 100%"
          addon-after="分钟"
        />
        <span style="margin-left: 8px; color: rgba(0, 0, 0, 0.45);">
          用于判断是否计入有效考勤
        </span>
      </a-form-item>

      <!-- 关联班次 -->
      <a-form-item label="关联班次" name="shiftId">
        <a-select
          v-model:value="formData.shiftId"
          placeholder="请选择关联班次（可选）"
          allow-clear
          show-search
          :filter-option="filterOption"
        >
          <a-select-option
            v-for="shift in shiftOptions"
            :key="shift.shiftId"
            :value="shift.shiftId"
          >
            {{ shift.shiftName }} ({{ shift.startTime }} ~ {{ shift.endTime }})
          </a-select-option>
        </a-select>
      </a-form-item>

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
import { ref, watch, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  timePeriodApi,
  type TimePeriodForm,
  type TimePeriodOption,
  TimePeriodType,
  TimePeriodTypeNames
} from '@/api/business/attendance/time-period';
import { shiftApi } from '@/api/business/attendance/shift';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  periodId?: number;
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

// 班次选项
const shiftOptions = ref<TimePeriodOption[]>([]);

// 表单数据
const formData = ref<TimePeriodForm>({
  periodName: '',
  periodType: TimePeriodType.MORNING_CHECK_IN,
  startTime: '',
  endTime: '',
  allowEarlyMinutes: 0,
  allowLateMinutes: 0,
  isFlexible: false,
  minWorkMinutes: undefined,
  shiftId: undefined,
  remarks: undefined,
  sortOrder: 0
});

// 时间选择器值
const startTimeValue = ref<Dayjs>();
const endTimeValue = ref<Dayjs>();

// 确认加载
const confirmLoading = ref(false);

// 标题
const title = computed(() => props.periodId ? '编辑时间段' : '新增时间段');

// 表单验证规则
const rules = {
  periodName: [
    { required: true, message: '请输入时间段名称', trigger: 'blur' },
    { max: 100, message: '时间段名称长度不能超过100个字符', trigger: 'blur' }
  ],
  periodType: [
    { required: true, message: '请选择时间段类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
};

// 时间处理函数
const handleStartTimeChange = (time: Dayjs) => {
  formData.value.startTime = time ? time.format('HH:mm:ss') : '';
};

const handleEndTimeChange = (time: Dayjs) => {
  formData.value.endTime = time ? time.format('HH:mm:ss') : '';
};

// 加载班次选项
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

// 班次搜索过滤
const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

// 加载时间段详情
const loadPeriodDetail = async () => {
  if (!props.periodId) return;

  try {
    const res = await timePeriodApi.getById(props.periodId);
    if (res.data) {
      const data = res.data;
      formData.value = {
        periodId: data.periodId,
        periodName: data.periodName,
        periodType: data.periodType,
        startTime: data.startTime,
        endTime: data.endTime,
        allowEarlyMinutes: data.allowEarlyMinutes,
        allowLateMinutes: data.allowLateMinutes,
        isFlexible: data.isFlexible,
        minWorkMinutes: data.minWorkMinutes,
        shiftId: data.shiftId,
        remarks: data.remarks,
        sortOrder: data.sortOrder
      };

      // 设置时间选择器值
      startTimeValue.value = data.startTime ? dayjs(data.startTime, 'HH:mm:ss') : undefined;
      endTimeValue.value = data.endTime ? dayjs(data.endTime, 'HH:mm:ss') : undefined;
    }
  } catch (error) {
    console.error('加载时间段详情失败:', error);
    message.error('加载时间段详情失败');
  }
};

// 重置表单
const resetForm = () => {
  formData.value = {
    periodName: '',
    periodType: TimePeriodType.MORNING_CHECK_IN,
    startTime: '',
    endTime: '',
    allowEarlyMinutes: 0,
    allowLateMinutes: 0,
    isFlexible: false,
    minWorkMinutes: undefined,
    shiftId: undefined,
    remarks: undefined,
    sortOrder: 0
  };
  startTimeValue.value = undefined;
  endTimeValue.value = undefined;
  formRef.value?.clearValidate();
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    confirmLoading.value = true;

    const data = { ...formData.value };

    if (props.periodId) {
      await timePeriodApi.update(props.periodId, data);
      message.success('更新成功');
    } else {
      await timePeriodApi.add(data);
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

// 取消
const handleCancel = () => {
  emit('update:visible', false);
};

// 监听visible变化
watch(() => props.visible, (visible) => {
  if (visible) {
    loadShiftOptions();
    if (props.periodId) {
      loadPeriodDetail();
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
