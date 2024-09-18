package com.abmatrix.bool.tg.dao.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 所有 DO 通用的属性或方法, ID 为 Long 类型
 *
 * @author abm
 */
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseSqlDO implements Serializable {

    private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 要注意注解的局限性, 可能某些场景下会失效
     */
    @Version
    private Long version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;
}
