package com.segurapass.service;

import com.segurapass.model.credentials.CredentialsResp;
import com.segurapass.model.credentials.PagedResponse;

public interface CredentialsService {

    PagedResponse<CredentialsResp> getCredentials(int page, int size);
    CredentialsResp addCredential(String website, String ivWebsite,
                                  String username, String ivUsername,
                                  String password, String ivPassword);
    CredentialsResp updateCredential(String website, String ivWebsite,
                                     String username, String ivUsername,
                                     String password, String ivPassword,
                                     String credentialId);
    void deleteCredential(String credentialId);
}
