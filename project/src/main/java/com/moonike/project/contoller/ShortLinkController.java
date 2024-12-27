package com.moonike.project.contoller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moonike.project.common.convention.result.Result;
import com.moonike.project.common.convention.result.Results;
import com.moonike.project.dto.req.ShortLinkCreateReqDTO;
import com.moonike.project.dto.req.ShortLinkPageReqDTO;
import com.moonike.project.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;
import com.moonike.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/shortlink/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @GetMapping("/api/shortlink/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

}
