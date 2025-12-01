/**
 * 扩展的权限指令
 * 基于RAC权限模型的v-permission指令扩展，支持资源-动作-条件（RAC）的细粒度权限控制
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */

import { permissionManager } from '@/utils/permission'
import { usePermissionStore } from '@/stores/smart-permission'

/**
 * 权限指令
 * 支持的用法：
 * v-permission="'resource:action'" - 检查资源动作权限
 * v-permission="'resource'" - 检查资源读权限（默认）
 * v-permission="['resource1:action1', 'resource2:action2']" - 批量权限检查
 * v-permission="{ resource: 'smart:device', action: 'WRITE', dataScope: 'AREA' }" - 对象格式权限检查
 * v-permission:resource="DEVICE_CODE" - 检查资源权限
 * v-permission:action="WRITE" - 检查动作权限
 * v-permission:dataScope="'AREA'" - 检查数据域权限
 * v-permission:role="'ROLE_CODE'" - 检查角色权限
 * v-permission:superAdmin - 检查超级管理员权限
 */
export const permissionDirective = {
  // 权限检查结果缓存
  permissionCache: new Map(),

  mounted(el, binding) {
    this.checkPermission(el, binding)
  },
  updated(el, binding) {
    this.checkPermission(el, binding)
  }
}

/**
 * 权限显示指令 v-permission-show
 * 不符合权限时完全移除DOM元素（而非隐藏）
 */
export const permissionShowDirective = {
  mounted(el, binding) {
    this.checkPermissionForShow(el, binding)
  },
  updated(el, binding) {
    this.checkPermissionForShow(el, binding)
  }
}

/**
 * 权限禁用指令 v-permission-disabled
 * 不符合权限时禁用元素（而非隐藏）
 */
export const permissionDisabledDirective = {
  mounted(el, binding) {
    this.checkPermissionForDisabled(el, binding)
  },
  updated(el, binding) {
    this.checkPermissionForDisabled(el, binding)
  }
}

/**
 * 检查权限
 * @param {HTMLElement} el - DOM元素
 * @param {Object} binding - 指令绑定对象
 */
async function checkPermission(el, binding) {
  try {
    let hasPermission = false

    // 处理不同类型的权限检查
    if (binding.arg) {
      // 带修饰符的权限检查
      hasPermission = await checkPermissionWithModifier(el, binding)
    } else if (binding.value) {
      // 直接权限值检查
      hasPermission = await checkPermissionValue(binding.value)
    }

    // 根据权限结果控制元素显示
    updateElementVisibility(el, hasPermission, binding)
  } catch (error) {
    console.error('权限检查失败:', error)
    updateElementVisibility(el, false, binding)
  }
}

/**
 * 带修饰符的权限检查
 * @param {HTMLElement} el - DOM元素
 * @param {Object} binding - 指令绑定对象
 * @returns {boolean} 是否有权限
 */
async function checkPermissionWithModifier(el, binding) {
  const modifier = binding.arg
  const value = binding.value

  switch (modifier) {
    case 'resource':
      return await checkResourcePermission(value, binding.modifiers)
    case 'action':
      return await checkActionPermission(value, binding.modifiers)
    case 'dataScope':
      return await checkDataScopePermissionWithModifier(value, binding.modifiers)
    case 'role':
      return await checkRolePermissionWithModifier(value, binding.modifiers)
    case 'superAdmin':
      return await checkSuperAdminPermission()
    default:
      console.warn('未知的权限修饰符:', modifier)
      return false
  }
}

/**
 * 检查资源权限
 * @param {string|Object} value - 资源值
 * @param {Object} modifiers - 修饰符
 * @returns {boolean} 是否有权限
 */
async function checkResourcePermission(value, modifiers) {
  const resourceCode = typeof value === 'string' ? value : value.code
  const action = modifiers.action || 'READ'

  return await permissionManager.hasPermission(resourceCode, action)
}

/**
 * 检查动作权限
 * @param {string} action - 动作
 * @param {Object} modifiers - 修饰符
 * @returns {boolean} 是否有权限
 */
async function checkActionPermission(action, modifiers) {
  const resourceCode = modifiers.resource

  if (!resourceCode) {
    console.warn('检查动作权限时缺少资源代码')
    return false
  }

  return await permissionManager.hasPermission(resourceCode, action)
}

/**
 * 检查数据域权限（带修饰符）
 * @param {string} dataScope - 数据域
 * @param {Object} modifiers - 修饰符
 * @returns {boolean} 是否有权限
 */
async function checkDataScopePermissionWithModifier(dataScope, modifiers) {
  const dataScopeMap = {
    'area': 'AREA',
    'dept': 'DEPT',
    'self': 'SELF',
    'custom': 'CUSTOM'
  }

  const actualDataScope = dataScopeMap[dataScope.toLowerCase()] || dataScope.toUpperCase()

  if (modifiers.area) {
    return await permissionManager.hasAreaPermission(modifiers.area)
  } else if (modifiers.dept) {
    return await permissionManager.hasDeptPermission(modifiers.dept)
  } else if (modifiers.userId) {
    return await permissionManager.hasPersonalDataPermission(modifiers.userId)
  } else {
    // 检查数据域类型权限
    const userPermissions = permissionManager.getUserPermissions()
    switch (actualDataScope) {
      case 'ALL':
        return true
      case 'AREA':
        return userPermissions.accessibleAreas?.size > 0
      case 'DEPT':
        return userPermissions.accessibleDepts?.size > 0
      case 'SELF':
        return true
      default:
        return false
    }
  }
}

/**
 * 检查角色权限（带修饰符）
 * @param {string|Array} role - 角色
 * @param {Object} modifiers - 修饰符
 * @returns {boolean} 是否有权限
 */
async function checkRolePermissionWithModifier(role, modifiers) {
  const userRoles = permissionManager.getUserRoles()

  if (modifiers.any) {
    // 检查是否具有任一指定角色
    const roles = Array.isArray(role) ? role : [role]
    return roles.some(r => userRoles.includes(r))
  } else if (modifiers.all) {
    // 检查是否具有所有指定角色
    const roles = Array.isArray(role) ? role : [role]
    return roles.every(r => userRoles.includes(r))
  } else {
    // 默认检查单个角色
    const roles = Array.isArray(role) ? role : [role]
    return roles.some(r => userRoles.includes(r))
  }
}

/**
 * 检查超级管理员权限
 * @returns {boolean} 是否有权限
 */
async function checkSuperAdminPermission() {
  return permissionManager.isSuperAdmin()
}

/**
 * 检查权限值
 * @param {string|Array|Object} value - 权限值
 * @returns {boolean} 是否有权限
 */
async function checkPermissionValue(value) {
  if (typeof value === 'string') {
    // 字符串格式：'resource:action' 或 'resource'
    return await checkPermissionString(value)
  } else if (Array.isArray(value)) {
    // 数组格式：['resource1:action1', 'resource2:action2']
    return await checkPermissionArray(value)
  } else if (typeof value === 'object' && value !== null) {
    // 对象格式：{ resource: 'smart:device', action: 'WRITE' }
    return await checkPermissionObject(value)
  }

  return false
}

/**
 * 检查字符串格式权限
 * @param {string} value - 权限字符串
 * @returns {boolean} 是否有权限
 */
async function checkPermissionString(value) {
  const cacheKey = `permission:${value}`
  if (permissionDirective.permissionCache.has(cacheKey)) {
    return permissionDirective.permissionCache.get(cacheKey)
  }

  let hasPermission = false
  const parts = value.split(':')

  if (parts.length === 1) {
    // 只有资源，默认检查READ权限
    hasPermission = await permissionManager.hasPermission(parts[0].trim(), 'READ')
  } else if (parts.length === 2) {
    // 资源:动作格式
    hasPermission = await permissionManager.hasPermission(parts[0].trim(), parts[1].trim())
  } else {
    // 格式错误
    console.warn('权限格式错误:', value)
    hasPermission = false
  }

  permissionDirective.permissionCache.set(cacheKey, hasPermission)
  return hasPermission
}

/**
 * 检查数组格式权限
 * @param {Array} values - 权限数组
 * @returns {boolean} 是否有权限
 */
async function checkPermissionArray(values) {
  // 检查任一权限（OR逻辑）
  for (const value of values) {
    if (await checkPermissionValue(value)) {
      return true
    }
  }
  return false
}

/**
 * 检查对象格式权限
 * @param {Object} obj - 权限对象
 * @returns {boolean} 是否有权限
 */
async function checkPermissionObject(obj) {
  const { resource, action = 'READ', dataScope, role, superAdmin } = obj

  // 检查超级管理员权限
  if (superAdmin) {
    return await permissionManager.isSuperAdmin()
  }

  // 检查角色权限
  if (role) {
    const hasRole = await checkRolePermissionWithModifier(role, {})
    if (!hasRole) {
      return false
    }
  }

  // 检查基础权限
  if (!resource) {
    console.warn('权限对象中缺少resource字段')
    return false
  }

  const hasBasicPermission = await permissionManager.hasPermission(resource, action)
  if (!hasBasicPermission) {
    return false
  }

  // 如果指定了数据域，再检查数据域权限
  if (dataScope) {
    const hasDataScopePermission = await checkDataScopeForObject(dataScope, obj)
    if (!hasDataScopePermission) {
      return false
    }
  }

  return true
}

/**
 * 检查数据域对象权限
 * @param {string} dataScope - 数据域
 * @param {Object} context - 上下文对象
 * @returns {boolean} 是否有权限
 */
async function checkDataScopeForObject(dataScope, context) {
  const dataScopeMap = {
    'ALL': 'ALL',
    'AREA': 'AREA',
    'DEPT': 'DEPT',
    'SELF': 'SELF',
    'CUSTOM': 'CUSTOM'
  }

  const actualDataScope = dataScopeMap[dataScope.toUpperCase()] || dataScope

  if (actualDataScope === 'ALL') {
    return true
  }

  // 如果有具体的区域ID，检查区域权限
  if (actualDataScope === 'AREA' && context.areaId) {
    return await permissionManager.hasAreaPermission(context.areaId)
  }

  // 如果有具体的部门ID，检查部门权限
  if (actualDataScope === 'DEPT' && context.deptId) {
    return await permissionManager.hasDeptPermission(context.deptId)
  }

  // 如果有具体的用户ID，检查个人数据权限
  if (actualDataScope === 'SELF' && context.userId) {
    return await permissionManager.hasPersonalDataPermission(context.userId)
  }

  // 否则检查用户是否具有该数据域类型的权限
  const userPermissions = permissionManager.getUserPermissions()
  switch (actualDataScope) {
    case 'AREA':
      return userPermissions.accessibleAreas?.size > 0
    case 'DEPT':
      return userPermissions.accessibleDepts?.size > 0
    case 'SELF':
      return true
    default:
      return false
  }
}

/**
 * 检查角色权限
 * @param {string|Array} role - 角色
 * @param {Object} modifiers - 修饰符
 * @returns {boolean} 是否有权限
 */
async function checkRolePermission(role, modifiers) {
  const userRoles = permissionManager.getUserRoles()

  if (modifiers.any) {
    // 检查是否具有任一指定角色
    const roles = Array.isArray(role) ? role : [role]
    return roles.some(r => userRoles.includes(r))
  } else if (modifiers.all) {
    // 检查是否具有所有指定角色
    const roles = Array.isArray(role) ? role : [role]
    return roles.every(r => userRoles.includes(r))
  } else {
    // 默认检查单个角色
    const roles = Array.isArray(role) ? role : [role]
    return roles.some(r => userRoles.includes(r))
  }
}

/**
 * 为v-permission-show检查权限
 */
async function checkPermissionForShow(el, binding) {
  try {
    let hasPermission = false

    if (binding.arg) {
      hasPermission = await checkPermissionWithModifier(el, binding)
    } else if (binding.value) {
      hasPermission = await checkPermissionValue(binding.value)
    }

    // 如果没有权限，移除DOM元素
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  } catch (error) {
    console.error('权限显示检查失败:', error)
    // 权限检查失败时移除元素
    el.parentNode && el.parentNode.removeChild(el)
  }
}

/**
 * 为v-permission-disabled检查权限
 */
async function checkPermissionForDisabled(el, binding) {
  try {
    let hasPermission = false

    if (binding.arg) {
      hasPermission = await checkPermissionWithModifier(el, binding)
    } else if (binding.value) {
      hasPermission = await checkPermissionValue(binding.value)
    }

    // 根据权限结果设置禁用状态
    if (el.tagName === 'BUTTON' || el.tagName === 'INPUT' || el.tagName === 'SELECT' || el.tagName === 'TEXTAREA') {
      el.disabled = !hasPermission

      if (!hasPermission) {
        el.setAttribute('data-permission-disabled', 'true')
        el.style.opacity = '0.5'
        el.style.cursor = 'not-allowed'
      } else {
        el.removeAttribute('data-permission-disabled')
        el.style.opacity = ''
        el.style.cursor = ''
      }
    }
  } catch (error) {
    console.error('权限禁用检查失败:', error)
    // 权限检查失败时禁用元素
    if (el.tagName === 'BUTTON' || el.tagName === 'INPUT' || el.tagName === 'SELECT' || el.tagName === 'TEXTAREA') {
      el.disabled = true
      el.setAttribute('data-permission-error', 'true')
      el.style.opacity = '0.5'
      el.style.cursor = 'not-allowed'
    }
  }
}

/**
 * 更新元素可见性
 * @param {HTMLElement} el - DOM元素
 * @param {boolean} visible - 是否可见
 * @param {Object} binding - 指令绑定对象
 */
function updateElementVisibility(el, visible, binding) {
  const displayMode = binding.modifiers?.display || 'none'
  const classMode = binding.modifiers?.class || ''

  if (visible) {
    // 有权限时显示元素
    el.style.display = ''
    el.removeAttribute('data-permission-denied')

    // 如果指定了显示模式，使用指定的显示方式
    if (displayMode !== 'none') {
      el.style.display = displayMode
    }

    // 如果指定了class模式，添加指定的class
    if (classMode) {
      el.classList.add('permission-granted')
    }
  } else {
    // 无权限时隐藏元素
    el.style.display = displayMode === 'none' ? 'none' : 'none'
    el.setAttribute('data-permission-denied', 'true')

    // 如果指定了class模式，移除权限class
    if (classMode) {
      el.classList.remove('permission-granted')
    }
  }
}

/**
 * 清除权限缓存
 */
function clearPermissionCache() {
  permissionDirective.permissionCache.clear()
  permissionManager.clearCache()
}

// 兼容旧版权限Store的辅助函数
function checkAreaPermission(store, binding) {
  // 如果有RAC权限管理器，优先使用
  if (permissionManager) {
    return checkResourcePermission(binding.value, binding.modifiers)
  }

  // 否则使用旧版权限Store
  const areaLevel = binding.value || 1
  if (binding.modifiers.manage) {
    return store.permissions.canManageArea()
  } else if (binding.modifiers.config) {
    return store.permissions.canConfigArea()
  } else {
    return store.permissions.canAccessArea(areaLevel)
  }
}

function checkDevicePermission(store, binding) {
  if (permissionManager) {
    return checkResourcePermission(binding.value, binding.modifiers)
  }

  if (binding.modifiers.view) {
    return store.permissions.canViewDevice()
  } else if (binding.modifiers.config) {
    return store.permissions.canConfigDevice()
  } else {
    return store.permissions.canControlDevice()
  }
}

function checkAttendancePermission(store, binding) {
  if (permissionManager) {
    return checkResourcePermission(binding.value, binding.modifiers)
  }

  if (binding.modifiers.view) {
    return store.permissions.canViewAttendance()
  } else if (binding.modifiers.export) {
    return store.permissions.canExportAttendance()
  } else {
    return store.permissions.canManageAttendance()
  }
}

function checkAccessPermission(store, binding) {
  if (permissionManager) {
    return checkResourcePermission(binding.value, binding.modifiers)
  }

  if (binding.modifiers.manage) {
    return store.permissions.canManageAccess()
  } else if (binding.modifiers.config) {
    return store.permissions.canConfigAccess()
  } else {
    return store.permissions.canEnterAccess()
  }
}

function checkWithContext(store, permissionCode, context) {
  // 这里可以实现更复杂的权限检查逻辑
  return new Promise((resolve) => {
    store.checkPermission(permissionCode, context)
      .then(result => {
        resolve(result?.granted || false)
      })
      .catch(() => {
        resolve(false)
      })
  })
}

function checkDataScopePermission(store, binding) {
  if (permissionManager) {
    const dataScope = binding.value || 'SELF'

    if (binding.modifiers.area) {
      return store.hasDataScope('AREA')
    } else if (binding.modifiers.dept) {
      return store.hasDataScope('DEPT')
    } else if (binding.modifiers.self) {
      return store.hasDataScope('SELF')
    } else if (binding.modifiers.custom) {
      return store.hasDataScope('CUSTOM')
    } else {
      return store.hasDataScope(dataScope)
    }
  }

  // 旧版数据域权限检查逻辑
  const dataScope = binding.value || 'SELF'

  if (binding.modifiers.area) {
    return store.hasDataScope('AREA')
  } else if (binding.modifiers.dept) {
    return store.hasDataScope('DEPT')
  } else if (binding.modifiers.self) {
    return store.hasDataScope('SELF')
  } else if (binding.modifiers.custom) {
    return store.hasDataScope('CUSTOM')
  } else {
    return store.hasDataScope(dataScope)
  }
}

function checkRolePermission(store, binding) {
  if (permissionManager) {
    return checkRolePermissionWithModifier(binding.value, binding.modifiers)
  }

  const requiredRole = binding.value

  if (!requiredRole) {
    return false
  }

  if (binding.modifiers.any) {
    // 检查是否具有任一指定角色
    const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole]
    return roles.some(role => store.hasRole(role))
  } else if (binding.modifiers.all) {
    // 检查是否具有所有指定角色
    const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole]
    return roles.every(role => store.hasRole(role))
  } else {
    // 默认检查单个角色
    const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole]
    return roles.some(role => store.hasRole(role))
  }
}

export default {
  // 主权限指令（隐藏无权限元素）
  permission: permissionDirective,

  // 权限显示指令（移除无权限元素）
  'permission-show': permissionShowDirective,

  // 权限禁用指令（禁用无权限元素）
  'permission-disabled': permissionDisabledDirective,

  // 工具方法
  clearCache,
  clearPermissionCache
}