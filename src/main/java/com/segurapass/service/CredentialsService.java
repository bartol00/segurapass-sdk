package com.segurapass.service;

import com.segurapass.models.credentials.DecryptedCredentials;
import xyz.segurapass.api.credentials.CredentialsRespSdk;
import com.segurapass.exception.SdkException;

public interface CredentialsService {

    DecryptedCredentials getCredentials(int page, int size, byte[] vaultKeyBytes)
            throws SdkException;
    CredentialsRespSdk addCredential(String website, String username, String password, byte[] vaultKeyBytes)
            throws SdkException;
    CredentialsRespSdk updateCredential(String credentialId, String website, String username, String password, byte[] vaultKeyBytes)
            throws SdkException;
    void deleteCredential(String credentialId) throws SdkException;
}
