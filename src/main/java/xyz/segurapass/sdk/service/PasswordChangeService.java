package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;

import java.util.UUID;

public interface PasswordChangeService {

    void changePassword(
            String email,
            char[] oldPassword,
            char[] newPassword,
            byte[] vaultKeyBytes,
            UUID deviceId
    ) throws SegurapassSdkException;

}
