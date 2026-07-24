package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.models.DecryptedCredential;
import xyz.segurapass.sdk.models.DecryptedCredentials;

import java.security.PrivateKey;

public interface CredentialsService {

    DecryptedCredentials getCredentials(int page, int size, byte[] vaultKeyBytes)
            throws SegurapassSdkException;

    DecryptedCredential addCredential(String website, String username, String password, byte[] vaultKeyBytes, PrivateKey signingKey)
            throws SegurapassSdkException;

    DecryptedCredential updateCredential(String credentialId, String website, String username, String password, byte[] vaultKeyBytes, PrivateKey signingKey)
            throws SegurapassSdkException;

    void deleteCredential(String credentialId, PrivateKey signingKey)
            throws SegurapassSdkException;

}
