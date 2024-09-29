package com.abmatrix.bool.tg.model.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 质押请求对象
 * 
 * @author PeterWong
 * @date 2024年7月31日
 */
@Data
public class StakeReq implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 质押数量
	 */
	private List<String> amount;
	/**
	 * 设备ID
	 */
	private List<String> deviceId;
	/**
	 * 用户加密hash
	 */
	private String hash;
	/**
	 * 校验data
	 */
	private String data;
}
