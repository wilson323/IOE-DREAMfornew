package net.lab1024.sa.enterprise.oa.document.domain.enums;

import lombok.Getter;
import net.lab1024.sa.common.enumeration.BaseEnum;

/**
 * 文档类型枚举
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Getter
public enum DocumentTypeEnum implements BaseEnum {

    // Office文档
    WORD("WORD", "Word文档", ".doc,.docx"),
    EXCEL("EXCEL", "Excel表格", ".xls,.xlsx"),
    PPT("PPT", "PPT演示文稿", ".ppt,.pptx"),

    // PDF文档
    PDF("PDF", "PDF文档", ".pdf"),

    // 图片文档
    IMAGE("IMAGE", "图片文档", ".jpg,.jpeg,.png,.gif,.bmp,.webp"),

    // 文本文档
    TEXT("TEXT", "文本文档", ".txt,.md"),

    // 压缩文件
    ARCHIVE("ARCHIVE", "压缩文件", ".zip,.rar,.7z,.tar,.gz"),

    // 音频文件
    AUDIO("AUDIO", "音频文件", ".mp3,.wav,.flac,.aac"),

    // 视频文件
    VIDEO("VIDEO", "视频文件", ".mp4,.avi,.mkv,.mov,.wmv"),

    // 其他文件
    OTHER("OTHER", "其他文件", "");

    private final String value;
    private final String description;
    private final String extensions;

    DocumentTypeEnum(String value, String description, String extensions) {
        this.value = value;
        this.description = description;
        this.extensions = extensions;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getDesc() {
        return description;
    }

    /**
     * 根据文件扩展名判断文档类型
     *
     * @param fileName 文件名
     * @return 文档类型
     */
    public static DocumentTypeEnum getByFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return OTHER;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return OTHER;
        }

        String extension = fileName.substring(lastDotIndex).toLowerCase();

        for (DocumentTypeEnum type : values()) {
            if (type.extensions.contains(extension)) {
                return type;
            }
        }

        return OTHER;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举
     */
    public static DocumentTypeEnum getByCode(String code) {
        for (DocumentTypeEnum type : values()) {
            if (type.value.equals(code)) {
                return type;
            }
        }
        return OTHER;
    }

}
