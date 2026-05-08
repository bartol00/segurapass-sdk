package com.segurapass.service;

import xyz.segurapass.api.credentials.CredentialsRespSdk;
import xyz.segurapass.api.credentials.PagedResponse;
import com.segurapass.exception.SdkException;

public interface CredentialsService {

    PagedResponse<CredentialsRespSdk> getCredentials(int page, int size) throws SdkException;
    CredentialsRespSdk addCredential(String website, String ivWebsite,
                                     String username, String ivUsername,
                                     String password, String ivPassword)
            throws SdkException;
    CredentialsRespSdk updateCredential(String website, String ivWebsite,
                                     String username, String ivUsername,
                                     String password, String ivPassword,
                                     String credentialId)
            throws SdkException;
    void deleteCredential(String credentialId) throws SdkException;
}
