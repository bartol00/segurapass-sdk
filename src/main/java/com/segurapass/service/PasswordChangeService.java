package com.segurapass.service;

import com.segurapass.exception.SdkException;

import java.util.UUID;

public interface PasswordChangeService {
    void changePassword(
            String email,
            char[] oldPassword,
            char[] newPassword,
            byte[] vaultKeyBytes,
            UUID deviceId
    ) throws SdkException;
}
