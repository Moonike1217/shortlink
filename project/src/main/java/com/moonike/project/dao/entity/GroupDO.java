package com.moonike.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moonike.admin.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接分组实体
 */
@Data
@TableName("t_group")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDO extends BaseDO {

    /**
    * ID
    */
    private Long id;

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 分组名称
    */
    private String name;

    /**
    * 创建分组用户名
    */
    private String username;

    /**
    * 分组排序
    */
    private Integer sortOrder;
}