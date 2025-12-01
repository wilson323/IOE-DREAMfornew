<!--
  权限按钮组件
  基于RAC权限模型的智能权限按钮，支持权限检查、禁用状态和权限提示
-->

<template>
  <a-tooltip
    :title="tooltipTitle"
    :placement="tooltipPlacement"
    :mouseLeaveDelay="0.1"
  >
    <a-button
      v-bind="buttonProps"
      :loading="loading"
      :disabled="disabled || !hasPermission"
      :class="buttonClass"
      @click="handleClick"
    >
      <template #icon v-if="hasPermission && $slots.icon">
        <slot name="icon" />
      </template>

      <slot v-if="hasPermission">
        <!-- 有权限时显示默认内容 -->
      </slot>

      <template v-else>
        <!-- 无权限时显示替代内容 -->
        <slot name="noPermission">
          <LockOutlined v-if="showNoPermissionIcon" />
          <span v-if="showNoPermissionText">{{ noPermissionText }}</span>
        </slot>
      </template>
    </a-button>
  </a-tooltip>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { LockOutlined } from '@ant-design/icons-vue'
import { permissionManager } from '@/utils/permission'

/**
 * 组件属性
 */
const props = defineProps({
  // 权限配置
  permission: {
    type: [String, Array, Object],
    default: null
  },

  // 资源编码
  resource: {
    type: String,
    default: ''
  },

  // 动作类型
  action: {
    type: String,
    default: 'READ'
  },

  // 数据域
  dataScope: {
    type: String,
    default: ''
  },

  // 角色要求
  roles: {
    type: [String, Array],
    default: null
  },

  // 是否需要超级管理员权限
  requireSuperAdmin: {
    type: Boolean,
    default: false
  },

  // 按钮属性
  type: {
    type: String,
    default: 'default'
  },

  size: {
    type: String,
    default: 'default'
  },

  shape: {
    type: String,
    default: 'default'
  },

  danger: {
    type: Boolean,
    default: false
  },

  ghost: {
    type: Boolean,
    default: false
  },

  block: {
    type: Boolean,
    default: false
  },

  disabled: {
    type: Boolean,
    default: false
  },

  // 权限相关配置
  hideOnNoPermission: {
    type: Boolean,
    default: false
  },

  disabledOnNoPermission: {
    type: Boolean,
    default: true
  },

  showNoPermissionIcon: {
    type: Boolean,
    default: false
  },

  showNoPermissionText: {
    type: Boolean,
    default: true
  },

  noPermissionText: {
    type: String,
    default: '无权限'
  },

  tooltipPlacement: {
    type: String,
    default: 'top'
  },

  // 自定义权限检查
  customPermissionCheck: {
    type: Function,
    default: null
  },

  // 权限检查模式
  permissionCheckMode: {
    type: String,
    default: 'strict', // strict | loose | custom
    validator: (value) => ['strict', 'loose', 'custom'].includes(value)
  }
})

/**
 * 事件定义
 */
const emit = defineEmits(['click', 'permissionDenied', 'permissionGranted'])

/**
 * 权限状态
 */
const hasPermission = ref(false)
const loading = ref(false)

/**
 * 计算按钮属性
 */
const buttonProps = computed(() => {
  const props = {
    type: props.type,
    size: props.size,
    shape: props.shape,
    danger: props.danger,
    ghost: props.ghost,
    block: props.block,
    disabled: props.disabled
  }

  // 根据权限状态调整按钮属性
  if (!hasPermission.value) {
    if (props.hideOnNoPermission) {
      props.style = { display: 'none' }
    } else if (props.disabledOnNoPermission) {
      props.disabled = true
    }
  }

  return props
})

/**
 * 计算按钮样式类
 */
const buttonClass = computed(() => {
  return {
    'permission-button': true,
    'permission-button--granted': hasPermission.value,
    'permission-button--denied': !hasPermission.value,
    'permission-button--loading': loading.value
  }
})

/**
 * 计算提示文本
 */
const tooltipTitle = computed(() => {
  if (!hasPermission.value && props.noPermissionText) {
    return props.noPermissionText
  }
  return ''
})

/**
 * 权限配置
 */
const permissionConfig = computed(() => {
  if (props.permission) {
    return props.permission
  }

  return {
    resource: props.resource,
    action: props.action,
    dataScope: props.dataScope,
    roles: props.roles,
    requireSuperAdmin: props.requireSuperAdmin
  }
})

/**
 * 检查权限
 */
const checkPermission = async () => {
  try {
    loading.value = true

    // 自定义权限检查
    if (props.permissionCheckMode === 'custom' && props.customPermissionCheck) {
      hasPermission.value = await props.customPermissionCheck(permissionConfig.value)
    } else if (props.permissionCheckMode === 'loose') {
      // 宽松模式，没有权限配置时默认允许
      hasPermission.value = !permissionConfig.value || await doPermissionCheck(permissionConfig.value)
    } else {
      // 严格模式，没有权限配置时默认拒绝
      hasPermission.value = permissionConfig.value ? await doPermissionCheck(permissionConfig.value) : false
    }

    // 触发权限状态事件
    if (hasPermission.value) {
      emit('permissionGranted')
    } else {
      emit('permissionDenied')
    }

  } catch (error) {
    console.error('权限按钮检查失败:', error)
    hasPermission.value = false
    message.error('权限检查失败')
  } finally {
    loading.value = false
  }
}

/**
 * 执行权限检查
 */
const doPermissionCheck = async (config) => {
  if (!config) {
    return false
  }

  let result = false

  // 根据权限配置类型进行检查
  if (typeof config === 'string') {
    result = await checkStringPermission(config)
  } else if (Array.isArray(config)) {
    result = await checkArrayPermission(config)
  } else if (typeof config === 'object') {
    result = await checkObjectPermission(config)
  }

  return result
}

/**
 * 检查字符串格式权限
 */
const checkStringPermission = async (permission) => {
  const parts = permission.split(':')

  if (parts.length === 1) {
    return await permissionManager.hasPermission(parts[0].trim(), 'READ')
  } else if (parts.length === 2) {
    return await permissionManager.hasPermission(parts[0].trim(), parts[1].trim())
  }

  return false
}

/**
 * 检查数组格式权限
 */
const checkArrayPermission = async (permissions) => {
  for (const permission of permissions) {
    if (typeof permission === 'string') {
      if (await checkStringPermission(permission)) {
        return true
      }
    } else if (typeof permission === 'object') {
      if (await checkObjectPermission(permission)) {
        return true
      }
    }
  }
  return false
}

/**
 * 检查对象格式权限
 */
const checkObjectPermission = async (obj) => {
  const {
    resource,
    action = 'READ',
    dataScope,
    roles,
    requireSuperAdmin,
    areaId,
    deptId,
    userId
  } = obj

  // 检查超级管理员权限
  if (requireSuperAdmin && !permissionManager.isSuperAdmin()) {
    return false
  }

  // 检查角色权限
  if (roles) {
    const requiredRoles = Array.isArray(roles) ? roles : [roles]
    const userRoles = permissionManager.getUserRoles()
    const hasRole = requiredRoles.some(role => userRoles.includes(role))

    if (!hasRole) {
      return false
    }
  }

  // 检查基础权限
  if (resource) {
    const hasBasicPermission = await permissionManager.hasPermission(resource, action)
    if (!hasBasicPermission) {
      return false
    }
  }

  // 检查数据域权限
  if (dataScope) {
    const hasDataScopePermission = await checkDataScopePermission(dataScope, {
      areaId,
      deptId,
      userId
    })
    if (!hasDataScopePermission) {
      return false
    }
  }

  return true
}

/**
 * 检查数据域权限
 */
const checkDataScopePermission = async (dataScope, context) => {
  const { areaId, deptId, userId } = context

  switch (dataScope.toUpperCase()) {
    case 'ALL':
      return true

    case 'AREA':
      if (areaId) {
        return await permissionManager.hasAreaPermission(areaId)
      }
      return permissionManager.getAccessibleAreas().length > 0

    case 'DEPT':
      if (deptId) {
        return await permissionManager.hasDeptPermission(deptId)
      }
      return permissionManager.getAccessibleDepts().length > 0

    case 'SELF':
      if (userId) {
        return await permissionManager.hasPersonalDataPermission(userId)
      }
      return true

    default:
      return false
  }
}

/**
 * 处理点击事件
 */
const handleClick = (event) => {
  if (!hasPermission.value) {
    // 无权限时的处理
    if (props.noPermissionText) {
      message.warning(props.noPermissionText)
    }
    return
  }

  // 有权限时触发点击事件
  emit('click', event)
}

/**
 * 刷新权限
 */
const refreshPermission = () => {
  checkPermission()
}

/**
 * 监听权限配置变化
 */
watch(
  () => permissionConfig.value,
  () => {
    checkPermission()
  },
  { deep: true }
)

onMounted(() => {
  checkPermission()
})

/**
 * 暴露方法给父组件
 */
defineExpose({
  hasPermission: computed(() => hasPermission.value),
  loading: computed(() => loading.value),
  refreshPermission,
  checkPermission
})
</script>

<style scoped>
.permission-button {
  position: relative;
}

.permission-button--granted {
  /* 有权限时的样式 */
}

.permission-button--denied {
  opacity: 0.6;
  cursor: not-allowed;
}

.permission-button--loading {
  /* 权限检查中的样式 */
}
</style>