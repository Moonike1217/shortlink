package com.moonike.admin.contoller;

import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.admin.remote.ShortLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后管-回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 中台远程调用 保存短链接到回收站
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/")
    public Result<Void> saveLinkToRecycleBin(@RequestBody SaveLinkToRecycleBinReqDTO requestParam) {
        shortLinkRemoteService.saveLinkToRecycleBin(requestParam);
        return Results.success();
    }
}