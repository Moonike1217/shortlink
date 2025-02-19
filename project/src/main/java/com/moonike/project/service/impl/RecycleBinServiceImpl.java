package com.moonike.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dao.entity.ShortLinkGotoDO;
import com.moonike.project.dao.mapper.ShortLinkGotoMapper;
import com.moonike.project.dao.mapper.ShortLinkMapper;
import com.moonike.project.dto.req.RecoverLinkFromRecycleBinReqDTO;
import com.moonike.project.dto.req.RemoveLinkFromRecycleBinReqDTO;
import com.moonike.project.dto.req.SaveLinkToRecycleBinReqDTO;
import com.moonike.project.dto.req.ShortLinkPageRecycleBinReqDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;
import com.moonike.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.moonike.project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static com.moonike.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private StringRedisTemplate stringRedisTemplate;
    private final ShortLinkGotoMapper shortLinkGotoMapper;

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

    /**
     * 将短链接从回收站移出
     * @param requestParam 恢复短链接请求参数
     */
    @Override
    public void recoverLinkFromRecycleBin(RecoverLinkFromRecycleBinReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0)
                .set(ShortLinkDO::getEnableStatus, 0);
        baseMapper.update(null, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    /**
     * 从回收站删除短链接
     * @param requestParam
     */
    @Override
    public void removeLinkFromRecycleBin(RemoveLinkFromRecycleBinReqDTO requestParam) {
        // 逻辑删除t_link表中的数据
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                // 短链接必须要处于禁用状态才可以删除
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0)
                .set(ShortLinkDO::getDelFlag, 1);
        baseMapper.update(null, updateWrapper);
        // 删除redis中的数据
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
        // 删除t_link_goto表中的数据
        LambdaQueryWrapper<ShortLinkGotoDO> deleteWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkGotoDO::getGid, requestParam.getGid());
        shortLinkGotoMapper.delete(deleteWrapper);
    }
}
