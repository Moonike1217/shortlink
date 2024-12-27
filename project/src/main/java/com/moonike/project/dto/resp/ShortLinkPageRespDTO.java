package com.moonike.project.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接分页返回对象
 */
@Data
public class ShortLinkPageRespDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;


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

    /**
     * 网站标识
     */
    private String favicon;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;



}
