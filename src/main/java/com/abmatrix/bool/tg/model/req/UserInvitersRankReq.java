package com.abmatrix.bool.tg.model.req;

import com.abmatrix.bool.tg.common.enuma.InviteModelType;
import com.abmatrix.bool.tg.common.enuma.InviteTimeType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserInvitersRankReq {
    private InviteTimeType type;
    @NotBlank
    private InviteModelType model;

    private String userName;

    private String code;
}
