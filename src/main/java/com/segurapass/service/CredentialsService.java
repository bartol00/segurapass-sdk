package com.segurapass.service;

import com.segurapass.exception.SdkException;
import com.segurapass.model.credentials.CredentialsResp;
import com.segurapass.model.credentials.PagedResponse;

public interface CredentialsService {

    PagedResponse<CredentialsResp> getCredentials(int page, int size) throws SdkException;
    CredentialsResp addCredential(String website, String ivWebsite,
                                  String username, String ivUsername,
                                  String password, String ivPassword)
            throws SdkException;
    CredentialsResp updateCredential(String website, String ivWebsite,
                                     String username, String ivUsername,
                                     String password, String ivPassword,
                                     String credentialId)
            throws SdkException;
    void deleteCredential(String credentialId) throws SdkException;
}
