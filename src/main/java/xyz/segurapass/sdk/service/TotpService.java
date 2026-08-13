package xyz.segurapass.sdk.service;

import xyz.segurapass.api.mfa.TotpResp;
import xyz.segurapass.sdk.helpers.LoginSuccessObject;

import java.security.PrivateKey;

public interface TotpService {

    TotpResp addTotp(PrivateKey signingKey);
    void removeTotp(PrivateKey signingKey);
    String verifyTotp(String otp);
    LoginSuccessObject loginTotp(String code, String otp, char[] masterPassword);
    LoginSuccessObject recoveryTotp(String code, String recoveryCode, char[] masterPassword);

}
