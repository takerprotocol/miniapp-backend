package com.abmatrix.bool.tg.model.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务请求
 * 
 * @author PeterWong
 * @date 2024年7月23日
 */
@Data
public class AssignmentReq implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 用户加密hash
	 */
	private String hash;
	/**
	 * 校验data
	 */
	private String data;
	/**
	 * 任务ID
	 */
	private Long assignmentId;
}
