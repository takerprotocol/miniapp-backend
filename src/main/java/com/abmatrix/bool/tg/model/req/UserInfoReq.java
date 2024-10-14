package com.abmatrix.bool.tg.model.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询请求
 * 
 * @author PeterWong
 * @date 2024年8月3日
 */
@Data
public class UserInfoReq implements Serializable {
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
	 * 头像地址
	 */
	private String photoUrl;


	/**
	 * 聊天id
	 */
	private String chatId;

}
