package com.moonike.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 短链接分页请求参数
 */
@Data
public class ShortLinkPageReqDTO extends Page {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序标识 todayUip, todayUv, todayPv, totalUip, totalUv, totalPv
     */
    private String orderTag;
}
