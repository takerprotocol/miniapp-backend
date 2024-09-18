package com.abmatrix.bool.tg.dao.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户邀请关系表
 *
 * @author abm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bool_user_invitation_relation")
public class BoolUserInvitationRelation extends BaseSqlDO {
    private static final long serialVersionUID = 1L;
	/**
     * 邀请人ID
     */
    @TableField("inviter_id")
    private Long inviterId;
    /**
     * 被邀请人ID
     */
    @TableField("invitee_id")
    private Long inviteeId;
    /**
     * 邀请时间
     */
    @TableField("invitation_time")
    private LocalDateTime invitationTime;
    /**
     * 交易哈希
     */
    @TableField("tx_hash")
    private String txHash;
}
