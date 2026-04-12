package com.segurapass.service;

import com.segurapass.exception.SdkException;
import com.segurapass.model.authorization.LoginCompleteResp;
import com.segurapass.model.authorization.RefreshResp;

import java.util.UUID;

public interface AuthorizationService {

    void register(String email, String masterPassword, UUID deviceId) throws SdkException;
    LoginCompleteResp login(String email, String masterPassword, UUID deviceId) throws SdkException;
    RefreshResp refreshJwt(String email, UUID deviceId, String refreshToken) throws SdkException;
    void logout(String email, UUID deviceId, String refreshToken) throws SdkException;
}
