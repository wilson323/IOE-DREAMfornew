package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.MealInventoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品库存DAO接口
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface MealInventoryDao extends BaseMapper<MealInventoryEntity> {
}
