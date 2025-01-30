package com.moonike.admin.contoller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.dto.req.ShortLinkUpdateReqDTO;
import com.moonike.admin.remote.ShortLinkRemoteService;
import com.moonike.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.moonike.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接后管管理控制层
 */
@RestController
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 中台远程调用短链接创建服务
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 中台远程调用短链接分页查询服务
     * @param requestParam
     * @return
     */
    @GetMapping("/api/shortlink/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 中台远程调用修改短链接信息服务
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success();
    }

}
