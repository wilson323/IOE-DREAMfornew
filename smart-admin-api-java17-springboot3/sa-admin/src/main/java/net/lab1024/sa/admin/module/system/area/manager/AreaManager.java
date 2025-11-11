package net.lab1024.sa.admin.module.system.area.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.area.dao.AreaDao;
import net.lab1024.sa.admin.module.system.area.domain.entity.AreaEntity;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaQueryForm;
import net.lab1024.sa.admin.module.system.area.domain.vo.AreaTreeVO;
import net.lab1024.sa.admin.module.system.area.domain.vo.AreaVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 区域Manager
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Component
public class AreaManager {

    @Resource
    private AreaDao areaDao;

    /**
     * 分页查询区域
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    public PageResult<AreaVO> queryPage(AreaQueryForm queryForm) {
        Page<?> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        List<AreaVO> list = areaDao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 获取区域树
     *
     * @return 区域树
     */
    public List<AreaTreeVO> getAreaTree() {
        List<AreaVO> allAreas = areaDao.listAll();
        return buildAreaTree(allAreas, 0L);
    }

    /**
     * 构建区域树
     *
     * @param allAreas 所有区域
     * @param parentId 父区域ID
     * @return 区域树列表
     */
    private List<AreaTreeVO> buildAreaTree(List<AreaVO> allAreas, Long parentId) {
        return allAreas.stream()
                .filter(area -> parentId.equals(area.getParentId()))
                .map(area -> {
                    AreaTreeVO treeVO = convertToTreeVO(area);
                    // 递归查找子区域
                    List<AreaTreeVO> children = buildAreaTree(allAreas, area.getAreaId());
                    treeVO.setChildren(children);

                    // 收集所有子区域ID
                    List<Long> allChildrenIds = collectAllChildrenIds(children);
                    allChildrenIds.add(area.getAreaId());
                    treeVO.setSelfAndAllChildrenIdList(allChildrenIds);

                    return treeVO;
                })
                .sorted(Comparator.comparing(AreaTreeVO::getSortOrder))
                .collect(Collectors.toList());
    }

    /**
     * 将AreaVO转换为AreaTreeVO
     *
     * @param areaVO 区域VO
     * @return 区域树VO
     */
    private AreaTreeVO convertToTreeVO(AreaVO areaVO) {
        AreaTreeVO treeVO = new AreaTreeVO();
        treeVO.setAreaId(areaVO.getAreaId());
        treeVO.setAreaCode(areaVO.getAreaCode());
        treeVO.setAreaName(areaVO.getAreaName());
        treeVO.setAreaType(areaVO.getAreaType());
        treeVO.setAreaLevel(areaVO.getAreaLevel());
        treeVO.setParentId(areaVO.getParentId());
        treeVO.setSortOrder(areaVO.getSortOrder());
        treeVO.setAreaConfig(areaVO.getAreaConfig());
        treeVO.setAreaDesc(areaVO.getAreaDesc());
        treeVO.setManagerId(areaVO.getManagerId());
        treeVO.setManagerName(areaVO.getManagerName());
        treeVO.setContactPhone(areaVO.getContactPhone());
        treeVO.setAddress(areaVO.getAddress());
        treeVO.setLongitude(areaVO.getLongitude());
        treeVO.setLatitude(areaVO.getLatitude());
        treeVO.setStatus(areaVO.getStatus());
        treeVO.setCreateTime(areaVO.getCreateTime());
        treeVO.setUpdateTime(areaVO.getUpdateTime());
        treeVO.setCreateUserId(areaVO.getCreateUserId());
        treeVO.setCreateUserName(areaVO.getCreateUserName());
        treeVO.setUpdateUserId(areaVO.getUpdateUserId());
        treeVO.setUpdateUserName(areaVO.getUpdateUserName());
        return treeVO;
    }

    /**
     * 收集所有子区域ID
     *
     * @param children 子区域列表
     * @return 所有子区域ID列表
     */
    private List<Long> collectAllChildrenIds(List<AreaTreeVO> children) {
        List<Long> ids = new ArrayList<>();
        for (AreaTreeVO child : children) {
            if (child.getSelfAndAllChildrenIdList() != null) {
                ids.addAll(child.getSelfAndAllChildrenIdList());
            }
        }
        return ids;
    }

    /**
     * 检查区域编码是否存在
     *
     * @param areaCode 区域编码
     * @param excludeAreaId 排除的区域ID
     * @return 是否存在
     */
    public boolean checkAreaCodeExists(String areaCode, Long excludeAreaId) {
        AreaEntity entity = areaDao.selectByAreaCode(areaCode);
        if (entity == null) {
            return false;
        }
        if (excludeAreaId != null && entity.getAreaId().equals(excludeAreaId)) {
            return false;
        }
        return true;
    }

}
