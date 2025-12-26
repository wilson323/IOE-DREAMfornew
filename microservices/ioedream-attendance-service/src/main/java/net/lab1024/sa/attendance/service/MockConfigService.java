package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.MockConfigForm;
import net.lab1024.sa.attendance.domain.vo.MockConfigVO;
import net.lab1024.sa.attendance.domain.vo.MockDataVO;

import java.util.List;

/**
 * Mock配置服务接口
 * <p>
 * 提供Mock配置管理和数据生成功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface MockConfigService {

    /**
     * 创建Mock配置
     *
     * @param form Mock配置表单
     * @return 配置ID
     */
    Long createMockConfig(MockConfigForm form);

    /**
     * 更新Mock配置
     *
     * @param form Mock配置表单
     */
    void updateMockConfig(MockConfigForm form);

    /**
     * 删除Mock配置
     *
     * @param configId 配置ID
     */
    void deleteMockConfig(Long configId);

    /**
     * 查询Mock配置
     *
     * @param configId 配置ID
     * @return Mock配置VO
     */
    MockConfigVO getMockConfig(Long configId);

    /**
     * 查询所有Mock配置
     *
     * @return Mock配置列表
     */
    List<MockConfigVO> getAllMockConfigs();

    /**
     * 按类型查询Mock配置
     *
     * @param configType 配置类型
     * @return Mock配置列表
     */
    List<MockConfigVO> getMockConfigsByType(String configType);

    /**
     * 生成Mock数据
     *
     * @param configId 配置ID
     * @return Mock数据
     */
    MockDataVO generateMockData(Long configId);

    /**
     * 生成Mock数据（按类型）
     *
     * @param configType 配置类型
     * @param mockScenario Mock场景
     * @return Mock数据
     */
    MockDataVO generateMockDataByType(String configType, String mockScenario);

    /**
     * 启用Mock配置
     *
     * @param configId 配置ID
     */
    void enableMockConfig(Long configId);

    /**
     * 禁用Mock配置
     *
     * @param configId 配置ID
     */
    void disableMockConfig(Long configId);

    /**
     * 检查Mock是否启用
     *
     * @param configType 配置类型
     * @return 是否启用
     */
    boolean isMockEnabled(String configType);
}
