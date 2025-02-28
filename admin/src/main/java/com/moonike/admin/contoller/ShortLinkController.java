package com.moonike.admin.contoller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.dto.req.ShortLinkUpdateReqDTO;
import com.moonike.admin.remote.ShortLinkActualRemoteService;
import com.moonike.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后管-短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 中台远程调用 短链接创建服务
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 中台远程调用 短链接分页查询服务
     * @param requestParam
     * @return
     */
    @GetMapping("/api/shortlink/admin/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkActualRemoteService.pageShortLink(requestParam);
    }

    /**
     * 中台远程调用 修改短链接服务
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }

}
