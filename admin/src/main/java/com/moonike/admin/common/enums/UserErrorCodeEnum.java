package com.moonike.admin.common.enums;

import com.moonike.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {

    USER_TOKEN_ERROR("A000200", "用户Token验证异常"),
    USER_NULL("B000200", "用户记录不存在"),
    USER_NAME_EXIST("B000201", "用户名已存在"),
    USER_EXIST("B000202", "用户记录已存在"),
    USER_SAVE_ERROR("B000203", "新增用户失败"),
    USER_LOGIN_ERROR("B000204", "用户名或密码错误"),
    USER_HAS_LOGIN("B000205", "用户已登录"),
    USER_LOGIN_STATE_ERROR("B000206", "用户登录信息异常"),
    USER_UNAUTHORIZED("B000207", "用户未登录");

    private final String code;
    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
