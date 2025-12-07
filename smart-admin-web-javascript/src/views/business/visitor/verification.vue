<!--
 * 访客验证页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-verification-page">
    <a-card :bordered="false" title="访客验证">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 二维码验证 -->
        <a-tab-pane key="qrcode" tab="二维码验证">
          <div class="verification-content">
            <div class="scan-area">
              <div class="scan-box">
                <ScanOutlined style="font-size: 64px; color: #1890ff;" />
                <p>请扫描访客二维码</p>
              </div>
              <a-input
                v-model:value="qrCodeValue"
                placeholder="或手动输入二维码内容"
                size="large"
                class="qr-input"
              >
                <template #addonAfter>
                  <a-button type="primary" @click="verifyQRCode" :loading="verifying">
                    验证
                  </a-button>
                </template>
              </a-input>
            </div>
          </div>
        </a-tab-pane>

        <!-- 身份证验证 -->
        <a-tab-pane key="idcard" tab="身份证验证">
          <div class="verification-content">
            <a-form layout="vertical">
              <a-form-item label="身份证号" required>
                <a-input
                  v-model:value="idCardForm.idCardNumber"
                  placeholder="请输入身份证号"
                  size="large"
                />
              </a-form-item>
              <a-form-item label="手机号" required>
                <a-input
                  v-model:value="idCardForm.phoneNumber"
                  placeholder="请输入手机号"
                  size="large"
                />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" size="large" @click="verifyIdCard" :loading="verifying">
                  验证
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>
      </a-tabs>

      <!-- 验证结果 -->
      <a-modal
        v-model:visible="resultModalVisible"
        :title="verifyResult.success ? '验证成功' : '验证失败'"
        :footer="null"
        width="500px"
      >
        <a-result
          :status="verifyResult.success ? 'success' : 'error'"
          :title="verifyResult.message"
        >
          <template #extra v-if="verifyResult.success && verifyResult.data">
            <div class="visitor-info">
              <p><strong>访客姓名：</strong>{{ verifyResult.data.visitorName }}</p>
              <p><strong>手机号：</strong>{{ verifyResult.data.phone }}</p>
              <p><strong>签到状态：</strong>{{ verifyResult.data.isCheckedIn ? '已签到' : '未签到' }}</p>
            </div>
            <a-space>
              <a-button type="primary" @click="closeResultModal">确定</a-button>
              <a-button @click="viewAppointmentDetail">查看详情</a-button>
            </a-space>
          </template>
        </a-result>
      </a-modal>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { ScanOutlined } from '@ant-design/icons-vue';
import { visitorApi } from '/@/api/business/visitor/visitor-api';

// 当前标签页
const activeTab = ref('qrcode');
const verifying = ref(false);

// 二维码表单
const qrCodeValue = ref('');

// 身份证表单
const idCardForm = reactive({
  idCardNumber: '',
  phoneNumber: ''
});

// 验证结果
const resultModalVisible = ref(false);
const verifyResult = reactive({
  success: false,
  message: '',
  data: null
});

// 验证二维码
const verifyQRCode = async () => {
  if (!qrCodeValue.value) {
    message.warning('请输入二维码内容');
    return;
  }

  verifying.value = true;
  try {
    const result = await visitorApi.checkInByQRCode(qrCodeValue.value);
    if (result.code === 200) {
      verifyResult.success = true;
      verifyResult.message = '验证成功，访客已签到';
      verifyResult.data = result.data;
      resultModalVisible.value = true;
      qrCodeValue.value = '';
    } else {
      verifyResult.success = false;
      verifyResult.message = result.message || '验证失败';
      verifyResult.data = null;
      resultModalVisible.value = true;
    }
  } catch (error) {
    message.error('验证失败');
    console.error('验证失败:', error);
  } finally {
    verifying.value = false;
  }
};

// 验证身份证
const verifyIdCard = async () => {
  if (!idCardForm.idCardNumber || !idCardForm.phoneNumber) {
    message.warning('请输入身份证号和手机号');
    return;
  }

  verifying.value = true;
  try {
    const result = await visitorApi.validateVisitorInfo(
      idCardForm.idCardNumber,
      idCardForm.phoneNumber
    );
    if (result.code === 200 && result.data) {
      if (result.data.isValid) {
        verifyResult.success = true;
        verifyResult.message = '验证成功';
        verifyResult.data = result.data;
        resultModalVisible.value = true;
        // 重置表单
        idCardForm.idCardNumber = '';
        idCardForm.phoneNumber = '';
      } else {
        verifyResult.success = false;
        verifyResult.message = result.data.message || '访客信息不存在';
        verifyResult.data = null;
        resultModalVisible.value = true;
      }
    } else {
      message.error(result.message || '验证失败');
    }
  } catch (error) {
    message.error('验证失败');
    console.error('验证失败:', error);
  } finally {
    verifying.value = false;
  }
};

// 关闭结果弹窗
const closeResultModal = () => {
  resultModalVisible.value = false;
};

// 查看预约详情
const viewAppointmentDetail = () => {
  // 跳转到详情页面
  resultModalVisible.value = false;
};
</script>

<style lang="scss" scoped>
.visitor-verification-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.verification-content {
  padding: 48px 24px;
  text-align: center;
}

.scan-area {
  max-width: 500px;
  margin: 0 auto;
}

.scan-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  margin-bottom: 24px;
  background: #fafafa;

  p {
    margin-top: 16px;
    color: #666;
    font-size: 16px;
  }
}

.qr-input {
  margin-top: 24px;
}

.visitor-info {
  text-align: left;
  margin: 24px 0;

  p {
    margin: 8px 0;
    font-size: 14px;
    color: #666;

    strong {
      color: #333;
    }
  }
}
</style>

