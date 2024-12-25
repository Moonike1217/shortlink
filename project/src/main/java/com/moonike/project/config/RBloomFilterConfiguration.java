package com.moonike.project.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 */
@Configuration
public class RBloomFilterConfiguration {

    /**
     * 防止短链接生成重复
     */
    @Bean
    public RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient
                .getBloomFilter("shortUriCreateCachePenetrationBloomFilter");
        cachePenetrationBloomFilter.tryInit(
                100000000,   // 预计布隆过滤器存储的元素数量
                  0.001    // 误判率
        );
        return cachePenetrationBloomFilter;
    }
}