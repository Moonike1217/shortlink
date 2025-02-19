package com.moonike.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.admin.dto.req.ShortLinkUpdateReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkPageRecycleBinReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkCountQueryRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短连接中台远程调用服务
 */
public interface ShortLinkRemoteService {
    /**
     * 远程调用分页查询短链接
     * @param requestParam
     * @return
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        // 返回的JSON字符串
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/page", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<>() {});
    }

    /**
     * 远程调用创建短链接
     * @param requestParam
     * @return
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String resultStr = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultStr, new TypeReference<>() {});
    }

    /**
     * 远程调用查询短链接分组内短链接数量
     * @param requestParam
     * @return
     */
    default Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        // 返回的JSON字符串
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/count", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<>() {});
    }

    /**
     * 远程调用更新短链接
     * @param requestParam
     */
    default void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/update", JSON.toJSONString(requestParam));
    }

    /**
     * 远程调用根据 URL 获取标题
     * @param url 目标网站地址
     * @return 网站标题
     */
    default Result<String> getTitleByUrl(String url) {
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/title?url=" + url);
        return JSON.parseObject(resultStr, new TypeReference<>() {});
    }

    /**
     * 远程调用保存短链接到回收站
     * @param requestParam
     */
    default void saveLinkToRecycleBin(SaveLinkToRecycleBinReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/", JSON.toJSONString(requestParam));
    }

    /**
     * 远程调用分页查询回收站短链接
     * @param requestParam
     * @return
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkPageRecycleBinReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gidList", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        // 返回的JSON字符串
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<>() {});
    }
}