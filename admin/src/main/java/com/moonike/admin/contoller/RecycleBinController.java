package com.moonike.admin.contoller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.remote.ShortLinkActualRemoteService;
import com.moonike.admin.remote.dto.req.RecoverLinkFromRecycleBinReqDTO;
import com.moonike.admin.remote.dto.req.RemoveLinkFromRecycleBinReqDTO;
import com.moonike.admin.remote.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.moonike.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后管-回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody SaveLinkToRecycleBinReqDTO requestParam) {
        shortLinkActualRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/shortlink/admin/v1/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink() {
        return recycleBinService.pageRecycleBinShortLink();
    }

    /**
     * 恢复短链接
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecoverLinkFromRecycleBinReqDTO requestParam) {
        shortLinkActualRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除短链接
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RemoveLinkFromRecycleBinReqDTO requestParam) {
        shortLinkActualRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }
}