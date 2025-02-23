package com.moonike.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接基础访问监控响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsAccessDailyRespDTO {

    /**
     * 日期
     */
    private String date;

    /**
     * 当天访问量
     */
    private Integer pv;

    /**
     * 当天独立访客数
     */
    private Integer uv;

    /**
     * 当天独立IP数
     */
    private Integer uip;
}