/**
 * 门禁管理API测试
 * <p>
 * 测试移动端门禁管理相关API调用
 * 包括权限查询、区域列表等功能
 * </p>
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { permissionApi } from '@/api/business/access/access-api'
import { getRequest } from '@/lib/smart-request'

// Mock请求库
vi.mock('@/lib/smart-request', () => ({
  getRequest: vi.fn()
}))

describe('门禁管理API测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('权限相关API', () => {
    it('应该成功获取用户权限', async () => {
      const userId = 1001

      getRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: {
          allowedAreaIds: [1, 2, 3],
          allowedDeviceIds: [101, 102, 103]
        }
      })

      const result = await permissionApi.getUserPermissions(userId)

      expect(getRequest).toHaveBeenCalledWith(`/api/v1/mobile/access/permissions/${userId}`)
      expect(result.success).toBe(true)
      expect(result.data.allowedAreaIds).toHaveLength(3)
    })
  })
})

