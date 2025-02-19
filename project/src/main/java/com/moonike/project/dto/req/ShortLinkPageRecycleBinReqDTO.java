package com.moonike.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moonike.project.dao.entity.ShortLinkDO;
import lombok.Data;

import java.util.List;

/**
 * 短链接回收站分页查询请求对象
 */
@Data
public class ShortLinkPageRecycleBinReqDTO extends Page<ShortLinkDO> {
    /**
     * 短链接分组标识集合
     */
    private List<String> gidList;
}
