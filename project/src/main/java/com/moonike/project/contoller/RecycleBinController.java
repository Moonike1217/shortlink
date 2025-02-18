package com.moonike.project.contoller;

import com.moonike.project.common.convention.result.Result;
import com.moonike.project.common.convention.result.Results;
import com.moonike.project.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
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
}
