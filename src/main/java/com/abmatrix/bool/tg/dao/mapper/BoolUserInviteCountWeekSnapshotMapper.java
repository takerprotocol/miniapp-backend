package com.abmatrix.bool.tg.dao.mapper;

import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountWeekSnapshot;
import com.abmatrix.bool.tg.model.resp.UserInviteRankResp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户邀请数量周维度快照表 Mapper 接口
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-22
 */
public interface BoolUserInviteCountWeekSnapshotMapper extends BaseMapper<BoolUserInviteCountWeekSnapshot> {
	/**
	 * 全量
	 * 
	 * @param snapList
	 */
	void batchSaveInviteCountWeekSnapshotForFull(List<BoolUserInviteCountWeekSnapshot> snapList);

	/**
	 * 查询前500名周榜单
	 *
	 * @return
	 */
	List<UserInviteRankResp> queryTop500Week(@Param("timestamp") Long timestamp, @Param("verified") Byte verified);
}
