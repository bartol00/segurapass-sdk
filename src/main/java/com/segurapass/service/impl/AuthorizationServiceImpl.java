package com.segurapass.service.impl;

import com.segurapass.helpers.*;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import xyz.segurapass.api.authorization.*;
import com.segurapass.api.ApiClient;
import com.segurapass.exception.SdkException;
import com.segurapass.service.AuthorizationService;
import com.segurapass.api.ApiResponse;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import static com.segurapass.helpers.SrpHelper.*;

public class AuthorizationServiceImpl implements AuthorizationService {

    private final ApiClient apiClient;
    private final SRP6GroupParameters group;
    private final String baseEndpoint;
    private final SecureRandom random = new SecureRandom();

    public AuthorizationServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.group = SRP6StandardGroups.rfc5054_3072;
        this.baseEndpoint = "/api/authorization";
    }

    @Override
    public void register(String email, char[] masterPassword, UUID deviceId) {

        String endpoint = baseEndpoint + "/register";
        Digest digest = new SHA256Digest();

        try(SrpRegisterSession ctx = SrpRegisterSession.create(random, masterPassword)) {
            BigInteger x = generateX(
                    digest,
                    ctx.saltAuth(),
                    email.getBytes(StandardCharsets.UTF_8),
                    ctx.passwordBytes()
            );
            BigInteger verifier = generateVerifier(x);
            validateVerifier(verifier, endpoint);

            SecretKey masterPasswordKey = EncryptionHelper.generateMasterPasswordKey(
                    ctx.passwordBytes(),
                    ctx.saltKey()
            );
            byte[] encryptedVaultKey = EncryptionHelper.encryptField(
                    ctx.vaultKey(),
                    ctx.vaultKeyIv(),
                    masterPasswordKey
            );

            RegistrationReq req = new RegistrationReq(
                    email,
                    Base64.getEncoder().encodeToString(ctx.saltAuth()),
                    Base64.getEncoder().encodeToString(verifier.toByteArray()),
                    Base64.getEncoder().encodeToString(encryptedVaultKey),
                    Base64.getEncoder().encodeToString(ctx.vaultKeyIv()),
                    Base64.getEncoder().encodeToString(ctx.saltKey()),
                    deviceId
            );

            apiClient.sendPostRequest(req, endpoint, null, null, null, null);

        } catch (SdkException e) {
            throw e;
        } catch (Exception e) {
            throw new SdkException(500, "POST", "Could not register", endpoint);
        }
    }

    @Override
    public LoginSuccessObject login(String email, char[] masterPassword, UUID deviceId) {
        String startEndpoint = baseEndpoint + "/login/start";
        String completeEndpoint = baseEndpoint + "/login/end";

        Digest digest = new SHA256Digest();

        try(SrpLoginSession ctx = SrpLoginSession.create(random, masterPassword, group)) {
            LoginStartReq startReq = new LoginStartReq(
                    email,
                    deviceId,
                    Base64.getEncoder().encodeToString(ctx.A().toByteArray())
            );

            ApiResponse<LoginStartResp> startApiResponse = apiClient.sendPostRequest(
                    startReq,
                    startEndpoint,
                    null,
                    null,
                    null,
                    LoginStartResp.class
            );

            LoginStartResp startResp = startApiResponse.getBody();

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

            LoginCompleteReq completeReq = new LoginCompleteReq(
                    email,
                    deviceId,
                    Base64.getEncoder().encodeToString(M1.toByteArray())
            );

            String requestId = startApiResponse.getHeaders()
                    .firstValue("X-Request-ID")
                    .orElse(null);
            Map<String, String> completeReqHeaders = new HashMap<>();
            completeReqHeaders.put("X-Request-ID", requestId);

            LoginCompleteResp completeResp = apiClient.sendPostRequest(
                    completeReq,
                    completeEndpoint,
                    null,
                    null,
                    completeReqHeaders,
                    LoginCompleteResp.class
            ).getBody();

            BigInteger clientM2 = generateM2(digest, ctx.A(), B, S, M1);
            if (!clientM2.equals(new BigInteger(1, Base64.getDecoder().decode(completeResp.getM2())))) {
                throw new SdkException(500, "POST", "M2 mismatch, cannot verify server authenticity", completeEndpoint);
            }

            SecretKey masterPasswordKey = EncryptionHelper.generateMasterPasswordKey(ctx.passwordBytes(), Base64.getDecoder().decode(completeResp.getSaltKey()));
            byte[] vaultKey = EncryptionHelper.decryptField(Base64.getDecoder().decode(completeResp.getVaultKey()), Base64.getDecoder().decode(completeResp.getIvVaultKey()), masterPasswordKey);

            return new LoginSuccessObject(
                    vaultKey,
                    completeResp.getAccessToken(),
                    completeResp.getRefreshToken(),
                    completeResp.getRefreshTokenExpiryTime()
            );
        } catch (SdkException e) {
            throw e;
        } catch (Exception e) {
            throw new SdkException(500, "POST", "Could not login", completeEndpoint);
        }
    }

    @Override
    public RefreshResp refreshJwt(String refreshToken) {
        String endpoint = baseEndpoint + "/refresh";

        RefreshReq req = new RefreshReq(refreshToken);

        return apiClient.sendPostRequest(
                req,
                endpoint,
                null,
                null,
                null,
                RefreshResp.class
        ).getBody();
    }

    @Override
    public void logout(String refreshToken) {
        String endpoint = baseEndpoint + "/logout";

        RefreshReq req = new RefreshReq(refreshToken);

        apiClient.sendPostRequest(
                req,
                endpoint,
                null,
                null,
                null,
                null
        );
    }
}
