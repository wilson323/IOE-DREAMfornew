package net.lab1024.sa.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dict.dao.DictDataDao;
import net.lab1024.sa.common.dict.dao.DictTypeDao;
import net.lab1024.sa.common.dict.entity.DictDataEntity;
import net.lab1024.sa.common.dict.entity.DictTypeEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 * <p>
 * 企业级真实实现 - 直接查询数据库
 * 遵循CLAUDE.md四层架构规范：Controller → DAO → 数据库
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
@RestController
@RequestMapping("/support/dict")
@Tag(name = "字典管理", description = "字典管理相关接口")
public class DictController {

    @Resource
    private DictTypeDao dictTypeDao;

    @Resource
    private DictDataDao dictDataDao;

    @Operation(summary = "获取所有字典数据")
    @GetMapping("/getAllDictData")
    public ResponseDTO<List<DictDataEntity>> getAllDictData() {
        log.info("[字典管理] 获取所有字典数据");
        try {
            List<DictDataEntity> dictDataList = dictDataDao.selectList(null);
            log.info("[字典管理] 查询成功，共{}条数据", dictDataList.size());
            return ResponseDTO.ok(dictDataList);
        } catch (Exception e) {
            log.error("[字典管理] 查询失败", e);
            return ResponseDTO.error("DICT_QUERY_ERROR", "查询字典数据失败");
        }
    }

    @Operation(summary = "获取字典类型列表")
    @GetMapping("/type/list")
    public ResponseDTO<List<DictTypeEntity>> getTypeList() {
        log.info("[字典管理] 获取字典类型列表");
        try {
            List<DictTypeEntity> typeList = dictTypeDao.selectList(null);
            log.info("[字典管理] 查询成功，共{}条数据", typeList.size());
            return ResponseDTO.ok(typeList);
        } catch (Exception e) {
            log.error("[字典管理] 查询失败", e);
            return ResponseDTO.error("DICT_TYPE_QUERY_ERROR", "查询字典类型失败");
        }
    }

    @Operation(summary = "根据类型编码获取字典数据")
    @GetMapping("/data/list")
    public ResponseDTO<List<DictDataEntity>> getDataList(@RequestParam String typeCode) {
        log.info("[字典管理] 根据类型编码获取字典数据，typeCode={}", typeCode);
        try {
            List<DictDataEntity> dataList = dictDataDao.selectByTypeCode(typeCode);
            log.info("[字典管理] 查询成功，共{}条数据", dataList.size());
            return ResponseDTO.ok(dataList);
        } catch (Exception e) {
            log.error("[字典管理] 查询失败，typeCode={}", typeCode, e);
            return ResponseDTO.error("DICT_DATA_QUERY_ERROR", "查询字典数据失败");
        }
    }
}
