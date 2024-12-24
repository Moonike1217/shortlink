package com.moonike.admin.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接分组修改请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortlinkGroupUpdateReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名
     */
    private String name;
}
