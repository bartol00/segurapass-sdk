package xyz.segurapass.sdk.service.impl;

import com.segurapass.api.ApiClient;
import com.segurapass.exception.ApiException;
import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.helpers.EncryptionHelper;
import xyz.segurapass.sdk.helpers.PasswordChangeObject;
import xyz.segurapass.sdk.service.PasswordChangeService;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.SRP6GroupParameters;
import xyz.segurapass.api.password.*;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static xyz.segurapass.sdk.helpers.SrpHelper.*;

public class PasswordChangeServiceImpl implements PasswordChangeService {

    private final ApiClient apiClient;
    private final Supplier<String> jwtSupplier;
    private final SRP6GroupParameters group;
    private final String baseEndpoint;
    private final SecureRandom random;

    public PasswordChangeServiceImpl(ApiClient apiClient, Supplier<String> jwtSupplier) {
        this.apiClient = apiClient;
        this.jwtSupplier = jwtSupplier;
        this.group = SRP6StandardGroups.rfc5054_3072;
        this.baseEndpoint = "/api/password";
        this.random = new SecureRandom();
    }

    @Override
    public void changePassword(
            String email,
            char[] oldPassword,
            char[] newPassword,
            byte[] vaultKeyBytes,
            PrivateKey privateSigningKey,
            UUID deviceId
    ) throws SegurapassSdkException {

        String startEndpoint = baseEndpoint + "/change/start";
        String completeEndpoint = baseEndpoint + "/change/end";

        Digest digest = new SHA256Digest();

        try(PasswordChangeObject ctx = PasswordChangeObject.create(random, oldPassword, newPassword, vaultKeyBytes, privateSigningKey, group)) {

            PasswordChangeStartReq startReq = new PasswordChangeStartReq(
                    deviceId,
                    Base64.getEncoder().encodeToString(ctx.A().toByteArray())
            );

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            PasswordChangeStartResp startResp = apiClient.sendPostRequest(
                    startReq,
                    startEndpoint,
                    null,
                    headers,
                    PasswordChangeStartResp.class
            ).body();

            byte[] saltAuth = Base64.getDecoder().decode(startResp.getSaltAuth());
            BigInteger B = new BigInteger(1, Base64.getDecoder().decode(startResp.getB()));

            validatePublicValue(ctx.A(), "A", startEndpoint);
            validatePublicValue(B, "B", startEndpoint);

            BigInteger x = generateX(
                    digest,
                    saltAuth,
                    email.getBytes(StandardCharsets.UTF_8),
                    ctx.passwordBytes()
            );
            BigInteger S = generateS(digest, x, ctx.a(), ctx.A(), B);
            BigInteger M1 = generateM1(digest, ctx.A(), B, S);

            digest = new SHA256Digest();

            BigInteger newX = generateX(
                    digest,
                    ctx.newSaltAuth(),
                    email.getBytes(StandardCharsets.UTF_8),
                    ctx.newPasswordBytes()
            );
            BigInteger newVerifier = generateVerifier(newX);
            validateVerifier(newVerifier, completeEndpoint);

            SecretKey masterPasswordKey = EncryptionHelper.generateMasterPasswordKey(
                    ctx.newPasswordBytes(),
                    ctx.newSaltKey()
            );

            String vaultWrappingInfo = "segurapass-vault-wrap";
            SecretKey vaultWrappingKey = EncryptionHelper.deriveKeyHkdf(
                    masterPasswordKey,
                    ctx.newSaltHkdf(),
                    vaultWrappingInfo
            );

            String signingWrappingInfo = "segurapass-signing-wrap";
            SecretKey signingWrappingKey = EncryptionHelper.deriveKeyHkdf(
                    masterPasswordKey,
                    ctx.newSaltHkdf(),
                    signingWrappingInfo
            );

            byte[] encryptedVaultKey = EncryptionHelper.encryptField(
                    ctx.vaultKeyBytes(),
                    ctx.newVaultKeyIv(),
                    vaultWrappingKey
            );

            byte[] encryptedSigningKey = EncryptionHelper.encryptField(
                    EncryptionHelper.getPrivateSigningKeyBytes(ctx.privateSigningKey()),
                    ctx.newPrivateSigningKeyIv(),
                    signingWrappingKey
            );

            PasswordChangeCompleteReq completeReq = new PasswordChangeCompleteReq(
                    deviceId,
                    Base64.getEncoder().encodeToString(M1.toByteArray()),
                    Base64.getEncoder().encodeToString(ctx.newSaltAuth()),
                    Base64.getEncoder().encodeToString(newVerifier.toByteArray()),
                    Base64.getEncoder().encodeToString(encryptedVaultKey),
                    Base64.getEncoder().encodeToString(ctx.newVaultKeyIv()),
                    Base64.getEncoder().encodeToString(ctx.newSaltKey()),
                    Base64.getEncoder().encodeToString(ctx.newSaltHkdf()),
                    Base64.getEncoder().encodeToString(encryptedSigningKey),
                    Base64.getEncoder().encodeToString(ctx.newPrivateSigningKeyIv())
            );

            apiClient.sendPostRequest(
                    completeReq,
                    completeEndpoint,
                    null,
                    headers,
                    null
            );

        }  catch (ApiException e) {
            throw new SegurapassSdkException(e);
        }  catch (Exception e) {
            throw new SegurapassSdkException(500, "POST", "Could not change master password", completeEndpoint);
        }
    }

}
