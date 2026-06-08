package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.models.DecryptedCredentials;
import xyz.segurapass.api.credentials.CredentialsRespSdk;

public interface CredentialsService {

    DecryptedCredentials getCredentials(int page, int size, byte[] vaultKeyBytes)
            throws SegurapassSdkException;

    CredentialsRespSdk addCredential(String website, String username, String password, byte[] vaultKeyBytes)
            throws SegurapassSdkException;

    CredentialsRespSdk updateCredential(String credentialId, String website, String username, String password, byte[] vaultKeyBytes)
            throws SegurapassSdkException;

    void deleteCredential(String credentialId)
            throws SegurapassSdkException;

}
