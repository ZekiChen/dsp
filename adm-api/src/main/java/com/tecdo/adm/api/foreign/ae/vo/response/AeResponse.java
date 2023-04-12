package com.tecdo.adm.api.foreign.ae.vo.response;

import com.tecdo.adm.api.foreign.ae.enums.AeCode;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Zeki on 2023/4/3
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AeResponse<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    public AeResponse(AeCode aeCode) {
        this.code = aeCode.getCode();
        this.message = aeCode.getDesc();
    }

    public boolean succeed() {
        return Objects.equals(AeCode.SUCCESS.getCode(), this.code);
    }

    public static <T> AeResponse<T> data(T data) {
        return new AeResponse<>(AeCode.SUCCESS.getCode(), null, data);
    }
}
