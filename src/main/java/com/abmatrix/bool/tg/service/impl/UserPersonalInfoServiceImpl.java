package com.abmatrix.bool.tg.service.impl;

import cn.hutool.core.lang.Assert;
import com.abmatrix.bool.tg.common.constants.RedisKeyConstants;
import com.abmatrix.bool.tg.common.enuma.PersonalInfoTypeEnum;
import com.abmatrix.bool.tg.common.model.vo.UserVo;
import com.abmatrix.bool.tg.dao.entity.BoolUserCustomInfo;
import com.abmatrix.bool.tg.dao.mapper.BoolUserCustomInfoMapper;
import com.abmatrix.bool.tg.middleware.redis.clients.SimpleRedisClient;
import com.abmatrix.bool.tg.model.req.UserPersonalInfoReq;
import com.abmatrix.bool.tg.service.UserPersonalInfoService;
import com.abmatrix.bool.tg.service.UserService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户个人信息接口
 * 
 * @author PeterWong
 * @date 2024年8月8日
 */
@Service
public class UserPersonalInfoServiceImpl implements UserPersonalInfoService {

	/**
	 * 用户接口
	 */
	@Autowired
	private UserService userService;
	/**
	 * 用户自定义信息mapper
	 */
	@Autowired
	private BoolUserCustomInfoMapper boolUserCustomInfoMapper;
	/**
	 * redis客户端
	 */
	@Autowired
	private SimpleRedisClient simpleRedisClient;

	@Override
	public void addUserPersonalInfo(UserPersonalInfoReq req) {
		String hash = req.getHash();
		String data = req.getData();
		Assert.notBlank(hash, "Identity identifier missing.");
		Assert.notBlank(data, "Identity identifier missing.");
		JSONObject checkUserResult = userService.checkAndParseData(data, hash);
		Assert.isFalse(checkUserResult.isEmpty(), "User identity verification failed.");
		Long userTgId = checkUserResult.getLong("id");
		Assert.notNull(userTgId, "User identity parsing failed.");
		UserVo user = userService.getUserInfo(userTgId);
		Assert.notNull(user, "Current user does not exist.");
		String userIdStr = user.getUserId();
		Long userId = Long.valueOf(userIdStr);
		String url = req.getPersonalUrl();
		if (StringUtils.isNotBlank(url)) {
			String validatePrefix = "https://t.me/";
			if (!StringUtils.startsWith(url, validatePrefix)) {
				Assert.isTrue(false, "Invalid url");
			}
			saveUrlInfo(userId, url);
			try {
				String key = StringUtils.join(RedisKeyConstants.USER_TG_KEY, userTgId);
				simpleRedisClient.expire(key, 0);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 保存用户个人url
	 * 
	 * @param userId
	 * @param url
	 */
	private void saveUrlInfo(Long userId, String url) {
		LambdaQueryWrapper<BoolUserCustomInfo> customInfoQuery = Wrappers.lambdaQuery();
		customInfoQuery.eq(BoolUserCustomInfo::getUserId, userId);
		customInfoQuery.eq(BoolUserCustomInfo::getPersonalInfoType, PersonalInfoTypeEnum.URL);
		customInfoQuery.select(BoolUserCustomInfo::getId, BoolUserCustomInfo::getPersonalInfoValue);
		customInfoQuery.orderByDesc(BoolUserCustomInfo::getId);
		customInfoQuery.last(" LIMIT 1 ");
		BoolUserCustomInfo existCustomInfo = boolUserCustomInfoMapper.selectOne(customInfoQuery, Boolean.FALSE);
		if (existCustomInfo != null) {
			if (StringUtils.equals(url, existCustomInfo.getPersonalInfoValue())) {
				return;
			}
			LambdaUpdateWrapper<BoolUserCustomInfo> updater = Wrappers.lambdaUpdate();
			updater.set(BoolUserCustomInfo::getPersonalInfoValue, url);
			updater.eq(BoolUserCustomInfo::getId, existCustomInfo.getId());
			boolUserCustomInfoMapper.update(updater);
		} else {
			BoolUserCustomInfo urlCustomInfo = new BoolUserCustomInfo();
			urlCustomInfo.setCreateUser(userId);
			urlCustomInfo.setUpdateUser(userId);
			urlCustomInfo.setUserId(userId);
			urlCustomInfo.setPersonalInfoValue(url);
			urlCustomInfo.setPersonalInfoType(PersonalInfoTypeEnum.URL);
			boolUserCustomInfoMapper.insert(urlCustomInfo);
		}
	}

}
