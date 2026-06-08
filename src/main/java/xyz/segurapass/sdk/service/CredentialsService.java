package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.models.DecryptedCredential;
import xyz.segurapass.sdk.models.DecryptedCredentials;

public interface CredentialsService {

    DecryptedCredentials getCredentials(int page, int size, byte[] vaultKeyBytes)
            throws SegurapassSdkException;

    DecryptedCredential addCredential(String website, String username, String password, byte[] vaultKeyBytes)
            throws SegurapassSdkException;

    DecryptedCredential updateCredential(String credentialId, String website, String username, String password, byte[] vaultKeyBytes)
            throws SegurapassSdkException;

    void deleteCredential(String credentialId)
            throws SegurapassSdkException;

}
