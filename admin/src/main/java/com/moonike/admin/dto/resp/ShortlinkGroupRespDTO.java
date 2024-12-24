package com.moonike.admin.dto.resp;

import lombok.Data;

/**
 * 短链接分组查询返回响应
 */
@Data
public class ShortlinkGroupRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
