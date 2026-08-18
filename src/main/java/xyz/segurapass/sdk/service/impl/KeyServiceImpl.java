package xyz.segurapass.sdk.service.impl;

import io.jsonwebtoken.Jwts;
import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.api.key.PublicKeyResp;
import com.segurapass.api.ApiClient;
import com.segurapass.api.ApiResponse;
import xyz.segurapass.sdk.service.KeyService;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyServiceImpl implements KeyService {

    private final ApiClient apiClient;
    private final String baseEndpoint;

    public KeyServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.baseEndpoint = "/.well-known";
    }

    @Override
    public PublicKey getPublicKey() {
        String endpoint = baseEndpoint + "/public-key";

        ApiResponse<PublicKeyResp> publicKeyResp = apiClient.sendGetRequest(
                endpoint,
                null,
                null,
                PublicKeyResp.class
        );

        return parsePublicKey(publicKeyResp.body().getPublicKey());
    }

    @Override
    public boolean isValid(String token, PublicKey publicKey) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private PublicKey parsePublicKey(String publicKeyString) throws SegurapassSdkException {
        try {

            String cleanPem = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("Ed25519").generatePublic(spec);

        } catch (Exception e) {
            throw new SegurapassSdkException(
                    500,
                    "GET",
                    "Failed to parse public key",
                    "/.well-known/public-key"
            );
        }
    }

}
