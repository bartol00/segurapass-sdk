package com.segurapass.service.impl;

import com.segurapass.helpers.SrpLoginSession;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import xyz.segurapass.api.deletion.*;
import com.segurapass.api.ApiClient;
import com.segurapass.service.DeletionService;
import com.segurapass.api.ApiResponse;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static com.segurapass.helpers.SrpHelper.*;

public class DeletionServiceImpl implements DeletionService {

    private final ApiClient apiClient;
    private final Supplier<String> jwtSupplier;
    private final SRP6GroupParameters group;
    private final String baseEndpoint;
    private final SecureRandom random = new SecureRandom();

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
    public void authorizedDeletion(String email, char[] masterPassword, UUID deviceId) {
        String startEndpoint = baseEndpoint + "/authorized/start";
        String endEndpoint = baseEndpoint + "/authorized/end";

        Digest digest = new SHA256Digest();

        try(SrpLoginSession ctx = SrpLoginSession.create(random, masterPassword, group)) {
            AuthorizedDeletionStartReq startReq = new AuthorizedDeletionStartReq(
                    deviceId,
                    Base64.getEncoder().encodeToString(ctx.A().toByteArray())
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
}
