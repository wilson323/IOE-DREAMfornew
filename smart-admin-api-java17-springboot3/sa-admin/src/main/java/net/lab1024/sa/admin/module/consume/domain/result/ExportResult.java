/*
 * 导出结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.result;

import java.time.LocalDateTime;


/**
 * 导出结果
 * 封装数据导出的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */




public class ExportResult {

    /**
     * 导出类型
     */
    private String exportType;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private long fileSize;

    /**
     * 导出记录数
     */
    private int recordCount;

    /**
     * 导出时间
     */
    private LocalDateTime exportTime;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 下载链接
     */
    private String downloadUrl;

    /**
     * 消息（兼容字段）
     */
    private String message;

    // 手动添加的getter/setter方法 (Lombok失效备用)


    public boolean isSuccess() {
        return success;
    }














}
