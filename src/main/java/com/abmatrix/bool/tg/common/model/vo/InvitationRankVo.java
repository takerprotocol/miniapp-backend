package com.abmatrix.bool.tg.common.model.vo;

import com.abmatrix.bool.tg.common.constants.NumberConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationRankVo {
	private String userId;
	private String userName;
	private String photoUrl;
	private Long inviterNumbers;
	private String inviterCode;
	private BigDecimal inviterIntegral;
	/**
	 * 一级邀请
	 */
	private Long inviterNumbers1;
	/**
	 * 二级邀请
	 */
	private Long inviterNumbers2;

	/**
	 * 排名
	 */
	private Long ranking;

	/**
	 * 是否验证
	 */
	private Boolean verify;

	/**
	 * 用户自定义链接
	 */
	private String personalUrl;

	/**
	 * 奖励
	 */
	private Long award = NumberConstants.ZERO_LONG;

}
