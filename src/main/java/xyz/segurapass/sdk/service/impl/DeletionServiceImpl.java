package xyz.segurapass.sdk.service.impl;

import xyz.segurapass.sdk.helpers.SrpLoginSession;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import xyz.segurapass.api.deletion.*;
import com.segurapass.api.ApiClient;
import xyz.segurapass.sdk.service.DeletionService;
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

import static xyz.segurapass.sdk.helpers.SrpHelper.*;

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

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            ApiResponse<AuthorizedDeletionStartResp> startApiResponse = apiClient.sendPostRequest(
                    startReq,
                    startEndpoint,
                    null,
                    headers,
                    AuthorizedDeletionStartResp.class
            );

            AuthorizedDeletionStartResp startResp = startApiResponse.body();

            BigInteger B = new BigInteger(1, Base64.getDecoder().decode(startResp.getB()));

            validatePublicValue(ctx.A(), "A", startEndpoint);
            validatePublicValue(B, "B", startEndpoint);

            BigInteger x = generateX(
                    digest,
                    startResp.getSaltAuth(),
                    email.getBytes(StandardCharsets.UTF_8),
                    ctx.passwordBytes()
            );
            BigInteger S = generateS(digest, x, ctx.a(), ctx.A(), B);
            BigInteger M1 = generateM1(digest, ctx.A(), B, S);

            AuthorizedDeletionCompleteReq completeReq = new AuthorizedDeletionCompleteReq(
                    deviceId,
                    Base64.getEncoder().encodeToString(M1.toByteArray())
            );

            String requestId = startApiResponse.headers()
                    .firstValue("X-Request-ID")
                    .orElse(null);
            Map<String, String> completeReqHeaders = new HashMap<>();
            completeReqHeaders.put("X-Request-ID", requestId);
            completeReqHeaders.put("Authorization", "Bearer " + jwtSupplier.get());

            apiClient.sendPostRequest(
                    completeReq,
                    endEndpoint,
                    null,
                    completeReqHeaders,
                    null
            );
        }
    }
}
