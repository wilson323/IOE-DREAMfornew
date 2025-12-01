package net.lab1024.sa.base.module.support.file.service;

import java.util.Collections;
import java.util.List;
import net.lab1024.sa.base.module.support.file.domain.vo.FileVO;

/**
 * 占位文件服务接口（最小化），用于打通编译。
 */
public interface FileService {

    default List<FileVO> getFileList(List<String> fileKeys) {
        return Collections.emptyList();
    }

    default String getFileUrl(String fileKey) {
        return null;
    }
}
