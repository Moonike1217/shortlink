package com.moonike.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dao.mapper.ShortLinkMapper;
import com.moonike.project.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.project.dto.req.ShortLinkPageRecycleBinReqDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;
import com.moonike.project.service.RecycleBinService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.moonike.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站接口实现层
 */
@Service
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private StringRedisTemplate stringRedisTemplate;

    /**
     * 将短链接移至回收站
     * @param requestParam
     */
    @Override
    public void saveLinkToRecycleBin(SaveLinkToRecycleBinReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                // 将短链接启用字段设置为1(即:禁用)
                .set(ShortLinkDO::getEnableStatus, 1);
        baseMapper.update(null, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    /**
     * 分页查询回收站内短链接
     * @param requestParam 分页查询回收站内短链接请求参数
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageRecycleBinShortLink(ShortLinkPageRecycleBinReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, requestParam.getGidList())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(item -> BeanUtil.toBean(item, ShortLinkPageRespDTO.class));
    }
}
