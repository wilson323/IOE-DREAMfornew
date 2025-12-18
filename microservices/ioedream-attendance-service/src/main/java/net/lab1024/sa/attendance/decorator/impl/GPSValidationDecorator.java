package net.lab1024.sa.attendance.decorator.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.decorator.IPunchExecutor;
import net.lab1024.sa.attendance.decorator.PunchDecorator;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * GPS位置验证装饰器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 验证用户是否在公司区域内打卡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class GPSValidationDecorator extends PunchDecorator {

    @Resource
    private CompanyAreaService companyAreaService;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public GPSValidationDecorator(IPunchExecutor delegate) {
        super(delegate);
    }

    @Override
    public PunchResult execute(MobilePunchRequest request) {
        // GPS位置验证
        if (request.getLatitude() != null && request.getLongitude() != null) {
            boolean withinCompanyArea = companyAreaService.isWithinArea(
                    request.getLatitude(),
                    request.getLongitude()
            );

            if (!withinCompanyArea) {
                log.warn("[GPS验证装饰器] 用户不在打卡范围内, userId={}, location=({}, {})",
                        request.getUserId(), request.getLatitude(), request.getLongitude());
                return PunchResult.failed("不在打卡范围内");
            }
        }

        return super.execute(request);
    }

    /**
     * 公司区域服务（临时接口，待实现）
     */
    public interface CompanyAreaService {
        boolean isWithinArea(Double latitude, Double longitude);
    }
}
