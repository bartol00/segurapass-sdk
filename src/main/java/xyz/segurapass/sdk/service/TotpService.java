package xyz.segurapass.sdk.service;

import xyz.segurapass.api.authorization.LoginCompleteResp;
import xyz.segurapass.api.mfa.TotpResp;

import java.security.PrivateKey;

public interface TotpService {

    TotpResp addTotp(PrivateKey signingKey);
    void removeTotp(PrivateKey signingKey);
    String verifyTotp(String otp);
    LoginCompleteResp loginTotp(String code, String otp);
    LoginCompleteResp recoveryTotp(String code, String recoveryCode);

}
