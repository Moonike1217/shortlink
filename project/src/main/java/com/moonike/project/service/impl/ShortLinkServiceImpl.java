package com.moonike.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.project.common.convention.exception.ClientException;
import com.moonike.project.common.convention.exception.ServiceException;
import com.moonike.project.common.enums.ValidDateTypeEnum;
import com.moonike.project.dao.entity.LinkAccessStatsDO;
import com.moonike.project.dao.entity.LinkLocateStatsDO;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dao.entity.ShortLinkGotoDO;
import com.moonike.project.dao.mapper.LinkAccessStatsMapper;
import com.moonike.project.dao.mapper.LinkLocateStatsMapper;
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
import com.moonike.project.tookit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.moonike.project.common.constant.RedisKeyConstant.*;
import static com.moonike.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * 短链接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocateStatsMapper linkLocateStatsMapper;

    @Value("${short-link.stats.locate.amap-key}")
    private String statsLocateAmapKey;

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 生成一个短链接
        String shortLinkSuffix = generateSuffix(requestParam);
        // 拼接完整链接
        String fullShortUrl = requestParam.getDomain() + "/" + shortLinkSuffix;
        // 拷贝基础信息
        ShortLinkDO shortLinkDO = BeanUtil.copyProperties(requestParam, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setFullShortUrl(requestParam.getDomain() + "/" + shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));

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
        //缓存预热，防止缓存雪崩
        stringRedisTemplate.opsForValue().set(
                StrUtil.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                //设置key的过期时间
                LinkUtil.getShortLinkCacheTime(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS
        );
        // 返回创建成功的短链接对象
        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkDO.getGid())
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl())
                .build();
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(item -> BeanUtil.toBean(item, ShortLinkPageRespDTO.class));
    }

    /**
     * 统计某一分组内短链接数量
     * @param requestParam 查询短链接分组内短链接数量请求参数
     * @return
     */
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

    /**
     * 修改短链接信息
     * @param requestParam 修改短链接信息请求参数
     */
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

    /**
     * 短链接跳转原链接
     * @param shortUri 短链接后缀
     * @param request  HTTP请求
     * @param response HTTP响应
     * @throws IOException
     */
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        // 拼接fullShortUrl
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        // 先在缓存中查询原链接
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            // 存在原链接，直接重定向
            shortLinkStats(fullShortUrl, null, request, response);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        // 不存在原链接，为了防止缓存穿透，需要先查询布隆过滤器，然后查询Redis中是否缓存了对应key的空值
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        String s = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(s)) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 查询数据库，需要先获取redisson分布式锁保证线程安全
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // Double-Checked Lock 双重判定锁，防止多个请求达到数据库，保证只有第一个失效请求达到数据库，解决缓存击穿
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                shortLinkStats(fullShortUrl, null, request, response);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            // 根据fullShortUrl在t_link_goto表中查询Gid
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(
                    Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                            .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
            );
            if (shortLinkGotoDO == null) {
                // 在goto表里查不到对应的短链接 重定向到notfound页面
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-");
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
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
                // 查询结果不为空，首先判断查询结果是否过期
                if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().isBefore(LocalDateTime.now())) {
                    // 页面过期 重定向到Notfound页面
                    stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-");
                    ((HttpServletResponse) response).sendRedirect("/page/notfound");
                    return;
                }
                // 将查询结果存入缓存，然后重定向到原链接
                stringRedisTemplate.opsForValue().set(
                        StrUtil.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortLinkDO.getOriginUrl(),
                        //设置key的过期时间
                        LinkUtil.getShortLinkCacheTime(shortLinkDO.getValidDate()),
                        TimeUnit.MILLISECONDS
                );
                // 记录统计数据
                shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
                // 重定向
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 生成短链接后缀
     * @param requestParam
     * @return
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        // 重试次数
        int customSuffixCount = 0;
        String shortLinkSuffix = null;
        String domain = requestParam.getDomain() + "/";
        while (customSuffixCount < 10) {
            // 加盐降低冲突概率
            shortLinkSuffix = HashUtil.hashToBase62(requestParam.getOriginUrl() + UUID.fastUUID().toString());
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

    /**
     * 获取网站favicon
     * @param url
     * @return
     */
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    /**
     * 短链接访问情况统计
     * @param fullShortUrl
     * @param gid
     * @param request
     * @param response
     */
    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        // 用户首次访问标识
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
            Runnable addResponseCookieTask = () -> {
                // 用户首次访问 发放uvCookie
                String uv = UUID.fastUUID().toString();
                Cookie uvCookie = new Cookie("uv", uv);
                uvCookie.setMaxAge(60 * 60 * 24 * 365);
                // 注意:StrUtil的sub是左闭右开的
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf('/'), fullShortUrl.length()));
                // 设置用户首次访问标识
                uvFirstFlag.set(Boolean.TRUE);
                ((HttpServletResponse) response).addCookie(uvCookie);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv);
            };
            if (ArrayUtil.isEmpty(cookies)) {
                // cookies数组为空 用户首次访问 执行addResponseCookieTask
                addResponseCookieTask.run();
            } else {
                // cookies数组不为空 尝试获取uvCookie
                Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals("uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(uv -> {
                            // uvCookie存在 非首次访问
                            Long uvAdded = stringRedisTemplate.opsForSet().add(UV_STATS_SHORT_LINK_KEY + fullShortUrl, uv);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask); // 首次访问 执行addResponseCookieTask
            }
            String actualIP = LinkUtil.getActualIP((HttpServletRequest)request);
            Long ipAdded = stringRedisTemplate.opsForSet().add(UIP_STATS_SHORT_LINK_KEY + fullShortUrl, actualIP);
            boolean ipFirstFlag = ipAdded != null && ipAdded > 0L;

            if (gid == null) {
                LambdaQueryWrapper<ShortLinkGotoDO> wrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(wrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = LocalDateTime.now().getHour();
            int weekValue = LocalDateTime.now().getDayOfWeek().getValue();
            LinkAccessStatsDO linkAccessStatsD0 = LinkAccessStatsDO.builder()
                    .pv(1)
                    // 根据用户首次访问标识uvFirstFlag来确定uv是否增加
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(ipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(LocalDateTime.now())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsD0);
            Map<String, Object> requestParam = new HashMap<>();
            requestParam.put("ip", actualIP);
            requestParam.put("key", statsLocateAmapKey);
            String locateResultStr = HttpUtil.get(AMAP_REMOTE_URL, requestParam);
            JSONObject locateResultObj = JSON.parseObject(locateResultStr);
            String infoCode = locateResultObj.getString("infocode");
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = locateResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocateStatsDO linkLocateStatsDO = LinkLocateStatsDO.builder()
                        .province(unknownFlag ? "未知" : province)
                        .city(unknownFlag ? "未知" : locateResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : locateResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(LocalDateTime.now())
                        .build();
                linkLocateStatsMapper.shortLinkLocateStats(linkLocateStatsDO);
            }
        } catch (Throwable ex) {
            log.error("统计短链接访问异常", ex);
        }
    }
}
