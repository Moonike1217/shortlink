package com.moonike.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 短链接创建响应对象
 */
@Data
@AllArgsConstructor
@Builder
public class ShortLinkCreateRespDTO {

    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

}
