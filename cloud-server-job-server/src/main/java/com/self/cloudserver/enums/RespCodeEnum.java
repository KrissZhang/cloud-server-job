package com.self.cloudserver.enums;

/**
 * 响应码
 */
public enum RespCodeEnum {
    /**
     * 成功
     */
    SUCCESS("200", "成功"),

    /**
     * 参数错误
     */
    FAIL_PARAM("401", "参数错误"),

    /**
     * 用户认证错误
     */
    FAIL_USER_AUTH("402", "用户认证错误"),

    /**
     * token鉴权失败 token失效
     */
    FAIL_AUTH("403", "token鉴权失败"),

    /**
     * 配置token失效
     */
    FAIL_CFG_TOKEN_EXPIRE("405", "配置token失效"),

    /**
     * 账号权限不足
     */
    FAIL_PERMISSION_DENIED("406", "账号权限不足"),

    /**
     * 记录存在，不能重复添加
     */
    EXISTS_DATA("501", "记录存在,不能重复添加"),

    /**
     * 记录不存在
     */
    NO_DATA("502", "记录不存在"),

    /**
     * 系统错误
     */
    FAIL_SYS("503", "系统错误"),

    /**
     * 数据重复
     */
    FAIL_DATA_REPEAT("504", "数据重复"),

    /**
     * 短信发送失败
     */
    FAIL_SMS_SEND("601","短信发送失败");



    private String code;

    private String msg;

    RespCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
