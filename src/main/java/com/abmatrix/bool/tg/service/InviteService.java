package com.abmatrix.bool.tg.service;

import com.abmatrix.bool.tg.common.model.vo.InvitationRankVo;
import com.abmatrix.bool.tg.common.model.vo.UserInvitationRelationVo;
import com.abmatrix.bool.tg.common.model.vo.UserRankVo;
import com.abmatrix.bool.tg.model.req.UserInvitersRankReq;
import com.abmatrix.bool.tg.model.resp.CustomPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 邀请数据接口
 * 
 * @author PeterWong
 * @date 2024年7月16日
 */
public interface InviteService {
	/**
	 * 分页查询要求数据
	 * 
	 * @param userId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	Page<UserInvitationRelationVo> pageInviteRecords(Long userId, Integer pageNo, Integer pageSize, Integer level);

	/**
	 * 查询积分排行
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	CustomPage<UserRankVo> rank(Integer pageNo, Integer pageSize);

	Page<InvitationRankVo> inviterRank(Integer pageNo, Integer pageSize, UserInvitersRankReq req);

	/**
	 * 用户邀请排名总榜
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	Page<InvitationRankVo> inviterRankFull(Integer pageNo, Integer pageSize);

	/**
	 * 用户邀请周榜
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	Page<InvitationRankVo> inviterRankWeek(Integer pageNo, Integer pageSize);
}
