package com.segurapass.service.impl;

import com.segurapass.api.ApiClient;
import com.segurapass.exception.SdkException;
import com.segurapass.helpers.EncryptionHelper;
import com.segurapass.models.password_change.PasswordChangeObject;
import com.segurapass.service.PasswordChangeService;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.SRP6GroupParameters;
import xyz.segurapass.api.password.*;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Supplier;

import static com.segurapass.helpers.SrpHelper.*;

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
            UUID deviceId
    ) throws SdkException {

        String startEndpoint = baseEndpoint + "/change/start";
        String completeEndpoint = baseEndpoint + "/change/end";

        Digest digest = new SHA256Digest();

        try(PasswordChangeObject ctx = PasswordChangeObject.create(random, oldPassword, newPassword, vaultKeyBytes, group)) {

            PasswordChangeStartReq startReq = new PasswordChangeStartReq(
                    deviceId,
                    Base64.getEncoder().encodeToString(ctx.A().toByteArray())
            );

            PasswordChangeStartResp startResp = apiClient.sendPostRequest(
                    startReq,
                    startEndpoint,
                    null,
                    jwtSupplier.get(),
                    null,
                    PasswordChangeStartResp.class
            ).getBody();

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

            byte[] encryptedVaultKey = encryptVaultKey(
                    ctx.newPasswordBytes(),
                    ctx.newSaltKey(),
                    ctx.vaultKeyBytes(),
                    ctx.newVaultKeyIv()
            );

            PasswordChangeCompleteReq completeReq = new PasswordChangeCompleteReq(
                    deviceId,
                    Base64.getEncoder().encodeToString(M1.toByteArray()),
                    Base64.getEncoder().encodeToString(ctx.newSaltAuth()),
                    Base64.getEncoder().encodeToString(newVerifier.toByteArray()),
                    Base64.getEncoder().encodeToString(encryptedVaultKey),
                    Base64.getEncoder().encodeToString(ctx.newVaultKeyIv()),
                    Base64.getEncoder().encodeToString(ctx.newSaltKey())
            );

            apiClient.sendPostRequest(
                    completeReq,
                    completeEndpoint,
                    null,
                    jwtSupplier.get(),
                    null,
                    null
            );
        }  catch (SdkException e) {
            throw e;
        }  catch (Exception e) {
            throw new SdkException(500, "POST", "Could not change master password", completeEndpoint);
        }
    }

    private byte[] encryptVaultKey(byte[] passwordBytes, byte[] salt, byte[] vaultKey, byte[] vaultKeyIv) throws Exception {
        SecretKey masterPasswordKey = EncryptionHelper.generateMasterPasswordKey(passwordBytes, salt);
        return EncryptionHelper.encryptField(vaultKey, vaultKeyIv, masterPasswordKey);
    }
}
