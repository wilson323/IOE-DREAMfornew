<!--
 * 设备配置弹窗组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="modalVisible"
    :title="modalTitle"
    :width="800"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
    >
      <!-- 基础信息 -->
      <a-divider>基础信息</a-divider>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备名称" name="deviceName">
            <a-input
              v-model:value="formData.deviceName"
              placeholder="请输入设备名称"
              :disabled="mode === 'config'"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="设备编码" name="deviceCode">
            <a-input
              v-model:value="formData.deviceCode"
              placeholder="请输入设备编码"
              :disabled="mode === 'config' || mode === 'edit'"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备类型" name="deviceType">
            <a-select
              v-model:value="formData.deviceType"
              placeholder="请选择设备类型"
              :disabled="mode === 'config'"
            >
              <a-select-option value="FINGERPRINT">指纹考勤机</a-select-option>
              <a-select-option value="FACE">人脸识别考勤机</a-select-option>
              <a-select-option value="CARD">IC卡考勤机</a-select-option>
              <a-select-option value="PASSWORD">密码考勤机</a-select-option>
              <a-select-option value="MOBILE">移动端打卡</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="厂商" name="manufacturer">
            <a-input
              v-model:value="formData.manufacturer"
              placeholder="请输入设备厂商"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="型号" name="model">
            <a-input
              v-model:value="formData.model"
              placeholder="请输入设备型号"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="所属区域" name="areaId">
            <a-tree-select
              v-model:value="formData.areaId"
              :tree-data="areaTreeData"
              placeholder="请选择所属区域"
              allow-clear
              :field-names="{ label: 'title', value: 'key', children: 'children' }"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="IP地址" name="ipAddress">
            <a-input
              v-model:value="formData.ipAddress"
              placeholder="请输入IP地址"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="端口号" name="port">
            <a-input-number
              v-model:value="formData.port"
              placeholder="请输入端口号"
              :min="1"
              :max="65535"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="安装位置" name="location">
        <a-input
          v-model:value="formData.location"
          placeholder="请输入设备安装位置"
        />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入设备描述"
          :rows="2"
          allow-clear
        />
      </a-form-item>

      <!-- 考勤配置（仅在配置模式下显示） -->
      <template v-if="mode === 'config'">
        <a-divider>考勤配置</a-divider>

        <a-form-item label="支持的打卡方式" name="punchModes">
          <a-checkbox-group v-model:value="formData.punchModes">
            <a-checkbox value="FINGERPRINT">指纹打卡</a-checkbox>
            <a-checkbox value="FACE">人脸识别</a-checkbox>
            <a-checkbox value="CARD">IC卡打卡</a-checkbox>
            <a-checkbox value="PASSWORD">密码打卡</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="GPS验证" name="gpsEnabled">
              <a-switch
                v-model:checked="formData.gpsEnabled"
                checked-children="启用"
                un-checked-children="禁用"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="拍照验证" name="photoEnabled">
              <a-switch
                v-model:checked="formData.photoEnabled"
                checked-children="启用"
                un-checked-children="禁用"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="活体检测" name="livenessEnabled">
              <a-switch
                v-model:checked="formData.livenessEnabled"
                checked-children="启用"
                un-checked-children="禁用"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="人脸识别设置" name="faceSettings" v-if="formData.punchModes?.includes('FACE')">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="识别阈值">
                <a-input-number
                  v-model:value="formData.faceSettings.recognitionThreshold"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  placeholder="0-1之间"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="活体阈值">
                <a-input-number
                  v-model:value="formData.faceSettings.livenessThreshold"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  placeholder="0-1之间"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="匹配超时(秒)">
                <a-input-number
                  v-model:value="formData.faceSettings.matchTimeout"
                  :min="1"
                  :max="30"
                  placeholder="1-30秒"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form-item>

        <a-form-item label="工作时间配置" name="workTimeConfig">
          <div class="work-time-config">
            <div v-for="(shift, index) in formData.workTimeConfig" :key="index" class="shift-item">
              <a-card size="small" :title="`班次 ${index + 1}`">
                <template #extra>
                  <a-button type="text" danger size="small" @click="removeShift(index)" v-if="formData.workTimeConfig.length > 1">
                    删除
                  </a-button>
                </template>
                <a-row :gutter="8">
                  <a-col :span="6">
                    <a-time-picker
                      v-model:value="shift.startTime"
                      format="HH:mm"
                      placeholder="开始时间"
                      style="width: 100%"
                      @change="(value) => updateShiftTime(index, 'startTime', value)"
                    />
                  </a-col>
                  <a-col :span="6">
                    <a-time-picker
                      v-model:value="shift.endTime"
                      format="HH:mm"
                      placeholder="结束时间"
                      style="width: 100%"
                      @change="(value) => updateShiftTime(index, 'endTime', value)"
                    />
                  </a-col>
                  <a-col :span="6">
                    <a-checkbox
                      :checked="shift.allowLate"
                      @change="(e) => updateShiftConfig(index, 'allowLate', e.target.checked)"
                    >
                      允许迟到
                    </a-checkbox>
                  </a-col>
                  <a-col :span="6">
                    <a-checkbox
                      :checked="shift.allowEarlyLeave"
                      @change="(e) => updateShiftConfig(index, 'allowEarlyLeave', e.target.checked)"
                    >
                      允许早退
                    </a-checkbox>
                  </a-col>
                </a-row>
                <div class="mt-2">
                  <span class="text-sm text-gray-500">迟到宽限时间：</span>
                  <a-input-number
                    v-model:value="shift.lateGracePeriod"
                    :min="0"
                    :max="60"
                    size="small"
                    style="width: 80px; margin-left: 8px"
                  />
                  <span class="text-sm text-gray-500 ml-2">分钟</span>
                </div>
              </a-card>
            </div>
            <a-button type="dashed" block @click="addShift" class="mt-2">
              <template #icon><PlusOutlined /></template>
              添加班次
            </a-button>
          </div>
        </a-form-item>

        <a-form-item label="安全设置" name="securitySettings">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="连续失败次数限制">
                <a-input-number
                  v-model:value="formData.securitySettings.maxFailedAttempts"
                  :min="1"
                  :max="10"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="锁定时间(分钟)">
                <a-input-number
                  v-model:value="formData.securitySettings.lockoutDuration"
                  :min="1"
                  :max="1440"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="设备管理员密码">
                <a-input-password
                  v-model:value="formData.securitySettings.adminPassword"
                  placeholder="输入新密码或留空保持不变"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form-item>

        <a-form-item label="高级配置" name="advancedSettings">
          <a-checkbox-group v-model:value="formData.advancedSettings">
            <a-checkbox value="autoSync">自动同步时间</a-checkbox>
            <a-checkbox value="voicePrompt">语音提示</a-checkbox>
            <a-checkbox value="screenSaver">屏幕保护</a-checkbox>
            <a-checkbox value="remoteControl">远程控制</a-checkbox>
            <a-checkbox value="dataBackup">数据备份</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
      </template>

      <!-- 状态设置 -->
      <a-divider v-if="mode !== 'config'">状态设置</a-divider>

      <a-form-item v-if="mode !== 'config'" label="设备状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">正常</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';

  // Props定义
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false
    },
    mode: {
      type: String,
      default: 'add', // add | edit | config
    },
    deviceData: {
      type: Object,
      default: null
    }
  });

  // Emits定义
  const emit = defineEmits(['update:visible', 'success']);

  // 响应式数据
  const modalVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  });

  const modalTitle = computed(() => {
    const titles = {
      add: '新增设备',
      edit: '编辑设备',
      config: '设备配置'
    };
    return titles[props.mode] || '设备配置';
  });

  // 表单数据
  const formData = reactive({
    deviceId: null,
    deviceName: '',
    deviceCode: '',
    deviceType: undefined,
    manufacturer: '',
    model: '',
    ipAddress: '',
    port: 80,
    location: '',
    areaId: null,
    description: '',
    status: 1,
    // 考勤配置
    punchModes: [],
    gpsEnabled: false,
    photoEnabled: false,
    livenessEnabled: false,
    faceSettings: {
      recognitionThreshold: 0.8,
      livenessThreshold: 0.7,
      matchTimeout: 5
    },
    workTimeConfig: [
      {
        startTime: null,
        endTime: null,
        allowLate: true,
        allowEarlyLeave: true,
        lateGracePeriod: 5
      }
    ],
    securitySettings: {
      maxFailedAttempts: 3,
      lockoutDuration: 5,
      adminPassword: ''
    },
    advancedSettings: []
  });

  const areaTreeData = ref([]);
  const formRef = ref();
  const confirmLoading = ref(false);

  // 表单验证规则
  const formRules = {
    deviceName: [
      { required: props.mode !== 'config', message: '请输入设备名称', trigger: 'blur' }
    ],
    deviceCode: [
      { required: props.mode !== 'config', message: '请输入设备编码', trigger: 'blur' },
      { pattern: /^[A-Z0-9_-]+$/, message: '设备编码只能包含大写字母、数字、下划线和横线', trigger: 'blur' }
    ],
    deviceType: [
      { required: props.mode !== 'config', message: '请选择设备类型', trigger: 'change' }
    ],
    ipAddress: [
      { required: props.mode !== 'config', message: '请输入IP地址', trigger: 'blur' },
      { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: '请输入正确的IP地址格式', trigger: 'blur' }
    ],
    port: [
      { required: props.mode !== 'config', message: '请输入端口号', trigger: 'blur' },
      { type: 'number', min: 1, max: 65535, message: '端口号应在1-65535之间', trigger: 'blur' }
    ]
  };

  // 监听设备数据变化
  watch(
    () => props.deviceData,
    (newData) => {
      if (newData && (props.mode === 'edit' || props.mode === 'config')) {
        Object.assign(formData, newData);
        // 处理配置模式的特殊逻辑
        if (props.mode === 'config' && !newData.workTimeConfig) {
          formData.workTimeConfig = [
            {
              startTime: null,
              endTime: null,
              allowLate: true,
              allowEarlyLeave: true,
              lateGracePeriod: 5
            }
          ];
        }
      }
    },
    { immediate: true }
  );

  // 监听弹窗显示状态
  watch(
    modalVisible,
    (newVisible) => {
      if (newVisible) {
        if (props.mode === 'add') {
          resetForm();
        }
        loadAreaTreeOptions();
      }
    }
  );

  // ============ 方法 ============

  // 重置表单
  const resetForm = () => {
    Object.assign(formData, {
      deviceId: null,
      deviceName: '',
      deviceCode: '',
      deviceType: undefined,
      manufacturer: '',
      model: '',
      ipAddress: '',
      port: 80,
      location: '',
      areaId: null,
      description: '',
      status: 1,
      punchModes: [],
      gpsEnabled: false,
      photoEnabled: false,
      livenessEnabled: false,
      faceSettings: {
        recognitionThreshold: 0.8,
        livenessThreshold: 0.7,
        matchTimeout: 5
      },
      workTimeConfig: [
        {
          startTime: null,
          endTime: null,
          allowLate: true,
          allowEarlyLeave: true,
          lateGracePeriod: 5
        }
      ],
      securitySettings: {
        maxFailedAttempts: 3,
        lockoutDuration: 5,
        adminPassword: ''
      },
      advancedSettings: []
    });
    nextTick(() => {
      formRef.value?.clearValidate();
    });
  };

  // 加载区域树选项
  const loadAreaTreeOptions = async () => {
    try {
      // 模拟数据，实际应该调用API
      areaTreeData.value = [
        {
          key: 1,
          title: '总部大楼',
          value: 1
        },
        {
          key: 2,
          title: '分部大楼',
          value: 2,
          children: [
            {
              key: 3,
              title: '技术部',
              value: 3
            },
            {
              key: 4,
              title: '市场部',
              value: 4
            }
          ]
        }
      ];
    } catch (error) {
      console.error('加载区域树选项失败:', error);
    }
  };

  // 添加班次
  const addShift = () => {
    formData.workTimeConfig.push({
      startTime: null,
      endTime: null,
      allowLate: true,
      allowEarlyLeave: true,
      lateGracePeriod: 5
    });
  };

  // 删除班次
  const removeShift = (index) => {
    formData.workTimeConfig.splice(index, 1);
  };

  // 更新班次时间
  const updateShiftTime = (index, field, value) => {
    formData.workTimeConfig[index][field] = value;
  };

  // 更新班次配置
  const updateShiftConfig = (index, field, value) => {
    formData.workTimeConfig[index][field] = value;
  };

  // 确认操作
  const handleOk = async () => {
    try {
      await formRef.value.validate();

      confirmLoading.value = true;

      const submitData = { ...formData };

      // 根据模式调用不同的API
      if (props.mode === 'add') {
        // await attendanceDeviceApi.createDevice(submitData);
        message.success('新增设备成功');
      } else if (props.mode === 'edit') {
        // await attendanceDeviceApi.updateDevice(submitData);
        message.success('更新设备成功');
      } else if (props.mode === 'config') {
        // await attendanceDeviceApi.saveDeviceConfig(submitData.deviceId, submitData);
        message.success('设备配置保存成功');
      }

      emit('success');
      handleCancel();
    } catch (error) {
      console.error('保存失败:', error);
      message.error('保存失败');
    } finally {
      confirmLoading.value = false;
    }
  };

  // 取消操作
  const handleCancel = () => {
    modalVisible.value = false;
  };
</script>

<style lang="less" scoped>
.work-time-config {
  .shift-item {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.text-gray-500 {
  color: #8c8c8c;
}

.text-sm {
  font-size: 12px;
}

.mt-2 {
  margin-top: 8px;
}

.ml-2 {
  margin-left: 8px;
}

.mb-2 {
  margin-bottom: 8px;
}
</style>