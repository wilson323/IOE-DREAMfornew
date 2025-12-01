package net.lab1024.sa.common.code;

import lombok.Getter;

/**
 * 用户级别的错误码（用户引起的错误返回码，可以不用关注）
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2021/09/21 22:12:27
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Getter
public enum UserErrorCode implements ErrorCode {

    PARAM_ERROR(30001, "参数错误"),

    DATA_NOT_EXIST(30002, "左翻右翻，数据竟然找不到了~"),

    ALREADY_EXIST(30003, "数据已存在了呀~"),

    REPEAT_SUBMIT(30004, "亲~您操作的太快了，请稍等下再操作~"),

    NO_PERMISSION(30005, "对不起，您没有权限访问此内容哦~"),

    DEVELOPING(30006, "系統正在紧急开发中，敬请期待~"),

    LOGIN_STATE_INVALID(30007, "您还未登录或登录失效，请重新登录！"),

    USER_STATUS_ERROR(30008, "用户状态异常"),

    FORM_REPEAT_SUBMIT(30009, "请勿重复提交"),

    LOGIN_FAIL_LOCK(30010, "登录连续失败已经被锁定，无法登录"), LOGIN_FAIL_WILL_LOCK(30011, "登录连续失败将会锁定提醒"),

    LOGIN_ACTIVE_TIMEOUT(30012, "长时间未操作系统，需要重新登录"),

    // 数据操作相关错误码
    DATA_ERROR(30013, "数据操作错误"),
    DATA_NOT_FOUND(30014, "数据不存在"),
    DATA_SAVE_ERROR(30015, "数据保存失败"),
    DATA_UPDATE_ERROR(30016, "数据更新失败"),
    DATA_DELETE_ERROR(30017, "数据删除失败"),
    DATA_QUERY_ERROR(30018, "数据查询错误");

    private final int code;

    private final String msg;

    private final String level;

    UserErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.level = LEVEL_USER;
    }

    @Override
    public String getLevel() {
        return this.level;
    }
}
