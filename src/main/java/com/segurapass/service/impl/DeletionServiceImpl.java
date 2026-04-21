package com.segurapass.service.impl;

import com.segurapass.api.ApiClient;
import com.segurapass.model.deletion.AuthorizedDeletionCompleteReq;
import com.segurapass.model.deletion.AuthorizedDeletionStartReq;
import com.segurapass.model.deletion.AuthorizedDeletionStartResp;
import com.segurapass.model.deletion.EmailDeletionStartReq;
import com.segurapass.service.DeletionService;
import com.segurapass.api.ApiResponse;
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
import java.util.function.Supplier;

public class DeletionServiceImpl implements DeletionService {

    private final ApiClient apiClient;
    private final Supplier<String> jwtSupplier;
    private final SRP6GroupParameters group;
    private final String baseEndpoint;

    public DeletionServiceImpl(ApiClient apiClient, Supplier<String> jwtSupplier) {
        this.apiClient = apiClient;
        this.jwtSupplier = jwtSupplier;
        this.group = SRP6StandardGroups.rfc5054_3072;
        this.baseEndpoint = "/api/deletion";
    }

    @Override
    public void emailDeletion(String email) {
        String endpoint = baseEndpoint + "/email/start";

        EmailDeletionStartReq req = new EmailDeletionStartReq(email);

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
    public void authorizedDeletion(String email, String masterPassword, UUID deviceId) {
        String startEndpoint = baseEndpoint + "/authorized/start";
        String endEndpoint = baseEndpoint + "/authorized/end";

        Digest digest = new SHA256Digest();
        SecureRandom random = new SecureRandom();

        BigInteger a = new BigInteger(256, random);
        BigInteger A = group.getG().modPow(a, group.getN());

        AuthorizedDeletionStartReq startReq = new AuthorizedDeletionStartReq(
                deviceId,
                Base64.getEncoder().encodeToString(A.toByteArray())
        );

        ApiResponse<AuthorizedDeletionStartResp> startApiResponse = apiClient.sendPostRequest(
                startReq,
                startEndpoint,
                null,
                jwtSupplier.get(),
                null,
                AuthorizedDeletionStartResp.class
        );

        AuthorizedDeletionStartResp startResp = startApiResponse.getBody();

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

        AuthorizedDeletionCompleteReq completeReq = new AuthorizedDeletionCompleteReq(
                deviceId,
                Base64.getEncoder().encodeToString(M1.toByteArray())
        );

        String requestId = startApiResponse.getHeaders()
                .firstValue("X-Request-ID")
                .orElse(null);
        Map<String, String> completeReqHeaders = new HashMap<>();
        completeReqHeaders.put("X-Request-ID", requestId);

        apiClient.sendPostRequest(
                completeReq,
                endEndpoint,
                null,
                jwtSupplier.get(),
                completeReqHeaders,
                null
        );
    }
}
