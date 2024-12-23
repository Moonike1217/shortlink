package com.moonike.admin.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接分组新增请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortlinkGroupSaveReqDTO {
    /**
     * 分组名
     */
    private String name;
}
