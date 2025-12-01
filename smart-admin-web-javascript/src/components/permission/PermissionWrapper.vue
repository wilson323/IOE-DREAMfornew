<!--
  权限包装组件
  基于RAC权限模型的权限控制组件，提供细粒度的权限管理
-->

<template>
  <div v-if="hasPermission" class="permission-wrapper">
    <slot />
  </div>
  <div v-else-if="showFallback" class="permission-fallback">
    <slot name="fallback">
      <a-empty
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
        description="您没有权限访问此内容"
      />
    </slot>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { Empty, message } from 'ant-design-vue'
import { permissionManager } from '@/utils/permission'

/**
 * 组件属性
 */
const props = defineProps({
  // 权限配置 - 支持多种格式
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

  // 权限检查模式
  mode: {
    type: String,
    default: 'strict', // strict | loose
    validator: (value) => ['strict', 'loose'].includes(value)
  },

  // 是否显示无权限时的替代内容
  showFallback: {
    type: Boolean,
    default: true
  },

  // 缓存权限检查结果
  cache: {
    type: Boolean,
    default: true
  },

  // 实时权限检查
  realtime: {
    type: Boolean,
    default: false
  }
})

/**
 * 权限检查结果
 */
const hasPermission = ref(false)
const loading = ref(false)
const lastCheckTime = ref(0)

/**
 * 计算权限配置
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

    const config = permissionConfig.value

    if (!config) {
      hasPermission.value = props.mode === 'loose'
      return
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

    hasPermission.value = result
    lastCheckTime.value = Date.now()

  } catch (error) {
    console.error('权限检查失败:', error)
    hasPermission.value = false
    message.error('权限检查失败')
  } finally {
    loading.value = false
  }
}

/**
 * 检查字符串格式权限
 */
const checkStringPermission = async (permission) => {
  const parts = permission.split(':')

  if (parts.length === 1) {
    // 只有资源，默认检查READ权限
    return await permissionManager.hasPermission(parts[0].trim(), 'READ')
  } else if (parts.length === 2) {
    // 资源:动作格式
    return await permissionManager.hasPermission(parts[0].trim(), parts[1].trim())
  }

  return false
}

/**
 * 检查数组格式权限
 */
const checkArrayPermission = async (permissions) => {
  // 检查任一权限（OR逻辑）
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
 * 刷新权限
 */
const refreshPermission = () => {
  lastCheckTime.value = 0
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

/**
 * 实时权限检查
 */
let checkInterval = null

onMounted(() => {
  checkPermission()

  if (props.realtime) {
    checkInterval = setInterval(() => {
      // 每30秒检查一次权限
      if (Date.now() - lastCheckTime.value > 30000) {
        checkPermission()
      }
    }, 5000)
  }
})

onBeforeUnmount(() => {
  if (checkInterval) {
    clearInterval(checkInterval)
  }
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
.permission-wrapper {
  width: 100%;
}

.permission-fallback {
  width: 100%;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.permission-fallback :deep(.ant-empty-description) {
  color: #999;
}
</style>