<!--
  @fileoverview 排班记录对话框组件
  @author IOE-DREAM Team
  @description 新增或编辑排班记录
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
      :wrapper-col="{ span: 16 }"
    >
      <!-- 基本信息 -->
      <a-divider orientation="left">基本信息</a-divider>

      <a-form-item label="工作日期" name="workDate">
        <a-date-picker
          v-model:value="workDateValue"
          format="YYYY-MM-DD"
          placeholder="请选择工作日期"
          style="width: 100%;"
          @change="handleWorkDateChange"
        />
      </a-form-item>

      <a-form-item label="班次" name="shiftId">
        <a-select
          v-model:value="formData.shiftId"
          placeholder="请选择班次"
          show-search
          :filter-option="filterOption"
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

      <a-form-item label="员工" name="employeeId">
        <a-select
          v-model:value="formData.employeeId"
          placeholder="请选择员工"
          show-search
          :filter-option="filterOption"
        >
          <a-select-option
            v-for="emp in employeeOptions"
            :key="emp.employeeId"
            :value="emp.employeeId"
          >
            {{ emp.employeeName }}
            <a-tag color="blue" style="margin-left: 8px;">{{ emp.departmentName }}</a-tag>
          </a-select-option>
        </a-select>
      </a-form-item>

      <!-- 时间信息 -->
      <a-divider orientation="left">时间信息</a-divider>

      <a-row>
        <a-col :span="12">
          <a-form-item label="开始时间" name="startTime">
            <a-time-picker
              v-model:value="startTimeValue"
              format="HH:mm:ss"
              placeholder="请选择开始时间"
              style="width: 100%;"
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
              style="width: 100%;"
              @change="handleEndTimeChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="工作时长" name="workHours">
        <a-input-number
          v-model:value="formData.workHours"
          :min="0"
          :max="24"
          :precision="2"
          placeholder="小时"
          style="width: 100%;"
          addon-after="小时"
        />
      </a-form-item>

      <!-- 其他信息 -->
      <a-divider orientation="left">其他信息</a-divider>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">正常</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="备注" name="notes">
        <a-text-area
          v-model:value="formData.notes"
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
import { scheduleApi, type ScheduleRecordForm } from '@/api/business/attendance/schedule';
import { shiftApi } from '@/api/business/attendance/shift';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  recordId?: number;
  date?: string;
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
const shiftOptions = ref<any[]>([]);

// 员工选项
const employeeOptions = ref([
  { employeeId: 1, employeeName: '张三', departmentName: '技术部' },
  { employeeId: 2, employeeName: '李四', departmentName: '市场部' },
  { employeeId: 3, employeeName: '王五', departmentName: '行政部' }
]);

// 表单数据
const formData = ref<ScheduleRecordForm>({
  planId: 1, // TODO: 从props传入
  employeeId: undefined,
  shiftId: undefined,
  workDate: '',
  startTime: '',
  endTime: '',
  workHours: undefined,
  status: 1,
  notes: undefined
});

// 日期选择器值
const workDateValue = ref<Dayjs>();
const startTimeValue = ref<Dayjs>();
const endTimeValue = ref<Dayjs>();

// 确认加载
const confirmLoading = ref(false);

// 标题
const title = computed(() => props.recordId ? '编辑排班记录' : '新增排班记录');

// 表单验证规则
const rules = {
  workDate: [
    { required: true, message: '请选择工作日期', trigger: 'change' }
  ],
  shiftId: [
    { required: true, message: '请选择班次', trigger: 'change' }
  ],
  employeeId: [
    { required: true, message: '请选择员工', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
};

// 时间处理函数
const handleWorkDateChange = (date: Dayjs) => {
  formData.value.workDate = date ? date.format('YYYY-MM-DD') : '';
};

const handleStartTimeChange = (time: Dayjs) => {
  formData.value.startTime = time ? time.format('HH:mm:ss') : '';
  calculateWorkHours();
};

const handleEndTimeChange = (time: Dayjs) => {
  formData.value.endTime = time ? time.format('HH:mm:ss') : '';
  calculateWorkHours();
};

// 计算工作时长
const calculateWorkHours = () => {
  if (formData.value.startTime && formData.value.endTime) {
    const start = dayjs(`2000-01-01 ${formData.value.startTime}`);
    const end = dayjs(`2000-01-01 ${formData.value.endTime}`);
    if (end.isBefore(start)) {
      // 跨天情况
      const duration = end.add(1, 'day').diff(start, 'hour', true);
      formData.value.workHours = parseFloat(duration.toFixed(2));
    } else {
      const duration = end.diff(start, 'hour', true);
      formData.value.workHours = parseFloat(duration.toFixed(2));
    }
  }
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

// 搜索过滤
const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase()) ||
         option.children[1]?.toLowerCase().includes(input.toLowerCase());
};

// 加载记录详情
const loadRecordDetail = async () => {
  if (!props.recordId) return;

  try {
    const res = await scheduleApi.getRecordDetail(props.recordId);
    if (res.data) {
      const data = res.data;
      formData.value = {
        recordId: data.recordId,
        planId: data.planId,
        employeeId: data.employeeId,
        shiftId: data.shiftId,
        workDate: data.workDate,
        startTime: data.startTime,
        endTime: data.endTime,
        workHours: undefined, // 需要计算
        status: data.status,
        notes: undefined
      };

      // 设置选择器值
      workDateValue.value = data.workDate ? dayjs(data.workDate) : undefined;
      startTimeValue.value = data.startTime ? dayjs(`2000-01-01 ${data.startTime}`) : undefined;
      endTimeValue.value = data.endTime ? dayjs(`2000-01-01 ${data.endTime}`) : undefined;

      calculateWorkHours();
    }
  } catch (error) {
    console.error('加载记录详情失败:', error);
    message.error('加载记录详情失败');
  }
};

// 重置表单
const resetForm = () => {
  formData.value = {
    planId: 1,
    employeeId: undefined,
    shiftId: undefined,
    workDate: '',
    startTime: '',
    endTime: '',
    workHours: undefined,
    status: 1,
    notes: undefined
  };
  workDateValue.value = undefined;
  startTimeValue.value = undefined;
  endTimeValue.value = undefined;
  formRef.value?.clearValidate();
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    confirmLoading.value = true;

    if (props.recordId) {
      // TODO: 调用更新API
      message.success('更新成功');
    } else {
      await scheduleApi.saveScheduleRecord(formData.value);
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
    if (props.date) {
      workDateValue.value = dayjs(props.date);
      formData.value.workDate = props.date;
    }
    if (props.recordId) {
      loadRecordDetail();
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
