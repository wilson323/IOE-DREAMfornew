package net.lab1024.sa.common.system.manager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.system.dao.SystemDictDao;
import net.lab1024.sa.common.system.domain.entity.SystemDictEntity;

public class DictManager {

    private final SystemDictDao systemDictDao;

    public DictManager(SystemDictDao dao) {
        this.systemDictDao = dao;
    }

    public List<SystemDictEntity> getDictList(String typeCode) {
        return systemDictDao.selectEnabledByTypeCode(typeCode);
    }

    public List<Map<String, Object>> buildDictTree(String typeCode) {
        List<Map<String, Object>> tree = systemDictDao.selectDictTree(typeCode);
        return tree != null ? tree : Collections.emptyList();
    }

    public void clearOtherDefaultValues(Long typeId, Long dictId) {
        systemDictDao.clearOtherDefaultValues(typeId, dictId);
    }

    public boolean checkDictValueUnique(Long typeId, String dictValue, Long excludeDictId) {
        long count = systemDictDao.checkDictValueUnique(typeId, dictValue, excludeDictId);
        return count == 0;
    }
}
