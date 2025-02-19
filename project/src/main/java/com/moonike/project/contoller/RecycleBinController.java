package com.moonike.project.contoller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moonike.project.common.convention.result.Result;
import com.moonike.project.common.convention.result.Results;
import com.moonike.project.dto.req.RemoveLinkFromRecycleBinReqDTO;
import com.moonike.project.dto.req.RecoverLinkFromRecycleBinReqDTO;
import com.moonike.project.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.project.dto.req.ShortLinkPageRecycleBinReqDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;
import com.moonike.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务中台-回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存短链接到回收站
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/")
    public Result<Void> saveLinkToRecycleBin(@RequestBody SaveLinkToRecycleBinReqDTO requestParam) {
        recycleBinService.saveLinkToRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站内短链接
     * @param requestParam
     * @return
     */
    @GetMapping("/api/shortlink/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageRecycleBinReqDTO requestParam) {
        return Results.success(recycleBinService.pageRecycleBinShortLink(requestParam));
    }

    /**
     * 从回收站恢复短链接
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/recover")
    public Result<Void> recoverLinkFromRecycleBin(@RequestBody RecoverLinkFromRecycleBinReqDTO requestParam) {
        recycleBinService.recoverLinkFromRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 从回收站删除短链接
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/delete")
    public Result<Void> deleteLinkFromRecycleBin(@RequestBody RemoveLinkFromRecycleBinReqDTO requestParam) {
        recycleBinService.removeLinkFromRecycleBin(requestParam);
        return Results.success();
    }
}
