package com.segurapass.model.authorization;

import lombok.Data;

import java.util.UUID;

@Data
public class LoginCompleteReq {
    private String email;
    private UUID deviceId;
    private String M1;
}
