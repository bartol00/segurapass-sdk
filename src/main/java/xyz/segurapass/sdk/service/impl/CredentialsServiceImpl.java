package xyz.segurapass.sdk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.helpers.CredentialsObject;
import xyz.segurapass.sdk.helpers.EncryptionHelper;
import xyz.segurapass.sdk.helpers.JsonHelper;
import xyz.segurapass.sdk.models.DecryptedCredential;
import xyz.segurapass.sdk.models.DecryptedCredentials;
import xyz.segurapass.api.credentials.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.segurapass.api.ApiClient;
import xyz.segurapass.sdk.service.CredentialsService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.function.Supplier;

public class CredentialsServiceImpl implements CredentialsService {

    private final ApiClient apiClient;
    private final Supplier<String> jwtSupplier;
    private final String baseEndpoint;
    private final SecureRandom random = new SecureRandom();

    public CredentialsServiceImpl(ApiClient apiClient, Supplier<String> jwtSupplier) {
        this.apiClient = apiClient;
        this.jwtSupplier = jwtSupplier;
        this.baseEndpoint = "/api/credentials";
    }

    @Override
    public DecryptedCredentials getCredentials(int page, int size, byte[] vaultKeyBytes) {
        String endpoint = baseEndpoint + "/get";

        try(CredentialsObject ctx = new CredentialsObject(vaultKeyBytes)) {

            TypeReference<PagedResponse<CredentialsRespSdk>> type =
                    new TypeReference<>() {};

            PagedResponse<CredentialsRespSdk> response;
            List<CredentialsRespSdk> encryptedCredentials = new ArrayList<>();

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            do {
                Map<String, String> params = new HashMap<>();
                params.put("page", String.valueOf(page));
                params.put("size", String.valueOf(size));

                response = apiClient.sendGetRequest(
                        endpoint,
                        params,
                        headers,
                        type
                ).body();

                if (response == null || response.getContent() == null) {
                    break;
                }

                encryptedCredentials.addAll(response.getContent());

                page++;
            } while (page < response.getTotalPages());

            return decryptCredentials(encryptedCredentials, ctx.vaultKey(), endpoint);

        }
    }

    @Override
    public DecryptedCredential addCredential(
            String website,
            String username,
            String password,
            byte[] vaultKeyBytes,
            PrivateKey signingKey
    ) {
        String startEndpoint = baseEndpoint + "/create/start";
        String endEndpoint = baseEndpoint + "/create/end";

        try(CredentialsObject ctx = new CredentialsObject(vaultKeyBytes)) {

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            NonceResp nonceResp = apiClient.sendGetRequest(
                    startEndpoint,
                    null,
                    headers,
                    NonceResp.class
            ).body();

            CredentialsReq req = encryptCredentials(
                    website,
                    username,
                    password,
                    ctx.vaultKey(),
                    endEndpoint
            );
            req.setNonce(nonceResp.getNonce());
            req.setOperation(CredentialsOperation.CREATE);

            headers.put("X-SeguraPass-Signature", createSignature(req, null, signingKey));

            CredentialsRespSdk credentialsRespSdk = apiClient.sendPostRequest(
                    req,
                    endEndpoint,
                    null,
                    headers,
                    CredentialsRespSdk.class
            ).body();

            return new DecryptedCredential(
                    credentialsRespSdk.getCredentialsId(),
                    website,
                    username,
                    password,
                    credentialsRespSdk.getCreatedAt(),
                    credentialsRespSdk.getLastUpdated(),
                    false
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DecryptedCredential updateCredential(
            String credentialId,
            String website,
            String username,
            String password,
            byte[] vaultKeyBytes
    ) {
        String endpoint = baseEndpoint + "/update/" + credentialId;

        try(CredentialsObject ctx = new CredentialsObject(vaultKeyBytes)) {

            CredentialsReq req = encryptCredentials(
                    website,
                    username,
                    password,
                    ctx.vaultKey(),
                    endpoint
            );

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtSupplier.get());

            CredentialsRespSdk credentialsRespSdk = apiClient.sendPutRequest(
                    req,
                    endpoint,
                    null,
                    headers,
                    CredentialsRespSdk.class
            ).body();

            return new DecryptedCredential(
                    credentialsRespSdk.getCredentialsId(),
                    website,
                    username,
                    password,
                    credentialsRespSdk.getCreatedAt(),
                    credentialsRespSdk.getLastUpdated(),
                    false
            );

        }
    }

    @Override
    public void deleteCredential(String credentialId) {
        String endpoint = baseEndpoint + "/delete/" + credentialId;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + jwtSupplier.get());

        apiClient.sendDeleteRequest(
                endpoint,
                null,
                headers,
                null
        );
    }

    private DecryptedCredentials decryptCredentials(
            List<CredentialsRespSdk> encryptedCredentials,
            byte[] vaultKeyBytes,
            String endpoint
    ) {
        SecretKey vaultKey = EncryptionHelper.generateKeyFromBytes(vaultKeyBytes);
        List<DecryptedCredential> credentials = new ArrayList<>();

        try {

            for (CredentialsRespSdk encryptedCredential : encryptedCredentials) {
                String encryptedWebsite = encryptedCredential.getWebsite();
                String websiteIv = encryptedCredential.getIvWebsite();
                String encryptedUsername = encryptedCredential.getUsername();
                String usernameIv = encryptedCredential.getIvUsername();
                String encryptedPassword = encryptedCredential.getPassword();
                String passwordIv = encryptedCredential.getIvPassword();

                byte[] plaintextWebsite = EncryptionHelper.decryptField(
                        Base64.getDecoder().decode(encryptedWebsite),
                        Base64.getDecoder().decode(websiteIv),
                        vaultKey
                );
                byte[] plaintextUsername = EncryptionHelper.decryptField(
                        Base64.getDecoder().decode(encryptedUsername),
                        Base64.getDecoder().decode(usernameIv),
                        vaultKey
                );
                byte[] plaintextPassword = EncryptionHelper.decryptField(
                        Base64.getDecoder().decode(encryptedPassword),
                        Base64.getDecoder().decode(passwordIv),
                        vaultKey
                );
                DecryptedCredential decryptedCredential = new DecryptedCredential(
                        encryptedCredential.getCredentialsId(),
                        new String(plaintextWebsite, StandardCharsets.UTF_8),
                        new String(plaintextUsername, StandardCharsets.UTF_8),
                        new String(plaintextPassword, StandardCharsets.UTF_8),
                        encryptedCredential.getCreatedAt(),
                        encryptedCredential.getLastUpdated(),
                        false
                );
                credentials.add(decryptedCredential);
            }

            return new DecryptedCredentials(credentials);

        } catch (Exception e) {
            throw new SegurapassSdkException(500, "GET", "Could not decrypt credentials", endpoint);
        }
    }

    private CredentialsReq encryptCredentials(
            String website,
            String username,
            String password,
            byte[] vaultKeyBytes,
            String endpoint
    ) {
        SecretKey vaultKey = EncryptionHelper.generateKeyFromBytes(vaultKeyBytes);

        byte[] websiteIv = new byte[12];
        byte[] usernameIv = new byte[12];
        byte[] passwordIv = new byte[12];

        random.nextBytes(websiteIv);
        random.nextBytes(usernameIv);
        random.nextBytes(passwordIv);

        try {

            CredentialsReq credentialsReq = new CredentialsReq();

            if (website != null && !website.isBlank()) {
                byte[] encryptedWebsite = EncryptionHelper.encryptField(
                        website.getBytes(StandardCharsets.UTF_8),
                        websiteIv,
                        vaultKey
                );
                credentialsReq.setWebsite(Base64.getEncoder().encodeToString(encryptedWebsite));
                credentialsReq.setIvWebsite(Base64.getEncoder().encodeToString(websiteIv));
            }

            if (username != null && !username.isBlank()) {
                byte[] encryptedUsername = EncryptionHelper.encryptField(
                        username.getBytes(StandardCharsets.UTF_8),
                        usernameIv,
                        vaultKey
                );
                credentialsReq.setUsername(Base64.getEncoder().encodeToString(encryptedUsername));
                credentialsReq.setIvUsername(Base64.getEncoder().encodeToString(usernameIv));
            }

            if (password != null && !password.isBlank()) {
                byte[] encryptedPassword = EncryptionHelper.encryptField(
                        password.getBytes(StandardCharsets.UTF_8),
                        passwordIv,
                        vaultKey
                );
                credentialsReq.setPassword(Base64.getEncoder().encodeToString(encryptedPassword));
                credentialsReq.setIvPassword(Base64.getEncoder().encodeToString(passwordIv));
            }

            return credentialsReq;

        } catch (Exception e) {
            throw new SegurapassSdkException(500, "POST", "Could not encrypt credentials", endpoint);
        }
    }

    private String createSignature(CredentialsReq req, UUID credentialsId, PrivateKey privateKey)
            throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        CredentialsWritePayload payload =
                new CredentialsWritePayload(
                        req.getWebsite(),
                        req.getUsername(),
                        req.getPassword(),
                        req.getIvWebsite(),
                        req.getIvUsername(),
                        req.getIvPassword(),
                        req.getNonce(),
                        req.getOperation(),
                        credentialsId
                );

        byte[] payloadBytes = JsonHelper.OBJECT_MAPPER.writeValueAsBytes(payload);

        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(privateKey);
        signer.update(payloadBytes);

        byte[] signatureBytes = signer.sign();

        return Base64.getEncoder().encodeToString(signatureBytes);
    }
}
