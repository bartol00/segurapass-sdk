package xyz.segurapass.sdk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.segurapass.api.ApiClient;
import xyz.segurapass.api.authorization.LoginCompleteResp;
import xyz.segurapass.api.credentials.NonceResp;
import xyz.segurapass.api.mfa.*;
import xyz.segurapass.sdk.helpers.JsonHelper;
import xyz.segurapass.sdk.service.TotpService;

import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TotpServiceImpl implements TotpService {

    private final ApiClient apiClient;
    private final Supplier<String> jwtSupplier;
    private final String baseEndpoint;

    public TotpServiceImpl(ApiClient apiClient, Supplier<String> jwtSupplier) {
        this.apiClient = apiClient;
        this.jwtSupplier = jwtSupplier;
        this.baseEndpoint = "/api/mfa";
    }

    @Override
    public TotpResp addTotp(PrivateKey signingKey) {
        String startEndpoint = baseEndpoint + "/add-totp/start";
        String endEndpoint = baseEndpoint + "/add-totp/end";

        try {

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            NonceResp nonceResp = apiClient.sendGetRequest(
                    startEndpoint,
                    null,
                    headers,
                    NonceResp.class
            ).body();

            TotpReq totpReq = new TotpReq(
                    MfaType.TOTP_ADD,
                    nonceResp.getNonce()
            );

            headers.put("X-SeguraPass-Signature", createSignature(totpReq, signingKey));

            return apiClient.sendPostRequest(
                    totpReq,
                    endEndpoint,
                    null,
                    headers,
                    TotpResp.class
            ).body();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeTotp(PrivateKey signingKey) {
        String startEndpoint = baseEndpoint + "/remove-totp/start";
        String endEndpoint = baseEndpoint + "/remove-totp/end";

        try {

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            NonceResp nonceResp = apiClient.sendGetRequest(
                    startEndpoint,
                    null,
                    headers,
                    NonceResp.class
            ).body();

            TotpReq totpReq = new TotpReq(
                    MfaType.TOTP_REMOVE,
                    nonceResp.getNonce()
            );

            headers.put("X-SeguraPass-Signature", createSignature(totpReq, signingKey));

            apiClient.sendPostRequest(
                    totpReq,
                    endEndpoint,
                    null,
                    headers,
                    TotpResp.class
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String verifyTotp(String otp) {
        String endpoint = baseEndpoint + "/verify-totp";

        try {

            TotpVerifyReq req = new TotpVerifyReq(otp);

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            return apiClient.sendPostRequest(
                    req,
                    endpoint,
                    null,
                    headers,
                    TotpVerifyResp.class
            ).body().getRecoveryCode();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginCompleteResp loginTotp(String code, String otp) {
        String endpoint = baseEndpoint + "/login-totp/" + code;

        try {

            TotpVerifyReq req = new TotpVerifyReq(otp);

            return apiClient.sendPostRequest(
                    req,
                    endpoint,
                    null,
                    null,
                    LoginCompleteResp.class
            ).body();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginCompleteResp recoveryTotp(String code, String recoveryCode) {
        String endpoint = baseEndpoint + "/recovery-totp/" + code;

        try {

            TotpRecoveryReq req = new TotpRecoveryReq(recoveryCode);

            return apiClient.sendPostRequest(
                    req,
                    endpoint,
                    null,
                    null,
                    LoginCompleteResp.class
            ).body();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createSignature(TotpReq req, PrivateKey privateKey)
            throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        TotpPayload payload =
                new TotpPayload(
                        req.getMfaType(),
                        req.getNonce()
                );

        byte[] payloadBytes = JsonHelper.OBJECT_MAPPER.writeValueAsBytes(payload);

        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(privateKey);
        signer.update(payloadBytes);

        byte[] signatureBytes = signer.sign();

        return Base64.getEncoder().encodeToString(signatureBytes);
    }

}
