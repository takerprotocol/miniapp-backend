package com.abmatrix.bool.tg.model.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户周邀请计算请求
 * 
 * @author PeterWong
 * @date 2024年9月2日
 */
@Data
public class UserInviteRankReq implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 计算周邀请
	 */
	private Long timestamp;
}
