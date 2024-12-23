package com.moonike.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录返回响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRespDTO {

    /**
     * 用户token
     */
    private String token;
}
