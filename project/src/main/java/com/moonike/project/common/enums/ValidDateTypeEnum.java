package com.moonike.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 有效期类型
 */
@RequiredArgsConstructor
public enum ValidDateTypeEnum {

    /**
     * 永久有效
     */
    PERMANENT(0),

    /**
     * 自定义有效
     */
    CUSTOM(1);

    @Getter
    private final Integer type;
}
