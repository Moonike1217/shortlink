package com.moonike.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moonike.project.dao.entity.ShortLinkDO;
import com.moonike.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 短链接访问统计自增
     */
    void incrementStats(@Param("gid") String gid,
                        @Param("fullShortUrl") String fullShortUrl,
                        @Param("totalPv") Integer totalPv,
                        @Param("totalUv") Integer totalUv,
                        @Param("totalUip") Integer totalUip);

    /**
     * 分页统计短链接
     * @param requestParam 请求参数
     * @return 分页统计结果
     */
    IPage<ShortLinkDO> pageLink(ShortLinkPageReqDTO requestParam);
}
