package com.abmatrix.bool.tg.dao.entity;


import cn.hutool.core.util.IdUtil;
import com.abmatrix.bool.tg.common.enuma.PrivateKeyType;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私钥碎片信息表
 *
 * @author abm
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bool_user_private_key_fragment_info")
public class BoolUserPrivateKeyFragmentInfo extends BaseSqlDO {
    private static final long serialVersionUID = 1L;
	/**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;
    /**
     * 私钥碎片类型
     */
    @TableField("key_type")
    @EnumValue
    private PrivateKeyType keyType;
    /**
     * 公钥地址
     */
    @TableField("public_key_address")
    private String publicKeyAddress;
    /**
     * 私钥 1 碎片
     */
    @TableField("private_key1fragment")
    private String privateKey1Fragment;
    /**
     * 私钥 2 碎片
     */
    @TableField("private_key2fragment")
    private String privateKey2Fragment;
    /**
     * 私钥 3 碎片
     */
    @TableField("private_key3fragment")
    private String privateKey3Fragment;
    /**
     * 私钥 1 碎片hash
     */
    @TableField("private_key1fragment_hash")
    private String privateKey1FragmentHash;

    /**
     * 私钥 3 碎片hash
     */
    @TableField("private_key3fragment_hash")
    private String privateKey3FragmentHash;
    /**
     * 私钥备份信息
     */
    @TableField("private_key_backup_info")
    private String privateKeyBackUpInfo;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    public BoolUserPrivateKeyFragmentInfo(Long userId,
                                          PrivateKeyType keyType,
                                          String publicKeyAddress,
                                          String privateKey1Fragment,
                                          String privateKey2Fragment,
                                          String privateKey3Fragment) {

        this.setId(IdUtil.getSnowflakeNextId());
        this.userId = userId;
        this.keyType = keyType;
        this.publicKeyAddress = publicKeyAddress;
        this.privateKey1Fragment = privateKey1Fragment;
        this.privateKey2Fragment = privateKey2Fragment;
        this.privateKey3Fragment = privateKey3Fragment;
//        this.uploadTime = LocalDateTime.now();
    }
}
