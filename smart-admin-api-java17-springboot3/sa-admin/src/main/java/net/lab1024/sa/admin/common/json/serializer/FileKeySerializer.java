package net.lab1024.sa.admin.common.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.file.service.FileService;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * 文件key进行序列化处理
 *
 * @Author 1024创新实验室
 * @Date 2020/8/15 22:06
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
public class FileKeySerializer extends JsonSerializer<String> {

    @Resource
    private FileService fileService;

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (StringUtils.isEmpty(value)) {
            jsonGenerator.writeString(value);
            return;
        }
        if (fileService == null) {
            jsonGenerator.writeString(value);
            return;
        }
        String fileUrl = fileService.getFileUrl(value);
        if (fileUrl != null) {
            jsonGenerator.writeString(fileUrl);
            return;
        }
        jsonGenerator.writeString(value);
    }
}