package com.moonike.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dto.req.SaveLinkToRecycleBinReqDTO;

/**
 * 回收站接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将短链接保存到回收站
     * @param requestParam
     */
    void saveLinkToRecycleBin(SaveLinkToRecycleBinReqDTO requestParam);
}
