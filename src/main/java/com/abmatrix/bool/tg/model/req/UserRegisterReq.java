package com.abmatrix.bool.tg.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * 用户加入请求体
 *
 * @author abm
 */
@Data
public class UserRegisterReq {

    @NotBlank
    private String hash;

    @NotBlank
    private String data;
}
