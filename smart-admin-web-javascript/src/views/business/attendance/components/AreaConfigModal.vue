<!--
 * 区域配置弹窗组件
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
      <a-form-item label="区域名称" name="areaName">
        <a-input
          v-model:value="formData.areaName"
          placeholder="请输入区域名称"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="区域编码" name="areaCode">
        <a-input
          v-model:value="formData.areaCode"
          placeholder="请输入区域编码"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="区域类型" name="areaType">
        <a-select v-model:value="formData.areaType" placeholder="请选择区域类型">
          <a-select-option value="OFFICE">办公区域</a-select-option>
          <a-select-option value="FACTORY">工厂区域</a-select-option>
          <a-select-option value="WAREHOUSE">仓库区域</a-select-option>
          <a-select-option value="OUTDOOR">户外区域</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="上级区域" name="parentId">
        <a-tree-select
          v-model:value="formData.parentId"
          :tree-data="areaTreeData"
          placeholder="请选择上级区域"
          allow-clear
          :field-names="fieldNames"
          tree-default-expand-all
        />
      </a-form-item>

      <a-form-item label="排序号" name="sortOrder">
        <a-input-number
          v-model:value="formData.sortOrder"
          placeholder="请输入排序号"
          :min="0"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-switch
          v-model:checked="formData.status"
          :checkedValue="1"
          :unCheckedValue="0"
        />
        <span class="ml-2">{{ formData.status === 1 ? '启用' : '禁用' }}</span>
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入区域描述"
          :rows="3"
          allow-clear
        />
      </a-form-item>

      <!-- 考勤配置 -->
      <a-divider>考勤配置</a-divider>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="启用GPS验证" name="gpsRequired">
            <a-switch
              v-model:checked="formData.gpsRequired"
              @change="handleGpsChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="启用照片验证" name="photoRequired">
            <a-switch
              v-model:checked="formData.photoRequired"
              @change="handlePhotoChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item
        v-if="formData.gpsRequired"
        label="GPS验证范围（米）"
        name="gpsRange"
      >
        <a-input-number
          v-model:value="formData.gpsRange"
          :min="10"
          :max="1000"
          placeholder="请输入GPS验证范围"
          style="width: 200px"
        />
        <span class="ml-2 text-gray-500">员工必须在此范围内才能打卡</span>
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="工作日打卡次数" name="workdayPunchRequired">
            <a-input-number
              v-model:value="formData.workdayPunchRequired"
              :min="1"
              :max="4"
              placeholder="请输入工作日打卡次数"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="休息日打卡次数" name="weekendPunchRequired">
            <a-input-number
              v-model:value="formData.weekendPunchRequired"
              :min="0"
              :max="4"
              placeholder="请输入休息日打卡次数"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 地理围栏配置 -->
      <a-divider>地理围栏配置</a-divider>

      <a-form-item label="地理围栏" name="geofenceEnabled">
        <a-switch
          v-model:checked="formData.geofenceEnabled"
          @change="handleGeofenceChange"
        />
        <span class="ml-2">启用后员工必须在围栏范围内打卡</span>
      </a-form-item>

      <a-form-item v-if="formData.geofenceEnabled" label="围栏顶点" name="geofencePoints">
        <div class="geofence-container">
          <div
            v-for="(point, index) in formData.geofencePoints"
            :key="index"
            class="geofence-point"
          >
            <a-space>
              <a-input-number
                :value="point.longitude"
                placeholder="经度"
                :precision="6"
                style="width: 150px"
                @change="(value) => updatePoint(index, 'longitude', value)"
              />
              <a-input-number
                :value="point.latitude"
                placeholder="纬度"
                :precision="6"
                style="width: 150px"
                @change="(value) => updatePoint(index, 'latitude', value)"
              />
              <a-input
                v-model:value="point.name"
                placeholder="地点名称"
                style="width: 120px"
                @change="(value) => updatePoint(index, 'name', value)"
              />
              <a-button
                type="text"
                danger
                size="small"
                @click="removePoint(index)"
              >
                删除
              </a-button>
            </a-space>
          </div>
          <a-button type="dashed" block @click="addPoint">
            <template #icon><PlusOutlined /></template>
            添加围栏顶点
          </a-button>
        </div>
      </a-form-item>

      <!-- 设备限制配置 -->
      <a-divider>设备限制配置</a-divider>

      <a-form-item label="设备限制" name="deviceLimitEnabled">
        <a-switch
          v-model:checked="formData.deviceLimitEnabled"
          @change="handleDeviceLimitChange"
        />
        <span class="ml-2">启用后只能在指定设备上打卡</span>
      </a-form-item>

      <a-form-item v-if="formData.deviceLimitEnabled" label="允许设备" name="allowedDevices">
        <a-select
          v-model:value="formData.allowedDevices"
          mode="multiple"
          placeholder="请选择允许的设备"
          style="width: 100%"
          :options="deviceOptions"
          :filter-option="filterDeviceOption"
        />
        <div class="mt-2 text-sm text-gray-500">
          可以选择多个设备，员工只能在选定的设备上打卡
        </div>
      </a-form-item>

      <!-- 时间限制配置 -->
      <a-divider>时间限制配置</a-divider>

      <a-form-item label="时间限制" name="timeLimitEnabled">
        <a-switch
          v-model:checked="formData.timeLimitEnabled"
          @change="handleTimeLimitChange"
        />
        <span class="ml-2">启用后只能在指定时间段内打卡</span>
      </a-form-item>

      <template v-if="formData.timeLimitEnabled">
        <a-form-item label="有效时间段" name="effectiveTimeRanges">
          <div class="time-ranges-container">
            <div
              v-for="(range, index) in formData.effectiveTimeRanges"
              :key="index"
              class="time-range"
            >
              <a-space>
                <a-time-picker
                  v-model:value="range.startTime"
                  format="HH:mm"
                  placeholder="开始时间"
                  @change="(value) => updateTimeRange(index, 'startTime', value)"
                />
                <span>至</span>
                <a-time-picker
                  v-model:value="range.endTime"
                  format="HH:mm"
                  placeholder="结束时间"
                  @change="(value) => updateTimeRange(index, 'endTime', value)"
                />
                <a-select
                  v-model:value="range.weekdays"
                  mode="multiple"
                  placeholder="选择星期"
                  style="width: 200px"
                  @change="(value) => updateTimeRange(index, 'weekdays', value)"
                >
                  <a-select-option value="1">周一</a-select-option>
                  <a-select-option value="2">周二</a-select-option>
                  <a-select-option value="3">周三</a-select-option>
                  <a-select-option value="4">周四</a-select-option>
                  <a-select-option value="5">周五</a-select-option>
                  <a-select-option value="6">周六</a-select-option>
                  <a-select-option value="0">周日</a-select-option>
                </a-select>
                <a-button
                  type="text"
                  danger
                  size="small"
                  @click="removeTimeRange(index)"
                >
                  删除
                </a-button>
              </a-space>
            </div>
            <a-button type="dashed" block @click="addTimeRange">
              <template #icon><PlusOutlined /></template>
              添加时间段
            </a-button>
          </div>
        </a-form-item>
      </template>

      <!-- 扩展配置 -->
      <a-divider>扩展配置</a-divider>

      <a-form-item label="扩展配置" name="extensionConfig">
        <a-textarea
          v-model:value="extensionConfigText"
          placeholder="请输入扩展配置（JSON格式）"
          :rows="4"
          @blur="handleExtensionConfigChange"
        />
        <div class="mt-2 text-sm text-gray-500">
          支持自定义配置，JSON格式：{"key": "value"}
        </div>
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
      default: 'add', // add | edit
    },
    areaData: {
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
    return props.mode === 'add' ? '新增区域配置' : '编辑区域配置';
  });

  // 表单数据
  const formData = reactive({
    areaId: null,
    areaName: '',
    areaCode: '',
    areaType: undefined,
    parentId: null,
    sortOrder: 0,
    status: 1,
    description: '',
    gpsRequired: false,
    photoRequired: false,
    gpsRange: 100,
    workdayPunchRequired: 2,
    weekendPunchRequired: 0,
    geofenceEnabled: false,
    geofencePoints: [],
    deviceLimitEnabled: false,
    allowedDevices: [],
    timeLimitEnabled: false,
    effectiveTimeRanges: [],
    extensionConfig: {}
  });

  const extensionConfigText = ref('');
  const areaTreeData = ref([]);
  const deviceOptions = ref([]);

  // 表单引用
  const formRef = ref();

  // 表单验证规则
  const formRules = {
    areaName: [
      { required: true, message: '请输入区域名称', trigger: 'blur' }
    ],
    areaCode: [
      { required: true, message: '请输入区域编码', trigger: 'blur' },
      { pattern: /^[A-Z0-9_-]+$/, message: '区域编码只能包含大写字母、数字、下划线和横线', trigger: 'blur' }
    ],
    areaType: [
      { required: true, message: '请选择区域类型', trigger: 'change' }
    ],
    gpsRange: [
      { type: 'number', min: 10, max: 1000, message: 'GPS范围应在10-1000米之间', trigger: 'blur' }
    ],
    workdayPunchRequired: [
      { type: 'number', min: 1, max: 4, message: '工作日打卡次数应在1-4次之间', trigger: 'blur' }
    ],
    weekendPunchRequired: [
      { type: 'number', min: 0, max: 4, message: '休息日打卡次数应在0-4次之间', trigger: 'blur' }
    ]
  };

  // 监听区域数据变化
  watch(
    () => props.areaData,
    (newData) => {
      if (newData && props.mode === 'edit') {
        Object.assign(formData, newData);
        // 转换扩展配置为文本
        if (newData.extensionConfig) {
          extensionConfigText.value = JSON.stringify(newData.extensionConfig, null, 2);
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
        loadDeviceOptions();
      }
    }
  );

  // ============ 方法 ============

  // 重置表单
  const resetForm = () => {
    Object.assign(formData, {
      areaId: null,
      areaName: '',
      areaCode: '',
      areaType: undefined,
      parentId: null,
      sortOrder: 0,
      status: 1,
      description: '',
      gpsRequired: false,
      photoRequired: false,
      gpsRange: 100,
      workdayPunchRequired: 2,
      weekendPunchRequired: 0,
      geofenceEnabled: false,
      geofencePoints: [],
      deviceLimitEnabled: false,
      allowedDevices: [],
      timeLimitEnabled: false,
      effectiveTimeRanges: [],
      extensionConfig: {}
    });
    extensionConfigText.value = '';
    nextTick(() => {
      formRef.value?.clearValidate();
    });
  };

  // 加载区域树选项
  const loadAreaTreeOptions = async () => {
    try {
      // 这里应该调用API获取区域树数据
      // const result = await attendanceAreaApi.getAreaTreeSelect();
      // areaTreeData.value = result.data || [];

      // 模拟数据
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

  // 加载设备选项
  const loadDeviceOptions = async () => {
    try {
      // 这里应该调用API获取设备列表
      // const result = await deviceApi.getDeviceList();
      // deviceOptions.value = result.data || [];

      // 模拟数据
      deviceOptions.value = [
        { label: '考勤机001', value: 'DEVICE_001' },
        { label: '考勤机002', value: 'DEVICE_002' },
        { label: '考勤机003', value: 'DEVICE_003' },
        { label: '人脸识别设备001', value: 'FACE_DEVICE_001' }
      ];
    } catch (error) {
      console.error('加载设备选项失败:', error);
    }
  };

  // 设备过滤选项
  const filterDeviceOption = (input, option) => {
    return option.label.toLowerCase().includes(input.toLowerCase());
  };

  // GPS验证变化处理
  const handleGpsChange = (checked) => {
    if (!checked) {
      formData.gpsRange = 100;
    }
  };

  // 照片验证变化处理
  const handlePhotoChange = (checked) => {
    // 可以在这里添加照片验证相关的逻辑
  };

  // 地理围栏变化处理
  const handleGeofenceChange = (checked) => {
    if (!checked) {
      formData.geofencePoints = [];
    }
  };

  // 设备限制变化处理
  const handleDeviceLimitChange = (checked) => {
    if (!checked) {
      formData.allowedDevices = [];
    }
  };

  // 时间限制变化处理
  const handleTimeLimitChange = (checked) => {
    if (!checked) {
      formData.effectiveTimeRanges = [];
    }
  };

  // 添加围栏顶点
  const addPoint = () => {
    formData.geofencePoints.push({
      longitude: null,
      latitude: null,
      name: ''
    });
  };

  // 更新围栏顶点
  const updatePoint = (index, field, value) => {
    formData.geofencePoints[index][field] = value;
  };

  // 删除围栏顶点
  const removePoint = (index) => {
    formData.geofencePoints.splice(index, 1);
  };

  // 添加时间段
  const addTimeRange = () => {
    formData.effectiveTimeRanges.push({
      startTime: null,
      endTime: null,
      weekdays: []
    });
  };

  // 更新时间段
  const updateTimeRange = (index, field, value) => {
    formData.effectiveTimeRanges[index][field] = value;
  };

  // 删除时间段
  const removeTimeRange = (index) => {
    formData.effectiveTimeRanges.splice(index, 1);
  };

  // 扩展配置变化处理
  const handleExtensionConfigChange = () => {
    try {
      if (extensionConfigText.value.trim()) {
        formData.extensionConfig = JSON.parse(extensionConfigText.value);
      } else {
        formData.extensionConfig = {};
      }
    } catch (error) {
      console.error('扩展配置格式错误:', error);
      message.error('扩展配置格式不正确，请检查JSON格式');
    }
  };

  // 确认操作
  const handleOk = async () => {
    try {
      await formRef.value.validate();

      const submitData = { ...formData };

      if (props.mode === 'add') {
        // 调用新增API
        // await attendanceAreaApi.createArea(submitData);
        message.success('新增区域配置成功');
      } else {
        // 调用更新API
        // await attendanceAreaApi.updateArea(submitData);
        message.success('更新区域配置成功');
      }

      emit('success');
      handleCancel();
    } catch (error) {
      console.error('保存失败:', error);
      message.error('保存失败');
    }
  };

  // 取消操作
  const handleCancel = () => {
    modalVisible.value = false;
  };
</script>

<style lang="less" scoped>
.geofence-container,
.time-ranges-container {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  padding: 16px;

  .geofence-point,
  .time-range {
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.ant-form-item {
  margin-bottom: 16px;
}

.text-gray-500 {
  color: #8c8c8c;
}
</style>