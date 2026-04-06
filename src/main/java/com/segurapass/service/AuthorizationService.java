package com.segurapass.service;

import com.segurapass.model.authorization.LoginCompleteResp;
import com.segurapass.model.authorization.RefreshResp;

import java.util.UUID;

public interface AuthorizationService {

    void register(String email, String masterPassword, UUID deviceId);
    LoginCompleteResp login(String email, String masterPassword, UUID deviceId);
    RefreshResp refreshJwt(String email, UUID deviceId, String refreshToken);
    void logout(String email, UUID deviceId, String refreshToken);
}
