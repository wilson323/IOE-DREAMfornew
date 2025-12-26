package net.lab1024.sa.consume.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON字段TypeHandler - 处理MySQL JSON类型与Java对象之间的转换
 *
 * 支持的JSON字段：
 * - POSID_ACCOUNTKIND.mode_config - 6种消费模式配置
 * - POSID_AREA.fixed_value_config - 各餐别定值金额
 * - POSID_AREA.area_config - 区域权限配置
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@MappedTypes({Object.class})
public class JSONTypeHandler<T> extends BaseTypeHandler<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Class<T> type;

    public JSONTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            log.error("[JSON TypeHandler] JSON序列化失败: type={}, error={}", type.getName(), e.getMessage(), e);
            throw new SQLException("JSON序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJSON(json);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJSON(json);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJSON(json);
    }

    /**
     * 解析JSON字符串为Java对象
     */
    private T parseJSON(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("[JSON TypeHandler] JSON反序列化失败: type={}, json={}, error={}", type.getName(), json, e.getMessage(), e);
            throw new SQLException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }
}
