package xyz.segurapass.sdk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.segurapass.api.ApiClient;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.params.SRP6GroupParameters;
import xyz.segurapass.api.authorization.LoginCompleteResp;
import xyz.segurapass.api.credentials.NonceResp;
import xyz.segurapass.api.mfa.*;
import xyz.segurapass.sdk.helpers.JsonHelper;
import xyz.segurapass.sdk.helpers.LoginSuccessObject;
import xyz.segurapass.sdk.helpers.SrpLoginSession;
import xyz.segurapass.sdk.service.TotpService;

import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static xyz.segurapass.sdk.helpers.LoginHelper.*;

public class TotpServiceImpl implements TotpService {

    private final ApiClient apiClient;
    private final Supplier<String> jwtSupplier;
    private final SRP6GroupParameters group;
    private final String baseEndpoint;
    private final SecureRandom random = new SecureRandom();

    public TotpServiceImpl(ApiClient apiClient, Supplier<String> jwtSupplier) {
        this.apiClient = apiClient;
        this.jwtSupplier = jwtSupplier;
        this.group = SRP6StandardGroups.rfc5054_3072;
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
    public LoginSuccessObject loginTotp(String code, String otp, char[] masterPassword) {
        String endpoint = baseEndpoint + "/login-totp/" + code;

        try(SrpLoginSession ctx = SrpLoginSession.create(random, masterPassword, group)) {

            TotpVerifyReq req = new TotpVerifyReq(otp);

            LoginCompleteResp resp = apiClient.sendPostRequest(
                    req,
                    endpoint,
                    null,
                    null,
                    LoginCompleteResp.class
            ).body();

            return getLoginSuccessObject(
                    ctx,
                    resp
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LoginSuccessObject recoveryTotp(String code, String recoveryCode, char[] masterPassword) {
        String endpoint = baseEndpoint + "/recovery-totp/" + code;

        try(SrpLoginSession ctx = SrpLoginSession.create(random, masterPassword, group)) {

            TotpRecoveryReq req = new TotpRecoveryReq(recoveryCode);

            LoginCompleteResp resp = apiClient.sendPostRequest(
                    req,
                    endpoint,
                    null,
                    null,
                    LoginCompleteResp.class
            ).body();

            return getLoginSuccessObject(
                    ctx,
                    resp
            );

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
