package com.segurapass.model.authorization;

import lombok.Data;

@Data
public class LoginStartResp {
    private String B;
    private String saltAuth;
}
