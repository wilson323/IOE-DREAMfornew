package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.consume.MealInventoryEntity;
import net.lab1024.sa.common.entity.consume.MealMenuEntity;
import net.lab1024.sa.consume.dao.MealInventoryDao;
import net.lab1024.sa.consume.dao.MealMenuDao;
import net.lab1024.sa.consume.manager.MealManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;

/**
 * 菜单服务 - 菜品管理服务层
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MealMenuService extends ServiceImpl<MealMenuDao, MealMenuEntity> {

    @Resource
    private MealMenuDao mealMenuDao;

    @Resource
    private MealInventoryDao mealInventoryDao;

    @Resource
    private MealManager mealManager;

    /**
     * 查询可用菜品列表（分页）
     *
     * @param orderDate 订餐日期
     * @param mealType 餐别（1-早餐 2-午餐 3-晚餐）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 菜品分页数据
     */
    public Page<MealMenuEntity> getAvailableMenus(LocalDate orderDate, Integer mealType,
                                                     Integer pageNum, Integer pageSize) {
        log.info("[菜单服务] 查询可用菜品: orderDate={}, mealType={}", orderDate, mealType);

        String dayOfWeek = String.valueOf(orderDate.getDayOfWeek().getValue());

        LambdaQueryWrapper<MealMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MealMenuEntity::getStatus, 1)
                .like(MealMenuEntity::getAvailableDays, dayOfWeek)
                .orderByAsc(MealMenuEntity::getSortOrder);

        Page<MealMenuEntity> page = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 查询库存信息
        page.getRecords().forEach(menu -> {
            LambdaQueryWrapper<MealInventoryEntity> invQuery = new LambdaQueryWrapper<>();
            invQuery.eq(MealInventoryEntity::getMenuId, menu.getMenuId())
                    .eq(MealInventoryEntity::getInventoryDate, orderDate)
                    .eq(MealInventoryEntity::getMealType, mealType);
            MealInventoryEntity inventory = mealInventoryDao.selectOne(invQuery);
            if (inventory != null) {
                menu.setCurrentQuantity(inventory.getRemainingQuantity());
            }
        });

        return page;
    }

    /**
     * 新增菜品
     *
     * @param menu 菜品实体
     * @return 菜品ID
     */
    public Long addMenu(MealMenuEntity menu) {
        log.info("[菜单服务] 新增菜品: menuName={}", menu.getMenuName());
        mealMenuDao.insert(menu);
        return menu.getMenuId();
    }

    /**
     * 更新菜品
     *
     * @param menu 菜品实体
     */
    public void updateMenu(MealMenuEntity menu) {
        log.info("[菜单服务] 更新菜品: menuId={}", menu.getMenuId());
        mealMenuDao.updateById(menu);
    }

    /**
     * 删除菜品
     *
     * @param menuId 菜品ID
     */
    public void deleteMenu(Long menuId) {
        log.info("[菜单服务] 删除菜品: menuId={}", menuId);
        mealMenuDao.deleteById(menuId);
    }

    /**
     * 上架菜品
     *
     * @param menuId 菜品ID
     */
    public void onShelf(Long menuId) {
        log.info("[菜单服务] 上架菜品: menuId={}", menuId);
        MealMenuEntity menu = mealMenuDao.selectById(menuId);
        if (menu != null) {
            menu.setStatus(1);
            mealMenuDao.updateById(menu);
        }
    }

    /**
     * 下架菜品
     *
     * @param menuId 菜品ID
     */
    public void offShelf(Long menuId) {
        log.info("[菜单服务] 下架菜品: menuId={}", menuId);
        MealMenuEntity menu = mealMenuDao.selectById(menuId);
        if (menu != null) {
            menu.setStatus(0);
            mealMenuDao.updateById(menu);
        }
    }
}
