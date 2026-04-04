package com.segurapass.model.authorization;

import lombok.Data;

import java.time.Instant;

@Data
public class LoginCompleteResp {
    private String M2;
    private String saltKey;
    private String accessToken;
    private String refreshToken;
    private Instant refreshTokenExpiryTime;
}
