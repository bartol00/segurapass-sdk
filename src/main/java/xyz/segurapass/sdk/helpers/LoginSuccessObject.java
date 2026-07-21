package xyz.segurapass.sdk.helpers;

import lombok.Getter;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;

public final class LoginSuccessObject {

    private byte[] vaultKey;
    private PrivateKey privateSigningKey;
    private PublicKey publicSigningKey;
    @Getter
    private final String accessToken;
    @Getter
    private final String refreshToken;
    @Getter
    private final Instant refreshTokenExpiryTime;

    public LoginSuccessObject(byte[] vaultKey,
                              PrivateKey privateSigningKey,
                              PublicKey publicSigningKey,
                              String accessToken,
                              String refreshToken,
                              Instant refreshTokenExpiryTime) {
        this.vaultKey = vaultKey;
        this.privateSigningKey = privateSigningKey;
        this.publicSigningKey = publicSigningKey;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryTime = refreshTokenExpiryTime;
    }

    public byte[] getVaultKey() {
        return vaultKey == null ? null : Arrays.copyOf(vaultKey, vaultKey.length);
    }

    public void destroy() {
        if (vaultKey != null) {
            Arrays.fill(vaultKey, (byte) 0);
            vaultKey = null;
        }
        if (privateSigningKey != null) {
            privateSigningKey = null;
        }
        if (publicSigningKey != null) {
            publicSigningKey = null;
        }
    }
}
