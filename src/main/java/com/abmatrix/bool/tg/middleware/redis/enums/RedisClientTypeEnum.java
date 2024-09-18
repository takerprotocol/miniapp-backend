package com.abmatrix.bool.tg.middleware.redis.enums;

/**
 * redis客户端类型枚举
 * 
 * @author PeterWong
 * @date 2023年1月22日
 */
public enum RedisClientTypeEnum {
	SIMPLE("简单类型客户端"), HASH("字典类型客户端"), SET("集合类型客户端"), ZSET("有序集合客户端"), LIST("列表类型客户端");

	private String typeName;

	private RedisClientTypeEnum(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

}
