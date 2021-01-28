package com.qinfei.core.exception;

public enum ResultEnum {
    UNKONW_ERROR(-1, "未知错误!"),
    SUCCESS(0, "成功!"),
    FORBIDDEN(403, "没有权限!"),
    NOAUDIT(1002, "您没有审核权限！"),
    ACCT_EOOR(1001, "用户名输入错误！"),
    PWD_ERROR(1003, "密码输入错误！"),
    DATE_CONFLICT(1004, ""),
    DICT_NAME_EXIST(1005,"存在相同的税种编码")
    ;

    private final Integer code;

    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}