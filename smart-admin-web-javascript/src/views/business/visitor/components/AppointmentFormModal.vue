<!--
 * 访客预约表单弹窗组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑预约' : '创建预约'"
    width="800px"
    @cancel="handleCancel"
    :confirm-loading="submitting"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="访客姓名" name="visitorName">
            <a-input v-model:value="formData.visitorName" placeholder="请输入访客姓名" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="手机号" name="phone">
            <a-input v-model:value="formData.phone" placeholder="请输入手机号" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="身份证号" name="idCard">
            <a-input v-model:value="formData.idCard" placeholder="请输入身份证号" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="来访公司">
            <a-input v-model:value="formData.company" placeholder="请输入来访公司" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="预约类型" name="appointmentType">
            <a-select v-model:value="formData.appointmentType" placeholder="请选择预约类型">
              <a-select-option
                v-for="type in appointmentTypes"
                :key="type.code"
                :value="type.code"
              >
                {{ type.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="预约时间" name="appointmentTime">
            <a-date-picker
              v-model:value="formData.appointmentTime"
              show-time
              format="YYYY-MM-DD HH:mm"
              placeholder="选择预约时间"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="来访事由" name="purpose">
        <a-textarea
          v-model:value="formData.purpose"
          placeholder="请输入来访事由"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="被访人" name="visiteeId">
        <a-input-number
          v-model:value="formData.visiteeId"
          placeholder="请输入被访人ID"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="访问区域">
        <a-select v-model:value="formData.areaId" placeholder="请选择访问区域">
          <a-select-option
            v-for="area in visitAreas"
            :key="area.id"
            :value="area.id"
          >
            {{ area.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="submitting">
          提交
        </a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  CalendarOutlined,
  UserOutlined,
  TeamOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue';
import { visitorApi } from '/@/api/business/visitor/visitor-api';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  appointmentData: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref(null);
const submitting = ref(false);
const appointmentTypes = ref([]);
const visitAreas = ref([]);

// 是否编辑模式
const isEdit = computed(() => !!props.appointmentData);

// 表单数据
const formData = reactive({
  visitorName: '',
  phone: '',
  idCard: '',
  company: '',
  appointmentType: undefined,
  appointmentTime: null,
  purpose: '',
  visiteeId: null,
  areaId: undefined
});

// 表单验证规则
const formRules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  appointmentType: [{ required: true, message: '请选择预约类型', trigger: 'change' }],
  appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }],
  purpose: [{ required: true, message: '请输入来访事由', trigger: 'blur' }],
  visiteeId: [{ required: true, message: '请输入被访人ID', trigger: 'blur' }]
};

// 监听弹窗显示
watch(() => props.visible, (val) => {
  if (val) {
    loadAppointmentTypes();
    loadVisitAreas();
    if (props.appointmentData) {
      Object.assign(formData, props.appointmentData);
    } else {
      resetForm();
    }
  }
});

// 加载预约类型
const loadAppointmentTypes = async () => {
  try {
    const result = await visitorApi.getAppointmentTypes();
    if (result.code === 200 && result.data) {
      appointmentTypes.value = result.data;
    }
  } catch (error) {
    console.error('加载预约类型失败:', error);
  }
};

// 加载访问区域
const loadVisitAreas = async () => {
  try {
    const result = await visitorApi.getVisitAreas();
    if (result.code === 200 && result.data) {
      visitAreas.value = result.data;
    }
  } catch (error) {
    console.error('加载访问区域失败:', error);
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    submitting.value = true;
    const result = await visitorApi.createAppointment(formData);
    if (result.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功');
      emit('success');
      handleCancel();
    } else {
      message.error(result.message || '操作失败');
    }
  } catch (error) {
    if (error.errorFields) {
      message.warning('请填写必填项');
    } else {
      message.error('操作失败');
      console.error('操作失败:', error);
    }
  } finally {
    submitting.value = false;
  }
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
  resetForm();
};

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    visitorName: '',
    phone: '',
    idCard: '',
    company: '',
    appointmentType: undefined,
    appointmentTime: null,
    purpose: '',
    visiteeId: null,
    areaId: undefined
  });
  formRef.value?.resetFields();
};
</script>

<style lang="scss" scoped>
.step-content {
  padding: 24px 0;
}
</style>

