package com.abmatrix.bool.tg.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author abm
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankVo {

	private String username;

	private String rank;

	private String rewardValue;
	/**
	 * 用户id
	 */
	private Long userId;
	
	/**
	 * 个人url地址
	 */
	private String personalUrl;
}
