<!--
  * 消费设备表单弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025/11/17
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    v-model:open="visible"
    :title="modalTitle"
    width="800px"
    :confirm-loading="loading"
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
      <a-form-item label="设备名称" name="deviceName">
        <a-input
          v-model:value="formData.deviceName"
          placeholder="请输入设备名称"
          :maxlength="50"
        />
      </a-form-item>

      <a-form-item label="设备编号" name="deviceCode">
        <a-input
          v-model:value="formData.deviceCode"
          placeholder="请输入设备编号"
          :maxlength="30"
          :disabled="mode === 'edit'"
        />
      </a-form-item>

      <a-form-item label="设备类型" name="deviceType">
        <a-select
          v-model:value="formData.deviceType"
          placeholder="请选择设备类型"
        >
          <a-select-option value="POS">POS机</a-select-option>
          <a-select-option value="SELF_SERVICE">自助消费机</a-select-option>
          <a-select-option value="CANTEEN">食堂消费机</a-select-option>
          <a-select-option value="MOBILE">移动消费终端</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="安装位置" name="location">
        <a-input
          v-model:value="formData.location"
          placeholder="请输入安装位置"
          :maxlength="100"
        />
      </a-form-item>

      <a-form-item label="所属区域" name="areaId">
        <a-tree-select
          v-model:value="formData.areaId"
          placeholder="请选择所属区域"
          :tree-data="areaTreeData"
          :field-names="{ label: 'areaName', value: 'areaId', children: 'children' }"
          allow-clear
          show-search
          tree-default-expand-all
        />
      </a-form-item>

      <a-form-item label="IP地址" name="ipAddress">
        <a-input
          v-model:value="formData.ipAddress"
          placeholder="请输入IP地址"
          :maxlength="15"
        />
      </a-form-item>

      <a-form-item label="端口" name="port">
        <a-input-number
          v-model:value="formData.port"
          placeholder="请输入端口号"
          :min="1"
          :max="65535"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio value="ONLINE">启用</a-radio>
          <a-radio value="OFFLINE">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="设备描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入设备描述"
          :maxlength="200"
          :rows="3"
        />
      </a-form-item>

      <a-divider>设备配置</a-divider>

      <a-form-item label="支持消费模式" name="supportedModes">
        <a-checkbox-group v-model:value="formData.supportedModes">
          <a-checkbox value="FIXED_AMOUNT">固定金额模式</a-checkbox>
          <a-checkbox value="FREE_AMOUNT">自由金额模式</a-checkbox>
          <a-checkbox value="PRODUCT">商品扫码模式</a-checkbox>
          <a-checkbox value="ORDERING">点餐模式</a-checkbox>
          <a-checkbox value="METERING">计量模式</a-checkbox>
          <a-checkbox value="SMART">智能模式</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-form-item label="支付方式" name="paymentMethods">
        <a-checkbox-group v-model:value="formData.paymentMethods">
          <a-checkbox value="BALANCE">余额支付</a-checkbox>
          <a-checkbox value="FACE">人脸支付</a-checkbox>
          <a-checkbox value="FINGERPRINT">指纹支付</a-checkbox>
          <a-checkbox value="QR_CODE">二维码支付</a-checkbox>
          <a-checkbox value="CARD">刷卡支付</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-form-item label="单笔限额" name="singleLimit">
        <a-input-number
          v-model:value="formData.singleLimit"
          placeholder="请输入单笔交易限额"
          :min="0"
          :precision="2"
          style="width: 100%"
          addon-before="¥"
        />
      </a-form-item>

      <a-form-item label="日累计限额" name="dailyLimit">
        <a-input-number
          v-model:value="formData.dailyLimit"
          placeholder="请输入日累计限额"
          :min="0"
          :precision="2"
          style="width: 100%"
          addon-before="¥"
        />
      </a-form-item>

      <a-divider>设备参数</a-divider>

      <a-form-item label="心跳间隔(秒)" name="heartbeatInterval">
        <a-input-number
          v-model:value="formData.heartbeatInterval"
          placeholder="请输入心跳间隔"
          :min="10"
          :max="300"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="超时时间(秒)" name="timeout">
        <a-input-number
          v-model:value="formData.timeout"
          placeholder="请输入超时时间"
          :min="5"
          :max="60"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="启用调试" name="debugEnabled">
        <a-switch v-model:checked="formData.debugEnabled" />
        <span class="form-item-extra">启用后将记录详细日志</span>
      </a-form-item>

      <a-form-item label="自动重启" name="autoRestart">
        <a-switch v-model:checked="formData.autoRestart" />
        <span class="form-item-extra">设备异常时自动重启</span>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { consumeDeviceApi } from '/@/api/business/consume/consume-device-api';
  import { areaApi } from '/@/api/business/access/area-api';

  // ----------------------- Props & Emits -----------------------
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    device: {
      type: Object,
      default: null,
    },
    mode: {
      type: String,
      default: 'add', // add | edit
    },
  });

  const emit = defineEmits(['update:visible', 'success']);

  // ----------------------- 响应式数据 -----------------------
  const formRef = ref();
  const loading = ref(false);
  const areaTreeData = ref([]);

  const formData = reactive({
    deviceName: '',
    deviceCode: '',
    deviceType: undefined,
    location: '',
    areaId: undefined,
    ipAddress: '',
    port: 8080,
    status: 'ONLINE',
    description: '',
    supportedModes: [],
    paymentMethods: [],
    singleLimit: 1000.00,
    dailyLimit: 10000.00,
    heartbeatInterval: 30,
    timeout: 30,
    debugEnabled: false,
    autoRestart: true,
  });

  // ----------------------- 表单验证规则 -----------------------
  const rules = {
    deviceName: [
      { required: true, message: '请输入设备名称', trigger: 'blur' },
      { min: 2, max: 50, message: '设备名称长度为2-50个字符', trigger: 'blur' },
    ],
    deviceCode: [
      { required: true, message: '请输入设备编号', trigger: 'blur' },
      { pattern: /^[A-Z0-9]{6,20}$/, message: '设备编号为6-20位大写字母和数字', trigger: 'blur' },
    ],
    deviceType: [
      { required: true, message: '请选择设备类型', trigger: 'change' },
    ],
    location: [
      { required: true, message: '请输入安装位置', trigger: 'blur' },
      { max: 100, message: '安装位置不能超过100个字符', trigger: 'blur' },
    ],
    ipAddress: [
      { required: true, message: '请输入IP地址', trigger: 'blur' },
      { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: '请输入正确的IP地址格式', trigger: 'blur' },
    ],
    port: [
      { required: true, message: '请输入端口号', trigger: 'blur' },
      { type: 'number', min: 1, max: 65535, message: '端口号范围为1-65535', trigger: 'blur' },
    ],
    status: [
      { required: true, message: '请选择设备状态', trigger: 'change' },
    ],
    supportedModes: [
      { required: true, message: '请至少选择一种消费模式', trigger: 'change' },
    ],
    paymentMethods: [
      { required: true, message: '请至少选择一种支付方式', trigger: 'change' },
    ],
    singleLimit: [
      { required: true, message: '请输入单笔限额', trigger: 'blur' },
      { type: 'number', min: 0, message: '单笔限额不能小于0', trigger: 'blur' },
    ],
    dailyLimit: [
      { required: true, message: '请输入日累计限额', trigger: 'blur' },
      { type: 'number', min: 0, message: '日累计限额不能小于0', trigger: 'blur' },
    ],
    heartbeatInterval: [
      { required: true, message: '请输入心跳间隔', trigger: 'blur' },
      { type: 'number', min: 10, max: 300, message: '心跳间隔范围为10-300秒', trigger: 'blur' },
    ],
    timeout: [
      { required: true, message: '请输入超时时间', trigger: 'blur' },
      { type: 'number', min: 5, max: 60, message: '超时时间范围为5-60秒', trigger: 'blur' },
    ],
  };

  // ----------------------- 计算属性 -----------------------
  const modalTitle = computed(() => {
    return props.mode === 'add' ? '添加消费设备' : '编辑消费设备';
  });

  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  // ----------------------- 监听器 -----------------------
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        if (props.mode === 'edit' && props.device) {
          Object.assign(formData, props.device);
        } else {
          resetForm();
        }
      }
    }
  );

  // ----------------------- 生命周期 -----------------------
  onMounted(() => {
    loadAreaTreeData();
  });

  // ----------------------- 方法 -----------------------
  const loadAreaTreeData = async () => {
    try {
      // TODO: 调用区域API
      // const res = await areaApi.getAreaTree();
      // areaTreeData.value = res.data;

      // 模拟数据
      areaTreeData.value = [
        {
          areaId: 1,
          areaName: '第一食堂',
          children: [
            { areaId: 11, areaName: '一层大厅' },
            { areaId: 12, areaName: '二层餐厅' },
          ],
        },
        {
          areaId: 2,
          areaName: '第二食堂',
          children: [
            { areaId: 21, areaName: '快餐区' },
            { areaId: 22, areaName: '面食区' },
          ],
        },
        {
          areaId: 3,
          areaName: '便利店',
        },
      ];
    } catch (error) {
      console.error('加载区域数据失败:', error);
    }
  };

  const resetForm = () => {
    Object.assign(formData, {
      deviceName: '',
      deviceCode: '',
      deviceType: undefined,
      location: '',
      areaId: undefined,
      ipAddress: '',
      port: 8080,
      status: 'ONLINE',
      description: '',
      supportedModes: [],
      paymentMethods: [],
      singleLimit: 1000.00,
      dailyLimit: 10000.00,
      heartbeatInterval: 30,
      timeout: 30,
      debugEnabled: false,
      autoRestart: true,
    });

    if (formRef.value) {
      formRef.value.clearValidate();
    }
  };

  const handleSubmit = async () => {
    try {
      loading.value = true;

      await formRef.value.validate();

      const submitData = { ...formData };

      if (props.mode === 'add') {
        // TODO: 调用添加API
        // await consumeDeviceApi.addDevice(submitData);
        message.success('添加设备成功');
      } else {
        // TODO: 调用更新API
        // await consumeDeviceApi.updateDevice(submitData);
        message.success('更新设备成功');
      }

      emit('success');

    } catch (error) {
      console.error('保存设备失败:', error);
      if (error.errorFields) {
        // 表单验证错误
        return;
      }
      message.error('保存设备失败');
    } finally {
      loading.value = false;
    }
  };

  const handleCancel = () => {
    resetForm();
    emit('update:visible', false);
  };
</script>

<style lang="less" scoped>
  .form-item-extra {
    margin-left: 8px;
    font-size: 12px;
    color: #8c8c8c;
  }
</style>