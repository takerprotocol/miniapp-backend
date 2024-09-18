package com.abmatrix.bool.tg.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author abm
 * @since 2024-07-29
 */
@Data
@TableName("bool_sync_config")
public class BoolSyncConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	@Version
	private Long version;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;
	
	
	@TableField("address")
	private String address;

	@TableField("private_key")
	private String privateKey;

}
