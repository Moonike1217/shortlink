package com.moonike.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moonike.admin.dao.entity.GroupDO;
import com.moonike.admin.dto.req.ShortlinkGroupSortReqDTO;
import com.moonike.admin.dto.req.ShortlinkGroupUpdateReqDTO;
import com.moonike.admin.dto.resp.ShortlinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);

    /**
     * 新增短链接分组
     * @param username 用户名
     * @param groupName 短链接分组名
     */
    void saveGroup(String username,String groupName);

//    /**
//     * 判断gid是否已经存在
//     * @param gid 分组标识
//     * @return true:存在 false:不存在
//     */
//    Boolean hasGid(String gid);

    /**
     * 判断gid是否已经存在
     * @param gid 分组标识
     * @return true:存在 false:不存在
     */
    Boolean hasGid(String username, String gid);

    /**
     * 查询所有短链接分组
     * @return 所有短链接分组集合
     */
    List<ShortlinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     * @param requestParam ShortlinkGroupUpdateReqDTO
     */
    void updateGroup(ShortlinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     * @param gid 短链接分组标识
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam List<ShortlinkGroupSortReqDTO>
     */
    void sortGroup(List<ShortlinkGroupSortReqDTO> requestParam);
}
