package com.moonike.project.dto.resp;

import lombok.Data;

/**
 * 短链接分组数量查询返回对象
 */
@Data
public class ShortLinkCountQueryRespDTO {

    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 当前分组下短链接数量
     */
    private Integer shortLinkCount;
}
