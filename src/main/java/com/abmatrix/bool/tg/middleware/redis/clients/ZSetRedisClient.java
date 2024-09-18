package com.abmatrix.bool.tg.middleware.redis.clients;

import com.abmatrix.bool.tg.middleware.redis.config.LettuceConfig;
import com.abmatrix.bool.tg.middleware.redis.enums.RedisClientTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 有序集合缓存客户端
 * 
 * @author wangjp31109
 * @date 2021年10月10日
 */
@Lazy
@Slf4j
@Component
@Order(6)
@DependsOn("boolRedisTemplate")
@SuppressWarnings("unchecked")
@ConditionalOnBean(LettuceConfig.class)
public class ZSetRedisClient extends CommonRedisClient {
	/**
	 * spring上下文
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 初始化方法
	 */
	@PostConstruct
	private void init() {
		super.init(applicationContext);
	}

	/**
	 * 构造
	 */
	public ZSetRedisClient() {
		super();
		this.clientType = RedisClientTypeEnum.ZSET.getTypeName();
	}

	/**
	 * 添加新元素
	 * 
	 * @param key   键
	 * @param value 插入值
	 * @param score 分数
	 * @return
	 */
	public boolean add(String key, Object value, double score) {
		try {
			redisTemplate.opsForZSet().add(key, value, score);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应zset值[", value, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 获取变量指定区间的元素。START为0,END为-1代表取全部
	 * 
	 * @param key   键
	 * @param start 起始索引
	 * @param end   结束索引
	 * @return
	 */
	public Set<Object> range(String key, long start, long end) {
		return redisTemplate.opsForZSet().range(key, start, end);
	}

	/**
	 * 用于获取满足非score的排序取值。这个排序只有在有相同分数的情况下才能使用，如果有不同的分数则返回值不确定
	 * 
	 * @param key   键
	 * @param range 范围
	 * @return
	 */
	public Set<Object> rangeByLex(String key, RedisZSetCommands.Range range) {
		return redisTemplate.opsForZSet().rangeByLex(key, range);
	}

	/**
	 * 获取zset的元素个数
	 * 
	 * @param key 键
	 * @return
	 */
	public long zCard(String key) {
		return redisTemplate.opsForZSet().zCard(key);
	}

	/**
	 * 获取分数区间值的个数
	 * 
	 * @param key 键
	 * @param min 最小分数
	 * @param max 最大分数
	 * @return
	 */
	public long count(String key, double min, double max) {
		return redisTemplate.opsForZSet().count(key, min, max);
	}

	/**
	 * 获取分数区间值对应的值
	 * 
	 * @param key 键
	 * @param min 最小分数
	 * @param max 最大分数
	 * @return
	 */
	public Set<Object> rangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max);
	}

	/**
	 * 根据设置的score获取区间值从给定下标和给定长度获取最终值
	 * 
	 * @param key    键
	 * @param min    最小分数
	 * @param max    最大分数
	 * @param offset 偏移量
	 * @param count  要求返回值的个数
	 * @return
	 */
	public Set<Object> rangeByScore(String key, double min, double max, long offset, long count) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
	}

	/**
	 * 修改zset中的元素的分值
	 * 
	 * @param key   键
	 * @param value 值
	 * @param delta 分数
	 * @return
	 */
	public Double incrementScore(String key, Object value, double delta) {
		return redisTemplate.opsForZSet().incrementScore(key, value, delta);
	}

	/**
	 * 获取元素分支
	 * 
	 * @param key   键
	 * @param value 值
	 * @return
	 */
	public Double score(String key, Object value) {
		return redisTemplate.opsForZSet().score(key, value);
	}

	/**
	 * 用于获取满足非score的设置下标开始的长度排序取值
	 * 
	 * @param key   键
	 * @param range 取值范围
	 * @param limit 限制区域
	 * @return
	 */
	public Set<Object> rangeByLex(String key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit) {
		return redisTemplate.opsForZSet().rangeByLex(key, range, limit);
	}

	/**
	 * 通过TypedTuple方式新增数据
	 * 
	 * @param key    键
	 * @param tuples 插入规则
	 */
	public void add(String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
		redisTemplate.opsForZSet().add(key, tuples);
	}

	/**
	 * 通过索引获取RedisZSetCommands.Tuples集合，返回值包括value和分数信息
	 * 
	 * @param key   键
	 * @param start 起始下标
	 * @param end   结束下标
	 * @return
	 */
	public Set<ZSetOperations.TypedTuple<Object>> rangeWithScores(String key, long start, long end) {
		return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
	}

	/**
	 * 通过分数获取RedisZSetCommands.Tuples集合，返回值包括value和分数信息
	 * 
	 * @param key 键
	 * @param min 最小分
	 * @param max 最高分
	 * @return
	 */
	public Set<ZSetOperations.TypedTuple<Object>> rangeByScoreWithScores(String key, double min, double max) {
		return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
	}

	/**
	 * 通过分数和下标两个维度获取RedisZSetCommands.Tuples集合
	 * 
	 * @param key    键
	 * @param min    最低分
	 * @param max    最高分
	 * @param offset 起始偏移量
	 * @param count  指定返回的个数
	 * @return
	 */
	public Set<ZSetOperations.TypedTuple<Object>> rangeByScoreWithScores(String key, double min, double max,
			long offset, long count) {
		return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * 获取指定元素的下标
	 * 
	 * @param key   键
	 * @param value 值
	 * @return
	 */
	public long rank(String key, Object value) {
		return redisTemplate.opsForZSet().rank(key, value);
	}

	/**
	 * 通过搜索条件获取TypedTuple集合
	 * 
	 * @param key     键
	 * @param options 复合搜索条件
	 * @return
	 */
	public Cursor<ZSetOperations.TypedTuple<Object>> scan(String key, ScanOptions options) {
		return redisTemplate.opsForZSet().scan(key, options);
	}

	/**
	 * 获取指定区间元素的翻转集合
	 * 
	 * @param key   键
	 * @param start 起始位置
	 * @param end   结束位置
	 * @return
	 */
	public Set<Object> reverseRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().reverseRange(key, start, end);
	}

	/**
	 * 通过分数获取区间元素的翻转集合
	 * 
	 * @param key 键
	 * @param min 最低分
	 * @param max 最高分
	 * @return
	 */
	public Set<Object> reverseRangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
	}

	/**
	 * 通过分数和下标偏移量两个维度获取翻转集合
	 * 
	 * @param key    键
	 * @param min    最低分
	 * @param max    最高分
	 * @param offset 偏移量
	 * @param count  取值的个数
	 * @return
	 */
	public Set<Object> reverseRangeByScore(String key, double min, double max, long offset, long count) {
		return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
	}

	/**
	 * 通过下标获取翻转集合的TypedTuple集合
	 * 
	 * @param key   键
	 * @param start 起始位置
	 * @param end   结束位置
	 * @return
	 */
	public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String key, long start, long end) {
		return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
	}

	/**
	 * 通过分数获取翻转集合的TypedTuple集合
	 * 
	 * @param key 键
	 * @param min 最低分
	 * @param max 最高分
	 * @return
	 */
	public Set<ZSetOperations.TypedTuple<Object>> reverseRangeByScoreWithScores(String key, double min, double max) {
		return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
	}

	/**
	 * 通过分数和下标偏移量两个维度获取翻转集合的TypedTuple集合
	 * 
	 * @param key    键
	 * @param min    最低分
	 * @param max    最高分
	 * @param offset 起始偏移量
	 * @param count  要返回元素的个数
	 * @return
	 */
	public Set<ZSetOperations.TypedTuple<Object>> reverseRangeByScoreWithScores(String key, double min, double max,
			long offset, long count) {
		return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * 获取翻转后值的索引下标
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public long reverseRank(String key, Object value) {
		return redisTemplate.opsForZSet().reverseRank(key, value);
	}

	/**
	 * 获取2个zset的交集存放到第3个zset里面
	 * 
	 * @param key      源键
	 * @param otherKey 比较键
	 * @param destKey  目标键
	 * @return
	 */
	public long intersectAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
	}

	/**
	 * 获取多个键的zset交集并存入最终zset中
	 * 
	 * @param key       源键
	 * @param otherKeys 比较键列表
	 * @param destKey   最终目标键
	 * @return
	 */
	public long intersectAndStore(String key, List<String> otherKeys, String destKey) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
	}

	/**
	 * 获取2个有序集合的并集存放到第3个集合里面。
	 * 
	 * @param key      键
	 * @param otherKey 比较键
	 * @param destKey  目标键
	 * @return
	 */
	public long unionAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
	}

	/**
	 * 获取多个zset集合的并集并赋值给最终目标集合
	 * 
	 * @param key       源键
	 * @param otherKeys 其它比较键列表
	 * @param destKey   目标键
	 * @return
	 */
	public long unionAndStore(String key, List<String> otherKeys, String destKey) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
	}

	/**
	 * 根据元素值批量移除
	 * 
	 * @param key    键
	 * @param values 值，可传多值
	 * @return 删除的数量
	 */
	public long remove(String key, Object... values) {
		return redisTemplate.opsForZSet().remove(key, values);
	}

	/**
	 * 移除分数区间内的值
	 * 
	 * @param key 键
	 * @param min 最低分
	 * @param max 最高分
	 * @return
	 */
	public long removeRangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
	}

	/**
	 * 根据索引移除区间内元素
	 * 
	 * @param key   键
	 * @param start 起始索引
	 * @param end   结束索引
	 * @return
	 */
	public long removeRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().removeRange(key, start, end);
	}
}
