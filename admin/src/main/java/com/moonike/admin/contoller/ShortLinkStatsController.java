package com.moonike.admin.contoller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.remote.ShortLinkActualRemoteService;
import com.moonike.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后管-短链接监控控制层
 */
@RestController(value = "shortLinkStatsControllerByAdmin")
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 中台远程调用 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 中台远程调用 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 中台远程调用 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(requestParam);
    }

    /**
     * 中台远程调用 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/shortlink/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(requestParam);
    }
}