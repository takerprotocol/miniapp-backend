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
 * 用户邀请数量周维度快照表
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-22
 */
@Getter
@Setter
@TableName("bool_user_invite_count_week_snapshot")
public class BoolUserInviteCountWeekSnapshot extends Model<BoolUserInviteCountWeekSnapshot> {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 版本号
	 */
	@TableField("version")
	private Long version;

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

	/**
	 * 用户ID
	 */
	@TableField("f_user_id")
	private Long userId;

	/**
	 * 邀请人数量1级+2级
	 */
	@TableField("f_amount")
	private Long amount;

	/**
	 * 业务时间
	 */
	@TableField("f_caculate_timestamp")
	private Long caculateTimestamp;

	/**
	 * 一级邀请人数
	 */
	@TableField("f_amount_1")
	private Long amount1;

	/**
	 * 二级邀请人数
	 */
	@TableField("f_amount_2")
	private Long amount2;
	/**
	 * 最晚邀请人邀请时间
	 */
	@TableField("f_latest_invitation_timestamp")
	private Long latestInvitationTimestamp;

	@Override
	public Serializable pkVal() {
		return this.id;
	}
}
