package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;

import java.security.PrivateKey;
import java.util.UUID;

public interface PasswordChangeService {

    void changePassword(
            String email,
            char[] oldPassword,
            char[] newPassword,
            byte[] vaultKeyBytes,
            PrivateKey privateSigningKey,
            UUID deviceId
    ) throws SegurapassSdkException;

}
