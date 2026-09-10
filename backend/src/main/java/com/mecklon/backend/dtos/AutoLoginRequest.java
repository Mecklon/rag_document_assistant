package com.mecklon.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutoLoginRequest {
    private String token;
}
