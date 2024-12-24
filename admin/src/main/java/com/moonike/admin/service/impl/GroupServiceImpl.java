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
import com.moonike.admin.dto.req.ShortlinkGroupUpdateReqDTO;
import com.moonike.admin.dto.resp.ShortlinkGroupRespDTO;
import com.moonike.admin.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短链接分组接口实现层
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
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
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(List.of(GroupDO::getSortOrder, GroupDO::getUpdateTime));
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(groupDOList, ShortlinkGroupRespDTO.class);
    }

    @Override
    public void updateGroup(ShortlinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
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
                .set(GroupDO::getDelFlag, 1);
        int deleted = baseMapper.update(null, updateWrapper);
        if (deleted < 1) {
            throw new ServiceException("删除短链接分组失败");
        }
    }
}
