package com.moonike.admin.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接分组排序请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortlinkGroupSortReqDTO {
    /**
     * 短链接分组唯一标识
     */
    private String gid;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
