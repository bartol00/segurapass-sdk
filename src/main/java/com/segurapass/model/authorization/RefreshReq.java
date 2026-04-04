package com.segurapass.model.authorization;

import lombok.Data;

import java.util.UUID;

@Data
public class RefreshReq {
    private String email;
    private UUID deviceId;
    private String refreshToken;
}
