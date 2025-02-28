package com.moonike.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.dto.req.ShortLinkUpdateReqDTO;
import com.moonike.admin.remote.dto.req.*;
import com.moonike.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 短链接中台远程调用服务
 */
@FeignClient(value = "short-link-project")
public interface ShortLinkActualRemoteService {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建响应
     */
    @PostMapping("/api/shortlink/v1/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求参数
     */
    @PostMapping("/api/shortlink/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam
     * @return 查询短链接响应
     */
    @GetMapping("/api/shortlink/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@SpringQueryMap ShortLinkPageReqDTO requestParam);

    /**
     * 根据 URL 获取标题
     *
     * @param url 目标网站地址
     * @return 网站标题
     */
    @GetMapping("/api/shortlink/v1/title")
    Result<String> getTitleByUrl(@RequestParam("url") String url);

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/save")
    void saveRecycleBin(@RequestBody SaveLinkToRecycleBinReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     * @return 查询短链接响应
     */
    @GetMapping("/api/shortlink/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@SpringQueryMap ShortLinkPageRecycleBinReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内监控数据
     * @return 短链接监控信息
     */
    @GetMapping("/api/shortlink/v1/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(@SpringQueryMap ShortLinkStatsReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内监控数据
     * @return 分组短链接监控信息
     */
    @GetMapping("/api/shortlink/v1/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(@SpringQueryMap ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     * @return 短链接监控访问记录信息
     */
    @GetMapping("/api/shortlink/v1/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@SpringQueryMap ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     * @return 分组短链接监控访问记录信息
     */
    @GetMapping("/api/shortlink/v1/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@SpringQueryMap ShortLinkGroupStatsAccessRecordReqDTO requestParam);

    /**
     * 恢复短链接
     *
     * @param requestParam 短链接恢复请求参数
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecoverLinkFromRecycleBinReqDTO requestParam);

    /**
     * 移除短链接
     *
     * @param requestParam 短链接移除请求参数
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/remove")
    void removeRecycleBin(@RequestBody RemoveLinkFromRecycleBinReqDTO requestParam);
}