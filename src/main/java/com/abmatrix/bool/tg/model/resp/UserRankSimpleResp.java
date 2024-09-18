package com.abmatrix.bool.tg.model.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户简易积分排名对象
 * 
 * @author PeterWong
 * @date 2024年8月12日
 */
@Data
public class UserRankSimpleResp implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 用户id
	 */
	private Long userId;
	/**
	 * 积分总额
	 */
	private String rewardValue;
	/**
	 * 用户名
	 */
	private String username;
}
