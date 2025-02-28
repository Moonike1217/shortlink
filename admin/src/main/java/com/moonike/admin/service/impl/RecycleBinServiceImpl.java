package com.moonike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.protobuf.ServiceException;
import com.moonike.admin.common.biz.user.UserContext;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.dao.entity.GroupDO;
import com.moonike.admin.dao.mapper.GroupMapper;
import com.moonike.admin.remote.ShortLinkActualRemoteService;
import com.moonike.admin.remote.dto.req.ShortLinkPageRecycleBinReqDTO;
import com.moonike.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.moonike.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短链接回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final GroupMapper groupMapper;

    @Override
    @SneakyThrows
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink() {
        // 查询当前用户是否有分组
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (groupDOList.isEmpty()) {
            throw new ServiceException("用户分组为空");
        }
        // 用户有分组 封装gidList
        List<String> gidList = groupDOList.stream().map(GroupDO::getGid).toList();
        ShortLinkPageRecycleBinReqDTO requestParam = new ShortLinkPageRecycleBinReqDTO();
        requestParam.setGidList(gidList);
        // 远程调用
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
