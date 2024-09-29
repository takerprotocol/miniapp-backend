package com.abmatrix.bool.tg.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 质押申请记录，只要来这里拼过交易都会记录一笔，无论最终是否提交上链
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-01
 */
@Getter
@Setter
@TableName("bool_stake_apply_record")
public class BoolStakeApplyRecord extends Model<BoolStakeApplyRecord> {

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
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 修改时间
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
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
	 * 用户id
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 数量，多个以,分隔
	 */
	@TableField("amounts")
	private String amounts;

	/**
	 * 设备id,多个以,分隔
	 */
	@TableField("deviceids")
	private String deviceids;
	/**
	 * 质押人地址
	 */
	@TableField("f_address")
	private String address;

	@Override
	public Serializable pkVal() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, amounts, createTime, deviceids, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoolStakeApplyRecord other = (BoolStakeApplyRecord) obj;
		return Objects.equals(address, other.address) && Objects.equals(amounts, other.amounts)
				&& Objects.equals(createTime, other.createTime) && Objects.equals(deviceids, other.deviceids)
				&& Objects.equals(userId, other.userId);
	}
	
	
	
	
}
