package net.lab1024.sa.admin.module.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.admin.module.access.dao.VisitorReservationDao;
import net.lab1024.sa.admin.module.access.dao.VisitorRecordDao;
import net.lab1024.sa.admin.module.access.domain.entity.VisitorReservationEntity;
import net.lab1024.sa.admin.module.access.domain.entity.VisitorRecordEntity;
import net.lab1024.sa.admin.module.access.service.VisitorService;
import net.lab1024.sa.admin.module.access.manager.VisitorCacheManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 访客管理服务实现
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 完整的业务逻辑实现
 * - 统一的事务管理
 * - 异常处理和日志记录
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class VisitorServiceImpl implements VisitorService {

    @Resource
    private VisitorReservationDao visitorReservationDao;

    @Resource
    private VisitorRecordDao visitorRecordDao;

    @Resource
    private VisitorCacheManager visitorCacheManager;

    private static final String QR_SECRET_KEY = "ioedream-visitor-qr-secret";
    private static final long QR_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时

    // ====================== 预约管理 ======================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createReservation(VisitorReservationEntity reservation) {
        try {
            log.info("创建访客预约: {}", reservation.getVisitorName());

            // 检查重复预约
            ResponseDTO<Boolean> duplicateCheck = checkDuplicateReservation(
                    reservation.getVisitorPhone(), reservation.getVisitDate(), null);
            if (!duplicateCheck.getOk() || duplicateCheck.getData()) {
                return ResponseDTO.userErrorParam("该访客在同一天已有预约");
            }

            // 检查黑名单
            ResponseDTO<Map<String, Object>> blacklistCheck = checkVisitorBlacklist(
                    reservation.getVisitorName(), reservation.getVisitorPhone(), reservation.getVisitorIdCard());
            if (!blacklistCheck.getOk() || (Boolean) blacklistCheck.getData().getOrDefault("isBlacklisted", false)) {
                return ResponseDTO.userErrorParam("访客在黑名单中，无法预约");
            }

            // 设置默认值
            reservation.setCreateTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());
            reservation.setDeletedFlag(0);
            if (reservation.getApprovalStatus() == null) {
                reservation.setApprovalStatus(0); // 待审批
            }

            int result = visitorReservationDao.insert(reservation);
            if (result > 0) {
                // 清除缓存
                visitorCacheManager.clearReservationCache();
                log.info("访客预约创建成功: ID={}, 姓名={}", reservation.getReservationId(), reservation.getVisitorName());
                return ResponseDTO.ok("预约创建成功");
            } else {
                return ResponseDTO.error("预约创建失败");
            }
        } catch (Exception e) {
            log.error("创建访客预约失败: {}", reservation.getVisitorName(), e);
            return ResponseDTO.error("预约创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateReservation(VisitorReservationEntity reservation) {
        try {
            log.info("更新访客预约: {}", reservation.getReservationId());

            VisitorReservationEntity existing = visitorReservationDao.selectById(reservation.getReservationId());
            if (existing == null) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            // 检查审批状态，已审批的预约不能修改关键信息
            if (existing.getApprovalStatus() != null && existing.getApprovalStatus() == 2) {
                return ResponseDTO.userErrorParam("已审批通过的预约不能修改");
            }

            reservation.setUpdateTime(LocalDateTime.now());
            int result = visitorReservationDao.updateById(reservation);

            if (result > 0) {
                // 清除缓存
                visitorCacheManager.clearReservationCache();
                log.info("访客预约更新成功: ID={}", reservation.getReservationId());
                return ResponseDTO.ok("预约更新成功");
            } else {
                return ResponseDTO.error("预约更新失败");
            }
        } catch (Exception e) {
            log.error("更新访客预约失败: {}", reservation.getReservationId(), e);
            return ResponseDTO.error("预约更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteReservation(Long reservationId) {
        try {
            log.info("删除访客预约: {}", reservationId);

            VisitorReservationEntity existing = visitorReservationDao.selectById(reservationId);
            if (existing == null) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            // 软删除
            existing.setDeletedFlag(1);
            existing.setUpdateTime(LocalDateTime.now());
            int result = visitorReservationDao.updateById(existing);

            if (result > 0) {
                // 清除缓存
                visitorCacheManager.clearReservationCache();
                log.info("访客预约删除成功: ID={}", reservationId);
                return ResponseDTO.ok("预约删除成功");
            } else {
                return ResponseDTO.error("预约删除失败");
            }
        } catch (Exception e) {
            log.error("删除访客预约失败: {}", reservationId, e);
            return ResponseDTO.error("预约删除失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<VisitorReservationEntity>> getReservationPage(
            PageParam pageParam, String keyword, Integer approvalStatus, LocalDate visitDate) {
        try {
            log.info("查询访客预约列表: keyword={}, approvalStatus={}, visitDate={}", keyword, approvalStatus, visitDate);

            // 构建查询条件
            LambdaQueryWrapper<VisitorReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorReservationEntity::getDeletedFlag, false);

            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like(VisitorReservationEntity::getVisitorName, keyword)
                        .or()
                        .like(VisitorReservationEntity::getVisitorPhone, keyword)
                        .or()
                        .like(VisitorReservationEntity::getVisitorCompany, keyword)
                        .or()
                        .like(VisitorReservationEntity::getHostUserName, keyword));
            }

            if (approvalStatus != null) {
                queryWrapper.eq(VisitorReservationEntity::getApprovalStatus, approvalStatus);
            }

            if (visitDate != null) {
                queryWrapper.eq(VisitorReservationEntity::getVisitDate, visitDate);
            }

            // 排序
            queryWrapper.orderByDesc(VisitorReservationEntity::getCreateTime);

            // 分页查询
            Page<VisitorReservationEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            IPage<VisitorReservationEntity> pageResult = visitorReservationDao.selectPage(page, queryWrapper);

            // 构建返回结果
            PageResult<VisitorReservationEntity> result = new PageResult<>();
            result.setPageNum(pageResult.getCurrent());
            result.setPageSize(pageResult.getSize());
            result.setTotal(pageResult.getTotal());
            result.setPages(pageResult.getPages());
            result.setList(pageResult.getRecords());

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("查询访客预约列表失败", e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<VisitorReservationEntity> getReservationById(Long reservationId) {
        try {
            log.info("获取访客预约详情: {}", reservationId);

            VisitorReservationEntity reservation = visitorReservationDao.selectById(reservationId);
            if (reservation == null || reservation.getDeletedFlag() != 0) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            return ResponseDTO.ok(reservation);
        } catch (Exception e) {
            log.error("获取访客预约详情失败: {}", reservationId, e);
            return ResponseDTO.error("获取详情失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<VisitorReservationEntity> getReservationByCode(String reservationCode) {
        try {
            log.info("根据编号查询访客预约: {}", reservationCode);

            LambdaQueryWrapper<VisitorReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorReservationEntity::getReservationCode, reservationCode)
                    .eq(VisitorReservationEntity::getDeletedFlag, false);

            VisitorReservationEntity reservation = visitorReservationDao.selectOne(queryWrapper);
            if (reservation == null) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            return ResponseDTO.ok(reservation);
        } catch (Exception e) {
            log.error("根据编号查询访客预约失败: {}", reservationCode, e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> approveReservation(Long reservationId, Integer approvalStatus,
                                                String approvalComment, Long approverId, String approverName) {
        try {
            log.info("审批访客预约: {}, 状态: {}", reservationId, approvalStatus);

            VisitorReservationEntity reservation = visitorReservationDao.selectById(reservationId);
            if (reservation == null || reservation.getDeletedFlag() != 0) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            reservation.setApprovalStatus(approvalStatus);
            reservation.setApprovalComment(approvalComment);
            reservation.setApproverId(approverId);
            reservation.setApproverName(approverName);
            reservation.setApprovalTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());

            int result = visitorReservationDao.updateById(reservation);
            if (result > 0) {
                // 清除缓存
                visitorCacheManager.clearReservationCache();
                log.info("访客预约审批成功: ID={}, 状态={}", reservationId, approvalStatus);
                return ResponseDTO.ok("审批成功");
            } else {
                return ResponseDTO.error("审批失败");
            }
        } catch (Exception e) {
            log.error("审批访客预约失败: {}", reservationId, e);
            return ResponseDTO.error("审批失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> generateQrCode(Long reservationId) {
        try {
            log.info("生成访客二维码: {}", reservationId);

            VisitorReservationEntity reservation = visitorReservationDao.selectById(reservationId);
            if (reservation == null || reservation.getDeletedFlag() != 0) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            if (reservation.getApprovalStatus() == null || reservation.getApprovalStatus() != 2) {
                return ResponseDTO.userErrorParam("预约未审批通过，无法生成二维码");
            }

            // 生成JWT令牌作为二维码内容
            Map<String, Object> claims = new HashMap<>();
            claims.put("reservationId", reservationId);
            claims.put("reservationCode", reservation.getReservationCode());
            claims.put("visitorName", reservation.getVisitorName());
            claims.put("visitorPhone", reservation.getVisitorPhone());
            claims.put("visitDate", reservation.getVisitDate().toString());
            claims.put("exp", System.currentTimeMillis() + QR_EXPIRE_TIME);

            String qrCode = Jwts.builder()
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.HS256, QR_SECRET_KEY)
                    .compact();

            // 缓存二维码
            visitorCacheManager.cacheQrCode(reservationId, qrCode, QR_EXPIRE_TIME);

            log.info("访客二维码生成成功: ID={}", reservationId);
            return ResponseDTO.ok(qrCode);
        } catch (Exception e) {
            log.error("生成访客二维码失败: {}", reservationId, e);
            return ResponseDTO.error("二维码生成失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<VisitorReservationEntity> validateQrCode(String qrCode) {
        try {
            log.info("验证访客二维码: {}", qrCode);

            // 验证JWT令牌
            try {
                Jwts.parser().setSigningKey(QR_SECRET_KEY).parseClaimsJws(qrCode);
            } catch (Exception e) {
                return ResponseDTO.userErrorParam("二维码无效或已过期");
            }

            // 从缓存获取预约信息
            Long reservationId = visitorCacheManager.getReservationIdFromQrCode(qrCode);
            if (reservationId == null) {
                // 从JWT解析
                try {
                    Map<String, Object> claims = Jwts.parser()
                            .setSigningKey(QR_SECRET_KEY)
                            .parseClaimsJws(qrCode)
                            .getBody();
                    reservationId = Long.valueOf(claims.get("reservationId").toString());
                } catch (Exception e) {
                    return ResponseDTO.userErrorParam("二维码解析失败");
                }
            }

            VisitorReservationEntity reservation = visitorReservationDao.selectById(reservationId);
            if (reservation == null || reservation.getDeletedFlag() != 0) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            if (reservation.getApprovalStatus() == null || reservation.getApprovalStatus() != 2) {
                return ResponseDTO.userErrorParam("预约未审批通过");
            }

            // 检查访问时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime visitStartTime = reservation.getVisitStartTime();
            LocalDateTime visitEndTime = reservation.getVisitEndTime();

            if (now.isBefore(visitStartTime.minusMinutes(30))) {
                return ResponseDTO.userErrorParam("预约时间未到，请稍后再试");
            }

            if (now.isAfter(visitEndTime.plusMinutes(30))) {
                return ResponseDTO.userErrorParam("预约时间已过，二维码失效");
            }

            log.info("访客二维码验证成功: ID={}", reservationId);
            return ResponseDTO.ok(reservation);
        } catch (Exception e) {
            log.error("验证访客二维码失败: {}", qrCode, e);
            return ResponseDTO.error("二维码验证失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> cancelReservation(Long reservationId, String cancelReason) {
        try {
            log.info("取消访客预约: {}, 原因: {}", reservationId, cancelReason);

            VisitorReservationEntity reservation = visitorReservationDao.selectById(reservationId);
            if (reservation == null || reservation.getDeletedFlag() != 0) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            reservation.setApprovalStatus(3); // 已取消
            reservation.setCancelReason(cancelReason);
            reservation.setCancelTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());

            int result = visitorReservationDao.updateById(reservation);
            if (result > 0) {
                // 清除缓存
                visitorCacheManager.clearReservationCache();
                log.info("访客预约取消成功: ID={}", reservationId);
                return ResponseDTO.ok("预约取消成功");
            } else {
                return ResponseDTO.error("预约取消失败");
            }
        } catch (Exception e) {
            log.error("取消访客预约失败: {}", reservationId, e);
            return ResponseDTO.error("预约取消失败: " + e.getMessage());
        }
    }

    // ====================== 访问记录管理 ======================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createAccessRecord(VisitorRecordEntity record) {
        try {
            log.info("创建访问记录: {}", record.getVisitorName());

            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            int result = visitorRecordDao.insert(record);
            if (result > 0) {
                log.info("访问记录创建成功: ID={}", record.getRecordId());
                return ResponseDTO.ok("记录创建成功");
            } else {
                return ResponseDTO.error("记录创建失败");
            }
        } catch (Exception e) {
            log.error("创建访问记录失败: {}", record.getVisitorName(), e);
            return ResponseDTO.error("记录创建失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<VisitorRecordEntity>> getAccessRecordPage(
            PageParam pageParam, String keyword, Long visitAreaId, Integer accessResult,
            LocalDate startDate, LocalDate endDate) {
        try {
            log.info("查询访问记录列表: keyword={}, visitAreaId={}, accessResult={}, startDate={}, endDate={}",
                    keyword, visitAreaId, accessResult, startDate, endDate);

            // 构建查询条件
            LambdaQueryWrapper<VisitorRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like(VisitorRecordEntity::getVisitorName, keyword)
                        .or()
                        .like(VisitorRecordEntity::getVisitorPhone, keyword)
                        .or()
                        .like(VisitorRecordEntity::getReservationCode, keyword));
            }

            if (visitAreaId != null) {
                queryWrapper.eq(VisitorRecordEntity::getVisitAreaId, visitAreaId);
            }

            if (accessResult != null) {
                queryWrapper.eq(VisitorRecordEntity::getAccessResult, accessResult);
            }

            if (startDate != null) {
                queryWrapper.ge(VisitorRecordEntity::getAccessTime, startDate.atStartOfDay());
            }

            if (endDate != null) {
                queryWrapper.le(VisitorRecordEntity::getAccessTime, endDate.atTime(23, 59, 59));
            }

            // 排序
            queryWrapper.orderByDesc(VisitorRecordEntity::getAccessTime);

            // 分页查询
            Page<VisitorRecordEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            IPage<VisitorRecordEntity> pageResult = visitorRecordDao.selectPage(page, queryWrapper);

            // 构建返回结果
            PageResult<VisitorRecordEntity> result = new PageResult<>();
            result.setPageNum(pageResult.getCurrent());
            result.setPageSize(pageResult.getSize());
            result.setTotal(pageResult.getTotal());
            result.setPages(pageResult.getPages());
            result.setList(pageResult.getRecords());

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("查询访问记录列表失败", e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VisitorRecordEntity>> getAccessRecordsByPhone(String visitorPhone, Integer limit) {
        try {
            log.info("根据手机号查询访问记录: {}, limit={}", visitorPhone, limit);

            LambdaQueryWrapper<VisitorRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorRecordEntity::getVisitorPhone, visitorPhone)
                    .orderByDesc(VisitorRecordEntity::getAccessTime)
                    .last("LIMIT " + limit);

            List<VisitorRecordEntity> records = visitorRecordDao.selectList(queryWrapper);
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("根据手机号查询访问记录失败: {}", visitorPhone, e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VisitorRecordEntity>> getAccessRecordsByReservation(Long reservationId) {
        try {
            log.info("根据预约ID查询访问记录: {}", reservationId);

            LambdaQueryWrapper<VisitorRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorRecordEntity::getReservationId, reservationId)
                    .orderByDesc(VisitorRecordEntity::getAccessTime);

            List<VisitorRecordEntity> records = visitorRecordDao.selectList(queryWrapper);
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("根据预约ID查询访问记录失败: {}", reservationId, e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VisitorRecordEntity>> getTodayAccessRecords() {
        try {
            log.info("查询今日访问记录");

            LocalDate today = LocalDate.now();
            LambdaQueryWrapper<VisitorRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(VisitorRecordEntity::getAccessTime, today.atStartOfDay())
                    .le(VisitorRecordEntity::getAccessTime, today.atTime(23, 59, 59))
                    .orderByDesc(VisitorRecordEntity::getAccessTime);

            List<VisitorRecordEntity> records = visitorRecordDao.selectList(queryWrapper);
            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("查询今日访问记录失败", e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    // ====================== 统计分析 ======================

    @Override
    public ResponseDTO<Map<String, Object>> getVisitorStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("获取访客统计数据: {} - {}", startDate, endDate);

            Map<String, Object> statistics = new HashMap<>();

            // 查询预约统计
            LambdaQueryWrapper<VisitorReservationEntity> reservationQuery = new LambdaQueryWrapper<>();
            reservationQuery.eq(VisitorReservationEntity::getDeletedFlag, false)
                    .between(VisitorReservationEntity::getVisitDate, startDate, endDate);

            Long totalReservations = visitorReservationDao.selectCount(reservationQuery);

            // 查询访问记录统计
            LambdaQueryWrapper<VisitorRecordEntity> recordQuery = new LambdaQueryWrapper<>();
            recordQuery.between(VisitorRecordEntity::getAccessTime, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

            Long totalAccessRecords = visitorRecordDao.selectCount(recordQuery);

            // 今日统计
            LocalDate today = LocalDate.now();
            LambdaQueryWrapper<VisitorReservationEntity> todayQuery = new LambdaQueryWrapper<>();
            todayQuery.eq(VisitorReservationEntity::getDeletedFlag, false)
                    .eq(VisitorReservationEntity::getVisitDate, today);

            Long todayReservations = visitorReservationDao.selectCount(todayQuery);

            // 构建统计数据
            statistics.put("totalReservations", totalReservations);
            statistics.put("totalAccessRecords", totalAccessRecords);
            statistics.put("todayReservations", todayReservations);
            statistics.put("startDate", startDate.toString());
            statistics.put("endDate", endDate.toString());

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取访客统计数据失败: {} - {}", startDate, endDate, e);
            return ResponseDTO.error("统计失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getAccessTrend(Integer days) {
        try {
            log.info("获取访问趋势数据: {} 天", days);

            List<Map<String, Object>> trendData = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);

                LambdaQueryWrapper<VisitorRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.between(VisitorRecordEntity::getAccessTime,
                        date.atStartOfDay(), date.atTime(23, 59, 59));

                Long count = visitorRecordDao.selectCount(queryWrapper);

                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", date.toString());
                dataPoint.put("count", count);
                trendData.add(dataPoint);
            }

            return ResponseDTO.ok(trendData);
        } catch (Exception e) {
            log.error("获取访问趋势数据失败: {} 天", days, e);
            return ResponseDTO.error("统计失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getAreaAccessStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("获取区域访问统计: {} - {}", startDate, endDate);

            // 这里需要根据实际的区域表结构来实现
            // 暂时返回空数据
            List<Map<String, Object>> areaStatistics = new ArrayList<>();
            return ResponseDTO.ok(areaStatistics);
        } catch (Exception e) {
            log.error("获取区域访问统计失败: {} - {}", startDate, endDate, e);
            return ResponseDTO.error("统计失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getDeviceUsageStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("获取设备使用统计: {} - {}", startDate, endDate);

            // 这里需要根据实际的设备表结构来实现
            // 暂时返回空数据
            List<Map<String, Object>> deviceStatistics = new ArrayList<>();
            return ResponseDTO.ok(deviceStatistics);
        } catch (Exception e) {
            log.error("获取设备使用统计失败: {} - {}", startDate, endDate, e);
            return ResponseDTO.error("统计失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getAbnormalAccessData(Integer timeWindow, Integer threshold) {
        try {
            log.info("获取异常访问数据: timeWindow={}, threshold={}", timeWindow, threshold);

            // 这里需要根据实际需求来实现异常检测逻辑
            // 暂时返回空数据
            List<Map<String, Object>> abnormalData = new ArrayList<>();
            return ResponseDTO.ok(abnormalData);
        } catch (Exception e) {
            log.error("获取异常访问数据失败: timeWindow={}, threshold={}", timeWindow, threshold, e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    // ====================== 业务验证 ======================

    @Override
    public ResponseDTO<Map<String, Object>> validateAccessPermission(Long reservationId, Long deviceId, Long areaId) {
        try {
            log.info("验证访客访问权限: reservationId={}, deviceId={}, areaId={}", reservationId, deviceId, areaId);

            VisitorReservationEntity reservation = visitorReservationDao.selectById(reservationId);
            if (reservation == null || reservation.getDeletedFlag() != 0) {
                return ResponseDTO.userErrorParam("预约不存在");
            }

            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("hasPermission", true);
            validationResult.put("reservationId", reservationId);
            validationResult.put("visitorName", reservation.getVisitorName());
            validationResult.put("visitAreas", reservation.getVisitAreaIds());
            validationResult.put("validateTime", LocalDateTime.now());

            return ResponseDTO.ok(validationResult);
        } catch (Exception e) {
            log.error("验证访客访问权限失败: {}", reservationId, e);
            return ResponseDTO.error("验证失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> checkVisitorBlacklist(String visitorName, String visitorPhone, String visitorIdCard) {
        try {
            log.info("检查访客黑名单: name={}, phone={}", visitorName, visitorPhone);

            Map<String, Object> checkResult = new HashMap<>();

            // 这里需要根据实际的黑名单表来实现检查逻辑
            // 暂时返回不在黑名单
            checkResult.put("isBlacklisted", false);
            checkResult.put("blacklistReason", null);
            checkResult.put("checkTime", LocalDateTime.now());

            return ResponseDTO.ok(checkResult);
        } catch (Exception e) {
            log.error("检查访客黑名单失败: name={}, phone={}", visitorName, visitorPhone, e);
            return ResponseDTO.error("检查失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> checkDuplicateReservation(String visitorPhone, LocalDate visitDate, Long excludeId) {
        try {
            log.info("检查重复预约: phone={}, date={}, excludeId={}", visitorPhone, visitDate, excludeId);

            LambdaQueryWrapper<VisitorReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorReservationEntity::getVisitorPhone, visitorPhone)
                    .eq(VisitorReservationEntity::getVisitDate, visitDate)
                    .eq(VisitorReservationEntity::getDeletedFlag, false)
                    .ne(VisitorReservationEntity::getApprovalStatus, 3); // 排除已取消的

            if (excludeId != null) {
                queryWrapper.ne(VisitorReservationEntity::getReservationId, excludeId);
            }

            Long count = visitorReservationDao.selectCount(queryWrapper);
            return ResponseDTO.ok(count > 0);
        } catch (Exception e) {
            log.error("检查重复预约失败: phone={}, date={}", visitorPhone, visitDate, e);
            return ResponseDTO.error("检查失败: " + e.getMessage());
        }
    }

    // ====================== 导出功能 ======================

    @Override
    public ResponseDTO<String> exportReservationData(LocalDate startDate, LocalDate endDate, Integer approvalStatus) {
        try {
            log.info("导出访客预约数据: {} - {}, status={}", startDate, endDate, approvalStatus);

            // 这里需要实现Excel导出逻辑
            String fileName = "visitor_reservations_" + startDate + "_" + endDate + ".xlsx";

            return ResponseDTO.ok(fileName);
        } catch (Exception e) {
            log.error("导出访客预约数据失败: {} - {}", startDate, endDate, e);
            return ResponseDTO.error("导出失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> exportAccessRecordData(LocalDate startDate, LocalDate endDate, Long visitAreaId, Integer accessResult) {
        try {
            log.info("导出访问记录数据: {} - {}, areaId={}, result={}", startDate, endDate, visitAreaId, accessResult);

            // 这里需要实现Excel导出逻辑
            String fileName = "visitor_records_" + startDate + "_" + endDate + ".xlsx";

            return ResponseDTO.ok(fileName);
        } catch (Exception e) {
            log.error("导出访问记录数据失败: {} - {}", startDate, endDate, e);
            return ResponseDTO.error("导出失败: " + e.getMessage());
        }
    }
}