package com.moonike.admin.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接修改请求对象
 */
@Data
public class ShortLinkUpdateReqDTO {

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 原始分组标识
     */
    private String originGid;

    /**
     * 完整链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;


    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */
    private int validDateType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validDate;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;
}
