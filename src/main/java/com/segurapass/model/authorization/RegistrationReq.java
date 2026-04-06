package com.segurapass.model.authorization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RegistrationReq {
    private String email;
    private String saltAuth;
    private String verifier;
    private String saltKey;
    private UUID deviceId;
}
