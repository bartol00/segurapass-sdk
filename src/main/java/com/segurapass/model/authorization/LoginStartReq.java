package com.segurapass.model.authorization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LoginStartReq {
    private String email;
    private UUID deviceId;
    private String A;
}
