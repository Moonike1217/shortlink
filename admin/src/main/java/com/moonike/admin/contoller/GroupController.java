package com.moonike.admin.contoller;

import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.dto.req.ShortlinkGroupSaveReqDTO;
import com.moonike.admin.dto.req.ShortlinkGroupSortReqDTO;
import com.moonike.admin.dto.req.ShortlinkGroupUpdateReqDTO;
import com.moonike.admin.dto.resp.ShortlinkGroupRespDTO;
import com.moonike.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后管-短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 中台远程调用 新建短链接分组
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/group")
    public Result<Void> saveGroup(@RequestBody ShortlinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 中台远程调用 查询短链接分组
     * @return
     */
    @GetMapping("/api/shortlink/admin/v1/group")
    public Result<List<ShortlinkGroupRespDTO>> listGroup() {
        List<ShortlinkGroupRespDTO> list =groupService.listGroup();
        return Results.success(list);
    }

    /**
     * 中台远程调用 修改短链接分组
     * @param requestParam
     * @return
     */
    @PutMapping("/api/shortlink/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortlinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 中台远程调用 删除短链接分组
     * @param gid 短链接分组唯一标识
     * @return
     */
    @DeleteMapping("/api/shortlink/admin/v1/group")
    public Result<Void> deleteGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 中台远程调用 短链接分组排序
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortlinkGroupSortReqDTO> requestParam) {
        groupService.sortGroup(requestParam);
        return Results.success();
    }
}
