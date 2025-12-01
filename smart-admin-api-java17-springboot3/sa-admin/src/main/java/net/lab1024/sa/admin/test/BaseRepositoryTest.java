package net.lab1024.sa.admin.test;

import net.lab1024.sa.base.common.repository.BaseRepository;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;

/**
 * BaseRepository测试类 - 验证BaseRepository接口是否可用
 */
public class BaseRepositoryTest implements BaseRepository<AttendanceRecordEntity, Long> {
    // 简单测试BaseRepository接口

    @Override
    protected String getCachePrefix() {
        return "test:";
    }

    // 其他BaseRepository方法都有默认实现，不需要重写
}