<!--
 * 访客登记页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-registration-page">
    <a-card :bordered="false" title="访客登记">
      <a-steps :current="currentStep" class="steps">
        <a-step title="身份验证" />
        <a-step title="信息登记" />
        <a-step title="完成登记" />
      </a-steps>

      <!-- 步骤1：身份验证 -->
      <div v-if="currentStep === 0" class="step-content">
        <a-form layout="vertical">
          <a-form-item label="身份证号" required>
            <a-input
              v-model:value="registrationForm.idCardNumber"
              placeholder="请输入身份证号"
              size="large"
            />
          </a-form-item>
          <a-form-item label="手机号" required>
            <a-input
              v-model:value="registrationForm.phoneNumber"
              placeholder="请输入手机号"
              size="large"
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" size="large" @click="validateInfo" :loading="validating">
                验证信息
              </a-button>
              <a-button size="large" @click="goBack">返回</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 步骤2：信息登记 -->
      <div v-if="currentStep === 1" class="step-content">
        <a-form :model="registrationForm" layout="vertical">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="访客姓名" required>
                <a-input v-model:value="registrationForm.visitorName" placeholder="请输入访客姓名" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="来访公司">
                <a-input v-model:value="registrationForm.company" placeholder="请输入来访公司" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="预约类型" required>
                <a-select v-model:value="registrationForm.appointmentType" placeholder="请选择预约类型">
                  <a-select-option value="BUSINESS">商务拜访</a-select-option>
                  <a-select-option value="PERSONAL">个人拜访</a-select-option>
                  <a-select-option value="INTERVIEW">面试</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="预约时间" required>
                <a-date-picker
                  v-model:value="registrationForm.appointmentTime"
                  show-time
                  format="YYYY-MM-DD HH:mm"
                  placeholder="选择预约时间"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="来访事由" required>
            <a-textarea
              v-model:value="registrationForm.purpose"
              placeholder="请输入来访事由"
              :rows="3"
            />
          </a-form-item>

          <a-form-item label="被访人" required>
            <a-input v-model:value="registrationForm.visiteeId" placeholder="请输入被访人ID" />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button @click="prevStep">上一步</a-button>
              <a-button type="primary" @click="submitRegistration" :loading="submitting">
                提交登记
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 步骤3：完成登记 -->
      <div v-if="currentStep === 2" class="step-content success-content">
        <a-result
          status="success"
          title="登记成功"
          sub-title="访客预约已创建，请等待审批"
        >
          <template #extra>
            <a-space>
              <a-button type="primary" @click="resetForm">继续登记</a-button>
              <a-button @click="goToAppointments">查看预约</a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { visitorApi } from '/@/api/business/visitor/visitor-api';

const router = useRouter();

// 当前步骤
const currentStep = ref(0);
const validating = ref(false);
const submitting = ref(false);

// 登记表单
const registrationForm = reactive({
  idCardNumber: '',
  phoneNumber: '',
  visitorName: '',
  company: '',
  appointmentType: undefined,
  appointmentTime: null,
  purpose: '',
  visiteeId: null
});

// 验证信息
const validateInfo = async () => {
  if (!registrationForm.idCardNumber || !registrationForm.phoneNumber) {
    message.warning('请输入身份证号和手机号');
    return;
  }

  validating.value = true;
  try {
    const result = await visitorApi.validateVisitorInfo(
      registrationForm.idCardNumber,
      registrationForm.phoneNumber
    );
    if (result.code === 200 && result.data) {
      if (result.data.isValid) {
        // 如果访客已存在，自动填充信息
        if (result.data.visitorName) {
          registrationForm.visitorName = result.data.visitorName;
        }
        message.success('验证成功，请继续填写信息');
        currentStep.value = 1;
      } else {
        message.success('新访客，请填写完整信息');
        currentStep.value = 1;
      }
    } else {
      message.error(result.message || '验证失败');
    }
  } catch (error) {
    message.error('验证失败');
    console.error('验证失败:', error);
  } finally {
    validating.value = false;
  }
};

// 提交登记
const submitRegistration = async () => {
  // 验证必填项
  if (!registrationForm.visitorName || !registrationForm.appointmentType ||
      !registrationForm.appointmentTime || !registrationForm.purpose || !registrationForm.visiteeId) {
    message.warning('请填写所有必填项');
    return;
  }

  submitting.value = true;
  try {
    const data = {
      visitorName: registrationForm.visitorName,
      phone: registrationForm.phoneNumber,
      idCard: registrationForm.idCardNumber,
      company: registrationForm.company,
      appointmentType: registrationForm.appointmentType,
      appointmentTime: registrationForm.appointmentTime,
      purpose: registrationForm.purpose,
      visiteeId: registrationForm.visiteeId
    };

    const result = await visitorApi.createAppointment(data);
    if (result.code === 200) {
      message.success('登记成功');
      currentStep.value = 2;
    } else {
      message.error(result.message || '登记失败');
    }
  } catch (error) {
    message.error('登记失败');
    console.error('登记失败:', error);
  } finally {
    submitting.value = false;
  }
};

// 上一步
const prevStep = () => {
  currentStep.value--;
};

// 重置表单
const resetForm = () => {
  Object.assign(registrationForm, {
    idCardNumber: '',
    phoneNumber: '',
    visitorName: '',
    company: '',
    appointmentType: undefined,
    appointmentTime: null,
    purpose: '',
    visiteeId: null
  });
  currentStep.value = 0;
};

// 返回
const goBack = () => {
  router.back();
};

// 查看预约
const goToAppointments = () => {
  router.push('/business/visitor/appointment');
};
</script>

<style lang="scss" scoped>
.visitor-registration-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.steps {
  margin: 32px 0 48px;
}

.step-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.success-content {
  text-align: center;
  padding: 48px 24px;
}
</style>

