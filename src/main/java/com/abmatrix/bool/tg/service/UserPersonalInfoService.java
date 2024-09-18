package com.abmatrix.bool.tg.service;

import com.abmatrix.bool.tg.model.req.UserPersonalInfoReq;

/**
 * 用户个人信息接口
 * 
 * @author PeterWong
 * @date 2024年8月8日
 */
public interface UserPersonalInfoService {
	/**
	 * 添加用户个人信息
	 * 
	 * @param req
	 */
	void addUserPersonalInfo(UserPersonalInfoReq req);
}
