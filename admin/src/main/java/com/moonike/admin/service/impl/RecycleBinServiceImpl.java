package com.moonike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.protobuf.ServiceException;
import com.moonike.admin.common.biz.user.UserContext;
import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.dao.entity.GroupDO;
import com.moonike.admin.dao.mapper.GroupMapper;
import com.moonike.admin.remote.ShortLinkRemoteService;
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

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};
    private final GroupMapper groupMapper;

    @Override
    @SneakyThrows
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink() {
        // 构建Wrappers
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (groupDOList.isEmpty()) {
            throw new ServiceException("用户分组为空");
        }
        // 提取gidList
        List<String> gidList = groupDOList.stream().map(GroupDO::getGid).toList();
        ShortLinkPageRecycleBinReqDTO requestParam = new ShortLinkPageRecycleBinReqDTO();
        // 封装gidList
        requestParam.setGidList(gidList);
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
