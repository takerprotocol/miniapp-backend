package com.abmatrix.bool.tg.dao.entity;

import com.abmatrix.bool.tg.common.enuma.PersonalInfoTypeEnum;
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
 * 用户自定义信息
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-08
 */
@Getter
@Setter
@TableName("bool_user_custom_info")
public class BoolUserCustomInfo extends Model<BoolUserCustomInfo> {

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
	 * 1 自定义url 还有其它未补充
	 */
	@TableField("f_type")
	private PersonalInfoTypeEnum personalInfoType;

	/**
	 * 自定义值
	 */
	@TableField("f_value")
	private String personalInfoValue;

	/**
	 * 用户id
	 */
	@TableField("f_user_id")
	private Long userId;

	@Override
	public Serializable pkVal() {
		return this.id;
	}
}
