package xyz.segurapass.sdk.helpers;

import xyz.segurapass.api.authorization.LoginCompleteResp;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

import static xyz.segurapass.sdk.helpers.WrappingInfoConstants.*;

public class LoginHelper {

    public static LoginSuccessObject getLoginSuccessObject(
            SrpLoginSession ctx,
            LoginCompleteResp completeResp
    ) throws Exception {

        SecretKey masterPasswordKey = EncryptionHelper.generateMasterPasswordKey(
                ctx.passwordBytes(),
                completeResp.getSaltKey()
        );

        SecretKey vaultWrappingKey = EncryptionHelper.deriveKeyHkdf(
                masterPasswordKey,
                completeResp.getSaltHkdf(),
                vaultWrappingInfo
        );

        SecretKey signingWrappingKey = EncryptionHelper.deriveKeyHkdf(
                masterPasswordKey,
                completeResp.getSaltHkdf(),
                signingWrappingInfo
        );

        byte[] vaultKey = EncryptionHelper.decryptField(
                completeResp.getVaultKey(),
                completeResp.getIvVaultKey(),
                vaultWrappingKey
        );

        byte[] privateSigningKeyBytes = EncryptionHelper.decryptField(
                completeResp.getPrivateSigningKey(),
                completeResp.getIvPrivateSigningKey(),
                signingWrappingKey
        );

        PrivateKey privateSigningKey = EncryptionHelper.getPrivateSigningKeyFromBytes(privateSigningKeyBytes);
        PublicKey publicSigningKey = EncryptionHelper.getPublicSigningKeyFromBytes(completeResp.getPublicSigningKey());

        return new LoginSuccessObject(
                vaultKey,
                privateSigningKey,
                publicSigningKey,
                completeResp.getAccessToken(),
                completeResp.getRefreshToken(),
                completeResp.getRefreshTokenExpiryTime()
        );
    }

}
