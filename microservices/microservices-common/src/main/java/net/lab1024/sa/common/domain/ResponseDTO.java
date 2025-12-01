package net.lab1024.sa.common.domain;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.code.ErrorCode;
import net.lab1024.sa.common.code.UserErrorCode;
import net.lab1024.sa.common.enumeration.DataTypeEnum;
import net.lab1024.sa.common.swagger.SchemaEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * 请求返回对象
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2021-10-31 21:06:11
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
@Data
@Schema
public class ResponseDTO<T> {

    public static final int OK_CODE = 0;

    public static final String OK_MSG = "操作成功";

    @Schema(description = "返回码")
    private Integer code;

    @Schema(description = "级别")
    private String level;

    private String msg;

    private Boolean ok;

    @Schema(description = "返回数据")
    private T data;

    @SchemaEnum(value = DataTypeEnum.class,desc = "数据类型")
    private Integer dataType;

    public ResponseDTO(Integer code, String level, boolean ok, String msg, T data) {
        this.code = code;
        this.level = level;
        this.ok = ok;
        this.msg = msg;
        this.data = data;
        this.dataType = DataTypeEnum.NORMAL.getValue().intValue();
    }

    public ResponseDTO(Integer code, String level, boolean ok, String msg) {
        this.code = code;
        this.level = level;
        this.ok = ok;
        this.msg = msg;
        this.dataType = DataTypeEnum.NORMAL.getValue().intValue();
    }

    public ResponseDTO(ErrorCode errorCode, boolean ok, String msg, T data) {
        this.code = errorCode.getCode();
        this.level = errorCode.getLevel();
        this.ok = ok;
        if (StringUtils.isNotBlank(msg)) {
            this.msg = msg;
        } else {
            this.msg = errorCode.getMsg();
        }
        this.data = data;
        this.dataType = DataTypeEnum.NORMAL.getValue().intValue();
    }

    public static <T> ResponseDTO<T> ok() {
        return new ResponseDTO<>(OK_CODE, null, true, OK_MSG, null);
    }

    public static <T> ResponseDTO<T> ok(T data) {
        return new ResponseDTO<>(OK_CODE, null, true, OK_MSG, data);
    }

    public static <T> ResponseDTO<T> ok(T data, String msg) {
        return new ResponseDTO<>(OK_CODE, null, true, msg, data);
    }

    public static <T> ResponseDTO<T> okMsg(String msg) {
        return new ResponseDTO<>(OK_CODE, null, true, msg, null);
    }

    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(OK_CODE, null, true, OK_MSG, data);
    }

    public static <T> ResponseDTO<T> success() {
        return new ResponseDTO<>(OK_CODE, null, true, OK_MSG, null);
    }

    // -------------------------------------------- 最常用的 用户参数 错误码 --------------------------------------------

    public static <T> ResponseDTO<T> userErrorParam() {
        return new ResponseDTO<>(UserErrorCode.PARAM_ERROR, false, null, null);
    }


    public static <T> ResponseDTO<T> userErrorParam(String msg) {
        return new ResponseDTO<>(UserErrorCode.PARAM_ERROR, false, msg, null);
    }

    // -------------------------------------------- 错误码 --------------------------------------------

    public static <T> ResponseDTO<T> error(ErrorCode errorCode) {
        return new ResponseDTO<>(errorCode, false, null, null);
    }

    public static <T> ResponseDTO<T> error(ErrorCode errorCode, boolean ok) {
        return new ResponseDTO<>(errorCode, ok, null, null);
    }

    public static <T>  ResponseDTO<T> error(ResponseDTO<?> responseDTO) {
        return new ResponseDTO<>(responseDTO.getCode(), responseDTO.getLevel(), responseDTO.getOk(), responseDTO.getMsg(), null);
    }

    public static <T> ResponseDTO<T> error(ErrorCode errorCode, String msg) {
        return new ResponseDTO<>(errorCode, false, msg, null);
    }

    public static <T> ResponseDTO<T> error(String msg) {
        return new ResponseDTO<>(UserErrorCode.PARAM_ERROR.getCode(), UserErrorCode.PARAM_ERROR.getLevel(), false, msg, null);
    }

    public static <T> ResponseDTO<T> error(String msg, T data) {
        return new ResponseDTO<>(UserErrorCode.PARAM_ERROR.getCode(), UserErrorCode.PARAM_ERROR.getLevel(), false, msg, data);
    }

    public static <T> ResponseDTO<T> errorData(ErrorCode errorCode, T data) {
        return new ResponseDTO<>(errorCode, false, null, data);
    }

    // 临时手动添加getter方法，解决Lombok问题
    public Integer getCode() {
        return this.code;
    }

    public String getLevel() {
        return this.level;
    }

    public String getMsg() {
        return this.msg;
    }

    public Boolean getOk() {
        return this.ok;
    }

    public T getData() {
        return this.data;
    }

    public Integer getDataType() {
        return this.dataType;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

}
