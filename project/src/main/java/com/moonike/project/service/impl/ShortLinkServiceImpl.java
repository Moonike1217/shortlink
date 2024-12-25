package com.moonike.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.project.common.convention.exception.ServiceException;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dao.mapper.ShortLinkMapper;
import com.moonike.project.dto.req.ShortLinkCreateReqDTO;
import com.moonike.project.dto.req.ShortLinkPageReqDTO;
import com.moonike.project.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;
import com.moonike.project.service.ShortLinkService;
import com.moonike.project.tookit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 短链接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 生成一个短链接
        String shortLinkSuffix = generateSuffix(requestParam);
        // 完整链接
        String fullShortUrl = requestParam.getDomain() + "/" + shortLinkSuffix;
        // 拷贝基础信息
        ShortLinkDO shortLinkDO = BeanUtil.copyProperties(requestParam, ShortLinkDO.class);
        // 将短链接封装到返回对象中
        shortLinkDO.setShortUri(shortLinkSuffix);
        // 将完整链接封装到返回对象中
        shortLinkDO.setFullShortUrl(requestParam.getDomain() + "/" + shortLinkSuffix);
        try {
            // 尝试向数据库中插入记录
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("存在相同的短链接！");
        } catch (Exception e) {
            throw new ServiceException("短链接创建失败");
        }
        // 将完整链接添加到布隆过滤器
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        // 返回创建成功的短链接对象
        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkDO.getGid())
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(item -> BeanUtil.toBean(item, ShortLinkPageRespDTO.class));
    }

    public String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        // 重试次数
        int customSuffixCount = 0;
        String shortLinkSuffix = null;
        String domain = requestParam.getDomain() + "/";
        while (customSuffixCount < 10) {
            // 加盐降低冲突概率
            shortLinkSuffix = HashUtil.hashToBase62(requestParam.getOriginUrl() + UUID.randomUUID().toString());
            if (!shortUriCreateCachePenetrationBloomFilter.contains(domain + shortLinkSuffix)) {
                // 如果生成的短链接不在布隆过滤器中，则证明生成成功，返回生成的短链接
                return shortLinkSuffix;
            }
            // 生成失败 重试
            customSuffixCount++;
        }
        // 10次生成失败，抛出异常
        throw new ServiceException("短链接生成失败，请稍后再试");
    }
}
