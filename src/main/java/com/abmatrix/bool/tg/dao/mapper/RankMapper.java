package com.abmatrix.bool.tg.dao.mapper;

import com.abmatrix.bool.tg.common.model.vo.UserRankVo;

import java.util.List;

/**
 * rank db service
 * 
 * @author PeterWong
 * @date 2024年7月19日
 */
public interface RankMapper {
	/**
	 * 查询用户排行榜
	 * 
	 * @param start
	 * @param limit
	 * @return
	 */
	List<UserRankVo> pageRank(Long start, Long limit);

	/**
	 * 取数量
	 * 
	 * @return
	 */
	Long total();
}
