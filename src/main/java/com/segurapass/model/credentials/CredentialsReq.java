package com.segurapass.model.credentials;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialsReq {
    private String website;
    private String username;
    private String password;
    private String ivWebsite;
    private String ivUsername;
    private String ivPassword;
}
