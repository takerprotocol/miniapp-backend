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
public class UserInvitationRelationVo {

	private String inviteeUsername;

	private String inviteePhotoUrl;

	private String rewardValue;

	private Long inviteTime;

	private String inviteeInviterAmount;
	
	
	/**
	 * 是否验证
	 */
	private Boolean verify;
	/**
	 * 用户自定义链接
	 */
	private String personalUrl;
}