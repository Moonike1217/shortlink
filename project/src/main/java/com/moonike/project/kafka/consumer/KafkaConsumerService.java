package com.moonike.project.kafka.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moonike.project.dao.entity.*;
import com.moonike.project.dao.mapper.*;
import com.moonike.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.moonike.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static com.moonike.project.kafka.constant.KafkaConstant.GROUP_SHORTLINK_STATS;
import static com.moonike.project.kafka.constant.KafkaConstant.TOPIC_SHORTLINK_STATS;

/**
 * kafka 消费者Service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final String LOCK_SHORTLINK_STATS_KEY = "lock:shortlink:stats:%s";

    @Value("${short-link.stats.locate.amap-key}")
    private String statsLocateAmapKey;

    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocateStatsMapper linkLocateStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final ShortLinkMapper shortLinkMapper;
    private final RedissonClient redissonClient;


    @KafkaListener(topics = TOPIC_SHORTLINK_STATS, groupId = GROUP_SHORTLINK_STATS)
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        // 获取消息唯一标识（组合分区+偏移量）
        String messageId = StrUtil.format("{}-{}-{}", record.topic(), record.partition(), record.offset());
        log.info("收到消息 topic:{} messageId:{}", record.topic(), messageId);
        try {
            // 核心处理逻辑
            ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(record.value(), ShortLinkStatsRecordDTO.class);
            actualSaveShortLinkStats(statsRecord);
            ack.acknowledge();
        } catch (Throwable ex) {
            log.error("记录短链接监控消费异常 topic:{}", record.topic(), ex);
            ack.acknowledge(); // 业务异常直接ACK避免死循环
        }
    }


    public void actualSaveShortLinkStats(ShortLinkStatsRecordDTO shortLinkStatsRecordDTO) {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_SHORTLINK_STATS_KEY, shortLinkStatsRecordDTO.getFullShortUrl()));
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try {
            String gid = findGidWithFullShortUrl(shortLinkStatsRecordDTO.getFullShortUrl());
            String fullShortUrl = shortLinkStatsRecordDTO.getFullShortUrl();
            boolean ipFirstFlag = shortLinkStatsRecordDTO.getUipFirstFlag();
            boolean uvFirstFlag = shortLinkStatsRecordDTO.getUvFirstFlag();
            String uv = shortLinkStatsRecordDTO.getUv();
            String actualIP = shortLinkStatsRecordDTO.getRemoteAddr();
            String os = shortLinkStatsRecordDTO.getOs();
            String browser = shortLinkStatsRecordDTO.getBrowser();
            String device = shortLinkStatsRecordDTO.getDevice();
            String network = shortLinkStatsRecordDTO.getNetwork();
            int hour = LocalDateTime.now().getHour();
            int weekValue = LocalDateTime.now().getDayOfWeek().getValue();

            //基础数据统计
            LinkAccessStatsDO linkAccessStatsD0 = LinkAccessStatsDO.builder()
                    .pv(1)
                    // 根据用户首次访问标识uvFirstFlag来确定uv是否增加
                    .uv(uvFirstFlag ? 1 : 0)
                    .uip(ipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(LocalDateTime.now())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsD0);

            // 访问位置统计
            Map<String, Object> requestParam = new HashMap<>();
            requestParam.put("ip", actualIP);
            requestParam.put("key", statsLocateAmapKey);
            // 调用高德地图API获取地理位置信息
            String locateResultStr = HttpUtil.get(AMAP_REMOTE_URL, requestParam);
            JSONObject locateResultObj = JSON.parseObject(locateResultStr);
            String infoCode = locateResultObj.getString("infocode");
            String actualProvince = "未知";
            String actualCity = "未知";
            // infocode为10000时，表示查询成功 执行后续逻辑
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = locateResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocateStatsDO linkLocateStatsDO = LinkLocateStatsDO.builder()
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : locateResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : locateResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(LocalDateTime.now())
                        .build();
                linkLocateStatsMapper.shortLinkLocateStats(linkLocateStatsDO);
            } else {
                log.error("调用高德地图API获取地理位置信息失败");
            }

            // 操作系统统计
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .os(os)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(LocalDateTime.now())
                    .cnt(1)
                    .build();
            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);

            // 浏览器统计
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(browser)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(LocalDateTime.now())
                    .cnt(1)
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);

            // 访问设备统计
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(device)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(LocalDateTime.now())
                    .cnt(1)
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceStats(linkDeviceStatsDO);

            // 访问网络统计
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(network)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(LocalDateTime.now())
                    .cnt(1)
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkStats(linkNetworkStatsDO);

            // 短链接访问统计
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .user(uv)
                    .browser(browser)
                    .os(os)
                    .ip(actualIP)
                    .device(device)
                    .network(network)
                    .locate(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);

            // 总访问数据自增
            shortLinkMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag ? 1 : 0, ipFirstFlag ? 1 : 0);

            // 今日访问数据记录
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(LocalDate.now())
                    .todayPv(1)
                    .todayUv(uvFirstFlag ? 1 : 0)
                    .todayUip(ipFirstFlag ? 1 : 0)
                    .build();
            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
            log.info("数据记录完成");

        } catch (Throwable ex) {
            log.error("统计短链接访问异常", ex);
        } finally {
            rLock.unlock();
        }
    }

    // 通过fullShortUrl查找gid
    private String findGidWithFullShortUrl(String fullShortUrl) {
        ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(new LambdaQueryWrapper<ShortLinkGotoDO>()
                .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
                .last("limit 1"));
        return shortLinkGotoDO == null ? null : shortLinkGotoDO.getGid();
    }
}
