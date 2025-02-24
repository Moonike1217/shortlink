package com.moonike.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moonike.project.dao.entity.LinkLocateStatsDO;
import com.moonike.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区统计访问持久层
 */
public interface LinkLocateStatsMapper extends BaseMapper<LinkLocateStatsDO> {
    /**
     * 记录地区访问监控数据
     */
    @Insert("INSERT INTO " +
            "t_link_locate_stats(full_short_url, gid, date, cnt, country, province, city, adcode, create_time, update_time, del_flag) " +
            "VALUES( #{linkLocateStats.fullShortUrl}, #{linkLocateStats.gid}, #{linkLocateStats.date}, #{linkLocateStats.cnt}, #{linkLocateStats.country}, #{linkLocateStats.province}, #{linkLocateStats.city}, #{linkLocateStats.adcode}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkLocateStats.cnt};")
    void shortLinkLocateStats(@Param("linkLocateStats") LinkLocateStatsDO linkLocateStatsDO);

    /**
     * 根据短链接获取指定日期内地区监控数据
     */
    @Select("SELECT " +
            "    tlls.province, " +
            "    SUM(tlls.cnt) AS cnt " +
            "FROM " +
            "    t_link tl INNER JOIN " +
            "    t_link_locate_stats tlls ON tl.full_short_url = tlls.full_short_url " +
            "WHERE " +
            "    tlls.full_short_url = #{param.fullShortUrl} " +
            "    AND tl.gid = #{param.gid} " +
            "    AND tl.del_flag = '0' " +
            "    AND tl.enable_status = #{param.enableStatus} " +
            "    AND tlls.date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    tlls.full_short_url, tl.gid, tlls.province;")
    List<LinkLocateStatsDO> listLocateByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
//
//    /**
//     * 根据分组获取指定日期内地区监控数据
//     */
//    @Select("SELECT " +
//            "    tlls.province, " +
//            "    SUM(tlls.cnt) AS cnt " +
//            "FROM " +
//            "    t_link tl INNER JOIN " +
//            "    t_link_locale_stats tlls ON tl.full_short_url = tlls.full_short_url " +
//            "WHERE " +
//            "    tl.gid = #{param.gid} " +
//            "    AND tl.del_flag = '0' " +
//            "    AND tl.enable_status = '0' " +
//            "    AND tlls.date BETWEEN #{param.startDate} and #{param.endDate} " +
//            "GROUP BY " +
//            "    tl.gid, tlls.province;")
//    List<LinkLocaleStatsDO> listLocaleByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
