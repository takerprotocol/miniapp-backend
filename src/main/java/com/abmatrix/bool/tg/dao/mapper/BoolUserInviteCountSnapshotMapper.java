package com.abmatrix.bool.tg.dao.mapper;

import com.abmatrix.bool.tg.dao.entity.BoolUserInviteCountSnapshot;
import com.abmatrix.bool.tg.model.resp.UserInviteRankResp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户邀请数量全量快照表 Mapper 接口
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-22
 */
public interface BoolUserInviteCountSnapshotMapper extends BaseMapper<BoolUserInviteCountSnapshot> {
	/**
	 * 全量邀请数量数据初始化
	 * 
	 * @param snapList
	 */
	void batchSaveUserInviteCountForFull(@Param("list") List<BoolUserInviteCountSnapshot> snapList);

	/**
	 * 增量邀请数量数据变更
	 * 
	 * @param snapList
	 */
	void batchSaveUserInviteCountForIncrease(@Param("list") List<BoolUserInviteCountSnapshot> snapList);

	/**
	 * 查询前500名全量榜单
	 * 
	 * @return
	 */
	List<UserInviteRankResp> queryTop500Full(@Param("verified") Byte verified);
}
