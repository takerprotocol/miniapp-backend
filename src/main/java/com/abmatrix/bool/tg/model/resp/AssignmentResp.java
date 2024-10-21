package com.abmatrix.bool.tg.model.resp;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务返回值
 * 
 * @author PeterWong
 * @date 2024年8月3日
 */
@Data
public class AssignmentResp implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键
	 */
	private Long assignmentId;

	/**
	 * 任务标题
	 */
	private String title;
	/**
	 * 描述
	 */
	private String describe;

	/**
	 * 跳转链接
	 */
	private String url;

	/**
	 * 是否完成
	 */
	private Boolean done;
	/**
	 * 奖励
	 */
	private String reward;
	/**
	 * 项目方
	 */
	private String project;
	/**
	 * logo
	 */
	private String logo;
	/**
	 * 完成数量
	 */
	private String complete;
	/**
	 * 是否是置顶项目
	 */
	private Boolean top;
	/**
	 * 任务变更时间戳
	 */
	private Long timestamp;
	/**
	 * 完成时间
	 */
	private String completeTime;
	/**
	 * 排序编号
	 */
	private Integer sort;
}
