package com.mecklon.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthResponse {
    private UUID id;
    private String token;
    private String email;
    private String username;
}