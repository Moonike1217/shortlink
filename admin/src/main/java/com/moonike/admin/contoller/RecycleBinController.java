package com.moonike.admin.contoller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.admin.remote.ShortLinkRemoteService;
import com.moonike.admin.remote.dto.req.RecoverLinkFromRecycleBinReqDTO;
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
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 中台远程调用 保存短链接到回收站
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/")
    public Result<Void> saveLinkToRecycleBin(@RequestBody SaveLinkToRecycleBinReqDTO requestParam) {
        shortLinkRemoteService.saveLinkToRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 中台远程调用 回收站内短链接分页查询服务
     * @return
     */
    @GetMapping("/api/shortlink/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink() {
        return recycleBinService.pageRecycleBinShortLink();
    }

    /**
     * 中台远程调用 从回收站恢复短链接
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/recover")
    public Result<Void> recoverLinkFromRecycleBin(@RequestBody RecoverLinkFromRecycleBinReqDTO requestParam) {
        shortLinkRemoteService.recoverLinkFromRecycleBin(requestParam);
        return Results.success();
    }

}