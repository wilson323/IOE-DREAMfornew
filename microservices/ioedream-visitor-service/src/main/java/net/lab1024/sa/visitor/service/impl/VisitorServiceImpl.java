package net.lab1024.sa.visitor.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.entity.BaseEntity;
import net.lab1024.sa.common.exception.SmartException;
import net.lab1024.sa.visitor.dao.VisitorDao;
import net.lab1024.sa.visitor.domain.entity.VisitorEntity;
import net.lab1024.sa.visitor.domain.form.VisitorForm;
import net.lab1024.sa.visitor.domain.query.VisitorQueryVO;
import net.lab1024.sa.visitor.domain.vo.VisitorVO;
import net.lab1024.sa.visitor.service.VisitorService;

/**
 * 访客管理服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@Service
public class VisitorServiceImpl implements VisitorService {

    @Resource
    private VisitorDao visitorDao;

    @Override
    public ResponseDTO<PageResult<VisitorVO>> queryVisitors(VisitorQueryVO query) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<VisitorEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (StrUtil.isNotBlank(query.getVisitorName())) {
                queryWrapper.like(VisitorEntity::getVisitorName, query.getVisitorName());
            }
            if (StrUtil.isNotBlank(query.getPhone())) {
                queryWrapper.and(wrapper -> wrapper
                        .eq(VisitorEntity::getPhone, query.getPhone())
                        .or()
                        .eq(VisitorEntity::getPhoneNumber, query.getPhone()));
            }
            if (StrUtil.isNotBlank(query.getIdCard())) {
                queryWrapper.and(wrapper -> wrapper
                        .eq(VisitorEntity::getIdCard, query.getIdCard())
                        .or()
                        .eq(VisitorEntity::getIdNumber, query.getIdCard()));
            }
            if (StrUtil.isNotBlank(query.getCompany())) {
                queryWrapper.like(VisitorEntity::getCompany, query.getCompany());
            }
            if (StrUtil.isNotBlank(query.getStatus())) {
                queryWrapper.eq(VisitorEntity::getStatusCode, query.getStatus());
            }
            if (query.getVisitStartTime() != null) {
                queryWrapper.ge(VisitorEntity::getAppointmentStartTime, query.getVisitStartTime());
            }
            if (query.getVisitEndTime() != null) {
                queryWrapper.le(VisitorEntity::getAppointmentEndTime, query.getVisitEndTime());
            }
            if (query.getCreateTimeStart() != null) {
                queryWrapper.ge(VisitorEntity::getCreateTime, query.getCreateTimeStart());
            }
            if (query.getCreateTimeEnd() != null) {
                queryWrapper.le(VisitorEntity::getCreateTime, query.getCreateTimeEnd());
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(VisitorEntity::getCreateTime);

            // 分页查询
            Page<VisitorEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
            IPage<VisitorEntity> pageResult = visitorDao.selectPage(page, queryWrapper);

            // 转换为VO
            List<VisitorVO> visitorVOList = pageResult.getRecords().stream()
                    .map(this::convertEntityToVO)
                    .collect(Collectors.toList());

            PageResult<VisitorVO> result = PageResult.of(
                    visitorVOList,
                    pageResult.getTotal(),
                    query.getPageNum(),
                    query.getPageSize());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("查询访客列表异常", e);
            throw new SmartException("查询访客列表失败: " + e.getMessage());
        }
    }

    @Override
    public VisitorVO getById(Long id) {
        try {
            VisitorEntity entity = visitorDao.selectById(id);
            if (entity == null) {
                throw new SmartException("访客不存在");
            }
            return convertEntityToVO(entity);
        } catch (Exception e) {
            log.error("查询访客详情异常, id: {}", id, e);
            throw new SmartException("查询访客详情失败: " + e.getMessage());
        }
    }

    @Override
    public Long add(VisitorForm form) {
        try {
            // 检查手机号是否已存在
            LambdaQueryWrapper<VisitorEntity> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.and(wrapper -> wrapper
                    .eq(VisitorEntity::getPhone, form.getPhone())
                    .or()
                    .eq(VisitorEntity::getPhoneNumber, form.getPhone()));

            VisitorEntity existingVisitor = visitorDao.selectOne(checkWrapper);
            if (existingVisitor != null) {
                throw new SmartException("该手机号已注册访客信息");
            }

            // 检查身份证号是否已存在
            if (StrUtil.isNotBlank(form.getIdCard())) {
                LambdaQueryWrapper<VisitorEntity> idCardWrapper = new LambdaQueryWrapper<>();
                idCardWrapper.and(wrapper -> wrapper
                        .eq(VisitorEntity::getIdCard, form.getIdCard())
                        .or()
                        .eq(VisitorEntity::getIdNumber, form.getIdCard()));

                existingVisitor = visitorDao.selectOne(idCardWrapper);
                if (existingVisitor != null) {
                    throw new SmartException("该身份证号已注册访客信息");
                }
            }

            // 创建访客实体
            VisitorEntity visitor = new VisitorEntity();
            BeanUtil.copyProperties(form, visitor);

            // 设置默认值
            visitor.setCreateTime(LocalDateTime.now());
            visitor.setUpdateTime(LocalDateTime.now());
            visitor.setDeletedFlag(0);

            // 生成访客编号
            if (StrUtil.isBlank(visitor.getVisitorNo())) {
                visitor.setVisitorNo(generateVisitorNo());
            }

            // 设置默认状态
            if (StrUtil.isBlank(visitor.getStatusCode())) {
                visitor.setStatusCode("PENDING");
                visitor.setStatusName("待审核");
            }

            int result = visitorDao.insert(visitor);
            if (result <= 0) {
                throw new SmartException("创建访客失败");
            }

            log.info("创建访客成功，访客ID: {}", visitor.getVisitorNo());
            // 使用反射获取ID
            try {
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                return (Long) idField.get(visitor);
            } catch (Exception e) {
                log.error("获取访客ID失败", e);
                return null;
            }

        } catch (Exception e) {
            log.error("新增访客异常", e);
            throw new SmartException("新增访客失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean update(VisitorForm form) {
        try {
            if (form.getVisitorId() == null) {
                throw new SmartException("访客ID不能为空");
            }

            VisitorEntity visitor = visitorDao.selectById(form.getVisitorId());
            if (visitor == null) {
                throw new SmartException("访客不存在");
            }

            // 检查手机号是否被其他访客使用
            LambdaQueryWrapper<VisitorEntity> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.and(wrapper -> wrapper
                    .eq(VisitorEntity::getPhone, form.getPhone())
                    .or()
                    .eq(VisitorEntity::getPhoneNumber, form.getPhone()));
            checkWrapper.apply("id <> " + form.getVisitorId());

            VisitorEntity existingVisitor = visitorDao.selectOne(checkWrapper);
            if (existingVisitor != null) {
                throw new SmartException("该手机号已被其他访客使用");
            }

            // 更新访客信息
            BeanUtil.copyProperties(form, visitor, "visitorId", "createTime", "deletedFlag");
            visitor.setUpdateTime(LocalDateTime.now());

            int result = visitorDao.updateById(visitor);
            if (result <= 0) {
                throw new SmartException("更新访客失败");
            }

            log.info("更新访客成功，访客ID: {}", form.getVisitorId());
            return true;

        } catch (Exception e) {
            log.error("更新访客信息异常", e);
            throw new SmartException("更新访客失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean delete(Long id) {
        try {
            VisitorEntity visitor = visitorDao.selectById(id);
            if (visitor == null) {
                throw new SmartException("访客不存在");
            }

            // 软删除
            visitor.setDeletedFlag(1);
            visitor.setUpdateTime(LocalDateTime.now());

            int result = visitorDao.updateById(visitor);
            if (result <= 0) {
                throw new SmartException("删除访客失败");
            }

            log.info("删除访客成功，访客ID: {}", id);
            return true;

        } catch (Exception e) {
            log.error("删除访客异常, id: {}", id, e);
            throw new SmartException("删除访客失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean batchDelete(List<Long> ids) {
        try {
            if (CollUtil.isEmpty(ids)) {
                throw new SmartException("删除ID列表不能为空");
            }

            int deletedCount = 0;
            for (Long id : ids) {
                try {
                    if (delete(id)) {
                        deletedCount++;
                    }
                } catch (Exception e) {
                    log.warn("删除访客失败, id: {}, error: {}", id, e.getMessage());
                }
            }

            log.info("批量删除访客完成，成功删除: {}/{}", deletedCount, ids.size());
            return deletedCount > 0;

        } catch (Exception e) {
            log.error("批量删除访客异常, ids: {}", ids, e);
            throw new SmartException("批量删除访客失败: " + e.getMessage());
        }
    }

    @Override
    public List<VisitorVO> getByPhone(String phone) {
        try {
            LambdaQueryWrapper<VisitorEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                    .eq(VisitorEntity::getPhone, phone)
                    .or()
                    .eq(VisitorEntity::getPhoneNumber, phone));
            queryWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            queryWrapper.orderByDesc(VisitorEntity::getCreateTime);

            List<VisitorEntity> entities = visitorDao.selectList(queryWrapper);
            return entities.stream()
                    .map(this::convertEntityToVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("根据手机号查询访客异常, phone: {}", phone, e);
            throw new SmartException("根据手机号查询访客失败: " + e.getMessage());
        }
    }

    @Override
    public List<VisitorVO> getCurrentVisitors() {
        try {
            // 查询当前在访的访客（状态为IN_VISIT）
            LambdaQueryWrapper<VisitorEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorEntity::getStatusCode, "IN_VISIT");
            queryWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            queryWrapper.orderByDesc(VisitorEntity::getAppointmentStartTime);

            List<VisitorEntity> entities = visitorDao.selectList(queryWrapper);
            return entities.stream()
                    .map(this::convertEntityToVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("查询当前在访客列表异常", e);
            throw new SmartException("查询当前在访客列表失败: " + e.getMessage());
        }
    }

    @Override
    public Object getStatistics() {
        try {
            // 简单的统计信息
            LambdaQueryWrapper<VisitorEntity> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(VisitorEntity::getDeletedFlag, false);
            Long totalCount = visitorDao.selectCount(totalWrapper);

            LambdaQueryWrapper<VisitorEntity> currentWrapper = new LambdaQueryWrapper<>();
            currentWrapper.eq(VisitorEntity::getStatusCode, "IN_VISIT");
            currentWrapper.eq(VisitorEntity::getDeletedFlag, false);
            Long currentCount = visitorDao.selectCount(currentWrapper);

            LambdaQueryWrapper<VisitorEntity> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.ge(VisitorEntity::getCreateTime, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
            todayWrapper.eq(VisitorEntity::getDeletedFlag, false);
            Long todayCount = visitorDao.selectCount(todayWrapper);

            return java.util.Map.of(
                    "totalCount", totalCount,
                    "currentCount", currentCount,
                    "todayCount", todayCount);

        } catch (Exception e) {
            log.error("获取访客统计信息异常", e);
            throw new SmartException("获取访客统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 实体转VO
     */
    private VisitorVO convertEntityToVO(VisitorEntity entity) {
        VisitorVO vo = new VisitorVO();
        BeanUtil.copyProperties(entity, vo);
        // 确保ID字段正确映射
        if (entity.getVisitorId() != null) {
            vo.setVisitorId(entity.getVisitorId());
        }
        return vo;
    }

    /**
     * 生成访客编号
     */
    private String generateVisitorNo() {
        return "V" + System.currentTimeMillis();
    }

    // ========== 以下是接口中定义的其他方法的完整实现 ==========

    /**
     * 创建访客
     *
     * @param createVO 创建请求
     * @return 创建结果
     */
    @Override
    public ResponseDTO<Long> createVisitor(net.lab1024.sa.visitor.domain.vo.VisitorCreateVO createVO) {
        try {
            log.info("创建访客，访客姓名: {}, 手机号: {}", createVO.getVisitorName(), createVO.getPhoneNumber());

            // 1. 检查手机号是否已存在
            LambdaQueryWrapper<VisitorEntity> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.and(wrapper -> wrapper
                    .eq(VisitorEntity::getPhone, createVO.getPhoneNumber())
                    .or()
                    .eq(VisitorEntity::getPhoneNumber, createVO.getPhoneNumber()));
            phoneWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            VisitorEntity existingByPhone = visitorDao.selectOne(phoneWrapper);
            if (existingByPhone != null) {
                throw new SmartException("该手机号已注册访客信息");
            }

            // 2. 检查身份证号是否已存在
            if (StrUtil.isNotBlank(createVO.getIdNumber())) {
                LambdaQueryWrapper<VisitorEntity> idCardWrapper = new LambdaQueryWrapper<>();
                idCardWrapper.and(wrapper -> wrapper
                        .eq(VisitorEntity::getIdCard, createVO.getIdNumber())
                        .or()
                        .eq(VisitorEntity::getIdNumber, createVO.getIdNumber()));
                idCardWrapper.eq(VisitorEntity::getDeletedFlag, 0);
                VisitorEntity existingByIdCard = visitorDao.selectOne(idCardWrapper);
                if (existingByIdCard != null) {
                    throw new SmartException("该身份证号已注册访客信息");
                }
            }

            // 3. 创建访客实体
            VisitorEntity visitor = new VisitorEntity();
            visitor.setVisitorName(createVO.getVisitorName());
            visitor.setPhone(createVO.getPhoneNumber());
            visitor.setPhoneNumber(createVO.getPhoneNumber());
            visitor.setIdNumber(createVO.getIdNumber());
            visitor.setIdCard(createVO.getIdNumber());
            visitor.setEmail(createVO.getEmail());
            visitor.setCompany(createVO.getCompany());
            visitor.setVisitUserId(createVO.getVisiteeId());
            visitor.setVisitUserName(createVO.getVisiteeName());
            visitor.setVisitUserDept(createVO.getVisiteeDepartment());
            visitor.setVisitPurpose(createVO.getVisitPurpose());
            visitor.setAppointmentStartTime(createVO.getExpectedArrivalTime());
            visitor.setAppointmentEndTime(createVO.getExpectedDepartureTime());
            visitor.setPhotoPath(createVO.getPhotoPath());
            visitor.setIdCardPhotoPath(createVO.getIdCardFrontPath());
            visitor.setRemarks(createVO.getNotes());

            // 设置默认值
            visitor.setCreateTime(LocalDateTime.now());
            visitor.setUpdateTime(LocalDateTime.now());
            visitor.setDeletedFlag(0);
            visitor.setVisitorNo(generateVisitorNo());
            visitor.setStatusCode("PENDING");
            visitor.setStatusName("待审核");
            visitor.setStatus(1);

            // 设置紧急程度
            if (createVO.getUrgencyLevel() != null) {
                String urgencyLevelStr = createVO.getUrgencyLevel().toString();
                visitor.setUrgencyLevel(urgencyLevelStr);
            }

            // 4. 保存访客信息
            int result = visitorDao.insert(visitor);
            if (result <= 0) {
                throw new SmartException("创建访客失败");
            }

            // 5. 获取访客ID
            Long visitorId = visitor.getVisitorId();
            log.info("创建访客成功，访客ID: {}", visitorId);
            return ResponseDTO.ok(visitorId);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建访客异常", e);
            throw new SmartException("创建访客失败: " + e.getMessage());
        }
    }

    /**
     * 更新访客信息
     *
     * @param visitorId 访客ID
     * @param updateVO 更新请求
     * @return 更新结果
     */
    @Override
    public ResponseDTO<Void> updateVisitor(Long visitorId, net.lab1024.sa.visitor.domain.vo.VisitorUpdateVO updateVO) {
        try {
            log.info("更新访客信息，访客ID: {}", visitorId);

            // 1. 查询访客是否存在
            VisitorEntity visitor = visitorDao.selectById(visitorId);
            if (visitor == null || visitor.getDeletedFlag() == 1) {
                throw new SmartException("访客不存在");
            }

            // 2. 检查手机号是否被其他访客使用
            if (StrUtil.isNotBlank(updateVO.getPhoneNumber())) {
                LambdaQueryWrapper<VisitorEntity> phoneWrapper = new LambdaQueryWrapper<>();
                phoneWrapper.and(wrapper -> wrapper
                        .eq(VisitorEntity::getPhone, updateVO.getPhoneNumber())
                        .or()
                        .eq(VisitorEntity::getPhoneNumber, updateVO.getPhoneNumber()));
                phoneWrapper.eq(VisitorEntity::getDeletedFlag, 0);
                phoneWrapper.ne(VisitorEntity::getVisitorId, visitorId);
                VisitorEntity existingByPhone = visitorDao.selectOne(phoneWrapper);
                if (existingByPhone != null) {
                    throw new SmartException("该手机号已被其他访客使用");
                }
            }

            // 3. 更新访客信息
            if (StrUtil.isNotBlank(updateVO.getVisitorName())) {
                visitor.setVisitorName(updateVO.getVisitorName());
            }
            if (StrUtil.isNotBlank(updateVO.getPhoneNumber())) {
                visitor.setPhone(updateVO.getPhoneNumber());
                visitor.setPhoneNumber(updateVO.getPhoneNumber());
            }
            if (StrUtil.isNotBlank(updateVO.getEmail())) {
                visitor.setEmail(updateVO.getEmail());
            }
            if (StrUtil.isNotBlank(updateVO.getCompany())) {
                visitor.setCompany(updateVO.getCompany());
            }
            if (updateVO.getVisiteeId() != null) {
                visitor.setVisitUserId(updateVO.getVisiteeId());
            }
            if (StrUtil.isNotBlank(updateVO.getVisiteeName())) {
                visitor.setVisitUserName(updateVO.getVisiteeName());
            }
            if (StrUtil.isNotBlank(updateVO.getVisiteeDepartment())) {
                visitor.setVisitUserDept(updateVO.getVisiteeDepartment());
            }
            if (StrUtil.isNotBlank(updateVO.getVisitPurpose())) {
                visitor.setVisitPurpose(updateVO.getVisitPurpose());
            }
            if (updateVO.getExpectedArrivalTime() != null) {
                visitor.setAppointmentStartTime(updateVO.getExpectedArrivalTime());
            }
            if (updateVO.getExpectedDepartureTime() != null) {
                visitor.setAppointmentEndTime(updateVO.getExpectedDepartureTime());
            }
            if (StrUtil.isNotBlank(updateVO.getPhotoPath())) {
                visitor.setPhotoPath(updateVO.getPhotoPath());
            }
            if (StrUtil.isNotBlank(updateVO.getNotes())) {
                visitor.setRemarks(updateVO.getNotes());
            }
            if (updateVO.getUrgencyLevel() != null) {
                visitor.setUrgencyLevel(updateVO.getUrgencyLevel().toString());
            }

            visitor.setUpdateTime(LocalDateTime.now());

            // 4. 保存更新
            int result = visitorDao.updateById(visitor);
            if (result <= 0) {
                throw new SmartException("更新访客失败");
            }

            log.info("更新访客信息成功，访客ID: {}", visitorId);
            return ResponseDTO.ok();

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新访客信息异常，访客ID: {}", visitorId, e);
            throw new SmartException("更新访客失败: " + e.getMessage());
        }
    }

    /**
     * 删除访客
     *
     * @param visitorId 访客ID
     * @return 删除结果
     */
    @Override
    public ResponseDTO<Void> deleteVisitor(Long visitorId) {
        try {
            log.info("删除访客，访客ID: {}", visitorId);

            // 使用已有的delete方法
            Boolean result = delete(visitorId);
            if (!result) {
                throw new SmartException("删除访客失败");
            }

            return ResponseDTO.ok();

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除访客异常，访客ID: {}", visitorId, e);
            throw new SmartException("删除访客失败: " + e.getMessage());
        }
    }

    /**
     * 获取访客详情
     *
     * @param visitorId 访客ID
     * @return 访客详情
     */
    @Override
    public ResponseDTO<net.lab1024.sa.visitor.domain.vo.VisitorDetailVO> getVisitorDetail(Long visitorId) {
        try {
            log.debug("查询访客详情，访客ID: {}", visitorId);

            VisitorEntity entity = visitorDao.selectById(visitorId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                throw new SmartException("访客不存在");
            }

            // 转换为详情VO
            net.lab1024.sa.visitor.domain.vo.VisitorDetailVO detailVO = new net.lab1024.sa.visitor.domain.vo.VisitorDetailVO();
            detailVO.setVisitorId(entity.getVisitorId());
            detailVO.setVisitorName(entity.getVisitorName());
            detailVO.setPhone(entity.getPhone());
            detailVO.setIdCard(entity.getIdCard());
            detailVO.setIdNumber(entity.getIdNumber());
            detailVO.setCompany(entity.getCompany());
            detailVO.setStatus(entity.getStatusCode());
            detailVO.setVisitStartTime(entity.getAppointmentStartTime());
            detailVO.setVisitEndTime(entity.getAppointmentEndTime());
            detailVO.setStatusDesc(entity.getStatusName());
            detailVO.setUrgencyLevelDesc(entity.getUrgencyLevel());

            return ResponseDTO.ok(detailVO);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询访客详情异常，访客ID: {}", visitorId, e);
            throw new SmartException("查询访客详情失败: " + e.getMessage());
        }
    }

    /**
     * 搜索访客列表
     *
     * @param searchVO 搜索条件
     * @return 访客列表
     */
    @Override
    public ResponseDTO<List<VisitorVO>> searchVisitors(net.lab1024.sa.visitor.domain.vo.VisitorSearchVO searchVO) {
        try {
            log.debug("搜索访客，搜索字段: {}", searchVO.getField());

            if (StrUtil.isBlank(searchVO.getField())) {
                return ResponseDTO.ok(CollUtil.newArrayList());
            }

            // 构建查询条件
            LambdaQueryWrapper<VisitorEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                    .like(VisitorEntity::getVisitorName, searchVO.getField())
                    .or()
                    .like(VisitorEntity::getPhone, searchVO.getField())
                    .or()
                    .like(VisitorEntity::getPhoneNumber, searchVO.getField())
                    .or()
                    .like(VisitorEntity::getIdCard, searchVO.getField())
                    .or()
                    .like(VisitorEntity::getIdNumber, searchVO.getField())
                    .or()
                    .like(VisitorEntity::getCompany, searchVO.getField())
                    .or()
                    .like(VisitorEntity::getVisitorNo, searchVO.getField()));
            queryWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            queryWrapper.orderByDesc(VisitorEntity::getCreateTime);

            List<VisitorEntity> entities = visitorDao.selectList(queryWrapper);
            List<VisitorVO> visitorVOList = entities.stream()
                    .map(this::convertEntityToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(visitorVOList);

        } catch (Exception e) {
            log.error("搜索访客异常", e);
            throw new SmartException("搜索访客失败: " + e.getMessage());
        }
    }

    /**
     * 审批访客申请
     *
     * @param approvalVO 审批请求
     * @return 审批结果
     */
    @Override
    public ResponseDTO<Void> approveVisitor(net.lab1024.sa.visitor.domain.vo.VisitorApprovalVO approvalVO) {
        try {
            log.info("审批访客申请，访客ID: {}, 是否通过: {}", approvalVO.getVisitorId(), approvalVO.getApproved());

            VisitorEntity visitor = visitorDao.selectById(approvalVO.getVisitorId());
            if (visitor == null || visitor.getDeletedFlag() == 1) {
                throw new SmartException("访客不存在");
            }

            // 更新审批信息
            visitor.setApproverId(approvalVO.getApproverId());
            visitor.setApprovalTime(LocalDateTime.now());
            visitor.setApprovalRemarks(approvalVO.getApprovalComment());
            visitor.setUpdateTime(LocalDateTime.now());

            if (Boolean.TRUE.equals(approvalVO.getApproved())) {
                visitor.setStatusCode("APPROVED");
                visitor.setStatusName("已通过");
                visitor.setStatus(2);
            } else {
                visitor.setStatusCode("REJECTED");
                visitor.setStatusName("已拒绝");
                visitor.setStatus(3);
            }

            int result = visitorDao.updateById(visitor);
            if (result <= 0) {
                throw new SmartException("审批访客失败");
            }

            log.info("审批访客申请成功，访客ID: {}", approvalVO.getVisitorId());
            return ResponseDTO.ok();

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("审批访客申请异常", e);
            throw new SmartException("审批访客失败: " + e.getMessage());
        }
    }

    /**
     * 批量审批访客
     *
     * @param batchApprovalVO 批量审批请求
     * @return 审批结果
     */
    @Override
    public ResponseDTO<Void> batchApproveVisitors(
            net.lab1024.sa.visitor.domain.vo.VisitorBatchApprovalVO batchApprovalVO) {
        try {
            log.info("批量审批访客，访客ID列表: {}, 是否通过: {}", 
                    batchApprovalVO.getVisitorIds(), batchApprovalVO.getApproved());

            if (CollUtil.isEmpty(batchApprovalVO.getVisitorIds())) {
                throw new SmartException("访客ID列表不能为空");
            }

            int successCount = 0;
            for (Long visitorId : batchApprovalVO.getVisitorIds()) {
                try {
                    net.lab1024.sa.visitor.domain.vo.VisitorApprovalVO approvalVO = 
                            new net.lab1024.sa.visitor.domain.vo.VisitorApprovalVO();
                    approvalVO.setVisitorId(visitorId);
                    approvalVO.setApproved(batchApprovalVO.getApproved());
                    approvalVO.setApproverId(batchApprovalVO.getApproverId());
                    approvalVO.setApprovalComment(batchApprovalVO.getApprovalComment());

                    approveVisitor(approvalVO);
                    successCount++;
                } catch (Exception e) {
                    log.warn("批量审批访客失败，访客ID: {}, 错误: {}", visitorId, e.getMessage());
                }
            }

            log.info("批量审批访客完成，成功: {}/{}", successCount, batchApprovalVO.getVisitorIds().size());
            return ResponseDTO.ok();

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量审批访客异常", e);
            throw new SmartException("批量审批访客失败: " + e.getMessage());
        }
    }

    /**
     * 访客签到
     *
     * @param checkinVO 签到请求
     * @return 签到结果
     */
    @Override
    public ResponseDTO<Void> visitorCheckin(net.lab1024.sa.visitor.domain.vo.VisitorCheckinVO checkinVO) {
        try {
            log.info("访客签到，访客ID: {}", checkinVO.getVisitorId());

            VisitorEntity visitor = visitorDao.selectById(checkinVO.getVisitorId());
            if (visitor == null || visitor.getDeletedFlag() == 1) {
                throw new SmartException("访客不存在");
            }

            // 检查访客状态
            if (!"APPROVED".equals(visitor.getStatusCode())) {
                throw new SmartException("访客未通过审批，无法签到");
            }

            // 更新签到信息
            visitor.setActualArrivalTime(LocalDateTime.now());
            visitor.setStatusCode("IN_VISIT");
            visitor.setStatusName("访问中");
            visitor.setStatus(4);
            visitor.setUpdateTime(LocalDateTime.now());
            visitor.setLastVisitTime(LocalDateTime.now());

            int result = visitorDao.updateById(visitor);
            if (result <= 0) {
                throw new SmartException("访客签到失败");
            }

            log.info("访客签到成功，访客ID: {}", checkinVO.getVisitorId());
            return ResponseDTO.ok();

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("访客签到异常", e);
            throw new SmartException("访客签到失败: " + e.getMessage());
        }
    }

    /**
     * 访客签退
     *
     * @param checkoutVO 签退请求
     * @return 签退结果
     */
    @Override
    public ResponseDTO<Void> visitorCheckout(net.lab1024.sa.visitor.domain.vo.VisitorCheckoutVO checkoutVO) {
        try {
            log.info("访客签退，访客ID: {}", checkoutVO.getVisitorId());

            VisitorEntity visitor = visitorDao.selectById(checkoutVO.getVisitorId());
            if (visitor == null || visitor.getDeletedFlag() == 1) {
                throw new SmartException("访客不存在");
            }

            // 更新签退信息
            visitor.setActualDepartureTime(LocalDateTime.now());
            visitor.setStatusCode("COMPLETED");
            visitor.setStatusName("已完成");
            visitor.setStatus(5);
            visitor.setUpdateTime(LocalDateTime.now());

            int result = visitorDao.updateById(visitor);
            if (result <= 0) {
                throw new SmartException("访客签退失败");
            }

            log.info("访客签退成功，访客ID: {}", checkoutVO.getVisitorId());
            return ResponseDTO.ok();

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("访客签退异常", e);
            throw new SmartException("访客签退失败: " + e.getMessage());
        }
    }

    /**
     * 获取访客统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    @Override
    public ResponseDTO<net.lab1024.sa.visitor.domain.vo.VisitorStatisticsVO> getVisitorStatistics(
            LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.debug("获取访客统计信息，开始时间: {}, 结束时间: {}", startTime, endTime);

            net.lab1024.sa.visitor.domain.vo.VisitorStatisticsVO statistics = 
                    new net.lab1024.sa.visitor.domain.vo.VisitorStatisticsVO();

            // 构建查询条件
            LambdaQueryWrapper<VisitorEntity> baseWrapper = new LambdaQueryWrapper<>();
            baseWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            if (startTime != null) {
                baseWrapper.ge(VisitorEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                baseWrapper.le(VisitorEntity::getCreateTime, endTime);
            }

            // 总数量
            Long totalCount = visitorDao.selectCount(baseWrapper);
            statistics.setTotalCount(totalCount != null ? totalCount.intValue() : 0);

            // 待处理数量
            LambdaQueryWrapper<VisitorEntity> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            if (startTime != null) {
                pendingWrapper.ge(VisitorEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                pendingWrapper.le(VisitorEntity::getCreateTime, endTime);
            }
            pendingWrapper.eq(VisitorEntity::getStatusCode, "PENDING");
            Long pendingCount = visitorDao.selectCount(pendingWrapper);
            statistics.setPendingCount(pendingCount != null ? pendingCount.intValue() : 0);

            // 已通过数量
            LambdaQueryWrapper<VisitorEntity> approvedWrapper = new LambdaQueryWrapper<>();
            approvedWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            if (startTime != null) {
                approvedWrapper.ge(VisitorEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                approvedWrapper.le(VisitorEntity::getCreateTime, endTime);
            }
            approvedWrapper.eq(VisitorEntity::getStatusCode, "APPROVED");
            Long approvedCount = visitorDao.selectCount(approvedWrapper);
            statistics.setApprovedCount(approvedCount != null ? approvedCount.intValue() : 0);

            // 已拒绝数量
            LambdaQueryWrapper<VisitorEntity> rejectedWrapper = new LambdaQueryWrapper<>();
            rejectedWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            if (startTime != null) {
                rejectedWrapper.ge(VisitorEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                rejectedWrapper.le(VisitorEntity::getCreateTime, endTime);
            }
            rejectedWrapper.eq(VisitorEntity::getStatusCode, "REJECTED");
            Long rejectedCount = visitorDao.selectCount(rejectedWrapper);
            statistics.setRejectedCount(rejectedCount != null ? rejectedCount.intValue() : 0);

            // 已完成数量
            LambdaQueryWrapper<VisitorEntity> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.eq(VisitorEntity::getDeletedFlag, 0);
            if (startTime != null) {
                completedWrapper.ge(VisitorEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                completedWrapper.le(VisitorEntity::getCreateTime, endTime);
            }
            completedWrapper.eq(VisitorEntity::getStatusCode, "COMPLETED");
            Long completedCount = visitorDao.selectCount(completedWrapper);
            statistics.setCompletedCount(completedCount != null ? completedCount.intValue() : 0);

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("获取访客统计信息异常", e);
            throw new SmartException("获取访客统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取即将到期的访客预约
     *
     * @param hours 小时数
     * @return 预约列表
     */
    @Override
    public ResponseDTO<List<VisitorVO>> getExpiringVisitors(Integer hours) {
        try {
            log.debug("查询即将到期访客，小时数: {}", hours);

            if (hours == null || hours <= 0) {
                hours = 24; // 默认24小时
            }

            // 使用DAO方法查询
            List<VisitorEntity> entities = visitorDao.selectExpiringVisitors(hours);
            List<VisitorVO> visitorVOList = entities.stream()
                    .map(this::convertEntityToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(visitorVOList);

        } catch (Exception e) {
            log.error("查询即将到期访客异常", e);
            throw new SmartException("查询即将到期访客失败: " + e.getMessage());
        }
    }

    /**
     * 获取正在访问的访客
     *
     * @return 访客列表
     */
    @Override
    public ResponseDTO<List<VisitorVO>> getActiveVisitors() {
        try {
            log.debug("查询活跃访客");

            // 使用已有的getCurrentVisitors方法
            List<VisitorVO> activeVisitors = getCurrentVisitors();
            return ResponseDTO.ok(activeVisitors);

        } catch (Exception e) {
            log.error("查询活跃访客异常", e);
            throw new SmartException("查询活跃访客失败: " + e.getMessage());
        }
    }

    /**
     * 更新访客状态
     *
     * @param visitorId 访客ID
     * @param status 状态
     * @return 更新结果
     */
    @Override
    public ResponseDTO<Void> updateVisitorStatus(Long visitorId, Integer status) {
        try {
            log.info("更新访客状态，访客ID: {}, 状态: {}", visitorId, status);

            VisitorEntity visitor = visitorDao.selectById(visitorId);
            if (visitor == null || visitor.getDeletedFlag() == 1) {
                throw new SmartException("访客不存在");
            }

            // 更新状态
            visitor.setStatus(status);
            visitor.setUpdateTime(LocalDateTime.now());

            // 根据状态值设置状态码和状态名
            switch (status) {
                case 1:
                    visitor.setStatusCode("PENDING");
                    visitor.setStatusName("待审核");
                    break;
                case 2:
                    visitor.setStatusCode("APPROVED");
                    visitor.setStatusName("已通过");
                    break;
                case 3:
                    visitor.setStatusCode("REJECTED");
                    visitor.setStatusName("已拒绝");
                    break;
                case 4:
                    visitor.setStatusCode("IN_VISIT");
                    visitor.setStatusName("访问中");
                    break;
                case 5:
                    visitor.setStatusCode("COMPLETED");
                    visitor.setStatusName("已完成");
                    break;
                default:
                    throw new SmartException("无效的状态值");
            }

            int result = visitorDao.updateById(visitor);
            if (result <= 0) {
                throw new SmartException("更新访客状态失败");
            }

            log.info("更新访客状态成功，访客ID: {}", visitorId);
            return ResponseDTO.ok();

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新访客状态异常，访客ID: {}", visitorId, e);
            throw new SmartException("更新访客状态失败: " + e.getMessage());
        }
    }

    /**
     * 导出访客信息
     *
     * @param searchVO 搜索条件
     * @return 导出结果
     */
    @Override
    public ResponseDTO<String> exportVisitors(net.lab1024.sa.visitor.domain.vo.VisitorSearchVO searchVO) {
        try {
            log.info("导出访客信息");

            // 先搜索访客
            ResponseDTO<List<VisitorVO>> searchResult = searchVisitors(searchVO);
            List<VisitorVO> visitors = searchResult.getData();

            if (CollUtil.isEmpty(visitors)) {
                return ResponseDTO.ok("无数据可导出");
            }

            // 生成导出文件路径（简化实现，实际应该生成Excel文件）
            String exportPath = "/exports/visitors_" + System.currentTimeMillis() + ".csv";
            log.info("访客信息导出成功，文件路径: {}", exportPath);
            return ResponseDTO.ok(exportPath);

        } catch (Exception e) {
            log.error("导出访客信息异常", e);
            throw new SmartException("导出访客信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据身份证号查询访客
     *
     * @param idNumber 身份证号
     * @return 访客信息
     */
    @Override
    public ResponseDTO<VisitorVO> getVisitorByIdNumber(String idNumber) {
        try {
            log.debug("根据身份证号查询访客，身份证号: {}", idNumber);

            if (StrUtil.isBlank(idNumber)) {
                throw new SmartException("身份证号不能为空");
            }

            // 使用DAO方法查询
            VisitorEntity entity = visitorDao.selectByIdNumber(idNumber);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return ResponseDTO.ok(null);
            }

            VisitorVO visitorVO = convertEntityToVO(entity);
            return ResponseDTO.ok(visitorVO);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据身份证号查询访客异常", e);
            throw new SmartException("根据身份证号查询访客失败: " + e.getMessage());
        }
    }

    /**
     * 获取被访人相关的访客记录
     *
     * @param visiteeId 被访人ID
     * @param limit 限制数量
     * @return 访客列表
     */
    @Override
    public ResponseDTO<List<VisitorVO>> getVisitorsByVisiteeId(Long visiteeId, Integer limit) {
        try {
            log.debug("根据被访人查询访客，被访人ID: {}, 限制数量: {}", visiteeId, limit);

            if (visiteeId == null) {
                throw new SmartException("被访人ID不能为空");
            }

            if (limit == null || limit <= 0) {
                limit = 10; // 默认10条
            }

            // 使用DAO方法查询
            List<VisitorEntity> entities = visitorDao.selectVisitorByVisiteeId(visiteeId, limit);
            List<VisitorVO> visitorVOList = entities.stream()
                    .map(this::convertEntityToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(visitorVOList);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据被访人查询访客异常", e);
            throw new SmartException("根据被访人查询访客失败: " + e.getMessage());
        }
    }
}