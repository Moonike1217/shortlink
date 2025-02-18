package com.moonike.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moonike.project.dao.entity.ShortLinkDO;
import lombok.Data;

/**
 * 短链接分页查询请求对象
 */
@Data
public class ShortLinkPageReqDTO extends Page<ShortLinkDO> {

    private String gid;

}
