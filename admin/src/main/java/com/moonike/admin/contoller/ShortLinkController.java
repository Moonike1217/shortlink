package com.moonike.admin.contoller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.remote.dto.ShortLinkRemoteService;
import com.moonike.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接后管管理控制层
 */
@RestController
public class ShortLinkController {

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @GetMapping("/api/shortlink/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        // 在中台远程调用短链接服务
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

}
