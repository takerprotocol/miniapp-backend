package com.abmatrix.bool.tg.model.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户个人额外信息req
 * 
 * @author PeterWong
 * @date 2024年8月8日
 */
@Data
public class UserPersonalInfoReq implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 用户个人地址
	 */
	private String personalUrl;
	/**
	 * 用户加密hash
	 */
	private String hash;
	/**
	 * 校验data
	 */
	private String data;
}
