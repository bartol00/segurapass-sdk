package com.segurapass.service;

import com.segurapass.helpers.LoginSuccessObject;
import xyz.segurapass.api.authorization.RefreshResp;
import com.segurapass.exception.SdkException;

import java.util.UUID;

public interface AuthorizationService {

    void register(String email, char[] masterPassword, UUID deviceId)
            throws SdkException;
    LoginSuccessObject login(String email, char[] masterPassword, UUID deviceId)
            throws SdkException;
    RefreshResp refreshJwt(String refreshToken)
            throws SdkException;
    void logout(String refreshToken)
            throws SdkException;
}
