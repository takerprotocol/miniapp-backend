package com.abmatrix.bool.tg.common.model.vo;

import com.abmatrix.bool.tg.common.enuma.RewardTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author abm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {

	private String evmAddress;

	private String userId;

	private String username;

	private String photoUrl;

	private String inviterCode;

	private String rank;

	private Long inviterCount;

	private String rewardValue;

	private String score;

	private Map<RewardTypeEnum, String> rewardMap;

	/**
	 * 是否认证为有价值用户
	 */
	private Boolean isVerify;
	/**
	 * 个人url地址
	 */
	private String personalUrl;
}
