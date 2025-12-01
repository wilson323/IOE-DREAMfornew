package net.lab1024.sa.admin.common.json.deserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.admin.module.system.file.domain.vo.FileVO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件key反序列化<br>
 * 由于前端接收到的是序列化过的字段, 这边入库需要进行反序列化操作比较方便处理
 *
 * @Author 1024创新实验室: 胡克
 * @Date 2022-11-24 17:15:23
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileKeyVoDeserializer extends JsonDeserializer<String> {

    private static final Logger log = LoggerFactory.getLogger(FileKeyVoDeserializer.class);

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        List<FileVO> list = new ArrayList<>();
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode listOrObjectNode = objectCodec.readTree(jsonParser);
        String deserialize = "";
        try {
            if (listOrObjectNode.isArray()) {
                for (JsonNode node : listOrObjectNode) {
                    // 使用 ObjectMapper.convertValue 以避免不同 Jackson 版本下 treeToValue 签名不匹配问题
                    list.add(((ObjectMapper) objectCodec).convertValue(node, FileVO.class));
                }
            } else {
                list.add(((ObjectMapper) objectCodec).convertValue(listOrObjectNode, FileVO.class));
            }
            deserialize = list.stream().map(fileVO -> null == fileVO ? "" : fileVO.getFileKey()).collect(Collectors.joining(","));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            deserialize = listOrObjectNode.asText();
        }
        return deserialize;
    }


}
