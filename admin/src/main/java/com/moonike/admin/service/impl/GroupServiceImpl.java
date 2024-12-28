package com.moonike.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.admin.common.biz.user.UserContext;
import com.moonike.admin.common.convention.exception.ServiceException;
import com.moonike.admin.dao.entity.GroupDO;
import com.moonike.admin.dao.mapper.GroupMapper;
import com.moonike.admin.dto.req.ShortlinkGroupSortReqDTO;
import com.moonike.admin.dto.req.ShortlinkGroupUpdateReqDTO;
import com.moonike.admin.dto.resp.ShortlinkGroupRespDTO;
import com.moonike.admin.remote.ShortLinkRemoteService;
import com.moonike.admin.remote.dto.resp.ShortLinkCountQueryRespDTO;
import com.moonike.admin.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短链接分组接口实现层
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomUtil.randomString(6);
        } while (hasGid(gid));
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .name(groupName)
                .username(UserContext.getUsername())
                .sortOrder(0)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    public Boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO groupDO = baseMapper.selectOne(queryWrapper);
        return groupDO != null;
    }

    @Override
    public List<ShortlinkGroupRespDTO> listGroup() {
        // 构建Wrappers
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(List.of(GroupDO::getSortOrder, GroupDO::getUpdateTime));
        // 从数据库中查询分组数据（不含shortLinkCount）
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        if (groupDOList.isEmpty()) {return Collections.emptyList();}
        // 提取gid列表
        List<String> gidList = groupDOList.stream().map(GroupDO::getGid).toList();
        // 远程调用 查询每个分组的短链接数量
        List<ShortLinkCountQueryRespDTO> data = shortLinkRemoteService.listGroupShortLinkCount(gidList).getData();
        // 构建Stream
        Map<String, Integer> shortLinkCountMap = data.stream()
                // 将Stream中的元素转换为Map
                .collect(Collectors.toMap(
                        ShortLinkCountQueryRespDTO::getGid,
                        ShortLinkCountQueryRespDTO::getShortLinkCount)
                );
        // 将除了shortLinkCount外的数据先进行封装
        List<ShortlinkGroupRespDTO> shortlinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortlinkGroupRespDTO.class);
        // 封装shortLinkCount
        shortlinkGroupRespDTOList.forEach(item ->
                item.setShortLinkCount(shortLinkCountMap.getOrDefault(item.getGid(), 0))
        );
        return shortlinkGroupRespDTOList;
    }

    @Override
    public void updateGroup(ShortlinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .set(GroupDO::getUpdateTime, LocalDateTime.now())
                .set(GroupDO::getName, requestParam.getName());
        int updated = baseMapper.update(null, updateWrapper);
        if (updated < 1) {
            throw new ServiceException("修改短链接分组失败");
        }
    }

    @Override
    public void deleteGroup(String gid) {
        /*
            删除一般使用软删除来实现，即 将 del_flag 字段设置为 1
         */
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .set(GroupDO::getUpdateTime, LocalDateTime.now())
                .set(GroupDO::getDelFlag, 1);
        int deleted = baseMapper.update(null, updateWrapper);
        if (deleted < 1) {
            throw new ServiceException("删除短链接分组失败");
        }
    }

    @Override
    public void sortGroup(List<ShortlinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(item -> {
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getGid, item.getGid())
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getDelFlag, 0)
                    .set(GroupDO::getUpdateTime, LocalDateTime.now())
                    .set(GroupDO::getSortOrder, item.getSortOrder());
                    baseMapper.update(null, updateWrapper);
        });
    }
}
