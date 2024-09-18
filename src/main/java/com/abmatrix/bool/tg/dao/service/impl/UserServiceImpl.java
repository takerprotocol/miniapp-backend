package com.abmatrix.bool.tg.dao.service.impl;

import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.dao.entity.BoolUser;
import com.abmatrix.bool.tg.dao.mapper.UserMapper;
import com.abmatrix.bool.tg.dao.service.IUserService;
import com.abmatrix.bool.tg.middleware.redis.clients.HashRedisClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author abm
 * @since 2024-07-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, BoolUser> implements IUserService {
	@Resource
	private UserMapper userMapper;
	/**
	 * hasi redis数据类型
	 */
	@Autowired
	private HashRedisClient hashRedisClient;

	@Override
	public String getUserRank(Long userId) {
		String userIdStr = String.valueOf(userId);
		Object rankObj = hashRedisClient.hget(RedisKeyConstants.USER_POINT_RANK_HASH_KEY, userIdStr);
		if (rankObj != null) {
			return String.valueOf(rankObj);
		}
		return "0";
	}
}
