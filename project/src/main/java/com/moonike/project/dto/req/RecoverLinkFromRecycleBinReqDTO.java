package com.moonike.project.dto.req;

import lombok.Data;

/**
 * 短链接移出回收站请求对象
 */
@Data
public class RecoverLinkFromRecycleBinReqDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 短链接分组标识
     */
    private String gid;
}
