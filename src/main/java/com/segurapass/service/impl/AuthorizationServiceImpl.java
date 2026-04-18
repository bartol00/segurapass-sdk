package com.segurapass.service.impl;

import com.segurapass.service.api.ApiClient;
import com.segurapass.exception.SdkException;
import com.segurapass.model.authorization.*;
import com.segurapass.service.AuthorizationService;
import com.segurapass.service.api.ApiResponse;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthorizationServiceImpl implements AuthorizationService {

    private final ApiClient apiClient;
    private final SRP6GroupParameters group;
    private final String baseEndpoint;

    public AuthorizationServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.group = SRP6StandardGroups.rfc5054_3072;
        this.baseEndpoint = "/api/authorization";
    }

    @Override
    public void register(String email, String masterPassword, UUID deviceId) {
        String endpoint = baseEndpoint + "/register";

        Digest digest = new SHA256Digest();
        SecureRandom random = new SecureRandom();

        byte[] saltKey = new byte[16];
        random.nextBytes(saltKey);

        byte[] saltAuth = new byte[16];
        random.nextBytes(saltAuth);

        BigInteger x = SRP6Util.calculateX(
                digest,
                group.getN(),
                saltAuth,
                email.getBytes(StandardCharsets.UTF_8),
                masterPassword.getBytes(StandardCharsets.UTF_8)
        );

        BigInteger verifier = group.getG().modPow(x, group.getN());

        if (verifier.compareTo(BigInteger.ONE) < 0 || verifier.compareTo(group.getN()) >= 0) {
            throw new SdkException(500, "POST", "Invalid SRP verifier computed. Please try registering again", endpoint);
        }

        if (verifier.bitLength() < group.getN().bitLength() / 2) {
            throw new SdkException(500, "POST", "Verifier is too small, which is a possible RNG issue. Please try registering again", endpoint);
        }

        RegistrationReq req = new RegistrationReq(
                email,
                Base64.getEncoder().encodeToString(saltAuth),
                Base64.getEncoder().encodeToString(verifier.toByteArray()),
                Base64.getEncoder().encodeToString(saltKey),
                deviceId
        );

        apiClient.sendPostRequest(
                req,
                endpoint,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public LoginCompleteResp login(String email, String masterPassword, UUID deviceId) {
        String startEndpoint = baseEndpoint + "/login/start";
        String completeEndpoint = baseEndpoint + "/login/end";

        Digest digest = new SHA256Digest();
        SecureRandom random = new SecureRandom();

        BigInteger a = new BigInteger(256, random);
        BigInteger A = group.getG().modPow(a, group.getN());

        LoginStartReq startReq = new LoginStartReq(
                email,
                deviceId,
                Base64.getEncoder().encodeToString(A.toByteArray())
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

        BigInteger x = SRP6Util.calculateX(digest, group.getN(), saltAuth,
                email.getBytes(StandardCharsets.UTF_8),
                masterPassword.getBytes(StandardCharsets.UTF_8));

        BigInteger u = SRP6Util.calculateU(digest, group.getN(), A, B);

        BigInteger k = SRP6Util.calculateK(digest, group.getN(), group.getG());
        BigInteger S = B.subtract(k.multiply(group.getG().modPow(x, group.getN())))
                .modPow(a.add(u.multiply(x)), group.getN());
        byte[] K = new byte[digest.getDigestSize()];
        digest.update(S.toByteArray(), 0, S.toByteArray().length);
        digest.doFinal(K, 0);

        BigInteger M1 = SRP6Util.calculateM1(digest, group.getN(), A, B, S);

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

        BigInteger M2_client = SRP6Util.calculateM2(digest, A, M1, S, B);

        if (!M2_client.equals(new BigInteger(1, Base64.getDecoder().decode(completeResp.getM2())))) {
            throw new SdkException(500, "POST", "M2 mismatch, cannot verify server authenticity", completeEndpoint);
        }

        return completeResp;
    }

    @Override
    public RefreshResp refreshJwt(String email, UUID deviceId, String refreshToken) {
        String endpoint = baseEndpoint + "/refresh";

        RefreshReq req = new RefreshReq(
                email,
                deviceId,
                refreshToken
        );

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
    public void logout(String email, UUID deviceId, String refreshToken) {
        String endpoint = baseEndpoint + "/logout";

        RefreshReq req = new RefreshReq(
                email,
                deviceId,
                refreshToken
        );

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
