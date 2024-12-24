package com.moonike.admin.contoller;

import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.dto.req.ShortlinkGroupSaveReqDTO;
import com.moonike.admin.dto.req.ShortlinkGroupUpdateReqDTO;
import com.moonike.admin.dto.resp.ShortlinkGroupRespDTO;
import com.moonike.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组接口控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 新建短链接分组
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/group")
    public Result<Void> saveGroup(@RequestBody ShortlinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组
     * @return
     */
    @GetMapping("/api/shortlink/v1/group")
    public Result<List<ShortlinkGroupRespDTO>> listGroup() {
        List<ShortlinkGroupRespDTO> list =groupService.listGroup();
        return Results.success(list);
    }

    /**
     * 修改短链接分组
     * @param requestParam
     * @return
     */
    @PutMapping("/api/shortlink/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortlinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

}
