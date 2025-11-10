<!--
  新建申请表单组件

  @Author:    Claude Code
  @Date:      2025-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="application-form">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <!-- 请假申请 -->
      <template v-if="type === 'leave'">
        <a-form-item label="请假类型" name="leaveType">
          <a-select v-model:value="formData.leaveType" placeholder="请选择请假类型">
            <a-select-option value="annual">年假</a-select-option>
            <a-select-option value="sick">病假</a-select-option>
            <a-select-option value="personal">事假</a-select-option>
            <a-select-option value="marriage">婚假</a-select-option>
            <a-select-option value="maternity">产假</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="请假时间" name="dateRange">
          <a-range-picker
            v-model:value="formData.dateRange"
            show-time
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="请假天数" name="days">
          <a-input-number
            v-model:value="formData.days"
            :min="0.5"
            :step="0.5"
            style="width: 100%"
            placeholder="自动计算或手动输入"
          />
        </a-form-item>
      </template>

      <!-- 加班申请 -->
      <template v-if="type === 'overtime'">
        <a-form-item label="加班类型" name="overtimeType">
          <a-select v-model:value="formData.overtimeType" placeholder="请选择加班类型">
            <a-select-option value="weekday">工作日加班</a-select-option>
            <a-select-option value="weekend">周末加班</a-select-option>
            <a-select-option value="holiday">节假日加班</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="加班时间" name="dateRange">
          <a-range-picker
            v-model:value="formData.dateRange"
            show-time
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="加班餐补">
          <a-checkbox-group v-model:value="formData.allowances">
            <a-checkbox value="meal">加班餐补</a-checkbox>
            <a-checkbox value="transport">交通补贴</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
      </template>

      <!-- 补签申请 -->
      <template v-if="type === 'makeup'">
        <a-form-item label="补签类型" name="makeupType">
          <a-radio-group v-model:value="formData.makeupType">
            <a-radio value="signin">补签到</a-radio>
            <a-radio value="signout">补签退</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="补签日期" name="date">
          <a-date-picker v-model:value="formData.date" style="width: 100%" />
        </a-form-item>
        <a-form-item label="补签时间" name="time">
          <a-time-picker v-model:value="formData.time" style="width: 100%" format="HH:mm" />
        </a-form-item>
        <a-form-item label="证明人" name="witness">
          <a-input v-model:value="formData.witness" placeholder="请输入证明人姓名" />
        </a-form-item>
      </template>

      <!-- 调班申请 -->
      <template v-if="type === 'shift'">
        <a-form-item label="调班类型" name="shiftType">
          <a-radio-group v-model:value="formData.shiftType">
            <a-radio value="exchange">员工间调班</a-radio>
            <a-radio value="personal">个人调班</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="原班次日期" name="originalDate">
          <a-date-picker v-model:value="formData.originalDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="调班后日期" name="newDate">
          <a-date-picker v-model:value="formData.newDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="调班对象" v-if="formData.shiftType === 'exchange'">
          <a-select v-model:value="formData.shiftPartner" placeholder="请选择调班对象">
            <a-select-option value="zhang">张三 - 技术研发部</a-select-option>
            <a-select-option value="li">李四 - 技术研发部</a-select-option>
            <a-select-option value="wang">王五 - 市场部</a-select-option>
          </a-select>
        </a-form-item>
      </template>

      <!-- 销假申请 -->
      <template v-if="type === 'cancel'">
        <a-form-item label="选择请假记录" name="leaveRecord">
          <a-select v-model:value="formData.leaveRecord" placeholder="请选择要销假的请假记录">
            <a-select-option value="APP001">年假申请 - 2024-01-10至2024-01-12（3天）</a-select-option>
            <a-select-option value="APP002">病假申请 - 2024-01-05至2024-01-06（2天）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="销假类型" name="cancelType">
          <a-radio-group v-model:value="formData.cancelType">
            <a-radio value="full">全部销假</a-radio>
            <a-radio value="partial">部分销假</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="销假时间" v-if="formData.cancelType === 'partial'">
          <a-range-picker
            v-model:value="formData.dateRange"
            show-time
            style="width: 100%"
          />
        </a-form-item>
      </template>

      <!-- 申请原因 -->
      <a-form-item label="申请原因" name="reason">
        <a-textarea
          v-model:value="formData.reason"
          :rows="4"
          placeholder="请详细说明申请原因..."
        />
      </a-form-item>

      <!-- 证明文件 -->
      <a-form-item label="证明文件">
        <a-upload
          v-model:file-list="fileList"
          :before-upload="beforeUpload"
          :max-count="5"
          multiple
        >
          <a-button>
            <template #icon><UploadOutlined /></template>
            上传文件
          </a-button>
        </a-upload>
        <div class="upload-hint">支持 JPG、PNG、PDF 格式，最大 10MB</div>
      </a-form-item>

      <!-- 操作按钮 -->
      <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
        <a-space>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">
            提交申请
          </a-button>
          <a-button @click="handleSaveDraft">
            保存草稿
          </a-button>
          <a-button @click="handleCancel">
            取消
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { message } from 'ant-design-vue';
import { UploadOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';

const props = defineProps({
  type: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(['submit', 'cancel']);

// 响应式数据
const formRef = ref();
const submitting = ref(false);
const fileList = ref([]);

const formData = reactive({
  leaveType: '',
  overtimeType: '',
  makeupType: 'signin',
  shiftType: 'exchange',
  cancelType: 'full',
  dateRange: null,
  date: null,
  time: null,
  days: null,
  allowances: [],
  witness: '',
  originalDate: null,
  newDate: null,
  shiftPartner: '',
  leaveRecord: '',
  reason: '',
});

// 表单验证规则
const formRules = {
  leaveType: [
    { required: true, message: '请选择请假类型', trigger: 'change' },
  ],
  dateRange: [
    { required: true, message: '请选择时间', trigger: 'change' },
  ],
  days: [
    { required: true, message: '请输入天数', trigger: 'change' },
  ],
  overtimeType: [
    { required: true, message: '请选择加班类型', trigger: 'change' },
  ],
  makeupType: [
    { required: true, message: '请选择补签类型', trigger: 'change' },
  ],
  date: [
    { required: true, message: '请选择补签日期', trigger: 'change' },
  ],
  time: [
    { required: true, message: '请选择补签时间', trigger: 'change' },
  ],
  witness: [
    { required: true, message: '请输入证明人', trigger: 'blur' },
  ],
  shiftType: [
    { required: true, message: '请选择调班类型', trigger: 'change' },
  ],
  originalDate: [
    { required: true, message: '请选择原班次日期', trigger: 'change' },
  ],
  newDate: [
    { required: true, message: '请选择调班后日期', trigger: 'change' },
  ],
  cancelType: [
    { required: true, message: '请选择销假类型', trigger: 'change' },
  ],
  leaveRecord: [
    { required: true, message: '请选择请假记录', trigger: 'change' },
  ],
  reason: [
    { required: true, message: '请输入申请原因', trigger: 'blur' },
  ],
};

// 方法
const beforeUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'application/pdf'].includes(file.type);
  if (!isValidType) {
    message.error('只能上传 JPG、PNG、PDF 格式的文件');
    return false;
  }
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB');
    return false;
  }
  return false; // 阻止自动上传
};

const handleSubmit = async () => {
  try {
    await formRef.value.validateFields();
    submitting.value = true;

    // 模拟提交
    await new Promise(resolve => setTimeout(resolve, 1000));

    emit('submit', {
      type: props.type,
      data: formData,
      attachments: fileList.value,
    });
  } catch (error) {
    console.error('表单验证失败:', error);
  } finally {
    submitting.value = false;
  }
};

const handleSaveDraft = () => {
  message.success('草稿已保存');
};

const handleCancel = () => {
  emit('cancel');
};

// 监听类型变化，重置表单
watch(
  () => props.type,
  () => {
    Object.keys(formData).forEach(key => {
      if (Array.isArray(formData[key])) {
        formData[key] = [];
      } else {
        formData[key] = null;
      }
    });
    fileList.value = [];
  }
);
</script>

<style lang="less" scoped>
.application-form {
  .upload-hint {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }
}
</style>
