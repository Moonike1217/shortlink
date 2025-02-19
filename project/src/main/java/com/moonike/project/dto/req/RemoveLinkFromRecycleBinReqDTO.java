package com.moonike.project.dto.req;

import lombok.Data;

/**
 * 从回收站删除短链接请求对象
 */
@Data
public class RemoveLinkFromRecycleBinReqDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 短链接分组标识
     */
    private String gid;
}
