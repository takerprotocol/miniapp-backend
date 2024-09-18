package com.abmatrix.bool.tg.dao.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户表
 *
 * @author abm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bool_user_user")
public class BoolUser extends BaseSqlDO {
    private static final long serialVersionUID = 1L;
	/**
     * TG ID
     */
    @TableField("user_tg_id")
    private Long userTgId;
    /**
     * TG First Name
     */
    @TableField("first_name")
    private String firstName;
    /**
     * TG Last Name
     */
    @TableField("last_name")
    private String lastName;
    /**
     * TG Username
     */
    @TableField("username")
    private String username;
    /**
     * 用户专属邀请码
     */
    @TableField("invitation_code")
    private String invitationCode;
    /**
     * 介绍
     */
    @TableField("introduction")
    private String introduction;
    /**
     * 额外信息
     */
    @TableField("additional_info")
    private String additionalInfo;
    /**
     * evm地址
     */
    @TableField("address")
    private String address;

    /**
     * 获得的总奖励数量
     */
    @TableField("reward_amount")
    private BigDecimal rewardAmount;
}
