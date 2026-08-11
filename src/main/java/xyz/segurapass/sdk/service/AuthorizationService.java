package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.helpers.LoginSuccessObject;

import java.util.UUID;

public interface AuthorizationService {

    void register(String email, char[] masterPassword, UUID deviceId)
            throws SegurapassSdkException;

    Object login(String email, char[] masterPassword, UUID deviceId)
            throws SegurapassSdkException;

    String refreshJwt(String refreshToken)
            throws SegurapassSdkException;

    void logout(String refreshToken)
            throws SegurapassSdkException;

}
