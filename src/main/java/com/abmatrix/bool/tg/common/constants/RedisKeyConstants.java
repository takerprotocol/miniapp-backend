package com.abmatrix.bool.tg.common.constants;

/**
 * redis key的常量池
 * 
 * @author PeterWong
 * @date 2023年7月5日
 */
public class RedisKeyConstants {
	/**
	 * 排行榜缓存key
	 */
	public static final String RANK_KEY = "bool:tg:rank:";

	/**
	 * 邀请记录key
	 */
	public static final String INVITE_RECORD_KEY = "bool:tg:invite:record:";
	/**
	 * tg用户key
	 */
	public static final String USER_TG_KEY = "bool:tg:user:tgid:";

	/**
	 * 用户积分排名hash redis key
	 */
	public static final String USER_POINT_RANK_HASH_KEY = "bool:tg:user:point:rank:hash";

	/**
	 * 用户任务分布式锁
	 */
	public static final String USER_ASSIGNMENT_LOCK = "bool:tg:assignement:lock:";
	/**
	 * 排行榜zset key
	 */
	public static final String USER_POINT_RANK_ZSET_KEY = "bool:tg:user:point:rank:zset";


	/**
	 * 任务完成人数量缓存key
	 */
	public static final String ASSIGNMENT_COMPELTE_COUNT = "bool:tg:task:finish:total:hash";
	/**
	 * 用户邀请排行榜计算分布式锁
	 */
	public static final String USER_FULL_INVITE_RANK_CALCULATE_LOCK = "bool:tg:user:full:invite:rank:calculate";

	/**
	 * 用户全量邀请排名zset
	 */
	public static final String USER_FULL_INVITE_RANK_ZSET = "bool:full:invite:rank:zset";

	/**
	 * 用户周邀请排行榜计算分布式锁
	 */
	public static final String USER_WEEKLY_INVITE_RANK_CALCULATE_LOCK = "bool:tg:user:weekly:invite:rank:calculate";

	/**
	 * 用户周邀请排行榜加载分布式锁
	 */
	public static final String USER_WEEKLY_INVITE_RANK_LOAD_LOCK = "bool:tg:user:weekly:invite:rank:load";
	/**
	 * 用户周邀请排名zset
	 */
	public static final String USER_WEEKLY_INVITE_RANK_ZSET = "bool:weekly:invite:rank:zset";
	/**
	 * 计算用户排名分布式锁
	 */
	public static final String USER_POINT_RANK_CALCULATE_LOCK = "bool:tg:user:point:rank:calculate";
	/**
	 * 周榜清算锁头
	 */
	public static final String USER_WEEKLY_INVITE_RANK_SETTLE_LOCK = "bool:weekly:invite:rank:settle:lock";
}
