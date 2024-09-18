package com.abmatrix.bool.tg.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 任务用户关联关系表
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-03
 */
@Getter
@Setter
@TableName("bool_assignment_user_relation")
public class BoolAssignmentUserRelation extends Model<BoolAssignmentUserRelation> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField("f_assignment_id")
    private Long assignmentId;

    /**
     * 用户ID
     */
    @TableField("f_user_id")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField("create_user")
    private Long createUser;

    /**
     * 修改人
     */
    @TableField("update_user")
    private Long updateUser;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
