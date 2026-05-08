package com.segurapass.service;

import xyz.segurapass.api.authorization.LoginCompleteResp;
import xyz.segurapass.api.authorization.RefreshResp;
import com.segurapass.exception.SdkException;

import java.util.UUID;

public interface AuthorizationService {

    void register(String email, String masterPassword, UUID deviceId) throws SdkException;
    LoginCompleteResp login(String email, String masterPassword, UUID deviceId) throws SdkException;
    RefreshResp refreshJwt(String email, UUID deviceId, String refreshToken) throws SdkException;
    void logout(String email, UUID deviceId, String refreshToken) throws SdkException;
}
