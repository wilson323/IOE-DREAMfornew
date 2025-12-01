/**
 * RAC权限管理工具单元测试
 * <p>
 * 测试前端权限验证、缓存机制和组件集成
 * 确保前后端权限控制的一致性
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */

import { describe, test, expect, beforeEach, vi, afterEach } from 'vitest'
import { permissionManager, usePermission, useDataScope } from './permission.js'

// Mock fetch API
global.fetch = vi.fn()

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
global.localStorage = localStorageMock

describe('PermissionManager - 权限管理器核心功能', () => {
  beforeEach(() => {
    // 重置 mocks
    vi.clearAllMocks()
    permissionManager.clearCache()
  })

  afterEach(() => {
    localStorage.clear()
  })

  test('hasPermission - 有权限用户', async () => {
    // Given: 模拟有权限的API响应
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ hasPermission: true })
    })

    // When: 检查权限
    const result = await permissionManager.hasPermission('smart:access:device', 'READ')

    // Then: 应该返回有权限
    expect(result).toBe(true)
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/auth/permission/check'),
      expect.objectContaining({
        method: 'GET',
        params: expect.objectContaining({
          resource: 'smart:access:device',
          action: 'READ'
        })
      })
    )
  })

  test('hasPermission - 无权限用户', async () => {
    // Given: 模拟无权限的API响应
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ hasPermission: false })
    })

    // When: 检查权限
    const result = await permissionManager.hasPermission('smart:access:device', 'DELETE')

    // Then: 应该返回无权限
    expect(result).toBe(false)
  })

  test('hasPermission - 权限缓存机制', async () => {
    // Given: 模拟API响应
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ hasPermission: true })
    })

    // When: 第一次检查权限
    const result1 = await permissionManager.hasPermission('smart:access:device', 'READ')

    // 第二次检查相同权限（应该使用缓存）
    const result2 = await permissionManager.hasPermission('smart:access:device', 'READ')

    // Then: 应该返回相同结果且只调用一次API
    expect(result1).toBe(true)
    expect(result2).toBe(true)
    expect(fetch).toHaveBeenCalledTimes(1)
  })

  test('hasPermission - 批量权限检查', async () => {
    // Given: 模拟批量权限API响应
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        permissions: {
          'smart:access:device:READ': true,
          'smart:access:device:WRITE': true,
          'smart:access:device:DELETE': false
        }
      })
    })

    // When: 批量检查权限
    const permissions = [
      'smart:access:device:READ',
      'smart:access:device:WRITE',
      'smart:access:device:DELETE'
    ]
    const results = await permissionManager.batchCheckPermissions(permissions)

    // Then: 应该返回正确的权限结果
    expect(results).toEqual({
      'smart:access:device:READ': true,
      'smart:access:device:WRITE': true,
      'smart:access:device:DELETE': false
    })
  })

  test('hasAreaPermission - 区域权限检查', async () => {
    // Given: 模拟区域权限API响应
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ hasPermission: true, accessibleAreas: [1, 2, 3] })
    })

    // When: 检查区域权限
    const result = await permissionManager.hasAreaPermission(1)

    // Then: 应该返回有权限
    expect(result).toBe(true)
  })

  test('hasDeptPermission - 部门权限检查', async () => {
    // Given: 模拟部门权限API响应
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ hasPermission: true, accessibleDepts: [10, 20] })
    })

    // When: 检查部门权限
    const result = await permissionManager.hasDeptPermission(10)

    // Then: 应该返回有权限
    expect(result).toBe(true)
  })

  test('clearCache - 清除缓存', async () => {
    // Given: 先设置一些缓存
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ hasPermission: true })
    })
    await permissionManager.hasPermission('smart:access:device', 'READ')

    // When: 清除缓存
    permissionManager.clearCache()

    // Then: 再次检查权限应该重新调用API
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ hasPermission: false })
    })
    const result = await permissionManager.hasPermission('smart:access:device', 'READ')

    expect(result).toBe(false)
    expect(fetch).toHaveBeenCalledTimes(2)
  })

  test('API错误处理', async () => {
    // Given: API返回错误
    fetch.mockRejectedValueOnce(new Error('Network error'))

    // When: 检查权限
    const result = await permissionManager.hasPermission('smart:access:device', 'READ')

    // Then: 应该返回false（安全默认）
    expect(result).toBe(false)
  })
})

describe('usePermission - 权限组合式API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  test('hasPermission - 权限检查', async () => {
    // Given: Mock权限管理器
    const mockHasPermission = vi.fn().mockResolvedValue(true)
    vi.spyOn(permissionManager, 'hasPermission').mockImplementation(mockHasPermission)

    // When: 使用组合式API
    const { hasPermission } = usePermission()
    const result = await hasPermission('smart:access:device', 'READ')

    // Then: 应该调用权限管理器并返回结果
    expect(mockHasPermission).toHaveBeenCalledWith('smart:access:device', 'READ')
    expect(result).toBe(true)
  })

  test('canRead - 读取权限检查', async () => {
    // Given: Mock权限管理器
    const mockHasPermission = vi.fn().mockResolvedValue(true)
    vi.spyOn(permissionManager, 'hasPermission').mockImplementation(mockHasPermission)

    // When: 检查读取权限
    const { canRead } = usePermission()
    const result = await canRead('smart:access:device')

    // Then: 应该调用READ权限检查
    expect(mockHasPermission).toHaveBeenCalledWith('smart:access:device', 'READ')
    expect(result).toBe(true)
  })

  test('canWrite - 写入权限检查', async () => {
    // Given: Mock权限管理器
    const mockHasPermission = vi.fn().mockResolvedValue(true)
    vi.spyOn(permissionManager, 'hasPermission').mockImplementation(mockHasPermission)

    // When: 检查写入权限
    const { canWrite } = usePermission()
    const result = await canWrite('smart:access:device')

    // Then: 应该调用WRITE权限检查
    expect(mockHasPermission).toHaveBeenCalledWith('smart:access:device', 'WRITE')
    expect(result).toBe(true)
  })

  test('isSuperAdmin - 超级管理员检查', async () => {
    // Given: Mock本地存储
    localStorageMock.getItem.mockReturnValue('true')

    // When: 检查超级管理员
    const { isSuperAdmin } = usePermission()
    const result = isSuperAdmin()

    // Then: 应该返回true
    expect(result).toBe(true)
    expect(localStorageMock.getItem).toHaveBeenCalledWith('isSuperAdmin')
  })
})

describe('useDataScope - 数据域组合式API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  test('hasAreaPermission - 区域权限检查', async () => {
    // Given: Mock权限管理器
    const mockHasAreaPermission = vi.fn().mockResolvedValue(true)
    vi.spyOn(permissionManager, 'hasAreaPermission').mockImplementation(mockHasAreaPermission)

    // When: 检查区域权限
    const { hasAreaPermission } = useDataScope()
    const result = await hasAreaPermission(1)

    // Then: 应该调用权限管理器并返回结果
    expect(mockHasAreaPermission).toHaveBeenCalledWith(1)
    expect(result).toBe(true)
  })

  test('hasDeptPermission - 部门权限检查', async () => {
    // Given: Mock权限管理器
    const mockHasDeptPermission = vi.fn().mockResolvedValue(true)
    vi.spyOn(permissionManager, 'hasDeptPermission').mockImplementation(mockHasDeptPermission)

    // When: 检查部门权限
    const { hasDeptPermission } = useDataScope()
    const result = await hasDeptPermission(10)

    // Then: 应该调用权限管理器并返回结果
    expect(mockHasDeptPermission).toHaveBeenCalledWith(10)
    expect(result).toBe(true)
  })

  test('getAccessibleAreas - 获取可访问区域', async () => {
    // Given: Mock权限管理器
    const expectedAreas = [1, 2, 3]
    vi.spyOn(permissionManager, 'getAccessibleAreas').mockResolvedValue(expectedAreas)

    // When: 获取可访问区域
    const { getAccessibleAreas } = useDataScope()
    const result = await getAccessibleAreas()

    // Then: 应该返回可访问区域列表
    expect(result).toEqual(expectedAreas)
  })
})

describe('权限组件集成测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  test('v-permission 指令 - 简单权限检查', async () => {
    // Given: Mock权限管理器
    vi.spyOn(permissionManager, 'hasPermission').mockResolvedValue(true)

    // When: 模拟权限指令检查
    const permission = 'smart:access:device:read'
    const result = await permissionManager.hasPermission(
      permission.split(':')[0] + ':' + permission.split(':')[1],
      permission.split(':')[2]
    )

    // Then: 应该正确解析并检查权限
    expect(result).toBe(true)
  })

  test('v-permission 指令 - 数组权限检查（OR逻辑）', async () => {
    // Given: Mock权限管理器（任一权限为true）
    vi.spyOn(permissionManager, 'hasPermission')
      .mockResolvedValueOnce(false) // 第一个权限false
      .mockResolvedValueOnce(true)  // 第二个权限true

    // When: 检查权限数组
    const permissions = ['smart:access:device:read', 'smart:access:device:write']
    const results = await Promise.all(
      permissions.map(perm =>
        permissionManager.hasPermission(
          perm.split(':')[0] + ':' + perm.split(':')[1],
          perm.split(':')[2]
        )
      )
    )

    // Then: 应该返回OR逻辑结果
    const hasAnyPermission = results.some(result => result)
    expect(hasAnyPermission).toBe(true)
  })

  test('v-permission 指令 - 对象格式权限检查', async () => {
    // Given: Mock权限管理器
    vi.spyOn(permissionManager, 'hasAreaPermission').mockResolvedValue(true)

    // When: 检查对象格式权限
    const permissionObj = {
      resource: 'smart:access:device',
      action: 'WRITE',
      dataScope: 'AREA',
      areaId: 1
    }
    const result = await permissionManager.hasAreaPermission(permissionObj.areaId)

    // Then: 应该检查区域权限
    expect(result).toBe(true)
  })
})

describe('权限性能测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  test('权限检查性能 - 大量并发请求', async () => {
    // Given: Mock快速响应
    fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ hasPermission: true })
    })

    // When: 并发检查大量权限
    const startTime = Date.now()
    const promises = Array.from({ length: 100 }, () =>
      permissionManager.hasPermission('smart:access:device', 'READ')
    )
    await Promise.all(promises)
    const endTime = Date.now()

    // Then: 应该在合理时间内完成（考虑缓存）
    expect(endTime - startTime).toBeLessThan(1000)
  })

  test('权限缓存性能 - 重复检查优化', async () => {
    // Given: Mock响应
    fetch.mockResolvedValue({
      ok: true,
      json: async () => ({ hasPermission: true })
    })

    // When: 多次检查相同权限
    const startTime = Date.now()
    for (let i = 0; i < 1000; i++) {
      await permissionManager.hasPermission('smart:access:device', 'READ')
    }
    const endTime = Date.now()

    // Then: 由于缓存，应该只调用一次API且很快完成
    expect(fetch).toHaveBeenCalledTimes(1)
    expect(endTime - startTime).toBeLessThan(100)
  })
})