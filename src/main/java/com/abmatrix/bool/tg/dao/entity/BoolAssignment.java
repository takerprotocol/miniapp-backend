package com.abmatrix.bool.tg.dao.entity;

import com.abmatrix.bool.tg.common.enuma.AssignmentTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 小任务表
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-03
 */
@Getter
@Setter
@TableName("bool_assignment")
public class BoolAssignment extends Model<BoolAssignment> {

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
	 * 任务名称
	 */
	@TableField("f_name")
	private String name;

	/**
	 * 任务标题
	 */
	@TableField("f_title")
	private String title;

	/**
	 * 描述
	 */
	@TableField("f_describe")
	private String assignmentDesc;

	/**
	 * 跳转链接
	 */
	@TableField("f_url")
	private String url;

	/**
	 * 是否在线 0不在线 1在线
	 */
	@TableField("f_online")
	private Integer online;

	/**
	 * 携带标志
	 */
	@TableField("f_flag")
	private String assignmentFlag;
	/**
	 * 任务类别
	 */
	@TableField("f_type")
	private AssignmentTypeEnum assignmentType;
	/**
	 * 奖励分数
	 */
	@TableField("reward_value")
	private BigDecimal rewardValue;
	/**
	 * 排序字段
	 */
	@TableField("sort_field")
	private Integer sortField;
	/**
	 * 项目方
	 */
	@TableField("f_project_item")
	private String project;
	/**
	 * 项目方logo
	 */
	@TableField("f_project_logo")
	private String logo;
	/**
	 * 是否置顶
	 */
	@TableField("top")
	private Boolean top;

	/**
	 * 指定设置时间
	 */
	@TableField("top_time")
	private LocalDateTime topTime;

	@Override
	public Serializable pkVal() {
		return this.id;
	}
}
