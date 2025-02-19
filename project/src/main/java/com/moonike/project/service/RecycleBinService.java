package com.moonike.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dto.req.RemoveLinkFromRecycleBinReqDTO;
import com.moonike.project.dto.req.RecoverLinkFromRecycleBinReqDTO;
import com.moonike.project.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.project.dto.req.ShortLinkPageRecycleBinReqDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将短链接保存到回收站
     * @param requestParam
     */
    void saveLinkToRecycleBin(SaveLinkToRecycleBinReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageRecycleBinShortLink(ShortLinkPageRecycleBinReqDTO requestParam);

    /**
     * 从回收站恢复短链接
     * @param requestParam 恢复短链接请求参数
     */
    void recoverLinkFromRecycleBin(RecoverLinkFromRecycleBinReqDTO requestParam);

    /**
     * 从回收站删除短链接
     * @param requestParam
     */
    void removeLinkFromRecycleBin(RemoveLinkFromRecycleBinReqDTO requestParam);
}
