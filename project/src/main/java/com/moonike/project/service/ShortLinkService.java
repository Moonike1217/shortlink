package com.moonike.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dto.req.ShortLinkCreateReqDTO;
import com.moonike.project.dto.req.ShortLinkPageReqDTO;
import com.moonike.project.dto.req.ShortLinkUpdateReqDTO;
import com.moonike.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.moonike.project.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询短链接分组内短链接数量
     * @param requestParam 查询短链接分组内短链接数量请求参数
     * @return 短链接分组内短链接数量返回结果
     */
    List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接信息
     * @param requestParam 修改短链接信息请求参数
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转原始链接
     * @param shortUri 短链接后缀
     * @param request HTTP请求
     * @param response HTTP响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException;
}
