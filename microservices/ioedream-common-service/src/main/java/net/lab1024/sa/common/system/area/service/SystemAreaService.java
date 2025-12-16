package net.lab1024.sa.common.system.area.service;

import java.util.List;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaAddForm;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaQueryForm;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaUpdateForm;
import net.lab1024.sa.common.system.area.domain.vo.SystemAreaVO;

/**
 * 系统区域服务
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
public interface SystemAreaService {

    PageResult<SystemAreaVO> queryPage(SystemAreaQueryForm queryForm);

    List<SystemAreaVO> getAreaTree();

    SystemAreaVO getDetail(Long areaId);

    Long add(SystemAreaAddForm form);

    void update(SystemAreaUpdateForm form);

    void delete(Long areaId);

    void batchDelete(List<Long> areaIds);
}

