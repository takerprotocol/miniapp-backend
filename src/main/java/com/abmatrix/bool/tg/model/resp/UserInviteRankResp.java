package com.abmatrix.bool.tg.model.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户邀请人数排名
 * 
 * @author PeterWong
 * @date 2024年8月22日
 */
@Data
public class UserInviteRankResp implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 榜单快照ID
	 */
	private Long snapId;
	/**
	 * 用户ID
	 */
	private Long userId;
	/**
	 * 邀请人数量
	 */
	private Long amount;
	/**
	 * 一级邀请人数量
	 */
	private Long amount1;
	/**
	 * 二级邀请人数量
	 */
	private Long amount2;
	/**
	 * 名次
	 */
	private Long ranking;

	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 是否验证
	 */
	private Boolean verify;
	/**
	 * 用户自定义链接
	 */
	private String personalUrl;
}
