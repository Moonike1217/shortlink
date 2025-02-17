package com.moonike.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.project.common.convention.exception.ClientException;
import com.moonike.project.common.convention.exception.ServiceException;
import com.moonike.project.common.enums.ValidDateTypeEnum;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dao.entity.ShortLinkGotoDO;
import com.moonike.project.dao.mapper.ShortLinkGotoMapper;
import com.moonike.project.dao.mapper.ShortLinkMapper;
import com.moonike.project.dto.req.ShortLinkCreateReqDTO;
import com.moonike.project.dto.req.ShortLinkPageReqDTO;
import com.moonike.project.dto.req.ShortLinkUpdateReqDTO;
import com.moonike.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.moonike.project.dto.resp.ShortLinkCreateRespDTO;
import com.moonike.project.dto.resp.ShortLinkPageRespDTO;
import com.moonike.project.service.ShortLinkService;
import com.moonike.project.tookit.HashUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 短链接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkGotoMapper shortLinkGotoMapper;

    // 新建短链接
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 生成一个短链接
        String shortLinkSuffix = generateSuffix(requestParam);
        // 拼接完整链接
        String fullShortUrl = requestParam.getDomain() + "/" + shortLinkSuffix;
        // 拷贝基础信息
        ShortLinkDO shortLinkDO = BeanUtil.copyProperties(requestParam, ShortLinkDO.class);
        // 将短链接封装到返回对象中
        shortLinkDO.setShortUri(shortLinkSuffix);
        // 将完整链接封装到返回对象中
        shortLinkDO.setFullShortUrl(requestParam.getDomain() + "/" + shortLinkSuffix);
        // 新生成的短链接默认为启用状态
        shortLinkDO.setEnableStatus(0);

        // 创建短链接跳转实体，用来插入到t_link_goto表中
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            // 尝试向数据库中插入记录
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("存在相同的短链接！");
        } catch (Exception e) {
            log.info(e.toString());
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

    // 分页查询短链接
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(item -> BeanUtil.toBean(item, ShortLinkPageRespDTO.class));
    }

    // 统计某一分组内短链接数量
    @Override
    public List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.<ShortLinkDO>query()
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkCountQueryRespDTO.class);
    }

    // 编辑短链接
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // 根据DTO中的originGid字段 查询数据库中是否存在该短链接记录
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            // 数据库中不存在该记录
            throw new ClientException("短链接记录不存在");
        }

        // 构造修改结果
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();

        // 数据库中存在该记录
        // 判断gid是否进行了修改 如果进行了修改 则需要删除并重新生成记录
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            // gid未修改 可对原记录进行修改
            // 构造修改条件
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            // 修改记录
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            // gid进行了修改 需要先根据originGid删除原记录 再新建记录
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
        }
    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        // 根据fullShortUrl在t_link_goto表中查询Gid
        ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(
                Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
        );
        //判断是否存在
        if (shortLinkGotoDO == null) {
            return;
        }
        // 构造查询条件
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        // 根据前面查到的gid，查询对应链接
        ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
        if (shortLinkDO != null) {
            // 查询结果不为空，重定向到原链接
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        }
    }

    // 生成短链接
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
