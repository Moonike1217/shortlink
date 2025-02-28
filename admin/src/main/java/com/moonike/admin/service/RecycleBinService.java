package com.moonike.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * 短链接回收站接口层
 */
public interface RecycleBinService {
    /**
     * 分页查询回收站短链接
     * @return
     */
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink ();
}
